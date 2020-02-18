package com.weishao.dbswitch.dbwriter.gpdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.support.JdbcUtils;
import com.weishao.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;
import com.weishao.dbswitch.dbwriter.gpdb.copy.element.BoolColumn;
import com.weishao.dbswitch.dbwriter.gpdb.copy.element.BytesColumn;
import com.weishao.dbswitch.dbwriter.gpdb.copy.element.Column;
import com.weishao.dbswitch.dbwriter.gpdb.copy.element.DateColumn;
import com.weishao.dbswitch.dbwriter.gpdb.copy.element.StringColumn;
import com.weishao.dbswitch.dbwriter.gpdb.copy.record.Record;
import com.weishao.dbswitch.dbwriter.gpdb.copy.record.impl.DefaultRecordImpl;
import com.weishao.dbswitch.dbwriter.util.ConnectionPoolSupportedUtils;

/**
 * Greenplum数据库Copy写入实现类
 * 
 * @author tang
 *
 */
public class GreenplumCopyWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	private static final Logger logger = LoggerFactory.getLogger(GreenplumCopyWriterImpl.class);

	private static final char FIELD_DELIMITER = '|';
	private static final char NEWLINE = '\n';
	private static final char QUOTE = '"';
	private static final char ESCAPE = '\\';

	protected Map<String, Integer> columnType;

	public GreenplumCopyWriterImpl(DataSource dataSource) {
		super(dataSource);

		if (!ConnectionPoolSupportedUtils.isSupportedDataSource(dataSource)) {
			throw new RuntimeException("连接PostgreSQL/Greenplum数据库请使用dbcp2连接池的BasicDataSource作为数据源！");
		}

		this.columnType = null;
	}

	@Override
	public void prepareWrite(String schemaName, String tableName) {
		String sql = String.format("SELECT * from \"%s\".\"%s\" where 1=2", schemaName, tableName);
		Map<String, Integer> columnMetaData = new HashMap<String, Integer>();
		Boolean ret = this.jdbcTemplate.execute(new ConnectionCallback<Boolean>() {

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
					throw new RuntimeException(
							String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", schemaName, tableName), e);
				} finally {
					JdbcUtils.closeResultSet(rs);
					JdbcUtils.closeStatement(stmt);
				}
			}
		});

		if (ret) {
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.columnType = Objects.requireNonNull(columnMetaData);

			if (this.columnType.isEmpty()) {
				throw new RuntimeException(
						String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", schemaName, tableName));
			}
		} else {
			throw new RuntimeException("内部代码出现错误，请开发人员排查！");
		}
	}
	
	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		if (null == this.columnType || this.columnType.isEmpty()) {
			throw new RuntimeException("请先调用prepareWrite()函数，或者出现内部代码集成调用错误！");
		}

		if (recordValues.isEmpty()) {
			return 0;
		}

		List<Integer> columnFieldType =new ArrayList<Integer>();
		LinkedHashMap<String, Pair<Integer, Integer>> types = new LinkedHashMap<String, Pair<Integer, Integer>>();
		for (int i = 0; i < fieldNames.size(); ++i) {
			String s = fieldNames.get(i);

			if (!this.columnType.containsKey(s)) {
				throw new RuntimeException(String.format("表%s.%s 中不存在字段名为%s的字段，请检查参数传入!", schemaName, tableName, s));
			}

			Integer t=this.columnType.get(s);
			Pair<Integer, Integer> pair = Pair.of(t, i);
			types.put(s, pair);
			
			columnFieldType.add(t);
		}

		String sql = getCopySql(schemaName, tableName, fieldNames, 0);
		if (logger.isDebugEnabled()) {
			logger.debug("Greenplum/PostgreSQL write data using copy SQL:{}", sql);
		}

		StringBuilder block = new StringBuilder();
		for (Object[] objects : recordValues) {
			if (fieldNames.size() != objects.length) {
				throw new RuntimeException(
						String.format("传入的参数有误，字段列数%d与记录中的值个数%d不相符合", fieldNames.size(), objects.length));
			}
			
			Record record = this.buildRecord(objects, types);
			//logger.info("Record: {}",record);
			try {
				StringBuilder sb = this.serializeRecord(record, columnFieldType);
				//logger.debug(new String(sb.toString()));
				block.append(sb);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		Connection connection=null;
		try {
			connection = dataSource.getConnection();
			try (InputStream in = new ByteArrayInputStream(block.toString().getBytes("UTF-8"));) {
				BaseConnection baseConn = ConnectionPoolSupportedUtils.asPgConnectionByDbcp2(connection);
				CopyManager mgr = new CopyManager(baseConn);
				long ret = mgr.copyIn(sql, in);
				recordValues.clear();
				if (logger.isDebugEnabled()) {
					logger.debug("Greenplum copy write data  affect count:{}", ret);
				}
				return ret;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}finally {
			JdbcUtils.closeConnection(connection);
		}

	}

	private String getCopySql(String schemaName,String tableName, List<String> columnList, int segment_reject_limit) {
		StringBuilder sb = new StringBuilder().append("COPY \"").append(schemaName).append("\".\"").append(tableName).append("\" (")
				.append(constructColumnNameList(columnList))
				.append(") FROM STDIN WITH DELIMITER '|' NULL '' CSV QUOTE '\"' ESCAPE E'\\\\'");

		if (segment_reject_limit >= 2) {
			sb.append(" LOG ERRORS SEGMENT REJECT LIMIT ").append(segment_reject_limit).append(";");
		} else {
			sb.append(";");
		}

		String sql = sb.toString();
		return sql;
	}
	
	private String constructColumnNameList(List<String> columnList) {
		List<String> columns = new ArrayList<String>();

		for (String column : columnList) {
			if (column.endsWith("\"") && column.startsWith("\"")) {
				columns.add(column);
			} else {
				columns.add("\"" + column + "\"");
			}
		}

		return StringUtils.join(columns, ",");
	}

	public Record buildRecord(Object[] objects, LinkedHashMap<String, Pair<Integer, Integer>> types) {
		Record record = new DefaultRecordImpl();
		try {
			for (Entry<String, Pair<Integer, Integer>> entry : types.entrySet()) {
				Object data = objects[entry.getValue().getRight()];

				switch (entry.getValue().getLeft()) {
				case Types.CHAR:
				case Types.NCHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
				case Types.NVARCHAR:
				case Types.LONGNVARCHAR:
				case Types.CLOB:
				case Types.NCLOB:
					if(data instanceof String) {
						record.addColumn(new StringColumn(data.toString()));
					}else if(null!=data){
						if(data instanceof Boolean) {
							record.addColumn(new StringColumn((boolean) data?"1":"0"));
						}else {
							record.addColumn(new StringColumn(data.toString()));
						}
					}else{
						record.addColumn(new StringColumn(null));
					}
					break;

				case Types.SMALLINT:
				case Types.TINYINT:
				case Types.INTEGER:
				case Types.BIGINT:
					if(null!=data) {
						if(data instanceof Boolean) {
							record.addColumn(new StringColumn((boolean) data?"1":"0"));
						}else {
							record.addColumn(new StringColumn(data.toString()));
						}
					}else {
						record.addColumn(new StringColumn(null));
					}
					break;

				case Types.NUMERIC:
				case Types.DECIMAL:
					if(null!=data) {
						if(data instanceof Boolean) {
							record.addColumn(new StringColumn((boolean) data?"1":"0"));
						}else {
							record.addColumn(new StringColumn(data.toString()));
						}
					}else {
						record.addColumn(new StringColumn(null));
					}
					break;

				case Types.FLOAT:
				case Types.REAL:
				case Types.DOUBLE:
					if(null!=data) {
						if(data instanceof Boolean) {
							record.addColumn(new StringColumn((boolean) data?"1":"0"));
						}else {
							record.addColumn(new StringColumn(data.toString()));
						}
					}else {
						record.addColumn(new StringColumn(null));
					}
					break;

				case Types.TIME:
				case Types.DATE:
				case Types.TIMESTAMP:
					if(data instanceof java.util.Date) {
						record.addColumn(new DateColumn((java.util.Date)data));
					}else if(null!=data){
						record.addColumn(new StringColumn(data.toString()));
					}else {
						record.addColumn(new StringColumn(null));
					}
					
					break;

				case Types.BINARY:
				case Types.VARBINARY:
				case Types.BLOB:
				case Types.LONGVARBINARY:
					if (null != data) {
						if (data instanceof byte[]) {
							record.addColumn(new BytesColumn((byte[]) data));
						} else {
							if(data instanceof Boolean) {
								record.addColumn(new StringColumn((boolean) data?"1":"0"));
							}else {
								record.addColumn(new StringColumn(data.toString()));
							}
						}
					} else {
						record.addColumn(new StringColumn(null));
					}
					break;

				// warn: bit(1) -> Types.BIT 可使用BoolColumn
				// warn: bit(>1) -> Types.VARBINARY 可使用BytesColumn
				case Types.BOOLEAN:
				case Types.BIT:
					if (null != data) {
						record.addColumn(new BoolColumn(Boolean.valueOf(data.toString())));
					} else {
						record.addColumn(new StringColumn(null));
					}
					break;

				case Types.NULL:
					if(null!=data) {
						record.addColumn(new StringColumn(data.toString()));
					}else {
						record.addColumn(new StringColumn(null));
					}
					break;

				default:
					throw new RuntimeException(String.format("不支持的数据库字段类型,表名:[%s.%s] 字段名:[%s].", entry.getKey(),
							this.schemaName, this.tableName));
				}
			}
		} catch (Exception e) {
			logger.warn("error:", e);
			throw new RuntimeException(e);
		}

		return record;
	}

	private StringBuilder serializeRecord(Record record, List<Integer> columnType) throws Exception {
		if (null == record) {
			throw new RuntimeException("serializeRecord()中传递的参数record为空异常NPE");
		}
		if (null == columnType) {
			throw new RuntimeException("serializeRecord()中传递的参数columnType为空异常NPE");
		}

		if (record.getColumnNumber() != columnType.size()) {
			throw new RuntimeException(
					String.format("记录内的列表个数%d与元信息中的列信息个数%d不符", record.getColumnNumber(), columnType.size()));
		}

		StringBuilder sb = new StringBuilder();
		Column column;
		for (int i = 0; i < columnType.size(); i++) {
			column = record.getColumn(i);
			int columnSqltype = columnType.get(i);
			switch (columnSqltype) {
			case Types.CHAR:
			case Types.NCHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR: {
				String data = column.asString();
				if (data != null) {
					sb.append(QUOTE);
					sb.append(escapeString(data));
					sb.append(QUOTE);
				}

				break;
			}
			case Types.BINARY:
			case Types.BLOB:
			case Types.CLOB:
			case Types.LONGVARBINARY:
			case Types.NCLOB:
			case Types.VARBINARY: {
				byte[] data = column.asBytes();

				if (data != null) {
					sb.append(escapeBinary(data));
				}

				break;
			}
			default: {
				String data = column.asString();
				if (data != null) {
					sb.append(data);
				}

				break;
			}
			}

			if (i + 1 < columnType.size()) {
				sb.append(FIELD_DELIMITER);
			}
		}
		sb.append(NEWLINE);
		return sb;
	}

	/**
	 * Any occurrence within the value of a QUOTE character or the ESCAPE character
	 * is preceded by the escape character.
	 */
	private String escapeString(String data) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < data.length(); ++i) {
			char c = data.charAt(i);
			switch (c) {
			case 0x00:
				logger.warn("字符串中发现非法字符 0x00，已经将其删除");
				continue;
			case QUOTE:
			case ESCAPE:
				sb.append(ESCAPE);
			}

			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Non-printable characters are inserted as '\nnn' (octal) and '\' as '\\'.
	 */
	private String escapeBinary(byte[] data) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < data.length; ++i) {
			if (data[i] == '\\') {
				sb.append('\\');
				sb.append('\\');
			} else if (data[i] < 0x20 || data[i] > 0x7e) {
				byte b = data[i];
				char[] val = new char[3];
				val[2] = (char) ((b & 07) + '0');
				b >>= 3;
				val[1] = (char) ((b & 07) + '0');
				b >>= 3;
				val[0] = (char) ((b & 03) + '0');
				sb.append('\\');
				sb.append(val);
			} else {
				sb.append((char) (data[i]));
			}
		}

		return sb.toString();
	}

}
