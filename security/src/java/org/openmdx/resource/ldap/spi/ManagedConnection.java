/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagedConnection.java,v 1.7 2010/07/16 13:19:02 hburger Exp $
 * Description: Managed LDAP Connection 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/07/16 13:19:02 $
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
package org.openmdx.resource.ldap.spi;

import javax.resource.ResourceException;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.security.PasswordCredential;

import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;

import org.openmdx.resource.ldap.v3.Connection;
import org.openmdx.resource.spi.AbstractManagedConnection;


/**
 * Managed LDAP Connection
 */
public class ManagedConnection extends AbstractManagedConnection {

    /**
     * Constructor 
     *
     * @param physicalConnection
     * @param credential
     */
    public ManagedConnection(
    	LDAPv3 physicalConnection,
        PasswordCredential credential
    ) {
    	super("LDAP","3.0", credential);
    	this.ldapConnection = physicalConnection;
    }

    /**
     * 
     */
    private LDAPv3 ldapConnection;
    
    /**
     * Used by LDAPConnection
     * 
     * @return
     */
    public LDAPv3 getPhysicalConnection(
    ){
    	return this.ldapConnection;
    }
    
    @Override
    public void destroy(
    ) throws ResourceException {
    	try {
			this.ldapConnection.disconnect();
		} catch (LDAPException exception) {
			throw this.log(
				new EISSystemException(
					"LDAP disconnection failure",
					exception
				)
			);
		} finally {
    		super.destroy();
	        this.ldapConnection = null;
		}
    }

    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnection#newConnection()
     */
    @Override
    protected Object newConnection() {
	    return new Connection();
    }

}
