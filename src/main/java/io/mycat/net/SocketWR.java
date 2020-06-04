package io.mycat.net;

import java.io.IOException;


public abstract class SocketWR {

	/**
	 * 异步读取
	 * @throws IOException
	 */
	public abstract void asynRead() throws IOException;

	/**
	 * 做下次写检查
	 */
	public abstract void doNextWriteCheck() ;

	/**
	 * 检查该通道连接是否关闭
	 * @return
	 */
	public abstract boolean checkAlive();

	/**
	 * 停止读取
	 */
	public abstract void disableRead();

	/**
	 * 启用读取
	 */
	public abstract void enableRead();
}
