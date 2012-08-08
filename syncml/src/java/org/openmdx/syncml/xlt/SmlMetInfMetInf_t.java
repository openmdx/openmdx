/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: SmlMetInfMetInf_t.java,v 1.5 2007/04/02 23:40:58 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/04/02 23:40:58 $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlPcdataList_t;
import org.openmdx.syncml.SmlPcdata_t;

public class SmlMetInfMetInf_t {

    public SmlPcdata_t format; /* opt. */

    public SmlPcdata_t type; /* opt. */

    public SmlPcdata_t mark; /* opt. */

    public SmlPcdata_t size; /* opt. */

    public SmlPcdata_t nextnonce; /* opt. */

    public SmlPcdata_t version;

    public SmlPcdata_t maxmsgsize; /* optional */

    /* SCTSTK - 18/03/2002, S.H. 2002-04-05 : SyncML 1.1 */
    public SmlPcdata_t maxobjsize; /* optional */

    public SmlMetInfMem_t mem; /* optional */

    public SmlPcdataList_t emi; /* optional */

    public SmlMetInfAnchor_t anchor; /* opt. */

    public byte[] toByteArray(
    ) throws SmlException_t {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if(this.format != null) {
                baos.write("<Format>".getBytes());
                baos.write(this.format.toByteArray());
                baos.write("</Format>".getBytes());
            }
            if(this.type != null) {
                baos.write("<Type>".getBytes());
                baos.write(this.type.toByteArray());
                baos.write("</Type>".getBytes());
            }
            if(this.mark != null) {
                baos.write(this.mark.toByteArray());
            }
            if(this.size != null) {
                baos.write("<Size>".getBytes());                
                baos.write(this.size.toByteArray());
                baos.write("</Size>".getBytes());                
            }
            if(this.nextnonce != null) {
                baos.write("<NextNonce>".getBytes());
                baos.write(this.nextnonce.toByteArray());
                baos.write("</NextNonce>".getBytes());
            }
            if(this.version != null) {
                baos.write(this.version.toByteArray());
            }
            if(this.maxmsgsize != null) {
                baos.write("<MaxMsgSize>".getBytes());
                baos.write(this.maxmsgsize.toByteArray());
                baos.write("</MaxMsgSize>".getBytes());
            }
            if(this.mem != null) {
                baos.write(this.mem.toByteArray());
            }
            if(this.emi != null) {
                baos.write(this.emi.toByteArray());
            }
            if(this.anchor != null) {
                baos.write("<Anchor>".getBytes());
                baos.write(this.anchor.toByteArray());
                baos.write("</Anchor>".getBytes());
            }
            return baos.toByteArray();
        }
        catch(IOException e) {
            throw new SmlException_t(Ret_t.SML_ERR_A_MGR_ERROR);
        }        
    }
    
}
