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
  export LOGGING_MANAGER="-Djava.util.logging.manager=java.util.logging.LogManager"  
  cd $CATALINA_HOME
  rm -Rf temp
  mkdir temp
  rm -Rf work
  
  if [ -e @@INSTALLDIR@@/bin/webapp01.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp01.sh START
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp02.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp02.sh START
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp03.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp03.sh START
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp04.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp04.sh START
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp05.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp05.sh START
  fi
     
  ./bin/catalina.sh run

fi

if [ "$1" = "STOP" ] ; then

  if [ -e @@INSTALLDIR@@/bin/webapp01.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp01.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp02.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp02.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp03.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp03.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp04.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp04.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/webapp05.sh ] ; then
  	. @@INSTALLDIR@@/bin/webapp05.sh STOP
  fi

  pkill -f '.*tomcat\.server\.port=@@TOMCAT_SERVER_PORT@@.*'

fi
