/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: LDAP Connection 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2018, OMEX AG, Switzerland
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

import org.openmdx.resource.ldap.cci.LDAPConnection;
import org.openmdx.resource.spi.AbstractConnection;

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
class Connection
	extends AbstractConnection
    implements LDAPConnection
{
    
    /**
     * Constructor 
     * 
     * @param delegate the delegate
     */
    Connection(LDAPv3 delegate) {
        this.delegate = delegate;
    }

    private LDAPv3 delegate;

    private static final String MANAGED_ENVIRONMENT = 
        "This method is not supported in a managed environment";

    /**
     * Tests whether this handle is connected
     * 
     * @return {@code true} if this handle is connected
     */
    private boolean isConnected() {
        return this.delegate != null;
    }

    //------------------------------------------------------------------------
    // Implements AutoCloseable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws LDAPException{
        disconnect();
    }
    
    
	//------------------------------------------------------------------------
	// Implements LDAPv3
	//------------------------------------------------------------------------

	/**
	 * Retrieve the delegate
	 * 
	 * @exception LDAPException if the connection is already disconnected
	 */
	private final LDAPv3 getDelegate(
	) throws LDAPException {
	    if(!isConnected()) throw new LDAPException(
			"Already disconnected", 
			LDAPException.CONNECT_ERROR
		);
    	return this.delegate;
    }

    @Override
    public void abandon(LDAPSearchResults arg0) throws LDAPException {
		this.getDelegate().abandon(arg0);
	}

  	@Override
	public void add(LDAPEntry arg0, LDAPConstraints arg1) throws LDAPException {
		this.getDelegate().add(arg0, arg1);
	}

  	@Override
	public void add(LDAPEntry arg0) throws LDAPException {
		this.getDelegate().add(arg0);
	}

  	@Override
	public void authenticate(int version, String distinguishedName, String password)
			throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public void authenticate(String distinguishedName, String passsword) throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public void bind(int arg0, String arg1, String arg2) throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public void bind(String arg0, String arg1) throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public boolean compare(String arg0, LDAPAttribute arg1, LDAPConstraints arg2)
			throws LDAPException {
		return this.getDelegate().compare(arg0, arg1, arg2);
	}

  	@Override
	public boolean compare(String arg0, LDAPAttribute arg1)
			throws LDAPException {
		return this.getDelegate().compare(arg0, arg1);
	}

  	@Override
	public void connect(int arg0, String arg1, int arg2, String arg3,
			String arg4) throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public void connect(String arg0, int arg1, String arg2, String arg3)
			throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public void connect(String arg0, int arg1) throws LDAPException {
        throw new UnsupportedOperationException(MANAGED_ENVIRONMENT);
	}

  	@Override
	public void delete(String arg0, LDAPConstraints arg1) throws LDAPException {
		this.getDelegate().delete(arg0, arg1);
	}

  	@Override
	public void delete(String arg0) throws LDAPException {
		this.getDelegate().delete(arg0);
	}

  	@Override
	public void disconnect() throws LDAPException {
  	    if(isConnected()) {
  	        this.delegate.disconnect();
  	        this.delegate = null;
  	        dissociateManagedConnection();
  	    }
	}

  	@Override
	public LDAPExtendedOperation extendedOperation(LDAPExtendedOperation arg0)
			throws LDAPException {
		return this.getDelegate().extendedOperation(arg0);
	}

  	@Override
	public Object getOption(int arg0) throws LDAPException {
		return this.getDelegate().getOption(arg0);
	}

  	@Override
	public LDAPControl[] getResponseControls() {
		try {
	        return this.getDelegate().getResponseControls();
        } catch (LDAPException e) {
        	throw new IllegalStateException(e);
        }
	}

  	@Override
	public void modify(String arg0, LDAPModification arg1, LDAPConstraints arg2)
			throws LDAPException {
		this.getDelegate().modify(arg0, arg1, arg2);
	}

  	@Override
	public void modify(String arg0, LDAPModification arg1) throws LDAPException {
		this.getDelegate().modify(arg0, arg1);
	}

  	@Override
	public void modify(String arg0, LDAPModificationSet arg1,
			LDAPConstraints arg2) throws LDAPException {
		this.getDelegate().modify(arg0, arg1, arg2);
	}

  	@Override
	public void modify(String arg0, LDAPModificationSet arg1)
			throws LDAPException {
		this.getDelegate().modify(arg0, arg1);
	}

  	@Override
	public LDAPEntry read(String arg0, String[] arg1, LDAPSearchConstraints arg2)
			throws LDAPException {
		return this.getDelegate().read(arg0, arg1, arg2);
	}

  	@Override
	public LDAPEntry read(String arg0, String[] arg1) throws LDAPException {
		return this.getDelegate().read(arg0, arg1);
	}

  	@Override
	public LDAPEntry read(String arg0) throws LDAPException {
		return this.getDelegate().read(arg0);
	}

  	@Override
	public void rename(String arg0, String arg1, boolean arg2,
			LDAPConstraints arg3) throws LDAPException {
		this.getDelegate().rename(arg0, arg1, arg2, arg3);
	}

  	@Override
	public void rename(String arg0, String arg1, boolean arg2)
			throws LDAPException {
		this.getDelegate().rename(arg0, arg1, arg2);
	}

  	@Override
	public void rename(String arg0, String arg1, String arg2, boolean arg3,
			LDAPConstraints arg4) throws LDAPException {
		this.getDelegate().rename(arg0, arg1, arg2, arg3, arg4);
	}

  	@Override
	public void rename(String arg0, String arg1, String arg2, boolean arg3)
			throws LDAPException {
		this.getDelegate().rename(arg0, arg1, arg2, arg3);
	}

  	@Override
	public LDAPSearchResults search(String arg0, int arg1, String arg2,
			String[] arg3, boolean arg4, LDAPSearchConstraints arg5)
			throws LDAPException {
		return this.getDelegate().search(arg0, arg1, arg2, arg3, arg4, arg5);
	}

  	@Override
	public LDAPSearchResults search(String arg0, int arg1, String arg2,
			String[] arg3, boolean arg4) throws LDAPException {
		return this.getDelegate().search(arg0, arg1, arg2, arg3, arg4);
	}

  	@Override
	public void setOption(int arg0, Object arg1) throws LDAPException {
		this.getDelegate().setOption(arg0, arg1);
	}

}
