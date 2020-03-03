package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;

public class BooleanValueHandler extends BaseValueHandler<Boolean> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Boolean value) throws Exception {
        buffer.writeInt(1);
        if (value) {
            buffer.writeByte(1);
        } else {
            buffer.writeByte(0);
        }
    }
}
