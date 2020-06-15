package io.mycat.web.entity;

import java.io.Serializable;

/**
 * 线程信息
 * @program: mycat->ThreadBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class ThreadBean implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 当前线程执行时间（纳秒）
     */
    private Long currentTime;

    /**
     * 当前守护线程数量
     */
    private Integer daemonCount;

    /**
     * 当前线程总数量（包括守护线程和非守护线程）
     */
    private Integer count;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

    public Integer getDaemonCount() {
        return daemonCount;
    }

    public void setDaemonCount(Integer daemonCount) {
        this.daemonCount = daemonCount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
