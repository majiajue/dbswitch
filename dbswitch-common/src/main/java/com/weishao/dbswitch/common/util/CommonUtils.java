package com.weishao.dbswitch.common.util;

import java.util.List;
import com.weishao.dbswitch.common.constant.DatabaseType;

public class CommonUtils {

	public static String getTableFullNameByDatabase(DatabaseType dbtype, String schemaName, String tableName) {
		if (dbtype == DatabaseType.MYSQL) {
			return String.format("`%s`.`%s`", schemaName, tableName);
		} else if (dbtype == DatabaseType.SQLSERVER || dbtype == DatabaseType.SQLSERVER2000) {
			return String.format("[%s].[%s]", schemaName, tableName);
		} else {
			return String.format("\"%s\".\"%s\"", schemaName, tableName);
		}
	}

	public static String getSelectColumnsSQL(DatabaseType dbtype, String schema, String table, List<String> columns) {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT ");
		for (int i = 0; i < columns.size(); ++i) {
			String field = columns.get(i);
			String quoteField = QuoteString(dbtype, field);
			sb.append(quoteField);

			if (i < columns.size() - 1) {
				sb.append(",");
			}
		}
		sb.append(" FROM ");
		if (null != schema && !schema.isEmpty()) {
			sb.append(QuoteString(dbtype, schema));
			sb.append(".");
		}
		sb.append(QuoteString(dbtype, table));

		return sb.toString();
	}

	private static String QuoteString(DatabaseType dbtype, String keyName) {
		if (dbtype == DatabaseType.MYSQL) {
			return String.format("`%s`", keyName);
		} else if (dbtype == DatabaseType.SQLSERVER || dbtype == DatabaseType.SQLSERVER2000) {
			return String.format("[%s]", keyName);
		} else {
			return String.format("\"%s\"", keyName);
		}
	}

	public static String getQuotationChar(DatabaseType dbtype) {
		if (dbtype == DatabaseType.MYSQL) {
			return "`";
		} else {
			return "\"";
		}
	}
}
