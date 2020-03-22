package com.weishao.dbswitch.dbwriter;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import java.lang.reflect.Constructor;

/**
 * 数据库写入器构造工厂类
 * 
 * @author tang
 *
 */
public class DatabaseWriterFactory {

	private static final Map<String, String> DATABASE_WRITER_MAPPER = new HashMap<String, String>() {

		private static final long serialVersionUID = 3365136872693503697L;

		{
			put("MYSQL", "com.weishao.dbswitch.dbwriter.mysql.MySqlWriterImpl");
			put("ORACLE", "com.weishao.dbswitch.dbwriter.oracle.OracleWriterImpl");
			put("SQLSERVER", "com.weishao.dbswitch.dbwriter.mssql.SqlServrerWriterImpl");
			put("POSTGRESQL", "com.weishao.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl");
			put("GREENPLUM", "com.weishao.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl");
		}
	};

	/**
	 * 获取指定数据库类型的写入器
	 * 
	 * @param dataSource 连接池数据源
	 * @return 写入器对象
	 */
	public static AbstractDatabaseWriter createDatabaseWriter(DataSource dataSource) {
		return DatabaseWriterFactory.createDatabaseWriter(dataSource, false);
	}

	/**
	 * 获取指定数据库类型的写入器
	 * 
	 * @param dataSource 连接池数据源
	 * @param insert     对于GP/GP数据库来说是否使用insert引擎写入
	 * @return 写入器对象
	 */
	public static AbstractDatabaseWriter createDatabaseWriter(DataSource dataSource, boolean insert) {
		String type = DatabaseWriterFactory.getDatabaseNameByDataSource(dataSource).toUpperCase();
		if (insert) {
			if (type.equals("POSTGRESQL") || type.equals("GREENPLUM")) {
				return new com.weishao.dbswitch.dbwriter.gpdb.GreenplumInsertWriterImpl(dataSource);
			}
		}

		if (DATABASE_WRITER_MAPPER.containsKey(type.trim())) {
			String className = DATABASE_WRITER_MAPPER.get(type);
			try {
				Class<?>[] paraTypes = { DataSource.class };
				Object[] paraValues = { dataSource };
				Class<?> clas = Class.forName(className);
				Constructor<?> cons = clas.getConstructor(paraTypes);
				AbstractDatabaseWriter process = (AbstractDatabaseWriter) cons.newInstance(paraValues);
				return process;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("[dbwrite] Unkown Supported database type (%s)", type));
	}

	/**
	 * 根据DataSource获取数据库的类型
	 * 
	 * @param dataSource 数据库源
	 * @return 数据库的类型：mysql/oracle/postgresql/greenplum
	 */
	private static String getDatabaseNameByDataSource(DataSource dataSource) {
		try {
			String productName = JdbcUtils.commonDatabaseName(
					JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName").toString());
			if (productName.equalsIgnoreCase("Greenplum")) {
				return "greenplum";
			} else if (productName.equalsIgnoreCase("Microsoft SQL Server")) {
				return "sqlserver";
			}

			DatabaseDriver databaseDriver = DatabaseDriver.fromProductName(productName);
			if (databaseDriver == DatabaseDriver.UNKNOWN) {
				throw new IllegalStateException("[dbwrite] Unable to detect database type from data source instance");
			}
			return databaseDriver.getId();
		} catch (MetaDataAccessException ex) {
			throw new IllegalStateException("[dbwrite] Unable to detect database type", ex);
		}
	}
}
