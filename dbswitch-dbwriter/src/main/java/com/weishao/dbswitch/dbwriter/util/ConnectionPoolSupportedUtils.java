package com.weishao.dbswitch.dbwriter.util;

import java.sql.Connection;
import javax.sql.DataSource;
import org.postgresql.core.BaseConnection;

/**
 * 数据库连接池辅助支持工具类
 * 
 * 备注：
 * （1）如果考虑支持更多的连接池只需在这里扩展即可；
 * （2）当前在Spring中集成的数据库连接池包括：
 *     hikari连接池：com.zaxxer.hikari.HikariDataSource
 *     dbcp2连接池：org.apache.commons.dbcp2.BasicDataSource
 *     tomcat连接池：org.apache.tomcat.jdbc.pool.DataSource
 * 
 * 参考：
 *    https://www.cnblogs.com/storml/p/8611388.html
 *    https://blog.csdn.net/kangsa998/article/details/90231518
 * 
 * @author tang
 *
 */
public class ConnectionPoolSupportedUtils {

	private ConnectionPoolSupportedUtils() {
	}

	/**
	 * 是否支持的数据库连接池类型
	 * 
	 * @param dataSource 数据源类型
	 * @return 支持返回true，否则为false
	 */
	public static boolean isSupportedDataSource(DataSource dataSource) {
		if (dataSource instanceof org.apache.commons.dbcp2.BasicDataSource) {
			return true;
		}

		return false;
	}

	/**
	 * 将数据库连接强制转换为PostgreSQL的BaseConnection
	 * 
	 * @param connection 数据库连接
	 * @return PostgreSQL的BaseConnection
	 */
	@SuppressWarnings("rawtypes")
	public static BaseConnection asPgConnectionByDbcp2(Connection connection) {
		if (connection instanceof org.apache.commons.dbcp2.DelegatingConnection) {
			org.apache.commons.dbcp2.DelegatingConnection dconn = (org.apache.commons.dbcp2.DelegatingConnection) connection;
			org.apache.commons.dbcp2.PoolableConnection pconn = (org.apache.commons.dbcp2.PoolableConnection) dconn.getDelegate();
			BaseConnection baseConn = (BaseConnection) ((pconn.getDelegate()));
			return baseConn;
		}

		throw new RuntimeException("使用了未知或不支持的数据库连接池，当前只支持：dbcp2等连接池！");
	}
	
}
