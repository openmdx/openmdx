/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: XmlScanner_t.java,v 1.4 2007/04/02 00:56:05 wfro Exp $
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

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlLib;
import org.openmdx.syncml.SmlPcdataExtension_t;

public class XmlScanner_t extends XltDecScanner_t {

    public void nextTok(
    ) throws SmlException_t {

        this.curtok.start = this.pos;

        XltDecXml.skipS(this);

        /* skip unsupported elements until we find a supported one */
        while (true) {
            if (SmlLib.smlLibStrncmp(this.buffer, this.pos, "<!--", 4) == 0) {
                XltDecXml.xmlSkipComment(this);
            } 
            else if (SmlLib.smlLibStrncmp(this.buffer, this.pos, "<?", 2) == 0) {
                XltDecXml.xmlSkipPI(this);
            } 
            else if (SmlLib.smlLibStrncmp(this.buffer, this.pos, "</", 2) == 0) {
                XltDecXml.xmlTag(this, true);
                break;
            } 
            else if (SmlLib.smlLibStrncmp(this.buffer, this.pos, "<![CDATA[", 9) == 0) {
                XltDecXml.xmlCDATA(this);
                break;
            } 
            else if ((XltDecXml.isPcdata(this.curtok.tagid)) && (this.curtok.type != XltTokType_t.TOK_TAG_END)) {
                XltDecXml.xmlSkipPCDATA(this);
                break;
            } 
            else if (SmlLib.smlLibStrncmp(this.buffer, this.pos, "<", 1) == 0) {
                XltDecXml.xmlTag(this, false);
                break;
            } 
            else {
                XltDecXml.xmlCharData(this);
                break;
            }
        }
    }

    public void destroy(
    ) throws SmlException_t {
    }

    public void pushTok(
    ) {
        this.pos = this.curtok.start;
        /* invalidate curtok */
        /* T.K. Possible Error. pScannerPriv.curtok is of type XltDecToken_t NOT ...Ptr_t */
        // OrigLine:
        // smlLibMemset(pScannerPriv.curtok, 0, sizeof(XltDecTokenPtr_t));
        this.curtok.type = XltTokType_t.TOK_UNDEF;

    }

    public int getPos(
    ) {
        return this.pos;
    }

    public String buffer;
    
    public SmlPcdataExtension_t ext; /* which is the actual open namespace ? */

    public SmlPcdataExtension_t prev_ext; /* which is the previous open namespace ? */

    public XltTagID_t ext_tag; /* which tag started the actual namespace ? */

    public XltTagID_t prev_ext_tag; /* which tag started the previous open namespace ? */

    public String nsprefix; /* prefix used for active namespace (if any) */

    public int nsprelen; /* how long is the prefix ? (to save smlLibStrlen calls) */

    public int finished;


}