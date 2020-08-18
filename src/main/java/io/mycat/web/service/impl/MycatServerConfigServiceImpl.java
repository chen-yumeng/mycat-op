package io.mycat.web.service.impl;

import io.mycat.MycatServer;
import io.mycat.config.MycatConfig;
import io.mycat.config.model.FirewallConfig;
import io.mycat.config.model.SystemConfig;
import io.mycat.config.model.UserConfig;
import io.mycat.config.model.UserPrivilegesConfig;
import io.mycat.web.entity.UserConfigDTO;
import io.mycat.web.entity.UserPrivilegesConfigDTO;
import io.mycat.web.service.MycatServerConfigService;
import io.mycat.web.utils.XMLLoaderUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @program: mycat->MycatServerConfigServiceImpl
 * @description:
 * @author: cg
 * @create: 2020-06-13 17:12
 **/
@Service
public class MycatServerConfigServiceImpl implements MycatServerConfigService {

    private static MycatConfig config;

    public MycatServerConfigServiceImpl() {
        config = MycatServer.getInstance().getConfig();
    }

    @Override
    public void saveUserItem(UserConfigDTO userConfigDTO, String oldName) {
        UserConfig userConfig = new UserConfig();
        UserPrivilegesConfig privilegesConfig = new UserPrivilegesConfig();

        BeanUtils.copyProperties(userConfigDTO.getPrivilegesConfig(), privilegesConfig);
        BeanUtils.copyProperties(userConfigDTO, userConfig);
        // 设置库权限
        userConfigDTO.getPrivilegesConfig().getSchemaPrivileges().forEach((s, schemaPrivilegeDTO) -> {
            UserPrivilegesConfig.SchemaPrivilege schemaPrivilege = new UserPrivilegesConfig.SchemaPrivilege();
            BeanUtils.copyProperties(schemaPrivilegeDTO, schemaPrivilege);
            schemaPrivilege.setDml(dmlToList(schemaPrivilegeDTO.getDmlList()));
            // 设置表权限
            schemaPrivilegeDTO.getTablePrivileges().forEach((s1, tablePrivilegeDTO) -> {
                UserPrivilegesConfig.TablePrivilege tablePrivilege = new UserPrivilegesConfig.TablePrivilege();
                BeanUtils.copyProperties(tablePrivilegeDTO, tablePrivilege);
                tablePrivilege.setDml(dmlToList(tablePrivilegeDTO.getDmlList()));
                schemaPrivilege.addTablePrivilege(tablePrivilege.getName(), tablePrivilege);
            });
            privilegesConfig.addSchemaPrivilege(schemaPrivilege.getName(), schemaPrivilege);
        });
        // 设置节点权限
        userConfigDTO.getPrivilegesConfig().getDataNodePrivileges().forEach((s, dataNodePrivilegeDTO) -> {
            UserPrivilegesConfig.DataNodePrivilege dataNodePrivilege = new UserPrivilegesConfig.DataNodePrivilege();
            BeanUtils.copyProperties(dataNodePrivilegeDTO, dataNodePrivilege);
            dataNodePrivilege.setDml(dmlToList(dataNodePrivilegeDTO.getDmlList()));
            privilegesConfig.addDataNodePrivileges(dataNodePrivilege.getName(), dataNodePrivilege);
        });
        userConfig.setPrivilegesConfig(privilegesConfig);
        Map<String, UserConfig> users = new HashMap<>();
        if (oldName!=null) {
            config.getUsers().forEach((s, config1) -> {
                if (!oldName.equals(s)) {
                    users.put(s, config1);
                }
            });
        } else {
            config.getUsers().forEach(users::put);
        }
        users.put(userConfig.getName(), userConfig);
        config.setUsers(users);

        // 修改xml
        Document doc = XMLLoaderUtils.getInstance();
        if (oldName!=null) {
            doc.getRootElement().elements("user").forEach(o -> {
                if (oldName.equals(((Element) o).attribute("name").getValue())) {
                    ((Element) o).detach();
                }
            });
        }
        Element userElement = doc.getRootElement().addElement("user");
        userElement.addAttribute("name", userConfig.getName());
        if (userConfig.isDefaultAccount()) {
            userElement.addAttribute("defaultAccount", "true");
        }
        if (!"".equals(userConfig.getPassword())&&userConfig.getPassword()!=null) {
            userElement.addElement("property").addAttribute("name", "password").setText(userConfig.getPassword());
        }
        if (!userConfig.getSchemas().isEmpty()) {
            userElement.addElement("property").addAttribute("name", "schemas").setText(StringUtils.join(userConfig.getSchemas().toArray(), ","));
        }
        if (!"".equals(userConfig.getDefaultSchema())&&userConfig.getDefaultSchema()!=null) {
            userElement.addElement("property").addAttribute("name", "defaultSchema").setText(userConfig.getDefaultSchema());
        }
        if (userConfig.isReadOnly()) {
            userElement.addElement("property").addAttribute("name", "readOnly").setText("true");
        }
        if (userConfig.getBenchmark()!=0) {
            userElement.addElement("property").addAttribute("name", "benchmark").setText(userConfig.getBenchmark()+"");
        }
        if (userConfig.getPrivilegesConfig()!=null&&userConfig.getPrivilegesConfig().isCheck()) {
            Element privilegesElement = userElement.addElement("privileges").addAttribute("check", "true");
            Map<String, UserPrivilegesConfigDTO.SchemaPrivilegeDTO> schemaPrivileges = userConfigDTO.getPrivilegesConfig().getSchemaPrivileges();
            Map<String, UserPrivilegesConfigDTO.DataNodePrivilegeDTO> dataNodePrivileges = userConfigDTO.getPrivilegesConfig().getDataNodePrivileges();
            if (schemaPrivileges.size()!=0) {
                schemaPrivileges.forEach((s, schemaPrivilege) -> {
                    Element schemaElement = privilegesElement.addElement("schema").addAttribute("name", schemaPrivilege.getName())
                            .addAttribute("dml", StringUtils.join(schemaPrivilege.getDmlList(), ","));
                    schemaPrivilege.getTablePrivileges().forEach((s1, tablePrivilege) -> {
                        schemaElement.addElement("table").addAttribute("name", tablePrivilege.getName())
                                .addAttribute("dml", StringUtils.join(tablePrivilege.getDmlList(), ","));
                    });
                });
            }
            if (dataNodePrivileges.size()!=0) {
                dataNodePrivileges.forEach((s, dataNodePrivilege) -> {
                    privilegesElement.addElement("dataNode").addAttribute("name", dataNodePrivilege.getName())
                            .addAttribute("dml", StringUtils.join(dataNodePrivilege.getDmlList(), ","));
                });
            }
        }
        XMLLoaderUtils.save(doc);
    }

