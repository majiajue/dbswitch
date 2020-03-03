package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;

public class IntegerValueHandler<T extends Number> extends BaseValueHandler<T> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final T value) throws Exception {
        buffer.writeInt(4);
        buffer.writeInt(value.intValue());
    }
}
