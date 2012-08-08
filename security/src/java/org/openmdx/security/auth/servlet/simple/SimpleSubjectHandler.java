/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SimpleSubjectHandler.java,v 1.9 2006/01/12 00:07:02 hburger Exp $
 * Description: SimpleSubjectHandler
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/12 00:07:02 $
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

package org.openmdx.security.auth.servlet.simple;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.security.pki.KeyProvider;
import org.openmdx.security.auth.servlet.cci.HttpHandler;
import org.openmdx.security.auth.servlet.spi.AbstractSubjectHandler;

/**
 * Simple Subject Handler
 */
public class SimpleSubjectHandler extends AbstractSubjectHandler {

    /**
     * Constructor
     */
    public SimpleSubjectHandler() {
        super();
    }
    
    //------------------------------------------------------------------------
    // Extends AbstractHandler
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractHandler#init()
     */
    protected void init(
    ) throws ServletException {
        String privateKeyProvider = getInitParameter(
            "private-key-provider",
            SimplePrivateKeyProvider.class.getName()
        );
        try {
            this.privateKeyProvider = (KeyProvider) Classes.getApplicationClass(privateKeyProvider).newInstance();
            if(isDebug()) {
                log("$Id: SimpleSubjectHandler.java,v 1.9 2006/01/12 00:07:02 hburger Exp $");
                log("private-key-provider: " + privateKeyProvider);
            }
        } catch (Exception exception) {
            throw (UnavailableException) Throwables.initCause(
                new UnavailableException(
                    "Private key provider acquisition failed"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("info", "$Id: SimpleSubjectHandler.java,v 1.9 2006/01/12 00:07:02 hburger Exp $"),
                    new BasicException.Parameter("name", getServletName()),
                    new BasicException.Parameter("private-key-provider", privateKeyProvider),
                }, null
            );
        }
        if(this.privateKeyProvider instanceof HttpHandler) ((HttpHandler)this.privateKeyProvider).init(this);
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractSubjectHandler
    //------------------------------------------------------------------------
    
    /**
     * The <code>SimpleSubjectHandler</code>'s private key provider
     */
    private KeyProvider privateKeyProvider;
    
    /* (non-Javadoc)
     * @see org.openmdx.security.auth.servlet.spi.AbstractSubjectHandler#getKeyProvider()
     */
    protected KeyProvider getKeyProvider() {
        return this.privateKeyProvider;
    }

    
    //------------------------------------------------------------------------
    // Implements HttpSubjectHandler
    //------------------------------------------------------------------------    

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.cci.HttpSubjectHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.Subject)
     */
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response, 
        Subject subject
    ) throws IOException {
        try {
            response.addCookie(getCookie(subject));
        } catch (LoginException exception) {
            throw new ExtendedIOException(exception);
        }
    }


}
