/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Configuration Builder 
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

package org.openmdx.kernel.configuration.spi;

import java.util.Map;

import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.openmdx.kernel.text.spi.Parser;

/**
 * Configuration Builder
 */
public class ConfigurationFactory {

    /**
     * Constructor 
     */
    ConfigurationFactory() {
        // Avoid instantiation
    }

    /**
     * Creates a configuration able to decode and parse
     * {@code String} property values 
     *
     * @param parser the parser to parse String property values
     * @param rawConfiguration the raw configuration
     */
    public static Configuration createConfiguration(
        Parser parser,
        Map<?,?>... propertySets
    ) {
        return new ParsingConfiguration(
            new RawConfiguration(parser, propertySets)
        );
    }
    
    /**
     * Create a configuration provider
     * 
     * @param Parser the value parser to be used
     * @param separator key segment separator
     * @param maps the sources from highest to lowest priority
     * 
     * @return a configuration provider
     */
    public static ConfigurationProvider createConfigurationProvider(
        Parser parser,
        char separator,
        Map<?, ?>... maps
    ){
        return new FilteringConfigurationProvider(
            separator, 
            new RawConfiguration(parser, maps)
        );
    }

}
