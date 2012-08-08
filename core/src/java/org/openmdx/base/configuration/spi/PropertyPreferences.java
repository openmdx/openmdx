/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PropertyPreferences.java,v 1.6 2008/03/21 18:29:18 hburger Exp $
 * Description: Property Preferences 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:29:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.base.configuration.spi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.openmdx.compatibility.base.naming.PathComponent;

/**
 * Property Preferences
 */
@SuppressWarnings("unchecked")
class PropertyPreferences
    extends AbstractPreferences
{

    /**
     * System Properties based Peferences
     * 
     * @param segment the XRI segment
     * @param userNode 
     */
    PropertyPreferences(
        PathComponent segment, 
        boolean userNode        
    ) {
        super(null, "");
        this.delegation = new Delegation(segment, userNode);
        this.prefix = "";
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param name
     */
    private PropertyPreferences(
        PropertyPreferences parent,
        String name
    ) {
        super(parent, name);
        this.delegation = parent.delegation;
        this.prefix = this.absolutePath().substring(1).replace('/', '.') + '.';
    }

    /**
     * Persistent values
     */
    private final Delegation delegation;

    /**
     * 
     */
    private final String prefix;        

    /**
     * Tells whether a given propertyName denotes a key for this node.
     * 
     * @param propertyName
     * 
     * @return <code>true</code> if the propertyName denotes a key for this node.
     */
    private boolean isKey(
        String propertyName
    ){
        return 
            propertyName.startsWith(this.prefix) &&
            propertyName.lastIndexOf('.') + 1 == prefix.length();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#isUserNode()
     */
    public boolean isUserNode(
    ) {
        return this.delegation.isUserNode();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#flush()
     */
    public void flush(
    ) throws BackingStoreException {
        this.delegation.getProperties().save();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#flushSpi()
     */
    protected void flushSpi(
    ) throws BackingStoreException {
        //
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeNodeSpi()
     */
    protected void removeNodeSpi(
    ) throws BackingStoreException {    
        AbstractProperties properties = this.delegation.getProperties();
        for(
            Enumeration e = properties.getPropertyNames();
            e.hasMoreElements();
        ) {
            String propertyName = (String) e.nextElement();
            if(isKey(propertyName)) properties.removeProperty(propertyName);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#sync()
     */
    public void sync(
    ) throws BackingStoreException {
        flush();
        this.delegation.getProperties().load();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#syncSpi()
     */
    protected void syncSpi(
    ) throws BackingStoreException {
        //
    }
    
    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#childrenNamesSpi()
     */
    protected String[] childrenNamesSpi(
    ) throws BackingStoreException {
        Set keys = new HashSet();
        for(
            Enumeration e = this.delegation.getProperties().getPropertyNames();
            e.hasMoreElements();
        ) {
            String propertyName = (String) e.nextElement();
            if(propertyName.startsWith(this.prefix)) {
                int i = this.prefix.length();
                int j = propertyName.indexOf('.', i + 1);
                if(j > 0) keys.add(propertyName.substring(i, j));
            }
        }
        return (String[]) keys.toArray(new String[keys.size()]);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#keysSpi()
     */
    protected String[] keysSpi(
    ) throws BackingStoreException {
        Set keys = new HashSet();
        for(
            Enumeration e = this.delegation.getProperties().getPropertyNames();
            e.hasMoreElements();
        ) {
            String propertyName = (String) e.nextElement();            
            if(isKey(propertyName)) keys.add(
                propertyName.substring(this.prefix.length())
            );
        }
        return (String[]) keys.toArray(new String[keys.size()]);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeSpi(java.lang.String)
     */
    protected void removeSpi(String key) {
        this.delegation.getProperties().removeProperty(prefix + key);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#getSpi(java.lang.String)
     */
    protected String getSpi(String key) {
        return this.delegation.getProperties().getProperty(prefix + key);
    }


    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#putSpi(java.lang.String, java.lang.String)
     */
    protected void putSpi(
        String key,
        String value
    ) {
        this.delegation.getProperties().setProperty(prefix + key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#childSpi(java.lang.String)
     */
    protected AbstractPreferences childSpi(
        String name
    ) {
        return new PropertyPreferences(this, name);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#toString()
     */
    public String toString(
    ) {
        return this.delegation.getProperties().getId() + ": " + absolutePath();
    }

    
    //------------------------------------------------------------------------
    // Classes
    //------------------------------------------------------------------------

    /**
     * Delegation
     */
    static class Delegation  {
        
        /**
         * Constructor 
         *
         * @param segment
         * @param userNode
         */
        Delegation(
            final PathComponent segment, 
            final boolean userNode
        ) {
            this.segment = segment;
            this.userNode = userNode;
            this.properties = new WeakHashMap();
        }

        /**
         * the XRI segment
         */
        private final PathComponent segment;
        
        /**
         * The <code>Poperties</code> the <code>Preferences</code> are based on.
         */
        private final Map properties;

        /**
         * Tells whether it's a system or a user node
         */
        private final boolean userNode;

        /**
         * Get the <code>ClassLoader</code> dependent <code>Properties</code> 
         * object.
         * 
         * @return the <code>ClassLoader</code> dependent <code>Properties</code>
         * object 
         */
        synchronized AbstractProperties getProperties(){
            ClassLoader context = this.userNode ? Thread.currentThread().getContextClassLoader() : null; 
            AbstractProperties properties = (AbstractProperties) this.properties.get(context);
            if(properties == null) {
                String[] subSegments = this.segment.getSuffix(0);
                for(
                    int i = this.segment.size() - 1;
                    i >= 0;
                    i--
                ) {
                    String subSegment = subSegments[i];
                    if("System".equals(subSegment)) {
                        properties = new SystemProperties(properties);
                    } else if (subSegment.startsWith("(") && subSegment.endsWith(")")) {
                        try {
                            properties = new StreamProperties(
                                properties, 
                                new URL(subSegment.substring(1, subSegment.length() - 1))
                            );
                        } catch (MalformedURLException exception) {
                            throw (IllegalArgumentException) new IllegalArgumentException(
                                "Subsegment " + i + " is not a valid URL cross reference: " + subSegment
                            ).initCause(
                                exception
                            );
                        }
                    } else throw new IllegalArgumentException(
                        "Subsegment " + i + " is neither 'System' nor an URL cross reference: " + subSegment
                    );
                }
                this.properties.put(context, properties);
            }
            return properties;
        }

        /**
         * Retrieve userNode.
         *
         * @return Returns the userNode.
         */
        public boolean isUserNode() {
            return this.userNode;
        }        
        
    }
        
    /**
     * Abstract Properties
     */
    static abstract class AbstractProperties extends Properties {

        /**
         * Constructor 
         *
         * @param defaults
         */
        protected AbstractProperties(AbstractProperties defaults) {            
            super(defaults);
        }

        /**
         * Lazy loading
         */
        private boolean loaded = false;
        
        /**
         * Retrieve loaded.
         *
         * @return Returns the loaded.
         */
        protected boolean isLoaded() {
            return this.loaded;
        }
        
        /**
         * Load the properties
         * 
         * @throws BackingStoreException in case of failure
         */
        protected void load(
        ) throws BackingStoreException {
            if(!this.loaded) {
                if(super.defaults instanceof AbstractProperties) {
                    ((AbstractProperties)super.defaults).load();
                }
                loadSpi();
                this.loaded = true;
            }
        }
            
        /**
         * Load the properties SPI is invoked not more than once.
         * 
         * @throws BackingStoreException in case of failure
         */
        protected abstract void loadSpi(
        ) throws BackingStoreException;

        /**
         * Save the properties
         * 
         * @param key the key of the property to be saved, 
         * or <code>null</code> to save all properties
         * 
         * @throws BackingStoreException in case of failure
         */
        public void save(
        ) throws BackingStoreException {
            saveSpi();
        }

        /**
         * Save the properties
         * 
         * @param key the key of the property to be saved, 
         * or <code>null</code> to save all properties
         * 
         * @throws BackingStoreException in case of failure
         */
        public abstract void saveSpi(
        ) throws BackingStoreException;

        /* (non-Javadoc)
         * @see java.util.Properties#propertyNames()
         */
        public Enumeration getPropertyNames(
        ) throws BackingStoreException {
            load();
            return propertyNames();
        }
        
        /**
         * Retrieve the <code>Properties</code>' id
         * 
         * @return
         */
        public abstract String getId();

        /* (non-Javadoc)
         * @see java.util.Hashtable#toString()
         */
        public synchronized String toString() {
            return getId() + ": " + super.toString();
        }
        
        /**
         * Remove a property
         * 
         * @param key the property's key
         * 
         * @return the property's previous value
         */
        public Object removeProperty(String key) {
            return containsKey(key) ?
                removeSpi(key) :
            super.defaults instanceof AbstractProperties ?
                ((AbstractProperties)super.defaults).removeProperty(key) :
                null;
        }

        /**
         * Remove a property
         * 
         * @param key the property's key
         * 
         * @return the property's previous value
         */
        public Object removeSpi(
            String key
        ){
            return this.remove(key);
        }
        
    }


    //------------------------------------------------------------------------
    // Class SystemProperties
    //------------------------------------------------------------------------

    /**
     * System Properties
     * <p>
     * System property modifications are <em>passed through</em> immediately.
     */
    static class SystemProperties
        extends AbstractProperties
    {

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -7084855074112216857L;

        /**
         * Constructor 
         *
         * @param defaults
         */
        protected SystemProperties(
            AbstractProperties defaults
        ) {
            super(defaults);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.spi.PropertyPreferences.AbstractProperties#loadSpi()
         */
        public void loadSpi(
        ) throws BackingStoreException {            
            try {
                Properties systemProperties = System.getProperties();
                for(
                    Enumeration e = systemProperties.propertyNames();
                    e.hasMoreElements();
                ){
                    String k = (String) e.nextElement();
                    if(!keySet().contains(k)) {
                        setProperty(k, systemProperties.getProperty(k));
                    }
                }
            } catch (SecurityException exception) {
                throw new BackingStoreException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.spi.PropertyPreferences.AbstractProperties#removeSpi(java.lang.String)
         */
        public Object removeSpi(String key) {
            return setProperty(key, null);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.spi.PropertyPreferences.AbstractProperties#getId()
         */
        public String getId() {
            return "System Properties";
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.spi.PropertyPreferences.AbstractProperties#saveSpi()
         */
        public synchronized void saveSpi(
        ) throws BackingStoreException {
            try {
                Properties systemProperties = System.getProperties();
                for(
                    Enumeration e = keys();
                    e.hasMoreElements();
                ){
                    String key = (String) e.nextElement();
                    String newValue = this.getProperty(key);
                    if(newValue == null) {
                        systemProperties.remove(key);                 
                    } else {
                        systemProperties.setProperty(key, newValue);
                    }
                }
                System.setProperties(systemProperties);
            } catch (SecurityException exception) {
                throw new BackingStoreException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Properties#getProperty(java.lang.String)
         */
        public String getProperty(String key) {
            String value = super.getProperty(key);
            if(
                value == null &&
                ! isLoaded() &&
                ! this.containsKey(key)
            ) {
                value = System.getProperty(key);
                super.setProperty(key, value);
            }
            return value;                
        }
        
    }


    //------------------------------------------------------------------------
    // Class Stream Properties
    //------------------------------------------------------------------------

    /**
     * Stream Properties
     */
    static class StreamProperties
        extends AbstractProperties
    {

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -2292124978877656188L;

        /**
         * Constructor 
         *
         * @param segment the properties' source
         * @throws IOException 
         */
        StreamProperties(
            AbstractProperties defaults,
            URL url
        ){
            super(defaults);
            this.url = url;
        }

        /**
         * The properties' URL
         */
        private final URL url;

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.PropertyPreferences.LoadableProperties#load()
         */
        public void loadSpi(
        ) throws BackingStoreException {
            try {
                load(this.url.openStream());
            } catch (IOException exception) {
                throw new BackingStoreException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.PropertyPreferences.AbstractProperties#saveSpi()
         */
        public void saveSpi(
        ) throws BackingStoreException {
            try {
                URLConnection connection = this.url.openConnection();
                connection.setDoOutput(true);
                store(
                    connection.getOutputStream(),
                    "Property preferences segment " + this.url
                );
            } catch (IOException exception) {
                throw new BackingStoreException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Properties#getProperty(java.lang.String)
         */
        public String getProperty(String key) {
            try {
                load();
            } catch (BackingStoreException exception) {
                // Ignore exception
            }
            return super.getProperty(key);
        }

        /* (non-Javadoc)
         * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
         */
        public synchronized Object setProperty(String key, String value) {
            try {
                load();
            } catch (BackingStoreException exception) {
                // Ignore exception
            }
            return super.setProperty(key, value);
        }


        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.spi.PropertyPreferences.AbstractProperties#removeProperty(java.lang.String)
         */
        public Object removeProperty(String key) {
            try {
                load();
            } catch (BackingStoreException exception) {
                // Ignore exception
            }
            return super.removeProperty(key);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.configuration.spi.PropertyPreferences.AbstractProperties#getId()
         */
        public String getId() {
            return "Stream properties: " + getURL();
        }
        
        /**
         * Retrieve the <code>Properties</code>' <code>URL</code>.
         * 
         * @return the <code>Properties</code>' <code>URL</code>
         */
        private URL getURL(){
            try {
                return this.url.openConnection().getURL();
            } catch (IOException exception) {
                return this.url;
            }
        }

    }

}
