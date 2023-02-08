/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Structure Fields Mapper
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
 * Structure Fields Mapper
 */
class StructureFieldsMapper extends CompartmentMapper {
	
	StructureFieldsMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<String, String> annotationRenderer
	){
		super( // 
			"structure-fields", "Structure Fields", "Field ", //
			ModelElement_1_0::isStructureFieldType, false, //
			pw, element, annotationRenderer, //
			"Name", "Multiplicity", "Type" //
		);
	}

	@Override
	protected void compartmentContent() {
		streamSortedElements().forEach(this::mapField);
	}

	private void mapField(ModelElement_1_0 current) {
		printLine("\t\t\t<table>");
		currentHead(current);
		printLine("\t\t\t\t<tbody class=\"uml-table-body\">");
		mapAnnotation(current);
  		printLine("\t\t\t\t\t<tr>");
		mapName(current);
		mapMultiplicity(current);
		mapType(getType(current));
		printLine("\t\t\t\t\t</tr>");
		printLine("\t\t\t\t</tbody>");
		printLine("\t\t\t</table>");
	}

	private void mapName(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t\t<td>", current.getName(), "</td>");
	}

	private void mapMultiplicity(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t\t<td>", getMultiplicity(current), "</td>");
	}

	private void mapType(ModelElement_1_0 type) {
		printLine("\t\t\t\t\t\t<td>");
		mapLink("\t\t\t\t\t\t\t", type);
		printLine("\t\t\t\t\t\t</td>");
	}

	/**
	 * All {@code Structure}s except {@code Void} have fields.
	 * That's the reason why we present {@code Void}'s empty compartment, too. 
	 * 
	 * @return {@code true}
	 */
	@Override
	protected boolean isEnabled() {
		return true;
	}

}