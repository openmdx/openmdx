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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openmdx.base.exception.RuntimeServiceException;
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
        boolean markdown, 
        PIMDocConfiguration configuration
    ){
		super(sink, getModel(packagesToBeExported), markdown, configuration);
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
		printLine("<body class=\"page\">");
		pageHead(getTitle());
		pageBody();
		printLine("</body>");
   }

	private void pageBody() {
		printLine("\t<div class=\"page-section page-body\">");
		navigationColumn();
		detailColumn();
		printLine("\t</div>");
	}

	private void navigationColumn() {
		printLine("\t\t<div class=\"page-column navigation-column\">");
		columnHead();
		columnBody();
		printLine("\t\t</div>");
	}


	private void columnBody() {
		printLine("\t\t\t<div class=\"column-body\">");
		navigationCompartment();
		printLine("\t\t\t</div>");
	}
	
	private void navigationCompartment() {
		printLine("\t\t\t\t<details open>");
		navigationSummary();
		navigationDetails();
		printLine("\t\t\t\t</details>");
	}

	private void navigationSummary() {
		printLine("\t\t\t\t\t<summary class=\"navigation-summary\">Package Groups</summary>");
	}

	private void navigationDetails() {
		getPackageGroups().entrySet().forEach(this::packageGroup);
	}

	private void packageGroup(Map.Entry<String,SortedSet<String>> entry) {
		printLine("\t\t\t\t\t<details class=\"uml-package-group\">");
		packageGroupSummary(entry.getKey());
		packageGroupDetails(entry.getValue());
		printLine("\t\t\t\t\t</details>");
	}

	private void packageGroupDetails(final SortedSet<String> classes) {
		classes.forEach(this::mapPackageMemberName);
	}
	
	private void mapPackageMemberName(String qualifiedName) {
		try {
			printLine("\t\t\t\t\t\t<p>");
			final ModelElement_1_0 contained = this.model.getElement(qualifiedName);
			printLine(
				"\t\t\t\t\t\t\t<a href=\"",
				getHref(contained),
				"\" title=\"",
				getDisplayName(contained),
				"\" target=\"",
				HTMLMapper.FRAME_NAME,
				"\">",
				contained.getName(),
				"</a>"
			);
			printLine("\t\t\t\t\t\t</p>");
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private void packageGroupSummary(String packagePattern) {
		printLine("\t\t\t\t\t\t<summary>");
		if(PackagePatternComparator.isWildcardPattern(packagePattern)) {
			mapPackageGroupName(packagePattern);
		} else {
			mapPackageLink(packagePattern);
		}
		printLine("\t\t\t\t\t\t</summary>");
	}

	private void mapPackageGroupName(String packagePattern) {
		printLine("\t\t\t\t\t\t\t"+ getDisplayNameOfPackageGroup(packagePattern));
	}

	private void mapPackageLink(String qualifiedName) {
		try {
			final ModelElement_1_0 packageElement = this.model.getElement(qualifiedName);
			printLine(
				"\t\t\t\t\t\t\t<a href=\"",
				getHref(packageElement),
				"\" target=\"",
				HTMLMapper.FRAME_NAME,
				"\">",
				getDisplayName(packageElement),
				"</a>"
			);
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private void columnHead() {
		printLine("\t\t\t<div class=\"column-head\">");
		printLine("\t\t\t\t<h2>Index</h2>");
		printLine("\t\t\t</div>");
	}

	private void pageHead(final String title) {
		printLine("\t<div class=\"page-section page-head\">");
		printLine("\t\t<div class=\"page-column\">");
		printLine("\t\t\t<a href=\"", getFileURL(MagicFile.INDEX), "\">");
		printLine("\t\t\t\t<img src=\"", getFileURL(MagicFile.LOGO), "\" />");
		printLine("\t\t\t</a>");
		printLine("\t\t</div>");
		printLine("\t\t<div class=\"page-column\">");
		printLine("\t\t\t<h1>", title, "</h1>");
		printLine("\t\t</div>");
		printLine("\t</div>");
	}

   /**
    * Produces the detail column frame
    */
	private void detailColumn() {
		printLine("\t\t<div class=\"page-column element-column\">");
		printLine("\t\t\t<iframe name=\"", HTMLMapper.FRAME_NAME, "\" src=\"welcome.html\"/>");
		printLine("\t\t</div>");
	}
    
    private DataCollector getPackageGroups(
    ){
    	final DataCollector navigationCompartment = new DataCollector();
    	configuration
    		.getPackageGroups()
			.forEach(navigationCompartment::addKey);
    	if(configuration.enumeratePackages()) {
        	this.packagesToBeExported.stream()
    			.map(ModelElement_1_0::getQualifiedName)
    			.forEach(navigationCompartment::addKey);
    	}
		streamElements()
    		.filter(this::isListable)
    		.map(ModelElement_1_0::getQualifiedName)
    		.forEach(navigationCompartment::addElement);
    	navigationCompartment.normalize();
    	return navigationCompartment;
    }
    
    private boolean isListable(ModelElement_1_0 element) {
    	return element.isClassType() || element.isDataType();
    }
    
    private String getDisplayNameOfPackageGroup(String packagePattern) {
    	return packagePattern.replaceAll(":", "::");
    }

    @Override
	protected String getTitle() {
		return this.configuration.getTitle();
	}

    
    /**
     * Navigation Compartment Data Collector
     */
    static class DataCollector extends TreeMap<String,SortedSet<String>> {

    	protected DataCollector() {
    		super(COMPARATOR);
    	}

    	private static final Comparator<String> COMPARATOR = new PackagePatternComparator();
    	
    	private static final long serialVersionUID = -4489160358886710466L;
    	private final Comparator<String> simpleNameComparator = new SimpleNameComparator();
    	
    	void addKey(String qualifiedName) {
    		this.computeIfAbsent(qualifiedName, key -> new TreeSet<String>(simpleNameComparator));
    	}

    	void addElement(String qualifiedName) {
    		for(Map.Entry<String,SortedSet<String>> e : entrySet()) {
    			if(isPartOfPackageGroup(e.getKey(), qualifiedName)) {
    				e.getValue().add(qualifiedName);
    			}
    		}
    	}
    	
    	boolean isPartOfPackageGroup(String packagePattern, String qualifiedName) {
    		if(PackagePatternComparator.isWildcardPattern(packagePattern)) {
    			return PackagePatternComparator.isCatchAllPattern(packagePattern) ||
    				qualifiedName.startsWith(PackagePatternComparator.removeWildcard(packagePattern) + ':');
    		} else {
    			return getPackageId(packagePattern).equals(getPackageId(qualifiedName));
    		}
    	}

    	protected String getPackageId(String qualifiedName) {
    		return qualifiedName.substring(0, qualifiedName.lastIndexOf(':'));
    	}
    	
    	void normalize() {
    		values().removeIf(Collection::isEmpty);
    	}
    	
    }
    
}
