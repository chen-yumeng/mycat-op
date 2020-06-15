package io.mycat.web.entity;

import java.io.Serializable;

/**
 * 操作系统信息
 * @program: mycat->SystemBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class SystemBean implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 操作系统名称
     */
    private String name;

    /**
     * 操作系统进程数量
     */
    private Integer processCount;

    /**
     * 操作系统架构
     */
    private String osArchName;

    /**
     * 操作系统负载平均值
     */
    private Double loadAverage;

    /**
     * 操作系统版本号
     */
    private String version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public void setProcessCount(Integer processCount) {
        this.processCount = processCount;
    }

    public String getOsArchName() {
        return osArchName;
    }

    public void setOsArchName(String osArchName) {
        this.osArchName = osArchName;
    }

    public Double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(Double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
