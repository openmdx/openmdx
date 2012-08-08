//*************************************************************************/
//* module:          Encoder source file                                  */
//* file:            xltenc.c                                             */
//* target system:   All                                                  */
//* target OS:       All                                                  */   
//*************************************************************************/

package org.openmdx.syncml.xlt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlChal_t;
import org.openmdx.syncml.SmlCred_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlExec_t;
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
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSearch_t;
import org.openmdx.syncml.SmlSourceList_t;
import org.openmdx.syncml.SmlSourceRefList_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlTargetOrSource_t;
import org.openmdx.syncml.SmlTargetRefList_t;
import org.openmdx.syncml.SmlVersion_t;

public class XltEnc {

    // %%% luz:2003-07-31: added SyncML FPI (formal public identifier) tables
    static String[] SyncMLFPI = new String[]{
      "???",
      "-//SYNCML//DTD SyncML 1.0//EN",
      "-//SYNCML//DTD SyncML 1.1//EN"
    };
    
    static String[] SyncMLDevInfFPI = new String[]{
      "???",
      "-//SYNCML//DTD DevInf 1.0//EN",
      "-//SYNCML//DTD DevInf 1.1//EN"
    };

    static PEEnc_t[] PE = new PEEnc_t[]{
        new PEEnc_t(XltTagID_t.TN_ADD,          SmlProtoElement_t.SML_PE_ADD),
        new PEEnc_t(XltTagID_t.TN_ALERT,        SmlProtoElement_t.SML_PE_ALERT),
        new PEEnc_t(XltTagID_t.TN_ATOMIC,       SmlProtoElement_t.SML_PE_ATOMIC_START),
        new PEEnc_t(XltTagID_t.TN_ATOMIC_END,   SmlProtoElement_t.SML_PE_ATOMIC_END),
        new PEEnc_t(XltTagID_t.TN_COPY,         SmlProtoElement_t.SML_PE_COPY),
        new PEEnc_t(XltTagID_t.TN_DELETE,       SmlProtoElement_t.SML_PE_DELETE),
        new PEEnc_t(XltTagID_t.TN_EXEC,         SmlProtoElement_t.SML_PE_EXEC),
        new PEEnc_t(XltTagID_t.TN_GET,          SmlProtoElement_t.SML_PE_GET),
        new PEEnc_t(XltTagID_t.TN_MAP,          SmlProtoElement_t.SML_PE_MAP),
        new PEEnc_t(XltTagID_t.TN_PUT,          SmlProtoElement_t.SML_PE_PUT),
        new PEEnc_t(XltTagID_t.TN_RESULTS,      SmlProtoElement_t.SML_PE_RESULTS),
        new PEEnc_t(XltTagID_t.TN_SEARCH,       SmlProtoElement_t.SML_PE_SEARCH),    
        new PEEnc_t(XltTagID_t.TN_SEQUENCE,     SmlProtoElement_t.SML_PE_SEQUENCE_START),
        new PEEnc_t(XltTagID_t.TN_SEQUENCE_END, SmlProtoElement_t.SML_PE_SEQUENCE_END),    
        new PEEnc_t(XltTagID_t.TN_STATUS,       SmlProtoElement_t.SML_PE_STATUS),
        new PEEnc_t(XltTagID_t.TN_SYNC,         SmlProtoElement_t.SML_PE_SYNC_START),
        new PEEnc_t(XltTagID_t.TN_SYNC_END,     SmlProtoElement_t.SML_PE_SYNC_END),    
        new PEEnc_t(XltTagID_t.TN_REPLACE,      SmlProtoElement_t.SML_PE_REPLACE),
        new PEEnc_t(XltTagID_t.TN_UNDEF,        SmlProtoElement_t.SML_PE_UNDEF)
      };

    public XltEnc(
    ) {
        
    }
    
    public static PEEnc_t[] getPEEncTable(
    ) {
        return PE;
    }

    public static XltTagID_t getTNbyPE(
        final SmlProtoElement_t pE
    ) throws SmlException_t {        
        int i = 0; 
        PEEnc_t[] pPETbl = getPEEncTable();
        while (((pPETbl[i]).type) != SmlProtoElement_t.SML_PE_UNDEF)
        {
          if (((pPETbl[i]).type) == pE)
          {
          	return (pPETbl[i]).tagid;
            
          }    
          i++;
        }        
        return XltTagID_t.TN_UNDEF;
    }

