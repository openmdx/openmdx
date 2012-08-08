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

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.W3CDOMStreamReader;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.XMLStreamHelper;
import org.openmdx.uses.org.apache.servicemix.jbi.util.ByteArrayDataSource;
import org.openmdx.uses.org.apache.servicemix.jbi.util.StreamDataSource;
import org.openmdx.uses.org.apache.servicemix.soap.SoapFault;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.2 $
 * @since 3.0
 */
public class SoapWriter {

    public static final String SOAP_PART_ID = "soap-request";

    private SoapMessage message;

    private String contentType;

    private SoapMarshaler marshaler;

    private MimeMultipart parts;

    public SoapWriter(SoapMarshaler marshaler, SoapMessage message) {
        this.marshaler = marshaler;
        this.message = message;
        this.contentType = prepare();
    }

    public String getContentType() {
        return contentType;
    }

    public void write(OutputStream out) throws Exception {
        if (message.hasAttachments()) {
            writeMultipartMessage(out);
        } else {
            writeSimpleMessage(out);
        }
    }

    private String prepare() {
        if (message.hasAttachments()) {
            parts = new MimeMultipart("related; type=\"text/xml\"; start=\"<" + SOAP_PART_ID + ">\"");
            return parts.getContentType();
        } else {
            return "text/xml";
        }
    }

    private void writeSimpleMessage(OutputStream out) throws Exception {
        XMLStreamWriter writer = marshaler.getOutputFactory().createXMLStreamWriter(out);
        writer.writeStartDocument();
        if (marshaler.isSoap()) {
            writeSoapEnvelope(writer);
        } else {
            if (message.hasHeaders()) {
                throw new IllegalStateException("SOAP headers found on non-soap message");
            }
            if (message.getFault() != null) {
                if (message.getFault().getDetails() != null) {
                    XMLStreamReader reader = marshaler.getSourceTransformer().toXMLStreamReader(
                            message.getFault().getDetails());
                    XMLStreamHelper.copy(reader, writer);
                } else {
                    throw new IllegalStateException("Cannot write non xml faults for non soap messages");
                }
            } else if (message.getSource() != null) {
                writeContents(writer);
            }
        }
        writer.writeEndDocument();
        writer.flush();
    }

    private void writeMultipartMessage(OutputStream out) throws Exception {
        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mime = new MimeMessage(session);
        // Add soap part
        MimeBodyPart soapPart = new MimeBodyPart();
        soapPart.setContentID("<" + SOAP_PART_ID + ">");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeSimpleMessage(baos);
        soapPart.setDataHandler(new DataHandler(new ByteArrayDataSource(baos.toByteArray(), "text/xml")));
        parts.addBodyPart(soapPart);
        // Add attachments
        for (Iterator itr = message.getAttachments().entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            String id = (String) entry.getKey();
            DataHandler dh = (DataHandler) entry.getValue();
            MimeBodyPart part = new MimeBodyPart();
            part.setDataHandler(dh);
            part.setContentID("<" + id + ">");
            parts.addBodyPart(part);
        }
        mime.setContent(parts);
        mime.setHeader(SoapMarshaler.MIME_CONTENT_TYPE, getContentType());
        // We do not want headers, so 
        //  * retrieve all headers
        //  * skip first 2 bytes (CRLF)
        mime.saveChanges();
        Enumeration headersEnum = mime.getAllHeaders();
        List headersList = new ArrayList();
        while (headersEnum.hasMoreElements()) {
            headersList.add(((Header) headersEnum.nextElement()).getName().toLowerCase());
        }
        String[] headers = (String[]) headersList.toArray(new String[0]);
        // Skip first 2 bytes
        OutputStream os = new FilterOutputStream(out) {
            private int nb = 0;
            public void write(int b) throws IOException {
                if (++nb > 2) {
                    super.write(b);
                }
            }
        };
        // Write
        mime.writeTo(os, headers);
    }

