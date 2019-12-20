package com.weishao.dbswitch.core.database.impl;

import com.weishao.dbswitch.core.database.IDatabaseInterface;

/**
 * 支持SQLServer2000数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseSqlserver2000Impl extends DatabaseSqlserverImpl implements IDatabaseInterface {

	public DatabaseSqlserver2000Impl() {
		super("com.microsoft.jdbc.sqlserver.SQLServerDriver");
	}
	
}
