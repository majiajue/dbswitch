package com.weishao.dbswitch.ddl.type;

import java.sql.Types;

/**
 * PostgreSQL的数据类型
 * 
 * 参考地址：https://www.yiibai.com/postgresql/postgresql-datatypes.html
 * 
 * @author tang
 *
 */
public enum PostgresDataType {

	//~~~~~整型类型~~~~~~~~
	SMALLINT(0,Types.SMALLINT),
	INT2(0,Types.SMALLINT),
	INTEGER(1,Types.INTEGER),
	INT4(0,Types.INTEGER),
	BIGINT(2,Types.BIGINT),
	INT8(0,Types.BIGINT),
	DECIMAL(3,Types.DECIMAL),
	NUMERIC(4,Types.NUMERIC),
	REAL(5,Types.REAL),
	DOUBLE(6,Types.DOUBLE),
	SERIAL(7,Types.INTEGER),
	BIGSERIAL(8,Types.BIGINT),
	
	//~~~~~日期和时间类型~~~~~~~~
	DATE(9,Types.DATE),
	TIME(10,Types.TIME),
	TIMESTAMP(11,Types.TIMESTAMP),
	
	//~~~~~字符串类型~~~~~~~~
	CHAR(12,Types.CHAR),
	VARCHAR(13,Types.VARCHAR),
	TEXT(14,Types.CLOB),
	BYTEA(15,Types.BLOB);
	
	private int index;
	private int jdbctype;

	PostgresDataType(int idx,int jdbcType) {
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
