/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: WBXML Reader 
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
package org.openmdx.base.wbxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.XMLConstants;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.Charsets;
import org.openmdx.base.xml.spi.FeatureSupport;
import org.openmdx.base.xml.spi.LargeObjectWriter;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * WBXML Reader
 */
public class WBXMLReader implements XMLReader, FeatureSupport {

    /**
     * Constructor
     * <p>
     * The plug-in is
     * <ul>
     * <li>either set via setProperty({@link WBXMLPlugIns#PLUG_IN}, plugIn)
     * <li>or PUBLIC id based (per document)
     * </ul>
     */
    public WBXMLReader() {
        this.plugIn = null;
    }

    /**
     * Constructor
     *
     * @param plugIn
     *            the {@link WBXMLPlugIns#PLUG_IN PlugIn} property
     */
    public WBXMLReader(
        PlugIn plugIn
    ) {
        this.plugIn = plugIn;
    }

    /**
     * The <em>Exhaust</em> feature
     */
    public static final String EXHAUST = "http://openmdx.org/wbxml/features/exhaust";

    /**
     * 
     */
    private ContentHandler contentHandler;

    /**
     * 
     */
    private ErrorHandler errorHandler;

    /**
     * The {@link WBXMLPlugIns#PLUG_IN} property value
     */
    private PlugIn plugIn;

    /**
     * The buffer for readStringI()
     */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    /**
     * The element queue
     */
    private final Deque<Object> stack = new ArrayDeque<Object>();

    /**
     * 
     */
    private InputStream in;

    /**
     * 
     */
    private Locator locator;

    /**
     * The curren attribute page, initially <code>0</code>
     */
    private int attributePage;

    /**
     * The current tag page, initially <code>0</code>
     */
    private int tagPage;

    /**
     * The character encoding
     */
    private String charset;

    /**
     * Character Data Buffer
     */
    private char[] characters = new char[256];

    /**
     * Read to the end of the input file if <code>true</code>
     */
    private boolean exhaust = true;

    /**
     * Determine the plug-in to be used for this document
     * 
     * @param publicDocumentId
     * 
     * @return the plug-in to be used for this document
     * @throws ServiceException
     */
    private PlugIn getPlugIn(
        String publicDocumentId
    )
        throws ServiceException {
        if (this.plugIn == null) {
            if (publicDocumentId != null) {
                return WBXMLPlugIns.newInstance(publicDocumentId);
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "A PUBLIC document id must be provided unless a WBXML plug-in has been configured"
            );
        } else {
            this.plugIn.reset();
            return this.plugIn;
        }
    }

    /**
     * Create a string source reading the content from the given input stream
     * 
     * @param charset
     * @param in
     * @param byteCount
     * 
     * @return a new string source
     * 
     * @throws IOException
     */
    protected StringSource newStringSource(
        Charset charset,
        InputStream in,
        int byteCount
    )
        throws IOException {
        return StandardStringSource.isSupported(charset) ? new StandardStringSource(
            charset,
            in,
            byteCount
        ) : new GenericStringSource(
            charset,
            in,
            byteCount
        );
    }

