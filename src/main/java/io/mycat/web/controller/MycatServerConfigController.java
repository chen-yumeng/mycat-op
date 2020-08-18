package io.mycat.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.mycat.config.model.FirewallConfig;
import io.mycat.web.entity.UserConfigDTO;
import io.mycat.web.service.MycatServerConfigService;
import io.mycat.web.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @program: Mycat->MycatServerController
 * @description:
 * @author: cg
 * @create: 2020-06-15 18:00
 **/
@RequestMapping("/mycat/properties/server/")
@RestController
public class MycatServerConfigController {

    private static Pattern IP_PATTERN = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");

    private static Pattern IP_MASK_PATTERN = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*]|\\*\\d\\d|\\d\\*\\d|\\d\\d\\*|\\*\\d|\\d\\*|\\*)\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*]|\\*\\d\\d|\\d\\*\\d|\\d\\d\\*|\\*\\d|\\d\\*|\\*)$");

    @Autowired
    private MycatServerConfigService serverConfigService;

    @GetMapping("getMycatSystemConfig")
    public ResultCode getMycatSystemConfig() {
        try {
            return new ResultCode(200, serverConfigService.getSystemConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/getMycatUsersConfig")
    public ResultCode getMycatUsersConfig() {
        try {
            return new ResultCode(200, serverConfigService.getUsersConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/getMycatAllFirewallConfig")
    public ResultCode getMycatAllFirewallConfig() {
        try {
            return new ResultCode(200, serverConfigService.getAllFirewallConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/addUserItem")
    public ResultCode addUserItem(String config) {
        try {
            UserConfigDTO dto = JSON.parseObject(config, UserConfigDTO.class);
            serverConfigService.saveUserItem(dto, null);
            return new ResultCode(200, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/editUserItem")
    public ResultCode editUserItem(String config, String oldName) {
        try {
            UserConfigDTO dto = JSON.parseObject(config, UserConfigDTO.class);
            serverConfigService.saveUserItem(dto, oldName);
            return new ResultCode(200, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/deleUserItem")
    public ResultCode deleUserItem(String key) {
        try {
            serverConfigService.deleUserItem(key);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/addWhiteHostItem")
    public ResultCode addWhiteHostItem(String key, String value) {
        try {
            if (IP_PATTERN.matcher(key).matches()) {
                JSONArray array = JSON.parseArray(value);
                if (serverConfigService.addWhiteHostItem(key, array)) {
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

    @PostMapping("/addWhiteHostMaskItem")
    public ResultCode addWhiteHostMaskItem(String key, String value) {
        try {
            String host = FirewallConfig.getHost(Pattern.compile(key));
            if (IP_MASK_PATTERN.matcher(host).matches()) {
                JSONArray array = JSON.parseArray(value);
                if (serverConfigService.addWhiteHostMaskItem(key, array)) {
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

    @PostMapping("/addBlackItem")
    public ResultCode addBlackItem(String key, String value) {
        try {
            if (IP_PATTERN.matcher(value).matches()) {
                serverConfigService.addBlackItem(key, value);
                return new ResultCode(200, "添加成功");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/editMycatSystemConfig")
    public ResultCode editSystemConfig(String key, String value) {
        try {
            if (serverConfigService.editSystemConfig(key, value)) {
                return new ResultCode(200, "修改成功");
            } else {
                return new ResultCode(200, "修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/editWhiteHostItem")
    public ResultCode editWhiteHostItem(String oldKey, String key, String value) {
        try {
            if (IP_PATTERN.matcher(key).matches()) {
                List array = JSON.parseArray(value);
                serverConfigService.editWhiteHostItem(oldKey, key, array);
                return new ResultCode(200, "修改成功!");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/editWhiteHostMaskItem")
    public ResultCode editWhiteHostMaskItem(String oldKey, String key, String value) {
        try {
            String host = FirewallConfig.getHost(Pattern.compile(key));
            if (IP_MASK_PATTERN.matcher(host).matches()) {
                List array = JSON.parseArray(value);
                serverConfigService.editWhiteHostMaskItem(oldKey, key, array);
                return new ResultCode(200, "修改成功");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/editBlackItem")
    public ResultCode editBlackItem(String key, String value) {
        try {
            if (IP_PATTERN.matcher(value).matches()) {
                serverConfigService.editBlackItem(key, value);
                return new ResultCode(200, "修改成功");
            } else {
                return new ResultCode(500, "IP地址不合法!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping(value = "/deleWhiteHostItem")
    public ResultCode deleWhiteHostItem(String key) {
        try {
            serverConfigService.deleWhiteHostItem(key);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping(value = "/deleWhiteHostMaskItem")
    public ResultCode deleWhiteHostMaskItem(String key) {
        try {
            serverConfigService.deleWhiteHostMaskItem(key);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/deleBlackItem")
    public ResultCode deleBlackItem(String key, String value) {
        try {
            serverConfigService.deleBlackItem(key, value);
            return new ResultCode(200, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

}
