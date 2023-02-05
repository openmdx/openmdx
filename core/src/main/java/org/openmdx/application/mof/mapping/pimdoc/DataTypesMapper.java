/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Data Types Mapper
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.PrintWriter;
import java.util.function.Function;

import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Data Types Mapper
 */
class DataTypesMapper extends CompartmentMapper {
	
	DataTypesMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<ModelElement_1_0, String> hrefMapper
	){
		super(pw, element, hrefMapper);
	}

	@Override
	protected void compartment() {
		printLine("\t\t<details class=\"uml-data-types\" open>");
		mapSummary();
		mapDetails();
		printLine("\t\t</details>");
	}

	private void mapSummary() {
		printLine("\t\t\t<summary>Data Types</summary>");
	}

	private void mapDetails() {
		printLine("\t\t\t<table>");
		mapTableHead();
		mapTableBody();
		printLine("\t\t\t</table>");
	}

	private void mapTableHead() {
		printLine("\t\t\t\t<thead>");
		printLine("\t\t\t\t\t<tr>");
		printLine("\t\t\t\t\t\t<th>Name</th>");
		printLine("\t\t\t\t\t\t<th>Kind</th>");
		printLine("\t\t\t\t\t\t<th>Type</th>");
		printLine("\t\t\t\t\t</tr>");
		printLine("\t\t\t\t</thead>");
	}

	private void mapTableBody() {
		printLine("\t\t\t\t<tbody>");
		containedElements()
			.filter(ModelElement_1_0::isDataType)
			.sorted(ELEMENT_NAME_COMPARATOR)
			.forEach(this::mapTableRow);
		printLine("\t\t\t\t</tbody>");
	}

	private void mapTableRow(ModelElement_1_0 element) {
		printLine("\t\t\t\t\t<tr id=\"" + element.getName() + "\">");
		mapDataTypeName(element);
		printLine("\t\t\t\t\t\t<td>" + getKind(element) + "</td>");
		if(element.isAliasType()) {
			mapType(getType(element));
		} else {
			printLine("\t\t\t\t\t\t<td/>");
		}
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapDataTypeName(ModelElement_1_0 element) {
		if(element.isStructureType()) {
			printLine("\t\t\t\t\t\t<td>");
			printLine(
				"\t\t\t\t\t\t\t<a href=\"",
				getHref(element),
				"\" title=\"",
				getDisplayName(element),
				"\" target=\"",
				HTMLMapper.FRAME_NAME,
				"\">",
				element.getName(),
				"</a>"
			);
			printLine("\t\t\t\t\t\t</td>");
		} else {
			printLine("\t\t\t\t\t\t<td>", element.getName(), "</td>");
		}
	}
	
	private String getKind(ModelElement_1_0 element) {
		return 
			element.isAliasType() ? "alias type" :
			element.isStructureType() ? "structure type" :
			element.isPrimitiveType() ? "primitive type" :
			"other";
	}

	private void mapType(ModelElement_1_0 type) {
		printLine("\t\t\t\t\t\t<td>");
		printLine(
			"\t\t\t\t\t\t\t<a href=\"",
			getHref(type),
			"\" title=\"",
			getDisplayName(type),
			"\" target=\"",
			HTMLMapper.FRAME_NAME,
			"\">",
			type.getName(),
			"</a>"
		);
		printLine("\t\t\t\t\t\t</td>");
	}

}