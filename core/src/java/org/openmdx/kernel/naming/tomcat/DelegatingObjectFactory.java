/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DelegatingObjectFactory.java,v 1.7 2008/01/09 15:55:07 hburger Exp $
 * Description: Delegating Object Factory
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/09 15:55:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.kernel.naming.tomcat;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.initial.ContextFactory;

/**
 * This JNDI Object Factory delegates to a remote JNDI context
 */
public class DelegatingObjectFactory 
  implements ObjectFactory
{

    /**
     * Constructor
     */
	public DelegatingObjectFactory(
	) {
	    super();
	}      

	/* (non-Javadoc)
     * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
     */
	public Object getObjectInstance(
	    Object obj,
	    Name name,
	    Context nameCtx,
	    Hashtable<?, ?> environment
	) throws Exception {
	    Reference reference = (Reference) obj;
	    Enumeration<?> configuration = reference.getAll();
	    Hashtable<String, String> delegateEnvironment = new Hashtable<String, String>();
	    String jndiName = null;
	    //
	    // Process Configuration
        //
	    while (configuration.hasMoreElements()) {
	        RefAddr property = (RefAddr) configuration.nextElement();
	        String key = property.getType();
	        String value = (String) property.getContent(); 
	        if (OBJECT_FACTORY.equals(key)){
	            // Used by Tomcat to identify the object factory class
	        } else if (JNDI_NAME.equals(key)) {
	            jndiName = value;
	        } else {
	            delegateEnvironment.put(key, value);
	        }
	    }
        // 
	    // Validate Configuration
	    //
	    if (jndiName == null) throw new ConfigurationException(
	        "No '" + JNDI_NAME + "' configured for resource factory " + this.getClass().getName()
	    );
	    if(!delegateEnvironment.containsKey(Context.INITIAL_CONTEXT_FACTORY)){
	        delegateEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, ContextFactory.class.getName());
	    }
	    if (!delegateEnvironment.containsKey(Context.PROVIDER_URL)){
	        if(ContextFactory.class.getName().equals(delegateEnvironment.get(Context.INITIAL_CONTEXT_FACTORY))){
		        delegateEnvironment.put(
		            Context.PROVIDER_URL, 
		            "//localhost:" + Contexts.getRegistryPort() + '/' + Contexts.getNamingService()
		        );
	        } else throw new ConfigurationException(
		        "No '" + Context.PROVIDER_URL + "' configured for resource factory " + this.getClass().getName()
		    );
	    }
        //
	    // Delegate Lookup
	    // 
        Context initialContext = new InitialContext(delegateEnvironment);
        try {
            return initialContext.lookup(jndiName);
        } finally {
            initialContext.close();
        }
	}

	/**
	 * Delegate JNDI Name Configuration Entry
	 */
	protected static final String JNDI_NAME = "jndiName";

	/**
	 * Tomcat given Object Factory Configuration Entry
	 */
	protected static final String OBJECT_FACTORY = "factory";

}
