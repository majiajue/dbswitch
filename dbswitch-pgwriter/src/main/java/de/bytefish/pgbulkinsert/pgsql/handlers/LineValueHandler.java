package de.bytefish.pgbulkinsert.pgsql.handlers;

import de.bytefish.pgbulkinsert.pgsql.model.geometric.Line;

import java.io.DataOutputStream;

public class LineValueHandler extends BaseValueHandler<Line> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Line value) throws Exception {
        buffer.writeInt(24);

        buffer.writeDouble(value.getA());
        buffer.writeDouble(value.getB());
        buffer.writeDouble(value.getC());
    }
}