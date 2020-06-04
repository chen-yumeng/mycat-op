/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package io.mycat.net;

import io.mycat.MycatServer;
import io.mycat.util.SelectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作为客户端去连接后台数据库（MySql，后端NIO通信）
 * @author mycat
 */
public final class NIOConnector extends Thread implements SocketConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(NIOConnector.class);
	public static final ConnectIdGenerator ID_GENERATOR = new ConnectIdGenerator();

	private final String name;
	private volatile Selector selector;
	private final BlockingQueue<AbstractConnection> connectQueue;
	private long connectCount;
	private final NIOReactorPool reactorPool;

	public NIOConnector(String name, NIOReactorPool reactorPool)
			throws IOException {
		super.setName(name);
		this.name = name;
		this.selector = Selector.open();
		this.reactorPool = reactorPool;
		this.connectQueue = new LinkedBlockingQueue<AbstractConnection>();
	}

	public long getConnectCount() {
		return connectCount;
	}

	public void postConnect(AbstractConnection c) {
		connectQueue.offer(c);
		selector.wakeup();
	}

	@Override
	public void run() {
		int invalidSelectCount = 0;
		for (;;) {
			try {
				final Selector tSelector = this.selector;
				++connectCount;

				long start = System.nanoTime();
				//查看有无连接就绪
				tSelector.select(1000L);
				long end = System.nanoTime();
				connect(tSelector);
				Set<SelectionKey> keys = tSelector.selectedKeys();
				if (keys.size() == 0 && (end - start) < SelectorUtil.MIN_SELECT_TIME_IN_NANO_SECONDS )
				{
					invalidSelectCount++;
				}
				else
				{
					try {
						for (SelectionKey key : keys)
						{
							Object att = key.attachment();
							if (att != null && key.isValid() && key.isConnectable())
							{
								finishConnect(key, att);
							} else
							{
								key.cancel();
							}
						}
					} finally
					{
						invalidSelectCount = 0;
						keys.clear();
					}
				}
				if (invalidSelectCount > SelectorUtil.REBUILD_COUNT_THRESHOLD)
				{
					final Selector rebuildSelector = SelectorUtil.rebuildSelector(this.selector);
					if (rebuildSelector != null)
					{
						this.selector = rebuildSelector;
					}
					invalidSelectCount = 0;
				}
			} catch (Exception e) {
				LOGGER.warn(name, e);
			} catch (final Throwable e) {
				LOGGER.warn("caught Throwable err: ", e);
			}
		}
	}

	private void connect(Selector selector) {
		AbstractConnection abstractConnection = null;
		while ((abstractConnection = connectQueue.poll()) != null) {
			try {
				SocketChannel channel = (SocketChannel) abstractConnection.getChannel();
				//注册OP_CONNECT监听与后端连接是否真正建立
				channel.register(selector, SelectionKey.OP_CONNECT, abstractConnection);
				//主动连接
				channel.connect(new InetSocketAddress(abstractConnection.host, abstractConnection.port));
			} catch (Exception e) {
				LOGGER.error("error:",e);
				abstractConnection.close(e.toString());
			}
		}
	}

	private void finishConnect(SelectionKey key, Object att) {
		BackendAIOConnection aioConnection = (BackendAIOConnection) att;
		try {
			//做原生NIO连接是否完成的判断和操作
			if (finishConnect(aioConnection, (SocketChannel) aioConnection.channel)) {
				clearSelectionKey(key);
				aioConnection.setId(ID_GENERATOR.getId());
				//绑定特定的NIOProcessor以作idle清理
				NIOProcessor processor = MycatServer.getInstance().nextProcessor();
				aioConnection.setProcessor(processor);
				//与特定NIOReactor绑定监听读写
				NIOReactor reactor = reactorPool.getNextReactor();
				reactor.postRegister(aioConnection);
				aioConnection.onConnectfinish();
			}
		} catch (Exception e) {
			//如有异常，将key清空
			clearSelectionKey(key);
			LOGGER.error("error:",e);
			aioConnection.close(e.toString());
			aioConnection.onConnectFailed(e);

		}
	}

	private boolean finishConnect(AbstractConnection abstractConnection, SocketChannel channel)
			throws IOException {
		if (channel.isConnectionPending()) {
			channel.finishConnect();

			abstractConnection.setLocalPort(channel.socket().getLocalPort());
			return true;
		} else {
			return false;
		}
	}

	private void clearSelectionKey(SelectionKey key) {
		if (key.isValid()) {
			key.attach(null);
			key.cancel();
		}
	}

	/**
	 * 后端连接ID生成器
	 *
	 * @author mycat
	 */
	public static class ConnectIdGenerator {

		private static final long MAX_VALUE = Long.MAX_VALUE;
		private AtomicLong connectId = new AtomicLong(0);

		public long getId() {
			return connectId.incrementAndGet();
		}
	}

}
