package io.mycat.web.service;

import io.mycat.config.model.FirewallConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.model.UserConfig;
import io.mycat.web.entity.UserConfigDTO;

import java.util.List;
import java.util.Map;

/**
 * @program: mycat->MycatServerConfigService
 * @description:
 * @author: cg
 * @create: 2020-06-13 17:09
 **/
public interface MycatServerConfigService {

    /**
     * 保存 Mycat 用户
     * @param userConfig
     * @param oldName 修改前的用户名
     */
    void saveUserItem(UserConfigDTO userConfig, String oldName);

    /**
     * 删除 Mycat 用户
     * @param key
     */
    void deleUserItem(String key);

    /**
     * 获取Mycat server配置
     *
     * @return
     */
    SystemConfig getSystemConfig();

    /**
     * 获取Mycat 所有用户配置
     *
     * @return
     */
    Map<String, UserConfig> getUsersConfig();

    /**
     * 获取Mycat SQL防火墙配置
     *
     * @return
     */
    FirewallConfig getAllFirewallConfig();

    /**
     * 删除 SQL防火墙白名单
     *
     * @param key
     */
    void deleWhiteHostItem(String key);

    /**
     * 删除 SQL防火墙网段白名单
     *
     * @param key
     */
    void deleWhiteHostMaskItem(String key);

    /**
     * 删除SQL防火墙黑名单
     *
     * @param key
     * @param value
     */
    void deleBlackItem(String key, String value);

    /**
     * 添加SQL防火墙白名单
     *
     * @param key
     * @param names
     */
    boolean addWhiteHostItem(String key, List names);

    /**
     * 添加SQL防火墙局域白名单
     *
     * @param key
     * @param names
     */
    boolean addWhiteHostMaskItem(String key, List names);

    /**
     * 添加SQL防火墙黑名单
     *
     * @param key
     * @param value
     */
    void addBlackItem(String key, String value);

    /**
     * 通过ip地址进行修改SQL防火墙白名单
     *
     * @param oldKey
     * @param key
     * @param names
     */
    void editWhiteHostItem(String oldKey, String key, List names);

    /**
     * 通过ip地址进行修改SQL防火墙白局域名单
     *
     * @param oldKey
     * @param key
     * @param names
     */
    void editWhiteHostMaskItem(String oldKey, String key, List names);

    /**
     * 修改SQL防火墙黑名单
     *
     * @param key
     * @param value
     */
    void editBlackItem(String key, String value);

    /**
     * 修改Mycat server配置
     *
     * @param key
     * @param value
     * @return
     */
    boolean editSystemConfig(String key, String value);

}
