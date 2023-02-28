/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Cluster Diagram Mapper 
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
package org.openmdx.application.mof.mapping.pimdoc.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openmdx.application.mof.mapping.pimdoc.MagicFile;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Cluster Diagram Mapper 
 */
public class ClusterDiagramMapper extends GraphvizMapper {

    /**
     * Constructor 
     */
    public ClusterDiagramMapper(
    	Sink sink, 
        ModelElement_1_0 cluster,
        boolean markdown, 
        PIMDocConfiguration configuration
        
    ){
		super(sink, cluster.getModel(), markdown, configuration);
		this.cluster = cluster;
    }    

    /**
     * Constructor 
     */
    public ClusterDiagramMapper(
    	Sink sink, 
        Model_1_0 model,
        boolean markdown, 
        PIMDocConfiguration configuration
    ){
		super(sink, model, markdown, configuration);
		this.cluster = null;
    }    
    
    private final ModelElement_1_0 cluster;
    
	@Override
	protected void graphBody() {
		printLine("\tlabel=\"", getTitle(), "\"");
		mapSubgraphs("\t", null, new TreeMap<>());
	}

	private void mapSubgraphs(String indent, ModelElement_1_0 anchestor, Map<String,List<String>> dependencies) {
		streamElements()
			.filter(ModelElement_1_0::isPackageType)
			.filter(p -> isChild(anchestor, p))
			.filter(this::isInScope)
			.forEach(p -> mapSubgraph(indent, anchestor, p, dependencies));
		if(anchestor == null) {
			mapDependencies(indent, dependencies);
		}
	}

	private void mapDependencies(String indent, Map<String, List<String>> dependencies) {
		for(Map.Entry<String, List<String>> e : dependencies.entrySet()) {
			printLine(indent, "\"package ", e.getKey(), "\" -> {");
				for(String v : e.getValue()) {
					printLine(indent, "\t\"package ", v, "\"");
				}
			printLine(indent, "}[style=\"invis\"]");
		}
	}

	private boolean isInScope(
		ModelElement_1_0 element
	) {
		if(this.cluster == null) {
			return true;
		}
		final String clusterPrefix = getPrefix(cluster);
		final String elementPrefix = getPrefix(element);
		return elementPrefix.startsWith(clusterPrefix) || clusterPrefix.startsWith(elementPrefix);
	}
	
	private void mapSubgraph(String indent, ModelElement_1_0 anchestor, ModelElement_1_0 element, Map<String, List<String>> dependencies) {
		final String clusterPrefix = getPrefix(cluster);
		final String elementPrefix = getPrefix(element);
		final String indent1 = indent + '\t';
		printLine(indent, "subgraph \"cluster ", elementPrefix, "\" {");
		mapSubgraphStyle(indent1, element);
		if(elementPrefix.startsWith(clusterPrefix)) {
			mapPackage(indent1, element);
			if(anchestor != null) {
				dependencies.computeIfAbsent(anchestor.getQualifiedName(), k -> new ArrayList<>()).add(element.getQualifiedName());
			}
		}
		mapSubgraphs(indent1, element, dependencies);
		printLine(indent,"}");
	}

	private void mapSubgraphStyle(String indent, ModelElement_1_0 element) {
		final Map<String, String> style = getClassStyle("uml_cluster");
		style.put("style", "filled");
		style.put("label", element.getName());
		style.put("href", getBaseURL() + getClusterPath(element, MagicFile.Type.TEXT));
		style.put("tooltip", getDisplayName(element));
		mapStyle(indent, style);
	}

	private void mapPackage(String indent, ModelElement_1_0 element) {
		printLine(indent, "\"package ", element.getQualifiedName(), "\" [");
		mapPackageStyle(indent + '\t', element);
		printLine(indent, "]");
	}

	private void mapPackageStyle(final String indent, ModelElement_1_0 element) {
		final Map<String, String> style = getClassStyle("uml_package");
		style.put("label", element.getName());
		style.put("style", "filled");
		style.put("shape","tab");
		style.put("href", getHref(element));
		style.put("tooltip", getDisplayName(element));
		mapStyle(indent, style);
	}
	
	/**
	 * Tests whether it's a package directly underneath the parent prefix, 
	 * e.g. "openmdx:openmdx" underneath "org:"
	 * 
	 * @param parentPackage the parent package
	 * @param candidatePackage the package to be tested
	 * 
	 * @return {@code true} if the candidate is exactly one level underneath the parent
	 */
	private boolean isChild(ModelElement_1_0 parentPackage, ModelElement_1_0 candidatePackage) {
		final String candidateName = candidatePackage.getQualifiedName();
		final String parentPrefix = getPrefix(parentPackage);
		return 
			candidateName.startsWith(parentPrefix) && 
			countColons(candidateName.substring(parentPrefix.length())) == 1;
	}

	/**
	 * Retrieve the model elements prefix including a trailing colon
	 * 
	 * @param ancestor the model element
	 * 
	 * @return the qualified name minus its name
	 */
	private String getPrefix(ModelElement_1_0 ancestor) {
		if(ancestor == null) {
			return "";
		} else {
			final String qualifiedName = ancestor.getQualifiedName();
			return qualifiedName.substring(0, qualifiedName.lastIndexOf(':') + 1);
		}
	}

	private long countColons(final String text) {
		return text.chars().filter(c -> c == ':').count();
	}
	
	@Override
	protected String getEntryName() {
		return GraphvizMapper.getClusterPath(cluster, MagicFile.Type.SOURCE);
	}

	@Override
	protected String getTitle() {
		return cluster == null ? "All Clusters" : ("Cluster " + getDisplayName(cluster) + "::**");
	}

	@Override
	protected String getBaseURL() {
		final StringBuilder baseURL = new StringBuilder();
		if(cluster != null) {
			for(long i = countColons(cluster.getQualifiedName()); i > 0; i--) {
				baseURL.append("../");
			}
		}
		return baseURL.toString();
	}
	
}
