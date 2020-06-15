/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ConfigurationProviders 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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

package org.openmdx.kernel.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.PersistenceManagerFactory;

import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.openmdx.kernel.configuration.spi.ConfigurationFactory;
import org.openmdx.kernel.loading.Resources;
import org.w3c.spi.PrimitiveTypeParsers;

/**
 * Configuration Providers
 */
public class Configurations {

    /**
     * Constructor 
     */
    private Configurations() {
        // Avoid instantiation
    }

    /**
     * Provide a data provider configuration with {@code '/'} as
     * key segment separator.
     * 
     * @param properties the properties the configuration is based on.
     * 
     * @return a configuration provider for the given properties
     */
    public static ConfigurationProvider getDataproviderConfigurationProvider(
        Properties properties
    ) {
        return ConfigurationFactory.createConfigurationProvider(
            PrimitiveTypeParsers.getStandardParser(),
            '/',
            properties
            );
    }
    
    /**
     * Provide a {@link PersistenceManagerFactory} configuration with {@code '.'} as
     * key segment separator.
     * 
     * @param properties the properties the configuration is based on.
     * 
     * @return a configuration provider for the given properties
     * 
     * @throws RuntimeException in case of load failures
     */
    public static ConfigurationProvider getPersistenceManagerFactoryConfigurationProvider(
        Map<?,?> overrides,
        Map<?,?> configuration,
        Map<?,?> defaults
    ){
       final Map<?, ?>[] maps = { 
           defaults, 
           configuration, 
           getAmendments(configuration), 
           overrides
       };
       return ConfigurationFactory.createConfigurationProvider(
           PrimitiveTypeParsers.getStandardParser(),
           '.',
           maps
       );
    }

    /**
     * Get the configuration amendments from one or more optional property files 
     * specified by the {@link Constants.PROPERTY_NAME} which are located in the
     * {@code META-INF}  directory
     *  
     * @param configuration the persistence manager factory configuration
     * 
     * @return the amendments from the property files
     * 
     * @throws RuntimeException in case of load failures
     */
    private static Map<?,?> getAmendments(
        Map<?,?> configuration
    ){
        final Optional<String> name = getName(configuration); 
        return name.isPresent() ?
            loadProperties(name.get() + ".properties") :
            Collections.emptyMap();
    }

    /**
     * Load the properties from the {@code META-INF} resources
     * 
     * @param resourceName the resource name relative to the {@code META-INF} directory
     * 
     * @return the properties
     * 
     * @throws RuntimeException in case of load failures
     */
    private static Properties loadProperties(
        final String resourceName
    ){
        final String uri = Resources.toMetaInfXRI(resourceName);
        try {
            return PropertiesProvider.getProperties(uri);
        } catch (IOException exception) {
            throw new RuntimeException(
                "Unable to load properties from " + uri, exception);
        }
    }
    
    /**
     * Determine the JDO persistence manager factory name
     * 
     * @param configuration the configuration
     * 
     * @return the persistence manager factory name
     * 
     * @throws ClassCastException if the key {@link Constants.PROPERTY_NAME} does
     * not belong to a {@link String} value
     */
    private static Optional<String> getName(
        Map<?,?> configuration
    ){
        return configuration instanceof Properties ?
            getName((Properties)configuration) :
            Optional.ofNullable((String)configuration.get(Constants.PROPERTY_NAME));
    }

    /**
     * Determine the JDO persistence manager factory name
     * 
     * @param configuration the configuration
     * 
     * @return the persistence manager factory name
     */
    private static Optional<String> getName(
        Properties configuration
    ){
        return Optional.ofNullable(
            configuration.getProperty(
                Constants.PROPERTY_NAME
            )
        );
    }
    
    /**
     * Provide a {@link PersistenceManagerFactory} configuration with {@code '.'} as
     * key segment separator.
     * 
     * @param properties the properties the configuration is based on.
     * 
     * @return a configuration provider for the given properties
     */
    public static Configuration getConnectionDriverConfiguration(
        Map<?,?> overrides,
        Map<?,?> configuration,
        String connectionDriverSection
    ) {
        return ConfigurationFactory.createConfigurationProvider(
            PrimitiveTypeParsers.getStandardParser(),
            '.',
            configuration, overrides
        ).getSection(
               connectionDriverSection
       );
    }
    
    public static Configuration getBeanConfiguration(
        Map<?,?> properties
    ){
        return ConfigurationFactory.createConfiguration(
            PrimitiveTypeParsers.getExtendedParser(), 
            properties
        );
    }
    
}
