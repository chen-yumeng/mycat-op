package io.mycat.web.entity;

import java.io.Serializable;

/**
 * 类加载数据
 * @program: mycat->ClassLoaderBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class ClassLoaderBean implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * JVM加载类的数量
     */
    private Integer count;

    /**
     * JVM已加载类数量
     */
    private Long loaded;

    /**
     * JVM未加载类数量
     */
    private Long unLoaded;

    /**
     * 是否启用类加载详细信息的输出
     */
    private Boolean isVerbose;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getLoaded() {
        return loaded;
    }

    public void setLoaded(Long loaded) {
        this.loaded = loaded;
    }

    public Long getUnLoaded() {
        return unLoaded;
    }

    public void setUnLoaded(Long unLoaded) {
        this.unLoaded = unLoaded;
    }

    public Boolean getIsVerbose() {
        return isVerbose;
    }

    public void setIsVerbose(Boolean verbose) {
        isVerbose = verbose;
    }
}
