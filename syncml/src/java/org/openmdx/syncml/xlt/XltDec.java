///*************************************************************************/
///* module:         SyncmML Decoder                                       */
///* file:           XLTDec.c                                              */
///* target system:  all                                                   */
///* target OS:      all                                                   */
///*************************************************************************/

package org.openmdx.syncml.xlt;

import java.util.EmptyStackException;
import java.util.Stack;

import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlChal_t;
import org.openmdx.syncml.SmlCred_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlItemList_t;
import org.openmdx.syncml.SmlItem_t;
import org.openmdx.syncml.SmlMapItemList_t;
import org.openmdx.syncml.SmlMapItem_t;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataList_t;
import org.openmdx.syncml.SmlPcdataType_t;
import org.openmdx.syncml.SmlPcdata_t;
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlSourceList_t;
import org.openmdx.syncml.SmlSourceRefList_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlTargetOrSource_t;
import org.openmdx.syncml.SmlTargetRefList_t;

public class XltDec {
    
  static PEBuilder_t[] PE = new PEBuilder_t[] { 
        new PEBuilderGenericCmd(XltTagID_t.TN_ADD, SmlProtoElement_t.SML_PE_ADD),
        new PEBuilderAlert(XltTagID_t.TN_ALERT, SmlProtoElement_t.SML_PE_ALERT),
        new PEBuilderAtomOrSeq(XltTagID_t.TN_ATOMIC, SmlProtoElement_t.SML_PE_ATOMIC_START),
        new PEBuilderGenericCmd(XltTagID_t.TN_COPY, SmlProtoElement_t.SML_PE_COPY),
        new PEBuilderGenericCmd(XltTagID_t.TN_DELETE, SmlProtoElement_t.SML_PE_DELETE),
        new PEBuilderExec(XltTagID_t.TN_EXEC, SmlProtoElement_t.SML_PE_EXEC),
        new PEBuilderPutOrGet(XltTagID_t.TN_GET, SmlProtoElement_t.SML_PE_GET),
        new PEBuilderMap(XltTagID_t.TN_MAP, SmlProtoElement_t.SML_PE_MAP),
        new PEBuilderPutOrGet(XltTagID_t.TN_PUT, SmlProtoElement_t.SML_PE_PUT),
        new PEBuilderResults(XltTagID_t.TN_RESULTS, SmlProtoElement_t.SML_PE_RESULTS),
        new PEBuilderSearch(XltTagID_t.TN_SEARCH, SmlProtoElement_t.SML_PE_SEARCH),
        new PEBuilderAtomOrSeq(XltTagID_t.TN_SEQUENCE, SmlProtoElement_t.SML_PE_SEQUENCE_START),
        new PEBuilderStatus(XltTagID_t.TN_STATUS, SmlProtoElement_t.SML_PE_STATUS),
        new PEBuilderSync(XltTagID_t.TN_SYNC, SmlProtoElement_t.SML_PE_SYNC_START),
        new PEBuilderGenericCmd(XltTagID_t.TN_REPLACE, SmlProtoElement_t.SML_PE_REPLACE),
        new PEBuilder_t(XltTagID_t.TN_UNDEF, SmlProtoElement_t.SML_PE_UNDEF) 
    };
  
    public static PEBuilder_t[] getPETable()
    { 
      return PE; 
    }


    public static boolean IS_START(XltDecToken_t tok) {
        return tok.type == XltTokType_t.TOK_TAG_START;
    }
    
    public static boolean IS_END(XltDecToken_t tok) {
        return tok.type == XltTokType_t.TOK_TAG_END;
    }
    
    public static boolean IS_EMPTY(XltDecToken_t tok) {
        return tok.type == XltTokType_t.TOK_TAG_EMPTY;
    }

    public static boolean IS_TAG(XltDecToken_t tok) {
        return (IS_START(tok) || IS_EMPTY(tok) || IS_END(tok));
    }
    
    public static boolean IS_START_OR_EMPTY(XltDecToken_t tok) {
        return (IS_START(tok) || IS_EMPTY(tok));
    }
    
    private static boolean IS_CONTENT(XltDecToken_t tok) {
        return tok.type == XltTokType_t.TOK_CONT;    
    }
    
