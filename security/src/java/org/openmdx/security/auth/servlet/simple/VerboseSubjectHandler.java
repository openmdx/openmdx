/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: VerboseSubjectHandler.java,v 1.2 2005/07/09 19:53:32 hburger Exp $
 * Description: SimpleSubjectHandler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/09 19:53:32 $
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
import java.io.Writer;
import java.security.Principal;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipal;

/**
 * Simple Subject Handler
 */
public class VerboseSubjectHandler extends SimpleSubjectHandler {

    /**
     * Constructor
     */
    public VerboseSubjectHandler() {
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
        super.init();
        this.title = super.getInitParameter(
            "title",
            "openMDX - Logged In"
        );
        if(isDebug()) {
            log("$Id: VerboseSubjectHandler.java,v 1.2 2005/07/09 19:53:32 hburger Exp $");
            log("title: " + this.title);
        }
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractHandler
    //------------------------------------------------------------------------
    
    /**
     * The title for succefull login messages
     */
    private String title;
    
    /**
     * Retrieve the title.
     * 
     * @return the <code>title</code>'s value
     */
    protected final String getTitle() {
        return this.title;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#beginHead(java.io.Writer)
     */
    protected void startHead(Writer html) throws IOException {
        super.startHead(html);
        html.write("<TITLE>");
        writeEncoded(html, getTitle());
        html.write("</TITLE>");
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractCallbackHandler#startBody(java.io.Writer)
     */
    protected void startBody(Writer html) throws IOException {
        super.startBody(html);
        html.write("<H1>");
        writeEncoded(html, getTitle());
        html.write("</H1>");
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
        Writer html = startDocument(response);
        startHead(html);
        endHead(html);
        startBody(html);
        html.write("<FIELDSET><LEGEND>Principals</LEGEND><UL>");
        for(
            Iterator i = subject.getPrincipals().iterator();
            i.hasNext();
        ){
            Principal p = (Principal) i.next();
            html.write("<li>Name = ");
            writeEncoded(html, p.getName());
            if(p instanceof GenericPrincipal) {
                GenericPrincipal g = (GenericPrincipal) p;
                html.write("<br>Type = "); writeEncoded(html, g.getType());
                html.write("<br>Identity = "); writeEncoded(html, g.getIdentity());
            }
            html.write(")");
        }
        html.write("</UL></FIELDSET>");
        try {
            Cookie cookie = getCookie(subject);
            html.write("<FIELDSET><LEGEND>Cookie</LEGEND><UL>");
            html.write("<li>Comment = "); writeEncoded(html, cookie.getComment());
            html.write("<li>Domain = "); writeEncoded(html, cookie.getDomain());
            html.write("<li>MaxAge = " + cookie.getMaxAge() + " s");
            html.write("<li>Name = "); writeEncoded(html, cookie.getName());
            html.write("<li>Path = "); writeEncoded(html, cookie.getPath());
            html.write("<li>Secure = " + cookie.getSecure());
            html.write("<li>Version = " + cookie.getVersion());
            html.write("<li>Value.length() = " + cookie.getValue().length());
            html.write("</UL></FIELDSET>");
            response.addCookie(cookie);
        } catch (LoginException exception) {
            throw new ExtendedIOException(exception);
        }
        endBody(html);
        endDocument(html);
    }

}
