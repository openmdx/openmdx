/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Abstract Mapper 
 * Owner: the original authors. 
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
package org.openmdx.application.mof.mapping.pimdoc.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.application.mof.mapping.spi.MapperTemplate;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Abstract Mapper
 */
public abstract class AbstractMapper extends MapperTemplate implements Archiving {

	private AbstractMapper(
		Sink sink, 
		Model_1_0 model, 
		boolean markdown, 
		PIMDocConfiguration configuration, 
		ByteArrayOutputStream stream
	) {
		super(
			new OutputStreamWriter(stream, StandardCharsets.UTF_8), 
			model, 
			markdown ? configuration.getMarkdownRendererFactory().instantiate() : Function.identity()
		);
		this.sink = sink;
		this.buffer = stream;
		this.configuration = configuration;
	}
	
	protected AbstractMapper(
		Sink sink, 
		Model_1_0 model, 
		boolean markdown, 
		PIMDocConfiguration configuration
	){
		this(sink, model, markdown, configuration, new ByteArrayOutputStream());
	}

	protected final Sink sink;
	protected final PIMDocConfiguration configuration;
	private final ByteArrayOutputStream buffer;
	
	@Override
	public void close() throws RuntimeServiceException {
		try {
			flush();
			sink.accept(getEntryName(), getTitle(), buffer);
		} catch (IOException exception) {
			throw new RuntimeServiceException(exception);
		}
	}

	public static String getBaseURL(final String qualifiedName) {
		final StringBuilder baseDir = new StringBuilder();
		for(long i = qualifiedName.chars().filter(c -> c == ':').count(); i > 0L; i--) {
    		baseDir.append("../");
    	}
    	return baseDir.toString();
	}
	
    protected abstract String getEntryName();
    protected abstract String getTitle();
    
}
