package com.weishao.dbswitch.constant;

/**
 * 数据库类型的枚举定义
 * @author Tang
 *
 */
public enum DatabaseType {
	UNKOWN(0), 
	MYSQL(1), 
	ORACLE(2), 
	SQLSERVER(3), 
	POSTGRESQL(4),
	GREENPLUM(5);

	private int index;

	DatabaseType(int idx) {
		this.index = idx;
	}

	public int getIndex() {
		return index;
	}
	
}
