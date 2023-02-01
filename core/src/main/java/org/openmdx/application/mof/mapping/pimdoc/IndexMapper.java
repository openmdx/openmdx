/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: index.html Mapper 
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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * index.html Mapper 
 */
public class IndexMapper extends HTMLMapper {

    /**
     * Constructor 
     */
    public IndexMapper(
    	Sink sink, 
        List<ModelElement_1_0> packagesToBeExported,
        PIMDocConfiguration configuration
    ) throws ServiceException {
		super(sink, getModel(packagesToBeExported), configuration);
		this.packagesToBeExported = packagesToBeExported;
    }    
    
    private final List<ModelElement_1_0> packagesToBeExported;
    
	private static Model_1_0 getModel(List<ModelElement_1_0> packagesToExport) {
		if(packagesToExport.isEmpty()) {
			throw new NoSuchElementException("No matching model packages to be exported");
		}
		return packagesToExport.get(0).getModel();
	}
	
	@Override
	protected void htmlBody() {
		this.pw.println("<body>");
		pageHead(getTitle());
		pageBody();
		this.pw.println("</body>");
   }

	private void pageBody() {
		this.pw.println("\t<div class=\"page-section page-body\">");
		navigationColumn();
		detailColumn();
		this.pw.println("\t</div>");
	}

	private void navigationColumn() {
		this.pw.println("\t\t<div class=\"page-column navigation-column\">");
		columnHead();
		columnBody();
		this.pw.println("\t\t</div>");
	}


	private void columnBody() {
		this.pw.println("\t\t\t<div class=\"column-body\">");
		classesCompartment();
		this.pw.println("\t\t\t</div>");
	}
	
	private void classesCompartment() {
		this.pw.println("\t\t\t\t<details open>");
		classesSummary();
		classesDetails();
		this.pw.println("\t\t\t\t</details>");
	}

	private void classesSummary() {
		this.pw.println("\t\t\t\t\t<summary class=\"navigation-summary\">Classes</summary>");
	}

	private void classesDetails() {
		getPackagesWithClasses().entrySet().forEach(this::classGroup);
	}

	private void classGroup(Map.Entry<String,SortedSet<String>> classGroup) {
		this.pw.println("\t\t\t\t\t<details class=\"uml-package-group\">");
		classGroupSummary(classGroup.getKey());
		classGroupDetails(classGroup.getValue());
		this.pw.println("\t\t\t\t\t</details>");
	}

	private void classGroupDetails(final SortedSet<String> classes) {
		classes.forEach(this::classLink);
	}
	
	private void classLink(String className) {
		this.pw.println("\t\t\t\t\t\t<p>");
		this.pw.println("\t\t\t\t\t\t\t<a href=\"" + getElementURL(className) + "\" target=\"uml-element\">" + getSimpleNameOfClass(className) + "</a>");
		this.pw.println("\t\t\t\t\t\t</p>");
	}
	
	private void classGroupSummary(String packagePattern) {
		this.pw.println("\t\t\t\t\t\t<summary>");
		if(PackagePatternComparator.isWildcardPattern(packagePattern)) {
			packageGroup(packagePattern);
		} else {
			packageLink(packagePattern);
		}
		this.pw.println("\t\t\t\t\t\t</summary>");
	}

	private void packageGroup(String packagePattern) {
		this.pw.println("\t\t\t\t\t\t\t"+ getDisplayNameOfPackagePattern(packagePattern));
	}

	private void packageLink(String qualifiedName) {
		this.pw.println("\t\t\t\t\t\t\t<a href=\"" + getElementURL(qualifiedName) + "\" target=\"uml-element\">" + getDisplayNameOfPackagePattern(qualifiedName) + "</a>");
	}

	private void columnHead() {
		this.pw.println("\t\t\t<div class=\"column-head\">");
		this.pw.println("\t\t\t\t<h2>Index</h2>");
		this.pw.println("\t\t\t</div>");
	}

	private void pageHead(final String title) {
		this.pw.println("\t<div class=\"page-section page-head\">");
		this.pw.println("\t\t<div class=\"page-column\">");
		this.pw.println("\t\t\t<a href=\"" + getFileURL(MagicFile.INDEX) + "\">");
		this.pw.println("\t\t\t\t<img src=\"" + getFileURL(MagicFile.LOGO) + "\" />");
		this.pw.println("\t\t\t</a>");
		this.pw.println("\t\t</div>");
		this.pw.println("\t\t<div class=\"page-column\">");
		this.pw.println("\t\t\t<h1>" + title + "</h1>");
		this.pw.println("\t\t</div>");
		this.pw.println("\t</div>");
	}

   /**
    * Produces the detail column frame
    */
	private void detailColumn() {
		this.pw.println("\t\t<div class=\"page-column detail-column\">");
		this.pw.println("\t\t\t<iframe name=\"uml-element\" src=\"welcome.html\"/>");
		this.pw.println("\t\t</div>");
	}
    
    private NavigationCompartmentDataCollector getPackagesWithClasses(
    ){
    	final NavigationCompartmentDataCollector navigationCompartment = new NavigationCompartmentDataCollector();
    	configuration
    		.getPackageGroups()
			.forEach(navigationCompartment::addKey);
    	if(configuration.enumeratePackages()) {
        	this.packagesToBeExported.stream()
    			.map(ModelElement_1_0::getQualifiedName)
    			.forEach(navigationCompartment::addKey);
    	}
    	this.model
    		.getContent()
    		.stream()
    		.filter(ModelElement_1_0::isClassType)
    		.map(ModelElement_1_0::getQualifiedName)
    		.forEach(navigationCompartment::addElement);
    	System.out.println("Before normalization: "+navigationCompartment);
    	navigationCompartment.normalize();
    	System.out.println("After normalization: "+ navigationCompartment);
    	return navigationCompartment;
    }
    
    private String getDisplayNameOfPackagePattern(String packagePattern) {
    	return packagePattern.replaceAll(":", "::");
    }

    private String getSimpleNameOfClass(String qualifiedName) {
    	return qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1);
    }
    
	@Override
	protected String getTitle() {
		return this.configuration.getTitle();
	}

}
