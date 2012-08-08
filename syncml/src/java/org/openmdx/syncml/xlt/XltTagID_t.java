/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: XltTagID_t.java,v 1.3 2007/03/19 01:06:47 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/19 01:06:47 $
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

public enum XltTagID_t {

    TN_UNDEF(0), TN_ADD(1), TN_ALERT(2), TN_ARCHIVE(3), TN_ATOMIC(4), TN_ATOMIC_END(5), /* 5 */
    TN_CHAL(6), TN_CMD(7), TN_CMDID(8), TN_CMDREF(9), TN_COPY(10), /* 10 */
    TN_CRED(11), TN_DATA(12), TN_DELETE(13), TN_EXEC(14), TN_FINAL(15), /* 15 */
    TN_GET(16), TN_ITEM(17), TN_LANG(18), TN_LOCNAME(19), TN_LOCURI(20), /* 20 */
    TN_MAP(21), TN_MAPITEM(22), TN_META(23), TN_MSGID(24), TN_MSGREF(25), /* 25 */
    TN_NORESP(26), TN_NORESULTS(27), TN_PUT(28), TN_REPLACE(29), TN_RESPURI(30), /* 30 */
    TN_RESULTS(31), TN_SEARCH(32), TN_SEQUENCE(33), TN_SEQUENCE_END(34), TN_SESSIONID(35), /* 35 */
    TN_SFTDEL(36), TN_SOURCE(37), TN_SOURCEREF(38), TN_STATUS(39), TN_SYNC(40), /* 40 */
    TN_SYNCBODY(41), TN_SYNCHDR(42), TN_SYNCML(43), TN_SYNC_END(44), TN_TARGET(45), /* 45 */
    TN_TARGETREF(46), TN_VERSION(47), TN_PROTO(48), TN_METINF_ANCHOR(49), TN_METINF_EMI(50), /* 50 */
    TN_METINF_FORMAT(51), TN_METINF_FREEID(52), TN_METINF_FREEMEM(53), TN_METINF_LAST(54), TN_METINF_MARK(55), /* 55 */
    TN_METINF_MAXMSGSIZE(56), TN_METINF_MEM(57), TN_METINF_METINF(58), TN_METINF_NEXT(59), TN_METINF_NEXTNONCE(60), /* 60 */
    TN_METINF_SHAREDMEM(61), TN_METINF_SIZE(62), TN_METINF_TYPE(63), TN_METINF_VERSION(64), TN_DEVINF_CTCAP(65), /* 65 */
    TN_DEVINF_CTTYPE(66), TN_DEVINF_DATASTORE(67), TN_DEVINF_DATATYPE(68), TN_DEVINF_DEVID(69), TN_DEVINF_DEVINF(70), /* 70 */
    TN_DEVINF_DEVTYP(71), TN_DEVINF_DISPLAYNAME(72), TN_DEVINF_DSMEM(73), TN_DEVINF_EXT(74), TN_DEVINF_FWV(75), /* 75 */
    TN_DEVINF_HWV(76), TN_DEVINF_MAN(77), TN_DEVINF_MAXGUIDSIZE(78), TN_DEVINF_MAXID(79), TN_DEVINF_MAXMEM(80), /* 80 */
    TN_DEVINF_MOD(81), TN_DEVINF_OEM(82), TN_DEVINF_PARAMNAME(83), TN_DEVINF_PROPNAME(84), TN_DEVINF_RX(85), /* 85 */
    TN_DEVINF_RXPREF(86), TN_DEVINF_SHAREDMEM(87), TN_DEVINF_SIZE(88), TN_DEVINF_SOURCEREF(89), TN_DEVINF_SWV(90), /* 90 */
    TN_DEVINF_SYNCCAP(91), TN_DEVINF_SYNCTYPE(92), TN_DEVINF_TX(93), TN_DEVINF_TXPREF(94), TN_DEVINF_VALENUM(95), /* 95 */
    TN_DEVINF_VERCT(96), TN_DEVINF_VERDTD(97), TN_DEVINF_XNAM(98), TN_DEVINF_XVAL(99), TN_NUMBEROFCHANGES(100), /* 100 */
    TN_MOREDATA(101), TN_METINF_MAXOBJSIZE(102), TN_DEVINF_UTC(103), TN_DEVINF_NOFM(104), TN_DEVINF_LARGEOBJECT(105);

    private int value;

    XltTagID_t(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
