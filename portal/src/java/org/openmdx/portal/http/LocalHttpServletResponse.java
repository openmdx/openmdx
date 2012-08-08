/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: LocalHttpServletResponse.java,v 1.5 2008/08/12 16:38:08 wfro Exp $
 * Description: BirtReportServlet
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:08 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class LocalHttpServletResponse 
    implements HttpServletResponse {
       
    //-----------------------------------------------------------------------
    public LocalHttpServletResponse(
        HttpServletResponse response,
        OutputStream buffer
    ) {
//      this.response = response;
        this.buffer = new LocalHttpServletOutputStream(buffer);
        this.writer = new PrintWriter(this.buffer);
    }    

    public void flushBuffer() throws IOException {
        this.writer.flush();
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return this.encoding;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return this.buffer;
    }

    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void resetBuffer() {
    }

    public void setBufferSize(int bufferSize) {
//      this.bufferSize = bufferSize;
    }

    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setContentLength(int contentLength) {
//      this.contentLength = contentLength;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void addCookie(Cookie arg0) {
        // TODO Auto-generated method stub
        
    }

    public void addDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub
        
    }

    public void addHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub
        
    }

    public void addIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }

    public boolean containsHeader(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public String encodeRedirectURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeRedirectUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeURL(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeUrl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void sendError(int arg0) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public void sendError(int arg0, String arg1) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public void sendRedirect(String arg0) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public void setDateHeader(String arg0, long arg1) {
        // TODO Auto-generated method stub
        
    }

    public void setHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub
        
    }

    public void setIntHeader(String arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }

    public void setStatus(int arg0) {
        // TODO Auto-generated method stub
        
    }

    public void setStatus(int arg0, String arg1) {
        // TODO Auto-generated method stub
        
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
//  private final HttpServletResponse response;
    private final LocalHttpServletOutputStream buffer;
    private final PrintWriter writer;
    private Locale locale = new Locale("en_US");
    private String contentType = "text/xml";
//  private int contentLength = 0;
    private String encoding = "UTF-8";
//  private int bufferSize = 0;

}
