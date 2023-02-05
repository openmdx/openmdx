/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Classes Mapper
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

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Stereotypes;

/**
 * Classes Mapper
 */
class ClassesMapper extends CompartmentMapper {
	
	ClassesMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<ModelElement_1_0, String> hrefMapper
	){
		super(pw, element, hrefMapper);
	}

	@Override
	protected void compartment() {
		printLine("\t\t<details class=\"uml-classes\" open>");
		mapSummary();
		mapDetails();
		printLine("\t\t</details>");
	}

	private void mapSummary() {
		printLine("\t\t\t<summary>Classes</summary>");
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
		printLine("\t\t\t\t\t\t<th>Abstract</th>");
		printLine("\t\t\t\t\t\t<th>Mix-In</th>");
		printLine("\t\t\t\t\t</tr>");
		printLine("\t\t\t\t</thead>");
	}

	private void mapTableBody() {
		printLine("\t\t\t\t<tbody>");
		containedElements()
			.filter(ModelElement_1_0::isClassType)
			.sorted(ELEMENT_NAME_COMPARATOR)
			.forEach(this::mapTableRow);
		printLine("\t\t\t\t</tbody>");
	}

	private void mapTableRow(ModelElement_1_0 element) {
		try {
			printLine("\t\t\t\t\t<tr>");
			printLine("\t\t\t\t\t\t<td>");
			printLine(
				"\t\t\t\t\t\t\t<a "
				+ "href=\"" + getHref(element) + "\" " 
				+ "title=\"" + getDisplayName(element) + "\">" 
				+ element.getName() + "</a>");
			printLine("\t\t\t\t\t\t</td>");
			printLine("\t\t\t\t\t\t<td>" + element.isAbstract() + "</td>");
			printLine("\t\t\t\t\t\t<td>" + isMixInClass(element) + "</td>");
			printLine("\t\t\t\t\t</tr>");
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private boolean isMixInClass(ModelElement_1_0 element) {
		return element.objGetList("stereotype").contains(Stereotypes.ROOT);
	}

}