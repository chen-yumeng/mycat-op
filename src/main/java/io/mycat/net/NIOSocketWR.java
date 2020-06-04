package io.mycat.net;

import io.mycat.config.FlowCotrollerConfig;
import io.mycat.sqlengine.WriteQueueFlowController;
import io.mycat.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 每个前端和后端连接都有一个对应的缓冲区，对连接读写操作具体如何操作的方法和缓存方式，封装到了这个类里面。
 */
public class NIOSocketWR extends SocketWR {
    private SelectionKey processKey;
    private static final int OP_NOT_READ = ~SelectionKey.OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;
    private final AbstractConnection abstractConnection;
    private final SocketChannel channel;
    private final AtomicBoolean writing = new AtomicBoolean(false);
    protected static final Logger LOGGER = LoggerFactory.getLogger(NIOSocketWR.class);
    public static final ByteBuffer EMPTY_BYTEBUFFER = ByteBuffer.allocate(1);

    public NIOSocketWR(AbstractConnection abstractConnection) {
        this.abstractConnection = abstractConnection;
        this.channel = (SocketChannel) abstractConnection.channel;
    }

    public void register(Selector selector) throws IOException {
        try {
            processKey = channel.register(selector, SelectionKey.OP_READ, abstractConnection);
        } finally {
            if (abstractConnection.isClosed.get()) {
                clearSelectionKey();
            }
        }
    }

