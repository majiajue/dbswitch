package com.weishao.dbswitch.ddl.sql.impl;

import java.util.Objects;
import com.weishao.dbswitch.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.ddl.type.GreenplumDataType;

public class GreenplumDialectImpl extends PostgresDialectImpl {

	@Override
	public String getFieldTypeName(ColumnDefinition column) {
		int length = column.getLengthOrPrecision();
		int scale = column.getScale();
		
		StringBuilder sb = new StringBuilder();
		GreenplumDataType type = null;
		try {
			type = GreenplumDataType.valueOf(column.getColumnType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("Invalid Greenplum data type: %s", column.getColumnType()));
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

}
