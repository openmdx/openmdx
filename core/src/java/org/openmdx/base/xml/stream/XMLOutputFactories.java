/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XML Output Factory Builder
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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
package org.openmdx.base.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;

/**
 * XML Output Factory Builder
 */
public class XMLOutputFactories {

    /**
     * Constructor 
     */
    private XMLOutputFactories() {
        // Avoid instantiation
    }

    /**
     * The MIME type property
     */
    public static final String MIME_TYPE = "org.openmdx.xml.stream.mimeType";
    
    /**
     * The configuration
     */
    private static final Properties configuration = getConfiguration();
    
    /**
     * The lazily fetched classes
     */
    private static final ConcurrentMap<String,Class<? extends XMLOutputFactory>> classes = new ConcurrentHashMap<String,Class<? extends XMLOutputFactory>>();
    
    /**
     * Tells whether the given content type is supported
     * 
     * @return <code>true</code> if given content type is supported
     */
    public static boolean isSupported(
        String mimeType
    ){
        return configuration.containsKey(mimeType);
    }
        
    /**
     * Create a an XML Output Factory instance
     * 
     * @param mimeType
     * 
     * @return a new XML Output Factory
     * 
     * @throws XMLStreamException if no factory can be provided for the given MIME type
     */
    public static XMLOutputFactory newInstance(
        String mimeType
    ) throws BasicException {
        Class<? extends XMLOutputFactory> factoryClass = XMLOutputFactories.classes.get(mimeType);
        if(factoryClass == null) {
            String factoryName = configuration.getProperty(mimeType);
            if(factoryName == null) {
                throw BasicException.newStandAloneExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "No XMLOutputFactory configured for the given MIME type",
                    new BasicException.Parameter("mime-type", mimeType)
               );
            }
            try {
                XMLOutputFactories.classes.put(
                    mimeType,
                    factoryClass = Classes.getApplicationClass(factoryName)
                );
            } catch (ClassNotFoundException exception) {
                throw BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "XMLOutputFactory class for the given MIME type not found",
                    new BasicException.Parameter("mime-type", mimeType),
                    new BasicException.Parameter("class", factoryName)
               );
            }
        }
        try {
            XMLOutputFactory factory = factoryClass.newInstance();
            if(factory.isPropertySupported(MIME_TYPE)) {
                factory.setProperty(MIME_TYPE, mimeType);
            }
            return factory;
        } catch (Exception exception) {
            throw BasicException.newStandAloneExceptionStack(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "XMLOutputFactory class for the given MIME type could not be instantiated",
                new BasicException.Parameter("mime-type", mimeType),
                new BasicException.Parameter("class", factoryClass.getName())
           );
        }
    }

    /**
     * Retrieve the configuration
     */
    private static Properties getConfiguration(
    ) {
        Properties configuration = new Properties();
        for(URL url : Resources.getMetaInfResources(XMLOutputFactories.class.getClassLoader(), "openmdx-xml-outputfactory.properties")) { 
            try (InputStream source = url.openStream()){
                configuration.load(source);
            } catch (IOException exception) {
                SysLog.warning("XML output factory configuration failure: " + url, exception);
            }
        }
        if(configuration.isEmpty()) {
            SysLog.warning("Empty XML output factory configuration");
        }
        return configuration;
    }

}
