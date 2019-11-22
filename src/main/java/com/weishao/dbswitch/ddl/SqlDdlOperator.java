package com.weishao.dbswitch.ddl;

import java.util.Objects;

public abstract class SqlDdlOperator {
	private String name;

	public SqlDdlOperator(String name) {
		this.name = Objects.requireNonNull(name);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public abstract String toSqlString(DatabaseDialect dialect) ;
}
