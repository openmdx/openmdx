/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: PEBuilderSync.java,v 1.3 2007/03/19 01:06:49 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/19 01:06:49 $
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
import org.openmdx.syncml.SmlSync_t;

public class PEBuilderSync extends PEBuilder_t {

    public PEBuilderSync(XltTagID_t tagid, SmlProtoElement_t type) {
        super(tagid, type);
    }
    
    public SmlSync_t build(
        final XltDecoder_t pDecoder 
    ) throws SmlException_t {
        XltDecScanner_t pScanner;
        SmlSync_t pSync;
        long cmdid = 0;
    
        /* stop decoding the Sync when we find a SyncML command */
        boolean break_sync = false;
    
        pScanner = pDecoder.scanner;
    
        /* initialize a new Sync */
        pSync = new SmlSync_t();
    
        /* initialize the element type field */
        pSync.elementType = SmlProtoElement_t.SML_PE_SYNC_START;
    
        if (XltDec.IS_EMPTY(pScanner.curtok)) {    
            return pSync;
        }
    
        /* get next token */
        XltDec.nextToken(pDecoder);
    
        /* parse child elements until we find a matching end tag
           or until we find a TN_ADD, TN_ATOMIC, etc. start tag */
        while (!XltDec.IS_END(pScanner.curtok) && !break_sync) {
            switch (pScanner.curtok.tagid) {
                /* PCDATA elements */
                case TN_CMDID:
                     pSync.cmdID = XltDec.buildPCData(pDecoder);
                    cmdid++;
                    break;
                case TN_META:
                    pSync.meta = XltDec.buildPCData(pDecoder);
                    break;
                case TN_NUMBEROFCHANGES:
                    pSync.noc = XltDec.buildPCData(pDecoder);
                    break;    
                    /* child tags */
                case TN_CRED:
                    pSync.cred = XltDec.buildCred(pDecoder);
                    break;
                case TN_TARGET:
                    pSync.target = XltDec.buildTargetOrSource(pDecoder);
                    break;
                case TN_SOURCE:
                    pSync.source = XltDec.buildTargetOrSource(pDecoder);
                    break;    
                    /* flags */
                case TN_NORESP:
                    pSync.flags |= Flag_t.SmlNoResp_f;
                    break;
    
                    /* quit if we find an Add, Atomic, etc.
                       element */
                case TN_ADD:
                case TN_ATOMIC:
                case TN_COPY:
                case TN_DELETE:
                case TN_SEQUENCE:
                case TN_REPLACE:
                    break_sync = true;
                    break;
    
                default:
                    throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
            }
            if (!break_sync) {
                /* get next token and continue as usual */
                XltDec.nextToken(pDecoder);
            } 
            else {
                /* we've found a SyncML command - we need to go
                   back one token and correct the tagstack */
                XltDec.discardToken(pDecoder);
            }
        }    
        if (!break_sync)  {
          if (pScanner.curtok.tagid != XltTagID_t.TN_SYNC)
          {
            throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_SYNCML_DOC);
          }
          else
          {
             pDecoder.tagstack.push(pScanner.curtok.tagid);
             pDecoder.scanner.pushTok();
          } 
        }
    
        return pSync;
    }

}

