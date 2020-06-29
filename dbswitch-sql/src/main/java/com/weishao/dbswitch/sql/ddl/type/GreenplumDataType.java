package com.weishao.dbswitch.sql.ddl.type;

import java.sql.Types;

/**
 * PostgreSQL的数据类型
 * 
 * 参考地址：https://www.yiibai.com/postgresql/postgresql-datatypes.html
 * 
 * @author tang
 *
 */
public enum GreenplumDataType {

	//~~~~~整型类型~~~~~~~~
	SMALLINT(0,Types.SMALLINT),
	INT2(1,Types.SMALLINT),
	INTEGER(2,Types.INTEGER),
	INT4(3,Types.INTEGER),
	BIGINT(4,Types.BIGINT),
	INT8(5,Types.BIGINT),
	DECIMAL(6,Types.DECIMAL),
	NUMERIC(7,Types.NUMERIC),
	REAL(8,Types.REAL),//equal float4
	FLOAT4(9,Types.FLOAT),
	DOUBLE(10,Types.DOUBLE),
	FLOAT8(11,Types.DOUBLE),
	SMALLSERIAL(12,Types.SMALLINT),
	SERIAL2(13,Types.SMALLINT),
	SERIAL(14,Types.INTEGER),
	SERIAL4(15,Types.INTEGER),
	BIGSERIAL(16,Types.BIGINT),
	SERIAL8(17,Types.BIGINT),
	
	//~~~~~日期和时间类型~~~~~~~~
	DATE(18,Types.DATE),
	TIME(19,Types.TIME),
	TIMESTAMP(20,Types.TIMESTAMP),
	
	//~~~~~字符串类型~~~~~~~~
	CHAR(21,Types.CHAR),
	VARCHAR(22,Types.VARCHAR),
	TEXT(23,Types.CLOB),
	BYTEA(24,Types.BLOB);
	
	private int index;
	private int jdbctype;

	GreenplumDataType(int idx,int jdbcType) {
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
