//*************************************************************************/
//* module:          Encoder utils file                                   */
//* file:            xltenccom.c                                          */
//* target system:   All                                                  */
//* target OS:       All                                                  */   
//*************************************************************************/

package org.openmdx.syncml.xlt;

import java.io.IOException;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;

public class XltEncCom {
    
    /**
     * FUNCTION: xltAddToBuffer
     *
     * Add a string to the global buffer
     *
     * PRE-Condition:  pContent contains some content bytes to write to the (WB) XML buffer
     *
     * POST-Condition: content is written to the buffer 
     *
     * IN:             pContent, the character pointer referencing the content to
     *                           write to the buffer
     *                 size, the content length
     * 
     * IN/OUT:         pBufMgr, pointer to a structure containing buffer management elements
     * 
     * RETURN:         shows error codes of function, 
     *                 0, if OK
     */
    public static void xltAddToBuffer(
        byte pContent, 
        BufferMgmt_t pBufMgr
    ) throws SmlException_t {
        xltAddToBuffer(new byte[]{pContent}, pBufMgr);
    }

    public static void xltAddToBuffer(
        String pContent,
        BufferMgmt_t pBufMgr
    ) throws SmlException_t {
      try {
          pBufMgr.smlXltBuffer.write(pContent.getBytes());
      }
      catch(IOException e) {
          throw new SmlException_t(Ret_t.SML_ERR_XLT_BUF_ERR);
      }
    }

    public static void xltAddToBuffer(
        char pContent,
        BufferMgmt_t pBufMgr
    ) throws SmlException_t {
      pBufMgr.smlXltBuffer.write(pContent);
    }

    public static void xltAddToBuffer(
        byte[] pContent, 
        BufferMgmt_t pBufMgr
    ) throws SmlException_t {
      try {
          pBufMgr.smlXltBuffer.write(pContent);
      }
      catch(IOException e) {
          throw new SmlException_t(Ret_t.SML_ERR_XLT_BUF_ERR);
      }
    }
    
}

