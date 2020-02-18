package com.weishao.dbswitch.dbwriter.gpdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.weishao.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;

/**
 * Greenplum数据库Insert写入实现类
 * 
 * @author tang
 *
 */
public class GreenplumInsertWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(GreenplumInsertWriterImpl.class);
	
	public GreenplumInsertWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		List<String> placeHolders = new ArrayList<String>();
		for (int i = 0; i < fieldNames.size(); ++i) {
			placeHolders.add("?");
		}

		String schemaName = Objects.requireNonNull(this.schemaName, "schema名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table名称为空，不合法!");
		String sqlInsert = String.format("INSERT INTO \"%s\".\"%s\" ( \"%s\" ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "\",\""), StringUtils.join(placeHolders, ","));
		
		int[] argTypes=new int[fieldNames.size()];
		for(int i=0;i<fieldNames.size();++i) {
			String col=fieldNames.get(i);
			argTypes[i]=this.columnType.get(col);
		}

		DefaultTransactionDefinition defination = new DefaultTransactionDefinition();
		defination.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		defination.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(this.jdbcTemplate.getDataSource());
		TransactionStatus status = transactionManager.getTransaction(defination);

		try {
			int[] affects=jdbcTemplate.batchUpdate(sqlInsert, recordValues,argTypes);
			int affect_count = 0;
			for (int i : affects) {
				affect_count += i;
			}

			recordValues.clear();
			transactionManager.commit(status);
			if (logger.isDebugEnabled()) {
				logger.debug("Greenplum insert data  affect count:{}", affect_count);
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
