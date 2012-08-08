@echo off

set JAVA_HOME=@@JAVA_HOME@@
set JAVA_HOME=%JAVA_HOME:/=\%

rem START or STOP Services
rem ----------------------
rem Check if argument is STOP or START

if ""%1"" == ""STOP"" goto stop

:start-services
if exist "@@INSTALLDIR@@\bin\000S.bat" (
  start "Tomcat LWC @@VERSION@@ Service 000 (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\000S.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\001S.bat" (
  start "Tomcat LWC @@VERSION@@ Service 001 (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\001S.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\002S.bat" (
  start "Tomcat LWC @@VERSION@@ Service 002 (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\002S.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\003S.bat" (
  start "Tomcat LWC @@VERSION@@ Service 003 (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\003S.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\004S.bat" (
  start "Tomcat LWC @@VERSION@@ Service 004 (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\004S.bat" START
  ping 127.0.0.1 > nul  
)
if exist "@@INSTALLDIR@@\bin\005S.bat" (
  start "Tomcat LWC @@VERSION@@ Service 005 (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\005S.bat" START
  ping 127.0.0.1 > nul  
)

if ""%1"" == ""START-SERVICES"" goto end

:start-tomcat
start "Tomcat LWC @@VERSION@@ (@@TOMCAT_HTTP_PORT@@)" "@@INSTALLDIR@@\bin\tomcat.bat" START

goto end

:stop

if exist "@@INSTALLDIR@@\bin\005S.bat" (
  taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ Service 005 (@@TOMCAT_HTTP_PORT@@)*"
)
if exist "@@INSTALLDIR@@\bin\004S.bat" (
  taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ Service 004 (@@TOMCAT_HTTP_PORT@@)*"
)
if exist "@@INSTALLDIR@@\bin\003S.bat" (
  taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ Service 003 (@@TOMCAT_HTTP_PORT@@)*"
)
if exist "@@INSTALLDIR@@\bin\002S.bat" (
  taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ Service 002 (@@TOMCAT_HTTP_PORT@@)*"
)
if exist "@@INSTALLDIR@@\bin\001S.bat" (
  taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ Service 001 (@@TOMCAT_HTTP_PORT@@)*"
)
if exist "@@INSTALLDIR@@\bin\000S.bat" (
  taskkill -FI "WINDOWTITLE eq Tomcat LWC @@VERSION@@ Service 000 (@@TOMCAT_HTTP_PORT@@)*"
)

start "Stop Tomcat" "@@INSTALLDIR@@\bin\tomcat.bat" STOP

:end