    public void writeSoapEnvelope(XMLStreamWriter writer) throws Exception {
        QName envelope = getEnvelopeName();
        String soapUri = envelope.getNamespaceURI();
        String soapPrefix = envelope.getPrefix();
        writer.setPrefix(soapPrefix, soapUri);
        writer.writeStartElement(soapPrefix, SoapMarshaler.ENVELOPE, soapUri);
        if (!marshaler.isRepairingNamespace()) {
            writer.writeNamespace(soapPrefix, soapUri);
            // XMLStreamHelper.writeNamespacesExcludingPrefixAndNamespace(out, in, soapPrefix, soapUri);
        }
        // Write Header
        if (message.getHeaders() != null && message.getHeaders().size() > 0) {
            writer.writeStartElement(soapPrefix, SoapMarshaler.HEADER, soapUri);
            for (Iterator it = message.getHeaders().values().iterator(); it.hasNext();) {
                DocumentFragment df = (DocumentFragment) it.next();
                Element e = (Element) df.getFirstChild();
                XMLStreamHelper.copy(new W3CDOMStreamReader(e), writer);
            }
            writer.writeEndElement();
        }
        // Write Body
        writer.writeStartElement(soapPrefix, SoapMarshaler.BODY, soapUri);
        if (message.getFault() != null) {
            writeFault(writer);
        } else if (message.getSource() != null) {
            writeContents(writer);
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeContents(XMLStreamWriter writer) throws Exception {
        XMLStreamReader reader = marshaler.getSourceTransformer().toXMLStreamReader(message.getSource());
        XMLStreamHelper.copy(reader, writer);
    }

    private void writeFault(XMLStreamWriter writer) throws Exception {
        QName envelope = getEnvelopeName();
        String soapUri = envelope.getNamespaceURI();
        if (SoapMarshaler.SOAP_11_URI.equals(soapUri)) {
            writeSoap11Fault(writer);
        } else if (SoapMarshaler.SOAP_12_URI.equals(soapUri)) {
            writeSoap12Fault(writer);
        } else {
            throw new IllegalStateException("Unknown soap namespace: " + soapUri);
        }
    }

    private void writeSoap11Fault(XMLStreamWriter writer) throws Exception {
        QName envelope = getEnvelopeName();
        String soapUri = envelope.getNamespaceURI();
        String soapPrefix = envelope.getPrefix();
        writer.setPrefix(soapPrefix, soapUri);
        SoapFault fault = message.getFault();
        fault.translateCodeTo11();

        writer.writeStartElement(soapPrefix, SoapMarshaler.FAULT, soapUri);
        QName code = fault.getCode();
        if (code != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_11_FAULTCODE);
            XMLStreamHelper.writeTextQName(writer, code);
            writer.writeEndElement();
        }
        String reason = fault.getReason();
        if (reason == null && fault.getCause() != null) {
            reason = fault.getCause().toString();
        }
        XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_11_FAULTSTRING);
        if (reason != null) {
            writer.writeCharacters(reason);
        }
        writer.writeEndElement();
        URI node = fault.getNode();
        if (node != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_11_FAULTACTOR);
            writer.writeCharacters(node.toString());
            writer.writeEndElement();
        }
        Source details = fault.getDetails();
        if (details != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_11_FAULTDETAIL);
            XMLStreamReader reader = marshaler.getSourceTransformer().toXMLStreamReader(details);
            XMLStreamHelper.copy(reader, writer);
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeSoap12Fault(XMLStreamWriter writer) throws Exception {
        QName envelope = getEnvelopeName();
        String soapUri = envelope.getNamespaceURI();
        String soapPrefix = envelope.getPrefix();
        writer.setPrefix(soapPrefix, soapUri);
        SoapFault fault = message.getFault();
        fault.translateCodeTo12();
        
        writer.writeStartElement(soapPrefix, SoapMarshaler.FAULT, soapUri);
        QName code = fault.getCode();
        if (code != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTCODE);
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTVALUE);
            XMLStreamHelper.writeTextQName(writer, code);
            writer.writeEndElement();
            QName subcode = fault.getSubcode();
            if (subcode != null) {
                XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTSUBCODE);
                XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTVALUE);
                XMLStreamHelper.writeTextQName(writer, subcode);
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        String reason = fault.getReason();
        if (reason == null && fault.getCause() != null) {
            reason = fault.getCause().toString();
        }
        XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTREASON);
        XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTTEXT);
        writer.writeAttribute(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI, "lang", "en");
        if (reason != null) {
            writer.writeCharacters(reason);
        }
        writer.writeEndElement();
        writer.writeEndElement();
        URI node = fault.getNode();
        if (node != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTNODE);
            writer.writeCharacters(node.toString());
            writer.writeEndElement();
        }

        URI role = fault.getRole();
        if (role != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTROLE);
            writer.writeCharacters(role.toString());
            writer.writeEndElement();
        }

        Source details = fault.getDetails();
        if (details != null) {
            XMLStreamHelper.writeStartElement(writer, SoapMarshaler.SOAP_12_FAULTDETAIL);
            XMLStreamReader reader = marshaler.getSourceTransformer().toXMLStreamReader(details);
            XMLStreamHelper.copy(reader, writer);
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }
    
    protected QName getEnvelopeName() {
        QName name = message.getEnvelopeName();
        if (name == null) {
            name = new QName(marshaler.getSoapUri(), SoapMarshaler.ENVELOPE, marshaler.getPrefix());
        } else if (name.getPrefix() == null) {
            name = new QName(name.getNamespaceURI(), name.getLocalPart(), marshaler.getPrefix());
        }
        return name;
    }
    
}
