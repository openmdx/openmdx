/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: WBXMLPlugIns.java,v 1.3 2010/04/16 13:17:37 hburger Exp $
 * Description: WBXML PlugIn Provider
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 13:17:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.wbxml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;

/**
 * XML Output Factory Builder
 */
public class WBXMLPlugIns {

    /**
     * The configuration
     */
    private static final Properties configuration = new Properties();
    
    /**
     * The <em>Plug-In</em> class property
     */
    public static final String PLUG_IN = "http://openmdx.org/wbxml/properties/plug-in";

    /**
     * The lazily fetched classes
     */
    private static final ConcurrentMap<String,Class<? extends PlugIn>> classes = new ConcurrentHashMap<String,Class<? extends PlugIn>>();
    
    /**
     * Create a an XML Output Factory instance
     * 
     * @param mimeType
     * 
     * @return a new XML Output Factory
     */
    public static PlugIn newInstance(
        String publicId
    ){
        Class<? extends PlugIn> plugInClass = classes.get(publicId);
        if(plugInClass == null) {
            String className = configuration.getProperty(publicId);
            if(className == null) {
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                        "No PlugIn class configured for the given PUBLIC id",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("PUBLIC", publicId)
                        )
                   )
               );
            }
            try {
                classes.put(
                    publicId,
                    plugInClass = Classes.getApplicationClass(className)
                );
            } catch (ClassNotFoundException exception) {
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                        "PlugIn class for the given PUBLIC id not found",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("PUBLIC", publicId),
                            new BasicException.Parameter("class", className)
                        )
                   )
               );
            }
        }
        try {
            return plugInClass.newInstance();
        } catch (Exception exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "XMLOutputFactory class for the given MIME type could not be instantiated",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("PUBLIC", publicId),
                        new BasicException.Parameter("class", plugInClass.getName())
                    )
               )
           );
        }
    }

    static {
        ClassLoader classLoader = WBXMLPlugIns.class.getClassLoader();
        try {
            for(
                Enumeration<URL> urls = classLoader.getResources("META-INF/openmdx-wbxml-plugin.properties");
                urls.hasMoreElements();
            ) {
                URL url = urls.nextElement();
                try {
                    InputStream source = url.openStream();       
                    configuration.load(source);
                    source.close();
                } catch (IOException exception) {
                    SysLog.warning("WBXML plug-in configuration failure: " + url, exception);
                }
            }
        } catch (IOException exception) {
            SysLog.error("WBXML plug-in configuration failure", exception);
        }
    }
    
}
