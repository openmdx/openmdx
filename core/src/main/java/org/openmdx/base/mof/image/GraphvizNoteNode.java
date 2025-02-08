/*
 * ====================================================================
 * Project: openMDX, http://www.openmdx.org
 * Description: Graphviz Note Node
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graphviz Note Node
 */
class GraphvizNoteNode extends GraphvizNode {

    GraphvizNoteNode(
        GraphvizStyle styleSheet,
        Sink sink
    ) {
        super(
            new GraphvizAttributes(styleSheet, "_note", "name"),
            sink
        );
    }

    @Override
    protected String defaultClass(){
        return "uml_note";
    }

    @Override
    public String getId() {
        return super.getId() + "__NOTE";
    }

    @Override
    protected String createLabel() throws ServiceException {
        final StringBuilder label = new StringBuilder("<{");
        appendTitle(label);
        appendCompartments(label);
        return label.append("}>").toString();
    }

    private void appendTitle(
        StringBuilder label
    ) throws ServiceException {
        final List<String> annotations = getAnnotations(getElementDef());
        if (annotations.isEmpty()) {
            label
                .append("<b><i>\n\t")
                .append(getElementDef().getName())
                .append("\n</i></b><br align=\"center\"/>");
        } else {
            label
                .append("<b><i>\n\t")
                .append(annotations.get(0))
                .append("\n</i></b><br align=\"center\"/>");
            annotations.subList(1, annotations.size()).forEach(
        annotation -> label
                .append("<i>")
                .append(annotation)
                .append("</i>")
                .append("<br align=\"left\"/>")
            );
        }
    }

    private List<String> getAnnotations(
        ModelElement_1_0 element
    ) {
        final String value = (String) element.objGetValue("annotation");
        final List<String> annotations;
        if (isEmpty(value)) {
            annotations = Collections.emptyList();
        } else {
            annotations = new ArrayList<>();
            for (String annotation : value.split("(\\n|<p>|<p/>)+")) {
                annotations.add(annotation.trim());
            }
        }
        return annotations;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    protected void appendAttributeCompartments(
        StringBuilder label,
        List<ModelElement_1_0> attributeDefs,
        double widthInInches
    ) throws ServiceException {
        appendCompartments(label, attributeDefs, widthInInches, true);
    }

    @Override
    protected void appendOperationCompartments(
        StringBuilder label,
        List<ModelElement_1_0> operationDefs,
        double widthInInches
    ) throws ServiceException {
        appendCompartments(label, operationDefs, widthInInches, false);
    }

    private void appendCompartments(
        StringBuilder label,
        List<ModelElement_1_0> elements,
        double widthInInches,
        boolean alignTop
    ) throws ServiceException {
        if (!elements.isEmpty()) {
            label
                .append("\n|\n<table border=\"0\" cellspacing=\"0\" width=\"")
                .append(Double.valueOf(75.0 * widthInInches).intValue())
                .append("px\">");
            for (ModelElement_1_0 attributeDef : elements) {
                label
                    .append("\n\t<tr><td valign=\"top\" align=\"left\" href=\"")
                    .append(relativeURI(attributeDef))
                    .append("\" tooltip=\"")
                    .append(GraphvizAttributes.getDisplayName(attributeDef))
                    .append("\">+")
                    .append(ModelHelper.isDerived(attributeDef) ? "/" : "")
                    .append(attributeDef.getName())
                    .append("</td>")
                    .append(alignTop ? "<td align=\"left\" valign=\"top\" >" : "<td align=\"left\">");
                appemdAnnotations(label, attributeDef);
                label.append("</td></tr>");
            }
            label.append("\n</table>");
        }
    }

    private void appemdAnnotations(
        StringBuilder label,
        ModelElement_1_0 attributeDef
    ) {
        List<String> annotations = getAnnotations(attributeDef);
        if (!annotations.isEmpty()) {
            label.append("<i>");
            annotations.forEach(
        annotation -> label
                .append(annotation)
                .append("<br align=\"left\"/>")
            );
            label.append("</i>");
        }
    }

}