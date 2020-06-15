package io.mycat.web.entity;

import java.io.Serializable;

/**
 * 内存池信息
 * @program: mycat->MemoryPoolBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class MemoryPoolBean implements Serializable {

    /**
     * 内存区名称
     */
    private String name;

    /**
     * 所属内存管理者
     */
    private String manageNames;

    /**
     * 已申请内存
     */
    private Long committed;

    /**
     * 最大内存量
     */
    private Long max;

    /**
     * 已使用内存（字节）
     */
    private Long used;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManageNames() {
        return manageNames;
    }

    public void setManageNames(String manageNames) {
        this.manageNames = manageNames;
    }

    public Long getCommitted() {
        return committed;
    }

    public void setCommitted(Long committed) {
        this.committed = committed;
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
}
