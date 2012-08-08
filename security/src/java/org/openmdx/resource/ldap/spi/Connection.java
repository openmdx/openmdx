/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Connection.java,v 1.2 2007/12/03 18:30:45 hburger Exp $
 * Description: LDAP Connection 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/03 18:30:45 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

import java.io.IOException;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPConstraints;
import netscape.ldap.LDAPControl;
import netscape.ldap.LDAPEntry;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPExtendedOperation;
import netscape.ldap.LDAPModification;
import netscape.ldap.LDAPModificationSet;
import netscape.ldap.LDAPSearchConstraints;
import netscape.ldap.LDAPSearchResults;
import netscape.ldap.LDAPv3;

/**
 * LDAP Connection
 */
public class Connection
    implements org.openmdx.resource.ldap.cci.Connection
{

    /**
     * 
     */
    private ManagedConnection managedConnection;

    /**
     * Used by ManagedConnection
     */
    public void setManagedConnection(
    	ManagedConnection managedConnection
    ){
    	if(
			managedConnection != null &&
			this.managedConnection != null
    	){
    		this.managedConnection.dissociateConnection(this);
    	}
    	this.managedConnection = managedConnection;
    }

    protected final LDAPv3 getDelegate(){
    	return this.managedConnection.getPhysicalConnection();
    }

	//------------------------------------------------------------------------
	// Implements Closeable
	//------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		this.managedConnection = null;
	}

	
	//------------------------------------------------------------------------
	// Imeplements LDAPv3
	//------------------------------------------------------------------------

    public void abandon(LDAPSearchResults arg0) throws LDAPException {
		getDelegate().abandon(arg0);
	}

	public void add(LDAPEntry arg0, LDAPConstraints arg1) throws LDAPException {
		getDelegate().add(arg0, arg1);
	}

	public void add(LDAPEntry arg0) throws LDAPException {
		getDelegate().add(arg0);
	}

	public void authenticate(int arg0, String arg1, String arg2)
			throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void authenticate(String arg0, String arg1) throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void bind(int arg0, String arg1, String arg2) throws LDAPException {
		getDelegate().bind(arg0, arg1, arg2);
	}

	public void bind(String arg0, String arg1) throws LDAPException {
		getDelegate().bind(arg0, arg1);
	}

	public boolean compare(String arg0, LDAPAttribute arg1, LDAPConstraints arg2)
			throws LDAPException {
		return getDelegate().compare(arg0, arg1, arg2);
	}

	public boolean compare(String arg0, LDAPAttribute arg1)
			throws LDAPException {
		return getDelegate().compare(arg0, arg1);
	}

	public void connect(int arg0, String arg1, int arg2, String arg3,
			String arg4) throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void connect(String arg0, int arg1, String arg2, String arg3)
			throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void connect(String arg0, int arg1) throws LDAPException {
		throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

	public void delete(String arg0, LDAPConstraints arg1) throws LDAPException {
		getDelegate().delete(arg0, arg1);
	}

	public void delete(String arg0) throws LDAPException {
		getDelegate().delete(arg0);
	}

	public void disconnect() throws LDAPException {
		if(this.managedConnection != null) {
			this.managedConnection.signalClose(this);
		}
	}

	public LDAPExtendedOperation extendedOperation(LDAPExtendedOperation arg0)
			throws LDAPException {
		return getDelegate().extendedOperation(arg0);
	}

	public Object getOption(int arg0) throws LDAPException {
		return getDelegate().getOption(arg0);
	}

	public LDAPControl[] getResponseControls() {
		return getDelegate().getResponseControls();
	}

	public void modify(String arg0, LDAPModification arg1, LDAPConstraints arg2)
			throws LDAPException {
		getDelegate().modify(arg0, arg1, arg2);
	}

	public void modify(String arg0, LDAPModification arg1) throws LDAPException {
		getDelegate().modify(arg0, arg1);
	}

	public void modify(String arg0, LDAPModificationSet arg1,
			LDAPConstraints arg2) throws LDAPException {
		getDelegate().modify(arg0, arg1, arg2);
	}

	public void modify(String arg0, LDAPModificationSet arg1)
			throws LDAPException {
		getDelegate().modify(arg0, arg1);
	}

	public LDAPEntry read(String arg0, String[] arg1, LDAPSearchConstraints arg2)
			throws LDAPException {
		return getDelegate().read(arg0, arg1, arg2);
	}

	public LDAPEntry read(String arg0, String[] arg1) throws LDAPException {
		return getDelegate().read(arg0, arg1);
	}

	public LDAPEntry read(String arg0) throws LDAPException {
		return getDelegate().read(arg0);
	}

	public void rename(String arg0, String arg1, boolean arg2,
			LDAPConstraints arg3) throws LDAPException {
		getDelegate().rename(arg0, arg1, arg2, arg3);
	}

	public void rename(String arg0, String arg1, boolean arg2)
			throws LDAPException {
		getDelegate().rename(arg0, arg1, arg2);
	}

	public void rename(String arg0, String arg1, String arg2, boolean arg3,
			LDAPConstraints arg4) throws LDAPException {
		getDelegate().rename(arg0, arg1, arg2, arg3, arg4);
	}

	public void rename(String arg0, String arg1, String arg2, boolean arg3)
			throws LDAPException {
		getDelegate().rename(arg0, arg1, arg2, arg3);
	}

	public LDAPSearchResults search(String arg0, int arg1, String arg2,
			String[] arg3, boolean arg4, LDAPSearchConstraints arg5)
			throws LDAPException {
		return getDelegate().search(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	public LDAPSearchResults search(String arg0, int arg1, String arg2,
			String[] arg3, boolean arg4) throws LDAPException {
		return getDelegate().search(arg0, arg1, arg2, arg3, arg4);
	}

	public void setOption(int arg0, Object arg1) throws LDAPException {
		getDelegate().setOption(arg0, arg1);
	}
    
	final static private String MANAGED_ENVIRONMENT = 
		"This method is not supported in a managed environment";

}
