/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConnectionFactory.java,v 1.2 2009/03/08 18:52:19 wfro Exp $
 * Description: LDAP Connection Factory 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.spi;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

import org.openmdx.resource.ldap.cci.Connection;

import netscape.ldap.LDAPException;

/**
 * LDAP Connection Factory
 */
public class ConnectionFactory
    implements org.openmdx.resource.ldap.cci.ConnectionFactory, Serializable, Referenceable
{

	/**
     * Constructor
     * 
     * @param managedConnectionFactory 
     * @param connectionManager
     */
    public ConnectionFactory(
        ManagedConnectionFactory managedConnectionFactory, 
        ConnectionManager connectionManager
    ) {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;        
    }

    /**
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = 6209042994072311135L;

    /**
     * 
     */
    private final ManagedConnectionFactory managedConnectionFactory;
    
    /**
     * 
     */
    private final ConnectionManager connectionManager;

    /**
     * 
     */
    private final ConnectionRequestInfo connectionRequestInfo = null;

    
    //------------------------------------------------------------------------
    // Implements ConnectionFactory
    //------------------------------------------------------------------------
    
    public Connection getConnection(
    ) throws LDAPException {
        try {
            return (Connection) this.connectionManager.allocateConnection(
                this.managedConnectionFactory, 
                this.connectionRequestInfo
            );
        } 
        catch (ResourceException exception) {
            throw (LDAPException) new LDAPException(
                "Connection handle acquisition failed",
                LDAPException.CONNECT_ERROR,
                exception.getMessage()
            ).initCause(
            	exception
            );
        }
    }


    //------------------------------------------------------------------------
    // Implements Referenceable
    //------------------------------------------------------------------------    

    /**
     * 
     */
    private Reference reference = null;
    
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
