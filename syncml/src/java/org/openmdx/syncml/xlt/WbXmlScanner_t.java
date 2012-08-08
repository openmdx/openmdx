/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: WbXmlScanner_t.java,v 1.4 2007/04/02 00:56:05 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.4 $
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

import java.util.Stack;

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlPcdataExtension_t;

public class WbXmlScanner_t extends XltDecScanner_t  {

    /**
     * FUNCTION: destroy
     * 
     * Free memory. Description see XltDecAll.h.
     */
    public void destroy(
    ) {
        this.tagstack = null; 
    }

    /**
     * FUNCTION: nextTok
     *
     * Get next token.
     */
    public void nextTok(
    ) throws SmlException_t {
    
        this.curtok.start = this.pos;
    
        /* keep going until we find a "supported" element */
        while(true) {
            /* skip PIs, extensions and entities... */
            if (XltDecWbXml.IS_PI(this.buffer, this.pos)) {
                XltDecWbXml.wbxmlSkipPI(this);
            } 
            else if (XltDecWbXml.IS_EXTENSION(this.buffer, this.pos)) {
                XltDecWbXml.wbxmlSkipExtension(this);
            } 
            else if (XltDecWbXml.IS_ENTITY(this.buffer, this.pos)) {
                XltDecWbXml.wbxmlSkipEntity(this);    
            /* ... decode strings, opaque data and tags */
            } 
            else if (XltDecWbXml.IS_STRING(this.buffer, this.pos)) {
                XltDecWbXml.wbxmlStringToken(this);
                break;
            } 
            else if (XltDecWbXml.IS_OPAQUE(this.buffer, this.pos)) {
                XltDecWbXml.wbxmlOpaqueToken(this);
                break;
            } 
            else {
                XltDecWbXml.wbxmlTagToken(this);
                break;
            }
        }
    }

    /**
     * FUNCTION: pushTok
     *
     * Reset the scanner to the starting position of the current token within
     * the buffer. 
     */
    public void pushTok(
    ) throws SmlException_t {
        Stack pTagStack;
        XltTagID_t tagid;
    
        pTagStack = this.tagstack;
    
        /* reset scanner to position where tok begins */
        this.pos = this.curtok.start;
    
        /* correct the tag stack */
        if (this.curtok.type == XltTokType_t.TOK_TAG_START) {
           tagid = (XltTagID_t)pTagStack.pop();
        } else if (this.curtok.type == XltTokType_t.TOK_TAG_END) {
            tagid = this.curtok.tagid;
            pTagStack.push(tagid);
        }
    
        /* invalidate curtok */
        /* T.K. Possible Error. pScannerPriv.curtok is of type XltDecToken_t NOT ...Ptr_t */
        // OrigLine:
        // smlLibMemset(pScannerPriv.curtok, 0, sizeof(XltDecTokenPtr_t));
        this.curtok.type = XltTokType_t.TOK_UNDEF;

    }

    public byte[] buffer;
    
    int pubIDIdx; /* strtbl index of the string
     version of the pubID - valid only
     when pubID == 0 */

    public Stack tagstack; /* stack of open start tags */

    public String strtbl; /* copy of the string table */

    public long strtbllen; /* length of the string table */

    public byte state; /* tag state or attribute state */

    public SmlPcdataExtension_t cptag; /* current codepage for tags */

    public byte cpattr; /* current codepage for attributes */

}
