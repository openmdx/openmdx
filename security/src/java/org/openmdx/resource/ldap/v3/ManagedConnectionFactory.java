/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Managed LDAP Connection Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2018, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.v3;

import java.net.URI;
import java.net.URISyntaxException;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.security.auth.Subject;

import org.openmdx.resource.ldap.spi.AbstractManagedConnectionFactory;
import org.openmdx.resource.ldap.spi.ManagedConnection;

import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;
import netscape.ldap.factory.JSSESocketFactory;


/**
 * Managed LDAP Connection Factory
 */
public class ManagedConnectionFactory extends AbstractManagedConnectionFactory {

    /**
     * Constructor
     */
    public ManagedConnectionFactory() {
	    super();
    }

	/**
	 * Implements <code>Serializable</code>
	 */
    private static final long serialVersionUID = 5970793592667548449L;

    /**
     * LDAP over SSL port
     */
    private static final int DEFAULT_SSL_PORT = 636;
    
    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#isManagedConnectionShareable()
     */
    @Override
    protected boolean isManagedConnectionShareable() {
	    return true;
    }

    @Override
    protected javax.resource.spi.ManagedConnection newManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
		return new ManagedConnection(
            this,
            getPasswordCredential(subject),
            connectionRequestInfo, 
            createPhysicalConnection(getConnectionURI())
         );
    }

    /**
     * Create (and validate) the connection URI
     */
    private URI getConnectionURI() throws ResourceException {
        final String connectionURL = this.getConnectionURL();
        try {
            return new URI(connectionURL);
        } catch (URISyntaxException exception) {
            throw this.log(
                new EISSystemException(
                    "Could not connect to LDAP host \"" + connectionURL + "\". Invalid connection URL.",
                    exception
                ), 
                true
            );
        }
    }

    /**
     * Create the physical connection
     * 
     * @param uri the connection URI
     * 
     * @return the {@code Cloneable} physical connection
     * 
     * @throws ResourceException 
     */
    private LDAPv3 createPhysicalConnection(final URI uri) throws ResourceException {
        final boolean useSSL = "ldaps".equalsIgnoreCase(uri.getScheme());
        int port = uri.getPort();
        if(port < 0) {
        	port = useSSL ? DEFAULT_SSL_PORT : LDAPConnection.DEFAULT_PORT;
        }
        final String host = uri.getHost();
        final LDAPv3 physicalConnection = useSSL ? new LDAPConnection(new JSSESocketFactory())  : new LDAPConnection();
        try {
            physicalConnection.connect(host, port);
        } catch (LDAPException exception) {
            throw this.log(
               new EISSystemException(
                   "Could not connect to LDAP host \"" + uri + "\". ",
                   exception
               ),
               true
           );
        }
        return physicalConnection;
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /**
     * Overriding required for Oracle WebLogic
     * 
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#equals(java.lang.Object)
     */
    @Override
    public boolean equals(
    	Object that
    ) {
        return super.equals(that);
    }

	/**
     * Overriding required for Oracle WebLogic
     * 
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
}
