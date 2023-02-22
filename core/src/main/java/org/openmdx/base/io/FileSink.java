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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * File Sink
 */
public class FileSink implements Sink {

	public FileSink(
		File directory
	) {
		this.directory = directory;
	}

	private final File directory;
	
	@Override
	public void accept(String name, long length, Consumer<OutputStream> streamer) {
		final File file = getFile(name);
		try (OutputStream stream = getStream(file)) {
			streamer.accept(stream);
		} catch (IOException exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.MEDIA_ACCESS_FAILURE,
				"Unable to create file",
				new BasicException.Parameter("file", file)
			);
		}
	}

	private OutputStream getStream(File file) {
		try {
			return new FileOutputStream(file);
		} catch (Exception ioException) {
			throw new RuntimeServiceException(
				ioException,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.CREATION_FAILURE,
				"Unable to create file",
				new BasicException.Parameter("file", file)
			);
		}
	}
	
	private File getFile(String name) {
		return new File(getDirectory(), name);
	}

	private File getDirectory() {
		if(!this.directory.exists() && !this.directory.mkdir() && !this.directory.exists()){
			throw new RuntimeServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.CREATION_FAILURE,
				"Unable to create directory",
				new BasicException.Parameter("directory", directory)
			);
		}
		return this.directory;
	}
	
	@Override
	public Sink nested(String base) {
		return new FileSink(new File(this.directory, base));
	}

}
