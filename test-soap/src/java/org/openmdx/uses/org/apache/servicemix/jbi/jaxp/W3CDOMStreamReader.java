/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.jbi.jaxp;

/*
 * This implementation comes from the XFire project
 * https://svn.codehaus.org/xfire/trunk/xfire/xfire-core/src/main/org/codehaus/xfire/util/stax/
 */
import java.util.ArrayList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class W3CDOMStreamReader extends DOMStreamReader {
    private Node content;

    private Document document;

    private W3CNamespaceContext context;

    /**
     * @param element
     */
    public W3CDOMStreamReader(Element element) {
        super(new ElementFrame(element, null));

        this.document = element.getOwnerDocument();
    }

    /**
     * Get the document associated with this stream.
     * 
     * @return
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Find name spaces declaration in atrributes and move them to separate
     * collection.
     */
    protected void newFrame(ElementFrame frame) {
        Element element = getCurrentElement();
        frame.uris = new ArrayList();
        frame.prefixes = new ArrayList();
        frame.attributes = new ArrayList();

        if (context == null)
            context = new W3CNamespaceContext();

        context.setElement(element);

        NamedNodeMap nodes = element.getAttributes();

        String nsURI = element.getNamespaceURI();
        String ePrefix = element.getPrefix();
        if (ePrefix == null) {
            ePrefix = "";
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String prefix = node.getPrefix();
            String localName = node.getLocalName();
            String value = node.getNodeValue();
            String name = node.getNodeName();
            String uri = node.getNamespaceURI();

            if (prefix == null)
                prefix = "";

            if (name != null && name.equals("xmlns")) {
                frame.uris.add(value);
                frame.prefixes.add("");
            } else if (prefix.length() > 0 && prefix.equals("xmlns")) {
                frame.uris.add(value);
                frame.prefixes.add(localName);
            } else if (name.startsWith("xmlns:")) {
                prefix = name.substring(6);
                frame.uris.add(value);
                frame.prefixes.add(prefix);
            } else {
                frame.attributes.add(node);
            }
        }
    }

    protected void endElement() {
        super.endElement();
    }

    Element getCurrentElement() {
        return (Element) getCurrentFrame().element;
    }

    protected ElementFrame getChildFrame(int currentChild) {
        return new ElementFrame(getCurrentElement().getChildNodes().item(currentChild), getCurrentFrame());
    }

    protected int getChildCount() {
        return getCurrentElement().getChildNodes().getLength();
    }

    protected int moveToChild(int currentChild) {
        this.content = getCurrentElement().getChildNodes().item(currentChild);

        if (content instanceof Text)
            return CHARACTERS;
        else if (content instanceof Element)
            return START_ELEMENT;
        else if (content instanceof CDATASection)
            return CDATA;
        else if (content instanceof Comment)
            return CHARACTERS;
        else if (content instanceof EntityReference)
            return ENTITY_REFERENCE;

        throw new IllegalStateException();
    }

    public String getElementText() throws XMLStreamException {
        return getText();
    }

    public String getNamespaceURI(String prefix) {
        ElementFrame frame = getCurrentFrame();

        while (null != frame) {
            int index = frame.prefixes.indexOf(prefix);
            if (index != -1) {
                return (String) frame.uris.get(index);
            }

            frame = frame.parent;
        }

        return null;
    }

    public String getAttributeValue(String ns, String local) {
        if (ns == null || ns.equals(""))
            return getCurrentElement().getAttribute(local);
        else
            return getCurrentElement().getAttributeNS(ns, local);
    }

    public int getAttributeCount() {
        return getCurrentFrame().attributes.size();
    }

    Attr getAttribute(int i) {
        return (Attr) getCurrentFrame().attributes.get(i);
    }

    private String getLocalName(Attr attr) {

        String name = attr.getLocalName();
        if (name == null) {
            name = attr.getNodeName();
        }
        return name;
    }

    public QName getAttributeName(int i) {
        Attr at = getAttribute(i);

        String prefix = at.getPrefix();
        String ln = getLocalName(at);
        // at.getNodeName();
        String ns = at.getNamespaceURI();

        if (prefix == null) {
            return new QName(ns, ln);
        } else {
            return new QName(ns, ln, prefix);
        }
    }

    public String getAttributeNamespace(int i) {
        return getAttribute(i).getNamespaceURI();
    }

    public String getAttributeLocalName(int i) {
        Attr attr = getAttribute(i);
        String name = getLocalName(attr);
        return name;
    }

    public String getAttributePrefix(int i) {
        return getAttribute(i).getPrefix();
    }

    public String getAttributeType(int i) {
        return "CDATA";
    }

    public String getAttributeValue(int i) {
        return getAttribute(i).getValue();
    }

    public boolean isAttributeSpecified(int i) {
        return getAttribute(i).getValue() != null;
    }

    public int getNamespaceCount() {
        return getCurrentFrame().prefixes.size();
    }

    public String getNamespacePrefix(int i) {
        return (String) getCurrentFrame().prefixes.get(i);
    }

    public String getNamespaceURI(int i) {
        return (String) getCurrentFrame().uris.get(i);
    }

    public NamespaceContext getNamespaceContext() {
        return context;
    }

    public String getText() {
        Node node = getCurrentElement().getChildNodes().item(getCurrentFrame().currentChild);
        return node.getNodeValue();
    }

    public char[] getTextCharacters() {
        return getText().toCharArray();
    }

    public int getTextStart() {
        return 0;
    }

    public int getTextLength() {
        return getText().length();
    }

    public String getEncoding() {
        return null;
    }

    public QName getName() {
        Element el = getCurrentElement();

        String prefix = getPrefix();
        String ln = getLocalName();

        if (prefix == null) {
            return new QName(el.getNamespaceURI(), ln);
        } else {
            return new QName(el.getNamespaceURI(), ln, prefix);
        }
    }

    public String getLocalName() {
        String name = getCurrentElement().getLocalName();
        // When the element has no namespaces, null is returned
        if (name == null) {
            name = getCurrentElement().getNodeName();
        }
        return name;
    }

    public String getNamespaceURI() {
        return getCurrentElement().getNamespaceURI();
    }

    public String getPrefix() {
        String prefix = getCurrentElement().getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        return prefix;
    }

    public String getPITarget() {
        throw new UnsupportedOperationException();
    }

    public String getPIData() {
        throw new UnsupportedOperationException();
    }
}
