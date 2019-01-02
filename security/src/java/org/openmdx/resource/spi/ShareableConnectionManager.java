/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Shareable Connection Manager
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 200-2018, OMEX AG, Switzerland
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

import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;


/**
 * Shareable Connection Manager
 */
public class ShareableConnectionManager extends AbstractConnectionManager {

    /**
     * Constructor
     */
    public ShareableConnectionManager(
        Set<?> credentials
    ) {
        super(credentials);
    }

	/**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 2446478778635712446L;

    /**
     * The managed connections to be shared
     */
    private final Set<ManagedConnection> sharedConnections = new HashSet<ManagedConnection>();

    /* (non-Javadoc)
     * @see org.openmdx.kernel.resource.spi.AbstractConnectionManager#allocateManagedConnection(javax.security.auth.Subject, javax.resource.spi.ManagedConnectionFactory, javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
    protected ManagedConnection allocateManagedConnection(
        Subject subject,
        ManagedConnectionFactory managedConnectionFactory,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        synchronized(this.sharedConnections){
            ManagedConnection managedConnection = managedConnectionFactory.matchManagedConnections(
                this.sharedConnections,
                subject,
                connectionRequestInfo
            );
            if(managedConnection == null) {
                managedConnection = super.allocateManagedConnection(
                    subject,
                    managedConnectionFactory,
                    connectionRequestInfo
                );
                this.sharedConnections.add(managedConnection);
            }
            return managedConnection;        
        }            
    }

}
