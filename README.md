# 异构数据库适配服务

## 一、功能描述

### 1、SQL语句的分类
- DDL—数据定义语言(CREATE，ALTER，DROP，DECLARE) 
- DML—数据操纵语言(SELECT，DELETE，UPDATE，INSERT) 
- DCL—数据控制语言(USER，GRANT，REVOKE，COMMIT，ROLLBACK)

### 2、sql 语句大小写问题
 - 关键字不区分大小写，例如： select ，from， 大小写均可
 - 标识符区分大小写，例如：表名，列名
 - 标识符如果不加英文双引号，表名字段名默认是按大写执行
 - 标识符如果加英文双引号，则表名字段名是按原始大小写执行

### 3、支持的功能

- 支持标准SQL语法DML/DDL(部分)格式与MySQL/Oralce/PostgreSQL/SqlServer/Greenplum数据库语法的转换；
- 通过给定的数据库连接信息获取相关的元信息数据(模式列表信息、表或视图信息、字段列信息、主键信息等)；
- 异构数据库建根据表结构分析对应数据库的建表语句等；
- 基于函数式的DDL建表/改表/删表/清表的SQL拼接生成；
- 允许使用?占位符进行DML类的SQL进行语法转换；

### 4、标准SQL参考

- 参考地址：[SQL99参考地址](https://crate.io/docs/sql-99/en/latest/)

- SQL书写建议：表名及字段名用双引号"进行包裹

## 二、不支持的功能描述

### 1、不支持的功能总述

- 全部的DCL类的SQL转换
> Oracle建帐号User牵涉表空间；Greenplum建帐号需要修改pg_hba.conf配置文件支持外部可访问等；

> **解决方法**: 具体问题具体分析；

- 非标准的DDL类SQL的转换
> 牵涉数据库的专用数据类型

> **解决方法**: 具体问题具体分析；

### 2、建表语句不能完全支持所有数据类型
> 因牵涉数据库的专用数据类型,需要在标准管理中使用底层数据库支持的数据类型建表；
> 不同数据库支持的数据类型整理见附录一；

> **解决方法**: 此问题为规则增加问题，发现问题可按照BUG处理；

### 3、建表时的自增字段问题不支持
> 各个数据库的实现差异较大，请参考[链接](https://www.w3school.com.cn/sql/sql_autoincrement.asp)

> **解决方法**: 不是问题，因在标准管理中不存在建立自增字段的情况；

### 4、建表时的表与字段注释问题不完全支持
> 当前MySQL支持在create table中使用comment 设置字段中文注释;
> 在Oracle/Greenplum需要使用单独的COMMENT ON TABLE、COMMENT ON COLUMN命令设置；

> **解决方法**: 不是问题，因在标准管理中的表的注释信息存储在MySQL配置库中；

### 4、表主键的修改问题
> MySQL/Oracle数据库允许直接使用修改,但Greenplum需要先将主键删除后再添加，不支持修改；

> **解决方法**: 考虑单独处理；

### 5、数据库结构转换的兼容性问题
 - oracle中 VARCHAR2(4000) 类型可以作为主键，在MySQL中varchar做主键最大长度为255 ;
 - 在MySQL数据库中text、varchar(>255)、blob等类型不允许做主键 ;
 - 在Greenplum中分布式键不允许修改；
 - 在MySQL数据库中varchar类型的总长度不应大于65535
 - 整理中....
 
> **解决方法**: 出现的案例及其少见，考虑人工干预处理；

## 三、支持的数据库

- SQL语法转换部分

| 数据库名称 | 数据库英文 | 简写 | 数据库版本 |
| :------:| :------: | :------: | :------: |
| 甲骨文数据库 | oracle | oracle | >=12c |
| 微软SqlServer | SqlServer | sqlserver | >=2012 |
| MySQL数据库 | mysql | mysql | >=5.7 |
| PostgreSQL | PostgreSQL | postgresql | >=9.0 |
| Greenplum | Greenplum | greenplum | >=6.0 |

> 注：Greenplum绝大多数语法同PostgreSQL的语法。

- 异构库表结构转换部分

| 数据库名称 | 数据库英文 | 简写 | 数据库版本 |
| :------:| :------: | :------: | :------: |
| 甲骨文数据库 | oracle | oracle | >=9i |
| 微软SqlServer | SqlServer | sqlserver | 2000及>=2005 |
| MySQL数据库 | mysql | mysql | >=5.5 |
| PostgreSQL | PostgreSQL | postgresql | >=9.0 |

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
| target | string | 目的库类型 | 标准SQL语句,支持mysql/oralce/sqlserver/posgresql/greenplum |
| sql | string | SQL语句 | 标准DML类SQL语句 |

**Request Example:**

```
{
    "target":"oracle",
    "sql":"select * from TEST_TABLE limit 10 offset 20"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 指定数据库语法的SQL | 指定数据库语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": "SELECT * FROM \"TEST_TABLE\" OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY"
  },
  "errmsg": "success"
}
```

**Supported Notice:**

以下支持的SQL类型均支持使用?预编译模式的SQL转换。

- Select语句
> **支持**where、like、order by、group by、having、count()、sum()、avg()、max()、min()、union、distinct、and/or、>、=、<、(not) in、as、INNER JOIN、LEFT JOIN、RIGHT JOIN、FULL JOIN、UNION ALL、limit/offset、between/and、is (not) null、 通配符(%_)、子查询、中文值；

> **不支持**特定数据库专用的函数，例如MySQL的now()、oracle的sysdate等；

> **特殊说明** 对于查询分页问题，对于Oracle只支持12c及其以上版本；标准分页的语句为limit a offset b;

- Insert语句
> **支持**VALUES、INSERT INTO SELECT FROM、

- Update语句
> **支持**where、like、and/or、>、=、<、(not) in

- Delete语句
> **支持**where、like、and/or、>、=、<、(not) in

### 2、标准DDL类SQL语句转换

 **URI:** http://host:port/sql/standard/ddl
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| target | string | 目的库类型 | 标准SQL语句,支持mysql/oralce/sqlserver/posgresql/greenplum |
| sql | string | SQL语句 | 标准SQL语句 |

**Request Example:**

```
{
    "target":"oracle",
    "sql":"create or replace view v_xxxx as (select xgh,name,sex from test_table where shenfen='student')"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 指定数据库语法的SQL | 指定数据库语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": "CREATE OR REPLACE VIEW \"V_XXXX\" AS\nSELECT \"XGH\", \"NAME\", \"SEX\"\nFROM \"TEST_TABLE\"\nWHERE \"SHENFEN\" = 'student'",
  },
  "errmsg": "success"
}
```

**Supported Notice:**

- 用于与数据库的数据类型无关的DDL类SQL转换，如：create view、 create table as select * from table、drop view、drop table等等，但不支持truncate table、alter table等；
- 因受限于不同数据库的数据类型的差异，这里并不支持特定的建表、改表、清表的SQL语句转换，此部分需要使用**第7部分的接口**来弥补；

### 3、[**调试使用**]标准DML类SQL语句转换

 **URI:** http://host:port/sql/debug/standard/dml
 
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
| data | Object | 数据对象 | 返回的数据对象 |
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

### 4、[**调试使用**]指定数据库的DML类SQL语句转换

 **URI:** http://host:port/sql/debug/special/dml
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| source | string | 源库类型 | 源库类型,支持mysql/oralce/sqlserver |
| target | string | 目的库类型 | 标准SQL语句,支持mysql/oralce/sqlserver/posgresql/greenplum |
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
| data | Object | 数据对象 | 返回的数据对象 |
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

### 5、[**调试使用**]标准DDL类SQL语句转换

 **URI:** http://host:port/sql/standard/ddl
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 备注说明 |
| :------:| :------: | :------: | :------ |
| sql | string | SQL语句 | 标准SQL语句 |

**Request Example:**

```
{
   "sql":"create or replace view v_xxxx as (select xgh,name,sex from test_table where shenfen='student')"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| oracle | string | Oracle语法的SQL | Oracle语法的SQL |
| postgresql | string | postgresql语法的SQL | postgresql语法的SQL |
| mysql | string | mysql语法的SQL | mysql语法的SQL |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": {
      "oracle": "CREATE OR REPLACE VIEW \"V_XXXX\" AS\nSELECT \"XGH\", \"NAME\", \"SEX\"\nFROM \"TEST_TABLE\"\nWHERE \"SHENFEN\" = 'student'",
      "postgresql": "CREATE OR REPLACE VIEW \"V_XXXX\" AS\nSELECT \"XGH\", \"NAME\", \"SEX\"\nFROM \"TEST_TABLE\"\nWHERE \"SHENFEN\" = 'student'",
      "sqlserver": "CREATE OR REPLACE VIEW [V_XXXX] AS\nSELECT [XGH], [NAME], [SEX]\nFROM [TEST_TABLE]\nWHERE [SHENFEN] = 'student'",
      "mysql": "CREATE OR REPLACE VIEW `V_XXXX` AS\nSELECT `XGH`, `NAME`, `SEX`\nFROM `TEST_TABLE`\nWHERE `SHENFEN` = 'student'"
    }
  },
  "errmsg": "success"
}
```

### 6、[**调试使用**]指定数据库的DDL类SQL语句转换

 **URI:** http://host:port/sql/debug/special/ddl
 
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
| data | Object | 数据对象 | 返回的数据对象 |
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
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|

**Request Example:**

```
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
        "TEST"
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
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| model | string | 模式名 | Schema名称 |
| charset | string | 字符集 | 数据库的字符集|

**Request Example:**

```
{
    "type":"oracle",
    "host":"172.17.207.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tangyibo",
    "dbname":"orcl",
    "model":"YI_BO",
    "charset":"utf-8"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | list | 数据列表 | 返回的数据列表 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks    | string | 中文描述 | 源库里的表注释描述,可能为null |

**Response Example:**

```
{
    "data":[
        {
            "table_type":"table",
            "table_name":"test_world",
            "remarks":"测试表"
        },
        {
            "table_type":"view",
            "table_name":"v_test",
            "remarks":"视图表"
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
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
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
    "host":"172.17.207.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tangyibo",
    "dbname":"orcl",
    "model":"YI_BO",
    "charset":"utf-8",
    "src_table":"C_SEX"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| primary_key | list | 表的主键列 | 表的主键字段列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：true-是；false-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | 浮点数的精度 | 浮点数的精度 |
| scale | integer | 浮点数的位数 | 浮点数的位数 |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 字段注释 | 源库里的字段的comment描述,可能为null |
| metadata | Object | 表元信息 | 表元信息对象 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks | string | 表注释 | metadata下的remarks字段，取值：null、空字符串、普通字符串|

 **Response Example:**
 
```
{
  "errcode": 0,
  "errmsg": "success",
  "data": {
    "metadata": {
      "table_name": "C_SEX",
      "remarks": "性别测试表",
      "table_type": "TABLE"
    },
    "columns": [
      {
        "class_type": "java.math.BigDecimal",
        "nullable": false,
        "precision": 11,
        "name": "id",
        "display_size": 12,
        "scale": 0,
        "type": "NUMBER",
        "remarks": "编号"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "name",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": "名称"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "value",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": "取值"
      }
    ],
    "primary_key": [
      "id"
    ]
  }
}
```

### 4、获取业务数据库中指定SQL的元信息
 
 **URI:** http://host:port/database/sql_info
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| model | string | 模式名 | Schema名称 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|
| querysql | string | SQL语句 | SELECT查询的SQL语句|
 
**Request Example:**

```
{
    "type":"oracle",
    "host":"172.17.207.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tangyibo",
    "dbname":"orcl",
    "charset":"utf-8",
    "querysql":"select * from YI_BO.C_SEX"
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：true-是；false-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | 浮点数的精度 | 浮点数的精度 |
| scale | integer | 浮点数的位数 | 浮点数的位数 |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 中文描述 | 源库里的字段的comment描述,可能为null |

 **Response Example:**
 
```
{
  "errcode": 0,
  "errmsg": "success",
  "data": {
    "columns": [
      {
        "class_type": "java.math.BigDecimal",
        "nullable": false,
        "precision": 11,
        "name": "id",
        "display_size": 12,
        "scale": 0,
        "type": "NUMBER",
        "remarks": null
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "name",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": null
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "value",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": null
      }
    ]
  }
}
```

### 5、转换业务数据库中指定表为建表SQL语句
 
 **URI:** http://host:port/database/table_sql
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
 | 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 源数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
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
    "host":"172.17.207.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tangyibo",
    "dbname":"orcl",
    "charset":"utf-8",
    "src_model":"YI_BO",
    "src_table":"C_SEX",
    "target":"mysql",
    "dest_model":"test",
    "dest_table":"test"
}
```
 
 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| create_sql | string | 建表的SQL语句 | 指定数据库语法的建表SQL语句 |
| primary_key | list | 表的主键列 | 表的主键字段列表 |
| columns | list | 表的字段列 | 表的字段列表 |
| name | string | 字段列名称 | 表的字段列表 |
| type | string | 字段列类型 | 表的字段列表 |
| nullable | integer | 是否可为空 | 取值：true-是；false-否 |
| display_size | integer | 显示长度 | 显示长度 |
| precision | integer | 浮点数的精度 | 浮点数的精度 |
| scale | integer | 浮点数的位数 | 浮点数的位数 |
| class_type | string | 内部存储类型 | 内部存储类型 |
| remarks    | string | 字段注释 | 源库里的字段的comment描述,可能为null |
| metadata | Object | 表元信息 | 表元信息对象 |
| table_name | string | 表名称 | 表或视图的英文名称 |
| table_type | string | 表类型 | 当表为物理表时标记为table;当表为视图表时标记为view |
| remarks | string | 表注释 | metadata下的remarks字段，取值：null、空字符串、普通字符串|

 **Response Example:**
 
```
{
  "errcode": 0,
  "data": {
    "metadata": {
      "table_name": "C_SEX",
      "remarks": "性别测试表",
      "table_type": "TABLE"
    },
    "columns": [
      {
        "class_type": "java.math.BigDecimal",
        "nullable": false,
        "precision": 11,
        "name": "id",
        "display_size": 12,
        "scale": 0,
        "type": "NUMBER",
        "remarks": "编号"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "name",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": "名称"
      },
      {
        "class_type": "java.lang.String",
        "nullable": true,
        "precision": 255,
        "name": "value",
        "display_size": 255,
        "scale": 0,
        "type": "NVARCHAR2",
        "remarks": "取值"
      }
    ],
    "create_sql": "CREATE TABLE `test`.`test` (\n\t`id` BIGINT NOT NULL,\n\t`name` VARCHAR(255),\n\t`value` VARCHAR(255),\n\tPRIMARY KEY (`id`)\n)",
    "primary_key": [
      "id"
    ]
  },
  "errmsg": "success"
}
```

### 6、测试指定数据库中sql有效性
 **URI:** http://host:port/database/sql_test
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver2000,sqlserver,postgresql |
| host | string | IP地址 | 数据库主机的IP地址 |
| port | integer | 端口号 | 整型的端口号 |
| mode | string | 连接方式 | 非必填，但只对Oracle连接有效，可取范围为：sid,servicename,tnsname三种，默认为sid |
| user | string | 帐号 | 登录的帐号名 |
| passwd | string | 密码 | 登录的密码 |
| dbname | string | 库名 | 连接的数据库名称 |
| charset | string | 字符集 | 数据库的字符集|
| querysql | string | SQL语句 | 待验证的合法SQL|

**Request Example:**

```
{
    "type":"oracle",
    "host":"172.17.207.252",
    "port":1521,
    "mode":"sid",
    "user":"yi_bo",
    "passwd":"tangyibo",
    "dbname":"orcl",
    "querysql":"select * from YI_BO.CJB",
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

## 七、表结构拼接生成部分接口

### 1、创建表拼接生成SQL语句
 
 **URI:** http://host:port/generator/create_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/Schema)名称 |
| table_name | string | 表名称 | 表名称 |
| column_list | list | 列信息 | 数组类型 |
| field_name | string | 字段英文名称 | 登录的帐号名 |
| comment | string | 字段注释 | 登录的密码 |
| field_type | string | 数据类型 | 不同数据库的数据类型存在差异,支持的数据类型请见后面的附录一 |
| length_or_precision | integer | 显示长度 | 显示长度 |
| scale | integer | 存储精度 | 对于浮点型数据与length_or_precision联合确定存储精度|
| nullable | integer | 是否可为空 | 1-为是；0-为否|
| primary_key | integer | 是否为主键 | 1-为是；0-为否|
| default_value | string | 默认值 | 当nullable为0时配置的默认值 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table",
    "column_list":[
        {
            "field_name":"col1",
            "comment":"列1",
            "field_type":"int",
            "length_or_precision":11,
            "scale":0,
            "nullable":0,
            "primary_key":1,
            "default_value":null
        },
        {
            "field_name":"col2",
            "comment":"列2",
            "field_type":"char",
            "length_or_precision":25,
            "scale":0,
            "nullable":0,
            "primary_key":0,
            "default_value":"test"
        }
    ]
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": " CREATE TABLE `tang`.`test_table` (\n  `col1` INT (11)  NOT NULL COMMENT '列1'\n,`col2` CHAR (25)  DEFAULT 'test' COMMENT '列2'\n, PRIMARY KEY (`col1`)\n )\n"
  },
  "errmsg": "success"
}
```

 **Supported Notice**
 
 - 建表参数中的字段注释comment当前只对于MySQL数据库有效，对于Oracle/Greenplum数据库无效；

### 2、修改表拼接生成SQL语句
 
 **URI:** http://host:port/generator/alter_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/schema)名称 |
| table_name | string | 表名称 | 表名称 |
| operator | string | 操作类型 | 取值范围请见下表《operator字段的取值说明》 |
| column_list | list | 列信息 | 数组类型 |
| field_name | string | 字段英文名称 | 字段英文名称 |
| comment | string | 字段注释 | 字段注释 |
| field_type | string | 数据类型 | 不同数据库的数据类型存在差异,,支持的数据类型请见后面的附录一 |
| length_or_precision | integer | 显示长度 | 显示长度 |
| scale | integer | 存储精度 | 对于浮点型数据与length_or_precision联合确定存储精度|
| nullable | integer | 是否可为空 | 1-为是；0-为否|
| default_value | string | 默认值 | 当nullable为0时配置的默认值 |

 **operator字段的取值说明**
 
| 取值 | 操作 | 描述 | 特殊说明 |
| :------:| :------: | :------: | :------ |
| add | 添加列 | 向数据库表中增加一列或多列 | 对于Oracle、MySQL两类数据库来说支持一次增加多列，对于PostgreSQL、Greenplum类数据库每次只能增加一列 |
| modify | 修改列 | 修改数据库表中的一列 | 每次只能修改一列的信息，包括列的类型、是否为空、默认值等 |
| drop | 删除列 | 删除数据库表中的一列 | 每次只能删除一列 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table",
    "operator":"add",
    "column_list":[
        {
            "field_name":"col1",
            "comment":"列1",
            "field_type":"int",
            "length_or_precision":11,
            "scale":0,
            "nullable":1,
            "default_value":null
        }
    ]
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": " CREATE TABLE `tang`.`test_table` (\n  `col1` INT (11)  NOT NULL COMMENT '列1'\n,`col2` CHAR (25)  DEFAULT 'test' COMMENT '列2'\n, PRIMARY KEY (`col1`)\n )\n"
  },
  "errmsg": "success"
}
```

**Supported Notice:**

 - 该接口不支持主键相关修改，Greenplum不支持主键更换修改，主键为分布式键；
 - 该接口不支持rename操作修改字段名；

### 3、删除表拼接生成SQL语句
 
 **URI:** http://host:port/generator/drop_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/Schema)名称 |
| table_name | string | 表名称 | 表名称 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": "DROP TABLE `public`.`test_table`"
  },
  "errmsg": "success"
}
```

### 4、清空表拼接生成SQL语句
 
 **URI:** http://host:port/generator/truncate_table
 
 **Request Method:** POST
 
 **Request Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| type | string | 数据库类型 | 可取值：oracle,mysql,sqlserver,postgresql,greenplum |
| schema_name | string | 模式名称 | 模式(model/Schema)名称 |
| table_name | string | 表名称 | 表名称 |

**Request Example:**

```
{
    "type":"mysql",
    "schema_name":"tang",
    "table_name":"test_table"
}
```

 **Response Format:** JOSN格式
 
| 字段名称 | 类型 | 描述 | 取值范围 |
| :------:| :------: | :------: | :------ |
| errcode | integer | 错误码 | 0为成功，其他为失败 |
| errmsg | string | 错误信息 | 当errcode=0时，为"ok",否则为错误的详细信息 |
| data | Object | 数据对象 | 返回的数据对象 |
| sql | string | 返回的SQL语句 | 返回的SQL语句 |

**Response Example:**

```
{
  "errcode": 0,
  "data": {
    "sql": "TRUNCATE TABLE `public`.`test_table`"
  },
  "errmsg": "success"
}
```

## 附录一、支持的三种数据库数据类型明细表

| 数据库 | 类型分类 | 数据类型 | 定义示例 |
| :------:| :------: | :------: | :------ |
| MySQL | 数字 | TINYINT | TINYINT(2) |
| MySQL | 数字 | SMALLINT | SMALLINT(2) |
| MySQL | 数字 | MEDIUMINT | MEDIUMINT(2) |
| MySQL | 数字 | INTEGER | INTEGER(2) |
| MySQL | 数字 | INT | INT(2) |
| MySQL | 数字 | BIGINT | BIGINT(2) |
| MySQL | 数字 | FLOAT | FLOAT(2) |
| MySQL | 数字 | DOUBLE | DOUBLE(2) |
| MySQL | 数字 | DECIMAL | DECIMAL(6,2) |
| MySQL | 时间 | DATE | DATE |
| MySQL | 时间 | TIME | TIME |
| MySQL | 时间 | YEAR | YEAR |
| MySQL | 时间 | DATETIME | DATETIME |
| MySQL | 时间 | TIMESTAMP | TIMESTAMP |
| MySQL | 文本 | CHAR | CHAR(2) |
| MySQL | 文本 | VARCHAR | VARCHAR(2) |
| MySQL | 文本 | TINYBLOB | TINYBLOB(2) |
| MySQL | 文本 | TINYTEXT | TINYTEXT |
| MySQL | 文本 | BLOB | BLOB |
| MySQL | 文本 | TEXT | TEXT |
| MySQL | 文本 | MEDIUMBLOB | MEDIUMBLOB |
| MySQL | 文本 | MEDIUMTEXT | MEDIUMTEXT |
| MySQL | 文本 | LONGBLOB | LONGBLOB |
| MySQL | 文本 | LONGTEXT | LONGTEXT |
| Oracle | 数字 | NUMBER | NUMBER(38,0)、NUMBER(38,2) |
| Oracle | 时间 | DATE | DATE |
| Oracle | 时间 | TIMESTAMP | TIMESTAMP |
| Oracle | 文本 | CHAR | CHAR(2) |
| Oracle | 文本 | NCHAR | NCHAR(2) |
| Oracle | 文本 | VARCHAR | VARCHAR(2) |
| Oracle | 文本 | VARCHAR2 | VARCHAR2(2) |
| Oracle | 文本 | LONG | LONG |
| Oracle | 文本 | CLOB | CLOB |
| Oracle | 文本 | BLOB | BLOB |
| Greenplum | 数字 | SMALLINT | SMALLINT(2) |
| Greenplum | 数字 | INT2 | INT2 |
| Greenplum | 数字 | INTEGER | INTEGER |
| Greenplum | 数字 | INT4 | INT4 |
| Greenplum | 数字 | BIGINT | BIGINT |
| Greenplum | 数字 | INT8 | INT8 |
| Greenplum | 数字 | DECIMAL | DECIMAL(8,2) |
| Greenplum | 数字 | NUMERIC | NUMERIC(8,2) |
| Greenplum | 数字 | REAL | REAL(8,2) |
| Greenplum | 数字 | DOUBLE | DOUBLE |
| Greenplum | 数字 | SERIAL | SERIAL |
| Greenplum | 数字 | BIGSERIAL | BIGSERIAL |
| Greenplum | 时间 | DATE | DATE |
| Greenplum | 时间 | TIME | TIME |
| Greenplum | 时间 | TIMESTAMP | TIMESTAMP |
| Greenplum | 文本 | CHAR | CHAR(2) |
| Greenplum | 文本 | VARCHAR | VARCHAR(128) |
| Greenplum | 文本 | TEXT | TEXT |
| Greenplum | 文本 | BYTEA | BYTEA |

## 附录二、编译打包方法

- 编译打包命令

```
git clone https://gitee.com/inrgihc/dbswitch.git
cd dbswitch/
sh ./build.sh
```

- 安装部署

当编译打包命令执行完成后，会在dbswitch/target/目录下生成dbswitch-relase-x.x.x.tar.gz的打包文件，将文件拷贝到部署机器上解压即可。

启动程序：

```
tar zxvf dbswitch-release-0.0.1.tar.gz
cd dbswitch-release-0.0.1/
bin/startup.sh
```

停止程序：

```
cd dbswitch-release-0.0.1/
bin/shutdown.sh
```
