package com.weishao.dbswitch.sql.ddl.sql;

import java.util.Objects;
import com.weishao.dbswitch.sql.ddl.DatabaseDialect;
import com.weishao.dbswitch.sql.ddl.SqlDdlOperator;
import com.weishao.dbswitch.sql.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.sql.ddl.pojo.TableDefinition;
import com.weishao.dbswitch.sql.ddl.sql.impl.GreenplumDialectImpl;
import com.weishao.dbswitch.sql.ddl.sql.impl.PostgresDialectImpl;

public class DdlSqlAlterTable extends SqlDdlOperator {

	protected enum AlterType {
		ADD(1), DROP(2), MODIFY(3),RENAME(4);

		private int index;

		AlterType(int idx) {
			this.index = idx;
		}

		public int getIndex() {
			return index;
		}
	}

	private TableDefinition table;
	private AlterType alterType;

	public DdlSqlAlterTable(TableDefinition t, String handle) {
		super("ALTER TABLE ");
		this.table = t;
		alterType = AlterType.valueOf(handle.toUpperCase());
	}

	@Override
	public String toSqlString(DatabaseDialect dialect) {
		String fullTableName = dialect.getSchemaTableName(table.getSchemaName(), table.getTableName());

		StringBuilder sb = new StringBuilder();
		sb.append(this.getName());
		sb.append(fullTableName);

		if (table.getColumns().size() < 1) {
			throw new RuntimeException("Alter table need one column at least!");
		}

		if (AlterType.ADD == alterType) {
			if (dialect instanceof PostgresDialectImpl || dialect instanceof GreenplumDialectImpl) {
				//PostgreSQL/Greenplum数据库的add只支持一列，不支持多列
				if (table.getColumns().size() != 1) {
					throw new RuntimeException("Alter table for PostgreSQL/Greenplum only can add one column!");
				}
				
				sb.append(" ADD ");
				ColumnDefinition cd = table.getColumns().get(0);
				sb.append(dialect.getFieldDefination(cd));
			} else {
				sb.append(" ADD (");
				for (int i = 0; i < table.getColumns().size(); ++i) {
					ColumnDefinition cd = table.getColumns().get(i);
					sb.append((i > 0) ? "," : " ");
					sb.append(dialect.getFieldDefination(cd));
				}
				sb.append(")");
			}
		} else if (AlterType.DROP == alterType) {
			if (table.getColumns().size() != 1) {
				throw new RuntimeException("Alter table only can drop one column!");
			}

			ColumnDefinition cd = table.getColumns().get(0);
			sb.append(" DROP ");
			sb.append(dialect.getQuoteFieldName(cd.getColumnName()));
		} else if (AlterType.MODIFY == alterType) {
			if (table.getColumns().size() != 1) {
				throw new RuntimeException("Alter table only can modify one column!");
			}

			ColumnDefinition cd = table.getColumns().get(0);
			if(dialect instanceof PostgresDialectImpl || dialect instanceof GreenplumDialectImpl ) {
				//PostgreSQL/Greenplum数据库的modify需要单独拆分
				String typename = dialect.getFieldTypeName(cd);
				boolean nullable = cd.isNullable();
				String defaultValue = cd.getDefaultValue();
				sb.append(" ALTER COLUMN " + dialect.getQuoteFieldName(cd.getColumnName()) + " TYPE " + typename);
				if (nullable) {
					sb.append(",ALTER COLUMN " + dialect.getQuoteFieldName(cd.getColumnName()) + " SET DEFAULT NULL");
				} else if (Objects.nonNull(defaultValue) && !defaultValue.isEmpty() && !defaultValue.toUpperCase().equals("NULL")) {
					sb.append(",ALTER COLUMN " + dialect.getQuoteFieldName(cd.getColumnName()) + " SET DEFAULT '"	+ defaultValue + "'");
				} else {
					sb.append(",ALTER COLUMN " + dialect.getQuoteFieldName(cd.getColumnName()) + " SET NOT NULL");
				}
			} else {
				sb.append(" MODIFY  ");
				sb.append(dialect.getFieldDefination(cd));
			}
		} else {
			// 当前不支持rename及其他操作
			throw new RuntimeException("Alter table unsupported operation : " + alterType.name());
		}

		return sb.toString();
	}

}
