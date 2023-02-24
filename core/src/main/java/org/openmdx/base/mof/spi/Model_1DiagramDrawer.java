/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Diagram Drawer 
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
package org.openmdx.base.mof.spi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.omg.mof.cci.DirectionKind;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.FileSink;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.uses.org.apache.commons.io.output.StringBuilderWriter;
import org.w3c.cci2.CharacterLargeObjects;

/**
 * Diagram Drawer
 */
public class Model_1DiagramDrawer {
	
	/**
	 * Graphviz Attributes
	 */
	static class GraphvizAttributes {
		
		GraphvizAttributes(GraphvizStyleSheet styleSheet, String... controlKeys) {
			this.styleSheet = styleSheet;
			this.controlKeys = Arrays.asList(controlKeys);
		}
		
		private final GraphvizStyleSheet styleSheet;
		private final Properties defaultValues = new Properties();
		private final Properties styleValues = new Properties(defaultValues);
		private final Properties parameterValues = new Properties(styleValues);
		private final Properties strictValues = new Properties(parameterValues);
		private final Collection<String> controlKeys;
		
		public String toString() {
			styleValues.putAll(styleSheet.getElementStyle(strictValues.getProperty("_class","")));
			final Map<String,String> attributes = new HashMap<String, String>();
			for(String key : strictValues.stringPropertyNames()) {
				if(!controlKeys.contains(key)) {
					attributes.put(key, getValue(key));
				}
			}
			return toAttributeList(attributes);
		}

		
		String getValue(String key) {
			return this.strictValues.getProperty(key);
		}
		
		void setDefaultValue(String key, String value) {
			this.defaultValues.put(key, value);
		}
		void setStrictValue(String key, String value) {
			this.strictValues.put(key, value);
		}
		
		void parseParameters(CharSequence parameterList) {
			for (String parameter : parameterList.toString().split(",")) {
	            String[] nv = parameter.split("=");
	            this.parameterValues.put(nv[0].toLowerCase(), unquote(nv[1]));
			}
		}
		
