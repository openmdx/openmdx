/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: SmlLib.java,v 1.5 2007/04/02 00:56:05 wfro Exp $
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
package org.openmdx.syncml;

public class SmlLib {

    public static int smlLibStrncmp(
        String buffer, 
        int pos, 
        String s2, 
        int len
    ) throws SmlException_t {
        try {
            return buffer.substring(pos, pos+len).compareTo(s2);
        }
        catch(StringIndexOutOfBoundsException e) {
            throw new SmlException_t(Ret_t.SML_ERR_A_MGR_ERROR);
        }
    }

    public static int smlLibStrlen(
        String buffer,
        int pos
    ) {
        int len = 0;
        while(
            (pos+len < buffer.length()) &&
            (buffer.charAt(pos+len) != 0)
        ) {
            len += 1;  
        }
        return len;
    }
    
    public static int smlLibStrlen(
        byte[] buffer,
        int pos
    ) {
        int len = 0;
        while(
            (pos+len < buffer.length) &&
            (buffer[pos+len] != 0)
        ) {
            len += 1;  
        }
        return len;
    }
    
    public static int smlLibStrchr(
        String buffer, 
        int pos, 
        String s
    ) {
        return buffer.indexOf(s, pos);
    }
    
}
