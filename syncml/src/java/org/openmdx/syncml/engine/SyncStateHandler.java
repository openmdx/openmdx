/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: SyncStateHandler.java,v 1.14 2007/04/02 23:40:57 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.14 $
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.kernel.id.UUIDs;
import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlItemList_t;
import org.openmdx.syncml.SmlItem_t;
import org.openmdx.syncml.SmlMapItemList_t;
import org.openmdx.syncml.SmlMapItem_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataType_t;
import org.openmdx.syncml.SmlPcdata_t;
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlSourceRefList_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlTargetOrSource_t;
import org.openmdx.syncml.SmlTargetRefList_t;
import org.openmdx.syncml.SmlUtil;
import org.openmdx.syncml.SmlVersion_t;
import org.openmdx.syncml.engine.SyncDatabase.DatabaseObject;
import org.openmdx.syncml.xlt.SmlMetInfMetInf_t;

/**
 * Sync allows the following invocations
 * <ul>
 *   <li>startMessage: message header
 *   <li>endMessage: 
 *   <li>startSync: database to be synchronized
 *   <li>endSync: optional service capabilities
 *   <li>addCmd: add data item
 *   <li>replaceCmd: replace data item
 *   <li>deleteCmd: delete data item
 * </ul>
 *
 */
public class SyncStateHandler extends DefaultStateHandler {

    //-----------------------------------------------------------------------
    public SyncStateHandler(
        MessageBuilder messageBuilder,
        String responseURI,
        Map<String, SyncDatabase> databases,
        SyncDatabase anchors,
        Map<String, String[]> syncAnchors,
        Date syncStartedAt
    ) {
        super(
            messageBuilder,
            responseURI,
            databases,
            anchors,
            syncAnchors,
            syncStartedAt
        );        
        this.syncCmds = new ArrayList();
        this.addCmds = new ArrayList();
        this.deleteCmds = new ArrayList();
        this.replaceCmds = new ArrayList();
        this.mapCmds = new ArrayList();
        this.modifiedObjectsIterators = new HashMap();
    }
    
