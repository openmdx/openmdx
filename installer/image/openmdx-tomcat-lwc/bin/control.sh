#!/bin/sh

export JAVA_HOME=@@JAVA_HOME@@

# START or STOP Services
# ----------------------
# Check if argument is STOP or START

if [ "$1" = "START" -o "$1" = "START-SERVICES" ] ; then

  if [ -e @@INSTALLDIR@@/bin/000S.sh ] ; then 
    @@INSTALLDIR@@/bin/000S.sh START &
    sleep 3
  fi
  if [ -e @@INSTALLDIR@@/bin/001S.sh ] ; then 
    @@INSTALLDIR@@/bin/001S.sh START &
    sleep 3
  fi
  if [ -e @@INSTALLDIR@@/bin/002S.sh ] ; then 
    @@INSTALLDIR@@/bin/002S.sh START &
    sleep 3
  fi  
  if [ -e @@INSTALLDIR@@/bin/003S.sh ] ; then 
    @@INSTALLDIR@@/bin/003S.sh START &
    sleep 3
  fi  
  if [ -e @@INSTALLDIR@@/bin/004S.sh ] ; then 
    @@INSTALLDIR@@/bin/004S.sh START &
    sleep 3
  fi  
  if [ -e @@INSTALLDIR@@/bin/005S.sh ] ; then 
    @@INSTALLDIR@@/bin/005S.sh START &
    sleep 3
  fi  

fi

if [ "$1" = "START" ] ; then

  if [ -e @@INSTALLDIR@@/bin/tomcat.sh ] ; then 
    @@INSTALLDIR@@/bin/tomcat.sh START
  fi

fi

if [ "$1" = "STOP" ] ; then

  if [ -e @@INSTALLDIR@@/bin/tomcat.sh ] ; then
    @@INSTALLDIR@@/bin/tomcat.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/005S.sh ] ; then 
    @@INSTALLDIR@@/bin/005S.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/004S.sh ] ; then 
    @@INSTALLDIR@@/bin/004S.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/003S.sh ] ; then 
    @@INSTALLDIR@@/bin/003S.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/002S.sh ] ; then 
    @@INSTALLDIR@@/bin/002S.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/001S.sh ] ; then 
    @@INSTALLDIR@@/bin/001S.sh STOP
  fi
  if [ -e @@INSTALLDIR@@/bin/000S.sh ] ; then 
    @@INSTALLDIR@@/bin/000S.sh STOP
  fi
    
fi
