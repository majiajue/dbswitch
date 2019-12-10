package com.weishao.dbswitch.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.springframework.stereotype.Service;
import com.weishao.dbswitch.calcite.TheMssqlSqlDialect;
import com.weishao.dbswitch.calcite.TheMysqlSqlDialect;
import com.weishao.dbswitch.calcite.TheOracleSqlDialect;
import com.weishao.dbswitch.calcite.ThePostgresqlSqlDialect;
import com.weishao.dbswitch.constant.DatabaseType;
import com.weishao.dbswitch.service.ISqlConvertService;

/**
 * SQL语法格式转换
 * 
 * @author tang
 * 
 * DDL—数据定义语言(CREATE，ALTER，DROP，DECLARE) 
 * DML—数据操纵语言(SELECT，DELETE，UPDATE，INSERT) 
 * DCL—数据控制语言(GRANT，REVOKE，COMMIT，ROLLBACK)
 * 
 */
@Service("CalciteConvertService")
public class CalciteSqlConvertServiceImpl implements ISqlConvertService {
	
	private static final Logger logger = LoggerFactory.getLogger(CalciteSqlConvertServiceImpl.class);
	
	private Lex getDatabaseLex(DatabaseType type) {
		switch (type) {
		case MYSQL:
			return Lex.MYSQL;
		case ORACLE:
			return Lex.ORACLE;
		case SQLSERVER:
		case SQLSERVER2000:
			return Lex.SQL_SERVER;
		case POSTGRESQL:
			return Lex.MYSQL_ANSI;
		default:
			throw new RuntimeException(String.format("Unkown database type (%s)",type.name()));
		}
	}
	
	private SqlDialect getDatabaseDialect(DatabaseType type) {
		switch (type) {
		case MYSQL:
			return TheMysqlSqlDialect.DEFAULT;
		case ORACLE:
			return TheOracleSqlDialect.DEFAULT;
		case SQLSERVER:
		case SQLSERVER2000:
			return TheMssqlSqlDialect.DEFAULT;
		case POSTGRESQL:
			return ThePostgresqlSqlDialect.DEFAULT;
		case GREENPLUM:
			return ThePostgresqlSqlDialect.DEFAULT;
		default:
			throw new RuntimeException(String.format("Unkown database type (%s)",type.name()));
		}
	}
	
	@Override
	public Map<String,String>  dmlSentence(String sql){
		SqlParser.Config config = SqlParser.configBuilder()
				//.setCaseSensitive(true)
				.build();
		SqlParser parser = SqlParser.create(sql, config);
		SqlNode sqlNode;
		try {
			sqlNode = parser.parseStmt();
		} catch (SqlParseException e) {
			throw new RuntimeException(e);
		}

		Map<String,String>ret=new HashMap<String,String>();
		ret.put("oracle",sqlNode.toSqlString(TheOracleSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		ret.put("postgresql",sqlNode.toSqlString(ThePostgresqlSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		ret.put("mysql",sqlNode.toSqlString(TheMysqlSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		ret.put("sqlserver",sqlNode.toSqlString(TheMssqlSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		return ret;
	}

	@Override
	public String dmlSentence(String sql, DatabaseType target) {
		logger.info("DML SQL: [{}] {} ", target.name(), sql);
		SqlParser.Config config = SqlParser.configBuilder().build();
		SqlParser parser = SqlParser.create(sql, config);
		SqlNode sqlNode;
		try {
			sqlNode = parser.parseStmt();
		} catch (SqlParseException e) {
			throw new RuntimeException(e);
		}

		return sqlNode.toSqlString(this.getDatabaseDialect(target)).toString().replace("\r\n", " ");
	}

	@Override
	public String dmlSentence(DatabaseType source, DatabaseType target, String sql) {
		logger.info("DML SQL: [{}->{}] {} ", source.name(), target.name(), sql);
		SqlParser.Config config = SqlParser.configBuilder().setLex(this.getDatabaseLex(source)).build();
		SqlParser parser = SqlParser.create(sql, config);
		SqlNode sqlNode;
		try {
			sqlNode = parser.parseStmt();
		} catch (SqlParseException e) {
			throw new RuntimeException(e);
		}

		return sqlNode.toSqlString(this.getDatabaseDialect(target)).toString().replace("\r\n", " ");
	}
	
	@Override
	public Map<String,String>  ddlSentence(String sql){
		logger.info("DDL Sentence SQL:{}",sql);
        SqlParser.Config config = SqlParser.configBuilder()
        	    .setParserFactory(SqlDdlParserImpl.FACTORY)
        	   // .setCaseSensitive(true)
        	    .build();

        SqlParser parser = SqlParser.create(sql, config);
		SqlNode sqlNode;
		try {
			sqlNode = parser.parseStmt();
		} catch (SqlParseException e) {
			logger.error("ERROR:  Invalid SQL format:{} --->",sql,e);
			throw new RuntimeException(e);
		}
		
		Map<String,String>ret=new HashMap<String,String>();
		ret.put("oracle",sqlNode.toSqlString(TheOracleSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		ret.put("postgresql",sqlNode.toSqlString(ThePostgresqlSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		ret.put("mysql",sqlNode.toSqlString(TheMysqlSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		ret.put("sqlserver",sqlNode.toSqlString(TheMssqlSqlDialect.DEFAULT).toString().replace("\r\n", " ").replace("\n", " "));
		return ret;
	}

	@Override
	public String ddlSentence(String sql, DatabaseType target) {
		logger.info("DDL SQL: [{}] {} ", target.name(), sql);
		SqlParser.Config config = SqlParser.configBuilder()
				.setParserFactory(SqlDdlParserImpl.FACTORY)
				// .setCaseSensitive(true)
				.build();

		SqlParser parser = SqlParser.create(sql, config);
		SqlNode sqlNode;
		try {
			sqlNode = parser.parseStmt();
		} catch (SqlParseException e) {
			throw new RuntimeException(e);
		}

		return sqlNode.toSqlString(this.getDatabaseDialect(target)).toString().replace("\r\n", " ");
	}

	@Override
	public String ddlSentence(DatabaseType source, DatabaseType target, String sql) {
		logger.info("DDL SQL: [{}->{}] {} ", source.name(), target.name(), sql);
		SqlParser.Config config = SqlParser.configBuilder()
				.setParserFactory(SqlDdlParserImpl.FACTORY)
				.setLex(this.getDatabaseLex(source))
				// .setCaseSensitive(true)
				.build();

		SqlParser parser = SqlParser.create(sql, config);
		SqlNode sqlNode;
		try {
			sqlNode = parser.parseStmt();
		} catch (SqlParseException e) {
			throw new RuntimeException(e);
		}

		return sqlNode.toSqlString(this.getDatabaseDialect(target)).toString().replace("\r\n", " ");
	}
	
	public Map<String,String>  dclSentence(String sql){
		throw new RuntimeException("Unimplement!");
	}
	
	@Override
	public String  dclSentence(DatabaseType source,DatabaseType target,String sql) {
		throw new RuntimeException("Unimplement!");
	}
	
}
