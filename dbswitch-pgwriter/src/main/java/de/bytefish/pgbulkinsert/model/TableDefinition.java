package de.bytefish.pgbulkinsert.model;

import de.bytefish.pgbulkinsert.util.StringUtils;

public class TableDefinition {

    private final String schema;

    private final String tableName;

    public TableDefinition(String tableName) {
        this("", tableName);
    }

    public TableDefinition(String schema, String tableName) {
        this.schema = schema;
        this.tableName = tableName;
    }

    public String getSchema() {
        return schema;
    }

    public String getTableName() {
        return tableName;
    }

    public String GetFullyQualifiedTableName() {
        if (StringUtils.isNullOrWhiteSpace(schema)) {
            return tableName;
        }
        return String.format("%1$s.%2$s", schema, tableName);
    }

    @Override
    public String toString() {
        return String.format("TableDefinition (Schema = {%1$s}, TableName = {%2$s})", schema, tableName);
    }
}