/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Node
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
import java.util.ArrayList;
import java.util.List;

import org.omg.mof.cci.DirectionKind;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.PIMDocFileType;
import org.openmdx.kernel.exception.BasicException;

/**
 * Graphviz Node
 */
class GraphvizNode {

    GraphvizNode(GraphvizStyle styleSheet, Sink sink) {
    	this.sink = sink;
		this.parameters = new GraphvizAttributes(styleSheet, "_class", "name", "compartments");
	}

    private String id;
    private ModelElement_1_0 elementDef;
    private final GraphvizAttributes parameters;
    private final Sink sink;
        
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
     * Retrieve nodeDef.
     *
     * @return Returns the nodeDef.
     */
    public ModelElement_1_0 getElementDef() {
        return this.elementDef;
    }

    /**
     * Set elementDef.
     * 
     * @param elementDef
     *            The elementDef to set.
     */
    public void setElementDef(ModelElement_1_0 elementDef) {
        this.elementDef = elementDef;
    }

    private String defaultClass() {
		return
			this.elementDef.isAliasType() ? decorate("alias") :
			this.elementDef.isExceptionType() ? decorate("exception") :
			this.elementDef.isPrimitiveType() ? decorate("primitive") :
			this.elementDef.isStructureType() ? decorate("structure") :
			this.elementDef.isClassType() ? decorate("class") :
			null;
    }
    
    private String decorate(String type) {
    	return new StringBuilder("uml_")
    		.append(type)
    		.append(isLocal() ? " uml_declared_" : " uml_imported_")
    		.append(type).toString();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            if (this.elementDef == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unknown class definition. Specify qualified class name with attribute 'name'"
                );
            } else {
            	this.parameters.setDefaultValue("_class", defaultClass());
        		this.parameters.setDefaultValue("compartments", "false");
                StringBuilder label = new StringBuilder("<{");
                appendStereotypes(label);
                appendName(label);
                appendCompartments(label);
                this.parameters.setStrictValue("label", label.append("}>").toString());
                this.parameters.setStrictValue("tooltip", GraphvizTemplates.toDisplayName(this.elementDef.getQualifiedName()));
                this.parameters.setStrictValue("href", relativeURI(elementDef).toString());
                return GraphvizTemplates.quote(this.id) + this.parameters;
            }
        } catch (Exception e) {
            throw new RuntimeServiceException(e);
        }
    }

    /**
     * Name
     */
	private void appendName(StringBuilder label) throws ServiceException {
		label.append("<b>");
		if(isAbstract()) {
			label.append("<i>").append(this.elementDef.getName()).append("</i>");
		} else { 
			label.append(this.elementDef.getName());
		}
		label.append("</b>");
	}

	/**
	 * Stereotypes
	 */
	private void appendStereotypes(StringBuilder label) {
		final List<Object> stereotypes = this.elementDef.objGetList("stereotype");
		if (!stereotypes.isEmpty()) {
		    String delimiter ="&laquo;";
		    for (Object stereotype : stereotypes) {
		        label.append(delimiter).append(stereotype);
		        delimiter = ", ";
		    }
		    label.append("&raquo;<br />");
		}
	}

    /**
     *  Compartments for attributes and operations
     */
	private void appendCompartments(StringBuilder label) throws ServiceException {
		if(isShowCompartments()) {
	        final Model_1_0 model = this.elementDef.getModel();
			this.parameters.setDefaultValue("width", "2.0");
			final double widthInInches = Double.parseDouble(this.parameters.getValue("width"));
			List<Object> containedElements = this.elementDef.objGetList("content");
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
			    label.append("|<table border=\"0\" cellspacing=\"0\" width=\"").append(Double.valueOf(75.0 * widthInInches).intValue()).append("px\">");
			    for (ModelElement_1_0 attributeDef : attributeDefs) {
			        label.append("<tr><td align=\"left\"><a href=\"").append(relativeURI(attributeDef)).append("\">");
			        label.append("+ ");
			        if(ModelHelper.isDerived(attributeDef)) label.append("/");
			        label.append(attributeDef.getName()).append(" : ").append(model.getElementType(attributeDef).getQualifiedName());
			        Multiplicity multiplicity = ModelHelper.getMultiplicity(attributeDef);
			        if (multiplicity != Multiplicity.SINGLE_VALUE) {
			            label.append(" [").append(multiplicity).append("]");
			        }
			        label.append("</a></td></tr>");
			    }
			    label.append("</table>");
			}
			if (!operationDefs.isEmpty()) {
			    label.append("|<table border=\"0\" cellspacing=\"0\" width=\"").append(Double.valueOf(75.0 * widthInInches).intValue()).append("px\">");
			    for (ModelElement_1_0 operationDef : operationDefs) {
			        label.append("<tr><td align=\"left\"><a href=\"").append(relativeURI(operationDef)).append("\">");
			        label.append("+ ").append(operationDef.getName()).append("(");
			        List<Object> params = operationDef.objGetList("content");
			        String sep = "";
			        ModelElement_1_0 returnParamDef = null;
			        for (Object param : params) {
			            ModelElement_1_0 paramDef = model.getElement(param);
			            Object direction = paramDef.objGetValue("direction");
			            if (DirectionKind.RETURN_DIR.equals(direction)) {
			                returnParamDef = paramDef;
			            } else if (DirectionKind.IN_DIR.equals(direction)) {
			                label.append(sep).append(paramDef.getName()).append(" : ").append(model.getElementType(paramDef).getQualifiedName());
			                sep = ", ";
			            }
			        }
			        label.append(") : ");
			        label.append(returnParamDef == null ? "void" : model.getElementType(returnParamDef).getQualifiedName());
			        label.append("</a></td></tr>");
			    }
			    label.append("</table>");
			}
		}
	}

	private boolean isAbstract() throws ServiceException {
		return Boolean.TRUE.equals(this.elementDef.isAbstract());
	}
	
    /**
     * Retrieve showCompartments.
     *
     * @return the showCompartments value.
     */
    private boolean isShowCompartments() {
        return Boolean.parseBoolean(this.parameters.getValue("compartments"));
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
	    	return this.sink.relativize(new URI(getPath(element).toString()));
		} catch (ServiceException | URISyntaxException exception) {
			throw new RuntimeServiceException(exception);
    	}
    }

    /** 
     * Determines whether the node's model element an the diagram belong to the same package
     * 
     * @return {@code true} if the node's model element an the diagram belong to the same package
     */
    private boolean isLocal() {
    	return this.relativeURI(this.elementDef).getPath().indexOf('/') < 0;
    }
    
   /**
	* 
	* Provide the path (using HTML entries)
	* 
	* @param element the model element used to derive the path
	* 
	* @return the path
	* 
	* @throws ServiceException in case of failure
	*/
	private StringBuilder getPath(ModelElement_1_0 element) throws ServiceException{
		if(element.isAttributeType() || element.isStructureFieldType() || element.isOperationType()) {
			return getPath(element.getModel().getElement(element.getContainer()))
				.append('#')
				.append(element.getName());
		} else {
			return new StringBuilder("/")
				.append(element.getModel().toJavaPackageName(element, null).replace('.', '/'))
				.append('/').append(element.getName()).append(PIMDocFileType.TEXT.extension());
		}
	}
}