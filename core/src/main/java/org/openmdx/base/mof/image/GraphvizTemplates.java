/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Graphviz Templates 
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
package org.openmdx.base.mof.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.PIMDocFileType;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.uses.org.apache.commons.io.output.StringBuilderWriter;
import org.w3c.cci2.CharacterLargeObjects;

/**
 * Graphviz Templates
 */
public class GraphvizTemplates {

	public GraphvizTemplates(final Model_1_0 model, final GraphvizStyle styleSheet, final Sink sink, final boolean graphvizHasIssue144) {
		this.model = model;
		this.styleSheet = styleSheet;
		this.sink = sink;
		this.graphvizHasIssue144 = graphvizHasIssue144;
	}
	
	private GraphvizTemplates(GraphvizTemplates that, String name, boolean graphvizHasIssue144) {
		this.model = that.model;
		this.styleSheet = that.styleSheet;
		this.sink = that.sink.nested(name);
		this.graphvizHasIssue144 = graphvizHasIssue144;
	}
	
	private final Model_1_0 model;
	private final GraphvizStyle styleSheet;
	private final Sink sink;
	private final boolean graphvizHasIssue144;
	
	private static final Pattern HTML_TITLE = Pattern.compile("\\s*(?:di)?graph\\s*(<.*)", Pattern.DOTALL);
	private static final Pattern TITLE = Pattern.compile("\\s*(?:di)?graph\\s*(\"(?:[^\"]|\\\\\")+\"|[A-Za-z0-9_.-]+)\\s*\\{.*", Pattern.DOTALL);

	public GraphvizTemplates nested(String name) {
		return new GraphvizTemplates(this, name, graphvizHasIssue144);
	}
	
