/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractURLConnection.java,v 1.3 2007/12/13 18:19:18 hburger Exp $
 * Description: Dekegating URL connection
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/13 18:19:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.url.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.Map;

/**
 * An delegating URLConnection support class.
 *
 * @version <tt>$Revision: 1.3 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class AbstractURLConnection
    extends URLConnection
{
    /**
     * Constructor 
     *
     * @param url
     * 
     * @throws MalformedURLException
     * @throws IOException
     */
    protected AbstractURLConnection(
        final URL url
    ) throws MalformedURLException, IOException {
       super(url);
       delegate = makeDelegateUrlConnection(makeDelegateUrl(url));
    }

    /**
     * To be prepended to the <code>java.protocol.handler.pkgs</code> system property.
     */
    public static final String PROTOCOL_HANDLER_PKG = AbstractURLConnection.class.getPackage().getName();
    
    /**
     * The delegate <code>URLConnection</code>
     */
    protected final URLConnection delegate;

    /**
     * This method must be by a subclass.
     * 
     * @param url
     * 
     * @return the delegate <code>URL</code>
     * 
     * @throws MalformedURLException
     * @throws IOException
     */
    protected abstract URL makeDelegateUrl(
        final URL url
    ) throws IOException;

    /**
     * This method may be overridden by a subclass.
     * 
     * @param url the delegate connection's <code>URL</code>
     * 
     * @return the delegate <code>URLConnection</code>
     * 
     * @throws IOException
     */
    protected URLConnection makeDelegateUrlConnection(
       final URL url
    ) throws IOException {
       return url.openConnection();
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#toString()
     */
    public String toString() {
        return super.toString() + "{ " + this.delegate + " }";
    }

    /**
     * @param key
     * @param value
     * @see java.net.URLConnection#addRequestProperty(java.lang.String, java.lang.String)
     */
    public void addRequestProperty(String key, String value) {
        this.delegate.addRequestProperty(key, value);
    }
    
    /**
     * @throws IOException
     * @see java.net.URLConnection#connect()
     */
    public void connect()
        throws IOException {
        this.delegate.connect();
    }
    
    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return this.delegate.equals(obj);
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getAllowUserInteraction()
     */
    public boolean getAllowUserInteraction() {
        return this.delegate.getAllowUserInteraction();
    }
    
    /**
     * @return
     * @throws IOException
     * @see java.net.URLConnection#getContent()
     */
    public Object getContent()
        throws IOException {
        return this.delegate.getContent();
    }
    
    /**
     * @param classes
     * @return
     * @throws IOException
     * @see java.net.URLConnection#getContent(java.lang.Class[])
     */
    public Object getContent(Class[] classes)
        throws IOException {
        return this.delegate.getContent(classes);
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getContentEncoding()
     */
    public String getContentEncoding() {
        return this.delegate.getContentEncoding();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getContentLength()
     */
    public int getContentLength() {
        return this.delegate.getContentLength();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getContentType()
     */
    public String getContentType() {
        return this.delegate.getContentType();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getDate()
     */
    public long getDate() {
        return this.delegate.getDate();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getDefaultUseCaches()
     */
    public boolean getDefaultUseCaches() {
        return this.delegate.getDefaultUseCaches();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getDoInput()
     */
    public boolean getDoInput() {
        return this.delegate.getDoInput();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getDoOutput()
     */
    public boolean getDoOutput() {
        return this.delegate.getDoOutput();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getExpiration()
     */
    public long getExpiration() {
        return this.delegate.getExpiration();
    }
    
    /**
     * @param n
     * @return
     * @see java.net.URLConnection#getHeaderField(int)
     */
    public String getHeaderField(int n) {
        return this.delegate.getHeaderField(n);
    }
    
    /**
     * @param name
     * @return
     * @see java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getHeaderField(String name) {
        return this.delegate.getHeaderField(name);
    }
    
    /**
     * @param name
     * @param Default
     * @return
     * @see java.net.URLConnection#getHeaderFieldDate(java.lang.String, long)
     */
    public long getHeaderFieldDate(String name, long Default) {
        return this.delegate.getHeaderFieldDate(name, Default);
    }
    
    /**
     * @param name
     * @param Default
     * @return
     * @see java.net.URLConnection#getHeaderFieldInt(java.lang.String, int)
     */
    public int getHeaderFieldInt(String name, int Default) {
        return this.delegate.getHeaderFieldInt(name, Default);
    }
    
    /**
     * @param n
     * @return
     * @see java.net.URLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderFieldKey(int n) {
        return this.delegate.getHeaderFieldKey(n);
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getHeaderFields()
     */
    public Map getHeaderFields() {
        return this.delegate.getHeaderFields();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getIfModifiedSince()
     */
    public long getIfModifiedSince() {
        return this.delegate.getIfModifiedSince();
    }
    
    /**
     * @return
     * @throws IOException
     * @see java.net.URLConnection#getInputStream()
     */
    public InputStream getInputStream()
        throws IOException {
        return this.delegate.getInputStream();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getLastModified()
     */
    public long getLastModified() {
        return this.delegate.getLastModified();
    }
    
    /**
     * @return
     * @throws IOException
     * @see java.net.URLConnection#getOutputStream()
     */
    public OutputStream getOutputStream()
        throws IOException {
        return this.delegate.getOutputStream();
    }
    
    /**
     * @return
     * @throws IOException
     * @see java.net.URLConnection#getPermission()
     */
    public Permission getPermission()
        throws IOException {
        return this.delegate.getPermission();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getRequestProperties()
     */
    public Map getRequestProperties() {
        return this.delegate.getRequestProperties();
    }
    
    /**
     * @param key
     * @return
     * @see java.net.URLConnection#getRequestProperty(java.lang.String)
     */
    public String getRequestProperty(String key) {
        return this.delegate.getRequestProperty(key);
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getURL()
     */
    public URL getURL() {
        return this.delegate.getURL();
    }
    
    /**
     * @return
     * @see java.net.URLConnection#getUseCaches()
     */
    public boolean getUseCaches() {
        return this.delegate.getUseCaches();
    }
    
    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.delegate.hashCode();
    }
    
    /**
     * @param allowuserinteraction
     * @see java.net.URLConnection#setAllowUserInteraction(boolean)
     */
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        this.delegate.setAllowUserInteraction(allowuserinteraction);
    }
    
    /**
     * @param defaultusecaches
     * @see java.net.URLConnection#setDefaultUseCaches(boolean)
     */
    public void setDefaultUseCaches(boolean defaultusecaches) {
        this.delegate.setDefaultUseCaches(defaultusecaches);
    }
    
    /**
     * @param doinput
     * @see java.net.URLConnection#setDoInput(boolean)
     */
    public void setDoInput(boolean doinput) {
        this.delegate.setDoInput(doinput);
    }
    
    /**
     * @param dooutput
     * @see java.net.URLConnection#setDoOutput(boolean)
     */
    public void setDoOutput(boolean dooutput) {
        this.delegate.setDoOutput(dooutput);
    }
    
    /**
     * @param ifmodifiedsince
     * @see java.net.URLConnection#setIfModifiedSince(long)
     */
    public void setIfModifiedSince(long ifmodifiedsince) {
        this.delegate.setIfModifiedSince(ifmodifiedsince);
    }
    
    /**
     * @param key
     * @param value
     * @see java.net.URLConnection#setRequestProperty(java.lang.String, java.lang.String)
     */
    public void setRequestProperty(String key, String value) {
        this.delegate.setRequestProperty(key, value);
    }
    
    /**
     * @param usecaches
     * @see java.net.URLConnection#setUseCaches(boolean)
     */
    public void setUseCaches(boolean usecaches) {
        this.delegate.setUseCaches(usecaches);
    }

}
