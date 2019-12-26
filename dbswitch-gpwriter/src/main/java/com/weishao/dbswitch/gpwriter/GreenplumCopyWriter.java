package com.weishao.dbswitch.gpwriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import com.weishao.dbswitch.gpwriter.element.BoolColumn;
import com.weishao.dbswitch.gpwriter.element.BytesColumn;
import com.weishao.dbswitch.gpwriter.element.Column;
import com.weishao.dbswitch.gpwriter.element.DateColumn;
import com.weishao.dbswitch.gpwriter.record.Record;
import com.weishao.dbswitch.gpwriter.record.impl.DefaultRecordImpl;
import com.weishao.dbswitch.gpwriter.element.DoubleColumn;
import com.weishao.dbswitch.gpwriter.element.LongColumn;
import com.weishao.dbswitch.gpwriter.element.StringColumn;

public class GreenplumCopyWriter {
	private static final Logger logger = LoggerFactory.getLogger(GreenplumCopyWriter.class);

	private static final char FIELD_DELIMITER = '|';
	private static final char NEWLINE = '\n';
	private static final char QUOTE = '"';
	private static final char ESCAPE = '\\';
	// private static int MaxCsvSize = 4194304;

	private Connection connection = null;
	private List<String> columnList = new ArrayList<String>();
	private List<Integer> columnType = new ArrayList<Integer>();
	private String sql = null;

	public GreenplumCopyWriter(Connection connection, String schemaName, String tableName,
			Map<String, Integer> columnMetaData) {
		this.connection = connection;

		for (Map.Entry<String, Integer> entry : columnMetaData.entrySet()) {
			this.columnList.add(entry.getKey());
			this.columnType.add(entry.getValue());
		}

		this.sql = getCopySql(schemaName, tableName, columnList, 0);
		logger.info("copy sql: {}", this.sql);
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

	private String getCopySql(String schemaName, String tableName, List<String> columnList, int segment_reject_limit) {
		StringBuilder sb = new StringBuilder().append("COPY \"").append(schemaName).append("\".\"").append(tableName)
				.append("\" (").append(constructColumnNameList(columnList))
				.append(") FROM STDIN WITH DELIMITER '|' NULL '' CSV QUOTE '\"' ESCAPE E'\\\\'");

		if (segment_reject_limit >= 2) {
			sb.append(" LOG ERRORS SEGMENT REJECT LIMIT ").append(segment_reject_limit).append(";");
		} else {
			sb.append(";");
		}

		String sql = sb.toString();
		return sql;
	}

	public long write(List<Record> records) throws Exception {
		long ret = 0;
		StringBuilder block = new StringBuilder();
		for (Record record : records) {
			try {
				StringBuilder sb = serializeRecord(record, this.columnType);
				//logger.info(new String(sb.toString()));
				block.append(sb);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// if (block.toString().length() > MaxCsvSize) {
		// String s = new String(block.toString()).substring(0, 100) + "...";
		// logger.error("数据元组超过 {} 字节长度限制被忽略。DATA:{}", MaxCsvSize, s);
		// return 0;
		// }

		try (InputStream in = new ByteArrayInputStream(block.toString().getBytes("UTF-8"));) {
			BaseConnection baseConn = (BaseConnection) connection;
			CopyManager mgr = new CopyManager(baseConn);
			ret = mgr.copyIn(sql, in);
			records.clear();
		}

		return ret;
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

	public Record buildRecord(ResultSet rs, ResultSetMetaData metaData) {
		Record record = new DefaultRecordImpl();
		try {
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				switch (metaData.getColumnType(i)) {
				case Types.CHAR:
				case Types.NCHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
				case Types.NVARCHAR:
				case Types.LONGNVARCHAR:
					String rawData = rs.getString(i);
					record.addColumn(new StringColumn(rawData));
					break;

				case Types.CLOB:
				case Types.NCLOB:
					record.addColumn(new StringColumn(rs.getString(i)));
					break;

				case Types.SMALLINT:
				case Types.TINYINT:
				case Types.INTEGER:
				case Types.BIGINT:
					record.addColumn(new LongColumn(rs.getString(i)));
					break;

				case Types.NUMERIC:
				case Types.DECIMAL:
					record.addColumn(new DoubleColumn(rs.getString(i)));
					break;

				case Types.FLOAT:
				case Types.REAL:
				case Types.DOUBLE:
					record.addColumn(new DoubleColumn(rs.getString(i)));
					break;

				case Types.TIME:
					record.addColumn(new DateColumn(rs.getTime(i)));
					break;

				// for mysql bug, see http://bugs.mysql.com/bug.php?id=35115
				case Types.DATE:
					if (metaData.getColumnTypeName(i).equalsIgnoreCase("year")) {
						record.addColumn(new LongColumn(rs.getInt(i)));
					} else {
						record.addColumn(new DateColumn(rs.getDate(i)));
					}
					break;

				case Types.TIMESTAMP:
					record.addColumn(new DateColumn(rs.getTimestamp(i)));
					break;

				case Types.BINARY:
				case Types.VARBINARY:
				case Types.BLOB:
				case Types.LONGVARBINARY:
					record.addColumn(new BytesColumn(rs.getBytes(i)));
					break;

				// warn: bit(1) -> Types.BIT 可使用BoolColumn
				// warn: bit(>1) -> Types.VARBINARY 可使用BytesColumn
				case Types.BOOLEAN:
				case Types.BIT:
					record.addColumn(new BoolColumn(rs.getBoolean(i)));
					break;

				case Types.NULL:
					String stringData = null;
					if (rs.getObject(i) != null) {
						stringData = rs.getObject(i).toString();
					}
					record.addColumn(new StringColumn(stringData));
					break;

				default:
					throw new RuntimeException(String.format(" 不支持的数据库字段类型. 字段名:[%s], 字段名称:[%s], 字段Java类型:[%s]. ",
							metaData.getColumnName(i), metaData.getColumnType(i), metaData.getColumnClassName(i)));
				}
			}
		} catch (Exception e) {
			logger.warn("error:", e);
			throw new RuntimeException(e);
		}
		
		return record;
	}

}
