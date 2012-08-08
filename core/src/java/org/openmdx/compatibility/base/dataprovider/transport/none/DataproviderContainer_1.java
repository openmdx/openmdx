/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderContainer_1.java,v 1.12 2007/12/13 18:19:20 hburger Exp $
 * Description: spice: container implementation
 * Revision:    $Revision: 1.12 $
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
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1_0;
import org.openmdx.kernel.log.SysLog;

/**
 * @deprecated in favour of {@link 
 * org.openmdx.kernel.application.container.lightweight.LightweightContainer 
 * LightweightContainer} and its implementation classes.
 */
public class DataproviderContainer_1 {

    /**
     * Constructor
     */
    public DataproviderContainer_1(
    ) throws ServiceException {
        this.configuration = DeploymentConfiguration_1.getInstance();
        this.serviceLocator = org.openmdx.compatibility.base.application.container.SimpleServiceLocator.getInstance();
    }

    /**
     * Activate dataproviders from an array of deployment units.
     */
    public void deploy(
        Path[] deploymentUnits
    ) throws ServiceException {
          this.deployDataprovider(
              this.getDataprovider(deploymentUnits)
          );
    }
    
    /**
     * Deactivate dataproviders from an array of deployment units.
     */
    public void undeploy(
        Path[] deploymentUnits
    ) throws ServiceException {
        // Get the dataprovider list with reverse deployment order.
        List dataproviderList = new ArrayList();
        for(
            Iterator iter = getDataprovider(deploymentUnits).iterator();
            iter.hasNext();
        ) dataproviderList.add(0, iter.next());
            
        for(
            Iterator iter = dataproviderList.iterator();
            iter.hasNext();
        ) {
            DataproviderObject_1_0 dataproviderObject = (DataproviderObject_1_0) iter.next();
            String registrationId = (String) dataproviderObject.values("registrationId").get(0);
            SysLog.trace("Undeploying dataprovider with registrationId", registrationId);
            this.serviceLocator.unbind(registrationId);
        }
    }
    
    
    /**
     * Deploy dataproviders.
     */
    private void deployDataprovider(
        List dataproviderList
    ) throws ServiceException {
        for(
            Iterator iter = dataproviderList.iterator();
            iter.hasNext();
        ) {
            DataproviderObject_1_0 dataproviderObject = (DataproviderObject_1_0) iter.next();
            String privateRegistrationId = (String) dataproviderObject.values("registrationId").get(0);
            SysLog.trace("Deploying dataprovider with private registrationId", privateRegistrationId);
            
            // Create a pool of dataprovider objects.
            int initialCapacity = dataproviderObject.values("initialCapacity").isEmpty() ? 0 : ((Number)dataproviderObject.values("initialCapacity").get(0)).intValue();
            int maximalCapacity = dataproviderObject.values("maximalCapacity").isEmpty() ? Integer.MAX_VALUE : ((Number)dataproviderObject.values("maximalCapacity").get(0)).intValue();
            int capacityIncrement = dataproviderObject.values("capacityIncrement").isEmpty() ? 1 : ((Number)dataproviderObject.values("capacityIncrement").get(0)).intValue();
            ManagedDataproviderConnectionFactory connectionFactory = new ManagedDataproviderConnectionFactory(
                dataproviderObject,
                privateRegistrationId,
                initialCapacity,
                maximalCapacity,
                capacityIncrement
            );
            
            // Register connection factory.
            this.serviceLocator.bind(
            	DataproviderConnection.CONTAINER_CONTEXT + ":" + new Path(new String[]{ privateRegistrationId }),
                connectionFactory
            );
            
            // Register Dataprovider
            this.serviceLocator.bind(
                privateRegistrationId,
                new DataproviderConnectionFactory(connectionFactory)
            );
            
            // Register DataproviderResource if necessary
            String providerName = dataproviderObject.path().getLastComponent().toString();
            DataproviderObject_1_0[] dataproviderResources = this.configuration.getChildren(dataproviderObject.path().getParent().getParent().getChild("resourceAdapter"));
      SysLog.trace("dataproviderResources", Arrays.asList(dataproviderResources));

            for(
                int index = 0;
                index < dataproviderResources.length;
                index++
            ) {
                String resourceAdapterName = dataproviderResources[index].path().getLastComponent().toString();
        
                // Deploy only the resources that match the provider name.
                if(providerName.equals(resourceAdapterName)) {
                    String publicRegistrationId = (String) dataproviderResources[index].values("registrationId").get(0);
                    SysLog.trace("Deploying dataprovider with public registrationId", publicRegistrationId);
                    this.serviceLocator.bind(
                        publicRegistrationId,
                        new DataproviderConnectionFactory(connectionFactory)
                    );
                }
            }
        }
    }
    
    
    /**
     * Return the list of dataproviders sorted by deployment order.
     *
     * @param       deploymentUnit
     *              the path of deployment unit
     *
     * @return      the list of dataproviders
     *
     * @exception   ServiceException
     *              in case of failure
     */
    private List getDataprovider(
        Path[] deploymentUnits
    ) throws ServiceException {
        ArrayList result = new ArrayList();
        
        // Get the dataprovider objects that correspond to deploymentUnits.
        ArrayList unsortedDataproviders = new ArrayList();
        for(
            int index = 0;
            index < deploymentUnits.length;
            index++
        ) {
            DataproviderObject_1_0[] modules = this.configuration.getChildren(deploymentUnits[index].getChild("module"));
            for(
                int i = 0;
                i < modules.length;
                i++
            ) {
                DataproviderObject_1_0[] dataproviderObjects = this.configuration.getChildren(modules[i].path().getChild("component"));
                unsortedDataproviders.addAll(
                    Arrays.asList(dataproviderObjects)
                );
            }
        }
        
        // Get the deployment orders and order the dataprovider objects according them.
        for(
            int index = 0;
            index < unsortedDataproviders.size();
            index++
        ) {
            int deploymentOrder = ((Number) ((DataproviderObject_1_0) unsortedDataproviders.get(index)).getValues("deploymentOrder").get(0)).intValue();
            boolean inserted = false;
            for(
                int i = 0;
                i < result.size();
                i++
            ) {
                if(deploymentOrder < ((Number) ((DataproviderObject_1_0) result.get(i)).getValues("deploymentOrder").get(0)).intValue()) {
                    result.add(i, unsortedDataproviders.get(index));
                    inserted = true;
                    break;
                }
            }
            if(!inserted) result.add(unsortedDataproviders.get(index));
        }
        SysLog.trace(
            "Dataproviders in ascending deployment order", 
            result
        );
        return result;
    }
        
    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------
    
    /**
     * Deployment configuration.
     */
    private final DeploymentConfiguration_1_0 configuration;
    
    /**
     * The service locator object.
     */
    private final org.openmdx.compatibility.base.application.cci.ServiceLocator_1_0 serviceLocator;
    
}
