/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RemoteAuthenticationHandler.java,v 1.2 2006/05/22 08:54:28 hburger Exp $
 * Description: Remote Authentication Handler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/05/22 08:54:28 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */

package org.openmdx.security.auth.login.iiop;

import java.util.Hashtable;

import javax.naming.Context;
import javax.servlet.ServletException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ShareableConnectionHolder;
import org.openmdx.security.auth.servlet.spi.AbstractRemoteAuthenticationHandler;

/**
 * Remote Authentication Handler
 */
public class RemoteAuthenticationHandler extends AbstractRemoteAuthenticationHandler {

    /**
     * This handler's Dataprovider Connection Factory Instance
     */
    private Dataprovider_1ConnectionFactory connectionFactory;
    
    /* (non-Javadoc)
     * @see org.openmdx.security.auth.servlet.spi.AbstractAuthenticationHandler#init()
     */
    public void init() throws ServletException {
        // 
        // URL
        //
        String providerURL = this.getInitParameter(
            "realm-provider-url", 
            realmProviderURLDefault()
        ); 
        // 
        // JNDI Name
        //
        String jndiName = this.getInitParameter(
            "realm-jndi-name", 
            realmJNDINameDefault()
       );
        // 
        // Initial Context Factory
        //
        String contextFactory = this.getInitParameter(
            "realm-initial-context-factory", 
            realmInitialContextFactoryDefault()            
       );
        // 
        // Connection Factory
        //
        Hashtable environment = new Hashtable();
        if(contextFactory != null) environment.put(
            Context.INITIAL_CONTEXT_FACTORY, 
            contextFactory
        );
        if(providerURL != null) environment.put(
            Context.PROVIDER_URL, 
            providerURL
        );
        this.connectionFactory = new Dataprovider_1ShareableConnectionHolder(
            new LateBindingConnection_1(
                jndiName,
                environment
            )
        );
        // 
        // Log in case of debug
        //
        if(isDebug()) {
            log("$Id: RemoteAuthenticationHandler.java,v 1.2 2006/05/22 08:54:28 hburger Exp $");
            log("realm-provider-url: " + providerURL);
            log("realm-jndi-name: " + jndiName);
            log("realm-initial-context-factory: " + contextFactory);
        }
        //
        // Complete initialization
        //
        super.init();
    }

    /* (non-Javadoc)
     * @see org.openmdx.security.auth.servlet.spi.AbstractRemoteAuthenticationHandler#getConnectionFactory()
     */
    protected Dataprovider_1ConnectionFactory getConnectionFactory(
    ) throws ServiceException {
        return this.connectionFactory;
    }

    /**
     * Provide the "realm-initial-context-factory" default value.
     * 
     * @return the "realm-initial-context-factory" default value
     */
    protected String realmInitialContextFactoryDefault(
    ){
        return "com.sun.jndi.cosnaming.CNCtxFactory";
    }

    /**
     * Provide the "realm-provider-url" default value.
     * 
     * @return the "realm-provider-url" default value
     */
    protected String realmProviderURLDefault(
    ){
        return null; // was "iiop://localhost:7101";
    }

    /**
     * Provide the "realm-jndi-name" default value.
     * 
     * @return the "realm-jndi-name" default value
     */
    protected String realmJNDNameDefault(
    ){
        return null; // was "org/openmdx/provider/gateway_1/NoOrNew";
    }

}
