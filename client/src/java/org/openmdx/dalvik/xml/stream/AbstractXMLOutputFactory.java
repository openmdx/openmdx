/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: Abstract XML Output Factory 
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
package org.openmdx.dalvik.xml.stream;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.transform.Result;

import org.openmdx.base.xml.stream.XMLOutputFactories;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLEventWriter;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLOutputFactory;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamException;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamWriter;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract XML Output Factory 
 */
public abstract class AbstractXMLOutputFactory extends XMLOutputFactory {

    /**
     * Constructor
     * 
     * @param defaultMimeType the default MIME type
     */
    protected AbstractXMLOutputFactory(
        String defaultMimeType
    ) {
        this.mimeType = defaultMimeType;
    }

    /**
     * The MIME type
     */
    private String mimeType;
    
    /**
     * Tells which MIME type the factory supports
     * 
     * @return the MIME type the factory supports
     */
    protected final String getMimeType(){
        return this.mimeType;
    }

    /**
     * Apply the default encoding to the binary stream methods without encoding
     * 
     * @return the default encoding 
     */
    protected String getDefaultEncoding(){
        return "UTF-8";
    }
    
    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(javax.xml.transform.Result)
     */
    @Override
    public XMLEventWriter createXMLEventWriter(
        Result target
    ) throws XMLStreamException {
        throw new XMLStreamException("Result targets are not supported by this XMLOutputFactory");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.OutputStream)
     */
    @Override
    public XMLEventWriter createXMLEventWriter(
        OutputStream target
    ) throws XMLStreamException {
        return createXMLEventWriter(
            target,
            getDefaultEncoding()
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.Writer)
     */
    @Override
    public XMLEventWriter createXMLEventWriter(
        Writer target
    ) throws XMLStreamException {
        throw new XMLStreamException("Character stream targets are not supported by this XMLOutputFactory");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.OutputStream, java.lang.String)
     */
    @Override
    public XMLEventWriter createXMLEventWriter(
        OutputStream target, 
        String encoding
    ) throws XMLStreamException {
        throw new XMLStreamException("Byte stream targets are not supported by this XMLOutputFactory");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        Writer target
    ) throws XMLStreamException {
        throw new XMLStreamException("Character stream targets are not supported by this XMLOutputFactory");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        OutputStream target
    ) throws XMLStreamException {
        return createXMLStreamWriter(
            target, 
            getDefaultEncoding()
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(javax.xml.transform.Result)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        Result target
    ) throws XMLStreamException {
        throw new XMLStreamException("Result targets are not supported by this XMLOutputFactory");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream, java.lang.String)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        OutputStream target, 
        String encoding
    ) throws XMLStreamException {
        throw new XMLStreamException("Byte stream targets are not supported by this XMLOutputFactory");
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(
        String name
    ) throws IllegalArgumentException {
        if(XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
            return false;
        } else if(XMLOutputFactories.MIME_TYPE.equals(name)) {
            return this.mimeType;
        } else throw BasicException.initHolder(
            new IllegalArgumentException(
                "Unsupported property",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("name", name)
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(
        String name, 
        Object value
    ) throws IllegalArgumentException {
        if(XMLOutputFactories.MIME_TYPE.equals(name)) {
            this.mimeType = (String) value;
        } else throw BasicException.initHolder(
            new IllegalArgumentException(
                isPropertySupported(name) ? "Read-only property" : "Unsupported property",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("name", name),
                    new BasicException.Parameter("value", value)
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#isPropertySupported(java.lang.String)
     */
    @Override
    public boolean isPropertySupported(String name) {
        return
            XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name) || 
            XMLOutputFactories.MIME_TYPE.equals(name);
    }

}
