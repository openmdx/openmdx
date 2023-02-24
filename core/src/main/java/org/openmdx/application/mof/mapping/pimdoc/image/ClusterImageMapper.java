/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Package Group Mapper 
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openmdx.application.mof.mapping.pimdoc.MagicFile;
import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.application.mof.mapping.pimdoc.text.ClusterTextMapper;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.GraphvizStyleSheet;

/**
 * Package Group Mapper 
 * 
 
 digraph {
    edge[
        style = invis
    ]
    label="All Clusters"
    subgraph cluster_org {
        style=filled
        label="org"
        fillcolor=lightcyan
        package_org[
            label="org"
            shape=tab 
            style=filled
            fillcolor=khaki1
        ];
        subgraph cluster_org_openmdx {
            label="::openmdx"
            style=filled
            fillcolor=lightcyan
            package_org_openmdx[
                label="openmdx"
                shape=tab 
                style=filled
                fillcolor=khaki1
            ];
            subgraph cluster_org_openmdx_base {
                label="::base"
                style=filled
                fillcolor=lightcyan
                package_org_openmdx_base[
                    label="base"
                    shape=tab
                    style=filled
                    fillcolor=khaki1
                ]
            }
            subgraph cluster_org_openmdx_state2 {
                label="::state2"
                style=filled
                fillcolor=lightcyan
                package_org_openmdx_state2[
                    label="state2"
                    shape=tab
                    style=filled
                    fillcolor=khaki1
                ]
            }
        }
    }
    subgraph cluster_net {
        label="net"
        style=filled
        fillcolor=lightcyan
        package_net[
            label="net"
            shape=tab 
            style=filled
            fillcolor=khaki1
        ];
        subgraph cluster_net_rfc {
            label="::rfc"
            style=filled
            fillcolor=lightcyan
            package_net_rfc[
                label="rfc"
                shape=tab 
                style=filled
                fillcolor=khaki1
            ]
        }
    }
    package_org -> { package_org_openmdx} 
    package_org_openmdx -> { package_org_openmdx_base package_org_openmdx_state2} 
    package_net -> { package_net_rfc} 
}

 
 */
public class ClusterImageMapper extends GraphvizMapper {

    /**
     * Constructor 
     */
    public ClusterImageMapper(
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
    public ClusterImageMapper(
    	Sink sink, 
        Model_1_0 model,
        boolean markdown, 
        PIMDocConfiguration configuration,
        GraphvizStyleSheet imageStyleSheet
        
    ){
		super(sink, model, markdown, configuration);
		this.cluster = null;
    }    
    
    private final ModelElement_1_0 cluster;
    
	@Override
	protected Map<String, String> getEdgeStyle() {
		final Map<String, String> style = super.getEdgeStyle();
		style.put("sytle", "invis");
		return style;
	}

	@Override
	protected void graphBody() {
		printLine("\tlabel=\"", getTitle(), "\"");
		mapSubgraphs("\t", null, new TreeMap<>());
	}

	private void mapSubgraphs(String indent, ModelElement_1_0 anchestor, Map<String,List<String>> dependencies) {
		streamElements()
			.filter(ModelElement_1_0::isPackageType)
			.filter(p -> isChild(anchestor, p) && !isOutOfScope(p))
			.forEach(p -> mapSubgraph(indent, anchestor, p, dependencies));
		if(anchestor == null) {
			mapDependencies(indent, dependencies);
		}
	}

	private void mapDependencies(String indent, Map<String, List<String>> dependencies) {
		for(Map.Entry<String, List<String>> e : dependencies.entrySet()) {
			printLine(indent, "\"", e.getKey(), "\" -> {");
				for(String v : e.getValue()) {
					printLine(indent, "\t\"", v, "\"");
				}
			printLine(indent, "}");
		}
	}

	private boolean isOutOfScope(
		ModelElement_1_0 element
	) {
		if(this.cluster == null) {
			return false;
		}
		final String clusterPrefix = getPrefix(cluster);
		final String elementPrefix = getPrefix(element);
		return !(elementPrefix.startsWith(clusterPrefix) || clusterPrefix.startsWith(elementPrefix));
	}
	
	private void mapSubgraph(String indent, ModelElement_1_0 anchestor, ModelElement_1_0 element, Map<String, List<String>> dependencies) {
		final String clusterPrefix = getPrefix(cluster);
		final String elementPrefix = getPrefix(element);
		printLine(indent, "subgraph \"cluster ", elementPrefix, "\" {");
		final String indent1 = indent + '\t';
		mapSubgraphStyle(indent1, element);
		if(elementPrefix.startsWith(clusterPrefix)) {
			mapPackage(indent1, element);
			dependencies.computeIfAbsent(anchestor.getQualifiedName(), k -> new ArrayList<>()).add(element.getQualifiedName());
		}
		mapSubgraphs(indent1, element, new HashMap<>());
		printLine(indent,"}");
	}

	private void mapSubgraphStyle(String indent, ModelElement_1_0 element) {
		final Map<String, String> style = getClassStyle("uml_cluster");
		style.put("style", "filled");
		style.put("label", indent.length() == 2 ? getDisplayName(element) : ("::" + element.getName()));
		style.put("href", getBaseURL() + getClusterPath(element));
		mapStyle(indent, style);
	}

	private void mapPackage(String indent, ModelElement_1_0 element) {
		printLine(indent, "\"package ", element.getQualifiedName(), "\"");
		final String indent1 = indent + '\t';
		mapPackageStyle(element, indent1);
		printLine(indent, "}");
	}

	private void mapPackageStyle(ModelElement_1_0 element, final String indent1) {
		final Map<String, String> style = getClassStyle("uml_package");
		style.put("label", element.getName());
		style.put("style", "filled");
		style.put("shape","tab");
		style.put("href", getHref(element));
		mapStyle(indent1, style);
	}
	
	private boolean isChild(ModelElement_1_0 parent, ModelElement_1_0 candidate) {
		final String segmentName = candidate.getSegmentName();
		if(parent == null) {
			return has2elements(segmentName);
		} else {
			final String prefix = getPrefix(parent);
			return segmentName.startsWith(prefix) && has2elements(segmentName.substring(prefix.length()));
		}
	}

	/**
	 * Retrieve the model elements prefix inlcuding a trailing colon
	 * 
	 * @param ancestor the model element
	 * 
	 * @return the qualified name minus its name
	 */
	private String getPrefix(ModelElement_1_0 ancestor) {
		final String qualifiedName = ancestor.getQualifiedName();
		return qualifiedName.substring(0, qualifiedName.lastIndexOf(':') + 1);
	}

	/**
	 * Tests whether it's a package directly underneath the prefix, e.g. "openmdx:openmdx" underneath "org:"
	 * 
	 * @param tail the tail to introspected
	 * 
	 * @return {@code true} if the tail contains exactly one colon
	 */
	private boolean has2elements(final String tail) {
		return tail.chars().filter(c -> c == ':').count() == 1;
	}
	
	@Override
	protected String getEntryName() {
		return ClusterTextMapper.getEntryName(cluster, MagicFile.Type.SOURCE);
	}

	@Override
	protected String getTitle() {
		return cluster == null ? "All Clusters" : ("Cluster " + getDisplayName(cluster) + "::**");
	}
	
}
