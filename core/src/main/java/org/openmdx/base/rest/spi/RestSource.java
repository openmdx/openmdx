/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Source 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2014, OMEX AG, Switzerland
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

package org.openmdx.base.rest.spi;

import java.io.Closeable;
import java.io.IOException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.URLDecoder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * REST Source
 */
public class RestSource implements Closeable {

    /**
     * Constructor 
     *
     * @param body
     */
    RestSource(
        InputSource body
    ){
        this.urlDecoder = new URLDecoder(".");
        this.body = body;
        this.format = Format.WBXML;
        this.exhaust = false;
        this.closeable = null;
    }
    
    /**
     * Constructor
     * 
     * @param contextURL
     * @param body
     * @param mimeType
     * @param closeable 
     */
    public RestSource(
        String contextURL, 
        InputSource body, 
        String mimeType, 
        Closeable closeable
    ) {
        this.urlDecoder = new URLDecoder(contextURL);
        this.body = body;
        this.format = Format.fromMimeType(mimeType);
        this.exhaust = true;
        this.closeable = closeable;
    }
    
    /**
     * The URL decoder
     */
    protected final URLDecoder urlDecoder;

    /**
     * The input source
     */
    private final InputSource body;

    /**
     * The input format
     */
    private final Format format;
    
    /**
     * The exhaust flag
     */
    private final boolean exhaust;
    
    /**
     * 
     */
    protected final Closeable closeable;
    
    /**
     * Retrieve the resource identifier for a given href URL
     * 
     * @param hrefURL the href URL
     * 
     * @return the XRI for the given href URL
     */
    protected Path getXRI(
        String hrefURL
    ) throws SAXException {
        try {
            return urlDecoder.decode(hrefURL);
        } catch (RuntimeException exception) {
            throw new SAXException(exception);
        }
    }

    /**
     * Set up and retrieve the <code>InputSource</code>
     * 
     * @return the <code>InputSource</code>
     */
    protected InputSource getBody(
    ){
        return this.body;
    }

    /**
     * Provide the input format
     */
    protected Format getFormat() {
        return this.format;
    }

    /**
     * Tells whether the source shall be exhausted
     * 
     * @return <code>true</code> if the source shall be exhausted
     */
    protected boolean isToBeExhausted() {
        return this.exhaust;
    }
    
    /**
     * Close the source 
     * 
     * @throws ServiceException
     */
    public void close(
    ) throws IOException{
        if(this.closeable != null) {
            this.closeable.close();
        }
    }
 
    static enum Format {
    	XML,
    	WBXML,
    	JSON;
    	
    	static Format fromMimeType(String mimeType) {
    		return 
    			RestParser.isBinary(mimeType) ? WBXML :
    			"application/json".equals(mimeType)	? JSON :
    			XML;
    	}
    }

}