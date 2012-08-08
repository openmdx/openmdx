/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: InsecureExceptionHandler.java,v 1.2 2005/06/20 14:14:41 hburger Exp $
 * Description: Unsecure HttpExceptionHandler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/06/20 14:14:41 $
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.security.auth.servlet.cci.HttpExceptionHandler;
import org.openmdx.security.auth.servlet.spi.AbstractHandler;

/**
 * This <code>HttpExceptionHandler</code> implementation displays the whole
 * exception stack, which is not a good idea for a production environment!
 */
public class InsecureExceptionHandler 
    extends AbstractHandler 
    implements HttpExceptionHandler 
{

    /**
     * Constructor
     */
    public InsecureExceptionHandler() {
        super();
    }

    /**
     * 
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
    
    //------------------------------------------------------------------------
    // Implements HttpExceptionHandler
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.cci.HttpExceptionHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.login.LoginException, int)
     */
    public boolean handle(
        HttpServletRequest request,
        HttpServletResponse response, 
        LoginException exception, 
        int attempt
    ) throws IOException {
        Writer html = startDocument(response);
        startHead(html);
        endHead(html);
        startBody(html);
        if(attempt > 0) html.write(
            "<H2>Exception during login attempt # " + attempt + "</H2>"
        );
        html.write("<FIELDSET><LEGEND>");
        html.write(exception.getClass().getName());
        html.write("</LEGEND>");
        String message = exception.getMessage();
        if(message != null) {
            html.write("<P>&nbsp;");
            writeEncoded(html, message);
            html.write("</P><HR>");
        }
        html.write("<PRE>&nbsp;");
        writeEncoded(html, toString(exception));
        html.write("</PRE></FIELDSET>");
        endBody(html);
        endDocument(html);
        return true; // Do not retry
    }

    /**
     * Retrieve an <code>Exception</code>'s <code>String</code> 
     * representation.
     * 
     * @param exception the <code>LoginException</code> to be converted
     * 
     * @return the <code>Exception</code>'s <code>String</code> 
     * representation
     */
    private String toString(
        LoginException exception
    ){
        if(exception instanceof MultiLineStringRepresentation) {
            return exception.toString();
        } else {
            StringWriter trace = new StringWriter();
            exception.printStackTrace(new PrintWriter(trace));
            return trace.getBuffer().toString();
        }
    }
    
    
    //------------------------------------------------------------------------
    // Extends AbstractHandler
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractHandler#init()
     */
    protected void init() throws ServletException {
        this.title = super.getInitParameter(
            "title",
            "openMDX - Login"
        );
        if(isDebug()) {
            log("$Id: InsecureExceptionHandler.java,v 1.2 2005/06/20 14:14:41 hburger Exp $");
            log("title: " + this.title);
        }
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

}
