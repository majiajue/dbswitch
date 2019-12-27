# 异构数据库数据与结构同步程序

## 一、支持功能

  oracle/SqlServer/mysql/PostgreSQL表结构及数据向Greenplum数据库的同步功能。


## 二、编译配置

### 1、编译打包命令

```
git clone https://gitee.com/inrgihc/dbswitch.git
cd dbswitch/
sh ./build.sh
```

### 2、安装部署

当编译打包命令执行完成后，会在dbswitch/target/目录下生成dbswitch-relase-x.x.x.tar.gz的打包文件，将文件拷贝到部署机器上解压即可。

### 3、配置文件

配置文件信息如下：vim conf/config.properties

| 配置参数 | 配置说明 | 示例 | 备注 |
| :------:| :------: | :------: | :------: |
| source.datasource.type     | 来源端连接池类型 | com.zaxxer.hikari.HikariDataSource |  默认即可 |
| source.datasource.jdbc-url | 来源端JDBC连接的URL | jdbc:oracle:thin:@172.17.207.158:1521:ORCL | 无 |
| source.datasource.driver-class-name | 来源端数据库的驱动类名称 | oracle.jdbc.driver.OracleDriver | 无 |
| source.datasource.username | 来源端连接帐号名 | tangyibo | 无 |
| source.datasource.password | 来源端连接帐号密码 | tangyibo | 无 |
| target.datasource.type     | 目的端连接池类型 | com.zaxxer.hikari.HikariDataSource |  默认即可 |
| target.datasource.jdbc-url | 目的端JDBC连接的URL | jdbc:postgresql://172.17.207.90:5432/study | 必须为PostgreSQL的jdbcurl |
| target.datasource.driver-class-name |目的端 数据库的驱动类名称 | org.postgresql.Driver | 必须值，不能修改 |
| target.datasource.username | 目的端连接帐号名 | study | 无 |
| target.datasource.password | 目的端连接帐号密码 | 123456 | 无 |
| source.datasource-fetch.size | 来源端数据库查询时的fetch_size设置 | 10000 | 需要大约1000有效 |
| source.datasource-source.schema | 来源端的schema名称 | ZFXFZB | 无 |
| source.datasource-source.excludes | 来源端schema下的表中需要过滤的表名称 | users,orgs | 不包含的表名称，多个之间用英文逗号分隔 |
| target.datasource-target.schema | 目的端的schema名称 | public | 无 |
| target.datasource-target.drop | 是否执行drop table命令 | true | 可选值为：true、false |

- mysql的配置样例

```
# source database connection information
source.datasource.type=com.zaxxer.hikari.HikariDataSource
source.datasource.jdbc-url= jdbc:mysql://172.17.207.210:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai
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
source.datasource-source.excludes=users,orgs

# target database configuration parameters
## schema name for create/insert table data
target.datasource-target.schema=public
## whether drop table where target database exist
target.datasource-target.drop=true
```

- oracle的配置样例

```
# source database connection information
source.datasource.type=com.zaxxer.hikari.HikariDataSource
source.datasource.jdbc-url= jdbc:oracle:thin:@172.17.207.158:1521:ORCL
source.datasource.driver-class-name= oracle.jdbc.driver.OracleDriver
source.datasource.username= ZFXFZB
source.datasource.password= ZFXFZB

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
source.datasource-source.schema=ZFXFZB
## table name exclude from table lists, 
source.datasource-source.excludes=ofuser_cas,organization_cas,t_demo_stu

# target database configuration parameters
## schema name for create/insert table data
target.datasource-target.schema=public
## whether drop table where target database exist
target.datasource-target.drop=true
```

- SqlServer的配置样例
```
# source database connection information
source.datasource.type=com.zaxxer.hikari.HikariDataSource
source.datasource.jdbc-url= jdbc:sqlserver://172.16.90.166:1433;DatabaseName=hqtest
source.datasource.driver-class-name= com.microsoft.sqlserver.jdbc.SQLServerDriver
source.datasource.username= hqtest
source.datasource.password= 123456

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
source.datasource-source.schema=dbo
## table name exclude from table lists, 
source.datasource-source.excludes=ofuser_cas,organization_cas,t_demo_stu

# target database configuration parameters
## schema name for create/insert table data
target.datasource-target.schema=public
## whether drop table where target database exist
target.datasource-target.drop=true
```

- PostgreSQL的配置样例
```
# source database connection information
source.datasource.type=com.zaxxer.hikari.HikariDataSource
source.datasource.jdbc-url= jdbc:postgresql://172.17.207.210:5432/tangyibo
source.datasource.driver-class-name= org.postgresql.Driver
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
source.datasource-source.schema= public
## table name exclude from table lists, 
source.datasource-source.excludes=ACT_GE_BYTEARRAY

# target database configuration parameters
## schema name for create/insert table data
target.datasource-target.schema=public
## whether drop table where target database exist
target.datasource-target.drop=true
```

启动执行命令如下：
```
cd dbswitch-release-0.0.1/
bin/datasync.sh
```

