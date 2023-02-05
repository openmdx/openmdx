/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Mapper Template 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.mof.mapping.spi;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Optional;

import org.openmdx.base.mof.cci.Model_1_0;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Mapper Template
 */
public abstract class MapperTemplate {
    
    protected MapperTemplate(
        Writer writer,
        Model_1_0 model, 
        Optional<MarkdownConfiguration> markdownConfiguration
    ) {
        this.pw = new PrintWriter(writer);
        this.model = model;
        if(this.markdown = markdownConfiguration.isPresent()) {
    		final MutableDataSet flexmarkOptions = FlexmarkExtensions.getOptions(markdownConfiguration.get().getLinkTarget());
            this.annotationParser = Parser.builder(flexmarkOptions).build();
            this.annotationRenderer = HtmlRenderer.builder(flexmarkOptions).build();
        } else {
        	this.annotationParser = null;
        	this.annotationRenderer = null;
        }
    }
        
    protected final PrintWriter pw;
    protected final Model_1_0 model;
    protected final boolean markdown;
    private final Parser annotationParser;
    private final HtmlRenderer annotationRenderer;

    /**
     * Renders the annotation if markdown is active
     * 
     * @param annotation the provided annotation
     * 
     * @return the rendered annotation
     */
	protected String renderAnnotation(final String annotation) {
		return this.markdown && annotation != null ? annotationRenderer.render(annotationParser.parse(annotation)) : annotation;
	}

	protected void printLine(CharSequence text) {
		this.pw.println(text);
	}

	protected void newLine() {
		this.pw.println();
	}

}