    /**
     * Description see XLTDec.h header file.
     */
    public static XltDecInitResult_t xltDecInit(
        final SmlEncoding_t enc,
        byte[] buffer
    ) throws SmlException_t {
        XltDecoder_t pDecoder;
        XltDecInitResult_t result = new XltDecInitResult_t();
    
        /* create new decoder object */
        pDecoder = new XltDecoder_t();
        pDecoder.isFinished = false;
        pDecoder.isFinal = false;
        pDecoder.scanner = null;
        pDecoder.tagstack = new Stack();
    
        try {
            if (enc == SmlEncoding_t.SML_WBXML) {
                pDecoder.scanner = XltDecWbXml.xltDecWbxmlInit(buffer);
                pDecoder.charset = pDecoder.scanner.charset;
                pDecoder.charsetStr = null;
            } 
            else if (enc == SmlEncoding_t.SML_XML) {        
                pDecoder.scanner = XltDecXml.xltDecXmlInit(buffer);
                pDecoder.charset = 0;
                pDecoder.charsetStr = pDecoder.scanner.charsetStr;
            } 
            else {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_ENC_UNK);
            }
        }
        catch(SmlException_t e) {
            xltDecTerminate((XltDecoder_t)pDecoder);
            throw e;
        }
    
        /* try to find SyncHdr element, first comes the SyncML tag... */
        nextToken(pDecoder);
        if (!IS_START(pDecoder.scanner.curtok) ||
                (pDecoder.scanner.curtok.tagid != XltTagID_t.TN_SYNCML)) {
            xltDecTerminate((XltDecoder_t)pDecoder);
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        /* ... then the SyncHdr */
        try {
            nextToken(pDecoder);
            result.ppSyncHdr = buildSyncHdr(pDecoder);
        }
        catch(SmlException_t e) {
            xltDecTerminate((XltDecoder_t)pDecoder);
            throw e;
        }
    
        result.ppDecoder = pDecoder;
        
        return result;
    }

    
    /**
     * Description see XLTDec.h header file.
     */
    public static XltDecNextResult_t xltDecNext(
        final XltDecoder_t pDecoder
    ) throws SmlException_t {
        XltDecoder_t pDecPriv = (XltDecoder_t)pDecoder;
        XltDecScanner_t pScanner = pDecPriv.scanner;
        XltTagID_t tagid;
        XltDecNextResult_t result = new XltDecNextResult_t();
        int i;
    
        /* if we are still outside the SyncBody, look for SyncBody start tag */
        tagid = (XltTagID_t)pDecPriv.tagstack.peek();
        if (tagid == XltTagID_t.TN_SYNCML) {
            nextToken(pDecPriv);
            if (!IS_START(pScanner.curtok) &&
                 (pScanner.curtok.tagid == XltTagID_t.TN_SYNCBODY)) {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
            }
        }
    
        // Reset code page in case there is no explicit code page switch
        pScanner.activeExt = SmlPcdataExtension_t.SML_EXT_UNDEFINED;        
        nextToken(pDecPriv);
    
        /* if we find a SyncML protocol element build the corresponding
           data structure */
        if ((IS_START_OR_EMPTY(pScanner.curtok)) && (pScanner.curtok.tagid != XltTagID_t.TN_FINAL)) {    
            PEBuilder_t[] pPEs = getPETable();
            i = 0;
            while ((pPEs[i].tagid) != XltTagID_t.TN_UNDEF) { 
                if (pPEs[i].tagid == pScanner.curtok.tagid) {
                    result.pe = pPEs[i].type;
                    result.ppContent = pPEs[i].build(pDecPriv);
                    /* T.K. adjust the SML_PE_ for 'generic' structures */
                    if (result.pe == SmlProtoElement_t.SML_PE_GENERIC) {
                        SmlGenericCmd_t g = (SmlGenericCmd_t)result.ppContent;
                        switch (pPEs[i].tagid) {
                            case TN_ADD    : g.elementType = SmlProtoElement_t.SML_PE_ADD;     break;
                            case TN_COPY   : g.elementType = SmlProtoElement_t.SML_PE_COPY;    break;
                            case TN_DELETE : g.elementType = SmlProtoElement_t.SML_PE_DELETE;  break;
                            case TN_REPLACE: g.elementType = SmlProtoElement_t.SML_PE_REPLACE; break;
                        }
                    }
                    break;
                }
                i++;
            }
            if (pPEs[i].tagid == XltTagID_t.TN_UNDEF) {
                result.pe = SmlProtoElement_t.SML_PE_UNDEF;
                result.ppContent = null;
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
            }
        } 
        else {    
            /* found end tag */
            switch (pScanner.curtok.tagid) {
                case TN_ATOMIC:
                    result.pe = SmlProtoElement_t.SML_PE_ATOMIC_END;
                    result.ppContent = null;
                    break;
                case TN_SEQUENCE:
                    result.pe = SmlProtoElement_t.SML_PE_SEQUENCE_END;
                    result.ppContent = null;
                    break;
                case TN_SYNC:
                    result.pe = SmlProtoElement_t.SML_PE_SYNC_END;
                    result.ppContent = null;
                    break;
                case TN_FINAL:
                    result.pe = SmlProtoElement_t.SML_PE_FINAL;
                    result.ppContent = null;
                    pDecPriv.isFinal = true;
                    break;
                case TN_SYNCBODY:
                    /* next comes the SyncML end tag, then we're done */
                    nextToken(pDecPriv);
                    if (IS_END(pScanner.curtok) &&
                            (pScanner.curtok.tagid == XltTagID_t.TN_SYNCML)) {
                        result.pe = SmlProtoElement_t.SML_PE_UNDEF;
                        result.ppContent = null;
                        pDecPriv.isFinished = true;
                    } else {
                        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                    }
                    break;
                default: 
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
            }
        }
    
        return result;
    }

