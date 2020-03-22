package com.weishao.dbswitch.dbwriter.oracle;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.weishao.dbswitch.dbwriter.AbstractDatabaseWriter;
import com.weishao.dbswitch.dbwriter.IDatabaseWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Oracle数据库写入实现类
 * 
 * @author tang
 *
 */
@Slf4j
public class OracleWriterImpl extends AbstractDatabaseWriter implements IDatabaseWriter {

	public OracleWriterImpl(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public long write(List<String> fieldNames, List<Object[]> recordValues) {
		List<String> placeHolders = new ArrayList<String>();
		for (int i = 0; i < fieldNames.size(); ++i) {
			placeHolders.add("?");
		}

		String schemaName = Objects.requireNonNull(this.schemaName, "schema-name名称为空，不合法!");
		String tableName = Objects.requireNonNull(this.tableName, "table-name名称为空，不合法!");
		String sqlInsert = String.format("INSERT INTO \"%s\".\"%s\" ( \"%s\" ) VALUES ( %s )", schemaName, tableName,
				StringUtils.join(fieldNames, "\",\""), StringUtils.join(placeHolders, ","));

		int[] argTypes = new int[fieldNames.size()];
		for (int i = 0; i < fieldNames.size(); ++i) {
			String col = fieldNames.get(i);
			argTypes[i] = this.columnType.get(col);
		}

		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(this.dataSource);
		TransactionStatus status = transactionManager.getTransaction(definition);

		try {
			int affect_count = 0;
			jdbcTemplate.batchUpdate(sqlInsert, recordValues, argTypes);
			affect_count = recordValues.size();
			recordValues.clear();
			transactionManager.commit(status);

			if (log.isDebugEnabled()) {
				log.debug("Oracle insert write data  affect count:{}", affect_count);
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
