package com.weishao.dbswitch.dbwriter.gpdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import javax.sql.DataSource;
import org.postgresql.PGConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.support.JdbcUtils;
import com.weishao.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;
import de.bytefish.pgbulkinsert.row.SimpleRow;
import de.bytefish.pgbulkinsert.row.SimpleRowWriter;
import de.bytefish.pgbulkinsert.util.PostgreSqlUtils;

/**
 * Greenplum数据库Copy写入实现类
 * 
 * @author tang
 *
 */
public class GreenplumCopyWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	private static final Logger logger = LoggerFactory.getLogger(GreenplumCopyWriterImpl.class);

	protected Map<String, Integer> columnType;

	public GreenplumCopyWriterImpl(DataSource dataSource) {
		super(dataSource);

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

		if (fieldNames.isEmpty()) {
			throw new IllegalArgumentException("参数1(fieldNames)为空无效!");
		}

		if (recordValues.isEmpty()) {
			return 0;
		}

		String[] columnNames = new String[fieldNames.size()];
		for (int i = 0; i < fieldNames.size(); ++i) {
			String s = fieldNames.get(i);
			if (!this.columnType.containsKey(s)) {
				throw new RuntimeException(String.format("表%s.%s 中不存在字段名为%s的字段，请检查参数传入!", schemaName, tableName, s));
			}

			columnNames[i] = s;
		}

		String schemaName = Objects.requireNonNull(this.schemaName, "schema名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table名称为空，不合法!");
		SimpleRowWriter.Table table = new SimpleRowWriter.Table(schemaName, tableName, columnNames);
		SimpleRowWriter pgwriter = new SimpleRowWriter(table);
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			PGConnection pgConnection = PostgreSqlUtils.getPGConnection(connection);
			pgwriter.open(pgConnection);

			long count = recordValues.size();
			for (Object[] objects : recordValues) {
				if (fieldNames.size() != objects.length) {
					throw new RuntimeException(
							String.format("传入的参数有误，字段列数%d与记录中的值个数%d不相符合", fieldNames.size(), objects.length));
				}

				pgwriter.startRow(new Consumer<SimpleRow>() {

					@Override
					public void accept(SimpleRow row) {
						for (int i = 0; i < objects.length; ++i) {
							String fieldName = fieldNames.get(i);
							Object fieldValue = objects[i];
							Integer fieldType = columnType.get(fieldName);
							switch (fieldType) {
							case Types.CHAR:
							case Types.NCHAR:
							case Types.VARCHAR:
							case Types.LONGVARCHAR:
							case Types.NVARCHAR:
							case Types.LONGNVARCHAR:
								if (null == fieldValue) {
									row.setVarChar(i, null);
								} else if (fieldValue instanceof java.lang.String) {
									row.setVarChar(i, (java.lang.String) fieldValue);
								} else if (fieldValue instanceof java.sql.Clob) {
									row.setVarChar(i, clob2Str((java.sql.Clob) fieldValue));
								} else if (fieldValue instanceof java.lang.Number) {
									row.setVarChar(i, fieldValue.toString());
								} else if (fieldValue instanceof java.lang.Boolean) {
									row.setVarChar(i, fieldValue.toString());
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.String/java.sql.Clob",
													schemaName, tableName, fieldName));
								}
								break;
							case Types.CLOB:
							case Types.NCLOB:
								if (null == fieldValue) {
									row.setText(i, null);
								} else if (fieldValue instanceof java.lang.String) {
									row.setText(i, (java.lang.String) fieldValue);
								} else if (fieldValue instanceof java.sql.Clob) {
									row.setText(i, clob2Str((java.sql.Clob) fieldValue));
								} else if (fieldValue instanceof java.lang.Number) {
									row.setText(i, fieldValue.toString());
								} else if (fieldValue instanceof java.lang.Boolean) {
									row.setText(i, fieldValue.toString());
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.String/java.sql.Clob",
													schemaName, tableName, fieldName));
								}
								break;
							case Types.TINYINT:
								if (null == fieldValue) {
									row.setByte(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setByte(i, ((java.lang.Number) fieldValue).byteValue());
								} else if (fieldValue instanceof java.lang.String) {
									row.setByte(i, Byte.parseByte(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.SMALLINT:
								if (null == fieldValue) {
									row.setShort(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setShort(i, ((java.lang.Number) fieldValue).shortValue());
								} else if (fieldValue instanceof java.lang.String) {
									row.setShort(i, Short.parseShort(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.INTEGER:
								if (null == fieldValue) {
									row.setInteger(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setInteger(i, ((java.lang.Number) fieldValue).intValue());
								} else if (fieldValue instanceof java.lang.String) {
									row.setInteger(i, Integer.parseInt(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.BIGINT:
								if (null == fieldValue) {
									row.setLong(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setLong(i, ((java.lang.Number) fieldValue).longValue());
								} else if (fieldValue instanceof java.lang.String) {
									row.setLong(i, Long.parseLong(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.NUMERIC:
							case Types.DECIMAL:
								if (null == fieldValue) {
									row.setNumeric(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setNumeric(i, (java.lang.Number) fieldValue);
								} else if (fieldValue instanceof java.lang.String) {
									row.setNumeric(i, new java.math.BigDecimal(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.FLOAT:
							case Types.DOUBLE:
								if (null == fieldValue) {
									row.setDouble(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setDouble(i, ((java.lang.Number) fieldValue).doubleValue());
								} else if (fieldValue instanceof java.lang.String) {
									row.setDouble(i, Double.parseDouble(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.REAL:
								if (null == fieldValue) {
									row.setFloat(i, null);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setFloat(i, ((java.lang.Number) fieldValue).floatValue());
								} else if (fieldValue instanceof java.lang.String) {
									row.setFloat(i, Float.parseFloat(fieldValue.toString()));
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Number", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.TIME:
								if (null == fieldValue) {
									row.setDate(i, null);
								} else if (fieldValue instanceof java.sql.Time) {
									java.sql.Time date = (java.sql.Time) fieldValue;
									LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
									row.setDate(i, localDate);
								} else if (fieldValue instanceof java.lang.String) {
									java.sql.Time date = java.sql.Time.valueOf(fieldValue.toString());
									LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
									row.setDate(i, localDate);
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.sql.Time", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.DATE:
								if (null == fieldValue) {
									row.setDate(i, null);
								} else if (fieldValue instanceof java.sql.Date) {
									java.sql.Date date = (java.sql.Date) fieldValue;
									LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
									row.setDate(i, localDate);
								} else if (fieldValue instanceof java.lang.String) {
									java.sql.Time date = java.sql.Time.valueOf(fieldValue.toString());
									LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
									row.setDate(i, localDate);
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.sql.Date", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.TIMESTAMP:
								if (null == fieldValue) {
									row.setTimeStamp(i, null);
								} else if (fieldValue instanceof java.sql.Timestamp) {
									java.sql.Timestamp t = (java.sql.Timestamp) fieldValue;
									LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(),
											ZoneId.systemDefault());
									row.setTimeStamp(i, localDateTime);
								} else if (fieldValue instanceof java.sql.Date) {
									java.sql.Date date = (java.sql.Date) fieldValue;
									LocalDate localDate = date.toLocalDate();
									LocalTime localTime = LocalTime.of(0, 0, 0);
									LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
									row.setTimeStamp(i, localDateTime);
								} else if (fieldValue instanceof java.lang.String) {
									java.sql.Timestamp t = java.sql.Timestamp.valueOf(fieldValue.toString());
									LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(),
											ZoneId.systemDefault());
									row.setTimeStamp(i, localDateTime);
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.sql.Timestamp", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.BINARY:
							case Types.VARBINARY:
							case Types.BLOB:
							case Types.LONGVARBINARY:
								if (null == fieldValue) {
									row.setByteArray(i, null);
								} else if (fieldValue instanceof byte[]) {
									row.setByteArray(i, (byte[]) fieldValue);
								} else if (fieldValue instanceof java.sql.Blob) {
									row.setByteArray(i, blob2Bytes((java.sql.Blob) fieldValue));
								} else if (fieldValue instanceof java.lang.String) {
									row.setByteArray(i, ((java.lang.String) fieldValue).getBytes());
								} else {
									row.setByteArray(i, toByteArray(fieldValue));
								}
								break;
							case Types.BOOLEAN:
							case Types.BIT:
								if (null == fieldValue) {
									row.setBoolean(i, false);
								} else if (fieldValue instanceof java.lang.Boolean) {
									row.setBoolean(i, (java.lang.Boolean) fieldValue);
								} else if (fieldValue instanceof java.lang.Number) {
									row.setBoolean(i, ((java.lang.Number) fieldValue).intValue() != 0);
								} else {
									throw new RuntimeException(
											String.format("表名:[%s.%s]的字段名:[%s]数据类型错误，应该为java.lang.Boolean", schemaName,
													tableName, fieldName));
								}
								break;
							case Types.NULL:
								if (null == fieldValue) {
									row.setText(i, null);
								} else {
									row.setText(i, fieldValue.toString());
								}
								break;
							default:
								throw new RuntimeException(String.format("不支持的数据库字段类型,表名:[%s.%s] 字段名:[%s].", schemaName,
										tableName, fieldName));
							}
						}
					}
				});
			}
			pgwriter.close();
			recordValues.clear();
			if (logger.isDebugEnabled()) {
				logger.debug("Greenplum copy write data affect count:{}", count);
			}
			return count;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			JdbcUtils.closeConnection(connection);
		}

	}

	/**
	 * 将java.sql.Clob类型转换为java.lang.String类型
	 * 
	 * @param clob java.sql.Clob类型对象
	 * @return java.lang.String类型数据
	 */
	private String clob2Str(java.sql.Clob clob) {
		if (null == clob) {
			return null;
		}

		java.io.Reader is = null;
		java.io.BufferedReader reader = null;
		try {
			is = clob.getCharacterStream();
			reader = new java.io.BufferedReader(is);
			String line = reader.readLine();
			StringBuffer sb = new StringBuffer();
			while (line != null) {
				sb.append(line);
				line = reader.readLine();
			}
			return sb.toString();
		} catch (SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (null != reader) {
					reader.close();
				}
				if (null != is) {
					is.close();
				}
			} catch (Exception ex) {

			}
		}
	}

	/**
	 * 将java.sql.Blob类型转换为byte数组
	 * 
	 * @param clob java.sql.Blob类型对象
	 * @return byte数组
	 */
	private byte[] blob2Bytes(java.sql.Blob blob) {
		if (null == blob) {
			return null;
		}

		java.io.BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new java.io.BufferedInputStream(blob.getBinaryStream());
			byte[] bytes = new byte[(int) blob.length()];
			int len = bytes.length;
			int offset = 0;
			int read = 0;
			while (offset < len && (read = bufferedInputStream.read(bytes, offset, len - offset)) >= 0) {
				offset += read;
			}
			return bytes;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				bufferedInputStream.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 将Object对象转换为字节数组
	 * 
	 * @param obj 对象
	 * @return 字节数组
	 */
	private byte[] toByteArray(Object obj) {
		if (null == obj) {
			return null;
		}

		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (null != oos) {
					oos.close();
				}

				if (null != bos) {
					bos.close();
				}
			} catch (Exception e) {

			}
		}
	}
}
