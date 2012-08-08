//*************************************************************************/
//* module:          Definition of WBXML/XML tags for the en-/decoder     */
//* file:            XLTTags.c                                            */
//* target system:   all                                                  */
//* target OS:       all                                                  */   
//*************************************************************************/

package org.openmdx.syncml.xlt;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlVersion_t;

public class XltTags {
    
    public static final int  XML_MAX_TAGLEN = 35;
    
    static String[] SyncMLNamespaces = new String[]{
      "???",
      "SYNCML:SYNCML1.0",
      "SYNCML:SYNCML1.1"
    };

    static Dtd_t[] XltDtdTbl = new Dtd_t[]{
        new Dtd_t("SYNCML:SYNCML1.0", SmlPcdataExtension_t.SML_EXT_UNDEFINED), // %%% note that this is the default, will be override by syncml version specific string from 
        new Dtd_t("syncml:metinf",    SmlPcdataExtension_t.SML_EXT_METINF),
        new Dtd_t("syncml:devinf",    SmlPcdataExtension_t.SML_EXT_DEVINF),
        new Dtd_t(null,               SmlPcdataExtension_t.SML_EXT_LAST)        
    };

    static Tag_t[] syncml = new Tag_t[]{
    new Tag_t(XltTagID_t.TN_ADD,            (byte)0x05,    "Add"),
    new Tag_t(XltTagID_t.TN_ALERT,          (byte)0x06,    "Alert"),
    new Tag_t(XltTagID_t.TN_ARCHIVE,        (byte)0x07,    "Archive"),
    new Tag_t(XltTagID_t.TN_ATOMIC,         (byte)0x08,    "Atomic"),
    new Tag_t(XltTagID_t.TN_CHAL,           (byte)0x09,    "Chal"),
    new Tag_t(XltTagID_t.TN_CMD,            (byte)0x0A,    "Cmd"),
    new Tag_t(XltTagID_t.TN_CMDID,          (byte)0x0B,    "CmdID"),
    new Tag_t(XltTagID_t.TN_CMDREF,         (byte)0x0C,    "CmdRef"),
    new Tag_t(XltTagID_t.TN_COPY,           (byte)0x0D,    "Copy"),
    new Tag_t(XltTagID_t.TN_CRED,           (byte)0x0E,    "Cred"),
    new Tag_t(XltTagID_t.TN_DATA,           (byte)0x0F,    "Data"),
    new Tag_t(XltTagID_t.TN_DELETE,         (byte)0x10,    "Delete"),
    new Tag_t(XltTagID_t.TN_EXEC,           (byte)0x11,    "Exec"),
    new Tag_t(XltTagID_t.TN_FINAL,          (byte)0x12,    "Final"),
    new Tag_t(XltTagID_t.TN_GET,            (byte)0x13,    "Get"),
    new Tag_t(XltTagID_t.TN_ITEM,           (byte)0x14,    "Item"),
    new Tag_t(XltTagID_t.TN_LANG,           (byte)0x15,    "Lang"),
    new Tag_t(XltTagID_t.TN_LOCNAME,        (byte)0x16,    "LocName"),
    new Tag_t(XltTagID_t.TN_LOCURI,         (byte)0x17,    "LocURI"),
    new Tag_t(XltTagID_t.TN_MAP,            (byte)0x18,    "Map"),
    new Tag_t(XltTagID_t.TN_MAPITEM,        (byte)0x19,    "MapItem"),
    new Tag_t(XltTagID_t.TN_META,           (byte)0x1A,    "Meta"),
    new Tag_t(XltTagID_t.TN_MSGID,          (byte)0x1B,    "MsgID"),
    new Tag_t(XltTagID_t.TN_MSGREF,         (byte)0x1C,    "MsgRef"),
    new Tag_t(XltTagID_t.TN_NORESP,         (byte)0x1D,    "NoResp"),
    new Tag_t(XltTagID_t.TN_NORESULTS,      (byte)0x1E,    "NoResults"),
    new Tag_t(XltTagID_t.TN_PUT,            (byte)0x1F,    "Put"),
    new Tag_t(XltTagID_t.TN_REPLACE,        (byte)0x20,    "Replace"),
    new Tag_t(XltTagID_t.TN_RESPURI,        (byte)0x21,    "RespURI"),
    new Tag_t(XltTagID_t.TN_RESULTS,        (byte)0x22,    "Results"),
    new Tag_t(XltTagID_t.TN_SEARCH,         (byte)0x23,    "Search"),
    new Tag_t(XltTagID_t.TN_SEQUENCE,       (byte)0x24,    "Sequence"),
    new Tag_t(XltTagID_t.TN_SESSIONID,      (byte)0x25,    "SessionID"),
    new Tag_t(XltTagID_t.TN_SFTDEL,         (byte)0x26,    "SftDel"),
    new Tag_t(XltTagID_t.TN_SOURCE,         (byte)0x27,    "Source"),
    new Tag_t(XltTagID_t.TN_SOURCEREF,      (byte)0x28,    "SourceRef"),
    new Tag_t(XltTagID_t.TN_STATUS,         (byte)0x29,    "Status"),
    new Tag_t(XltTagID_t.TN_SYNC,           (byte)0x2A,    "Sync"),
    new Tag_t(XltTagID_t.TN_SYNCBODY,       (byte)0x2B,    "SyncBody"),
    new Tag_t(XltTagID_t.TN_SYNCHDR,        (byte)0x2C,    "SyncHdr"),
    new Tag_t(XltTagID_t.TN_SYNCML,         (byte)0x2D,    "SyncML"),
    new Tag_t(XltTagID_t.TN_TARGET,         (byte)0x2E,    "Target"),
    new Tag_t(XltTagID_t.TN_TARGETREF,      (byte)0x2F,    "TargetRef"),
    new Tag_t(XltTagID_t.TN_VERSION,        (byte)0x31,    "VerDTD"),
    new Tag_t(XltTagID_t.TN_PROTO,          (byte)0x32,    "VerProto"),
    new Tag_t(XltTagID_t.TN_NUMBEROFCHANGES,(byte)0x33,    "NumberOfChanges"),
    new Tag_t(XltTagID_t.TN_MOREDATA,       (byte)0x34,    "MoreData"),
    new Tag_t(XltTagID_t.TN_UNDEF,          (byte)0x00,    null)
  };