    private static int[] dmlToList(List<String> dmlList) {
        List<Integer> dmlArray = new ArrayList();
        for (int i = 0; i < dmlList.size(); i++) {
            for (int offset1 = 0; offset1 < dmlList.get(i).length(); offset1++) {
                dmlArray.add(Character.getNumericValue(dmlList.get(i).charAt(offset1)));
            }
            if ((i + 1) != dmlList.size()) {
                dmlArray.add(-1);
            }
        }
        return dmlArray.stream().mapToInt(Integer::valueOf).toArray();
    }

    @Override
    public void deleUserItem(String key) {
        Map<String, UserConfig> map = new HashMap<>();
        config.getUsers().forEach((s, userConfig) -> {
            if (!key.equals(s)) {
                map.put(s, userConfig);
            }
        });
        config.setUsers(map);

        Document doc = XMLLoaderUtils.getInstance();
        doc.getRootElement().elements("user").forEach(o -> {
            if (key.equals(((Element) o).attribute("name").getValue())) {
                ((Element) o).detach();
            }
        });
        XMLLoaderUtils.save(doc);
    }

    @Override
    public SystemConfig getSystemConfig() {
        return config.getSystem();
    }

    @Override
    public Map<String, UserConfig> getUsersConfig() {
        return config.getUsers();
    }

    @Override
    public FirewallConfig getAllFirewallConfig() {
        return config.getFirewall();
    }

