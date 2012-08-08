/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: HttpCallbackHandler.java,v 1.4 2005/11/15 13:20:41 hburger Exp $
 * Description: HTTP Callback Handler
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/11/15 13:20:41 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.security.auth.servlet.cci;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP Callback Handler
 */
public interface HttpCallbackHandler extends HttpHandler {

    /**
     * One or two phase callback handling
     * 
     * @param request
     * @param response
     * @param callbacks
     * 
     * @return <code>true</code> in case of two-phase handling, i.e. if a
     * body has been added to the response and callback handling will be
     * resumed using handle(HttpServletRequest,Callback[]).
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    boolean handle(
        HttpServletRequest request,
        HttpServletResponse response,
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException;

    /**
     * One or second phase callback handling
     * 
     * @param request
     * @param callbacks
     * 
     * @param return <code>true</code> in case of successfull
     * reply processing, <code>false</code> if callbacks is <code>null</code>
     * or if the reply has been repeated.
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    boolean handle(
        HttpServletRequest request,
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException;
    
}
