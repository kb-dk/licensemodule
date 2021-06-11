#!/usr/bin/env bash

cd /tmp/src

cp -rp -- /tmp/src/target/licensemodule-*.war "$TOMCAT_APPS/licensemodule.war"
cp -- /tmp/src/conf/ocp/licensemodule.xml "$TOMCAT_APPS/licensemodule.xml"

export WAR_FILE=$(readlink -f "$TOMCAT_APPS/licensemodule.war")
