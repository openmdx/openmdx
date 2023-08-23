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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.openmdx.application.mof.mapping.pimdoc.MagicFile;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.application.mof.mapping.pimdoc.spi.PackageGroupBuilder;
import org.openmdx.application.mof.mapping.pimdoc.spi.PackagePatternComparator;
import org.openmdx.application.mof.repository.accessor.NamespaceLocation;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.PIMDocFileType;

/**
 * index.html Mapper
 */
public class IndexMapper extends HTMLMapper {

    private final SortedMap<String, String> catchAllPkgDiagramIndexData = new TreeMap<>();
    private final SortedMap<String, SortedMap<String, String>> wildcardPkgDiagramIndexData = new TreeMap<>();
    private final SortedMap<String, SortedMap<String, String>> standardPkgDiagramIndexData = new TreeMap<>();

    /**
     * Constructor
     */
    public IndexMapper(Sink sink, Model_1_0 model, boolean markdown, PIMDocConfiguration configuration) {
        super(sink, model, MagicFile.TABLE_OF_CONTENT, markdown, configuration);
        loadDiagramIndexData();
    }

    private void loadDiagramIndexData() {
        this.streamElements().filter(ModelElement_1_0::isPackageType).forEach(this::putIntoMapSingleDiagramData);
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
        getPackageGroups().entrySet().forEach(this::packageCluster);
    }

