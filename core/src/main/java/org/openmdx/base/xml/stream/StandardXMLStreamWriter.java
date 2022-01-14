/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Non-Escaping XML Stream Writer
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.xml.stream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;


/**
 * Non-Escaping XML Stream Writer
 */
public class StandardXMLStreamWriter extends AbstractXMLStreamWriter {

    /**
     * Constructor 
     * 
     * @param whitespace include tab and newline characters if <code>true</code>
     * @param writer the target stream
     * @param encoding the factory defined encoding
     * 
     * @throws UnsupportedEncodingException 
     */
    protected StandardXMLStreamWriter(
        String encoding,
        boolean whitespace,
        OutputStream binaryStream
    ) throws UnsupportedEncodingException {
        this.encoding = encoding;
        this.whitespace = whitespace;
        this.stream = newWriter(binaryStream, encoding);
    }

    /**
     * Constructor 
     * 
     * @param whitespace include tab and newline characters if <code>true</code>
     * @param writer the target stream
     * @param encoding the factory defined encoding
     */
    protected StandardXMLStreamWriter(
        String encoding,
        boolean whitespace,
        Writer writer
    ) {
        this.encoding = encoding;
        this.whitespace = whitespace;
        this.stream = writer;
    }

    /**
     * The encoding is defined by the factory
     */
    private final String encoding;

    /**
     * The target stream
     */
    private Writer stream;

    /**
     * Tells whether a start tag is missing its closing angle bracket.
     */
    private boolean starting = false;

    /**
     * Detect "content but no children" elements
     */
    private int lastBegin = 0;

    /**
     * Defines whether tab and newline characters are added
     */
    private final boolean whitespace;

    /**
     * Stack of open tag names
     */
    private final Deque<String> tags = new ArrayDeque<String>();

    /**
     * The XML version supported by the standard XML writer
     */
    private static final String VERSION = "1.0";

    /**
     * 
     */
    protected static final String[] INDENTS = {"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t", "\t\t\t\t\t\t", "\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t\t\t\t\t", "\t\t\t\t\t\t\t\t\t\t\t\t\t\t"};


    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#newWriter(java.io.OutputStream, java.lang.String)
     */
    @Override
    protected Writer newWriter(
        OutputStream binaryStream,
        String encoding
    ) throws UnsupportedEncodingException {
        return new BufferedWriter(
            super.newWriter(binaryStream, encoding)
        );
    }

    /**
     * 
     * @param writer
     */
    void reset(
        Writer writer
    ){
        this.stream = writer;
    }

    private final Writer getDelegate(
    ) throws IOException{
        return this.stream;
    }

    /**
     * Complete tag if necessary
     * 
     * @throws IOException
     */
    private void completeTag(
    ) throws IOException {
        if(this.starting) {
            this.stream.write('>');
            this.starting = false;
        }
    }

    /**
     * Moves to the next line and indents according to the number of open tags
     * 
     * @throws IOException 
     */
    private void newLine(
    ) throws IOException{
        this.stream.write('\n');
        this.stream.write(INDENTS[this.tags.size()]);
    }

    /**
     * Start tag
     * 
     * @throws IOException
     */
    private void startTag(
    ) throws IOException {
        completeTag();
        if(this.whitespace){
            this.lastBegin = this.tags.size();
            newLine();
        }
    }

