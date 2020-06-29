package com.weishao.dbswitch.core.model;

import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.database.AbstractDatabase;

/**
 * 数据库表列的元信息
 * @author tang
 *
 */
public class ColumnMetaData {

	/** Value type indicating that the value has no type set */
	public static final int TYPE_NONE = 0;

	/**
	 * Value type indicating that the value contains a floating point double
	 * precision number.
	 */
	public static final int TYPE_NUMBER = 1;

	/** Value type indicating that the value contains a text String. */
	public static final int TYPE_STRING = 2;

	/** Value type indicating that the value contains a Date. */
	public static final int TYPE_DATE = 3;

	/** Value type indicating that the value contains a boolean. */
	public static final int TYPE_BOOLEAN = 4;

	/** Value type indicating that the value contains a long integer. */
	public static final int TYPE_INTEGER = 5;

	/**
	 * Value type indicating that the value contains a floating point precision
	 * number with arbitrary precision.
	 */
	public static final int TYPE_BIGNUMBER = 6;

	/** Value type indicating that the value contains an Object. */
	public static final int TYPE_SERIALIZABLE = 7;

	/**
	 * Value type indicating that the value contains binary data: BLOB, CLOB, ...
	 */
	public static final int TYPE_BINARY = 8;

	/**
	 * Value type indicating that the value contains a date-time with nanosecond
	 * precision
	 */
	public static final int TYPE_TIMESTAMP = 9;
	
	/** Value type indicating that the value contains a time */
	public static final int TYPE_TIME=10;

	/** Value type indicating that the value contains a Internet address */
	public static final int TYPE_INET = 11;

	/** The Constant typeCodes. */
	public static final String[] typeCodes = new String[] { "-", "Number", "String", "Date", "Boolean", "Integer",
			"BigNumber", "Serializable", "Binary", "Timestamp", "Time", "Internet Address", };

	//////////////////////////////////////////////////////////////////////

	/* Column name */
	protected String name;
	protected int length;
	protected int precision;
	protected int type;

