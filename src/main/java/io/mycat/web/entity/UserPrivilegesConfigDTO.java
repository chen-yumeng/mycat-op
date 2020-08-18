package io.mycat.web.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: Mycat->UserPrivilegesConfigDTO
 * @description:
 * @author: cg
 * @create: 2020-08-18 14:40
 **/
public class UserPrivilegesConfigDTO implements Serializable {

    private boolean check = false;

    /** 库级权限 */
    private Map<String, SchemaPrivilegeDTO> schemaPrivileges = new HashMap<>();

    /** dataNode权限 */
    private Map<String, DataNodePrivilegeDTO> dataNodePrivileges = new HashMap<>();

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public Map<String, SchemaPrivilegeDTO> getSchemaPrivileges() {
        return schemaPrivileges;
    }

    public void setSchemaPrivileges(Map<String, SchemaPrivilegeDTO> schemaPrivileges) {
        this.schemaPrivileges = schemaPrivileges;
    }

    public Map<String, DataNodePrivilegeDTO> getDataNodePrivileges() {
        return dataNodePrivileges;
    }

    public void setDataNodePrivileges(Map<String, DataNodePrivilegeDTO> dataNodePrivileges) {
        this.dataNodePrivileges = dataNodePrivileges;
    }

    @Override
    public String toString() {
        return "UserPrivilegesConfigDTO{" +
                "check=" + check +
                ", schemaPrivileges=" + schemaPrivileges +
                ", dataNodePrivileges=" + dataNodePrivileges +
                '}';
    }

    public static class SchemaPrivilegeDTO {

        private String name;
        private int[] dml = new int[]{0, 0, 0, 0};
        private List<String> dmlList;

        private Map<String, TablePrivilegeDTO> tablePrivileges = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int[] getDml() {
            return dml;
        }

        public void setDml(int[] dml) {
            this.dml = dml;
        }

        public List<String> getDmlList() {
            return dmlList;
        }

        public void setDmlList(List<String> dmlList) {
            this.dmlList = dmlList;
        }

        public Map<String, TablePrivilegeDTO> getTablePrivileges() {
            return tablePrivileges;
        }

        public void setTablePrivileges(Map<String, TablePrivilegeDTO> tablePrivileges) {
            this.tablePrivileges = tablePrivileges;
        }

        @Override
        public String toString() {
            return "SchemaPrivilegeDTO{" +
                    "name='" + name + '\'' +
                    ", dml=" + Arrays.toString(dml) +
                    ", dmlList=" + dmlList +
                    ", tablePrivileges=" + tablePrivileges +
                    '}';
        }
    }

    public  static class TablePrivilegeDTO {
        private String name;
        private int[] dml = new int[]{0, 0, 0, 0};
        private List<String> dmlList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int[] getDml() {
            return dml;
        }

        public void setDml(int[] dml) {
            this.dml = dml;
        }

        public List<String> getDmlList() {
            return dmlList;
        }

        public void setDmlList(List<String> dmlList) {
            this.dmlList = dmlList;
        }

        @Override
        public String toString() {
            return "TablePrivilegeDTO{" +
                    "name='" + name + '\'' +
                    ", dml=" + Arrays.toString(dml) +
                    ", dmlList=" + dmlList +
                    '}';
        }
    }

    public static class DataNodePrivilegeDTO {
        private String name;
        private int[] dml = new int[]{0, 0, 0, 0};
        private List<String> dmlList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int[] getDml() {
            return dml;
        }

        public void setDml(int[] dml) {
            this.dml = dml;
        }

        public List<String> getDmlList() {
            return dmlList;
        }

        public void setDmlList(List<String> dmlList) {
            this.dmlList = dmlList;
        }

        @Override
        public String toString() {
            return "DataNodePrivilegeDTO{" +
                    "name='" + name + '\'' +
                    ", dml=" + Arrays.toString(dml) +
                    ", dmlList=" + dmlList +
                    '}';
        }
    }

}
