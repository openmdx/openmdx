/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: InitStateHandler.java,v 1.13 2007/04/02 23:40:57 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.13 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2007/04/02 23:40:57 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, CRIXP Corp., Switzerland
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
package org.openmdx.syncml.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlChal_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlItemList_t;
import org.openmdx.syncml.SmlItem_t;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataType_t;
import org.openmdx.syncml.SmlPcdata_t;
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSourceRefList_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlTargetOrSource_t;
import org.openmdx.syncml.SmlTargetRefList_t;
import org.openmdx.syncml.SmlUtil;
import org.openmdx.syncml.SmlVersion_t;
import org.openmdx.syncml.engine.SyncDatabase.DatabaseObject;
import org.openmdx.syncml.xlt.SmlMetInfAnchor_t;
import org.openmdx.syncml.xlt.SmlMetInfMetInf_t;

/**
 * Init allows the following invocations
 * <ul>
 *   <li>startMessage: message header
 *   <li>endMessage: 
 *   <li>alertCmd: database to be synchronized
 *   <li>putCmd: optional service capabilities
 *   <li>getCmd: requested service capabilities
 * </ul>
 *
 */
public class InitStateHandler extends DefaultStateHandler {

    //-----------------------------------------------------------------------
    public InitStateHandler(
        MessageBuilder messageBuilder,
        String respURI,
        Map<String, SyncDatabase> databases,
        SyncDatabase anchors,
        Map<String, String[]> syncAnchors,
        Date syncStartedAt
    ) {
        super(
            messageBuilder,
            respURI,
            databases,
            anchors,
            syncAnchors,
            syncStartedAt
        );        
        this.alertCmds = new ArrayList();
        this.getCmds = new ArrayList();
        this.putCmds = new ArrayList();
    }
    
