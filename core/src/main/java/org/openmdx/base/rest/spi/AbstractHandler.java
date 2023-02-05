/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractHandler 
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

package org.openmdx.base.rest.spi;

import org.openmdx.base.xml.spi.LargeObjectWriter;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.CharacterLargeObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Error handler and content handler for character data
 */
class AbstractHandler extends DefaultHandler implements LargeObjectWriter {

    /**
     * Constructor
     */
    AbstractHandler() {
    }

    /**
     * The character data
     */
    private final StringBuilder characterData = new StringBuilder();

    /**
     * Tells whether some character data has been read.
     */
    private boolean hasCharacterData = false;

    /**
     * binary data
     */
    private byte[] binaryData = null;

    /**
     * Tells whether there is either character or binary content available
     * 
     * @return {@code true} if some content is available
     */
    protected boolean hasData(
    ){
        return this.hasCharacterData || binaryData != null;
    }
    
    /**
     * Retrieve the elements character data
     * 
     * @return the elements character data
     */
    protected Object getData() {
        return this.hasCharacterData ? characterData.toString() : this.binaryData;
    }

    @Override
    public void error(
        SAXParseException exception
    ) throws SAXException {
        StringBuilder locationString = new StringBuilder();
        String systemId = exception.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            locationString.append(systemId);
        }
        locationString.append(
            ':'
        ).append(
            exception.getLineNumber()
        ).append(
            ':'
        ).append(
            exception.getColumnNumber()
        );
        throw Throwables.log(
            BasicException.initHolder(
                new SAXException(
                    "XML parse error",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        new BasicException.Parameter("message", exception.getMessage()),
                        new BasicException.Parameter("location", locationString),
                        new BasicException.Parameter("systemId", systemId),
                        new BasicException.Parameter("lineNumber", exception.getLineNumber()),
                        new BasicException.Parameter("columnNumber", exception.getColumnNumber())
                    )
                )
            )
        );
    }

    @Override
    public void characters(
        char[] ch, 
        int start, 
        int length
    ) throws SAXException {
        this.hasCharacterData = true;
        this.characterData.append(ch, start, length);
    }

    @Override
    public void startElement(
        String uri,
        String localName,
        String name,
        Attributes attributes)
    throws SAXException {
        this.hasCharacterData = false;
        this.characterData.setLength(0);
        this.binaryData = null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(byte[], int, int)
     */
//  @Override
    public void writeBinaryData(
        byte[] data, 
        int offset, 
        int length
    ){
        this.binaryData = offset == 0 && length == data.length ? data : ArraysExtension.copyOfRange(data, offset, offset + length);           
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(org.w3c.cci2.BinaryLargeObject)
     */
//  @Override
    public void writeBinaryData(
        BinaryLargeObject data
    ){
        throw new UnsupportedOperationException("Large object streaming not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeCharacterData(org.w3c.cci2.CharacterLargeObject)
     */
//  @Override
    public void writeCharacterData(
        CharacterLargeObject data
    ){
        throw new UnsupportedOperationException("Large object streaming not yet implemented");
    }

}