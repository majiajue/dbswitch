package com.weishao.dbswitch.ddl.sql.impl;

import java.util.Objects;
import com.weishao.dbswitch.ddl.DatabaseDialect;
import com.weishao.dbswitch.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.ddl.type.PostgresDataType;

public class PostgresDialectImpl  extends DatabaseDialect {

	@Override
	public String getFieldTypeName(ColumnDefinition column) {
		int length = column.getLengthOrPrecision();
		int scale = column.getScale();
		
		StringBuilder sb = new StringBuilder();
		PostgresDataType type = null;
		try {
			type = PostgresDataType.valueOf(column.getColumnType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("Invalid PostgreSQL data type: %s", column.getColumnType()));
		}

		sb.append(type.name());
		switch (type) {
		case DECIMAL:
			if (Objects.isNull(length) || length < 0) {
				throw new RuntimeException(
						String.format("Invalid Greenplum data type length: %s(%d)", column.getColumnType(), length));
			}
			
			if (Objects.isNull(scale) || scale < 0) {
				throw new RuntimeException(
						String.format("Invalid Greenplum data type scale: %s(%d,%d)", column.getColumnType(), length, scale));
			}
			
			sb.append(String.format("(%d,%d)", length,scale));
			break;
		case CHAR:
		case VARCHAR:
			if (Objects.isNull(length) || length < 0) {
				throw new RuntimeException(
						String.format("Invalid Greenplum data type length: %s(%d)", column.getColumnType(), length));
			}
			sb.append(String.format(" (%d) ", length));
		default:
			break;
		}

		return sb.toString();
	}
	
	@Override
	public String getFieldDefination(ColumnDefinition column) {
		String fieldname = column.getColumnName();
		boolean nullable = column.isNullable();
		String defaultValue = column.getDefaultValue();
		//String comment=column.getColumnComment();

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\"%s\" ",fieldname.trim()));
		sb.append(this.getFieldTypeName(column));

		if (nullable) {
			sb.append(" DEFAULT NULL");
		} else if (Objects.nonNull(defaultValue) &&!defaultValue.isEmpty()) {
			if (defaultValue.toUpperCase().equals("NULL")) {
				sb.append(" DEFAULT NULL");
			} else {
				sb.append(String.format(" DEFAULT '%s'", defaultValue));
			}
		}else {
			sb.append(" NOT NULL");
		}
		
		return sb.toString();
	}

}