	/**
	 * Draw given diagrams and send them to the sink
	 * 
	 * @param styleSheet    the Graphviz style sheet
	 * @param fileExtension the file extension, either "dot" or "gv"
	 * 
	 * @throws ServiceException in case of failure
	 */
	public void drawDiagrams(final File sourceDir) throws ServiceException {
		try {
			if (sourceDir.exists() && sourceDir.isDirectory()) {
				for (final File file : sourceDir.listFiles()) {
					if (file.isDirectory()) {
						nested(file.getName()).drawDiagrams(file);
					} else if (PIMDocFileType.GRAHVIZ_TEMPLATE.test(file.getName())) {
						drawDiagram(file);
					}
				}
			}
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	private void drawDiagram(final File file) throws IOException, ServiceException {
		final StringBuilder dot = readTemplate(file);
		final String title = getTitle(dot).orElse(file.getPath());
		final Map<String, GraphvizNode> classNodes = processClasses(dot);
		processAttributeStatement(dot);
		processInstanceOf(dot, classNodes);
		final Map<String, GraphvizEdge> associationNodes = processNamedAssociations(dot);
		processWildcardAssoications(dot, classNodes, associationNodes);
		final SortedMap<Integer, GraphvizLayer> layerNodes = processLayer(dot);
		processLayers(dot, layerNodes);
		sink.accept(
			PIMDocFileType.GRAPHVIZ_SOURCE.from(file.getName(), PIMDocFileType.GRAHVIZ_TEMPLATE), 
			title,
			CharacterLargeObjects.createByteArrayOutputStream(dot)
		);
	}

	// Default visibility for testing
	Optional<String> getTitle(CharSequence dot) {
		final String uncommented = uncomment(dot);
		final Matcher titleMatcher = TITLE.matcher(uncommented);
		if(titleMatcher.matches()) {
			return Optional.of(unquoteAndUnescape(titleMatcher.group(1)));
		} else {
			final Matcher htmlTitleMatcher = HTML_TITLE.matcher(uncommented);
			if(htmlTitleMatcher.matches()) {
				final String tail = htmlTitleMatcher.group(1);
				for(int i = 1, n = 1; i < tail.length(); i++) {
					char c = tail.charAt(i);
					if(c == '<') {
						n++; 
					} else if (c == '>') {
						if(--n == 0) {
							return Optional.of(tail.substring(1, i));
						}
						
					}
				}
			}
		}
		return Optional.empty();
	}

	private static String uncomment(CharSequence dot) {
		return dot.toString().replaceAll("(?s)//[^\\v]*[\\v]|/\\*.*?\\*/", "\n");
	}
	
	private static String unquoteAndUnescape(String value) {
		if(value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1).replaceAll("\\\\\"", "\"");
		} else {
			return value;
		}
	}	
	
	/**
	 * ${LAYER[layer=…,mindist=…,n1=v1,…]}
	 */
	private SortedMap<Integer, GraphvizLayer> processLayer(StringBuilder dot) throws ServiceException {
		final SortedMap<Integer, GraphvizLayer> layerNodes = new TreeMap<>();
		for (int startPos = dot.indexOf("${LAYER["); startPos >= 0; ) {
			final int endPos = dot.indexOf("]}", startPos);
			if (endPos > startPos) {
				final GraphvizLayer layerNode = new GraphvizLayer(styleSheet);
				layerNode.getParameters().parseParameters(dot.subSequence(startPos + "${LAYER[".length(), endPos));
				dot.replace(startPos, endPos + 2, layerNode.toString());
				final GraphvizLayer conflict = layerNodes.put(layerNode.getLayer(), layerNode);
				if(conflict != null) {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN, 
						BasicException.Code.ASSERTION_FAILURE,
						"${LAYER[layer=…]}: The 'layer' value is not unique within this digraph",
						new BasicException.Parameter("layer", conflict.getLayer())
					);
				}
			} else {
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN, 
					BasicException.Code.PARSE_FAILURE,
					"${ASSOCIATION[…]} place holder is not properly closed",
					new BasicException.Parameter("dot", dot.substring(startPos))
				);
			}
			startPos = dot.indexOf("${LAYER[", endPos);
		}
		return layerNodes;
	}
	
	/**
	 * ${LAYERS}
	 */
	private void processLayers(StringBuilder dot, SortedMap<Integer, GraphvizLayer> layerNodes) throws ServiceException {
		if(!layerNodes.isEmpty()) {
			final int startPos = dot.indexOf("${LAYERS}");
			if (startPos >= 0) {
				final int endPos = startPos + "${LAYERS}".length();
				if(endPos > startPos) {
					final GraphvizAttributes parameters = new GraphvizAttributes(styleSheet, "_class");
		            parameters.setStrictValue("style", "invis");
					StringBuilder layerEdges = new StringBuilder();
					final Iterator<GraphvizLayer> nodes = layerNodes.values().iterator();
					GraphvizLayer predecessor = nodes.next();
					while(nodes.hasNext()) {
						GraphvizLayer successor = nodes.next();
						parameters.setStrictValue("minlen", successor.getMinDist());
						layerEdges.append("\n\t");
						GraphvizAttributes.appendQuoted(layerEdges, predecessor.getId());
						layerEdges.append(" -> ");
						GraphvizAttributes.appendQuoted(layerEdges, successor.getId());
						parameters.appendTo(layerEdges, "\t");
						predecessor = successor;
					}
					dot.replace(startPos, endPos + 2, layerEdges.toString());
				} else {
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN, 
						BasicException.Code.PARSE_FAILURE,
						"${LAYERS[…]} place holder is not properly closed",
						new BasicException.Parameter("dot", dot.substring(startPos))
					);
				}
			}
		}
	}
	
	/**
	 * ${ASSOCIATION[name=*]}
	 */
	private void processWildcardAssoications(final StringBuilder dot, final Map<String, GraphvizNode> classNodes,
			final Map<String, GraphvizEdge> associationNodes) throws ServiceException {
		final int startPos = dot.indexOf("${ASSOCIATION[name=*]}");
		if (startPos >= 0) {
			final int endPos = startPos + "${ASSOCIATION[name=*]}".length();
			final StringBuilder associationEdges = new StringBuilder();
			// Associations
			for (ModelElement_1_0 elementDef : model.getContent()) {
				if (elementDef.isAssociationType()) {
					ModelElement_1_0 end1 = model.getElement(elementDef.objGetList("content").get(0));
					ModelElement_1_0 end1Type = model.getElementType(end1);
					ModelElement_1_0 end2 = model.getElement(elementDef.objGetList("content").get(1));
					ModelElement_1_0 end2Type = model.getElementType(end2);
					if (classNodes.containsKey(end1Type.getQualifiedName())
							&& classNodes.containsKey(end2Type.getQualifiedName())
							&& !associationNodes.containsKey(elementDef.getQualifiedName())) {
						GraphvizEdge associationNode = new GraphvizEdge(styleSheet, sink, graphvizHasIssue144);
						associationNode.setId(elementDef.getQualifiedName());
						associationNode.setAssociationDef(elementDef);
						associationEdges.append("\n\t").append(associationNode);
					}
				}
			}
			// Complex structure fields as edges
			for (GraphvizNode classNode : classNodes.values()) {
				ModelElement_1_0 classDef = classNode.getElementDef();
				if (model.isStructureType(classDef)) {
					List<Object> elements = classDef.objGetList("content");
					for (Object element : elements) {
						ModelElement_1_0 fieldDef = model.getElement(element);
						if (model.isStructureFieldType(fieldDef)) {
							ModelElement_1_0 fieldDefType = fieldDef.getDereferencedType();
							if (fieldDefType.isStructureType()
									&& classNodes.containsKey(fieldDefType.getQualifiedName())) {
								GraphvizEdge fieldNode = new GraphvizEdge(styleSheet, sink, graphvizHasIssue144);
								fieldNode.setId(fieldDef.getQualifiedName());
								fieldNode.setFieldDef(fieldDef);
								fieldNode.getParameters().setStrictValue("minlen", "3");
								associationEdges.append("\n\t").append(fieldNode);
							}
						}
					}
				}
			}
			dot.replace(startPos, endPos, associationEdges.toString());
		}
	}

	/**
	 * ${ASSOCIATION[name=…,n1=v1,…]}
	 */
	private Map<String, GraphvizEdge> processNamedAssociations(final StringBuilder dot) throws ServiceException {
		final Map<String, GraphvizEdge> associationNodes = new HashMap<String, GraphvizEdge>();
		for (int startPos = dot.indexOf("${ASSOCIATION["); startPos >= 0; ) {
			final int endPos = dot.indexOf("]}", startPos);
			if (endPos > startPos) {
				final GraphvizEdge associationNode = new GraphvizEdge(styleSheet, sink, graphvizHasIssue144);
				final GraphvizAttributes parameters = associationNode.getParameters();
				parameters.parseParameters(dot.subSequence(startPos + "${ASSOCIATION[".length(), endPos));
				final String qualifiedName = parameters.getValue("name");
				if (!isWildcard(qualifiedName)) {
					ModelElement_1_0 associationDef = model.getElement(qualifiedName);
					associationNode.setId(associationDef.getQualifiedName());
					associationNode.setAssociationDef(associationDef);
					associationNodes.put(associationNode.getId(), associationNode);
					dot.replace(startPos, endPos + 2, associationNode.toString());
				}
			} else {
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN, 
					BasicException.Code.PARSE_FAILURE,
					"${ASSOCIATION[…]} place holder is not properly closed",
					new BasicException.Parameter("dot", dot.substring(startPos))
				);
			}
			startPos = dot.indexOf("${ASSOCIATION[", endPos);
		}
		return associationNodes;
	}

	private boolean isWildcard(final String qualifiedName) {
		return "*".equals(qualifiedName);
	}

	/**
	 * ${INSTANCE_OF}
	 */
	private void processInstanceOf(final StringBuilder dot, final Map<String, GraphvizNode> classNodes) throws ServiceException {
		final int startPos = dot.indexOf("${INSTANCE_OF}");
		if (startPos >= 0) {
			final int endPos = startPos + "${INSTANCE_OF}".length();
			StringBuilder instanceOfEdges = new StringBuilder();
			for (GraphvizNode classNode : classNodes.values()) {
				ModelElement_1_0 classDef = model.getElement(classNode.getId());
				List<Object> supertypes = classDef.objGetList("supertype");
				for (Object supertype : supertypes) {
					ModelElement_1_0 supertypeDef = model.getElement(supertype);
					if (classNodes.containsKey(supertypeDef.getQualifiedName())) {
						GraphvizAttributes attributes = new GraphvizAttributes(styleSheet, "_class");
						attributes.setDefaultValue("_class", "uml_instance_of");
						attributes.setStrictValue("dir", "forward");
						attributes.setStrictValue("arrowtail", "onormal");
						instanceOfEdges.append("\n\t");
						GraphvizAttributes.appendQuoted(instanceOfEdges, classNode.getId());
						instanceOfEdges.append(" -> ");
						GraphvizAttributes.appendQuoted(instanceOfEdges, supertypeDef.getQualifiedName());
						attributes.appendTo(instanceOfEdges, "\t");
					}
				}
			}
			dot.replace(startPos, endPos, instanceOfEdges.toString());
		}
	}

	/**
	 * ${ATTRIBUTE_STATEMENTS}
	 */
	private void processAttributeStatement(final StringBuilder dot) {
		final int startPos = dot.indexOf("${ATTRIBUTE_STATEMENTS}");
		if (startPos >= 0) {
			final int endPos = startPos + "${ATTRIBUTE_STATEMENTS}".length();
			final StringBuilder attributeStatements = new StringBuilder();
			for (String kind : Arrays.asList("graph", "node", "edge")) {
				final Map<String, String> style = styleSheet.getElementStyle(kind);
				if (!style.isEmpty()) {
					attributeStatements.append("\n\t").append(kind);
					GraphvizAttributes.appendAttributeList(attributeStatements, "\t", style);
				}
			}
			getTitle(dot).ifPresent(
				title -> appendTitle(attributeStatements, title)
			);
			dot.replace(startPos, endPos, attributeStatements.toString());
		}
	}

	private void appendTitle(final StringBuilder target, String title) {
		target.append("\n\tlabel = ");
		GraphvizAttributes.appendQuoted(target, title);
	}

	/**
	 * ${CLASS[name=…,compartments=…,n1=v1,…]}
	 */
	private Map<String, GraphvizNode> processClasses(final StringBuilder dot) throws ServiceException {
		final Map<String, GraphvizNode> classNodes = new HashMap<String, GraphvizNode>();
		for (int startPos = dot.indexOf("${CLASS["); startPos >= 0;) {
			final int endPos = dot.indexOf("]}", startPos);
			if (endPos > startPos) {
				final GraphvizNode classNode = new GraphvizNode(styleSheet, sink);
				classNode.getParameters().parseParameters(dot.subSequence(startPos + "${CLASS[".length(), endPos));
				final String qualifiedName = classNode.getParameters().getValue("name");
				final ModelElement_1_0 classDef = model.getElement(qualifiedName);
				classNode.setElementDef(classDef);
				classNode.setId(classDef.getQualifiedName());
				classNodes.put(classNode.getId(), classNode);
				dot.replace(startPos, endPos + 2, classNode.toString());
			} else {
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN, 
					BasicException.Code.PARSE_FAILURE,
					"${CLASS[…]} place holder is not properly closed",
					new BasicException.Parameter("dot", dot.substring(startPos))
				);
			}
			startPos = dot.indexOf("${CLASS[", endPos);
		}
		return classNodes;
	}

	/**
	 * Read the DOTT file
	 * 
	 * @param f the source file
	 * 
	 * @return a {@code StringBuilder} with the source file's content
	 * 
	 * @throws IOException
	 */
	private StringBuilder readTemplate(final File f) throws IOException {
		System.out.println("INFO: Processing diagram " + f.getAbsolutePath());
		System.out.flush();
		try (
			final Reader source = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
			final StringBuilderWriter target = new StringBuilderWriter();
		) {
			CharacterLargeObjects.streamCopy(source, 0L, target);
			return target.getBuilder();
		}
	}

}