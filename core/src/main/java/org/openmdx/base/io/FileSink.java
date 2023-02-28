/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: A File Sink
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * File Sink
 */
public class FileSink extends AbstractSink {

	public FileSink(
		File directory
	) {
		this.directory = directory;
	}

	private FileSink(
		FileSink parent,	
		String baseName
	) {
		super(parent, baseName);
		this.directory = new File(parent.directory, baseName);
	}
	
	private final File directory;
	
	@Override
	public void accept(String name, String title, ByteArrayOutputStream data) throws IOException {
		final File file = new File(getDirectory(), name);
		try (OutputStream stream = new FileOutputStream(file)) {
			data.writeTo(stream);
		}
		accept(path().resolve(name), title);
	}

	private File getDirectory() throws IOException {
		if(!this.directory.exists() && !this.directory.mkdirs() && !this.directory.exists()){
			throw Throwables.initCause(
				new IOException("Unable to create directory"),
				null,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.CREATION_FAILURE,
				new BasicException.Parameter("directory", directory)
			);
		}
		return this.directory;
	}
	
	@Override
	public Sink nested(String baseName) {
		return new FileSink(this, baseName);
	}
	
}
