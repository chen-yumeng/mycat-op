package io.mycat.web.entity;

import java.io.Serializable;

/**
 * JVM垃圾回收信息
 * @program: mycat->GarbageCollectorBean
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:10
 **/
public class GarbageCollectorBean implements Serializable {

    /**
     * GC回收次数
     */
    private Long count;

    /**
     * GC回收耗时
     */
    private Long time;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