    /**
     * Description see XLTDec.h header file.
     */
    public static Ret_t xltDecTerminate(
        final XltDecoder_t pDecoder
    ) throws SmlException_t {
        XltDecoder_t pDecPriv;
    
        if (pDecoder == null)
            return Ret_t.SML_ERR_OK;
    
        pDecPriv = (XltDecoder_t)pDecoder;
        if (pDecPriv.scanner != null)
            pDecPriv.scanner.destroy();
        if (pDecPriv.tagstack != null)
            pDecPriv.tagstack = null;
    
        return Ret_t.SML_ERR_OK;
    }

    public static Ret_t xltDecReset(
        final XltDecoder_t pDecoder
    ) throws SmlException_t {
      return xltDecTerminate(pDecoder);
    }

    /**
     * Gets the next token from the scanner.
     * Checks if the current tag is an end tag and if so, whether the last
     * open start tag has the same tag id as the current end tag. An open start
     * tag is one which matching end tag has not been seen yet.
     * If the current tag is a start tag its tag ID will be pushed onto the
     * tag stack.
     * If the current tag is an empty tag or not a tag at all nothing will be
     * done.
     */
    public static void nextToken(
        final XltDecoder_t pDecoder
    ) throws SmlException_t {
        
        XltDecScanner_t scanner = pDecoder.scanner;    
        scanner.nextTok();    
        XltDecToken_t pToken = scanner.curtok;
        Stack pTagStack = pDecoder.tagstack;
    
        if (IS_START(pToken)) {
            if (pTagStack.push(pToken.tagid) == null)
                throw new SmlException_t(Ret_t.SML_ERR_UNSPECIFIC);
        } else if (IS_END(pToken)) {
            XltTagID_t lastopen;
            try {
                lastopen = (XltTagID_t)pTagStack.pop();
            }
            catch(Exception e) {
                throw new SmlException_t(Ret_t.SML_ERR_UNSPECIFIC);
            }
            if (pToken.tagid != lastopen)
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    }

    public static void discardToken(
        final XltDecoder_t pDecoder
    ) throws SmlException_t {
        pDecoder.scanner.pushTok();
        try {
            pDecoder.tagstack.pop();
        }
        catch(EmptyStackException e) {
            throw new SmlException_t(Ret_t.SML_ERR_UNSPECIFIC);
        }
    }

    public static SmlPcdata_t concatPCData(
        final SmlPcdata_t pDat1, 
        final SmlPcdata_t pDat2
    ) throws SmlException_t {
        if (pDat1.contentType != pDat2.contentType)
            return null;
    
        switch (pDat1.contentType) {
            case SML_PCDATA_STRING:
            case SML_PCDATA_OPAQUE:
                byte[] d1 = (byte[])pDat1.content;
                byte[] d2 = (byte[])pDat2.content;
                byte[] t = new byte[d1.length + d2.length];
                System.arraycopy(pDat1.content, 0, t, 0, d1.length);
                System.arraycopy(pDat2.content, 0, t, d1.length, d2.length);
                pDat1.content = t;
                break;
            default:
                return null;
        }
        return pDat1;
    }

    public static SmlSyncHdr_t buildSyncHdr(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlSyncHdr_t pSyncHdr;
        long sessionid = 0, msgid = 0, source = 0, target = 0, version = 0, proto = 0;
    
        /* shortcut to the scanner object */
        pScanner = pDecoder.scanner;
    
        /* initialize new SmlSyncHdr */
        pSyncHdr = new SmlSyncHdr_t();
    
        /* initialize the element type field */
        pSyncHdr.elementType = SmlProtoElement_t.SML_PE_HEADER;
    
        /* empty SmlSyncHdr is possible */
        if (IS_EMPTY(pScanner.curtok)) {
            return pSyncHdr;
        }
    
        /* get next Token */
        nextToken(pDecoder);
    
        /* parse child elements until we find a matching end tag */
        while (!IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
    
                /* PCDATA elements */
                case TN_VERSION:
                    pSyncHdr.version = buildPCData(pDecoder);
                    version++;
                    break;
                case TN_PROTO:
                    pSyncHdr.proto = buildPCData(pDecoder);
                    proto++;
                    break;
                case TN_SESSIONID:
                    pSyncHdr.sessionID = buildPCData(pDecoder);
                    sessionid++;
                    break;
                case TN_MSGID:
                    pSyncHdr.msgID = buildPCData(pDecoder);
                    msgid++;
                    break;
                case TN_RESPURI:
                    pSyncHdr.respURI = buildPCData(pDecoder);
                    break;    
                    /* child tags */
                case TN_TARGET:
                    pSyncHdr.target = buildTargetOrSource(pDecoder);
                    target++;
                    break;
                case TN_SOURCE:
                    pSyncHdr.source = buildTargetOrSource(pDecoder);
                    source++;
                    break;
                case TN_CRED:
                    pSyncHdr.cred = buildCred(pDecoder);
                    break;
                case TN_META:
                    pSyncHdr.meta = buildPCData(pDecoder);
                    break;    
                    /* flags (empty tags) */
                case TN_NORESP:
                    pSyncHdr.flags |= Flag_t.SmlNoResp_f;
                    break;
                case TN_NORESULTS:
                    pSyncHdr.flags |= Flag_t.SmlNoResults_f;
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
    
            /* get next token */
            nextToken(pDecoder);
        }
    
        if ((sessionid == 0) || (msgid == 0) || (target == 0) || (source == 0) || (version == 0) || (proto == 0))
        {
          throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        return pSyncHdr;
    }
    
    public static SmlTargetOrSource_t buildTargetOrSource(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlTargetOrSource_t pTarget;
        long locuri = 0;
    
        pScanner = pDecoder.scanner;    
        pTarget = new SmlTargetOrSource_t();    
        if (IS_EMPTY(pScanner.curtok)) {
            throw new SmlException_t(Ret_t.SML_ERR_OK);
        }    
        nextToken(pDecoder);    
        while (!IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
    
                /* PCDATA elements */
                case TN_LOCURI:
                    pTarget.locURI = buildPCData(pDecoder);
                    locuri++;
                    break;
                case TN_LOCNAME:
                    pTarget.locName = buildPCData(pDecoder);
                    break;    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            nextToken(pDecoder);
        }
    
        if (locuri == 0) 
        {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        return pTarget;
    }

    public static SmlChal_t buildChal(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlChal_t pChal;
        long meta = 0;
    
        pScanner = pDecoder.scanner;
    
        pChal = new SmlChal_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pChal;
        }
    
        nextToken(pDecoder);
    
        while (!IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
    
                /* PCDATA elements */
                case TN_META:
                    pChal.meta = buildPCData(pDecoder);
                    meta++;
                    break;
    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            nextToken(pDecoder);
        }
    
        if (meta == 0) 
        {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }    
        return pChal;
    }

    public static SmlCred_t buildCred(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlCred_t pCred;
        long data = 0;     
        pScanner = pDecoder.scanner;    
        pCred = new SmlCred_t();    
        if (IS_EMPTY(pScanner.curtok)) {
            return pCred;
        }    
        nextToken(pDecoder);    
        while (!IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {    
                /* PCDATA elements */
                case TN_DATA:
                case TN_METINF_NEXT:
                    pCred.data = buildPCData(pDecoder);
                    data++;
                    break;
                case TN_META:
                    pCred.meta = buildPCData(pDecoder);
                    break;    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            nextToken(pDecoder);
        }    
        if (data == 0)
        {
          throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }    
        return pCred;
    }

    public static SmlItem_t buildItem(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlItem_t pItem;
    
        pScanner = pDecoder.scanner;
    
        pItem = new SmlItem_t();
    
        /* Item might be empty */
        if (IS_EMPTY(pScanner.curtok)) {
            return pItem;
        }
    
        nextToken(pDecoder);
    
        while (!IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
                case TN_META:
                    pItem.meta = buildPCData(pDecoder);
                    break;
                case TN_DATA:
                    pItem.data = buildPCData(pDecoder);
                    break;
                case TN_TARGET:
                    pItem.target = buildTargetOrSource(pDecoder);
                    break;
                case TN_SOURCE:
                    pItem.source = buildTargetOrSource(pDecoder);
                    break;
                case TN_MOREDATA:
                    pItem.flags |= Flag_t.SmlMoreData_f;
                    break;    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            nextToken(pDecoder);
        }    
        return pItem;
    }

    public static SmlMapItem_t buildMapItem(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlMapItem_t pMapItem;
        long target = 0, source = 0;
    
        pScanner = pDecoder.scanner;
    
        pMapItem = new SmlMapItem_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        nextToken(pDecoder);
    
        while (!IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
                /* child tags */
                case TN_TARGET:
                    pMapItem.target = buildTargetOrSource(pDecoder);
                    target++;
                    break;
                case TN_SOURCE:
                    pMapItem.source = buildTargetOrSource(pDecoder);
                    source++;
                    break;
    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            nextToken(pDecoder);
        }
    
        if ((target == 0) || (source == 0)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        return pMapItem;
    }

    public static SmlPcdata_t buildPCData(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlPcdata_t pPCData = null;
        SmlPcdataExtension_t ext;
    
        pScanner = pDecoder.scanner;
    
        if (IS_EMPTY(pScanner.curtok)) {
            pPCData = new SmlPcdata_t();
            return pPCData;
        } 
    
        pPCData = null;
       
        try {
            nextToken(pDecoder);
        }
        catch(SmlException_t e) {
            if(e.getErrorCode() == Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC) {
                return pScanner.curtok.pcdata;
            }
            throw e;
        }
    
        if (IS_CONTENT(pScanner.curtok)) {
            /* PCData element has a regular string or opaque content */
            while (!IS_END(pScanner.curtok)) {
                if (pPCData == null)
                    pPCData = pScanner.curtok.pcdata;
                else {
                    pPCData = concatPCData(pPCData, pScanner.curtok.pcdata);    				
                    if (pPCData == null)
                        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PCDATA);
                }                   
                nextToken(pDecoder);
            }
        } 
        else if (IS_START_OR_EMPTY(pScanner.curtok)) {
            /* PCData element contains an XML dokument that is handled by an
               extension mechanism  */
            ext = pScanner.curtok.ext;
            discardToken(pDecoder);
            pPCData = new SmlPcdata_t();
            pPCData.contentType = SmlPcdataType_t.SML_PCDATA_EXTENSION;
            pPCData.extension = ext;
            switch (ext) {
  				case SML_EXT_METINF:
                    pPCData.content = XltMetInf.buildMetInfMetInfCmd(pDecoder).toByteArray();
                    break;
   				case SML_EXT_DEVINF:                    
                    pPCData.content = XltDevInf.buildDevInfDevInfCmd(pDecoder).toByteArray();
                    /* the scanner must point to the closing PCDATA tag */
                    nextToken(pDecoder); 
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_EXT);
            }
    
        } 
        else if (IS_END(pScanner.curtok)) {
            /* PCData element is empty */
        } 
        else {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PCDATA);
        }
        if (!IS_END(pScanner.curtok))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PCDATA);
    
        if (pPCData == null) {
            pPCData = new SmlPcdata_t();
        }
    
        return pPCData;
    }

