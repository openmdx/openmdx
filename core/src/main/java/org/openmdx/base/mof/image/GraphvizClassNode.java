/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Class Node
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

import java.util.List;

import org.omg.mof.cci.DirectionKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Multiplicity;

/**
 * Graphviz Class Node
 */
class GraphvizClassNode extends GraphvizNode {

	GraphvizClassNode(
		GraphvizStyle styleSheet,
		Sink sink
	) {
		super(
			new GraphvizAttributes(styleSheet, "_class", "name", COMPARTMENTS),
			sink
		);
	}

	/**
	 * This parameter tells, whether structural and behavioural feature compartments shall be shown or not
	 */
	private static final String COMPARTMENTS = "compartments";

	public String toString() {
		getParameters().setDefaultValue(COMPARTMENTS, Boolean.FALSE.toString());
		return super.toString();
	}

	/**
	 * Tells, whether compartments shall be shown
	 *
	 * @return {@code true} if the compartments shall be shown
	 */
	private boolean showCompartments() {
		return Boolean.parseBoolean(getParameters().getValue(COMPARTMENTS));
	}

	@Override
    protected String defaultClass() throws ServiceException {
		ModelElement_1_0 element = getElementDef();
		return
			element.isAliasType() ? decorate("alias") :
			element.isExceptionType() ? decorate("exception") :
			element.isPrimitiveType() ? decorate("primitive") :
			element.isStructureType() ? decorate("structure") :
			element.isClassType() ? decorate("class") :
			null;
    }

	private String decorate(String type) throws ServiceException {
    	return new StringBuilder("uml_")
    		.append(type)
    		.append(isLocal() ? " declared_" : " imported_")
    		.append(type).toString();
    }

	protected String createLabel() throws ServiceException {
		final StringBuilder label = new StringBuilder("<{");
		appendStereotypes(label);
		appendName(label);
		if(showCompartments()) {
			appendCompartments(label);
		}
        return label.append("}>").toString();
	}

	/**
     * Name
     */
	private void appendName(
		StringBuilder label
	) throws ServiceException {
		label.append("<b>\n\t");
		if(isAbstract()) {
			label
				.append("<i>\n\t\t")
				.append(getElementDef().getName())
				.append("\n\t</i>");
		} else { 
			label.append(getElementDef().getName());
		}
		label.append("\n</b>");
	}

	/**
	 * Stereotypes
	 */
	private void appendStereotypes(StringBuilder label) throws ServiceException {
		final List<Object> stereotypes = getElementDef().objGetList("stereotype");
		if (!stereotypes.isEmpty()) {
		    String delimiter = "&laquo;";
		    for (Object stereotype : stereotypes) {
		        label
					.append(delimiter)
					.append(stereotype);
		        delimiter = ", ";
		    }
		    label.append("&raquo;\n<br/>\n");
		}
	}

	@Override
	protected void appendAttributeCompartments(StringBuilder label, List<ModelElement_1_0> attributeDefs, double widthInInches) throws ServiceException {
		if (!attributeDefs.isEmpty()) {
			label.append("\n|\n<table border=\"0\" cellspacing=\"0\" width=\"").append(Double.valueOf(75.0 * widthInInches).intValue()).append("px\">");
			for (ModelElement_1_0 attributeDef : attributeDefs) {
				label
					.append("\n\t<tr>\n\t\t<td align=\"left\" href=\"")
					.append(relativeURI(attributeDef))
					.append("\" tooltip=\"")
					.append(GraphvizAttributes.getDisplayName(attributeDef))
					.append("\">\n\t\t\t+")
					.append(ModelHelper.isDerived(attributeDef) ? "/" : "")
					.append(attributeDef.getName())
					.append(" : ")
					.append(getType(attributeDef));
				appendMultiplicity(label, attributeDef);
				label.append("\n\t\t</td>\n\t</tr>");
			}
			label.append("\n</table>");
		}
	}

	private static void appendMultiplicity(StringBuilder label, ModelElement_1_0 attributeDef) throws ServiceException {
		final Multiplicity multiplicity = ModelHelper.getMultiplicity(attributeDef);
		if (multiplicity != Multiplicity.SINGLE_VALUE) {
			label
				.append(" [")
				.append(multiplicity)
				.append("]");
		}
	}

	@Override
	protected void appendOperationCompartments(StringBuilder label, List<ModelElement_1_0> operationDefs, double widthInInches) throws ServiceException {
		if (!operationDefs.isEmpty()) {
			label.append("\n|\n<table border=\"0\" cellspacing=\"0\" width=\"").append(Double.valueOf(75.0 * widthInInches).intValue()).append("px\">");
			for (ModelElement_1_0 operationDef : operationDefs) {
				label
					.append("\n\t<tr>\n\t\t<td align=\"left\" href=\"")
					.append(relativeURI(operationDef))
					.append("\">\n\t\t\t")
					.append("+ ")
					.append(operationDef.getName())
					.append("(");
				List<Object> params = operationDef.objGetList("content");
				String sep = "";
				ModelElement_1_0 returnParamDef = null;
				for (Object param : params) {
					ModelElement_1_0 paramDef = operationDef.getModel().getElement(param);
					Object direction = paramDef.objGetValue("direction");
					if (DirectionKind.RETURN_DIR.equals(direction)) {
						returnParamDef = paramDef;
					} else if (DirectionKind.IN_DIR.equals(direction)) {
						label
							.append(sep)
							.append(paramDef.getName())
							.append(" : ")
							.append(getType(paramDef));
						sep = ", ";
					}
				}
				label
					.append(") : ")
					.append(returnParamDef == null ? "void" : getType(returnParamDef))
					.append("\n\t\t</td>\n\t</tr>");
			}
			label.append("\n</table>");
		}
	}

	private String getType(ModelElement_1_0 elementDef) throws ServiceException {
		return GraphvizAttributes.getDisplayName(
			elementDef.getModel().getElementType(elementDef)
		);
	}

	private boolean isAbstract() throws ServiceException {
		return Boolean.TRUE.equals(getElementDef().isAbstract());
	}

    /**
     * Determines whether the node's model element an the diagram belong to the same package
     * 
     * @return {@code true} if the node's model element an the diagram belong to the same package
     */
    private boolean isLocal() throws ServiceException {
    	return !relativeURI(getElementDef()).getPath().contains("/");
    }
    	
}