// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.pgbulkinsert.pgsql.handlers.utils;

import de.bytefish.pgbulkinsert.pgsql.model.geometric.Point;

import java.io.DataOutputStream;
import java.io.IOException;

public class GeometricUtils {

    public static void writePoint(DataOutputStream buffer, final Point value) throws IOException {
        buffer.writeDouble(value.getX());
        buffer.writeDouble(value.getY());
    }

}
