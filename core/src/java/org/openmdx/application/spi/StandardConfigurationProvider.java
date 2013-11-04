/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Configuration Provider
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

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;

/**
 * Standard Configuration Provider
 */
public class StandardConfigurationProvider implements ConfigurationProvider_1_0 {

    /**
     * Constructor 
     *
     * @param url
     * 
     * @throws ServiceException
     */
    public StandardConfigurationProvider(
        String url
    ) throws ServiceException {
        if(url == null) {
          throw new ServiceException(
              BasicException.Code.DEFAULT_DOMAIN,
              BasicException.Code.INVALID_CONFIGURATION,
              "Configuration URL missing",
              new BasicException.Parameter("url", url)
          );
        } else if(url.startsWith("java:comp/env")) {
            //
            // Use JNDI context and properties
            //
            try {
                this.secondary = Classes.newApplicationInstance(
                    ConfigurationProvider_1_0.class, 
                    "org.openmdx.application.naming.NamingConfigurationProvider",  
                    url, 
                    Boolean.FALSE // strict
                );
            } catch (Exception exception) {
                throw new ServiceException(exception);
            }
            this.primary = "java:comp/env".equals(url) ? new PropertiesConfigurationProvider(
            	Resources.toMetaInfXRI("openmdxcontext.properties"),
                false // strict
            ) : new PropertiesConfigurationProvider(
            		Resources.toMetaInfXRI(url.substring(14).replace('/', '.') + ".properties"),
                false // strict
            );
        } else {
            //
            // Use properties only
            //
            this.secondary = null;
            this.primary = new PropertiesConfigurationProvider(
                url,
                true
            ); 
        }
    }

    /**
     * The property configuration provider overrides the naming configuration provider
     */
    private final PropertiesConfigurationProvider primary;

    /**
     * The naming configuration provider is used mainly in an EJB context
     */
    private final ConfigurationProvider_1_0 secondary;
        
    /* (non-Javadoc)
     * @see org.openmdx.application.cci.ConfigurationProvider_1_0#getConfiguration(java.lang.String[], java.util.Map)
     */
    @Override
    public Configuration getConfiguration(
        String[] section
    ) throws ServiceException {
        if(this.secondary == null) {
            return this.primary.getConfiguration(section);
        } else {
            Configuration configuration = this.secondary.getConfiguration(section);
            if(this.primary != null) {
                this.primary.amendConfiguration(configuration, section);
            }
            return configuration;
        }
    }

}
