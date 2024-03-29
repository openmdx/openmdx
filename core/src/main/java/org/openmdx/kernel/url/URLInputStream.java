/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: URL Input Stream
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This input stream is lazily opened
 */
public class URLInputStream extends InputStream {

	public URLInputStream(
		URL url
    ){
		this.url = url;
	}
	
	private final URL url;
	
	/**
	 * 
	 */
	private InputStream getDelegate = null;

	/**
	 * Retrieve the resource identifier of this input stream
	 * 
	 * @return the resource identifier of this input stream
	 */
	public URL getURL(){
		return this.url;
	}
	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	protected synchronized InputStream getDelegate(
    ) throws IOException{
		if(this.getDelegate == null) {
			this.getDelegate = this.url.openStream();
		}
		return this.getDelegate;
	}
	
	/**
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#available()
	 */
    @Override
	public int available() throws IOException {
		return getDelegate().available();
	}

	/**
	 * @throws IOException
	 * @see java.io.InputStream#close()
	 */
    @Override
	public void close() throws IOException {
		getDelegate().close();
	}

	/**
	 * @param readlimit
	 * @see java.io.InputStream#mark(int)
	 */
    @Override
	public synchronized void mark(int readlimit) {
		try {
			getDelegate().mark(readlimit);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 * @see java.io.InputStream#markSupported()
	 */
    @Override
	public boolean markSupported() {
		try {
			return getDelegate().markSupported();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read()
	 */
    @Override
	public int read() throws IOException {
		return getDelegate().read();
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
    @Override
	public int read(byte[] b, int off, int len) throws IOException {
		return getDelegate().read(b, off, len);
	}

	/**
	 * @param b
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#read(byte[])
	 */
    @Override
	public int read(byte[] b) throws IOException {
		return getDelegate().read(b);
	}

	/**
	 * @throws IOException
	 * @see java.io.InputStream#reset()
	 */
    @Override
	public synchronized void reset() throws IOException {
		getDelegate().reset();
	}

	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see java.io.InputStream#skip(long)
	 */
    @Override
	public long skip(long n) throws IOException {
		return getDelegate().skip(n);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
    @Override
	public String toString() {
		return getClass().getName() + ": " + this.url;
	}
	
}
