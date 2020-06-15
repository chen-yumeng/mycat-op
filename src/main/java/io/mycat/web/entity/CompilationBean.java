package io.mycat.web.entity;

import java.io.Serializable;

/**
 * 编译信息
 * @program: mycat->CompilationBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class CompilationBean implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 即时（JIT）编译器名称
     */
    private String name;

    /**
     * 总编译时间（毫秒）
     */
    private Long totalTime;

    /**
     * JVM是否支持对编译时间的监视
     */
    private Boolean isSupport;

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

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Boolean getIsSupport() {
        return isSupport;
    }

    public void setIsSupport(Boolean support) {
        isSupport = support;
    }
}
