package com.weishao.dbswitch.dbwriter;

import java.util.Map;
import java.util.HashMap;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
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
			put("sqlserver", "com.weishao.dbswitch.dbwriter.mssql.SqlServrerWriterImpl");
			put("POSTGRESQL", "com.weishao.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl");
			put("GREENPLUM", "com.weishao.dbswitch.dbwriter.gpdb.GreenplumCopyWriterImpl");
		}
	};
	
	/**
	 * 获取指定数据库类型的写入器
	 * @param dataSource  DBCP连接池的数据源
	 * @return  写入器对象
	 */
	public static AbstractDatabaseWriter createDatabaseWriter(BasicDataSource dataSource) {
		return DatabaseWriterFactory.createDatabaseWriter(dataSource, false);
	}

	/**
	 * 获取指定数据库类型的写入器
	 * @param dataSource  DBCP连接池的数据源
	 * @param insert    对于GP数据库来说是否使用insert引擎写入
	 * @return  写入器对象
	 */
	public static AbstractDatabaseWriter createDatabaseWriter(BasicDataSource dataSource,boolean insert) {
		dataSource.setAccessToUnderlyingConnectionAllowed(true);
		String type=DatabaseWriterFactory.getDatabaseNameByDataSource(dataSource).toUpperCase();
		if (insert) {
			if (type.equals("POSTGRESQL") || type.equals("GREENPLUM")) {
				return new com.weishao.dbswitch.dbwriter.gpdb.GreenplumInsertWriterImpl(dataSource);
			}
		}
		
		if (DATABASE_WRITER_MAPPER.containsKey(type.trim())) {
			String className = DATABASE_WRITER_MAPPER.get(type);
			try {
				Class<?>[] paraTypes = {DataSource.class};
				Object[] paraValues = {dataSource};
				Class<?> clas = Class.forName(className);
				Constructor<?> cons = clas.getConstructor(paraTypes);
				AbstractDatabaseWriter process = (AbstractDatabaseWriter)cons.newInstance(paraValues);
				return process;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("Unkown database type (%s)", type));
	}
	
	/**
	 * 根据DataSource获取数据库的类型
	 * @param dataSource  数据库DataSource对象
	 * @return  数据库的类型：mysql/oracle/postgresql/greenplum
	 */
	public static String getDatabaseNameByDataSource(BasicDataSource dataSource) {
		String driverClassName=dataSource.getDriverClassName();
		if(driverClassName.contains("mysql")) {
			return "mysql";
		}else if(driverClassName.contains("oracle")) {
			return "oracle";
		}else if(driverClassName.contains("postgresql")) {
			return "postgresql";
		}else if(driverClassName.contains("Greenplum")) {
			return "greenplum";
		}else if (driverClassName.contains("sqlserver")) {
			return "sqlserver";
		}else {
			throw new RuntimeException(String.format("Unsupport database type by driver class name [%s]",driverClassName));
		}
	}
}