    /**
     * Parse the binary stream
     * 
     * @param in
     *            the stream to be parsed
     * @throws SAXException
     * @throws IOException
     */
    private void parse(
        InputStream in
    )
        throws IOException,SAXException {
        this.tagPage = 0;
        this.attributePage = 0;
        this.in = in;
        this.stack.clear();
        try {
            //
            // Version
            //
            int version = readByte();
            if (version != Version.SPECIFICATION_VERSION_ID) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Unsupported WBXML Version",
                    new BasicException.Parameter("supported", Version.SPECIFICATION_VERSION),
                    new BasicException.Parameter("requested", Version.toString(version))
                );
            }
            //
            // Document type
            //
            final int publicid = readInt();
            final String publicDocumentId = publicid == ExternalIdentifiers.LITERAL ? readStringFromTable()
                : ExternalIdentifiers.toPublicDocumentId(publicid);
            this.plugIn = getPlugIn(publicDocumentId);
            //
            // Character Encoding
            //
            int mibEnum = readInt();
            this.charset = mibEnum == 0 ? "UTF-8" : Charsets.toCharsetName(mibEnum);
            //
            // String Table
            //
            int stringTableSize = readInt();
            if (stringTableSize > 0) {
                this.plugIn.setStringTable(
                    newStringSource(
                        Charset.forName(this.charset),
                        this.in,
                        stringTableSize
                    )
                );
            }
            //
            // Body
            //
            this.contentHandler.startDocument();
            for (boolean epilog = false; !epilog;) {
                int id = this.in.read();
                switch (id) {
                    case -1:
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE,
                            "Unclosed elements at end of file",
                            new BasicException.Parameter("left", this.stack)
                        );
                    case GlobalTokens.SWITCH_PAGE:
                        this.tagPage = readByte();
                        break;
                    case GlobalTokens.END:
                        Object tag = this.stack.removeLast();
                        if (tag instanceof CodeResolution) {
                            CodeResolution t = (CodeResolution) tag;
                            this.contentHandler.endElement(
                                t.namespaceURI,
                                t.localName,
                                t.name
                            );
                        } else {
                            this.contentHandler.endElement(
                                XMLConstants.NULL_NS_URI,
                                (String) tag,
                                (String) tag
                            );
                        }
                        epilog = this.stack.isEmpty();
                        break;

                    case GlobalTokens.ENTITY:
                        this.characters[0] = (char) readInt();
                        this.contentHandler.characters(this.characters, 0, 1);
                        break;

                    case GlobalTokens.STR_I: {
                        String s = readInlineString();
                        if (this.characters.length < s.length()) {
                            int newLength = 2 * this.characters.length;
                            while (newLength < s.length()) {
                                newLength *= 2;
                            }
                            this.characters = new char[newLength];
                        }
                        s.getChars(0, s.length(), characters, 0);
                        this.contentHandler.characters(characters, 0, s.length());
                        break;
                    }

                    case GlobalTokens.EXT_I_0:
                    case GlobalTokens.EXT_I_1:
                    case GlobalTokens.EXT_I_2:
                    case GlobalTokens.EXT_T_0:
                    case GlobalTokens.EXT_T_1:
                    case GlobalTokens.EXT_T_2:
                    case GlobalTokens.EXT_0:
                    case GlobalTokens.EXT_1:
                    case GlobalTokens.EXT_2:
                    case GlobalTokens.OPAQUE:
                        handleExtensions(id);
                        break;

                    case GlobalTokens.PI:
                        readProcessingInstruction();
                        break;

                    case GlobalTokens.STR_T: {
                        String s = readStringFromTable();
                        if (this.characters.length < s.length()) {
                            int newLength = 2 * this.characters.length;
                            while (newLength < s.length()) {
                                newLength *= 2;
                            }
                            this.characters = new char[newLength];
                        }
                        s.getChars(0, s.length(), this.characters, 0);
                        this.contentHandler.characters(this.characters, 0, s.length());
                        break;
                    }

                    default:
                        Object resolvedTag = this.plugIn.resolveTag(this.tagPage, (id & 0x03f));
                        if (resolvedTag instanceof CodeResolution) {
                            this.readElement(
                                (CodeResolution) resolvedTag,
                                (id & GlobalTokens.ATTRIBUTE_FLAG) != 0,
                                (id & GlobalTokens.CONTENT_FLAG) != 0
                            );
                        } else {
                            this.readElement(
                                (String) resolvedTag,
                                (id & GlobalTokens.ATTRIBUTE_FLAG) != 0,
                                (id & GlobalTokens.CONTENT_FLAG) != 0
                            );
                        }
                        epilog = this.stack.isEmpty();
                }
            }
            if (this.exhaust) {
                for (int id = this.in.read(); id >= 0; id = this.in.read()) {
                    if (id == GlobalTokens.PI) {
                        this.readProcessingInstruction();
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE,
                            "Only processing instructions are allowed in epilog",
                            new BasicException.Parameter("expected", GlobalTokens.PI),
                            new BasicException.Parameter("actual", id)
                        );
                    }
                }
            }
            this.contentHandler.endDocument();
        } catch (ServiceException exception) {
            BasicException cause = exception.getCause();
            throw new SAXParseException(
                cause.getDescription(),
                this.locator,
                cause
            );
        }
    }

    // -------------- internal methods start here --------------------

    private void handleExtensions(
        int id
    )
        throws ServiceException,IOException,SAXException {
        switch (id) {
            case GlobalTokens.EXT_I_0:
                this.plugIn.ext0(readInlineString());
                break;
            case GlobalTokens.EXT_I_1:
                this.plugIn.ext1(readInlineString());
                break;
            case GlobalTokens.EXT_I_2:
                this.plugIn.ext2(readInlineString());
                break;

            case GlobalTokens.EXT_T_0:
                this.plugIn.ext0(readInt());
                break;
            case GlobalTokens.EXT_T_1:
                this.plugIn.ext1(readInt());
                break;
            case GlobalTokens.EXT_T_2:
                this.plugIn.ext2(readInt());
                break;

            case GlobalTokens.EXT_0:
                this.plugIn.ext0();
                break;
            case GlobalTokens.EXT_1:
                this.plugIn.ext1();
                break;
            case GlobalTokens.EXT_2:
                this.plugIn.ext2();
                break;

            case GlobalTokens.OPAQUE: {
                int len = readInt();
                byte[] buf = new byte[len];
                for (int i = 0; i < len; i++) {
                    buf[i] = (byte) readByte();
                }
                if (this.contentHandler instanceof LargeObjectWriter) {
                    ((LargeObjectWriter) this.contentHandler).writeBinaryData(buf, 0, len);
                } else {
                    this.plugIn.opaque(buf);
                }
            } // case OPAQUE
        } // SWITCH
    }

    /**
     * Read a processing instruction
     * 
     * @throws ServiceException
     * @throws SAXException
     * @throws IOException
     */
    private void readProcessingInstruction()
        throws IOException,SAXException,ServiceException {
        Attributes processingInstruction = readAttributes();
        this.contentHandler.processingInstruction(
            processingInstruction.getQName(0),
            processingInstruction.getValue(0)
        );
    }

    /**
     * Read attributes or a processing instruction
     * 
     * @return the attributes holder
     * 
     * @throws IOException
     * @throws SAXException
     * @throws ServiceException
     */
    private Attributes readAttributes()
        throws IOException,SAXException,ServiceException {
        AttributesImpl result = new AttributesImpl();
        for (int id = readByte(); id != GlobalTokens.END;) {
            if (id == GlobalTokens.SWITCH_PAGE) {
                this.attributePage = readByte();
                id = readByte();
            } else {
                CodeResolution attribute = this.plugIn.resolveAttributeStart(this.attributePage, id);
                String value = attribute.valueStart;
                StringBuilder buffer = null;
                id = readByte();
                while (id > 128 || id == GlobalTokens.ENTITY || id == GlobalTokens.STR_I || id == GlobalTokens.STR_T
                    || (id >= GlobalTokens.EXT_I_0 && id <= GlobalTokens.EXT_I_2) || (id >= GlobalTokens.EXT_T_0
                        && id <= GlobalTokens.EXT_T_2)) {
                    if (buffer == null) {
                        buffer = new StringBuilder(value);
                    }
                    switch (id) {
                        case GlobalTokens.ENTITY:
                            buffer.append((char) readInt());
                            break;
                        case GlobalTokens.STR_I:
                            buffer.append(readInlineString());
                            break;
                        case GlobalTokens.EXT_I_0:
                        case GlobalTokens.EXT_I_1:
                        case GlobalTokens.EXT_I_2:
                        case GlobalTokens.EXT_T_0:
                        case GlobalTokens.EXT_T_1:
                        case GlobalTokens.EXT_T_2:
                        case GlobalTokens.EXT_0:
                        case GlobalTokens.EXT_1:
                        case GlobalTokens.EXT_2:
                        case GlobalTokens.OPAQUE:
                            handleExtensions(id);
                            break;
                        case GlobalTokens.STR_T:
                            buffer.append(readStringFromTable());
                            break;
                        default:
                            buffer.append(this.plugIn.resolveAttributeValue(this.attributePage, id));
                    }
                    id = readByte();
                }
                if (buffer != null) {
                    value = buffer.toString();
                }
                result.addAttribute(attribute.namespaceURI, attribute.localName, attribute.name, null, value);
            }
        }
        return result;
    }

    private void readElement(
        CodeResolution tag,
        boolean hasAttributes,
        boolean hasContent
    )
        throws IOException,SAXException,ServiceException {
        this.contentHandler.startElement(
            tag.namespaceURI,
            tag.localName,
            tag.name,
            hasAttributes ? readAttributes() : new AttributesImpl()
        );
        if (hasContent) {
            this.stack.add(tag);
        } else {
            this.contentHandler.endElement(
                tag.namespaceURI,
                tag.localName,
                tag.name
            );
        }
    }

    private void readElement(
        String tag,
        boolean hasAttributes,
        boolean hasContent
    )
        throws IOException,SAXException,ServiceException {
        contentHandler.startElement(
            XMLConstants.NULL_NS_URI,
            tag,
            tag,
            hasAttributes ? readAttributes() : new AttributesImpl()
        );
        if (hasContent) {
            stack.add(tag);
        } else {
            contentHandler.endElement(
                XMLConstants.NULL_NS_URI,
                tag,
                tag
            );
        }
    }

    int readByte()
        throws IOException,SAXException {
        int i = in.read();
        if (i == -1)
            throw new SAXException("Unexpected EOF");
        return i;
    }

    /**
     * Read a multi-byte integer value
     * 
     * @return the <code>Integer</code> value
     * 
     * @throws SAXException
     * @throws IOException
     */
    private int readInt()
        throws SAXException,IOException {
        int result = 0;
        int i;
        do {
            i = readByte();
            result <<= 7;
            result |= i & 0x7f;
        } while ((i & 0x80) != 0);
        return result;
    }

    /**
     * Read inline string
     * 
     * @return the <code>String</code> value
     * 
     * @throws IOException
     * @throws SAXException
     */
    private String readInlineString()
        throws IOException,SAXException {
        this.buffer.reset();
        while (true) {
            int i = in.read();
            if (i == -1)
                throw new SAXException("Unexpected EOF");
            if (i == 0)
                return buffer.toString(this.charset);
            buffer.write(i);
        }
    }

    /**
     * Read string from table
     * 
     * @return the <code>String</code> value
     * 
     * @throws IOException
     * @throws SAXException
     * @throws ServiceException
     */
    private String readStringFromTable()
        throws IOException,SAXException,ServiceException {
        return this.plugIn.resolveString(readInt()).toString();
    }

    //------------------------------------------------------------------------
    // Implements XMLReader
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getContentHandler()
     */
    @Override
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getDTDHandler()
     */
    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(
        String name
    )
        throws SAXNotRecognizedException,SAXNotSupportedException {
        if (WBXMLReader.EXHAUST.equals(name)) {
            return this.exhaust;
        } else
            throw new SAXNotRecognizedException(
                "Feature: " + name
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.xml.spi.FeatureSupport#isFeatureSupported(java.lang.String)
     */
    @Override
    public boolean isFeatureSupported(
        String feature
    ) {
        return WBXMLReader.EXHAUST.equals(feature);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(
        String name
    )
        throws SAXNotRecognizedException,SAXNotSupportedException {
        if (WBXMLPlugIns.PLUG_IN.equals(name)) {
            return this.plugIn;
        } else {
            throw new SAXNotRecognizedException("Property: " + name);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    @Override
    public void parse(
        final InputSource input
    )
        throws IOException,SAXException {
        this.locator = newLocator(input);
        if (input.getByteStream() != null) {
            parse(input.getByteStream());
        } else if (input.getSystemId() != null) {
            parse(new URL(input.getSystemId()).openStream());
        } else if (input.getCharacterStream() != null) {
            throw new SAXNotSupportedException(
                "A WBXML reader needs binary input"
            );
        } else {
            throw new SAXException("Empty input source");
        }
    }

    /**
     * Creates a WBXML Locator
     * 
     * @param input
     *            the input source
     * 
     * @return a WBXML Locator
     */
    private Locator newLocator(
        final InputSource input
    ) {
        return new Locator() {

            @Override
            public int getColumnNumber() {
                return -1;
            }

            @Override
            public int getLineNumber() {
                return -1;
            }

            @Override
            public String getPublicId() {
                return input.getPublicId();
            }

            @Override
            public String getSystemId() {
                return input.getSystemId();
            }

        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    @Override
    public void parse(
        String systemId
    )
        throws IOException,SAXException {
        parse(new InputSource(systemId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    @Override
    public void setContentHandler(
        ContentHandler handler
    ) {
        this.contentHandler = handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    @Override
    public void setDTDHandler(
        DTDHandler handler
    ) {
        // no DTD will be provided
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(
        EntityResolver resolver
    ) {
        // no entity resolution will take place
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(
        ErrorHandler handler
    ) {
        this.errorHandler = handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    @Override
    public void setFeature(
        String name,
        boolean value
    )
        throws SAXNotRecognizedException,SAXNotSupportedException {
        if (WBXMLReader.EXHAUST.equals(name)) {
            this.exhaust = value;
        } else
            throw new SAXNotRecognizedException(
                "Feature: " + name
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(
        String name,
        Object value
    )
        throws SAXNotRecognizedException,SAXNotSupportedException {
        if (WBXMLPlugIns.PLUG_IN.equals(name)) {
            this.plugIn = (PlugIn) value;
        } else {
            throw new SAXNotRecognizedException("Property: " + name);
        }
    }

}
