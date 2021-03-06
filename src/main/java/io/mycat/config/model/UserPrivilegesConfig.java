package io.mycat.config.model;

import java.util.*;

/**
 * 用户 SQL 权限配置
 *
 * @author zhuam
 *
 */
public class UserPrivilegesConfig {

	private boolean check = false;

	/** 库级权限 */
	private Map<String, SchemaPrivilege> schemaPrivileges = new HashMap<String, SchemaPrivilege>();

	/** dataNode权限 */
	private Map<String, DataNodePrivilege> dataNodePrivileges = new HashMap<String, DataNodePrivilege>();

	public List<SchemaPrivilege> getSchemaPrivileges() {
		List<SchemaPrivilege> list = new ArrayList<>();
		schemaPrivileges.forEach((s, schemaPrivilege) -> list.add(schemaPrivilege));
		return list;
	}

	public List<DataNodePrivilege> getDataNodePrivileges() {
		List<DataNodePrivilege> list = new ArrayList<>();
		dataNodePrivileges.forEach((s, dataNodePrivilege) -> list.add(dataNodePrivilege));
		return list;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public void addSchemaPrivilege(String schemaName, SchemaPrivilege privilege) {
		this.schemaPrivileges.put(schemaName, privilege);
	}

	public SchemaPrivilege getSchemaPrivilege(String schemaName) {
		return schemaPrivileges.get( schemaName );
	}

	public void addDataNodePrivileges(String dataNodeName, DataNodePrivilege privilege) {
		this.dataNodePrivileges.put(dataNodeName, privilege);
	}

	public DataNodePrivilege getDataNodePrivilege(String dataNodeName) {
		return dataNodePrivileges.get(dataNodeName);
	}

	/**
	 * 库级权限
	 */
	public static class SchemaPrivilege {
		
		private String name;
		private int[] dml = new int[]{0, 0, 0, 0};

		private Map<String, TablePrivilege> tablePrivileges = new HashMap<String, TablePrivilege>();

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

		public void addTablePrivilege(String tableName, TablePrivilege privilege) {
			this.tablePrivileges.put(tableName, privilege);
		}

		public TablePrivilege getTablePrivilege(String tableName) {
			TablePrivilege tablePrivilege = tablePrivileges.get( tableName );
			if ( tablePrivilege == null ) {
				tablePrivilege = new TablePrivilege();
				tablePrivilege.setName(tableName);
				tablePrivilege.setDml(dml);
			}
			return tablePrivilege;
		}

		public List<TablePrivilege> getTablePrivileges() {
			List<TablePrivilege> list = new ArrayList<>();
			tablePrivileges.forEach((s, tablePrivilege) -> list.add(tablePrivilege));
			return list;
		}
	}

	/**
	 * 表级权限
	 */
	public static class TablePrivilege {

		private String name;
		private int[] dml = new int[] { 0, 0, 0, 0 };

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

	}

	/**
	 * dataNode权限
	 */
	public static class DataNodePrivilege {

		private String name;
		private int[] dml = new int[] { 0, 0, 0, 0 };

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
	}
}