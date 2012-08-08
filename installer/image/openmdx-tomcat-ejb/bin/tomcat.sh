#!/bin/sh

export CATALINA_HOME=@@INSTALLDIR@@/apache-tomcat-6

# START or STOP Tomcat
# --------------------

if [ "$1" = "START" ] ; then

  export JAVA_HOME=@@JAVA_HOME@@
  export JAVA_OPTS="$JAVA_OPTS -Xmx800M"
  export JAVA_OPTS="$JAVA_OPTS -XX:MaxPermSize=128m"
  export JAVA_OPTS="$JAVA_OPTS -Dtomcat.server.port=@@TOMCAT_SERVER_PORT@@"
  export JAVA_OPTS="$JAVA_OPTS -Djava.protocol.handler.pkgs=org.openmdx.kernel.url.protocol"
  export JAVA_OPTS="$JAVA_OPTS -Dorg.openmdx.log.config.filename=@@INSTALLDIR@@/apache-tomcat-6/server.log.properties"
  export JAVA_OPTS="$JAVA_OPTS -Dorg.opencrx.maildir=@@INSTALLDIR@@/apache-tomcat-6/maildir"
  cd $CATALINA_HOME
  rm -Rf temp
  mkdir temp
  rm -Rf work
  ./bin/catalina.sh run

fi

if [ "$1" = "STOP" ] ; then

  pkill -f '.*tomcat\.server\.port=@@TOMCAT_SERVER_PORT@@.*'

fi
