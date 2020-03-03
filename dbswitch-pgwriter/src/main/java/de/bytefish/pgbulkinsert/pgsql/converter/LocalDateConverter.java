package de.bytefish.pgbulkinsert.pgsql.converter;

import de.bytefish.pgbulkinsert.pgsql.utils.TimeStampUtils;

import java.time.LocalDate;

public class LocalDateConverter implements IValueConverter<LocalDate, Integer> {

    @Override
    public Integer convert(final LocalDate date) {
        return TimeStampUtils.toPgDays(date);
    }

}
