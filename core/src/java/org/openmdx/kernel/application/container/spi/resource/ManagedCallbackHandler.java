/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagedCallbackHandler.java,v 1.2 2005/07/07 15:26:50 hburger Exp $
 * Description: Managed Callback Handler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/07 15:26:50 $
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

package org.openmdx.kernel.application.container.spi.resource;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.openmdx.compatibility.kernel.application.cci.Classes;

/**
 * Managed <code>CallbackHandler</code>
 */
public class ManagedCallbackHandler implements CallbackHandler {

    /**
     * Constructor
     *
     * @param applicationClientClassLoader
     * @param applicationClientCallbackHandler
     */
    public ManagedCallbackHandler(
        ClassLoader applicationClientClassLoader,
        String callbackHandlerClass
    ) {
        this.classLoader = applicationClientClassLoader;
        this.callbackHandlerClass = callbackHandlerClass; 
    }

    /**
     * The application client's <code>ClassLoader</code>
     */
    private final ClassLoader classLoader;

    /**
     * The application client's <code>CallbackHandler</code>
     */
    private final String callbackHandlerClass;
    
    /**
     * The application client's <code>CallbackHandler</code>
     */
    private CallbackHandler callbackHandler = null;


    //------------------------------------------------------------------------
    // Implements CallbackHandler
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the application client's <code>CallbackHandler</code>
     * 
     * @return an instance of the application client's 
     * <code>CallbackHandler</code>
     * @throws IOException 
     */
    private synchronized CallbackHandler getDelegate(
    ) throws IOException{        
        if(this.callbackHandler == null) try {
            this.callbackHandler = (CallbackHandler) Classes.getApplicationClass(
                this.callbackHandlerClass
            ).newInstance();
        } catch (Exception exception) {
            throw new IOException(
                "Could not acquire application client callback handler of type " + 
                this.callbackHandlerClass + ": " + exception.getMessage()
            );
        }
        return this.callbackHandler; 
    }
    
    /* (non-Javadoc)
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException {
        Thread thread = Thread.currentThread();
        Object callerContext = thread.getContextClassLoader();
        thread.setContextClassLoader(this.classLoader);
        try {
            getDelegate().handle(callbacks);
        } finally {
            thread.setContextClassLoader((ClassLoader) callerContext);
        }
    }

}
