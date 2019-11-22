package com.weishao.dbswitch.ddl.type;

import java.sql.Types;

/**
 * MySQL的数据类型
 * 
 * 参考地址：https://www.yiibai.com/mysql/data-types.html
 * 
 * @author tang
 *
 */
public enum MySqlDataType {
	
	//~~~~~整型类型~~~~~~~~
	TINYINT(0,Types.TINYINT),
	SMALLINT(1,Types.SMALLINT),
	MEDIUMINT(2,Types.INTEGER),
	INTEGER(3,Types.INTEGER),
	INT(4,Types.INTEGER),
	BIGINT(5,Types.BIGINT),
	FLOAT(6,Types.FLOAT),
	DOUBLE(7,Types.DOUBLE),
	DECIMAL(8,Types.DECIMAL),
	
	//~~~~~日期和时间类型~~~~~~~~
	DATE(8,Types.DATE),
	TIME(9,Types.TIME),
	YEAR(10,Types.DATE),
	DATETIME(11,Types.TIMESTAMP),
	TIMESTAMP(12,Types.TIMESTAMP),
	
	//~~~~~字符串类型~~~~~~~~
	CHAR(13,Types.CHAR),
	VARCHAR(14,Types.VARCHAR),
	TINYBLOB(15,Types.VARBINARY),
	TINYTEXT(16,Types.CLOB),
	BLOB(17,Types.VARBINARY),
	TEXT(18,Types.CLOB),
	MEDIUMBLOB(19,Types.LONGVARBINARY),
	MEDIUMTEXT(20,Types.LONGVARCHAR),
	LONGBLOB(21,Types.LONGVARBINARY),
	LONGTEXT(22,Types.LONGVARCHAR);
	
	private int index;
	private int jdbctype;

	MySqlDataType(int idx,int jdbcType) {
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