    /**
     * FUNCTION: smlXltEncInit
     *
     * Initializes an XML buffer; Creates XML code for the SyncHdr
     * and appends it to the buffer.
     * Returns 0 if operation was successful.
     *
     * PRE-Condition:   no memory should be allocated for ppEncoder (should be null)
     *                  pHeader has to contain a valid SyncHdr structure
     *                  pBufEnd must point to the end of the (WB)XML buffer
     *                  ppBufPos has to be initialized to the start point of the
     *                  (WB)XML buffer.
     *                  
     *
     * POST-Condition:  After the function call ppBufPos points to the
     *                  first free byte in the buffer behind the (WB)XML document
     *
     * IN:              enc, the encoding constant (SML_WBXML or SML_XML)
     *                  pHeader, the SyncML header structure
     *                  pBufEnd, pointer to the end of the buffer to write on
     * 
     * IN/OUT:          ppBufPos, current position of the bufferpointer
     *                  ppEncoder, the encoder object       
     *
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     *                  Possible Error Codes:
     *                  SML_ERR_XLT_MISSING_CONT            
     *                  SML_ERR_XLT_BUF_ERR                 
     *                  SML_ERR_XLT_INVAL_ELEM_TYPE         
     *                  SML_ERR_XLT_INVAL_LIST_TYPE         
     *                  SML_ERR_XLT_INVAL_TAG_TYPE          
     *                  SML_ERR_XLT_ENC_UNK	               
     *                  SML_ERR_XLT_INVAL_PROTO_ELEM
     */
    public static XltEncoder_t xltEncInit(
        final SmlEncoding_t enc, 
        final SmlSyncHdr_t pHeader, 
        final ByteArrayOutputStream ppBufPos, 
        final SmlVersion_t vers
    ) throws SmlException_t {

        XltEncoder_t _pEncoder;

        // Structure containing buffer pointers, length and written bytes
        BufferMgmt_t _pBufMgr = new BufferMgmt_t();

        byte _stablen = 0x1D; // XLT_STABLEN;
        byte _wbxmlver = Xlt_t.XLT_WBXMLVER;
        byte _charset = Xlt_t.XLT_CHARSET;
        byte _pubident1 = Xlt_t.XLT_PUBIDENT1;
        byte _pubident2 = Xlt_t.XLT_PUBIDENT2;
        // %%% luz:2003-07-31: now uses FPI according to syncml version
        String _syncmldtd = SyncMLFPI[vers.getValue()];

        String _tmpStr;
        String _xmlver = Xlt_t.XML_VERSION;
        String _xmlenc = Xlt_t.XML_ENCODING;
        char _begpar = Xlt_t.XML_BEGPAR;
        char _endpar = Xlt_t.XML_ENDPAR;

        // MemByte_t _tmp = 0x00;CURRENTLY NOT USED

        _pEncoder = new XltEncoder_t();

        // set the encoding
        _pEncoder.enc = enc;

        // %%% luz:2003-07-31: added version
        _pEncoder.vers = vers;

        _pEncoder.cur_ext = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        _pEncoder.last_ext = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        _pEncoder.end_tag_size = 0;
        _pEncoder.space_evaluation = null;

        _pBufMgr.smlXltBuffer = ppBufPos;
        _pBufMgr.smlCurExt = _pEncoder.cur_ext;
        _pBufMgr.smlLastExt = _pEncoder.last_ext;
        _pBufMgr.smlActiveExt = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        _pBufMgr.switchExtTag = XltTagID_t.TN_UNDEF;
        _pBufMgr.spaceEvaluation = false;
        _pBufMgr.vers = vers;
        _pBufMgr.endTagSize = 0;

        switch (enc) {

            case SML_WBXML: {
    
                // Set the WBXML Header Values
                // WBXML Version
                XltEncWbXml.wbxmlWriteTypeToBuffer(_wbxmlver, XltElementType_t.TAG, _pBufMgr);
                // Public Idetifier - default unknown
                XltEncWbXml.wbxmlWriteTypeToBuffer(_pubident1, XltElementType_t.TAG, _pBufMgr);
                XltEncWbXml.wbxmlWriteTypeToBuffer(_pubident2, XltElementType_t.TAG, _pBufMgr);
                // Character set - not yet implemented
                XltEncWbXml.wbxmlWriteTypeToBuffer(_charset, XltElementType_t.TAG, _pBufMgr);
                // Sting table length - not yet implemented
                XltEncWbXml.wbxmlWriteTypeToBuffer(_stablen, XltElementType_t.TAG, _pBufMgr);
                // FPI - %%% luz:2003-07-31: not constant any more, varies according
                // to SyncML version
                XltEncCom.xltAddToBuffer(_syncmldtd, _pBufMgr);
                break;
            }

            case SML_XML: {
    
                XltEncCom.xltAddToBuffer(_begpar, _pBufMgr);
                _tmpStr = "?xml version=\"";
                XltEncCom.xltAddToBuffer(_tmpStr, _pBufMgr);
                _tmpStr = _xmlver;
                XltEncCom.xltAddToBuffer(_tmpStr, _pBufMgr);
                _tmpStr = "\" encoding=\"";
                XltEncCom.xltAddToBuffer(_tmpStr, _pBufMgr);
                _tmpStr = _xmlenc;
                XltEncCom.xltAddToBuffer(_tmpStr, _pBufMgr);
                _tmpStr = "\"?";
                XltEncCom.xltAddToBuffer(_tmpStr, _pBufMgr);
                XltEncCom.xltAddToBuffer(_endpar, _pBufMgr);    
                break;
            }

            default: {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_ENC_UNK);
            }
        }

        // SyncML Tag
        xltGenerateTag(XltTagID_t.TN_SYNCML, XltTagType_t.TT_BEG, enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);

        // Generate SmlSyncHdr
        xltEncBlock(XltTagID_t.TN_SYNCHDR, XltRO_t.REQUIRED, pHeader, enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);

        // SyncBody Tag 
        xltGenerateTag(XltTagID_t.TN_SYNCBODY, XltTagType_t.TT_BEG, enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);

        _pEncoder.cur_ext = _pBufMgr.smlCurExt;
        _pEncoder.last_ext = _pBufMgr.smlLastExt;
        _pEncoder.end_tag_size = _pBufMgr.endTagSize;

        _pEncoder.isFinal = false;

