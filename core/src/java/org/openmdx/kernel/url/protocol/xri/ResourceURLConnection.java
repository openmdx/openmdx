/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ResourceURLConnection.java,v 1.5 2008/03/13 17:16:15 hburger Exp $
 * Description: Resource URL Connection
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/13 17:16:15 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 * ___________________________________________________________________________ 
 *
 * This class should log as it has to be loaded by the system class loader. 
 */
package org.openmdx.kernel.url.protocol.xri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openmdx.kernel.url.protocol.AbstractURLConnection;

/**
 * Provides access to system resources as an URLConnection.
 *
 * @author Original author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ResourceURLConnection
   extends AbstractURLConnection
{

    /**
     * 
     * @param url
     * @throws IOException
     */
     public ResourceURLConnection(
        final URL url
     ) throws IOException {
        super(url);
    }

    /**
     * 
     * @param url
     * @throws IOException
     */
    protected URL makeDelegateUrl(
        final URL url
    ) throws IOException {     
        String path = url.getPath();
        if(!path.startsWith("/")) throw new MalformedURLException(
            getClass().getName() + " requires an absolute path: " + url
        );
        String name = path.substring(1);
        //
        // first try TCL and then SCL
        //
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL target = contextClassLoader == null ? null : contextClassLoader.getResource(name);
        if (target == null) target = ClassLoader.getSystemClassLoader().getResource(name);
        if (target == null) throw new FileNotFoundException(
            "Could not locate resource: " + name
         );
        return target;
   }
   
}
