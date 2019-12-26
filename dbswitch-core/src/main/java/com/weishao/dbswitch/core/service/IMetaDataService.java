package com.weishao.dbswitch.core.service;

import java.util.List;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.TableDescription;

/**
 * 元信息获取接口定义
 * 
 * @author tang
 *
 */
public interface IMetaDataService {

	/**
	 * 设置数据库连接的数据库类型
	 * 
	 * @param dbtype
	 */
	public void setDatabaseConnection(DatabaseType dbtype);

	/**
	 * 获取数据库的schema模式列表
	 * 
	 * @param jdbcUrl  数据库连接的JDBC-URL
	 * @param username 数据库连接的帐号
	 * @param password 数据库连接的密码
	 * @return
	 */
	public List<String> querySchemaList(String jdbcUrl, String username, String password);

	/**
	 * 获取指定Schema下所有的表列表
	 * 
	 * @param jdbcUrl    数据库连接的JDBC-URL
	 * @param username   数据库连接的帐号
	 * @param password   数据库连接的密码
	 * @param schemaName 模式名称
	 * @return
	 */
	public List<TableDescription> queryTableList(String jdbcUrl, String username, String password, String schemaName);

	/**
	 * 获取指定schema.table的表结构字段信息
	 * 
	 * @param jdbcUrl    数据库连接的JDBC-URL
	 * @param username   数据库连接的帐号
	 * @param password   数据库连接的密码
	 * @param schemaName 模式名称
	 * @param tableName  表或视图名称
	 * @return
	 */
	public List<ColumnDescription> queryTableColumnMeta(String jdbcUrl, String username, String password,
			String schemaName, String tableName);

	/**
	 * 获取指定SQL结构字段信息
	 * 
	 * @param jdbcUrl  数据库连接的JDBC-URL
	 * @param username 数据库连接的帐号
	 * @param password 数据库连接的密码
	 * @param querySql 查询的SQL语句
	 * @return
	 */
	public List<ColumnDescription> querySqlColumnMeta(String jdbcUrl, String username, String password,
			String querySql);

	/**
	 * 获取表的主键信息字段列表
	 * 
	 * @param jdbcUrl    数据库连接的JDBC-URL
	 * @param username   数据库连接的帐号
	 * @param password   数据库连接的密码
	 * @param schemaName
	 * @param tableName
	 * @return
	 */
	public List<String> queryTablePrimaryKeys(String jdbcUrl, String username, String password, String schemaName,
			String tableName);

	/**
	 * 测试数据库SQL查询
	 * 
	 * @param jdbcUrl  数据库连接的JDBC-URL
	 * @param username 数据库连接的帐号
	 * @param password 数据库连接的密码
	 * @param sql      待查询的SQL语句
	 */
	public void testQuerySQL(String jdbcUrl, String username, String password, String sql);

	/**
	 * 根据字段结构信息组装对应数据库的建表DDL语句
	 * 
	 * @param type        目的数据库类型
	 * @param fieldNames  字段结构信息
	 * @param primaryKeys 主键字段信息
	 * @param tableName   模式名称
	 * @param tableName   表名称
	 * @param ifNotExist  是否不存在才创建
	 * @return 对应数据库的DDL建表语句
	 */
	public String getDDLCreateTableSQL(DatabaseType type, List<ColumnDescription> fieldNames, List<String> primaryKeys,
			String schemaName, String tableName, boolean ifNotExist);
}