    public static SmlPcdataList_t buildPCDataList(
        final XltDecoder_t pDecoder, 
        SmlPcdataList_t ppPCData
    ) throws SmlException_t {
		SmlPcdataList_t pPCDataList = null, pPrev = null;
    
		pPCDataList = (SmlPcdataList_t)ppPCData;

		/* advance to the end of the list, and create ther an empty list element */
		while (pPCDataList != null) {
			pPrev = pPCDataList;
			pPCDataList = pPrev.next;
		}
		pPCDataList = new SmlPcdataList_t();
		if (pPrev != null) /* we already had some entries in the list */
			pPrev.next = pPCDataList;
		else /* nope we created a new list */
			ppPCData = pPCDataList;
		pPCDataList.data = buildPCData(pDecoder);
		/* at this point pPCDataList should point to an valid list element */
        return ppPCData;
    }


    public static SmlItemList_t appendItemList(
        final XltDecoder_t pDecoder, 
        SmlItemList_t ppItemList
    ) throws SmlException_t {
        SmlItemList_t pNewItemList;
        SmlItemList_t pItemList;
    
        pItemList = ppItemList;
        if (pItemList != null)
            while (pItemList.next != null)
                pItemList = pItemList.next;
    
        pNewItemList = new SmlItemList_t();
    
        pNewItemList.item = buildItem(pDecoder);
    
        if (pItemList == null)
            ppItemList = pNewItemList;
        else
            pItemList.next = pNewItemList;
    
        return ppItemList;
    }

