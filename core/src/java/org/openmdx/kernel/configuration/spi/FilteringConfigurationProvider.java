/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Filtering Configuration Provider 
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

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.openmdx.kernel.text.MultiLineStringRepresentation;


/**
 * Filtering Configuration Provider
 */
class FilteringConfigurationProvider implements ConfigurationProvider, MultiLineStringRepresentation {

    /**
     * Constructor 
     *
     * @param separator separates the entry name segments
     * @param rawConfiguration the raw configuration
     */
    FilteringConfigurationProvider(
        char separator,
        RawConfiguration rawConfiguration
    ) {
        this.separator = separator;
        this.rawConfiguration = rawConfiguration;
    }

    /**
     * Separates the entry name segments
     */
    private final char separator;
    
    /**
     * The raw configuration
     */
    private final RawConfiguration rawConfiguration;
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.ConfigurationProvider#getConfiguration(java.lang.String)
     */
    @Override
    public Configuration getSection(String section) {
        return getSection(
           Collections.emptyMap(), 
           section, 
           Collections.emptyMap()
       );
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.ConfigurationProvider#getConfiguration(java.util.Map, java.lang.String, java.util.Map)
     */
    @Override
    public Configuration getSection(
        Map<String, ?> override,
        String section, 
        Map<String, ?> defaults
    ) {
       return new ParsingConfiguration(
           this.rawConfiguration.getSection(
               separator, 
               defaults, 
               section, 
               override
           )
       ); 
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.ConfigurationProvider#getSelection(java.util.function.Predicate)
     */
    @Override
    public Configuration getSelection(Predicate<String> predicate) {
        return new ParsingConfiguration(
            this.rawConfiguration.getSelection(predicate)
        ); 
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + this.rawConfiguration;
    }

}
