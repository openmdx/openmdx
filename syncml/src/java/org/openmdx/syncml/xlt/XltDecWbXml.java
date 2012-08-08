//*************************************************************************/
//* module:         WBXML decoder                                         */
//* file:           XLTDecWbxml.c                                         */
//* target system:  all                                                   */
//* target OS:      all                                                   */
//*************************************************************************/

package org.openmdx.syncml.xlt;

import java.util.EmptyStackException;
import java.util.Stack;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlLib;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataType_t;
import org.openmdx.syncml.SmlPcdata_t;

/**
 * Private Interface for the WBXML scanner.
 *
 * The private scanner interface contains some additional member attributes
 * that are not listed in the public interface, e.g. a copy of the string
 * table and some other items that do not need to be known outside the
 * scanner module.
 */

public class XltDecWbXml {
    
    private static final int TAG_STATE = 0;
    private static final int _MAJOR_VERSION = 1;
    private static final int _MINOR_VERSION = 2;
    private static final int ATTRIBUTE_STATE = 1;
    
    public static boolean IS_LITERAL(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == 0x04;        
    }
    
    public static boolean HAS_CONTENT(
        byte[] buffer,
        int pos
    ) {
        byte tag = buffer[pos];
        return (tag & 0x40) != 0;    
    }
    
    public static byte IDENTITY(
        byte[] buffer,
        int pos
    ) {
        byte tag = buffer[pos];
        return (byte)(tag & 0x3F);    
    }
    
    public static boolean HAS_ATTRIBUTES(
        byte[] buffer,
        int pos
    ) {
        byte tag = buffer[pos];
        return (tag & 0x80) != 0;    
    }
    
    public static boolean IS_SWITCH(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0x00;        
    }
    
    public static boolean IS_END(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0x01;    
    }
    
    public static boolean IS_PI(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0x43;
    }

    public static boolean IS_EXT(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return ((tok == (byte)0xC0) || (tok == (byte)0xC1) || (tok == (byte)0xC2));
    }
    
    public static boolean IS_EXT_I(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return (tok == (byte)0x40) || (tok == (byte)0x41) || (tok == (byte)0x42);
    }
    
    public static boolean IS_EXT_T(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return ((tok == (byte)0x80) || (tok == (byte)0x81) || (tok == (byte)0x82));
    }
    
    public static boolean IS_EXTENSION(
        byte[] buffer,
        int pos
    ) {
        return (IS_EXT_I(buffer, pos) || IS_EXT_T(buffer, pos) || IS_EXT(buffer, pos));
    }
      
    public static boolean IS_STR_I(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0x03;
    }
    
    public static boolean IS_STR_T(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0x83;
    }
    
    public static boolean IS_STRING(
        byte[] buffer,
        int pos
    ) {
        return (IS_STR_I(buffer, pos) || IS_STR_T(buffer, pos));
    }
   
    public static boolean IS_ENTITY(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0x02;    
    }
    
    public static boolean IS_OPAQUE(
        byte[] buffer,
        int pos
    ) {
        byte tok = buffer[pos];
        return tok == (byte)0xC3;    
    }
    
    /**
     * FUNCTION: XltDecWbxmlInit
     *
     * Create and initialize a new WBXML scanner. Description see XLTDec.h.
     */
    public static XltDecScanner_t xltDecWbxmlInit(
        final byte[] buffer 
    ) throws SmlException_t {
        WbXmlScanner_t pScanner;
        
        /* initialize new WBXML scanner */
        pScanner = new WbXmlScanner_t();
        pScanner.buffer = buffer;
        pScanner.pos = 0;
        pScanner.curtok = new XltDecToken_t();
        pScanner.curtok.pcdata = null;
        pScanner.tagstack = new java.util.Stack();
        pScanner.state = TAG_STATE;

        /* decode WBXML header */
        wbxmlHeader(pScanner);
        return pScanner;
    }

/*************************************************************************/
/* Internal Functions                                                    */
/*************************************************************************/

    /**
     * FUNCTION: readBytes
     *
     * Advance the position pointer. Description see above.
     */
    static boolean readBytes(
        final WbXmlScanner_t pScanner, 
        final int bytes
    ) {
        if (pScanner.pos + bytes > pScanner.buffer.length) {
            pScanner.finished = 0;
            return false;
        }
        pScanner.pos += bytes;
        return true;
    }

