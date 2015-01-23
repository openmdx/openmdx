/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Configuration Provider
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmdx.kernel.text.spi.Parser;

/**
 * Standard Configuration Provider
 */
public class PropertiesConfigurationProvider implements ConfigurationProvider {
    
	/**
     * Constructor 
     * 
     * @param parser the primitive type parser to be used
     * @param uri the URI to retrieve the properties
     * 
     * @throws IOException 
     */
    protected PropertiesConfigurationProvider(
    	Parser	parser,
    	char delimiter,	
        Properties properties
    ) throws IOException{
    	this.delimiter = delimiter;
    	this.parser = parser;
        this.properties = properties;
    }

    /**
     * The primitive type parser
     */
    private final Parser parser;
	
    /**
     * The property file prefix
     */
    private final Properties properties;
    
    /**
     * Delimiter to form qualified names
     */
    final char delimiter;
    
    /**
     * Create a ConfigurationProvider which loads the properties eagerly
     * 
     * @param parser the primitive type parser to be used
     * @param uri the URI to load the properties
     * 
     * @return a configuration provider with a snapshot of the properties
     * 
     * @throws IOException
     */
    public static ConfigurationProvider newInstance(
    	Parser	parser,
    	char delimiter,
        String uri
    ) throws IOException{
    	return new PropertiesConfigurationProvider(
    		parser,
    		delimiter,
    		PropertiesProvider.getProperties(uri)
    	);
    }
    
    /* (non-Javadoc)
	 * @see org.openmdx.kernel.configuration.ConfigurationProvider#getConfiguration(java.lang.String)
	 */
	@Override
	public Configuration getConfiguration(String section) {
		return new MapConfiguration(selectEntries(null, section), parser);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.configuration.ConfigurationProvider#getConfiguration(java.lang.String, java.util.Map)
	 */
	@Override
	public Configuration getConfiguration(String section, Map<String, ?> override) {
		final MapConfiguration configuration = new MapConfiguration(override, parser);
		configuration.populate(selectEntries(configuration, section));
		return configuration;
	}

    private Map<String, ?> selectEntries(
    	MapConfiguration exclusion,
    	String section
    ) {
    	Map<String, Object> target = new HashMap<String, Object>();
    	for(Enumeration<?> n = this.properties.propertyNames();n.hasMoreElements();) {
    		final Object key = n.nextElement();
    		if(key instanceof String) {
    			final String propertyName = (String) key;
				final String simpleName = selectSimpleName(propertyName, section);
    			if(simpleName != null) {
    				if(exclusion == null || !exclusion.containsKey(simpleName)) {
    					target.put(simpleName, this.properties.getProperty(propertyName));
    				}
    			}
    		}
    	}
    	return target;
    }
    
    /**
     * Retrieve the simple names belonging to the section
     * 
     * @param qualifiedPropertyName the (dot separated) qualified property name 
     * @param section the (dot separated) qualified section name 
     * 
     * @return return the simple name if the qualified one belongs to the section,
     * <code>null</code> otherwise
     */
    private String selectSimpleName(
    	String qualifiedPropertyName,
    	String section
    ) {
    	final int sectionLength = section.length();
    	if(sectionLength == 0) {
    		return (
				!qualifiedPropertyName.isEmpty() &&
				qualifiedPropertyName.indexOf(delimiter) < 0 
			) ? qualifiedPropertyName : null;
    	} else {
    		final int simpleNameStart = sectionLength + 1;
    		return (
				qualifiedPropertyName.length() > simpleNameStart &&
				qualifiedPropertyName.startsWith(section) &&
				qualifiedPropertyName.charAt(sectionLength) == delimiter &&
				qualifiedPropertyName.indexOf(delimiter, simpleNameStart) < 0 
			) ? qualifiedPropertyName.substring(simpleNameStart) : null;
    	}
    }
    
}
