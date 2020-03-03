package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;

public class LongValueHandler<T extends Number> extends BaseValueHandler<T> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final T value) throws Exception {
        buffer.writeInt(8);
        buffer.writeLong(value.longValue());
    }
}
