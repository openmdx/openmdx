package org.openmdx.syncml.example;

import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlItemList_t;
import org.openmdx.syncml.SmlItem_t;
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlTargetOrSource_t;
import org.openmdx.syncml.SmlUtil;
import org.openmdx.syncml.SmlVersion_t;
import org.openmdx.syncml.engine.MessageBuilder;

public class SampleHelper {
    
    public SampleHelper(
        MessageBuilder cmdBuilder
    ) {
        this.messageBuilder = cmdBuilder;
    }
    
    /**
     * Start building SyncML document
     *
     * @param id instance ID
     * @return return value of smlStartMessage
     * @see smlStartMessage
     * @see smlStartMessageExt
     */
    public void startMessage(
    ) throws SmlException_t {
        SmlSyncHdr_t hdr = new SmlSyncHdr_t();
        SmlTargetOrSource_t source = new SmlTargetOrSource_t(); 
        SmlTargetOrSource_t target = new SmlTargetOrSource_t();
    
        /* Create SyncML proto element for message header */
        hdr.elementType = SmlProtoElement_t.SML_PE_HEADER;
        hdr.version = SmlUtil.smlString2Pcdata("1.1");
        hdr.proto = SmlUtil.smlString2Pcdata("SyncML/1.1");
        hdr.sessionID = SmlUtil.smlString2Pcdata("1");
        hdr.msgID = SmlUtil.smlString2Pcdata("1000");
        hdr.flags = Flag_t.SmlNoResp_f;
    
        target.locURI = SmlUtil.smlString2Pcdata("http://data.sync.server.url");
        target.locName = SmlUtil.smlString2Pcdata("Data Sync Server");
        hdr.target = target;
    
        source.locURI = SmlUtil.smlString2Pcdata("data_sync_client");
        source.locName = SmlUtil.smlString2Pcdata("Data Sync Client");
        hdr.source = source;
    
        hdr.cred = null;
        hdr.meta = null;
        hdr.respURI = null;
    
        /* Start a new message using the SyncHdr proto element */
        this.messageBuilder.smlStartMessage(
            hdr, 
            SmlVersion_t.SML_VERS_1_1
        );
    }
    
    /**
     * Start building Sync command
     *
     * @param id instance ID
     * @return return value of smlStartSync
     * @see smlStartSync
     */
    public void startSync(
    ) throws SmlException_t {
        SmlSync_t sync = new SmlSync_t(); 
        SmlTargetOrSource_t source = new SmlTargetOrSource_t(); 
        SmlTargetOrSource_t target = new SmlTargetOrSource_t();
    
        /* Start sync cmd */
        sync.elementType = SmlProtoElement_t.SML_PE_SYNC_START;
        sync.cmdID = SmlUtil.smlString2Pcdata("1");
    
        target.locURI = SmlUtil.smlString2Pcdata("./contacts_server");
        target.locName = SmlUtil.smlString2Pcdata("Sync Server Database");
        sync.target = target;
    
        source.locURI = SmlUtil.smlString2Pcdata("./contacts_client");
        source.locName = SmlUtil.smlString2Pcdata("Sync Client Database");
        sync.source = source;
    
        sync.cred = null;
        sync.noc = SmlUtil.smlString2Pcdata("1");    
        this.messageBuilder.smlStartSync(
            sync
        );
    }
    
    /**
     * Start building Add command
     *
     * @param id instance ID
     * @return return value of smlAddCmd
     * @see smlAddCmd
     */
    public void addCmd(
    ) throws SmlException_t {
        SmlGenericCmd_t add = new SmlGenericCmd_t();
        SmlItem_t item = new SmlItem_t();
        SmlItemList_t itemList = new SmlItemList_t(); 
        SmlTargetOrSource_t source = new SmlTargetOrSource_t(); 
        SmlTargetOrSource_t target = new SmlTargetOrSource_t();
    
        /* Add cmd */
        add.elementType = SmlProtoElement_t.SML_PE_ADD;
        add.cmdID = SmlUtil.smlString2Pcdata("2");
        add.cred = null;
        add.meta = SmlUtil.smlString2Pcdata("<Type xmlns='syncml:metinf'>text/x-vcard</Type>");
        add.flags = Flag_t.SmlNoResp_f;
    
        target.locURI = SmlUtil.smlString2Pcdata("1");
        target.locName = SmlUtil.smlString2Pcdata("Element ID at Target");
        item.target = target;
    
        source.locURI = SmlUtil.smlString2Pcdata("2");
        source.locName = SmlUtil.smlString2Pcdata("Element ID at Source");
        item.source = source;
    
        item.data = SmlUtil.smlString2Pcdata("BEGIN:VCARD\n" +
                                     "VERSION:2.1\n" +
                                     "N:Puppy;Dusty\n" +
                                     "ORG:Columbia Internet\n" +
                                     "EMAIL:dusty_puppy@userfriendly.org\n" +
                                     "URL:http://www.userfriendly.org\n" +
                                     "TEL;WORK:+123 456 78 9012\n" + "END:VCARD\n");
        item.meta = null;
    
        itemList.item = item;
        itemList.next = null;
        add.itemList = itemList;
    
        this.messageBuilder.smlAddCmd(add);
    }
    
    /**
     * End the sync block 
     *
     * @param id instance ID
     * @return return value of smlEndSync
     * @see smlEndSync
     */
    public void endSync(
    ) throws SmlException_t {
        this.messageBuilder.smlEndSync();
    }
    
    /**
     * End the sync message
     *
     * @param id instance ID
     * @return return value of smlEndMessage
     * @see smlEndMessage
     */
    public void endMessage(
    ) throws SmlException_t {
        /* This ends the SyncML document ... it has been assembled */
        /* SmlFinal_f says this is the last message in the SyncML package */
        /* (since it's the only one) */
        this.messageBuilder.smlEndMessage(
            true
        );
    }
    
    /**
     * Close SyncML Toolkit session 
     *
     * @return return value of smlTerminate
     * @see smlTerminate
     */
    public void terminate(
    ) throws SmlException_t {
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final MessageBuilder messageBuilder;
    
}
