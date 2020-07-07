package io.mycat.config;

/**
 * Created by chenguang on 2020/6/4.
 * 流式查询控制器配置信息，通过server.xml获取
 */
public class FlowControllerConfig {

    private final boolean enableFlowControl;
    private final int start;
    private final int stop;

    public FlowControllerConfig(boolean enableFlowControl, int start, int end) {
        this.enableFlowControl = enableFlowControl;
        this.start = start;
        this.stop = end;
    }

    public boolean isEnableFlowControl() {
        return enableFlowControl;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }
}
