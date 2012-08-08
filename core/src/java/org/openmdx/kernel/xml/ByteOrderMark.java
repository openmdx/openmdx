/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ByteOrderMark.java,v 1.3 2008/09/09 14:19:59 hburger Exp $
 * Description: Byte Order Mark
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 14:19:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.kernel.xml;

import java.io.IOException;
import java.io.InputStream;

/**
 * Byte Order Mark
 * <p>
 *  The exact bytes 
 *  comprising the BOM will be whatever the Unicode character FEFF is 
 *  converted into by that transformation format. In that form, the BOM 
 *  serves to indicate both that it is a Unicode file, and which of the 
 *  formats it is in. Examples:</p>
 *  <div align="center">
 *    <center>
 *    <table border="1" cellpadding="2" cellspacing="0">
 *      <tr>
 *        <th width="50%">Bytes</th>
 *        <th width="50%">Encoding Form</th>
 *      </tr>
 *      <tr>
 *        <td width="50%">EF BB BF</td>
 *        <td width="50%">UTF-8</td>
 *      </tr>
 *      <tr>
 *        <td width="50%">00 00 FE FF</td>
 *        <td width="50%">UTF-32, big-endian</td>
 *      </tr>
 *      <tr>
 *        <td width="50%">FF FE 00 00</td>
 *        <td width="50%">UTF-32, little-endian</td>
 *      </tr>
 *      <tr>
 *        <td width="50%">FE FF</td>
 *        <td width="50%">UTF-16, big-endian</td>
 *      </tr>
 *      <tr>
 *        <td width="50%">FF FE</td>
 *        <td width="50%">UTF-16, little-endian</td>
 *      </tr>
 *    </table>
 *    </center>
 *  </div>
 */
public class ByteOrderMark {

	private ByteOrderMark() {
	    // Avoid instantiation
	}

	/**
	 * The unicode character point used as byte order mark.
	 */
	public final static char VALUE = 0xFEFF;
	
	/**
	 * Each <code>ENCODINGS</code> entry corresponds to a
	 * <code>REPRESENTATIONS</code> entry.
	 */
	final public static String[] ENCODINGS = {
		Encodings.UTF_8, 
		Encodings.UTF_32BE,
		Encodings.UTF_32LE,
		Encodings.UTF_16BE,
		Encodings.UTF_16LE		
	};

	/**
	 * Each <code>REPRESENTATIONS</code> entry corresponds to an 
	 * <code>ENCODINGS</code> entry.
	 */
	final public static byte[][] REPRESENTATIONS = new byte[][]{
		new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF},
		new byte[]{(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF},
		new byte[]{(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00},
		new byte[]{(byte)0xFE, (byte)0xFF},
		new byte[]{(byte)0xFF, (byte)0xFE}
	};

	/**
	 * Consume the input stream's byte order mark if any and return the 
	 * corresponding encoding or reset the input stream otherwise.
	 * 
	 * @param in the input stream
	 * 
	 * @return the byte order mark's encoding; or <code>null</code> in absence
	 * of a byte order mark.
	 * 
	 * @throws IOException  
	 */
	public static String readByteOrderMark(
		InputStream in
	) throws IOException {
		in.mark(4);
		byte[] head = new byte[4];
		int limit = in.read(head);
		encodings: for(
			int encoding = 0;
			encoding < REPRESENTATIONS.length;
			encoding++
		){
			byte[] bom = ByteOrderMark.REPRESENTATIONS[encoding];
			if(limit < bom.length) continue encodings;
			for(
				int j = 0;
				j < bom.length;
				j++
			) if(
				bom[j] != head[j]
			) continue encodings;
			in.reset();
			in.skip(bom.length);
			return ByteOrderMark.ENCODINGS[encoding];				
		}
		in.reset();
		return null;
	}

}
