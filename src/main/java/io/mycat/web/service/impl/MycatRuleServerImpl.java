package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.web.service.MycatRuleServer;
import org.springframework.stereotype.Service;

/**
 * @program: mycat->MycatRuleServerImpl
 * @description:
 * @author: cg
 * @create: 2020-06-14 14:07
 **/
@Service
public class MycatRuleServerImpl implements MycatRuleServer {

    private static MycatConfig config;

    public MycatRuleServerImpl() {
        config = MycatServer.getInstance().getConfig();
    }

    @Override
    public String getRuleConfig() {
        return null;
    }

}
