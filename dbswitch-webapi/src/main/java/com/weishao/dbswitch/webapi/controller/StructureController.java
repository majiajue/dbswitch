package com.weishao.dbswitch.webapi.controller;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.weishao.dbswitch.common.constant.DatabaseType;
import com.weishao.dbswitch.core.model.ColumnDescription;
import com.weishao.dbswitch.core.model.DatabaseDescription;
import com.weishao.dbswitch.webapi.model.ResponseResult;
import com.weishao.dbswitch.core.model.TableDescription;
import com.weishao.dbswitch.core.service.IMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = { "表结构抽取转换接口" })
@RestController
@RequestMapping("/database")
public class StructureController {

	@Autowired
	@Qualifier("MigrationService")
	private IMigrationService migrationService;
	
	@RequestMapping(value = "/models_list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "查询所有的模式(model/schema)", notes = "获取数据库中所有的模式(model/schema),，请求的示例包体格式为：\n"
			+ "{\r\n" + 
			"\"type\":\"oracle\",\r\n" + 
			"\"host\":\"172.17.207.252\",\r\n" + 
			"\"port\":1521,\r\n" + 
			"\"mode\":\"sid\",\r\n" + 
			"\"user\":\"yi_bo\",\r\n" + 
			"\"passwd\":\"tangyibo\",\r\n" + 
			"\"dbname\":\"orcl\",\r\n" + 
			"\"charset\":\"utf-8\"\r\n" + 
			"}")
	/*
	 * 参数的JSON格式：
		{
		"type":"oracle",
		"host":"172.17.207.252",
		"port":1521,
		"mode":"sid",
		"user":"yi_bo",
		"passwd":"tangyibo",
		"dbname":"orcl",
		"charset":"utf-8"
		}
	 */
	public ResponseResult queryDatabaseSchemas(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String type=object.getString("type");
		String host=object.getString("host");
		Integer port=object.getInteger("port");
		String mode=object.getString("mode");
		String user=object.getString("user");
		String passwd=object.getString("passwd");
		String dbname=object.getString("dbname");
		String charset=object.getString("charset");
		
		if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(user)
				|| Strings.isNullOrEmpty(passwd) || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
				|| Objects.isNull(port)) {
			throw new RuntimeException("Invalid input parameter");
		}

