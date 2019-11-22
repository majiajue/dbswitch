package com.weishao.dbswitch.ddl;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.weishao.dbswitch.ddl.pojo.ColumnDefinition;

public abstract class DatabaseDialect {

	public String getSchemaTableName(String schemaName, String tableName) {
		return String.format("\"%s\".\"%s\"", schemaName.trim(),tableName.trim());
	}
	
	public String getQuoteFieldName(String fieldName) {
		return String.format("\"%s\"", fieldName.trim());
	}
	
	public abstract String getFieldTypeName(ColumnDefinition column);
	
	public abstract String getFieldDefination(ColumnDefinition column);
	
	public  String getPrimaryKeyAsString(List<String> pks) {
		if(pks.size()>0) {
			StringBuilder sb = new StringBuilder();
			sb.append("\"");
			sb.append(StringUtils.join(pks, "\" , \""));
			sb.append("\"");
			return sb.toString();
		}
		
		return "";
	}
	
}
