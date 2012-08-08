/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: SmlDevInfDevInf_t.java,v 1.3 2007/03/19 01:06:46 wfro Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SmlDevInfDevInf_t {

    public SmlPcdata_t verdtd;

    public SmlPcdata_t man; /* optional */

    public SmlPcdata_t mod; /* optional */

    public SmlPcdata_t oem; /* optional */

    public SmlPcdata_t fwv; /* optional */

    public SmlPcdata_t swv; /* optional */

    public SmlPcdata_t hwv; /* optional */

    public SmlPcdata_t devid;

    public SmlPcdata_t devtyp;

    public SmlDevInfDatastoreList_t datastore;

    public SmlDevInfCtcapList_t ctcap;

    public SmlDevInfExtList_t ext;

    /* SCTSTK - 18/03/2002, S.H. 2002-04-05 : SyncML 1.1 */
    public int flags;

    public byte[] toByteArray(
    ) throws SmlException_t {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(verdtd.toByteArray());
            baos.write(man.toByteArray());
            baos.write(mod.toByteArray());
            baos.write(oem.toByteArray());
            baos.write(fwv.toByteArray());
            baos.write(swv.toByteArray());
            baos.write(hwv.toByteArray());
            baos.write(devid.toByteArray());
            baos.write(devtyp.toByteArray());
            baos.write(datastore.toByteArray());        
            baos.write(ctcap.toByteArray());        
            baos.write(ext.toByteArray());        
            return baos.toByteArray();
        }
        catch(IOException e) {
            throw new SmlException_t(Ret_t.SML_ERR_A_MGR_ERROR);
        }
    }
}
