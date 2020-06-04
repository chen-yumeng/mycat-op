package io.mycat.sqlengine;

import io.mycat.MycatServer;
import io.mycat.config.FlowCotrollerConfig;

/**
 * Created by chenguang on 2020/6/4.
 * 写队列的流式查询控制器
 */
public final class WriteQueueFlowController {
    private static final WriteQueueFlowController INSTANCE = new WriteQueueFlowController();
    private volatile FlowCotrollerConfig config = null;

    private WriteQueueFlowController() {
    }

    public static void init() throws Exception {
        INSTANCE.config = new FlowCotrollerConfig(
                MycatServer.getInstance().getConfig().getSystem().isEnableFlowControl(),
                MycatServer.getInstance().getConfig().getSystem().getFlowControlStartThreshold(),
                MycatServer.getInstance().getConfig().getSystem().getFlowControlStopThreshold());
        if (INSTANCE.config.getEnd() < 0 || INSTANCE.config.getStart() <= 0) {
            throw new Exception("The flowControlStartThreshold & flowControlStopThreshold must be positive integer");
        } else if (INSTANCE.config.getEnd() >= INSTANCE.config.getStart()) {
            throw new Exception("The flowControlStartThreshold must bigger than flowControlStopThreshold");
        }
    }

    public static FlowCotrollerConfig getFlowCotrollerConfig() {
        return INSTANCE.config;
    }

    public static void configChange(FlowCotrollerConfig newConfig) {
        INSTANCE.config = newConfig;
    }
}


