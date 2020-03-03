package de.bytefish.pgbulkinsert.pgsql.handlers;

import de.bytefish.pgbulkinsert.pgsql.constants.DataType;

public interface IValueHandlerProvider {

    <TTargetType> IValueHandler<TTargetType> resolve(DataType targetType);

}
