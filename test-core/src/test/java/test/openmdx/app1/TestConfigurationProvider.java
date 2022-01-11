/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Configuration Provider
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

package test.openmdx.app1;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.w3c.cci2.SparseArray;

/**
 * Test Configuration Provider
 */
public class TestConfigurationProvider {
    
    @Test
    public void readByLegacyProvider(
    ) throws InvalidPropertiesFormatException, IOException, ServiceException {
        // Arrange
        Properties properties = getProperties("test-Main-DataManagerFactory.xml");
        // Act
        final Map<String, Configuration> plugInConfigurations = createPlugInConfigurations(properties);
        final Configuration auditConfiguration = plugInConfigurations.get("xri://@openmdx*test.openmdx.app1/provider/Audit/($...)");
        // Assert
        final Optional<String> configuration = auditConfiguration.getOptionalValue("configuration", String.class);
        Assertions.assertTrue(configuration.isPresent());
        Assertions.assertEquals("xri://+resource/META-INF/app1BeforeImageProvider.properties", configuration.get());
    }

    private Map<String, Configuration> createPlugInConfigurations(
        Properties properties
    ) throws ServiceException {
        final Map<String,Configuration> plugInConfigurations = new HashMap<>();
        ConfigurationProvider configurationProvider = Configurations.getPersistenceManagerFactoryConfigurationProvider(
            Collections.emptyMap(),
            properties,
            Collections.emptyMap()
        );
        final Configuration persistenceManagerConfiguration = configurationProvider.getSection(
            "org.openmdx.jdo.DataManager"
        );
        final SparseArray<String> restPlugIns = persistenceManagerConfiguration.getSparseArray(
            "restPlugIn",
            String.class
        );
        final SparseArray<String> xriPatterns = persistenceManagerConfiguration.getSparseArray(
            "xriPattern",
            String.class
        );
        for (ListIterator<String> i = xriPatterns.populationIterator(); i.hasNext();) {
            int index = i.nextIndex();
            String pattern = i.next();
            String restPlugIn = restPlugIns.get(Integer.valueOf(index));
            if(!plugInConfigurations.containsKey(pattern)) {
                Configuration plugInConfiguration = configurationProvider.getSection(
                    restPlugIn
                );
                plugInConfigurations.put(pattern, plugInConfiguration);
            }
        }
        return plugInConfigurations;
    }

    private Properties getProperties(
        final String uri
    ) throws IOException, InvalidPropertiesFormatException {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream(uri));
        return properties;
    }

}
