package com.weishao.dbswitch.data.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.common.util.CommonUtils;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMetaDataService;
import com.weishao.dbswitch.data.config.PropertiesConfig;
import com.weishao.dbswitch.data.util.JdbcTemplateUtils;
import com.weishao.dbswitch.dbwriter.DatabaseWriterFactory;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;

@Service("MainService")
public class MainService {

	private static final Logger logger = LoggerFactory.getLogger(MainService.class);
	
	@Autowired
	@Qualifier("sourceDataSource")
	private BasicDataSource sourceDataSource;
	
	@Autowired
	@Qualifier("targetDataSource")
	private BasicDataSource targetDataSource;

	@Autowired
	private PropertiesConfig properties;
	
	@Autowired
	private IMetaDataService  metaDataService;
	
	public void run() {
		DatabaseType sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.sourceDataSource);
		metaDataService.setDatabaseConnection(sourceDatabaseType);
		
		IDatabaseWriter writer = DatabaseWriterFactory.createDatabaseWriter(this.targetDataSource,properties.engineInsert);

		StopWatch watch=new StopWatch();
		watch.start();
		
		try {
			logger.info("service is running....");
			JdbcTemplate sourceJdbcTemplate=new JdbcTemplate(this.sourceDataSource);
			JdbcTemplate targetJdbcTemplate=new JdbcTemplate(this.targetDataSource);

			List<TableDescription> tableList = metaDataService.queryTableList(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, properties.schemaNameSource);
			List<String> includes = properties.getSourceTableNameIncludes();
			logger.info("Includes tables is :{}",JSON.toJSONString(includes));
			List<String> filters = properties.getSourceTableNameExcludes();
			logger.info("Filter tables is :{}",JSON.toJSONString(filters));
			
			boolean useExcludeTables=includes.isEmpty();
			if(useExcludeTables) {
				logger.info("!!!! Use source.datasource-source.excludes to filter tables");
			}else {
				logger.info("!!!! Use source.datasource-source.includes to filter tables");
			}
			
			int finished = 0;
			for (TableDescription td : tableList) {
				String tableName = td.getTableName();
				if (useExcludeTables) {
					if (!filters.contains(tableName)) {
						this.doDataMigration(sourceJdbcTemplate, targetJdbcTemplate, td, writer);
					}
				} else {
					if (includes.contains(tableName)) {
						this.doDataMigration(sourceJdbcTemplate, targetJdbcTemplate, td, writer);
					}
				}

				logger.info("#### Complete data migration count is {},total is {}, process is {}", ++finished,
						tableList.size(), finished * 100.0 / tableList.size());
			}

			logger.info("service run success!");
		} catch (Exception e) {
			logger.error("error:", e);
		} finally {
			watch.stop();
			logger.info("total elipse = " + watch.getTotalTimeSeconds() + " s");
		}
	}

	private void doDataMigration(JdbcTemplate sourceJdbcTemplate, JdbcTemplate targetJdbcTemplate, TableDescription table, IDatabaseWriter writer) throws SQLException {
		DatabaseType sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.sourceDataSource);
		DatabaseType targetDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(this.targetDataSource);
		String fullTableName = CommonUtils.getTableFullNameByDatabase(sourceDatabaseType, table.getSchemaName(),table.getTableName());
		Map<String, Integer> columnMetaData = JdbcTemplateUtils.getColumnMetaData(sourceJdbcTemplate, fullTableName);

		if (properties.dropTargetTable) {
			String targetFullTableName = CommonUtils.getTableFullNameByDatabase(targetDatabaseType,properties.dbTargetSchema, table.getTableName());
			String sqlDropTable = String.format("DROP TABLE %s", targetFullTableName);
			try {
				targetJdbcTemplate.execute(sqlDropTable);
			} catch (Exception e) {
				logger.info("Table {} is not exits!", targetFullTableName);
			}
			logger.info("Execute SQL: {}", sqlDropTable);
			
			List<ColumnDescription> columnDescs = metaDataService.queryTableColumnMeta(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, table.getSchemaName(), table.getTableName());
			List<String> primaryKeys = metaDataService.queryTablePrimaryKeys(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, table.getSchemaName(), table.getTableName());
			String sqlCreateTable = metaDataService.getDDLCreateTableSQL(targetDatabaseType, columnDescs, primaryKeys,
					properties.dbTargetSchema, table.getTableName(), false);
			targetJdbcTemplate.execute(sqlCreateTable);
			logger.info("Execute SQL: \n{}", sqlCreateTable);
		}

		List<String> columnList = new ArrayList<String>();
		for (Map.Entry<String, Integer> entry : columnMetaData.entrySet()) {
			columnList.add(entry.getKey());
		}
		
		writer.prepareWrite(properties.dbTargetSchema, table.getTableName());
		String sqlQuery = CommonUtils.getSelectColumnsSQL(sourceDatabaseType, table.getSchemaName(),table.getTableName(), columnList);
		logger.info("Query source database table sql: {}", sqlQuery);

		sourceJdbcTemplate.execute(new ConnectionCallback<Boolean>() {

			public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
				Statement stmt = null;
				ResultSet rs = null;
				long totalCount = 0;
				List<Object[]> recordValues = new LinkedList<Object[]>();

				int fetchSize = 100;
				if (properties.fetchSizeSource >= 100) {
					fetchSize = properties.fetchSizeSource;
				}

				try {
					con.setAutoCommit(false);
					stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					stmt.setQueryTimeout(3600);
					if (sourceDatabaseType == DatabaseType.MYSQL) {
						stmt.setFetchSize(Integer.MIN_VALUE);
					} else {
						stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
						stmt.setFetchSize(fetchSize);
					}

					rs = stmt.executeQuery(sqlQuery);
					ResultSetMetaData metaData = rs.getMetaData();
					while (rs.next()) {
						Object args[] = new Object[metaData.getColumnCount()];
						for (int j = 0; j < metaData.getColumnCount(); ++j) {
							args[j] = rs.getObject(j + 1);
							if (args[j] instanceof Boolean) {
								args[j] = String.valueOf((boolean) args[j] ? 1 : 0);
							}
						}

						recordValues.add(args);
						++totalCount;

						if (recordValues.size() >= fetchSize) {
							long ret = writer.write(columnList, recordValues);
							logger.info("handle table [{}] data count: {}", fullTableName, ret);
							recordValues.clear();
						}
					}

					if (recordValues.size() >= 0) {
						long ret = writer.write(columnList, recordValues);
						logger.info("handle table [{}] data count: {}", fullTableName, ret);
						recordValues.clear();
					}

					logger.info("handle table [{}]  total data count:{} ", fullTableName, totalCount);
				} catch (Exception ex) {
					JdbcUtils.closeResultSet(rs);
					rs = null;
					JdbcUtils.closeStatement(stmt);
					stmt = null;
					throw new SQLException(ex);
				} finally {
					JdbcUtils.closeResultSet(rs);
					JdbcUtils.closeStatement(stmt);
				}

				return true;
			}
		});

	}
	
}
