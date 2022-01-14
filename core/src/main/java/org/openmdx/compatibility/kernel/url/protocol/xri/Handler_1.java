/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: XRI protocol handler
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.compatibility.kernel.url.protocol.xri;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openmdx.kernel.xri.XRIAuthorities;

/**
 * To handle version 1 XRIs.
 */
public abstract class Handler_1
   extends URLStreamHandler
{
    
    /**
     * Constructor 
     */
    protected Handler_1() {
        super();
    }

    /* (non-Javadoc)
     * @see java.net.URLStreamHandler#openConnection(java.net.URL)
     */
    @Override
    protected URLConnection openConnection(
        final URL url
    ) throws IOException {
        Logger.getLogger(
            "org.openmdx.kernel.log.LoggerFactory"
        ).log(
            Level.INFO, 
            "Sys|XRI 1 based URL's are deprecated|{0}", 
            new Object[]{url}
        );
        String path = url.getPath();
        if(path.startsWith(RESOURCE_PREFIX)) {
            return new ResourceURLConnection(url);
        } else if(path.startsWith(ZIP_PREFIX)){
            return new ZipURLConnection(url);
        } else {
            throw newMalformedURLException(url);
        }
    }

    /**
     * Unsupported authority exception
     * 
     * @param url an <code>URL</code> which can't be handled by this handler
     * 
     * @return a <code>MalformedURLException</code> for the given <code>URL</code>.
     */
    abstract protected MalformedURLException newMalformedURLException(
        URL url
    );
    
    
    /**
     *
     */
    private final static String ZIP_PREFIX = XRIAuthorities.ZIP_AUTHORITY + ".(";

    /**
     * 
     */
    private final static String RESOURCE_PREFIX = XRIAuthorities.RESOURCE_AUTHORITY + '/';
    
}