    //-----------------------------------------------------------------------
    public SyncStateHandler(
        MessageBuilder messageBuilder,
        SyncStateHandler that
    ) {
        super(
            messageBuilder,
            that.responseURI,
            that.databases,
            that.anchors,
            that.syncAnchors,
            that.syncStartedAt
        );        
        // Remember pending syncs
        this.syncCmds = that.syncCmds;
        // Remember iterators
        this.modifiedObjectsIterators = that.modifiedObjectsIterators;
        // Clear client requests
        this.addCmds = new ArrayList();
        this.deleteCmds = new ArrayList();
        this.replaceCmds = new ArrayList();
        this.mapCmds = new ArrayList();
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void startMessage(
        SmlSyncHdr_t syncHdr
    ) throws SmlException_t {
        this.requestHdr = syncHdr;
    }

    //-----------------------------------------------------------------------
    @Override
    public void addCmd(SmlGenericCmd_t smlAdd) throws SmlException_t {
        while(this.addCmds.size() < this.syncCmds.size()) {
            this.addCmds.add(new ArrayList());
        }
        this.addCmds.get(this.syncCmds.size()-1).add(smlAdd);
    }

    //-----------------------------------------------------------------------
    @Override
    public void deleteCmd(SmlGenericCmd_t smlDelete) throws SmlException_t {
        while(this.deleteCmds.size() < this.syncCmds.size()) {
            this.deleteCmds.add(new ArrayList());
        }
        this.deleteCmds.get(this.syncCmds.size()-1).add(smlDelete);
    }

    //-----------------------------------------------------------------------
    @Override
    public void replaceCmd(SmlGenericCmd_t smlReplace) throws SmlException_t {
        while(this.replaceCmds.size() < this.syncCmds.size()) {
            this.replaceCmds.add(new ArrayList());
        }
        this.replaceCmds.get(this.syncCmds.size()-1).add(smlReplace);
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void putCmd(SmlGetPut_t smlPut) throws SmlException_t {
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void getCmd(SmlGetPut_t smlGet) throws SmlException_t {
    }

    //-----------------------------------------------------------------------
    @Override
    public void mapCmd(SmlMap_t smlMap) throws SmlException_t {
        this.mapCmds.add(smlMap);
    }

    //-----------------------------------------------------------------------
    @Override
    public void startSync(
        SmlSync_t smlSync
    ) throws SmlException_t {
        this.syncCmds.add(smlSync);
    }

    //-----------------------------------------------------------------------
    @Override
    public void endSync(
    ) throws SmlException_t {
    }

    //-----------------------------------------------------------------------
    private String getItemType(
        SmlGenericCmd_t cmd
    ) {
        String meta = SmlUtil.smlPcdata2String(cmd.meta);
        if((meta != null) && meta.startsWith("<Type")) {
            return meta.substring(meta.indexOf(">") + 1, meta.indexOf("</Type>"));
        }
        else {
            return null;
        }
    }

    //-----------------------------------------------------------------------
    private String getItemData(
        SmlGenericCmd_t cmd
    ) {
        SmlItemList_t itemList = cmd.itemList;
        while(itemList != null) {
            SmlItem_t item = itemList.item;
            if(item.data != null) {
                return SmlUtil.smlPcdata2String(item.data);
            }
            itemList = itemList.next;
        }
        return null;        
    }

    //-----------------------------------------------------------------------
    private String getItemURI(
        SmlGenericCmd_t cmd
    ) {
        SmlItemList_t itemList = cmd.itemList;
        while(itemList != null) {
            SmlItem_t item = itemList.item;
            if((item.source != null) && (item.source.locURI != null)) {
                return SmlUtil.smlPcdata2String(item.source.locURI);
            }
            itemList = itemList.next;
        }
        return null;
    }
    
    //-----------------------------------------------------------------------
    protected void processReplace(
        SmlGenericCmd_t cmd,
        SyncDatabase database
    ) {
        String clientName = this.getClientName();
        if(clientName != null) {
            String externalId = this.getItemURI(cmd);
            DatabaseObject dbo = database.findObjectByExternalId(
                clientName,
                externalId
            );
            if(dbo == null) {            
                dbo = database.putObject(
                    UUIDs.getGenerator().next().toString(),
                    new String[]{
                        this.getItemType(cmd),
                        this.getItemData(cmd)
                    }
                );            
            }
            else {
                database.putObject(
                    dbo.getId(),
                    new String[]{
                        this.getItemType(cmd),
                        this.getItemData(cmd)
                    }
                );
            }
            dbo.setExternalId(
                clientName,
                externalId
            );
        }
    }
    
    //-----------------------------------------------------------------------
    protected void processAdd(
        SmlGenericCmd_t cmd,
        SyncDatabase database
    ) {
        String clientName = this.getClientName();
        if(clientName != null) {
            DatabaseObject dbo = database.putObject(
                UUIDs.getGenerator().next().toString(),
                new String[]{
                    this.getItemType(cmd),
                    this.getItemData(cmd)
                }
            );            
            dbo.setExternalId(
                clientName,                
                this.getItemURI(cmd)
            );
        }
    }
    
    //-----------------------------------------------------------------------
    protected void processDelete(
        SmlGenericCmd_t cmd,
        SyncDatabase database
    ) {
        String clientName = this.getClientName();
        if(clientName != null) {
            String externalId = this.getItemURI(cmd);
            DatabaseObject dbo = database.findObjectByExternalId(clientName, externalId);
            if(dbo != null) {
                database.deleteObject(dbo.getId());
            }
        }
    }
    
    //-----------------------------------------------------------------------
    protected void processSync(
        int syncId
    ) throws SmlException_t {

        // Status for Sync command
        SmlSync_t syncCmd = this.syncCmds.get(syncId);
        SmlStatus_t status = new SmlStatus_t();
        status.cmdID = this.getNextCmdID();
        status.msgRef = this.requestHdr.msgID;
        status.cmdRef = syncCmd.cmdID;
        status.cmd = SmlUtil.smlString2Pcdata("Sync");
        if((syncCmd.source != null) && (syncCmd.source.locURI != null)) {
            status.sourceRefList = new SmlSourceRefList_t();
            status.sourceRefList.sourceRef = syncCmd.source.locURI;
        }
        SyncDatabase database = null;
        if((syncCmd.target != null) && (syncCmd.target.locURI != null)) {
            status.targetRefList = new SmlTargetRefList_t();
            status.targetRefList.targetRef = syncCmd.target.locURI;
            database = this.databases.get(SmlUtil.smlPcdata2String(syncCmd.target.locURI));
        }
        status.data = SmlUtil.smlString2Pcdata(
            database == null
                ? "0" // ERROR
                : "200" // OK
        ); 
        this.messageBuilder.smlStatusCmd(status);
        
        if(database != null) {
    
            // Process replaces
            if(syncId < this.replaceCmds.size()) {
                for(SmlGenericCmd_t cmd : this.replaceCmds.get(syncId)) {
                    status = new SmlStatus_t();
                    status.cmdID = this.getNextCmdID();
                    status.msgRef = this.requestHdr.msgID;
                    status.cmdRef = cmd.cmdID;
                    status.cmd = SmlUtil.smlString2Pcdata("Replace");
                    String itemKey = this.getItemURI(cmd);
                    if(itemKey != null) {
                        this.processReplace(
                            cmd,
                            database
                        );
                        status.data = SmlUtil.smlString2Pcdata("200"); // OK
                        status.sourceRefList = new SmlSourceRefList_t();
                        status.sourceRefList.sourceRef = SmlUtil.smlString2Pcdata(itemKey);
                    }
                    this.messageBuilder.smlStatusCmd(status);            
                }
            }
    
            // Process adds
            if(syncId < this.addCmds.size()) {
                for(SmlGenericCmd_t cmd : this.addCmds.get(syncId)) {
                    status = new SmlStatus_t();
                    status.cmdID = this.getNextCmdID();
                    status.msgRef = this.requestHdr.msgID;
                    status.cmdRef = cmd.cmdID;
                    status.cmd = SmlUtil.smlString2Pcdata("Add");
                    String itemKey = this.getItemURI(cmd);
                    if(itemKey != null) {
                        this.processAdd(
                            cmd,
                            database
                        );                
                        status.sourceRefList = new SmlSourceRefList_t();
                        status.sourceRefList.sourceRef = SmlUtil.smlString2Pcdata(itemKey);
                    }
                    status.data = SmlUtil.smlString2Pcdata("201"); // SLOW_SYNC
                    this.messageBuilder.smlStatusCmd(status);
                }
            }
    
            // Process deletes
            if(syncId < this.deleteCmds.size()) {
                for(SmlGenericCmd_t cmd : this.deleteCmds.get(syncId)) {
                    status = new SmlStatus_t();
                    status.cmdID = this.getNextCmdID();
                    status.msgRef = this.requestHdr.msgID;
                    status.cmdRef = cmd.cmdID;
                    status.cmd = SmlUtil.smlString2Pcdata("Delete");
                    String itemKey = this.getItemURI(cmd);
                    if(itemKey != null) {
                        this.processDelete(
                            cmd,
                            database
                        );
                        status.data = SmlUtil.smlString2Pcdata("200"); // OK
                        status.sourceRefList = new SmlSourceRefList_t();
                        status.sourceRefList.sourceRef = SmlUtil.smlString2Pcdata(itemKey);
                    }
                    this.messageBuilder.smlStatusCmd(status);            
                }
            }
        }
        // Send modified objects to client
        String clientName = this.getClientName();    
        String clientAnchorKey = CLIENT_ANCHOR_PREFIX + database.getName();
        // Get modified objects
        Integer position = this.modifiedObjectsIterators.get(database.getName());
        if(position == null) {
            position = new Integer(0);
        }
        // Do not include objects modified later than synchronization started
        DatabaseObject anchor = this.anchors.getObject(
            clientName
        );            
        Date since = anchor == null
            ? new Date(0L)
            : anchor.getModifiedAt();
        Collection<DatabaseObject> modifiedObjects = database.getModifiedObjects(
            since,
            this.getSyncStartedAt(),
            position.intValue()
        );
        System.out.println("[" + database.getName() + "] modified objects=" + modifiedObjects.size());
        if(
            (clientName != null) && 
            (this.syncAnchors.get(clientAnchorKey) != null) &&
            (modifiedObjects.size() > 0)
        ) {
            // Sync
            SmlSync_t syncReply = new SmlSync_t();
            syncReply.cmdID = this.getNextCmdID();
            syncReply.source = new SmlTargetOrSource_t();
            syncReply.source.locURI = syncCmd.target.locURI;
            syncReply.target = new SmlTargetOrSource_t();
            syncReply.target.locURI = syncCmd.source.locURI;
            this.messageBuilder.smlStartSync(syncReply);
            for(DatabaseObject modifiedObject : modifiedObjects) {
                // If modifications are sent to client a request with corresponding stats is expected
                if(this.messageBuilder.getBufferSize() > this.maxMessageSize) {
                    this.serverMessageIsFinal = false;
                    break;
                }
                SmlGenericCmd_t updateCmd = new SmlGenericCmd_t();
                updateCmd.cmdID = this.getNextCmdID();
                if(modifiedObject.getValues()[0] != null) {
                    updateCmd.meta = new SmlPcdata_t();
                    SmlMetInfMetInf_t metInf = new SmlMetInfMetInf_t();
                    metInf.type = SmlUtil.smlString2Pcdata(modifiedObject.getValues()[0]);
                    updateCmd.meta.contentType = SmlPcdataType_t.SML_PCDATA_EXTENSION;
                    updateCmd.meta.extension = SmlPcdataExtension_t.SML_EXT_METINF;                    
                    updateCmd.meta.content = metInf;
                }
                updateCmd.itemList = new SmlItemList_t();
                updateCmd.itemList.item = new SmlItem_t();
                String data = modifiedObject.getValues()[1];
                // TODO: Large Object handling
                updateCmd.itemList.item.data = SmlUtil.smlString2Pcdata(
                    data == null
                        ? null
                        : data.length() > 1000 ? data.substring(0, 1000) : data
                );                
                updateCmd.itemList.item.source = new SmlTargetOrSource_t();
                updateCmd.itemList.item.source.locURI = SmlUtil.smlString2Pcdata(modifiedObject.getId());
                // Return external id if known 
                if(modifiedObject.getExternalId(clientName) != null) {                
                    updateCmd.itemList.item.target = new SmlTargetOrSource_t();
                    updateCmd.itemList.item.target.locURI = SmlUtil.smlString2Pcdata(
                        modifiedObject.getExternalId(clientName)
                    );
                    this.messageBuilder.smlReplaceCmd(updateCmd);
                }
                else {
                    this.messageBuilder.smlAddCmd(updateCmd);
                }
                position++;
            }
            this.modifiedObjectsIterators.put(
                database.getName(), 
                position
            );
            this.messageBuilder.smlEndSync();
        }        
    }
    
    //-----------------------------------------------------------------------
    protected void processMapping(
        SmlMap_t mapCmd
    ) throws SmlException_t {
        String clientName = this.getClientName();
        if(clientName != null) {
            SmlMapItemList_t itemList = mapCmd.mapItemList;
            // Get target database
            SyncDatabase database = null;        
            if((mapCmd.target != null) && (mapCmd.target.locURI != null)) {
                database = this.databases.get(
                    SmlUtil.smlPcdata2String(mapCmd.target.locURI)
                );
            }
            // Get ids
            String externalId = null;
            String id = null;
            while(itemList != null) {
                SmlMapItem_t item = itemList.mapItem;
                if((item.source != null) && (item.source.locURI != null)) {
                    externalId = SmlUtil.smlPcdata2String(item.source.locURI);
                }
                if((item.target != null) && (item.target.locURI != null)) {
                    id = SmlUtil.smlPcdata2String(item.target.locURI);                
                }
                itemList = itemList.next;
            }
            // Update external id        
            if((database != null) && (id != null) && (externalId != null)) {
                DatabaseObject dbo = database.getObject(id);
                if(dbo != null) {
                    dbo.setExternalId(
                        clientName,
                        externalId
                    );
                }
            }
        }
        SmlStatus_t status = new SmlStatus_t();
        status.cmdID = this.getNextCmdID();
        status.msgRef = this.requestHdr.msgID;
        status.cmdRef = mapCmd.cmdID;
        status.cmd = SmlUtil.smlString2Pcdata("Map");
        status.sourceRefList = new SmlSourceRefList_t();
        status.sourceRefList.sourceRef = mapCmd.source.locURI;
        status.targetRefList = new SmlTargetRefList_t();
        status.targetRefList.targetRef = mapCmd.target.locURI;
        status.data = SmlUtil.smlString2Pcdata("200");
        this.messageBuilder.smlStatusCmd(status);
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
        // Header status
        SmlStatus_t status = new SmlStatus_t();
        status.cmdID = this.getNextCmdID();
        status.msgRef = this.requestHdr.msgID;
        status.cmdRef = SmlUtil.smlString2Pcdata("0");
        status.cmd = SmlUtil.smlString2Pcdata("SyncHdr");        
        if((requestHdr.source != null) && (requestHdr.source.locURI != null)) {
            status.sourceRefList = new SmlSourceRefList_t();
            status.sourceRefList.sourceRef = requestHdr.source.locURI;
        }
        if((requestHdr.target != null) && (requestHdr.target.locURI != null)) {
            status.targetRefList = new SmlTargetRefList_t();
            status.targetRefList.targetRef = requestHdr.target.locURI;
        }
        status.data = SmlUtil.smlString2Pcdata("200"); // OK
        this.messageBuilder.smlStatusCmd(status);
        // Sync
        for(int i = 0; i < this.syncCmds.size(); i++) {
            this.processSync(i);
        }
        // Update/Set Next anchor for all databases
        String clientName = this.getClientName();
        if(
            (clientName != null) && 
            this.serverMessageIsFinal && 
            (this.syncAnchors != null)
        ) {
            for(SyncDatabase database : this.databases.values()) {
                DatabaseObject dbo = this.anchors.getObject(clientName);
                if(dbo == null) {
                    dbo = this.anchors.putObject(
                        clientName,
                        new String[]{}
                    );
                }
                String clienAnchortKey = CLIENT_ANCHOR_PREFIX + database.getName();
                if(this.syncAnchors.get(clienAnchortKey) != null) {
                    dbo.setExternalId(
                        clienAnchortKey, 
                        this.syncAnchors.get(clienAnchortKey)[1] // Next
                    );
                }
                String serverAnchorKey = SERVER_ANCHOR_PREFIX + database.getName();
                if(this.syncAnchors.get(serverAnchorKey) != null) {
                    dbo.setExternalId(
                        serverAnchorKey, 
                        this.syncAnchors.get(serverAnchorKey)[1] // Next
                    );
                    dbo.touch(this.syncStartedAt.getTime());
                }
                dbo.touch(this.syncStartedAt.getTime());
            }
        }        
        // Map
        for(SmlMap_t mapCmd : this.mapCmds) {
            this.processMapping(mapCmd);
        }        
        this.messageBuilder.smlEndMessage(this.serverMessageIsFinal);
        this.clientMessageIsFinal = isFinal;
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void statusCmd(
        SmlStatus_t smlStatus
    ) throws SmlException_t {
    }

    //-----------------------------------------------------------------------
    @Override
    public void alertCmd(SmlAlert_t smlAlert) throws SmlException_t {
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    protected final List<SmlSync_t> syncCmds;
    protected final List<List<SmlGenericCmd_t>> addCmds;
    protected final List<List<SmlGenericCmd_t>> deleteCmds;
    protected final List<List<SmlGenericCmd_t>> replaceCmds;
    protected final List<SmlMap_t> mapCmds;
    // Iteration position for each database
    protected final Map<String, Integer> modifiedObjectsIterators;
    
}
