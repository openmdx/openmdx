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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.ExtendedXMLStreamReader;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.FragmentStreamReader;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StaxSource;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;
import org.openmdx.uses.org.apache.servicemix.jbi.util.DOMUtil;
import org.openmdx.uses.org.apache.servicemix.soap.SoapFault;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.1 $
 * @since 3.0
 */
public class SoapReader {

	private SoapMarshaler marshaler;
  
    protected static final Source EMPTY_CONTENT = new StringSource("<payload/>");

	public SoapReader(SoapMarshaler marshaler) {
		this.marshaler = marshaler;
	}

	public SoapMessage read(InputStream is, String contentType)
			throws Exception {
		if (contentType != null && contentType.toLowerCase().startsWith(SoapMarshaler.MULTIPART_CONTENT)) {
			Session session = Session.getDefaultInstance(new Properties());
            is = new SequenceInputStream(new ByteArrayInputStream(new byte[] { 13, 10 }), is);
			MimeMessage mime = new MimeMessage(session, is);
			mime.setHeader(SoapMarshaler.MIME_CONTENT_TYPE, contentType);
			return read(mime);
		} else {
			return read(is);
		}
	}

	public SoapMessage read(InputStream is) throws Exception {
		if (marshaler.isSoap()) {
            if (marshaler.isUseDom()) {
                return readSoapUsingDom(is);
            } else {
                return readSoapUsingStax(is);
            }
		} else {
			SoapMessage message = new SoapMessage();
			message.setSource(new StreamSource(is));
			return message;
		}
	}

    private SoapMessage readSoapUsingDom(InputStream is) throws Exception {
        SoapMessage message = new SoapMessage();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(is);
        message.setDocument(doc);
        Element env = doc.getDocumentElement();
        QName envName = DOMUtil.getQName(env);
        if (!envName.getLocalPart().equals(SoapMarshaler.ENVELOPE)) {
            throw new SoapFault(SoapFault.SENDER, "Unrecognized element: "
                    + envName + ". Expecting 'Envelope'.");
        }
        message.setEnvelopeName(envName);
        // Check soap 1.1 or 1.2
        String soapUri = envName.getNamespaceURI();
        if (!SoapMarshaler.SOAP_11_URI.equals(soapUri) && !SoapMarshaler.SOAP_12_URI.equals(soapUri)) {
            throw new SoapFault(SoapFault.SENDER, "Unrecognized namespace: " + soapUri
                    + " for element 'Envelope'.");
        }
        // Check Headers
        Element child = DOMUtil.getFirstChildElement(env);
        if (DOMUtil.getQName(child).equals(new QName(soapUri, SoapMarshaler.HEADER))) {
            parseHeaders(message, child);
            child = DOMUtil.getNextSiblingElement(child);
        }
        // Check Body
        if (!DOMUtil.getQName(child).equals(new QName(soapUri, SoapMarshaler.BODY))) {
            throw new SoapFault(SoapFault.SENDER, "Unrecognized element: "
                    + DOMUtil.getQName(child) + ". Expecting 'Body'.");
        }
        // Create Source for content
        child = DOMUtil.getFirstChildElement(child);
        if (child != null) {
            QName childName = DOMUtil.getQName(child);
            message.setBodyName(childName);
            // Check for fault
            if (childName.equals(new QName(soapUri, SoapMarshaler.FAULT))) {
                message.setFault(readFaultUsingDom(child));
            } else {
                message.setSource(new DOMSource(child));
            }
        }
        child = DOMUtil.getNextSiblingElement(child);
        if (child != null) {
            throw new SoapFault(SoapFault.RECEIVER, "Body element has more than one child element.");
        }
        return message;
    }
    
    private void parseHeaders(SoapMessage message, Element headers) {
        for (Element child = DOMUtil.getFirstChildElement(headers);
             child != null;
             child = DOMUtil.getNextSiblingElement(child)) {
            DocumentFragment df = child.getOwnerDocument().createDocumentFragment();
            df.appendChild(child.cloneNode(true));
            message.addHeader(DOMUtil.getQName(child), df);
        }
    }
    
