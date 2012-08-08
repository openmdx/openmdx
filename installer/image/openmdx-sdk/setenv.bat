@echo off

set JAVA_HOME=$JDKPath
set ANT_HOME=$ANTPath
set ANT_OPTS=-Xmx512m 
set JRE_16=$JDKPath\jre
set THIRDPARTY_HOME=$INSTALL_PATH\openmdx-$PROJECT_VERSION\core\thirdparty
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%THIRDPARTY_HOME%\bin\windows;%PATH%
