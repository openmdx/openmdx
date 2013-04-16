/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: Source 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.rest.spi;

import java.io.Closeable;
import java.io.IOException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.xml.sax.InputSource;

/**
 * Source
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
        this.contextURL = ".";
        this.body = body;
        this.wbxml = true;
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
        this.contextURL = contextURL;
        this.body = body;
        this.wbxml = RestParser.isBinary(mimeType);
        this.exhaust = true;
        this.closeable = closeable;
    }
    
    /**
     * The HREF prefix
     */
    protected final String contextURL;

    /**
     * The input source
     */
    private final InputSource body;

    /**
     * The WBXML flag
     */
    private final boolean wbxml;
    
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
    ) throws ServiceException {
        if (hrefURL.startsWith(this.contextURL)) {
            return RestParser.toResourceIdentifier(
                hrefURL.substring(this.contextURL.length())
            );
        } else
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The URL does not start with the expected base URL",
                new BasicException.Parameter("contextURL", this.contextURL),
                new BasicException.Parameter("url", hrefURL)
           );
    }

    /**
     * Set up and retrieve the <code>InputSource</code>
     * 
     * @return the <code>InputSource</code>
     * 
     * @throws ServiceException
     */
    protected InputSource getBody(
    ) throws ServiceException {
        return this.body;
    }

    /**
     * Tells whether XML or WBXML shall be used
     */
    protected boolean isWBXML() {
        return this.wbxml;
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
    
}