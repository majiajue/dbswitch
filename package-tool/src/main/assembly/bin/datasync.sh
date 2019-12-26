#!/usr/bin/env bash
#############################################
# !!!!!! Modify here please

DS_MAIN="com.weishao.dbswitch.data.DataSyncApplication"

#############################################

DS_HOME="${BASH_SOURCE-$0}"
DS_HOME="$(dirname "${DS_HOME}")"
DS_HOME="$(cd "${DS_HOME}"; pwd)"
DS_HOME="$(cd "$(dirname ${DS_HOME})"; pwd)"
#echo "Base Directory:${DS_HOME}"

DS_BIN_PATH=$DS_HOME/bin
DS_LIB_PATH=$DS_HOME/lib
DS_CONF_PATH=$DS_HOME/conf

# JVMFLAGS JVM参数可以在这里设置
JVMFLAGS="-Dfile.encoding=UTF-8 -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi

#把lib下的所有jar都加入到classpath中
CLASSPATH=$DS_CONF_PATH
for i in $DS_LIB_PATH/*.jar
do
	CLASSPATH="$i:$CLASSPATH"
done

res=`ps aux|grep java|grep $DS_HOME|grep $DS_MAIN|grep -v grep|awk '{print $2}'`
if [ -n "$res"  ]; then
        echo "$res program is already running"
        exit 1
else
        $JAVA -cp $CLASSPATH $JVMFLAGS $DS_MAIN $DS_CONF_PATH
fi
