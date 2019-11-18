package com.weishao.dbswitch.database;

import java.util.List;
import com.weishao.dbswitch.model.ColumnDescription;
import com.weishao.dbswitch.model.ColumnMetaData;
import com.weishao.dbswitch.model.TableDescription;

/**
 * 数据库访问通用业务接口
 * @author tang
 *
 */
public interface IDatabaseInterface {
	
	/**
	 * 建立数据库连接
	 * @param jdbcurl      JDBC的URL连接字符串
	 * @param username  用户名
	 * @param password  密码
	 */
	public void Connect(String jdbcurl,String username,String password);
	
	/**
	 * 断开数据库连接
	 */
	public void Close();
	
	/**
	 * 获取数据库的模式schema列表
	 * @return  模式名列表
	 */
	public List<String> querySchemaList();
	
	/**
	 * 获取指定模式Schema内的所有表列表
	 * @param schemaName   模式名称
	 * @return   表及视图名列表
	 */
	public List<TableDescription> queryTableList(String schemaName);

	/**
	 * 获取指定模式表的元信息
	 * @param schemaName   模式名称
	 * @param tableName       表或视图名称
	 * @return    字段元信息列表
	 */
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName);
	
	/**
	 * 获取指定查询SQL的元信息
	 * @param sql   SQL查询语句
	 * @return    字段元信息列表
	 */
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql);

	/**
	 * 获取指定模式表的主键字段列表
	 * @param schemaName  模式名称
	 * @param tableName      表名称
	 * @return  主键字段名称列表
	 */
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName);
	
	/**
	 * 测试查询SQL语句的有效性
	 * @param sql  待验证的SQL语句
	 */
	public void testQuerySQL(String sql);
	
	/**
	 * 获取数据库的表全名
	 * @param schemaName   模式名称
	 * @param tableName        表名称
	 * @return 表全名
	 */
	public String getQuotedSchemaTableCombination(String schemaName, String tableName);
	
	/**
	 * 获取字段列的结构定义
	 * @param vmd  值元数据定义
	 * @param pks    主键字段名称列表
	 * @return  字段定义字符串
	 */
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean add_cr);
	
	/**
	 * 主键列转换为逗号分隔的字符串
	 * @param pks
	 * @return
	 */
	public  String getPrimaryKeyAsString(List<String> pks);
	
	/**
	 * SQL语句格式化
	 * @param sql   SQL的语句
	 * @return  格式化后的SQL语句
	 */
	public String formatSQL(String sql);
}
