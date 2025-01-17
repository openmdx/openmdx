/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Image Frame Mapper 
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
package org.openmdx.application.mof.mapping.pimdoc.text;

import java.net.URI;

import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.mapping.pimdoc.MagicFile;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.PIMDocFileType;

/**
 * Image Frame Mapper 
 */
public class ImageFrameMapper extends HTMLMapper {

    /**
     * Constructor 
     */
    public ImageFrameMapper(
    	Sink sink, 
    	Model_1_0 model,
        URI graphvizSourceURI,
        String title,
        AnnotationFlavour annotationFlavour, 
        PIMDocConfiguration configuration
    ){
		super(sink, model, toFrameURI(graphvizSourceURI), annotationFlavour, configuration);
		this.title = title;
    }    
    
    private final String title;

    @Override
	protected String getTitle() {
		return title;
	}

	@Override
	protected void htmlBody() {
		printLine("<body class=\"image-column\">");
		columnHead();
		columnBody();
		printLine("</body>");
   }

	protected void columnHead() {
		printLine("\t<div class=\"column-head image-head\">");
		printLine("\t\t<h2>");
		printLine("\t\t\t", getTitle());
		diagramInNewTab();
		printLine("\t\t</h2>");
		printLine("\t</div>");
	}

	private void diagramInNewTab() {
		printLine(
			"\t\t\t<a href=\"", 
			getImageURI(), 
			"\" target=\"_blank\">"
		);
		printLine(
			"\t\t\t\t<img alt=\"UML\" class=\"uml-symbol\" src=\"",
			getBaseURL(),
			MagicFile.UML_SYMBOL.getFileName(MagicFile.Type.IMAGE),
			"\"/>"
		);
		printLine("\t\t\t</a>");
	}

	private void columnBody() {
		printLine("\t<div class=\"column-body image-body\">");
		printLine("\t\t${SVG}");
		printLine("\t</div>");
	}

	private static URI toFrameURI(URI graphvizSourceURI) {
		return URI.create(
			PIMDocFileType.TEXT.from(graphvizSourceURI.getPath(), PIMDocFileType.GRAPHVIZ_SOURCE)
		);
	}
	
	private String getImageURI() {
		final String entryName = getEntryName();
		return PIMDocFileType.IMAGE.from(entryName.substring(entryName.lastIndexOf('/') + 1), PIMDocFileType.TEXT);
	}

	@Override
	protected String getBaseURL() {
		final StringBuilder baseURL = new StringBuilder();
		for(long i = getEntryName().chars().filter(c -> c == '/').count(); i > 0; i--) {
			baseURL.append("../");
		}
		return baseURL.toString();
	}

}
