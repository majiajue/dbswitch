package com.weishao.dbswitch.core.service.impl;

import java.util.List;
import com.weishao.dbswitch.core.constant.Const;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.DatabaseFactory;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.ColumnMetaData;
import com.weishao.dbswitch.core.model.DatabaseDescription;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMigrationService;
import com.weishao.dbswitch.core.util.JdbcUrlUtils;

/**
 * 结构迁移转换实现类
 * 备注：字段信息、主键、生成建表的SQL语句
 * 
 * @author tang
 *
 */
public class MigrationConvertServiceImpl implements IMigrationService {

	private static int connectTimeOut=6;
	protected AbstractDatabase database=null;
	protected DatabaseDescription databaseDesc=null;
	
	public void setDatabaseConnection(DatabaseDescription databaseDesc) {
		this.database=DatabaseFactory.getDatabaseInstance(databaseDesc.getType());
		this.databaseDesc=databaseDesc;
	}
	
	@Override
	public List<String> querySchemaList() {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.Connect(jdbcUrl, username, password);
			return this.database.querySchemaList();
		}finally {
			this.database.Close();
		}
	}

	@Override
	public List<TableDescription> queryTableList(String schemaName) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.Connect(jdbcUrl, username, password);
			return this.database.queryTableList(schemaName);
		}finally {
			this.database.Close();
		}
	}

	@Override
	public List<ColumnDescription> queryTableColumnMeta(String schemaName, String tableName) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.Connect(jdbcUrl, username, password);
			return this.database.queryTableColumnMeta(schemaName, tableName);
		}finally {
			this.database.Close();
		}
	}
	
	@Override
	public List<ColumnDescription> querySqlColumnMeta(String querySql){
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.Connect(jdbcUrl, username, password);
			return this.database.querySelectSqlColumnMeta(querySql);
		}finally {
			this.database.Close();
		}
	}

	@Override
	public List<String> queryTablePrimaryKeys(String schemaName, String tableName) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.Connect(jdbcUrl, username, password);
			return this.database.queryTablePrimaryKeys(schemaName, tableName);
		}finally {
			this.database.Close();
		}
	}

	public void testQuerySQL(String sql) {
		String jdbcUrl=JdbcUrlUtils.getJdbcUrl(this.databaseDesc,MigrationConvertServiceImpl.connectTimeOut);
		String username=this.databaseDesc.getUsername();
		String password=this.databaseDesc.getPassword();

		try {
			this.database.Connect(jdbcUrl, username, password);
			this.database.testQuerySQL(sql);
		}finally {
			this.database.Close();
		}
	}

	@Override
	public String getDDLCreateTableSQL(DatabaseType type, List<ColumnDescription> fieldNames, List<String> primaryKeys,
			String schemaName, String tableName, boolean ifNotExist) {
		StringBuilder retval = new StringBuilder();
		AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);

		retval.append(Const.CREATE_TABLE);
		ifNotExist = false;
		// if(ifNotExist && type!=DatabaseType.ORACLE) {
		// retval.append( Const.IF_NOT_EXISTS );
		// }
		retval.append(db.getQuotedSchemaTableCombination(schemaName, tableName) + Const.CR);
		retval.append("(").append(Const.CR);

		for (int i = 0; i < fieldNames.size(); i++) {
			if (i > 0) {
				retval.append(", ");
			} else {
				retval.append("  ");
			}

			ColumnMetaData v = fieldNames.get(i).getMetaData();
			retval.append(db.getFieldDefinition(v, primaryKeys, true));
		}

		if (primaryKeys.size() > 0) {
			String pk = db.getPrimaryKeyAsString(primaryKeys);
			retval.append(", PRIMARY KEY (").append(pk).append(")").append(Const.CR);
		}

		retval.append(")").append(Const.CR);
		return db.formatSQL(retval.toString());
	}

}