    @Override
    public void doNextWriteCheck() {
        //检查是否正在写,看CAS更新writing值是否成功
        if (!writing.compareAndSet(false, true)) {
            return;
        }

        try {
            if (!channel.isOpen()) {
                AbstractConnection.LOGGER.debug("caught err: {}", abstractConnection);
            }
            //利用缓存队列和写缓冲记录保证写的可靠性，返回true则为全部写入成功
            boolean noMoreData = write0();
            //因为只有一个线程可以成功CAS更新writing值，所以这里不用再CAS
            writing.set(false);
            //如果全部写入成功而且写入队列为空（有可能在写入过程中又有新的Bytebuffer加入到队列），则取消注册写事件
            //否则，继续注册写事件
            if (noMoreData && abstractConnection.writeQueue.isEmpty()) {
                if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) != 0)) {
                    disableWrite();
                }
            } else {
                if ((processKey.isValid() && (processKey.interestOps() & SelectionKey.OP_WRITE) == 0)) {
                    enableWrite(false);
                }
            }

        } catch (IOException e) {
            if (AbstractConnection.LOGGER.isWarnEnabled()) {
                AbstractConnection.LOGGER.warn("caught err:", e);
            }
            abstractConnection.close("err:" + e);
        } finally {
            writing.set(false);
        }

    }

    @Override
    public boolean checkAlive() {
        try {
            return channel.read(EMPTY_BYTEBUFFER) == 0;
        } catch (IOException e) {
            LOGGER.error("", e);
            return false;
        } finally {
            EMPTY_BYTEBUFFER.position(0);
        }
    }

    private boolean write0() throws IOException {
        int flowControlCount = -1;
        int written = 0;
        ByteBuffer buffer = abstractConnection.writeBuffer;
        // buffer不为空说明写缓冲记录中还有数据,客户端还未读取
        if (buffer != null) {
            //只要写缓冲记录中还有数据就不停写入，但如果写入字节为0，证明阻塞，则退出
            while (buffer.hasRemaining()) {
                written = channel.write(buffer);
                if (written > 0) {
                    abstractConnection.netOutBytes += written;
                    abstractConnection.processor.addNetOutBytes(written);
                    abstractConnection.lastWriteTime = TimeUtil.currentTimeMillis();
                } else {
                    break;
                }
            }

            // 检查当前是否需要停止流式查询控制
            flowControlCount = checkFlowControl(flowControlCount);

            //如果写缓冲中还有数据证明网络繁忙或阻塞，退出，否则清空缓冲
            if (buffer.hasRemaining()) {
                return false;
            } else {
                abstractConnection.writeBuffer = null;
                abstractConnection.recycle(buffer);
            }
        }
        //读取缓存队列并写入通道
        while ((buffer = abstractConnection.writeQueue.poll()) != null) {
            if (buffer.limit() == 0) {
                abstractConnection.recycle(buffer);
                abstractConnection.close("quit send");
                return true;
            }

            buffer.flip();
            try {
                //如果写缓冲中还有数据证明网络繁忙，计数，记录下这次未写完的数据到写缓冲记录并退出，否则回收缓冲
                while (buffer.hasRemaining()) {
                    written = channel.write(buffer);
                    // java.io.IOException:
                    // Connection reset by peer
                    if (written > 0) {
                        abstractConnection.lastWriteTime = TimeUtil.currentTimeMillis();
                        abstractConnection.netOutBytes += written;
                        abstractConnection.processor.addNetOutBytes(written);
                        abstractConnection.lastWriteTime = TimeUtil.currentTimeMillis();
                        abstractConnection.writeAttempts = 0;
                    } else {
                        abstractConnection.writeAttempts++;
                        break;
                    }
                }
            } catch (IOException e) {
                abstractConnection.recycle(buffer);
                throw e;
            }

            // 检查当前是否需要停止流式查询控制
            flowControlCount = checkFlowControl(flowControlCount);

            if (buffer.hasRemaining()) {
                abstractConnection.writeBuffer = buffer;
                return false;
            } else {
                abstractConnection.recycle(buffer);
            }
        }
        return true;
    }

    /**
     * 检查是否需要停止流式控制控制
     *
     * @param flowControlCount
     * @return
     */
    private int checkFlowControl(int flowControlCount) {
        FlowCotrollerConfig config = WriteQueueFlowController.getFlowCotrollerConfig();
        // 如果配置了开启流式查询控制，进行检查，否则不作处理
        if (config.isEnableFlowControl()) {
            // 未开启流式查询
            if (!config.isEnableFlowControl()) {
                abstractConnection.stopFlowControl();
                return -1;
            } else if ((flowControlCount != -1) && (flowControlCount <= config.getEnd())) {
                int currentSize = this.abstractConnection.writeQueue.size();
                // 达到停止条件时
                if (currentSize <= config.getEnd()) {
                    abstractConnection.stopFlowControl();
                    return -1;
                } else {
                    return currentSize;
                }
            } else if (flowControlCount == -1) {
                int currentSize = this.abstractConnection.writeQueue.size();
                // 达到停止条件时
                if (currentSize <= config.getEnd()) {
                    abstractConnection.stopFlowControl();
                    return -1;
                } else {
                    return currentSize;
                }
            } else {
                return --flowControlCount;
            }
        } else {
            return -1;
        }
    }

    private void disableWrite() {
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } catch (Exception e) {
            AbstractConnection.LOGGER.warn("can't disable write " + e + " abstractConnection "
                    + abstractConnection);
        }

    }

    private void enableWrite(boolean wakeup) {
        boolean needWakeup = false;
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            needWakeup = true;
        } catch (Exception e) {
            AbstractConnection.LOGGER.warn("can't enable write " + e);

        }
        if (needWakeup && wakeup) {
            processKey.selector().wakeup();
        }
    }

    @Override
    public void disableRead() {
        SelectionKey key = this.processKey;
        key.interestOps(key.interestOps() & OP_NOT_READ);
    }

    @Override
    public void enableRead() {
        boolean needWakeup = false;
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            needWakeup = true;
        } catch (Exception e) {
            AbstractConnection.LOGGER.warn("enable read fail " + e);
        }
        if (needWakeup) {
            processKey.selector().wakeup();
        }
    }

    private void clearSelectionKey() {
        try {
            SelectionKey key = this.processKey;
            if (key != null && key.isValid()) {
                key.attach(null);
                key.cancel();
            }
        } catch (Exception e) {
            AbstractConnection.LOGGER.warn("clear selector keys err:" + e);
        }
    }

    @Override
    public void asynRead() throws IOException {
        ByteBuffer theBuffer = abstractConnection.readBuffer;
        if (theBuffer == null) {
            theBuffer = abstractConnection.processor.getBufferPool().allocate(abstractConnection.processor.getBufferPool().getChunkSize());
            abstractConnection.readBuffer = theBuffer;
        }
        //从channel中读取数据，并且保存到对应AbstractConnection的readBuffer中，readBuffer处于write mode，返回读取了多少字节
        int got = channel.read(theBuffer);
        //调用处理读取到的数据的方法
        abstractConnection.onReadData(got);
    }

}
