package com.weishao.dbswitch.data.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import com.weishao.dbswitch.common.constant.DatabaseType;

public class JdbcTemplateUtils {

	/**
	 * 获取数据库类型
	 * 
	 * @param jdbcTemplate JDBC数据库连接
	 * @return DatabaseType 数据库类型
	 * @throws SQLException
	 */
	public static DatabaseType getDatabaseProduceName(JdbcTemplate jdbcTemplate) {

		return jdbcTemplate.execute(new ConnectionCallback<DatabaseType>() {

			@Override
			public DatabaseType doInConnection(Connection con) throws SQLException, DataAccessException {
				DatabaseMetaData meta = con.getMetaData();

				String name = meta.getDatabaseProductName();
				if (name.toLowerCase().equals("microsoft sql server")) {
					name = "sqlserver";
				}

				return DatabaseType.valueOf(name.toUpperCase());
			}

		});

	}

	
	/**
	 * 获取表字段的元信息
	 * @param sourceJdbcTemplate
	 * @param fullTableName       表的全名
	 * @return
	 */
	public static Map<String, Integer> getColumnMetaData(JdbcTemplate sourceJdbcTemplate, String fullTableName) {
		final String sql = String.format("select * from %s where 1=2", fullTableName);
		Map<String, Integer> columnMetaData = new HashMap<String, Integer>();
		Boolean ret = sourceJdbcTemplate.execute(new ConnectionCallback<Boolean>() {

			public Boolean doInConnection(Connection conn) throws SQLException, DataAccessException {
				Statement stmt = null;
				ResultSet rs = null;
				try {
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 0, len = rsMetaData.getColumnCount(); i < len; i++) {
						columnMetaData.put(rsMetaData.getColumnName(i + 1), rsMetaData.getColumnType(i + 1));
					}
					return true;
				} catch (Exception e) {
					throw new RuntimeException(String.format("获取表:%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", fullTableName), e);
				} finally {
				}
			}
		});

		if (ret) {
			return columnMetaData;
		}

		return null;
	}

}
