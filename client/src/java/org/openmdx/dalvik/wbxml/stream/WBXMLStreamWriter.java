/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: WBXML Stream Writer 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD licensev as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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
package org.openmdx.dalvik.wbxml.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

import java.util.ArrayDeque;



import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.Charsets;
import org.openmdx.base.wbxml.CodeResolution;
import org.openmdx.base.wbxml.CodeToken;
import org.openmdx.base.wbxml.DynamicPlugIn;
import org.openmdx.base.wbxml.ExternalIdentifiers;
import org.openmdx.base.wbxml.GlobalTokens;
import org.openmdx.base.wbxml.PlugIn;
import org.openmdx.base.wbxml.StringToken;
import org.openmdx.base.wbxml.Version;
import org.openmdx.base.wbxml.WBXMLPlugIns;
import org.openmdx.base.xml.Entities;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamException;
import org.openmdx.dalvik.xml.stream.AbstractXMLStreamWriter;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;

/**
 * WBXML Stream Writer 
 */
public class WBXMLStreamWriter extends AbstractXMLStreamWriter {

    /**
     * Constructor 
     */
    public WBXMLStreamWriter(
        PlugIn plugIn,
        OutputStream out,
        String defaultEncoding
    ){
        this.plugIn = plugIn;
        this.out = out;
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Group 1 represents the public id
     */
    private static Pattern DOCTYPEDECL_PUBLIC_ID = Pattern.compile(
        "^<!DOCTYPE\\s+[^\\s]+\\s+PUBLIC\\s+\"([^\"]+)\"\\s+\"[^\"]+\".*>$"
    );

    private final String defaultEncoding;
    private Writer writer = null;
    private PlugIn plugIn;
    protected final OutputStream out;
    private int tagPage = 0;
    private int attributePage = 0;
    private String publicDocumentIdentifier = null;
    private int documentEncoding = 0;
    private boolean prologPending = false;
    private boolean tagPending = false;
    private final Queue<CodeResolution> pendingAttributes = new ArrayDeque<CodeResolution>();

    /**
     * Append an integer value
     * 
     * @param value
     * 
     * @throws IOException
     */
    private void appendValue(
        int value
    ) throws IOException{
        int source = value;
        byte[] target = new byte[5];
        int idx = 0;
        do {
            target[idx++] = (byte) (source & 0x7f);
            source >>= 7;
        } while (source != 0);
        while (idx > 1) {
            this.out.write(target[--idx] | 0x80);
        }
        this.out.write(target[0]);
    }

    /**
     * Append a null-terminated string value
     * 
     * @param value
     * 
     * @throws IOException 
     */
    private void appendValue(
        String value
    ) throws IOException{
        this.writer.write(value);
        this.writer.write('\u0000');
        this.writer.flush();
    }

    /**
     * Append an inline string 
     * 
     * @param data
     * @param offset
     * @param length
     * 
     * @throws IOException
     */
    private void appendStrI(
        char[] data,
        int offset,
        int length
    ) throws IOException {
        this.out.write(GlobalTokens.STR_I);
        this.writer.write(data, offset, length);
        this.writer.write('\u0000');
        this.writer.flush();
    }

    /**
     * Append a string table reference
     * 
     * @param s
     * 
     * @throws XMLStreamException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private void appendStrT(
        String s
    ) throws XMLStreamException, IOException {
        StringToken stringToken = this.plugIn.getStringToken(s);
        if (stringToken == null) {
            throw new XMLStreamException("String table lookup failure: " + s);
        } else {
            if(stringToken.isNew()) {
                this.appendNewToken(stringToken, s);
            }
            this.out.write(GlobalTokens.STR_T);
            appendValue(stringToken.getCode());
        }
    }

    /**
     * Append an in-line string or a string reference
     * 
     * @param value
     * 
     * @throws IOException
     */
    private void appendString(
        String value
    ) throws IOException {
        StringToken token = this.plugIn.findStringToken(value);
        if(token ==  null) {
            this.out.write(GlobalTokens.STR_I);
            this.appendValue(value);
        } else {
            this.out.write(GlobalTokens.STR_T);
            appendValue(token.getCode());
            return;
        }
    }
    
    /**
     * Determines whether we are using a dynamic plug-in
     * 
     * @return <code>true</code> if we are using a dynamic plug-in
     */
    protected boolean isDynamic() {
        return this.plugIn instanceof DynamicPlugIn;
    }
    
    protected void appendNewToken(
        CodeToken token,
        String value
    ) throws IOException{
        if(isDynamic()) {
            this.out.write(GlobalTokens.EXT_T_0);
            this.appendValue(token.intValue());
            this.out.write(GlobalTokens.EXT_I_0);
            this.appendValue(value);
        }
    }

    protected void appendNewToken(
        StringToken token,
        String value
    ) throws IOException{
        if(isDynamic()) {
            this.out.write(GlobalTokens.EXT_T_1);
            this.appendValue(token.getCode());
            this.out.write(GlobalTokens.EXT_I_1);
            this.appendValue(value);
        }
    }

    /**
     * Attribute or processing instruction name/value pairs
     */
    private void appendNameValuePair(
        String namespaceURI,
        String elementName,
        String name,
        String value
    ) throws XMLStreamException, IOException{
        CodeToken attributeToken = value == null ? this.plugIn.getAttributeNameToken(
            namespaceURI,
            elementName,
            name
        ) : this.plugIn.findAttributeStartToken(
            false,
            namespaceURI,
            elementName,
            name,
            value
        );
        if(attributeToken == null) {
            throw new XMLStreamException("Name space overflow, string table extension no yet implemented");
        } else {
            int attributePage = attributeToken.getPage();
            if(this.attributePage != attributePage) {
                this.appendSwitchPage(attributePage);
                this.attributePage = attributePage;
            }
            if(attributeToken.isNew()) {
                appendNewToken(attributeToken, value);
            }
            this.out.write(attributeToken.getCode());
            if(value != null && value.length() > 0){
                if(attributeToken.length() > name.length()) {
                    int remaining = name.length() + 1 + value.length() - attributeToken.length();
                    if(remaining > 0){
                        appendString(value.substring(value.length() - remaining));
                    }
                } else {
                    appendString(value);
                }

            }
        }
    }

    /**
     * Append switch page instruction
     * 
     * @param to the page to switch to
     * 
     * @throws IOException
     */
    private void appendSwitchPage(Integer to) throws IOException {
        this.out.write(GlobalTokens.SWITCH_PAGE);
        appendValue(to);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeDTD(java.lang.String)
     */
    @Override
    public void writeDTD(
        String dtd
    ) throws XMLStreamException {
        Matcher publicIdMatcher = DOCTYPEDECL_PUBLIC_ID.matcher(dtd);
        if(publicIdMatcher.matches()) {
            this.publicDocumentIdentifier = publicIdMatcher.group(1);
            if(this.plugIn == null) try {
                this.plugIn = WBXMLPlugIns.newInstance(this.publicDocumentIdentifier);
            } catch (IllegalArgumentException exception) {
                throw new XMLStreamException(
                    "DTD based plug-in activation failed",
                    exception
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#close()
     */
//  @Override
    public void close(
    ) throws XMLStreamException {
        try {
            out.close();
            this.plugIn.reset();
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#flush()
     */
//  @Override
    public void flush(
    ) throws XMLStreamException {
        try {
            this.out.flush();
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeAttribute(java.lang.String, java.lang.String)
     */
//  @Override
    public void writeAttribute(
        String name,
        String value
    ) throws XMLStreamException {
        int i = name.indexOf(':');
        this.pendingAttributes.add(
            i < 0 ? new CodeResolution(
                getCurrentNamespaceURI(),
                getCurrentElementName(),
                name,
                value
            ) : new CodeResolution(
                super.getNamespaceURI(name.substring(0, i)),
                name.substring(i + 1),
                name,
                value
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String)
     */
//  @Override
    public void writeAttribute(
        String namespaceURI,
        String localName,
        String value
    ) throws XMLStreamException {
        this.pendingAttributes.add(
            new CodeResolution(
                namespaceURI,
                null, // indicates namespace global scope
                localName,
                value
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
//  @Override
    public void writeAttribute(
        String prefix,
        String namespaceURI,
        String localName,
        String value
    ) throws XMLStreamException {
        validatePrefix(namespaceURI, prefix);
        writeAttribute(namespaceURI, localName, value);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeCData(java.lang.String)
     */
//  @Override
    public void writeCData(
        String data
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            appendString(data);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeCharacters(java.lang.String)
     */
//  @Override
    public void writeCharacters(
        String data
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            appendString(data);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeCharacters(char[], int, int)
     */
//  @Override
    public void writeCharacters(
        char[] data,
        int offset,
        int length
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            appendStrI(data, offset,length);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.BinaryDataWriter#writeBinaryData(org.w3c.cci2.BinaryLargeObject)
     */
//  @Override
    public void writeBinaryData(
        BinaryLargeObject data
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            Long length = data.getLength();
            if(length == null) {
                //
                // Length is unknown
                // 
                ByteArrayOutputStream counter = new ByteArrayOutputStream(){

                    @Override
                    public void flush(
                    ) throws IOException {
                        out.write(super.buf, 0, super.count);
                    }

                };
                BinaryLargeObjects.streamCopy(
                    data.getContent(),
                    0,
                    counter
                );
                this.out.write(GlobalTokens.OPAQUE);
                this.appendValue(counter.size());
                counter.flush();
            } else {
                //
                // Length is known
                // 
                this.out.write(GlobalTokens.OPAQUE);
                this.appendValue(length.intValue());
                BinaryLargeObjects.streamCopy(
                    data.getContent(),
                    0,
                    this.out
                );
            }
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }

    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(byte[], int, int)
     */
//  @Override
    public void writeBinaryData(
        byte[] data,
        int offset,
        int length
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            this.out.write(GlobalTokens.OPAQUE);
            this.appendValue(length);
            this.out.write(data, offset, length);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.ExtendedXMLStreamWriter#writeCharacterData(org.w3c.cci2.CharacterLargeObject)
     */
//  @Override
    public void writeCharacterData(
        CharacterLargeObject data
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            this.out.write(GlobalTokens.STR_I);
            CharacterLargeObjects.streamCopy(
                data.getContent(),
                0l,
                this.writer
            );
            this.writer.write('\u0000');
            this.writer.flush();
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeComment(java.lang.String)
     */
//  @Override
    public void writeComment(
        String comment
    ) throws XMLStreamException {
        // comments must be discarded
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEmptyElement(java.lang.String)
     */
//  @Override
    public void writeEmptyElement(
        String name
    ) throws XMLStreamException {
        writeEmptyElement(
            name,
            getDefaultNamespaceURI()
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String)
     */
//  @Override
    public void writeEmptyElement(
        String localName,
        String namespaceURI
    ) throws XMLStreamException {
        this.flushStartElement(true);
        try {
            CodeToken token = this.plugIn.getTagToken(namespaceURI, localName);
            if(token == null) {
                throw new XMLStreamException("Name space overflow, string table extension no yet implemented");
            } else {
                int page = token.getPage();
                if(this.tagPage != page) {
                    appendSwitchPage(page);
                    this.tagPage = page;
                }
                if(token.isNew()) {
                    appendNewToken(token, localName);
                }
                this.out.write(token.getCode());
            }
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeEmptyElement(java.lang.String, java.lang.String, java.lang.String)
     */
//  @Override
    public void writeEmptyElement(
        String prefix,
        String localName,
        String namespaceURI
    ) throws XMLStreamException {
        validatePrefix(namespaceURI, prefix);
        writeEmptyElement(localName, namespaceURI);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEndDocument()
     */
//  @Override
    public void writeEndDocument(
    ) throws XMLStreamException {
        // nothing to do
    }

    private void appendEndElement(
    ) throws XMLStreamException {
        if(!this.flushStartElement(false)) {
            try {
                this.out.write(GlobalTokens.END);
            } catch (IOException exception) {
                throw toXMLStreamException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeEndElement()
     */
//  @Override
    public void writeEndElement(
    ) throws XMLStreamException {
        appendEndElement();
        leaveNamespaceScope();
    }

    /**
     * Writes an entity reference
     * <p>
     * As opposed to the WBXML specification this method does not replace
     * encodable entities by their character representation
     */
//  @Override
    public void writeEntityRef(
        String name
    ) throws XMLStreamException {
        try {
            this.out.write(GlobalTokens.ENTITY);
            appendValue(Entities.valueOf(name));
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /**
     * Defaults to<ul>
     * <li>Default Encoding
     * <li>XML version 1.0
     * </ul>
     */
    @Override
    public void writeStartDocument(
    ) throws XMLStreamException {
        writeStartDocument(this.defaultEncoding, Version.SPECIFICATION_VERSION);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeStartDocument(java.lang.String, java.lang.String)
     */
//  @Override
    public void writeStartDocument(
        String encoding,
        String version
    ) throws XMLStreamException {
        this.prologPending = true;
        try {
            //
            // Version
            //
            if(!Version.SPECIFICATION_VERSION.equals(version)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported WBXML version",
                new BasicException.Parameter("supported", Version.SPECIFICATION_VERSION),
                new BasicException.Parameter("requested", version)
            );
            //
            // Encoding
            //
            this.writer = newWriter(this.out, encoding);
            this.documentEncoding = Charsets.toEnum(encoding);
        } catch (Exception exception) {
            throw toXMLStreamException(exception);
        }
    }

    /**
     * Flushes the document header
     * 
     * @throws XMLStreamException
     */
    private void flushStartDocument(
    ) throws XMLStreamException {
        if(this.prologPending) try {
            this.out.write(Version.SPECIFICATION_VERSION_ID);
            String xmlPublicDocumentIdentifier = this.publicDocumentIdentifier == null ?
                this.plugIn.getDefaultDocumentPublicIdentifier(this.getDefaultNamespaceURI()) :
                this.publicDocumentIdentifier;
            int wbxmlPublicDocumentIdentifier = ExternalIdentifiers.toPublicDocumentId(xmlPublicDocumentIdentifier);
            this.appendValue(wbxmlPublicDocumentIdentifier);
            if(wbxmlPublicDocumentIdentifier == ExternalIdentifiers.LITERAL) {
                this.appendValue(xmlPublicDocumentIdentifier);
            }
            this.appendValue(this.documentEncoding);
            if(this.plugIn.isStringTableReadyAtStartOfDocument()) {
                ByteBuffer stringTable = this.plugIn.getStringTable();
                int size = stringTable == null ? 0 : stringTable.remaining();
                this.appendValue(size);
                if(size > 0) {
                    this.out.write(stringTable.array(), stringTable.position(), stringTable.remaining());
                }
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                "Lazy string table retrieval is not yet implemented"
            );
            NamespaceContext namespaceContext = this.plugIn.getNamespaceContext();
            if(namespaceContext != null) {
                super.setNamespaceContext(namespaceContext);
            }
            this.prologPending = false;
        } catch (Exception exception) {
            throw toXMLStreamException(exception);
        }
    }

    /**
     * Flushes the start element tag
     * 
     * @param hasContent
     * 
     * @return <code>true</code> if there was a pending tag to be flushed
     * 
     * @throws XMLStreamException
     */
    private boolean flushStartElement(
        boolean hasContent
    ) throws XMLStreamException {
        if(this.prologPending) this.flushStartDocument();
        boolean flushing = this.tagPending;
        if(flushing) try {
            String localName = getCurrentElementName();
            CodeToken tagToken = this.plugIn.getTagToken(getCurrentNamespaceURI(), localName);
            if(tagToken == null) {
                throw new XMLStreamException("Unable to get token from code page (localName=>" + localName + "<, namespace=>" + this.getCurrentNamespaceURI() + "<)");
            } else {
                int tagPage = tagToken.getPage();
                int tag = tagToken.getCode();
                if(this.tagPage != tagPage) {
                    appendSwitchPage(tagPage);
                    this.tagPage = tagPage;
                }
                if(tagToken.isNew()) {
                    appendNewToken(tagToken, localName);
                }
                boolean hasAttributes = !this.pendingAttributes.isEmpty();
                if(hasAttributes){
                    tag |= GlobalTokens.ATTRIBUTE_FLAG;
                }
                if(hasContent) {
                    tag |= GlobalTokens.CONTENT_FLAG;
                }
                this.out.write(tag);
                if(hasAttributes){
                    while(!this.pendingAttributes.isEmpty()) {
                        CodeResolution attribute = this.pendingAttributes.remove();
                        this.appendNameValuePair(
                            attribute.namespaceURI,
                            "".equals(attribute.localName) ? null : attribute.localName,
                            attribute.name,
                            attribute.valueStart
                        );
                    }
                    this.out.write(GlobalTokens.END);
                }
            }
            this.tagPending = false;
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
        return flushing;
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeStartElement(java.lang.String)
     */
//  @Override
    public void writeStartElement(
        String name
    ) throws XMLStreamException {
        int i = name.indexOf(':');
        if(i < 0) {
            writeStartElement(
                name,
                super.getDefaultNamespaceURI()
            );
        } else {
            writeStartElement(
                name.substring(i + 1),
                super.getNamespaceURI(name.substring(0, i))
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeStartElement(java.lang.String, java.lang.String, java.lang.String)
     */
//  @Override
    public void writeStartElement(
        String localName,
        String namespaceURI
    ) throws XMLStreamException {
        this.flushStartElement(true);
        enterNamespaceScope(
            namespaceURI,
            null,
            localName
        );
        this.tagPending = true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeStartElement(java.lang.String, java.lang.String, java.lang.String)
     */
//  @Override
    public void writeStartElement(
        String prefix,
        String localName,
        String namespaceURI
    ) throws XMLStreamException {
        validatePrefix(namespaceURI, prefix);
        writeStartElement(localName, namespaceURI);
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String)
     */
//  @Override
    public void writeProcessingInstruction(
        String target
    ) throws XMLStreamException {
        try {
            this.out.write(GlobalTokens.PI);
            this.appendNameValuePair(null, null, target, null);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeProcessingInstruction(java.lang.String, java.lang.String)
     */
//  @Override
    public void writeProcessingInstruction(
        String target,
        String value
    ) throws XMLStreamException {
        try {
            this.out.write(GlobalTokens.PI);
            this.appendNameValuePair(XMLConstants.NULL_NS_URI, null, target, value);
        } catch (IOException exception) {
            throw toXMLStreamException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(
        String property
    ) throws IllegalArgumentException {
        return
            "plugIn".equals(property) ? this.plugIn :
             super.getProperty(property);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.AbstractXMLStreamWriter#writeNamespace(java.lang.String, java.lang.String)
     */
//  @Override
    public void writeNamespace(
        String prefix,
        String uri
    ) throws XMLStreamException {
        // nothing to do for WBXML
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLStreamWriter#writeDefaultNamespace(java.lang.String)
     */
//  @Override
    public void writeDefaultNamespace(
        String uri
    ) throws XMLStreamException {
        // nothing to do for WBXML
    }

}