    public static SmlSourceList_t appendSourceList(
        final XltDecoder_t pDecoder, 
        SmlSourceList_t ppSourceList
    ) throws SmlException_t {
        SmlSourceList_t pNewSourceList;
        SmlSourceList_t pSourceList;
        pSourceList = ppSourceList;
        if (pSourceList != null)
            while (pSourceList.next != null)
                pSourceList = pSourceList.next;
    
        pNewSourceList = new SmlSourceList_t();
    
        pNewSourceList.source = buildTargetOrSource(pDecoder);
    
        if (pSourceList == null)
            ppSourceList = pNewSourceList;
        else
            pSourceList.next = pNewSourceList;
    
        return ppSourceList;
    }


    public static SmlMapItemList_t appendMapItemList(
        final XltDecoder_t pDecoder, 
        SmlMapItemList_t ppMapItemList
    ) throws SmlException_t {
        SmlMapItemList_t pNewMapItemList;
        SmlMapItemList_t pMapItemList;
    
        pMapItemList = ppMapItemList;
        if (pMapItemList != null)
            while (pMapItemList.next != null)
                pMapItemList = pMapItemList.next;
    
        pNewMapItemList = new SmlMapItemList_t();
    
        pNewMapItemList.mapItem = buildMapItem(pDecoder);
    
        if (pMapItemList == null)
            ppMapItemList = pNewMapItemList;
        else
            pMapItemList.next = pNewMapItemList;
    
        return ppMapItemList;
    }


