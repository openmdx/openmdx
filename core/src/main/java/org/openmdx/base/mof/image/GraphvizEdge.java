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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.kernel.exception.BasicException;

/**
 * Graphviz Edge
 */
public class GraphvizEdge {

	GraphvizEdge(GraphvizStyle styleSheet, Sink sink) {
		this.parameters = new GraphvizAttributes(styleSheet, "_class", "name");
        this.parameters.setDefaultValue("style", "");
        this.sink = sink;
        this.strangeSpline = STRANGE_SPLINES.contains(styleSheet.getSplines());
	}
	
    private String id;
    private ModelElement_1_0 associationDef;
    private ModelElement_1_0 fieldDef;
    private final GraphvizAttributes parameters;
    private final Sink sink;
    private final boolean strangeSpline;
    
    private static final Set<Splines> STRANGE_SPLINES = EnumSet.of(Splines.CURVED, Splines.ORTHO);
	
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
     * Get arrow for given association ends.
     */
    private String getArrow(
        ModelElement_1_0 end1,
        ModelElement_1_0 end2
    ) throws ServiceException {
        if (isSharedAggregation(end1)) {
            return "odiamond";
        } else if (isCompositeAggregation(end1)) {
            return "diamond";
        } else if (!isAggregation(end1) && hasPrimitiveQualifier(end1)) {
            return "none";
        } else if (isNavigable(end2)) {
        	return isNavigable(end1) ? "none" : "vee";
        } else {
            return "dot";
        }
    }

	private boolean hasPrimitiveQualifier(ModelElement_1_0 end) throws ServiceException {
		final List<Object> qualifierTypes = end.objGetList("qualifierType");
		return qualifierTypes.size() == 1 && end.getModel().getElement(qualifierTypes.get(0)).isPrimitiveType();
	}

	private String getEndLabel(ModelElement_1_0 end1, final ModelElement_1_0 end2) throws ServiceException {
    	final Model_1_0 model = this.associationDef.getModel();
		final StringBuilder label = new StringBuilder("<<table border=\"0\">");
		final List<Object> qualifierNames = end1.objGetList("qualifierName");
		final List<Object> qualifierTypes = end1.objGetList("qualifierType");
		for(int i = 0; i < qualifierNames.size(); i++) {
			label
				.append("\n\t<tr>\n\t\t<td border=\"1\">")
				.append(qualifierNames.get(i))
				.append(" : ")
				.append(GraphvizAttributes.getDisplayName(model.getElement(qualifierTypes.get(0))))
				.append("</td>\n\t</tr>");
		}
		label
			.append("\n\t<tr>\n\t\t<td>");
		if(isNavigable(end2)) {
			label.append("+");
		}
		label
			.append(end2.getName())
			.append("<br/>")
			.append(getCardinality(end2))
			.append("</td>\n\t</tr>");
		return label
			.append("\n</table>\n>")
			.toString();
	}
	
	private String getCardinality(ModelElement_1_0 element) throws ServiceException {
		final Multiplicity multiplicity = ModelHelper.getMultiplicity(element);
		switch(multiplicity) {
			case OPTIONAL:
				return "0..1";
			case SINGLE_VALUE: 
				return "1";
			case SET:
				return "* {unqiue}";
			case LIST:
				return "* {ordered}";
			default: 
				return "{" + multiplicity.code() + "}";
		}
	}

	private boolean isAggregation(ModelElement_1_0 end) throws ServiceException {
		return !AggregationKind.NONE.equals(end.getAggregation());
	}
	
	private boolean isSharedAggregation(ModelElement_1_0 end) throws ServiceException {
		return AggregationKind.SHARED.equals(end.getAggregation());
	}

	private boolean isCompositeAggregation(ModelElement_1_0 end) throws ServiceException {
		return AggregationKind.COMPOSITE.equals(end.getAggregation());
	}

