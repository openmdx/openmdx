///*************************************************************************/
///* module:          DeviceInf DTD related functions for the en-/decoder  */
///* file:            xltdevinf.c                                          */
///* target system:   all                                                  */
///* target OS:       all                                                  */   
///*************************************************************************/

package org.openmdx.syncml.xlt;

import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlDevInfCTCap_t;
import org.openmdx.syncml.SmlDevInfCTDataList_t;
import org.openmdx.syncml.SmlDevInfCTDataPropList_t;
import org.openmdx.syncml.SmlDevInfCTDataProp_t;
import org.openmdx.syncml.SmlDevInfCTData_t;
import org.openmdx.syncml.SmlDevInfCtcapList_t;
import org.openmdx.syncml.SmlDevInfDSMem_t;
import org.openmdx.syncml.SmlDevInfDatastoreList_t;
import org.openmdx.syncml.SmlDevInfDatastore_t;
import org.openmdx.syncml.SmlDevInfDevInf_t;
import org.openmdx.syncml.SmlDevInfExtList_t;
import org.openmdx.syncml.SmlDevInfExt_t;
import org.openmdx.syncml.SmlDevInfSyncCap_t;
import org.openmdx.syncml.SmlDevInfXmitList_t;
import org.openmdx.syncml.SmlDevInfXmit_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataList_t;
import org.openmdx.syncml.SmlPcdataType_t;
import org.openmdx.syncml.SmlPcdata_t;

public class XltDevInf {
   
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
    
    public static SmlDevInfDevInf_t buildDevInfDevInfCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfDevInf_t pElem = null;
    
        pScanner = pDecoder.scanner;
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        switch (pScanner.curtok.tagid) {
            case TN_DEVINF_DEVINF:
                pElem = buildDevInfDevInfContent(pDecoder);
                break;
            default:
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
        return pElem;
    }

    public static SmlDevInfDevInf_t buildDevInfDevInfContent(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfDevInf_t pElem;
    	/* Modified by Tomy to allow <UTC></UTC>, <SupportNumberOfChanges></SupportNumberOfChanges> and <SupportLargeObjs></SupportLargeObjs> */
    	SmlPcdata_t tmp_ptr;
    	/* End modified by Tomy */
    
        pScanner = pDecoder.scanner;
    
        pElem = new SmlDevInfDevInf_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
                case TN_DEVINF_VERDTD:
                    pElem.verdtd = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_MAN:
                    pElem.man = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_MOD:
                    pElem.mod = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_OEM:
                    pElem.oem = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_FWV:
                    pElem.fwv = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_SWV:
                    pElem.swv = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_HWV:
                    pElem.hwv = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_DEVID:
                    pElem.devid = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_DEVTYP:
                    pElem.devtyp = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_DATASTORE:
                    pElem.datastore = buildDevInfDataStoreList(pDecoder, pElem.datastore);
                    break;
                case TN_DEVINF_CTCAP:
                    pElem.ctcap = buildDevInfCtcap(pDecoder, pElem.ctcap);
                    break;
                case TN_DEVINF_EXT:
                    pElem.ext = buildDevInfExtList(pDecoder,  pElem.ext);
                    break;
                /* SCTSTK - 18/03/2002 S.H. 2002-04-05 : SyncML 1.1 */
                case TN_DEVINF_UTC:
                    pElem.flags |= Flag_t.SmlDevInfUTC_f;
    				/* Modified by Tomy to allow <UTC></UTC> */
    				tmp_ptr = null;
    				tmp_ptr = XltDec.buildPCData(pDecoder);
    				if (tmp_ptr.contentType != SmlPcdataType_t.SML_PCDATA_UNDEFINED && tmp_ptr.extension != SmlPcdataExtension_t.SML_EXT_UNDEFINED && ((byte[])tmp_ptr.content).length != 0) {
                        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                    }
    				/* End modified by Tomy */
                    break;
                case TN_DEVINF_NOFM:
                    pElem.flags |= Flag_t.SmlDevInfNOfM_f;
    				/* Modified by Tomy to allow <SupportNumberOfChanges></SupportNumberOfChanges> */
    				tmp_ptr = null;
    				tmp_ptr = XltDec.buildPCData(pDecoder);
    				if (tmp_ptr.contentType != SmlPcdataType_t.SML_PCDATA_UNDEFINED && tmp_ptr.extension != SmlPcdataExtension_t.SML_EXT_UNDEFINED && ((byte[])tmp_ptr.content).length != 0) {
                        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                    }
    				/* End modified by Tomy */
                   break;
                case TN_DEVINF_LARGEOBJECT:
                    pElem.flags |= Flag_t.SmlDevInfLargeObject_f;
    				/* Modified by Tomy to allow <SupportLargeObjs></SupportLargeObjs> */
    				tmp_ptr = null;
    				tmp_ptr = XltDec.buildPCData(pDecoder);
    				if (tmp_ptr.contentType != SmlPcdataType_t.SML_PCDATA_UNDEFINED && tmp_ptr.extension != SmlPcdataExtension_t.SML_EXT_UNDEFINED && ((byte[])tmp_ptr.content).length != 0) {
                        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                    }
    				/* End modified by Tomy */
                   break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pElem;
    }

