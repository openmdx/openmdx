/*
 * ====================================================================
 * Project:     openMDX/SyncML, http://www.openmdx.org/
 * Name:        $Id: Ret_t.java,v 1.3 2007/03/19 01:06:47 wfro Exp $
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
package org.openmdx.syncml;

public enum Ret_t {

    SML_ERR_UNDEF(-1),
    SML_ERR_OK(0x00), // OK   
    SML_ERR_UNSPECIFIC(0x10), // unspecific error
    SML_ERR_NOT_ENOUGH_SPACE(0x11), // not enough memory to perform this operation
    SML_ERR_WRONG_USAGE(0x13), // function was called in wrong context
    SML_ERR_WRONG_PARAM(0x20), // wrong parameter
    SML_ERR_INVALID_SIZE(0x21), // param has an invalid size
    SML_ERR_INVALID_HANDLE(0x22), // if handle is invalid/unknown
    SML_ERR_INVALID_OPTIONS(0x23), // unkown or unallowed options
    SML_ERR_A_MGR_ERROR(0x1001), // a template
    SML_ERR_MGR_INVALID_INSTANCE_INFO(0x1002), // a invalid Instance Info structure is used
    SML_ERR_COMMAND_NOT_HANDLED(0x1003), // no callback function is available to handle this command
    SML_ERR_ALREADY_INITIALIZED(0x1004), // Mgr allready initialized
    SML_ERR_XLT_MISSING_CONT(0x2001), // required field content missing
    SML_ERR_XLT_BUF_ERR(0x2002), // Buffer too small 
    SML_ERR_XLT_INVAL_PCDATA_TYPE(0x2003), // Invalid (WBXML) Element Type (STR_I etc.)
    SML_ERR_XLT_INVAL_LIST_TYPE(0x2004), // Invalid List Type (COL_LIST etc.)
    SML_ERR_XLT_INVAL_TAG_TYPE(0x2005), // Invalid Tag Type (TT_BEG etc.)
    SML_ERR_XLT_ENC_UNK(0x2007), // Unknown Encoding (WBXML, XML)
    SML_ERR_XLT_INVAL_PROTO_ELEM(0x2008), // Invalid Protocol Element (ADD, Delete, ...)
    SML_ERR_MISSING_LIST_ELEM(0x2009), // Missing Content of List Elements 
    SML_ERR_XLT_INCOMP_WBXML_VERS(0x200A), // Incompatible WBXML Content Format Version
    SML_ERR_XLT_INVAL_SYNCML_DOC(0x200B), // Document does not conform to SyncML DTD 
    SML_ERR_XLT_INVAL_PCDATA(0x200C), // Invalid PCData elem (e.g. not encoded as OPAQUE data)
    SML_ERR_XLT_TOKENIZER_ERROR(0x200D), // Unspecified tokenizer error
    SML_ERR_XLT_INVAL_WBXML_DOC(0x200E), // Document does not conform to WBXML specification
    SML_ERR_XLT_WBXML_UKN_TOK(0x200F), // Document contains unknown WBXML token
    SML_ERR_XLT_MISSING_END_TAG(0x2010), // Non-empty start tag without matching end tag
    SML_ERR_XLT_INVALID_CODEPAGE(0x2011), // WBXML document uses unspecified code page
    SML_ERR_XLT_END_OF_BUFFER(0x2012), // End of buffer reached
    SML_ERR_XLT_INVAL_XML_DOC(0x2013), // Document does not conform to XML 1.0 specification
    SML_ERR_XLT_XML_UKN_TAG(0x2014), // Document contains unknown XML tag
    SML_ERR_XLT_INVAL_PUB_IDENT(0x2015), // Invalid Public Identifier
    SML_ERR_XLT_INVAL_EXT(0x2016), // Invalid Codepage Extension
    SML_ERR_XLT_NO_MATCHING_CODEPAGE(0x2017), // No matching Codepage could be found
    SML_ERR_XLT_INVAL_INPUT_DATA(0x2018), // Data missing in input structure
    SML_ERR_WSM_BUF_TABLE_FULL(0x3001), // no more empty entries in buffer table available
    SML_ERR_A_UTI_UNKNOWN_PROTO_ELEMENT(0x7001);

    private int value;

    Ret_t(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
    
}
