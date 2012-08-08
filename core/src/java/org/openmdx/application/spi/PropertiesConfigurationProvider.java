/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: PropertiesConfigurationProvider.java,v 1.3 2009/09/18 12:34:31 hburger Exp $
 * Description: Standard Configuration Provider
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/18 12:34:31 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.application.spi;

import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.jdo.Constants;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;

/**
 * Standard Configuration Provider
 */
public class PropertiesConfigurationProvider implements ConfigurationProvider_1_0 {
    
    /**
     * Constructor 
     * 
     * @throws ServiceException 
     * 
     * @throws ServiceException  
     */
    public PropertiesConfigurationProvider(
        String url,
        boolean strict
        
    ) throws ServiceException {
        this.url = url;
        this.strict = strict;
    }

    /**
     * The property file prefix
     */
    private final String url;

    /**
     * 
     */
    private final boolean strict;
    
    /* (non-Javadoc)
     * @see org.openmdx.application.cci.ConfigurationProvider_1_0#getConfiguration(java.lang.String[], java.util.Map)
     */
    void amendConfiguration(
        Configuration configuration,
        String[] section
    ) throws ServiceException{
        try {
            if(this.url.startsWith("xri://+resource/")) {
                for(
                    Enumeration<URL> resources = Classes.getResources(this.url.substring(16));
                    resources.hasMoreElements();
                ) {
                    amendConfiguration(
                        resources.nextElement(),
                        configuration,
                        section
                    );
                }
            } else {
                amendConfiguration(
                    new URL(this.url),
                    configuration,
                    section
                );
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            if(strict) throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Could not load properties configuration",
                new BasicException.Parameter("url", this.url),
                new BasicException.Parameter("section", (Object[])section)
            );
        }
    }

    /**
     * Amend the configuration
     * 
     * @param url
     * @param configuration
     * @param section
     * 
     * @throws ServiceException
     */
    private void amendConfiguration(
        URL url,
        Configuration configuration,
        String[] section
    ) throws ServiceException {
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
            amendConfiguration(properties, configuration, "/", section);
        }  catch(Exception exception) {
            if(this.strict) throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Could not load properties configuration",
                new BasicException.Parameter("url", url),
                new BasicException.Parameter("section", (Object[])section)
            );
        }
    }

    /**
     * Amend the configuration
     * 
     * @param properties
     * @param configuration
     * @param separator
     * @param section
     * 
     * @throws ServiceException
     */
    private static void amendConfiguration(
        Properties properties,
        Configuration configuration,
        String separator,
        String[] section
    ) throws ServiceException {
        Properties: for(
            Enumeration<?> propertyNames = properties.propertyNames();
            propertyNames.hasMoreElements();
        ) {
            Object key = propertyNames.nextElement();
            if(key instanceof String) {
                String qualifiedPropertyName = (String)key;
                String[] p = qualifiedPropertyName.split(separator);                            
                if(p.length == section.length + 1) {
                    for(int i = 0; i < section.length; i++) {
                        if(!p[i].equals(section[i])) {
                            continue Properties;
                        }
                    }
                    String propertyValue = properties.getProperty(qualifiedPropertyName);
                    Object value = null;
                    if(propertyValue.startsWith("(java.lang.Boolean)")) {
                        value = Boolean.valueOf(propertyValue.substring(19));
                    }
                    else if(propertyValue.startsWith("(java.lang.Integer)")) {
                        value = Integer.valueOf(propertyValue.substring(19));
                    }
                    else if(propertyValue.startsWith("(java.lang.Long)")) {
                        value = Long.valueOf(propertyValue.substring(16));
                    }
                    else if(propertyValue.startsWith("(java.lang.Short)")) {
                        value = Short.valueOf(propertyValue.substring(17));
                    }
                    else if(propertyValue.startsWith("(java.lang.Byte)")) {
                        value = Byte.valueOf(propertyValue.substring(16));
                    }
                    else if(propertyValue.startsWith("(java.lang.String)")) {
                        value = propertyValue.substring(18);
                    }
                    else {
                        value = propertyValue;
                    }
                    configuration.setValue(
                        p[p.length - 1],
                        value,
                        true
                    );
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.cci.ConfigurationProvider_1_0#getConfiguration(java.lang.String[], java.util.Map)
     */
    public Configuration getConfiguration(
        String[] section,
        Map<String, ConfigurationSpecifier> specification
    ) throws ServiceException {
        Configuration configuration = new Configuration();
        amendConfiguration(
            configuration,
            section
        );
        return configuration;
    }

    /**
     * Handle dot separated property names
     * 
     * @param source
     * @param section
     * 
     * @return the requested configuration
     * 
     * @throws ServiceException
     */
    public static Configuration getConfiguration(
        Properties source,
        String... section
    ) throws ServiceException {
        if(section == null) {
            return new Configuration();
        } else {
            String name = source.getProperty(Constants.PROPERTY_NAME);
            Configuration target = name == null ? new Configuration(
            ) : new PropertiesConfigurationProvider(
                "xri://+resource/META-INF/" + name + ".properties",
                true // strict
            ).getConfiguration(
                section, 
                null
            );
            amendConfiguration(
                source,
                target,
                "\\.",
                section
            );
            return target;
        }
    }

    /**
     * Select the &lt;String,String&gt; entries
     * 
     * @param source a Map
     * 
     * @return corresponding properties
     */
    public static Properties toProperties(
        Map<?,?> source
    ){
        Properties target = new Properties();
        for(Map.Entry<?,?> entry : source.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if(key instanceof String && value instanceof String) {
                target.setProperty((String)key, (String)value);
            }
        }
        return target;
        
    }
    
}
