package de.bytefish.pgbulkinsert.util;

import java.nio.charset.Charset;

public class StringUtils {
	
	private static Charset utf8Charset = Charset.forName("UTF-8");

    private StringUtils() {}

    public static boolean isNullOrWhiteSpace(String input) {
        return  input == null || input.trim().length() == 0;
    }

    public static byte[] getUtf8Bytes(String value) {
        return value.getBytes(utf8Charset);
    }

}