package com.weishao.dbswitch.sql.ddl.sql.impl;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import com.weishao.dbswitch.sql.ddl.DatabaseDialect;
import com.weishao.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.sql.ddl.type.MySqlDataType;

public class MySqlDialectImpl extends DatabaseDialect {

	@Override
	public String getSchemaTableName(String schemaName, String tableName) {
		if(Objects.isNull(schemaName) || schemaName.trim().isEmpty()) {
			return String.format("`%s`", tableName);
		}
		return String.format("`%s`.`%s`", schemaName,tableName);
	}
	
	@Override
	public String getQuoteFieldName(String fieldName) {
		return String.format("`%s`", fieldName.trim());
	}
	
	@Override
	public  String getPrimaryKeyAsString(List<String> pks) {
		if(pks.size()>0) {
			StringBuilder sb = new StringBuilder();
			sb.append("`");
			sb.append(StringUtils.join(pks, "` , `"));
			sb.append("`");
			return sb.toString();
		}
		
		return "";
	}
	
	@Override
	public String getFieldTypeName(ColumnDefinition column) {
		int length = column.getLengthOrPrecision();
		int scale = column.getScale();
		StringBuilder sb = new StringBuilder();
		MySqlDataType type = null;
		try {
			type = MySqlDataType.valueOf(column.getColumnType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("Invalid MySQL data type: %s", column.getColumnType()));
		}

		sb.append(type.name());
		switch (type) {
		case FLOAT:
		case DOUBLE:
		case DECIMAL:
			if (Objects.isNull(length) || length < 0) {
				throw new RuntimeException(
						String.format("Invalid MySQL data type length: %s(%d)", column.getColumnType(), length));
			}
			
			if (Objects.isNull(scale) || scale < 0) {
				throw new RuntimeException(
						String.format("Invalid MySQL data type scale: %s(%d,%d)", column.getColumnType(), length, scale));
			}
			
			sb.append(String.format("(%d,%d)", length,scale));
			break;
		case TINYINT:
		case SMALLINT:
		case MEDIUMINT:
		case INTEGER:
		case INT:
		case BIGINT:
		case CHAR:
		case VARCHAR:
			if (Objects.isNull(length) || length < 0) {
				throw new RuntimeException(
						String.format("Invalid MySQL data type length: %s(%d)", column.getColumnType(), length));
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
		String comment=column.getColumnComment();

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("`%s` ",fieldname.trim()));
		sb.append(this.getFieldTypeName(column));

		if (nullable) {
			sb.append(" DEFAULT NULL");
		} else if (Objects.nonNull(defaultValue) && !defaultValue.isEmpty()) {
			if (defaultValue.toUpperCase().equals("NULL")) {
				sb.append(" DEFAULT NULL");
			} else {
				sb.append(String.format(" DEFAULT '%s'", defaultValue));
			}
		}else {
			sb.append(" NOT NULL");
		}

		if(Objects.nonNull(comment) && !comment.isEmpty()) {
			sb.append(String.format(" COMMENT '%s'", comment));
		}
		
		return sb.toString();
	}

}
