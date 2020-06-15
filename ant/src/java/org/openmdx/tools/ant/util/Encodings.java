/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Encodings
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.tools.ant.util;

/**
 * Canonical Encoding Names for <code>java.io</code> and 
 * <code>java.lang</code> API. 
 */
public class Encodings {

	/**
	 * Eight-bit UCS Transformation Format
	 * 
	 * @since JRE 1.2
	 */
	public final static String UTF_8 = "UTF8";

	/**
	 * American Standard Code for Information Interchange
	 * 
	 * @since JRE 1.2
	 */
	public final static String US_ASCII = "ASCII";
	
	/**
	 * ISO 8859-1, Latin Alphabet No. 1
	 * 
	 * @since JRE 1.2
	 */
	public final static String ISO_8859_1 = "ISO8859_1";
		
	/**
	 * Sixteen-bit Unicode Transformation Format, big-endian byte order, with byte-order mark 
	 * 
	 * @since JRE 1.2
	 */
	public final static String UTF_16BE_WITH_BOM = "UnicodeBig";
	
	/**
	 * Sixteen-bit Unicode Transformation Format, little-endian byte order, with byte-order mark 
	 * 
	 * @since JRE 1.2
	 */
	public final static String UTF_16LE_WITH_BOM = "UnicodeLittle";
	
	/**
	 * Windows Latin-1
	 * 
	 * @since JRE 1.2
	 */
	public final static String WINDOWS_1252 = "Cp1252";
	/**
	 * Sixteen-bit Unicode Transformation Format, big-endian byte order
	 * 
	 * @since JRE 1.3
	 */
	public final static String UTF_16BE = "UnicodeBigUnmarked";
	
	/**
	 * Sixteen-bit Unicode Transformation Format, little-endian byte order
	 * 
	 * @since JRE 1.3
	 */
	public final static String UTF_16LE = "UnicodeLittleUnmarked";
	
	/**
	 * Sixteen-bit UCS Transformation Format, byte order identified by<ul> 
	 * <li>a mandatory initial byte-order mark 
	 * @since JRE 1.3
	 * </ul>
	 * <ul>
	 * <li>an optional byte-order mark 
	 * @since JRE 1.4
	 * </ul>
	 */
	public final static String UTF_16 = "UTF-16";
	
	/**
	 * Thirtytwo-bit UCS Transformation Format, byte order identified by an optional byte-order mark.
	 */
	public final static String UTF_32 = "UTF-32";
	
	/**
	 * Thirtytwo-bit UCS Transformation Format, big-endian byte order
	 */
	public final static String UTF_32BE = "UTF-32BE";

	/**
	 * Thirtytwo-bit UCS Transformation Format, little-endian byte order
	 */
	public final static String UTF_32LE = "UTF-32LE";
	
}
