/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ServletTestCase.java,v 1.15 2008/04/04 17:55:31 hburger Exp $
 * Description: ServletTestCase
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 17:55:31 $
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

package org.openmdx.test.security.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;
import junit.runner.Version;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class ServletTestCase extends TestCase {

    protected ServletTestCase() {
        super();
    }

    protected ServletTestCase(String name) {
        super(name);
    }

    //------------------------------------------------------------------------
    // Class TestConfig
    //------------------------------------------------------------------------

    protected class TestConfig implements ServletConfig {

        public TestConfig(
        ){
            this(Collections.EMPTY_MAP);
        }
        
		public TestConfig(
            Map initParameters
        ){
            this.servletContext = new TestContext(initParameters);
        }

        final ServletContext servletContext;
        
        public String getServletName() {
            return getName();
        }

        public Enumeration getInitParameterNames() {
            return getServletContext().getInitParameterNames();
        }

        public ServletContext getServletContext() {
            return this.servletContext;
        }

        public String getInitParameter(String name) {
            return getServletContext().getInitParameter(name);
        }
        
    }
    

    //------------------------------------------------------------------------
    // Class TestContext
    //------------------------------------------------------------------------

    protected class TestContext implements ServletContext {

        public String getContextPath() {
            // TODO Auto-generated method stub
            return null;
        }

        public TestContext(
            Map initParameters
        ){
            this.initParameters = new HashMap(initParameters);
        }
        
        private final Map initParameters;
        
        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
         */
        public Object getAttribute(String arg0) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getAttributeNames()
         */
        public Enumeration getAttributeNames() {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getContext(java.lang.String)
         */
        public ServletContext getContext(String arg0) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
         */
        public String getInitParameter(String name) {
            return (String) this.initParameters.get(name);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getInitParameterNames()
         */
        public Enumeration getInitParameterNames() {
            return Collections.enumeration(this.initParameters.keySet());
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getMajorVersion()
         */
        public int getMajorVersion() {
            return 2;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
         */
        public String getMimeType(String file) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getMinorVersion()
         */
        public int getMinorVersion() {
            return 4;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
         */
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
         */
        public String getRealPath(String path) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
         */
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getResource(java.lang.String)
         */
        public URL getResource(String path) throws MalformedURLException {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
         */
        public InputStream getResourceAsStream(String path) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
         */
        public Set getResourcePaths(String path) {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServerInfo()
         */
        public String getServerInfo() {
            return "JUnit/" + Version.id();
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServlet(java.lang.String)
         */
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServletContextName()
         */
        public String getServletContextName() {
            return TestHttpHandler.class.getName();
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServletNames()
         */
        public Enumeration getServletNames() {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#getServlets()
         */
        public Enumeration getServlets() {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#log(java.lang.Exception, java.lang.String)
         */
        public void log(Exception exception, String message) {
            log(message, exception);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#log(java.lang.String, java.lang.Throwable)
         */
        public void log(String message, Throwable throwable) {
            System.err.println(message);
            System.err.print('\t');
            throwable.printStackTrace(System.err);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#log(java.lang.String)
         */
        public void log(String message) {
            System.out.println(message);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String name) {
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String name, Object value) {
            // Not supported
        }

    }
    
    
    //------------------------------------------------------------------------
    // Class TestResponse
    //------------------------------------------------------------------------

    /**
     * Create a new response <code>File</code>
     * 
     * @return a newly created <code>File</code>
     * 
     * @throws IOException
     */
    protected File newFile(
    ) throws IOException{
        File file = File.createTempFile(getName()+'-', ".html");
        System.out.println(file);
        return file;
    }
    

    protected class TestResponse implements HttpServletResponse {

        private String characterEncoding = "UTF-8";
        
        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#flushBuffer()
         */
        public void flushBuffer() throws IOException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getBufferSize()
         */
        public int getBufferSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getCharacterEncoding()
         */
        public String getCharacterEncoding() {
            return this.characterEncoding;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getContentType()
         */
        public String getContentType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getLocale()
         */
        public Locale getLocale() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getOutputStream()
         */
        public ServletOutputStream getOutputStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#getWriter()
         */
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(newFile()), 
                    getCharacterEncoding()
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#isCommitted()
         */
        public boolean isCommitted() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#reset()
         */
        public void reset() {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#resetBuffer()
         */
        public void resetBuffer() {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setBufferSize(int)
         */
        public void setBufferSize(int arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
         */
        public void setCharacterEncoding(String encoding) {
            this.characterEncoding = encoding;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setContentLength(int)
         */
        public void setContentLength(int arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
         */
        public void setContentType(String arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
         */
        public void setLocale(Locale arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
         */
        public void addCookie(Cookie arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
         */
        public void addDateHeader(String arg0, long arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
         */
        public void addHeader(String arg0, String arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
         */
        public void addIntHeader(String arg0, int arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
         */
        public boolean containsHeader(String arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
         */
        public String encodeRedirectUrl(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
         */
        public String encodeRedirectURL(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
         */
        public String encodeUrl(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
         */
        public String encodeURL(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
         */
        public void sendError(int arg0, String arg1) throws IOException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#sendError(int)
         */
        public void sendError(int arg0) throws IOException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
         */
        public void sendRedirect(String arg0) throws IOException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
         */
        public void setDateHeader(String arg0, long arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
         */
        public void setHeader(String arg0, String arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
         */
        public void setIntHeader(String arg0, int arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
         */
        public void setStatus(int arg0, String arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletResponse#setStatus(int)
         */
        public void setStatus(int arg0) {
            // TODO Auto-generated method stub
            
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class TestRequest
    //------------------------------------------------------------------------
    
    protected String getContextPath(){
        return "/" + getClass().getName();        
    }
    
    protected class TestRequest implements HttpServletRequest {

        public TestRequest(){
            this(null, "POST", Collections.EMPTY_MAP);
        }
        
        public TestRequest(
             HttpSession session, 
             String method, 
             Map parameters
        ){
            this.session = session;
            this.method = method;
            this.parameters = parameters;
        }
        
        HttpSession session;
        final String method;
        final Map parameters;
        
        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getAuthType()
         */
        public String getAuthType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getContextPath()
         */
        public String getContextPath() {
            return ServletTestCase.this.getContextPath();
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getCookies()
         */
        public Cookie[] getCookies() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
         */
        public long getDateHeader(String arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
         */
        public String getHeader(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
         */
        public Enumeration getHeaderNames() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
         */
        public Enumeration getHeaders(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
         */
        public int getIntHeader(String arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getMethod()
         */
        public String getMethod() {
            return this.method;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getPathInfo()
         */
        public String getPathInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
         */
        public String getPathTranslated() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getQueryString()
         */
        public String getQueryString() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
         */
        public String getRemoteUser() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
         */
        public String getRequestedSessionId() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getRequestURI()
         */
        public String getRequestURI() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getRequestURL()
         */
        public StringBuffer getRequestURL() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getServletPath()
         */
        public String getServletPath() {
            return getContextPath() + '/' + ServletTestCase.this.getName();
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getSession()
         */
        public HttpSession getSession() {
            return getSession(true);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
         */
        public HttpSession getSession(boolean create) {
            if(create && this.session == null) this.session = new TestSession(true);
            return this.session;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
         */
        public Principal getUserPrincipal() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
         */
        public boolean isRequestedSessionIdFromCookie() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
         */
        public boolean isRequestedSessionIdFromUrl() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
         */
        public boolean isRequestedSessionIdFromURL() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
         */
        public boolean isRequestedSessionIdValid() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
         */
        public boolean isUserInRole(String arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
         */
        public Object getAttribute(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getAttributeNames()
         */
        public Enumeration getAttributeNames() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getCharacterEncoding()
         */
        public String getCharacterEncoding() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getContentLength()
         */
        public int getContentLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getContentType()
         */
        public String getContentType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getInputStream()
         */
        public ServletInputStream getInputStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getLocalAddr()
         */
        public String getLocalAddr() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getLocale()
         */
        public Locale getLocale() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getLocales()
         */
        public Enumeration getLocales() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getLocalName()
         */
        public String getLocalName() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getLocalPort()
         */
        public int getLocalPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
         */
        public String getParameter(String name) {
            String[] parameters = getParameterValues(name);
            return parameters == null ? null : parameters[0];
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getParameterMap()
         */
        public Map getParameterMap() {
            return this.parameters;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getParameterNames()
         */
        public Enumeration getParameterNames() {
            return Collections.enumeration(this.parameters.keySet());
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
         */
        public String[] getParameterValues(String name) {
            return (String[]) this.parameters.get(name);
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getProtocol()
         */
        public String getProtocol() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getReader()
         */
        public BufferedReader getReader() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
         */
        public String getRealPath(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getRemoteAddr()
         */
        public String getRemoteAddr() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getRemoteHost()
         */
        public String getRemoteHost() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getRemotePort()
         */
        public int getRemotePort() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
         */
        public RequestDispatcher getRequestDispatcher(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getScheme()
         */
        public String getScheme() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getServerName()
         */
        public String getServerName() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#getServerPort()
         */
        public int getServerPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#isSecure()
         */
        public boolean isSecure() {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String arg0) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String arg0, Object arg1) {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
         */
        public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
            // TODO Auto-generated method stub
            
        }
        
    }
    

    //------------------------------------------------------------------------
    // Class TestSession
    //------------------------------------------------------------------------

    final UUIDGenerator uuidGenerator = UUIDs.getGenerator();
    
    class TestSession implements HttpSession {
        
        public TestSession(
        ){
            this(false);
        }

        TestSession(
             boolean internal
        ){
            this.internal = internal;
        }
        
        final boolean internal;
        final long creationTime = System.currentTimeMillis();
        final String id = ServletTestCase.this.uuidGenerator.next().toString();
        final Map attributes = new HashMap();
        
        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
         */
        public Object getAttribute(String arg0) {
            return this.attributes.get(arg0);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getAttributeNames()
         */
        public Enumeration getAttributeNames() {
            return Collections.enumeration(this.attributes.keySet());
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getCreationTime()
         */
        public long getCreationTime() {
            return this.creationTime;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getId()
         */
        public String getId() {
            return this.id;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getLastAccessedTime()
         */
        public long getLastAccessedTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
         */
        public int getMaxInactiveInterval() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getServletContext()
         */
        public ServletContext getServletContext() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @deprecated
         * 
         * @see javax.servlet.http.HttpSession#getSessionContext()
         */
        public javax.servlet.http.HttpSessionContext getSessionContext() {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
         */
        public Object getValue(String arg0) {
            return getAttribute(arg0);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#getValueNames()
         */
        public String[] getValueNames() {
            return (String[]) this.attributes.keySet().toArray(
                 new String[this.attributes.size()]
            );
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#invalidate()
         */
        public void invalidate() {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#isNew()
         */
        public boolean isNew() {
            return this.internal;
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
         */
        public void putValue(String arg0, Object arg1) {
            setAttribute(arg0, arg1);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String arg0) {
            this.attributes.remove(arg0);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
         */
        public void removeValue(String arg0) {
            removeAttribute(arg0);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String arg0, Object arg1) {
            this.attributes.put(arg0, arg1);
        }

        /* (non-Javadoc)
         * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
         */
        public void setMaxInactiveInterval(int arg0) {
            // TODO Auto-generated method stub
        }
        
        
        
    }

    
    //------------------------------------------------------------------------
    // Class ServletExceptionMapper
    //------------------------------------------------------------------------

    /**
     * 
     */
    static final class ServletExceptionMapper 
        implements BasicException.Mapper
    {

        /**
         * 
         */
        public BasicException map(Throwable throwable) {
            return BasicException.toStackedException(
                ((ServletException)throwable).getRootCause(), 
                throwable
            );
        }       
        
    }

    static {
        BasicException.register(
             ServletException.class, 
             new ServletExceptionMapper()
        );
    }

}