	private SoapMessage readSoapUsingStax(InputStream is) throws Exception {
		SoapMessage message = new SoapMessage();
		XMLStreamReader reader = marshaler.getInputFactory().createXMLStreamReader(is);
		reader = new ExtendedXMLStreamReader(reader);
		reader.nextTag();
		// Check Envelope tag
		if (!reader.getLocalName().equals(SoapMarshaler.ENVELOPE)) {
			throw new SoapFault(SoapFault.SENDER, "Unrecognized element: "
					+ reader.getName() + " at ["
					+ reader.getLocation().getLineNumber() + ","
					+ reader.getLocation().getColumnNumber()
					+ "]. Expecting 'Envelope'.");
		}
		message.setEnvelopeName(reader.getName());
		// Check soap 1.1 or 1.2
		String soapUri = reader.getNamespaceURI();
		if (!SoapMarshaler.SOAP_11_URI.equals(soapUri) && !SoapMarshaler.SOAP_12_URI.equals(soapUri)) {
			throw new SoapFault(SoapFault.SENDER, "Unrecognized namespace: " + soapUri
					+ " for element 'Envelope' at ["
					+ reader.getLocation().getLineNumber() + ","
					+ reader.getLocation().getColumnNumber()
					+ "]. Expecting 'Envelope'.");
		}
		// Check Headers
		reader.nextTag();
		if (reader.getName().equals(new QName(soapUri, SoapMarshaler.HEADER))) {
			parseHeaders(message, reader);
			reader.nextTag();
		}
		// Check Body
		if (!reader.getName().equals(new QName(soapUri, SoapMarshaler.BODY))) {
			throw new SoapFault(SoapFault.SENDER, "Unrecognized element: "
					+ reader.getName() + " at ["
					+ reader.getLocation().getLineNumber() + ","
					+ reader.getLocation().getColumnNumber()
					+ "]. Expecting 'Body'.");
		}
		// Create Source for content
		if (reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            QName childName = reader.getName();
            message.setBodyName(childName);
            // Check for fault
            if (childName.equals(new QName(soapUri, SoapMarshaler.FAULT))) {
                message.setFault(readFaultUsingStax(reader));
            } else {
                message.setSource(new StaxSource(new FragmentStreamReader(reader)));
            }
		}
		return message;
	}
    
    private SoapFault readFaultUsingDom(Element element) throws SoapFault {
        QName code = null;
        QName subcode = null;
        String reason = null;
        URI node = null;
        URI role = null;
        Source details = null;
        // Parse soap 1.1 faults
        if (element.getNamespaceURI().equals(SoapMarshaler.SOAP_11_URI)) {
            // Fault code
            Element child = DOMUtil.getFirstChildElement(element);
            checkElementName(child, SoapMarshaler.SOAP_11_FAULTCODE);
            code = DOMUtil.createQName(child, DOMUtil.getElementText(child));
            // Fault string
            child = DOMUtil.getNextSiblingElement(child);
            checkElementName(child, SoapMarshaler.SOAP_11_FAULTSTRING);
            reason = DOMUtil.getElementText(child);
            child = DOMUtil.getNextSiblingElement(child);
            QName childname = DOMUtil.getQName(child);
            // Fault actor
            if (SoapMarshaler.SOAP_11_FAULTACTOR.equals(childname)) {
                node = URI.create(DOMUtil.getElementText(child));
                child = DOMUtil.getNextSiblingElement(child);
                childname = DOMUtil.getQName(child);
            }
            // Fault details
            if (SoapMarshaler.SOAP_11_FAULTDETAIL.equals(childname)) {
                Element subchild = DOMUtil.getFirstChildElement(child);
                if (subchild != null) {
                    details = new DOMSource(subchild);
                    subchild = DOMUtil.getNextSiblingElement(subchild);
                    if (subchild != null) {
                        throw new SoapFault(SoapFault.RECEIVER, "Multiple elements are not supported in Detail");
                    }
                }
                child = DOMUtil.getNextSiblingElement(child);
                childname = DOMUtil.getQName(child);
            }
            // Nothing should be left
            if (childname != null) {
                throw new SoapFault(SoapFault.SENDER, "Unexpected element: " + childname);
            }
        // Parse soap 1.2 faults
        } else {
            // Fault code
            Element child = DOMUtil.getFirstChildElement(element);
            checkElementName(child, SoapMarshaler.SOAP_12_FAULTCODE);
            Element subchild = DOMUtil.getFirstChildElement(child);
            checkElementName(subchild, SoapMarshaler.SOAP_12_FAULTVALUE);
            code = DOMUtil.createQName(subchild, DOMUtil.getElementText(subchild));
            if (!SoapMarshaler.SOAP_12_CODE_DATAENCODINGUNKNOWN.equals(code) &&
                !SoapMarshaler.SOAP_12_CODE_MUSTUNDERSTAND.equals(code) &&
                !SoapMarshaler.SOAP_12_CODE_RECEIVER.equals(code) &&
                !SoapMarshaler.SOAP_12_CODE_SENDER.equals(code) &&
                !SoapMarshaler.SOAP_12_CODE_VERSIONMISMATCH.equals(code)) {
                throw new SoapFault(SoapFault.SENDER, "Unexpected fault code: " + code); 
            }
            subchild = DOMUtil.getNextSiblingElement(subchild);
            if (subchild != null) {
                checkElementName(subchild, SoapMarshaler.SOAP_12_FAULTSUBCODE);
                Element subsubchild = DOMUtil.getFirstChildElement(subchild);
                checkElementName(subsubchild, SoapMarshaler.SOAP_12_FAULTVALUE);
                subcode = DOMUtil.createQName(subsubchild, DOMUtil.getElementText(subsubchild));
                subsubchild = DOMUtil.getNextSiblingElement(subsubchild);
                if (subsubchild != null) {
                    checkElementName(subsubchild, SoapMarshaler.SOAP_12_FAULTSUBCODE);
                    throw new SoapFault(SoapFault.RECEIVER, "Unsupported nested subcodes");
                }
            }
            // Fault reason
            child = DOMUtil.getNextSiblingElement(child);
            checkElementName(child, SoapMarshaler.SOAP_12_FAULTREASON);
            subchild = DOMUtil.getFirstChildElement(child);
            checkElementName(subchild, SoapMarshaler.SOAP_12_FAULTTEXT);
            reason = DOMUtil.getElementText(subchild);
            subchild = DOMUtil.getNextSiblingElement(subchild);
            if (subchild != null) {
                throw new SoapFault(SoapFault.RECEIVER, "Unsupported multiple reasons");
            }
            // Fault node
            child = DOMUtil.getNextSiblingElement(child);
            QName childname = DOMUtil.getQName(child);
            if (SoapMarshaler.SOAP_12_FAULTNODE.equals(childname)) {
                node = URI.create(DOMUtil.getElementText(child));
                child = DOMUtil.getNextSiblingElement(child);
                childname = DOMUtil.getQName(child);
            }
            // Fault role
            if (SoapMarshaler.SOAP_12_FAULTROLE.equals(childname)) {
                role = URI.create(DOMUtil.getElementText(child));
                child = DOMUtil.getNextSiblingElement(child);
                childname = DOMUtil.getQName(child);
            }
            // Fault details
            if (SoapMarshaler.SOAP_12_FAULTDETAIL.equals(childname)) {
                subchild = DOMUtil.getFirstChildElement(child);
                if (subchild != null) {
                    details = new DOMSource(subchild);
                    subchild = DOMUtil.getNextSiblingElement(subchild);
                    if (subchild != null) {
                        throw new SoapFault(SoapFault.RECEIVER, "Multiple elements are not supported in Detail");
                    }
                }
                child = DOMUtil.getNextSiblingElement(child);
                childname = DOMUtil.getQName(child);
            }
            // Nothing should be left
            if (childname != null) {
                throw new SoapFault(SoapFault.SENDER, "Unexpected element: " + childname);
            }
        }
        SoapFault fault = new SoapFault(code, subcode, reason, node, role, details);
        return fault;
    }
    
