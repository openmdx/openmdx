/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Compartment Mapper
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
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openmdx.application.mof.mapping.pimdoc.spi.AbstractMapper;
import org.openmdx.application.mof.mapping.pimdoc.spi.NamespaceFilter;
import org.openmdx.application.mof.mapping.spi.MapperTemplate;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;

/**
 * Compartment Mapper
 */
abstract class CompartmentMapper extends MapperTemplate {

	protected CompartmentMapper(
		String compartmentId, 
		String compartmentTitle, 
		String currentTitlePrefix, 
		Predicate<ModelElement_1_0> typeFilter, 
		boolean inherit,
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<String, String> annotationRenderer, String... columnTitles
	) {
		super(pw, element.getModel(), annotationRenderer);
		this.compartmentId = compartmentId;
		this.compartmentTitle = compartmentTitle;
		this.typeFilter = typeFilter;
		this.inherit = inherit;
		this.element = element;
		this.columnTitles = columnTitles;
		this.currentTitlePrefix = currentTitlePrefix;
		this.namespaceFilter = new NamespaceFilter(element);
	}

	protected final ModelElement_1_0 element;
	protected final Predicate<ModelElement_1_0> namespaceFilter;
	protected final Predicate<ModelElement_1_0> typeFilter;
	private final boolean inherit;
	private final String compartmentId;
	private final String compartmentTitle;
	private final String currentTitlePrefix;
	private final String[] columnTitles;
	protected static final Comparator<ModelElement_1_0> ELEMENT_NAME_COMPARATOR = new ElementNameComparator();
	
	protected void compartment(boolean open) {
		if(isEnabled()) {
			printLine(
				"\t\t<details id=\"",
				this.compartmentId,
				"\" class=\"uml-",
				this.compartmentId,
				"\"",
				open ? " open" : "",
				">"
			);
			compartmentSummary();
			compartmentDetails();
			printLine("\t\t</details>");
		}
	}

	protected void compartment() {
		if(isEnabled()) {
			printLine(
				"\t\t<div id=\"",
				this.compartmentId,
				"\" class=\"uml-",
				this.compartmentId,
				"\">"
			);
			compartmentTitle();
			compartmentContent();
			printLine("\t\t</div>");
		}
	}
	

	private void compartmentTitle() {
		printLine("\t\t\t<h3>", this.compartmentTitle, "</h3>");
	}
	
	private void compartmentSummary() {
		printLine("\t\t\t<summary>", this.compartmentTitle, "</summary>");
	}

	private void compartmentDetails() {
		printLine("\t\t\t<table>");
		compartmentHead();
		compartmentContent();
		printLine("\t\t\t</table>");
	}	
	
	protected void compartmentHead() {
		printLine("\t\t\t\t<thead class=\"uml-table-head\">");
		compartmentColumnHeaders();
		printLine("\t\t\t\t</thead>");
	}

	protected void currentHead(ModelElement_1_0 current) {
		printLine("\t\t\t\t<thead class=\"uml-table-head\">");
		currentElementHeader(current);
		compartmentColumnHeaders();
		printLine("\t\t\t\t</thead>");
	}

	private void currentElementHeader(ModelElement_1_0 current) {
		printLine("\t\t\t\t\t<tr>");
		printLine(
			"\t\t\t\t\t\t<th colspan=\"",
			Integer.toString(columnTitles.length),
			"\" id=\"",
			element.getName(),
			"\">",
			this.currentTitlePrefix,
			getDisplayName(current),
			"</th>"
		);
		printLine("\t\t\t\t\t</tr>");
	}

	private void compartmentColumnHeaders() {
		printLine("\t\t\t\t\t<tr>");
		int i = 0;
		for(String columnTitle : columnTitles) {
			printLine(
				"\t\t\t\t\t\t<th class=\"uml-",
				this.compartmentId,
				"-",
				Integer.toString(++i),
				"\">",
				columnTitle, 
				"</th>"
			);
		}
		printLine("\t\t\t\t\t</tr>");
	}
	
