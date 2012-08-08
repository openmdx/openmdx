/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Handler.java,v 1.7 2006/02/19 21:49:02 wfro Exp $
 * Description: XRI Protocol Handler
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/02/19 21:49:02 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.url.protocol.xri;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.openmdx.compatibility.kernel.url.protocol.xri.Handler_1;
import org.openmdx.kernel.url.protocol.XriProtocols;

/**
 * A protocol handler for the 'xri' protocol. 
 */
public class Handler
   extends Handler_1 // URLStreamHandler
{
	public URLConnection openConnection(
   		final URL url
	) throws IOException {
   		String authority = url.getAuthority();
        if(authority == null) {
            return super.openConnection(url);
        } else if(XriProtocols.RESOURCE_AUTHORITY.equals(authority)) {
	   		return new ResourceURLConnection(url);
   		} else if(authority.startsWith(ZIP_PREFIX)){
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
    protected MalformedURLException newMalformedURLException(
        URL url
    ){
        return new MalformedURLException(
            getClass().getName() + 
            " supports only XRI authorities starting with " + 
            XriProtocols.RESOURCE_AUTHORITY + " or " + 
            XriProtocols.ZIP_AUTHORITY + ": " + url
        );
    }
    
    /**
	 *
	 */
	private final static String ZIP_PREFIX = XriProtocols.ZIP_AUTHORITY + "*(";
    
}
