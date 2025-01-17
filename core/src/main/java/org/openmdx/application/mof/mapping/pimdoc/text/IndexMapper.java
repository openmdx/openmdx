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
package org.openmdx.application.mof.mapping.pimdoc.text;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.mapping.pimdoc.MagicFile;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.application.mof.mapping.pimdoc.spi.PackageGroupComparator;
import org.openmdx.application.mof.repository.accessor.NamespaceLocation;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.PIMDocFileType;

/**
 * index.html Mapper
 */
public class IndexMapper extends HTMLMapper {

    /**
     * Constructor
     */
    public IndexMapper(
		Sink sink, 
		Model_1_0 model, 
		AnnotationFlavour annotationFlavour, 
		PIMDocConfiguration configuration
	) {
        super(sink, model, MagicFile.TABLE_OF_CONTENT, annotationFlavour, configuration);
    }

	private Stream<PackageGroup> streamPackageGroups() {
		return new PackageGroupStreamBuilder(streamTableOfContent())
			.withDiagrams(streamDiagrams())
			.withClassesAndDataTypes(streamClassesAndDataTypes())
			.build();
	}

    private Stream<String> streamTableOfContent() {
        final Collection<String> tableOfContentEntries = configuration.getTableOfContentEntries();
        return tableOfContentEntries.isEmpty() ? streamTableOfContentDefault() : tableOfContentEntries.stream();
    }

	private Stream<String> streamTableOfContentDefault() {
		return Stream.concat(
        	streamElements()
            	.filter(ModelElement_1_0::isPackageType)
            	.map(ModelElement_1_0::getQualifiedName),
            Stream.of(
            	PackageGroupComparator.getCatchAllPattern()
            )
        );
	}

    private Stream<Map.Entry<URI, String>> streamDiagrams() {
    	return sink
    		.getTableOfContent()
    		.entrySet()
    		.stream()
    		.filter(this::isGraphvizSource);
    }

    private Stream<String> streamClassesAndDataTypes() {
        return streamElements()
        	.filter(element -> element.isClassType() || element.isDataType())
            .map(ModelElement_1_0::getQualifiedName);
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
        frameColumn();
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
    	streamPackageGroups().forEach(this::packageGroup);
    }

    private boolean isGraphvizSource(Map.Entry<URI, String> candidate) {
    	return PIMDocFileType.GRAPHVIZ_SOURCE.test(candidate.getKey().getPath());
    }

    private void packageGroup(final PackageGroup packageGroup) {
        printLine("\t\t\t\t\t<details class=\"uml-package-group\">");
        packageGroupSummary(packageGroup.getPackageClusterKey());
    	packageGroupDetails(packageGroup);
        printLine("\t\t\t\t\t</details>");
    }

	private void packageGroupDetails(final PackageGroup packageGroup) {
		if(packageGroup.hasClassesOrDataTypes()) {
    		packageGroupClassesAndDataTypes(packageGroup);
    	}
    	if(packageGroup.hasDiagrams()) {
	        packageGroupDiagrams(packageGroup);
    	}
	}

    private void packageGroupClassesAndDataTypes(PackageGroup packageGroup) {
        printLine("\t\t\t\t\t\t<details class=\"uml-class-group\" open>");
        printLine("\t\t\t\t\t\t\t<summary>Classes &amp; Data Types</summary>");
        packageGroup.forEachClassOrDataType(this::mapClassOrDataTypeLink);
        printLine("\t\t\t\t\t\t</details>");
    }

    private void packageGroupDiagrams(PackageGroup packageGroup) {
        printLine("\t\t\t\t\t\t<details class=\"uml-diagram-group\" open>");
        printLine("\t\t\t\t\t\t\t<summary>Diagrams</summary>");
        packageGroup.forEachDiagram(this::mapDiagramLink);
        printLine("\t\t\t\t\t\t</details>");
    }

