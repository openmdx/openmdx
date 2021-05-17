/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Target 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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

package org.openmdx.base.rest.stream;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.URLEncoder;
import org.openmdx.base.rest.spi.Target;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;

/**
 * Abstract Target
 */
public abstract class RestTarget implements Target {

    /**
     * Constructor
     * 
     * @param uri
     */
    protected RestTarget(String uri) {
        this.urlEncoder = new URLEncoder(uri);
    }

    /**
     * Map a cause to an XMLStreamException
     * 
     * @param exception
     *            the exception to be mapped
     * 
     * @return an XMLStreamException
     */
    protected static XMLStreamException toXMLStreamException(
        Exception exception
    ) {
        BasicException cause = BasicException.toExceptionStack(exception);
        return new XMLStreamException(cause.getMessage(), cause);
    }

    /**
     * The URL encoder
     */
    private final URLEncoder urlEncoder;

    /**
     * The XML output stream
     */
    private XMLStreamWriter writer;

    /**
     * Retrieve the base URI
     * 
     * @return the HREF prefix
     */
    protected String getBase() {
        return this.urlEncoder.getContextURL();
    }

    /**
     * Terminates and flushes the document
     */
    @Override
    public void close(
    ) throws IOException {
        if (this.writer != null) try {
            this.writer.writeEndDocument();
            this.writer.flush();
            this.writer = null;
        } catch (XMLStreamException exception) {
            throw new IOException("Flushing failed", exception);
        }
    }

    /**
     * Discard the writer
     */
    protected void reset(){
        this.writer = null;
    }
    
    /**
     * Create an XML stream writer
     * 
     * @return a new XML stream writer
     * 
     * @throws XMLStreamException
     */
    protected abstract XMLStreamWriter newWriter()
    throws XMLStreamException;

    /**
     * Retrieve an XML Stream Write
     * 
     * @return an XML Stream Writer for an open document
     * 
     * @throws XMLStreamException
     */
    protected XMLStreamWriter getWriter()
    throws XMLStreamException {
        if (this.writer == null) {
            this.writer = newWriter();
            this.writer.writeStartDocument();
        }
        return this.writer;
    }

    /**
     * Retrieve the href URL for a given resource identifier
     * 
     * @param xri
     *            the resource identifier
     * 
     * @return the href URL for the given XRI
     */
    protected String toURL(
        Path xri
    ) {
        return xri != null ? urlEncoder.encode(xri) : getBase() + "/!" + UUIDs.newUUID();
    }

}