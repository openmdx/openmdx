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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omg.mof.cci.DirectionKind;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObjects;

/**
 * Diagram Drawer
 */
public class Model_1DiagramDrawer {

    /**
     * GraphvizNode
     *
     */
    public static class GraphvizNode {

        public Map<String, String> getParameters() {
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
                    String s = "\"" + this.id + "\"[";
                    String label = "<{";
                    // Stereotypes
                    List<Object> stereotypes = this.classDef.objGetList("stereotype");
                    if (!stereotypes.isEmpty()) {
                        label += "&lt;&lt;";
                        for (Object stereotype : stereotypes) {
                            label += stereotype.toString();
                        }
                        label += "&gt;&gt;<br />";
                    }
                    // Name
                    boolean isAbstract = Boolean.TRUE.equals(this.classDef.isAbstract());
                    label += "<b>" + (isAbstract ? "<i>" : "") + this.classDef.getName() + (isAbstract ? "</i>" : "") + "</b>";
                    // Compartments for attributes and operations
                    if (this.showCompartments) {
                        if (this.classDef != null) {
                            double widthInInches = 2.0;
                            if (this.parameters.containsKey("width")) {
                                widthInInches = Double.parseDouble(this.parameters.get("width"));
                            }
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
                    s += "label=" + label;
                    s += ",tooltip=\"" + this.classDef.getQualifiedName() + "\"";
                    for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                        s += "," + parameter.getKey() + "=" + parameter.getValue();
                    }
                    s += "]";
                    return s;
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
            return this.showCompartments;
        }

        /**
         * Set showCompartments.
         * 
         * @param showCompartments
         *            The showCompartments to set.
         */
        public void setShowCompartments(boolean showCompartments) {
            this.showCompartments = showCompartments;
        }

        private String id;
        private ModelElement_1_0 classDef;
        private boolean showCompartments;
        private Map<String, String> parameters = new HashMap<String, String>();
    }

    /**
     * GraphvizEdge
     *
     */
    public static class GraphvizEdge {

