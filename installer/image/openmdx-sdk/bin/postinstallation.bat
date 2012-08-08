@echo off
set ANT_HOME=$ANTPath
set JAVA_HOME=$JDKPath
set THIRDPARTY_HOME=$INSTALL_PATH\openmdx-$PROJECT_VERSION\core\thirdparty
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%THIRDPARTY_HOME%\bin\windows;%PATH%
start /WAIT "Build openMDX SDK" cmd /C "ant -f ""$INSTALL_PATH\bin\postinstaller.xml"" -logfile ""$INSTALL_PATH\build.log"" postinstall"
