@echo off
set ANT_HOME=@@ANT_HOME@@
set JAVA_HOME=@@JAVA_HOME@@
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%
ant -f "@@INSTALLDIR@@\installer\openmdx-tomcat-ejb\bin\postinstaller.xml" postinstall
