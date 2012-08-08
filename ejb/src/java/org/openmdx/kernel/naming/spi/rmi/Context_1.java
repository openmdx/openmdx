/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Context_1.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: Context Service
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.naming.spi.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.openmdx.kernel.naming.initial.ContextFactory;


/**
 * Context Service
 */
public class Context_1 extends UnicastRemoteObject implements Context_1_0 {

	/**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4120853248229127992L;

    /**
	 * 
	 */
	private final Hashtable<String, Object> environment;

	/**
	 * 
	 */
	private final String initialContextName;

	/**
	 * Constructor 
	 *
	 * @throws NamingException
	 * @throws RemoteException
	 */
	public Context_1(
	) throws NamingException, RemoteException {
	    this.environment = new Hashtable<String, Object>();
		this.environment.put(
			Context.INITIAL_CONTEXT_FACTORY,
			ContextFactory.class.getName()
		);
		this.environment.put(
			Context.URL_PKG_PREFIXES,
			"org.openmdx.kernel.naming.container"
		);
		Context initialContext = borrowContext();
		try {
			this.initialContextName = initialContext.getNameInNamespace();
		} finally {
			returnContext(initialContext);
		}
	}

	/**
	 * Acquire an initial context.
	 * 
	 * @return a new Initial Context
	 * 
	 * @throws NamingException
	 */
	private Context borrowContext(
	) throws NamingException{
		return new InitialContext(this.environment);
	}
	
	/**
	 * Return an initial context.
	 * 
	 * @param the initial context to return 
	 * 
	 * @throws NamingException
	 */
	private void returnContext(
		Context context
	) throws NamingException{
		context.close();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#listBindings(java.lang.String)
	 */
	public NamingEnumeration<Binding> listBindings(
		String name
	) throws RemoteException, NamingException {
		List<Binding> target = new ArrayList<Binding>();
		Context initialContext = borrowContext();
		try {
			for(
			    NamingEnumeration<Binding> source = initialContext.listBindings(name);
			    source.hasMore();
			){
				Binding binding = source.next();
				target.add(
					new Binding(
						binding.getName(),
						binding.getClassName(),
						marshal(binding.getObject()),
						binding.isRelative()
					)
				);
			}
		} catch (NamingException exception){
			return new RelocatableEnumeration<Binding>(target, exception);
		} finally {
			returnContext(initialContext);
		}
		return new RelocatableEnumeration<Binding>(target);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#list(java.lang.String)
	 */
	public NamingEnumeration<NameClassPair> list(
		String name
	) throws RemoteException, NamingException {
		List<NameClassPair> target = new ArrayList<NameClassPair>();
		Context initialContext = borrowContext();
		try {
			for(
				NamingEnumeration<NameClassPair> source = initialContext.list(name);
				source.hasMore();
			){
				NameClassPair binding = source.next();
				target.add(
					new NameClassPair(
						binding.getName(),
						binding.getClassName(),
						binding.isRelative()
					)
				);
			}
		} catch (NamingException exception){
			return new RelocatableEnumeration<NameClassPair>(target, exception);
		} finally {
			returnContext(initialContext);
		}
		return new RelocatableEnumeration<NameClassPair>(target);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#lookup(java.lang.String)
	 */
	public Object lookup(
		String name
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			return marshal(initialContext.lookup(name));
		} finally {
			returnContext(initialContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#lookupLink(java.lang.String)
	 */
	public Object lookupLink(
		String name
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			return marshal(initialContext.lookupLink(name));
		} finally {
			returnContext(initialContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#bind(java.lang.String, java.lang.Object)
	 */
	public void bind(
		String name, 
		Object obj
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			initialContext.bind(name, obj);
		} finally {
			returnContext(initialContext);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#createSubcontext(java.lang.String)
	 */
	public Context createSubcontext(
		String name
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			return (Context) marshal(initialContext.createSubcontext(name));
		} finally {
			returnContext(initialContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#destroySubcontext(java.lang.String)
	 */
	public void destroySubcontext(
		String name
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			initialContext.destroySubcontext(name);
		} finally {
			returnContext(initialContext);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#rebind(java.lang.String, java.lang.Object)
	 */
	public void rebind(
		String name, 
		Object obj
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			initialContext.rebind(name, obj);
		} finally {
			returnContext(initialContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#rename(java.lang.String, java.lang.String)
	 */
	public void rename(
		String oldName, 
		String newName
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			initialContext.rename(oldName, newName);
		} finally {
			returnContext(initialContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.naming.rmi.Context_1_0#unbind(java.lang.String)
	 */
	public void unbind(
		String name
	) throws RemoteException, NamingException {
		Context initialContext = borrowContext();
		try {
			initialContext.unbind(name);
		} finally {
			returnContext(initialContext);
		}
	}
	
	/**
	 * Wraps Context objects
	 * 
	 * @param source
	 * 
	 * @return the marshalled object
	 * 
	 * @throws NamingException
	 */
	private Object marshal(
		Object source
	) throws NamingException{
		if(source instanceof Context){
			Context context = (Context)source;
			String contextName = context.getNameInNamespace(); 
			if(contextName.startsWith(this.initialContextName)){
				return new RelocatableContext(
					this, 
					contextName.substring(this.initialContextName.length())
				);
			}
		}
		return source;
	}
	
}
