package com.weishao.dbswitch.database.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.druid.sql.SQLUtils;
import com.weishao.dbswitch.constant.Const;
import com.weishao.dbswitch.constant.DatabaseType;
import com.weishao.dbswitch.database.AbstractDatabase;
import com.weishao.dbswitch.database.IDatabaseInterface;
import com.weishao.dbswitch.model.ColumnDescription;
import com.weishao.dbswitch.model.ColumnMetaData;
import com.weishao.dbswitch.model.TableDescription;

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

	@Override
	public List<TableDescription> queryTableList(String schemaName) {
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
				TableDescription td=new TableDescription();
				td.setSchemaName(rs.getString("TABLE_SCHEMA"));
				td.setTableName(rs.getString("TABLE_NAME"));
				td.setRemarks(rs.getString("COMMENTS"));
				String tableType=rs.getString("TABLE_TYPE").trim();
				 if(tableType.equalsIgnoreCase("VIEW")) {
					td.setTableType("VIEW");
				}else {
					td.setTableType("TABLE");
				}
				 
				 ret.add(td);
			}

			return ret;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}finally {
			if(null!=pstmt) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	@Override
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
		String sql=this.getTableFieldsQuerySQL(schemaName, tableName);
		List<ColumnDescription> ret= this.querySelectSqlColumnMeta(sql);
		String qsql = String.format(
				"SELECT a.name AS COLUMN_NAME,CONVERT(nvarchar(50),ISNULL(g.[value], '')) AS REMARKS FROM sys.columns a\r\n" + 
				"LEFT JOIN sys.extended_properties g ON ( a.object_id = g.major_id AND g.minor_id = a.column_id )\r\n" + 
				"WHERE object_id = (SELECT object_id FROM sys.tables st INNER JOIN INFORMATION_SCHEMA.TABLES t on st.name=t.TABLE_NAME\r\n" + 
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
				for(ColumnDescription cd : ret) {
					if(column_name.equalsIgnoreCase(cd.getFieldName())) {
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
	public String getFieldDefinition(ColumnMetaData v, List<String> pks, boolean add_cr) {
		String fieldname = v.getName();
		int length = v.getLength();
		int precision = v.getPrecision();
		int type = v.getType();

		String retval = " ["+fieldname + "]  ";

		switch (type) {
		case ColumnMetaData.TYPE_TIMESTAMP:
		case ColumnMetaData.TYPE_DATE:
			retval += "DATETIME";
			break;
		case ColumnMetaData.TYPE_BOOLEAN:
			retval += "CHAR(1)";
			break;
		case ColumnMetaData.TYPE_NUMBER:
		case ColumnMetaData.TYPE_INTEGER:
		case ColumnMetaData.TYPE_BIGNUMBER:
			if (null!=pks && pks.contains(fieldname)) {
				retval += "BIGINT";
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
			retval += " UNKNOWN";
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
			sb.append(StringUtils.join(pks, " ] , [ "));
			sb.append("]");
			return sb.toString();
		}

		return "";
	}
	
}
