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
package org.openmdx.uses.org.apache.servicemix.soap.marshalers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.openmdx.uses.org.apache.servicemix.soap.SoapFault;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * Simple DTO to hold attachments, soap headers and main xml source.
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.1 $
 * @since 3.0 
 */
public class SoapMessage {

	private QName envelopeName;
	private QName bodyName;
	private Source source;
	private Map attachments;
	private Map headers;
	private SoapFault fault;
    private Subject subject;
    private Document document;
	
	/**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }
    /**
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }
    /**
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }
    /**
     * @param subject the subject to set
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    public Map getAttachments() {
		return attachments;
	}
	public void setAttachments(Map attachments) {
		this.attachments = attachments;
	}
	public void addAttachment(String name, DataHandler handler) {
		if (this.attachments == null) {
			this.attachments = new HashMap();
		}
		this.attachments.put(name, handler);
	}
	public boolean hasAttachments() {
		return attachments != null && attachments.size() > 0;
	}
	
	public Map getHeaders() {
		return headers;
	}
	public void setHeaders(Map headers) {
		this.headers = headers;
	}
	public void addHeader(QName name, DocumentFragment header) {
		if (this.headers == null) {
			this.headers = new HashMap();
		}
		this.headers.put(name, header);
	}
	public boolean hasHeaders() {
		return headers != null && headers.size() > 0;
	}
	
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	
	public QName getEnvelopeName() {
		return envelopeName;
	}
	public void setEnvelopeName(QName envelopeName) {
		this.envelopeName = envelopeName;
	}
	public QName getBodyName() {
		return bodyName;
	}
	public void setBodyName(QName bodyName) {
		this.bodyName = bodyName;
	}
	
	public SoapFault getFault() {
		return fault;
	}
	public void setFault(SoapFault fault) {
		this.fault = fault;
	}
    
    public void addPrincipal(Principal principal) {
        if (subject == null) {
            subject = new Subject();
        }
        subject.getPrincipals().add(principal);
    }
	
}
