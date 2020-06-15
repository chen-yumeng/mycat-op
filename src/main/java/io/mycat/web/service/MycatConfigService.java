package io.mycat.web.service;

import io.mycat.config.model.FirewallConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.model.UserConfig;

import java.util.Map;

/**
 * @program: mycat->MycatConfigService
 * @description:
 * @author: cg
 * @create: 2020-06-13 17:09
 **/
public interface MycatConfigService {

    /**
     * 获取Mycat server配置
     * @return
     */
    SystemConfig getSystemConfig();

    /**
     * 获取Mycat 所有用户配置
     * @return
     */
    Map<String, UserConfig> getUsersConfig();

    /**
     * 获取Mycat 防火墙配置
     * @return
     */
    FirewallConfig getAllFirewallConfig();

    /**
     * 修改Mycat server配置
     * @param key
     * @param value
     * @return
     */
    boolean editSystemConfig(String key, String value);

}
