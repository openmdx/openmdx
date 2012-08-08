/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ResourceContext.java,v 1.1 2009/09/11 13:16:23 hburger Exp $
 * Description: Resource Context 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/11 13:16:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming.spi;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;


/**
 * Resource Context
 */
public abstract class ResourceContext extends StringBasedContext {

    /**
     * Constructor 
     */
    protected ResourceContext(
    ){
        super();
    }

    /**
     * Lazily established connection factories
     */
    private final Map<String,Object> connectionFactories = new HashMap<String,Object>();
    
    /* (non-Javadoc)
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    public final void bind(
        String name, 
        Object obj
    ) throws NamingException {
        if(this.connectionFactories.containsKey(name)) {
            throw new NameAlreadyBoundException(name);
        } else {
            this.connectionFactories.put(name, obj);
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    public String composeName(
        String name, 
        String prefix
    ) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    public final Context createSubcontext(
        String name
    ) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    public final void destroySubcontext(
        String name
    ) throws NamingException {
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameInNamespace()
     */
    public final String getNameInNamespace(
    ) throws NamingException {
        return "";
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    public NameParser getNameParser(
        String name
    ) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#list(java.lang.String)
     */
    public final NamingEnumeration<NameClassPair> list(
        String name
    ) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    public final NamingEnumeration<Binding> listBindings(
        String name
    ) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public final Object lookupLink(
        String name
    ) throws NamingException {
        return this.connectionFactories.get(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    public void rebind(
        String name, 
        Object obj
    ) throws NamingException {
        this.connectionFactories.put(name, obj);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    public final void rename(
        String oldName, 
        String newName
    ) throws NamingException {
        throw new OperationNotSupportedException();
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    public final void unbind(
        String name
    ) throws NamingException {
        this.connectionFactories.remove(name);
    }
    
}
