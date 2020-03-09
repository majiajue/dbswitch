#!/usr/bin/env bash
#
# Author : tang
# Date :2019-12-16
#
#############################################
# !!!!!! Modify here please

APP_MAIN="com.weishao.dbswitch.data.DataSyncApplication"

#############################################

APP_HOME="${BASH_SOURCE-$0}"
APP_HOME="$(dirname "${APP_HOME}")"
APP_HOME="$(cd "${APP_HOME}"; pwd)"
APP_HOME="$(cd "$(dirname ${APP_HOME})"; pwd)"
#echo "Base Directory:${APP_HOME}"

APP_BIN_PATH=$APP_HOME/bin
APP_LIB_PATH=$APP_HOME/lib
APP_CONF_PATH=$APP_HOME/conf

# JVMFLAGS JVM参数可以在这里设置
JVMFLAGS="-Xms1024m -Xmx1024m -Xmn512m -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Dfile.encoding=UTF-8 "

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

#把lib下的所有jar都加入到classpath中
CLASSPATH=$APP_CONF_PATH
for i in $APP_LIB_PATH/*.jar
do
	CLASSPATH="$i:$CLASSPATH"
done

res=`ps aux|grep java|grep $APP_HOME|grep $APP_MAIN|grep -v grep|awk '{print $2}'`
if [ -n "$res"  ]; then
        echo "$res program is already running"
        exit 1
else
        $JAVA -cp $CLASSPATH $JVMFLAGS $APP_MAIN $APP_CONF_PATH
fi