	protected void mapLink(String indent, ModelElement_1_0 current) {
		printLine(
			"<a href=\"", 
			getHref(current),
			"\" title=\"",
			getDisplayName(current),
			"\" target=\"",
			HTMLMapper.FRAME_NAME,
			"\">",
			current.getName(),
			"</a>"
		);
	}
	
	protected ModelElement_1_0 getContainer(ModelElement_1_0 current){
		try {
			return getElement(current.getContainer());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	protected String getHref(ModelElement_1_0 current) {
    	if(current.isPackageType() || current.isClassType() || current.isStructureType()){
        	return getBaseURL() + HTMLMapper.getEntryName(current);
    	} else try {
    		final ModelElement_1_0 container = this.model.getElement(current.getContainer());
        	return getBaseURL() + HTMLMapper.getEntryName(container) + "#" + current.getName();
    	} catch (ServiceException e) {
    		throw new RuntimeServiceException(e);
    	}
	}
	
	private String getBaseURL() {
    	return AbstractMapper.getBaseURL(element.getQualifiedName());
    }

	protected String getDisplayName(ModelElement_1_0 elcurrentement) {
		return HTMLMapper.getDisplayName(elcurrentement);
	}
	
	protected ModelElement_1_0 getElement(Object objectId) {
		try {
			return super.model.getElement(objectId);
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private Predicate<ModelElement_1_0> getSupertypeFilter() {
		final Set<?> supertypes = element.objGetSet("allSupertype");
		return new Predicate<ModelElement_1_0>() {

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
	}

	protected ModelElement_1_0 getType(ModelElement_1_0 element) {
		try {
			return element.getModel().getElement(element.getType());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	protected void mapBallotBox(String indent, boolean value, ModelElement_1_0 link) {
		printLine(
			indent, 
			"<td class=\"uml-ballot-box\">", 
			HTMLMapper.renderBox(value)
		); 
		if(link != null) {
			mapLink("\t", link);
		}
		printLine("</td>");
	}
	
    protected void mapAnnotation(
    	ModelElement_1_0 element
    ) {
    	final String annotation  = (String)element.objGetValue("annotation");
		if(annotation != null && !annotation.trim().isEmpty()) {
			final String rendered = renderAnnotation(annotation);
	  		printLine("\t\t\t\t\t<tr>");
	  		printLine(
	  			"\t\t\t\t\t\t<td colspan=\"",
	  			Integer.toString(this.columnTitles.length),
	  			"\" class=\"uml-comment\"",
	  			">");
			if(rendered.contains("<pre>")) {
				print(rendered);
			} else {
				MapperUtils.wrapText("\t\t\t\t\t\t\t", rendered, this::printLine);
			}
	  		printLine("\t\t\t\t\t\t</td");
	  		printLine("\t\t\t\t\t</tr>");
		}
    }

    protected boolean excludeSelf(ModelElement_1_0 element) {
		return !element.equals(this.element);
	}
	
	protected String getMultiplicity(ModelElement_1_0 element) {
		try {
			return  ModelHelper.getMultiplicity(element).code();
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	protected Stream<ModelElement_1_0> streamTypeElements(){
		return super.model
			.getContent()
			.stream()
			.filter(namespaceFilter);
	}
	
	protected Stream<ModelElement_1_0> streamSupertypesElements(){
		return super.model
			.getContent()
			.stream()
			.filter(getSupertypeFilter());
	}
	
	protected Stream<ModelElement_1_0> streamCompartmentElements() {
		if(typeFilter == null) {
			return Stream.empty();
		}
		final Stream<ModelElement_1_0> anyElementStream = inherit ? streamSupertypesElements() : streamTypeElements();
		return anyElementStream.filter(typeFilter);
	}

	protected Stream<ModelElement_1_0> streamSortedElements() {
		return streamCompartmentElements().sorted(ELEMENT_NAME_COMPARATOR);
	}

	protected boolean isEnabled() {
		return typeFilter == null || streamCompartmentElements().findAny().isPresent();
	}

	protected abstract void compartmentContent();
	
}
