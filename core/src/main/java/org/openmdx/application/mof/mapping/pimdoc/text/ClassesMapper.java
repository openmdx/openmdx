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
package org.openmdx.application.mof.mapping.pimdoc.text;

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
		Function<String, String> annotationRenderer
	){
		super(
			"classes", "Classes", "",
			ModelElement_1_0::isClassType, false,
			pw, element, annotationRenderer, "Name", "Abstract", "Mix-In"
		);
	}

	@Override
	protected void compartmentContent() {
		printLine("\t\t\t\t<tbody class=\"uml-table-body\">");
		streamSortedElements().forEach(this::mapTableRow);
		printLine("\t\t\t\t</tbody>");
	}

	private void mapTableRow(ModelElement_1_0 current) {
		try {
			printLine("\t\t\t\t\t<tr>");
			printLine("\t\t\t\t\t\t<td>");
			mapLink("\t\t\t\t\t\t\t", current);
			printLine("\t\t\t\t\t\t</td>");
			mapBallotBox("\t\t\t\t\t\t", current.isAbstract(), null);
			mapBallotBox("\t\t\t\t\t\t", isMixInClass(current), null);
			printLine("\t\t\t\t\t</tr>");
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private boolean isMixInClass(ModelElement_1_0 element) {
		return element.objGetList("stereotype").contains(Stereotypes.ROOT);
	}

}