package com.weishao.dbswitch.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.weishao.dbswitch.core.model.DatabaseDescription;

/**
 * JDBC的URL相关工具类
 * @author tang
 *
 */
public class JdbcUrlUtils {
	
	protected enum OracleJdbcConnectionMode {
		SID(1), SERVICENAME(2), TNSNAME(3);

		private int index;

		OracleJdbcConnectionMode(int idx) {
			this.index = idx;
		}

		public int getIndex() {
			return index;
		}
	}
	
	/**
	 * 根据数据库种类拼接jdbc的url信息
	 * 参考地址：https://www.cnblogs.com/chenglc/p/8421573.html
	 * 说明：
	 * （1）SQLServer数据库驱动问题
	 * 在SQL Server 2000 中加载驱动和URL路径的语句是
	 *  String driverName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
	 *  String dbURL = "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=sample";
	 *  而SQL Server 2005和SQL Server 2008 中加载驱动和URL的语句则为
	 *  String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	 *  String dbURL = "jdbc:sqlserver://localhost:1433; DatabaseName=sample";
	 *  ==========================================================================
	 *  SQL Server 2000/SQL Server 2005和SQL Server 2008等可以综合使用jDTS驱动（开源）
	 *  参考文章地址：https://blog.csdn.net/inrgihc/article/details/105244514
	 *  （2）Oracle数据库驱动连接问题
	 *  JDBC的URL三种方式：https://blog.csdn.net/gnail_oug/article/details/80075263
	 *  
	 * @param db          数据库连接描述信息
	 * @param connectTimeout  连接超时时间(单位：秒）
	 * @param mode   对于Oracle数据库的模式，可取范围为：sid,servicename,tnsname三种
	 * @return   对应数据库的JDBC的URL字符串
	 * 
	 */
	public static String getJdbcUrl(DatabaseDescription db, int connectTimeout) {
		switch (db.getType()) {
		case MYSQL:
			String charset=db.getCharset();
			if(Objects.isNull(charset) || charset.isEmpty()) {
				charset="utf-8";
			}
			return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=%s&nullCatalogMeansCurrent=true&connectTimeout=%d", db.getHost(),db.getPort(),db.getDbname(),charset,connectTimeout*1000);
		case ORACLE:
			OracleJdbcConnectionMode type;
			String mode=db.getMode();
			if (Objects.isNull(mode)) {
				type = OracleJdbcConnectionMode.SID;
			} else {
				try {
					type = OracleJdbcConnectionMode.valueOf(mode.trim().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(String.format("Invalid Oracle mode type: %s", mode));
				}
			}
			
			// Oracle设置连接超时时间
			System.setProperty("oracle.net.CONNECT_TIMEOUT", Integer.toString(1000 * connectTimeout));
			if (OracleJdbcConnectionMode.SID == type) {
				return String.format("jdbc:oracle:thin:@%s:%d:%s", db.getHost(), db.getPort(), db.getDbname());
			} else if (OracleJdbcConnectionMode.SERVICENAME == type) {
				return String.format("jdbc:oracle:thin:@//%s:%d/%s", db.getHost(), db.getPort(), db.getDbname());
			} else if (OracleJdbcConnectionMode.TNSNAME == type) {
				return String.format(
						"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%d))) "
						+ "(CONNECT_DATA=(SERVICE_NAME=%s)))",
						db.getHost(), db.getPort(), db.getDbname());
			}else {
				return String.format("jdbc:oracle:thin:@%s:%d:%s", db.getHost(), db.getPort(), db.getDbname());
			}
		//case SQLSERVER2000:
		//	return String.format("jdbc:microsoft:sqlserver://%s:%d;DatabaseName=%s",db.getHost(),db.getPort(),db.getDbname());
		//case SQLSERVER:
		//	return String.format("jdbc:sqlserver://%s:%d;DatabaseName=%s",db.getHost(),db.getPort(),db.getDbname());
		case SQLSERVER:
			return String.format("jdbc:jtds:sqlserver://%s:%d/;DatabaseName=%s",db.getHost(),db.getPort(),db.getDbname());
		case POSTGRESQL:
			return String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=%d", db.getHost(),db.getPort(),db.getDbname(),connectTimeout);
		case GREENPLUM:
			return String.format("jdbc:pivotal:greenplum://%s:%d;DatabaseName=%s", db.getHost(),db.getPort(),db.getDbname());
		default:
			throw new RuntimeException(String.format("Unkown database type (%s)",db.getType().name()));
		}
	}
	
	/**
	 * 从MySQL数据库的JDBC的URL中提取数据库连接相关参数
	 * @param jdbcUrl  JDBC连接的URL字符串
	 * @return Map 参数列表
	 */
	public static Map<String, String> findParamsByMySqlJdbcUrl(String jdbcUrl) {
		Pattern pattern = null;
		if (jdbcUrl.indexOf('?') > 0) {
			pattern = Pattern.compile(	"(?<protocol>^.+):(?<dbtype>.+)://(?<addresss>.+):(?<port>.+)/(?<schema>.+)\\?(?<path>.+)");
		} else {
			pattern = Pattern.compile("(?<protocol>^.+):(?<dbtype>.+)://(?<addresss>.+):(?<port>.+)/(?<schema>.+)");
		}

		Matcher m = pattern.matcher(jdbcUrl);
		if (m.find()) {
			Map<String, String> ret=new HashMap<String, String>();
			ret.put("protocol", m.group("protocol"));
			ret.put("dbtype", m.group("dbtype"));
			ret.put("addresss", m.group("addresss"));
			ret.put("port", m.group("port"));
			ret.put("schema", m.group("schema"));
			
			if (m.groupCount() > 5) {
				ret.put("path", m.group("path"));
			}
			
			return ret;
		} else {
			return null;
		}
	}
	
}
