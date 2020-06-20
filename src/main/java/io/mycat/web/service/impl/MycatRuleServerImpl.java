package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.loader.xml.XMLRuleLoader;
import io.mycat.config.model.rule.TableRuleConfig;
import io.mycat.route.function.AbstractPartitionAlgorithm;
import io.mycat.web.service.MycatRuleServer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public List<Map> getRuleConfig() {
        XMLRuleLoader ruleLoader = new XMLRuleLoader(null);
        Map<String, TableRuleConfig> tableRules = ruleLoader.getTableRules();
        Map<String, AbstractPartitionAlgorithm> functions = ruleLoader.getFunctions();
        List<Map> list = new ArrayList<>();
        list.add(tableRules);
        list.add(functions);
        return list;
    }

}
