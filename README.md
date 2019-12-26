# 异构数据库数据同步服务

## 一、支持功能

  表结构与数据的向Greenplum数据库的同步功能。

## 二、支持的数据库

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

## 三、编译打包方法

- 编译打包命令

```
git clone https://gitee.com/inrgihc/dbswitch.git
cd dbswitch/
sh ./build.sh
```

- 安装部署

当编译打包命令执行完成后，会在dbswitch/target/目录下生成dbswitch-relase-x.x.x.tar.gz的打包文件，将文件拷贝到部署机器上解压即可。

(1)启动WEBAPI服务程序：

```
tar zxvf dbswitch-release-0.0.1.tar.gz
cd dbswitch-release-0.0.1/
bin/startup.sh
```

(2)停止WEBAPI程序：

```
cd dbswitch-release-0.0.1/
bin/shutdown.sh
```

- 数据同步
支持表结构与数据的同步功能。配置文件信息如下：vim conf/config.properties
```
# source database connection information
source.datasource.type=com.zaxxer.hikari.HikariDataSource
source.datasource.jdbc-url= jdbc:mysql://172.17.207.210:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&zeroDateTimeBehavior=convertToNull
source.datasource.driver-class-name= com.mysql.cj.jdbc.Driver
source.datasource.username= tangyibo
source.datasource.password= tangyibo

# target database connection information,only support postgresql
target.datasource.type=com.zaxxer.hikari.HikariDataSource
target.datasource.jdbc-url= jdbc:postgresql://172.17.207.90:5432/study
target.datasource.driver-class-name= org.postgresql.Driver
target.datasource.username= study
target.datasource.password= 123456

# source database configuration parameters
## fetch size for query source database
source.datasource-fetch.size=10000
## schema name for query source database
source.datasource-source.schema=test
## table name exclude from table lists, 
source.datasource-source.excludes=ofuser_cas

# target database configuration parameters
## schema name for create/insert table data
target.datasource-target.schema=public
## whether drop table where target database exist
target.datasource-target.drop=true
```

执行命令如下：
```
cd dbswitch-release-0.0.1/
bin/datasync.sh
```

## 三、支持的三种数据库数据类型明细表

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

