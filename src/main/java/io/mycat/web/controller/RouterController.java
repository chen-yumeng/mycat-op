package io.mycat.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: mycat->RouterController
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
@Controller
public class RouterController {

    /**
     * 首页
     *
     * @return
     */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    /**
     * Mycat概述页
     *
     * @return
     */
    @GetMapping("/mycat/jvm/overview")
    public String overview() {
        return "mycat/jvm/overview";
    }

    /**
     * Mycat类加载监控页
     *
     * @return
     */
    @GetMapping("/mycat/jvm/class")
    public String monitor() {
        return "mycat/jvm/class";
    }

    /**
     * Mycat GC监控页
     *
     * @return
     */
    @GetMapping("/mycat/jvm/gc")
    public String gc() {
        return "mycat/jvm/gc";
    }

    /**
     * Mycat内存监控页
     *
     * @return
     */
    @GetMapping("/mycat/jvm/memory")
    public String memory() {
        return "mycat/jvm/memory";
    }

    /**
     * Mycat线程监控页
     *
     * @return
     */
    @GetMapping("/mycat/jvm/thread")
    public String thread() {
        return "mycat/jvm/thread";
    }

    /**
     * Mycat server.xml监控页
     *
     * @return
     */
    @GetMapping("/mycat/properties/server")
    public String server() {
        return "mycat/properties/server";
    }

    /**
     * Mycat schema.xml监控页
     *
     * @return
     */
    @GetMapping("/mycat/properties/schema")
    public String schema() {
        return "mycat/properties/schema";
    }

    /**
     * Mycat rule.xml监控页
     *
     * @return
     */
    @GetMapping("/mycat/properties/rule")
    public String rule() {
        return "mycat/properties/rule";
    }

    /**
     * 登录页面
     * @return
     */
    @GetMapping("/login")
    public String goLogin() {
        return "login";
    }
}