        public Map<String, String> getParameters() {
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
         * 
         * @param associationEnd
         * @return
         * @throws ServiceException
         */
        private String getArrowhead(
            ModelElement_1_0 end1,
            ModelElement_1_0 end2
        )
            throws ServiceException {
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
                if (this.fieldDef != null) {
                    Model_1_0 model = this.fieldDef.getModel();
                    ModelElement_1_0 exposedEndType = model.getElement(this.fieldDef.getContainer());
                    ModelElement_1_0 referencedEndType = this.fieldDef.getDereferencedType();
                    String dir = "forward";
                    if (this.parameters.containsKey("dir")) {
                        dir = this.parameters.get("dir");
                        this.parameters.remove("dir");
                    }
                    String s = ("forward".equals(dir)
                        ? "\"" + exposedEndType.getQualifiedName() + "\" -> \"" + referencedEndType.getQualifiedName()
                        : "\"" + referencedEndType.getQualifiedName() + "\" -> \"" + exposedEndType.getQualifiedName()) +
                        "\" [";
                    s += "label=\"\"";
                    s += ",tooltip=\"\"";
                    s += ",headlabel=\"[1..1]\"";
                    s += ",taillabel=\"" + this.fieldDef.getName() + " [" + ModelHelper.getMultiplicity(this.fieldDef) + "]\"";
                    s += ",arrowhead=tee";
                    s += ",arrowtail=vee";
                    s += ",color=\"#0000FF\"";
                    boolean hasContraint = false;
                    boolean hasLabelDistance = false;
                    boolean hasStyleInvis = false;
                    for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                        s += "," + parameter.getKey() + "=" + parameter.getValue();
                        hasContraint = "constraint".equals(parameter.getKey());
                        hasLabelDistance = "labeldistance".equals(parameter.getKey());
                        hasStyleInvis = "style".equals(parameter.getKey()) && "invis".equals(parameter.getValue());
                    }
                    if (!hasContraint) {
                        s += ",contraint=false";
                    }
                    if (!hasLabelDistance) {
                        s += ",labeldistance=3";
                    }
                    s += "]";
                    return hasStyleInvis ? "" : s;
                } else if (this.associationDef != null) {
                    Model_1_0 model = this.associationDef.getModel();
                    ModelElement_1_0 end1 = model.getElement(this.associationDef.objGetList("content").get(0));
                    ModelElement_1_0 end2 = model.getElement(this.associationDef.objGetList("content").get(1));
                    ModelElement_1_0 referencedEnd = null;
                    ModelElement_1_0 exposedEnd = null;
                    if (Boolean.TRUE.equals(end1.objGetValue("isNavigable"))) {
                        referencedEnd = end1;
                        exposedEnd = end2;
                    } else {
                        referencedEnd = end2;
                        exposedEnd = end1;
                    }
                    ModelElement_1_0 referencedEndType = model.getElementType(referencedEnd);
                    ModelElement_1_0 exposedEndType = model.getElementType(exposedEnd);
                    String dir = "forward";
                    if (this.parameters.containsKey("dir")) {
                        dir = this.parameters.get("dir");
                        this.parameters.remove("dir");
                    }
                    String s = ("forward".equals(dir)
                        ? "\"" + exposedEndType.getQualifiedName() + "\" -> \"" + referencedEndType.getQualifiedName()
                        : "\"" + referencedEndType.getQualifiedName() + "\" -> \"" + exposedEndType.getQualifiedName()) +
                        "\" [";
                    s += "label=\"" + this.associationDef.getName() + "\"";
                    s += ",tooltip=\"" + this.associationDef.getQualifiedName() + "\"";
                    s += ",headlabel=\"" + exposedEnd.getName() + " [" + ModelHelper.getMultiplicity(exposedEnd) + "]\"";
                    s += ",taillabel=\"" + referencedEnd.getName() + " [" + ModelHelper.getMultiplicity(referencedEnd) + "]\"";
                    s += ",arrowhead=" + this.getArrowhead(referencedEnd, exposedEnd);
                    s += ",arrowtail=" + this.getArrowhead(exposedEnd, referencedEnd);
                    s += ",color=\"#0000FF\"";
                    boolean hasContraint = false;
                    boolean hasLabelDistance = false;
                    boolean hasStyleInvis = false;
                    for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                        s += "," + parameter.getKey() + "=" + parameter.getValue();
                        hasContraint = "constraint".equals(parameter.getKey());
                        hasLabelDistance = "labeldistance".equals(parameter.getKey());
                        hasStyleInvis = "style".equals(parameter.getKey()) && "invis".equals(parameter.getValue());
                    }
                    if (!hasContraint) {
                        s += ",contraint=false";
                    }
                    if (!hasLabelDistance) {
                        s += ",labeldistance=3";
                    }
                    s += "]";
                    return hasStyleInvis ? "" : s;
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

        private String id;
        private ModelElement_1_0 associationDef;
        private ModelElement_1_0 fieldDef;
        private final Map<String, String> parameters = new HashMap<String, String>();
    }

