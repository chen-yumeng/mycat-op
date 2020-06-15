package io.mycat.web.entity;

import java.io.Serializable;

/**
 * 堆内存信息
 * @program: mycat->MemoryBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class MemoryBean implements Serializable {

    /**
     * 已申请的堆内存
     */
    private Long committed;

    /**
     * JVM初始化堆占用的内存总量
     */
    private Long init;

    /**
     * JVM提供可用于内存管理的最大内存量
     */
    private Long max;

    /**
     * 内存区已使用空间大小（字节）
     */
    private Long used;

    /**
     * 已申请的非堆内存大小
     */
    private Long nonCommitted;

    /**
     * JVM初始化非堆区占用的内存总量
     */
    private Long nonInit;

    /**
     * JVM提供可用于非堆内存区管理的最大内存量
     */
    private Long nonMax;

    /**
     * JVM非堆内存区已使用空间大小（字节）
     */
    private Long nonUsed;

    public Long getCommitted() {
        return committed;
    }

    public void setCommitted(Long committed) {
        this.committed = committed;
    }

    public Long getInit() {
        return init;
    }

    public void setInit(Long init) {
        this.init = init;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public Long getNonCommitted() {
        return nonCommitted;
    }

    public void setNonCommitted(Long nonCommitted) {
        this.nonCommitted = nonCommitted;
    }

    public Long getNonInit() {
        return nonInit;
    }

    public void setNonInit(Long nonInit) {
        this.nonInit = nonInit;
    }

    public Long getNonMax() {
        return nonMax;
    }

    public void setNonMax(Long nonMax) {
        this.nonMax = nonMax;
    }

    public Long getNonUsed() {
        return nonUsed;
    }

    public void setNonUsed(Long nonUsed) {
        this.nonUsed = nonUsed;
    }
}
