package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.web.service.MycatSchemaServer;
import org.springframework.stereotype.Service;

/**
 * @program: mycat->MycatSchemaServerImpl
 * @description:
 * @author: cg
 * @create: 2020-06-14 14:05
 **/
@Service
public class MycatSchemaServerImpl implements MycatSchemaServer {

    private static MycatConfig config;

    public MycatSchemaServerImpl() {
        config = MycatServer.getInstance().getConfig();
    }

    @Override
    public SystemConfig getSchemaConfig() {
        return config.getSystem();
    }
}
