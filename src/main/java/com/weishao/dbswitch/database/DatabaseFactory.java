package com.weishao.dbswitch.database;

import java.util.HashMap;
import java.util.Map;
import com.weishao.dbswitch.constant.DatabaseType;
import com.weishao.dbswitch.database.impl.DatabaseGreenplumImpl;
import com.weishao.dbswitch.database.impl.DatabaseMysqlImpl;
import com.weishao.dbswitch.database.impl.DatabaseOracleImpl;
import com.weishao.dbswitch.database.impl.DatabasePostgresImpl;
import com.weishao.dbswitch.database.impl.DatabaseSqlserverImpl;

/**
 * 数据库示例构建工厂类
 * @author tang
 *
 */
public class DatabaseFactory {
	
	private static final Map<DatabaseType,String> DATABASE_MAPPER=new HashMap<DatabaseType, String>(){
		
		private static final long serialVersionUID = 9202705534880971997L;

	{  
	      put(DatabaseType.MYSQL,DatabaseMysqlImpl.class.getName());
	      put(DatabaseType.ORACLE,DatabaseOracleImpl.class.getName());
	      put(DatabaseType.SQLSERVER,DatabaseSqlserverImpl.class.getName());
	      put(DatabaseType.POSTGRESQL,DatabasePostgresImpl.class.getName());
	      put(DatabaseType.GREENPLUM,DatabaseGreenplumImpl.class.getName());
	}}; 
	
	public static AbstractDatabase getDatabaseInstance(DatabaseType type) {
		if(DATABASE_MAPPER.containsKey(type)) {
			String className= DATABASE_MAPPER.get(type);
			try {
				return (AbstractDatabase) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException(String.format("Unkown database type (%s)",type.name()));
	}
}
