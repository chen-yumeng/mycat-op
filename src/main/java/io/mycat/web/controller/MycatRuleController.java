package io.mycat.web.controller;

import io.mycat.web.service.MycatRuleServer;
import io.mycat.web.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: Mycat->MycatRuleController
 * @description:
 * @author: cg
 * @create: 2020-06-15 18:04
 **/
@RestController
public class MycatRuleController {

    @Autowired
    private MycatRuleServer ruleServer;

    @GetMapping("/mycat/properties/rule/getMycatRuleConfig")
    public ResultCode getMycatRuleConfig() {
        try {
            return new ResultCode(200, ruleServer.getRuleConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }
}
