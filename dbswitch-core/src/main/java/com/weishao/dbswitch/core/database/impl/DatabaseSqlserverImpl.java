package com.weishao.dbswitch.core.database.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.druid.sql.SQLUtils;
import com.weishao.dbswitch.core.constant.Const;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.IDatabaseInterface;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.ColumnMetaData;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.util.JdbcOperatorUtils;

/**
 * 支持SQLServer数据库的元信息实现
 * 
 * @author tang
 *
 */
public class DatabaseSqlserverImpl extends AbstractDatabase implements IDatabaseInterface {

	public DatabaseSqlserverImpl() {
		super("com.microsoft.sqlserver.jdbc.SQLServerDriver");
	}
	
	public DatabaseSqlserverImpl(String driverName) {
		super(driverName);
	}
	
	private int getDatabaseMajorVersion() {
		int majorVersion=0;
		try {
			majorVersion=this.metaData.getDatabaseMajorVersion();
			return majorVersion;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<TableDescription> queryTableList(String schemaName) {
		int majorVersion=getDatabaseMajorVersion();
		if(majorVersion<=8) {
			return super.queryTableList(schemaName);
		}
		
		List<TableDescription> ret = new ArrayList<TableDescription>();
		String sql = String.format(
				"SELECT t.TABLE_SCHEMA as TABLE_SCHEMA,	t.TABLE_NAME as TABLE_NAME,	t.TABLE_TYPE as TABLE_TYPE,	CONVERT(nvarchar(50),ISNULL(g.[value], '')) as COMMENTS \r\n" + 
				"FROM INFORMATION_SCHEMA.TABLES t LEFT JOIN sysobjects d on t.TABLE_NAME = d.name \r\n" + 
				"LEFT JOIN sys.extended_properties g on g.major_id=d.id and g.minor_id='0' where t.TABLE_SCHEMA='%s'",
				schemaName);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = this.connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				TableDescription td = new TableDescription();
				td.setSchemaName(rs.getString("TABLE_SCHEMA"));
				td.setTableName(rs.getString("TABLE_NAME"));
				td.setRemarks(rs.getString("COMMENTS"));
				String tableType = rs.getString("TABLE_TYPE").trim();
				if (tableType.equalsIgnoreCase("VIEW")) {
					td.setTableType("VIEW");
				} else {
					td.setTableType("TABLE");
				}

				ret.add(td);
			}

			return ret;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcOperatorUtils.closeResultSet(rs);
			JdbcOperatorUtils.closeStatement(pstmt);
		}
	}

	@Override
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
		int majorVersion = getDatabaseMajorVersion();
		if (majorVersion <= 8) {
			return super.queryTableColumnMeta(schemaName, tableName);
		}
		
		String sql=this.getTableFieldsQuerySQL(schemaName, tableName);
		List<ColumnDescription> ret= this.querySelectSqlColumnMeta(sql);
		String qsql = String.format(
				"SELECT a.name AS COLUMN_NAME,CONVERT(nvarchar(50),ISNULL(g.[value], '')) AS REMARKS FROM sys.columns a\r\n" + 
				"LEFT JOIN sys.extended_properties g ON ( a.object_id = g.major_id AND g.minor_id = a.column_id )\r\n" + 
				"WHERE object_id = (SELECT top 1 object_id FROM sys.tables st INNER JOIN INFORMATION_SCHEMA.TABLES t on st.name=t.TABLE_NAME\r\n" + 
				"WHERE	st.name = '%s' and t.TABLE_SCHEMA='%s')",
				tableName,schemaName);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = this.connection.prepareStatement(qsql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String column_name = rs.getString("COLUMN_NAME");
				String remarks = rs.getString("REMARKS");
				for (ColumnDescription cd : ret) {
					if (column_name.equalsIgnoreCase(cd.getFieldName())) {
						cd.setRemarks(remarks);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcOperatorUtils.closeResultSet(rs);
			JdbcOperatorUtils.closeStatement(pstmt);
		}

		return ret;
	}
	
	@Override
	public List<ColumnDescription> querySelectSqlColumnMeta(String sql) {
		String querySQL = String.format("SELECT TOP 1 * from (%s) tmp ", sql.replace(";", ""));
		return this.getSelectSqlColumnMeta(querySQL, DatabaseType.SQLSERVER);
	}

	@Override
	protected String getTableFieldsQuerySQL(String schemaName, String tableName) {
		return String.format("select top 1 * from [%s].[%s] ", schemaName, tableName);
	}

	@Override
	protected String getTestQuerySQL(String sql) {
		return String.format("SELECT top 1 * from ( %s ) tmp", sql.replace(";", ""));
	}

	@Override
	public String getQuotedSchemaTableCombination(String schemaName, String tableName) {
		return String.format("  [%s].[%s] ", schemaName, tableName);
	}

	@Override
	public String formatSQL(String sql) {
		return SQLUtils.formatSQLServer(sql);
	}

	@Override
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean use_autoinc, boolean add_cr) {
		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();
		int type = v.getType();

		String retval = " [" + fieldname + "]  ";

		switch (type) {
		case ColumnMetaData.TYPE_TIMESTAMP:
			retval += "DATETIME";
			break;
		case ColumnMetaData.TYPE_TIME:
			retval += "TIME";
			break;
		case ColumnMetaData.TYPE_DATE:
			retval += "DATE";
			break;
		case ColumnMetaData.TYPE_BOOLEAN:
			retval += "CHAR(32)";
			break;
		case ColumnMetaData.TYPE_NUMBER:
		case ColumnMetaData.TYPE_INTEGER:
		case ColumnMetaData.TYPE_BIGNUMBER:
			if (null != pks && pks.contains(fieldname)) {
				if (use_autoinc) {
					retval += "BIGINT IDENTITY(0,1)";
				} else {
					retval += "BIGINT";
				}
			} else {
				if (precision == 0) {
					if (length > 18) {
						retval += "DECIMAL(" + length + ",0)";
					} else {
						if (length > 9) {
							retval += "BIGINT";
						} else {
							retval += "INT";
						}
					}
				} else {
					if (precision > 0 && length > 0) {
						retval += "DECIMAL(" + length + "," + precision + ")";
					} else {
						retval += "FLOAT(53)";
					}
				}
			}
			break;
		case ColumnMetaData.TYPE_STRING:
			if (length < 8000) {
				// Maybe use some default DB String length in case length<=0
				if (length > 0) {
					// VARCHAR(n)最多能存n个字节，一个中文是两个字节。
					length = 2 * length;
					if (length > 8000) {
						length = 8000;
					}
					retval += "VARCHAR(" + length + ")";
				} else {
					retval += "VARCHAR(100)";
				}
			} else {
				retval += "TEXT"; // Up to 2bilion characters.
			}
			break;
		case ColumnMetaData.TYPE_BINARY:
			retval += "VARBINARY(MAX)";
			break;
		default:
			retval += "TEXT";
			break;
		}

		if (add_cr) {
			retval += Const.CR;
		}

		return retval;
	}

	@Override
	public  String getPrimaryKeyAsString(List<String> pks) {
		if (pks.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(StringUtils.join(pks, "] , ["));
			sb.append("]");
			return sb.toString();
		}

		return "";
	}
	
}
