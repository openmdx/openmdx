/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagedConnectionFactory.java,v 1.2 2010/08/03 14:27:24 hburger Exp $
 * Description: Managed LDAP Connection Factory
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/03 14:27:24 $
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
package org.openmdx.resource.ldap.v3;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.resource.ldap.spi.AbstractManagedConnectionFactory;
import org.openmdx.resource.ldap.spi.ManagedConnection;

import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;


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
    private static final long serialVersionUID = -396153144173718931L;

    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#isManagedConnectionShareable()
     */
    @Override
    protected boolean isManagedConnectionShareable() {
	    return false;
    }


	/* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public javax.resource.spi.ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = this.getCredential(subject);
        try {
        	LDAPv3 physicalConnection = new LDAPConnection();
			physicalConnection.connect(
				this.getProtocolVersion(),
				this.getConnectionURL(), 
				LDAPConnection.DEFAULT_PORT,
				credential == null ? this.getUserName() : credential.getUserName(),
				credential == null ? this.getPassword() : new String (credential.getPassword())
			);
	        return new ManagedConnection(
                physicalConnection,
                credential
             );
		} catch (LDAPException exception) {
			 String message = "Could not connect to LDAP host(s) \"" + this.getConnectionURL() + "\".";
		     switch( exception.getLDAPResultCode() ) {
		         case LDAPException.NO_SUCH_OBJECT:
		        	 message += "User \"" + credential.getUserName() + "\" does not exist.";
		             break;
		         case LDAPException.INVALID_CREDENTIALS:
		        	 message += "Invalid password for user \"" + credential.getUserName() + "\".";
		             System.out.println( "Invalid password." );
		             break;
		     }
			 throw this.log(
				new EISSystemException(
					message,
					exception
				)
			);
		}
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
