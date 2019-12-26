package com.weishao.dbswitch.common.util;

import com.weishao.dbswitch.common.constant.DatabaseType;

public class CommonUtils {

	/**
	 * 根据tableName和schemaName转换为全名
	 * 
	 * @param dbtype                数据库类型
	 * @param schemaName     Schema名称
	 * @param tableName          Table表名称
	 * @return        表的全名称
	 */
	public static String getTableFullNameByDatabase(DatabaseType dbtype, String schemaName, String tableName) {
		if (dbtype==DatabaseType.MYSQL) {
			return String.format("`%s`.`%s`", schemaName, tableName);
		} else if (dbtype==DatabaseType.SQLSERVER || dbtype==DatabaseType.SQLSERVER2000) {
			return String.format("[%s].[%s]", schemaName, tableName);
		} else {
			return String.format("\"%s\".\"%s\"", schemaName, tableName);
		}
	}

	/**
	 * 获取数据库SQL中的引号字符
	 * @param dbtype
	 * @return
	 */
	public static String getQuotationChar(DatabaseType dbtype) {
		if (dbtype==DatabaseType.MYSQL) {
			return "`";
		}else {
			return "\"";
		}
	}
}
