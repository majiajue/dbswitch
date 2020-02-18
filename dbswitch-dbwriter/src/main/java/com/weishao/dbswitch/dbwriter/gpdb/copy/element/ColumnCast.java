package com.weishao.dbswitch.dbwriter.gpdb.copy.element;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.text.ParseException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public final class ColumnCast {

	public static void bind() {
		StringCast.init();
		DateCast.init();
		BytesCast.init();
	}

	public static Date string2Date(final StringColumn column) throws ParseException {
		return StringCast.asDate(column);
	}

	public static byte[] string2Bytes(final StringColumn column) throws UnsupportedEncodingException {
		return StringCast.asBytes(column);
	}

	public static String date2String(final DateColumn column) {
		return DateCast.asString(column);
	}

	public static String bytes2String(final BytesColumn column) throws UnsupportedEncodingException {
		return BytesCast.asString(column);
	}
}

class StringCast {
	static String datetimeFormat = "yyyy-MM-dd HH:mm:ss.SSS";

	static String dateFormat = "yyyy-MM-dd";

	static String timeFormat = "HH:mm:ss";

	static List<String> extraFormats = Collections.emptyList();

	static String timeZone = "GMT+8";

	static FastDateFormat dateFormatter;

	static FastDateFormat timeFormatter;

	static FastDateFormat datetimeFormatter;

	static TimeZone timeZoner;

	static String encoding = "UTF-8";

	static void init() {
		StringCast.timeZoner = TimeZone.getTimeZone(StringCast.timeZone);
		StringCast.datetimeFormatter = FastDateFormat.getInstance(StringCast.datetimeFormat, StringCast.timeZoner);
		StringCast.dateFormatter = FastDateFormat.getInstance(StringCast.dateFormat, StringCast.timeZoner);
		StringCast.timeFormatter = FastDateFormat.getInstance(StringCast.timeFormat, StringCast.timeZoner);
	}

	static Date asDate(final StringColumn column) throws ParseException {
		if (null == column.asString()) {
			return null;
		}

		try {
			return StringCast.datetimeFormatter.parse(column.asString());
		} catch (ParseException ignored) {
		}

		try {
			return StringCast.dateFormatter.parse(column.asString());
		} catch (ParseException ignored) {
		}

		ParseException e;
		try {
			return StringCast.timeFormatter.parse(column.asString());
		} catch (ParseException ignored) {
			e = ignored;
		}

		for (String format : StringCast.extraFormats) {
			try {
				return FastDateFormat.getInstance(format, StringCast.timeZoner).parse(column.asString());
			} catch (ParseException ignored) {
				e = ignored;
			}
		}
		throw e;
	}

	static byte[] asBytes(final StringColumn column) throws UnsupportedEncodingException {
		if (null == column.asString()) {
			return null;
		}

		return column.asString().getBytes(StringCast.encoding);
	}
}

/**
 * 后续为了可维护性，可以考虑直接使用 apache 的DateFormatUtils.
 * 
 * 迟南已经修复了该问题，但是为了维护性，还是直接使用apache的内置函数
 */
class DateCast {

	static String datetimeFormat = "yyyy-MM-dd HH:mm:ss";

	static String dateFormat = "yyyy-MM-dd";

	static String timeFormat = "HH:mm:ss";

	static String timeZone = "GMT+8";

	static TimeZone timeZoner = TimeZone.getTimeZone(DateCast.timeZone);

	static void init() {
		DateCast.timeZoner = TimeZone.getTimeZone(DateCast.timeZone);
		return;
	}

	static String asString(final DateColumn column) {
		if (null == column.asDate()) {
			return null;
		}

		switch (column.getSubType()) {
		case DATE:
			return DateFormatUtils.format(column.asDate(), DateCast.dateFormat, DateCast.timeZoner);
		case TIME:
			return DateFormatUtils.format(column.asDate(), DateCast.timeFormat, DateCast.timeZoner);
		case DATETIME:
			return DateFormatUtils.format(column.asDate(), DateCast.datetimeFormat, DateCast.timeZoner);
		default:
			throw new RuntimeException("时间类型出现不支持类型，目前仅支持DATE/TIME/DATETIME。该类型属于编程错误，请反馈开发团队 .");
		}
	}
}

class BytesCast {
	static String encoding = "utf-8";

	static void init() {
		BytesCast.encoding = "utf-8";
	}

	static String asString(final BytesColumn column) throws UnsupportedEncodingException {
		if (null == column.asBytes()) {
			return null;
		}

		return new String(column.asBytes(), encoding);
	}
}