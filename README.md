# 异构数据库适配服务

## 一、功能描述

### 1、支持的功能

- 支持标准SQL语法DML/DDL格式与MySQL/Oralce/PostgreSQL/SqlServer/Greenplum数据库语法的转换；
- 通过给定的数据库连接信息获取相关的元信息数据(模式列表信息、表或视图信息、字段列信息、主键信息等)；
- 异构数据库建根据表结构分析对应数据库的建表语句等；

### 2、不支持的功能

- DCL类的SQL转换

### 3、标准SQL参考

- 参考地址：[SQL99参考地址](https://crate.io/docs/sql-99/en/latest/)

## 二、知识点梳理

### 1、SQL分类
- DDL—数据定义语言(CREATE，ALTER，DROP，DECLARE) 
- DML—数据操纵语言(SELECT，DELETE，UPDATE，INSERT) 
- DCL—数据控制语言(GRANT，REVOKE，COMMIT，ROLLBACK)

### 2、sql 语句大小写的问题
 - 关键字不区分大小写，例如 select ，from， 大小写均可
 - 标识符区分大小写，例如 表名，列名
 - 标识符如果不加双引号，默认是按大写执行
 - 标识符如果加引号，则是按原始大小写执行
 
### 3、支持在JDBC中常用带占位符的预编译读取和写入
> 允许使用?占位符进行DML类的SQL进行语法转换

### 4、create table的自增字段问题不支持
> 各个数据库的实现差异较大，请参考[链接](https://www.w3school.com.cn/sql/sql_autoincrement.asp)

## 三、支持的数据库

- SQL语法转换部分

| 数据库名称 | 数据库英文 | 简写 | 数据库版本 |
| :------:| :------: | :------: | :------: |
| 甲骨文数据库 | oracle | oracle | >=12c |
| 微软SqlServer | SqlServer | sqlserver | >=2012 |
| MySQL数据库 | mysql | mysql | >=5.7 |
| PostgreSQL | PostgreSQL | postgresql | >=9.4 |
| Greenplum | Greenplum | greenplum | >=6.0 |

> 注：Greenplum绝大多数语法同PostgreSQL的语法。

- 异构库表结构转换部分

| 数据库名称 | 数据库英文 | 简写 | 数据库版本 |
| :------:| :------: | :------: | :------: |
| 甲骨文数据库 | oracle | oracle | >=10g |
| 微软SqlServer | SqlServer | sqlserver | >=2005 |
| MySQL数据库 | mysql | mysql | >=5.5 |
| PostgreSQL | PostgreSQL | postgresql | >=9.2 |

## 四、错误异常返回

所有接口均存在异常情况，定义的异常返回格式如下：

```
{
  "errcode": -1,
  "errmsg": "Invalid JSON format：expect ':' at 0, name source:"
}
```

| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |


## 五、SQL语法转换部分接口

### 1、标准DML类SQL语句转换

 **URI:** http://host:port/sql/standard/dml
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| sql | string | SQL语句 | 标准DML类SQL语句 |

**Request Example:**

```
{
	"sql":"select * from TEST_TABLE limit 10 offset 20"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的数据列表 |
| oracle | string | Oracle语法的SQL | Oracle语法的SQL |
| postgresql | string | postgresql语法的SQL | postgresql语法的SQL |
| mysql | string | mysql语法的SQL | mysql语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": {
      "oracle": "SELECT * FROM \"TEST_TABLE\" OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY",
      "postgresql": "SELECT * FROM \"TEST_TABLE\" OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY",
      "mysql": "SELECT * FROM `TEST_TABLE` LIMIT 10 OFFSET 20"
    }
  },
  "errmsg": "success"
}
```

**Supported Type:**

以下支持的SQL类型均支持使用?预编译模式的SQL转换。

- Select语句
> where、like、order by、group by、having、count()、sum()、avg()、max()、min()、union、distinct、and/or、>、=、<、(not) in、as、INNER JOIN、LEFT JOIN、RIGHT JOIN、FULL JOIN、UNION ALL、limit/offset、between/and、is (not) null、 通配符(%_)、子查询、中文值

- Insert语句
> VALUES、INSERT INTO SELECT FROM、

- Update语句
> where、like、and/or、>、=、<、(not) in

- Delete语句
> where、like、and/or、>、=、<、(not) in

### 2、标准DDL类SQL语句转换

 **URI:** http://host:port/sql/standard/ddl
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| sql | string | SQL语句 | 标准SQL语句 |

**Request Example:**

```
{
	"sql":"CREATE TABLE \"TEST_TABLE_ALL_TYPE\" (\"A\" INTEGER NOT NULL,\"B\" VARCHAR(5) NULL,\"C\" CHAR(10),\"D\" TIMESTAMP,\"E\" DATE, PRIMARY KEY (\"A\"))"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的数据列表 |
| oracle | string | Oracle语法的SQL | Oracle语法的SQL |
| postgresql | string | postgresql语法的SQL | postgresql语法的SQL |
| mysql | string | mysql语法的SQL | mysql语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": {
      "oracle": "CREATE TABLE \"TEST_TABLE_ALL_TYPE\" (\"A\" INTEGER NOT NULL, \"B\" VARCHAR(5), \"C\" CHAR(10), \"D\" TIMESTAMP, \"E\" DATE, PRIMARY KEY (\"A\"))",
      "postgresql": "CREATE TABLE \"TEST_TABLE_ALL_TYPE\" (\"A\" INTEGER NOT NULL, \"B\" VARCHAR(5), \"C\" CHAR(10), \"D\" TIMESTAMP, \"E\" DATE, PRIMARY KEY (\"A\"))",
      "sqlserver": "CREATE TABLE [TEST_TABLE_ALL_TYPE] ([A] INTEGER NOT NULL, [B] VARCHAR(5), [C] CHAR(10), [D] TIMESTAMP, [E] DATE, PRIMARY KEY ([A]))",
      "mysql": "CREATE TABLE `TEST_TABLE_ALL_TYPE` (`A` INTEGER NOT NULL, `B` VARCHAR(5), `C` CHAR(10), `D` TIMESTAMP, `E` DATE, PRIMARY KEY (`A`))"
    }
  },
  "errmsg": "success"
}
```

### 3、[调试使用]指定数据库的DML类SQL语句转换

 **URI:** http://host:port/sql/special/dml
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| source | string | 源库类型 | 源库类型,支持mysql/oralce/sqlserver |
| target | string | 目的库类型 | 标准SQL语句,支持mysql/oralce/sqlserver/posgresql |
| sql | string | SQL语句 | 源库语法的SQL语句 |

**Request Example:**

```
{
	"source":"mysql",
	"target":"oracle",
	"sql":"select * from `test_table`"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的数据列表 |
| sql | string | 对应数据库语法的SQL | 对应数据库语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": "SELECT * FROM \"test_table\""
  },
  "errmsg": "success"
}
```

### 4、[调试使用]指定数据库的DDL类SQL语句转换

 **URI:** http://host:port/sql/special/ddl
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| source | string | 源库类型 | 源库类型,支持mysql/oralce/sqlserver |
| target | string | 目的库类型 | 标准SQL语句,支持mysql/oralce/sqlserver/posgresql |
| sql | string | SQL语句 | 源库语法的SQL语句 |

**Request Example:**

```
{
	"source":"mysql",
	"target":"oracle",
	"sql":"create table `test_table` (`i` int not null, `j` varchar(5) null)"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的数据列表 |
| sql | string | 对应数据库语法的SQL | 对应数据库语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": "CREATE TABLE \"test_table\" (\"i\" INTEGER NOT NULL, \"j\" VARCHAR(5))"
  },
  "errmsg": "success"
}
```

## 六、异构库表结构转换部分接口

### 1、获取数据库中所有的模式(model/schema)
 
 **URI:** http://host:port/database/models_list
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|

**Request Example:**

```
 {
    "type":"oracle",
    "host":"172.16.90.252",
    "port":1521,
    "user":"yi_bo",
    "passwd":"yi_bo",
    "dbname":"orcl",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的模式列表 |

**Response Example:**

```
{
    "data":[                       
		"SYS",
		"ODI",
		"TEST",
    ],
    "errcode":0,                  
    "errmsg":"ok"                 
}
```

### 2、获取数据库中指定模式下的所有表
 **URI:** http://host:port/database/tables_list
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| model | string | 模式名 | Schema名称 |
| charset | string | 字符集 | 数据库的字符集|

**Request Example:**

```
 {
    "type":"oracle",
    "host":"172.16.90.252",
    "port":1521,
    "user":"yi_bo",
    "passwd":"yi_bo",
    "dbname":"orcl",
    "model":"ODI",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的列表 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks    | string | 中文描述 | 源库里的表comment描述,可能为null |

**Response Example:**

```
{
    "data":[                     
		{
			"table_type": "table",   
			"table_name": "test_world",
			"remarks": "测试表" 
		},
		{
			"table_type": "view",
			"table_name": "v_test",
			"remarks": "视图表" 
		}
    ],
    "errcode":0,            
    "errmsg":"ok"           
}
```

### 3、获取业务数据库中指定表的元信息
 
 **URI:** http://host:port/database/table_info
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| model | string | 模式名 | Schema名称 |
| charset | string | 字符集 | 数据库的字符集|
| src_table | string | 源表名称 | 查询的源业务库表名的实际名称|
 
**Request Example:**

```
{
    "type":"oracle",  
    "host":"172.16.90.252",
    "port":1521,
    "user":"yi_bo",
    "passwd":"yi_bo",
    "dbname":"orcl",
    "model":"YI_BO",
    "charset":"utf-8",
    "src_table":"C_SEX",
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据列表 | 返回的列表 |
| create_sql | string | 建表的SQL语句 | MySQL数据库语法的建表SQL语句 |
| primary_key | list | 表的主键列 | 表的主键字段列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：1-是；0-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | Number precision | Number precision |
| scale | integer | Number scale | Number scale  |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 中文描述 | 源库里的字段的comment描述,可能为null |

 **Response Example:**
 
```
{
    "data": {
        "primary_key": [
            "id"
        ],
        "columns": [
            {
                "scale": 0,
                "name": "id",
                "nullable": 0,
                "type": "NUMBER",
                "display_size": 12,
                "precision": 11,
				"remarks": "编号",
                "class_type": "java.math.BigDecimal"
            },
            {
                "scale": null,
                "name": "name",
                "nullable": 1,
                "type": "NVARCHAR2",
                "display_size": 255,
                "precision": null,
				"remarks": "名称",
                "class_type": "java.lang.String"
            },
            {
                "scale": null,
                "name": "value",
                "nullable": 1,
                "type": "NVARCHAR2",
                "display_size": 255,
                "precision": null,
				"remarks": "取值",
                "class_type": "java.lang.String"
            }
        ]
    },
    "errcode": 0,
    "errmsg": "ok"
}
```


### 4、获取业务数据库中指定SQL的元信息
 
 **URI:** http://host:port/database/sql_info
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|
| querysql | string | SQL语句 | SELECT查询的SQL语句|
 
**Request Example:**

```
{
    "type":"oracle",  
    "host":"172.16.90.252",
    "port":1521,
    "user":"yi_bo",
    "passwd":"yi_bo",
    "dbname":"orcl",
    "charset":"utf-8",
    "src_table":"select * fromt YI_BO.C_SEX",
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据列表 | 返回的列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：1-是；0-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | Number precision | Number precision |
| scale | integer | Number scale | Number scale  |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 中文描述 | 源库里的字段的comment描述,可能为null |

 **Response Example:**
 
```
{
    "data": {
        "primary_key": [
            "id"
        ],
        "columns": [
            {
                "scale": 0,
                "name": "id",
                "nullable": 0,
                "type": "NUMBER",
                "display_size": 12,
                "precision": 11,
				"remarks": null,
                "class_type": "java.math.BigDecimal"
            },
            {
                "scale": null,
                "name": "name",
                "nullable": 1,
                "type": "NVARCHAR2",
                "display_size": 255,
                "precision": null,
				"remarks": null,
                "class_type": "java.lang.String"
            },
            {
                "scale": null,
                "name": "value",
                "nullable": 1,
                "type": "NVARCHAR2",
                "display_size": 255,
                "precision": null,
				"remarks": null,
                "class_type": "java.lang.String"
            }
        ]
    },
    "errcode": 0,
    "errmsg": "ok"
}
```


### 5、转换业务数据库中指定表为建表SQL语句
 
 **URI:** http://host:port/database/table_sql
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 源数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| charset | string | 字符集 | 数据库的字符集|
| dbname | string | 库名 | 连接的数据库名称 |
| src_model | string | 来源库模式名 | 来源库Schema名称 |
| src_table | string | 来源库源表名称 | 来源库业务库表名的实际名称|
| target | string | 目的数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| dest_model | string | 目的库模式名 | 目的库Schema名称 |
| dest_table | string | 目的库表名称 | 目的库建表的名称|
 
**Request Example:**

```
{
    "type":"oracle",  
    "host":"172.16.90.252",
    "port":1521,
    "user":"yi_bo",
    "passwd":"yi_bo",
    "dbname":"orcl",
    "charset":"utf-8",
    "src_model":"YI_BO",
    "src_table":"C_SEX",
	"target":"mysql",
    "dest_model":"test",
    "dest_table":"test",
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据列表 | 返回的列表 |
| create_sql | string | 建表的SQL语句 | MySQL数据库语法的建表SQL语句 |
| primary_key | list | 表的主键列 | 表的主键字段列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：1-是；0-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | Number precision | Number precision |
| scale | integer | Number scale | Number scale  |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 中文描述 | 源库里的字段的comment描述,可能为null |

 **Response Example:**
 
```
{
    "data": {
        "primary_key": [
            "id"
        ],
        "columns": [
            {
                "scale": 0,
                "name": "id",
                "nullable": 0,
                "type": "NUMBER",
                "display_size": 12,
                "precision": 11,
				"remarks": "编号",
                "class_type": "java.math.BigDecimal"
            },
            {
                "scale": null,
                "name": "name",
                "nullable": 1,
                "type": "NVARCHAR2",
                "display_size": 255,
                "precision": null,
				"remarks": "名称",
                "class_type": "java.lang.String"
            },
            {
                "scale": null,
                "name": "value",
                "nullable": 1,
                "type": "NVARCHAR2",
                "display_size": 255,
                "precision": null,
				"remarks": "取值",
                "class_type": "java.lang.String"
            }
        ],
		"create_sql": "CREATE TABLE IF NOT EXISTS `test`.`test` (\n`id` BIGINT not null,\n`name` TEXT null,\n`value` TEXT null,\nPRIMARY KEY (`id`)\n)"
    },
    "errcode": 0,
    "errmsg": "ok"
}
```

### 6、测试指定数据库中sql有效性
 **URI:** http://host:port/database/sql_test
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|
| querysql | string | SQL语句 | 待验证的合法SQL|

**Request Example:**

```
 {
    "type":"oracle",
    "host":"172.16.90.252",
    "port":1521,
    "user":"yi_bo",
    "passwd":"yi_bo",
    "dbname":"orcl",
    "querysql":"select * from YI_BO.test",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |

**Response Example:**

```
{
    "errcode":0,            
    "errmsg":"ok"           
}
```