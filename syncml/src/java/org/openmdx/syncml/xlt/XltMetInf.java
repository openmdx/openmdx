//*************************************************************************/
//* module:          MetaInf DTD related functions for the en-/decoder    */
//* file:            xltmetinf.c                                          */
//* target system:   all                                                  */
//* target OS:       all                                                  */   
//*************************************************************************/

package org.openmdx.syncml.xlt;

import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataList_t;

public class XltMetInf {
    
    public static SmlMetInfAnchor_t buildMetInfAnchorCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlMetInfAnchor_t pAnchor;
    
        pScanner = pDecoder.scanner;
    
        pAnchor = new SmlMetInfAnchor_t();
    
        if (pScanner.curtok.type == XltTokType_t.TOK_TAG_EMPTY) {
            return pAnchor;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
    				    /* PCDATA elements */
                case TN_METINF_LAST:
                    pAnchor.last = XltDec.buildPCData(pDecoder);
                    break;
                case TN_METINF_NEXT:
                    pAnchor.next = XltDec.buildPCData(pDecoder);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pAnchor;
    }
    
    public static SmlMetInfMem_t buildMetInfMemCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlMetInfMem_t pMem;
    
        pScanner = pDecoder.scanner;
    
        pMem = new SmlMetInfMem_t();
    
        if (pScanner.curtok.type == XltTokType_t.TOK_TAG_END) {
            return pMem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
    				    /* PCDATA elements */
                case TN_METINF_SHAREDMEM:
                    pMem.shared = XltDec.buildPCData(pDecoder);
                    break;
                case TN_METINF_FREEMEM:
                    pMem.free = XltDec.buildPCData(pDecoder);
                    break;
                case TN_METINF_FREEID:
                    pMem.freeid = XltDec.buildPCData(pDecoder);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pMem;
    }
    
    public static SmlMetInfMetInf_t buildMetInfMetInfCmd(
        XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlMetInfMetInf_t pMeta;
        boolean foundWrapper =  false;
    
        pScanner = pDecoder.scanner;
    
        pMeta = new SmlMetInfMetInf_t();
    
        if (pScanner.curtok.type == XltTokType_t.TOK_TAG_END) {
            return pMeta;
        }
    
       XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
    		  case TN_METINF_METINF: /* ignore - it's just the wrapper tag */
    			foundWrapper = true;
    			break;
              case TN_METINF_FORMAT:
                pMeta.format = XltDec.buildPCData(pDecoder);
                break;
              case TN_METINF_TYPE:
                pMeta.type = XltDec.buildPCData(pDecoder);
                break;
              case TN_METINF_MARK:
                pMeta.mark = XltDec.buildPCData(pDecoder);
                break;
              case TN_METINF_SIZE:
                pMeta.size = XltDec.buildPCData(pDecoder);
                break;
              case TN_METINF_VERSION:
                pMeta.version = XltDec.buildPCData(pDecoder);
                break;
    		  case TN_METINF_NEXTNONCE:
    			pMeta.nextnonce = XltDec.buildPCData(pDecoder);
    			break;
    		  case TN_METINF_ANCHOR:
    			pMeta.anchor = buildMetInfAnchorCmd(pDecoder);
    			break;
    		  case TN_METINF_MAXMSGSIZE:
    			pMeta.maxmsgsize = XltDec.buildPCData(pDecoder);
    			break;
    		  /* SCTSTK - 18/03/2002 S.H. 2002-04-05: SyncML 1.1 */
    		  case TN_METINF_MAXOBJSIZE:
    			pMeta.maxobjsize = XltDec.buildPCData(pDecoder);
    			break;
    		  case TN_METINF_MEM:
    			pMeta.mem = buildMetInfMemCmd(pDecoder);
    			break;
    		  case TN_METINF_EMI:
    			pMeta.emi = XltDec.buildPCDataList(pDecoder, pMeta.emi);
    			break;
              default:
                  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
    
    	if (foundWrapper) { 
    		/* Optional Metinf root tag was used in this message.
    		 * The actual token is the closing root tag.
    		 * It is required that the scanner points to the first tag _after_ 
    		 * <MetInf>...</MetInf>, so we just skip to the next token and continue.
    		 */
          XltDec.nextToken(pDecoder);
    	}
        return pMeta;
    }
    
    /* see xltenc.c:XltEncBlock for description of parameters */
    public static void metinfEncBlock(
        final XltTagID_t tagId, 
        final XltRO_t reqOptFlag, 
        final Object pContent, 
        final SmlEncoding_t enc, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
    	SmlPcdataList_t pList = null;
    	//Check if pContent of a required field is missing
    	if ((reqOptFlag == XltRO_t.REQUIRED) && (pContent == null))
    		throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
    	//Check if pContent of a optional field is missing . if yes we are done
    	else if (pContent == null)
    		return;
      
    	//Generate the commands . see DTD
    	switch (tagId) {
    	case TN_METINF_ANCHOR:
    		XltEnc.xltGenerateTag(XltTagID_t.TN_METINF_ANCHOR, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_LAST, XltRO_t.OPTIONAL, ((SmlMetInfAnchor_t) pContent).last, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_NEXT, XltRO_t.REQUIRED, ((SmlMetInfAnchor_t) pContent).next, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		XltEnc.xltGenerateTag(XltTagID_t.TN_METINF_ANCHOR, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		break;
    	case TN_METINF_MEM:
    		XltEnc.xltGenerateTag(XltTagID_t.TN_METINF_MEM, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_SHAREDMEM, XltRO_t.OPTIONAL, ((SmlMetInfMem_t) pContent).shared, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_FREEMEM,   XltRO_t.REQUIRED, ((SmlMetInfMem_t) pContent).free,   enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_FREEID,    XltRO_t.REQUIRED, ((SmlMetInfMem_t) pContent).freeid, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		XltEnc.xltGenerateTag(XltTagID_t.TN_METINF_MEM, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		break;
    	case TN_METINF_SHAREDMEM:                  
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number)pContent).intValue()) & (Flag_t.SmlMetInfSharedMem_f)) != 0)
            XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
          break;
    	case TN_METINF_METINF:
    		metinfEncBlock(XltTagID_t.TN_METINF_FORMAT,    XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).format, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_TYPE,      XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).type, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_MARK,      XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).mark, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_SIZE,      XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).size, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_ANCHOR,    XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).anchor, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_VERSION,   XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).version, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_NEXTNONCE, XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).nextnonce, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_MAXMSGSIZE,XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).maxmsgsize, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		metinfEncBlock(XltTagID_t.TN_METINF_MAXOBJSIZE,XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).maxobjsize, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		pList = ((SmlMetInfMetInf_t)pContent).emi;
            while (pList != null) {        
                XltEnc.xltEncBlock(XltTagID_t.TN_METINF_EMI, XltRO_t.OPTIONAL, pList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
                pList = pList.next;
            };    
    		metinfEncBlock(XltTagID_t.TN_METINF_MEM,       XltRO_t.OPTIONAL, ((SmlMetInfMetInf_t) pContent).mem, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_METINF);
    		break;
    	default: { // all leaf nodes (PCDATA#)
            XltEnc.xltEncPcdata(tagId, reqOptFlag, pContent, enc, pBufMgr, attFlag);
    	 } /* eof default statement from switch tagid */
    	} /* eof switch tagid */
    }

}
