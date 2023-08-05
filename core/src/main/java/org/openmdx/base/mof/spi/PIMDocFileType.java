/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: PIMDoc File Type
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.mof.spi;

import java.util.function.Predicate;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * PIMDoc File Type
 */
public enum PIMDocFileType implements Predicate<String> {

	/**
	 * File ending of the HTML 5 files
	 */
	TEXT(".html"),
	
	/**
	 * File ending for the Graphviz Template Files
	 */
	GRAHVIZ_TEMPLATE(".dott"),
	
	/**
	 * File ending for the Graphviz Source Files
	 */
	GRAPHVIZ_SOURCE(".dot"),

	/**
	 * File ending for the SVG Graphics
	 */
	IMAGE(".svg");

	PIMDocFileType(String extension) {
		this.extension = extension;
	}

	/**
	 * The extension (including the leading dot).
	 */
	private final String extension;
	
	public String extension() {
		return extension;
	}


	@Override
	public boolean test(String uri) {
		return uri.endsWith(extension);
	}

	public String from(String uri, PIMDocFileType type) {
		if(type.test(uri)) {
			return uri.substring(0, uri.length() - type.extension.length()) + this.extension();
		} else {
			throw new RuntimeServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.BAD_PARAMETER,
				"The path should end with the expected extension",
				new BasicException.Parameter("expected-extension", type.extension),
				new BasicException.Parameter("uri", uri)
			);
		}
	}
	
}
