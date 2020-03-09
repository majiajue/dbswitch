package com.weishao.dbswitch.core.service.impl;

import java.util.List;
import java.util.Objects;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.DatabaseFactory;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMetaDataService;
import com.weishao.dbswitch.core.util.GenerateSqlUtils;

public class MigrationMetaDataServiceImpl implements IMetaDataService {

	protected AbstractDatabase database = null;

	public void setDatabaseConnection(DatabaseType dbtype) {
		this.database = DatabaseFactory.getDatabaseInstance(dbtype);
	}

	@Override
	public List<String> querySchemaList(String jdbcUrl, String username, String password) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.querySchemaList();
		} finally {
			db.close();
		}
	}

	@Override
	public List<TableDescription> queryTableList(String jdbcUrl, String username, String password, String schemaName) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.queryTableList(schemaName);
		} finally {
			db.close();
		}
	}

	@Override
	public List<ColumnDescription> queryTableColumnMeta(String jdbcUrl, String username, String password,
			String schemaName, String tableName) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.queryTableColumnMeta(schemaName, tableName);
		} finally {
			db.close();
		}
	}

	@Override
	public List<ColumnDescription> querySqlColumnMeta(String jdbcUrl, String username, String password,
			String querySql) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.querySelectSqlColumnMeta(querySql);
		} finally {
			db.close();
		}
	}

	@Override
	public List<String> queryTablePrimaryKeys(String jdbcUrl, String username, String password, String schemaName,
			String tableName) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			return db.queryTablePrimaryKeys(schemaName, tableName);
		} finally {
			db.close();
		}
	}

	public void testQuerySQL(String jdbcUrl, String username, String password, String sql) {
		AbstractDatabase db = Objects.requireNonNull(this.database, "Please call setDatabaseConnection() first!");
		try {
			db.connect(jdbcUrl, username, password);
			db.testQuerySQL(sql);
		} finally {
			db.close();
		}
	}

	@Override
	public String getDDLCreateTableSQL(DatabaseType type, List<ColumnDescription> fieldNames, List<String> primaryKeys,
			String schemaName, String tableName, boolean ifNotExist) {
		return GenerateSqlUtils.getDDLCreateTableSQL(type, fieldNames, primaryKeys, schemaName, tableName, ifNotExist);
	}
}
