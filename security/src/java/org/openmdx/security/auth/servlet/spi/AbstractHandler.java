/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractHandler.java,v 1.6 2008/04/04 17:55:30 hburger Exp $
 * Description: Abstract Handler
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 17:55:30 $
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
package org.openmdx.security.auth.servlet.spi;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.text.conversion.HtmlEncoder;
import org.openmdx.security.auth.servlet.cci.HttpHandler;

/**
 * Abstract Handler
 */
public abstract class AbstractHandler implements HttpHandler, ServletConfig {

    /**
     * Constructor
     */
    protected AbstractHandler() {
    }
    
    /**
     * The <code>AbstractHandler</code>'s configuration delegate
     */
    private ServletConfig configuration;
    
    /**
     * Tells whether debugging is enabled or not
     */
    private boolean debug;

    /**
     * Tells whether debugging is enabled or not.
     * 
     * @return <code>true</code> if debugging is enabled
     */
    protected boolean isDebug(){
        return this.debug;
    }
    
    /**
     * Use the <code>Servlet</code>'s configuration to initialize the handler.
     * 
     * @throws ServletException
     */
    public void init(
       ServletConfig configuration
    ) throws ServletException {
        this.configuration = configuration;
        this.debug = getInitParameter(
            "debug",
            debugDefault()
        );
        init();
    }

    /**
     * A convenience method which can be overridden so that there's no need to 
     * call <code>super.init(config)</code>.
     * <p>
     * Instead of overriding <code>init(ServletConfig)</code>, simply override 
     * this method and it will be called by 
     * <code>AbstractHandler.init(ServletConfig)</code>. 
     */    
    protected void init(
    ) throws ServletException {
    }
    
    /**
     * Retrieve an init parameter or its default value
     * 
     * @param name the parameter name
     * @param defaultValue the parameter's default value
     * 
     * @return the init parameter or its default value if it where 
     * <code>null</code>
     */
    protected final String getInitParameter(
        String name,
        String defaultValue
    ){
        String value = this.getInitParameter(name);
        return value == null ? defaultValue : value;
    }
    
    /**
     * Retrieve an init parameter or its default value
     * 
     * @param name the parameter name
     * @param defaultValue the parameter's default value
     * 
     * @return the init parameter or ist default value if it where 
     * <code>null</code>
     */
    protected final int getInitParameter(
        String name,
        int defaultValue
    ){
        String value = this.getInitParameter(name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Retrieve an init parameter or its default value
     * 
     * @param name the parameter name
     * @param defaultValue the parameter's default value
     * 
     * @return the init parameter or ist default value if it where 
     * <code>null</code>
     */
    protected final long getInitParameter(
        String name,
        long defaultValue
    ){
        String value = this.getInitParameter(name);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    /**
     * Retrieve an init parameter or its default value
     * 
     * @param name the parameter name
     * @param defaultValue the parameter's default value
     * 
     * @return the init parameter or ist default value if it where 
     * <code>null</code>
     */
    protected final boolean getInitParameter(
        String name,
        boolean defaultValue
    ){
        String value = this.getInitParameter(name);
        return value == null ? defaultValue : Boolean.valueOf(value).booleanValue();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
     */
    public final String getInitParameter(String name) {
        return this.configuration.getInitParameter(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletConfig#getInitParameterNames()
     */
    public final Enumeration<?> getInitParameterNames(
    ){
        return this.configuration.getInitParameterNames();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletConfig#getServletContext()
     */
    public final ServletContext getServletContext(
    ){
        return this.configuration.getServletContext();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletConfig#getServletName()
     */
    public final String getServletName(
    ){
        return this.configuration.getServletName();
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.cci.HttpHandler#destroy()
     */
    public void destroy() {
        this.configuration = null;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
     */
    protected void log(String message, Throwable throwable) {
        this.configuration.getServletContext().log(
            this.configuration.getServletName() + ": " + message, 
            throwable
        );
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContext#log(java.lang.String)
     */
    protected void log(String message) {
        this.configuration.getServletContext().log(
            this.configuration.getServletName() + ": " + message
        );
    }
    
    /**
     * Start a document
     * 
     * @param response
     * @return a <code>Writer</code> for the document
     * 
     * @throws IOException 
     */
    protected Writer startDocument(
        HttpServletResponse response
    ) throws IOException{
        response.setContentType("text/html;charset=UTF-8");
        Writer html = response.getWriter();
        html.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><HTML>");
        return html;
    }

    /**
     * End a document
     * 
     * @param html
     * 
     * @throws IOException
     */
    protected void endDocument(
        Writer html
    ) throws IOException{
        html.write("</HTML>");
        html.flush();
    }
    
    /**
     * Write the HEAD's start tag including its META element.
     * 
     * @param html
     * 
     * @throws IOException 
     */
    protected void startHead(
         Writer html
    ) throws IOException{
        html.write("<HEAD><META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    }
         
    /**
     * Write the HEAD's end tag.
     * 
     * @param html
     * 
     * @throws IOException 
     */
    protected void endHead(
         Writer html
    ) throws IOException{
        html.write("</HEAD>");
    }

    /**
     * Write the BODY's start tag.
     * 
     * @param html
     * 
     * @throws IOException
     */
    protected void startBody(
        Writer html
    ) throws IOException{
        html.write("<BODY>");
    }

    /**
     * Write the BODY's start tag.
     * 
     * @param html
     * 
     * @throws IOException
     */
    protected void endBody(
        Writer html
    ) throws IOException{
        html.write("</BODY>");
    }

    /**
     * Apply HTTP encoding to the text before writing it.
     * 
     * @param html
     * @param text
     * 
     * @throws IOException
     * @throws NullPointerException if html is <code>null</code>
     */
    protected static void writeEncoded(
        Writer html,
        String text
    ) throws IOException {
        if(text != null) html.write(HtmlEncoder.encode(text, false));
    }

    /**
     * Provide the "debug" default value.
     * 
     * @return the "debug" default value
     */
    protected boolean debugDefault(
    ){
        return false;
    }
    
}

