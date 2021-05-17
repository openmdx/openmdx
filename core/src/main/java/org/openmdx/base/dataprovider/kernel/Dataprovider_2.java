/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Embedded Data Provider
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.kernel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAllocationException;

import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.PropertiesProvider;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.loading.PlugInFactory;
import org.w3c.cci2.SparseArray;

/**
 * Embedded Dataprovider
 */
public class Dataprovider_2 implements Port<RestConnection> {

	/**
	 * Constructor
	 */
	public Dataprovider_2(
	){
		this(null);
	}

	/**
	 * Constructor
	 */
	public Dataprovider_2(
		String configuration
	){
		this(configuration, Collections.<String,Map<String,?>>emptyMap());
	}

	/**
	 * Constructor
	 */
	public Dataprovider_2(
		String configuration,
		Map<String, Map<String, ?>> layerPlugInConfigurationOverride
	){
		this.configuration = configuration;
		this.layerPlugInConfigurationOverride = layerPlugInConfigurationOverride;
	}
	
    /**
     * The configuration URL
     */
    private String configuration;

    /**
     * The shareable data provider
     */
    private Port<RestConnection> delegate;
    
    /**
     * The data source URL
     */
    private String datasourceName;

    /**
     * Maps the layer name to its configuration override
     */
    private final Map<String, Map<String, ?>> layerPlugInConfigurationOverride;
    
    /**
     * The connections' factory
     * 
     * @deprecated will not be supported by the dataprovider 2 stack
     */
     @Deprecated
    private ConnectionFactory connectionFactory;
     
    /**
     * Retrieve the data source name.
     *
     * @return Returns the data source name.
     */
    public String getDatasourceName() {
        return this.datasourceName;
    }

    /**
     * Set the data source name.
     * 
     * @param datasourceName The data source name to set.
     */
    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

	 /**
	  * Set the configuration URL
	  * 
	  * @param configuration The configuration URL to set.
	  */
	 public void setConfiguration(String configuration) {
		 this.configuration = configuration;
	 }
	 
	 /**
	  * Retrieve the configuration URL
	  *
	  * @return Returns the configuration URL
	  */
	 public String getConfiguration() {
		 return this.configuration;
	 }
	 
	 /**
	  * The delegate is built lazily
	  * 
	  * @return the delegate
	  * 
	 *  @throws ResourceException 
	  */
     protected Port<RestConnection> buildDelegate(
     ) throws ResourceException {
    	 final ConfigurationProvider configurationProvider = getConfigurationProvider();
    	 final Configuration dataproviderConfiguration = configurationProvider.getSection("");
    	 final SparseArray<String> layerPlugInNames = dataproviderConfiguration.getSparseArray("layerPlugIn", String.class);
    	 Port<RestConnection> delegate = null;
    	 for(String layerPlugInName : layerPlugInNames) {
    		 final Configuration layerPlugInConfiguration = configurationProvider.getSection(
    			getLayerPlugInConfigurationOverride(layerPlugInName, delegate), 
    			layerPlugInName, 
    			Collections.emptyMap()
    		 );
    		 delegate = buildLayerPlugIn(layerPlugInConfiguration);
    	 }
    	 return delegate;
     }

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     /**
      * Build the layer plug-in according to the configuration
      * 
      * @param plugInConfiguration the layer plug-in configuration
      * 
      * @return the layer plug-in
      * 
      * @throws ResourceException
      */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Port<RestConnection> buildLayerPlugIn(
		final Configuration plugInConfiguration
	) throws ResourceException{
		try {
			final Factory<Port> plugInFactory = PlugInFactory.newInstance(Port.class, plugInConfiguration);
			return plugInFactory.instantiate();
		} catch (RuntimeException exception) {
			throw ResourceExceptions.initHolder(
				new ResourceAllocationException(
					"Unable to initialize the embedded data provider",	
					BasicException.newEmbeddedExceptionStack(
						exception, 
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.ACTIVATION_FAILURE
					)	
				)
			);
		}
	}

    /**
     * Provider the configuration override entries
     * 
     * @param delegate the delegate layer
     * 
     * @return the layer specific configuration override values
     */
	private Map<String, ?> getLayerPlugInConfigurationOverride(
		String layerPlugInName,
		Port<RestConnection> delegate
	) {
		final Map<String,Object> layerPlugInConfigurationOverride = new HashMap<String, Object>(); 
		if(this.layerPlugInConfigurationOverride.containsKey(layerPlugInName)){
			layerPlugInConfigurationOverride.putAll(
				this.layerPlugInConfigurationOverride.get(layerPlugInName)
			);
		}
        if(this.connectionFactory != null) {
            layerPlugInConfigurationOverride.put(
                "connectionFactory", 
                this.connectionFactory
            );
        }
		if(delegate != null) {
			layerPlugInConfigurationOverride.put(
				"delegate",
				delegate
		     );
		 } else {
			 final String datasourceName = getDatasourceName();
			 if(datasourceName != null) {
				 layerPlugInConfigurationOverride.put(
				    SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY_NAME, 
					datasourceName	 
				 );
			 }
		 }
		return layerPlugInConfigurationOverride;
	}

     /**
      * Load and parse the configuration properties
      * 
      * @return the configuration provider
      * 
      * @throws InvalidPropertyException
      */
	private ConfigurationProvider getConfigurationProvider(
	) throws InvalidPropertyException {
	    final Properties properties;
		try {
		    properties = PropertiesProvider.getProperties(getConfiguration());
		} catch (IOException exception) {
			throw ResourceExceptions.initHolder(
				new InvalidPropertyException(
					"Unable to retrieve the embedded data provider configuration",	
					BasicException.newEmbeddedExceptionStack(
						exception, 
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.INVALID_CONFIGURATION
					)	
				)
			);
		}
        return Configurations.getDataproviderConfigurationProvider(
            properties
        );
	}

    /**
     * Retrieve dataprovider.
     *
     * @return Returns the dataprovider.
     * 
     * @deprecated will not be supported by the dataprovider 2 stack
     */
     @Deprecated
    public ConnectionFactory getDataprovider() {
        return this.connectionFactory;
    }
    
    /**
     * Set dataprovider.
     * 
     * @param dataprovider The dataprovider to set.
     * 
     * @deprecated will not be supported by the dataprovider 2 stack
     */
     @Deprecated
    public void setDataprovider(ConnectionFactory dataprovider) {
        this.connectionFactory = dataprovider;
    }
     
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestPlugIn#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
		RestConnection connection
    ) throws ResourceException {
        if(this.delegate == null) {
        	this.delegate = buildDelegate();
        }
    	return new LayerInteraction(
    		connection, 
    		this.delegate.getInteraction(connection)
    	);
    }
    
    /**
     * Layer Interaction
     */
    class LayerInteraction extends AbstractInteraction<RestConnection> {

		protected LayerInteraction(RestConnection connection, Interaction delegate) {
			super(connection, delegate);
		}
    	
    }
    
}
