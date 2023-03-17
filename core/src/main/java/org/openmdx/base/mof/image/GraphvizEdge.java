/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Edge
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
package org.openmdx.base.mof.image;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * Graphviz Edge
 *
 */
public class GraphvizEdge {

	GraphvizEdge(GraphvizStyle styleSheet) {
		this.parameters = new GraphvizAttributes(styleSheet, "_class", "name", "dir");
		this.parameters.setDefaultValue("dir", "forward");
        this.parameters.setDefaultValue("style", "");
	}
	
    private String id;
    private ModelElement_1_0 associationDef;
    private ModelElement_1_0 fieldDef;
    private final GraphvizAttributes parameters;
	
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
        if (isSharedAggregation(end1)) {
            return "oboxodiamond";
        } else if (isCompositeAggregation(end1)) {
            return "oboxdiamond";
        } else if (AggregationKind.NONE.equals(end1.objGetValue("aggregation")) && !end1.objGetList("qualifierType").isEmpty()) {
            return "obox";
        } else if (isNavigable(end2)) {
            return "vee";
        } else {
            return "tee";
        }
    }

	private boolean isSharedAggregation(ModelElement_1_0 end) throws ServiceException {
		return AggregationKind.SHARED.equals(end.getAggregation());
	}

	private boolean isCompositeAggregation(ModelElement_1_0 end) {
		return AggregationKind.COMPOSITE.equals(end.objGetValue("aggregation"));
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
                this.parameters.setDefaultValue("_class", "uml_field");
                this.parameters.setDefaultValue("color", "#0000FF");
                this.parameters.setDefaultValue("constraint", "false");
                this.parameters.setDefaultValue("labeldistance", "3");
                this.parameters.setStrictValue("xlabel", "");
                this.parameters.setStrictValue("tooltip", "");
                this.parameters.setStrictValue("headlabel", "[1..1]");
                this.parameters.setStrictValue("taillabel", this.fieldDef.getName() + " [" + ModelHelper.getMultiplicity(this.fieldDef) + "]");
                this.parameters.setStrictValue("arrowhead", "tee");
                this.parameters.setStrictValue("arrowtail", "vee");
                return createEdge(exposedEndType, referencedEndType);
            } else if (this.associationDef != null) {
            	final Model_1_0 model = this.associationDef.getModel();
                final ModelElement_1_0 end1 = model.getElement(this.associationDef.objGetList("content").get(0));
                final ModelElement_1_0 end2 = model.getElement(this.associationDef.objGetList("content").get(1));
                final ModelElement_1_0 referencedEnd;
                final ModelElement_1_0 exposedEnd;
                if (isNavigable(end1)) {
                    referencedEnd = end1;
                    exposedEnd = end2;
                } else {
                    referencedEnd = end2;
                    exposedEnd = end1;
                }
                final ModelElement_1_0 referencedEndType = model.getElementType(referencedEnd);
                final ModelElement_1_0 exposedEndType = model.getElementType(exposedEnd);
                this.parameters.setDefaultValue("_class", getDefaultAssociationClass(referencedEnd, exposedEnd));
                this.parameters.setDefaultValue("color","#0000FF");
                this.parameters.setDefaultValue("constraint","false");
                this.parameters.setDefaultValue("labeldistance","3");
                this.parameters.setStrictValue("xlabel", this.associationDef.getName());
                this.parameters.setStrictValue("tooltip", GraphvizAttributes.getDisplayName(this.associationDef));
                this.parameters.setStrictValue("headlabel",exposedEnd.getName() + " [" + ModelHelper.getMultiplicity(exposedEnd) + "]");
                this.parameters.setStrictValue("taillabel",referencedEnd.getName() + " [" + ModelHelper.getMultiplicity(referencedEnd) + "]");
                this.parameters.setStrictValue("arrowhead",this.getArrowhead(referencedEnd, exposedEnd));
                this.parameters.setStrictValue("arrowtail",this.getArrowhead(exposedEnd, referencedEnd));
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

    private String getDefaultAssociationClass(
        ModelElement_1_0 end1,
        ModelElement_1_0 end2
    ) throws ServiceException {
    	StringBuilder defaultClass = new StringBuilder("uml_association");
    	if(isCompositeAggregation(end1) || isCompositeAggregation(end2)) {
    		defaultClass.append(" uml_composite_aggregation");
    	} else if(isSharedAggregation(end1) || isSharedAggregation(end2)) {
    		defaultClass.append(" uml_shared_aggregation");
    	}
    	return defaultClass.toString();
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
		final StringBuilder edge = new StringBuilder();
		GraphvizAttributes.appendQuoted(edge, left.getQualifiedName());
		edge.append(" -> ");
		GraphvizAttributes.appendQuoted(edge, right.getQualifiedName());
		this.parameters.appendTo(edge, "\t");
		return edge.toString();
	}

	private boolean isNavigable(final ModelElement_1_0 end1) {
		return Boolean.TRUE.equals(end1.objGetValue("isNavigable"));
	}

    private boolean isDirForward() {
    	return "forward".equals(this.parameters.getValue("dir"));
    }
    
	private boolean hasStyleInvis() {
		return "invis".equals(this.parameters.getValue("style"));
	}

}