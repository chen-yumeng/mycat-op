package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.FirewallConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.model.UserConfig;
import io.mycat.web.service.MycatConfigService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @program: mycat->MycatConfigServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-13 17:12
 **/
@Service
public class MycatConfigServiceImpl implements MycatConfigService {

    private static MycatConfig config;

    public MycatConfigServiceImpl() {
        config = MycatServer.getInstance().getConfig();
    }

    @Override
    public SystemConfig getSystemConfig() {
        //System.out.println(config.getFirewall().getWhitehost());
        //System.out.println(config.getFirewall().getWhitehostMask());
        //System.out.println(config.getFirewall().getBlacklist());
        //System.out.println(config.getFirewall().isCheck());
        //Map<Pattern, List<UserConfig>> whitehostMask = config.getFirewall().getWhitehostMask();
        //for (Pattern pattern : whitehostMask.keySet()) {
        //    List<UserConfig> userConfigs = whitehostMask.get(pattern);
        //    String host = FirewallConfig.getHost(pattern);
        //    System.out.println(host);
        //}
        //System.out.println(whitehostMask);
        FirewallConfig firewallConfig = getAllFirewallConfig();
        System.out.println(firewallConfig.getWhitehost());
        System.out.println(firewallConfig.getWhitehostMask());
        System.out.println(firewallConfig.getBlacklist());
        return config.getSystem();
    }

    @Override
    public Map<String, UserConfig> getUsersConfig() {
        return config.getUsers();
    }

    @Override
    public FirewallConfig getAllFirewallConfig() {
        FirewallConfig firewall = config.getFirewall();
        return firewall;
    }

    @Override
    public void deleWhiteHostItem(String key) {
        FirewallConfig firewall = config.getFirewall();
        firewall.getWhitehost().remove(key);
    }

    @Override
    public void deleWhiteHostMaskItem(String key) {
        FirewallConfig firewall = config.getFirewall();
        Map<Pattern, List<UserConfig>> whitehostMask = firewall.getWhitehostMask();
        Map<Pattern, List<UserConfig>> newWhitehostMask = new HashMap<>();
        whitehostMask.forEach((pattern, userConfigs) -> {
            if (!key.equals(pattern.toString())) {
                newWhitehostMask.put(pattern, userConfigs);
            }
        });
        config.getFirewall().setWhitehostMask(newWhitehostMask);
    }

    @Override
    public void deleBlackItem(String key, String value) {
        FirewallConfig firewall = config.getFirewall();
        firewall.getBlacklist().remove(key, value);
    }

    @Override
    public boolean addWhiteHostItem(String key, List names) {
        Map<String, List<UserConfig>> whitehost = config.getFirewall().getWhitehost();
        if (whitehost.containsKey(key)) {
            return false;
        }
        List<UserConfig> list = new ArrayList<>();
        names.forEach(name -> {
            config.getUsers().forEach((s, userConfig) -> {
                if (s.equals(name)) {
                    list.add(userConfig);
                }
            });
        });
        whitehost.put(key, list);
        config.getFirewall().setWhitehost(whitehost);
        return true;
    }

    @Override
    public boolean addWhiteHostMaskItem(String key, List names) {
        Map<Pattern, List<UserConfig>> whitehostMask = config.getFirewall().getWhitehostMask();
        for (Pattern pattern : whitehostMask.keySet()) {
            if (pattern.toString().equals(key)) {
                return false;
            }
        }
        List<UserConfig> list = new ArrayList<>();
        names.forEach(name -> {
            config.getUsers().forEach((s, userConfig) -> {
                if (s.equals(name)) {
                    list.add(userConfig);
                }
            });
        });
        whitehostMask.put(Pattern.compile(key), list);
        config.getFirewall().setWhitehostMask(whitehostMask);
        return true;
    }

    @Override
    public void addBlackItem(String key, String value) {
        FirewallConfig firewall = config.getFirewall();
        firewall.getBlacklist().put(key, value);
    }

    @Override
    public void editWhiteHostItem(String oldKey, String key, List names) {
        FirewallConfig firewall = config.getFirewall();
        Map<String, List<UserConfig>> whiteHost = firewall.getWhitehost();
        whiteHost.remove(oldKey);
        List<UserConfig> userConfigs = new ArrayList<>();
        Map<String, UserConfig> users = config.getUsers();
        names.forEach(name -> userConfigs.add(users.get(name)));
        whiteHost.put(key, userConfigs);
    }

    @Override
    public void editWhiteHostMaskItem(String oldKey, String key, List names) {
        Map<Pattern, List<UserConfig>> whiteHostMask = config.getFirewall().getWhitehostMask();
        Map<Pattern, List<UserConfig>> map = new HashMap<>();
        List<UserConfig> list = new ArrayList<>();
        Map<String, UserConfig> users = config.getUsers();
        names.forEach(name -> list.add(users.get(name)));
        whiteHostMask.forEach((pattern, userConfigs) -> {
            if (!pattern.toString().equals(oldKey)) {
                map.put(pattern, userConfigs);
            }
        });
        map.put(Pattern.compile(key), list);
        config.getFirewall().setWhitehostMask(map);
    }

