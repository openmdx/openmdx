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

import java.util.Iterator;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.openmdx.uses.org.apache.commons.httpclient.Header;
import org.openmdx.uses.org.apache.commons.httpclient.HttpMethod;
import org.openmdx.uses.org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.openmdx.uses.org.apache.commons.httpclient.methods.PostMethod;
import org.openmdx.uses.org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * A class which marshalls a client HTTP request to a NMS message
 *
 * @version $Revision: 1.1 $
 */
public class HttpClientMarshaler {

    protected SourceTransformer sourceTransformer;
    private boolean streaming;
    private String contentType = "text/xml";

    public HttpClientMarshaler() {
        this(false);
    }
    
    public HttpClientMarshaler(boolean streaming) {
        this.sourceTransformer = new SourceTransformer();
        this.streaming = streaming;
    }

    /**
     * @return the streaming
     */
    public boolean isStreaming() {
        return streaming;
    }

    /**
     * @param streaming the streaming to set
     */
    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void toNMS(NormalizedMessage normalizedMessage, HttpMethod method) throws Exception {
        addNmsProperties(normalizedMessage, method);
        if (streaming) {
            normalizedMessage.setContent(new StreamSource(method.getResponseBodyAsStream()));
        } else {
            normalizedMessage.setContent(new StringSource(method.getResponseBodyAsString()));
        }
    }

    public void fromNMS(PostMethod method, MessageExchange exchange, NormalizedMessage normalizedMessage) throws Exception, TransformerException {
        addHttpHeaders(method, normalizedMessage);
        if (streaming) {
            method.setContentChunked(true);
            Source src = normalizedMessage.getContent();
            if (src instanceof StreamSource && ((StreamSource) src).getInputStream() != null) {
                method.setRequestEntity(new InputStreamRequestEntity(
                        ((StreamSource) src).getInputStream(), -1));
            } else {
                String text = sourceTransformer.toString(normalizedMessage.getContent());
                method.setRequestEntity(new StringRequestEntity(text));
                
            }
        } else {
            String text = sourceTransformer.toString(normalizedMessage.getContent());
            method.setRequestEntity(new StringRequestEntity(text));
        }
    }

    protected void addHttpHeaders(HttpMethod method, NormalizedMessage message) {
        for (Iterator iter = message.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = message.getProperty(name);
            if (shouldIncludeHeader(message, name, value)) {
                method.addRequestHeader(name, value.toString());
            }
        }
        if (method.getRequestHeader("Content-Type") == null) {
            method.setRequestHeader("Content-Type", contentType);
        }
    }

    protected void addNmsProperties(NormalizedMessage message, HttpMethod method) {
        Header[] headers = method.getResponseHeaders();
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            String name = header.getName();
            String value = header.getValue();
            message.setProperty(name, value);
        }
    }

    /**
     * Decides whether or not the given header should be included in the JMS message.
     * By default this includes all suitable typed values
     */
    protected boolean shouldIncludeHeader(NormalizedMessage normalizedMessage, String name, Object value) {
        return value instanceof String && 
                !"Content-Length".equalsIgnoreCase(name) &&
                !"Content-Type".equalsIgnoreCase(name);
    }

}
