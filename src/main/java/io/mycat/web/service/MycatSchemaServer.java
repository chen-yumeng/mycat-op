package io.mycat.web.service;

import io.mycat.config.model.DataHostConfig;
import io.mycat.config.model.SchemaConfig;

import java.util.List;
import java.util.Map;

/**
 * @program: mycat->MycatSchemaServer
 * @description:
 * @author: cg
 * @create: 2020-06-14 14:04
 **/
public interface MycatSchemaServer {

    /**
     * 获取Mycat schema配置
     * @return
     */
    Map<String, SchemaConfig> getSchemaConfig();

    /**
     * 获取Mycat DataHosts配置信息
     * @return
     */
    List<DataHostConfig> getMycatDataHostsConfig();

    /**
     * 获取Mycat DataNodes配置信息
     * @return
     */
    List<Map<String ,Object>> getMycatDataNodesConfig();
}