    public static SmlTargetRefList_t appendTargetRefList(
        final XltDecoder_t pDecoder, 
        SmlTargetRefList_t ppTargetRefList
    ) throws SmlException_t {
        SmlTargetRefList_t pNewTargetRefList;
        SmlTargetRefList_t pTargetRefList;
    
        pTargetRefList = ppTargetRefList;
        if (pTargetRefList != null)
            while (pTargetRefList.next != null)
                pTargetRefList = pTargetRefList.next;
    
        pNewTargetRefList = new SmlTargetRefList_t();
        pNewTargetRefList.targetRef = buildPCData(pDecoder);
    
        if (pTargetRefList == null)
            ppTargetRefList = pNewTargetRefList;
        else
            pTargetRefList.next = pNewTargetRefList;
    
        return ppTargetRefList;
    }

    public static SmlSourceRefList_t appendSourceRefList(
        final XltDecoder_t pDecoder, 
        SmlSourceRefList_t ppSourceRefList
    ) throws SmlException_t {
        SmlSourceRefList_t pNewSourceRefList;
        SmlSourceRefList_t pSourceRefList;
    
        pSourceRefList = ppSourceRefList;
        if (pSourceRefList != null)
            while (pSourceRefList.next != null)
                pSourceRefList = pSourceRefList.next;
    
        pNewSourceRefList = new SmlSourceRefList_t();
    
        pNewSourceRefList.sourceRef = buildPCData(pDecoder);
    
        if (pSourceRefList == null)
            ppSourceRefList = pNewSourceRefList;
        else
            pSourceRefList.next = pNewSourceRefList;
    
        return ppSourceRefList;
    }

}
