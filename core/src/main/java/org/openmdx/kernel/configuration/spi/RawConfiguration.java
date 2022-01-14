/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Raw Configuration 
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
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.text.spi.Decoder;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;
import org.w3c.spi.PrimitiveTypeParsers;

/**
 * Raw Configuration
 * <p>
 * This configuration is backed by three different maps<ul>
 * <li>one for multi-valued entries ({@link SparseArray}s)
 * <li>one for map entries (other {@link Map}s)
 * <li>one for single-valued entries (any other type)
 * </ul>
 */
class RawConfiguration implements Configuration, MultiLineStringRepresentation {

    /**
     * Constructor 
     *
     * @param typeCastDecoder to decode String property values
     */
    private RawConfiguration(
        Parser parser
    ){
        this.parser = parser;
        this.typeCastDecoder = PrimitiveTypeParsers.getDecoder(parser);
    }

    /**
     * Constructor 
     *
     * @param parser the parser to decode String property values
     * @param propertySets the property sets
     */
    RawConfiguration(
        Parser parser,
        Map<?,?>[] propertySets
    ) {
        this(parser);
        for(Map<?,?> propertySet : propertySets) {
            propagate(propertySet);
        }
    }
    
    /**
     * The primitve type parser
     */
    private final Parser parser;

    /**
     * The type-cast aware decoder
     */
    final Decoder typeCastDecoder;
    
    /**
     * Holds the single valued entries
     */
    private final Map<String, Object> singleValued = new HashMap<>();
    
    /**
     * Holds the multi-valued entries
     */
    private final Map<String, SparseArray<Object>> multiValued = new HashMap<>();

    /**
     * This pattern is used to identify and parse multi-valued keys
     */
    private static final Pattern MULTI_VALUED_KEY_PATTERN = Pattern.compile(
        "([^\\[\\]]+)\\[([0-9]+)\\]"
    );
    
    /**            final Object key = entry.getKey();

     * Retrieve parser.
     *
     * @return Returns the parser.            final Object key = entry.getKey();

     */
    protected Parser getParser() {
        return this.parser;
    }

    /**
     * Build the configuration for the requested selection
     * 
     * @param predicate to filter the entries
     * 
     * @return the resulting configuration
     */
    RawConfiguration getSelection(
        Predicate<String> predicate 
    ){
        final RawConfiguration configuration = new RawConfiguration(parser);
        for(Map.Entry<String,?> e : this.singleValued.entrySet()) {
            final String key = e.getKey();
            if(predicate.test(key)) {
                configuration.singleValued.put(key, e.getValue());
            }
        }
        for(Map.Entry<String,SparseArray<Object>> e : this.multiValued.entrySet()) {
            final String key = e.getKey();
            if(predicate.test(key)) {
                configuration.multiValued.put(key, e.getValue());
            }
        }
        return configuration;
    }
    
    /**
     * Build the configuration for the requested section
     * 
     * @param separator entry name separator
     * @param defaults The default entries
     * @param section the requested section
     * @param override overrides section local entries
     * 
     * @return the resulting configuration
     */
    RawConfiguration getSection(
        char separator,
        Map<String, ?> defaults,
        String section, 
        Map<String,?> override
    ){
        final RawConfiguration configuration = new RawConfiguration(parser);
        configuration.propagate(defaults);
        for(Map.Entry<String,?> e : this.singleValued.entrySet()) {
            entryInSection(
                e.getKey(), 
                section, 
                separator
            ).ifPresent(
                name -> configuration.singleValued.put(name, e.getValue())
            );
        }
        for(Map.Entry<String,SparseArray<Object>> e : this.multiValued.entrySet()) {
            entryInSection(
                e.getKey(), 
                section, 
                separator
            ).ifPresent(
                name -> configuration.multiValued.put(name, e.getValue())
            );
        }
        configuration.propagate(override);
        return configuration;
    }

