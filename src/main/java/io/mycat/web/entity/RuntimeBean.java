package io.mycat.web.entity;

import io.mycat.web.utils.JSONTemplate;

import java.io.Serializable;
import java.util.List;

/**
 * JVM运行时信息
 * @program: mycat->RuntimeBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class RuntimeBean implements Serializable {

    /**
     * 主机
     */
    private String host;

    /**
     * JVM
     */
    private String jvm;

    /**
     * JDK版本
     */
    private String version;

    /**
     * JDK 路径
     */
    private String home;

    /**
     * 当前JVM参数信息
     */
    private List<String> args;

    /**
     * JVM开始启动的时间（毫秒）
     */
    private Long startTime;

    /**
     * JSON格式化后系统详细参数
     */
    private List<JSONTemplate> properties;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getJvm() {
        return jvm;
    }

    public void setJvm(String jvm) {
        this.jvm = jvm;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public List<JSONTemplate> getProperties() {
        return properties;
    }

    public void setProperties(List<JSONTemplate> properties) {
        this.properties = properties;
    }
}
