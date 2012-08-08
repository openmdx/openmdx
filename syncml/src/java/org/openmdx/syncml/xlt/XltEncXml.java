//*************************************************************************/
//* module:          The XML Encoder source file                          */
//* file:            xltencxml.c                                          */
//* target system:   All                                                  */
//* target OS:       All                                                  */   
//*************************************************************************/

package org.openmdx.syncml.xlt;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlPcdataExtension_t;

public class XltEncXml {
    
    /**
     * FUNCTION: xmlGenerateTag
     *
     * Generates a XML tag
     *
     * PRE-Condition:   valid parameters 
     *
     * POST-Condition:  the XML tag is written to the XML buffer
     *
     * IN:              tagId, the ID for the tag to generate (TN_ADD, ...)
     *                  tagType, the tag type (e.g. Begin Tag . TT_BEG, ...)
     *                  attFlag, indicates if the encoded tag contain Attributes in namespace extensions
     *
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static void xmlGenerateTag(
        final XltTagID_t tagId, 
        final XltTagType_t tagType, 
        final BufferMgmt_t pBufMgr, 
        final SmlPcdataExtension_t attFlag
    ) throws SmlException_t {

        char _begpar = Xlt_t.XML_BEGPAR;
        char _tagdel = Xlt_t.XML_TAGDEL;
        char _endpar = Xlt_t.XML_ENDPAR;
        String _nstagstart = Xlt_t.XML_NSSTART;
        String _nstagend = Xlt_t.XML_NSEND;

        String _tagstr = null;
        String _tagnsattr = null;

        _tagstr = XltTags.getTagString(tagId, attFlag);

        if (_tagstr == null) { // check again as _tagstr might be alterd in
                                // getTagString
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_TAG_TYPE);
        }

        /* the <SyncML> tag _must_ have an xmlns attribute */
        if (attFlag != pBufMgr.smlActiveExt || tagId == XltTagID_t.TN_SYNCML) {
            // %%% luz:2003-07-31: now uses namespace from table according to
            // version
            _tagnsattr = XltTags.getExtName(attFlag, pBufMgr.vers);
        }
        pBufMgr.smlActiveExt = attFlag;
        // check if content byte has to be added to the tag
        switch (tagType) {
            // set the end tag
            case TT_END: {
                XltEncCom.xltAddToBuffer(_begpar, pBufMgr);
                XltEncCom.xltAddToBuffer(_tagdel, pBufMgr);
                XltEncCom.xltAddToBuffer(_tagstr, pBufMgr);
                XltEncCom.xltAddToBuffer(_endpar, pBufMgr);
                if (tagId == pBufMgr.switchExtTag) {
                    pBufMgr.smlActiveExt = pBufMgr.smlLastExt;
                    pBufMgr.smlCurExt = pBufMgr.smlLastExt;
                    pBufMgr.smlLastExt = attFlag;
                }
                // just forget the stored number ob bytes for this end-tag since
                // written now
                pBufMgr.endTagSize -= (3 + _tagstr.length());
                break;
            }
                // Empty tag
            case TT_ALL: {
                XltEncCom.xltAddToBuffer(_begpar, pBufMgr);
                XltEncCom.xltAddToBuffer(_tagstr, pBufMgr);
                if (_tagnsattr != null) {
                    XltEncCom.xltAddToBuffer(_nstagstart, pBufMgr);
                    XltEncCom.xltAddToBuffer(_tagnsattr, pBufMgr);
                    XltEncCom.xltAddToBuffer(_nstagend, pBufMgr);
                }
                XltEncCom.xltAddToBuffer(_tagdel, pBufMgr);
                XltEncCom.xltAddToBuffer(_endpar, pBufMgr);
                break;
            }
                // Only Begin Tag . content follows . content byte has to be added
            case TT_BEG: {
                XltEncCom.xltAddToBuffer(_begpar, pBufMgr);
                XltEncCom.xltAddToBuffer(_tagstr, pBufMgr);
                if (_tagnsattr != null) {
                    XltEncCom.xltAddToBuffer(_nstagstart, pBufMgr);
                    XltEncCom.xltAddToBuffer(_tagnsattr, pBufMgr);
                    XltEncCom.xltAddToBuffer(_nstagend, pBufMgr);
                }
                XltEncCom.xltAddToBuffer(_endpar, pBufMgr);
    
                // remember the number of byte that must follow for the according  end-tag
                pBufMgr.endTagSize += (3 + _tagstr.length());
                break;
            }
            default: {
                throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_TAG_TYPE);
            }
        }
    }
}
