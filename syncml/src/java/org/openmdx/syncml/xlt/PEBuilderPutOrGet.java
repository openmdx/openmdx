/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: PEBuilderPutOrGet.java,v 1.5 2007/04/02 00:56:05 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/04/02 00:56:05 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of OMEX AG nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.syncml.xlt;

import org.openmdx.syncml.Flag_t;
import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlProtoElement_t;

public class PEBuilderPutOrGet extends PEBuilder_t {

    public PEBuilderPutOrGet(XltTagID_t tagid, SmlProtoElement_t type) {
        super(tagid, type);
    }
    
    public SmlGetPut_t build(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlGetPut_t pGet;
        long items = 0, cmdid = 0;
    
        pScanner = pDecoder.scanner;
    
        pGet = new SmlGetPut_t();
    
        /* initialize the element type field */
        pGet.elementType = SmlProtoElement_t.SML_PE_PUT_GET;
    
        if (XltDec.IS_EMPTY(pScanner.curtok)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        XltDec.nextToken(pDecoder);
    
        while (!XltDec.IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
                case TN_CMDID:
                    pGet.cmdID = XltDec.buildPCData(pDecoder);
                    cmdid++;
                    break;
                case TN_LANG:
                    pGet.lang = XltDec.buildPCData(pDecoder);
                    break;
                case TN_META: 
                    pGet.meta = XltDec.buildPCData(pDecoder);
                    break;
                case TN_CRED: 
                    pGet.cred = XltDec.buildCred(pDecoder);
                    break;    
                case TN_NORESP:
                    pGet.flags |= Flag_t.SmlNoResp_f;
                    break;
                case TN_ITEM:
                case TN_METINF_VERSION:
                    pGet.itemList = XltDec.appendItemList(pDecoder, pGet.itemList);
                    items++;
                    break;    
                default:
                   throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
    
        if ((items == 0) || (cmdid == 0))
        {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        return pGet;
    }
}