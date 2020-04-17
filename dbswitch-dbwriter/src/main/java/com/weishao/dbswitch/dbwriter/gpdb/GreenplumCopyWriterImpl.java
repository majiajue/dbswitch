package com.weishao.dbswitch.dbwriter.gpdb;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Greenplum数据库Copy写入实现类
 * 
 * @author tang
 *
 */
@Slf4j
public class GreenplumCopyWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

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
			this.schemaName = Objects.requireNonNull(schemaName, "schema-name名称为空，不合法!");
			this.tableName = Objects.requireNonNull(tableName, "table-name名称为空，不合法!");
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
			throw new IllegalArgumentException("第一个参数[fieldNames]为空,无效!");
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

		String schemaName = Objects.requireNonNull(this.schemaName, "schema-name名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table-name名称为空，不合法!");
		SimpleRowWriter.Table table = new SimpleRowWriter.Table(schemaName, tableName, columnNames);
		SimpleRowWriter pgwriter = new SimpleRowWriter(table, true);
		pgwriter.enableNullCharacterHandler();
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

				/**
				 * 数据类型转换参考
				 * <p>
				 * 1. spring-jdbc: {@code org.springframework.jdbc.core.StatementCreatorUtils}
				 * </p>
				 * <p>
				 * 2. postgresql-driver: {@code org.postgresql.jdbc.PgPreparedStatement}
				 * </p>
				 */
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
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
									row.setTimeStamp(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
									row.setTimeStamp(i, null);
								} else {
									String val = castToString(fieldValue);
									if (null == val) {
										throw new RuntimeException(String.format(
												"表[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.String/java.sql.Clob，而实际的数据类型为%s",
												schemaName, tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setVarChar(i, val);
								}
								break;
							case Types.CLOB:
							case Types.NCLOB:
								if (null == fieldValue) {
									row.setText(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
									row.setTimeStamp(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
									row.setTimeStamp(i, null);
								} else {
									String val = castToString(fieldValue);
									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.String/java.sql.Clob，而实际的数据类型为%s",
												schemaName, tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setText(i, val);
								}
								break;
							case Types.TINYINT:
								if (null == fieldValue) {
									row.setByte(i, null);
								} else {
									Byte val = null;
									try {
										val = castToByte(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转错误，应该为java.lang.Byte，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setByte(i, val);
								}
								break;
							case Types.SMALLINT:
								if (null == fieldValue) {
									row.setShort(i, null);
								} else {
									Short val = null;
									try {
										val = castToShort(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.Short，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setShort(i, val);
								}
								break;
							case Types.INTEGER:
								if (null == fieldValue) {
									row.setInteger(i, null);
								} else {
									Integer val = null;
									try {
										val = castToInteger(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.Integer，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setInteger(i, val);
								}
								break;
							case Types.BIGINT:
								if (null == fieldValue) {
									row.setLong(i, null);
								} else {
									Long val = null;
									try {
										val = castToLong(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.Long，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setLong(i, val);
								}
								break;
							case Types.NUMERIC:
							case Types.DECIMAL:
								if (null == fieldValue) {
									row.setNumeric(i, null);
								} else {
									Number val = null;
									try {
										val = castToNumeric(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.Number，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setNumeric(i, val);
								}
								break;
							case Types.FLOAT:
							case Types.REAL:
								if (null == fieldValue) {
									row.setFloat(i, null);
								} else {
									Float val = null;
									try {
										val = castToFloat(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.Float，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setFloat(i, val);
								}
								break;
							case Types.DOUBLE:
								if (null == fieldValue) {
									row.setDouble(i, null);
								} else {
									Double val = null;
									try {
										val = castToDouble(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.lang.Double，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}
									row.setDouble(i, val);
								}
								break;
							case Types.BOOLEAN:
							case Types.BIT:
								if (null == fieldValue) {
									row.setBoolean(i, null);
								} else {
									Boolean val = null;
									try {
										val = castToBoolean(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型错误，应该为java.lang.Boolean，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}
									row.setBoolean(i, val);
								}
								break;
							case Types.TIME:
								if (null == fieldValue) {
									row.setDate(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
									row.setDate(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
									row.setDate(i, null);
								} else {
									LocalDate val = null;
									try {
										val = castToLocalDate(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.sql.Time，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}
									row.setDate(i, val);
								}
								break;
							case Types.DATE:
								if (null == fieldValue) {
									row.setDate(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
									row.setDate(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
									row.setDate(i, null);
								} else {
									LocalDate val = null;
									try {
										val = castToLocalDate(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型转换错误，应该为java.sql.Date，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}
									row.setDate(i, val);
								}
								break;
							case Types.TIMESTAMP:
								if (null == fieldValue) {
									row.setTimeStamp(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
									row.setTimeStamp(i, null);
								} else if (fieldValue.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
									row.setTimeStamp(i, null);
								} else {
									LocalDateTime val = null;
									try {
										val = castToLocalDateTime(fieldValue);
									} catch (RuntimeException e) {
										throw new RuntimeException(String.format("表名[%s.%s]的字段名[%s]数据类型转错误，%s",
												schemaName, tableName, fieldName, e.getMessage()));
									}

									if (null == val) {
										throw new RuntimeException(String.format(
												"表名[%s.%s]的字段名[%s]数据类型错误，应该为java.sql.Timestamp，而实际的数据类型为%s", schemaName,
												tableName, fieldName, fieldValue.getClass().getName()));
									}

									row.setTimeStamp(i, val);
								}
								break;
							case Types.BINARY:
							case Types.VARBINARY:
							case Types.BLOB:
							case Types.LONGVARBINARY:
								if (null == fieldValue) {
									row.setByteArray(i, null);
								} else {
									row.setByteArray(i, castToByteArray(fieldValue));
								}
								break;
							case Types.NULL:
							case Types.OTHER:
								if (null == fieldValue) {
									row.setText(i, null);
								} else {
									row.setText(i, fieldValue.toString());
								}
								break;
							default:
								throw new RuntimeException(String.format("不支持的数据库字段类型,表名[%s.%s] 字段名[%s].", schemaName,
										tableName, fieldName));
							}
						}
					}
				});
			}
			pgwriter.close();
			if (log.isDebugEnabled()) {
				log.debug("Greenplum copy write data affect count:{}", count);
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

	/**
	 * 将任意类型转换为java.lang.String类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.String类型
	 */
	private String castToString(final Object in) {
		if (in instanceof java.lang.Character) {
			return in.toString();
		} else if (in instanceof java.lang.String) {
			return in.toString();
		} else if (in instanceof java.lang.Character) {
			return in.toString();
		} else if (in instanceof java.sql.Clob) {
			return clob2Str((java.sql.Clob) in);
		} else if (in instanceof java.lang.Number) {
			return in.toString();
		} else if (in instanceof java.sql.RowId) {
			return in.toString();
		} else if (in instanceof java.lang.Boolean) {
			return in.toString();
		} else if (in instanceof java.util.Date) {
			return in.toString();
		} else if (in instanceof java.time.LocalDate) {
			return in.toString();
		} else if (in instanceof java.time.LocalTime) {
			return in.toString();
		} else if (in instanceof java.time.LocalDateTime) {
			return in.toString();
		} else if (in instanceof java.time.OffsetDateTime) {
			return in.toString();
		} else if (in instanceof java.util.UUID) {
			return in.toString();
		} else if (in instanceof org.postgresql.util.PGobject) {
			return in.toString();
		} else if (in instanceof org.postgresql.jdbc.PgSQLXML) {
			try {
				return ((org.postgresql.jdbc.PgSQLXML) in).getString();
			} catch (Exception e) {
				return "";
			}
		} else if (in instanceof java.sql.SQLXML) {
			return in.toString();
		} else if (in.getClass().getName().equals("oracle.sql.INTERVALDS")) {
			return in.toString();
		} else if (in.getClass().getName().equals("oracle.sql.INTERVALYM")) {
			return in.toString();
		} else if (in.getClass().getName().equals("oracle.sql.TIMESTAMPLTZ")) {
			return in.toString();
		} else if (in.getClass().getName().equals("oracle.sql.TIMESTAMPTZ")) {
			return in.toString();
		} else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
			return in.toString();
		} else if (in instanceof java.sql.Array) {
			return in.toString();
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Byte类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Byte类型
	 */
	private Byte castToByte(final Object in) {
		if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).byteValue();
		} else if (in instanceof java.util.Date) {
			return Long.valueOf(((java.util.Date) in).getTime()).byteValue();
		} else if (in instanceof java.lang.String) {
			try {
				return Byte.parseByte(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Byte类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Character) {
			try {
				return Byte.parseByte(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.Character类型转换为java.lang.Byte类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Byte.parseByte(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Byte类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? (byte) 1 : (byte) 0;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Short类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Short类型
	 */
	private Short castToShort(final Object in) {
		if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).shortValue();
		} else if (in instanceof java.lang.Byte) {
			return (short) (((byte) in) & 0xff);
		} else if (in instanceof java.util.Date) {
			return (short) ((java.util.Date) in).getTime();
		} else if (in instanceof java.util.Calendar) {
			return (short) ((java.util.Calendar) in).getTime().getTime();
		} else if (in instanceof java.time.LocalDateTime) {
			return (short) java.sql.Timestamp.valueOf((java.time.LocalDateTime) in).getTime();
		} else if (in instanceof java.time.OffsetDateTime) {
			return (short) java.sql.Timestamp.valueOf(((java.time.OffsetDateTime) in).toLocalDateTime()).getTime();
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return Short.parseShort(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Number类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Short.parseShort(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Number类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? (short) 1 : (short) 0;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Integer类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Integer类型
	 */
	private Integer castToInteger(final Object in) {
		if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).intValue();
		} else if (in instanceof java.lang.Byte) {
			return (int) (((byte) in) & 0xff);
		} else if (in instanceof java.util.Date) {
			return (int) ((java.util.Date) in).getTime();
		} else if (in instanceof java.util.Calendar) {
			return (int) ((java.util.Calendar) in).getTime().getTime();
		} else if (in instanceof java.time.LocalDateTime) {
			return (int) java.sql.Timestamp.valueOf((java.time.LocalDateTime) in).getTime();
		} else if (in instanceof java.time.OffsetDateTime) {
			return (int) java.sql.Timestamp.valueOf(((java.time.OffsetDateTime) in).toLocalDateTime()).getTime();
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return Integer.parseInt(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Number类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Integer.parseInt(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Number类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? (int) 1 : (int) 0;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Long类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Long类型
	 */
	private Long castToLong(final Object in) {
		if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).longValue();
		} else if (in instanceof java.lang.Byte) {
			return (long) (((byte) in) & 0xff);
		} else if (in instanceof java.util.Date) {
			return ((java.util.Date) in).getTime();
		} else if (in instanceof java.util.Calendar) {
			return ((java.util.Calendar) in).getTime().getTime();
		} else if (in instanceof java.time.LocalDateTime) {
			return java.sql.Timestamp.valueOf((java.time.LocalDateTime) in).getTime();
		} else if (in instanceof java.time.OffsetDateTime) {
			return java.sql.Timestamp.valueOf(((java.time.OffsetDateTime) in).toLocalDateTime()).getTime();
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return Long.parseLong(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Long类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Long.parseLong(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Long类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? (long) 1 : (long) 0;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Number类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Number类型
	 */
	private Number castToNumeric(final Object in) {
		if (in instanceof java.lang.Number) {
			return (java.lang.Number) in;
		} else if (in instanceof java.util.Date) {
			return ((java.util.Date) in).getTime();
		} else if (in instanceof java.util.Calendar) {
			return ((java.util.Calendar) in).getTime().getTime();
		} else if (in instanceof java.time.LocalDateTime) {
			return java.sql.Timestamp.valueOf((java.time.LocalDateTime) in).getTime();
		} else if (in instanceof java.time.OffsetDateTime) {
			return java.sql.Timestamp.valueOf(((java.time.OffsetDateTime) in).toLocalDateTime()).getTime();
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return new java.math.BigDecimal(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Number类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return new java.math.BigDecimal(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Number类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? (long) 1 : (long) 0;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Float类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Float类型
	 */
	private Float castToFloat(final Object in) {
		if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).floatValue();
		} else if (in instanceof java.util.Date) {
			return (float) ((java.util.Date) in).getTime();
		} else if (in instanceof java.util.Calendar) {
			return (float) ((java.util.Calendar) in).getTime().getTime();
		} else if (in instanceof java.time.LocalDateTime) {
			return (float) java.sql.Timestamp.valueOf((java.time.LocalDateTime) in).getTime();
		} else if (in instanceof java.time.OffsetDateTime) {
			return (float) java.sql.Timestamp.valueOf(((java.time.OffsetDateTime) in).toLocalDateTime()).getTime();
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return Float.parseFloat(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Float类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Float.parseFloat(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Float类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? 1f : 0f;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.lang.Double类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.lang.Double类型
	 */
	private Double castToDouble(final Object in) {
		if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).doubleValue();
		} else if (in instanceof java.util.Date) {
			return (double) ((java.util.Date) in).getTime();
		} else if (in instanceof java.util.Calendar) {
			return (double) ((java.util.Calendar) in).getTime().getTime();
		} else if (in instanceof java.time.LocalDateTime) {
			return (double) java.sql.Timestamp.valueOf((java.time.LocalDateTime) in).getTime();
		} else if (in instanceof java.time.OffsetDateTime) {
			return (double) java.sql.Timestamp.valueOf(((java.time.OffsetDateTime) in).toLocalDateTime()).getTime();
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return Double.parseDouble(in.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将将java.lang.String类型转换为java.lang.Double类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Double.parseDouble(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.lang.Double类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in ? 1d : 0d;
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.time.LocalDate类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.time.LocalDate类型
	 */
	private LocalDate castToLocalDate(final Object in) {
		if (in instanceof java.sql.Time) {
			java.sql.Time date = (java.sql.Time) in;
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate;
		} else if (in instanceof java.sql.Timestamp) {
			java.sql.Timestamp t = (java.sql.Timestamp) in;
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime.toLocalDate();
		} else if (in instanceof java.util.Date) {
			java.sql.Date date = new java.sql.Date(((java.util.Date) in).getTime());
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate;
		} else if (in instanceof java.util.Calendar) {
			java.sql.Date date = new java.sql.Date(((java.util.Calendar) in).getTime().getTime());
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate;
		} else if (in instanceof java.time.LocalDate) {
			return (java.time.LocalDate) in;
		} else if (in instanceof java.time.LocalTime) {
			return java.time.LocalDate.MIN;
		} else if (in instanceof java.time.LocalDateTime) {
			return ((java.time.LocalDateTime) in).toLocalDate();
		} else if (in instanceof java.time.OffsetDateTime) {
			return ((java.time.OffsetDateTime) in).toLocalDate();
		} else if (in.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			Class<?> clz = in.getClass();
			try {
				Method m = clz.getMethod("timestampValue");
				java.sql.Timestamp date = (java.sql.Timestamp) m.invoke(in);
				LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				return localDate;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
			Class<?> clz = in.getClass();
			try {
				Method m = clz.getMethod("getTimestamp");
				java.sql.Timestamp t = (java.sql.Timestamp) m.invoke(in);
				LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
				return localDateTime.toLocalDate();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				java.sql.Time date = java.sql.Time.valueOf(in.toString());
				LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				return localDate;
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(String.format("无法将java.lang.String类型转换为java.sql.Time类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				java.sql.Time date = java.sql.Time.valueOf(clob2Str((java.sql.Clob) in));
				LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				return localDate;
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("无法将java.sql.Clob类型转换为java.sql.Time类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Number) {
			java.sql.Timestamp t = new java.sql.Timestamp(((java.lang.Number) in).longValue());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime.toLocalDate();
		}

		return null;
	}

	/**
	 * 将任意类型转换为java.time.LocalDateTime类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return java.time.LocalDateTime类型
	 */
	private LocalDateTime castToLocalDateTime(final Object in) {
		if (in instanceof java.sql.Timestamp) {
			java.sql.Timestamp t = (java.sql.Timestamp) in;
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime;
		} else if (in instanceof java.sql.Date) {
			java.sql.Date date = (java.sql.Date) in;
			LocalDate localDate = date.toLocalDate();
			LocalTime localTime = LocalTime.of(0, 0, 0);
			LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
			return localDateTime;
		} else if (in instanceof java.sql.Time) {
			java.sql.Time date = (java.sql.Time) in;
			java.sql.Timestamp t = new java.sql.Timestamp(date.getTime());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime;
		} else if (in instanceof java.util.Date) {
			java.sql.Timestamp t = new java.sql.Timestamp(((java.util.Date) in).getTime());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime;
		} else if (in instanceof java.util.Calendar) {
			java.sql.Timestamp t = new java.sql.Timestamp(((java.util.Calendar) in).getTime().getTime());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime;
		} else if (in instanceof java.time.LocalDate) {
			LocalDate localDate = (java.time.LocalDate) in;
			LocalTime localTime = LocalTime.of(0, 0, 0);
			LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
			return localDateTime;
		} else if (in instanceof java.time.LocalTime) {
			LocalDate localDate = java.time.LocalDate.MIN;
			LocalTime localTime = (java.time.LocalTime) in;
			LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
			return localDateTime;
		} else if (in instanceof java.time.LocalDateTime) {
			return (java.time.LocalDateTime) in;
		} else if (in instanceof java.time.OffsetDateTime) {
			return ((java.time.OffsetDateTime) in).toLocalDateTime();
		} else if (in.getClass().getName().equals("oracle.sql.TIMESTAMP")) {
			Class<?> clz = in.getClass();
			try {
				Method m = clz.getMethod("timestampValue");
				java.sql.Timestamp t = (java.sql.Timestamp) m.invoke(in);
				LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
				return localDateTime;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (in.getClass().getName().equals("microsoft.sql.DateTimeOffset")) {
			Class<?> clz = in.getClass();
			try {
				Method m = clz.getMethod("getTimestamp");
				java.sql.Timestamp t = (java.sql.Timestamp) m.invoke(in);
				LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
				return localDateTime;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				java.sql.Timestamp t = java.sql.Timestamp.valueOf(in.toString());
				LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
				return localDateTime;
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.sql.TimeStamp类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				java.sql.Timestamp t = java.sql.Timestamp.valueOf(clob2Str((java.sql.Clob) in));
				LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
				return localDateTime;
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.sql.Clob类型转换为java.sql.TimeStamp类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.lang.Number) {
			java.sql.Timestamp t = new java.sql.Timestamp(((java.lang.Number) in).longValue());
			LocalDateTime localDateTime = LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault());
			return localDateTime;
		}
		

		return null;
	}

	/**
	 * 将任意类型转换为byte[]类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return byte[]类型
	 */
	private byte[] castToByteArray(final Object in) {
		if (in instanceof byte[]) {
			return (byte[]) in;
		} else if (in instanceof java.util.Date) {
			return in.toString().getBytes();
		} else if (in instanceof java.sql.Blob) {
			return blob2Bytes((java.sql.Blob) in);
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			return in.toString().getBytes();
		} else if (in instanceof java.sql.Clob) {
			return clob2Str((java.sql.Clob) in).toString().getBytes();
		} else {
			return toByteArray(in);
		}
	}

	/**
	 * 将任意类型转换为Boolean类型
	 * 
	 * @param in 任意类型的对象实例
	 * @return Boolean类型
	 */
	private Boolean castToBoolean(final Object in) {
		if (in instanceof java.lang.Boolean) {
			return (java.lang.Boolean) in;
		} else if (in instanceof java.lang.Number) {
			return ((java.lang.Number) in).intValue() != 0;
		} else if (in instanceof java.lang.String || in instanceof java.lang.Character) {
			try {
				return Boolean.parseBoolean(in.toString());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(
						String.format("无法将java.lang.String类型转换为java.lang.Boolean类型:%s", e.getMessage()));
			}
		} else if (in instanceof java.sql.Clob) {
			try {
				return Boolean.parseBoolean(clob2Str((java.sql.Clob) in));
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("无法将java.sql.Clob类型转换为java.lang.Boolean类型:%s", e.getMessage()));
			}
		}

		return null;
	}

}