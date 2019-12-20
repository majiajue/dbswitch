package com.weishao.dbswitch.common.constant;

/**
 * 数据库类型的枚举定义
 * @author Tang
 *
 */
public enum DatabaseType {
	UNKOWN(0), 
	MYSQL(1), 
	ORACLE(2), 
	SQLSERVER2000(3), 
	SQLSERVER(4), 
	POSTGRESQL(5),
	GREENPLUM(6);

	private int index;

	DatabaseType(int idx) {
		this.index = idx;
	}

	public int getIndex() {
		return index;
	}
	
}
