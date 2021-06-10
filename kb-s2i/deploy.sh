#!/usr/bin/env bash

cp -- /tmp/src/conf/ocp/licensemodule.logback.xml "$CONF_DIR/licensemodule.logback.xml"
cp -- /tmp/src/conf/license_module.properties "$CONF_DIR/license_module.properties"
 
ln -s -- "$TOMCAT_APPS/licensemodule.xml" "$DEPLOYMENT_DESC_DIR/licensemodule.xml"
