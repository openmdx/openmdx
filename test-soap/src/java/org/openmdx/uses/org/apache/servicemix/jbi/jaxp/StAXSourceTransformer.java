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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * An enhanced {@link org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer} which adds support for converting from and to
 * {@link StAXSource} instances. Since this class introduces a runtime dependency on StAX
 * which some users may not use/require, this class is separated out from the core JAXP transformer.
 *
 * @version $Revision: 1.1 $
 */
public class StAXSourceTransformer extends SourceTransformer {

    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;

    /**
     * Converts the source instance to a {@link javax.xml.transform.dom.DOMSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    public StaxSource toStaxSource(Source source) throws XMLStreamException {
        if (source instanceof StaxSource) {
            return (StaxSource) source;
        }
        else {
            XMLInputFactory factory = getInputFactory();
            XMLStreamReader reader = factory.createXMLStreamReader(source);
            return new StaxSource(reader);
        }
    }
    
    public XMLStreamReader toXMLStreamReader(Source source) throws XMLStreamException, TransformerException {
        // It seems that woodstox 2.9.3 throws some NPE in the servicemix-soap
        // when using DOM, so use our own dom / stax parser
        if (source instanceof DOMSource) {
            Node n = ((DOMSource) source).getNode();
            Element el = n instanceof Document ? ((Document) n).getDocumentElement() : n instanceof Element ? (Element) n : null;
            if (el != null) {
                return new W3CDOMStreamReader(el);
            }
        }
        XMLInputFactory factory = getInputFactory();
        try {
        	return factory.createXMLStreamReader(source);
        } catch (XMLStreamException e) {
        	return factory.createXMLStreamReader(toReaderFromSource(source));
        }
    }

    public DOMSource toDOMSource(Source source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DOMSource answer = super.toDOMSource(source);
        if (answer == null && source instanceof StaxSource) {
            answer = toDOMSourceFromStax((StaxSource) source);
        }
        return answer;
    }

    public SAXSource toSAXSource(Source source) throws IOException, SAXException, TransformerException {
        SAXSource answer = super.toSAXSource(source);
        if (answer == null && source instanceof StaxSource) {
            answer = toSAXSourceFromStax((StaxSource) source);
        }
        return answer;
    }

    public DOMSource toDOMSourceFromStax(StaxSource source) throws TransformerException {
        Transformer transformer = createTransfomer();
        DOMResult result = new DOMResult();
        transformer.transform(source, result);
        return new DOMSource(result.getNode(), result.getSystemId());
    }

    public SAXSource toSAXSourceFromStax(StaxSource source) {
        return (SAXSource) source;
    }

    // Properties
    //-------------------------------------------------------------------------
    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = createInputFactory();
        }
        return inputFactory;
    }

    public void setInputFactory(XMLInputFactory inputFactory) {
        this.inputFactory = inputFactory;
    }
    
    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = createOutputFactory();
        }
        return outputFactory;
    }
    
    public void setOutputFactory(XMLOutputFactory outputFactory) {
        this.outputFactory = outputFactory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected XMLInputFactory createInputFactory() {
        XMLInputFactory answer = XMLInputFactory.newInstance();
        return answer;
    }

    protected XMLOutputFactory createOutputFactory() {
        XMLOutputFactory answer = XMLOutputFactory.newInstance();
        return answer;
    }

}
