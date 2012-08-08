/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: SCTSServlet.java,v 1.8 2007/04/02 00:56:05 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.8 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2007/04/02 00:56:05 $
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
package org.openmdx.syncml.scts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.engine.SyncDatabase;
import org.openmdx.syncml.engine.SyncEngine;
import org.openmdx.syncml.engine.SyncOptions;
import org.openmdx.syncml.engine.SyncDatabase.DatabaseObject;
import org.openmdx.syncml.servlet.SyncServlet;

public class SCTSServlet extends SyncServlet {

    // -----------------------------------------------------------------------
    @Override
    protected SyncEngine createSyncEngine(
        SyncOptions options,
        HttpServletRequest request
    ) throws SmlException_t {
        return new SCTSSyncEngine(
            options,
            request.getRequestURL().append(";jsessionid=" + request.getSession().getId()).toString(),
            this.getDatabases(),
            this.getAnchors()
        );        
    }
    
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(
        HttpServletRequest request, 
        HttpServletResponse response
    ) throws ServletException, IOException {
        super.doPost(request, response);
        // Simulate modification of some objects
        if(System.currentTimeMillis() > this.touchObjectsAt) {
            for(SyncDatabase database : this.getDatabases().values()) {
                int count = 0;
                for(DatabaseObject object : database.getObjects()) {
                    object.touch(System.currentTimeMillis());
                    if(count > 20) break;
                    count++;
                }
            }
            this.touchObjectsAt = System.currentTimeMillis() + 60000L;
        }
    }

    // -----------------------------------------------------------------------
    @Override
    protected Map<String, SyncDatabase> createSyncDatabases(
    ) {
        Map<String, SyncDatabase> databases = new HashMap();
        databases.put(
            "Contacts", 
            new ContactsDatabase("Contacts")
        );
        databases.put(
            "Calendar", 
            new CalendarDatabase("Calendar")
        );
        databases.put(
            "Memo", 
            new MemoDatabase("Memo")
        );
        return databases;
    }

    // -----------------------------------------------------------------------
    private static final long serialVersionUID = -3074849855012209273L;
    
    private long touchObjectsAt = 0L;
            
}
