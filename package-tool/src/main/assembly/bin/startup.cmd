::	
:: Author: tang	
:: Date: 2020-03-06
::	
@echo off
title dbswitch
setlocal enabledelayedexpansion
cls

::需要启动的Java类
set APP_MAINCLASS=com.weishao.dbswitch.webapi.WebServiceApplication

::Java应用根目录
set APP_HOME=%~dp0
set APP_HOME=%APP_HOME%\..\
cd %APP_HOME%
set APP_HOME=%cd%

set APP_BIN_PATH=%APP_HOME%\bin
set APP_LIB_PATH=%APP_HOME%\lib
set APP_CONF_PATH=%APP_HOME%\conf

::classpath参数，包括指定conf目录下所有的配置
set CLASSPATH=%APP_CONF_PATH%
For /r "%APP_HOME%\lib" %%f in (*.jar) do (
	set CLASSPATH=!CLASSPATH!;%%f
)

::java虚拟机启动参数
set JAVA_OPTS=-server -Xms1024m -Xmx1024m -Xmn512m -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Djava.awt.headless=true -Dfile.encoding=UTF-8 

::打印环境信息
echo System Information:
echo ********************************************************
echo COMPUTERNAME=%COMPUTERNAME%
echo OS=%OS%
echo.
echo APP_HOME=%APP_HOME%
echo APP_MAINCLASS=%APP_MAINCLASS%
echo CLASSPATH=%CLASSPATH%
echo CURRENT_DATE=%date% %time%:~0,8%
echo ********************************************************

::执行java
echo Starting %APP_MAINCLASS% ...
echo java -classpath %APP_CONF_PATH%;%APP_LIB_PATH%\* %JAVA_OPTS% %APP_MAINCLASS%
echo .
java -classpath %APP_CONF_PATH%;%APP_LIB_PATH%\* %JAVA_OPTS% %APP_MAINCLASS%

:exit
pause
