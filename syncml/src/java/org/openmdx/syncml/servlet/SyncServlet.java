/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: SyncServlet.java,v 1.4 2007/04/02 00:56:04 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.4 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2007/04/02 00:56:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.syncml.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlProcessMode_t;
import org.openmdx.syncml.engine.MessageParser;
import org.openmdx.syncml.engine.SyncDatabase;
import org.openmdx.syncml.engine.SyncEngine;
import org.openmdx.syncml.engine.SyncOptions;
import org.openmdx.syncml.xlt.SmlEncoding_t;

public class SyncServlet extends HttpServlet {

    // -----------------------------------------------------------------------
    protected byte[] copyToByteArray(
        InputStream stream
    ) throws IOException {
        ByteArrayOutputStream sb = new ByteArrayOutputStream();
        int b = 0;
        while((b = stream.read()) >= 0) {
            sb.write(b);
        }
        return sb.toByteArray();        
    }
    
    // -----------------------------------------------------------------------
    protected SyncOptions createSyncOptions(
        byte[] request
    ) {
        SyncOptions syncOptions = new SyncOptions();
        if(request[0] == '<') {
            syncOptions.encoding = SmlEncoding_t.SML_XML;
        }
        else {
            syncOptions.encoding = SmlEncoding_t.SML_WBXML;
        }
        return syncOptions;
    }
    
    //-----------------------------------------------------------------------
    protected Map<String, SyncDatabase> createSyncDatabases(
    ) {
        Map<String, SyncDatabase> databases = new HashMap();
        databases.put(
            "Contacts", 
            new SyncDatabase("Contacts")
        );
        databases.put(
            "Calendar", 
            new SyncDatabase("Calendar")
        );
        databases.put(
            "Memo", 
            new SyncDatabase("Memo")
        );
        return databases;
    }
    
    //-----------------------------------------------------------------------
    protected SyncDatabase createAnchorsDatabase(
    ) {
        return new SyncDatabase("Anchors");
    }
    
    // -----------------------------------------------------------------------
    protected SyncEngine createSyncEngine(
        SyncOptions options,
        HttpServletRequest request
    ) throws SmlException_t {
        HttpSession session = request.getSession();
        return new SyncEngine(
            options,
            request.getRequestURL().append(";jsessionid=" + session.getId()).toString(),
            this.getDatabases(),
            this.getAnchors()
        );
    }
    
    //-----------------------------------------------------------------------
    protected Map<String, SyncDatabase> getDatabases(
    ) {
        if(this.databases == null) {
            this.databases = this.createSyncDatabases();
        }
        return this.databases;
    }
    
    //-----------------------------------------------------------------------
    protected SyncDatabase getAnchors(
    ) {
        if(this.anchors == null) {
            this.anchors = this.createAnchorsDatabase();
        }
        return this.anchors;
    }
    
    // -----------------------------------------------------------------------
    private SyncEngine getSyncEngine(
        SyncOptions options,
        HttpServletRequest request
    ) throws SmlException_t {
        String key = SyncServlet.class.getName() + ":SyncEngine";
        HttpSession session = request.getSession();
        SyncEngine syncEngine = (SyncEngine)session.getAttribute(key);
        if(syncEngine == null) {
            syncEngine = this.createSyncEngine(
                options,
                request
            );
            session.setAttribute(
                key, 
                syncEngine
            );
        }
        return syncEngine;
    }
    
    // -----------------------------------------------------------------------
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        this.doPost(request, response);
    }

    // -----------------------------------------------------------------------
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            if(session == null) {
                session = request.getSession(true);
            }          
            byte[] requestAsByteArray = this.copyToByteArray(request.getInputStream());
            System.out.println();
            System.out.println("Request");
            System.out.println(new String(requestAsByteArray));
            SyncOptions options = this.createSyncOptions(
                requestAsByteArray
            );
            SyncEngine syncEngine = this.getSyncEngine(
                options,
                request
            );
            MessageParser messageParser = new MessageParser(
                syncEngine,
                options,
                requestAsByteArray
            );
            boolean hasMore = true;
            while(hasMore) {
                hasMore = messageParser.parse(
                    SmlProcessMode_t.SML_ALL_COMMANDS
                );
            }                
            int length = syncEngine.getResponse(response.getOutputStream());                
            response.setContentType("text/xml");
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLength(length); // suppress chunking
        } 
        catch (Exception e) {
            new ServiceException(e).log();
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    // -----------------------------------------------------------------------
    private static final long serialVersionUID = -383856706933338965L;
    
    private Map<String, SyncDatabase> databases = null;
    private SyncDatabase anchors = null;
        
}
