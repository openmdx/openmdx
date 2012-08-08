/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConnectionFactory_2.java,v 1.5 2008/06/09 17:31:27 hburger Exp $
 * Description: Accessor Factory
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/09 17:31:27 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.persistence.resource;

import java.io.PrintWriter;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;

import org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory;

/**
 * Accessor Factory
 */
public class ConnectionFactory_2 
    extends DelegatingPersistenceManagerFactory 
{

    /**
     * Constructor 
     *
     * @param managedConnectionFactory
     * @param properties
     */
    ConnectionFactory_2(
        ManagedConnectionFactory managedConnectionFactory,
        Map<String,Object> properties
    ) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.properties = properties;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7598687918919661756L;

    /**
     * 
     */
    private final Map<String,Object> properties;

    /**
     * 
     */
    private PersistenceManagerFactory delegate = null;

    /**
     * The managed connection factory
     */
    private final ManagedConnectionFactory managedConnectionFactory;
    
    /**
     * Retrieve the delegate persistence manager factory.
     * 
     * @return the delegate persistence manager factory
     */
    protected PersistenceManagerFactory delegate(){
        if(this.delegate == null) {
            PrintWriter logWriter;
            try {
                logWriter = this.managedConnectionFactory.getLogWriter();
            } catch (ResourceException ignored) {
                logWriter = null;
            }
            try {
                this.delegate = JDOHelper.getPersistenceManagerFactory(
                    this.properties
                );
                if(logWriter != null) logWriter.println(
                    "PersistenceManagerFactory acquired: " + this.properties
                );
            } catch (RuntimeException exception) {
                if(logWriter != null) exception.printStackTrace(
                    logWriter
                );
            }
        }
        return this.delegate;
    }
    
}
