/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Structural Features Mapper
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

/**
 * Structural Features Mapper
 */
class StructuralFeaturesMapper extends CompartmentMapper {
	
	StructuralFeaturesMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<String, String> annotationRenderer
	){
		super( //
			"structural-features", "Structural Features", "", //
			ModelElement_1_0::isStructuralFeatureType, true, //
			pw, element, annotationRenderer, //
			"Name", "Multiplicity", "Type", "Kind", "Inherited", "Changeable", "Derived" //
		);
	}

	@Override
	protected void compartmentContent() {
		printLine("\t\t\t\t<tbody class=\"uml-table-body\">");
		streamSortedElements().forEach(this::mapStructuralFeature);
		printLine("\t\t\t\t</tbody>");
	}

	private void mapStructuralFeature(ModelElement_1_0 current) {
		final boolean inherited = !namespaceFilter.test(current);
		printLine(
			"\t\t\t\t\t<tr class=\"",
			getStyleClass(current),
			" ",
			getStyleClass(inherited),
			"\">"
		);
		mapName(current);
		mapMultiplicity(current);
		mapType(getType(current));
		mapKind(current);
		mapInheritance(inherited, current);
		mapChangeable(current);
		mapDerived(current);
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapName(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t\t<td>");
		mapLink("\t\t\t\t\t\t\t", current);
		printLine("\t\t\t\t\t\t</td>");
	}

	private void mapType(ModelElement_1_0 type) {
		printLine("\t\t\t\t\t\t<td>");
		mapLink("\t\t\t\t\t\t\t", type);
		printLine("\t\t\t\t\t\t</td>");
	}
	
	private void mapMultiplicity(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t\t<td>", getMultiplicity(current), "</td>");
	}

	private void mapKind(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t\t<td>", getKind(current), "</td>");
	}

	private void mapInheritance(boolean inherited, ModelElement_1_0 current) {
		mapBallotBox("\t\t\t\t\t\t", inherited, inherited ? getContainer(current) : null);
	}

	private void mapChangeable(ModelElement_1_0 current) {
		mapBallotBox("\t\t\t\t\t\t", isChangeable(current), null);
	}

	private boolean isChangeable(ModelElement_1_0 current){
		try {
			return Boolean.TRUE.equals(current.isChangeable());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private void mapDerived(ModelElement_1_0 current) {
		mapBallotBox("\t\t\t\t\t\t", isDerived(current), null);
	}

	private boolean isDerived(ModelElement_1_0 current){
		try {
			return Boolean.TRUE.equals(current.isDerived());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private String getStyleClass(ModelElement_1_0 current) {
		try {
			return 
				current.isReference() ? "uml-reference" :
				current.isAttributeType() ? "uml-attribute" :
				"uml-structural-feature";
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private String getKind(ModelElement_1_0 current) {
		try {
			return 
				current.isReference() ? "Reference" :
				current.isAttributeType() ? "Attribute" :
				current.getDelegate().getRecordName();
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private String getStyleClass(boolean inherited) {
		return inherited ? "uml-inherited" : "uml-declared";
	}

}