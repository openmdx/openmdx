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
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * Graphviz Node
 */
abstract class GraphvizNode {

    protected GraphvizNode(
        GraphvizAttributes parameters,
        Sink sink
    ) {
        this.parameters = parameters;
        this.sink = sink;
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
    public ModelElement_1_0 getElementDef() throws ServiceException {
        if(this.elementDef == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unknown class definition. Specify qualified class name with attribute 'name'"
            );
        } else {
            return this.elementDef;
        }
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

    public String toString() {
        try {
            this.parameters.setDefaultValue("_class", defaultClass());
            this.parameters.setStrictValue("tooltip", createTooltip());
            this.parameters.setStrictValue("href", createHref());
            this.parameters.setStrictValue("label", createLabel());
            return createNode();
        } catch (Exception e) {
            throw new RuntimeServiceException(e);
        }
    }

    private String createNode() {
        final StringBuilder node = new StringBuilder();
        GraphvizAttributes.appendQuoted(node, getId());
        this.parameters.appendTo(node, "\t\t");
        return node.toString();
    }

    private String createTooltip() throws ServiceException {
        return GraphvizAttributes.getDisplayName(getElementDef());
    }

    private String createHref() throws ServiceException {
        return relativeURI(getElementDef()).toString();
    }

    /**
     * Provide the relative URI (using HTML entries)
     *
     * @param element
     *          the model element used to derive the relative URI
     *
     * @return the relative URI
     */
    protected URI relativeURI(
        ModelElement_1_0 element
    ) throws ServiceException {
        try {
            return this.sink.relativize(GraphvizAttributes.getURI(element));
        } catch (URISyntaxException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Compartments for attributes and operations
     */
    protected void appendCompartments(
        StringBuilder label
    ) throws ServiceException {
        this.parameters.setDefaultValue("width", "2.0");
        final double widthInInches = Double.parseDouble(this.parameters.getValue("width"));
        final List<Object> containedElements = getElementDef().objGetList("content");
        final List<ModelElement_1_0> attributeDefs = new ArrayList<>();
        final List<ModelElement_1_0> operationDefs = new ArrayList<>();
        final Model_1_0 model = getElementDef().getModel();
        for (Object contained : containedElements) {
            final ModelElement_1_0 element = model.getElement(contained);
            if (
                model.isAttributeType(element) ||
                (model.isStructureFieldType(element) && !element.getDereferencedType().isStructureType())
            ) {
                attributeDefs.add(element);
            } else if (model.isOperationType(element)) {
                operationDefs.add(element);
            }
        }
        appendAttributeCompartments(label, attributeDefs, widthInInches);
        appendOperationCompartments(label, operationDefs, widthInInches);
    }

    protected abstract void appendAttributeCompartments(
        StringBuilder label,
        List<ModelElement_1_0> attributeDefs,
        double widthInInches
    ) throws ServiceException;

    protected abstract void appendOperationCompartments(
        StringBuilder label,
        List<ModelElement_1_0> operationDefs,
        double widthInInches
    ) throws ServiceException;

    protected abstract String defaultClass() throws ServiceException;

    protected abstract String createLabel() throws ServiceException;

}
