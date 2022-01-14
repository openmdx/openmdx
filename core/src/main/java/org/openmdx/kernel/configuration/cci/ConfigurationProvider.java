/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Configuration Provider 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.kernel.configuration.cci;

import java.util.Map;
import java.util.function.Predicate;

/**
 * To access the components or subcomponents configuration
 */
public interface ConfigurationProvider { 

    /**
     * Get a specific configuration
     *
     * @param       section
     *              The qualified name of the section to be parsed, where
     *              {@code ""} represents root section and {code null} is
     *              forbidden
     * 
     * @return      the requested configuration which may be empty but never 
     *              is {@code null}
     *              
     * @throws      NullPointerException if {@code section} is {@code null}             
     */
    Configuration getSection(
        String section
    );

    /**
     * Get a specific configuration
     * 
     * @param       override             
     * 	            overrides the given entries or amends them by new ones 
     * @param       section
     *              The qualified name of the section to be parsed, where
     *              {@code ""} represents root section and {code null} is
     *              forbidden
     * @param       defaults             
     *              Taken into account for missing entries 
     * @return      the requested configuration which may be empty but never 
     *              is {@code null}
     *              
     * @throws      NullPointerException if {@code section} is {@code null}             
     */
    Configuration getSection(
        Map<String,?> override,
        String section, 
        Map<String, ?> defaults
    );

    /**
     * Get the configuration for the requested selection
     * 
     * @param predicate to filter the entries
     * 
     * @return the resulting configuration
     * 
     * @throws      NullPointerException if {@code predicate} is {@code null}             
     */
    Configuration getSelection(
        Predicate<String> predicate 
    );
    
}
