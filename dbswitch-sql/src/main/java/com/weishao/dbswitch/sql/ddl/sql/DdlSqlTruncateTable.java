package com.weishao.dbswitch.sql.ddl.sql;

import com.weishao.dbswitch.sql.ddl.DatabaseDialect;
import com.weishao.dbswitch.sql.ddl.SqlDdlOperator;
import com.weishao.dbswitch.sql.ddl.pojo.TableDefinition;

public class DdlSqlTruncateTable extends SqlDdlOperator {

	private TableDefinition table;
	
	public DdlSqlTruncateTable(TableDefinition t) {
		super("TRUNCATE TABLE ");
		this.table=t;
	}

	@Override
	public String toSqlString(DatabaseDialect dialect) {
		StringBuilder sb=new StringBuilder();
		sb.append(this.getName());
		String fullTableName=dialect.getSchemaTableName(table.getSchemaName(), table.getTableName());
		sb.append(fullTableName);
		return sb.toString();
	}

}
