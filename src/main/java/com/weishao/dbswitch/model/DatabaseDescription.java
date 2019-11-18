package com.weishao.dbswitch.model;

import com.weishao.dbswitch.constant.DatabaseType;

/**
 * 数据库连接描述符信息定义(Database Description)
 * @author tang
 *
 */
public class DatabaseDescription {
	protected DatabaseType type;
	protected String host;
	protected int port;
	protected String dbname;
	protected String charset;
	protected String username;
	protected String password;
	
	public DatabaseDescription(String dbtype,String host,int port,String dbname,String charset,String username,String password) {
		this.type=DatabaseType.valueOf(dbtype.toUpperCase());
		this.host=host;
		this.port=port;
		this.dbname=dbname;
		this.charset=charset;
		this.username=username;
		this.password=password;
	}
	
	public DatabaseType getType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getDbname() {
		return dbname;
	}

	public String getCharset() {
		return charset;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "DatabaseDesc [type=" + type.name() + ", host=" + host + ", port=" + port + ", dbname=" + dbname + ", charset="
				+ charset + ", username=" + username + ", password=" + password + "]";
	}

}
