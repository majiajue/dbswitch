package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;

public class ByteValueHandler<T extends Number> extends BaseValueHandler<T> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final T value) throws Exception {
        buffer.writeInt(1);
        buffer.writeByte(value.byteValue());
    }
}
