package com.weishao.dbswitch.core.util;

import java.util.List;
import java.util.stream.Collectors;

import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.constant.Const;
import com.weishao.dbswitch.core.database.AbstractDatabase;
import com.weishao.dbswitch.core.database.DatabaseFactory;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.ColumnMetaData;

public class GenerateSqlUtils {

	public static String getDDLCreateTableSQL(DatabaseType type, List<ColumnDescription> fieldNames,
			List<String> primaryKeys, String schemaName, String tableName, boolean ifNotExist) {
		StringBuilder retval = new StringBuilder();
		List<String> pks = fieldNames.stream().filter((cd) -> primaryKeys.contains(cd.getFieldName()))
				.map((cd) -> cd.getFieldName()).collect(Collectors.toList());

		AbstractDatabase db = DatabaseFactory.getDatabaseInstance(type);

		retval.append(Const.CREATE_TABLE);
		ifNotExist = false;
		// if(ifNotExist && type!=DatabaseType.ORACLE) {
		// retval.append( Const.IF_NOT_EXISTS );
		// }
		retval.append(db.getQuotedSchemaTableCombination(schemaName, tableName) + Const.CR);
		retval.append("(").append(Const.CR);

		for (int i = 0; i < fieldNames.size(); i++) {
			if (i > 0) {
				retval.append(", ");
			} else {
				retval.append("  ");
			}

			ColumnMetaData v = fieldNames.get(i).getMetaData();
			retval.append(db.getFieldDefinition(v, pks, false, true));
		}

		if (pks.size() > 0) {
			String pk = db.getPrimaryKeyAsString(pks);
			retval.append(", PRIMARY KEY (").append(pk).append(")").append(Const.CR);
		}

		retval.append(")").append(Const.CR);
		return db.formatSQL(retval.toString());
	}
}
