#!/bin/sh

export JAVA_HOME=@@JAVA_HOME@@
export ANT_HOME=@@ANT_HOME@@
export ANT_OPTS=-Xmx512m 
export JRE_15=@@JAVA_HOME@@/jre
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