    static Tag_t[] metinf = new Tag_t[]{
    new Tag_t(XltTagID_t.TN_METINF_ANCHOR,      (byte)0x05,   "Anchor"),
    new Tag_t(XltTagID_t.TN_METINF_EMI,           (byte)0x06,   "EMI"),
    new Tag_t(XltTagID_t.TN_METINF_FORMAT,        (byte)0x07, "Format"),
    new Tag_t(XltTagID_t.TN_METINF_FREEID,        (byte)0x08, "FreeID"),
    new Tag_t(XltTagID_t.TN_METINF_FREEMEM,     (byte)0x09,   "FreeMem"),
    new Tag_t(XltTagID_t.TN_METINF_LAST,          (byte)0x0A, "Last"),
    new Tag_t(XltTagID_t.TN_METINF_MARK,            (byte)0x0B,   "Mark"),
    new Tag_t(XltTagID_t.TN_METINF_MAXMSGSIZE,  (byte)0x0C,   "MaxMsgSize"),
    new Tag_t(XltTagID_t.TN_METINF_MEM,           (byte)0x0D, "Mem"),
    new Tag_t(XltTagID_t.TN_METINF_METINF,        (byte)0x0E, "MetInf"),
    new Tag_t(XltTagID_t.TN_METINF_NEXT,            (byte)0x0F,   "Next"),
    new Tag_t(XltTagID_t.TN_METINF_NEXTNONCE,     (byte)0x10, "NextNonce"),
    new Tag_t(XltTagID_t.TN_METINF_SHAREDMEM,     (byte)0x11, "SharedMem"),
    new Tag_t(XltTagID_t.TN_METINF_SIZE,            (byte)0x12,   "Size"),
    new Tag_t(XltTagID_t.TN_METINF_TYPE,            (byte)0x13,   "Type"),
    new Tag_t(XltTagID_t.TN_METINF_VERSION,     (byte)0x14,   "Version"),
    new Tag_t(XltTagID_t.TN_METINF_MAXOBJSIZE,  (byte)0x15,   "MaxObjSize"),
    new Tag_t(XltTagID_t.TN_UNDEF,                    (byte)0x00, null)
    };


