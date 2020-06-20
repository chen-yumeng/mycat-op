package io.mycat.web.service;

import java.util.List;
import java.util.Map;

/**
 * @program: mycat->MycatRuleServer
 * @description:
 * @author: cg
 * @create: 2020-06-14 14:07
 **/
public interface MycatRuleServer {

    /**
     * 获取Mycat rule配置
     * @return
     */
    List<Map> getRuleConfig();

}
