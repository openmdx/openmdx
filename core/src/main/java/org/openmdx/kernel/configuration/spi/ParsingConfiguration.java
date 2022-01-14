/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Parsing Configuration 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.configuration.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Parsing Configuration
 * <p>
 * This configuration is backed by three different maps<ul>
 * <li>one for multi-valued entries ({@link SparseArray}s)
 * <li>one for map entries (other {@link Map}s)
 * <li>one for single-valued entries (any other type)
 * </ul>
 * String values are parsed to the requested type and the
 * resulting values cached.
 */
class ParsingConfiguration implements Configuration, MultiLineStringRepresentation {

    /**
     * Constructor 
     *
     * @param parser the parser to parse String property values
     * @param rawConfiguration the raw configuration
     */
    ParsingConfiguration(
        RawConfiguration rawConfiguration
    ) {
        this.rawConfiguration = rawConfiguration;
    }

    private final RawConfiguration rawConfiguration;
    
    /**
     * Holds the single valued entries
     */
    private final Map<String, Object> singleValuedEntries = new HashMap<>();
    
    /**
     * Holds the multi-valued entries
     */
    private final Map<String, SparseArray<?>> multiValuedEntries = new HashMap<>();

    /**
     * The mutable map entries are not populated by configurations
     */
    private final Map<String, Map<String,?>> mapEntries = new HashMap<>();
    
    /**
     * To indicate an absent raw value
     */
    private static Object NIL = new Object();
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#getOptionalValue(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getOptionalValue(
        String entryName,
        Class<T> type
    ) {
        Object value = singleValuedEntries.get(entryName);
        if(value == NIL) {
            return Optional.empty();
        } else if(value == null) {
            final Optional<?> rawValue = rawConfiguration.getOptionalValue(entryName, Object.class);
            if(!rawValue.isPresent()) {
                this.singleValuedEntries.put(entryName, NIL);
                return Optional.empty();
            } else if(type == null) {
                return Optional.<T>of((T)rawValue.get());
            } else {
                T t = parse(entryName, type, rawValue.get());
                this.singleValuedEntries.put(entryName, t);
                return Optional.of(t);
            }
        } else {
            return Optional.of(type == null ? (T)value : type.cast(value));
        }
    }

    private <T> T parse(
        String entryName,
        Class<T> type,
        Object value
    ) {
        if(!type.isInstance(value)) {
            final Parser parser = this.rawConfiguration.getParser();
            if(parser.handles(type)) {
                if(value instanceof String) {
                    value = parser.parse(type, (String)value);
                } else {
                    throw new IllegalArgumentException(
                        "Only String values can be parsed",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION,
                            new BasicException.Parameter("key", entryName),
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("type", type.getClass().getName())
                        )
                    );
                }
            } else {
                throw new IllegalArgumentException(
                    "The given type is not supported by the parser",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter("key", entryName),
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("type", type.getName())
                    )
                );
            }
        }
        return type.cast(value);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#getSparseArray(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> SparseArray<T> getSparseArray(
        String entryName,
        Class<T> elementType
    ) {
        SparseArray<T> sparseArray = (SparseArray<T>) multiValuedEntries.get(entryName);
        if(sparseArray == null) {
            SparseArray<Object> rawArray = this.rawConfiguration.getSparseArray(entryName, Object.class);
            if(rawArray.isEmpty()) {
                sparseArray = SortedMaps.emptySparseArray();
                multiValuedEntries.put(entryName, sparseArray);
            } else if(elementType == null) {
                sparseArray = (SparseArray<T>) rawArray;
            } else {
                sparseArray = new TreeSparseArray<>();
                for(Map.Entry<Integer, ?> e : rawArray.entrySet()) {
                    sparseArray.put(
                        e.getKey(), 
                        parse(entryName, elementType, e.getValue())
                    );
                }
                sparseArray = SortedMaps.unmodifiableSparseArray(sparseArray);
                multiValuedEntries.put(entryName, sparseArray);
            }
        }
        return sparseArray;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#getMutableMap(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Map<String, T> getMutableMap(
        String entryName,
        Class<T> elementType
    ) {
        Map<String, T> mutableMap = (Map<String, T>) this.mapEntries.get(entryName);
        if(mutableMap == null) {
            mutableMap = new HashMap<>();
            this.mapEntries.put(entryName, mutableMap);
        }
        return mutableMap;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#singleValuedEntryNames()
     */
    @Override
    public Set<String> singleValuedEntryNames() {
        return rawConfiguration.singleValuedEntryNames();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#multiValuedEntryNames()
     */
    @Override
    public Set<String> multiValuedEntryNames() {
        return rawConfiguration.multiValuedEntryNames();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#mapValuedEntryNames()
     */
    @Override
    public Set<String> mapValuedEntryNames() {
        return Collections.unmodifiableSet(this.mapEntries.keySet());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.rawConfiguration.toString();
    }

 }