    @Override
    public void editBlackItem(String key, String value) {
        config.getFirewall().getBlacklist().entrySet().forEach(stringObjectEntry -> {
            if (stringObjectEntry.getKey().equals(key)) {
                stringObjectEntry.setValue(value);
            }
        });
    }

    @Override
    public boolean editSystemConfig(String key, String value) {
        SystemConfig system = config.getSystem();
        switch (key) {
            case "nonePasswordLogin":
                system.setNonePasswordLogin(Integer.parseInt(value));
                break;
            case "ignoreUnknownCommand":
                system.setIgnoreUnknownCommand(Integer.parseInt(value));
                break;
            case "useSqlStat":
                system.setUseSqlStat(Integer.parseInt(value));
                break;
            case "useGlobleTableCheck":
                system.setUseGlobleTableCheck(Integer.parseInt(value));
                break;
            case "sqlExecuteTimeout":
                system.setSqlExecuteTimeout(Long.parseLong(value));
                break;
            case "sequenceHandlerType":
                system.setSequenceHandlerType(Integer.parseInt(value));
                break;
            case "sequnceHandlerPattern":
                system.setSequnceHandlerPattern(value);
                break;
            case "subqueryRelationshipCheck":
                system.setSubqueryRelationshipCheck(Boolean.parseBoolean(value));
                break;
            case "sequenceHanlderClass":
                system.setSequenceHanlderClass(value);
                break;
            case "useCompression":
                system.setUseCompression(Integer.parseInt(value));
                break;
            case "fakeMySQLVersion":
                system.setFakeMySQLVersion(value);
                break;
            case "processor":
                system.setProcessors(Integer.parseInt(value));
                break;
            case "processorExecutor":
                system.setProcessorExecutor(Integer.parseInt(value));
                break;
            case "processorBufferPoolType":
                system.setProcessorBufferPoolType(Integer.parseInt(value));
                break;
            case "maxStringLiteralLength":
                system.setMaxStringLiteralLength(Integer.parseInt(value));
                break;
            case "backSocketNoDelay":
                system.setBackSocketNoDelay(Integer.parseInt(value));
                break;
            case "frontSocketNoDelay":
                system.setFrontSocketNoDelay(Integer.parseInt(value));
                break;
            case "serverPort":
                system.setServerPort(Integer.parseInt(value));
                break;
            case "managerPort":
                system.setManagerPort(Integer.parseInt(value));
                break;
            case "idleTimeout":
                system.setIdleTimeout(Integer.parseInt(value));
                break;
            case "dataNodeIdleCheckPeriod":
                system.setDataNodeIdleCheckPeriod(Integer.parseInt(value));
                break;
            case "bindIp":
                system.setBindIp(value);
                break;
            case "frontWriteQueueSize":
                system.setFrontWriteQueueSize(Integer.parseInt(value));
                break;
            case "handleDistributedTransactions":
                system.setHandleDistributedTransactions(Integer.parseInt(value));
                break;
            case "enableFlowControl":
                system.setEnableFlowControl(Boolean.parseBoolean(value));
                break;
            case "flowControlStartMaxValue":
                system.setFlowControlStartMaxValue(Integer.parseInt(value));
                break;
            case "flowControlStopMaxValue":
                system.setFlowControlStopMaxValue(Integer.parseInt(value));
                break;
            case "bufferPoolChunkSize":
                system.setBufferPoolChunkSize(Short.parseShort(value));
                break;
            case "bufferPoolPageSize":
                system.setBufferPoolPageSize(Integer.parseInt(value));
                break;
            case "bufferPoolPageNumber":
                system.setBufferPoolPageNumber(Short.parseShort(value));
                break;
            case "useOffHeapForMerge":
                system.setUseOffHeapForMerge(Integer.parseInt(value));
                break;
            case "memoryPageSize":
                system.setMemoryPageSize(value);
                break;
            case "spillsFileBufferSize":
                system.setSpillsFileBufferSize(value);
                break;
            case "useStreamOutput":
                system.setUseStreamOutput(Integer.parseInt(value));
                break;
            case "systemReserveMemorySize":
                system.setSystemReserveMemorySize(value);
                break;
            case "useZKSwitch":
                system.setUseZKSwitch(Boolean.parseBoolean(value));
                break;
            case "XARecoveryLogBaseDir":
                system.setXARecoveryLogBaseDir(value);
                break;
            case "XARecoveryLogBaseName":
                system.setXARecoveryLogBaseName(value);
                break;
            case "strictTxIsolation":
                system.setStrictTxIsolation(Boolean.parseBoolean(value));
                break;
            case "parallExecute":
                system.setParallExecute(Integer.parseInt(value));
                break;
            default:
                return false;
        }
        return true;
    }
}
