/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DelegatingContext.java,v 1.2 2010/06/02 13:45:39 hburger Exp $
 * Description: DelegatingContext
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:45:39 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming.spi;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * DelegatingContext
 */
public class DelegatingContext implements Context {

    /**
     * Constructor
     */
    private DelegatingContext(
        Hashtable<?,?> environment,
        Context context
    ){
        this.environment = environment == null ? null : new Hashtable<Object,Object>(environment);
        this.context = context;
    }

    /**
     * Constructor 
     * 
     * @param environment
     */
    protected DelegatingContext(
        Hashtable<?,?> environment
    ){
        this(environment, null);
    }

    /**
     * Constructor
     */
    public DelegatingContext(
        Context context
    ){
        this(null, context);
    }

    /**
     * This Context's provate environment
     */
    private Hashtable<Object,Object> environment;
    
    /**
     * This Context's delegate
     */
    private Context context;

    /**
     * The delegate has to be provided by a subclass
     * 
     * @return the delegate
     * 
     * @throws javax.naming.NamingException
     */
    protected Context getDelegate(
    ) throws NamingException {
        return this.context;
    }

    /**
     * Set the delegate
     * 
     * @param context
     * @throws NamingException
     */
    protected void setDelegate(
        Context context
    ) throws NamingException {
        this.context = context;
    }

    /**
     * Retrieve the environment
     * 
     * @return an environment not affecting the original one
     * 
     * @throws NamingException
     */
    @SuppressWarnings("unchecked")
    private Hashtable<Object,Object> environment(
    ) throws NamingException {
        return this.environment == null ?
            this.environment = (Hashtable<Object, Object>) getDelegate().getEnvironment() :
            this.environment;
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#addToEnvironment(java.lang.String, java.lang.Object)
     */
    public Object addToEnvironment(
        String propName, 
        Object propVal
    ) throws NamingException {
        return environment().put(propName, propVal);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
     */
    public void bind(
        String name, 
        Object obj
    ) throws NamingException {
        getDelegate().bind(name, obj);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
     */
    public void bind(
        Name name, 
        Object obj
    ) throws NamingException {
        getDelegate().bind(name, obj);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#close()
     */
    public void close(
    ) throws NamingException {
        this.environment = null;
        this.context = null;
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#composeName(java.lang.String, java.lang.String)
     */
    public String composeName(
         String name, 
         String prefix
    ) throws NamingException {
        return getDelegate().composeName(name, prefix);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#composeName(javax.naming.Name, javax.naming.Name)
     */
    public Name composeName(
        Name name, 
        Name prefix
    ) throws NamingException {
        return getDelegate().composeName(name, prefix);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(java.lang.String)
     */
    public Context createSubcontext(
        String name
    ) throws NamingException {
        return getDelegate().createSubcontext(name);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#createSubcontext(javax.naming.Name)
     */
    public Context createSubcontext(
        Name name
    ) throws NamingException {
        return getDelegate().createSubcontext(name);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(java.lang.String)
     */
    public void destroySubcontext(
        String name
    ) throws NamingException {
        getDelegate().destroySubcontext(name);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#destroySubcontext(javax.naming.Name)
     */
    public void destroySubcontext(
        Name name
    ) throws NamingException {
        getDelegate().destroySubcontext(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getEnvironment()
     */
    public Hashtable<?,?> getEnvironment(
    ) throws NamingException {
        return new Hashtable<Object,Object>(environment());
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace(
    ) throws NamingException {
        return getDelegate().getNameInNamespace();
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#getNameParser(java.lang.String)
     */
    public NameParser getNameParser(
        String name
    ) throws NamingException {
        return getDelegate().getNameParser(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameParser(javax.naming.Name)
     */
    public NameParser getNameParser(
        Name name
    ) throws NamingException {
        return getDelegate().getNameParser(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#list(java.lang.String)
     */
    public NamingEnumeration<NameClassPair> list(
        String name
    ) throws NamingException {
        return getDelegate().list(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#list(javax.naming.Name)
     */
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return getDelegate().list(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#listBindings(java.lang.String)
     */
    public NamingEnumeration<Binding> listBindings(
        String name
    ) throws NamingException {
        return getDelegate().listBindings(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#listBindings(javax.naming.Name)
     */
    public NamingEnumeration<Binding> listBindings(
        Name name
    ) throws NamingException {
        return getDelegate().listBindings(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String name
    ) throws NamingException {
        return "".equals(name) ?
                new DelegatingContext(this.environment,  this.context) :
                getDelegate().lookup(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(javax.naming.Name)
     */
    public Object lookup(Name name) throws NamingException {
        return name.isEmpty() ?
            new DelegatingContext(this.environment,  this.context) :
            getDelegate().lookup(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookupLink(java.lang.String)
     */
    public Object lookupLink(
        String name
    ) throws NamingException {
        return getDelegate().lookupLink(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#lookupLink(javax.naming.Name)
     */
    public Object lookupLink(
        Name name
    ) throws NamingException {
        return getDelegate().lookupLink(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rebind(java.lang.String, java.lang.Object)
     */
    public void rebind(
        String name, 
        Object obj
    ) throws NamingException {
        getDelegate().rebind(name, obj);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rebind(javax.naming.Name, java.lang.Object)
     */
    public void rebind(
        Name name, 
        Object obj
    ) throws NamingException {
        getDelegate().rebind(name, obj);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#removeFromEnvironment(java.lang.String)
     */
    public Object removeFromEnvironment(
        String propName
    ) throws NamingException {
        return environment().remove(propName);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rename(java.lang.String, java.lang.String)
     */
    public void rename(
        String oldName, 
        String newName
    ) throws NamingException {
        getDelegate().rename(oldName, newName);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#rename(javax.naming.Name, javax.naming.Name)
     */
    public void rename(
        Name oldName, 
        Name newName
    ) throws NamingException {
        getDelegate().rename(oldName, newName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
        try {
            return getDelegate().toString();
        } catch (NamingException e) {
            return super.toString();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.naming.Context#unbind(java.lang.String)
     */
    public void unbind(
        String name
    ) throws NamingException {
        getDelegate().unbind(name);
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#unbind(javax.naming.Name)
     */
    public void unbind(
        Name name
    ) throws NamingException {
        getDelegate().unbind(name);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(this.getClass() != obj.getClass()) return false;
        DelegatingContext that = (DelegatingContext) obj;
        try {
            return this.getDelegate().equals(that.getDelegate()) &&
                this.getEnvironment().equals(that.getEnvironment());
        } catch (NamingException exception) {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        try {
            return getDelegate().hashCode();
        } catch (NamingException e) {
            return super.hashCode();
        }
    }
}
