/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ContextFactory.java,v 1.5 2008/01/10 15:26:57 hburger Exp $
 * Description: openMDX Native Context Factory
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/10 15:26:57 $
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
package org.openmdx.kernel.naming.initial;

import java.rmi.Naming;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.naming.spi.rmi.Context_1_0;
import org.openmdx.kernel.naming.spi.rmi.RelocatableContext;

/**
 * openMDX Native Context Factory
 */
public class ContextFactory implements javax.naming.spi.InitialContextFactory {

    /**
     * Constructor
     */
    public ContextFactory() {
        super();
    }
    
    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(
        Hashtable<?,?> environment
    ) throws NamingException {
    	String providerUrl = (String) environment.get(Context.PROVIDER_URL);
    	Context shared;
        if(
        	providerUrl != null &&
			!providerUrl.equals(getProviderURL())
		) synchronized(remoteContexts) {
        	shared = remoteContexts.get(providerUrl);
        	if(shared == null) try {
				remoteContexts.put(
				    providerUrl, 
				    shared = new RelocatableContext(
	                    (Context_1_0) Naming.lookup(providerUrl),
	                    ""
	                )
				 );
			} catch (Exception exception) {
				throw (NamingException) new NoInitialContextException(
					"Service unavailable: " + providerUrl
				).initCause(
				    exception
				);
        	}
        } else if (LightweightContainer.hasInstance()) {
        	shared = LightweightContainer.getInstance().getContainerContext();
        } else throw new NoInitialContextException(
		    "Need to specify the provider URL in an environment or system property: " + 
		    Context.PROVIDER_URL
        );
        Context context = (Context) shared.lookup("");
        for(Map.Entry<?,?> entry : environment.entrySet()){
        	context.addToEnvironment((String) entry.getKey(),entry.getValue());
        }       
        return context;
    }
    
    /**
     * Cache remote initial contexts
     */
    private final static Map<String,Context> remoteContexts = new HashMap<String,Context>();

    
    //------------------------------------------------------------------------
    // Provider URL
    //------------------------------------------------------------------------
    
	/**
	 * Get the Naming Service's Provider URL.
	 * 
	 * @return the Naming Service's Provider URL
	 */
	public static String getProviderURL(
	){
		return LightweightContainer.hasInstance() ?
		    LightweightContainer.getInstance().getProviderURL() :
		    null;
	}

	/**
	 * Tells whether the Naming Service's is accessible through RMI.
	 * 
	 * @return <code>true</code> if the Naming Service is accessible through RMI
	 */
	public static boolean hasProviderURL(
	){
		return getProviderURL() != null;		
	}

}
