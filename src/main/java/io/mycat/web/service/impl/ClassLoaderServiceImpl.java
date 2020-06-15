package io.mycat.web.service.impl;

import io.mycat.web.entity.ClassLoaderBean;
import io.mycat.web.service.ClassLoaderService;
import org.springframework.stereotype.Service;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * @program: mycat->ClassLoaderServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:13
 **/
@Service
public class ClassLoaderServiceImpl implements ClassLoaderService {

    @Override
    public ClassLoaderBean get() {
        return init();
    }

    private ClassLoaderBean init() {
        ClassLoaderBean bean = new ClassLoaderBean();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        bean.setLoaded(classLoadingMXBean.getTotalLoadedClassCount());
        bean.setCount(classLoadingMXBean.getLoadedClassCount());
        bean.setUnLoaded(classLoadingMXBean.getUnloadedClassCount());
        bean.setIsVerbose(classLoadingMXBean.isVerbose());
        return bean;
    }
}
