/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Album Mapper
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
package org.openmdx.application.mof.mapping.pimdoc.text;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.spi.PIMDocFileType;

/**
 * Album Mapper
 */
public class AlbumMapper extends CompartmentMapper {
	
	AlbumMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<String, String> annotationRenderer,
		Map<String,String> diagrams
	){
		super(
			COMPARTMENT_ID, "Diagrams", "",
			null, false,
			pw, element, annotationRenderer
		);
		this.diagrams = diagrams;
	}

	private final Map<String,String> diagrams;
	private static final int COLUMNS = 4;
	public static final String COMPARTMENT_ID = "diagram-album";
	
	protected void compartmentHead() {
		// none
	}
	
	@Override
	protected void compartmentContent() {
		printLine("\t\t\t\t<tbody class=\"uml-table-body\">");
		mapDiagrams();
		printLine("\t\t\t\t</tbody>");
	}

	private void mapDiagrams() {
		final int rows = (diagrams.size() + COLUMNS - 1) / COLUMNS;
		final Iterator<Entry<String, String>> i = this.diagrams.entrySet().iterator();
		for(int r = 0; r < rows; r++) {
			printLine("\t\t\t\t\t<tr class=\"album-row\">");
			for(int c = 0; c < COLUMNS; c++) {
				if(i.hasNext()) {
					final Entry<String, String> e = i.next();
					printLine("\t\t\t\t\t\t<td class=\"album-cell\" title=\"", e.getValue(), "\">");
					mapDiagram(e.getKey(), e.getValue());
					printLine("\t\t\t\t\t\t</td>");
				} else {
					printLine("\t\t\t\t\t\t<td/>");
				}
			}
			printLine("\t\t\t\t\t</tr>");
		}
	}

	private void mapDiagram(String sourceName, String title) {
		final String textName = PIMDocFileType.TEXT.from(sourceName, PIMDocFileType.GRAPHVIZ_SOURCE);
		final String imageName = PIMDocFileType.IMAGE.from(sourceName, PIMDocFileType.GRAPHVIZ_SOURCE);
		printLine(
			"\t\t\t\t\t\t\t<a href=\"", 
			textName, 
			"\" target=\"",
			HTMLMapper.FRAME_NAME,
			"\">"
		);
		printLine("\t\t\t\t\t\t\t\t<img class=\"album-thumbnail\" src=\"", imageName, "\" alt=\"", title, "\">");
		printLine("\t\t\t\t\t\t\t</a>");
	}

	@Override
	protected boolean isEnabled() {
		return !this.diagrams.isEmpty();
	}

}