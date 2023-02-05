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
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;

/**
 * Structural Features Mapper
 */
class StructuralFeaturesMapper extends CompartmentMapper {
	
	StructuralFeaturesMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<ModelElement_1_0, String> hrefMapper
	){
		super(
			"structural-features", "Structural Features", 
			pw, element, hrefMapper,
			"Name", "Multiplicity", "Type", "Kind", "Inherited", "Changeable", "Derived"
		);
	}

	@Override
	protected void mapTableBody() {
		printLine("\t\t\t\t<tbody>");
		getStructuralFeatures().forEach(this::mapStructuralFeature);
		printLine("\t\t\t\t</tbody>");
	}

	private void mapStructuralFeature(ModelElement_1_0 element) {
		final boolean inherited = !namespaceFilter.test(element);
		if(inherited) {
			printLine(
				"\t\t\t\t\t<tr class=\"",
				getStyleClass(element),
				" ",
				getStyleClass(inherited),
				"\">"
			);
		} else {
			printLine(
				"\t\t\t\t\t<tr id=\"", 
				element.getName(), 
				"\" class=\"",
				getStyleClass(element),
				" ",
				getStyleClass(inherited),
				"\">"
			);
		}
		mapName(element, inherited);
		mapMultiplicity(element);
		mapType(getType(element));
		mapKind(element);
		mapInheritance(inherited);
		mapChangeable(element);
		mapDerived(element);
		printLine("\t\t\t\t\t</tr>");
	}

	private void mapName(ModelElement_1_0 element, final boolean inherited) {
		if(inherited) {
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

	private void mapType(ModelElement_1_0 element) {
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
	}
	
	private void mapMultiplicity(ModelElement_1_0 element) {
		printLine("\t\t\t\t\t\t<td>", getMultiplicity(element), "</td>");
	}

	private void mapKind(ModelElement_1_0 element) {
		printLine("\t\t\t\t\t\t<td>", getKind(element), "</td>");
	}

	private void mapInheritance(boolean inherited) {
		printLine("\t\t\t\t\t\t<td>", inherited, "</td>");
	}

	private void mapChangeable(ModelElement_1_0 element) {
		printLine("\t\t\t\t\t\t<td>", isChangeable(element), "</td>");
	}

	private boolean isChangeable(ModelElement_1_0 element){
		try {
			return Boolean.TRUE.equals(element.isChangeable());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private void mapDerived(ModelElement_1_0 element) {
		printLine("\t\t\t\t\t\t<td>", isDerived(element), "</td>");
	}

	private boolean isDerived(ModelElement_1_0 element){
		try {
			return Boolean.TRUE.equals(element.isDerived());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private String getStyleClass(ModelElement_1_0 element) {
		try {
			return 
				element.isReference() ? "uml-reference" :
				element.isAttributeType() ? "uml-attribute" :
				"uml-structural-feature";
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private String getKind(ModelElement_1_0 element) {
		try {
			return 
				element.isReference() ? "Reference" :
				element.isAttributeType() ? "Attribute" :
				element.getDelegate().getRecordName();
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private String getStyleClass(boolean inherited) {
		return inherited ? "uml-inherited" : "uml-declared";
	}
	
	List<ModelElement_1_0> getStructuralFeatures(){
		return getModel()
			.getContent()
			.stream()
			.filter(getSupertypeFilter())
			.filter(ModelElement_1_0::isStructuralFeatureType)
			.sorted(ELEMENT_NAME_COMPARATOR)
			.collect(Collectors.toList());
	}

	private Predicate<ModelElement_1_0> getSupertypeFilter() {
		final Set<?> supertypes = element.objGetSet("allSupertype");
		Predicate<ModelElement_1_0> membership = new Predicate<ModelElement_1_0>() {

			@Override
			public boolean test(ModelElement_1_0 t) {
				try {
					final Path container = t.getContainer();
					return container != null && supertypes.contains(container);
				} catch (ServiceException e) {
					throw new RuntimeServiceException(e);
				}
			}
			
		};
		return membership;
	}
	
}