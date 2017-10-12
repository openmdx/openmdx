/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Configuration Provider
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Properties;

import javax.jdo.Constants;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.configuration.PropertiesProvider;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;

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
        String uri,
        boolean strict
        
    ) throws ServiceException {
        this.uri = uri;
        this.strict = strict;
    }

    /**
     * The property file prefix
     */
    private final String uri;

    /**
     * 
     */
    private final boolean strict;
    
    /**
     * Amend the configuration by the properties found at the given URL
     * 
     * @param configuration
     * @param section
     * 
     * @throws ServiceException
     */
    void amendConfiguration(
        Configuration configuration,
        String[] section
    ) throws ServiceException{
        try {
            final Properties properties = PropertiesProvider.getProperties(this.uri);
			amendConfiguration(properties, configuration, "/", section);
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            if(this.strict) throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Could not load properties configuration",
                new BasicException.Parameter("url", this.uri),
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
                    configuration.setValue(
                        p[p.length - 1],
                        propertyValue.startsWith("(java.lang.Boolean)") ? Boolean.valueOf(propertyValue.substring(19)) :
                            propertyValue.startsWith("(java.lang.Integer)") ? Integer.valueOf(propertyValue.substring(19)) :
                            propertyValue.startsWith("(java.lang.Long)") ? Long.valueOf(propertyValue.substring(16)) :
                            propertyValue.startsWith("(java.lang.Short)") ?  Short.valueOf(propertyValue.substring(17)) :
                            propertyValue.startsWith("(java.lang.Byte)") ? Byte.valueOf(propertyValue.substring(16)) :
                            propertyValue.startsWith("(java.lang.String)") ? propertyValue.substring(18) :
                            propertyValue.startsWith("(java.math.BigInteger)") ? new BigInteger(propertyValue.substring(22)) :
                            propertyValue.startsWith("(java.math.BigDecimal)") ? new BigDecimal(propertyValue.substring(22)) :
                            propertyValue,
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
        String[] section
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
                Resources.toMetaInfXRI(name + ".properties"),
                true // strict
            ).getConfiguration(
                section
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

}
