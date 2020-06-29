package com.weishao.dbswitch.webapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 知识点总结：
 * （1）sql 语句大小写的问题
 *    关键字不区分大小写，例如 select ，from， 大小写均可
 * 	  标识符区分大小写，例如 表名，列名
 * 	  标识符如果不加双引号，默认是按大写执行
 * 	  标识符如果加引号，则是按原始大小写执行
 * （2）支持在JDBC中常用带占位符的预编译读取和写入
 * 
 * 当前存在的问题：
 * （1）[已解决]select的分页查询问题，MySQL不支持offset fetch方式分页
 * （2）[已解决]insert的row问题
 * （3）create table的自增字段问题
 *    各个数据库的实现参考：https://www.w3school.com.cn/sql/sql_autoincrement.asp
 * （4）各个数据库间的字段类型定义差异
 * （5）各个数据库的特殊函数问题：SELECT ProductName, UnitPrice, Now() as PerDate FROM Products
 * 
 * @author tang
 *
 *
 *@RunWith作用:
 *
 *	@RunWith 就是一个运行器
 *	@RunWith(JUnit4.class) 就是指用JUnit4来运行
 *	@RunWith(SpringJUnit4ClassRunner.class),让测试运行于Spring测试环境
 *	@RunWith(Suite.class) 的话就是一套测试集合，
 *	@ContextConfiguration Spring整合JUnit4测试时，使用注解引入多个配置文件
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SwitchApplicationTests{
	private static final Logger logger = LoggerFactory.getLogger(SwitchApplicationTests.class);
	
	@Autowired
    private WebApplicationContext wac;
    
	/**
	 * JSON path的使用测试
	 */
	@Test
	public void test_json_path_usage_01() {
		logger.info("#### Run test json path ");
		String json1 = "{\"author\":\"tangyibo\"}";
		String result = JsonPath.read(json1, "$.author");
		logger.info("#### author name = {}", result);
	}
	
	
	/**
	 * 【单表查询】用例一：SELECT * FROM dbo.Persons
	 * 说明：常用的全表查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_1_all() throws Exception {
		String sql = "SELECT * FROM dbo.Persons";
		logger.info("#### Run test 1 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		//action.andDo(MockMvcResultHandlers.print());
		//String content=action.andReturn().getResponse().getContentAsString();
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `DBO`.`PERSONS`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [DBO].[PERSONS]"));
	}
	
	
	/**
	 * 【单表查询】用例二：SELECT LastName,FirstName FROM dbo.Persons
	 * 说明：常用取部分字段的全表查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_2_with_fields() throws Exception {
		String sql = "SELECT LastName,FirstName FROM dbo.Persons";
		logger.info("#### Run test 2 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"LASTNAME\", \"FIRSTNAME\" FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"LASTNAME\", \"FIRSTNAME\" FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `LASTNAME`, `FIRSTNAME` FROM `DBO`.`PERSONS`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [LASTNAME], [FIRSTNAME] FROM [DBO].[PERSONS]"));
	}
	
	
	/**
	 * 【单表查询】用例三：SELECT * FROM dbo.Persons where LastName='tang'
	 * 说明：常用带条件的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_3_all_with_where() throws Exception {
		String sql = "SELECT * FROM dbo.Persons where LastName='tang'";
		logger.info("#### Run test 3 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"DBO\".\"PERSONS\" WHERE \"LASTNAME\" = 'tang'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"DBO\".\"PERSONS\" WHERE \"LASTNAME\" = 'tang'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `DBO`.`PERSONS` WHERE `LASTNAME` = 'tang'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [DBO].[PERSONS] WHERE [LASTNAME] = 'tang'"));
	}

	
	/**
	 * 【单表查询】用例四：SELECT * FROM dbo.Persons where Sex=?
	 * 说明：带占位符的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_4_all_with_where_placeholder() throws Exception {
		String sql = "SELECT * FROM dbo.Persons where Sex=?";
		logger.info("#### Run test 4 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"DBO\".\"PERSONS\" WHERE \"SEX\" = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"DBO\".\"PERSONS\" WHERE \"SEX\" = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `DBO`.`PERSONS` WHERE `SEX` = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [DBO].[PERSONS] WHERE [SEX] = ?"));
	}
	
	
	/**
	 * 【单表查询】用例五：SELECT "LastName", "FirstName" FROM "dbo"."Persons" WHERE "Sex" = '0'
	 * 说明：表名及列名大小写敏感的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_5_all_with_case_sensitive() throws Exception {
		String sql = "SELECT \"LastName\", \"FirstName\" FROM \"dbo\".\"Persons\" WHERE \"Sex\" = '0'";
		logger.info("#### Run test 5 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"LastName\", \"FirstName\" FROM \"dbo\".\"Persons\" WHERE \"Sex\" = '0'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"LastName\", \"FirstName\" FROM \"dbo\".\"Persons\" WHERE \"Sex\" = '0'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `LastName`, `FirstName` FROM `dbo`.`Persons` WHERE `Sex` = '0'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [LastName], [FirstName] FROM [dbo].[Persons] WHERE [Sex] = '0'"));
	}
	
	
	/**
	 * 【单表查询】用例六：SELECT * FROM DBO.PERSONS WHERE SEX LIKE '%f' 
	 * 说明：带like关键词的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_6_all_with_where_keyword_like() throws Exception {
		String sql = "SELECT * FROM DBO.PERSONS WHERE SEX LIKE '%f'";
		logger.info("#### Run test 6 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"DBO\".\"PERSONS\" WHERE \"SEX\" LIKE '%f'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"DBO\".\"PERSONS\" WHERE \"SEX\" LIKE '%f'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `DBO`.`PERSONS` WHERE `SEX` LIKE '%f'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [DBO].[PERSONS] WHERE [SEX] LIKE '%f'"));
	}
	
	
	/**
	 * 【单表查询】用例七：SELECT COUNT(*) FROM DBO.PERSONS 
	 * 说明：带count(*)关键词的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_7_all_use_count() throws Exception {
		String sql = "SELECT COUNT(*) FROM DBO.PERSONS";
		logger.info("#### Run test 7 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT COUNT(*) FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT COUNT(*) FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT COUNT(*) FROM `DBO`.`PERSONS`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT COUNT(*) FROM [DBO].[PERSONS]"));
	}
	

	/**
	 * 【单表查询】用例八：SELECT COUNT(FirstName) FROM DBO.PERSONS 
	 * 说明：带count(FirstName)关键词的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_8_all_use_count() throws Exception {
		String sql = "SELECT COUNT(FirstName) FROM DBO.PERSONS";
		logger.info("#### Run test 8 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT COUNT(\"FIRSTNAME\") FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT COUNT(\"FIRSTNAME\") FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT COUNT(`FIRSTNAME`) FROM `DBO`.`PERSONS`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT COUNT([FIRSTNAME]) FROM [DBO].[PERSONS]"));
	}
	
	
	/**
	 * 【单表查询】用例九：SELECT DISTINCT  FirstName  FROM DBO.PERSONS 
	 * 说明：带DISTINCT关键词的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_9_all_use_distinct() throws Exception {
		String sql = "SELECT DISTINCT  FirstName  FROM DBO.PERSONS";
		logger.info("#### Run test 9 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT DISTINCT \"FIRSTNAME\" FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT DISTINCT \"FIRSTNAME\" FROM \"DBO\".\"PERSONS\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT DISTINCT `FIRSTNAME` FROM `DBO`.`PERSONS`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT DISTINCT [FIRSTNAME] FROM [DBO].[PERSONS]"));
	}
	
	
	/**
	 * 【单表查询】用例十：SELECT * FROM Persons WHERE RQ>1965
	 * 说明：带运算符的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_10_all_use_count() throws Exception {
		String sql = "SELECT * FROM Persons WHERE RQ>1965";
		logger.info("#### Run test 10 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"PERSONS\" WHERE \"RQ\" > 1965"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"PERSONS\" WHERE \"RQ\" > 1965"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `PERSONS` WHERE `RQ` > 1965"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [PERSONS] WHERE [RQ] > 1965"));
	}
	

	/**
	 * 【单表查询】用例十一：SELECT * FROM Persons WHERE FirstName='Thomas' AND LastName='Carter'
	 * 说明：带AND 运算的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_11_where_and() throws Exception {
		String sql = "SELECT * FROM Persons WHERE FirstName='Thomas' AND LastName='Carter'";
		logger.info("#### Run test 11 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"PERSONS\" WHERE \"FIRSTNAME\" = 'Thomas' AND \"LASTNAME\" = 'Carter'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"PERSONS\" WHERE \"FIRSTNAME\" = 'Thomas' AND \"LASTNAME\" = 'Carter'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `PERSONS` WHERE `FIRSTNAME` = 'Thomas' AND `LASTNAME` = 'Carter'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [PERSONS] WHERE [FIRSTNAME] = 'Thomas' AND [LASTNAME] = 'Carter'"));
	}
	

	/**
	 * 【单表查询】用例十二：SELECT * FROM Persons WHERE  (FirstName='Thomas' OR FirstName='William') AND LastName='Carter'
	 * 说明：带AND 和 OR 运算符的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_12_where_and_or() throws Exception {
		String sql = "SELECT * FROM Persons WHERE  (FirstName='Thomas' OR FirstName='William') AND LastName='Carter'";
		logger.info("#### Run test 12 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"PERSONS\" WHERE (\"FIRSTNAME\" = 'Thomas' OR \"FIRSTNAME\" = 'William') AND \"LASTNAME\" = 'Carter'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"PERSONS\" WHERE (\"FIRSTNAME\" = 'Thomas' OR \"FIRSTNAME\" = 'William') AND \"LASTNAME\" = 'Carter'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `PERSONS` WHERE (`FIRSTNAME` = 'Thomas' OR `FIRSTNAME` = 'William') AND `LASTNAME` = 'Carter'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [PERSONS] WHERE ([FIRSTNAME] = 'Thomas' OR [FIRSTNAME] = 'William') AND [LASTNAME] = 'Carter'"));
	}
	

	/**
	 * 【单表查询】用例十三：SELECT Company, OrderNumber FROM Orders ORDER BY Company, OrderNumber
	 * 说明：带ORDER BY 语句的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_13_order_by() throws Exception {
		String sql = "SELECT Company, OrderNumber FROM Orders ORDER BY Company, OrderNumber";
		logger.info("#### Run test 13 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"COMPANY\", \"ORDERNUMBER\" FROM \"ORDERS\" ORDER BY \"COMPANY\", \"ORDERNUMBER\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"COMPANY\", \"ORDERNUMBER\" FROM \"ORDERS\" ORDER BY \"COMPANY\", \"ORDERNUMBER\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `COMPANY`, `ORDERNUMBER` FROM `ORDERS` ORDER BY `COMPANY`, `ORDERNUMBER`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [COMPANY], [ORDERNUMBER] FROM [ORDERS] ORDER BY [COMPANY], [ORDERNUMBER]"));
	}
	

	/**
	 * 【单表查询】用例十四：SELECT * FROM Persons WHERE LastName IN ('Adams','Carter')
	 * 说明：带IN 操作符语句的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_14_use_in() throws Exception {
		String sql = "SELECT * FROM Persons WHERE LastName IN ('Adams','Carter')";
		logger.info("#### Run test 14 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"PERSONS\" WHERE \"LASTNAME\" IN ('Adams', 'Carter')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"PERSONS\" WHERE \"LASTNAME\" IN ('Adams', 'Carter')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `PERSONS` WHERE `LASTNAME` IN ('Adams', 'Carter')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [PERSONS] WHERE [LASTNAME] IN ('Adams', 'Carter')"));
	}
	

	/**
	 * 【多表查询】用例十五：SELECT po.OrderID, p.LastName, p.FirstName FROM Persons AS p, Product_Orders AS po WHERE p.LastName='Adams' AND p.FirstName='John'
	 * 说明：为列名称和表名称指定别名的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_15_alias_name() throws Exception {
		String sql = "SELECT po.OrderID, p.LastName, p.FirstName FROM Persons AS p, Product_Orders AS po WHERE p.LastName='Adams' AND p.FirstName='John'";
		logger.info("#### Run test 15 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"PO\".\"ORDERID\", \"P\".\"LASTNAME\", \"P\".\"FIRSTNAME\" FROM \"PERSONS\" \"P\", \"PRODUCT_ORDERS\" \"PO\" WHERE \"P\".\"LASTNAME\" = 'Adams' AND \"P\".\"FIRSTNAME\" = 'John'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"PO\".\"ORDERID\", \"P\".\"LASTNAME\", \"P\".\"FIRSTNAME\" FROM \"PERSONS\" AS \"P\", \"PRODUCT_ORDERS\" AS \"PO\" WHERE \"P\".\"LASTNAME\" = 'Adams' AND \"P\".\"FIRSTNAME\" = 'John'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `PO`.`ORDERID`, `P`.`LASTNAME`, `P`.`FIRSTNAME` FROM `PERSONS` AS `P`, `PRODUCT_ORDERS` AS `PO` WHERE `P`.`LASTNAME` = 'Adams' AND `P`.`FIRSTNAME` = 'John'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [PO].[ORDERID], [P].[LASTNAME], [P].[FIRSTNAME] FROM [PERSONS] AS [P], [PRODUCT_ORDERS] AS [PO] WHERE [P].[LASTNAME] = 'Adams' AND [P].[FIRSTNAME] = 'John'"));
	}
	

	/**
	 * 【多表查询】用例十六：SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons INNER JOIN Orders ON Persons.Id_P = Orders.Id_P ORDER BY Persons.LastName
	 * 说明：使用关键词INNER JOIN 来从两个表中的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_16_inner_join() throws Exception {
		String sql = "SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons INNER JOIN Orders ON Persons.Id_P = Orders.Id_P ORDER BY Persons.LastName";
		logger.info("#### Run test 16 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" INNER JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" INNER JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `PERSONS`.`LASTNAME`, `PERSONS`.`FIRSTNAME`, `ORDERS`.`ORDERNO` FROM `PERSONS` INNER JOIN `ORDERS` ON `PERSONS`.`ID_P` = `ORDERS`.`ID_P` ORDER BY `PERSONS`.`LASTNAME`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [PERSONS].[LASTNAME], [PERSONS].[FIRSTNAME], [ORDERS].[ORDERNO] FROM [PERSONS] INNER JOIN [ORDERS] ON [PERSONS].[ID_P] = [ORDERS].[ID_P] ORDER BY [PERSONS].[LASTNAME]"));
	}
	

	/**
	 * 【多表查询】用例十七：SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons LEFT JOIN Orders ON Persons.Id_P=Orders.Id_P ORDER BY Persons.LastName
	 * 说明：使用关键词LEFT JOIN 来从两个表中的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_17_left_join() throws Exception {
		String sql = "SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons LEFT JOIN Orders ON Persons.Id_P=Orders.Id_P ORDER BY Persons.LastName";
		logger.info("#### Run test 17 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" LEFT JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" LEFT JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `PERSONS`.`LASTNAME`, `PERSONS`.`FIRSTNAME`, `ORDERS`.`ORDERNO` FROM `PERSONS` LEFT JOIN `ORDERS` ON `PERSONS`.`ID_P` = `ORDERS`.`ID_P` ORDER BY `PERSONS`.`LASTNAME`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [PERSONS].[LASTNAME], [PERSONS].[FIRSTNAME], [ORDERS].[ORDERNO] FROM [PERSONS] LEFT JOIN [ORDERS] ON [PERSONS].[ID_P] = [ORDERS].[ID_P] ORDER BY [PERSONS].[LASTNAME]"));
	}
	

	/**
	 * 【多表查询】用例十八：SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons RIGHT JOIN Orders ON Persons.Id_P=Orders.Id_P ORDER BY Persons.LastName
	 * 说明：使用关键词RIGHT JOIN 来从两个表中的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_18_right_join() throws Exception {
		String sql = "SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons RIGHT JOIN Orders ON Persons.Id_P=Orders.Id_P ORDER BY Persons.LastName";
		logger.info("#### Run test 18 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" RIGHT JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" RIGHT JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `PERSONS`.`LASTNAME`, `PERSONS`.`FIRSTNAME`, `ORDERS`.`ORDERNO` FROM `PERSONS` RIGHT JOIN `ORDERS` ON `PERSONS`.`ID_P` = `ORDERS`.`ID_P` ORDER BY `PERSONS`.`LASTNAME`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [PERSONS].[LASTNAME], [PERSONS].[FIRSTNAME], [ORDERS].[ORDERNO] FROM [PERSONS] RIGHT JOIN [ORDERS] ON [PERSONS].[ID_P] = [ORDERS].[ID_P] ORDER BY [PERSONS].[LASTNAME]"));
	}
	
	
	/**
	 * 【多表查询】用例十九：SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons FULL JOIN Orders ON Persons.Id_P=Orders.Id_P ORDER BY Persons.LastName
	 * 说明：使用关键词FULL JOIN 来从两个表中的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_19_full_join() throws Exception {
		String sql = "SELECT Persons.LastName, Persons.FirstName, Orders.OrderNo FROM Persons FULL JOIN Orders ON Persons.Id_P=Orders.Id_P ORDER BY Persons.LastName";
		logger.info("#### Run test 19 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" FULL JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"PERSONS\".\"LASTNAME\", \"PERSONS\".\"FIRSTNAME\", \"ORDERS\".\"ORDERNO\" FROM \"PERSONS\" FULL JOIN \"ORDERS\" ON \"PERSONS\".\"ID_P\" = \"ORDERS\".\"ID_P\" ORDER BY \"PERSONS\".\"LASTNAME\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `PERSONS`.`LASTNAME`, `PERSONS`.`FIRSTNAME`, `ORDERS`.`ORDERNO` FROM `PERSONS` FULL JOIN `ORDERS` ON `PERSONS`.`ID_P` = `ORDERS`.`ID_P` ORDER BY `PERSONS`.`LASTNAME`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [PERSONS].[LASTNAME], [PERSONS].[FIRSTNAME], [ORDERS].[ORDERNO] FROM [PERSONS] FULL JOIN [ORDERS] ON [PERSONS].[ID_P] = [ORDERS].[ID_P] ORDER BY [PERSONS].[LASTNAME]"));
	}
	
	
	/**
	 * 【多表查询】用例二十：SELECT E_Name FROM Employees_China UNION ALL SELECT E_Name FROM Employees_USA
	 * 说明：使用关键词UNION ALL 来从两个表中的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_20_union_all() throws Exception {
		String sql = "SELECT E_Name FROM Employees_China UNION ALL SELECT E_Name FROM Employees_USA";
		logger.info("#### Run test 20 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"E_NAME\" FROM \"EMPLOYEES_CHINA\" UNION ALL SELECT \"E_NAME\" FROM \"EMPLOYEES_USA\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"E_NAME\" FROM \"EMPLOYEES_CHINA\" UNION ALL SELECT \"E_NAME\" FROM \"EMPLOYEES_USA\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `E_NAME` FROM `EMPLOYEES_CHINA` UNION ALL SELECT `E_NAME` FROM `EMPLOYEES_USA`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [E_NAME] FROM [EMPLOYEES_CHINA] UNION ALL SELECT [E_NAME] FROM [EMPLOYEES_USA]"));
	}
	

	/**
	 * 【多表查询】用例二十一：INSERT INTO private.Persons_backup SELECT LastName,FirstName FROM public.Persons
	 * 说明：使用SELECT INTO拷贝数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_21_select_into() throws Exception {
		String sql = "INSERT INTO private.Persons_backup SELECT LastName,FirstName FROM public.Persons";
		logger.info("#### Run test 21 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("INSERT INTO \"PRIVATE\".\"PERSONS_BACKUP\" (SELECT \"LASTNAME\", \"FIRSTNAME\" FROM \"PUBLIC\".\"PERSONS\")"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("INSERT INTO \"PRIVATE\".\"PERSONS_BACKUP\" (SELECT \"LASTNAME\", \"FIRSTNAME\" FROM \"PUBLIC\".\"PERSONS\")"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("INSERT INTO `PRIVATE`.`PERSONS_BACKUP` (SELECT `LASTNAME`, `FIRSTNAME` FROM `PUBLIC`.`PERSONS`)"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("INSERT INTO [PRIVATE].[PERSONS_BACKUP] (SELECT [LASTNAME], [FIRSTNAME] FROM [PUBLIC].[PERSONS])"));
	}
	
	
	/**
	 * 【单表插入】用例二十二：INSERT INTO Persons VALUES ('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')
	 * 说明：使用INSERT INTO写入数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_insert_1_22() throws Exception {
		String sql = "INSERT INTO Persons VALUES ('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')";
		logger.info("#### Run test 22 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("INSERT INTO \"PERSONS\" VALUES('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("INSERT INTO \"PERSONS\" VALUES('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("INSERT INTO `PERSONS` VALUES('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("INSERT INTO [PERSONS] VALUES('Gates', 'Bill', 'Xuanwumen 10', 'Beijing')"));
	}
	
	
	/**
	 * 
	 * 【单表插入】用例二十三：INSERT INTO Persons (LastName, Address) VALUES ('Wilson', 'Champs-Elysees')
	 * 说明：使用INSERT INTO写入数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_insert_2_23() throws Exception {
		String sql = "INSERT INTO Persons (LastName, Address) VALUES ('Wilson', 'Champs-Elysees')";
		logger.info("#### Run test 23 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("INSERT INTO \"PERSONS\" (\"LASTNAME\", \"ADDRESS\") VALUES('Wilson', 'Champs-Elysees')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("INSERT INTO \"PERSONS\" (\"LASTNAME\", \"ADDRESS\") VALUES('Wilson', 'Champs-Elysees')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("INSERT INTO `PERSONS` (`LASTNAME`, `ADDRESS`) VALUES('Wilson', 'Champs-Elysees')"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("INSERT INTO [PERSONS] ([LASTNAME], [ADDRESS]) VALUES('Wilson', 'Champs-Elysees')"));
	}
	
	
	/**
	 * 【单表插入】用例二十四：INSERT INTO Persons (LastName, Address) VALUES (?, ?)
	 * 说明：使用INSERT INTO预编译写入数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_insert_3_24() throws Exception {
		String sql = "INSERT INTO Persons (LastName, Address) VALUES (?, ?)";
		logger.info("#### Run test 24 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("INSERT INTO \"PERSONS\" (\"LASTNAME\", \"ADDRESS\") VALUES(?, ?)"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("INSERT INTO \"PERSONS\" (\"LASTNAME\", \"ADDRESS\") VALUES(?, ?)"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("INSERT INTO `PERSONS` (`LASTNAME`, `ADDRESS`) VALUES(?, ?)"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("INSERT INTO [PERSONS] ([LASTNAME], [ADDRESS]) VALUES(?, ?)"));
	}
	
	
	/**
	 * 【单表更新】用例二十五：UPDATE Person SET Address = ?, City = ? WHERE LastName = ?
	 * 说明：使用UPDATE SET 预编译更新数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_update_25() throws Exception {
		String sql = "UPDATE Person SET Address = ?, City = ? WHERE LastName = ?";
		logger.info("#### Run test 25 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("UPDATE \"PERSON\" SET \"ADDRESS\" = ? , \"CITY\" = ? WHERE \"LASTNAME\" = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("UPDATE \"PERSON\" SET \"ADDRESS\" = ? , \"CITY\" = ? WHERE \"LASTNAME\" = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("UPDATE `PERSON` SET `ADDRESS` = ? , `CITY` = ? WHERE `LASTNAME` = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("UPDATE [PERSON] SET [ADDRESS] = ? , [CITY] = ? WHERE [LASTNAME] = ?"));
	}
	
	
	/**
	 * 【单表删除】用例二十六：DELETE FROM Person WHERE LastName = 'Wilson' 
	 * 说明：使用UPDATE SET 更新数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_delete_26() throws Exception {
		String sql = "DELETE FROM Person WHERE LastName = 'Wilson'";
		logger.info("#### Run test 26 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("DELETE FROM \"PERSON\" WHERE \"LASTNAME\" = 'Wilson'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("DELETE FROM \"PERSON\" WHERE \"LASTNAME\" = 'Wilson'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("DELETE FROM `PERSON` WHERE `LASTNAME` = 'Wilson'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("DELETE FROM [PERSON] WHERE [LASTNAME] = 'Wilson'"));
	}
	
	
	/**
	 * 【单表删除】用例二十七：DELETE FROM Person WHERE LastName = ? 
	 * 说明：使用DELETE预编译删除数据
	 * @throws Exception
	 */
	@Test
	public void standard_dml_delete_27() throws Exception {
		String sql = "DELETE FROM Person WHERE LastName = ?";
		logger.info("#### Run test 27 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("DELETE FROM \"PERSON\" WHERE \"LASTNAME\" = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("DELETE FROM \"PERSON\" WHERE \"LASTNAME\" = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("DELETE FROM `PERSON` WHERE `LASTNAME` = ?"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("DELETE FROM [PERSON] WHERE [LASTNAME] = ?"));
	}
	
	
	/**
	 * LIMIT的查询分页问题
	 * 【单表查询】用例二十八：select * from Persons limit 2 offset 5
	 * 说明：使用limit分页查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_limit_28() throws Exception {
		String sql = "select * from Persons limit 2 offset 5";
		logger.info("#### Run test 28 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"PERSONS\" OFFSET 5 ROWS FETCH NEXT 2 ROWS ONLY"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"PERSONS\" OFFSET 5 ROWS FETCH NEXT 2 ROWS ONLY"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `PERSONS` LIMIT 2 OFFSET 5"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [PERSONS] OFFSET 5 ROWS FETCH NEXT 2 ROWS ONLY"));
	}
	
	
	/**
	 * 【单表查询】用例二十九：
	 *  问题：首先于字段类型，有许多字段类型无法解析，无法兼容底层数据库
		CREATE TABLE Persons
		(
				P_Id int NOT NULL,
				LastName varchar(255) NOT NULL,
				FirstName varchar(255),
				Address varchar(255),
				City varchar(255),
				PRIMARY KEY (P_Id)
		)
	 * 说明：使用limit分页查询
	 * @throws Exception
	 */
	@Test
	public void standard_ddl_create_table_29() throws Exception {
		String sql = "CREATE TABLE Persons( P_Id int NOT NULL,LastName varchar(255) NOT NULL,FirstName varchar(255),Address varchar(255),City varchar(255),PRIMARY KEY (P_Id))";
		logger.info("#### Run test 29 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/ddl")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("CREATE TABLE \"PERSONS\" (\"P_ID\" INTEGER NOT NULL, \"LASTNAME\" VARCHAR(255) NOT NULL, \"FIRSTNAME\" VARCHAR(255), \"ADDRESS\" VARCHAR(255), \"CITY\" VARCHAR(255), PRIMARY KEY (\"P_ID\"))"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("CREATE TABLE \"PERSONS\" (\"P_ID\" INTEGER NOT NULL, \"LASTNAME\" VARCHAR(255) NOT NULL, \"FIRSTNAME\" VARCHAR(255), \"ADDRESS\" VARCHAR(255), \"CITY\" VARCHAR(255), PRIMARY KEY (\"P_ID\"))"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("CREATE TABLE `PERSONS` (`P_ID` INTEGER NOT NULL, `LASTNAME` VARCHAR(255) NOT NULL, `FIRSTNAME` VARCHAR(255), `ADDRESS` VARCHAR(255), `CITY` VARCHAR(255), PRIMARY KEY (`P_ID`))"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("CREATE TABLE [PERSONS] ([P_ID] INTEGER NOT NULL, [LASTNAME] VARCHAR(255) NOT NULL, [FIRSTNAME] VARCHAR(255), [ADDRESS] VARCHAR(255), [CITY] VARCHAR(255), PRIMARY KEY ([P_ID]))"));
	}
	
	

	/**
	 * 【子查询】用例三十：
		select s.s_id , s.s_name , avg(score) from t_student s join t_grade g on s.s_id = g.s_id group by s.s_id , s.s_name 
		having avg(score) > (select avg(score) from t_student s join t_grade g on s.s_id = g.s_id where s.s_name = 'zhang' group by s.s_id) 
	 * 说明：使用聚合函数、Group by与子查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_table_30() throws Exception {
		String sql = "select s.s_id , s.s_name , avg(score) from t_student s join t_grade g on s.s_id = g.s_id group by s.s_id , s.s_name "
				+ "having avg(score) > (select avg(score) from t_student s join t_grade g on s.s_id = g.s_id where s.s_name = 'zhang' group by s.s_id)";
		logger.info("#### Run test 30 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/ddl")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT \"S\".\"S_ID\", \"S\".\"S_NAME\", AVG(\"SCORE\") FROM \"T_STUDENT\" \"S\" INNER JOIN \"T_GRADE\" \"G\" ON \"S\".\"S_ID\" = \"G\".\"S_ID\" GROUP BY \"S\".\"S_ID\", \"S\".\"S_NAME\" HAVING AVG(\"SCORE\") > (SELECT AVG(\"SCORE\") FROM \"T_STUDENT\" \"S\" INNER JOIN \"T_GRADE\" \"G\" ON \"S\".\"S_ID\" = \"G\".\"S_ID\" WHERE \"S\".\"S_NAME\" = 'zhang' GROUP BY \"S\".\"S_ID\")"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT \"S\".\"S_ID\", \"S\".\"S_NAME\", AVG(\"SCORE\") FROM \"T_STUDENT\" AS \"S\" INNER JOIN \"T_GRADE\" AS \"G\" ON \"S\".\"S_ID\" = \"G\".\"S_ID\" GROUP BY \"S\".\"S_ID\", \"S\".\"S_NAME\" HAVING AVG(\"SCORE\") > (SELECT AVG(\"SCORE\") FROM \"T_STUDENT\" AS \"S\" INNER JOIN \"T_GRADE\" AS \"G\" ON \"S\".\"S_ID\" = \"G\".\"S_ID\" WHERE \"S\".\"S_NAME\" = 'zhang' GROUP BY \"S\".\"S_ID\")"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT `S`.`S_ID`, `S`.`S_NAME`, AVG(`SCORE`) FROM `T_STUDENT` AS `S` INNER JOIN `T_GRADE` AS `G` ON `S`.`S_ID` = `G`.`S_ID` GROUP BY `S`.`S_ID`, `S`.`S_NAME` HAVING AVG(`SCORE`) > (SELECT AVG(`SCORE`) FROM `T_STUDENT` AS `S` INNER JOIN `T_GRADE` AS `G` ON `S`.`S_ID` = `G`.`S_ID` WHERE `S`.`S_NAME` = 'zhang' GROUP BY `S`.`S_ID`)"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT [S].[S_ID], [S].[S_NAME], AVG([SCORE]) FROM [T_STUDENT] AS [S] INNER JOIN [T_GRADE] AS [G] ON [S].[S_ID] = [G].[S_ID] GROUP BY [S].[S_ID], [S].[S_NAME] HAVING AVG([SCORE]) > (SELECT AVG([SCORE]) FROM [T_STUDENT] AS [S] INNER JOIN [T_GRADE] AS [G] ON [S].[S_ID] = [G].[S_ID] WHERE [S].[S_NAME] = 'zhang' GROUP BY [S].[S_ID])"));
	}
	

	/**
	 * 【单表查询】用例三十一：select * from test_table where name = '王五' 
	 * 说明：含有中文文本类的查询
	 * @throws Exception
	 */
	@Test
	public void standard_dml_select_table_31() throws Exception {
		String sql = "select * from test_table where name = '王五'";
		logger.info("#### Run test 31 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/dml")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("SELECT * FROM \"TEST_TABLE\" WHERE \"NAME\" = '王五'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("SELECT * FROM \"TEST_TABLE\" WHERE \"NAME\" = '王五'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("SELECT * FROM `TEST_TABLE` WHERE `NAME` = '王五'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("SELECT * FROM [TEST_TABLE] WHERE [NAME] = '王五'"));
	}
	
	/**
	 * 【创建视图】用例三十二：create or replace view public.v_xxxx as select xgh,name,sex from test_table where shenfen='student'
	 * 说明：创建或修改该视图
	 * @throws Exception
	 */
	@Test
	public void standard_ddl_create_or_replace_view_32() throws Exception {
		String sql = "create or replace view public.v_xxxx as select xgh,name,sex from test_table where shenfen='student'";
		logger.info("#### Run test 32 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/ddl")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("CREATE OR REPLACE VIEW \"PUBLIC\".\"V_XXXX\" AS SELECT \"XGH\", \"NAME\", \"SEX\" FROM \"TEST_TABLE\" WHERE \"SHENFEN\" = 'student'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("CREATE OR REPLACE VIEW \"PUBLIC\".\"V_XXXX\" AS SELECT \"XGH\", \"NAME\", \"SEX\" FROM \"TEST_TABLE\" WHERE \"SHENFEN\" = 'student'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("CREATE OR REPLACE VIEW `PUBLIC`.`V_XXXX` AS SELECT `XGH`, `NAME`, `SEX` FROM `TEST_TABLE` WHERE `SHENFEN` = 'student'"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("CREATE OR REPLACE VIEW [PUBLIC].[V_XXXX] AS SELECT [XGH], [NAME], [SEX] FROM [TEST_TABLE] WHERE [SHENFEN] = 'student'"));
	}
	
	/**
	 * 【删除表】用例三十三：drop table public.t_test
	 * @throws Exception
	 */
	@Test
	public void standard_dml_drop_table_33() throws Exception {
		String sql = "drop table public.t_test";
		logger.info("#### Run test 33 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/ddl")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("DROP TABLE \"PUBLIC\".\"T_TEST\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("DROP TABLE \"PUBLIC\".\"T_TEST\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("DROP TABLE `PUBLIC`.`T_TEST`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("DROP TABLE [PUBLIC].[T_TEST]"));
	}
	
	/**
	 * 【删除视图】用例三十四：drop view public.v_test
	 * @throws Exception
	 */
	@Test
	public void standard_ddl_drop_view_34() throws Exception {
		String sql = "drop view public.v_test";
		logger.info("#### Run test 34 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/ddl")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("DROP VIEW \"PUBLIC\".\"V_TEST\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("DROP VIEW \"PUBLIC\".\"V_TEST\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("DROP VIEW `PUBLIC`.`V_TEST`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("DROP VIEW [PUBLIC].[V_TEST]"));
	}
	

	/**
	 * 【删除视图】用例三十五：create table private.t_table_test as select * from public.t_other_table
	 * @throws Exception
	 */
	@Test
	public void standard_ddl_create_table_as_select_35() throws Exception {
		String sql = "create table private.t_table_test as select * from public.t_other_table";
		logger.info("#### Run test 35 : {}", sql);
		JSONObject json = new JSONObject();
		json.put("sql", sql);

		MockHttpSession session = new MockHttpSession();
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post("/sql/debug/standard/ddl")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(json.toJSONString().getBytes())
				.session(session));

		action.andExpect(MockMvcResultMatchers.status().isOk());
		action.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.errmsg").value("success"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.oracle").value("CREATE TABLE \"PRIVATE\".\"T_TABLE_TEST\" AS SELECT * FROM \"PUBLIC\".\"T_OTHER_TABLE\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.postgresql").value("CREATE TABLE \"PRIVATE\".\"T_TABLE_TEST\" AS SELECT * FROM \"PUBLIC\".\"T_OTHER_TABLE\""));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.mysql").value("CREATE TABLE `PRIVATE`.`T_TABLE_TEST` AS SELECT * FROM `PUBLIC`.`T_OTHER_TABLE`"));
		action.andExpect(MockMvcResultMatchers.jsonPath("$.data.sql.sqlserver").value("CREATE TABLE [PRIVATE].[T_TABLE_TEST] AS SELECT * FROM [PUBLIC].[T_OTHER_TABLE]"));
	}
}
