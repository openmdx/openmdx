@echo off

rem START or STOP Tomcat
rem --------------------

set CATALINA_HOME=@@INSTALLDIR@@\apache-tomcat-6

if ""%1"" == ""START"" goto start
if ""%1"" == ""STOP"" goto stop
if ""%1"" == ""START-TOMCAT"" goto start-tomcat
goto end

:start
start "Tomcat EJB @@VERSION@@ (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\tomcat.bat" START-TOMCAT
goto end

:start-tomcat
set JAVA_HOME=@@JAVA_HOME@@
set JAVA_HOME=%JAVA_HOME:/=\%
set JAVA_OPTS=%JAVA_OPTS% -Xmx800M 
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=128m
set JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.openmdx.kernel.url.protocol
set JAVA_OPTS=%JAVA_OPTS% -Dorg.opencrx.maildir="%CATALINA_HOME%\maildir"
cd %CATALINA_HOME%
rmdir /s /q temp
rmdir /s /q work
mkdir temp
bin\catalina.bat run
goto end

:stop
taskkill -FI "WINDOWTITLE eq Tomcat EJB @@VERSION@@ (@@TOMCAT_HTTP_PORT@@)*"
goto end

:end

exit
