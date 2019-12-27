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
source.datasource-source.excludes=ofuser_cas

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

