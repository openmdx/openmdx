/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DeploymentConfiguration_1.java,v 1.8 2008/03/21 18:34:48 hburger Exp $
 * Description: ModelPackage_1_0 interface
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:34:48 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.deployment1.accessor.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.log.SysLog;
/**
 * Provides a simple interface to access org:openmdx:cci:deplyoment1-compliant
 * configurations. This interface is a very leightweight version of the 
 * org.openmdx.cci.deployment.accessor.jmi model interface
 * and is used mainly for spice-internal purposes (e.g. for light-weight 
 * container bootstraping)
 * 
 * @author hburger
 */
@SuppressWarnings("unchecked")
public class DeploymentConfiguration_1 
    implements DeploymentConfiguration_1_0
{

    /**
     * Constructor
     */
    private DeploymentConfiguration_1(
        Map deploymentConfiguration
    ) throws ServiceException {
        this.deploymentConfiguration = deploymentConfiguration;
    }

    /**
     * Constructor
     * 
     * @param uri
     * @throws ServiceException
     */
    public DeploymentConfiguration_1(
        String[] uri
    ) throws ServiceException {
        this(new HashMap());
        new XmlImporter(
            this.deploymentConfiguration,
            VALIDATE
        ).process(uri);
    }


    //------------------------------------------------------------------------
    // Implements DeploymentConfiguration_1_0
    //------------------------------------------------------------------------
    
    /**
     * This method shall be replaced by more specific ones.
     */
    public DataproviderObject_1_0[] getChildren(
        Path parent
    ) {
        List result = new ArrayList();
        for(
            Iterator iter = this.deploymentConfiguration.values().iterator();
            iter.hasNext();
        ) {
            DataproviderObject_1_0 dataproviderObject = (DataproviderObject_1_0)iter.next();
            if(dataproviderObject.path().getParent().equals(parent)) result.add(dataproviderObject);
        }
        return (DataproviderObject_1_0[]) result.toArray(new DataproviderObject[result.size()]);
    }

    /**
     * This method shall be replaced by more specific ones.
     */
    public DataproviderObject_1_0[] getResourceAdapters(
        Path ancestor
    ){
        List result = new ArrayList();
        for(
            Iterator iter = this.deploymentConfiguration.values().iterator();
            iter.hasNext();
        ) {
            DataproviderObject_1_0 dataproviderObject = (DataproviderObject_1_0)iter.next();
            if(
                dataproviderObject.path().size() == 15 &&
                "resourceAdapter".equals(dataproviderObject.path().get(13)) &
                dataproviderObject.path().startsWith(ancestor)
            ) result.add(dataproviderObject);
        }
        return (DataproviderObject_1_0[]) result.toArray(new DataproviderObject[result.size()]);
    }

    /**
     * This method may be replaced by more specific ones.
     */
    public DataproviderObject_1_0 get(
        Path path
    ) {
        return (DataproviderObject)this.deploymentConfiguration.get(path); 
    }

    /**
     * Return the configuration specific service locator
     */
    public static DeploymentConfiguration_1_0 getInstance(
    ) throws ServiceException {
        if (DeploymentConfiguration_1.instance == null) {
            synchronized (DeploymentConfiguration_1.class) {
                if (DeploymentConfiguration_1.instance == null) 
                    DeploymentConfiguration_1.instance = 
                    new DeploymentConfiguration_1(
                        getDeploymentConfigurationUrls()
                    );
            }
        }
        return DeploymentConfiguration_1.instance;
    }

    /**
     * Creates a new deployment configuration respectively replaces an
     * existing one.
     * <p>
     * The newly created deployment configuration will not include any entries
     * from XML deployment configuration URL's.
     *
     * @param       deploymentConfiguration
     *              A Map containing "dataproviderObject.path() -> 
     *              dataproviderObject" mappings. This map will be shared by
     *              caller and callee.
     *
     * @return      the newly created instance
     */
    public static DeploymentConfiguration_1_0 createInstance(
        Map deploymentConfiguration
    ) throws ServiceException {
        synchronized (DeploymentConfiguration_1.class) {
            DeploymentConfiguration_1.instance = new DeploymentConfiguration_1(
                deploymentConfiguration
            );
        }
        return DeploymentConfiguration_1.instance;
    }

    /**
     * Creates a new deployment configuration respectively replaces an
     * existing one.
     * 
     * @param uri
     * @throws ServiceException
     */
    public static DeploymentConfiguration_1_0 createInstance(
        String[] uri
    ) throws ServiceException {
        Map configuration = new HashMap();
        new XmlImporter(
            configuration,
            VALIDATE
        ).process(uri);
        return createInstance(configuration);
    }


    /**
     * Allow the spcification of a comma separated list of URLs
     */
    private static String[] getDeploymentConfigurationUrls(
    ){
        StringTokenizer tokenizer = new StringTokenizer(
            System.getProperty(
                DEPLOYMENT_CONFIGURATION_PROPERTY,
                DEFAULT_DEPLOYMENT_CONFIGURATION
            ),
            SEPARATOR
        );
        List result = new ArrayList();
        while(tokenizer.hasMoreTokens()) result.add(tokenizer.nextToken());
        SysLog.trace(DEPLOYMENT_CONFIGURATION_PROPERTY, result);
        return (String[])result.toArray(new String[result.size()]);
    }


    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------
    
    private final Map deploymentConfiguration;

    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------
    
    /**
     * The DeploymentConfiguation_1 singleton
     */
    private static DeploymentConfiguration_1_0 instance = null;

    /**
     * The 
     */
    private final static String DEFAULT_DEPLOYMENT_CONFIGURATION = 
        "deployment.configuration.xml";

    /**
     * One can override the default deployment configuration URL by setting
     * this system property.
     */
    public final static String DEPLOYMENT_CONFIGURATION_PROPERTY =
        "org.openmdx.deployment1.accessor.basic.configuration";

    /**
     * The URLs are separated by commas.
     */ 
    public final static String SEPARATOR = ",";

    private static boolean VALIDATE = false;
    
}
