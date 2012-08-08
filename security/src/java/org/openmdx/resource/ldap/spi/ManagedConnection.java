/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagedConnection.java,v 1.2 2008/07/24 17:24:03 hburger Exp $
 * Description: Managed LDAP Connection 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/07/24 17:24:03 $
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

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;


/**
 * Managed LDAP Connection
 */
public class ManagedConnection 
    implements javax.resource.spi.ManagedConnection
{

    /**
     * Constructor 
     *
     * @param credential
     * @param certificate
     * @param key
     */
    public ManagedConnection(
    	LDAPv3 physicalConnection,
        PasswordCredential credential
    ) {
    	this.ldapConnection = physicalConnection;
        this.credential = credential;
    }

    /**
     * 
     */
    private LDAPv3 ldapConnection;
    
    /**
     * Lazy initialized meta data
     */
    private ManagedConnectionMetaData metaData = null;

    /**
     * 
     */
    private PasswordCredential credential;

    /**
     * 
     */
    private PrintWriter logWriter = null;

    /**
     * 
     */
    private final Set<ConnectionEventListener> listeners = new HashSet<ConnectionEventListener>();
    
    /**
     * 
     */
    private final Set<Connection> connections = new HashSet<Connection>();
    
    /**
     * 
     */
    private final static String NON_TRANSACTIONAL = "KeyStore resources are non-transactional";
    
    
    /**
     * Used by LDAPConnection
     * 
     * @return
     */
    LDAPv3 getPhysicalConnection(
    ){
    	return this.ldapConnection;
    }
    
	void signalClose(
		Connection connection
	){
    	ConnectionEvent event = new ConnectionEvent(
    		this,
    		ConnectionEvent.CONNECTION_CLOSED
    	);
    	event.setConnectionHandle(connection);
    	for(ConnectionEventListener listener : this.listeners) {
			listener.connectionClosed(event);
    	}
    }
    
    void signalException(
		Connection connection,
    	Exception exception 
    ){
    	ConnectionEvent event = new ConnectionEvent(
    		this,
    		ConnectionEvent.CONNECTION_ERROR_OCCURRED,
    		exception
    	);
    	event.setConnectionHandle(connection);
    	for(ConnectionEventListener listener : this.listeners) {
			listener.connectionErrorOccurred(event);
    	}
    }
    	
    public void addConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
    	this.listeners.add(connectionEventListener);
    }

    void dissociateConnection(
        Object connection
    ) {
    	this.connections.remove(connection);
    }
    
    public void associateConnection(
        Object connection
    ) throws ResourceException {
        try {            
        	Connection ldapConnection = (Connection) connection;
        	ldapConnection.setManagedConnection(this);
        	this.connections.add(ldapConnection);
        } catch (ClassCastException exception){
            throw new ResourceException(
                "Managed connection class and connection class do not match",
                exception
            );
		}
    }


    public void cleanup(
    )throws ResourceException {
    	for(Connection connection : this.connections) {
    		connection.setManagedConnection(null);
    	}
    }

    public void destroy(
    ) throws ResourceException {
    	try {
			this.ldapConnection.disconnect();
		} catch (LDAPException exception) {
			throw log(
				new EISSystemException(
					"LDAP disconnection failure",
					exception
				)
			);
		} finally {
	        this.credential = null;
	        this.ldapConnection = null;
		}
    }

    public Object getConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        Object connection = new Connection();
        this.associateConnection(connection);
        return connection;
    }

    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        throw log(new NotSupportedException(NON_TRANSACTIONAL));
    }

    public PrintWriter getLogWriter(
    ) throws ResourceException {
        return this.logWriter;
    }

    public ManagedConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData == null ?
            this.metaData = new MetaData() :
            this.metaData;
    }

    public XAResource getXAResource(
    ) throws ResourceException {
        throw log(new NotSupportedException(NON_TRANSACTIONAL));
    }

    public void removeConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
    	this.listeners.remove(connectionEventListener);
    }

    public void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;        
    }
    
    /**
     * Log and return an exception
     * 
     * @param an exception
     * 
     * @return the exception
     */
    private ResourceException log(
        ResourceException exception
    ){
        try {
            PrintWriter logWriter = getLogWriter();
            if(logWriter != null) exception.printStackTrace(logWriter);
        } catch (Exception ignore) {
            // Ensure that the original exception will be available
        }
        return exception;
    }

    /**
     * Test whether the managed connection was created with the same credentials.
     * 
     * @param credential
     * 
     * @return <code>true</code> if the managed connection was created with the same credentials
     */
    public boolean matches(
        PasswordCredential credential
    ){
        return this.credential == credential || (
        	this.credential != null &&
        	credential != null &&
        	this.credential.equals(credential)
        );
    }

    /**
     * ManagedConnectionMetaData implementation
     */
    class MetaData
        implements ManagedConnectionMetaData
    {

        /**
         * Constructor 
         *
         * @param alias the certificate's alias
         */
        public MetaData(
        ) {
        }

        public String getEISProductName(
        ) throws ResourceException {
            return "LDAP";
        }

        public String getEISProductVersion(
        ) throws ResourceException {
            return "3.0";
        }

        public int getMaxConnections(
        )throws ResourceException {
            return 0; // no limit
        }

        public String getUserName(
        ) throws ResourceException {
            return credential.getUserName();
        }

    }
    
}
