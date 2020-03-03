package de.bytefish.pgbulkinsert.pgsql.handlers;

import de.bytefish.pgbulkinsert.pgsql.handlers.utils.GeometricUtils;
import de.bytefish.pgbulkinsert.pgsql.model.geometric.Point;

import java.io.DataOutputStream;

public class PointValueHandler extends BaseValueHandler<Point> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final Point value) throws Exception {
        buffer.writeInt(16);

        GeometricUtils.writePoint(buffer, value);
    }
}