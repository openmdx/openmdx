/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagedConnectionFactory_2.java,v 1.4 2008/03/28 17:17:50 hburger Exp $
 * Description: JDO Managed Connection Factory 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/28 17:17:50 $
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * Managed Connection Factory
 */
public class ManagedConnectionFactory_2 implements ManagedConnectionFactory {
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1882357331093514181L;

    /**
     * 
     */
    private final Map<String,Object> properties = new HashMap<String,Object>();
    
    /**
     * 
     */
    private PrintWriter logWriter;
        
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public Object createConnectionFactory(
    ) throws ResourceException {
        return new ConnectionFactory_2(this, properties);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    public Object createConnectionFactory(
        ConnectionManager cxManager
    ) throws ResourceException {
        return createConnectionFactory();
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo cxRequestInfo
    ) throws ResourceException {
        if(this.logWriter != null) this.logWriter.print("!!!createManagedConnection!!!");
        return null;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
     */
    public final PrintWriter getLogWriter(
    ) throws ResourceException {
        return this.logWriter;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    @SuppressWarnings("unchecked")
    public ManagedConnection matchManagedConnections(
        Set connectionSet,
        Subject subject,
        ConnectionRequestInfo cxRequestInfo
    ) throws ResourceException {
        if(this.logWriter != null) this.logWriter.print("!!!matchManagedConnection!!!");
        return null;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    public final void setLogWriter(
        PrintWriter out
    ) throws ResourceException {
        this.logWriter = out;
    }

    /**
     * Retrieve the accessor factory classs.
     *
     * @return Returns the accessor factory classs.
     */
    public String getAccessorFactoryClass() {
        return this.properties.get(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName()
        ).toString();
    }

    /**
     * Set the accessor factory classs.
     * 
     * @param accessorFactoryClass The accessor factory classs to set.
     */
    public void setAccessorFactoryClass(
        String accessorFactoryClass
    ) {
        this.properties.put(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(), 
            accessorFactoryClass
        );
    }
    
    /**
     * Retrieve connectionFactoryName.
     *
     * @return Returns the connectionFactoryName.
     */
    public String getConnectionFactoryName() {
        return this.properties.get(
            ConfigurableProperty.ConnectionFactoryName.qualifiedName()
        ).toString();
    }

    /**
     * Set connectionFactoryName.
     * 
     * @param connectionFactoryName The connectionFactoryName to set.
     */
    public void setConnectionFactoryName(
        String connectionFactoryName
    ) {
        this.properties.put(
            ConfigurableProperty.ConnectionFactoryName.qualifiedName(), 
            connectionFactoryName
        );
    }
    
    /**
     * Retrieve nontransactionalRead.
     *
     * @return Returns the nontransactionalRead.
     */
    public Boolean isNontransactionalRead() {
        return Boolean.valueOf(
            this.properties.get(
                ConfigurableProperty.NontransactionalRead.qualifiedName()
            ).toString()
        );
    }

    /**
     * Set nontransactionalRead.
     * 
     * @param nontransactionalRead The nontransactionalRead to set.
     */
    public void setNontransactionalRead(
        Boolean nontransactionalRead
    ) {
        this.properties.put(
            ConfigurableProperty.NontransactionalRead.qualifiedName(), 
            nontransactionalRead.toString()
        );
    }

    /**
     * Retrieve optimistic.
     *
     * @return Returns the optimistic.
     */
    public Boolean isOptimistic() {
        return Boolean.valueOf(
            this.properties.get(
                ConfigurableProperty.Optimistic.qualifiedName()
            ).toString()
        );
    }

    /**
     * Set optimistic.
     * 
     * @param optimistic The optimistic to set.
     */
    public void setOptimistic(
        Boolean optimistic
    ) {
        this.properties.put(
            ConfigurableProperty.Optimistic.qualifiedName(), 
            optimistic.toString()
        );
    }
    
}
