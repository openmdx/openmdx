/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: URL Decoder 
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

package org.openmdx.base.naming;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.xri.XRIAuthorities;

/**
 * URL Decoder
 */
public class URLDecoder {

    /**
     * Constructor
     *
     * @param contextURL
     *            the context URL without trailing '/'
     */
    public URLDecoder(String contextURL) {
        this.contextURL = contextURL;
    }

    /**
     * the context URL without trailing '/'
     */
    private final String contextURL;

    /**
     * Extracts the URL encoded XRI by adding the openMDX authority sub-segment if necessary
     * 
     * @param url
     *            the encoded URL
     * 
     * @return the decoded XRI
     * 
     * @throws NullPointerException
     *             if either argument is {@code null}
     * @throws RuntimeServiceException
     *             if the {@code url} does not start with the {@code contextURL}
     */
    public Path decode(String url) {
        if (!url.startsWith(contextURL)) {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The URL does not start with the expected context URL",
                new BasicException.Parameter("contextURL", contextURL),
                new BasicException.Parameter("url", url)
            );
        }
        return new Path(prependFirstSubsegmentIfNecessary(url.substring(contextURL.length())));
    }

    private String prependFirstSubsegmentIfNecessary(String pathInfo) {
        if (pathInfo.isEmpty()) {
            return XRIAuthorities.OPENMDX_AUTHORITY;
        }
        final String tail = pathInfo.substring(1);
        if (tail.startsWith(XRIAuthorities.OPENMDX_AUTHORITY)) {
            return tail;
        }
        final String separator = tail.startsWith("!") ? "" : "*";
        return XRIAuthorities.OPENMDX_AUTHORITY + separator + tail;
    }

    
    /**
     * Retrieve the context URL.
     *
     * @return Returns the context URL
     */
    public String getContextURL() {
        return this.contextURL;
    }

    
}
