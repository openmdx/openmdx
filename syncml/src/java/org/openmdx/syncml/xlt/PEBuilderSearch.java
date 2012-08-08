/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: PEBuilderSearch.java,v 1.3 2007/03/19 01:06:48 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/19 01:06:48 $
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
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlSearch_t;

public class PEBuilderSearch extends PEBuilder_t {

    public PEBuilderSearch(XltTagID_t tagid, SmlProtoElement_t type) {
        super(tagid, type);
    }
    
    public SmlSearch_t build(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlSearch_t pSearch;
        long source = 0, meta = 0, data = 0, cmdid = 0;
    
        pScanner = pDecoder.scanner;
    
        pSearch = new SmlSearch_t();
    
        /* initialize the element type field */
        pSearch.elementType = SmlProtoElement_t.SML_PE_SEARCH;
    
        /* Meta is required */
        if (XltDec.IS_EMPTY(pScanner.curtok)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        while (!XltDec.IS_END(pScanner.curtok)) {
            switch (pScanner.curtok.tagid) {
    
                /* PCDATA elements */
                case TN_CMDID:
                    pSearch.cmdID = XltDec.buildPCData(pDecoder);
                    cmdid++;
                    break;
                case TN_LANG:
                    pSearch.lang = XltDec.buildPCData(pDecoder);
                    break;
                case TN_META: 
                    pSearch.meta = XltDec.buildPCData(pDecoder);
                    meta++;
                    break;
                case TN_DATA: 
                    pSearch.data = XltDec.buildPCData(pDecoder);
                    data++;
                    break;
                    /* child tags */
                case TN_CRED: 
                    pSearch.cred = XltDec.buildCred(pDecoder);
                    break;
                case TN_TARGET: 
                    pSearch.target = XltDec.buildTargetOrSource(pDecoder);
                    break;    
                    /* flags */
                case TN_NORESP:
                    pSearch.flags |= Flag_t.SmlNoResp_f;
                    break;
                case TN_NORESULTS:
                    pSearch.flags |= Flag_t.SmlNoResults_f;
                    break;    
                    /* Lists */
                case TN_SOURCE:
                    pSearch.sourceList = XltDec.appendSourceList(pDecoder, pSearch.sourceList);
                    source++;
                    break;
    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            XltDec.nextToken(pDecoder);
        }
    
        if ((source == 0) || (meta == 0) || (data == 0) || (cmdid == 0)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        return pSearch;
    }
}