/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedDataproviderConnectionFactory.java,v 1.6 2007/12/13 18:19:20 hburger Exp $
 * Description: ManagedConnectionFactory class
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/13 18:19:20 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.none;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.ConfigurationProvider_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.kernel.Dataprovider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1_0;
import org.openmdx.kernel.log.SysLog;

/**
 * @deprecated in favour of {@link 
 * org.openmdx.kernel.application.container.lightweight.LightweightContainer 
 * LightweightContainer} and its implementation classes.
 */
class ManagedDataproviderConnectionFactory {

    /**
     * Constructor.
     */
    ManagedDataproviderConnectionFactory(
        DataproviderObject_1_0 dataproviderObject,
        String registrationId,
        int initialCapacity,
        int maximalCapacity,
        int capacityIncrement
    ) throws ServiceException {
        this.dataproviderObject = dataproviderObject;
        this.registrationId = registrationId;
        this.capacityIncrement = capacityIncrement;
        createDataprovider(initialCapacity);
    }
    
    /**
     * Get a connection from the factory.
     *
     * @return  connection object
     */
    synchronized Dataprovider_1_0Connection getConnection(
    ) throws ServiceException {
        if(this.managedConnections.size() == 0) createDataprovider(this.capacityIncrement);
        return (Dataprovider_1_0Connection) this.managedConnections.remove(0);
    }
        
    /**
     * Close the connection.
     */
    synchronized void returnConnection(
        Dataprovider_1_0Connection connection
    ){
        this.managedConnections.add(connection);
    }
    
    /**
     *
     */
    private void createDataprovider(
        int capacity
    ) throws ServiceException {
        
        // Construct property provider containing standard and additional properties.
        ConfigurationProvider_1_0 configurationProvider = new LocalConfigurationProvider(
            DeploymentConfiguration_1.getInstance(),
            (Path)this.dataproviderObject.values("dataproviderType").get(0),
            this.dataproviderObject.path()
        );
        
        for(
            int index = 0;
            index < capacity;
            index++
        ) {
            Configuration configuration = new Configuration();
            DataproviderObject_1_0[] resources = DeploymentConfiguration_1.getInstance().getChildren(
                this.dataproviderObject.path().getChild("resource")
            );
      SysLog.trace("resources configured for provider", Arrays.asList(resources)); 
            for(
                int i = 0;
                i < resources.length;
                i++
            ) {
                String registrationId = (String) resources[i].values("registrationId").get(0);
                Object resource = 
                  org.openmdx.compatibility.base.application.container.SimpleServiceLocator.getInstance(
                  ).lookup(
                  	registrationId
				  );
                PathComponent resourceName = resources[i].path().getLastComponent();
                configuration.values(resourceName.get(0)).set(
                    Integer.parseInt(resourceName.get(1)),
                    resource instanceof Dataprovider_1ConnectionFactory ? // isConnectionStateless
                        ((Dataprovider_1ConnectionFactory)resource).createConnection() :
                        resource
                );
            }
            this.managedConnections.add(
                new Dataprovider_1(
                    configuration,
                    configurationProvider, 
                    null // self
                )
            );
        }
    }
    
    /**
     * Returns the registration id.
     */
    public String getRegistrationId(
    ){
        return this.registrationId;
    }
    
    //------------------------------------------------------------------------
    // Classes
    //------------------------------------------------------------------------
    
    class LocalConfigurationProvider
        implements ConfigurationProvider_1_0 {
            
        public LocalConfigurationProvider(
            DeploymentConfiguration_1_0 deploymentConfiguration,
            Path dataproviderType,
            Path dataproviderInstance
        ) {
            this.standardProperties = deploymentConfiguration.getChildren(
                dataproviderType.getChild("property")
            );
            this.additionalProperties = deploymentConfiguration.getChildren(
                dataproviderInstance.getChild("property")
            );
        }
        
        public Configuration getConfiguration(
            String[] _section,
            Map specification
        ) throws ServiceException {
            Configuration configuration = new Configuration();
            String[] section = _section == null ? new String[]{} : _section;
            for(
                int index = 0;
                index < this.standardProperties.length;
                index++
            ) {
                PathComponent id = this.standardProperties[index].path().getLastComponent();
                if(id.startsWith(section) && id.size() == section.length+1) {
                    configuration.values(id.getLastField()).addAll(
                        this.standardProperties[index].values("value")
                    );
                }
            }
            for(
                int index = 0;
                index < this.additionalProperties.length;
                index++
            ) {
                PathComponent id = this.additionalProperties[index].path().getLastComponent();
                if(id.startsWith(section) && id.size() == section.length+1) {
          configuration.values(id.getLastField()).clear();
                    configuration.values(id.getLastField()).addAll(
                        this.additionalProperties[index].values("value")
                    );
                }
            }
        
            SysLog.trace(
                "Dataprovider Type Configuration", 
                Arrays.asList(this.standardProperties)
            );
            SysLog.trace(
                "Dataprovider Instance Configuration", 
                Arrays.asList(this.additionalProperties)
            );
            SysLog.trace(
                "Section " + Arrays.asList(section), 
                configuration
            );
            return configuration;
        }
        
        private DataproviderObject_1_0[] standardProperties;
        private DataproviderObject_1_0[] additionalProperties;
    }
    
    
    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    
    /**
     * The dataprovider object.
     */
    final private DataproviderObject_1_0 dataproviderObject;
    
    /**
     * Registration id at which the connection factory
     * is registered in service locator.
     */
    final private String registrationId;
    
    /**
     * Capacity increment value.
     */
    final private int capacityIncrement;
    
    /**
     * The list of managed connections.
     */
    final private List managedConnections = new ArrayList();
    
}
