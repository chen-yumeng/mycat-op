package io.mycat.web.service;

import io.mycat.config.model.SystemConfig;

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
    SystemConfig getSchemaConfig();

}