	/**
	 * Constructor function
	 * 
	 * @param name
	 * @param type
	 * @param length
	 * @param precision
	 */
	public ColumnMetaData(ColumnDescription desc) {
		this.create(desc);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Checks whether or not the value is a String.
	 *
	 * @return true if the value is a String.
	 */
	public boolean isString() {
		return type == TYPE_STRING;
	}

	/**
	 * Checks whether or not this value is a Date
	 *
	 * @return true if the value is a Date
	 */
	public boolean isDate() {
		return type == TYPE_DATE;
	}
	
	/**
	 * Checks whether or not this value is a Time
	 *
	 * @return true if the value is a Time
	 */
	public boolean isTime() {
		return type == TYPE_TIME;
	}
	
	/**
	 * Checks whether or not this value is a DateTime
	 *
	 * @return true if the value is a DateTime
	 */
	public boolean isDateTime() {
		return type == TYPE_TIMESTAMP;
	}

	/**
	 * Checks whether or not the value is a Big Number
	 *
	 * @return true is this value is a big number
	 */
	public boolean isBigNumber() {
		return type == TYPE_BIGNUMBER;
	}

	/**
	 * Checks whether or not the value is a Number
	 *
	 * @return true is this value is a number
	 */
	public boolean isNumber() {
		return type == TYPE_NUMBER;
	}

	/**
	 * Checks whether or not this value is a boolean
	 *
	 * @return true if this value has type boolean.
	 */
	public boolean isBoolean() {
		return type == TYPE_BOOLEAN;
	}

	/**
	 * Checks whether or not this value is of type Serializable
	 *
	 * @return true if this value has type Serializable
	 */
	public boolean isSerializableType() {
		return type == TYPE_SERIALIZABLE;
	}

	/**
	 * Checks whether or not this value is of type Binary
	 *
	 * @return true if this value has type Binary
	 */
	public boolean isBinary() {
		return type == TYPE_BINARY;
	}

	/**
	 * Checks whether or not this value is an Integer
	 *
	 * @return true if this value is an integer
	 */
	public boolean isInteger() {
		return type == TYPE_INTEGER;
	}

	/**
	 * Checks whether or not this Value is Numeric A Value is numeric if it is
	 * either of type Number or Integer
	 *
	 * @return true if the value is either of type Number or Integer
	 */
	public boolean isNumeric() {
		return isInteger() || isNumber() || isBigNumber();
	}

	/**
	 * Checks whether or not the specified type is either Integer or Number
	 *
	 * @param t the type to check
	 * @return true if the type is Integer or Number
	 */
	public static final boolean isNumeric(int t) {
		return t == TYPE_INTEGER || t == TYPE_NUMBER || t == TYPE_BIGNUMBER;
	}

	/**
	 * Return the type of a value in a textual form: "String", "Number", "Integer",
	 * "Boolean", "Date", ...
	 *
	 * @return A String describing the type of value.
	 */
	public String getTypeDesc() {
		return typeCodes[type];
	}

	private void create(ColumnDescription desc) {
		int length = -1;
		int precision = -1;
		int valtype = ColumnMetaData.TYPE_NONE;
		int type = desc.getFieldType();
		boolean signed = desc.isSigned();

		switch (type) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGVARCHAR: // Character Large Object
			valtype = ColumnMetaData.TYPE_STRING;
			length = desc.getDisplaySize();
			break;

		case java.sql.Types.CLOB:
		case java.sql.Types.NCLOB:
			valtype = ColumnMetaData.TYPE_STRING;
			length = AbstractDatabase.CLOB_LENGTH;
			break;

		case java.sql.Types.BIGINT:
			// verify Unsigned BIGINT overflow!
			//
			if (signed) {
				valtype = ColumnMetaData.TYPE_INTEGER;
				precision = 0; // Max 9.223.372.036.854.775.807
				length = 15;
			} else {
				valtype = ColumnMetaData.TYPE_BIGNUMBER;
				precision = 0; // Max 18.446.744.073.709.551.615
				length = 16;
			}
			break;

		case java.sql.Types.INTEGER:
			valtype = ColumnMetaData.TYPE_INTEGER;
			precision = 0; // Max 2.147.483.647
			length = 9;
			break;

		case java.sql.Types.SMALLINT:
			valtype = ColumnMetaData.TYPE_INTEGER;
			precision = 0; // Max 32.767
			length = 4;
			break;

		case java.sql.Types.TINYINT:
			valtype = ColumnMetaData.TYPE_INTEGER;
			precision = 0; // Max 127
			length = 2;
			break;

		case java.sql.Types.DECIMAL:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
		case java.sql.Types.REAL:
		case java.sql.Types.NUMERIC:
			valtype = ColumnMetaData.TYPE_NUMBER;
			length = desc.getPrecisionSize();
			precision = desc.getScaleSize();
			if (length >= 126) {
				length = -1;
			}
			if (precision >= 126) {
				precision = -1;
			}

			if (type == java.sql.Types.DOUBLE || type == java.sql.Types.FLOAT || type == java.sql.Types.REAL) {
				if (precision == 0) {
					if (!signed) {
						precision = -1; // precision is obviously incorrect if the type if
						// Double/Float/Real
					} else {
						length = 18;
						precision = 4;
					}
				}

				// If we're dealing with PostgreSQL and double precision types
				if (desc.getDbType() == DatabaseType.POSTGRESQL && type == java.sql.Types.DOUBLE && precision >= 16
						&& length >= 16) {
					precision = -1;
					length = -1;
				}

				// MySQL: max resolution is double precision floating point (double)
				// The (12,31) that is given back is not correct
				if (desc.getDbType() == DatabaseType.MYSQL) {
					if (precision >= length) {
						precision = -1;
						length = -1;
					}
				}

				// if the length or precision needs a BIGNUMBER
				//if (length > 15 || precision > 15) {
				//	valtype = ColumnMetaData.TYPE_BIGNUMBER;
				//}
			} else {
				if (precision == 0) {
					if (length <= 18 && length > 0) { // Among others Oracle is affected
						// here.
						valtype = ColumnMetaData.TYPE_INTEGER; // Long can hold up to 18
						// significant digits
					} else if (length > 18) {
						valtype = ColumnMetaData.TYPE_BIGNUMBER;
					}
				} else { // we have a precision: keep NUMBER or change to BIGNUMBER?
					if (length > 15 || precision > 15) {
						valtype = ColumnMetaData.TYPE_BIGNUMBER;
					}
				}
			}

			if (desc.getDbType() == DatabaseType.POSTGRESQL || desc.getDbType() == DatabaseType.GREENPLUM) {
				// undefined size => arbitrary precision
				if (type == java.sql.Types.NUMERIC && length == 0 && precision == 0) {
					valtype = ColumnMetaData.TYPE_BIGNUMBER;
					length = -1;
					precision = -1;
				}
			}

			if (desc.getDbType() == DatabaseType.ORACLE) {
				if (precision == 0 && length == 38) {
					valtype = ColumnMetaData.TYPE_INTEGER;
				}
				if (precision <= 0 && length <= 0) {
					// undefined size: BIGNUMBER,
					// precision on Oracle can be 38, too
					// big for a Number type
					valtype = ColumnMetaData.TYPE_BIGNUMBER;
					length = -1;
					precision = -1;
				}
			}

			break;

		case java.sql.Types.TIMESTAMP:
			valtype = ColumnMetaData.TYPE_TIMESTAMP;
			length = desc.getScaleSize();
			break;

		case java.sql.Types.DATE:
			valtype = ColumnMetaData.TYPE_DATE;
			break;
			
		case java.sql.Types.TIME:
			valtype = ColumnMetaData.TYPE_TIME;
			break;

		case java.sql.Types.BOOLEAN:
		case java.sql.Types.BIT:
			valtype = ColumnMetaData.TYPE_BOOLEAN;
			break;

		case java.sql.Types.BINARY:
		case java.sql.Types.BLOB:
		case java.sql.Types.VARBINARY:
		case java.sql.Types.LONGVARBINARY:
			valtype = ColumnMetaData.TYPE_BINARY;
			precision = -1;
			break;

		default:
			valtype = ColumnMetaData.TYPE_STRING;
			precision = desc.getScaleSize();
			break;
		}

		this.name = desc.getFieldName();
		this.length = length;
		this.precision = precision;
		this.type = valtype;
	}
	
}
