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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip Sink
 */
public class ZipSink extends AbstractSink {

	public ZipSink(
		ZipOutputStream zip
	){
		this.zip = zip;
	}

	private ZipSink(
		ZipSink parent,
		String baseName
	) {
		super(parent, baseName);
		this.zip = parent.zip;
	}
	
	private final ZipOutputStream zip;
	
	@Override
	public void accept(String name, String title, ByteArrayOutputStream data) throws IOException {
		final URI entryURI = path().resolve(name);
		final String entryName = entryURI.getPath().substring(1);
		final ZipEntry entry = new ZipEntry(entryName);
		final int size = data.size();
		entry.setSize(size);
		zip.putNextEntry(entry);
		data.writeTo(zip);
		accept(entryURI, title);
	}

	@Override
	public Sink nested(String baseName) {
		return new ZipSink(this, baseName);
	}

}
