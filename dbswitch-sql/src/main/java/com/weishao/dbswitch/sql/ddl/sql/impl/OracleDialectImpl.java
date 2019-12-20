package com.weishao.dbswitch.sql.ddl.sql.impl;

import java.util.Objects;
import com.weishao.dbswitch.sql.ddl.DatabaseDialect;
import com.weishao.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.sql.ddl.type.OracleDataType;

public class OracleDialectImpl  extends DatabaseDialect {

	@Override
	public String getFieldTypeName(ColumnDefinition column) {
		int length = column.getLengthOrPrecision();
		int scale = column.getScale();
		StringBuilder sb = new StringBuilder();
		OracleDataType type = null;
		try {
			type = OracleDataType.valueOf(column.getColumnType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("Invalid Oracle data type: %s", column.getColumnType()));
		}

		sb.append(type.name());
		switch (type) {
		case NUMBER:
			if (Objects.isNull(length) || length < 0) {
				throw new RuntimeException(
						String.format("Invalid Oracle data type length: %s(%d)", column.getColumnType(), length));
			}
			
			if (length > 0) {
				sb.append(String.format("(%d)", length));
			} else {
				if (Objects.isNull(scale) || scale < 0) {
					throw new RuntimeException(String.format("Invalid Oracle data type scale: %s(%d,%d)",
							column.getColumnType(), length, scale));
				}

				sb.append(String.format("(%d,%d)", length, scale));
			}
			break;
		case CHAR:
		case NCHAR:
		case VARCHAR:
		case VARCHAR2:
			if (Objects.isNull(length) || length < 0) {
				throw new RuntimeException(
						String.format("Invalid Oracle data type length: %s(%d)", column.getColumnType(), length));
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