        return _pEncoder;
    }

    /**
     * FUNCTION: smlXltEncTerminate
     *
     * Filnalizes the (WB)XML document and returns the size of written bytes to 
     * the workspace module
     *
     * PRE-Condition:   pEncoder holds the initialized encoder structure.
     *                  the initialization takes place in the xltEncAppend function
     *                  pBufEnd must point to the end of the (WB)XML buffer
     *                  ppBufPos has to be initialized to the start point of the
     *                  (WB)XML buffer.
     *                  
     * POST-Condition:  After the function call ppBufPos points to the
     *                  first free byte in the buffer behind the (WB)XML document
     *
     * IN:              pEncoder, the encoder object
     *                  pBufEnd, pointer to the end of the buffer to write on
     * 
     * IN/OUT:          ppBufPos, current position of the bufferpointer
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     *                  Possible Error Codes:
     *                  SML_ERR_XLT_BUF_ERR      
     *                  SML_ERR_XLT_MISSING_CONT
     *                  SML_ERR_XLT_INVAL_ELEM_TYPE         
     *                  SML_ERR_XLT_INVAL_LIST_TYPE         
     *                  SML_ERR_XLT_INVAL_TAG_TYPE          
     *                  SML_ERR_XLT_ENC_UNK	               
     *                  SML_ERR_XLT_INVAL_PROTO_ELEM
     */
    public static void xltEncTerminate(
        final XltEncoder_t pEncoder,
        ByteArrayOutputStream buffer
    ) throws SmlException_t {

        // encoding type
        SmlEncoding_t _enc;

        // Structure containing buffer pointers, length and written bytes
        BufferMgmt_t _pBufMgr = new BufferMgmt_t();

        // get the encoding type
        _enc = pEncoder.enc;

        _pBufMgr.vers = pEncoder.vers; // %%% luz:2003-07-31: pass SyncML version to bufmgr
        _pBufMgr.smlXltBuffer = buffer;
        _pBufMgr.smlXltStoreBuf = _pBufMgr.smlXltBuffer;
        _pBufMgr.smlCurExt = pEncoder.cur_ext;
        _pBufMgr.smlLastExt = pEncoder.last_ext;
        _pBufMgr.smlActiveExt = pEncoder.cur_ext;
        _pBufMgr.switchExtTag = XltTagID_t.TN_UNDEF;
        _pBufMgr.spaceEvaluation = pEncoder.space_evaluation == null ? false : true;
        _pBufMgr.endTagSize = 0;

        try {
            if (pEncoder.isFinal) {
                xltGenerateTag(XltTagID_t.TN_FINAL, XltTagType_t.TT_ALL, _enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
            }    
            // SyncBody End Tag
            xltGenerateTag(XltTagID_t.TN_SYNCBODY, XltTagType_t.TT_END, _enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
            // SyncML End Tag
            xltGenerateTag(XltTagID_t.TN_SYNCML, XltTagType_t.TT_END, _enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
        }
        catch(SmlException_t e) {
            xltEncReset(pEncoder);
            throw e;          
        }

        pEncoder.cur_ext = _pBufMgr.smlCurExt;
        pEncoder.last_ext = _pBufMgr.smlLastExt;

        xltEncReset(pEncoder);
    }

    public static void xltEncReset(
        final XltEncoder_t pEncoder
    ) throws SmlException_t {
        if ((pEncoder != null) && (pEncoder.space_evaluation != null)) {
            pEncoder.space_evaluation = null;
        }
    }

    /**
     * FUNCTION: smlXltStartEvaluation
     *
     * Starts an evaluation run which prevents further API-Calls to write tags - 
     * just the tag-sizes are calculated. Must be sopped via smlEndEvaluation
     *
     * IN:              XltEncoderPtr_t
     *                  the encoder object
     *
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public static Ret_t xltStartEvaluation(
        final XltEncoder_t pEncoder
    ) {
        XltSpaceEvaluation_t _pSpaceEvaluation;

        if (pEncoder.space_evaluation != null)
            return Ret_t.SML_ERR_WRONG_USAGE;

        if ((_pSpaceEvaluation = new XltSpaceEvaluation_t()) == null)
            return Ret_t.SML_ERR_NOT_ENOUGH_SPACE;
        // %%% luz 2002-09-03: init encoder state shadow copies for evaluation
        // from real encoder
        _pSpaceEvaluation.cur_ext = pEncoder.cur_ext;
        _pSpaceEvaluation.last_ext = pEncoder.last_ext;

        pEncoder.space_evaluation = _pSpaceEvaluation;
        return Ret_t.SML_ERR_OK;
    }


    /**
     * FUNCTION: smlXltEndEvaluation
     *
     * Stops an evaluation run which prevents further API-Calls to write tags - 
     * the remaining free buffer size after all Tags are written is returned
     *
     * IN:              XltEncoderPtr_t
     *                  the encoder object
     *
     * IN/OUT:          MemSize_t              
     *					Size of free buffer for data after all tags are written
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public static Ret_t xltEndEvaluation(
        final XltEncoder_t pEncoder, 
        final long freemem
    ) {
        pEncoder.space_evaluation = null;
        return Ret_t.SML_ERR_OK;
    }


    /**
     * FUNCTION: xltEncBlock
     *
     * Generates a (WB)XML Block for a given tag ID and a given content
     *
     * PRE-Condition:   pContent holds a valid content structure
     *                  tagId contains a valid SyncML tag ID                  
     *
     * POST-Condition:  the (WB)XML buffer in the pBufMgr structure contains the
     *                  encoded (WB)XML block
     *
     * IN:              tagId, the ID for the tag to generate (TN_ADD, ...)
     *                  reqOptFlag, flag if the block is required or optional
     *                  pContent, the content structure of the block
     *                  enc, the encoding constant (SML_WBXML or SML_XML)
     *                  attFlag, indicates if the encoded tag contain Attributes in namespace extensions
     * 
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static void xltEncBlock(
        final XltTagID_t tagId, 
        final XltRO_t reqOptFlag, 
        final Object pContent, 
        final SmlEncoding_t enc, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
    
      //Check if pContent of a required field is missing
      if ((reqOptFlag == XltRO_t.REQUIRED) && (pContent == null)) {
        switch (tagId) {
          case TN_ATOMIC_END:
          case TN_SYNC_END:
          case TN_SEQUENCE_END:      
            break;
          default:  
            throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
        }    
      }  
      //Check if pContent of a optional field is missing
      else if ((pContent == null) && (tagId != XltTagID_t.TN_SYNC_END) && (tagId != XltTagID_t.TN_ATOMIC_END) && (tagId != XltTagID_t.TN_SEQUENCE_END))
          return;
      
      //Generate the commands . see DTD
      switch (tagId){
        case TN_SYNCHDR:
          // SyncHdr Begin Tag
          xltGenerateTag(XltTagID_t.TN_SYNCHDR, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Version
          xltEncBlock(XltTagID_t.TN_VERSION, XltRO_t.REQUIRED, ((SmlSyncHdr_t) pContent).version, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Proto
          xltEncBlock(XltTagID_t.TN_PROTO, XltRO_t.REQUIRED, ((SmlSyncHdr_t) pContent).proto, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // SessionID
          xltEncBlock(XltTagID_t.TN_SESSIONID, XltRO_t.REQUIRED, ((SmlSyncHdr_t) pContent).sessionID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // MsgID
          xltEncBlock(XltTagID_t.TN_MSGID, XltRO_t.REQUIRED, ((SmlSyncHdr_t) pContent).msgID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Target
          xltEncBlock(XltTagID_t.TN_TARGET, XltRO_t.REQUIRED, ((SmlSyncHdr_t) pContent).target, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Source
          xltEncBlock(XltTagID_t.TN_SOURCE, XltRO_t.REQUIRED, ((SmlSyncHdr_t) pContent).source, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // RespURI?
          xltEncBlock(XltTagID_t.TN_RESPURI, XltRO_t.OPTIONAL, ((SmlSyncHdr_t) pContent).respURI, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlSyncHdr_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlSyncHdr_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlSyncHdr_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // SyncHdr End Tag
          xltGenerateTag(XltTagID_t.TN_SYNCHDR, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_CRED:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_CRED, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlCred_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // Data
          xltEncBlock(XltTagID_t.TN_DATA, XltRO_t.REQUIRED, ((SmlCred_t) pContent).data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // End tag
          xltGenerateTag(XltTagID_t.TN_CRED, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_SOURCE:
        case TN_TARGET:
          // Begin tag
          xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // LocURI
          xltEncBlock(XltTagID_t.TN_LOCURI, XltRO_t.REQUIRED, ((SmlTargetOrSource_t) pContent).locURI, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                 
          // LocName?
          xltEncBlock(XltTagID_t.TN_LOCNAME, XltRO_t.OPTIONAL, ((SmlTargetOrSource_t) pContent).locName, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                       
          // End tag
          xltGenerateTag(tagId, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_ITEM:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_ITEM, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Target?
          xltEncBlock(XltTagID_t.TN_TARGET, XltRO_t.OPTIONAL, ((SmlItem_t) pContent).target, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Source?
          xltEncBlock(XltTagID_t.TN_SOURCE, XltRO_t.OPTIONAL, ((SmlItem_t) pContent).source, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlItem_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Data?
          xltEncBlock(XltTagID_t.TN_DATA, XltRO_t.OPTIONAL, ((SmlItem_t) pContent).data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    	    // MoreData?
          xltEncBlock(XltTagID_t.TN_MOREDATA, XltRO_t.OPTIONAL, (((SmlItem_t) pContent).flags), enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // End tag
          xltGenerateTag(XltTagID_t.TN_ITEM, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_ADD:
        case TN_COPY:
          // Begin tag
          xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlGenericCmd_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                     
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                     
          // Item+
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.REQUIRED, ((SmlGenericCmd_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(tagId, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_ALERT:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_ALERT, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlAlert_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlAlert_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlAlert_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // Data?
          xltEncBlock(XltTagID_t.TN_DATA, XltRO_t.OPTIONAL, ((SmlAlert_t) pContent).data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // Item*
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.OPTIONAL, ((SmlAlert_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(XltTagID_t.TN_ALERT, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_ATOMIC:
        case TN_SEQUENCE:
          // Begin tag
          xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlAtomic_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlAtomic_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlAtomic_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          //End tag in TN_ATOMIC_END
    
          break;
        case TN_ATOMIC_END:
          // End tag
          xltGenerateTag(XltTagID_t.TN_ATOMIC, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          break;
        case TN_SEQUENCE_END:
          // End tag
          xltGenerateTag(XltTagID_t.TN_SEQUENCE, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          break;
        case TN_DELETE:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_DELETE, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlGenericCmd_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // Archive?
          xltEncBlock(XltTagID_t.TN_ARCHIVE, XltRO_t.OPTIONAL, (((SmlGenericCmd_t) pContent).flags), enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // SftDel?
          xltEncBlock(XltTagID_t.TN_SFTDEL, XltRO_t.OPTIONAL, (((SmlGenericCmd_t) pContent).flags), enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Item+
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.REQUIRED, ((SmlGenericCmd_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(XltTagID_t.TN_DELETE, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_EXEC:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_EXEC, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlExec_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlExec_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlExec_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlExec_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Item
          xltEncBlock(XltTagID_t.TN_ITEM, XltRO_t.REQUIRED, ((SmlExec_t) pContent).item, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // End tag
          xltGenerateTag(XltTagID_t.TN_EXEC, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_GET:
        case TN_PUT:
          // Begin tag
          xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlGetPut_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlGetPut_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // Lang?
          xltEncBlock(XltTagID_t.TN_LANG, XltRO_t.OPTIONAL, ((SmlGetPut_t) pContent).lang, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlGetPut_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlGetPut_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Item+
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.REQUIRED, ((SmlGetPut_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(tagId, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                    
          
          break;
        case TN_MAP:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_MAP, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlMap_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // Target
          xltEncBlock(XltTagID_t.TN_TARGET, XltRO_t.REQUIRED, ((SmlMap_t) pContent).target, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // Source
          xltEncBlock(XltTagID_t.TN_SOURCE, XltRO_t.REQUIRED, ((SmlMap_t) pContent).source, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlMap_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlMap_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Mapitemlist
          xltEncList(XltListType_t.MAPITEM_LIST, XltRO_t.REQUIRED, ((SmlMap_t) pContent).mapItemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // End tag
          xltGenerateTag(XltTagID_t.TN_MAP, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_MAPITEM:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_MAPITEM, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Target
          xltEncBlock(XltTagID_t.TN_TARGET, XltRO_t.REQUIRED, ((SmlMapItem_t) pContent).target, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Source
          xltEncBlock(XltTagID_t.TN_SOURCE, XltRO_t.REQUIRED, ((SmlMapItem_t) pContent).source, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // End tag
          xltGenerateTag(XltTagID_t.TN_MAPITEM, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_RESULTS:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_RESULTS, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlResults_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // MsgRef?
          xltEncBlock(XltTagID_t.TN_MSGREF, XltRO_t.OPTIONAL, ((SmlResults_t) pContent).msgRef, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // CmdRef
          xltEncBlock(XltTagID_t.TN_CMDREF, XltRO_t.REQUIRED, ((SmlResults_t) pContent).cmdRef, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                            
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlResults_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // TargetRef?
          xltEncBlock(XltTagID_t.TN_TARGETREF, XltRO_t.OPTIONAL, ((SmlResults_t) pContent).targetRef, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // SourceRef?
          xltEncBlock(XltTagID_t.TN_SOURCEREF, XltRO_t.OPTIONAL, ((SmlResults_t) pContent).sourceRef, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // Item+
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.REQUIRED, ((SmlResults_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(XltTagID_t.TN_RESULTS, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_CHAL:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_CHAL, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // Meta
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.REQUIRED, ((SmlChal_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // End tag
          xltGenerateTag(XltTagID_t.TN_CHAL, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_SEARCH:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_SEARCH, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlSearch_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlSearch_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // NoResults?
          xltEncBlock(XltTagID_t.TN_NORESULTS, XltRO_t.OPTIONAL, ((SmlSearch_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlSearch_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Target?
          xltEncBlock(XltTagID_t.TN_TARGET, XltRO_t.OPTIONAL, ((SmlSearch_t) pContent).target, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                     
          // Source List
          xltEncList(XltListType_t.SOURCE_LIST, XltRO_t.REQUIRED, ((SmlSearch_t) pContent).sourceList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                        
          // Lang?
          xltEncBlock(XltTagID_t.TN_LANG, XltRO_t.OPTIONAL, ((SmlSearch_t) pContent).lang, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Meta
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.REQUIRED, ((SmlSearch_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Dsta
          xltEncBlock(XltTagID_t.TN_DATA, XltRO_t.REQUIRED, ((SmlSearch_t) pContent).data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // End tag
          xltGenerateTag(XltTagID_t.TN_SEARCH, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_STATUS:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_STATUS, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlStatus_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // MsgRef?
          xltEncBlock(XltTagID_t.TN_MSGREF, XltRO_t.REQUIRED, ((SmlStatus_t) pContent).msgRef, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // CmdRef
          xltEncBlock(XltTagID_t.TN_CMDREF, XltRO_t.REQUIRED, ((SmlStatus_t) pContent).cmdRef, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Cmd
          xltEncBlock(XltTagID_t.TN_CMD, XltRO_t.REQUIRED, ((SmlStatus_t) pContent).cmd, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // TargetRefList?
          xltEncList(XltListType_t.TARGETREF_LIST, XltRO_t.OPTIONAL, ((SmlStatus_t) pContent).targetRefList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // SourceRefList?
          xltEncList(XltListType_t.SOURCEREF_LIST, XltRO_t.OPTIONAL, ((SmlStatus_t) pContent).sourceRefList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlStatus_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Chal?
          xltEncBlock(XltTagID_t.TN_CHAL, XltRO_t.OPTIONAL, ((SmlStatus_t) pContent).chal, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Data
          xltEncBlock(XltTagID_t.TN_DATA, XltRO_t.REQUIRED, ((SmlStatus_t) pContent).data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                          
          // Item*
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.OPTIONAL, ((SmlStatus_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(XltTagID_t.TN_STATUS, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_SYNC:
          // Begin tag
          xltGenerateTag(XltTagID_t.TN_SYNC, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlSync_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlSync_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                         
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlSync_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // Target?
          xltEncBlock(XltTagID_t.TN_TARGET, XltRO_t.OPTIONAL, ((SmlSync_t) pContent).target, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // Source?
          xltEncBlock(XltTagID_t.TN_SOURCE, XltRO_t.OPTIONAL, ((SmlSync_t) pContent).source, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                       
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlSync_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
    	  // NumberOfChanges?
          xltEncBlock(XltTagID_t.TN_NUMBEROFCHANGES, XltRO_t.OPTIONAL, ((SmlSync_t) pContent).noc, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // End tag in TN_SYNC_END
    
          break;
        case TN_SYNC_END:
          //End tag
          xltGenerateTag(XltTagID_t.TN_SYNC, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_REPLACE:
          // Begin tag
          xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          // CmdID
          xltEncBlock(XltTagID_t.TN_CMDID, XltRO_t.REQUIRED, ((SmlGenericCmd_t) pContent).cmdID, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // NoResp?
          xltEncBlock(XltTagID_t.TN_NORESP, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).flags, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                           
          // Cred?
          xltEncBlock(XltTagID_t.TN_CRED, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).cred, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                     
          // Meta?
          xltEncBlock(XltTagID_t.TN_META, XltRO_t.OPTIONAL, ((SmlGenericCmd_t) pContent).meta, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                     
          // Item+     
          xltEncList(XltListType_t.ITEM_LIST, XltRO_t.REQUIRED, ((SmlGenericCmd_t) pContent).itemList, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);                                   
          // End tag
          xltGenerateTag(tagId, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
    
          break;
        case TN_ARCHIVE:                        
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number)pContent).intValue()) & (Flag_t.SmlArchive_f)) != 0) {
            xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          }                       
          break;
        case TN_SFTDEL:                 
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number) pContent).intValue()) & (Flag_t.SmlSftDel_f)) != 0) {
            xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          }
          break;
    	  case TN_MOREDATA:
    	    //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number) pContent).intValue()) & (Flag_t.SmlMoreData_f)) != 0) {
            xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          }
          break;
        case TN_NORESULTS:
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number) pContent).intValue()) & (Flag_t.SmlNoResults_f)) != 0) {
            xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          }
          break;
        case TN_NORESP:                                 
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number) pContent).intValue()) & (Flag_t.SmlNoResp_f)) != 0) {
            xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          }
          break;
        case TN_FINAL:                  
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number) pContent).intValue()) & (Flag_t.SmlFinal_f)) != 0) {
            xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
          }
          break;
        default:  // all leaf nodes (PCDATA#)
            xltEncPcdata(tagId, reqOptFlag, pContent, enc, pBufMgr, attFlag);
        }
    }
          
    public static Ret_t xltEncPcdata(
        final XltTagID_t tagId, 
        final XltRO_t reqOptFlag, 
        final Object pContent, 
        final SmlEncoding_t enc, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
    
        //generate PCDATA begin tag
        xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, attFlag);
    
        //write the pContent to the buffer according the encoding type
        switch (enc) {
            case SML_WBXML:
                switch (((SmlPcdata_t)pContent).contentType) {
                    case SML_PCDATA_STRING:
                        XltEncWbXml.wbxmlWriteTypeToBuffer((byte[])((SmlPcdata_t)pContent).content, XltElementType_t.STR_I, pBufMgr);
                        break;
                    // Note: SML_PCDATA_CDATA case added by luz to allow direct translation from XML to WBXML
                    case SML_PCDATA_CDATA:
                    case SML_PCDATA_OPAQUE:
                        XltEncWbXml.wbxmlWriteTypeToBuffer((byte[])((SmlPcdata_t)pContent).content, XltElementType_t.OPAQUE, pBufMgr);
                        break;
                    case SML_PCDATA_EXTENSION:
                        xltBuildExtention(((SmlPcdata_t)pContent).extension, reqOptFlag, ((SmlPcdata_t)pContent).content, enc, pBufMgr);
                        break;
                    default:
                        // 2003-11-24: Tomy to deal with pcdata empty extensions (for example <Meta></Meta> which is valid)
            			// refer to xltdec.c to see that empty extensions result in SmlPcdataPtr_t with all fields (data) set to 0
            			if (((SmlPcdata_t)pContent).contentType != SmlPcdataType_t.SML_PCDATA_UNDEFINED ||
            				((SmlPcdata_t)pContent).extension != SmlPcdataExtension_t.SML_EXT_UNDEFINED ||
            				((SmlPcdata_t)pContent).content != null)
            				return Ret_t.SML_ERR_XLT_INVAL_PCDATA_TYPE;
                        //			return SML_ERR_XLT_INVAL_PCDATA_TYPE;
                        // end modified by Tomy
                }; // eof switch(contenttype)
                break;
    
            case SML_XML:
                switch (((SmlPcdata_t)pContent).contentType) {
                    // Note: SML_PCDATA_OPAQUE case added by luz to allow direct translation from WBXML to XML
                    case SML_PCDATA_OPAQUE:
                    case SML_PCDATA_CDATA: {
        			    String _tmpStr;        
                        _tmpStr = "<![CDATA[";
                        XltEncCom.xltAddToBuffer(_tmpStr, pBufMgr);
                        XltEncCom.xltAddToBuffer((byte[])((SmlPcdata_t)pContent).content, pBufMgr);
                        _tmpStr = "]]>";
                        XltEncCom.xltAddToBuffer(_tmpStr, pBufMgr);
                        break;
                        }
                    // Note: SyncFest #5 shows that <![CDATA[ is not correctly parsed by the RTK
                    //       so we don't use it and risk the danger of failing on payload which has
                    //       XML in it.
                    case SML_PCDATA_STRING:
                        XltEncCom.xltAddToBuffer((byte[])((SmlPcdata_t)pContent).content, pBufMgr);
                        break;
                    case SML_PCDATA_EXTENSION:
                        xltBuildExtention(((SmlPcdata_t)pContent).extension, reqOptFlag, ((SmlPcdata_t)pContent).content, enc, pBufMgr);
                        break;
                    default:
        				// 2003-11-24: Tomy to deal with pcdata empty extensions (for example <Meta></Meta> which is valid)
        				// refer to xltdec.c to see that empty extensions result in SmlPcdataPtr_t with all fields (data) set to 0
        				if (((SmlPcdata_t)pContent).contentType != SmlPcdataType_t.SML_PCDATA_UNDEFINED ||
        					((SmlPcdata_t)pContent).extension != SmlPcdataExtension_t.SML_EXT_UNDEFINED ||
        					((SmlPcdata_t)pContent).content != null)
        					return Ret_t.SML_ERR_XLT_INVAL_PCDATA_TYPE;
        				//			return SML_ERR_XLT_INVAL_PCDATA_TYPE;
        				// end modified by Tomy
                }
                break;         
    
            default:
                return Ret_t.SML_ERR_XLT_ENC_UNK;
        } // eof switch(enc)
    
        //generate PCDATA END tag
        xltGenerateTag(tagId, XltTagType_t.TT_END, enc, pBufMgr, attFlag);
        return Ret_t.SML_ERR_OK;
    }

/**
     * FUNCTION: xltEncList
     *
     * Generates a list element which is not directly related to a tag
     *
     * PRE-Condition:   pList holds a valid list structure
     *                  listId contains a valid SyncML list ID                  
     *
     * POST-Condition:  the (WB)XML buffer in the pBufMgr structure contains the
     *                  encoded (WB)XML list
     *
     * IN:              listId, the ID of the list to generate (e.g. TARGET_LIST, ...)
     *                  pList, reference to the list to process
     *                  enc, the encoding constant (SML_WBXML or SML_XML)
     *                  attFlag, indicates if the encoded tag contain Attributes in namespace extensions
     * 
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static Object xltEncList(
        final XltListType_t listId, 
        final XltRO_t reqOptFlag, 
        Object pList, 
        final SmlEncoding_t enc, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
          //check if list is required or not
          if ((reqOptFlag == XltRO_t.REQUIRED) && (pList == null))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
          else if (pList == null)
             return pList;
          
          //encode the different list types
          switch (listId)
          {
            case ITEM_LIST:
            {
              while ((SmlItemList_t)pList != null) {
                  xltEncBlock(XltTagID_t.TN_ITEM, XltRO_t.OPTIONAL, ((SmlItemList_t)pList).item, enc, pBufMgr, attFlag);
                  pList = ((SmlItemList_t)pList).next;
              }        
              break;
            }
            case SOURCE_LIST:
            {
              while ((SmlSourceList_t)pList != null) {
                  xltEncBlock(XltTagID_t.TN_SOURCE, XltRO_t.OPTIONAL, ((SmlSourceList_t)pList).source, enc, pBufMgr, attFlag);
                  pList = ((SmlSourceList_t)pList).next;
              }         
              break;
            }
        	case TARGETREF_LIST:
            {
              while ((SmlTargetRefList_t)pList != null) {
                  xltEncBlock(XltTagID_t.TN_TARGETREF, XltRO_t.OPTIONAL, ((SmlTargetRefList_t)pList).targetRef, enc, pBufMgr, attFlag);
                  pList = ((SmlTargetRefList_t)pList).next;
              }         
              break;
            }
            case SOURCEREF_LIST:
            {
              while ((SmlSourceRefList_t)pList != null) {
                  xltEncBlock(XltTagID_t.TN_SOURCEREF, XltRO_t.OPTIONAL, ((SmlSourceRefList_t)pList).sourceRef, enc, pBufMgr, attFlag);
                  pList = ((SmlSourceRefList_t)pList).next;
              }         
              break;
            }
            case MAPITEM_LIST:
            {
              while ((SmlMapItemList_t)pList != null) {
                  xltEncBlock(XltTagID_t.TN_MAPITEM, XltRO_t.OPTIONAL, ((SmlMapItemList_t)pList).mapItem, enc, pBufMgr, attFlag);
                  pList = ((SmlMapItemList_t)pList).next;
              }         
              break;
            }
            default:
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_LIST_TYPE);
            }
          return pList;
    }

    /**
     * FUNCTION: xltGenerateTag
     *
     * Generates a (WB)XML tag
     *
     * PRE-Condition:   valis parameters
     *
     * POST-Condition:  the buffer contains a new tag
     *
     * IN:              tagId, the tag ID
     *                  TagType, the tag type (begin tag, end tag, ...)
     *                  enc, the encoding constant (SML_WBXML or SML_XML)                  
     *                  attFlag, indicates if the encoded tag contain Attributes in namespace extensions
     * 
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static void xltGenerateTag(
        final XltTagID_t tagId, 
        final XltTagType_t TagType, 
        final SmlEncoding_t enc, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
    
      byte _switchpage = Xlt_t.XLT_SWITCHPAGE;
    
      switch (enc) {
        case SML_WBXML:
            /* in WBXML codepage switches are done for starting tags only */
            if (TagType != XltTagType_t.TT_END) {
                //codepage switching with wbxml instead of namespace
                if (XltTags.getCodePage(attFlag) != XltTags.getCodePage(pBufMgr.smlCurExt)) {
                    byte _newcp = XltTags.getCodePage(attFlag);
                    XltEncWbXml.wbxmlWriteTypeToBuffer(_switchpage, XltElementType_t.TAG, pBufMgr);
                    XltEncWbXml.wbxmlWriteTypeToBuffer(_newcp, XltElementType_t.TAG, pBufMgr);
                }
          
          if (attFlag != pBufMgr.smlCurExt) {
                    pBufMgr.switchExtTag = tagId;
                    pBufMgr.smlLastExt = pBufMgr.smlCurExt;
                    pBufMgr.smlCurExt = attFlag;
                }
    		} // for TagType
          XltEncWbXml.wbxmlGenerateTag(tagId, TagType, pBufMgr);  
          break;
        case SML_XML:    
            if (attFlag != pBufMgr.smlCurExt) {
                pBufMgr.switchExtTag = tagId;
    			pBufMgr.smlLastExt = pBufMgr.smlCurExt;
                pBufMgr.smlCurExt = attFlag;
            }
    		XltEncXml.xmlGenerateTag(tagId, TagType, pBufMgr, attFlag);
            break;
        default:
          throw new SmlException_t(Ret_t.SML_ERR_XLT_ENC_UNK);
      }
    
      //return SML_ERR_XLT_ENC_UNK;NOT NEEDED
    }

    /* Entrypoint for SubDTD's. If we reached this point we already know 
     * a) we have data fora sub-DTD to encode and
     * b) we know which sub-DTD should be encoded.
     * So just call the appropriate sub-DTD encoder and thats it.
     */
    public static void xltBuildExtention(
        SmlPcdataExtension_t extId, 
        XltRO_t reqOptFlag, 
        Object pContent, 
        SmlEncoding_t enc, 
        BufferMgmt_t pBufMgr
    ) throws SmlException_t {
    
    	switch (extId) {
        	case SML_EXT_METINF:
        		XltMetInf.metinfEncBlock(XltTagID_t.TN_METINF_METINF,reqOptFlag,pContent,enc,pBufMgr,SmlPcdataExtension_t.SML_EXT_METINF);
                break;
        	case SML_EXT_DEVINF:
        		/* a deviceInf DTD always starts with this token */
                /* we have to choose, wether we have to encode the DevInf as XML or WBXML */
                /* in the latter case, we need a special treatment of this sub-dtd, as we have */
                /* to put it into a SML_PCDATA_OPAQUE field ... */
                if (enc == SmlEncoding_t.SML_XML)
        		    XltDevInf.devinfEncBlock(XltTagID_t.TN_DEVINF_DEVINF,reqOptFlag,pContent,enc,pBufMgr,SmlPcdataExtension_t.SML_EXT_DEVINF);
                else
                    subdtdEncWBXML(XltTagID_t.TN_DEVINF_DEVINF,reqOptFlag,pContent,SmlEncoding_t.SML_WBXML,pBufMgr,SmlPcdataExtension_t.SML_EXT_DEVINF);
                break;
        	default:
        		throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_EXT);
      }
      //return Ret_t.SML_ERR_OK;CAN NOT BE REACHED
    }


    /* Sub DTD's need a special treatment when used together with WBXML.
     * We need to eoncode them as a complete WBXML message including headers and stuff
     * and store the result within an SML_PCDATA_OPAQUE datafield.
     * To archieve this we create a new encoder, encode the message and finally
     * copy the result into the allready existing encoder.
     */
    public static void subdtdEncWBXML(
        XltTagID_t tagId, 
        XltRO_t reqOptFlag, 
        Object pContent, 
        SmlEncoding_t enc, 
        BufferMgmt_t pBufMgr, 
        SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
        BufferMgmt_t pSubBufMgr = null;
    
        // %%% luz 2003-07-31: ensured that we send the right version here!
        String FPIstring = SyncMLDevInfFPI[pBufMgr.vers.getValue()];
    
        // first create a sub buffer
        pSubBufMgr = new BufferMgmt_t();
        pSubBufMgr.smlXltBuffer        = new ByteArrayOutputStream();
        pSubBufMgr.smlActiveExt        = pBufMgr.smlActiveExt;
        pSubBufMgr.smlCurExt           = pBufMgr.smlCurExt;
        pSubBufMgr.smlLastExt          = pBufMgr.smlLastExt;
    	pSubBufMgr.spaceEvaluation     = pBufMgr.spaceEvaluation;
        
    	// in case of space evaluation, just count the number of written bytes
    	if (!pSubBufMgr.spaceEvaluation) {
            // create the WBXML header
            pSubBufMgr.smlXltBuffer.write((byte)0x02); // WBXML Version 1.2
            pSubBufMgr.smlXltBuffer.write((byte)0x00); // use Stringtable for ID
            pSubBufMgr.smlXltBuffer.write((byte)0x00); // empty/unknown public ID
            pSubBufMgr.smlXltBuffer.write((byte)0x6A); // charset encoding UTF-8
            pSubBufMgr.smlXltBuffer.write((byte)0x1D); // lenght of stringtable
            try {
                // Generate FPI
                // %%% luz 2003-07-31: ensured that we send the right version here!      
                pSubBufMgr.smlXltBuffer.write(FPIstring.getBytes());
            }
            catch(IOException e) {
                throw new SmlException_t(Ret_t.SML_ERR_A_MGR_ERROR);
            }
    	}
        // do the encoding
    	switch (attFlag) {
    	case SML_EXT_DEVINF:
            XltDevInf.devinfEncBlock(XltTagID_t.TN_DEVINF_DEVINF,reqOptFlag,pContent,enc,pSubBufMgr,SmlPcdataExtension_t.SML_EXT_DEVINF);
    		break;
    	/* oops - we don not know about that extension . bail out */
    	default:
    		throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_EXT);
        }
        // move it to the 'real' encoder buffer
        // now set up the OPAQUE field
    	if (pBufMgr.spaceEvaluation == false) {
            pBufMgr.smlXltBuffer.write((byte)0xC3); // OPAQUE data identifier       
        	XltEncWbXml.wbxmlOpaqueSize2Buf(0, pBufMgr);
            try {
        	    pBufMgr.smlXltBuffer.write(pSubBufMgr.smlXltStoreBuf.toByteArray());
            }
            catch(IOException e) {
                throw new SmlException_t(Ret_t.SML_ERR_A_MGR_ERROR);
            }
    	} 
        else {
    	    XltEncWbXml.wbxmlOpaqueSize2Buf(0, pBufMgr);
    	}
    }

    /**
     * 
     * FUNCTION: smlXltEncAppend
     *
     * Generates (WB)XML code and appends it to the XML buffer.
     *
     * PRE-Condition:   pEncoder holds the initialized encoder structure.
     *                  the initialization takes place in the xltEncAppend function
     *                  pContent has to contain a valid content structure structure
     *                  pBufEnd must point to the end of the (WB)XML buffer
     *                  ppBufPos has to be initialized to the start point of the
     *                  (WB)XML buffer.
     *                  
     *
     * POST-Condition:  After the function call ppBufPos points to the
     *                  first free byte in the buffer behind the (WB)XML document
     *
     * IN:              pEncoder, the encoder object
     *                  pe, the protocol element (PE_ADD, ...)    
     *                  pBufEnd, pointer to the end of the buffer to write on
     *                  pContent, the content to append to the SyncML document
     * 
     * IN/OUT:          ppBufPos, current position of the bufferpointer
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     *                  Possible Error Codes:
     *                  SML_ERR_XLT_MISSING_CONT            
     *                  SML_ERR_XLT_BUF_ERR                 
     *                  SML_ERR_XLT_INVAL_ELEM_TYPE         
     *                  SML_ERR_XLT_INVAL_LIST_TYPE         
     *                  SML_ERR_XLT_INVAL_TAG_TYPE          
     *                  SML_ERR_XLT_ENC_UNK                
     *                  SML_ERR_XLT_INVAL_PROTO_ELEM
     */
    public static void xltEncAppend(
        final XltEncoder_t pEncoder, 
        final SmlProtoElement_t pe, 
        final Object pContent,
        ByteArrayOutputStream buffer
    ) throws SmlException_t {
        
          XltTagID_t tagID = XltTagID_t.TN_UNDEF;
        
          // encoding type
          SmlEncoding_t _enc;
        
          //Structure containing buffer pointers, length and written bytes
          BufferMgmt_t _pBufMgr = new BufferMgmt_t();
        
          //get the encoding type
          _enc = pEncoder.enc;
          
          _pBufMgr.vers = pEncoder.vers; // %%% luz:2003-07-31: pass SyncML version to bufmgr
          _pBufMgr.smlXltBuffer = buffer;
          _pBufMgr.smlXltStoreBuf = _pBufMgr.smlXltBuffer;
          _pBufMgr.smlActiveExt = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
          _pBufMgr.switchExtTag = XltTagID_t.TN_UNDEF;
          _pBufMgr.spaceEvaluation = (pEncoder.space_evaluation == null) ? false : true;
          // %%% luz 2002-09-03: evaluation may not mess with encoder state
          if ( _pBufMgr.spaceEvaluation) {
            // spaceEval state
            _pBufMgr.smlCurExt = pEncoder.space_evaluation.cur_ext;
            _pBufMgr.smlLastExt = pEncoder.space_evaluation.last_ext;    
          }
          else {
            // normal encoder state
            _pBufMgr.smlCurExt = pEncoder.cur_ext;
            _pBufMgr.smlLastExt = pEncoder.last_ext;
          }
        
          _pBufMgr.endTagSize =0;
        
         tagID = getTNbyPE(pe);
        
          xltEncBlock(tagID, XltRO_t.REQUIRED, pContent, _enc, _pBufMgr, SmlPcdataExtension_t.SML_EXT_UNDEFINED);
        
          if (pEncoder.space_evaluation != null) {
            // Only calculating size
            pEncoder.space_evaluation.end_tag_size += _pBufMgr.endTagSize;
            // save it only into evaluation state
            pEncoder.space_evaluation.cur_ext = _pBufMgr.smlCurExt;
            pEncoder.space_evaluation.last_ext = _pBufMgr.smlLastExt;    
          } else {
            // really generating data
            pEncoder.end_tag_size += _pBufMgr.endTagSize;
            // save it into encoder state
            pEncoder.cur_ext = _pBufMgr.smlCurExt;
            pEncoder.last_ext = _pBufMgr.smlLastExt;
          }

    }

}
