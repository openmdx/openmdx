#!/bin/sh

export CATALINA_HOME=@@INSTALLDIR@@/apache-tomcat-6

# START or STOP Tomcat
# --------------------

if [ "$1" = "START" ] ; then

  export JAVA_OPTS="-Xmx800M -XX:MaxPermSize=128m -Dtomcat.server.port=@@TOMCAT_SERVER_PORT@@ -Djava.protocol.handler.pkgs=org.openmdx.kernel.url.protocol -Dorg.openmdx.log.config.filename=@@INSTALLDIR@@/apache-tomcat-6/server.log.properties -Dorg.opencrx.maildir=@@INSTALLDIR@@/apache-tomcat-6/maildir"
  cd $CATALINA_HOME
  rm -Rf temp
  mkdir temp
  rm -Rf work
  ./bin/catalina.sh run

fi

if [ "$1" = "STOP" ] ; then

  @@INSTALLDIR@@/bin/zap.sh 'tomcat.server.port=@@TOMCAT_SERVER_PORT@@'

fi
