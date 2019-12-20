#!/bin/sh

mvn clean -f pom.xml
mvn package -f pom.xml -Dmaven.test.skip=true
