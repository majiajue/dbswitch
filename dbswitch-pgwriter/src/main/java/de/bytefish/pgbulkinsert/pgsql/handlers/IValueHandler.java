package de.bytefish.pgbulkinsert.pgsql.handlers;

import java.io.DataOutputStream;

public interface IValueHandler<TTargetType> extends ValueHandler {

    void handle(DataOutputStream buffer, final TTargetType value);
}
