/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract Connection Manager
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.resource.spi;

import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.openmdx.resource.cci.AuthenticationInfo;
import org.openmdx.resource.cci.NoConnectionRequestInfo;

/**
 * Abstract Connection Manager
 */
public abstract class AbstractConnectionManager
    implements ConnectionManager 
{

    /**
     * Constructor
     * 
     * @param credentials
     * 
     * @throws ResourceException
     */
    protected AbstractConnectionManager(
        Set<?> credentials
    ){
        this.credentials = credentials;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -6580404870568064926L;

    /**
     * @serial the Subject's private credentials
     */
    private final Set<?> credentials;
    
    /**
     * Convert an exception to a resource exception
     * 
     * @param cause
     * @param message
     * 
     * @return a resource exception with the  given exception linked to it
     */
    protected static ResourceException toResourceException(
        Exception cause,
        String message
    ){
        return new ResourceException(message, cause);
    }
        
    /**
     * Establish and return the subject
     * 
     * @return the subject with its private credentials set 
     */
    protected Subject getSubject(
    ){
    	final Subject subject = new Subject();
	    subject.getPrivateCredentials().addAll(this.credentials);
        return subject;
    }
    
    /**
     * Allocate a managed connection
     * 
     * @param subject
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * 
     * @return a (maybe newly created) managed connection
     * 
     * @throws ResourceException 
     */
    protected ManagedConnection allocateManagedConnection(
        Subject subject,
        ManagedConnectionFactory managedConnectionFactory, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        return managedConnectionFactory.createManagedConnection(
            subject,
            connectionRequestInfo
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionManager#allocateConnection(javax.resource.spi.ManagedConnectionFactory, javax.resource.spi.ConnectionRequestInfo)
     */
    public Object allocateConnection (
        ManagedConnectionFactory managedConnectionFactory,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        final Subject subject;
        final ConnectionRequestInfo managedConnectionrequestInfo;
        if(connectionRequestInfo instanceof AuthenticationInfo) {
            subject = new Subject();
            subject.getPrivateCredentials().add(
                PasswordCredentials.newPasswordCredential(
                    managedConnectionFactory, 
                    (AuthenticationInfo) connectionRequestInfo
                )
            );
            managedConnectionrequestInfo = new NoConnectionRequestInfo();
        } else {
            subject = getSubject();
            managedConnectionrequestInfo = connectionRequestInfo;
        }
        final ManagedConnection managedConnection = allocateManagedConnection(
            subject, 
            managedConnectionFactory, 
            managedConnectionrequestInfo
        );
        return managedConnection.getConnection(
            subject, 
            managedConnectionrequestInfo
        );
    }

}
