package io.mycat.web.service.impl;

import io.mycat.web.entity.GarbageCollectorBean;
import io.mycat.web.entity.MemoryPoolBean;
import io.mycat.web.service.GarbageCollectorService;
import org.springframework.stereotype.Service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @program: mycat->GarbageCollectorServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:14
 **/
@Service
public class GarbageCollectorServiceImpl implements GarbageCollectorService {

    @Override
    public GarbageCollectorBean get() {
        GarbageCollectorBean gcBean = new GarbageCollectorBean();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        garbageCollectorMXBeans.forEach(bean -> {
            gcBean.setCount(bean.getCollectionCount());
            gcBean.setTime(bean.getCollectionTime());
        });
        return gcBean;
    }

    @Override
    public List<MemoryPoolBean> getPools() {
        List<MemoryPoolBean> list = new ArrayList<>();
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        memoryPoolMXBeans.forEach(bean -> {
            MemoryPoolBean poolBean = new MemoryPoolBean();
            poolBean.setName(bean.getName());
            poolBean.setManageNames(Arrays.toString(bean.getMemoryManagerNames()));
            poolBean.setUsed(bean.getUsage().getUsed());
            poolBean.setMax(bean.getUsage().getMax());
            poolBean.setCommitted(bean.getUsage().getCommitted());
            list.add(poolBean);
        });
        return list;
    }
}
