/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: SyncCallbackHandler.java,v 1.9 2007/04/02 23:40:58 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.9 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2007/04/02 23:40:58 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, CRIXP Corp., Switzerland
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
 * * Neither the name of CRIXP Corp. nor the names of the contributors
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
package org.openmdx.syncml.engine;

import java.util.Date;
import java.util.Map;

import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlExec_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSearch_t;
import org.openmdx.syncml.SmlSequence_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;

public interface SyncCallbackHandler {

    public boolean serverMessageIsFinal();
    
    public boolean clientMessageIsFinal();
    
    public SmlSyncHdr_t getRequestHdr();

    public Date getSyncStartedAt();
    
    public Map<String, String[]> getSyncAnchors();
    
    public void startMessage(SmlSyncHdr_t syncHdr) throws SmlException_t;

    public void endMessage(boolean isFinal) throws SmlException_t;        

    public void startSync(SmlSync_t smlSync) throws SmlException_t;

    public void endSync() throws SmlException_t;

    public void startAtomic(SmlAtomic_t smlAtomic) throws SmlException_t;

    public void endAtomic() throws SmlException_t;

    public void startSequence(SmlSequence_t smlSequence) throws SmlException_t;

    public void endSequence() throws SmlException_t;

    public void addCmd(SmlGenericCmd_t smlAdd) throws SmlException_t;

    public void alertCmd(SmlAlert_t smlAlert) throws SmlException_t;

    public void deleteCmd(SmlGenericCmd_t smlDeleteCmd) throws SmlException_t;

    public void getCmd(SmlGetPut_t smlGet) throws SmlException_t;

    public void putCmd(SmlGetPut_t smlPut) throws SmlException_t;

    public void mapCmd(SmlMap_t smlMap) throws SmlException_t;

    public void resultsCmd(SmlResults_t smlResults) throws SmlException_t;

    public void statusCmd(SmlStatus_t smlStatus) throws SmlException_t;

    public void replaceCmd(SmlGenericCmd_t smlReplace) throws SmlException_t;

    public void copyCmd(SmlGenericCmd_t smlCopy) throws SmlException_t;

    public void execCmd(SmlExec_t smlExec) throws SmlException_t;

    public void searchCmd(SmlSearch_t smlSearch) throws SmlException_t;

    public void handleError() throws SmlException_t;
    
}