  static Tag_t[] devinf = new Tag_t[]{
    new Tag_t(XltTagID_t.TN_DEVINF_CTCAP,       (byte)0x05,   "CTCap"),
    new Tag_t(XltTagID_t.TN_DEVINF_CTTYPE,      (byte)0x06,   "CTType"),
    new Tag_t(XltTagID_t.TN_DEVINF_DATASTORE,   (byte)0x07,   "DataStore"),
    new Tag_t(XltTagID_t.TN_DEVINF_DATATYPE,    (byte)0x08,   "DataType"),
    new Tag_t(XltTagID_t.TN_DEVINF_DEVID,       (byte)0x09,   "DevID"),
    new Tag_t(XltTagID_t.TN_DEVINF_DEVINF,      (byte)0x0A,   "DevInf"),
    new Tag_t(XltTagID_t.TN_DEVINF_DEVTYP,      (byte)0x0B,   "DevTyp"),
    new Tag_t(XltTagID_t.TN_DEVINF_DISPLAYNAME, (byte)0x0C,   "DisplayName"),
    new Tag_t(XltTagID_t.TN_DEVINF_DSMEM,       (byte)0x0D,   "DSMem"),
    new Tag_t(XltTagID_t.TN_DEVINF_EXT,         (byte)0x0E,   "Ext"),
    new Tag_t(XltTagID_t.TN_DEVINF_FWV,         (byte)0x0F,   "FwV"),
    new Tag_t(XltTagID_t.TN_DEVINF_HWV,         (byte)0x10,   "HwV"),
    new Tag_t(XltTagID_t.TN_DEVINF_MAN,         (byte)0x11,   "Man"),
    new Tag_t(XltTagID_t.TN_DEVINF_MAXGUIDSIZE, (byte)0x12,   "MaxGUIDSize"),
    new Tag_t(XltTagID_t.TN_DEVINF_MAXID,       (byte)0x13,   "MaxID"),
    new Tag_t(XltTagID_t.TN_DEVINF_MAXMEM,      (byte)0x14,   "MaxMem"),
    new Tag_t(XltTagID_t.TN_DEVINF_MOD,         (byte)0x15,   "Mod"),
    new Tag_t(XltTagID_t.TN_DEVINF_OEM,         (byte)0x16,   "OEM"),
    new Tag_t(XltTagID_t.TN_DEVINF_PARAMNAME,   (byte)0x17,   "ParamName"),
    new Tag_t(XltTagID_t.TN_DEVINF_PROPNAME,    (byte)0x18,   "PropName"),
    new Tag_t(XltTagID_t.TN_DEVINF_RX,          (byte)0x19,   "Rx"),
    new Tag_t(XltTagID_t.TN_DEVINF_RXPREF,      (byte)0x1A,   "Rx-Pref"),
    new Tag_t(XltTagID_t.TN_DEVINF_SHAREDMEM,   (byte)0x1B,   "SharedMem"),
    new Tag_t(XltTagID_t.TN_DEVINF_SIZE,        (byte)0x1C,   "Size"),
    new Tag_t(XltTagID_t.TN_DEVINF_SOURCEREF,   (byte)0x1D,   "SourceRef"),
    new Tag_t(XltTagID_t.TN_DEVINF_SWV,         (byte)0x1E,   "SwV"),
    new Tag_t(XltTagID_t.TN_DEVINF_SYNCCAP,     (byte)0x1F,   "SyncCap"),
    new Tag_t(XltTagID_t.TN_DEVINF_SYNCTYPE,    (byte)0x20,   "SyncType"),
    new Tag_t(XltTagID_t.TN_DEVINF_TX,          (byte)0x21,   "Tx"),
    new Tag_t(XltTagID_t.TN_DEVINF_TXPREF,      (byte)0x22,   "Tx-Pref"),
    new Tag_t(XltTagID_t.TN_DEVINF_VALENUM,     (byte)0x23,   "ValEnum"),
    new Tag_t(XltTagID_t.TN_DEVINF_VERCT,       (byte)0x24,   "VerCT"),
    new Tag_t(XltTagID_t.TN_DEVINF_VERDTD,      (byte)0x25,   "VerDTD"),
    new Tag_t(XltTagID_t.TN_DEVINF_XNAM,        (byte)0x26,   "XNam"),
    new Tag_t(XltTagID_t.TN_DEVINF_XVAL,        (byte)0x27,   "XVal"),
    new Tag_t(XltTagID_t.TN_DEVINF_UTC,         (byte)0x28,   "UTC"),
    new Tag_t(XltTagID_t.TN_DEVINF_NOFM,        (byte)0x29,   "SupportNumberOfChanges"),
    new Tag_t(XltTagID_t.TN_DEVINF_LARGEOBJECT, (byte)0x2A,   "SupportLargeObjs"),
    new Tag_t(XltTagID_t.TN_UNDEF,              (byte)0x00,    null)
  };
    
