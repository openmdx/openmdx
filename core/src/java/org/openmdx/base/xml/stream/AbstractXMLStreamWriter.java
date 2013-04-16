/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract XML Stream Writer 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.xml.stream;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.UTF8Writer;
import org.openmdx.base.xml.spi.LargeObjectWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract XML Stream Writer
 */
public abstract class AbstractXMLStreamWriter implements XMLStreamWriter, LargeObjectWriter {

    /**
     * Constructor 
     */
    protected AbstractXMLStreamWriter(
    ) {
        super();
    }

    /**
     * The namespace context
     */
    private NonRepairingNamespaceContext namespaceContext = new NonRepairingNamespaceContext(
        null,
        null,
        null,
        null
     );

    
    /**
     * Create a namespace scope
     * 
     * @param namespaceURI the element's namespace URI, may be <code>null</code>
     * @param prefix the element's prefix, may be <code>XMLConstants.DEFAULT_NS_PREFIX</code> or <code>null</code>
     * @param localName the elements local name
     */
    protected void enterNamespaceScope(
        String namespaceURI,
        String prefix,
        String localName
    ){
        this.namespaceContext = new NonRepairingNamespaceContext(
            this.namespaceContext,
            namespaceURI,
            prefix,
            localName
        );
    }

    /**
     * Retrieve the namespace URI of the currrent element
     * 
     * @return the namespace URI of the currrent element
     */
    protected String getCurrentNamespaceURI(){
        return this.namespaceContext.getCurrentNamespaceURI();
    }

    /**
     * Retrieve the local name of the currrent element
     * @return the local name of the currrent element
     */
    protected String getCurrentElementName(){
        return this.namespaceContext.getCurrentElementName();
    }
    
    /**
     * Create a new namespace scope
     */
    protected void leaveNamespaceScope(){
        this.namespaceContext = this.namespaceContext.getNext();
    }
    
    /**
     * Map a cause to an XMLStreamException
     * 
     * @param exception the exception to be mapped
     * 
     * @return an XMLStreamException
     */
    protected static XMLStreamException toXMLStreamException(
        Exception exception
    ){
        BasicException cause = BasicException.toExceptionStack(exception);
        return new XMLStreamException(cause.getMessage(), cause);
    }

    
    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#getNamespaceContext()
     */
//  @Override
    public NamespaceContext getNamespaceContext(
    ) {
        return this.namespaceContext;
    }
    
    /**
     * Validate a namespace URI prefix
     * 
     * @param namespaceURI
     * @param prefix
     * 
     * @return <code>true</code> if the namespace is already bound to the given prefix
     * @throws XMLStreamException  
     * 
     * @throws XMLStreamException 
     */
    protected boolean validatePrefix(
        String namespaceURI,
        String prefix
    ) throws XMLStreamException {
        try {
            Collection<Object> bound = null;
            for(
                Iterator<?> i = this.namespaceContext.getPrefixes(namespaceURI);
                i.hasNext();
            ) {
                if(prefix.equals(i)){
                    return true;
                } else {
                    if(bound == null) {
                        bound = new ArrayList<Object>();
                    }
                    bound.add(i);
                }
            }
            if(bound == null) {
                return false;
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Prefix mismatch",
                new BasicException.Parameter("namespaceURI", namespaceURI),
                new BasicException.Parameter("boundTo", bound),
                new BasicException.Parameter("prefix", prefix)
            );
        } catch (ServiceException exception) {
            throw toXMLStreamException(exception);
        }
    }
    
    /**
     * Retrieve the current default namespace URI
     * 
     * @return  the current default namespace URI
     */
    protected String getDefaultNamespaceURI(){
        return this.namespaceContext.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
    }
    
    /**
     * Retrieve the namespace URI for a given prefix
     * 
     * @param prefix
     * 
     * @return the namespace URI for the given prefix
     */
    protected String getNamespaceURI(
        String prefix
    ){
        return this.namespaceContext.getNamespaceURI(prefix);
    }
    
    /**
     * Tells whether a given namespace URI is the default namespace URI
     * 
     * @param namespaceURI
     * 
     * @return <code>true</code> if the  given namespace URI is the default namespace URI
     */
    protected boolean isDefaultNamespaceURI(
        String namespaceURI
    ){
        return getDefaultNamespaceURI().equals(namespaceURI);
    }
    
    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#getPrefix(java.lang.String)
     */
//  @Override
    public String getPrefix(
        String namespaceURI
    ) throws XMLStreamException {
        return this.namespaceContext.getPrefix(namespaceURI);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#getProperty(java.lang.String)
     */
//  @Override
    public Object getProperty(
        String property
    ) throws IllegalArgumentException {
        throw BasicException.initHolder(
            new IllegalArgumentException(
                "Unsupported property",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("property", property)
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#setDefaultNamespace(java.lang.String)
     */
//  @Override
    public void setDefaultNamespace(
        String namespaceURI
    ) throws XMLStreamException {
        this.namespaceContext.put(XMLConstants.DEFAULT_NS_PREFIX, namespaceURI);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#setNamespaceContext(javax.xml.namespace.NamespaceContext)
     */
//  @Override
    public void setNamespaceContext(
        NamespaceContext namespaceContext
    ) throws XMLStreamException {
        if(this.namespaceContext.getNext() != null) {
            throw new IllegalStateException("It is too late to set the namespace context");
        }
        this.namespaceContext = new NonRepairingNamespaceContext(
            this.namespaceContext,
            null,
            null,
            null
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#setPrefix(java.lang.String, java.lang.String)
     */
//  @Override
    public void setPrefix(
        String prefix, 
        String namespaceURI
    ) throws XMLStreamException {
        this.namespaceContext.put(
            prefix == null || XMLConstants.XMLNS_ATTRIBUTE.equals(prefix) ? XMLConstants.DEFAULT_NS_PREFIX : prefix, 
            namespaceURI
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeDTD(java.lang.String)
     */
//  @Override
    public void writeDTD(
        String dtd
    ) throws XMLStreamException {
        throw new XMLStreamException("This XML stream writer is unable to write DTDs");
    }

    /**
     * Defaults to<ul>
     * <li>Encoding UTF-8
     * <li>XML version 1.0
     * </ul>
     */
//  @Override
    public void writeStartDocument(
    ) throws XMLStreamException {
        writeStartDocument("UTF-8", "1.0");
    }

    /**
     * Defaults to<ul>
     * <li>Encoding UTF-8
     * </ul>
     */
//  @Override
    public void writeStartDocument(
        String version
    ) throws XMLStreamException {
        writeStartDocument("UTF-8", version);
    }

    /**
     * Create a Writer
     * 
     * @param binaryStream
     * @param encoding
     * 
     * @return a newly created <code>Writer</code>
     * @throws UnsupportedEncodingException
     */
    protected Writer newWriter(
        OutputStream binaryStream,
        String encoding
    ) throws UnsupportedEncodingException {
        return "UTF-8".equalsIgnoreCase(encoding) ? new UTF8Writer(
            binaryStream
        ) : new OutputStreamWriter(
            binaryStream, 
            encoding
        );
    }
    
}
