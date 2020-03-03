package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;

import de.bytefish.pgbulkinsert.util.StringUtils;

public class StringValueHandler extends BaseValueHandler<String> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final String value) throws Exception {
        byte[] utf8Bytes = StringUtils.getUtf8Bytes(value);

        buffer.writeInt(utf8Bytes.length);
        buffer.write(utf8Bytes);
    }
}