    private SoapFault readFaultUsingStax(XMLStreamReader reader) throws SoapFault {
        try {
            FragmentStreamReader rh = new FragmentStreamReader(reader);
            Document doc = (Document) marshaler.getSourceTransformer().toDOMNode(
                    new StaxSource(rh));
            return readFaultUsingDom(doc.getDocumentElement());
        } catch (SoapFault e) {
            throw e;
        } catch (Exception e) {
            throw new SoapFault(e);
        }
    }
    
    private void checkElementName(Element element, QName expected) throws SoapFault {
        QName name= DOMUtil.getQName(element);
        if (!expected.equals(name)) {
            throw new SoapFault(SoapFault.SENDER, "Expected element: " + expected + " but found " + name);
        }            
    }

	private void parseHeaders(SoapMessage message, XMLStreamReader reader)
			throws Exception {
		while (reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
			QName hn = reader.getName();
			FragmentStreamReader rh = new FragmentStreamReader(reader);
			Document doc = (Document) marshaler.getSourceTransformer().toDOMNode(
					new StaxSource(rh));
			DocumentFragment df = doc.createDocumentFragment();
			df.appendChild(doc.getDocumentElement());
			message.addHeader(hn, df);
		}
	}

	public SoapMessage read(MimeMessage mime) throws Exception {
		final Object content = mime.getContent();
		if (content instanceof MimeMultipart) {
			MimeMultipart multipart = (MimeMultipart) content;
			ContentType type = new ContentType(mime.getContentType());
			String contentId = type.getParameter("start");
			// Get request
			MimeBodyPart contentPart = null;
            if (contentId != null) {
                contentPart = (MimeBodyPart) multipart.getBodyPart(contentId);
            } else {
                for (int i = 0; i < multipart.getCount(); i++) {
                  MimeBodyPart contentPart2 = (MimeBodyPart) multipart.getBodyPart(i);
                  String contentType = contentPart2.getContentType();
                  
                  if (contentType.indexOf("xml") >= 0) {
                    contentPart = contentPart2;
                    break;
                  }
                }
            }
            
            SoapMessage message = null;
            if (contentPart != null) {
              message = read(contentPart.getInputStream());  
            } else {
              message = new SoapMessage();
              message.setSource(EMPTY_CONTENT);
            }
            
            // Get attachments
			for (int i = 0; i < multipart.getCount(); i++) {
                MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
                if (part != contentPart) {
                    String id = part.getContentID();
                    if (id == null) {
                        id = "Part" + i;
                    } else if (id.startsWith("<")) {
						id = id.substring(1, id.length() - 1);
					}
					message.addAttachment(id, part.getDataHandler());
				}
			}
			return message;
		} else {
			throw new UnsupportedOperationException(
					"Expected a javax.mail.internet.MimeMultipart object");
		}
	}

}
