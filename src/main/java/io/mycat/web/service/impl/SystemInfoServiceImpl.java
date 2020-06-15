package io.mycat.web.service.impl;

import io.mycat.web.entity.SystemBean;
import io.mycat.web.service.SystemInfoService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @program: mycat->SystemInfoServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:14
 **/
@Service
public class SystemInfoServiceImpl implements SystemInfoService {

    @Override
    public SystemBean get() {
        return init();
    }

    private SystemBean init() {
        SystemBean bean = new SystemBean();
        OperatingSystemMXBean mxBean = ManagementFactory.getOperatingSystemMXBean();
        bean.setName(mxBean.getName());
        bean.setProcessCount(mxBean.getAvailableProcessors());
        // System.getProperty("os.arch");
        bean.setOsArchName(mxBean.getArch());
        bean.setLoadAverage(mxBean.getSystemLoadAverage());
        // System.getProperty("os.version");
        bean.setVersion(mxBean.getVersion());
        return bean;
    }
}
