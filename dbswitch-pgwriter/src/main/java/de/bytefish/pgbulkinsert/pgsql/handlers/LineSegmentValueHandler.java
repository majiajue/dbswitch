package de.bytefish.pgbulkinsert.pgsql.handlers;

import de.bytefish.pgbulkinsert.pgsql.handlers.utils.GeometricUtils;
import de.bytefish.pgbulkinsert.pgsql.model.geometric.LineSegment;

import java.io.DataOutputStream;

public class LineSegmentValueHandler extends BaseValueHandler<LineSegment> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final LineSegment value) throws Exception {
        buffer.writeInt(32);

        GeometricUtils.writePoint(buffer, value.getP1());
        GeometricUtils.writePoint(buffer, value.getP2());
    }
}