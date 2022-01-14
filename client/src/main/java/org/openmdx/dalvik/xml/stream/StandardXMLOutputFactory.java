/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: Standard XML Output Factory 
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
package org.openmdx.dalvik.xml.stream;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamException;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamWriter;

/**
 * The Standard XML Output Factory creates non-escaping XMLStreamWriters 
 */
public class StandardXMLOutputFactory extends AbstractXMLOutputFactory {

    /**
     * Constructor 
     */
    public StandardXMLOutputFactory(
    ) {
        super("text/xml");
    }

    /**
     * Tells whether white space characters shall be included or not
     * 
     * @return <code>true</code> if white space characters shall be included
     */
    protected boolean isPretty(
    ){
        return getMimeType().startsWith("text/");
    }
    
    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        Writer characterStream
    ) throws XMLStreamException {
        return new StandardXMLStreamWriter(
            null, // encoding
            isPretty(), 
            characterStream
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream, java.lang.String)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        OutputStream byteStream, 
        String encoding
    ) throws XMLStreamException {
        try {
            return new StandardXMLStreamWriter(
                encoding, 
                isPretty(), 
                byteStream
            );
        } catch (UnsupportedEncodingException exception) {
            throw AbstractXMLStreamWriter.toXMLStreamException(exception);
        }
    }

}