    public static SmlDevInfDatastore_t buildDevInfDataStoreCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfDatastore_t pElem;
    
        pScanner = pDecoder.scanner;
    
        pElem = new SmlDevInfDatastore_t();

    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
    		    /* PCDATA elements */
                case TN_DEVINF_SOURCEREF:
                    pElem.sourceref = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_DISPLAYNAME:
                    pElem.displayname = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_MAXGUIDSIZE:
                    pElem.maxguidsize = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_RXPREF:
                    pElem.rxpref = buildDevInfXmitCmd(pDecoder);
                    break;
                case TN_DEVINF_TXPREF:
                    pElem.txpref = buildDevInfXmitCmd(pDecoder);
                    break;
                case TN_DEVINF_RX:
                     pElem.rx = buildDevInfXmitList(pDecoder, pElem.rx );
                    break;
                case TN_DEVINF_TX:
                    pElem.tx = buildDevInfXmitList(pDecoder, pElem.tx);
                    break;
                case TN_DEVINF_DSMEM:
                    pElem.dsmem = buildDevInfDSMemCmd(pDecoder);
                    break;
                case TN_DEVINF_SYNCCAP:
                    pElem.synccap = buildDevInfSyncCapCmd(pDecoder);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pElem;
    }

    public static SmlDevInfXmit_t buildDevInfXmitCmd(
        final XltDecoder_t pDecoder
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfXmit_t pXmit;
    
        pScanner = pDecoder.scanner;
    
        pXmit = new SmlDevInfXmit_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pXmit;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
    		    /* PCDATA elements */
                case TN_DEVINF_CTTYPE:
                    pXmit.cttype = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_VERCT:
                    pXmit.verct = XltDec.buildPCData(pDecoder);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pXmit;
    }

    public static SmlDevInfXmitList_t buildDevInfXmitList(
        final XltDecoder_t pDecoder, 
        SmlDevInfXmitList_t ppElem
    ) throws SmlException_t {
        SmlDevInfXmitList_t pElem = null, pPrev = null;
    
        pElem = ppElem;
    
        /* advance to the end of the list, and create ther an empty list element */
        while (pElem != null) {
    	    pPrev = pElem;
    	    pElem = pPrev.next;
        }
        pElem = new SmlDevInfXmitList_t();
        if (pPrev != null) /* we already had some entries in the list */
    	    pPrev.next = pElem;
        else /* nope we created a new list */
    	    ppElem = pElem;
        pElem.data = null;
        /* at this point pElem should point to an valid list element */
        pElem.data = buildDevInfXmitCmd(pDecoder);
        return pElem;
    }

    public static SmlDevInfDatastoreList_t buildDevInfDataStoreList(
        final XltDecoder_t pDecoder, 
        Object ppElem
    ) throws SmlException_t {
        SmlDevInfDatastoreList_t pElem = null, pPrev = null;
    
        pElem = (SmlDevInfDatastoreList_t) ppElem;
    
        /* advance to the end of the list, and create ther an empty list element */
        while (pElem != null) {
    	    pPrev = pElem;
    	    pElem = pPrev.next;
        }
        pElem = new SmlDevInfDatastoreList_t();
        if (pPrev != null) /* we already had some entries in the list */
    	    pPrev.next = pElem;
        else /* nope we created a new list */
    	    ppElem = pElem;
        pElem.data = null;
        /* at this point pElem should point to an valid list element */
        pElem.data = buildDevInfDataStoreCmd(pDecoder);
        return pElem;
    }

    public static SmlDevInfExtList_t buildDevInfExtList(
        final XltDecoder_t pDecoder, 
        Object ppElem
    ) throws SmlException_t {
        SmlDevInfExtList_t pElem = null, pPrev = null;
    
        pElem = (SmlDevInfExtList_t)ppElem;
    
        /* advance to the end of the list, and create ther an empty list element */
        while (pElem != null) {
    	    pPrev = pElem;
    	    pElem = pPrev.next;
        }
        pElem = new SmlDevInfExtList_t();
        if (pPrev != null) /* we already had some entries in the list */
    	    pPrev.next = pElem;
        else /* nope we created a new list */
    	    ppElem = pElem;
        pElem.data = null;
        /* at this point pElem should point to an valid list element */
        pElem.data = buildDevInfExtCmd(pDecoder);
        return pElem;
    }


    public static SmlDevInfCtcapList_t buildDevInfCtcap(
        final XltDecoder_t pDecoder,
        SmlDevInfCtcapList_t ppElem
    ) throws SmlException_t {
        SmlDevInfCtcapList_t       pCtcap        = null, pPrev = null;
        SmlDevInfCTDataPropList_t  pOldProp      = null, pProp = null;
        SmlDevInfCTDataList_t      pOldParam     = null, pParam = null;
        SmlDevInfCtcapList_t       pElem         = null;
        XltDecScanner_t            pScanner;
    
        pElem = (SmlDevInfCtcapList_t)ppElem;
        pScanner = pDecoder.scanner;
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
            case TN_DEVINF_CTTYPE:        
                pCtcap = (SmlDevInfCtcapList_t)ppElem;
                /* advance to the end of the list, and create ther an empty list element */
                while (pCtcap != null) {
    	            pPrev = pCtcap;
    	            pCtcap = pPrev.next;
                }
                pCtcap = new SmlDevInfCtcapList_t();
                if (pPrev != null) /* we already had some entries in the list */
    	            pPrev.next = pCtcap;
                else /* nope we created a new list */
    	            ppElem = pCtcap;
                pCtcap.data = new SmlDevInfCTCap_t();
                pCtcap.data.cttype = XltDec.buildPCData(pDecoder);
                break;
            case TN_DEVINF_PROPNAME:
                pCtcap = (SmlDevInfCtcapList_t)ppElem;
                if (pCtcap == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                while (pCtcap.next != null) {
                    pPrev = pCtcap;
                    pCtcap = pCtcap.next;
                }
                /* here we are at the latest defined DevInfCTCapPtr_t */
                /* now we need to create a new DevInfCTDataPtr_t element, tostore the properties name */
                pOldProp = null;
                pProp    = pCtcap.data.prop;
                while (pProp != null) {
                    pOldProp = pProp;
                    pProp = pProp.next;
                }
                pProp = new SmlDevInfCTDataPropList_t();
                if (pOldProp != null)
                    pOldProp.next = pProp;
                else 
                    pCtcap.data.prop = pProp;
                pProp.data = new SmlDevInfCTDataProp_t();
                pProp.data.prop = new SmlDevInfCTData_t();
                if (pProp.data.prop == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
                }
                pProp.data.prop.name = XltDec.buildPCData(pDecoder);
                break;
            case TN_DEVINF_PARAMNAME:
                pCtcap = (SmlDevInfCtcapList_t)ppElem;
                if (pCtcap == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                while (pCtcap.next != null) {
                    pPrev = pCtcap;
                    pCtcap = pCtcap.next;
                }
                /* here we are at the latest defined DevInfCTCapPtr_t */
                pProp    = pCtcap.data.prop;
                if (pProp == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                while (pProp.next != null) {
                    pProp = pProp.next;
                }
                /* here we are at the latest defined PropList Element in the latest defined CTCap element */
                /* now lets insert a new Param element into this property */
                pOldParam = null;
                pParam = pProp.data.param;
                while (pParam != null) {
                    pOldParam = pParam;
                    pParam    = pParam.next;
                }
                pParam = new SmlDevInfCTDataList_t();
                if (pOldParam != null)
                    pOldParam.next = pParam;
                else
                    pProp.data.param = pParam;
                pParam.data = new SmlDevInfCTData_t();
                pParam.data.name = XltDec.buildPCData(pDecoder);
                break;
            case TN_DEVINF_DISPLAYNAME:
            case TN_DEVINF_VALENUM:
            case TN_DEVINF_DATATYPE:
            case TN_DEVINF_SIZE:
                /* The code for the above 4 is basically the same.
                 * The hardpart is finding the right SmlDevInfCTDataPtr_t
                 * struct, as it can be either within a Property or an Parameter.
                 */
                pCtcap = (SmlDevInfCtcapList_t)ppElem;
                if (pCtcap == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                while (pCtcap.next != null) {
                    pCtcap = pCtcap.next;
                }
                /* here we are at the latest defined DevInfCTCapPtr_t */
                pProp    = pCtcap.data.prop;
                if (pProp == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                while (pProp.next != null) {
                    pProp = pProp.next;
                }
    
                if (pProp.data == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                if (pProp.data.prop == null) {
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                }
                if (pProp.data.param == null) {
                    /* No Param's yet so we have Property fields to fill */
                    switch(pScanner.curtok.tagid) {
                    case TN_DEVINF_DISPLAYNAME:
                        pProp.data.prop.dname = XltDec.buildPCData(pDecoder);
                        break;
                    case TN_DEVINF_VALENUM:
                        pProp.data.prop.valenum = XltDec.buildPCDataList(pDecoder, pProp.data.prop.valenum);
                        break;
                    case TN_DEVINF_DATATYPE:
                        pProp.data.prop.datatype = XltDec.buildPCData(pDecoder);
                        break;
                    case TN_DEVINF_SIZE:
                        pProp.data.prop.size = XltDec.buildPCData(pDecoder);
                        break;
                    default:
                        break;
                    }
                } else {
                    pParam = pProp.data.param;
                    while (pParam.next != null) {
                        pParam = pParam.next;
                    }
                    if (pParam.data == null) {
                        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
                    }
                    switch(pScanner.curtok.tagid) {
                    case TN_DEVINF_DISPLAYNAME:
                        pParam.data.dname = XltDec.buildPCData(pDecoder);
                        break;
                    case TN_DEVINF_VALENUM:
                        pParam.data.valenum = XltDec.buildPCDataList(pDecoder, pParam.data.valenum);
                        break;
                    case TN_DEVINF_DATATYPE:
                        pParam.data.datatype = XltDec.buildPCData(pDecoder);
                        break;
                    case TN_DEVINF_SIZE:
                        pParam.data.size = XltDec.buildPCData(pDecoder);
                        break;
                    default:
                        break;
                    }
                }
                break;
            default:
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        } /* eof while */
        return ppElem;
    }

    public static SmlDevInfDSMem_t buildDevInfDSMemCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfDSMem_t pElem;
    
        pScanner = pDecoder.scanner;
    
        pElem = new SmlDevInfDSMem_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
        		    /* PCDATA elements */
                case TN_DEVINF_SHAREDMEM:
                    // %%% luz:2003-04-28: made work as a flag
                    pElem.flags |= Flag_t.SmlDevInfSharedMem_f;
                    // rc = buildPCData(pDecoder, (VoidPtr_t)&pElem.shared);
                    break;
                case TN_DEVINF_MAXMEM:
                    pElem.maxmem = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_MAXID:
                    pElem.maxid = XltDec.buildPCData(pDecoder);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pElem;
    }

    public static SmlDevInfCTCap_t buildDevInfCTCapCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfCTCap_t pElem;
    
        pScanner = pDecoder.scanner;
    
        pElem = new SmlDevInfCTCap_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
                case TN_DEVINF_CTTYPE:
                    pElem.cttype = XltDec.buildPCData(pDecoder);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pElem;
    }

    public static SmlDevInfSyncCap_t buildDevInfSyncCapCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfSyncCap_t pElem;
    
        pScanner = pDecoder.scanner;
    
        pElem = new SmlDevInfSyncCap_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
                case TN_DEVINF_SYNCTYPE:
                    pElem.synctype = XltDec.buildPCDataList(pDecoder, pElem.synctype);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
        return pElem;
    }

    public static SmlDevInfExt_t buildDevInfExtCmd(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlDevInfExt_t pElem;
    
        pScanner = pDecoder.scanner;
    
        pElem = new SmlDevInfExt_t();
    
        if (IS_EMPTY(pScanner.curtok)) {
            return pElem;
        }
    
        XltDec.nextToken(pDecoder);
    
        while (pScanner.curtok.type != XltTokType_t.TOK_TAG_END) {
            switch (pScanner.curtok.tagid) {
                case TN_DEVINF_XNAM:
                    pElem.xnam = XltDec.buildPCData(pDecoder);
                    break;
                case TN_DEVINF_XVAL:
                    pElem.xval = XltDec.buildPCDataList(pDecoder, pElem.xval);
                    break;
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
           XltDec.nextToken(pDecoder);
        }
        return pElem;
    }


/* see xltenc.c:XltEncBlock for description of parameters */
    public static void devinfEncBlock(
        final XltTagID_t tagId, 
        final XltRO_t reqOptFlag, 
        final Object pContent, 
        final SmlEncoding_t enc, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {
        
    	SmlPcdataList_t pList = null, p2List = null;
        SmlDevInfDatastoreList_t dsList = null;
        SmlDevInfCtcapList_t ctList = null;
        SmlDevInfExtList_t exList = null;
        SmlDevInfXmitList_t xmList = null;
        SmlDevInfCTDataPropList_t propList = null;
        SmlDevInfCTDataList_t paramList = null;
    
        // Check if pContent of a required field is missing
    	if ((reqOptFlag == XltRO_t.REQUIRED) && (pContent == null))
    		 throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
    	//Check if pContent of a optional field is missing . if yes we are done
    	else if (pContent == null)
    		 return;
      
    	//Generate the commands . see DTD
    	switch (tagId) {
      	case TN_DEVINF_EXT:
      		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_EXT, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
      		devinfEncBlock(XltTagID_t.TN_DEVINF_XNAM, XltRO_t.REQUIRED, ((SmlDevInfExt_t) pContent).xnam, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
      		pList = ((SmlDevInfExt_t)pContent).xval;
              while (pList != null) {        
                  devinfEncBlock(XltTagID_t.TN_DEVINF_XVAL, XltRO_t.OPTIONAL, pList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                  pList = pList.next;
              };
      		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_EXT, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
      		break;
      	case TN_DEVINF_SYNCCAP:
      		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_SYNCCAP, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
      		pList = ((SmlDevInfSyncCap_t)pContent).synctype;
              while (pList != null) {        
                  devinfEncBlock(XltTagID_t.TN_DEVINF_SYNCTYPE, XltRO_t.OPTIONAL, pList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                  pList = pList.next;
              };
      		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_SYNCCAP, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
      		break;
      	case TN_DEVINF_SHAREDMEM:                  
          //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number)pContent).intValue()) & (Flag_t.SmlDevInfSharedMem_f)) != 0)
              XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
          break;
        // %%% luz:2003-04-28 added missing 1.1 devinf tags here
    	  case TN_DEVINF_UTC:
    	    //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number)pContent).intValue()) & (Flag_t.SmlDevInfUTC_f)) != 0) {
            XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
          }
          break;
    	  case TN_DEVINF_NOFM:
    	    //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number)pContent).intValue()) & (Flag_t.SmlDevInfNOfM_f)) != 0) {
            XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
          }
          break;
    	  case TN_DEVINF_LARGEOBJECT:
    	    //set the flag in the (WB)XML document if the flag is in the pContent
          if (((((Number)pContent).intValue()) & (Flag_t.SmlDevInfLargeObject_f)) != 0) {
            XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_ALL, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
          }
          break;
        // %%% end luz
        case TN_DEVINF_CTCAP:
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_CTCAP, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
            ctList = ((SmlDevInfCtcapList_t)pContent);
            if (ctList == null)  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_INPUT_DATA);
            while (ctList != null) { 
                if (ctList.data == null)  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_INPUT_DATA);
                devinfEncBlock(XltTagID_t.TN_DEVINF_CTTYPE, XltRO_t.OPTIONAL, ctList.data.cttype, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                /* now the propList */
                // %%% luz 2002-11-27: made property list optional (e.g. text/message of P800 has none)
                propList = ctList.data.prop;
                // %%% original: if (propList == null) return SML_ERR_XLT_INVAL_INPUT_DATA;
                while (propList != null) {
                    if (propList.data == null)  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_INPUT_DATA);
                    if (propList.data.prop == null)  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_INPUT_DATA);
                    /* -- Propname */
                    devinfEncBlock(XltTagID_t.TN_DEVINF_PROPNAME, XltRO_t.REQUIRED, propList.data.prop.name, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                    /* -- (ValEnum+ | (Datatype, Size?))? */
                    //if (propList.data.prop.valenum == null && propList.data.prop.datatype == null) return SML_ERR_XLT_INVAL_INPUT_DATA;
                    if (propList.data.prop.valenum != null && propList.data.prop.datatype != null)  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_INPUT_DATA);
                    if (propList.data.prop.valenum != null) {
                        // ValEnum+
                        pList = propList.data.prop.valenum;
                        while (pList != null) {        
                            devinfEncBlock(XltTagID_t.TN_DEVINF_VALENUM, XltRO_t.REQUIRED, pList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                            pList = pList.next;
                        };
                    } else if (propList.data.prop.datatype != null) {
                        // Datatype, Size?
                        devinfEncBlock(XltTagID_t.TN_DEVINF_DATATYPE, XltRO_t.REQUIRED, propList.data.prop.datatype, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                        devinfEncBlock(XltTagID_t.TN_DEVINF_SIZE,     XltRO_t.OPTIONAL, propList.data.prop.size,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                    }
                    /* -- DisplayName ? */
                    devinfEncBlock(XltTagID_t.TN_DEVINF_DISPLAYNAME, XltRO_t.OPTIONAL, propList.data.prop.dname, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                    /* -- now the paramList */
                    paramList = propList.data.param;
                    while (paramList != null) {
                        devinfEncBlock(XltTagID_t.TN_DEVINF_PARAMNAME, XltRO_t.REQUIRED, paramList.data.name, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                        /* -- (ValEnum+ | (Datatype, Size?))? */
                        //if (paramList.data.valenum == null && paramList.data.datatype == null) return SML_ERR_XLT_INVAL_INPUT_DATA;
                        if (paramList.data.valenum != null && paramList.data.datatype != null)  throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_INPUT_DATA);
                        if (paramList.data.valenum != null) {
                            // ValEnum+
                            p2List = paramList.data.valenum;
                            while (p2List != null) {        
                                devinfEncBlock(XltTagID_t.TN_DEVINF_VALENUM, XltRO_t.REQUIRED, p2List.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                                p2List = p2List.next;
                            };
                        } else if (paramList.data.datatype != null) {
                            // Datatype, Size?
                            devinfEncBlock(XltTagID_t.TN_DEVINF_DATATYPE, XltRO_t.REQUIRED, paramList.data.datatype, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                            devinfEncBlock(XltTagID_t.TN_DEVINF_SIZE,     XltRO_t.OPTIONAL, paramList.data.size,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                        }
                        /* -- DisplayName ? */
                        devinfEncBlock(XltTagID_t.TN_DEVINF_DISPLAYNAME, XltRO_t.OPTIONAL, paramList.data.dname, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                        paramList = paramList.next;
                    }
                    propList = propList.next;
                }
                /* eof propList */
                ctList = ctList.next;
            };        
    
            XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_CTCAP, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		break;
        case TN_DEVINF_DSMEM:
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_DSMEM, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_SHAREDMEM, XltRO_t.OPTIONAL, (((SmlDevInfDSMem_t) pContent).flags), enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_MAXMEM,    XltRO_t.OPTIONAL, ((SmlDevInfDSMem_t) pContent).maxmem, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_MAXID,     XltRO_t.OPTIONAL, ((SmlDevInfDSMem_t) pContent).maxid,  enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_DSMEM, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		break;
            // special case, the following 4 have the same structure, only the tag name differs
        case TN_DEVINF_RX:
        case TN_DEVINF_TX:
        case TN_DEVINF_RXPREF:
        case TN_DEVINF_TXPREF:
    		XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_CTTYPE, XltRO_t.REQUIRED, ((SmlDevInfXmit_t) pContent).cttype, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_VERCT,  XltRO_t.REQUIRED, ((SmlDevInfXmit_t) pContent).verct,  enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		XltEnc.xltGenerateTag(tagId, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		break;
        case TN_DEVINF_DATASTORE:
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_DATASTORE, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_SOURCEREF,    XltRO_t.REQUIRED, ((SmlDevInfDatastore_t) pContent).sourceref,    enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_DISPLAYNAME,  XltRO_t.OPTIONAL, ((SmlDevInfDatastore_t) pContent).displayname,  enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_MAXGUIDSIZE,  XltRO_t.OPTIONAL, ((SmlDevInfDatastore_t) pContent).maxguidsize,  enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_RXPREF,       XltRO_t.REQUIRED, ((SmlDevInfDatastore_t) pContent).rxpref,       enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		xmList = ((SmlDevInfDatastore_t)pContent).rx;
            while (xmList != null) {        
                devinfEncBlock(XltTagID_t.TN_DEVINF_RX, XltRO_t.OPTIONAL, xmList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                xmList = xmList.next;
            };
    		devinfEncBlock(XltTagID_t.TN_DEVINF_TXPREF,       XltRO_t.REQUIRED, ((SmlDevInfDatastore_t) pContent).txpref,       enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		xmList = ((SmlDevInfDatastore_t)pContent).tx;
            while (xmList != null) {        
                devinfEncBlock(XltTagID_t.TN_DEVINF_TX, XltRO_t.OPTIONAL, xmList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                xmList = xmList.next;
            };
    		devinfEncBlock(XltTagID_t.TN_DEVINF_DSMEM,        XltRO_t.OPTIONAL, ((SmlDevInfDatastore_t) pContent).dsmem,        enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_SYNCCAP,      XltRO_t.REQUIRED, ((SmlDevInfDatastore_t) pContent).synccap,      enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_DATASTORE, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		break;
        case TN_DEVINF_DEVINF:
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_DEVINF, XltTagType_t.TT_BEG, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_VERDTD,  XltRO_t.REQUIRED, ((SmlDevInfDevInf_t) pContent).verdtd,  enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_MAN,     XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t) pContent).man,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_MOD,     XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t) pContent).mod,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_OEM,     XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t) pContent).oem,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_FWV,     XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t) pContent).fwv,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_SWV,     XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t) pContent).swv,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_HWV,     XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t) pContent).hwv,     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_DEVID,   XltRO_t.REQUIRED, ((SmlDevInfDevInf_t) pContent).devid,   enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_DEVTYP,  XltRO_t.REQUIRED, ((SmlDevInfDevInf_t) pContent).devtyp,  enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		// %%% luz:2003-04-28 added missing SyncML 1.1 devinf tags		
    		devinfEncBlock(XltTagID_t.TN_DEVINF_UTC,     XltRO_t.OPTIONAL, (((SmlDevInfDevInf_t) pContent).flags),     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_NOFM,    XltRO_t.OPTIONAL, (((SmlDevInfDevInf_t) pContent).flags),     enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		devinfEncBlock(XltTagID_t.TN_DEVINF_LARGEOBJECT, XltRO_t.OPTIONAL, (((SmlDevInfDevInf_t) pContent).flags), enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
        // %%% end luz		
    		
    		dsList = ((SmlDevInfDevInf_t)pContent).datastore;
            if (dsList == null) throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
            devinfEncBlock(XltTagID_t.TN_DEVINF_DATASTORE, XltRO_t.REQUIRED, dsList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
            dsList = dsList.next;
            while (dsList != null) {        
                devinfEncBlock(XltTagID_t.TN_DEVINF_DATASTORE, XltRO_t.OPTIONAL, dsList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                dsList = dsList.next;
            };  
            devinfEncBlock(XltTagID_t.TN_DEVINF_CTCAP, XltRO_t.OPTIONAL, ((SmlDevInfDevInf_t)pContent).ctcap, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		exList = ((SmlDevInfDevInf_t)pContent).ext;
            while (exList != null) {        
                devinfEncBlock(XltTagID_t.TN_DEVINF_EXT, XltRO_t.OPTIONAL, exList.data, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
                exList = exList.next;
            };
        
    		XltEnc.xltGenerateTag(XltTagID_t.TN_DEVINF_DEVINF, XltTagType_t.TT_END, enc, pBufMgr, SmlPcdataExtension_t.SML_EXT_DEVINF);
    		break;
    
    	default: { // all leaf nodes (PCDATA#)
            XltEnc.xltEncPcdata(tagId, reqOptFlag, pContent, enc, pBufMgr, attFlag);
    	 } //* eof default statement from switch tagid 
    	} // eof switch tagid 
    }

}
