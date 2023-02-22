/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Magic Resource
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.function.Consumer;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObjects;

/**
 * Magic Resource
 */
class MagicResource {

	MagicResource(URL url, String entryName) {
		this.url = url;
		this.entryName = entryName;
	}

	private final URL url;
	private final String entryName;

	void copyTo(Sink sink) {
		try(final ByteArrayOutputStream source = readSource()) {
			sink.accept(entryName, source.size(), new Consumer<OutputStream>() {

				@Override
				public void accept(OutputStream target) {
					try {
						source.writeTo(target);
					} catch (IOException exception) {
						throw new RuntimeServiceException(
							exception,
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.PROCESSING_FAILURE,
							"Unable to cooy resource",
							new BasicException.Parameter("source", url),
							new BasicException.Parameter("target", entryName)
						);
					}
				}
				
			});
		} catch (IOException exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.PROCESSING_FAILURE,
				"Unable to cooy resource",
				new BasicException.Parameter("source", url),
				new BasicException.Parameter("target", entryName)
			);
		}
		
	}

	/**
	 * Copies a resource to the buffer
	 *
	 * @return the buffer filled with the source's content
	 *
	 * @throws IOException in case of failure
	 */
	private ByteArrayOutputStream readSource() throws IOException {
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try(InputStream s = url.openStream()){
			BinaryLargeObjects.streamCopy(s, 0, buffer);
			return buffer;
		}
	}
	
}