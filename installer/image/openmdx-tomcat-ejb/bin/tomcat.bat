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
set LOGGING_MANAGER=-Djava.util.logging.manager=java.util.logging.LogManager
cd %CATALINA_HOME%
rmdir /s /q temp
rmdir /s /q work
mkdir temp

if exist "@@INSTALLDIR@@\bin\webapp01.bat" (
  call "@@INSTALLDIR@@\bin\webapp01.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp02.bat" (
  call "@@INSTALLDIR@@\bin\webapp02.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp03.bat" (
  call "@@INSTALLDIR@@\bin\webapp03.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp04.bat" (
  call "@@INSTALLDIR@@\bin\webapp04.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp05.bat" (
  call "@@INSTALLDIR@@\bin\webapp05.bat" START
  ping 127.0.0.1 > nul  
)

bin\catalina.bat run
goto end

:stop
if exist "@@INSTALLDIR@@\bin\webapp01.bat" (
  call "@@INSTALLDIR@@\bin\webapp01.bat" STOP
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp02.bat" (
  call "@@INSTALLDIR@@\bin\webapp02.bat" STOP
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp03.bat" (
  call "@@INSTALLDIR@@\bin\webapp03.bat" STOP
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp04.bat" (
  call "@@INSTALLDIR@@\bin\webapp04.bat" STOP
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\webapp05.bat" (
  call "@@INSTALLDIR@@\bin\webapp05.bat" STOP
  ping 127.0.0.1 > nul  
)
taskkill -FI "WINDOWTITLE eq Tomcat EJB @@VERSION@@ (@@TOMCAT_HTTP_PORT@@)*"
goto end

:end

exit
