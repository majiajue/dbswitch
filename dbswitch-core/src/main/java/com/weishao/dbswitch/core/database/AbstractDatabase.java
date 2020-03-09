package com.weishao.dbswitch.core.database;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.ColumnMetaData;
import com.weishao.dbswitch.core.model.TableDescription;

/**
 * 数据库元信息抽象基类
 * 
 * @author tang
 *
 */
public abstract class AbstractDatabase implements IDatabaseInterface {

	public static final int CLOB_LENGTH = 9999999;

	protected Connection connection = null;
	protected DatabaseMetaData metaData = null;
	protected String catalogName = null;

	public AbstractDatabase(String driverClassName) {
		this.catalogName = null;

		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void connect(String jdbcUrl, String username, String password) {
		/*
		 * 超时时间设置问题： https://blog.csdn.net/lsunwing/article/details/79461217
		 * https://blog.csdn.net/weixin_34405332/article/details/91664781
		 */
		try {
			/**
			 * Oracle在通过jdbc连接的时候需要添加一个参数来设置是否获取注释
			 */
			Properties props = new Properties();
			props.put("user", username);
			props.put("password", password);
			props.put("remarksReporting", "true");

			DriverManager.setLoginTimeout(15);// 设置最大时间
			this.connection = DriverManager.getConnection(jdbcUrl, props);
			this.metaData = this.connection.getMetaData();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void close() {
		if (null != connection) {
			try {
				connection.close();
			} catch (SQLException e) {
			}
			connection = null;
		}
	}

	@Override
	public List<String> querySchemaList() {
		Set<String> ret = new HashSet<String>();
		ResultSet schemas;
		try {
			schemas = this.metaData.getSchemas();
			while (schemas.next()) {
				ret.add(schemas.getString("TABLE_SCHEM"));
			}
			return new ArrayList<String>(ret);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<TableDescription> queryTableList(String schemaName) {
		List<TableDescription> ret = new ArrayList<TableDescription>();
		Set<String> uniqueSet = new HashSet<String>();
		ResultSet tables;
		try {
			tables = this.metaData.getTables(this.catalogName, schemaName, "%", new String[] { "TABLE", "VIEW" });
			while (tables.next()) {
				String tableName = tables.getString("TABLE_NAME");
				if (uniqueSet.contains(tableName)) {
					continue;
				} else {
					uniqueSet.add(tableName);
				}

				TableDescription td = new TableDescription();
				td.setSchemaName(schemaName);
				td.setTableName(tableName);
				td.setRemarks(tables.getString("REMARKS"));
				td.setTableType(tables.getString("TABLE_TYPE").toUpperCase());
				ret.add(td);
			}
			return ret;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
		String sql = this.getTableFieldsQuerySQL(schemaName, tableName);
		List<ColumnDescription> ret = this.querySelectSqlColumnMeta(sql);
		try {
			ResultSet columns = this.metaData.getColumns(this.catalogName, schemaName, tableName, null);
			while (columns.next()) {
				String column_name = columns.getString("COLUMN_NAME");
				String remarks = columns.getString("REMARKS");
				for (ColumnDescription cd : ret) {
					if (column_name.equalsIgnoreCase(cd.getFieldName())) {
						cd.setRemarks(remarks);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	@Override
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
		Set<String> ret = new HashSet<String>();
		try {
			ResultSet primarykeys = this.metaData.getPrimaryKeys(this.catalogName, schemaName, tableName);
			while (primarykeys.next()) {
				String name = primarykeys.getString("COLUMN_NAME");
				if (!ret.contains(name)) {
					ret.add(name);
				}
			}
			return new ArrayList<String>(ret);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public abstract List<ColumnDescription> querySelectSqlColumnMeta(String sql);

	@Override
	public void testQuerySQL(String sql) {
		String wrapperSql = this.getTestQuerySQL(sql);

		try {
			PreparedStatement pstmt = this.connection.prepareStatement(wrapperSql);
			pstmt.executeQuery();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
		return String.format(" \"%s\".\"%s\" ", schemaName, tableName);
	}

	@Override
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean add_cr) {
		throw new RuntimeException("AbstractDatabase Unempliment!");
	}

	@Override
	public String getPrimaryKeyAsString(List<String> pks) {
		if (pks.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"");
			sb.append(StringUtils.join(pks, "\" , \""));
			sb.append("\"");
			return sb.toString();
		}

		return "";
	}

	public abstract String formatSQL(String sql);

	/**************************************
	 * internal function
	 **************************************/

	protected abstract String getTableFieldsQuerySQL(String schemaName, String tableName);

	protected abstract String getTestQuerySQL(String sql);

	protected List<ColumnDescription> getSelectSqlColumnMeta(String querySQL, DatabaseType dbtype) {
		List<ColumnDescription> ret = new ArrayList<ColumnDescription>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = this.connection.prepareStatement(querySQL);
			rs = pstmt.executeQuery();

			ResultSetMetaData m = rs.getMetaData();
			int columns = m.getColumnCount();
			for (int i = 1; i <= columns; i++) {
				String name = m.getColumnLabel(i);
				if (null == name) {
					name = m.getColumnName(i);
				}

				ColumnDescription cd = new ColumnDescription();
				cd.setFieldName(name);
				cd.setLabelName(name);
				cd.setFieldType(m.getColumnType(i));
				if (0 != cd.getFieldType()) {
					cd.setFieldTypeName(m.getColumnTypeName(i));
					cd.setFiledTypeClassName(m.getColumnClassName(i));
					cd.setDisplaySize(m.getColumnDisplaySize(i));
					cd.setPrecisionSize(m.getPrecision(i));
					cd.setScaleSize(m.getScale(i));
					cd.setAutoIncrement(m.isAutoIncrement(i));
					cd.setNullable(m.isNullable(i) != ResultSetMetaData.columnNoNulls);
				} else {
					// 处理视图中NULL as fieldName的情况
					cd.setFieldTypeName("CHAR");
					cd.setFiledTypeClassName(String.class.getName());
					cd.setDisplaySize(1);
					cd.setPrecisionSize(1);
					cd.setScaleSize(0);
					cd.setAutoIncrement(false);
					cd.setNullable(true);
				}

				boolean signed = false;
				try {
					signed = m.isSigned(i);
				} catch (Exception ignored) {
					// This JDBC Driver doesn't support the isSigned method
					// nothing more we can do here by catch the exception.
				}
				cd.setSigned(signed);
				cd.setDbType(dbtype);

				ret.add(cd);
			}

			return ret;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
