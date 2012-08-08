/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ClassLoaderContext.java,v 1.4 2010/06/04 22:45:00 hburger Exp $
 * Description: Java URL Context
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:45:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.naming.spi;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.openmdx.kernel.lightweight.naming.spi.HashMapContext;
import org.openmdx.kernel.lightweight.naming.spi.NameBasedContext;


/**
 * Java URL Context
 */
public class ClassLoaderContext extends NameBasedContext {

    /**
     * Constructor
     * 
     * @param environment 
     * @param urlPrefix 
     */
    ClassLoaderContext(
        Map<?,?> environment, 
        String urlPrefix
    ) {
    	super(environment);
        this.urlPrefix = urlPrefix;
    }
    
    /**
     * Retrieve a class-loader specific sub-context for the specified class loader. 
     * 
     * @param contextName 
     * @param classLoader
     * 
     * @return the requested sub-context
     */
    public Context getContext(
        String contextName, 
        ClassLoader classLoader
    ) throws NamingException {
        Map<ClassLoader,Context> map = classLoaderContexts.get(contextName);
        if(map == null){
            map = new WeakHashMap<ClassLoader,Context>();
            classLoaderContexts.put(contextName, map);
        }
        synchronized(map){
            Context context = map.get(classLoader);
            if(context == null) {
                map.put(
                    classLoader, 
                    context = new HashMapContext(
                        super.getEnvironment(), 
                        null, 
                        ""
                    )
                );
            }
            return context;
        }
    }
    
    /**
     * Retrieves a given context
     * 
     * @param contextName
     *  
     * @return the given context; or null.
     */
    synchronized Context getContext(
        String contextName
    ){
        Map<ClassLoader,Context> map = classLoaderContexts.get(contextName);
        Context context = null;
        if(map != null) for(
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            context == null && classLoader != null;
            classLoader = classLoader.getParent()
        ) context = map.get(classLoader);
        return context;
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractContext
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.naming.spi.AbstractContext#resolve(java.lang.String, boolean)
     */
    @Override
	protected Object resolveLink(
        String nameComponent
    ) throws NamingException {
        String contextName = nameComponent.startsWith(urlPrefix) ?
            nameComponent.substring(urlPrefix.length()) :
            nameComponent;
        if("".equals(contextName)) return this;
        Object object = getContext(contextName);
		if(object == null) throw new NameNotFoundException(
            "No object bound to \"" + nameComponent + '"'
        );
		return object;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.naming.spi.AbstractContext#listBindings()
     */
    @Override
	protected NamingEnumeration<Binding> listBindings() throws NamingException {
        return new Bindings(classLoaderContexts.keys());
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace() throws NamingException {
        return "";
    }

    /**
     * Maps a class loader to its initial context
     */
    private final Hashtable<String,Map<ClassLoader,Context>> classLoaderContexts = new Hashtable<String,Map<ClassLoader,Context>>();
    
    /**
     * 
     */
    final private String urlPrefix;
    
    
    //------------------------------------------------------------------------
    // Class Bindings
    //------------------------------------------------------------------------
    
    /**
     * Bindings
     */
    private final class Bindings implements NamingEnumeration<Binding> {
        
        /**
         * The delegate
         */
        Enumeration<String> contextNames; 

        /**
         * Prefetched context
         */
        Context nextContext = null;
        
        /**
         * Prefetched context name
         */
        String nextName = null;

        /**
         * Constructor
         * 
         * @param contextNames the delegate
         */
        Bindings(
            Enumeration<String> contextNames            
        ){
            this.contextNames = contextNames;
        }
        
        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#next()
         */
        public Binding next() throws NamingException {
            return nextElement();
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#hasMore()
         */
        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#close()
         */
        public void close() throws NamingException {
            this.contextNames = null;
            this.nextName = null;
            this.nextContext = null;
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        public boolean hasMoreElements() {
            while(
                this.nextContext == null &&
                this.contextNames.hasMoreElements()
            ){
                this.nextName = this.contextNames.nextElement();
                this.nextContext = getContext(this.nextName);
            }
            return this.nextContext != null;
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        public Binding nextElement() {
            if(hasMoreElements()) return new Binding(
                this.nextName,
                this.nextContext
             );
            throw new NoSuchElementException();
        }
        
    }

}
