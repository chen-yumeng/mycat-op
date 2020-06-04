package io.mycat.config;

/**
 * Created by chenguang on 2020/6/4.
 * 流式查询控制器配置信息，通过server.xml获取
 */
public class FlowCotrollerConfig {

    private final boolean enableFlowControl;
    private final int start;
    private final int end;

    public FlowCotrollerConfig(boolean enableFlowControl, int start, int end) {
        this.enableFlowControl = enableFlowControl;
        this.start = start;
        this.end = end;
    }

    public boolean isEnableFlowControl() {
        return enableFlowControl;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
