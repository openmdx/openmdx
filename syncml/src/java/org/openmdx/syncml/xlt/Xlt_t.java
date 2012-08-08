/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: Xlt_t.java,v 1.4 2007/04/02 00:56:05 wfro Exp $
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

public class Xlt_t {
    
    // byte for WBXML String Table Length - not yet implemented yet -> 0x00
    public static int XLT_STABLEN = 0x00;

    // byte for WBXML charset - not yet implemented - default UTF-8
    public static byte XLT_CHARSET = (byte)0x6A;
    
    // byte for WBXML Version Number
    public static byte XLT_WBXMLVER = (byte)0x02;
    
    // byte to add to a tag if a content follows
    public static byte XLT_CONTBYTE = (byte)0x40;
    
    // byte to add to a tag if an attribute follows
    public static byte XLT_ATTRBYTE = (byte)0x80;
    
    // termination character for certain WBXML element types (e.g. STR_I)
    public static byte XLT_TERMSTR = (byte)0x00;
    
    // public identifier 0x00,0x00 -> unknown, use stringtable
    public static byte XLT_PUBIDENT1 = (byte)0x00;
    public static byte XLT_PUBIDENT2 = (byte)0x00;
    
    // %%% luz: 2003-07-31: now in xltenc.c's SyncMLFPI table
    //#define XLT_DTD_ID "-//SYNCML//DTD SyncML 1.0//EN"
    
    // switch page tag 0x00
    public static byte XLT_SWITCHPAGE = 0x00;
    
    // default codepage
    public static byte XLT_DEFAULTCODEPAGE = 0x00;    

    // byte for XML tag begin parentheses
    public static final char XML_BEGPAR = '<';
    
    // byte for XML tag end parentheses
    public static final char XML_ENDPAR = '>';
    
    // byte for XML tag del
    public static final char XML_TAGDEL = '/';
    
    // XML version
    public static final String XML_VERSION = "1.0";
    
    // XML encoding
    public static final String XML_ENCODING = "UTF-8";
    
    // XML namespaceattribute
    public static final String XML_NSSTART = " xmlns='";
    public static final String XML_NSEND = "'";
    
}
