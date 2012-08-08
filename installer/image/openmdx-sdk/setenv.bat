@echo off

set JAVA_HOME=@@JAVA_HOME@@
set ANT_HOME=@@ANT_HOME@@
set ANT_OPTS=-Xmx512m 
set JRE_15=@@JAVA_HOME@@\jre
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%
