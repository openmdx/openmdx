/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Factory
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
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Classes;
import org.w3c.cci2.SparseArray;

/**
 * The properties provider loads and merges properties
 */
public class PropertiesProvider {
    
    /**
     * Constructor 
     */
    private PropertiesProvider(){
    	// Avoid instantiation
    }

    /**
     * 
     */
    private static final String RESOURCE_XRI_PREFIX = "xri://+resource/";

    /**
     * 
     */
    private static final Pattern INCLUDES = Pattern.compile("^include\\[([0-9]+)\\]?$");

    /**
     * 
     */
    private static final String DEFAULTS = "defaults";

    /**
     * Select the &lt;String,String&gt; entries
     * 
     * @param source a Map
     * 
     * @return corresponding properties
     * 
     * @deprecated without replacement
     */
    @Deprecated
    public static Properties toProperties(
        Map<?,?> source
    ){
        Properties target = new Properties();
        for(Map.Entry<?,?> entry : source.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if(key instanceof String && value instanceof String) {
                target.setProperty((String)key, (String)value);
            }
        }
        return target;
    }
    
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
        final List<URL> urls = uri == null || uri.length() == 0 ? Collections.<URL>emptyList(
        ) : uri.startsWith(RESOURCE_XRI_PREFIX) ? Collections.list(
            Classes.getResources(uri.substring(RESOURCE_XRI_PREFIX.length()))
        ) : Collections.singletonList(
            new URL(uri)
        );
        switch(urls.size()) {
            case 0:
                return new Properties();
            case 1:
                return getProperties(urls.get(0));
            default:
                Properties target = new Properties();
                for(URL url : urls) {
                    include(target, getProperties(url));
                }
                return target;
        }
    }

    /**
     * Include the source into the target
     * 
     * @param target
     * @param source
     * 
     * @throws IOException in case of conflicting values
     */
    private static void include(
        Properties target,
        Properties source
    ) throws IOException{
        for(Map.Entry<?, ?> e : source.entrySet()) {
            Object name = e.getKey();
            if(target.containsKey(name)) {
                Object oldValue = target.get(name);
                Object newValue = e.getValue();
                if(oldValue == null ? newValue != null : !oldValue.equals(newValue)) {
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
            } else {
                target.put(e.getKey(), e.getValue());
            }
        }
    }
    
    /**
     * Retrieve the properties by resolving defaults and include entries. 
     * 
     * @param url
     * 
     * @return the properties retrieved for the given URL
     * 
     * @throws IOException in case of access failure or a configuration conflict
     */
    private static Properties getProperties(
        URL url
    ) throws IOException {
        Properties properties = new Properties();
        try (InputStream source = url.openStream()){
            properties.load(source);
        }
        String defaultsURI = (String) properties.remove(DEFAULTS);
        if(defaultsURI != null) {
            Properties combined = new Properties(getProperties(defaultsURI));
            combined.putAll(properties);
            properties = combined;
        }
        SparseArray<String> includes = new TreeSparseArray<String>();
        for(
            Iterator<Entry<Object, Object>> i  = properties.entrySet().iterator();
            i.hasNext();
        ) {
            Map.Entry<?, ?> e = i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            if(e.getKey() instanceof String && value instanceof String){
                Matcher matcher = INCLUDES.matcher((String)key); 
                if(matcher.matches()) {
                    includes.put(
                        Integer.valueOf(matcher.group(1)), 
                        ((String)value).trim()
                    );
                    i.remove();
                }
            }
        }
        for(
            ListIterator<String> i = includes.populationIterator();
            i.hasNext();
        ){
            include(properties, getProperties(i.next()));
        }
        return properties;
    }

}
