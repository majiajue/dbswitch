package de.bytefish.pgbulkinsert.pgsql.processor.handler;

import java.util.List;

public interface IBulkWriteHandler<TEntity> {

    void write(List<TEntity> entities) throws Exception;

}
