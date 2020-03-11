package de.bytefish.pgbulkinsert.util;

import java.nio.charset.Charset;

public class StringUtils {

	private static Charset utf8Charset = Charset.forName("UTF-8");

	private StringUtils() {
	}

	public static boolean isNullOrWhiteSpace(String input) {
		return input == null || input.trim().length() == 0;
	}

	public static byte[] getUtf8Bytes(String value) {
		return value.getBytes(utf8Charset);
	}

	/**
	 * 字符串中发现非法字符 0x00，将其删除
	 * 
	 * @author tang
	 * 
	 * @param data 处理前的字符串
	 * @return 处理后的字符串
	 */
	public static String escapeString(String data) {
		if (null == data) {
			return data;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < data.length(); ++i) {
			char c = data.charAt(i);
			switch (c) {
			case 0x00:
				continue;
			default:
				break;
			}

			sb.append(c);
		}
		return sb.toString();
	}
}