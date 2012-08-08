/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Bean.java,v 1.1 2005/07/11 23:35:56 hburger Exp $
 * Description: A Dataprovider Service for WAS
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/11 23:35:56 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.application.dataprovider.transport.ejb.websphere;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.DbConnectionManager_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.kernel.log.SysLog;

/**
 * A Dataprovider Service for IBM WebSphere Application Server
 */
public class Dataprovider_1Bean
	extends org.openmdx.compatibility.application.dataprovider.transport.ejb.server.Dataprovider_1Bean 
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3763097470548587058L;

    /**
     * WAS 5.0 FP 4 Patch 2004-06-15, D. Mueller: use synchronized access to lookup a DataSource.
     */
    protected void getDataSources(
        Configuration dataproviderConfiguration
    ) throws ServiceException {
        synchronized(javax.naming.Context.class) {
            SysLog.trace("WAS 5.0 FP 4 Patch: synchronized DataSource lookup ...");
            super.getDataSources(dataproviderConfiguration);
            SysLog.trace("WAS 5.0 FP 4 Patch: synchronized DataSource lookup done");
        }
    }

    /**
     * Database Connection Manager Factory
     * 
     * @param jndiName the connection managers JNDI name
     * 
     * @return a DB connection Manager
     * 
     * @throws ServiceException
     */
    protected DbConnectionManager_1_0 getDatabaseConnectionManager(
        String jndiName
    ) throws ServiceException{
        return new DbConnectionManagerPool_1(jndiName);
    }
        
}
