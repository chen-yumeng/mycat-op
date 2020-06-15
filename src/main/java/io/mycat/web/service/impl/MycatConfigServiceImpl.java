package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.FirewallConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.model.UserConfig;
import io.mycat.web.service.MycatConfigService;
import org.springframework.stereotype.Service;

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
        Map<Pattern, List<UserConfig>> mask = firewall.getWhitehostMask();
        for (Map.Entry<Pattern, List<UserConfig>> entry : mask.entrySet()) {
            String host = FirewallConfig.getHost(entry.getKey());
            mask.put(Pattern.compile(host), entry.getValue());
            mask.remove(entry.getKey());
        }
        return firewall;
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
