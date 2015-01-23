/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Map Backed Configuration 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.configuration;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Map Based Configuration
 * <p>
 * Note that single- and multi-valued attributes use two distinct namespaces:<ul>
 * <li>source entry names without square brackets are used to populate the single- 
 * valued entries (e.g. "entry")
 * <li>source entry names with square brackets are used to populate the multi- 
 * valued entries (e.g. "entry[5]")
 * </ul>
 * <p>
 * The rules for value parsing are as following<ol>
 * <li>Cast values are parsed upon population (e.g. "(java.lang.Integer)5")
 * <li>String values are parsed upon retrieval
 * <li>Other values are returned as they are
 * </ol>
 */
public class MapConfiguration implements Configuration {

	/**
	 * Constructor
	 */
	protected MapConfiguration(
		Parser parser
	){
		this.parser = parser;
		this.singleValued = new HashMap<String, Object>();
		this.multiValued = new HashMap<String, SparseArray<Object>>();
	}
			
	/**
	 * Constructor
	 * 
	 * @param source the configuration is populated from the source
	 * but the source object itself is not kept by the configuration.
	 * Later changes to the source will therefore not affect the
	 * configuration.
	 * 
	 * @param parser the parser is used to parse string values (lazily)
	 * upon value retrieval, while other values are returned as they are.
	 */
	public MapConfiguration(
		Map<String,?> source,
		Parser parser
	) {
		this(parser);
		populate(source);
	}

	/**
	 * Constructor
	 * 
	 * @param source the configuration is populated from the source
	 * but the source object itself is not kept by the configuration.
	 * Later changes to the source will therefore not affect the
	 * configuration.
	 * 
	 * @param parser the parser is used to parse string values (lazily)
	 * upon value retrieval, while other values are returned as they are.
	 */
	public MapConfiguration(
		Properties source,
		Parser parser
	) {
		this(parser);
		populate(source);
	}
	
	protected final Map<String,Object> singleValued;
	protected final Map<String,SparseArray<Object>> multiValued;
	private final Parser parser;

	private static final Pattern MULTI_VALUED_ENTRY_NAME = Pattern.compile(
		"([A-Za-z0-9_]+)\\[([0-9]+)\\]"	
	);
	
