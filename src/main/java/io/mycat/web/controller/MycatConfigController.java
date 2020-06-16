package io.mycat.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.mycat.config.model.FirewallConfig;
import io.mycat.web.service.MycatConfigService;
import io.mycat.web.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @program: Mycat->MycatServerController
 * @description:
 * @author: cg
 * @create: 2020-06-15 18:00
 **/
@RestController
public class MycatConfigController {

    private static Pattern IP_PATTERN = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");

    private static Pattern IP_MASK_PATTERN = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*]|\\*\\d\\d|\\d\\*\\d|\\d\\d\\*|\\*\\d|\\d\\*|\\*)\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*]|\\*\\d\\d|\\d\\*\\d|\\d\\d\\*|\\*\\d|\\d\\*|\\*)$");

    @Autowired
    private MycatConfigService configService;

    @GetMapping("/mycat/properties/server/getMycatSystemConfig")
    public ResultCode getMycatSystemConfig() {
        try {
            return new ResultCode(200, configService.getSystemConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/mycat/properties/server/getMycatUsersConfig")
    public ResultCode getMycatUsersConfig() {
        try {
            return new ResultCode(200, configService.getUsersConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/mycat/properties/server/getMycatAllFirewallConfig")
    public ResultCode getMycatAllFirewallConfig() {
        try {
            return new ResultCode(200, configService.getAllFirewallConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/addWhiteHostItem")
    public ResultCode addWhiteHostItem(String key, String value) {
        try {
            if (IP_PATTERN.matcher(key).matches()) {
                List array = JSON.parseArray(value);
                if (configService.addWhiteHostItem(key, array)) {
                    return new ResultCode(200, "添加成功");
                } else {
                    return new ResultCode(500, "该IP已被添加!");
                }
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/addWhiteHostMaskItem")
    public ResultCode addWhiteHostMaskItem(String key, String value) {
        try {
            String host = FirewallConfig.getHost(Pattern.compile(key));
            if (IP_MASK_PATTERN.matcher(host).matches()) {
                JSONArray array = JSON.parseArray(value);
                if (configService.addWhiteHostMaskItem(key, array)) {
                    return new ResultCode(200, "添加成功");
                } else {
                    return new ResultCode(500, "该IP已被添加!");
                }
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/addBlackItem")
    public ResultCode addBlackItem(String key, String value) {
        try {
            if (IP_PATTERN.matcher(value).matches()) {
                configService.addBlackItem(key, value);
                return new ResultCode(200, "添加成功");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/editMycatSystemConfig")
    public ResultCode editSystemConfig(String key, String value) {
        try {
            if (configService.editSystemConfig(key, value)) {
                return new ResultCode(200, "修改成功");
            } else {
                return new ResultCode(200, "修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/editWhiteHostItem")
    public ResultCode editWhiteHostItem(String oldKey, String key, String value) {
        try {
            if (IP_PATTERN.matcher(key).matches()) {
                List array = JSON.parseArray(value);
                configService.editWhiteHostItem(oldKey, key, array);
                return new ResultCode(200, "修改成功!");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/editWhiteHostMaskItem")
    public ResultCode editWhiteHostMaskItem(String oldKey, String key, String value) {
        try {
            String host = FirewallConfig.getHost(Pattern.compile(key));
            if (IP_MASK_PATTERN.matcher(host).matches()) {
                List array = JSON.parseArray(value);
                configService.editWhiteHostMaskItem(oldKey, key, array);
                return new ResultCode(200, "修改成功");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/editBlackItem")
    public ResultCode editBlackItem(String key, String value) {
        try {
            if (IP_PATTERN.matcher(value).matches()) {
                configService.editBlackItem(key, value);
                return new ResultCode(200, "修改成功");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping(value = "/mycat/properties/server/deleWhiteHostItem")
    public ResultCode deleWhiteHostItem(String key) {
        try {
            configService.deleWhiteHostItem(key);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping(value = "/mycat/properties/server/deleWhiteHostMaskItem")
    public ResultCode deleWhiteHostMaskItem(String key) {
        try {
            configService.deleWhiteHostMaskItem(key);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/server/deleBlackItem")
    public ResultCode deleBlackItem(String key, String value) {
        try {
            configService.deleBlackItem(key, value);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

}