	private Ends getEnds() throws ServiceException {
    	final Model_1_0 model = this.associationDef.getModel();
        final List<Object> content = this.associationDef.objGetList("content");
        final ModelElement_1_0 end0 = model.getElement(content.get(0));
		final ModelElement_1_0 end1 = model.getElement(content.get(1));
		if(isAggregation(end1)) {
			return new Ends(end0, end1, isDirectionToBeInverted(end0, end1));
		}
		if(isAggregation(end0)) {
			return new Ends(end1, end0, isDirectionToBeInverted(end1, end0));
		}
		if(hasPrimitiveQualifier(end1)) {
			return new Ends(end0, end1, false);
		}
		if(hasPrimitiveQualifier(end0)) {
			return new Ends(end1, end0, false);
		}
		if(!isNavigable(end0) && isNavigable(end1)) {
			return new Ends(end0, end1, isDirectionToBeInverted(end0, end1));
		}
		if(isNavigable(end0) && !isNavigable(end1)) {
			return new Ends(end1, end0, isDirectionToBeInverted(end1, end0));
		}
		return new Ends(end0, end1, false);
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
        	if(hasStyleInvisible()) {
        		return "";
        	} else if (this.fieldDef != null) {
                final Model_1_0 model = this.fieldDef.getModel();
                final ModelElement_1_0 exposedEndType = model.getElement(this.fieldDef.getContainer());
                final ModelElement_1_0 referencedEndType = this.fieldDef.getDereferencedType();
                this.parameters.setDefaultValue("_class", "uml_field");
                this.parameters.setDefaultValue("color", "#0000FF");
                this.parameters.setDefaultValue("constraint", "false");
                this.parameters.setDefaultValue("labeldistance", "2.5");
                this.parameters.setStrictValue("dir","both");
                this.parameters.setStrictValue("xlabel", "");
                this.parameters.setStrictValue("tooltip", "");
                this.parameters.setStrictValue("taillabel", "1");
                this.parameters.setStrictValue("headlabel", "<+" + this.fieldDef.getName() + "<br/>" + getCardinality(this.fieldDef) + ">");
                this.parameters.setStrictValue("headhref", relativeURI(this.fieldDef).toString());
                this.parameters.setStrictValue("headtooltip", GraphvizAttributes.getDisplayName(this.fieldDef));
                this.parameters.setStrictValue("arrowhead", "vee");
                this.parameters.setStrictValue("arrowtail", "dot");
                return strangeSpline ? createEdge(referencedEndType, exposedEndType) : createEdge(exposedEndType, referencedEndType);
            } else if (this.associationDef != null) {
            	final Ends ends = getEnds();
            	final Model_1_0 model = this.associationDef.getModel();
                final ModelElement_1_0 exposedEndType = model.getElementType(ends.exposedEnd);
                final ModelElement_1_0 referencedEndType = model.getElementType(ends.referencedEnd);
                this.parameters.setDefaultValue("_class", getDefaultAssociationClass(ends.exposedEnd, ends.referencedEnd));
                this.parameters.setDefaultValue("color","#0000FF");
                this.parameters.setDefaultValue("constraint","false");
                this.parameters.setDefaultValue("labeldistance","2.5");
                this.parameters.setStrictValue("dir","both");
                this.parameters.setStrictValue("xlabel", this.associationDef.getName());
                this.parameters.setStrictValue("tooltip", GraphvizAttributes.getDisplayName(this.associationDef));
                this.parameters.setStrictValue("headhref", relativeURI(ends.referencedEnd).toString());
                this.parameters.setStrictValue("headtooltip", GraphvizAttributes.getDisplayName(ends.referencedEnd));
                if(isNavigable(ends.exposedEnd)) {
                    this.parameters.setStrictValue("tailhref", relativeURI(ends.exposedEnd).toString());
                    this.parameters.setStrictValue("tailtooltip", GraphvizAttributes.getDisplayName(ends.exposedEnd));
                }
                this.parameters.setStrictValue("headlabel",getEndLabel(ends.exposedEnd, ends.referencedEnd));
                this.parameters.setStrictValue("taillabel",getEndLabel(ends.referencedEnd, ends.exposedEnd));
                this.parameters.setStrictValue("arrowhead",getArrow(ends.exposedEnd, ends.referencedEnd));
                this.parameters.setStrictValue("arrowtail",getArrow(ends.referencedEnd, ends.exposedEnd));
                return ends.invertDirection ? createEdge(referencedEndType, exposedEndType) : createEdge(exposedEndType, referencedEndType);
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
		final StringBuilder edge = new StringBuilder();
		GraphvizAttributes.appendQuoted(edge, exposedEndType.getQualifiedName());
		edge.append(" -> ");
		GraphvizAttributes.appendQuoted(edge, referencedEndType.getQualifiedName());
		this.parameters.appendTo(edge, "\t");
		return edge.toString();
	}

	private boolean isNavigable(final ModelElement_1_0 end) {
		return Boolean.TRUE.equals(end.objGetValue("isNavigable"));
		
	}

	private boolean hasStyleInvisible() {
		return "invis".equals(this.parameters.getValue("style"));
	}

	private static class Ends {
		
		Ends(ModelElement_1_0 exposedEnd, ModelElement_1_0 referencedEnd, boolean invertDirection) {
			this.exposedEnd = exposedEnd;
			this.referencedEnd = referencedEnd;
			this.invertDirection = invertDirection;
		}
		
		final ModelElement_1_0 exposedEnd;
		final ModelElement_1_0 referencedEnd;
		final boolean invertDirection;
	}
	
	private boolean isDirectionToBeInverted(ModelElement_1_0 end0, ModelElement_1_0 end1) {
		return strangeSpline && (isNavigable(end0) != isNavigable(end1));
	}
	
	/**
	 * 
	 * Provide the relative URI (using HTML entries)
	 * 
	 * @param element the model element used to derive the relative URI
	 * 
	 * @return the relative URI
	 */
    private URI relativeURI(ModelElement_1_0 element){
    	try {
    		return this.sink.relativize(GraphvizAttributes.getURI(element));
		} catch (ServiceException | URISyntaxException exception) {
			throw new RuntimeServiceException(exception);
    	}
    }

}