    /**
     * NOTICE: Entities, Extensions, Processing Instructions and Attributes
     * are not supported by the WBXML scanner.
     *
     * Extensions and Attributes are document-specific and are as such not used 
     * by the SyncML specification.
     * The scanner will just ignore and skip over them. Neither
     * this scanner nor the parser use processing instructions so they are
     * skipped as well.
     */
    
    /**
     * FUNCTION: wbxmlHeader
     *
     * Decode the WBXML header containing version number, document public
     * identifier, character set and a string table.
     */
    static void wbxmlHeader(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        /* decode the WBXML header */
        wbxmlVersion(pScanner);
        wbxmlPublicID(pScanner);
        wbxmlCharset(pScanner);
        wbxmlStrtbl(pScanner);
    }

    /**
     * FUNCTION: wbxmlVersion
     *
     * Decode WBXML version. The scanner returns an error if the major version
     * of the document differs from the major version this scanner supports or
     * if the minor version of the document is larger than the minor version
     * the scanner supports.
     */
    static void wbxmlVersion(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        byte major, minor;
    
        minor = (byte)(pScanner.pos & 0x0F);
        major = (byte)((pScanner.pos >> 4) + 1);
           
        if (major != _MAJOR_VERSION || minor > _MINOR_VERSION)
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INCOMP_WBXML_VERS);
    
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
    
    }

    /**
     * FUNCTION: wbxmlPublicID
     *
     * Decodes WBXML Document Public Identifier.
     */
    static void wbxmlPublicID(
        WbXmlScanner_t pScanner
    ) throws SmlException_t {
        int tmp;    
        if (pScanner.buffer[pScanner.pos] != 0) {
            /* pre-defined numeric identifier */
            tmp = parseInt(pScanner);
            pScanner.pubID = tmp;
            pScanner.pubIDIdx = 0;
        } 
        else {
            /* public id is given as string table entry (which we
               haven't read at this point so we'll save the reference
               for later) */
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            tmp = parseInt(pScanner);
            pScanner.pubID = 0;
            pScanner.pubIDIdx = tmp;
        }
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
    }

    /**
     * FUNCTION: wbxmlCharset
     *
     * Decode WBXML Charset.
     */
    static void wbxmlCharset(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        /* TODO: if charset iformation has to be processed
           it can be done here. For the moment only UTF-8 is used by SyncML */
        int mibenum;
    
        /* charset is given as a single IANA assigned MIBEnum value */
        mibenum = parseInt(pScanner);
        pScanner.charset = mibenum;
    
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
    
    }

    /**
     * FUNCTION: wbxmlStrtbl
     *
     * Keep a copy of the string table.
     */
    static void wbxmlStrtbl(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        int len;
    
        len = parseInt(pScanner);
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        pScanner.strtbllen = len;
        if (len > 0) {
            if (pScanner.pos + len > pScanner.buffer.length)
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            pScanner.strtbl = new String(
                pScanner.buffer,
                pScanner.pos, 
                len
            );
            readBytes(pScanner, len);
        } else {
            pScanner.strtbl = null;
        }
    
        /* if the public ID was given as a string table reference save a
           reference to the corresponding string for later */
        if (pScanner.pubID == 0) {
            if (pScanner.pubIDIdx > pScanner.strtbllen)
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_WBXML_DOC);
            pScanner.pubIDStr = pScanner.strtbl.substring(
                pScanner.pubIDIdx
            );
        }    
    }

    static int parseInt(
        final WbXmlScanner_t pScanner 
    ) throws SmlException_t {
        int mbi = 0;
        /* accumulate byte value until continuation flag (MSB) is zero */
        for (;;)  {
            mbi = mbi << 7;
            byte b = pScanner.buffer[pScanner.pos];
            mbi += b & 0x7F;
            if ((b & 0x80) == 0) break;
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        }
        return mbi;
    }

    static SmlPcdata_t wbxmlStringToken(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        SmlPcdata_t pPcdata;
    
        pPcdata = new SmlPcdata_t();
        /* copy the string into the new PCdata struct */
        if (IS_STR_I(pScanner.buffer, pScanner.pos)) {
            /* inline string */
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            pPcdata.extension = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
            pPcdata.contentType = SmlPcdataType_t.SML_PCDATA_STRING;
            int len = SmlLib.smlLibStrlen(pScanner.buffer, pScanner.pos);
            if (pScanner.pos + len > pScanner.buffer.length) {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            }
            pPcdata.content = new byte[len];
            System.arraycopy(
                pScanner.buffer, 
                pScanner.pos, 
                pPcdata.content, 
                0, 
                len
            );
            readBytes(pScanner, len + 1);
        }
        else {
            /* string table reference */
            int offset; /* offset into string table */
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            offset = parseInt(pScanner);
            if (offset >= pScanner.strtbllen) {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_WBXML_DOC);
            }
            int len = SmlLib.smlLibStrlen(pScanner.strtbl, offset);
            pPcdata.contentType = SmlPcdataType_t.SML_PCDATA_STRING;
            pPcdata.content = new byte[len];
            System.arraycopy(
                pScanner.strtbl, 
                offset, 
                pPcdata.content, 
                0, 
                len
            );            
            readBytes(pScanner, 1);
        }
    
        pScanner.curtok.pcdata = pPcdata;    
        pScanner.curtok.type = XltTokType_t.TOK_CONT;
    
        return pPcdata;
    }

    static SmlPcdata_t wbxmlOpaqueToken(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        SmlPcdata_t pPcdata = null;
        int len;    
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        len = parseInt(pScanner);
        if (pScanner.pos + len > pScanner.buffer.length)
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        pPcdata = new SmlPcdata_t();
        pPcdata.extension = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        pPcdata.contentType = SmlPcdataType_t.SML_PCDATA_OPAQUE;
        pPcdata.content = new byte[len];
        System.arraycopy(
            pScanner.buffer, 
            pScanner.pos, 
            pPcdata.content, 
            0, 
            len
        );
        pScanner.curtok.pcdata = pPcdata;
        readBytes(pScanner, len + 1);
        pScanner.curtok.type = XltTokType_t.TOK_CONT;
        return pPcdata;
    }

    static XltTagID_t wbxmlTagToken(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        XltTagID_t tagid;
        boolean has_cont, has_attr;
    
        if (IS_SWITCH(pScanner.buffer, pScanner.pos)) {
            wbxmlSwitchPage(pScanner);
        }
    
        /* we have to look at the top of the tagstack to see which
           start tag an end tag belongs to */
        if (IS_END(pScanner.buffer, pScanner.pos)) {
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            pScanner.curtok.type = XltTokType_t.TOK_TAG_END;
            try {
                tagid = (XltTagID_t)pScanner.tagstack.pop();
            }
            catch(EmptyStackException e) {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_WBXML_DOC);
            }
            pScanner.curtok.tagid = tagid;
            return tagid;
        } 
        
        /* look at the two MSB: does this tag have content or attributes? */        
        has_cont = HAS_CONTENT(pScanner.buffer, pScanner.pos);
        has_attr = HAS_ATTRIBUTES(pScanner.buffer, pScanner.pos);    
    
        /* look up tag ID either by string or by number */
        if (IS_LITERAL(pScanner.buffer, pScanner.pos)) {
            int offset; /* offset into the string table */
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            offset = parseInt(pScanner);
            if (offset > pScanner.strtbllen)
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_WBXML_DOC);
            tagid = XltTags.getTagIDByStringAndExt(
                pScanner.strtbl.substring(
                    offset, 
                    offset + SmlLib.smlLibStrlen(pScanner.strtbl, offset)
                ),
                pScanner.activeExt);
            if ((tagid == XltTagID_t.TN_UNDEF)) {
                return tagid;
            }
        }
        else {
            try {
                tagid = XltTags.getTagIDByByteAndExt(
                    (byte) IDENTITY(pScanner.buffer, pScanner.pos), 
                    pScanner.activeExt
                );
            }
            catch(SmlException_t e) {
                tagid = XltTags.getTagIDByByteAndExt(
                    (byte) IDENTITY(pScanner.buffer, pScanner.pos), 
                    SmlPcdataExtension_t.SML_EXT_UNDEFINED
                );
                pScanner.activeExt = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
            }
            if ((tagid == XltTagID_t.TN_UNDEF)) {
                return tagid;
            }
        }
    
        /* we know everything we need to know */
        pScanner.curtok.tagid = tagid;
        pScanner.curtok.type = has_cont ? XltTokType_t.TOK_TAG_START : XltTokType_t.TOK_TAG_EMPTY;
        pScanner.curtok.ext = (pScanner.cptag == null) || (pScanner.cptag == SmlPcdataExtension_t.SML_EXT_UNDEFINED) 
            ? SmlPcdataExtension_t.SML_EXT_UNDEFINED 
            : SmlPcdataExtension_t.SML_EXT_METINF;
    
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);

        /* push tag onto tagstack unless this tag is empty */
        if (has_cont) {
            pScanner.tagstack.push(tagid);
        }

        /* skip attributes */
        if (has_attr) {
            pScanner.state = ATTRIBUTE_STATE;
            wbxmlSkipAttribute(pScanner);
            pScanner.state = TAG_STATE;
        }
    
        return tagid;
    }

    /**
     * FUNCTION: wbxmlSwitchPage
     *
     * Switch WBXML code page.
     */
    /* T.K. 06.02.01
     * We need to enhance this as soon as we introduce 
     * Sub DTD's with more than one WBXML codepage. But till then
     * there is only one case where WBXML codepages can occure, and 
     * this is the MetInf Sub DTD. So in case we find a codepage switch
     * to something other than codepage zero, we set the active extension 
     * to metinf.
     * In future versions the pScanner needs to be enhanced, to translate
     * codepageswitches context sensitive to the active extension.
     */
    static void wbxmlSwitchPage(
        WbXmlScanner_t pScanner
    ) throws SmlException_t {
        if (!readBytes(pScanner, 1))
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        if (pScanner.state == TAG_STATE)
            pScanner.cptag = SmlPcdataExtension_t.getEnum(pScanner.buffer[pScanner.pos]);
        else
            pScanner.cpattr = (byte) pScanner.buffer[pScanner.pos];
        readBytes(pScanner, 1);
        /* T.K. this needs to be adjusted as described above */
        if (pScanner.cptag == SmlPcdataExtension_t.SML_EXT_METINF) {
            pScanner.activeExt = SmlPcdataExtension_t.SML_EXT_METINF;
        }
        else if(pScanner.cptag == SmlPcdataExtension_t.SML_EXT_DEVINF) {
            pScanner.activeExt = SmlPcdataExtension_t.SML_EXT_DEVINF;            
        }
        else {
            pScanner.activeExt = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        }
    }


    /******************************/
    /* Unsupported WBXML elements */
    /******************************/
    
    /**
     * FUNCTION: wbxmlSkipEntity
     *
     * Skips entities but doesn't do anything useful yet.
     */
    static Ret_t wbxmlSkipEntity(
        WbXmlScanner_t pScanner
    ) throws SmlException_t {
        if (!readBytes(pScanner, 1))
            return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
        parseInt(pScanner);
        if (!readBytes(pScanner, 1))
            return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
        return Ret_t.SML_ERR_OK;
    }

    /**
     * FUNCTION: wbxmlSkipExtension
     *
     * Decode WBXML extensions. Skips the extension but doesn't do anything
     * useful with it.
     */
    static Ret_t wbxmlSkipExtension(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        int tmp;
    
        if (IS_EXT(pScanner.buffer, pScanner.pos)) {
            /* single byte extension token */
            if (!readBytes(pScanner, 1))
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
        }
        else if (IS_EXT_I(pScanner.buffer, pScanner.pos)) {
            /* inline string extension token */
            if (!readBytes(pScanner, 1))
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
            if (!readBytes(pScanner, SmlLib.smlLibStrlen(pScanner.buffer, pScanner.pos) + 1))
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
        }
        else {
            /* inline integer extension token */
            if (!readBytes(pScanner, 1))
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
            tmp = parseInt(pScanner);
            if (!readBytes(pScanner, tmp + 1))
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
        }
        return Ret_t.SML_ERR_OK;
    }

    /**
     * FUNCTION: wbxmlSkipPI
     *
     * Handle XML processing instructions. PIs are not supported but the
     * scanner recognizes and skips over them.
     */
    public static void wbxmlSkipPI(
        final WbXmlScanner_t pScanner
    ) throws SmlException_t {
        /* PIs are just like tag attributes with a special PI token instead
         * of the attribute start token */
        wbxmlSkipAttribute(pScanner);
    }

    /**
     * FUNCTION: wbxmlSkipAttribute
     *
     * Handle attributes. Attributes are not supported but the
     * scanner recognizes and skips over them.
     */
    public static XltDecToken_t wbxmlSkipAttribute(
        WbXmlScanner_t pScanner
    ) throws SmlException_t {
        XltDecToken_t oldtok;
    
        /* skipping attributes shouldn't change the current token so we
           make a copy... */
        oldtok = new XltDecToken_t(pScanner.curtok);
    
        /* ... skip until attribute end tag... */
        while (!IS_END(pScanner.buffer, pScanner.pos)) {
            if (IS_STRING(pScanner.buffer, pScanner.pos)) {
                wbxmlStringToken(pScanner);
                /*
                 * avoid memory leak due to this ugly workaround of skipping
                 * attributes
                 */
            }
            else if (IS_EXTENSION(pScanner.buffer, pScanner.pos)) {
                wbxmlSkipExtension(pScanner);
            }
            else if (IS_ENTITY(pScanner.buffer, pScanner.pos)) {
                wbxmlSkipEntity(pScanner);
            }
            else if (IS_OPAQUE(pScanner.buffer, pScanner.pos)) {
                wbxmlOpaqueToken(pScanner);
                /*
                 * avoid memory leak due to this ugly workaround of skipping
                 * attributes
                 */
            }
            else if (IS_LITERAL(pScanner.buffer, pScanner.pos)) {
                if (!readBytes(pScanner, 1))
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
                parseInt(pScanner);
                if (!readBytes(pScanner, 1))
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            }
            else if (IS_SWITCH(pScanner.buffer, pScanner.pos)) {
                wbxmlSwitchPage(pScanner);
            }
            else {
                if (!readBytes(pScanner, 1))
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            }
        }
        /* ... then skip the end tag itself... */
        readBytes(pScanner, 1);
    
        /* ... and finaly restore our copy of curtok */
        oldtok = new XltDecToken_t(pScanner.curtok);
    
        return oldtok;
    }

    /*
     * This function tries to decode an inlined WBXML document inside
     * an PCDATA element.
     * In case of failing to decode it the PCDATA element isn't changed
     * at all.
     */
     
    public static SmlPcdata_t subdtdDecodeWbxml(
        final XltDecoder_t pDecoder,
        final SmlPcdata_t ppPcdata
    ) throws SmlException_t {
        
        Ret_t _err = Ret_t.SML_ERR_OK;
        byte[] pSubBuf = null;
        SmlPcdata_t pSubPcdata = null;
        XltDecoder_t pSubDecoder = null;
        WbXmlScanner_t pScannerPriv = null;
    
        /* some sanity checks at first */           
        if (ppPcdata == null) {
     		if (pDecoder != null) /* use this rare case to remove warning */
     		{
     		}
     		return ppPcdata; 
     	}
    
        if ((ppPcdata).contentType != SmlPcdataType_t.SML_PCDATA_OPAQUE) return ppPcdata;
    
        // now create a sub buffer
        int len = ((byte[])ppPcdata.content).length;
        pSubBuf = new byte[len];
        System.arraycopy(
            ppPcdata.content, 
            0, 
            pSubBuf, 
            0, 
            len
        );
        
        /* ok looks fine sofar - now lets decode the rest */
        /* now lets create a decoder, but without parsing the SyncML
         * start tags (because it's not there) and skip the XML
         * part as we don't need it.
         */
        pSubDecoder = new XltDecoder_t();
        pSubDecoder.isFinished = false;
        pSubDecoder.isFinal = false;
        pSubDecoder.tagstack = new Stack();
        pSubDecoder.scanner = xltDecWbxmlInit((byte[])ppPcdata.content);
        pSubDecoder.charset = pSubDecoder.scanner.charset;
        pSubDecoder.charsetStr = null;
    
        pSubPcdata = new SmlPcdata_t();
        if (pSubPcdata == null) {
            XltDec.xltDecTerminate(pSubDecoder);
            return ppPcdata;
        }
        /* T.K.
         * In the future we need to check the WBXML stringtable and
         * switch into the right Sub DTD. But sofar only DevInf is
         * supported so we can save time and space
         */
        /* T.K.
         * To prevent buffer corruption when __USE_DEVINF__ is not used
         * we initialize _err with any errorcode != OK, and this way
         * force the function to exit without modifying the ppPcdata
         */
        _err = Ret_t.SML_ERR_UNSPECIFIC;
        pSubPcdata.contentType = SmlPcdataType_t.SML_PCDATA_EXTENSION;
        pSubPcdata.extension = SmlPcdataExtension_t.SML_EXT_DEVINF;
        pSubPcdata.content = null;
    
        pScannerPriv = (WbXmlScanner_t) pSubDecoder.scanner;
        pScannerPriv.activeExt = SmlPcdataExtension_t.SML_EXT_DEVINF;
        pScannerPriv.cpattr = 0;
        pScannerPriv.cptag = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        pScannerPriv.curtok = null; 
    
        pSubPcdata.content = XltDevInf.buildDevInfDevInfCmd(pSubDecoder).toByteArray();
    
        if (_err != Ret_t.SML_ERR_OK) {
            XltDec.xltDecTerminate(pSubDecoder);
            return ppPcdata;
        }
        
        /* we are done */
        XltDec.xltDecTerminate(pSubDecoder);
    
        return pSubPcdata;
    }
    
}