    /**
     * Determines whether the key belongs to the requested section
     * 
     * @param key the complete entry name
     * @param section the requested section
     * @param separator entry name separator
     * 
     * @return The section local key
     */
    private Optional<String> entryInSection(
        String key, 
        String section, 
        char separator
    ){
        if(section == null) {
            System.out.println("Wait a moment");
        }
        final int sl = section.length();
        if(
            key.length() - sl > 1 && 
            key.startsWith(section) &&
            (sl == 0 || key.charAt(sl) == separator)
        ) {
            final String entry = sl == 0 ? key : key.substring(sl + 1);
            if(entry.indexOf(separator) < 0) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    /**
     * Propagate a single priority level
     * 
     * @param propertySet property set at a given priority level
     */
    private void propagate(
        Map<?, ?> propertySet
    ) {
        if(propertySet instanceof Properties) {
            propagate((Properties)propertySet);
        } else {
            for(Map.Entry<?,?> entry : propertySet.entrySet()) {
                propagate(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Propagate a single priority level.
     * Non-{@link String} entries are ignored. 
     * 
     * @param properties property set at a given priority level
     */
    private void propagate(
        Properties properties
    ) {
        for(String key : properties.stringPropertyNames()) {
            propagate(key, properties.getProperty(key));
        }
    }
    
    /**
     * Propagate a single entry
     * 
     * @param key the entry's key
     * @param value the entry's value
     */
    private void propagate(
        final Object key,
        final Object value
    ) {
        if(key instanceof String) {
            if(value instanceof String) {
                propagate(
                    (String)key, 
                    (String)value
                );
            } else {
                propagate(
                    (String)key, 
                    value
                );
            }
        } else {
            throw new IllegalArgumentException(
                "Keys are required to be Strings",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    new BasicException.Parameter("key", key),
                    new BasicException.Parameter("expected", String.class.getName()),
                    new BasicException.Parameter("actual", key.getClass().getName())
                )
            );
        }
    }

    /**
     * Propagate a single entry
     * 
     * @param key the entry's key
     * @param value the entry's value
     */
    private void propagate(
        final String key,
        final String value
    ) {
        propagate(
            key, 
            typeCastDecoder.decode(value)
        );
    }
    
    /**
     * Propagate a single entry level
     * 
     * @param key the raw key
     * @param value the decoded value 
     */
    @SuppressWarnings("unchecked")
    private void propagate(
        String key,
        Object value
    ) {
        final Matcher matcher = MULTI_VALUED_KEY_PATTERN.matcher(key);
        if(matcher.matches()) {
            final String entryName = matcher.group(1);
            final Integer index = Integer.valueOf(matcher.group(2));
            values(entryName).put(index, value);
        } else if (value instanceof SparseArray) {
            this.multiValued.put(key, (SparseArray<Object>) value);
        } else {
            this.singleValued.put(key, value);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#getMutableMap(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> Map<String, T> getMutableMap(
        String entryName,
        Class<T> elementType
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
	public <T> Optional<T> getOptionalValue(String entryName, Class<T> valueClass) {
        final Object value = this.singleValued.get(entryName);
		return value == null ? Optional.empty() : Optional.of(valueClass.cast(value));
	}

    @Override
    @SuppressWarnings("unchecked")
	public <T> SparseArray<T> getSparseArray(String entryName, Class<T> elementType) {
        final SparseArray<T> values = (SparseArray<T>) this.multiValued.get(entryName);
	    return values == null ? SortedMaps.emptySparseArray() : SortedMaps.unmodifiableSparseArray(values);
	}

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#singleValuedEntryNames()
     */
    @Override
    public Set<String> singleValuedEntryNames() {
        return Collections.unmodifiableSet(this.singleValued.keySet());
    }

    public Set<String> multiValuedEntryNames() {
        return Collections.unmodifiableSet(this.multiValued.keySet());
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.configuration.cci.Configuration#mapValuedEntryNames()
     */
    @Override
    public Set<String> mapValuedEntryNames() {
        return Collections.emptySet();
    }

    private SparseArray<Object> values(String entryName){
        SparseArray<Object> target = this.multiValued.get(entryName);
        if(target == null) {
            target = new TreeSparseArray<>();
            this.multiValued.put(entryName, target);
        }
        return target;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        Map<String, Object> entries = new HashMap<>();
        entries.putAll(singleValued);
        entries.putAll(multiValued);
        return IndentingFormatter.toString(entries);
    }

}
