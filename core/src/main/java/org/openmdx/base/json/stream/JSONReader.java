/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JSONReader
 * Owner:       the original authors.
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
 * Copyright 2006 Envoi Solutions LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.base.json.stream;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * JSONReader
 *
 */
public class JSONReader implements XMLReader {

	public JSONReader(
	) {
		Map<?,?> jsonConfiguration = Collections.emptyMap();
		delegate = new StandardXMLInputFactory(jsonConfiguration);
	}
	
    //------------------------------------------------------------------------
    // Implements XMLReader
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getContentHandler()
     */
    @Override
    public ContentHandler getContentHandler(
    ) {
        return this.contentHandler;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getDTDHandler()
     */
    @Override
    public DTDHandler getDTDHandler(
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(
        String name
    ) throws SAXNotRecognizedException, SAXNotSupportedException {
    	throw new SAXNotRecognizedException("Feature: " + name);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(
        String name
    ) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property: " + name);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    @Override
    public void parse(
        final InputSource input
    ) throws IOException, SAXException {
        try {
        	final XMLStreamReader jsonReader;
	        if(input.getCharacterStream() != null) {
	        	jsonReader = delegate.createXMLStreamReader(input.getCharacterStream());
	        } else if(input.getByteStream() != null) {
	        	jsonReader = delegate.createXMLStreamReader(input.getByteStream(), input.getEncoding());
	        } else if(input.getSystemId() != null) {
	        	jsonReader = delegate.createXMLStreamReader(new URL(input.getSystemId()).openStream(), input.getEncoding());
	        } else {
	            throw new SAXException("Empty input source");
	        }
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.transform(
	            new StAXSource(jsonReader), 
	            new SAXResult(this.contentHandler)
	        );
        } catch (Exception exception) {
        	throw new SAXException("JSON reader failure", exception);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    @Override
    public void parse(
        String systemId
    ) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    @Override
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    @Override
    public void setDTDHandler(DTDHandler handler) {
        // no DTD will be provided
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(EntityResolver resolver) {
        // no entity resolution will take place
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    @Override
    public void setFeature(
        String name,
        boolean value
    ) throws SAXNotRecognizedException, SAXNotSupportedException {
    	throw new SAXNotRecognizedException("Feature: " + name);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(
        String name,
        Object value
     ) throws SAXNotRecognizedException, SAXNotSupportedException {
            throw new SAXNotRecognizedException("Property: " + name);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private final StandardXMLInputFactory delegate;
    private static TransformerFactory transformerFactory = TransformerFactory.newInstance();    
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;
    
}
