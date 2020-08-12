package com.weishao.dbswitch.core.database;

import java.util.HashMap;
import java.util.Map;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.database.impl.DatabaseGreenplumImpl;
import com.weishao.dbswitch.core.database.impl.DatabaseMysqlImpl;
import com.weishao.dbswitch.core.database.impl.DatabaseOracleImpl;
import com.weishao.dbswitch.core.database.impl.DatabasePostgresImpl;
import com.weishao.dbswitch.core.database.impl.DatabaseSqlserver2000Impl;
import com.weishao.dbswitch.core.database.impl.DatabaseSqlserverImpl;

/**
 * 数据库实例构建工厂类
 * @author tang
 *
 */
public final class DatabaseFactory {
	
	private static final Map<DatabaseType,String> DATABASE_MAPPER=new HashMap<DatabaseType, String>(){
		
		private static final long serialVersionUID = 9202705534880971997L;

	{  
	      put(DatabaseType.MYSQL,DatabaseMysqlImpl.class.getName());
	      put(DatabaseType.ORACLE,DatabaseOracleImpl.class.getName());
	      put(DatabaseType.SQLSERVER2000,DatabaseSqlserver2000Impl.class.getName());
	      put(DatabaseType.SQLSERVER,DatabaseSqlserverImpl.class.getName());
	      put(DatabaseType.POSTGRESQL,DatabasePostgresImpl.class.getName());
	      put(DatabaseType.GREENPLUM,DatabaseGreenplumImpl.class.getName());
	}}; 
	
	public static AbstractDatabase getDatabaseInstance(DatabaseType type) {
		if(DATABASE_MAPPER.containsKey(type)) {
			String className= DATABASE_MAPPER.get(type);
			try {
				return (AbstractDatabase) Class.forName(className).getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException(String.format("Unkown database type (%s)",type.name()));
	}
}