    /**
     * Draw given diagrams and store in destination directory.
     * 
     * @param names
     * @param destDir
     * @throws ServiceException
     */
    static void drawDiagrams(
        File sourceDir,
        File destDir
    )
        throws ServiceException {
        Model_1_0 model = Model_1Factory.getModel();
        try {
            if (sourceDir.exists()) {
                final String[] sourceDirs = sourceDir.list();
                if (sourceDirs != null)
                    for (String name : sourceDir.list()) {
                        File f = new File(sourceDir, name);
                        if (f.isDirectory()) {
                            drawDiagrams(
                                f,
                                new File(destDir, f.getName())
                            );
                        } else if (name.endsWith(".dott")) {
                            System.out.println("INFO: Processing diagram " + f.getAbsolutePath());
                            System.out.flush();
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            try (FileInputStream source = new FileInputStream(f)) {
                                BinaryLargeObjects.streamCopy(source, 0L, bos);
                            }
                            String dot = bos.toString("UTF-8");
                            Map<String, GraphvizNode> classNodes = new HashMap<String, GraphvizNode>();
                            // ${CLASS[name=...,compartments=...,n1=v1,...]}
                            while (dot.indexOf("${CLASS[") >= 0) {
                                int startPos = dot.indexOf("${CLASS[");
                                int endPos = dot.indexOf("]}", startPos);
                                if (endPos > startPos) {
                                    GraphvizNode classNode = new GraphvizNode();
                                    String[] parameters = dot.substring(startPos + 8, endPos).split(",");
                                    for (String parameter : parameters) {
                                        String[] nv = parameter.split("=");
                                        if ("name".equals(nv[0])) {
                                            ModelElement_1_0 classDef = model.getElement(nv[1]);
                                            classNode.setClassDef(classDef);
                                            classNode.setId(classDef.getQualifiedName());
                                            classNodes.put(
                                                classNode.getId(),
                                                classNode
                                            );
                                        } else if ("compartments".equals(nv[0])) {
                                            classNode.setShowCompartments(Boolean.parseBoolean(nv[1]));
                                        } else {
                                            classNode.getParameters().put(
                                                nv[0],
                                                nv[1]
                                            );
                                        }
                                    }
                                    dot = dot.substring(0, startPos) +
                                        (classNode == null ? "" : classNode.toString()) +
                                        dot.substring(endPos + 2);
                                } else {
                                    throw new ServiceException(
                                        BasicException.Code.DEFAULT_DOMAIN,
                                        BasicException.Code.PARSE_FAILURE,
                                        "${CLASS[...]} place holder is not properly closed",
                                        new BasicException.Parameter("dot", dot.substring(startPos))
                                    );
                                }
                            }
                            // ${INSTANCE_OF}
                            if (dot.indexOf("${INSTANCE_OF}") >= 0) {
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
                                dot = dot.replace("${INSTANCE_OF}", instanceOfEdges);
                            }
                            // ${ASSOCIATION[name=...,n1=v1,...]}
                            Map<String, GraphvizEdge> associationNodes = new HashMap<String, GraphvizEdge>();
                            while (dot.indexOf("${ASSOCIATION[") >= 0) {
                                int startPos = dot.indexOf("${ASSOCIATION[");
                                int endPos = dot.indexOf("]}", startPos);
                                boolean isWildcard = false;
                                if (endPos > startPos) {
                                    GraphvizEdge associationNode = new GraphvizEdge();
                                    String[] parameters = dot.substring(startPos + 14, endPos).split(",");
                                    for (String parameter : parameters) {
                                        String[] nv = parameter.split("=");
                                        if ("name".equals(nv[0])) {
                                            isWildcard = "*".equals(nv[1]);
                                            if (!isWildcard) {
                                                ModelElement_1_0 associationDef = model.getElement(nv[1]);
                                                associationNode.setId(associationDef.getQualifiedName());
                                                associationNode.setAssociationDef(associationDef);
                                                associationNodes.put(
                                                    associationNode.getId(),
                                                    associationNode
                                                );
                                            }
                                        } else {
                                            associationNode.getParameters().put(
                                                nv[0],
                                                nv[1]
                                            );
                                        }
                                    }
                                    if (isWildcard) {
                                        break;
                                    } else {
                                        dot = dot.substring(0, startPos) +
                                            (associationNode == null ? "" : associationNode.toString()) +
                                            dot.substring(endPos + 2);
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
                            // ${ASSOCIATION[name=*]}
                            if (dot.indexOf("${ASSOCIATION[name=*]}") >= 0) {
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
                                            GraphvizEdge associationNode = new GraphvizEdge();
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
                                                    GraphvizEdge fieldNode = new GraphvizEdge();
                                                    fieldNode.setId(fieldDef.getQualifiedName());
                                                    fieldNode.setFieldDef(fieldDef);
                                                    fieldNode.getParameters().put("minlen", "3");
                                                    associationEdges += "\n\t" + fieldNode.toString() + ";";
                                                }
                                            }
                                        }
                                    }
                                }
                                dot = dot.replace("${ASSOCIATION[name=*]}", associationEdges);
                            }
                            destDir.mkdirs();
                            File destFile = new File(destDir, name.replace(".dott", ".dot"));
                            try (FileOutputStream destOs = new FileOutputStream(destFile)) {
                                BinaryLargeObjects.streamCopy(
                                    new ByteArrayInputStream(dot.getBytes("UTF-8")),
                                    0L,
                                    destOs
                                );
                            }

                        }
                    }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Completes diagram templates with model information.
     * 
     * @param diagram
     *            source dir
     * @param diagram
     *            destination dir
     */
    public static void main(
        String... arguments
    ) {
        if (arguments == null || arguments.length != 2) {
            System.err.println("Usage: java " + Model_1DiagramDrawer.class.getName() + " <sourceDir> <destDir>");
        } else {
            String sourceDir = arguments[0];
            String destDir = arguments[1];
            try {
                System.out.println("INFO: Mapping model diagram templates from " + sourceDir + " to " + destDir);
                System.out.flush();
                drawDiagrams(
                    new File(sourceDir),
                    new File(destDir)
                );
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(-1);
            }
        }
    }

}
