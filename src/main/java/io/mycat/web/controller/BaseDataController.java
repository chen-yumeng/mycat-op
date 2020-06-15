package io.mycat.web.controller;

import io.mycat.web.service.*;
import io.mycat.web.utils.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: mycat->BaseDataController
 * @description:
 * @author: cg
 * @create: 2020-06-12 14:07
 **/
@RestController
public class BaseDataController {

    @Autowired
    private ClassLoaderService classLoaderService;

    @GetMapping("/mycat/class/get")
    public ResultCode getClassLoader() {
        try {
            return new ResultCode(200, classLoaderService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private CompilationService compilationService;

    @GetMapping("/mycat/compilation/get")
    public ResultCode getCompilation() {
        try {
            return new ResultCode(200, compilationService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private MemoryService memoryService;

    @GetMapping("/mycat/memory/get")
    public ResultCode getMemory() {
        try {
            return new ResultCode(200, memoryService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private RuntimeInfoService runtimeInfoService;

    @GetMapping("/mycat/runtime/get")
    public ResultCode getRuntimeInfo() {
        try {
            return new ResultCode(200, runtimeInfoService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private SystemInfoService systemInfoService;

    @GetMapping("/mycat/system/get")
    public ResultCode getSystemInfo() {
        try {
            return new ResultCode(200, systemInfoService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private ThreadInfoService threadInfoService;

    @GetMapping("/mycat/thread/get")
    public ResultCode getThreadInfo() {
        try {
            return new ResultCode(200, threadInfoService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private GarbageCollectorService garbageCollectorService;

    @GetMapping("/mycat/gc/get")
    public ResultCode getGC() {
        try {
            return new ResultCode(200, garbageCollectorService.get());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/mycat/gc/getPools")
    public ResultCode getGCPools() {
        try {
            return new ResultCode(200, garbageCollectorService.getPools());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private MycatConfigService mycatConfigService;

    @GetMapping("/mycat/properties/getMycatSystemConfig")
    public ResultCode getMycatSystemConfig() {
        try {
            return new ResultCode(200, mycatConfigService.getSystemConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/mycat/properties/getMycatUsersConfig")
    public ResultCode getMycatUsersConfig() {
        try {
            return new ResultCode(200, mycatConfigService.getUsersConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @GetMapping("/mycat/properties/getMycatAllFirewallConfig")
    public ResultCode getMycatAllFirewallConfig() {
        try {
            return new ResultCode(200, mycatConfigService.getAllFirewallConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @PostMapping("/mycat/properties/editMycatSystemConfig")
    public ResultCode editSystemConfig(String key, String value) {
        try {
            return new ResultCode(200, mycatConfigService.editSystemConfig(key, value));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private MycatSchemaServer schemaServer;

    @GetMapping("/mycat/properties/getMycatSchemaConfig")
    public ResultCode getMycatSchemaConfig() {
        try {
            return new ResultCode(200, schemaServer.getSchemaConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

    @Autowired
    private MycatRuleServer ruleServer;

    @GetMapping("/mycat/properties/getMycatRuleConfig")
    public ResultCode getMycatRuleConfig() {
        try {
            return new ResultCode(200, ruleServer.getRuleConfig());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultCode(500, e.getMessage());
        }
    }

}
