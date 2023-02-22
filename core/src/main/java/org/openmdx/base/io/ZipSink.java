/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Zip Sink
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

import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Zip Sink
 */
public class ZipSink implements Sink {

	public ZipSink(
		ZipOutputStream zip
	) {
		this(zip, "");
	}

	private ZipSink(
		ZipOutputStream zip,
		String prefix
	) {
		this.zip = zip;
		this.prefix = prefix;
	}
	
	private final ZipOutputStream zip;
	private final String prefix;
	
	@Override
	public void accept(String name, long length, Consumer<OutputStream> streamer) {
		final String entryName = newName(name);
		try {
			final ZipEntry entry = new ZipEntry(entryName);
			entry.setSize(length);
			zip.putNextEntry(entry);
			streamer.accept(zip);
		} catch (Exception exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.MEDIA_ACCESS_FAILURE,
				"Unable to add entry to zip file",
				new BasicException.Parameter("entry-name", entryName)
			);
		}
	}

	private String newName(String suffix) {
		return this.prefix + '/' + suffix;
	}
	
	@Override
	public Sink nested(String suffix) {
		return new ZipSink(this.zip, newName(suffix));
	}

}
