/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RelocatableContext.java,v 1.6 2010/06/07 10:27:10 hburger Exp $
 * Description: Relocatable Context
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/07 10:27:10 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.naming.spi.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;

import org.openmdx.kernel.lightweight.naming.spi.AbstractContext;
import org.openmdx.kernel.lightweight.naming.spi.StringBasedContext;

/**
 * Relocatable Context
 */
public class RelocatableContext extends StringBasedContext implements Serializable {

	/**
     * 
     */
    private static final long serialVersionUID = 3544394707867217975L;

    /**
	 * 
	 */
	private Context_1_0 initialContext;
	
	/**
	 * 
	 */
	private String prefix;

    /**
     * Constructor
     * 
	 * @param environment
	 * @param nameParser
	 */
	public RelocatableContext(
		Context_1_0 initialContext,
		String nameInNamespace
	) {
		this.initialContext = initialContext;
		this.prefix = nameInNamespace;
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#lookup(java.lang.String)
	 */
	public Object lookup(String name) throws NamingException {
		try {
			return this.initialContext.lookup(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
	 */
	public void bind(String name, Object obj) throws NamingException {
		try {
			this.initialContext.bind(
				composeName(name, this.prefix),
				obj
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
	 */
	public void rebind(String name, Object obj) throws NamingException {
		try {
			this.initialContext.rebind(
				composeName(name, this.prefix),
				obj
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#unbind(java.lang.String)
	 */
	public void unbind(String name) throws NamingException {
		try {
			this.initialContext.unbind(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
	 */
	public void rename(String oldName, String newName) throws NamingException {
		try {
			this.initialContext.rename(
				composeName(oldName, this.prefix),
				composeName(newName, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#list(java.lang.String)
	 */
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
		try {
			return this.initialContext.list(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#listBindings(java.lang.String)
	 */
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
		try {
			return this.initialContext.listBindings(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#destroySubcontext(java.lang.String)
	 */
	public void destroySubcontext(String name) throws NamingException {
		try {
			this.initialContext.destroySubcontext(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#createSubcontext(java.lang.String)
	 */
	public Context createSubcontext(String name) throws NamingException {
		try {
			return this.initialContext.createSubcontext(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#lookupLink(java.lang.String)
	 */
	public Object lookupLink(String name) throws NamingException {
		try {
			return this.initialContext.lookupLink(
				composeName(name, this.prefix)
			);
		} catch (RemoteException exception) {
			throw marshal(exception);
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#getNameParser(java.lang.String)
	 */
	public NameParser getNameParser(String name) throws NamingException {
		return AbstractContext.nameParser;
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
	 */
	public String composeName(String name, String prefix) throws NamingException {
        return name.length() == 0 ? 
            	prefix : 
            prefix.length() == 0 ?
            	name:
            	prefix + '/' + name;
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#close()
	 */
	@Override
	public void close() throws NamingException {
		super.close();
		this.prefix = null;
		this.initialContext = null;
	}

	/* (non-Javadoc)
	 * @see javax.naming.Context#getNameInNamespace()
	 */
	public String getNameInNamespace() throws NamingException {
		return this.prefix;
	}
	
	/**
	 * Marshals a RemoteException to a ServiceUnavailableException
	 * 
	 * @param remoteException
	 * @return
	 */
	private NamingException marshal(
		RemoteException remoteException
	){
		NamingException namingException = new ServiceUnavailableException();
		namingException.setRootCause(remoteException);
		return namingException;
	}

}
