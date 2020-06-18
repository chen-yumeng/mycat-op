package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.DataHostConfig;
import io.mycat.config.model.SchemaConfig;
import io.mycat.web.service.MycatSchemaServer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, SchemaConfig> getSchemaConfig() {
        return config.getSchemas();
    }

    @Override
    public List<DataHostConfig> getMycatDataHostsConfig() {
        List<DataHostConfig>  dataHostConfigs = new ArrayList<>();
        config.getDataHosts().entrySet().forEach(stringPhysicalDBPoolEntry -> dataHostConfigs.add(stringPhysicalDBPoolEntry.getValue().getDataHostConfig()));
        return dataHostConfigs;
    }

    @Override
    public List<Map<String ,Object>> getMycatDataNodesConfig() {
        List<Map<String ,Object>> list = new ArrayList<>();
        config.getDataNodes().entrySet().forEach(stringPhysicalDBNodeEntry -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", stringPhysicalDBNodeEntry.getValue().getName());
            map.put("database", stringPhysicalDBNodeEntry.getValue().getDatabase());
            map.put("dataHost", stringPhysicalDBNodeEntry.getValue().getDbPool().getDataHostConfig());
            list.add(map);
        });
        return list;
    }
}