    @Override
    public void deleWhiteHostItem(String key) {
        FirewallConfig firewall = config.getFirewall();
        firewall.getWhitehost().remove(key);

        String host = FirewallConfig.getHost(Pattern.compile(key));
        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("firewall").element("whitehost").elements();
        elements.forEach(element -> {
            Attribute attribute = element.attribute("host");
            if (host.equals(attribute.getValue())) {
                element.detach();
            }
        });
        XMLLoaderUtils.save(doc);
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

        String host = FirewallConfig.getHost(Pattern.compile(key));
        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("firewall").element("whitehost").elements();
        elements.forEach(element -> {
            Attribute attribute = element.attribute("host");
            if (host.equals(attribute.getValue())) {
                element.detach();
            }
        });
        XMLLoaderUtils.save(doc);
    }

    @Override
    public void deleBlackItem(String key, String value) {
        FirewallConfig firewall = config.getFirewall();
        firewall.getBlacklist().remove(key, value);

        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("firewall").element("blacklist").elements();
        elements.forEach(element -> {
            if (key.equals(element.attribute("name").getValue())) {
                element.detach();
            }
        });
        XMLLoaderUtils.save(doc);
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

        Document doc = XMLLoaderUtils.getInstance();
        Element element = doc.getRootElement().element("firewall").element("whitehost").addElement("host");
        element.addAttribute("host", FirewallConfig.getHost(Pattern.compile(key)));
        element.addAttribute("user", String.join(",", names));
        XMLLoaderUtils.save(doc);
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

        Document doc = XMLLoaderUtils.getInstance();
        Element element = doc.getRootElement().element("firewall").element("whitehost").addElement("host");
        element.addAttribute("host", FirewallConfig.getHost(Pattern.compile(key)));
        element.addAttribute("user", String.join(",", names));
        XMLLoaderUtils.save(doc);
        return true;
    }

    @Override
    public void addBlackItem(String key, String value) {
        FirewallConfig firewall = config.getFirewall();
        firewall.getBlacklist().put(key, value);

        Document doc = XMLLoaderUtils.getInstance();
        Element element = doc.getRootElement().element("firewall").element("blacklist").addElement("property");
        element.addAttribute("name", key);
        element.setText(value);
        XMLLoaderUtils.save(doc);
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

        String oldHost = FirewallConfig.getHost(Pattern.compile(oldKey));
        String host = FirewallConfig.getHost(Pattern.compile(key));
        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("firewall").element("whitehost").elements();
        elements.forEach(element -> {
            Attribute attribute = element.attribute("host");
            if (oldHost.equals(attribute.getValue())) {
                attribute.setValue(host);
                element.addAttribute("user", String.join(",", names));
            }
        });
        XMLLoaderUtils.save(doc);
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

        String oldHost = FirewallConfig.getHost(Pattern.compile(oldKey));
        String host = FirewallConfig.getHost(Pattern.compile(key));
        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("firewall").element("whitehost").elements();
        elements.forEach(element -> {
            Attribute attribute = element.attribute("host");
            if (oldHost.equals(attribute.getValue())) {
                attribute.setValue(host);
                element.addAttribute("user", String.join(",", names));
            }
        });
        XMLLoaderUtils.save(doc);
    }

    @Override
    public void editBlackItem(String key, String value) {
        config.getFirewall().getBlacklist().entrySet().forEach(stringObjectEntry -> {
            if (stringObjectEntry.getKey().equals(key)) {
                stringObjectEntry.setValue(value);
            }
        });
        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("firewall").element("blacklist").elements();
        elements.forEach(element -> {
            if (key.equals(element.attribute("name").getValue())) {
                element.setText(value);
            }
        });
        XMLLoaderUtils.save(doc);
    }

    @Override
    public boolean editSystemConfig(String key, String value) {
        Document doc = XMLLoaderUtils.getInstance();
        List<Element> elements = doc.getRootElement().element("system").elements();
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
        elements.forEach(element -> {
            if (key.equals(element.attribute("name").getValue())) {
                element.setText(value);
            }
        });
        XMLLoaderUtils.save(doc);
        return true;
    }
}