    // free table obtained with getDtdTable()
    public static void freeDtdTable(Dtd_t tbl)
    {
    }

    /**
     * FUNCTION: getDtdTable
     *
     * Returns a copy of the table containing all known (sub) dtd's
     * On error a null pointer is returned
     */
 // without WSM, the DTD table is a global read-only constant
    public static Dtd_t[] getDtdTable() {
      // NOWSM method, table is const, just return a pointer
    	return XltDtdTbl;
    }


    /**
     * FUNCTION: getExtName
     *
     * Returns the official name for a given extention/sub-DTD
     * and stored it in 'name'. If not found name isn't modified
     */
    // %%% luz:2003-04-24: added syncmlvers parameter
    // %%% luz:2003-07-31: changed to vers enum
    public static String getExtName(
        final SmlPcdataExtension_t ext, 
        final SmlVersion_t vers
    ) throws SmlException_t {
    	Dtd_t[] dtdhead = getDtdTable();
    	String dtdname;
    	if (dtdhead == null) {
            throw new SmlException_t(Ret_t.SML_ERR_UNDEF);
        }
    	for (int i = 0; dtdhead[i].ext != SmlPcdataExtension_t.SML_EXT_LAST; i++) {
    	    Dtd_t dtd = dtdhead[i];            
    		if (dtd.name == null) continue; /* skip empty names (should not appear but better be on the safe side) */
    		if (dtd.ext == ext) {
    		  // this is the default
    		  dtdname=dtd.name;
    		  // %%% luz:2003-04-24: added dynamic generation of namespace according to SyncML version
    		  if (ext==SmlPcdataExtension_t.SML_EXT_UNDEFINED && vers!=SmlVersion_t.SML_VERS_UNDEF) {
    		    // this requests SyncML namespace
    		    dtdname=SyncMLNamespaces[vers.getValue()];
    		  }
    			return dtdname;
    		}
    	}
    	throw new SmlException_t(Ret_t.SML_ERR_UNDEF);
    }

    /**
     * FUNCTION: getCpByName
     *
     * Returns the codepage constant assoziated with the name stored in 'ns'
     *
     * RETURN:             a SmlPcdataExtension_t representing the corresponding codepage id.
     *                     If no corresponding codepage is found -1 is returned.
     */
    public static SmlPcdataExtension_t getExtByName(
        final String ns
    ) {
    	Dtd_t[] dtdhead = getDtdTable();
        SmlPcdataExtension_t ext = SmlPcdataExtension_t.SML_EXT_NOT_FOUND;
        if (dtdhead == null)
            return SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        for (int i = 0; dtdhead[i].ext != SmlPcdataExtension_t.SML_EXT_LAST; i++) {
            Dtd_t dtd = dtdhead[i];            
            String dtdname = dtd.name;
            if (dtdname == null)
                continue; /*
                             * skip empty names (should not appear but better be
                             * on the safe side)
                             */
            if (dtd.ext == SmlPcdataExtension_t.SML_EXT_UNDEFINED && ns.startsWith("SYNCML:SYNCML")) {
                // SyncML namespace is ok without checking version!
                ext = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
                break;
            }
            else if (dtdname.equals(ns)) {
                ext = dtd.ext;
                break;
            }
        }
        return ext;
    }

