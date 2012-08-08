@echo off
"@@INSTALLDIR@@\bin\7z.exe" x -o"@@INSTALLDIR@@" "@@INSTALLDIR@@\repository\distribution\openmdx-@@OPENMDX_VERSION@@-core.jre-1.5.zip" 
"@@INSTALLDIR@@\bin\7z.exe" x -o"@@INSTALLDIR@@" "@@INSTALLDIR@@\repository\distribution\openmdx-@@OPENMDX_VERSION@@-portal.jre-1.5"
"@@INSTALLDIR@@\bin\7z.exe" x -o"@@INSTALLDIR@@" "@@INSTALLDIR@@\repository\distribution\openmdx-@@OPENMDX_VERSION@@-security.jre-1.5"
"@@INSTALLDIR@@\bin\7z.exe" x -o"@@INSTALLDIR@@" "@@INSTALLDIR@@\repository\distribution\openmdx-@@OPENMDX_VERSION@@-tomcat.tomcat-6"
"@@INSTALLDIR@@\bin\7z.exe" x -o"@@INSTALLDIR@@" "@@INSTALLDIR@@\repository\distribution\openmdx-@@OPENMDX_VERSION@@-websphere.websphere-6"
set ANT_HOME=@@ANT_HOME@@
set JAVA_HOME=@@JAVA_HOME@@
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%
cd "@@INSTALLDIR@@\openmdx-@@OPENMDX_VERSION@@\core"
cmd /C ant install-src
cd "@@INSTALLDIR@@\openmdx-@@OPENMDX_VERSION@@\portal"
cmd /C ant install-src
cd "@@INSTALLDIR@@\openmdx-@@OPENMDX_VERSION@@\security"
cmd /C ant install-src
cd "@@INSTALLDIR@@\openmdx-@@OPENMDX_VERSION@@\tomcat"
cmd /C ant install-src
cd "@@INSTALLDIR@@\openmdx-@@OPENMDX_VERSION@@\websphere"
cmd /C ant install-src
