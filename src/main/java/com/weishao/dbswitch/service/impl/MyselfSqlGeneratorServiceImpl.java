package com.weishao.dbswitch.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.weishao.dbswitch.constant.DatabaseType;
import com.weishao.dbswitch.service.ISqlGeneratorService;
import com.weishao.dbswitch.ddl.DatabaseDialect;
import com.weishao.dbswitch.ddl.SqlDdlOperator;
import com.weishao.dbswitch.ddl.pojo.TableDefinition;
import com.weishao.dbswitch.ddl.sql.DdlSqlCreateTable;
import com.weishao.dbswitch.ddl.sql.DdlSqlAlterTable;
import com.weishao.dbswitch.ddl.sql.DdlSqlDropTable;
import com.weishao.dbswitch.ddl.sql.DdlSqlTruncateTable;
import com.weishao.dbswitch.ddl.sql.impl.GreenplumDialectImpl;
import com.weishao.dbswitch.ddl.sql.impl.MySqlDialectImpl;
import com.weishao.dbswitch.ddl.sql.impl.OracleDialectImpl;
import com.weishao.dbswitch.ddl.sql.impl.PostgresDialectImpl;

@Service("MyselfSqlGeneratorService")
public class MyselfSqlGeneratorServiceImpl implements ISqlGeneratorService {

	private static final Map<DatabaseType, String> DATABASE_MAPPER = new HashMap<DatabaseType, String>();

	static {
		DATABASE_MAPPER.put(DatabaseType.MYSQL, MySqlDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseType.ORACLE, OracleDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseType.POSTGRESQL, PostgresDialectImpl.class.getName());
		DATABASE_MAPPER.put(DatabaseType.GREENPLUM, GreenplumDialectImpl.class.getName());
	}

	public static DatabaseDialect getDatabaseInstance(DatabaseType type) {
		if (DATABASE_MAPPER.containsKey(type)) {
			String className = DATABASE_MAPPER.get(type);
			try {
				return (DatabaseDialect) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException(String.format("Unkown database type (%s)", type.name()));
	}

	@Override
	public String createTable(String dbtype, TableDefinition t) {
		DatabaseType type = DatabaseType.valueOf(dbtype.toUpperCase());
		DatabaseDialect dialect = getDatabaseInstance(type);
		SqlDdlOperator operator = new DdlSqlCreateTable(t);
		return operator.toSqlString(dialect);
	}

	@Override
	public String alterTable(String dbtype, String handle, TableDefinition t){
		DatabaseType type = DatabaseType.valueOf(dbtype.toUpperCase());
		DatabaseDialect dialect = getDatabaseInstance(type);
		SqlDdlOperator operator = new DdlSqlAlterTable(t,handle);
		return operator.toSqlString(dialect);
	}

	@Override
	public String dropTable(String dbtype, TableDefinition t) {
		DatabaseType type = DatabaseType.valueOf(dbtype.toUpperCase());
		DatabaseDialect dialect = getDatabaseInstance(type);
		SqlDdlOperator operator = new DdlSqlDropTable(t);
		return operator.toSqlString(dialect);
	}

	@Override
	public String truncateTable(String dbtype, TableDefinition t) {
		DatabaseType type = DatabaseType.valueOf(dbtype.toUpperCase());
		DatabaseDialect dialect = getDatabaseInstance(type);
		SqlDdlOperator operator = new DdlSqlTruncateTable(t);
		return operator.toSqlString(dialect);
	}

}
