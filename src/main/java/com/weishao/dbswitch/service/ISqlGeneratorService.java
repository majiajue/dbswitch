package com.weishao.dbswitch.service;

import com.weishao.dbswitch.ddl.pojo.TableDefinition;

public interface ISqlGeneratorService {

	public String createTable(String dbtype, TableDefinition t);

	public String alterTable(String dbtype, String handle, TableDefinition t);

	public String dropTable(String dbtype, TableDefinition t);

	public String truncateTable(String dbtype, TableDefinition t);
}
