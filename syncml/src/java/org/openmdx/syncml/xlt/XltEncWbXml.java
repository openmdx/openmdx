//*************************************************************************/
//* module:          The WBXML Encoder source file                        */
//* file:            xltencwbxml.c                                        */
//* target system:   All                                                  */
//* target OS:       All                                                  */   
//*************************************************************************/

package org.openmdx.syncml.xlt;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;

public class XltEncWbXml {
    
    static GlobTok_t[] globtoken = new GlobTok_t[]{
        new GlobTok_t(XltElementType_t.END,    (byte)0x01),  //Tag End
        new GlobTok_t(XltElementType_t.STR_I,  (byte)0x03),  //Inline string    
        new GlobTok_t(XltElementType_t.OPAQUE, (byte)0xC3),  //Opaque Data  
        new GlobTok_t(XltElementType_t.UNDEF,  (byte)0x00)
      };
        
    /**
     * FUNCTION: wbxmlGetGlobToken
     *
     * Converts a element type into its wbxml token
     *
     * PRE-Condition:   valid element type
     *
     * POST-Condition:  return of wbxml token
     *
     * IN:              elType, element type
     * 
     * OUT:             wbxml token
     * 
     * RETURN:          wbxml token 
     *                  0, if no matching wbxml token
     */
    public static byte wbxmlGetGlobToken(
        final XltElementType_t elType
    ) {
    
      // encoding of global tokens; related to the type XML_ElementType_t
      int i = -1; 
      while (globtoken[++i].id != XltElementType_t.UNDEF)
        if (globtoken[i].id == elType)
          return globtoken[i].wbxml;
      return 0;
    
    }

    /**
     * FUNCTION: wbxmlGenerateTag
     *
     * Generates a tag for a given tag ID and a given tag type
     *
     * PRE-Condition:   valid parameters 
     *
     * POST-Condition:  a new wbxml tag is written to the buffer
     *
     * IN:              tagId, the ID for the tag to generate (TN_ADD, ...)
     *                  tagType, the tag type (e.g. Begin Tag . TT_BEG, ...)
     * 
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static void wbxmlGenerateTag(
        final XltTagID_t tagId, 
        final XltTagType_t tagType, 
        final BufferMgmt_t pBufMgr
    ) throws SmlException_t {
    
      Ret_t _err = Ret_t.SML_ERR_OK;
      byte _tmp = 0x00;
    
      //check if content byte has to be added to the tag
      switch (tagType)
      {
        //set the end tag
        case TT_END:
        {
          _tmp = wbxmlGetGlobToken(XltElementType_t.END);
          if (_tmp == 0) throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_TAG_TYPE);
          XltEncCom.xltAddToBuffer((_tmp), pBufMgr);
          // remember the number of byte that must follow for the according  end-tag
    	  if (_err == Ret_t.SML_ERR_OK) pBufMgr.endTagSize -= 1;
    	  return;
        }    
        //Begin and End Tag in one
        case TT_ALL:
        {
          _tmp = XltTags.getTagByte(tagId, pBufMgr.smlCurExt);
          if ((_tmp == 0) || (_err != Ret_t.SML_ERR_OK)) throw new SmlException_t(_err);
          XltEncCom.xltAddToBuffer(_tmp, pBufMgr);
          return;
        }
        //Only Begin Tag . content follows . content byte has to be added
        case TT_BEG:
        {
          _tmp = XltTags.getTagByte(tagId, pBufMgr.smlCurExt);
          if ((_tmp == 0) || (_err != Ret_t.SML_ERR_OK)) throw new SmlException_t(_err);
    
          _tmp = (byte)(_tmp | Xlt_t.XLT_CONTBYTE);
          XltEncCom.xltAddToBuffer(_tmp, pBufMgr);
          // remember the number of byte that must follow for the according  end-tag
    	  if (_err == Ret_t.SML_ERR_OK) pBufMgr.endTagSize += 1;
    	  return;
        }
    	default:
    	    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_TAG_TYPE);
      }
    }

    /**
     * FUNCTION: wbxmlWriteTypeToBuffer
     *
     * Write a content of a certain WBXML element type (e.g. STR_I) to the global buffer
     *
     * PRE-Condition:   valid parameters 
     *
     * POST-Condition:  the content is written to the wbxml buffer with the leading
     *                  bytes for the opaque data type or the STR_I data type
     *
     * IN:              pContent, the character pointer referencing the content to
     *                            write to the buffer
     *                  elType, the element type to write to the buffer (e.g. STR_I)
     *                  size, the content length
     * 
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static void wbxmlWriteTypeToBuffer(
        final byte pContent, 
        final XltElementType_t elType,
        final BufferMgmt_t pBufMgr
    ) throws SmlException_t {
        wbxmlWriteTypeToBuffer(
            new byte[]{pContent},
            elType,
            pBufMgr
        );        
    }
    
    public static void wbxmlWriteTypeToBuffer(
        final byte[] pContent, 
        final XltElementType_t elType,
        final BufferMgmt_t pBufMgr
    ) throws SmlException_t {
    
      byte _termstr = Xlt_t.XLT_TERMSTR;
      byte _tmp;
      
      switch(elType)
      {
        case TAG:
        {
            XltEncCom.xltAddToBuffer(pContent, pBufMgr);
            break;
        }
        case STR_I:
        {
            _tmp = wbxmlGetGlobToken(XltElementType_t.STR_I);
            if (_tmp == 0) throw new SmlException_t(Ret_t.SML_ERR_XLT_WBXML_UKN_TOK);
    
            //add the STR_I identifier
            XltEncCom.xltAddToBuffer(_tmp, pBufMgr);
    
            //add the string to the buffer
            XltEncCom.xltAddToBuffer(pContent, pBufMgr);
    
            //add the string terminator '\0'
            XltEncCom.xltAddToBuffer(_termstr, pBufMgr);
            break;
        }
        case OPAQUE:
        {
          _tmp = wbxmlGetGlobToken(XltElementType_t.OPAQUE);
           if (_tmp == 0) throw new SmlException_t(Ret_t.SML_ERR_XLT_WBXML_UKN_TOK);
    
          //add the OPAQUE identifier
          XltEncCom.xltAddToBuffer(_tmp, pBufMgr);
    
          //add the pContent length      
          XltEncWbXml.wbxmlOpaqueSize2Buf(pContent.length, pBufMgr);
    
          //add the string buffer
          XltEncCom.xltAddToBuffer(pContent, pBufMgr);
          break;
        }
    	default:
    	    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PCDATA_TYPE);
      }
    }
    
    /**
     * FUNCTION: wbxmlOpaqueSize2Buf
     *
     * Converts a Long_t opaque size to a wbxml mb_u_int32 and adds it to the buffer
     *
     * PRE-Condition:   size of the content to be written as opaque datatype
     *
     * POST-Condition:  the size is converted to the mb_u_int32 representation and added
     *                  to the buffer
     *
     * IN:              size, length of the opaque data
     * 
     * IN/OUT:          pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:          shows error codes of function, 
     *                  0, if OK
     */
    public static void wbxmlOpaqueSize2Buf(
        int size, 
        BufferMgmt_t pBufMgr
    ) throws SmlException_t {
        pBufMgr.smlXltBuffer.write(size);
    }

}
