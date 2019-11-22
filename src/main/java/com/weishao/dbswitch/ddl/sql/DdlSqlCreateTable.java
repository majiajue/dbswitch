package com.weishao.dbswitch.ddl.sql;

import java.util.ArrayList;
import java.util.List;
import com.weishao.dbswitch.constant.Const;
import com.weishao.dbswitch.ddl.DatabaseDialect;
import com.weishao.dbswitch.ddl.SqlDdlOperator;
import com.weishao.dbswitch.ddl.pojo.ColumnDefinition;
import com.weishao.dbswitch.ddl.pojo.TableDefinition;

public class DdlSqlCreateTable extends SqlDdlOperator {

	private TableDefinition table;

	public DdlSqlCreateTable(TableDefinition t) {
		super(" CREATE TABLE ");
		this.table = t;
	}

	@Override
	public String toSqlString(DatabaseDialect dialect) {
		StringBuilder sb=new StringBuilder();
		sb.append(this.getName());
		String fullTableName=dialect.getSchemaTableName(table.getSchemaName(), table.getTableName());
		sb.append(fullTableName);
		sb.append(" (");
		sb.append(Const.CR);
		
		List<ColumnDefinition> columns=table.getColumns();
		List<String> pks=new ArrayList<>();
		for(int i=0;i<columns.size();++i) {
			ColumnDefinition c=columns.get(i);
			if(c.isPrimaryKey()) {
				pks.add(c.getColumnName());
			}
			
			if (i > 0) {
				sb.append(",");
			} else {
				sb.append("  ");
			}
			
			String definition=dialect.getFieldDefination(c);
			sb.append(definition);
			sb.append(Const.CR);
		}
		
		if (pks.size() > 0) {
			String pk = dialect.getPrimaryKeyAsString(pks);
			sb.append(", PRIMARY KEY (").append(pk).append(")").append(Const.CR);
		}

		sb.append(" )").append(Const.CR);
		return sb.toString();
	}
	
}
