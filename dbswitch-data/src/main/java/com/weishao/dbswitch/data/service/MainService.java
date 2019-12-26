package com.weishao.dbswitch.data.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.common.util.CommonUtils;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMetaDataService;
import com.weishao.dbswitch.data.config.PropertiesConfig;
import com.weishao.dbswitch.data.util.JdbcTemplateUtils;
import com.weishao.dbswitch.gpwriter.GreenplumCopyWriter;
import com.weishao.dbswitch.gpwriter.record.Record;

@Service("MainService")
public class MainService {

	private static final Logger logger = LoggerFactory.getLogger(MainService.class);
	
	@Autowired
	@Qualifier("sourceJdbcTemplate")
	JdbcTemplate sourceJdbcTemplate;
	
	@Autowired
	@Qualifier("targetJdbcTemplate")
	JdbcTemplate targetJdbcTemplate;

	@Autowired
	PropertiesConfig properties;
	
	@Autowired
	IMetaDataService  metaDataService;
	
	public void run() {
		DatabaseType sourceDatabaseType = JdbcTemplateUtils.getDatabaseProduceName(sourceJdbcTemplate);
		metaDataService.setDatabaseConnection(sourceDatabaseType);

		long begin = System.currentTimeMillis();

		try {
			logger.info("service is running....");

			List<TableDescription> tableList = metaDataService.queryTableList(properties.dbSourceJdbcUrl,
					properties.dbSourceUserName, properties.dbSourcePassword, properties.schemaNameSource);
			List<String> filters = properties.getSourceTableNameExcludes();
			logger.info("Filter tables is :{}",JSON.toJSONString(filters));
			
			for (TableDescription td : tableList) {
				String tableName = td.getTableName();
				if (!filters.contains(tableName)) {
					this.doDataMigration(sourceDatabaseType, td);
				}
			}

			logger.info("service run success!");
		} catch (Exception e) {
			logger.error("error:", e);
		} finally {
			long end = System.currentTimeMillis();
			logger.info("total elipse = " + (end - begin) + " ms");
		}
	}

	private void doDataMigration(DatabaseType dbtype, TableDescription table) throws SQLException {
		String fullTableName = CommonUtils.getTableFullNameByDatabase(dbtype, table.getSchemaName(), table.getTableName());
		Map<String, Integer> columnMetaData = JdbcTemplateUtils.getColumnMetaData(sourceJdbcTemplate, fullTableName);

		if (properties.dropTargetTable) {
			String targetTableName = CommonUtils.getTableFullNameByDatabase(DatabaseType.POSTGRESQL,
					properties.dbTargetSchema, table.getTableName());
			String sqlDropTable = String.format("drop table %s", targetTableName);
			try {
				targetJdbcTemplate.execute(sqlDropTable);
			} catch (Exception e) {
				logger.info("Table {} is not exits!", targetTableName);
			}
			logger.info("Execute SQL: {}", sqlDropTable);
		}
		
		List<ColumnDescription> columnDescs = metaDataService.queryTableColumnMeta(properties.dbSourceJdbcUrl,
				properties.dbSourceUserName, properties.dbSourcePassword, table.getSchemaName(), table.getTableName());
		List<String> primaryKeys = metaDataService.queryTablePrimaryKeys(properties.dbSourceJdbcUrl,
				properties.dbSourceUserName, properties.dbSourcePassword, table.getSchemaName(), table.getTableName());
		String sqlCreateTable = metaDataService.getDDLCreateTableSQL(DatabaseType.POSTGRESQL, columnDescs, primaryKeys,
				properties.dbTargetSchema, table.getTableName(), false);
		targetJdbcTemplate.execute(sqlCreateTable);
		logger.info("Execute SQL: \n{}", sqlCreateTable);

		try (Connection conn = createGreenplumConnection(properties);) {
			GreenplumCopyWriter writer = new GreenplumCopyWriter(conn, properties.dbTargetSchema,	table.getTableName(), columnMetaData);
			
			List<String> columnList = new ArrayList<String>();
			for (Map.Entry<String, Integer> entry : columnMetaData.entrySet()) {
				columnList.add(entry.getKey());
			}
			String quoteChar=CommonUtils.getQuotationChar(dbtype);
			String sqlQuery = String.format("select %s%s%s from %s ", quoteChar,	StringUtils.join(columnList, quoteChar + "," + quoteChar), quoteChar, fullTableName);
			logger.info("Query source database table sql: {}",sqlQuery);
			
			sourceJdbcTemplate.execute(new ConnectionCallback<Boolean>() {

				public Boolean doInConnection(Connection con) throws SQLException, DataAccessException {
					Statement stmt = null;
					ResultSet rs = null;
					long totalCount = 0;
					List<Record> records = new ArrayList<Record>();

					int fetchSize = 100;
					if (properties.fetchSizeSource >= 100) {
						fetchSize = properties.fetchSizeSource;
					}
					
					try {
						con.setAutoCommit(false);
						stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
						stmt.setQueryTimeout(3600);
						if (dbtype==DatabaseType.MYSQL) {
							stmt.setFetchSize(Integer.MIN_VALUE);
						} else {
							stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
							stmt.setFetchSize(fetchSize);
						}

						rs = stmt.executeQuery(sqlQuery);
						ResultSetMetaData metaData = rs.getMetaData();
						while (rs.next()) {
							Record record = writer.buildRecord(rs, metaData);
							if(null==record) {
								continue;
							}
							
							records.add(record);
							++totalCount;

							if (records.size() >= fetchSize) {
								long ret = writer.write(records);
								logger.info("handle table [{}] data count: {}", fullTableName, ret);
								records.clear();
							}
						}

						if (records.size() >= 0) {
							long ret = writer.write(records);
							logger.info("handle table [{}] data count: {}", fullTableName, ret);
							records.clear();
						}

						JdbcUtils.closeResultSet(rs);
						rs = null;

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
		} finally {
			
		}
		
	}
	
	public static Connection createGreenplumConnection(PropertiesConfig propertiesConfig) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			logger.error("error:",e);
			return null;
		}
		
		try {
			return  DriverManager.getConnection(propertiesConfig.dbTargetJdbcUrl, propertiesConfig.dbTargetUserName,propertiesConfig.dbTargetPassword);
		} catch (SQLException e) {
			logger.error("error:",e);
		}
		
		return null;
	}
	
}