    /**
     * FUNCTION: getTagTable
     *
     * Returns the tag table - this function is used to avoid a global
     * tag table variable
     *
     * RETURN:             a pointer to the tag table containing tag ids, 
     *                     codepages, wbxml tags and xml tags
     */
    /* T.K. initialized the structure via _TOKEN Macro, to take
     * out the XML name tags when not compiled with XML support.
     * In addtion removed the (unused) pointer for the build functions 
     */
    public static Tag_t[] getTagTable(
        final SmlPcdataExtension_t ext
    ) {
        Tag_t[] _tmpTag = null;
        if (
            (ext == null) || 
            (ext == SmlPcdataExtension_t.SML_EXT_UNDEFINED)
        ) {
            _tmpTag = syncml;
        }
        else if (ext == SmlPcdataExtension_t.SML_EXT_METINF) {
            _tmpTag = metinf;
        }
        else if (ext == SmlPcdataExtension_t.SML_EXT_DEVINF) {
            _tmpTag = devinf;
        }
        return _tmpTag;
    }

    /**
     * FUNCTION: getTagString
     *
     * Returns a tag string which belongs to a tag ID. 
     * This function is needed for the XML encoding
     *
     * PRE-Condition:   valid tag ID, the tagSring has to be allocated 
     *
     * POST-Condition:  tag string is returned
     *
     * IN:              tagId, the ID for the tag 
     *
     * IN/OUT:          tagString, allocated string into which the XML 
     *                             tag string will be written
     * 
     * RETURN:          0,if OK
     */
    public static String getTagString(
        final XltTagID_t tagID, 
        final SmlPcdataExtension_t ext
    ) throws SmlException_t {
        int i = 0;
        Tag_t[] pTags = getTagTable(ext);
        if (pTags == null) {
            throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
        }
        while ((pTags[i].id) != XltTagID_t.TN_UNDEF) {
            if (((pTags[i].id) == tagID)) {
                return pTags[i].xml;
            }
            i++;
        }
        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
    }

    /**
     * FUNCTION: getTagByte
     *
     * Returns a WBXML byte which belongs to a tag ID in a defined codepage. 
     * This function is needed for the WBXML encoding
     *
     * PRE-Condition:   valid tag ID, valid code page
     *
     * POST-Condition:  tag byte is returned
     *
     * IN:              tagId, the ID for the tag 
     *                  cp, code page group for the tag 
     *                  pTagByte, the byte representation of the tag
     * 
     * RETURN:          0, if OK
     */
    public static byte getTagByte(
        final XltTagID_t tagID, 
        final SmlPcdataExtension_t ext 
    ) throws SmlException_t {
        int i = 0;
        Tag_t[] pTags = getTagTable(ext);
        if (pTags == null) {
            throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
        }
        while ((pTags[i].id) != XltTagID_t.TN_UNDEF) {
            if ((pTags[i].id) == tagID) {
                return pTags[i].wbxml;
            }
            i++;
        }
        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
    }

    /**
     * FUNCTION: getCodePage
     * 
     * Returns the code page which belongs to a certain PCDATA extension type.
     * 
     * PRE-Condition: valid PCDATA extension type
     * 
     * POST-Condition: the code page is returned
     * 
     * IN: ext, the PCDATA extension type
     * 
     * RETURN: the code page
     */
    public static byte getCodePage(
        final SmlPcdataExtension_t ext
    ) {
      if (ext == SmlPcdataExtension_t.SML_EXT_METINF)
        return 1;
      if (ext == SmlPcdataExtension_t.SML_EXT_DEVINF)
        return 0;
        return 0;
    }

