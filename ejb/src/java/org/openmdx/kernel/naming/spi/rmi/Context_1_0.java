/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Context_1_0.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: Remote Context
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Remote Context
 */
public interface Context_1_0 extends Remote {

    /* (non-Javadoc)
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    public NamingEnumeration<Binding> listBindings(
    	String name
	) throws RemoteException, NamingException;

    /* (non-Javadoc)
     * @see javax.naming.Context#list(java.lang.String)
     */
    public NamingEnumeration<NameClassPair> list(
        String name
	) throws RemoteException, NamingException;

    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String name
	) throws RemoteException, NamingException;

    /* (non-Javadoc)
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public Object lookupLink(
        String name
	) throws RemoteException, NamingException;
	
	/* (non-Javadoc)
	 * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
	 */
	public void bind(
		String name, 
		Object obj
	) throws RemoteException, NamingException;
	
	/* (non-Javadoc)
	 * @see javax.naming.Context#createSubcontext(java.lang.String)
	 */
	public Context createSubcontext(
		String name
	) throws RemoteException, NamingException;
	
	/* (non-Javadoc)
	 * @see javax.naming.Context#destroySubcontext(java.lang.String)
	 */
	public void destroySubcontext(
		String name
	) throws RemoteException, NamingException;
	
	/* (non-Javadoc)
	 * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
	 */
	public void rebind(
		String name, 
		Object obj
	) throws RemoteException, NamingException;
	
	/* (non-Javadoc)
	 * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
	 */
	public void rename(
		String oldName, 
		String newName
	) throws RemoteException, NamingException;
	
	/* (non-Javadoc)
	 * @see javax.naming.Context#unbind(java.lang.String)
	 */
	public void unbind(
		String name
	) throws RemoteException, NamingException;
	
}
