package io.mycat.web.entity;

import java.io.Serializable;
import java.util.Set;

/**
 * @program: Mycat->UserConfigDTO
 * @description:
 * @author: cg
 * @create: 2020-08-18 14:38
 **/
public class UserConfigDTO implements Serializable {

    private String name;
    /**
     * 明文
     */
    private String password;
    /**
     * 密文
     */
    private String encryptPassword;
    /**
     * 负载限制, 默认0表示不限制
     */
    private int benchmark = 0;
    /**
     * SQL表级的增删改查权限控制
     */
    private UserPrivilegesConfigDTO privilegesConfig;
    private String defaultSchema;
    /**
     * 是否无密码登陆的默认账户
     */
    private boolean defaultAccount = false;
    private boolean readOnly = false;
    private Set<String> schemas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }

    public int getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(int benchmark) {
        this.benchmark = benchmark;
    }

    public UserPrivilegesConfigDTO getPrivilegesConfig() {
        return privilegesConfig;
    }

    public void setPrivilegesConfig(UserPrivilegesConfigDTO privilegesConfig) {
        this.privilegesConfig = privilegesConfig;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    @Override
    public String toString() {
        return "UserConfigDTO{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", encryptPassword='" + encryptPassword + '\'' +
                ", benchmark=" + benchmark +
                ", privilegesConfig=" + privilegesConfig +
                ", defaultSchema='" + defaultSchema + '\'' +
                ", defaultAccount=" + defaultAccount +
                ", readOnly=" + readOnly +
                ", schemas=" + schemas +
                '}';
    }
}
