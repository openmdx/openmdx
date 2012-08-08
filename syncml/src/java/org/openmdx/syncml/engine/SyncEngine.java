/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: SyncEngine.java,v 1.13 2007/04/02 23:40:58 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/04/02 23:40:58 $
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
 * * Neither the name of OMEX AG nor the names of the contributors
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
package org.openmdx.syncml.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlExec_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSearch_t;
import org.openmdx.syncml.SmlSequence_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlUtil;

public class SyncEngine implements SyncCallbackHandler {

    //-----------------------------------------------------------------------
    public SyncEngine(
        SyncOptions options,
        final String responseURI,
        Map<String, SyncDatabase> databases,
        SyncDatabase anchors
    ) throws SmlException_t {
        this.options = options;
        this.responseURI = responseURI;
        this.databases = databases;
        this.anchors = anchors;
    }
    
    //-----------------------------------------------------------------------
    public Date getSyncStartedAt(
    ) {
        return this.currentState.getSyncStartedAt();
    }
    
    //-----------------------------------------------------------------------
    public boolean serverMessageIsFinal(
    ) {
        return this.currentState.serverMessageIsFinal();
    }
    
    //-----------------------------------------------------------------------
    public boolean clientMessageIsFinal(
    ) {
        return this.currentState.clientMessageIsFinal();
    }
    
    //-----------------------------------------------------------------------
    public SmlSyncHdr_t getRequestHdr(
    ) {
        return this.currentState.getRequestHdr();
    }
    
    //-----------------------------------------------------------------------
    public Map<String, String[]> getSyncAnchors(
    ) {
        return this.currentState.getSyncAnchors();
    } 
    
    //-----------------------------------------------------------------------
    public void startMessage(
        SmlSyncHdr_t syncHdr
    ) throws SmlException_t {
        this.replyBuffer = new ByteArrayOutputStream(); 
        this.replyMessageBuilder = new MessageBuilder(
            this.options,
            this.replyBuffer
        );
        // Next state
        if(this.currentState == null) {
            this.currentState = new InitStateHandler(
                this.replyMessageBuilder, 
                this.responseURI,
                this.databases,
                this.anchors,
                new HashMap(),
                new Date()
            );
        }
        else if(this.currentState instanceof InitStateHandler) {
            if(((InitStateHandler)this.currentState).isAuthenticated()) {
                this.currentState = new SyncStateHandler(
                    this.replyMessageBuilder, 
                    this.responseURI,
                    this.databases,
                    this.anchors,
                    this.getSyncAnchors(),
                    this.getSyncStartedAt()
                );
            }
            else {
                this.currentState = new InitStateHandler(
                    this.replyMessageBuilder, 
                    this.responseURI,
                    this.databases,
                    this.anchors,
                    this.getSyncAnchors(),
                    this.getSyncStartedAt()
                );
            }
        }
        else if(this.currentState instanceof SyncStateHandler) {
            // New sessions
            String s1 = SmlUtil.smlPcdata2String(syncHdr.sessionID);
            String s2 = SmlUtil.smlPcdata2String(this.getRequestHdr().sessionID);
            if(!s1.equals(s2)) {
                this.currentState = new InitStateHandler(
                    this.replyMessageBuilder, 
                    this.responseURI,
                    this.databases,
                    this.anchors,
                    new HashMap(),
                    new Date()
                );                
            }
            // Continue session
            else {
                this.currentState = new SyncStateHandler(
                    this.replyMessageBuilder,
                    (SyncStateHandler)this.currentState
                );
            }
        }
        else {
            throw new UnsupportedOperationException("Unsupported state " + this.currentState.getClass().getName());
        }
        // Start
        this.currentState.startMessage(syncHdr);
    };

    //-----------------------------------------------------------------------
    public void endMessage(boolean isFinal) throws SmlException_t {        
        this.currentState.endMessage(isFinal);
    };

