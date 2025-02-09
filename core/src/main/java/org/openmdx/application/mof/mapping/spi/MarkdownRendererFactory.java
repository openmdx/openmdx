/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Markdown Renderer Factory
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
package org.openmdx.application.mof.mapping.spi;

import java.util.function.Function;
import java.util.function.Supplier;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * Markdown Renderer Factory
 */
public class MarkdownRendererFactory implements Supplier<Function<String, String>> {

	/**
	 * Constructor using the default link target {@code "_self"}
	 */
	public MarkdownRendererFactory() {
		this("_self");
	}

	/**
	 * Constructor
	 * 
	 * @param linkTarget the target tp be used for markdown links
	 */
	public MarkdownRendererFactory(
		String linkTarget
	) {
		this.linkTarget = linkTarget;
	}
	
	private final String linkTarget;

	String getLinkTarget() {
		return linkTarget;
	}

	@Override
	public Function<String, String> get() {
		final MutableDataSet flexmarkOptions = FlexmarkExtensions.getOptions(linkTarget);
        final Parser parser = Parser.builder(flexmarkOptions).build();
        final HtmlRenderer renderer = HtmlRenderer.builder(flexmarkOptions).build();
		return new MarkownRenderer(parser, renderer);
	}

}
