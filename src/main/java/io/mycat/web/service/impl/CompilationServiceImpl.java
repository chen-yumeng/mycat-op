package io.mycat.web.service.impl;

import io.mycat.web.entity.CompilationBean;
import io.mycat.web.service.CompilationService;
import org.springframework.stereotype.Service;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

/**
 * @program: mycat->CompilationServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:14
 **/
@Service
public class CompilationServiceImpl implements CompilationService {

    @Override
    public CompilationBean get() {
        return init();
    }

    private CompilationBean init() {
        CompilationBean bean = new CompilationBean();
        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        bean.setName(compilationMXBean.getName());
        bean.setTotalTime(compilationMXBean.getTotalCompilationTime());
        bean.setIsSupport(compilationMXBean.isCompilationTimeMonitoringSupported());
        return bean;
    }
}
