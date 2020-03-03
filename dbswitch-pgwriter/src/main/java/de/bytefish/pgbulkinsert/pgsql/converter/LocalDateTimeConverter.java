package de.bytefish.pgbulkinsert.pgsql.converter;

import de.bytefish.pgbulkinsert.pgsql.utils.TimeStampUtils;

import java.time.LocalDateTime;

public class LocalDateTimeConverter implements IValueConverter<LocalDateTime, Long> {
    @Override
    public Long convert(final LocalDateTime dateTime) {
        return TimeStampUtils.convertToPostgresTimeStamp(dateTime);
    }
}
