/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: GlobalTokens.java,v 1.1 2010/02/27 06:12:20 hburger Exp $
 * Description: Global Tokens
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/02/27 06:12:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.wbxml;

/**
 * The following token codes are common across all document types and are 
 * present in all code spaces and all code pages.
 */
public class GlobalTokens {

    /**
     * Change the code page for the current token state. Followed by a
     * single u_int8 indicating the new code page number.
     */
    static public final short SWITCH_PAGE = 0x0;
    
    /**
     * Indicates the end of an attribute list or the end of an element.
     */
    static public final short END = 0x1;
    
    /**
     * A character entity. Followed by a mb_u_int32 encoding the
     * character entity number.
     */
    static public final short ENTITY = 0x2;
    
    /**
     * Inline string. Followed by a termstr.
     */
    static public final short STR_I = 0x3;
    
    /**
     * An unknown attribute name, or unknown tag posessing no
     * attributes or content.Followed by a mb_u_int32 that encodes
     * an offset into the string table.
     */
    static public final short LITERAL = 0x4;
    
    /**
     * Inline string document-type-specific extension token. Token is
     * followed by a termstr.
     */
    static public final short EXT_I_0 = 0x40;
    
    /**
     * Inline string document-type-specific extension token. Token is
     * followed by a termstr.
     */
    static public final short EXT_I_1 = 0x41;
    
    /**
     * Inline string document-type-specific extension token. Token is
     * followed by a termstr.
     */
    static public final short EXT_I_2 = 0x42;
    
    /**
     * Processing instruction.
     */
    static public final short PI = 0x43;
    
    /**
     * An unknown tag posessing content but no attributes.
     */
    static public final short LITERAL_C = 0x44;
    
    /**
     * Inline integer document-type-specific extension token. Token is
     * followed by a mb_u_int32.
     */
    static public final short EXT_T_0 = 0x80;
    
    /**
     * Inline integer document-type-specific extension token. Token is
     * followed by a mb_u_int32.
     */
    static public final short EXT_T_1 = 0x81;
    
    /**
     * Inline integer document-type-specific extension token. Token is
     * followed by a mb_u_int32.
     */
    static public final short EXT_T_2 = 0x82;
    
    /**
     * String table reference. Followed by a mb_u_int32 encoding a
     * byte offset from the beginning of the string table.
     */
    static public final short STR_T = 0x83;
    
    /**
     * An unknown tag posessing attributes but no content.
     */
    static public final short LITERAL_A = 0x84;
    
    /**
     * Single -byte document-type-specific extension token.
     */
    static public final short EXT_0 = 0xc0;
    
    /**
     * Single -byte document-type-specific extension token.
     */
    static public final short EXT_1 = 0xc1;
    
    /**
     * Single -byte document-type-specific extension token.
     */
    static public final short EXT_2 = 0xc2;
    
    /**
     * Opaque document-type-specific data.
     */
    static public final int OPAQUE = 0xc3;
    
    /**
     * An unknown tag posessing both attributes and content.
     */
    static public final int LITERAL_AC = 0xc4;

    
    //----------------------------------------------------------
    // Tag Modifiers
    //----------------------------------------------------------
    
    /**
     * Element has attribute(s)
     */
    public static final int ATTRIBUTE_FLAG = 0x80;
    
    /**
     * Element has content
     */
    public static final int CONTENT_FLAG = 0x40;

}
