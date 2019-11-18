package com.weishao.dbswitch.model;

import com.weishao.dbswitch.constant.DBTableType;

/**
 * 数据库表描述符信息定义(Table Description)
 * @author tang
 *
 */
public class TableDescription {

	private String tableName;
	private String schemaName;
	private String remarks;
	private DBTableType tableType;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public String getRemarks() {
		return this.remarks;
	}
	
	public void setRemarks(String remarks) {
		this.remarks=remarks;
	}

	public String getTableType() {
		return tableType.name();
	}

	public void setTableType(String tableType) {
		this.tableType = DBTableType.valueOf(tableType.toUpperCase());
	}

}
