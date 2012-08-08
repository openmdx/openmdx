//*************************************************************************/
//* module:         XML scanner                                           */
//* file:           XLTDecXml.c                                           */
//* target system:  all                                                   */
//* target OS:      all                                                   */
//*************************************************************************/
/**
 * Private Interface for the XML scanner.
 */

package org.openmdx.syncml.xlt;

import java.io.UnsupportedEncodingException;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlLib;
import org.openmdx.syncml.SmlPcdataExtension_t;
import org.openmdx.syncml.SmlPcdataType_t;
import org.openmdx.syncml.SmlPcdata_t;

public class XltDecXml {

    public static XltDecScanner_t xltDecXmlInit(
        final byte[] buffer
    ) throws SmlException_t {
        XmlScanner_t pScanner;

        pScanner = new XmlScanner_t();

        pScanner.finished = 0;
        pScanner.pos = 0;
        try {
            pScanner.buffer = new String(buffer, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            pScanner.buffer = new String(buffer);
        }
        pScanner.curtok = new XltDecToken_t();

        pScanner.curtok.pcdata = null;
        pScanner.curtok.tagid = XltTagID_t.TN_UNDEF;
        pScanner.pubID = 0;
        pScanner.pubIDStr = null;
        pScanner.charset = 0;
        pScanner.charsetStr = null;
        pScanner.ext = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        pScanner.prev_ext = SmlPcdataExtension_t.SML_EXT_NOT_FOUND;
        pScanner.ext_tag = XltTagID_t.TN_UNDEF;
        pScanner.prev_ext_tag = XltTagID_t.TN_UNDEF;
        pScanner.nsprelen = 0;
        pScanner.nsprefix = null;

        xmlProlog(pScanner);
        return pScanner;
    }

    /**
     * FUNCTION: readBytes
     * 
     * Advance the position pointer. Description see above.
     */
    public static boolean readBytes(
        final XmlScanner_t pScanner, 
        final int bytes
    ) {
        if (pScanner.pos + bytes > pScanner.buffer.length()) {
            pScanner.finished = 1;
            return false;
        }
        pScanner.pos += bytes;
        return true;
    }

    /**
     * FUNCTION: skipS
     * 
     * Skip whitespace.
     */
    public static void skipS(
        final XmlScanner_t pScanner
    ) {
        for (;;) {
            switch (pScanner.buffer.charAt(pScanner.pos)) {
                case 9: /* tab stop */
                case 10: /* line feed */
                case 13: /* carriage return */
                case 32: /* space */
                    // %%% luz: 2001-07-03: added exit from loop if no more bytes
                    if (!readBytes(pScanner, 1))
                        return;
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * FUNCTION: xmlProlog
     * 
     * Scan the XML prolog (might be empty...).
     */
    public static Ret_t xmlProlog(
        XmlScanner_t pScanner
    ) throws SmlException_t {
        Ret_t rc;

        if (pScanner.pos + 5 > pScanner.buffer.length())
            return Ret_t.SML_ERR_OK;
        if (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "<?xml", 5) == 0)
            xmlXMLDecl(pScanner);

        skipS(pScanner);

        while (
            (pScanner.pos + 4 <= pScanner.buffer.length()) && 
            ((SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "<!--", 4) == 0) || (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "<?", 2) == 0))
        ) {
            if (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "<!--", 4) == 0)
                rc = xmlSkipComment(pScanner);
            else
                rc = xmlSkipPI(pScanner);
            if (rc != Ret_t.SML_ERR_OK)
                return rc;
            skipS(pScanner);
        }

        if ((pScanner.pos + 9 <= pScanner.buffer.length()) && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "<!DOCTYPE", 9) == 0))
            xmlDocTypeDecl(pScanner);

        skipS(pScanner);

        return Ret_t.SML_ERR_OK;
    }

    /**
     * FUNCTION: xmlDocTypeDecl
     * 
     * Part of the Prolog scanning
     */
    public static void xmlDocTypeDecl(
        final XmlScanner_t pScanner
    ) throws SmlException_t {

        readBytes(pScanner, 9);
        skipS(pScanner);
        xmlName(pScanner);
        skipS(pScanner);

        /* parse ExternalID */
        if ((pScanner.pos + 6 <= pScanner.buffer.length()) && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "SYSTEM", 6) == 0)) {
            readBytes(pScanner, 6);
            skipS(pScanner);
            xmlStringConst(pScanner);
        }
        else if ((pScanner.pos + 6 <= pScanner.buffer.length()) && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "PUBLIC", 6) == 0)) {
            readBytes(pScanner, 6);
            skipS(pScanner);
            xmlStringConst(pScanner);
            skipS(pScanner);
            xmlStringConst(pScanner);
        }

        skipS(pScanner);

        if (pScanner.pos != '>')
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
        readBytes(pScanner, 1);
    }

    /**
     * FUNCTION: xmlXMLDecl
     * 
     * Part of the Prolog scanning
     */
    public static void xmlXMLDecl(
        XmlScanner_t pScanner
    ) throws SmlException_t {
        
        String name;

        readBytes(pScanner, 5);
        skipS(pScanner);

        /* mandatory version info */
        String[] nv = xmlAttribute(pScanner);
        name = nv[0];
        if (!name.equals("version")) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
        }
        skipS(pScanner);

        /* optional attributes are encoding and standalone */
        while ((pScanner.pos + 2 <= pScanner.buffer.length()) && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "?>", 2) != 0)) {
            nv = xmlAttribute(pScanner);
            name = nv[0];
            skipS(pScanner);
        }
        if (pScanner.pos + 2 > pScanner.buffer.length())
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        readBytes(pScanner, 2);
    }

    /**
     * FUNCTION: xmlAttribute
     * 
     * Handle Attributes //function can be used if attributes get necessary
     */
    public static String[] xmlAttribute(
        final XmlScanner_t pScanner
    ) throws SmlException_t {

        String[] result = new String[2];
        skipS(pScanner);

        result[0] = xmlName(pScanner);

        skipS(pScanner);

        /* no attributes found, because this tag has none . bail out */
        if (pScanner.buffer.charAt(pScanner.pos) == '>') {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
        }
        if (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "/>", 2) == 0) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_MISSING_CONT);
        }

        if (pScanner.buffer.charAt(pScanner.pos) != '=') {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
        }
        readBytes(pScanner, 1);

        skipS(pScanner);

        result[1] = xmlStringConst(pScanner);

        return result;
    }

    /**
     * FUNCTION: xmlStringConst
     * 
     * Handle Pcdata String Constants
     */
    public static String xmlStringConst(
        final XmlScanner_t pScanner
    ) throws SmlException_t {
        int end;
        int len;
        String value;

        String del = Character.toString(pScanner.buffer.charAt(pScanner.pos));
        if (!"\"".equals(del) && !"'".equals(del)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
        }
        readBytes(pScanner, 1);

        if ((end = SmlLib.smlLibStrchr(pScanner.buffer, pScanner.pos, del)) == -1) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        }
        len = end - pScanner.pos;
        value = pScanner.buffer.substring(pScanner.pos, pScanner.pos+len);
        readBytes(pScanner, len + 1);

        return value;
    }

    /**
     * FUNCTION: xmlCharData
     * 
     * Handle Pcdata character data content
     */
    public static Ret_t xmlCharData(final XmlScanner_t pScanner) {
        SmlPcdata_t pPCData;
        int begin;

        pPCData = new SmlPcdata_t();
        pPCData.contentType = SmlPcdataType_t.SML_PCDATA_UNDEFINED;
        pPCData.content = new byte[0];

        begin = pScanner.pos;

        if (pScanner.pos >= pScanner.buffer.length()) {
            pPCData.content = new byte[0];
            pPCData.contentType = SmlPcdataType_t.SML_PCDATA_UNDEFINED;
            pPCData.extension = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
            pScanner.curtok.type = XltTokType_t.TOK_CONT;
            pScanner.curtok.pcdata = pPCData;
            // smlLibFree(pPCData);
            return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
        }

        while (pScanner.pos != '<') /* && (*pScanner.pos != '&') */
        {
            if (pScanner.pos >= pScanner.buffer.length()) {
                return Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC;
            }
            if (!readBytes(pScanner, 1)) {
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;
            }
        }
        pPCData.content = pScanner.buffer.substring(begin, begin).getBytes();
        pPCData.contentType = SmlPcdataType_t.SML_PCDATA_STRING;
        pScanner.curtok.type = XltTokType_t.TOK_CONT;
        pScanner.curtok.pcdata = pPCData;

        return Ret_t.SML_ERR_OK;
    }

    /**
     * FUNCTION: xmlName
     * 
     * Handle Name Elements
     */
    public static String xmlName(
        final XmlScanner_t pScanner
    ) throws SmlException_t {
        int begin;
        int len;

        begin = pScanner.pos;
        char c = pScanner.buffer.charAt(pScanner.pos);
        while (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
                || ((c >= '0') && (c <= '9')) || (c == '.') || (c == '-')
                || (c == '_') || (c == ':')
        ) {
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
            c = pScanner.buffer.charAt(pScanner.pos);
        }
        
        len = pScanner.pos - begin;
        /* T.K. bail out if len is zero without modifying name */
        return pScanner.buffer.substring(begin, begin+len);
    }

    /**
     * FUNCTION: xmlTag
     * 
     * Handle XML Tags
     */
    public static void xmlTag(
        final XmlScanner_t pScanner, 
        final boolean endtag
    ) throws SmlException_t {
        String name, attname = null, value = null, nsprefix = null;
        int nsprelen = 0;
        XltTagID_t tagid;
        SmlPcdataExtension_t ext;

        if (endtag) {
            if (!readBytes(pScanner, 2))
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        }
        else {
            if (!readBytes(pScanner, 1))
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
        }

        name = XltDecXml.xmlName(pScanner);

        ext = pScanner.ext;
        if (!endtag) {
            /*
             * Namespaces can only be defined on start, never on endtags but we
             * have to make sure we close a namespace on the corrsponding
             * endtag. Thats why we a) only open a namespace when it differs
             * from the previous one, and b) record the tag_id that opend the
             * namespace so we can close it when the corresponding endtag is
             * reached.
             */

            try {
                String[] nv = xmlAttribute(pScanner);
                attname = nv[0];
                value = nv[1];
                if (attname.startsWith("xmlns")) {
                    /* Heureka we found a Namespace :-) */
                    if ((attname.length() >= 6) && (attname.charAt(5) == ':')) { // we found a namespace prefixdefinition
                        nsprelen = SmlLib.smlLibStrlen(attname, 6);
                        nsprefix = attname.substring(6);
                    }
                    ext = XltTags.getExtByName(value);
                    if (ext == SmlPcdataExtension_t.SML_EXT_NOT_FOUND) {
                         throw new SmlException_t(Ret_t.SML_ERR_XLT_INVALID_CODEPAGE);
                    }
                }
                else {
                    /* we found an unknown attribute . bail out */
                    /* nsprefix is empty here so we save us a function call */
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
                }
            }
            catch(SmlException_t e) {
                if (e.getErrorCode() != Ret_t.SML_ERR_XLT_MISSING_CONT) {
                    /*
                     * xmlAttribute returns an SML_ERR_XLT_MISSING_CONT error when
                     * no attribute was found. This is not an error, but everything
                     * else is.
                     */
                }
            }
        }

        if (pScanner.ext == ext) {
            /* no new Namespace found - lets proceed with the active one */

            /*
             * first lets check wether a tag is in the right namespace, in case
             * we are using namespaces with prefix notation ('mi:Format' instead
             * of 'Format nsattr="..."). If we are and the token is not in this
             * namespace . bail out
             */
            if (pScanner.nsprelen > 0 && name.length() > pScanner.nsprelen + 1) {
                if (name.charAt(pScanner.nsprelen) != ':' || !name.equals(pScanner.nsprefix)) {
                     throw new SmlException_t(Ret_t.SML_ERR_XLT_NO_MATCHING_CODEPAGE);
                }
            }
            /*
             * Strip off namespace prefixes and ':' to find the tag. If no
             * prefix is defined (pScanner.nsprelen == 0) take the whole
             * tagname.
             */
            if (pScanner.nsprelen > 0)
                tagid = XltTags.getTagIDByStringAndExt(name.substring(0 + pScanner.nsprelen + 1), pScanner.ext);
            else
                tagid = XltTags.getTagIDByStringAndExt(name, pScanner.ext);
        }
        else {
            /* we have a new Namespace */
            if (nsprelen > 0 && name.length() > nsprelen + 1) {
                if (name.charAt(nsprelen) != ':' || !name.equals(nsprefix)) {
                     throw new SmlException_t(Ret_t.SML_ERR_XLT_NO_MATCHING_CODEPAGE);
                }
            }
            /*
             * Strip off namespace prefixes and ':' to find the tag. If no
             * prefix is defined (pScanner.nsprelen == 0) take the whole
             * tagname.
             */
            if (nsprelen > 0)
                tagid = XltTags.getTagIDByStringAndExt(name.substring(nsprelen + 1), ext);
            else
                tagid = XltTags.getTagIDByStringAndExt(name, ext);
        }
        /* free temporary buffers */

        if ((tagid == XltTagID_t.TN_UNDEF)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
        }

        /*
         * remember the old extension including the corresponding start tag if
         * we found a new one
         */
        if (ext != pScanner.ext) { /* namespace changed */
            pScanner.prev_ext = pScanner.ext; /* remember the old ext */
            // .. and the corresponding start tag
            pScanner.prev_ext_tag = pScanner.ext_tag;
            pScanner.ext = ext;
            pScanner.ext_tag = tagid;
            pScanner.nsprefix = nsprefix;
            pScanner.nsprelen = nsprelen;
        }

        pScanner.curtok.tagid = tagid;
        pScanner.curtok.ext = pScanner.ext;
        skipS(pScanner);

        if (endtag) {
            /* found end tag */
            if (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, ">", 1) != 0)
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
            // Content does not have start and end tag
            pScanner.curtok.type = XltTokType_t.TOK_TAG_END;
            readBytes(pScanner, 1);
            /* in case of an endtag we might need to close the current CP */
            if (tagid == pScanner.ext_tag) {
                pScanner.ext_tag = pScanner.prev_ext_tag;
                pScanner.ext = pScanner.prev_ext;
                pScanner.prev_ext = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
                pScanner.prev_ext_tag = XltTagID_t.TN_UNDEF;
                pScanner.nsprelen = 0;
                pScanner.nsprefix = null;
            }
        }
        else {
            /* Attributes are not supported in SyncML . skip them */
            xmlSkipAttributes(pScanner);
            if (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "/>", 2) == 0) {
                /* found empty tag */
                pScanner.curtok.type = XltTokType_t.TOK_TAG_EMPTY;
                readBytes(pScanner, 2);
            }
            else if (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, ">", 1) == 0) {
                pScanner.curtok.type = XltTokType_t.TOK_TAG_START;
                readBytes(pScanner, 1);
            }
            else {
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_XML_DOC);
            }
        }
    }

    /**
     * FUNCTION: xmlSkipPI
     * 
     * Skip PI elements
     */
    public static Ret_t xmlSkipPI(
        final XmlScanner_t pScanner
    ) {
        // Get rid of warning, this should not be called anyway
        if (pScanner != null) {
        }
        return Ret_t.SML_ERR_UNSPECIFIC;
    }

    /**
     * FUNCTION: xmlSkipComment
     * 
     * Skip comments
     */
    public static Ret_t xmlSkipComment(
        final XmlScanner_t pScanner
    ) throws SmlException_t {
        readBytes(pScanner, 4);

        while ((pScanner.pos + 3 <= pScanner.buffer.length()) && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "-->", 3) != 0))
            if (!readBytes(pScanner, 1))
                return Ret_t.SML_ERR_XLT_END_OF_BUFFER;

        if (pScanner.pos + 3 > pScanner.buffer.length())
            return Ret_t.SML_ERR_XLT_END_OF_BUFFER;

        if (!readBytes(pScanner, 3))
            return Ret_t.SML_ERR_XLT_END_OF_BUFFER;

        skipS(pScanner);

        return Ret_t.SML_ERR_OK;
    }

    /**
     * FUNCTION: xmlSkipAttributes
     * 
     * Skip attributes . they are not supported in SyncML
     */
    public static void xmlSkipAttributes(
        final XmlScanner_t pScanner
    ) throws SmlException_t {
        while ((pScanner.pos + 1 <= pScanner.buffer.length()) && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, ">", 1) != 0)
                && (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, "/>", 2) != 0))
            if (!readBytes(pScanner, 1))
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);

        if (pScanner.pos + 1 > pScanner.buffer.length())
             throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);
    }

    /**
     * FUNCTION: xmlCDATA
     * 
     * Handle a CDATA content
     */
    public static void xmlCDATA(
        final XmlScanner_t pScanner
    ) throws SmlException_t {
        SmlPcdata_t pPCData;
        int begin;

        readBytes(pScanner, 9);

        pPCData = new SmlPcdata_t();
        pPCData.contentType = SmlPcdataType_t.SML_PCDATA_UNDEFINED;
        pPCData.content = new byte[0];

        begin = pScanner.pos;
        while (!((pScanner.buffer.charAt(pScanner.pos) == ']') && (pScanner.buffer.charAt(pScanner.pos+1) == ']') && (pScanner.buffer.charAt(pScanner.pos+2) == '>')))
            if (!readBytes(pScanner, 1))
                throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);

        pPCData.content = pScanner.buffer.substring(begin, pScanner.pos).getBytes();
        pPCData.contentType = SmlPcdataType_t.SML_PCDATA_CDATA;
        pScanner.curtok.type = XltTokType_t.TOK_CONT;
        pScanner.curtok.pcdata = pPCData;

        readBytes(pScanner, 3);
    }

    /**
     * FUNCTION: xmlSkipPCDATA
     * 
     * Read over a Pcdata content
     */
    public static void xmlSkipPCDATA(
        final XmlScanner_t pScanner
    ) throws SmlException_t {
        SmlPcdata_t pPCData;
        int begin;
        int len;
        String _tagString = null;
        String _tagString2 = null;

        /*
         * TODO: Sub-DTDs in PCData is not supported yet.
         */ 
        _tagString = XltTags.getTagString(
            pScanner.curtok.tagid, 
            pScanner.curtok.ext
        );
        _tagString2 = "</";
        if (pScanner.nsprelen > 0) {
            _tagString2 += pScanner.nsprefix;
            _tagString2 += ":";
        }
        _tagString2 += _tagString;
        _tagString2 += ">";
        pPCData = new SmlPcdata_t();

        if (pPCData == null) {
            throw new SmlException_t(Ret_t.SML_ERR_NOT_ENOUGH_SPACE);
        }
        pPCData.contentType = SmlPcdataType_t.SML_PCDATA_UNDEFINED;
        pPCData.extension = SmlPcdataExtension_t.SML_EXT_UNDEFINED;
        pPCData.content = new byte[0];
        begin = pScanner.pos;

        // read Pcdata content until end tag appears
        while (SmlLib.smlLibStrncmp(pScanner.buffer, pScanner.pos, _tagString2, _tagString2.length()) != 0) {
            if (pScanner.pos >= pScanner.buffer.length()) {
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            if (!readBytes(pScanner, 1))
                 throw new SmlException_t(Ret_t.SML_ERR_XLT_END_OF_BUFFER);

        }
        len = pScanner.pos - begin;
        pPCData.content = pScanner.buffer.substring(begin, begin + len).getBytes();
        pPCData.contentType = SmlPcdataType_t.SML_PCDATA_STRING;
        pScanner.curtok.type = XltTokType_t.TOK_CONT;
        pScanner.curtok.pcdata = pPCData;        
    }

    /**
     * FUNCTION: isPcdata
     * 
     * Check if the current tag id represents a Pcdata element
     */
    public static boolean isPcdata(XltTagID_t tagid) {
        switch (tagid) {
        case TN_CMD:
        case TN_CMDID:
        case TN_CMDREF:
        case TN_LANG:
        case TN_LOCNAME:
        case TN_LOCURI:
        case TN_MSGID:
        case TN_MSGREF:
        case TN_RESPURI:
        case TN_SESSIONID:
        case TN_SOURCEREF:
        case TN_TARGETREF:
        case TN_VERSION:
        case TN_PROTO:
        case TN_DATA:
        case TN_META:
        case TN_NUMBEROFCHANGES:
        case TN_METINF_EMI:
        case TN_METINF_FORMAT:
        case TN_METINF_FREEID:
        case TN_METINF_FREEMEM:
        case TN_METINF_LAST:
        case TN_METINF_MARK:
        case TN_METINF_MAXMSGSIZE:
            /* SCTSTK - 18/03/2002 S.H. 2002-04-05 : SyncML 1.1 */
        case TN_METINF_MAXOBJSIZE:
        case TN_METINF_NEXT:
        case TN_METINF_NEXTNONCE:
        case TN_METINF_SIZE:
        case TN_METINF_TYPE:
        case TN_METINF_VERSION:
        case TN_DEVINF_MAN:
        case TN_DEVINF_MOD:
        case TN_DEVINF_OEM:
        case TN_DEVINF_FWV:
        case TN_DEVINF_SWV:
        case TN_DEVINF_HWV:
        case TN_DEVINF_DEVID:
        case TN_DEVINF_DEVTYP:
        case TN_DEVINF_MAXGUIDSIZE:
        case TN_DEVINF_SOURCEREF:
        case TN_DEVINF_DISPLAYNAME:
        case TN_DEVINF_CTTYPE:
        case TN_DEVINF_DATATYPE:
        case TN_DEVINF_SIZE:
        case TN_DEVINF_PROPNAME:
        case TN_DEVINF_VALENUM:
        case TN_DEVINF_PARAMNAME:
        case TN_DEVINF_SYNCTYPE:
        case TN_DEVINF_XNAM:
        case TN_DEVINF_XVAL:
        case TN_DEVINF_MAXMEM:
        case TN_DEVINF_MAXID:
        case TN_DEVINF_VERCT:
        case TN_DEVINF_VERDTD:
            return true;
        default:
            return false;
        }
    }

}
