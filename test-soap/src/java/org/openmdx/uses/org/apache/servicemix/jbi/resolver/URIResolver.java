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
package org.openmdx.uses.org.apache.servicemix.jbi.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class URIResolver extends EndpointResolverSupport {

    /**
     * The uri to resolve
     */
    private String uri;
    
    public URIResolver() {
    }
    
    public URIResolver(String uri) {
        this.uri = uri;
    }
    
    protected JBIException createServiceUnavailableException() {
        return new JBIException("Unable to resolve uri: " + uri);
    }

    public ServiceEndpoint[] resolveAvailableEndpoints(ComponentContext context, MessageExchange exchange)
            throws JBIException {
        if (uri.startsWith("interface:")) {
            String uri = this.uri.substring(10);
            String[] parts = split2(uri);
            return context.getEndpoints(new QName(parts[0], parts[1]));
        } else if (uri.startsWith("operation:")) {
            // ignore operation
            String uri = this.uri.substring(10);
            String[] parts = split3(uri);
            return context.getEndpoints(new QName(parts[0], parts[1]));
        } else if (uri.startsWith("service:")) {
            String uri = this.uri.substring(8);
            String[] parts = split2(uri);
            return context.getEndpointsForService(new QName(parts[0], parts[1]));
        } else if (uri.startsWith("endpoint:")) {
            String uri = this.uri.substring(9);
            String[] parts = split3(uri);
            ServiceEndpoint se = context.getEndpoint(new QName(parts[0], parts[1]), parts[2]);
            if (se != null) {
                return new ServiceEndpoint[] { se };
            }
        // Try an EPR resolution
        } else {
            DocumentFragment epr = createWSAEPR(uri);
            ServiceEndpoint se = context.resolveEndpointReference(epr);
            if (se != null) {
                return new ServiceEndpoint[] { se };
            }
        }
        return null;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public static DocumentFragment createWSAEPR(String uri) {
        Document doc;
        try {
            doc = new SourceTransformer().createDocument();            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DocumentFragment epr = doc.createDocumentFragment();
        Element root = doc.createElement("epr");
        Element address = doc.createElementNS("http://www.w3.org/2005/08/addressing", "wsa:Address");
        Text txt = doc.createTextNode(uri);
        address.appendChild(txt);
        root.appendChild(address);
        epr.appendChild(root);
        return epr;
    }
    
    /**
     * Configure a JBI exchange with the given URI as the target
     * 
     * @param exchange the exchange to configure
     * @param context a component context used to resolve endpoints
     * @param uri the target uri
     */
    public static void configureExchange(MessageExchange exchange, ComponentContext context, String uri) {
        if (exchange == null) throw new NullPointerException("exchange is null");
        if (context == null) throw new NullPointerException("context is null");
        if (uri == null) throw new NullPointerException("uri is null");
        if (uri.startsWith("interface:")) {
            String uri2 = uri.substring(10);
            String[] parts = URIResolver.split2(uri2);
            exchange.setInterfaceName(new QName(parts[0], parts[1]));
        } else if (uri.startsWith("operation:")) {
            String uri2 = uri.substring(10);
            String[] parts = URIResolver.split3(uri2);
            exchange.setInterfaceName(new QName(parts[0], parts[1]));
            exchange.setOperation(new QName(parts[0], parts[2]));
        } else if (uri.startsWith("service:")) {
            String uri2 = uri.substring(8);
            String[] parts = URIResolver.split2(uri2);
            exchange.setService(new QName(parts[0], parts[1]));
        } else if (uri.startsWith("endpoint:")) {
            String uri2 = uri.substring(9);
            String[] parts = URIResolver.split3(uri2);
            ServiceEndpoint se = context.getEndpoint(new QName(parts[0], parts[1]), parts[2]);
            exchange.setEndpoint(se);
        } else {
            DocumentFragment epr = URIResolver.createWSAEPR(uri);
            ServiceEndpoint se = context.resolveEndpointReference(epr);
            exchange.setEndpoint(se);
        }
    }
    
    public static String[] split3(String uri) {
        char sep;
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        int idx2 = uri.lastIndexOf(sep, idx1 - 1);
        String epName = uri.substring(idx1 + 1);
        String svcName = uri.substring(idx2 + 1, idx1);
        String nsUri   = uri.substring(0, idx2);
        return new String[] { nsUri, svcName, epName };
    }
    
    public static String[] split2(String uri) {
        char sep;
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        String svcName = uri.substring(idx1 + 1);
        String nsUri   = uri.substring(0, idx1);
        return new String[] { nsUri, svcName };
    }
    
}
