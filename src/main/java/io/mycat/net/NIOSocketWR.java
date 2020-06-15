package io.mycat.net;

import io.mycat.MycatServer;
import io.mycat.config.FlowCotrollerConfig;
import io.mycat.config.model.SystemConfig;
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
        int writeQueueSizeCount = -1;
        int written = 0;
        ByteBuffer buffer = abstractConnection.writeBuffer;
        // buffer不为空说明写缓冲记录中还有数据,客户端还未读取
        if (buffer != null) {
            //只要写缓冲记录中还有数据就不停写入
            while (buffer.hasRemaining()) {
                written = channel.write(buffer);
                // 如果写入字节为0，证明阻塞，则退出
                if (written > 0) {
                    abstractConnection.netOutBytes += written;
                    abstractConnection.processor.addNetOutBytes(written);
                    abstractConnection.lastWriteTime = TimeUtil.currentTimeMillis();
                } else {
                    break;
                }
            }

            /**
             * 如果开启了流式查询，则返回当前写队列大小
             */
            writeQueueSizeCount = checkWriteQueueSize(writeQueueSizeCount);

            //如果写缓冲中还有数据证明网络繁忙或阻塞，退出，否则清空缓冲
            if (buffer.hasRemaining()) {
                return false;
            } else {
                abstractConnection.writeBuffer = null;
                abstractConnection.recycle(buffer);
            }
        }


        //读取缓存队列buffer
        while ((buffer = abstractConnection.writeQueue.poll()) != null) {
            if (buffer.limit() == 0) {
                abstractConnection.recycle(buffer);
                abstractConnection.close("quit send");
                return true;
            }

            buffer.flip();
            try {
                //如果写缓存队列buffer中还有数据证明网络繁忙，计数，记录下这次未写完的数据到写缓冲记录并退出，否则回收缓冲
                // 写缓存队列buffer中还有数据就不停写入
                while (buffer.hasRemaining()) {
                    written = channel.write(buffer);
                    // 如果写入字节为0，证明阻塞，则退出
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

            /**
             * 检查当前队列大小是否达到可停止值
             */
            writeQueueSizeCount = checkWriteQueueSize(writeQueueSizeCount);

            // 如果写队列buffer中还有数据，说明阻塞，记录下这次未写完的数据到写缓冲记录并退出
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
     * 检查当前写队列大小是否可以停止流式控制
     *
     * @param writeQueueSizeCount
     * @return
     */
    private int checkWriteQueueSize(int writeQueueSizeCount) {
        SystemConfig systemConfig = MycatServer.getInstance().getConfig().getSystem();
        FlowCotrollerConfig config = new FlowCotrollerConfig(systemConfig.isEnableFlowControl(),systemConfig.getFlowControlStartMaxValue(),systemConfig.getFlowControlStopMaxValue());
        // 如果配置了开启流式查询控制则进行检查，否则不作处理
        if (config.isEnableFlowControl()) {
            if ((writeQueueSizeCount != -1) && (writeQueueSizeCount <= config.getStop())) {
                // 获取当前写队列大小
                int writeQueueSize = this.abstractConnection.writeQueue.size();
                // 达到停止条件时,停止流式控制，否则返回当前写队列大小
                if (writeQueueSize <= config.getStop()) {
                    abstractConnection.stopFlowControl();
                    return -1;
                } else {
                    return writeQueueSize;
                }
            // 默认值，即第一次调用一定进入
            } else if (writeQueueSizeCount == -1) {
                // 获取当前写队列大小
                int writeQueueSize = this.abstractConnection.writeQueue.size();
                // 达到停止条件时,停止流式控制，否则返回当前写队列大小
                if (writeQueueSize <= config.getStop()) {
                    abstractConnection.stopFlowControl();
                    return -1;
                } else {
                    return writeQueueSize;
                }
            // 未达到停止条件
            } else {
                return --writeQueueSizeCount;
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
