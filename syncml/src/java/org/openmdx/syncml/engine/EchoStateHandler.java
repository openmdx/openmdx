package org.openmdx.syncml.engine;

import java.util.Date;
import java.util.Map;

import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlExec_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlItemList_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSearch_t;
import org.openmdx.syncml.SmlSequence_t;
import org.openmdx.syncml.SmlSourceRefList_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlTargetRefList_t;
import org.openmdx.syncml.SmlUtil;
import org.openmdx.syncml.SmlVersion_t;

public class EchoStateHandler extends DefaultStateHandler {
    
    //-----------------------------------------------------------------------
    public EchoStateHandler(
        MessageBuilder messageBuilder,
        String respURI,
        Map<String, SyncDatabase> databases,
        SyncDatabase anchors,
        Date syncStartedAt
    ) {
        super(
            messageBuilder,
            respURI,
            databases,
            anchors,
            null,
            syncStartedAt
        );
    }
    
    //-----------------------------------------------------------------------
    public void startMessage(
        SmlSyncHdr_t syncHdr
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.startMessage()");
        System.out.println(new Date() + " INFO     Echo.SyncHdr.VerDTD = " + SmlUtil.smlPcdata2String(syncHdr.version));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.VerProto = " + SmlUtil.smlPcdata2String(syncHdr.proto));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.SessionID = " + SmlUtil.smlPcdata2String(syncHdr.sessionID));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.MsgID = " + SmlUtil.smlPcdata2String(syncHdr.msgID));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.Flags = " + syncHdr.flags);
        System.out.println(new Date() + " INFO     Echo.SyncHdr.Target.locURI = " + SmlUtil.smlPcdata2String(syncHdr.target.locURI));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.Source.locURI = " + SmlUtil.smlPcdata2String(syncHdr.source.locURI));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.RespURI = " + SmlUtil.smlPcdata2String(syncHdr.respURI));
        System.out.println(new Date() + " INFO     Echo.SyncHdr.Meta = " + SmlUtil.smlPcdata2String(syncHdr.meta));
        System.out.println(new Date() + " INFO   < Echo.startMessage()");
        this.messageBuilder.smlStartMessage(
            syncHdr, 
            SmlVersion_t.SML_VERS_1_1
        );        
    }
    
    //-----------------------------------------------------------------------
    public void startSync(
        SmlSync_t smlSync
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.startSync()");
        System.out.println(new Date() + " INFO     Echo.Sync.CmdID = " + SmlUtil.smlPcdata2String(smlSync.cmdID));
        System.out.println(new Date() + " INFO     Echo.Sync.Target.LocURI = " + SmlUtil.smlPcdata2String(smlSync.target.locURI));
        System.out.println(new Date() + " INFO     Echo.Sync.Source.LocURI = " + SmlUtil.smlPcdata2String(smlSync.source.locURI));
        System.out.println(new Date() + " INFO     Echo.Sync.NumberOfChange = " + SmlUtil.smlPcdata2String(smlSync.noc));
        System.out.println(new Date() + " INFO   < Echo.startSync()");
        this.messageBuilder.smlStartSync(
            smlSync
        );        
    }
    
    //-----------------------------------------------------------------------
    public void addCmd(
        SmlGenericCmd_t smlAdd
    ) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.addCmd()");
        System.out.println(new Date() + " INFO     Echo.Add.CmdID = " + SmlUtil.smlPcdata2String(smlAdd.cmdID));
        System.out.println(new Date() + " INFO     Echo.Add.Flags = " + smlAdd.flags);
        System.out.println(new Date() + " INFO     Echo.Add.Meta = " + SmlUtil.smlPcdata2String(smlAdd.meta));    
        ele = smlAdd.itemList;
        while (ele != null) {
            System.out.println(new Date() + " INFO     Echo.Add.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            System.out.println(new Date() + " INFO     Echo.Add.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.addCmd()");
        this.messageBuilder.smlAddCmd(
            smlAdd
        );        
    }
    
    //-----------------------------------------------------------------------
    public void endSync(
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.endSync()");
        System.out.println(new Date() + " INFO   < Echo.endSync()");
        this.messageBuilder.smlEndSync();        
    }
    
    //-----------------------------------------------------------------------
    public void endMessage(
        boolean isFinal
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.endMessage()");
        System.out.println(new Date() + " INFO   < Echo.endMessage()");
        this.messageBuilder.smlEndMessage(
            isFinal
        );        
    }
    
    //-----------------------------------------------------------------------
    public void alertCmd(SmlAlert_t smlAlert) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.alertCmd()");
        System.out.println(new Date() + " INFO     Echo.Alert.CmdID = " + SmlUtil.smlPcdata2String(smlAlert.cmdID));
        System.out.println(new Date() + " INFO     Echo.Alert.Flags = " + smlAlert.flags);    
        ele = smlAlert.itemList;
        while (ele != null) {
            System.out.println(new Date() + " INFO     Echo.Alert.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            System.out.println(new Date() + " INFO     Echo.Alert.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.alertCmd()");
        this.messageBuilder.smlAlertCmd(
            smlAlert
        );        
    }

    //-----------------------------------------------------------------------
    public void statusCmd(SmlStatus_t smlStatus) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.statusCmd()");
        System.out.println(new Date() + " INFO     Echo.Status.CmdID = " + SmlUtil.smlPcdata2String(smlStatus.cmdID));    
        System.out.println(new Date() + " INFO     Echo.Status.MsgRef = " + SmlUtil.smlPcdata2String(smlStatus.msgRef));    
        System.out.println(new Date() + " INFO     Echo.Status.CmdRef = " + SmlUtil.smlPcdata2String(smlStatus.cmdRef));    
        System.out.println(new Date() + " INFO     Echo.Status.Cmd = " + SmlUtil.smlPcdata2String(smlStatus.cmd));
        SmlTargetRefList_t targetRefList = smlStatus.targetRefList;
        while(targetRefList != null) {
            System.out.println(new Date() + " INFO     Echo.Status.TargetRef = " + SmlUtil.smlPcdata2String(targetRefList.targetRef));
            targetRefList = targetRefList.next;
        }
        SmlSourceRefList_t sourceRefList = smlStatus.sourceRefList;
        while(sourceRefList != null) {
            System.out.println(new Date() + " INFO     Echo.Status.SourceRef = " + SmlUtil.smlPcdata2String(sourceRefList.sourceRef));
            sourceRefList = sourceRefList.next;
        }
        System.out.println(new Date() + " INFO     Echo.Status.Data = " + SmlUtil.smlPcdata2String(smlStatus.data));
        ele = smlStatus.itemList;
        while (ele != null) {
            if((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Echo.Status.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Echo.Status.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.statusCmd()");
        this.messageBuilder.smlStatusCmd(
            smlStatus
        );                
    }
    
    //-----------------------------------------------------------------------
    public void mapCmd(SmlMap_t smlMap) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.mapCmd()");
        System.out.println(new Date() + " INFO     Echo.Map.CmdID = " + SmlUtil.smlPcdata2String(smlMap.cmdID));
        System.out.println(new Date() + " INFO     Echo.Map.Meta = " + SmlUtil.smlPcdata2String(smlMap.meta));    
        System.out.println(new Date() + " INFO   < Echo.mapCmd()");
        this.messageBuilder.smlMapCmd(
            smlMap
        );                
    }
    
    //-----------------------------------------------------------------------
    public void resultsCmd(SmlResults_t smlResults) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.resultsCmd()");
        System.out.println(new Date() + " INFO     Echo.Results.CmdID = " + SmlUtil.smlPcdata2String(smlResults.cmdID));    
        ele = smlResults.itemList;
        while (ele != null) {
            if((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Echo.Results.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Echo.Results.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.resultsCmd()");
        this.messageBuilder.smlResultsCmd(
            smlResults
        );                
    }
    
    //-----------------------------------------------------------------------
    public void replaceCmd(SmlGenericCmd_t smlReplace) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.replaceCmd()");
        System.out.println(new Date() + " INFO     Echo.Replace.CmdID = " + SmlUtil.smlPcdata2String(smlReplace.cmdID));
        ele = smlReplace.itemList;
        while (ele != null) {
            if ((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Echo.Replace.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Echo.Replace.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.replaceCmd()");
        this.messageBuilder.smlReplaceCmd(
            smlReplace
        );                
    }
    
    //-----------------------------------------------------------------------
    public void putCmd(SmlGetPut_t smlPut) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.putCmd()");
        System.out.println(new Date() + " INFO     Echo.Put.CmdID = " + SmlUtil.smlPcdata2String(smlPut.cmdID));
        System.out.println(new Date() + " INFO     Echo.Put.Flags = " + smlPut.flags);
        System.out.println(new Date() + " INFO     Echo.Put.Meta = " + SmlUtil.smlPcdata2String(smlPut.meta));
        ele = smlPut.itemList;
        while (ele != null) {
            if ((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Echo.Put.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Echo.Put.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.putCmd()");
        this.messageBuilder.smlPutCmd(
            smlPut
        );                
    }

    //-----------------------------------------------------------------------
    public void getCmd(SmlGetPut_t smlGet) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > Echo.getCmd()");
        System.out.println(new Date() + " INFO     Echo.Get.CmdID = " + SmlUtil.smlPcdata2String(smlGet.cmdID));
        System.out.println(new Date() + " INFO     Echo.Get.Flags = " + smlGet.flags);
        System.out.println(new Date() + " INFO     Echo.Get.Meta = " + SmlUtil.smlPcdata2String(smlGet.meta));
        ele = smlGet.itemList;
        while (ele != null) {
            if((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Echo.Get.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Echo.Get.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < Echo.getCmd()");
        this.messageBuilder.smlGetCmd(
            smlGet
        );                
    }

    //-----------------------------------------------------------------------
    public void copyCmd(SmlGenericCmd_t smlCopy) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.copyCmd()");
        this.messageBuilder.smlCopyCmd(
            smlCopy
        );                        
    }

    //-----------------------------------------------------------------------
    public void deleteCmd(SmlGenericCmd_t smlDeleteCmd) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.deleteCmd()");
        this.messageBuilder.smlDeleteCmd(
            smlDeleteCmd
        );                
    }

    //-----------------------------------------------------------------------
    public void endAtomic() throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.endAtomic()");
        this.messageBuilder.smlEndAtomic();                
    }

    //-----------------------------------------------------------------------
    public void endSequence() throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.endSequence()");
        this.messageBuilder.smlEndSequence();                
    }

    //-----------------------------------------------------------------------
    public void execCmd(SmlExec_t smlExec) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.execCmd()");
        this.messageBuilder.smlExecCmd(
            smlExec
        );                
    }

    //-----------------------------------------------------------------------
    public void handleError() throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.handleError()");
    }

    //-----------------------------------------------------------------------
    public void searchCmd(SmlSearch_t smlSearch) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.searchCmd()");
        this.messageBuilder.smlSearchCmd(
            smlSearch
        );                
    }

    //-----------------------------------------------------------------------
    public void startAtomic(SmlAtomic_t smlAtomic) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.startAtomic()");
        this.messageBuilder.smlStartAtomic(
            smlAtomic
        );                
    }

    //-----------------------------------------------------------------------
    public void startSequence(SmlSequence_t smlSequence) throws SmlException_t {
        System.out.println(new Date() + " INFO   > Echo.startSequence()");
        this.messageBuilder.smlStartSequence(
            smlSequence
        );                
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------

}