    //-----------------------------------------------------------------------
    public boolean isAuthenticated(
    ) {
        return this.isAuthenticated;
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void startMessage(
        SmlSyncHdr_t syncHdr
    ) throws SmlException_t {
        this.requestHdr = syncHdr;
        if(syncHdr.meta != null) {
            String metaAsString = SmlUtil.smlPcdata2String(syncHdr.meta);
            int pos = 0;
            if((pos = metaAsString.indexOf(MAX_MSG_SIZE_TAG_BEGIN)) >= 0) {
                this.maxMessageSize = Integer.valueOf(
                    metaAsString.substring(pos + MAX_MSG_SIZE_TAG_BEGIN.length(), metaAsString.indexOf(MAX_MSG_SIZE_TAG_END))
                );
            }
        }
        this.syncStartedAt = new Date();
    }

    //-----------------------------------------------------------------------
    @Override
    public void alertCmd(
        SmlAlert_t smlAlert
    ) throws SmlException_t {
        this.alertCmds.add(smlAlert);
    }

    //-----------------------------------------------------------------------
    @Override
    public void getCmd(
        SmlGetPut_t smlGet
    ) throws SmlException_t {
        this.getCmds.add(smlGet);
    }

    //-----------------------------------------------------------------------
    @Override
    public void putCmd(
        SmlGetPut_t smlPut
    ) throws SmlException_t {
        this.putCmds.add(smlPut);
    }

    //-----------------------------------------------------------------------
    protected void processAlert(
        SmlAlert_t alertCmd
    ) throws SmlException_t {
        
        // Status for alert
        SmlStatus_t status = new SmlStatus_t();
        status.cmdID = this.getNextCmdID();
        status.msgRef = this.requestHdr.msgID;
        status.cmdRef = alertCmd.cmdID;
        status.cmd = SmlUtil.smlString2Pcdata("Alert");
        SmlSourceRefList_t sourceRefList = null;
        SmlTargetRefList_t targetRefList = null;
        SmlItemList_t itemList = alertCmd.itemList;
        SmlPcdata_t sourceDatabase = null;
        String targetDatabaseName = null;
        String clientAnchorLast = null;
        String clientAnchorNext = null;
        String clientName = this.getClientName();
        while(itemList != null) {
            // Source
            if(sourceRefList == null) {
                status.sourceRefList = sourceRefList = new SmlSourceRefList_t();                
            }
            else {
                sourceRefList.next = new SmlSourceRefList_t();
                sourceRefList = sourceRefList.next;
            }
            sourceRefList.sourceRef = itemList.item.source.locURI;
            sourceDatabase = itemList.item.source.locURI;
            // Target
            if(targetRefList == null) {
                status.targetRefList = targetRefList = new SmlTargetRefList_t();                
            }
            else {
                targetRefList.next = new SmlTargetRefList_t();                
                targetRefList = targetRefList.next;
            }
            targetRefList.targetRef = itemList.item.target.locURI;
            targetDatabaseName = SmlUtil.smlPcdata2String(itemList.item.target.locURI);
            // Meta
            if(itemList.item.meta != null) {
                String anchors = SmlUtil.smlPcdata2String(itemList.item.meta);
                if(anchors.indexOf("<Last>") >= 0) {
                    clientAnchorLast = anchors.substring(anchors.indexOf("<Last>") + 6, anchors.indexOf("</Last>"));
                }
                if(anchors.indexOf("<Next>") >= 0) {
                    clientAnchorNext = anchors.substring(anchors.indexOf("<Next>") + 6, anchors.indexOf("</Next>"));
                }
            }
            itemList = alertCmd.itemList.next;
        }
        // Reply next anchor in item list
        if(clientAnchorNext != null) {
            status.itemList = new SmlItemList_t();
            status.itemList.item = new SmlItem_t();
            SmlMetInfMetInf_t metInf = new SmlMetInfMetInf_t();
            status.itemList.item.data = new SmlPcdata_t();
            metInf.anchor = new SmlMetInfAnchor_t();
            metInf.anchor.next = SmlUtil.smlString2Pcdata(clientAnchorNext);
            status.itemList.item.data.contentType = SmlPcdataType_t.SML_PCDATA_EXTENSION;
            status.itemList.item.data.extension = SmlPcdataExtension_t.SML_EXT_METINF;
            status.itemList.item.data.content = metInf;
        }
        status.data = SmlUtil.smlString2Pcdata("200"); // OK
        this.messageBuilder.smlStatusCmd(status);
        
        // Alert the requested database if it exists
        if(
            (targetDatabaseName != null) && 
            (this.databases.get(targetDatabaseName) != null)
        ) {
            SmlAlert_t alertForSync = new SmlAlert_t();
            alertForSync.cmdID = this.getNextCmdID();
            DatabaseObject anchorItem = this.anchors.getObject(clientName);
            String serverAnchorLast = anchorItem == null
                ? null
                : anchorItem.getExternalId(SERVER_ANCHOR_PREFIX + targetDatabaseName);            
            String serverAnchorNext = DateFormat.getInstance().format(new Date());
            // If stored last client anchor exists and matches supplied last client anchor 
            // initiate a TWO_WAY otherwise a SLOW sync
            if(
                (anchorItem != null) && 
                (clientAnchorLast != null) && 
                clientAnchorLast.equals(anchorItem.getExternalId(CLIENT_ANCHOR_PREFIX + targetDatabaseName))
            ) {
                alertForSync.data = SmlUtil.smlString2Pcdata("200"); // TWO-WAY
            }
            else {
                alertForSync.data = SmlUtil.smlString2Pcdata("201"); // SLOW
            }
            alertForSync.itemList = new SmlItemList_t();
            alertForSync.itemList.item = new SmlItem_t();
            alertForSync.itemList.item.source = new SmlTargetOrSource_t();
            alertForSync.itemList.item.source.locURI = sourceDatabase;
            alertForSync.itemList.item.target = new SmlTargetOrSource_t();
            alertForSync.itemList.item.target.locURI = SmlUtil.smlString2Pcdata(targetDatabaseName);
            SmlMetInfMetInf_t metInf = new SmlMetInfMetInf_t();
            metInf.anchor = new SmlMetInfAnchor_t();
            metInf.anchor.next = SmlUtil.smlString2Pcdata(serverAnchorNext);
            metInf.anchor.last = SmlUtil.smlString2Pcdata(serverAnchorLast);
            alertForSync.itemList.item.meta = new SmlPcdata_t();
            alertForSync.itemList.item.meta.contentType = SmlPcdataType_t.SML_PCDATA_EXTENSION;
            alertForSync.itemList.item.meta.extension = SmlPcdataExtension_t.SML_EXT_METINF;
            alertForSync.itemList.item.meta.content = metInf;
            this.messageBuilder.smlAlertCmd(alertForSync);
            // Set sync anchors for current session. 
            // At session end the anchors are made persistent
            this.syncAnchors.put(
                CLIENT_ANCHOR_PREFIX + targetDatabaseName, 
                new String[]{clientAnchorLast, clientAnchorNext}
            );
            this.syncAnchors.put(
                SERVER_ANCHOR_PREFIX + targetDatabaseName, 
                new String[]{serverAnchorLast, serverAnchorNext}
            );
        }
    }
    
    //-----------------------------------------------------------------------
    protected String getServerInfo(
    ) {
        return 
            "<VerDTD>1.2</VerDTD>" +
            "<DevID>38628136</DevID>" +
            "<DevTyp>workstation</DevTyp>" +
            "<SupportNumberOfChanges/>" +
            "<SupportLargeObjs/>";        
    }
    
    //-----------------------------------------------------------------------
    protected void processGet(
        SmlGetPut_t getCmd
    ) throws SmlException_t {
        SmlResults_t results = new SmlResults_t();
        if(getCmd.meta != null) {
            String metaContent = new String((byte[])getCmd.meta.content);
            if(metaContent.indexOf("application/vnd.syncml-devinf+xml") >= 0) {
                results.cmdID = this.getNextCmdID();
                results.msgRef = this.requestHdr.msgID;
                results.cmdRef = getCmd.cmdID;
                results.meta = SmlUtil.smlString2Pcdata(
                    "<Type xmlns='syncml:metinf'>application/vnd.syncml-devinf+xml</Type>"
                );
                results.itemList = new SmlItemList_t();
                results.itemList.item = new SmlItem_t();
                String serverInfo = this.getServerInfo();
                String dataStoreInfo = "";
                for(SyncDatabase database : this.databases.values()) {
                    dataStoreInfo += database.getDataStoreInfo();
                }
                results.itemList.item.source = new SmlTargetOrSource_t();
                results.itemList.item.source.locURI = SmlUtil.smlString2Pcdata("./devinf11");
                results.itemList.item.data = SmlUtil.smlString2Pcdata(
                    "<DevInf xmlns='syncml:devinf'>" + 
                        serverInfo +
                        dataStoreInfo + 
                    "</DevInf>"
                );            
                this.messageBuilder.smlResultsCmd(results);
            }
        }
        
    }
    
    //-----------------------------------------------------------------------
    protected void processPut(
        SmlGetPut_t putCmd
    ) throws SmlException_t {
        SmlStatus_t status = new SmlStatus_t();
        status.cmdID = this.getNextCmdID();
        status.msgRef = this.requestHdr.msgID;
        status.cmdRef = putCmd.cmdID;
        status.cmd = SmlUtil.smlString2Pcdata("Put");
        SmlItemList_t itemList = putCmd.itemList;
        while(itemList != null) {
            SmlItem_t item = itemList.item;
            if((item.source != null) && (item.source.locURI != null)) {
                status.sourceRefList = new SmlSourceRefList_t();        
                status.sourceRefList.sourceRef = item.source.locURI;                
            }
            itemList = itemList.next;
        }
        status.data = SmlUtil.smlString2Pcdata("200");
        this.messageBuilder.smlStatusCmd(status);        
    }
    
    //-----------------------------------------------------------------------
    protected void processAuthentication(
    ) throws SmlException_t {
        // Authenticate if not already authenticated
        if(!this.isAuthenticated) {
            if(
                (this.requestHdr.cred != null) &&
                (this.requestHdr.cred.meta != null) &&
                (SmlUtil.smlPcdata2String(this.requestHdr.cred.meta).indexOf("syncml:auth-basic") >= 0) &&
                (this.requestHdr.cred.data != null)
            ) {
                String credential = SmlUtil.smlPcdata2String(this.requestHdr.cred.data);
                this.isAuthenticated = true;
            }
        }
        // Reply with status 212 (= authenticated)
        if(this.isAuthenticated) {
            SmlStatus_t authStatus = new SmlStatus_t();
            authStatus.cmdID = this.getNextCmdID();
            authStatus.msgRef = this.requestHdr.msgID;
            authStatus.cmdRef = SmlUtil.smlString2Pcdata("0");
            authStatus.cmd = SmlUtil.smlString2Pcdata("SyncHdr");
            authStatus.data = SmlUtil.smlString2Pcdata("212");
            this.messageBuilder.smlStatusCmd(authStatus);
        }
        // Reply with status 407 (= credentials missing)
        else {
            SmlStatus_t authStatus = new SmlStatus_t();
            authStatus.cmdID = this.getNextCmdID();
            authStatus.msgRef = this.requestHdr.msgID;
            authStatus.cmdRef = SmlUtil.smlString2Pcdata("0");
            authStatus.cmd = SmlUtil.smlString2Pcdata("SyncHdr");
            authStatus.chal = new SmlChal_t();
            authStatus.chal.meta = new SmlPcdata_t();
            authStatus.chal.meta.contentType = SmlPcdataType_t.SML_PCDATA_EXTENSION;
            authStatus.chal.meta.extension = SmlPcdataExtension_t.SML_EXT_METINF;
            SmlMetInfMetInf_t metInf = new SmlMetInfMetInf_t();
            metInf.format = SmlUtil.smlString2Pcdata("b64");
            metInf.type = SmlUtil.smlString2Pcdata("syncml:auth-basic");
            authStatus.chal.meta.content = metInf;
            authStatus.data = SmlUtil.smlString2Pcdata("407");
            this.messageBuilder.smlStatusCmd(authStatus);            
        }        
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void endMessage(
        boolean isFinal
    ) throws SmlException_t {

        // Header
        SmlSyncHdr_t replyHdr = new SmlSyncHdr_t();
        replyHdr.elementType = SmlProtoElement_t.SML_PE_HEADER;
        replyHdr.version = SmlUtil.smlString2Pcdata("1.1");
        replyHdr.proto = this.requestHdr.proto;
        replyHdr.sessionID = this.requestHdr.sessionID;
        replyHdr.msgID = this.requestHdr.msgID;
        replyHdr.flags = Flag_t.SmlNoResp_f;    
        replyHdr.respURI = SmlUtil.smlString2Pcdata(this.responseURI);
        replyHdr.target = this.requestHdr.target;    
        replyHdr.source = this.requestHdr.source;    
        replyHdr.cred = null;
        replyHdr.meta = null;
        this.messageBuilder.smlStartMessage(
            replyHdr, 
            SmlVersion_t.SML_VERS_1_1
        );
        // Authentication
        this.processAuthentication();
        // Status for each alert
        for(SmlAlert_t alertCmd : this.alertCmds) {
            this.processAlert(alertCmd);
        }
        // Result for each get
        for(SmlGetPut_t getCmd : this.getCmds) {
            this.processGet(getCmd);
        }
        // Result for each put
        for(SmlGetPut_t putCmd : this.putCmds) {
            this.processPut(putCmd);
        }
        this.messageBuilder.smlEndMessage(isFinal);
        this.clientMessageIsFinal = isFinal;
    }

    //-----------------------------------------------------------------------
    @Override
    public void statusCmd(SmlStatus_t smlStatus) throws SmlException_t {
    }

    //-----------------------------------------------------------------------
    @Override
    public void startSync(SmlSync_t smlSync) throws SmlException_t {
        // No op during init
    }

    //-----------------------------------------------------------------------
    @Override
    public void endSync() throws SmlException_t {
        // No op during init
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final static String MAX_MSG_SIZE_TAG_BEGIN = "<MaxMsgSize xmlns='syncml:metinf'>";
    protected final static String MAX_MSG_SIZE_TAG_END = "</MaxMsgSize>";
    
    private boolean isAuthenticated = false;
    private final List<SmlAlert_t> alertCmds;
    private final List<SmlGetPut_t> getCmds;
    private final List<SmlGetPut_t> putCmds;
    
}
