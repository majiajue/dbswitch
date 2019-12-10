#!/bin/sh

mvn clean
mvn package -f pom_deploy.xml -Dmaven.test.skip=true