    private void mapDiagramLink(String sourcePath, String diagramData) {
        final String textName = PIMDocFileType.TEXT.from(sourcePath, PIMDocFileType.GRAPHVIZ_SOURCE);
        printLine("\t\t\t\t\t\t\t<div>");
        printLine("\t\t\t\t\t\t\t\t<a href=\"", textName, "\" target=\"", HTMLMapper.FRAME_NAME, "\">", diagramData, "</a>");
        printLine("\t\t\t\t\t\t\t</div>");
    }

    private void mapClassOrDataTypeLink(String qualifiedName, String simpleName) {
        printLine("\t\t\t\t\t\t\t<div>");
		final ModelElement_1_0 contained = getElement(qualifiedName);
		printLine("\t\t\t\t\t\t\t\t<a href=\"", getHref(contained), "\" title=\"", getDisplayName(contained),
		        "\" target=\"", HTMLMapper.FRAME_NAME, "\">", contained.getName(), "</a>");
		printLine("\t\t\t\t\t\t\t</div>");
    }

    private void packageGroupSummary(String packagePattern) {
        printLine("\t\t\t\t\t\t<summary>");
        if (PackageGroupComparator.isWildcardPattern(packagePattern)) {
            mapPackageClusterLink(packagePattern);
        } else {
            mapPackageLink(packagePattern);
        }
        printLine("\t\t\t\t\t\t</summary>");
    }

    private void mapPackageClusterLink(String packagePattern) {
        final String namespace = PackageGroupComparator.removeWildcard(packagePattern);
        printLine("\t\t\t\t\t\t\t<a href=\"", getBaseURL(), NamespaceLocation.getLocation(namespace),
                MagicFile.PACKAGE_CLUSTER.getFileName(MagicFile.Type.TEXT), "\" target=\"", HTMLMapper.FRAME_NAME,
                "\">", packagePattern.replace(":", "::"), "</a>");
    }

    private void mapPackageLink(String qualifiedName) {
        final ModelElement_1_0 packageElement = getElement(qualifiedName);
		printLine("\t\t\t\t\t\t\t<a href=\"", getHref(packageElement), "\" target=\"", HTMLMapper.FRAME_NAME, "\">",
		        getDisplayName(packageElement), "</a>");
		printLine("\t\t\t\t\t\t\t<a href=\"", getHref(packageElement), "#", AlbumMapper.COMPARTMENT_ID,
		        "\" target=\"", HTMLMapper.FRAME_NAME, "\">");
		printLine("\t\t\t\t\t\t\t\t<img alt=\"UML\" class=\"uml-symbol\" src=\"",
		        MagicFile.UML_SYMBOL.getFileName(MagicFile.Type.IMAGE), "\"/>");
		printLine("\t\t\t\t\t\t\t</a>");
    }

    private void columnHead() {
        printLine("\t\t\t<div class=\"column-head\">");
        printLine("\t\t\t\t<h2>Index</h2>");
        printLine("\t\t\t</div>");
    }

    private void pageHead(final String title) {
        printLine("\t<div class=\"page-section page-head\">");
        printLine("\t\t<div class=\"page-column\">");
        printLine("\t\t\t<a href=\"", configuration.getTargetName(MagicFile.WELCOME_PAGE, MagicFile.Type.TEXT),
                "\" target=\"", HTMLMapper.FRAME_NAME, "\">");
        printLine("\t\t\t\t<img src=\"", configuration.getTargetName(MagicFile.WELCOME_PAGE, MagicFile.Type.IMAGE),
                "\"/>");
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
    private void frameColumn() {
        printLine("\t\t<div class=\"page-column frame-column\">");
        printLine("\t\t\t<iframe name=\"", HTMLMapper.FRAME_NAME, "\" src=\"",
                configuration.getTargetName(MagicFile.WELCOME_PAGE, MagicFile.Type.TEXT), "\"/>");
        printLine("\t\t</div>");
    }

    @Override
    protected String getTitle() {
        return this.configuration.getTitle();
    }

    @Override
    protected String getBaseURL() {
        return "";
    }

}
