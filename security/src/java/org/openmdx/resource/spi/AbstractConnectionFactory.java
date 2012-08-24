/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Abstract Connection Factory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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
package org.openmdx.resource.spi;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;

/**
 * Abstract Connection Factory
 */
public class AbstractConnectionFactory <C> implements Serializable, Referenceable {

	/**
     * Constructor
     * 
     * @param managedConnectionFactory 
     * @param connectionManager
     */
    protected AbstractConnectionFactory(
        AbstractManagedConnectionFactory managedConnectionFactory, 
        ConnectionManager connectionManager
    ) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = 
        	connectionManager == null ? this.managedConnectionFactory.getConnectionManager() : 
        	connectionManager;    
    }

	/**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 717396893019818237L;

    /**
     * 
     */
    private final AbstractManagedConnectionFactory managedConnectionFactory;
    
    /**
     * 
     */
    private final ConnectionManager connectionManager;
	
    /**
     * 
     */
    private Reference reference = null;

    /**
     * Create a new connection
     * 
     * @param connection request info
     * 
     * @return a new connection
     * @throws ResourceException 
     */
    @SuppressWarnings("unchecked")
    protected C newConnection(
    	ConnectionRequestInfo connectionRquestInfo
    ) throws ResourceException{
    	return (C) this.connectionManager.allocateConnection(
            this.managedConnectionFactory, 
            connectionRquestInfo
        );
    }
    
    //------------------------------------------------------------------------
    // Implements Referenceable
    //------------------------------------------------------------------------    

    /* (non-Javadoc)
     * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference(
    ) throws NamingException {
        return this.reference;
    }

}
