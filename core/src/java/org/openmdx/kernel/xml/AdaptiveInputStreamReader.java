/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AdaptiveInputStreamReader.java,v 1.3 2007/05/27 03:00:19 hburger Exp $
 * Description: Adaptive InputStream Reader
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/27 03:00:19 $
 * ====================================================================
 *
 * This software is published under the BSD license  as listed below.
 * 
 * Copyright (c) 2005-2007, OMEX AG, Switzerland
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * The Adaptive Input Stream Reader has the following encoding priorities<ol>
 * <li>The constructor's <code>encoding</code> argument
 * <li>A byte order mark
 * <li>An XML declaration's encoding attribute
 * <li>The platform's default encoding
 * </ol>
 */
public class AdaptiveInputStreamReader extends Reader {
	
	/**
	 * Constructor
	 * 
	 * @param in
	 * @param encoding; may be <code>null</code>
	 * @param byteOrderMarkAware 
	 * @param xmlDeclarationAware 
	 * 
	 * @throws IOException 
	 */
	public AdaptiveInputStreamReader(
		InputStream in,
		String encoding, 
		boolean byteOrderMarkAware, 
		boolean xmlDeclarationAware
	) throws IOException {
		this.encoding = encoding;
		if(byteOrderMarkAware || xmlDeclarationAware){
			InputStream stream = new BufferedInputStream(in);
			if (byteOrderMarkAware){
				this.byteOrderMark = ByteOrderMark.readByteOrderMark(stream);
				if(this.encoding == null) {
                    this.encoding = this.byteOrderMark;
                }
			}
			if(xmlDeclarationAware) {
				if(this.encoding == null) {
					this.xmlDeclaration = XMLDeclaration.readXMLDeclaration(
						stream
					); 
					if(this.xmlDeclaration != null) {
                        this.encoding = this.xmlDeclaration.getEncoding();
                    }
					this.delegate = new BufferedReader(
						this.encoding == null ? 
							new InputStreamReader(stream) : 
							new InputStreamReader(stream, this.encoding)
					);
				} else {
					this.xmlDeclaration = XMLDeclaration.readXMLDeclaration(
						this.delegate = new BufferedReader(
							new InputStreamReader(stream, this.encoding)
						)
					);
				}
			} else {
                this.delegate = new BufferedReader(
                    this.encoding == null ? 
                        new InputStreamReader(in) : 
                        new InputStreamReader(in, this.encoding)
                );
            }
		} else {
			this.delegate = new BufferedReader(
				encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding)
			);
		}
	}	
	
	/**
	 * 
	 */
	private Reader delegate = null;

	/**
	 * 
	 */
	private String encoding = null;
	
	/**
	 * 
	 */
	private String byteOrderMark = null;

	/**
	 * 
	 */
	private XMLDeclaration xmlDeclaration = null;

	/**
	 * 
	 * @return
	 */
	public String getEncoding(){
		return this.encoding;
	}

	/**
	 * Retrieve the byte order mark evaluation result.
	 * 
	 * @return the byte order makr's encoding; or <code>null</code>. 
	 */
	public String getByteOrderMark(){
		return this.byteOrderMark;
	}

	/**
	 * The AdaptiveInputStreamReader does not keep a reference to the 
	 * <code>XMLDeclaration</code> returned by this method.
	 * 
	 * @return a copy of the XML Declaration; or <code>null</code>
	 */
	public XMLDeclaration getXMLDclaration(){
		return this.xmlDeclaration == null ? null : new XMLDeclaration(this.xmlDeclaration);
	}

	
	/* (non-Javadoc)
	 * @see java.io.Reader#mark(int)
	 */
	public void mark(int readAheadLimit) throws IOException {
		delegate.mark(readAheadLimit);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#markSupported()
	 */
	public boolean markSupported() {
		return delegate.markSupported();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read()
	 */
	public int read() throws IOException {
		return delegate.read();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[], int, int)
	 */
	public int read(char[] cbuf, int off, int len) throws IOException {
		return delegate.read(cbuf, off, len);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[])
	 */
	public int read(char[] cbuf) throws IOException {
		return delegate.read(cbuf);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#ready()
	 */
	public boolean ready() throws IOException {
		return delegate.ready();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#reset()
	 */
	public void reset() throws IOException {
		delegate.reset();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#skip(long)
	 */
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#close()
	 */
	public void close() throws IOException {
		delegate.close();
	}

}
