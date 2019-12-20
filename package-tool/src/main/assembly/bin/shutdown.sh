#!/usr/bin/env bash
#############################################
# !!!!!! Modify here please

DS_PROG="dbswitch-webapi-0.0.1.jar"

#############################################

DS_HOME="${BASH_SOURCE-$0}"
DS_HOME="$(dirname "${DS_HOME}")"
DS_HOME="$(cd "${DS_HOME}"; pwd)"
DS_HOME="$(cd "$(dirname ${DS_HOME})"; pwd)"
#echo "Base Directory:${DS_HOME}"

DS_BIN_PATH=$DS_HOME/bin
DS_LIB_PATH=$DS_HOME/lib
DS_CONF_PATH=$DS_HOME/conf

PIDS=`ps -ef | grep java | grep "$DS_HOME" | grep "$DS_PROG" |awk '{print $2}'`
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
