package com.weishao.dbswitch.sql.service;

import com.weishao.dbswitch.sql.ddl.pojo.TableDefinition;

public interface ISqlGeneratorService {

	public String createTable(String dbtype, TableDefinition t);

	public String alterTable(String dbtype, String handle, TableDefinition t);

	public String dropTable(String dbtype, TableDefinition t);

	public String truncateTable(String dbtype, TableDefinition t);
}