    private void putIntoMapSingleDiagramData(ModelElement_1_0 element) {
        final URI directory;
        try {
            directory = new URI("/" + element.getModel().toJavaPackageName(element, null).replace('.', '/') + '/');
            for (final Map.Entry<URI, String> e : sink.getTableOfContent().entrySet()) {
                if (e.getKey().getPath().contains(directory.getPath())) {
                    final String p = e.getKey().getPath().substring(1);
                    if (PIMDocFileType.GRAPHVIZ_SOURCE.test(p) && p.indexOf('/') > -1) {
                        final String qualifiedName = element.getQualifiedName();
                        SortedMap<String, String> diagramDataMap = standardPkgDiagramIndexData.get(qualifiedName);
                        if (diagramDataMap == null) {
                            diagramDataMap = new TreeMap<>();
                        }
                        diagramDataMap.put(p, e.getValue());
                        standardPkgDiagramIndexData.put(qualifiedName, diagramDataMap);
                        // all diagrams to be listed under the catch-all ("**") package group
                        catchAllPkgDiagramIndexData.putAll(diagramDataMap);
                    }
                }
            }

        } catch (URISyntaxException | ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    private void packageCluster(Map.Entry<String, SortedSet<String>> entry) {
        printLine("\t\t\t\t\t<details class=\"uml-package-cluster\">");
        packageGroupSummary(entry.getKey());
        outputClasses(entry.getValue());
        outputDiagrams(entry.getKey());
        printLine("\t\t\t\t\t</details>");
    }

    private void outputClasses(final SortedSet<String> classes) {
        printLine("\t\t\t\t\t\t<details class=\"uml-package-cluster class-section\">");
        printLine("\t\t\t\t\t\t\t<summary>Classes</summary>");
        printLine("\t\t\t\t\t\t\t<div>");
        packageGroupClassDetails(classes);
        printLine("\t\t\t\t\t\t\t</div>");
        printLine("\t\t\t\t\t\t</details>");
    }

    private void outputDiagrams(final String packageClusterKey) {
        printLine("\t\t\t\t\t\t<details class=\"uml-package-cluster diagram-section\">");
        printLine("\t\t\t\t\t\t\t<summary>Diagrams</summary>");
        printLine("\t\t\t\t\t\t\t<div>");
        packageGroupDiagramDetails(packageClusterKey);
        printLine("\t\t\t\t\t\t\t</div>");
        printLine("\t\t\t\t\t\t</details>");
    }

    private void packageGroupDiagramDetails(final String packageClusterKey) {
        if (packageClusterKey.equals("**")) {
            mapPackageDiagrams(catchAllPkgDiagramIndexData);
            return;
        }
        SortedMap<String, String> wildcardDiagramMap = null;
        for (Map.Entry<String, SortedMap<String, String>> e : this.standardPkgDiagramIndexData.entrySet()) {
            if (packageClusterKey.equals(e.getKey())) {
                mapPackageDiagrams(e.getValue());
            } else if (packageClusterKey.endsWith("**") && e.getKey().contains(packageClusterKey.substring(0, packageClusterKey.length() - 3))) {
                wildcardDiagramMap = wildcardPkgDiagramIndexData.get(packageClusterKey);
                if (wildcardDiagramMap == null) {
                    wildcardDiagramMap = new TreeMap<>(e.getValue());
                } else {
                    wildcardDiagramMap.putAll(e.getValue());
                }
                wildcardPkgDiagramIndexData.put(packageClusterKey, wildcardDiagramMap);
            }
        }

        if (!wildcardPkgDiagramIndexData.isEmpty() && wildcardDiagramMap != null && !wildcardDiagramMap.isEmpty()) {
            mapPackageDiagrams(wildcardPkgDiagramIndexData.get(packageClusterKey));
        }
    }

    private void mapPackageDiagrams(SortedMap<String, String> diagramsIndexDataMap) {
        final Stream<Map.Entry<String, String>> sorted =
                diagramsIndexDataMap.entrySet().stream().sorted(Map.Entry.comparingByValue());
        sorted.forEach(e -> this.outputSingleDiagramHtml(e.getKey(), e.getValue()));
    }

    private void outputSingleDiagramHtml(String sourcePath, String diagramData) {
        final String textName = PIMDocFileType.TEXT.from(sourcePath, PIMDocFileType.GRAPHVIZ_SOURCE);
        printLine("\t\t\t\t\t\t\t\t<div>");
        printLine("\t\t\t\t\t\t\t\t\t<a href=\"", textName, "\" target=\"", HTMLMapper.FRAME_NAME, "\">");
        printLine(diagramData);
        printLine("\t\t\t\t\t\t\t\t\t</a>");
        printLine("\t\t\t\t\t\t\t\t</div>");
    }

    private void packageGroupClassDetails(final SortedSet<String> classes) {
        classes.forEach(this::mapPackageMemberName);
    }

    private void mapPackageMemberName(String qualifiedName) {
        try {
            printLine("\t\t\t\t\t\t\t\t<div>");
            final ModelElement_1_0 contained = this.model.getElement(qualifiedName);
            printLine("\t\t\t\t\t\t\t<a href=\"", getHref(contained), "\" title=\"", getDisplayName(contained),
                    "\" target=\"", HTMLMapper.FRAME_NAME, "\">", contained.getName(), "</a>");
            printLine("\t\t\t\t\t\t\t\t</div>");
        } catch (ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    private void packageGroupSummary(String packagePattern) {
        printLine("\t\t\t\t\t\t<summary>");
        if (PackagePatternComparator.isWildcardPattern(packagePattern)) {
            mapPackageGroupLink(packagePattern);
        } else {
            mapPackageLink(packagePattern);
        }
        printLine("\t\t\t\t\t\t</summary>");
    }

    private void mapPackageGroupLink(String packagePattern) {
        final String namespace = PackagePatternComparator.removeWildcard(packagePattern);
        printLine("\t\t\t\t\t\t\t<a href=\"", getBaseURL(), NamespaceLocation.getLocation(namespace),
                MagicFile.PACKAGE_CLUSTER.getFileName(MagicFile.Type.TEXT), "\" target=\"", HTMLMapper.FRAME_NAME,
                "\">", packagePattern.replace(":", "::"), "</a>");
    }

    private void mapPackageLink(String qualifiedName) {
        try {
            final ModelElement_1_0 packageElement = this.model.getElement(qualifiedName);
            printLine("\t\t\t\t\t\t\t<a href=\"", getHref(packageElement), "\" target=\"", HTMLMapper.FRAME_NAME, "\">",
                    getDisplayName(packageElement), "</a>");
            printLine("\t\t\t\t\t\t\t<a href=\"", getHref(packageElement), "#", AlbumMapper.COMPARTMENT_ID,
                    "\" target=\"", HTMLMapper.FRAME_NAME, "\">");
            printLine("\t\t\t\t\t\t\t\t<img alt=\"UML\" class=\"uml-symbol\" src=\"",
                    MagicFile.UML_SYMBOL.getFileName(MagicFile.Type.IMAGE), "\"/>");
            printLine("\t\t\t\t\t\t\t</a>");
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

    private PackageGroupBuilder getPackageGroups() {
        final PackageGroupBuilder navigationCompartment = new PackageGroupBuilder();
        final Collection<String> tableOfContentEntries = configuration.getTableOfContentEntries();
        if (tableOfContentEntries.isEmpty()) {
            streamElements().filter(ModelElement_1_0::isPackageType).map(ModelElement_1_0::getQualifiedName)
                    .forEach(navigationCompartment::addKey);
            navigationCompartment.addKey(PackagePatternComparator.getCatchAllPattern());
        } else {
            tableOfContentEntries.forEach(navigationCompartment::addKey);
        }
        streamElements().filter(element -> element.isClassType() || element.isDataType())
                .map(ModelElement_1_0::getQualifiedName).forEach(navigationCompartment::addElement);
        navigationCompartment.normalize();
        return navigationCompartment;
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