    /**
     * FUNCTION: getCodePageById
     *
     * Returns the codepage which belongs to a certain tag ID
     *
     * PRE-Condition:   valid tag ID
     *
     * POST-Condition:  the code page is returned
     *
     * IN:              tagID, the ID of the tag 
     *                  pCp, the codepage/extention of the tag
     *
     * RETURN:          0, if OK
     */
    public static SmlPcdataExtension_t getExtById(
        final XltTagID_t tagID 
    ) throws SmlException_t {
        int i = 0;
        /*
         * Iterate over all defined extensions to find the corresponding TAG.
         * Empty extensions, e.g. not defined numbers will be skipped.
         */
        for (SmlPcdataExtension_t ext : new SmlPcdataExtension_t[] { SmlPcdataExtension_t.SML_EXT_UNDEFINED,
                SmlPcdataExtension_t.SML_EXT_METINF, SmlPcdataExtension_t.SML_EXT_DEVINF }) {
            Tag_t[] pTags = getTagTable(ext);
            if (pTags == null) {
                continue; /* skip empty codepage */
            }
            i = 0;
            while (pTags[i].id != XltTagID_t.TN_UNDEF) {
                if (pTags[i].id == tagID) {
                    return ext;
                }
                i++;
            }
        }
        /* tag not found in any extension */
        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
    }

    /**
     * FUNCTION: getTagIDByStringAndCodepage
     *
     * Returns the tag ID which belongs to a tag string in a certain codepage
     *
     * PRE-Condition:   valid tag string, valid code page
     *
     * POST-Condition:  tag id is returned
     *
     * IN:              tag, the string representation of the tag 
     *                  cp, code page group for the tag 
     *                  pTagID, the tag id of the tag
     * 
     * RETURN:          0, if OK
     */
    
    public static XltTagID_t getTagIDByStringAndExt(
        final String tag, 
        final SmlPcdataExtension_t ext 
    ) throws SmlException_t {
        int i = 0; 
        Tag_t[] pTags = getTagTable(ext);
        if (pTags == null) {
          throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
        }
        for (i=0; pTags[i].id != XltTagID_t.TN_UNDEF; i++) {
            if (pTags[i].xml.equals(tag)) {
                return pTags[i].id;
            }         
        }        
        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
    }

    /**
     * FUNCTION: getTagIDByByteAndCodepage
     *
     * Returns the tag ID which belongs to a tag byte in a certain codepage
     *
     * PRE-Condition:   valid tag byte, valid code page
     *
     * POST-Condition:  tag id is returned
     *
     * IN:              tag, the byte representation of the tag 
     *                  cp, code page group for the tag  
     *                  pTagID, the tag id of the tag
     * 
     * RETURN:          0, if OK
     */
    public static XltTagID_t getTagIDByByteAndExt(
        final byte tag, 
        final SmlPcdataExtension_t ext 
    ) throws SmlException_t {
    
        int i = 0; 
        Tag_t[] pTags = getTagTable(ext);
        if (pTags == null)
        {
          throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
        }
        while (((pTags[i]).id) != XltTagID_t.TN_UNDEF)
        {
          if ((pTags[i].wbxml) == tag)
          {
            return pTags[i].id;
          }    
          i++;
        }        
        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
    }

    /**
     * FUNCTION: getTagIDByStringAndNamespace
     *
     * Returns the tag ID which belongs to a tag string in a certain namespace
     *
     * PRE-Condition:   valid tag string, valid namespace
     *
     * POST-Condition:  tag id is returned
     *
     * IN:              tag, the string representation of the tag 
     *                  ns, namespace group for the tag  
     *                  pTagID, the tag id of the tag
     * 
     * RETURN:          0, if OK
     */
    public static XltTagID_t getTagIDByStringAndNamespace(
        final String tag, 
        final String ns 
    ) throws SmlException_t {
        int i = 0; 
        Tag_t[] pTags = getTagTable(getExtByName(ns));
        if (pTags == null)
        {
          throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
        }
        while (pTags[i].id != XltTagID_t.TN_UNDEF)
        {
          if (pTags[i].xml.equals(tag))
          {
            return pTags[i].id;
          }    
          i++;
        }        
        throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);
    }

}

