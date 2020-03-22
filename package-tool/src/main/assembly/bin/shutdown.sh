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

PIDS=`ps -ef | grep java | grep "$APP_HOME" | grep "$APP_MAIN" |awk '{print $2}'`
if [ -z "$PIDS" ]; then
        echo "ERROR: The server does not started!"
        exit 1
fi

echo -e "Stopping the server ...\c"
for PID in $PIDS ; do
        kill $PID > /dev/null 2>&1
done

COUNT=0
while [ $COUNT -lt 1 ]; do
        echo -e ".\c"
        sleep 1
        COUNT=1
        for PID in $PIDS ; do
                PID_EXIST=`ps -f -p $PID | grep java`
                if [ -n "$PID_EXIST" ]; then
                        COUNT=0
                        break
                fi
        done
done

echo "OK!"
echo "PID: $PIDS"
