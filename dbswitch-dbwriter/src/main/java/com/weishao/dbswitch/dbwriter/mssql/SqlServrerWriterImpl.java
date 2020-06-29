package com.weishao.dbswitch.dbwriter.mssql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.weishao.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlServrerWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	public SqlServrerWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public void prepareWrite(String schemaName, String tableName) {
		String sql = String.format("SELECT *  FROM [%s].[%s]  WHERE 1=2", schemaName, tableName);
		Map<String, Integer> columnMetaData = new HashMap<String, Integer>();
		Boolean ret = this.jdbcTemplate.execute(new ConnectionCallback<Boolean>() {

			public Boolean doInConnection(Connection conn) throws SQLException, DataAccessException {
				Statement stmt = null;
				ResultSet rs = null;
				try {
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 0, len = rsMetaData.getColumnCount(); i < len; i++) {
						columnMetaData.put(rsMetaData.getColumnName(i + 1), rsMetaData.getColumnType(i + 1));
					}

					return true;
				} catch (Exception e) {
					throw new RuntimeException(
							String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", schemaName, tableName), e);
				} finally {
					JdbcUtils.closeResultSet(rs);
					JdbcUtils.closeStatement(stmt);
				}
			}
		});

		if (ret) {
			this.schemaName = schemaName;
			this.tableName = tableName;
			this.columnType = Objects.requireNonNull(columnMetaData);

			if (this.columnType.isEmpty()) {
				throw new RuntimeException(
						String.format("获取表:%s.%s 的字段的元信息时失败. 请联系 DBA 核查该库、表信息.", schemaName, tableName));
			}
		} else {
			throw new RuntimeException("内部代码出现错误，请开发人员排查！");
		}
	}
	
	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		List<String> placeHolders = new ArrayList<String>();
		for (int i = 0; i < fieldNames.size(); ++i) {
			placeHolders.add("?");
		}

		String schemaName = Objects.requireNonNull(this.schemaName, "schema名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table名称为空，不合法!");
		String sqlInsert = String.format("INSERT INTO [%s].[%s] ( [%s] ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "],["), StringUtils.join(placeHolders, ","));

		int[] argTypes = new int[fieldNames.size()];
		for (int i = 0; i < fieldNames.size(); ++i) {
			String col = fieldNames.get(i);
			argTypes[i] = this.columnType.get(col);
		}

		DefaultTransactionDefinition defination = new DefaultTransactionDefinition();
		defination.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		defination.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(
				this.jdbcTemplate.getDataSource());
		TransactionStatus status = transactionManager.getTransaction(defination);

		try {
			int[] affects = jdbcTemplate.batchUpdate(sqlInsert, recordValues, argTypes);
			int affect_count = 0;
			for (int i : affects) {
				affect_count += i;
			}

			recordValues.clear();
			transactionManager.commit(status);
			if (log.isDebugEnabled()) {
				log.debug("SQL Server insert data affect count: {}", affect_count);
			}
			return affect_count;
		} catch (TransactionException e) {
			transactionManager.rollback(status);
			throw e;
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}

	}

}
