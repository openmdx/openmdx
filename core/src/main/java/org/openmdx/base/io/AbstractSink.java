/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract Sink
 * Owner:       the original authors.
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.io;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract Sink
 */
public abstract class AbstractSink implements Sink {

	protected AbstractSink() {
		this.uri = URI.create("/");
		this.toc = new TreeMap<>();
	}

	protected AbstractSink(
		AbstractSink that,
		String child
	) {
		this.uri = that.uri.resolve(child + '/');
		this.toc = that.toc;
	}

	private final URI uri;
	
	/**
	 * The table of content
	 */
	private final Map<URI, String> toc;
	
	@Override
	public URI path() {
		return this.uri;
	}

	@Override
	public URI relativize(URI uri) {
		if(uri.isAbsolute() || uri.isOpaque()) throw new IllegalArgumentException("Relative URI required: " + uri);
		URI relativeURI = path().relativize(uri);
		final String relativePath = relativeURI.getPath();
		if(relativePath.startsWith("/")) {
			relativeURI = URI.create(path().getPath().substring(1).replaceAll("[^/]+/", "../") + relativePath.substring(1));
		}
		return relativeURI;
	}

	protected void accept(URI name, String title) {
		this.toc.put(name, title);
	}
	
	@Override
	public Map<URI, String> getTableOfContent() {
		return toc;
	}
	
}
