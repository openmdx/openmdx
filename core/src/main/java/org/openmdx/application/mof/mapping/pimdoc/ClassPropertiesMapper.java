/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Class Properties Mapper
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
 * Class Properties Mapper
 */
class ClassPropertiesMapper extends CompartmentMapper {
	
	ClassPropertiesMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<String, String> annotationRenderer
	){
		super(
			"class-properties", "Class Properties", "",
			null, false,
			pw, element, annotationRenderer, "Name", "Value", "Stereotype"
		);
	}

	@Override
	protected void compartmentContent() {
		printLine("\t\t\t\t<tbody class=\"uml-table-body\">");
		mapAbstract();
		mapMixIn();
		mapAspectCapability();
		printLine("\t\t\t\t</tbody>");
	}

	private void mapAbstract() {
		printLine("\t\t\t\t\t<tr>");
		printLine("\t\t\t\t\t\t<td>abstract</td>");
		mapBallotBox("\t\t\t\t\t\t", isAbstract(), null);
		printLine("\t\t\t\t\t\t<td/>");
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapMixIn() {
		printLine("\t\t\t\t\t<tr>");
		printLine("\t\t\t\t\t\t<td>mix-in</td>");
		if(isMixInClass()) {
			mapBallotBox("\t\t\t\t\t\t", true, null);
			printLine("\t\t\t\t\t\t<td>", Stereotypes.ROOT, "</td>");
		} else {
			mapBallotBox("\t\t\t\t\t\t", false, null);
			printLine("\t\t\t\t\t\t<td/>");
		}
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapAspectCapability() {
		printLine("\t\t\t\t\t<tr>");
		printLine("\t\t\t\t\t\t<td>aspect-capability</td>");
		if(isAspect()) {
			printLine("\t\t\t\t\t\t<td>Aspect</td>");
			if(isRoleClass()) {
				printLine("\t\t\t\t\t\t<td>", Stereotypes.ROLE, "</td>");
			} else {
				printLine("\t\t\t\t\t\t<td/>");
			}
		} else if (isAspectCapable()){
			printLine("\t\t\t\t\t\t<td>Core</td>");
			printLine("\t\t\t\t\t\t<td/>");
		} else {
			printLine("\t\t\t\t\t\t<td>None</td>");
			printLine("\t\t\t\t\t\t<td/>");
		}
		printLine("\t\t\t\t\t</tr>");
	}

	
	private Boolean isAbstract(){
		try {
			return element.isAbstract();
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private boolean isMixInClass() {
		return element.objGetList("stereotype").contains(Stereotypes.ROOT);
	}

	private boolean isRoleClass() {
		return element.objGetList("stereotype").contains(Stereotypes.ROLE);
	}
	
	private boolean isAspect() {
		return isSubclassOf("org:openmdx:base:Aspect");
	}
	
	private boolean isAspectCapable() {
		return isSubclassOf("org:openmdx:base:AspectCapable");
	}

	protected boolean isSubclassOf(String superClass) {
		return element
			.objGetSet("allSupertype")
			.stream()
			.map(this::getElement)
			.filter(this::excludeSelf)
			.map(ModelElement_1_0::getQualifiedName)
			.anyMatch(superClass::equals);
	}

}