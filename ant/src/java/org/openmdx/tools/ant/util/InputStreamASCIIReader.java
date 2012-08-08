/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InputStreamASCIIReader.java,v 1.2 2005/08/24 20:17:02 hburger Exp $
 * Description: InputStream ASCII Reader
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/08/24 20:17:02 $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * InputStream ASCII Reader
 * <p>
 * This <code>InputStream</code> <code>Reader</code> is able to read ASCII
 * characters encoded in any of  the following formats provided the stream 
 * does not contain a byte order mark or any other non-ASCII character up
 * the position it is read through the <code>InputStreamASCIIReader</code>.
 * <ul>
 * <li>US-ASCII
 * <li>ISO-8859-1
 * <li>UTF-8
 * <li>UTF-16
 * <li>UTF-32
 * </ul>
 * The <code>InputStreamASCIIReader</code> is designed not to read ahead.
 */
public class InputStreamASCIIReader extends Reader {

	/**
	 * Constructor
	 * 
	 * @param source
	 */
	public InputStreamASCIIReader(
		InputStream in
	) {
		this.in = in;
	}

	/**
	 * 
	 */
	protected InputStream in;

	/**
	 * 
	 */
	private int prefix0 = -1;
	
	/**
	 * 
	 */
	private int suffix0 = -1;
	
	
	//------------------------------------------------------------------------
	// Extends Reader
	//------------------------------------------------------------------------

	/**
	 * Close disconnects the reader from the underlaying 
	 * <code>InputStream</code> rather than closing it.
	 * 
	 * @exception IOException
	 */
	public void close() throws IOException {
		this.in = null;
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[], int, int)
	 */
	public int read(
		char[] cbuf, 
		int off, 
		int len
	) throws IOException {
		for(
			int i = 0;
			i < len;
			i++
		){
			int c = read();
			if(c < 0) return i == 0 ? -1 : i;
			cbuf[i + off] = (char) c;
		}
		return len;
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read()
	 */
	public int read(
	) throws IOException {
		if(isEncodingKnown()){
			for(int i = prefix0; i > 0; i--) this.in.read();
			int c = this.in.read();
			for(int i = suffix0; i > 0; i--) this.in.read();
			return c > 127 ? 0 : c;
		} else if (prefix0 < 0) { // prefix0 unkown
			for(
				int i = 0;
				i < 4;
				i++
			){
				int c = this.in.read();
				if(c != 0) {
					this.prefix0 = i;
					if(i > 0) this.suffix0 = 0;
					return c;
				}
			}
		} else { // suffix0 unkown
			for(
				int i = 0;
				i < 4;
				i++
			){
				int c = this.in.read();
				if(c != 0) {
					this.suffix0 = i;
					return c;
				}
			}
		}
		return 0;
	}

	/**
	 * Tells whether the encoding is already konwn
	 * 
	 * @return true if the encoding is already konwn
	 */
	private boolean isEncodingKnown(){
		return prefix0 >= 0 && suffix0 >= 0;
	}
	
	/**
	 * Determine the maximal number of bytes per character
	 * 
	 * @return the maximal number of bytes per character
	 */
	private int maxBytesPerCharacter(
	){
		return isEncodingKnown() ? 1 + prefix0 + suffix0 : 4;
	}
	
	/* (non-Javadoc)
	 * @see java.io.Reader#mark(int)
	 */
	public void mark(int readAheadLimit) throws IOException {
		this.in.mark(
			readAheadLimit * maxBytesPerCharacter()
		);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#markSupported()
	 */
	public boolean markSupported() {
		return this.in.markSupported();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#reset()
	 */
	public void reset() throws IOException {
		this.in.reset();
	}
	
}