    /**
     * Start end tag
     * 
     * @throws IOException
     */
    private void endTag(
    ) throws IOException {
        completeTag();
        if(this.whitespace && this.lastBegin > this.tags.size()){
            this.lastBegin = this.tags.size();
            newLine();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(byte[], int, int)
     */
    @Override
    public void writeBinaryData(
        byte[] data,
        int offset,
        int length
    ) throws ServiceException {
        writeBinaryData(
            BinaryLargeObjects.valueOf(data, offset, length)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.ExtendedStreamWriter#writeBinaryData(org.w3c.cci2.BinaryLargeObject)
     */
    @Override
    public void writeBinaryData(
        BinaryLargeObject data
    ) throws ServiceException {
        try {
            completeTag();
            Base64.encode(data.getContent(), getDelegate());
        } catch (IOException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.ExtendedXMLStreamWriter#writeCharacterData(org.w3c.cci2.CharacterLargeObject)
     */
    @Override
    public void writeCharacterData(
        CharacterLargeObject data
    ) throws ServiceException {
        try {
            completeTag();
            this.stream.write("<![CDATA[");
            CharacterLargeObjects.streamCopy(
                data.getContent(),
                0l,
                getDelegate()
            );
            this.stream.write("]]>");
        } catch (IOException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#close()
     */
    @Override
    public void close(
    ) throws XMLStreamException {
        if(this.stream != null) try {
            this.flush();
            this.stream.close();
            this.stream = null;
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#flush()
     */
    @Override
    public void flush(
    ) throws XMLStreamException {
        try {
            this.stream.flush();
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String)
     */
    @Override
    public void writeAttribute(
        String name,
        String value
    ) throws XMLStreamException {
        try {
            this.stream.write(' ');
            this.stream.write(name);
            this.stream.write("=\"");
            this.stream.write(value);
            this.stream.write('"');
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void writeAttribute(
        String namespaceURI,
        String localName,
        String value
    ) throws XMLStreamException {
        if(isDefaultNamespaceURI(namespaceURI)) {
            writeAttribute(localName, value);
        } else {
            try {
                String prefix = super.getPrefix(namespaceURI);
                if(prefix == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "No prefix bound to namespace URI",
                        new BasicException.Parameter("namespaceURI", namespaceURI)
                    );
                }
                this.stream.write(' ');
                this.stream.write(prefix);
                this.stream.write(':');
                this.stream.write(localName);
                this.stream.write("=\"");
                this.stream.write(value);
                this.stream.write('"');
            } catch (ServiceException exception) {
                throw toXMLStreamException(exception);
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void writeAttribute(
        String prefix,
        String namespaceURI,
        String localName,
        String value
    ) throws XMLStreamException {
        if(
            ("".equals(namespaceURI) && (prefix == null || "".equals(prefix))) ||
            isDefaultNamespaceURI(namespaceURI)
        ) {
            writeAttribute(localName, value);
        } else {
            try {
                if(!validatePrefix(namespaceURI, prefix)) {
                    this.stream.write("xmlns:");
                    this.stream.write(prefix);
                    this.stream.write("=\"");
                    this.stream.write(namespaceURI);
                    this.stream.write('"');
                }
                this.stream.write(' ');
                this.stream.write(prefix);
                this.stream.write(':');
                this.stream.write(localName);
                this.stream.write("=\"");
                this.stream.write(value);
                this.stream.write('"');
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeCData(java.lang.String)
     */
    @Override
    public void writeCData(
        String data
    ) throws XMLStreamException {
        try {
            completeTag();
            this.stream.write("<![CDATA[");
            this.stream.write(data);
            this.stream.write("]]>");
        } catch (NullPointerException exception) {
            throw toXMLStreamException(exception);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeCharacters(java.lang.String)
     */
    @Override
    public void writeCharacters(
        String data
    ) throws XMLStreamException {
        try {
            completeTag();
            this.stream.write(data);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeCharacters(char[], int, int)
     */
    @Override
    public void writeCharacters(
        char[] data,
        int start,
        int length
    ) throws XMLStreamException {
        try {
            completeTag();
            this.stream.write(data, start, length);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeComment(java.lang.String)
     */
    @Override
    public void writeComment(
        String comment
    ) throws XMLStreamException {
        try {
            startTag();
            this.stream.write("<!--");
            this.stream.write(comment);
            this.stream.write("-->");
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String)
     */
    @Override
    public void writeEmptyElement(
        String name
    ) throws XMLStreamException {
        try {
            startTag();
            this.stream.write('<');
            this.stream.write(name);
            this.stream.write("/>");
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String)
     */
    @Override
    public void writeEmptyElement(
        String namespaceURI,
        String localName
    ) throws XMLStreamException {
        if(isDefaultNamespaceURI(namespaceURI)) {
            writeEmptyElement(localName);
        } else {
            try {
                String prefix = super.getPrefix(namespaceURI);
                if(prefix == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "No prefix bound to namespace URI",
                        new BasicException.Parameter("namespaceURI", namespaceURI)
                    );
                }
                startTag();
                this.stream.write('<');
                this.stream.write(prefix);
                this.stream.write(':');
                this.stream.write(localName);
                this.stream.write("/>");
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            } catch (ServiceException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void writeEmptyElement(
        String prefix,
        String localName,
        String namespaceURI
    ) throws XMLStreamException {
        if(isDefaultNamespaceURI(namespaceURI)) {
            writeEmptyElement(localName);
        } else {
            try {
                super.validatePrefix(namespaceURI, prefix);
                startTag();
                this.stream.write('<');
                this.stream.write(prefix);
                this.stream.write(':');
                this.stream.write(localName);
                this.stream.write("/>");
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEndDocument()
     */
    @Override
    public void writeEndDocument(
    ) throws XMLStreamException {
        if(!this.tags.isEmpty()) {
            throw BasicException.initHolder(
                new XMLStreamException(
                    "There are unclosed elements",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        new BasicException.Parameter("element", this.tags)
                    )
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEndElement()
     */
    @Override
    public void writeEndElement(
    ) throws XMLStreamException {
        try {
            String name = this.tags.removeLast();
            if(this.starting){
                this.stream.write("/>");
                this.starting = false;
            } else {
                endTag();
                this.stream.write("</");
                this.stream.write(name);
                this.stream.write('>');
            }
            super.leaveNamespaceScope();
        } catch (Exception exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEntityRef(java.lang.String)
     */
    @Override
    public void writeEntityRef(
        String entity
    ) throws XMLStreamException {
        try {
            completeTag();
            this.stream.write('&');
            this.stream.write(entity);
            this.stream.write(';');
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String)
     */
    @Override
    public void writeProcessingInstruction(
        String target
    ) throws XMLStreamException {
        try {
            startTag();
            this.stream.write("<?");
            this.stream.write(target);
            this.stream.write("?>");
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void writeProcessingInstruction(
        String target,
        String data
    ) throws XMLStreamException {
        try {
            startTag();
            this.stream.write("<?");
            this.stream.write(target);
            this.stream.write(' ');
            this.stream.write(data);
            this.stream.write("?>");
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeStartDocument(java.lang.String, java.lang.String)
     */
    @Override
    public void writeStartDocument(
        String requestedEncoding,
        String version
    ) throws XMLStreamException {
        try {
            //
            // Start tag and add version
            //
            if(!VERSION.equals(VERSION)) throw BasicException.initHolder(
                new XMLStreamException(
                    "XML version not supported by the standard XML writer",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("supported", VERSION),
                        new BasicException.Parameter("requested", version)
                    )
                )
            );
            this.stream.write("<?xml version=\"");
            this.stream.write(VERSION);
            //
            // Add encoding and close tag
            //
            final String encoding;
            if(requestedEncoding == null) {
                encoding = this.encoding;
            } else if (this.encoding == null || this.encoding.equalsIgnoreCase(requestedEncoding)){
                encoding = requestedEncoding;
            } else throw BasicException.initHolder(
                new XMLStreamException(
                    "Encoding mismatch",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("expected", this.encoding),
                        new BasicException.Parameter("actual", requestedEncoding)
                   )
                )
            );
            if(encoding != null) {
                this.stream.write("\" encoding=\"");
                this.stream.write(encoding);
            }
            this.stream.write("\"?>");
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeStartDocument(java.lang.String)
     */
    @Override
    public void writeStartDocument(
        String version
    ) throws XMLStreamException {
        if(this.encoding == null) {
            super.writeStartDocument(version);
        } else {
            writeStartDocument(this.encoding, version);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeStartDocument()
     */
    @Override
    public void writeStartDocument(
    ) throws XMLStreamException {
        if(this.encoding == null) {
            super.writeStartDocument();
        } else {
            writeStartDocument(this.encoding, "1.0");
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String)
     */
    @Override
    public void writeStartElement(
        String name
    ) throws XMLStreamException {
        try {
            int i = name.indexOf(':');
            startTag();
            super.enterNamespaceScope(
                null,
                i < 0 ? XMLConstants.DEFAULT_NS_PREFIX : name.substring(0, i),
                i < 0 ? name : name.substring(i + 1)
            );
            this.tags.add(name);
            this.stream.write('<');
            this.stream.write(name);
            this.starting = true;
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void writeStartElement(
        String prefix,
        String localName,
        String namespaceURI
    ) throws XMLStreamException {
        if(isDefaultNamespaceURI(namespaceURI)){
            writeStartElement(localName);
        } else {
            try {
                validatePrefix(namespaceURI, prefix);
                startTag();
                super.enterNamespaceScope(
                    namespaceURI,
                    prefix,
                    localName
                );
                this.tags.add(prefix + ':' + localName);
                this.stream.write('<');
                this.stream.write(prefix);
                this.stream.write(':');
                this.stream.write(localName);
                this.starting = true;
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String, java.lang.String)
     */
    @Override
    public void writeStartElement(
        String namespaceURI,
        String localName
    ) throws XMLStreamException {
        if(isDefaultNamespaceURI(namespaceURI)){
            writeStartElement(localName);
        } else {
            try {
                String prefix = super.getPrefix(namespaceURI);
                if(prefix == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "No prefix bound to namespace URI",
                        new BasicException.Parameter("namespaceURI", namespaceURI)
                    );
                }
                startTag();
                enterNamespaceScope(
                    namespaceURI,
                    null,
                    localName
                );
                this.tags.add(prefix + ':' + localName);
                this.stream.write('<');
                this.stream.write(prefix);
                this.stream.write(':');
                this.stream.write(localName);
                this.starting = true;
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            } catch (ServiceException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeNamespace(java.lang.String, java.lang.String)
     */
    @Override
    public void writeNamespace(
        String prefix,
        String uri
    ) throws XMLStreamException {
        if(
            prefix == null ||
            XMLConstants.DEFAULT_NS_PREFIX.equals(prefix) ||
            XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)
        ) {
            writeDefaultNamespace(uri);
        } else {
            try {
                this.stream.write(" xmlns:");
                this.stream.write(prefix);
                this.stream.write("=\"");
                this.stream.write(uri);
                this.stream.write('"');
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeDefaultNamespace(java.lang.String)
     */
    @Override
    public void writeDefaultNamespace(
        String uri
    ) throws XMLStreamException {
        try {
          this.stream.write(" xmlns=\"");
          this.stream.write(uri);
          this.stream.write('"');
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

}
