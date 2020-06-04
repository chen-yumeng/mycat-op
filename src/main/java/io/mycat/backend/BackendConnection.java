package io.mycat.backend;

import io.mycat.backend.mysql.nio.handler.ResponseHandler;
import io.mycat.net.ClosableConnection;
import io.mycat.route.RouteResultsetNode;
import io.mycat.server.ServerConnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface BackendConnection extends ClosableConnection {
    boolean isModifiedSQLExecuted();

    boolean isFromSlaveDB();

    String getSchema();

    void setSchema(String newSchema);

    long getLastTime();

    boolean isClosedOrQuit();

    void setAttachment(Object attachment);

    void quit();

    void setLastTime(long currentTimeMillis);

    void release();

    boolean setResponseHandler(ResponseHandler commandHandler);

    void commit();

    void query(String sql) throws UnsupportedEncodingException;

    Object getAttachment();

    // public long getThreadId();

    void execute(RouteResultsetNode node, ServerConnection source,
                        boolean autocommit) throws IOException;

    void recordSql(String host, String schema, String statement);

    boolean syncAndExcute();

    void rollback();

    boolean isBorrowed();

    void setBorrowed(boolean borrowed);

    int getTxIsolation();

    boolean isAutocommit();

    boolean isTxReadonly();

    int getSqlSelectLimit();

    long getId();

    void discardClose(String reason);

    void query(String sql, int charsetIndex);

    boolean checkAlive();

    /**
     * 停止读取
     */
    void disableRead();

    /**
     * 启用读取
     */
    void enableRead();

}
