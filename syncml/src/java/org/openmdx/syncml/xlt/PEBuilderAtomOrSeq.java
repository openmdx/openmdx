/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: PEBuilderAtomOrSeq.java,v 1.3 2007/03/19 01:06:48 wfro Exp $
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
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlProtoElement_t;

public class PEBuilderAtomOrSeq extends PEBuilder_t {

    public PEBuilderAtomOrSeq(XltTagID_t tagid, SmlProtoElement_t type) {
        super(tagid, type);
    }
    
    public SmlAtomic_t build(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t { 
        XltDecScanner_t pScanner;
        SmlAtomic_t pAoS;        /* SmlAtomicPtr_t and SequencePtr_t are pointer
                                    to the same structure! */
        byte break_aos = 0;    /* stop decoding the Atomic when we find a
                                    SyncML command */
        long cmdid = 0;
    
        pScanner = pDecoder.scanner;
    
        pAoS = new SmlAtomic_t();
    
        /* initialize the element type field */
        pAoS.elementType = SmlProtoElement_t.SML_PE_CMD_GROUP;
    
        if (XltDec.IS_EMPTY(pScanner.curtok)) {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        /* get next token */
        XltDec.nextToken(pDecoder);
    
        /* parse child elements until we find a matching end tag
           or until we find a TN_ADD, TN_ATOMIC, etc. start tag */
        while (!XltDec.IS_END(pScanner.curtok) && (break_aos != 0)) {
            switch (pScanner.curtok.tagid) {
    
                /* PCDATA elements */
                case TN_CMDID:
                    pAoS.cmdID = XltDec.buildPCData(pDecoder);
                    cmdid++;
                    break;
                case TN_META:
                    pAoS.meta = XltDec.buildPCData(pDecoder);
                    break;
    
                    /* flags */
                case TN_NORESP:
                    pAoS.flags |= Flag_t.SmlNoResp_f;
                    break;
    
                    /* quit if we find an Add, Atomic, etc.
                       element */
                case TN_ADD:
                case TN_REPLACE:
                case TN_DELETE:
                case TN_COPY:
                case TN_ATOMIC:
                case TN_MAP:
                case TN_SYNC:
                case TN_GET:
                case TN_ALERT:
                case TN_EXEC:
                    break_aos = 1;
                    break;
    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            if (break_aos != 0) {
                XltDec.nextToken(pDecoder); 
            } else {
                /* we've found a SyncML command - we need to go
                   back one token and correct the tagstack */
                XltDec.discardToken(pDecoder);
            }
        }
    
        if (break_aos != 0) {
            /* Atomic/Sequence must contain at least one SyncML command */
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        if (cmdid == 0)
        {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
        }
    
        return pAoS;
    }

}