		DatabaseDescription databaseDesc = new DatabaseDescription(type, host, port, mode, dbname, charset, user,passwd);
		migrationService.setDatabaseConnection(databaseDesc);
		return ResponseResult.success(migrationService.querySchemaList());
	}
	
	@RequestMapping(value = "/tables_list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "查询指定模式下的所有表(table/view)", notes = "获取数据库中所有的表(物理表及视图)，请求的示例包体格式为：\n"
			+ "{\r\n" + 
			"\"type\":\"oracle\",\r\n" + 
			"\"host\":\"172.17.207.158\",\r\n" + 
			"\"port\":1521,\r\n" + 
			"\"mode\":\"sid\",\r\n" + 
			"\"user\":\"hqtest\",\r\n" + 
			"\"passwd\":\"123456\",\r\n" + 
			"\"dbname\":\"orcl\",\r\n" + 
			"\"model\":\"ODI\",\r\n" + 
			"\"charset\":\"utf-8\"\r\n" + 
			"}")
	/*
	 * 参数的JSON格式：
		{
			"type":"oracle",
			"host":"172.17.207.158",
			"port":1521,
			"mode":"sid",
			"user":"hqtest",
			"passwd":"123456",
			"dbname":"orcl",
			"model":"ODI",
			"charset":"utf-8"
		}
	 */
	public ResponseResult queryDatabaseTableList(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String type=object.getString("type");
		String host=object.getString("host");
		Integer port=object.getInteger("port");
		String mode=object.getString("mode");
		String user=object.getString("user");
		String passwd=object.getString("passwd");
		String dbname=object.getString("dbname");
		String charset=object.getString("charset");
		String model=object.getString("model");
		
		if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(user)
				|| Strings.isNullOrEmpty(passwd) || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
				|| Strings.isNullOrEmpty(model) || Objects.isNull(port)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		DatabaseDescription databaseDesc=new DatabaseDescription(type, host, port, mode, dbname, charset, user, passwd);
		migrationService.setDatabaseConnection(databaseDesc);
		List<TableDescription> tables=migrationService.queryTableList(model);
		List<Map<String,String>> ret=new ArrayList<Map<String,String>>();
		for(TableDescription td : tables) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("table_name", td.getTableName());
			item.put("table_type", td.getTableType());
			item.put("remarks", td.getRemarks());
			ret.add(item);
		}
		
		return ResponseResult.success(ret);
	}
	
	@RequestMapping(value = "/table_info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "查询指定表的元信息", notes = "查询指定表的详细元信息，请求的示例包体格式为：\n"
			+ "		{\r\n" + 
			"		    \"type\":\"oracle\",  \r\n" + 
			"		    \"host\":\"172.17.207.252\",\r\n" + 
			"		    \"port\":1521,\r\n" + 
			"          \"mode\":\"sid\",\r\n" + 
			"		    \"user\":\"yi_bo\",\r\n" + 
			"		    \"passwd\":\"tangyibo\",\r\n" + 
			"		    \"dbname\":\"orcl\",\r\n" + 
			"		    \"model\":\"YI_BO\",\r\n" + 
			"		    \"charset\":\"utf-8\",\r\n" + 
			"		    \"src_table\":\"C_SEX\"\r\n" + 
			"		}")
	/*
	 * 参数的JSON格式：
		{
		    "type":"oracle",  
		    "host":"172.17.207.252",
		    "port":1521,
			"mode":"sid",
		    "user":"yi_bo",
		    "passwd":"yi_bo",
		    "dbname":"orcl",
		    "model":"YI_BO",
		    "charset":"utf-8",
		    "src_table":"C_SEX",
		}
	 */
	public ResponseResult queryDatabaseTableInfo(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String type = object.getString("type");
		String host = object.getString("host");
		Integer port = object.getInteger("port");
		String mode = object.getString("mode");
		String user = object.getString("user");
		String passwd = object.getString("passwd");
		String dbname = object.getString("dbname");
		String charset = object.getString("charset");
		String model = object.getString("model");
		String src_table = object.getString("src_table");

		if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(user)
				|| Strings.isNullOrEmpty(passwd) || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
				|| Strings.isNullOrEmpty(model) || Strings.isNullOrEmpty(src_table) || Objects.isNull(port)) {
			throw new RuntimeException("Invalid input parameter");
		}

		Map<String, Object> ret = new HashMap<String, Object>();

		DatabaseDescription databaseDesc = new DatabaseDescription(type, host, port, mode, dbname, charset, user, passwd);
		migrationService.setDatabaseConnection(databaseDesc);
		List<ColumnDescription> columnDescs = migrationService.queryTableColumnMeta(model, src_table);
		List<String> primaryKeys = migrationService.queryTablePrimaryKeys(model, src_table);
		List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
		for (ColumnDescription col : columnDescs) {
			Map<String, Object> one = new HashMap<String, Object>();
			one.put("name", col.getFieldName());
			one.put("type", col.getFieldTypeName());
			one.put("class_type", col.getFiledTypeClassName());
			one.put("display_size", col.getDisplaySize());
			one.put("precision", col.getPrecisionSize());
			one.put("scale", col.getScaleSize());
			one.put("nullable", col.isNullable());
			one.put("remarks", col.getRemarks());
			one.put("auto_increment", col.isAutoIncrement());

			columns.add(one);
		}

		ret.put("primary_key", primaryKeys);
		ret.put("columns", columns);

		List<TableDescription> tables = migrationService.queryTableList(model);
		for (TableDescription td : tables) {
			if (src_table.equals(td.getTableName())) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("table_name", td.getTableName());
				item.put("table_type", td.getTableType());
				item.put("remarks", td.getRemarks());
				ret.put("metadata", item);
			}
		}

		return ResponseResult.success(ret);
	}

	@RequestMapping(value = "/sql_info", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "查询指定SQL的元信息", notes = "查询指定SQL的详细元信息，请求的示例包体格式为：\n"
			+ "		{\r\n" + 
			"		    \"type\":\"oracle\",  \r\n" + 
			"		    \"host\":\"172.17.207.252\",\r\n" + 
			"		    \"port\":1521,\r\n" + 
			"          \"mode\":\"sid\",\r\n" + 
			"		    \"user\":\"yi_bo\",\r\n" + 
			"		    \"passwd\":\"tangyibo\",\r\n" + 
			"		    \"dbname\":\"orcl\",\r\n" + 
			"		    \"charset\":\"utf-8\",\r\n" + 
			"		    \"querysql\":\"SELECT * FROM YI_BO.C_SEX\"\r\n" + 
			"		}")
	/*
	 * 参数的JSON格式：
		{
		    "type":"oracle",  
		    "host":"172.17.207.252",
		    "port":1521,
			"mode":"sid",
		    "user":"yi_bo",
		    "passwd":"yi_bo",
		    "dbname":"orcl",
		    "charset":"utf-8",
		    "querysql":"SELECT * FROM YI_BO.C_SEX",
		}
	 */
	public ResponseResult queryDatabaseSqlInfo(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String type=object.getString("type");
		String host=object.getString("host");
		Integer port=object.getInteger("port");
		String user=object.getString("user");
		String mode=object.getString("mode");
		String passwd=object.getString("passwd");
		String dbname=object.getString("dbname");
		String charset=object.getString("charset");
		String querysql=object.getString("querysql");
		
		if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(user)
				|| Strings.isNullOrEmpty(passwd) || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
				||  Strings.isNullOrEmpty(querysql) || Objects.isNull(port)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		DatabaseDescription databaseDesc=new DatabaseDescription(type, host, port, mode, dbname, charset, user, passwd);
		migrationService.setDatabaseConnection(databaseDesc);
		List<ColumnDescription> columnDescs=migrationService.querySqlColumnMeta(querysql);
		List<Map<String,Object>> columns=new ArrayList<Map<String,Object>>();
		for(ColumnDescription col : columnDescs) {
			Map<String,Object> one=new HashMap<String,Object>();
			one.put("name", col.getFieldName());
			one.put("type", col.getFieldTypeName());
			one.put("class_type", col.getFiledTypeClassName());
			one.put("display_size", col.getDisplaySize());
			one.put("precision", col.getPrecisionSize());
			one.put("scale", col.getScaleSize());
			one.put("nullable", col.isNullable());
			one.put("remarks", col.getRemarks());
			one.put("auto_increment", col.isAutoIncrement());
			
			columns.add(one);
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("columns", columns);
		return ResponseResult.success(ret);
	}
	
	@RequestMapping(value = "/table_sql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "查询指定表结构转换的建表SQL语句", notes = "查询指定表结构转换的建表SQL语句，请求的示例包体格式为：\n"
			+ "		{\r\n" + 
			"		    \"type\":\"oracle\",  \r\n" + 
			"		    \"host\":\"172.17.207.252\",\r\n" + 
			"		    \"port\":1521,\r\n" + 
			"          \"mode\":\"sid\",\r\n" + 
			"		    \"user\":\"yi_bo\",\r\n" + 
			"		    \"passwd\":\"tangyibo\",\r\n" + 
			"		    \"dbname\":\"orcl\",\r\n" + 
			"		    \"charset\":\"utf-8\",\r\n" + 
			"		    \"src_model\":\"YI_BO\",\r\n" + 
			"		    \"src_table\":\"C_SEX\",\r\n" + 
			"		    \"target\":\"mysql\",  \r\n" + 
			"		    \"dest_model\":\"TANG\",\r\n" + 
			"		    \"dest_table\":\"my_test_table\"\r\n" + 
			"		}")
	/*
	 * 参数的JSON格式：
		{
		    "type":"oracle",  
		    "host":"172.17.207.252",
		    "port":1521,
			"mode":"sid",
		    "user":"yi_bo",
		    "passwd":"yi_bo",
		    "dbname":"orcl",
		    "charset":"utf-8",
		    "src_model":"YI_BO",
		    "src_table":"C_SEX",
		    "target":"mysql",
		    "dest_model":"TANG",
		    "dest_table":"my_test_table"
		}
	 */
	public ResponseResult queryDatabaseTableSQL(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String type=object.getString("type");
		String host=object.getString("host");
		Integer port=object.getInteger("port");
		String user=object.getString("user");
		String mode=object.getString("mode");
		String passwd=object.getString("passwd");
		String dbname=object.getString("dbname");
		String charset=object.getString("charset");
		String src_model=object.getString("src_model");
		String src_table=object.getString("src_table");
		String target=object.getString("target");
		String dest_model=object.getString("dest_model");
		String dest_table=object.getString("dest_table");
		
		if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(user)
				|| Strings.isNullOrEmpty(passwd) || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
				|| Strings.isNullOrEmpty(src_model) || Strings.isNullOrEmpty(src_table) || Objects.isNull(port)
				|| Strings.isNullOrEmpty(dest_model) || Strings.isNullOrEmpty(dest_table) ||Strings.isNullOrEmpty(target)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		DatabaseDescription databaseDesc=new DatabaseDescription(type, host, port, mode, dbname, charset, user, passwd);
		DatabaseType taregetDabaseType=DatabaseType.valueOf(target.toUpperCase());
		migrationService.setDatabaseConnection(databaseDesc);
		
		List<ColumnDescription> columnDescs=migrationService.queryTableColumnMeta(src_model, src_table);
		List<String> primaryKeys=migrationService.queryTablePrimaryKeys(src_model, src_table);
		String sql=migrationService.getDDLCreateTableSQL(taregetDabaseType, columnDescs, primaryKeys, dest_model, dest_table, false);
		
		Map<String, Object> ret = new HashMap<String, Object>();
		
		List<Map<String, Object>> columns = new ArrayList<Map<String, Object>>();
		for (ColumnDescription col : columnDescs) {
			Map<String, Object> one = new HashMap<String, Object>();
			one.put("name", col.getFieldName());
			one.put("type", col.getFieldTypeName());
			one.put("class_type", col.getFiledTypeClassName());
			one.put("display_size", col.getDisplaySize());
			one.put("precision", col.getPrecisionSize());
			one.put("scale", col.getScaleSize());
			one.put("nullable", col.isNullable());
			one.put("remarks", col.getRemarks());

			columns.add(one);
		}

		ret.put("create_sql", sql);
		ret.put("primary_key", primaryKeys);
		ret.put("columns", columns);

		List<TableDescription> tables = migrationService.queryTableList(src_model);
		for (TableDescription td : tables) {
			if (src_table.equals(td.getTableName())) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("table_name", td.getTableName());
				item.put("table_type", td.getTableType());
				item.put("remarks", td.getRemarks());
				ret.put("metadata", item);
			}
		}

		return ResponseResult.success(ret);
	}
	
	@RequestMapping(value = "/sql_test", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "测试指定数据库中sql有效性", notes = "测试指定数据库中sql有效性，请求的示例包体格式为：\n"
			+ " {\r\n" + 
			"    \"type\":\"oracle\",\r\n" + 
			"    \"host\":\"172.17.207.252\",\r\n" + 
			"    \"port\":1521,\r\n" + 
			"    \"mode\":\"sid\",\r\n" + 
			"    \"user\":\"yi_bo\",\r\n" + 
			"    \"passwd\":\"tangyibo\",\r\n" + 
			"    \"dbname\":\"orcl\",\r\n" + 
			"    \"querysql\":\"select * from CJB\",\r\n" + 
			"    \"charset\":\"utf-8\"\r\n" + 
			"}\r\n" + 
			"")
	/*
	 * 参数的JSON格式：
		 {
		    "type":"oracle",
		    "host":"172.17.207.252",
		    "port":1521,
			"mode":"sid",
		    "user":"yi_bo",
		    "passwd":"yi_bo",
		    "dbname":"orcl",
		    "querysql":"select * from CJB",
		    "charset":"utf-8"
		}
	 */
	public ResponseResult testQuerySql(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String type=object.getString("type");
		String host=object.getString("host");
		Integer port=object.getInteger("port");
		String mode=object.getString("mode");
		String user=object.getString("user");
		String passwd=object.getString("passwd");
		String dbname=object.getString("dbname");
		String charset=object.getString("charset");
		String querysql=object.getString("querysql");
		
		if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(host) || Strings.isNullOrEmpty(user)
				|| Strings.isNullOrEmpty(passwd) || Strings.isNullOrEmpty(dbname) || Strings.isNullOrEmpty(charset)
				|| Strings.isNullOrEmpty(querysql) || Objects.isNull(port)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		DatabaseDescription databaseDesc=new DatabaseDescription(type, host, port, mode, dbname, charset, user, passwd);
		migrationService.setDatabaseConnection(databaseDesc);
		migrationService.testQuerySQL(querysql);
		return ResponseResult.success("ok");
	}
	
}
