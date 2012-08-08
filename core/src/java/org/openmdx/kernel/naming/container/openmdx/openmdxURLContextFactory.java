/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: openmdxURLContextFactory.java,v 1.4 2008/01/12 02:45:29 hburger Exp $
 * Description: java URL Context Factory
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/12 02:45:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.naming.container.openmdx;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.ObjectFactory;

import org.openmdx.kernel.naming.spi.ClassLoaderContext;
import org.openmdx.kernel.naming.spi.ClassLoadertContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * java URL Context Factory
 */
public class openmdxURLContextFactory implements ObjectFactory {

    /**
     * Creates a new context and assigns it to the specified class loader 
     * 
     * @param contextName 
     * @param classLoader
     */
    static Context createContext(
        String contextName, 
        ClassLoader classLoader
    ){
        try {
            return initialContext.getContext(contextName, classLoader);
        } catch (NamingException exception) {
            logger.error(
                "Initialization of class loader specific context \"" +
				contextName + "\" in namespace \"" + URL_PREFIX + "\" failed",            		
            	exception
			);
            return null;
        }
    }

    /**
     * Create a new Context's instance.
     */
    public Object getObjectInstance(
       Object _object, 
       Name name, 
       Context nameCtx,
       Hashtable<?,?> environment
    ) throws NamingException {
        Object object = _object;
        if(object instanceof Object[]){
            Object[] urls = (Object[]) object;
            if(urls.length == 0) throw new NoInitialContextException("URL array is empty");
            object = urls[0]; // Just take the first of the equivalent URLs
        }
        if(object == null){
            return initialContext;
        } else if(object instanceof String){
            String url = (String) object;
            if(!url.startsWith(URL_PREFIX)) throw new NoInitialContextException(
                URL_SCHEME + " URL scheme expected: " + url
            );
            return initialContext.lookup(url);
        } else { 
            throw new NoInitialContextException(
                URL_SCHEME + " URL supports String object only: " + object.getClass().getName()
            );
        }
    }

    /**
     * To log instantiation failures
     */
    final static private Logger logger = LoggerFactory.getLogger(openmdxURLContextFactory.class);
    
    /**
     * The URL scheme supported by this factory
     */
    final static public String URL_SCHEME = "openmdx";
    
    /**
     * The URL prefix supported by this factory
     */
    final static public String URL_PREFIX = URL_SCHEME + ':';
    
    /**
     * The container context.
     */
    final static public String CONTAINER_CONTEXT = "container";

    /**
     * The deployment manager's internal registry
     */
    final static public String APPLICATION_CONTEXT = "application";

    /**
     * The lightweight container's internal registry.
     */
    final static public String PRIVATE_CONTEXT = "private";

    /**
     * The URL scheme specific initial context
     */
    final static private ClassLoaderContext initialContext = ClassLoadertContextFactory.newInitialContext(URL_PREFIX);

}

