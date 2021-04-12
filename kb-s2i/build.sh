#!/usr/bin/env bash

cd /tmp/src

cp -rp -- /tmp/src/target/pdf-service-*.war "$TOMCAT_APPS/pdf-service.war"
cp -- /tmp/src/conf/ocp/pdf-service.xml "$TOMCAT_APPS/pdf-service.xml"

export WAR_FILE=$(readlink -f "$TOMCAT_APPS/pdf-service.war")
