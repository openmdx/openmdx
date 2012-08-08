/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LDAPConnectionFactory.java,v 1.4 2009/03/08 18:52:20 wfro Exp $
 * Description: Managed LDAP Connection Factory
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:20 $
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

import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;

import org.openmdx.resource.ldap.spi.ManagedConnection;

/**
 * Managed LDAP Connection Factory
 */
public class LDAPConnectionFactory
    extends AbstractConnectionFactory {

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -2136791474088304800L;

    public javax.resource.spi.ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = this.getCredential(subject);
        String hosts = this.getConnectionURL();
    	try {
        	LDAPv3 physicalConnection = new LDAPConnection();
        	if(credential == null) {
    			physicalConnection.connect(
    				this.getProtocolVersion(),
					hosts, 
					LDAPConnection.DEFAULT_PORT,
					null,
					null
				);
        	} 
        	else {
    			physicalConnection.connect(
    				this.getProtocolVersion(),
					hosts, 
					LDAPConnection.DEFAULT_PORT, 
					credential.getUserName(), 
					new String (credential.getPassword())
				);
        	}
	        return new ManagedConnection(
                physicalConnection,
                credential
             );
		} 
    	catch (LDAPException exception) {
			 String message = "Could not connect to LDAP host(s) \"" + hosts + "\".";
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

}
