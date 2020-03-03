# 引用说明

## 代码来源

该模块代码源自开源的PgBulkInsert,其介绍如下：

> PgBulkInsert is a Java library for Bulk Inserts with PostgreSQL.

作者博客：https://www.bytefish.de/blog/pgbulkinsert_row_writer/

源码地址：http://www.github.com/bytefish/PgBulkInsert

## 改进点

- 为copy语句中表名与字段名的引号，解决大小写敏感问题与关键词冲突问题；

- 修改了数据类型以支持写入的字段值为null的情况

