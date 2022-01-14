/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: HTTP Context 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.application.rest.http;

import java.net.URL;
import java.time.Duration;

import javax.resource.ResourceException;

import org.openmdx.base.exception.ServiceException;


/**
 * HTTP Context
 */
public interface HttpContext {

    /**
     * Retrieve the content type.
     *
     * @return Returns the content type.
     */
    String getContentType();

    /**
     * Create the URL
     * 
     * @param path
     * @param query
     * 
     * @return the message's URL 
     * @throws ServiceException 
     */
    URL newURL(
        String path, 
        String query
    ) throws ResourceException;
    
    /**
     * Retrieve URI
     *
     * @return Returns the URI
     */
    String getConnectionURL();

    /**
     * Retrieve the connection's connect timeout
     * 
     * @return the connection's connect timeout
     */
    Duration getConnectionConnectTimeout();
    
    /**
     * Retrieve the connection's read timeout
     * 
     * @return the connection's read timeout
     */
    Duration getConnectionReadTimeout();
    
    /**
     * Retrieve the MIME type.
     *
     * @return Returns the mimeType.
     */
    String getMimeType();

}
