/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Formatter Factory
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
package org.openmdx.base.rest.spi;

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.loading.Classes;

/**
 * REST Formatter Factory
 */
public class RestFormatters {

    /**
     * Constructor
     */
    private RestFormatters() {
        // Avoid instantiation
    }

    /**
     * The eagerly acquired REST formatter
     */
    private static final RestFormatter formatter = Classes.newPlatformInstance(
        "org.openmdx.base.rest.stream.StandardRestFormatter",
        RestFormatter.class
    );
    
    /**
     * Convert the path info into a resource identifier
     * 
     * @param pathInfo
     * 
     * @return the XRI corresponding to the path info
     */
    public static Path toResourceIdentifier(
        String pathInfo
    ){  
        return new Path(
            (pathInfo.startsWith("/@openmdx") ? "" : pathInfo.startsWith("/!") ? "xri://@openmdx" : "xri://@openmdx*") + 
            pathInfo.substring(1)
        );
    }
    
    /**
     * Tells whether the given MIME type requires a binary stream
     * 
     * @param mimeType
     * 
     * @return <code>true</code> if the given MIME type requires a binary stream
     */
    public static boolean isBinary(
        String mimeType
    ){
        return "application/vnd.openmdx.wbxml".equals(mimeType);
    }

    
    /**
     * Retrieve formatter.
     *
     * @return Returns the formatter.
     */
    @SuppressWarnings("unchecked")
    public static <T extends RestFormatter> T getFormatter() {
        return (T) formatter;
    }
    
}