	/* (non-Javadoc)
	 * @see org.openmdx.kernel.configuration.Configuration#singleValuedEntryNames()
	 */
	@Override
	public Set<String> singleValuedEntryNames() {
		return Collections.unmodifiableSet(this.singleValued.keySet());
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.configuration.Configuration#multiValuedEntryNames()
	 */
	@Override
	public Set<String> multiValuedEntryNames() {
		return Collections.unmodifiableSet(this.multiValued.keySet());
	}

	@Override
	public boolean isEnabled(String entryName, boolean defaultValue) {
		return getValue(entryName, Boolean.valueOf(defaultValue)).booleanValue();
	}

	@Override
	public <T> T getValue(String entryName, T defaultValue) {
		if(defaultValue == null) {
			throw new IllegalArgumentException(
				"Specify the expected class if the default value shall be null",
				BasicException.newEmbeddedExceptionStack(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.BAD_PARAMETER,
					new BasicException.Parameter("name", entryName),
					new BasicException.Parameter("defaultValue", defaultValue)
				)
			);
		}
		return parse(getClass(defaultValue), this.singleValued.get(entryName), defaultValue);
	}

	@Override
	public <T> T getOptionalValue(String entryName, Class<T> valueClass) {
		return parse(valueClass, this.singleValued.get(entryName), null);
	}

	@Override
	public <T> SparseArray<T> getValues(String entryName, Class<T> elementType) {
		if(this.multiValued.containsKey(entryName)) {
			return SortedMaps.asSparseArray(
				new MarshallingSparseArray<T>(
					elementType, 
					this.multiValued.get(entryName)
				)
			);
		} else {
			return SortedMaps.<T>emptySparseArray();
		}
	}
	
	protected <T> T parse(Class<T> type, final Object value, T defaultValue) {
		return
			value == null ? defaultValue : 
			parser != null && value instanceof String ? parser.parse(type, (String)value) : 
			cast(type, value);
	}

	/**
	 * Retrieve the value's class
	 * 
	 * @param defaultValue the (non-null) value
	 * 
	 * @return the type of the value
	 */
	@SuppressWarnings("unchecked")
	private <T> Class<T> getClass(T defaultValue) {
		return (Class<T>) defaultValue.getClass();
	}

	@SuppressWarnings("unchecked")
	private <T> T cast(Class<T> type, final Object rawValue) {
		return type != null ? type.cast(
			rawValue
		) : (T)rawValue;
	}

	protected boolean containsKey(String key) {
		final Matcher matcher = MULTI_VALUED_ENTRY_NAME.matcher(key);
		if(matcher.matches()) {
			return this.multiValued.containsKey(matcher.group(1));
		} else {
			return this.singleValued.containsKey(key);
		}
	}
	
	protected void populate(
		Map<?,?> source
	){
		final Parser parser = new CastAwareParser(this.parser);
		for(Map.Entry<?, ?> e : source.entrySet()){
			final Object key = e.getKey();
			final Object value = e.getValue();
			if(key instanceof String){
				final Object entryValue;
				if(value instanceof String){
					entryValue = parser.parse((Class<?>)null, (String)value);
				} else {
					entryValue = value;
				}
				final String entryName;
				final Matcher matcher = MULTI_VALUED_ENTRY_NAME.matcher((String) key);
				if(matcher.matches()) {
					entryName = matcher.group(1);
					final Integer entryIndex = Integer.valueOf(matcher.group(2));
					final SparseArray<Object> values = values(entryName);
					values.put(entryIndex, entryValue);
				} else {
					entryName = (String)key;
					this.singleValued.put(entryName, entryValue);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> SparseArray<T> values(String entryName) {
		SparseArray<Object> values = this.multiValued.get(entryName);
		if(values == null) {
			values = new TreeSparseArray<Object>();
			this.multiValued.put(entryName, values);
		}
		return (SparseArray<T>) values;
	}

		
	/**
	 * Class MarshallingSparseArray
	 */
	private class MarshallingSparseArray<T> extends AbstractMap<Integer,T> implements SortedMap<Integer,T> {

		/**
		 * Constructor
		 */
		MarshallingSparseArray(Class<T> elementType, SortedMap<Integer, ?> delegate) {
			this.rawMap = delegate;
			this.elementType = elementType;
		}

		final Class<T> elementType;
		final SortedMap<Integer,?> rawMap;

		T getParsedValue(Integer key) {
			return parse(elementType, rawMap.get(key), null);
		}
		
		@Override
		public Comparator<? super Integer> comparator() {
			return rawMap.comparator();
		}

		@Override
		public SortedMap<Integer, T> subMap(Integer fromKey, Integer toKey) {
			return new MarshallingSparseArray<T>(elementType, rawMap.subMap(fromKey, toKey));
		}

		@Override
		public SortedMap<Integer, T> headMap(Integer toKey) {
			return new MarshallingSparseArray<T>(elementType, rawMap.headMap(toKey));
		}

		@Override
		public SortedMap<Integer, T> tailMap(Integer fromKey) {
			return new MarshallingSparseArray<T>(elementType, rawMap.tailMap(fromKey));
		}

		@Override
		public Integer firstKey() {
			return rawMap.firstKey();
		}

		@Override
		public Integer lastKey() {
			return rawMap.lastKey();
		}

		@Override
		public Set<Map.Entry<Integer, T>> entrySet() {
			final Set<Integer> keys = rawMap.keySet();
			return new AbstractSet<Map.Entry<Integer, T>>(){
				
				@Override
				public Iterator<Map.Entry<Integer, T>> iterator() {
					final Iterator<Integer> delegate = keys.iterator();
					
					return new Iterator<Map.Entry<Integer, T>>(){

						@Override
						public boolean hasNext() {
							return delegate.hasNext();
						}

						@Override
						public Map.Entry<Integer, T> next() {
							final Integer key = delegate.next();
							return new Map.Entry<Integer, T>(){
								
								T value;
								
								@Override
								public Integer getKey() {
									return key;
								}

								@Override
								public T getValue() {
									if(value == null) {
										value = getParsedValue(key);
									}
									return value;
								}

								@Override
								public T setValue(T value) {
									throw new UnsupportedOperationException(
										"A configuration is unmodifiable"
									);
								}
								
							};
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException(
								"A configuration is unmodifiable"
							);
						}
						
					};
				}

				@Override
				public int size() {
					return keys.size();
				}
				
			};
		}
		
	}
	
}
