package org.openmdx.syncml.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlItemList_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlMessageBuilder;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSourceRefList_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlTargetRefList_t;
import org.openmdx.syncml.SmlUtil;
import org.openmdx.syncml.SmlVersion_t;
import org.openmdx.syncml.SyncOptions;
import org.openmdx.syncml.SyncServlet;
import org.openmdx.syncml.xlt.SmlEncoding_t;
import org.openmdx.syncml.xlt.XltEncoder_t;

public class SampleSyncServlet extends SyncServlet {
    
    //-----------------------------------------------------------------------
    public SampleSyncServlet(
        SyncOptions options
    ) throws SmlException_t {
        super(options);
        this.echoBuffer = new ByteArrayOutputStream();
        this.echoMessageBuilder = new SmlMessageBuilder(
            this.echoBuffer
        );
        this.echoEncoder = null;
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void startMessage(
        SmlSyncHdr_t syncHdr
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > startMessage()");
        System.out.println(new Date() + " INFO     SyncHdr.VerDTD = " + SmlUtil.smlPcdata2String(syncHdr.version));
        System.out.println(new Date() + " INFO     SyncHdr.VerProto = " + SmlUtil.smlPcdata2String(syncHdr.proto));
        System.out.println(new Date() + " INFO     SyncHdr.SessionID = " + SmlUtil.smlPcdata2String(syncHdr.sessionID));
        System.out.println(new Date() + " INFO     SyncHdr.MsgID = " + SmlUtil.smlPcdata2String(syncHdr.msgID));
        System.out.println(new Date() + " INFO     SyncHdr.Flags = " + syncHdr.flags);
        System.out.println(new Date() + " INFO     SyncHdr.Target.locURI = " + SmlUtil.smlPcdata2String(syncHdr.target.locURI));
        System.out.println(new Date() + " INFO     SyncHdr.Source.locURI = " + SmlUtil.smlPcdata2String(syncHdr.source.locURI));
        System.out.println(new Date() + " INFO     SyncHdr.RespURI = " + SmlUtil.smlPcdata2String(syncHdr.respURI));
        System.out.println(new Date() + " INFO     SyncHdr.Meta = " + SmlUtil.smlPcdata2String(syncHdr.meta));
        System.out.println(new Date() + " INFO   < startMessage()");
        this.echoEncoder = this.echoMessageBuilder.smlStartMessage(
            SmlEncoding_t.SML_XML, 
            syncHdr, 
            SmlVersion_t.SML_VERS_1_1
        );
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void startSync(
        SmlSync_t smlSync
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > startSync()");
        System.out.println(new Date() + " INFO     Sync.CmdID = " + SmlUtil.smlPcdata2String(smlSync.cmdID));
        System.out.println(new Date() + " INFO     Sync.Target.LocURI = " + SmlUtil.smlPcdata2String(smlSync.target.locURI));
        System.out.println(new Date() + " INFO     Sync.Source.LocURI = " + SmlUtil.smlPcdata2String(smlSync.source.locURI));
        System.out.println(new Date() + " INFO     Sync.NumberOfChange = " + SmlUtil.smlPcdata2String(smlSync.noc));
        System.out.println(new Date() + " INFO   < startSync()");
        this.echoMessageBuilder.smlStartSync(
            this.echoEncoder, 
            smlSync
        );        
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void addCmd(
        SmlGenericCmd_t smlAdd
    ) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > addCmd()");
        System.out.println(new Date() + " INFO     Add.CmdID = " + SmlUtil.smlPcdata2String(smlAdd.cmdID));
        System.out.println(new Date() + " INFO     Add.Flags = " + smlAdd.flags);
        System.out.println(new Date() + " INFO     Add.Meta = " + SmlUtil.smlPcdata2String(smlAdd.meta));    
        ele = smlAdd.itemList;
        while (ele != null) {
            System.out.println(new Date() + " INFO     Add.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            System.out.println(new Date() + " INFO     Add.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < addCmd()");
        this.echoMessageBuilder.smlAddCmd(
            this.echoEncoder, 
            smlAdd
        );        
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void endSync(
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > endSync()");
        System.out.println(new Date() + " INFO   < endSync()");
        this.echoMessageBuilder.smlEndSync(
            this.echoEncoder 
        );        
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void endMessage(
        boolean isFinal
    ) throws SmlException_t {
        System.out.println(new Date() + " INFO   > endMessage()");
        System.out.println(new Date() + " INFO   < endMessage()");
        this.echoMessageBuilder.smlEndMessage(
            this.echoEncoder,
            isFinal
        );        
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void alertCmd(SmlAlert_t smlAlert) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > alertCmd()");
        System.out.println(new Date() + " INFO     Alert.CmdID = " + SmlUtil.smlPcdata2String(smlAlert.cmdID));
        System.out.println(new Date() + " INFO     Alert.Flags = " + smlAlert.flags);    
        ele = smlAlert.itemList;
        while (ele != null) {
            System.out.println(new Date() + " INFO     Alert.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            System.out.println(new Date() + " INFO     Alert.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < alertCmd()");
        this.echoMessageBuilder.smlAlertCmd(
            this.echoEncoder,
            smlAlert
        );        
    }

    //-----------------------------------------------------------------------
    @Override
    public void statusCmd(SmlStatus_t smlStatus) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > statusCmd()");
        System.out.println(new Date() + " INFO     Status.CmdID = " + SmlUtil.smlPcdata2String(smlStatus.cmdID));    
        System.out.println(new Date() + " INFO     Status.MsgRef = " + SmlUtil.smlPcdata2String(smlStatus.msgRef));    
        System.out.println(new Date() + " INFO     Status.CmdRef = " + SmlUtil.smlPcdata2String(smlStatus.cmdRef));    
        System.out.println(new Date() + " INFO     Status.Cmd = " + SmlUtil.smlPcdata2String(smlStatus.cmd));
        SmlTargetRefList_t targetRefList = smlStatus.targetRefList;
        while(targetRefList != null) {
            System.out.println(new Date() + " INFO     Status.TargetRef = " + SmlUtil.smlPcdata2String(targetRefList.targetRef));
            targetRefList = targetRefList.next;
        }
        SmlSourceRefList_t sourceRefList = smlStatus.sourceRefList;
        while(sourceRefList != null) {
            System.out.println(new Date() + " INFO     Status.SourceRef = " + SmlUtil.smlPcdata2String(sourceRefList.sourceRef));
            sourceRefList = sourceRefList.next;
        }
        System.out.println(new Date() + " INFO     Status.Data = " + SmlUtil.smlPcdata2String(smlStatus.data));
        ele = smlStatus.itemList;
        while (ele != null) {
            if((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Status.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Status.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < statusCmd()");
        this.echoMessageBuilder.smlStatusCmd(
            this.echoEncoder,
            smlStatus
        );                
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void mapCmd(SmlMap_t smlMap) throws SmlException_t {
        System.out.println(new Date() + " INFO   > mapCmd()");
        System.out.println(new Date() + " INFO     Map.CmdID = " + SmlUtil.smlPcdata2String(smlMap.cmdID));
        System.out.println(new Date() + " INFO     Map.Meta = " + SmlUtil.smlPcdata2String(smlMap.meta));    
        System.out.println(new Date() + " INFO   < mapCmd()");
        this.echoMessageBuilder.smlMapCmd(
            this.echoEncoder,
            smlMap
        );                
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void resultsCmd(SmlResults_t smlResults) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > resultsCmd()");
        System.out.println(new Date() + " INFO     Results.CmdID = " + SmlUtil.smlPcdata2String(smlResults.cmdID));    
        ele = smlResults.itemList;
        while (ele != null) {
            if((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Results.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Results.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < resultsCmd()");
        this.echoMessageBuilder.smlResultsCmd(
            this.echoEncoder,
            smlResults
        );                
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void replaceCmd(SmlGenericCmd_t smlReplace) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > replaceCmd()");
        System.out.println(new Date() + " INFO     Replace.CmdID = " + SmlUtil.smlPcdata2String(smlReplace.cmdID));
        ele = smlReplace.itemList;
        while (ele != null) {
            if ((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Replace.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Replace.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < replaceCmd()");
        this.echoMessageBuilder.smlReplaceCmd(
            this.echoEncoder,
            smlReplace
        );                
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void putCmd(SmlGetPut_t smlPut) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > putCmd()");
        System.out.println(new Date() + " INFO     Put.CmdID = " + SmlUtil.smlPcdata2String(smlPut.cmdID));
        System.out.println(new Date() + " INFO     Put.Flags = " + smlPut.flags);
        System.out.println(new Date() + " INFO     Put.Meta = " + SmlUtil.smlPcdata2String(smlPut.meta));
        ele = smlPut.itemList;
        while (ele != null) {
            if ((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Put.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Put.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < putCmd()");
        this.echoMessageBuilder.smlPutCmd(
            this.echoEncoder,
            smlPut
        );                
    }

    //-----------------------------------------------------------------------
    @Override
    public void getCmd(SmlGetPut_t smlGet) throws SmlException_t {
        SmlItemList_t ele;
        System.out.println(new Date() + " INFO   > getCmd()");
        System.out.println(new Date() + " INFO     Get.CmdID = " + SmlUtil.smlPcdata2String(smlGet.cmdID));
        System.out.println(new Date() + " INFO     Get.Flags = " + smlGet.flags);
        System.out.println(new Date() + " INFO     Get.Meta = " + SmlUtil.smlPcdata2String(smlGet.meta));
        ele = smlGet.itemList;
        while (ele != null) {
            if((ele.item != null) && (ele.item.source != null)) {
                System.out.println(new Date() + " INFO     Get.ItemList.Item.Source.LocURI = " + SmlUtil.smlPcdata2String(ele.item.source.locURI));
            }
            System.out.println(new Date() + " INFO     Get.ItemList.Item.Data = " + SmlUtil.smlPcdata2String(ele.item.data));
            ele = ele.next;
        }
        System.out.println(new Date() + " INFO   < getCmd()");
        this.echoMessageBuilder.smlGetCmd(
            this.echoEncoder,
            smlGet
        );                
    }

    //-----------------------------------------------------------------------
    @Override
    public void getResponse(
        OutputStream response
    ) throws SmlException_t {
        try {
            this.echoBuffer.close();
        } catch(IOException e) {}
        try {
            response.write(this.echoBuffer.toByteArray());
        } catch(IOException e) {}
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final ByteArrayOutputStream echoBuffer;
    private final SmlMessageBuilder echoMessageBuilder;
    private XltEncoder_t echoEncoder;
}