		private static String unquote(String value) {
			return value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length() - 1) : value;
		}
		
	}
	
	
    /**
     * GraphvizNode
     */
    static class GraphvizNode {

        GraphvizNode(GraphvizStyleSheet styleSheet) {
			this.parameters = new GraphvizAttributes(styleSheet, "name", "compartments");
			this.parameters.setDefaultValue("compartments", "false");
		}

		public GraphvizAttributes getParameters() {
            return this.parameters;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        /**
         * Retrieve classDef.
         *
         * @return Returns the classDef.
         */
        public ModelElement_1_0 getClassDef() {
            return this.classDef;
        }

        /**
         * Set classDef.
         * 
         * @param classDef
         *            The classDef to set.
         */
        public void setClassDef(ModelElement_1_0 classDef) {
            this.classDef = classDef;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            try {
                if (this.classDef == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unknown class definition. Specify qualified class name with attribute 'name'"
                    );
                } else {
                    Model_1_0 model = this.classDef.getModel();
                    String label = "<{";
                    // Stereotypes
                    List<Object> stereotypes = this.classDef.objGetList("stereotype");
                    if (!stereotypes.isEmpty()) {
                        label += "&laquo;";
                        for (Object stereotype : stereotypes) {
                            label += stereotype.toString();
                        }
                        label += "&raquo;<br />";
                    }
                    // Name
                    boolean isAbstract = Boolean.TRUE.equals(this.classDef.isAbstract());
                    label += "<b>" + (isAbstract ? "<i>" : "") + this.classDef.getName() + (isAbstract ? "</i>" : "") + "</b>";
                    // Compartments for attributes and operations
                    if (isShowCompartments()) {
                        if (this.classDef != null) {
                        	this.parameters.setDefaultValue("width", "2.0");
                            final double widthInInches = Double.parseDouble(this.parameters.getValue("width"));
                            List<Object> containedElements = this.classDef.objGetList("content");
                            List<ModelElement_1_0> attributeDefs = new ArrayList<ModelElement_1_0>();
                            List<ModelElement_1_0> operationDefs = new ArrayList<ModelElement_1_0>();
                            for (Object contained : containedElements) {
                                ModelElement_1_0 element = model.getElement(contained);
                                if (model.isAttributeType(element)) {
                                    attributeDefs.add(element);
                                } else if (model.isStructureFieldType(element) && !element.getDereferencedType().isStructureType()) {
                                    attributeDefs.add(element);
                                } else if (model.isOperationType(element)) {
                                    operationDefs.add(element);
                                }
                            }
                            if (!attributeDefs.isEmpty()) {
                                label += "|<table border=\"0\" cellspacing=\"0\" width=\"" + Double.valueOf(75.0 * widthInInches).intValue()
                                    + "px\">";
                                for (ModelElement_1_0 attributeDef : attributeDefs) {
                                    label += "<tr><td align=\"left\">";
                                    label += "+ " + (ModelHelper.isDerived(attributeDef) ? "/" : "") + attributeDef.getName() + " : "
                                        + model.getElementType(attributeDef).getQualifiedName();
                                    Multiplicity multiplicity = ModelHelper.getMultiplicity(attributeDef);
                                    if (multiplicity != Multiplicity.SINGLE_VALUE) {
                                        label += " [" + multiplicity.toString() + "]";
                                    }
                                    label += "</td></tr>";
                                }
                                label += "</table>";
                            }
                            if (!operationDefs.isEmpty()) {
                                label += "|<table border=\"0\" cellspacing=\"0\" width=\"" + Double.valueOf(75.0 * widthInInches).intValue()
                                    + "px\">";
                                for (ModelElement_1_0 operationDef : operationDefs) {
                                    label += "<tr><td align=\"left\">";
                                    label += "+ " + operationDef.getName() + "(";
                                    List<Object> params = operationDef.objGetList("content");
                                    String sep = "";
                                    ModelElement_1_0 returnParamDef = null;
                                    for (Object param : params) {
                                        ModelElement_1_0 paramDef = model.getElement(param);
                                        Object direction = paramDef.objGetValue("direction");
                                        if (DirectionKind.RETURN_DIR.equals(direction)) {
                                            returnParamDef = paramDef;
                                        } else if (DirectionKind.IN_DIR.equals(direction)) {
                                            label += sep + paramDef.getName() + " : " + model.getElementType(paramDef).getQualifiedName();
                                            sep = ", ";
                                        }
                                    }
                                    label += ") : " + (returnParamDef == null ? "void"
                                        : model.getElementType(returnParamDef).getQualifiedName());
                                    label += "</td></tr>";
                                }
                                label += "</table>";
                            }
                        }
                    }
                    label += "}>";
                    this.parameters.setStrictValue("label", label);
                    this.parameters.setStrictValue("tooltip", toDisplayName(this.classDef.getQualifiedName()));
                    return quote(this.id) + this.parameters;
                }
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }

        /**
         * Retrieve showCompartments.
         *
         * @return Returns the showCompartments.
         */
        public boolean isShowCompartments() {
            return Boolean.parseBoolean(this.parameters.getValue("compartments"));
        }

        private String id;
        private ModelElement_1_0 classDef;
        private final GraphvizAttributes parameters;
    }

    /**
     * GraphvizEdge
     *
     */
    public static class GraphvizEdge {

    	GraphvizEdge(GraphvizStyleSheet styleSheet) {
			this.parameters = new GraphvizAttributes(styleSheet, "name", "dir");
			this.parameters.setDefaultValue("dir", "forward");
            this.parameters.setDefaultValue("style","");
		}
    	
        public GraphvizAttributes getParameters() {
            return this.parameters;
        }

        /**
         * Retrieve associationDef.
         *
         * @return Returns the associationDef.
         */
        public ModelElement_1_0 getAssociationDef() {
            return this.associationDef;
        }

        /**
         * Set associationDef.
         * 
         * @param associationDef
         *            The associationDef to set.
         */
        public void setAssociationDef(ModelElement_1_0 associationDef) {
            this.associationDef = associationDef;
        }

        /**
         * Set fieldDef.
         * 
         * @param fieldDef
         *            The fieldDef to set.
         */
        public void setFieldDef(ModelElement_1_0 fieldDef) {
            this.fieldDef = fieldDef;
        }

        /**
         * Retrieve id.
         *
         * @return Returns the id.
         */
        public String getId() {
            return this.id;
        }

        /**
         * Set id.
         * 
         * @param id
         *            The id to set.
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Get arrowhead for given association ends.
         */
        private String getArrowhead(
            ModelElement_1_0 end1,
            ModelElement_1_0 end2
        ) throws ServiceException {
            if (AggregationKind.SHARED.equals(end1.getAggregation())) {
                return "oboxodiamond";
            } else if (AggregationKind.COMPOSITE.equals(end1.objGetValue("aggregation"))) {
                return "oboxdiamond";
            } else if (AggregationKind.NONE.equals(end1.objGetValue("aggregation")) && !end1.objGetList("qualifierType").isEmpty()) {
                return "obox";
            } else if (Boolean.TRUE.equals(end2.objGetValue("isNavigable"))) {
                return "vee";
            } else {
                return "tee";
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            try {
            	if(hasStyleInvis()) {
            		return "";
            	} else if (this.fieldDef != null) {
                    final Model_1_0 model = this.fieldDef.getModel();
                    final ModelElement_1_0 exposedEndType = model.getElement(this.fieldDef.getContainer());
                    final ModelElement_1_0 referencedEndType = this.fieldDef.getDereferencedType();
                    this.parameters.setStrictValue("label", "");
                    this.parameters.setStrictValue("tooltip", "");
                    this.parameters.setStrictValue("headlabel", "[1..1]");
                    this.parameters.setStrictValue("taillabel", this.fieldDef.getName() + " [" + ModelHelper.getMultiplicity(this.fieldDef) + "]");
                    this.parameters.setStrictValue("arrowhead", "tee");
                    this.parameters.setStrictValue("arrowtail", "vee");
                    this.parameters.setDefaultValue("color", "#0000FF");
                    this.parameters.setDefaultValue("constraint", "false");
                    this.parameters.setDefaultValue("labeldistance", "3");
                    return createEdge(exposedEndType, referencedEndType);
                } else if (this.associationDef != null) {
                	final Model_1_0 model = this.associationDef.getModel();
                    final ModelElement_1_0 end1 = model.getElement(this.associationDef.objGetList("content").get(0));
                    final ModelElement_1_0 end2 = model.getElement(this.associationDef.objGetList("content").get(1));
                    final ModelElement_1_0 referencedEnd;
                    final ModelElement_1_0 exposedEnd;
                    if (Boolean.TRUE.equals(end1.objGetValue("isNavigable"))) {
                        referencedEnd = end1;
                        exposedEnd = end2;
                    } else {
                        referencedEnd = end2;
                        exposedEnd = end1;
                    }
                    final ModelElement_1_0 referencedEndType = model.getElementType(referencedEnd);
                    final ModelElement_1_0 exposedEndType = model.getElementType(exposedEnd);
                    this.parameters.setStrictValue("label", this.associationDef.getName());
                    this.parameters.setStrictValue("tooltip", toDisplayName(this.associationDef.getQualifiedName()));
                    this.parameters.setStrictValue("headlabel",exposedEnd.getName() + " [" + ModelHelper.getMultiplicity(exposedEnd) + "]");
                    this.parameters.setStrictValue("taillabel",referencedEnd.getName() + " [" + ModelHelper.getMultiplicity(referencedEnd) + "]");
                    this.parameters.setStrictValue("arrowhead",this.getArrowhead(referencedEnd, exposedEnd));
                    this.parameters.setStrictValue("arrowtail",this.getArrowhead(exposedEnd, referencedEnd));
                    this.parameters.setDefaultValue("color","#0000FF");
                    this.parameters.setDefaultValue("constraint","false");
                    this.parameters.setDefaultValue("labeldistance","3");
                    return createEdge(exposedEndType, referencedEndType);
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unknown association definition. Specify qualified association name with attribute 'name'"
                    );
                }
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }

		private String createEdge(final ModelElement_1_0 exposedEndType, final ModelElement_1_0 referencedEndType) {
			final ModelElement_1_0 left;
			final ModelElement_1_0 right;
			if(isDirForward()) {
				left = exposedEndType;
				right = referencedEndType;
			} else {
				 left = referencedEndType;
				 right = exposedEndType;
			}
			return quote(left.getQualifiedName()) + " -> " + quote(right.getQualifiedName()) + this.parameters;
		}

        private boolean isDirForward() {
        	return "forward".equals(this.parameters.getValue("dir"));
        }
        
		private boolean hasStyleInvis() {
			return "invis".equals(this.parameters.getValue("style"));
		}

        private String id;
        private ModelElement_1_0 associationDef;
        private ModelElement_1_0 fieldDef;
        private final GraphvizAttributes parameters;
    }

    /**
     * Draw given diagrams and send them to the sink
     * 
     * @param styleSheet the Graphviz style sheet
     * @param fileExtension the file extension, either "dot" or "gv"
     * 
     * @throws ServiceException
     */
    static void drawDiagrams(
        final Model_1_0 model,
        final File sourceDir, 
        final Sink sink, 
        final GraphvizStyleSheet styleSheet, 
        final String fileExtension
    ) throws ServiceException {
    	try {
            if (sourceDir.exists()) {
                for (final File file : sourceDir.listFiles()) {
                    if (file.isDirectory()) {
                        drawDiagrams(
                            model,
                            file, 
                            sink.nested(file.getName()),
                            styleSheet, 
                            fileExtension
                        );
                    } else if (file.getName().endsWith(".dott")) {
                        final StringBuilder dot = readTemplate(file);
                        final Map<String, GraphvizNode> classNodes = processClasses(dot, model, styleSheet);
                        processAttributeStatement(dot, styleSheet);
                        processInstanceOf(dot, model, classNodes);
                        final Map<String, GraphvizEdge> associationNodes = processNamedAssociations(dot, model, styleSheet);
                        processWildcardAssoications(dot, model, styleSheet, classNodes, associationNodes);
                        final byte[] data = toByteArray(dot);
                        final String entryName = file.getName().replace(".dott", "." + fileExtension);
						writeDiagram(sink, entryName, data);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

	private static void writeDiagram(final Sink sink, final String entryName, final byte[] data) {
		sink.accept(
			entryName,
			data.length,
			target -> {
				try {
					target.write(data);
				} catch (IOException exception) {
					throw new RuntimeServiceException(
						exception,
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.MEDIA_ACCESS_FAILURE,
						"Unable to save image source"
					);
				}
			}
		);
	}

	private static byte[] toByteArray(final StringBuilder dot) throws IOException {
		final byte[] data;
		try (
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(buffer, StandardCharsets.UTF_8)
		){
			writer.append(dot);
			writer.flush();
			data = buffer.toByteArray();
		}
		return data;
	}

    /**
     * ${ASSOCIATION[name=*]}
     */
	private static void processWildcardAssoications(
		final StringBuilder dot, 
		final Model_1_0 model,
		final GraphvizStyleSheet styleSheet, 
		final Map<String, GraphvizNode> classNodes, 
		final Map<String, GraphvizEdge> associationNodes
	) throws ServiceException {
		final int startPos = dot.indexOf("${ASSOCIATION[name=*]}");
		if (startPos >= 0) {
			final int endPos = startPos + "${ASSOCIATION[name=*]}".length();
		    String associationEdges = "";
		    // Associations
		    for (ModelElement_1_0 elementDef : model.getContent()) {
		        if (elementDef.isAssociationType()) {
		            ModelElement_1_0 end1 = model.getElement(elementDef.objGetList("content").get(0));
		            ModelElement_1_0 end1Type = model.getElementType(end1);
		            ModelElement_1_0 end2 = model.getElement(elementDef.objGetList("content").get(1));
		            ModelElement_1_0 end2Type = model.getElementType(end2);
		            if (classNodes.containsKey(end1Type.getQualifiedName()) &&
		                classNodes.containsKey(end2Type.getQualifiedName()) &&
		                !associationNodes.containsKey(elementDef.getQualifiedName())) {
		                GraphvizEdge associationNode = new GraphvizEdge(styleSheet);
		                associationNode.setId(elementDef.getQualifiedName());
		                associationNode.setAssociationDef(elementDef);
		                associationEdges += "\n\t" + associationNode.toString() + ";";
		            }
		        }
		    }
		    // Complex structure fields as edges
		    for (GraphvizNode classNode : classNodes.values()) {
		        ModelElement_1_0 classDef = classNode.getClassDef();
		        if (model.isStructureType(classDef)) {
		            List<Object> elements = classDef.objGetList("content");
		            for (Object element : elements) {
		                ModelElement_1_0 fieldDef = model.getElement(element);
		                if (model.isStructureFieldType(fieldDef)) {
		                    ModelElement_1_0 fieldDefType = fieldDef.getDereferencedType();
		                    if (fieldDefType.isStructureType() && classNodes.containsKey(
		                        fieldDefType.getQualifiedName()
		                    )) {
		                        GraphvizEdge fieldNode = new GraphvizEdge(styleSheet);
		                        fieldNode.setId(fieldDef.getQualifiedName());
		                        fieldNode.setFieldDef(fieldDef);
		                        fieldNode.getParameters().setStrictValue("minlen", "3");
		                        associationEdges += "\n\t" + fieldNode.toString() + ";";
		                    }
		                }
		            }
		        }
		    }
		    dot.replace(startPos, endPos, associationEdges);
		}
	}

    /**
     * ${ASSOCIATION[name=...,n1=v1,...]}
     */
	private static Map<String, GraphvizEdge> processNamedAssociations(
		final StringBuilder dot, 
		final Model_1_0 model, GraphvizStyleSheet styleSheet
	) throws ServiceException {
		final Map<String, GraphvizEdge> associationNodes = new HashMap<String, GraphvizEdge>();
		for (int startPos = dot.indexOf("${ASSOCIATION["); startPos >= 0; startPos = dot.indexOf("${ASSOCIATION[")) {
		    final int endPos = dot.indexOf("]}", startPos);
		    if (endPos > startPos) {
		        final GraphvizEdge associationNode = new GraphvizEdge(styleSheet);
		        associationNode.getParameters().parseParameters(dot.subSequence(startPos + 14, endPos));
		        final String qualifiedName = associationNode.getParameters().getValue("name");
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
		            "${ASSOCIATION[...]} place holder is not properly closed",
		            new BasicException.Parameter("dot", dot.substring(startPos))
		        );
		    }
		}
		return associationNodes;
	}

	private static boolean isWildcard(final String qualifiedName) {
		return "*".equals(qualifiedName);
	}

    /**
     * ${INSTANCE_OF}
     */
	private static void processInstanceOf(
		final StringBuilder dot, 
		final Model_1_0 model,
		final Map<String, GraphvizNode> classNodes
	) throws ServiceException {
		final int startPos = dot.indexOf("${INSTANCE_OF}");
		if (startPos >= 0) {
			final int endPos = startPos + "${INSTANCE_OF}".length();
		    String instanceOfEdges = "";
		    for (GraphvizNode classNode : classNodes.values()) {
		        ModelElement_1_0 classDef = model.getElement(classNode.getId());
		        List<Object> supertypes = classDef.objGetList("supertype");
		        for (Object supertype : supertypes) {
		            ModelElement_1_0 supertypeDef = model.getElement(supertype);
		            if (classNodes.containsKey(supertypeDef.getQualifiedName())) {
		                instanceOfEdges += "\n\t\"" + classNode.getId() + "\" -> \"" + supertypeDef.getQualifiedName()
		                    + "\" [dir=forward,arrowtail=onormal];";
		            }
		        }
		    }
		    dot.replace(startPos, endPos, instanceOfEdges);
		}
	}

	/**
	 * ${ATTRIBUTE_STATEMENTS}
	 */
	private static void processAttributeStatement(
		final StringBuilder dot, 
		final GraphvizStyleSheet styleSheet
	) {
		final int startPos = dot.indexOf("${ATTRIBUTE_STATEMENTS}");
		if (startPos >= 0) {
			final int endPos = startPos + "${ATTRIBUTE_STATEMENTS}".length();
		    StringBuilder attributeStatements = new StringBuilder();
		    for(String kind : Arrays.asList("graph", "node", "edge")) {
		    	final Map<String, String> style = styleSheet.getElementStyle(kind);
		    	if(!style.isEmpty()) {
		    		attributeStatements.append("\n\t").append(kind).append(toAttributeList(style));
		    	}
		    }
		    dot.replace(startPos, endPos, attributeStatements.toString());
		}
	}

    /**
     * ${CLASS[name=...,compartments=...,n1=v1,...]}
     */
	private static Map<String, GraphvizNode> processClasses(
		final StringBuilder dot, 
		final Model_1_0 model, 
		final GraphvizStyleSheet styleSheet
	) throws ServiceException {
		final Map<String, GraphvizNode> classNodes = new HashMap<String, GraphvizNode>();
		for (int startPos = dot.indexOf("${CLASS["); startPos >= 0; startPos = dot.indexOf("${CLASS[")) {
		    final int endPos = dot.indexOf("]}", startPos);
		    if (endPos > startPos) {
		        final GraphvizNode classNode = new GraphvizNode(styleSheet);
		        classNode.getParameters().setDefaultValue("compartments", "false");
		        classNode.getParameters().parseParameters(dot.subSequence(startPos + "${CLASS[".length(), endPos));
		        final String qualifiedName = classNode.getParameters().getValue("name");
                final ModelElement_1_0 classDef = model.getElement(qualifiedName);
                classNode.setClassDef(classDef);
                classNode.setId(classDef.getQualifiedName());
                classNodes.put(classNode.getId(), classNode);
		        dot.replace(startPos, endPos + 2, classNode.toString());
		    } else {
		        throw new ServiceException(
		            BasicException.Code.DEFAULT_DOMAIN,
		            BasicException.Code.PARSE_FAILURE,
		            "${CLASS[...]} place holder is not properly closed",
		            new BasicException.Parameter("dot", dot.substring(startPos))
		        );
		    }
		}
		return classNodes;
	}

	static String toDisplayName(String qualifiedName) {
		return qualifiedName.replace(":", "::");
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
	private static StringBuilder readTemplate(final File f) throws IOException {
		System.out.println("INFO: Processing diagram " + f.getAbsolutePath());
		System.out.flush();
		StringBuilderWriter target = new StringBuilderWriter();
		try (
			Reader source = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
		){
		    CharacterLargeObjects.streamCopy(source, 0L, target);
		    
		}
		return target.getBuilder();
	}
	
	static String toAttributeList(Map<String,String> attributes) {
		final StringBuilder attributeList = new StringBuilder();
		char delimiter='[';
		for(Map.Entry<String, String> e : attributes.entrySet()) {
			attributeList.append(delimiter).append(e.getKey()).append('=');
			delimiter = ',';
			final String value = e.getValue();
			attributeList.append(isHTML(value) ? value : quote(value));
		}
		return attributeList.toString();
	}
	
	private static boolean isHTML(String value) {
		return value.startsWith("<{") && value.endsWith("}>");
	}

	private static String quote(String value) {
		return '"' + value + "'";
	}

    /**
     * Completes diagram templates with model information.
     * 
     * @param arguments ‹source-directory› ‹destination-directory› [‹style-sheet-file›]
     */
    public static void main(
        String... arguments
    ) {
    	System.out.println("Style sheet: " + getStyleSheet(null));
        if (arguments == null || arguments.length < 2 || arguments.length > 3) {
            System.err.println("Usage: java " + Model_1DiagramDrawer.class.getName() + " ‹source-directory› ‹destination-directory› [‹style-sheet-file›]");
        } else {
            final String sourceDir = arguments[0];
            final String destDir = arguments[1];
            final File styleFile = arguments.length == 3 ? new File(arguments[2]) : null;
            try {
                System.out.println("INFO: Mapping model diagram templates from " + sourceDir + " to " + destDir);
                System.out.flush();
                drawDiagrams(
            		Model_1Factory.getModel(),
                    new File(sourceDir), 
                    new FileSink(new File(destDir)),
                    getStyleSheet(styleFile), 
                    "dot"
                    
                );
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private static GraphvizStyleSheet getStyleSheet(File styleFile) {
    	if(styleFile == null) {
    		System.out.println("INFO: Do not use a Graphviz style file");
    		return new GraphvizStyleSheet();
    	}
    	if(styleFile.exists() && styleFile.isFile()) {
    		try {
				return new GraphvizStyleSheet(styleFile.toURI().toURL());
			} catch (Exception e) {
				System.err.println("WARNING: Unable to read " + styleFile);
			}
    	}
		System.out.println("INFO: falling back to default-style-sheet.gvs");
    	return new GraphvizStyleSheet(
    		Resources.getResource("org/openmdx/application/mof/mapping/pimdoc/default-style-sheet.gvs")
    	);
    }
        
}
