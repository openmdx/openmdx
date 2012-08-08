#!/bin/sh
cd "$INSTALL_PATH"
export ANT_HOME=$ANTPath
export JAVA_HOME=$JDKPath
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
ant -f "$INSTALL_PATH/bin/postinstaller.xml" -logfile "$INSTALL_PATH/build.log" postinstall
