/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: SmlProtoElement_t.java,v 1.3 2007/03/19 01:06:46 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/19 01:06:46 $
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

public enum SmlProtoElement_t {

    SML_PE_UNDEF(0),
    SML_PE_ERROR(1),
    SML_PE_ADD(2),
    SML_PE_ALERT(3),
    SML_PE_ATOMIC_START(4),
    SML_PE_ATOMIC_END(5),
    SML_PE_COPY(6),  
    SML_PE_DELETE(7),
    SML_PE_EXEC(8),
    SML_PE_GET(9),
    SML_PE_MAP(10),
    SML_PE_PUT(11),
    SML_PE_RESULTS(12),
    SML_PE_SEARCH(13),
    SML_PE_SEQUENCE_START(14),
    SML_PE_SEQUENCE_END(15),
    SML_PE_STATUS(16),
    SML_PE_SYNC_START(17),
    SML_PE_SYNC_END(18),
    SML_PE_REPLACE(19),
    SML_PE_HEADER(20),
    SML_PE_PUT_GET(21),
    SML_PE_CMD_GROUP(22),
    SML_PE_GENERIC(23),
    SML_PE_FINAL(24);
    
    private final int value;
    
    SmlProtoElement_t(
        int value
    ) {
        this.value = value;
    }
    
    public int getValue(
    ) {
        return this.value;
    }
 
}
