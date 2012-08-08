/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ClassLoadertContextFactory.java,v 1.2 2008/01/12 02:45:29 hburger Exp $
 * Description: Container Context Factory
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/12 02:45:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.naming.spi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;

/**
 * Container Context Factory
 */
public abstract class ClassLoadertContextFactory
    implements InitialContextFactory
{

    /**
     * Constructor the <code>openmdx:</code>&lsaquo;contextName&rsaquo; context.
     * 
     * @param contextName 
     */
    protected ClassLoadertContextFactory(
        String contextName
    ) {
        this.contextName = contextName;
    }

    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public final Context getInitialContext(
        Hashtable<?,?> environment
    ) throws NamingException {
        ClassLoader classLoader = (ClassLoader) environment.get(CLASS_LOADER);
        String uri = (String) environment.get(CONTEXT_URI);
        if(classLoader == null) {
            throw new NoInitialContextException(
                "The environment lacks its mandatory " + ClassLoader.class.getName() + " entry"
            );
        } else {
            return getSubContext(this.contextName, classLoader, uri);
        }
    }

    public static Hashtable<String,Object> getEnvironment(
        ClassLoader classLoader,
        String uri
    ){
        Hashtable<String,Object> environment = new Hashtable<String,Object>();
        if (classLoader != null) {
            environment.put(CLASS_LOADER, classLoader);
        }
        if(uri != null) {
            environment.put(CONTEXT_URI, uri);
        }
        return environment;
        
    }
    
    /**
     * 
     * @param name
     * @param classLoader
     * @param uri
     * @return
     * @throws NamingException
     */
    protected abstract Context getSubContext(
        String name,
        ClassLoader classLoader,
        String uri
    ) throws NamingException;
    
    public static ClassLoaderContext newInitialContext(
        String urlPrefix
    ){
        Hashtable<String,Object> environment = new Hashtable<String,Object>();
        String urlPkgPrefixes = System.getProperty(Context.URL_PKG_PREFIXES);
        if(urlPkgPrefixes != null) {
            environment.put(
                Context.URL_PKG_PREFIXES, 
                urlPkgPrefixes
            );
        }
        return new ClassLoaderContext(environment, urlPrefix);
    }

    /**
     * 
     */
    private final String contextName;
    
    /**
     * The context name environment entry
     */
    private static final String CONTEXT_URI = "org.openmdx.naming.context.uri";

    /**
     * The class loader environment entry
     */
    private static final String CLASS_LOADER = ClassLoader.class.getName();

}
