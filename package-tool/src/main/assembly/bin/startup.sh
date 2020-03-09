#!/usr/bin/env bash
#
# Author : tang
# Date :2019-12-16
#
#############################################
# !!!!!! Modify here please

APP_MAIN="com.weishao.dbswitch.webapi.WebServiceApplication"

#############################################

APP_HOME="${BASH_SOURCE-$0}"
APP_HOME="$(dirname "${APP_HOME}")"
APP_HOME="$(cd "${APP_HOME}"; pwd)"
APP_HOME="$(cd "$(dirname ${APP_HOME})"; pwd)"
#echo "Base Directory:${APP_HOME}"

APP_BIN_PATH=$APP_HOME/bin
APP_LIB_PATH=$APP_HOME/lib
APP_CONF_PATH=$APP_HOME/conf

APP_PID_FILE="${APP_HOME}/run/${APP_MAIN}.pid"
APP_RUN_LOG="${APP_HOME}/run/run_${APP_MAIN}.log"

[ -d "${APP_HOME}/run" ] || mkdir -p "${APP_HOME}/run"
cd ${APP_HOME}

echo -n `date +'%Y-%m-%d %H:%M:%S'`              >>${APP_RUN_LOG}
echo "---- Start service [${APP_MAIN}] process. ">>${APP_RUN_LOG}

# JVMFLAGS JVM参数可以在这里设置
JVMFLAGS="-Dfile.encoding=UTF-8 -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:./gc.log"

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
fi

nohup $JAVA -cp $CLASSPATH $JVMFLAGS $APP_MAIN $APP_CONF_PATH >>${APP_RUN_LOG} 2>&1 &
#echo "$JAVA -cp $CLASSPATH $JVMFLAGS $APP_MAIN $APP_CONF_PATH >>${APP_RUN_LOG} 2>&1 &"

RETVAL=$?
PID=$!

echo ${PID} >${APP_PID_FILE}
exit ${RETVAL}

