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
package org.openmdx.uses.org.apache.servicemix.components.http;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.dom.DOMSource;

import org.openmdx.uses.org.apache.commons.httpclient.HttpMethod;
import org.openmdx.uses.org.apache.commons.httpclient.methods.PostMethod;
import org.openmdx.uses.org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.CachedXPathAPI;

/**
 * A class which marshalls a client HTTP request to a NMS message
 * 
 * @version $Revision: 1.1 $
 */
public class HttpSoapClientMarshaler extends HttpClientMarshaler {

    public HttpSoapClientMarshaler() {
        super();
    }
    
    public HttpSoapClientMarshaler(boolean streaming) {
        super(streaming);
    }
    
    public void toNMS(NormalizedMessage normalizedMessage, HttpMethod method)
            throws Exception {
        addNmsProperties(normalizedMessage, method);
        String response = method.getResponseBodyAsString();
        Node node = sourceTransformer.toDOMNode(new StringSource(response));
        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
        NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, "/*/*[local-name()='Body']/*");
        Node root = iterator.nextNode();
        if (root instanceof Element == false) {
        	throw new IllegalStateException("Could not find body content");
        }
        Element element = (Element) root;
        
        // Copy embedded namespaces from the envelope into the body root
        for (Node parent = element.getParentNode(); parent != null; parent = parent.getParentNode()) {
            NamedNodeMap attributes = parent.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    Attr att = (Attr) attributes.item(i);
                    if (att.getName().startsWith("xmlns:")
                            && element.getAttributes().getNamedItemNS(att.getNamespaceURI(),
                                    att.getLocalName()) == null) {
                    	element.setAttributeNS(att.getNamespaceURI(),
                                    att.getName(),
                                    att.getValue());
                    }
                }
            }
        }
        
        normalizedMessage.setContent(new DOMSource(element));
    }

    public void fromNMS(PostMethod method, MessageExchange exchange, NormalizedMessage normalizedMessage) throws Exception {
        addHttpHeaders(method, normalizedMessage);
        Element elem = sourceTransformer.toDOMElement(normalizedMessage.getContent());
        Document document = sourceTransformer.createDocument();
        Element env = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "env:Envelope");
        document.appendChild(env);
        env.setAttribute("xmlns:env", "http://schemas.xmlsoap.org/soap/envelope/");
        Element body = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "env:Body");
        env.appendChild(body);
        body.appendChild(document.importNode(elem, true));
        String text = sourceTransformer.toString(document);
        method.setRequestEntity(new StringRequestEntity(text));
    }

}
