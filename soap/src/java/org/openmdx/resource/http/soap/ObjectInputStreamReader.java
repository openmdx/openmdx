/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: ObjectInputStreamReader.java,v 1.2 2007/03/22 15:32:53 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:32:53 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland; France Telecom, France
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 */
package org.openmdx.resource.http.soap;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Class used to block the xml streamreader before e finds the SoapMEssage
 * Footer.
 */
public class ObjectInputStreamReader extends FilterReader {
	public ObjectInputStreamReader(Reader reader) {
		super(reader);
		this.reader = new BufferedReader(reader);
	}

	private void assertBuffer() throws IOException {
		if (this.eof)
			return;
		if ((this.buffer == null) || (this.pos >= this.buffer.length())) {
			this.buffer = readLine();
			this.pos = 0;
		}
		this.eof = "</object-stream>".equalsIgnoreCase(this.buffer.trim());
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		throw new UnsupportedOperationException();
	}

	public String readLine() throws IOException {
		return ((BufferedReader) reader).readLine();
	}

	@Override
	public boolean markSupported() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read() throws IOException {
		this.assertBuffer();
		if (this.eof) {
			return -1;
		} else {
			return this.buffer.charAt(this.pos);
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		this.assertBuffer();
		if (this.eof) {
			return -1;
		} else {
			len = java.lang.Math.min(this.buffer.length() - this.pos, len);
			System.arraycopy(this.buffer.toCharArray(), this.pos, cbuf, off,
					len);
			this.pos += len;
			return len;
		}
	}

	@Override
	public boolean ready() throws IOException {
		this.assertBuffer();
		return !this.eof;
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void close() {
	}

	private boolean eof = false;

	private String buffer = null;

	private int pos = -1;

	private final Reader reader;
}
