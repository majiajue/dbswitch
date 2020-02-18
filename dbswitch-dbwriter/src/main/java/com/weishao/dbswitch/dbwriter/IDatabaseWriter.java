package com.weishao.dbswitch.dbwriter;

import java.util.List;
import javax.sql.DataSource;

public interface IDatabaseWriter {

	/**
	 * 获取数据源对象
	 * 
	 * @return DataSource数据源对象
	 */
	public DataSource getDataSource();

	/**
	 * 批量写入预处理
	 * 
	 * @param schemaName schema名称
	 * @param tableName  table名称
	 */
	public void prepareWrite(String schemaName, String tableName);

	/**
	 * 批量数据写入
	 * 
	 * @param fieldNames   字段名称列表
	 * @param recordValues 数据记录
	 * @return 返回实际写入的数据记录条数
	 */
	public long write(List<String> fieldNames, List<Object[]> recordValues);
}
