/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Configuration
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.configuration;

import java.util.Set;

import org.w3c.cci2.SparseArray;

/**
 * The configuration interface
 */
public interface Configuration {

	/**
	 * Provide the entry names of the single-valued entries
	 *
	 * @return the single-valued entry entry name set
	 */
	Set<String> singleValuedEntryNames();

	/**
	 * Provide the entry names of the multi-valued entries
	 *
	 * @return the multi-valued entry entry name set
	 */
	Set<String> multiValuedEntryNames();
	
    /**
     * Tells whether a given flag is enabled or not
     * 
     * @param entryName the configuration entry's name
     * @param defaultValue the default in case the value is neither 
     * <code>true</code> nor <code>false</code>
     * 
     * @return the flag value
     */
    boolean isEnabled(
		String entryName,
		boolean defaultValue
	);
    
    /**
     * Retrieve a single-value configuration entry
     * 
     * @param entryName the configuration entry's name
     * @param defaultValue the default in case the entry is absent
     * 
     * @return the entry's value if it is present, the default value otherwise
     */
    <T> T getValue(
    	String entryName,
    	T defaultValue
    );
    
    /**
     * Retrieve a single-value configuration entry
     * @param entryName the configuration entry's name
     * @param type the component type is used to parse or cast the value
     * 
     * @return the entry's value if it is present, <code>null</code> otherwise
     */
    <T> T getOptionalValue(
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
    <T> SparseArray<T> getValues(
    	String entryName,
    	Class<T> elementType
    );
    
}