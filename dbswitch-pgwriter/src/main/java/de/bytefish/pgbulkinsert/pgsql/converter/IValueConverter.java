package de.bytefish.pgbulkinsert.pgsql.converter;

public interface IValueConverter<TSource, TTarget> {

    TTarget convert(TSource source);

}
