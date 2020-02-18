package com.weishao.dbswitch.sql.ddl.sql.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import com.weishao.dbswitch.sql.ddl.DatabaseDialect;
import com.weishao.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.sql.ddl.type.MySqlDataType;

/**
 * 关于MySQL的的自增列问题：
 * （1）一张表中，只能有一列为自增长列。
 * （2）列的数据类型，必须为数值型。
 * （3）不能设置默认值。
 * （4）会自动应用not null。
 * 
 * @author tang
 *
 */
public class MySqlDialectImpl extends DatabaseDialect {
	
	private static List<MySqlDataType> integerTypes;
	
	static{
		integerTypes= new ArrayList<MySqlDataType>();
		integerTypes.add(MySqlDataType.TINYINT);
		integerTypes.add(MySqlDataType.SMALLINT);
		integerTypes.add(MySqlDataType.MEDIUMINT);
		integerTypes.add(MySqlDataType.INTEGER);
		integerTypes.add(MySqlDataType.INT);
		integerTypes.add(MySqlDataType.BIGINT);
	}

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
		
		if(column.isAutoIncrement()) {
			if(!MySqlDialectImpl.integerTypes.contains(type)) {
				throw new RuntimeException(String.format("Invalid MySQL auto increment data type: %s", column.getColumnType()));
			}
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

		if (column.isAutoIncrement() && column.isPrimaryKey() ) {
			//在MySQL数据库里只有主键是自增的
			sb.append(" NOT NULL AUTO_INCREMENT ");
		} else {
			if (nullable) {
				sb.append(" DEFAULT NULL");
			} else if (Objects.nonNull(defaultValue) && !defaultValue.isEmpty()) {
				if (defaultValue.toUpperCase().equals("NULL")) {
					sb.append(" DEFAULT NULL");
<<<<<<< HEAD
=======
				} else if (defaultValue.toUpperCase().trim().startsWith("CURRENT_TIMESTAMP")) {
					// 处理时间字段的默认当前时间问题
					sb.append(String.format(" DEFAULT %s", defaultValue));
>>>>>>> 4a13e121a4301e8dd0193879083b9422830635fe
				} else {
					sb.append(String.format(" DEFAULT '%s'", defaultValue));
				}
			} else {
				sb.append(" NOT NULL");
			}
		}

		if(Objects.nonNull(comment) && !comment.isEmpty()) {
			sb.append(String.format(" COMMENT '%s'", comment));
		}
		
		return sb.toString();
	}

}