    //-----------------------------------------------------------------------
    public void startSync(SmlSync_t smlSync) throws SmlException_t {
        this.currentState.startSync(smlSync);
    };

    //-----------------------------------------------------------------------
    public void endSync() throws SmlException_t {
        this.currentState.endSync();
    };

    //-----------------------------------------------------------------------
    public void startAtomic(SmlAtomic_t smlAtomic) throws SmlException_t {
        this.currentState.startAtomic(smlAtomic);
    };

    //-----------------------------------------------------------------------
    public void endAtomic() throws SmlException_t {
        this.currentState.endAtomic();
    };

    //-----------------------------------------------------------------------
    public void startSequence(SmlSequence_t smlSequence) throws SmlException_t {
        this.currentState.startSequence(smlSequence);
    };

    //-----------------------------------------------------------------------
    public void endSequence() throws SmlException_t {
        this.currentState.endSequence();
    };

    //-----------------------------------------------------------------------
    public void addCmd(SmlGenericCmd_t smlAdd) throws SmlException_t {
        this.currentState.addCmd(smlAdd);
    };

    //-----------------------------------------------------------------------
    public void alertCmd(SmlAlert_t smslAlert) throws SmlException_t {
        this.currentState.alertCmd(smslAlert);
    };

    //-----------------------------------------------------------------------
    public void deleteCmd(SmlGenericCmd_t smlDeleteCmd) throws SmlException_t {
        this.currentState.deleteCmd(smlDeleteCmd);
    };

    //-----------------------------------------------------------------------
    public void getCmd(SmlGetPut_t smlGet) throws SmlException_t {
        this.currentState.getCmd(smlGet);
    };

    //-----------------------------------------------------------------------
    public void putCmd(SmlGetPut_t smlPut) throws SmlException_t {
        this.currentState.putCmd(smlPut);
    };

    //-----------------------------------------------------------------------
    public void mapCmd(SmlMap_t smlMap) throws SmlException_t {
        this.currentState.mapCmd(smlMap);
    };

    //-----------------------------------------------------------------------
    public void resultsCmd(SmlResults_t smlResults) throws SmlException_t {
        this.currentState.resultsCmd(smlResults);
    };

    //-----------------------------------------------------------------------
    public void statusCmd(SmlStatus_t smlStatus) throws SmlException_t {
        this.currentState.statusCmd(smlStatus);
    };

    //-----------------------------------------------------------------------
    public void replaceCmd(SmlGenericCmd_t smlReplace) throws SmlException_t {
        this.currentState.replaceCmd(smlReplace);
    };

    //-----------------------------------------------------------------------
    public void copyCmd(SmlGenericCmd_t smlCopy) throws SmlException_t {
        this.currentState.copyCmd(smlCopy);
    };

    //-----------------------------------------------------------------------
    public void execCmd(SmlExec_t smlExec) throws SmlException_t {
        this.currentState.execCmd(smlExec);
    };

    //-----------------------------------------------------------------------
    public void searchCmd(SmlSearch_t smlSearch) throws SmlException_t {
        this.currentState.searchCmd(smlSearch);
    };

    //-----------------------------------------------------------------------
    public void handleError() throws SmlException_t {
        this.currentState.handleError();
    };

    //-----------------------------------------------------------------------
    public int getResponse(
        OutputStream response        
    ) throws SmlException_t {
        System.err.println("Response");
        System.err.println(this.replyBuffer);
        try {
            this.replyBuffer.close();
        } catch(IOException e) {}
        try {
            response.write(this.replyBuffer.toByteArray());
        } catch(IOException e) {}
        return this.replyBuffer.size();
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final String responseURI;
    protected final Map<String, SyncDatabase> databases;
    protected final SyncDatabase anchors;
    protected final SyncOptions options;
    protected SyncCallbackHandler currentState = null;
    protected ByteArrayOutputStream replyBuffer = null;
    protected MessageBuilder replyMessageBuilder = null;
    
}
