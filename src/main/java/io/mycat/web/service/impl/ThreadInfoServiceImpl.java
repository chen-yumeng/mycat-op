package io.mycat.web.service.impl;

import io.mycat.web.entity.ThreadBean;
import io.mycat.web.service.ThreadInfoService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @program: mycat->ThreadInfoServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:14
 **/
@Service
public class ThreadInfoServiceImpl implements ThreadInfoService {

    @Override
    public ThreadBean get() {
        return init();
    }

    private ThreadBean init() {
        ThreadBean bean = new ThreadBean();
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        bean.setCurrentTime(mxBean.getCurrentThreadUserTime());
        bean.setDaemonCount(mxBean.getDaemonThreadCount());
        bean.setCount(mxBean.getThreadCount());
        return bean;
    }
}
