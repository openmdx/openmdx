/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Mapper 
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.util.Comparator;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Stereotypes;

/**
 * Package Mapper 
 */
public class PackageMapper extends HTMLMapper {

    /**
     * Constructor 
     */
    public PackageMapper(
    	Sink sink, 
        ModelElement_1_0 packageToBeExported,
        PIMDocConfiguration configuration
    ) throws ServiceException {
		super(sink, packageToBeExported, configuration);
    }    

	private final Comparator<ModelElement_1_0> elementNameComparator = new ElementNameComparator();
    
	@Override
	protected void htmlBody() {
		this.pw.println("<body class=\"uml-element\">");
		columnHead();
		columnBody();
		this.pw.println("</body>");
   }

	private void columnHead() {
		this.pw.println("\t<div class=\"column-head\">");
		this.pw.println("\t\t<h2>" + getTitle() + "</h2>");
		this.pw.println("\t</div>");
	}
	
	private void columnBody() {
		this.pw.println("\t<div class=\"column-body\">");
		annotation(element);
		classesCompartment();
		this.pw.println("\t</div>");
	}
	
	private void classesCompartment() {
		this.pw.println("\t\t<details open>");
		classesSummary();
		classesDetails();
		this.pw.println("\t\t</details");
	}

	private void classesSummary() {
		this.pw.println("\t\t\t<summary>Classes</summary>");
	}

	private void classesDetails() {
		this.pw.println("\t\t\t<table>");
		classesTableHead();
		classesTableBody();
		this.pw.println("\t\t\t</table>");
	}

	private void classesTableHead() {
		this.pw.println("\t\t\t\t<thead>");
		this.pw.println("\t\t\t\t\t<tr>");
		this.pw.println("\t\t\t\t\t\t<th>Name</th>");
		this.pw.println("\t\t\t\t\t\t<th>Abstract</th>");
		this.pw.println("\t\t\t\t\t\t<th>Mix-In</th>");
		this.pw.println("\t\t\t\t\t</tr>");
		this.pw.println("\t\t\t\t</thead>");
	}

	private void classesTableBody() {
		this.pw.println("\t\t\t\t<tbody>");
		this.model
			.getContent()
			.stream()
			.filter(ModelElement_1_0::isClassType)
			.filter(this::isLocal)
			.sorted(this.elementNameComparator)
			.forEach(this::classesTableRow);
		this.pw.println("\t\t\t\t</tbody>");
	}

	private void classesTableRow(ModelElement_1_0 element) {
		try {
			this.pw.println("\t\t\t\t\t<tr>");
			this.pw.println("\t\t\t\t\t\t<td>");
			this.pw.println("\t\t\t\t\t\t\t<a href=\"" + getElementURL(element) + "\">"  + element.getName() + "</a>");
			this.pw.println("\t\t\t\t\t\t</td>");
			this.pw.println("\t\t\t\t\t\t<td>" + element.isAbstract() + "</td>");
			this.pw.println("\t\t\t\t\t\t<td>" + isMixInClass(element) + "</td>");
			this.pw.println("\t\t\t\t\t</tr>");
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
		
	}

	private boolean isMixInClass(ModelElement_1_0 element) {
		return element.objGetList("stereotype").contains(Stereotypes.ROOT);
	}
	
	@Override
	protected String getTitle() {
		return "Package " + getDisplayName();
	}

}
