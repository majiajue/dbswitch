package com.weishao.dbswitch.ddl.type;

import java.sql.Types;

/**
 * Oracle的数据类型
 * 
 * 参考地址：http://blog.itpub.net/26736162/viewspace-2149685
 * 
 * @author tang
 *
 */
public enum OracleDataType {

	//~~~~~整型类型~~~~~~~~
	NUMBER(1,Types.NUMERIC),
	
	//~~~~~日期和时间类型~~~~~~~~
	DATE(2,Types.DATE),
	TIMESTAMP(3,Types.TIMESTAMP),
	
	//~~~~~字符串类型~~~~~~~~
	CHAR(4,Types.CHAR),
	NCHAR(5,Types.CHAR),
	VARCHAR(6,Types.VARCHAR),
	VARCHAR2(7,Types.VARCHAR),
	LONG(8,Types.LONGVARBINARY),
	CLOB(9,Types.CLOB),
	BLOB(10,Types.BLOB);
	
	private int index;
	private int jdbctype;

	OracleDataType(int idx,int jdbcType) {
		this.index = idx;
		this.jdbctype=jdbcType;
	}

	public int getIndex() {
		return index;
	}
	
	public int getJdbcType() {
		return this.jdbctype;
	}
	
}
