#!/bin/sh

export JAVA_HOME=$JDKPath
export ANT_HOME=$ANTPath
export ANT_OPTS=-Xmx512m 
export JRE_16=$JDKPath/jre
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
