// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;
import java.io.IOException;

public class ShortValueHandler<T extends Number> extends BaseValueHandler<T> {

    @Override
    protected void internalHandle(DataOutputStream buffer, final T value) throws IOException {
        buffer.writeInt(2);
        buffer.writeShort(value.shortValue());
    }

    @Override
    public int getLength(T value) {
        return 2;
    }
}
