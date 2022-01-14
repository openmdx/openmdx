/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Configuration
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.configuration.cci;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.w3c.cci2.SparseArray;

/**
 * The configuration interface
 */
public interface Configuration {

    /**
     * Retrieve a single-value configuration entry
     * @param entryName the configuration entry's name
     * @param type the type is used to parse or cast the value
     * 
     * @return the entry's value if it is present, {@link Optional.empty()} otherwise
     */
    <T> Optional<T> getOptionalValue(
    	String entryName,
    	Class<T> type
    );

    /**
     * Retrieve a multi-value configuration entry
     * @param entryName the configuration entry's name
     * @param elementType the element type is used to parse or cast the single values
     * 
     * @return the entry's values if present, an empty sparse array otherwise
     */
    <T> SparseArray<T> getSparseArray(
    	String entryName,
    	Class<T> elementType
    );
    
    /**
     * Retrieve a {@code Map} configuration entry
     * @param entryName the configuration entry's name
     * @param elementType the element type is used to type the map values
     * 
     * @return the map, never {@code null}
     */
    <T> Map<String,T> getMutableMap(
        String entryName,
        Class<T> elementType
    );

    /**
     * Determine single-valued entry names
     * 
     * @return the single-valued entry names
     * 
     * @see {@link #getOptionalValue(String, Class)}
     */
    Set<String> singleValuedEntryNames();

    /**
     * Determine multi-valued entry names
     * 
     * @return the multi-valued entry names
     * 
     * @see {@link #getSparseArray(String, Class)}
     */
    Set<String> multiValuedEntryNames();

    /**
     * Determine map-valued entry names
     * 
     * @return the map-valued entry names
     * 
     * @see {@link #getMutableMap(String, Class)}
     */
    Set<String> mapValuedEntryNames();

}