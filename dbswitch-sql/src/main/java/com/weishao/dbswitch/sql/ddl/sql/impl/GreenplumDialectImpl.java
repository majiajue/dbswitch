package com.weishao.dbswitch.sql.ddl.sql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.weishao.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.sql.ddl.type.GreenplumDataType;

public class GreenplumDialectImpl extends PostgresDialectImpl {

	protected static List<GreenplumDataType> integerTypes;

	static {
		integerTypes = new ArrayList<GreenplumDataType>();
		integerTypes.add(GreenplumDataType.SERIAL2);
		integerTypes.add(GreenplumDataType.SERIAL4);
		integerTypes.add(GreenplumDataType.SERIAL8);
		integerTypes.add(GreenplumDataType.SMALLSERIAL);
		integerTypes.add(GreenplumDataType.SERIAL);
		integerTypes.add(GreenplumDataType.BIGSERIAL);
	}

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
		
		if(column.isAutoIncrement()) {
			if(!GreenplumDialectImpl.integerTypes.contains(type)) {
				throw new RuntimeException(String.format("Invalid Greenplum auto increment data type: %s", column.getColumnType()));
			}
		}

		sb.append(type.name());
		switch (type) {
		case NUMERIC:
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
			break;
		case TIMESTAMP:
			if (Objects.isNull(length) || length < 0) {
				sb.append(String.format(" (0) "));
			} else if (0 == length || 6 == length) {
				sb.append(String.format(" (%d) ", length));
			} else {
				throw new RuntimeException(
						String.format("Invalid Greenplum data type length: %s(%d)", column.getColumnType(), length));
			}
			break;
		case DOUBLE:
			sb.append(" PRECISION ");
			break;
		default:
			break;
		}

		return sb.toString();
	}

}
