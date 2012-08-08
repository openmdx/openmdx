#!/bin/sh
export ANT_HOME=@@ANT_HOME@@
export JAVA_HOME=@@JAVA_HOME@@
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
ant -f "@@INSTALLDIR@@/installer/openmdx-tomcat-ejb/bin/postinstaller.xml" postinstall
