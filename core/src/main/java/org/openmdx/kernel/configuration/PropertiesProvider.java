/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Provider
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
package org.openmdx.kernel.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Resources;
import org.w3c.cci2.SparseArray;

/**
 * The properties provider loads and merges properties
 */
public class PropertiesProvider extends Properties {
    
	/**
	 * Constructor
	 */
    private PropertiesProvider(
    ){
    	super();
    }

    /**
     * Includes are applied in the given order
     */
    private static final Pattern INCLUDES = Pattern.compile("^include\\[([0-9]+)\\]?$");

    /**
     * The name of the URI property used to retrieve the default properties
     */
    private static final String DEFAULTS = "defaults";
	
	/**
	 * Implements {@code Serializable}
	 */
	private static final long serialVersionUID = 8070630882717670165L;
	
    /**
     * Retrieve the properties by resolving the URI. All matching resources
     * are taken into account in case of a resource XRI. 
     * 
     * @param uri either a resource XRI or a standard URL.
     * 
     * @return the properties retrieved for the given URI
     * 
     * @throws IOException in case of access failure
     */
    public static Properties getProperties(
        String uri
    ) throws IOException {
    	return mergeFromURI(uri);
    }
    
    /**
     * Creates an instance by resolving defaults and including entries. 
     * 
     * @param url the URL of the properties file
     * 
     * @return the properties retrieved for the given URL
     * 
     * @throws IOException in case of access failure or a configuration conflict
     */
	private static PropertiesProvider loadFromURL(URL url) throws IOException {
		final PropertiesProvider instance = new PropertiesProvider();
        try (InputStream source = url.openStream()){
        	instance.load(source);
        	instance.applyDefaults();
        	instance.applyIncludes();
        }
		return instance;
	}

    /**
     * Creates an instance by resolving all URLs for the given URI 
     * 
     * @param uri the URI of the properties files
     * 
     * @return the properties retrieved for the given URI
     * 
     * @throws IOException in case of access failure or a configuration conflict
     */
	private static PropertiesProvider mergeFromURI(String uri) throws IOException {
		final Iterator<URL> i = Resources.findResolvedURLs(uri).iterator();
		if(i.hasNext()) {
			final PropertiesProvider result = PropertiesProvider.loadFromURL(i.next());
			while(i.hasNext()) {
				result.merge(PropertiesProvider.loadFromURL(i.next()));
			}
	    	return result;
		} else {
			return new PropertiesProvider();
		}
	}
	
	private PropertiesProvider getDefaults() {
		return (PropertiesProvider) this.defaults;
	}
	
	private void setDefaults(PropertiesProvider defaults) {
		this.defaults = defaults;
	}
	
	private boolean hasDefaults() {
		return this.defaults != null;
	}
	
	private PropertiesProvider removeDefaults() {
		PropertiesProvider defaults = getDefaults();
		this.defaults = null;
		return defaults;
	}
	
	private void applyDefaults() throws IOException {
		final String defaultsURI = (String) remove(DEFAULTS);
        if(defaultsURI != null) {
        	setDefaults(mergeFromURI(defaultsURI));
        }
	}
	
	private void applyIncludes() throws IOException{
        final SparseArray<String> includes = new TreeSparseArray<String>();
    	for(String name : stringPropertyNames()) {
            final Matcher matcher = INCLUDES.matcher(name); 
            if(matcher.matches()) {
            	String uri = (String) remove(name);
                includes.put(
                    Integer.valueOf(matcher.group(1)), 
                    uri.trim()
                );
    		}
        }
    	for(ListIterator<String> i = includes.populationIterator(); i.hasNext();) {
    		merge(mergeFromURI(i.next()));
    	}
	}
	
	private void merge(PropertiesProvider that) throws IOException {
		final PropertiesProvider defaults = chainDefaults(this.removeDefaults(), that.removeDefaults());
    	propagateValues(that);
    	setDefaults(defaults);
	}

	private void propagateValues(PropertiesProvider that) throws InvalidPropertiesFormatException {
		for(String name : that.stringPropertyNames()) {
    		final String newValue = that.getProperty(name);
    		final Object oldValue = this.setProperty(name, newValue);
    		if(oldValue != null && !oldValue.equals(newValue)) {
                throw Throwables.initCause(
                	new InvalidPropertiesFormatException("Conflicting property settings"), 
                	null, 
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Conflicting property settings",
                    new BasicException.Parameter("name", name),
                    new BasicException.Parameter("oldValue", oldValue),
                    new BasicException.Parameter("newValue", newValue)
                );
            }
    	}
	}
	
	private static PropertiesProvider chainDefaults(PropertiesProvider primary, PropertiesProvider secondary) {
		final PropertiesProvider chain;
		if(primary == null) {
			chain = secondary;
		} else if (secondary == null){
			chain = primary;
		} else {
			PropertiesProvider current = chain = primary;;
			while(current.hasDefaults()) {
				current = current.getDefaults();
			}
			current.setDefaults(secondary);
		}
        return chain;
	}
		
}
