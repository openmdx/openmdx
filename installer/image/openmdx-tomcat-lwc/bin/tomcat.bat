rem START or STOP Tomcat
rem --------------------

set CATALINA_HOME=@@INSTALLDIR@@\apache-tomcat-6

if not ""%1"" == ""START"" goto stop

set JAVA_OPTS=-Xmx800M 
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=128m
set JAVA_OPTS=%JAVA_OPTS% -Djava.protocol.handler.pkgs=org.openmdx.kernel.url.protocol
set JAVA_OPTS=%JAVA_OPTS% -Dorg.openmdx.log.config.filename="%CATALINA_HOME%\server.log.properties"
set JAVA_OPTS=%JAVA_OPTS% -Dorg.opencrx.maildir="%CATALINA_HOME%\maildir"
cd %CATALINA_HOME%
rmdir /s /q temp
rmdir /s /q work
mkdir temp
bin\catalina.bat run

goto end

:stop

taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ (@@TOMCAT_HTTP_PORT@@)*"

:end

exit
