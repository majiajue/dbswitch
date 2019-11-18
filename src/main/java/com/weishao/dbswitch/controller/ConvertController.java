package com.weishao.dbswitch.controller;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.weishao.dbswitch.constant.DatabaseType;
import com.weishao.dbswitch.service.ISqlConvertService;

@Api(tags = { "SQL语句格式转换接口" })
@RestController
@RequestMapping("/sql")
public class ConvertController extends BaseController {

	@Autowired
	@Qualifier("CalciteConvertService")
	ISqlConvertService convertService;
	
	private DatabaseType getDatabaseTypeByName(String name) {
		if(name.equalsIgnoreCase(DatabaseType.MYSQL.name())) {
			return DatabaseType.MYSQL;
		}else if (name.equalsIgnoreCase(DatabaseType.ORACLE.name())) {
			return DatabaseType.ORACLE;
		}else if (name.equalsIgnoreCase(DatabaseType.SQLSERVER.name())) {
			return DatabaseType.SQLSERVER;
	}else if (name.equalsIgnoreCase(DatabaseType.POSTGRESQL.name())) {
			return DatabaseType.POSTGRESQL;
		}else {
			throw new RuntimeException(String.format("Unkown database name (%s)", name));
		}
	}

	@RequestMapping(value = "/standard/dml", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "标准DML类SQL语句转换", notes = "将标准DML类型的SQL语句分别转换为三种数据库对应语法的SQL格式，请求的示例包体格式为：\n "
			+ "{\r\n" + 
			"    \"sql\":\"select * from test_table\"\r\n" + 
			"} ")
	/*
	 * 参数的JSON格式： 
		{
		    "sql":"select * from test_table"
		}
	 */
	public Map<String, Object> StandardDataManipulationLanguage(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String sql = object.getString("sql");
		if (Strings.isNullOrEmpty(sql)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("sql", convertService.dmlSentence(sql));
		return success(ret);
	}

	@RequestMapping(value = "/standard/ddl", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "标准DDL类SQL语句转换", notes = "将标准DDL类型的SQL语句分别转换为三种数据库对应语法的SQL格式，请求的示例包体格式为：\n"
			+ "{\r\n" + 
			"    \"sql\":\"create table test_table (i int not null, j varchar(5) null)\"\r\n" + 
			"}")
	/*
	 * 参数的JSON格式：
		{
		    "sql":" create table test_table (i int not null, j varchar(5) null)"
		}
	 */
	public Map<String, Object> StandardDataDefinitionLanguage(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String sql = object.getString("sql");
		if (Strings.isNullOrEmpty(sql)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("sql", convertService.ddlSentence(sql));
		return success(ret);
	}


	@RequestMapping(value = "/special/dml", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "指定数据库的DML类SQL语句转换", notes = "将标准DML类型的SQL语句分别转换为三种数据库对应语法的SQL格式，请求的示例包体格式为：\n "
			+ "{\r\n" + 
			"    \"source\":\"mysql\",\r\n" + 
			"    \"target\":\"oracle\",\r\n" + 
			"    \"sql\":\"select * from `test_table`\"\r\n" + 
			"} ")
	/*
	 * 参数的JSON格式：
		{
		    "source":"mysql",
		    "target":"oracle",
		    "sql":"select * from `test_table`"
		}
	 */
	public Map<String, Object> SpecialDataManipulationLanguage(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String source = object.getString("source");
		String target = object.getString("target");
		String sql = object.getString("sql");
		if (Strings.isNullOrEmpty(sql) || Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(target)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("sql", convertService.dmlSentence(getDatabaseTypeByName(source),getDatabaseTypeByName(target),sql));
		return success(ret);
	}

	@RequestMapping(value = "/special/ddl", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value = "指定数据库的DDL类SQL语句转换", notes = "将标准DDL类型的SQL语句分别转换为三种数据库对应语法的SQL格式，请求的示例包体格式为：\n  "
			+ "{\r\n" + 
			"    \"source\":\"mysql\",\r\n" + 
			"    \"target\":\"oracle\",\r\n" + 
			"    \"sql\":\"create table `test_table` (`i` int not null, `j` varchar(5) null)\"\r\n" + 
			"} ")
	/*
	 * 参数的JSON格式：
		{
		    "source":"mysql",
		    "target":"oracle",
		    "sql":" create table `test_table` (`i` int not null, `j` varchar(5) null)"
		}
	 */
	public Map<String, Object> SpecialDataDefinitionLanguage(@RequestBody String body) {
		JSONObject object = JSON.parseObject(body);
		String source = object.getString("source");
		String target = object.getString("target");
		String sql = object.getString("sql");
		if (Strings.isNullOrEmpty(sql) || Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(target)) {
			throw new RuntimeException("Invalid input parameter");
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("sql", convertService.ddlSentence(getDatabaseTypeByName(source),getDatabaseTypeByName(target),sql));
		return success(ret);
	}

}
