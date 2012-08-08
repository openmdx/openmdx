#!/bin/sh
gunzip @@INSTALLDIR@@/repository/distribution/*.gz
cd "@@INSTALLDIR@@"
tar xf "@@INSTALLDIR@@/repository/distribution/openmdx-@@OPENMDX_VERSION@@-core.jre-1.5.tar"
tar xf "@@INSTALLDIR@@/repository/distribution/openmdx-@@OPENMDX_VERSION@@-portal.jre-1.5.tar"
tar xf "@@INSTALLDIR@@/repository/distribution/openmdx-@@OPENMDX_VERSION@@-security.jre-1.5.tar"
tar xf "@@INSTALLDIR@@/repository/distribution/openmdx-@@OPENMDX_VERSION@@-tomcat.tomcat-6.tar"
tar xf "@@INSTALLDIR@@/repository/distribution/openmdx-@@OPENMDX_VERSION@@-websphere.websphere-6.tar"
export ANT_HOME=@@ANT_HOME@@
export JAVA_HOME=@@JAVA_HOME@@
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH
cd "@@INSTALLDIR@@/openmdx-@@OPENMDX_VERSION@@/core"
ant install-src
cd "@@INSTALLDIR@@/openmdx-@@OPENMDX_VERSION@@/portal"
ant install-src
cd "@@INSTALLDIR@@/openmdx-@@OPENMDX_VERSION@@/security"
ant install-src
cd "@@INSTALLDIR@@/openmdx-@@OPENMDX_VERSION@@/tomcat"
ant install-src
cd "@@INSTALLDIR@@/openmdx-@@OPENMDX_VERSION@@/websphere"
ant install-src
