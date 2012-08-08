/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConnectorContainer_1.java,v 1.21 2008/09/10 08:55:24 hburger Exp $
 * Description: ConnectorContainer_1 class
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
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
package org.openmdx.compatibility.base.dataprovider.kernel;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1Home;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1RemoteConnectionFactory;
import org.openmdx.compatibility.base.application.cci.DbConnectionManager_1_0;
import org.openmdx.compatibility.base.application.spi.DbConnectionManagerSimple_1;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.webservices.WebService_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * @deprecated in favour of {@linkplain javax.naming.InitialContext
 * Standard JNDI access} for Dataprovider {@link
 * org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection 
 * Dataprovider_1_0Connections} and in favour of {@link 
 * org.openmdx.kernel.application.container.lightweight.LightweightContainer 
 * LightweightContainer} in case of {@link javax.sql.DataSource DataSources}
 */
@SuppressWarnings("unchecked")
public class ConnectorContainer_1 {

    //-------------------------------------------------------------------------
    public static void undeploy(
        Path[] deploymentUnits
    ) throws ServiceException {
        for (int i = 0; i < deploymentUnits.length; i++) {
            SysLog.trace("Undeploying deployment unit", deploymentUnits[i]);
            DataproviderObject_1_0[] resourceAdapters =
                DeploymentConfiguration_1.getInstance().getResourceAdapters(deploymentUnits[i]);
            for (int j = 0; j < resourceAdapters.length; j++) {
                DataproviderObject_1_0 resourceAdapter = resourceAdapters[j];
                String registrationId =
                    (String)resourceAdapter.getValues("registrationId").get(0);
                SysLog.trace("Undeploying resource adapter", resourceAdapter);
                org.openmdx.compatibility.base.application.container.SimpleServiceLocator.getInstance(
                ).unbind(
                    registrationId
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    public static void deploy(
        Path[] deploymentUnits
    ) throws ServiceException {
        org.openmdx.compatibility.base.application.cci.ServiceLocator_1_0 serviceLocator = 
            org.openmdx.compatibility.base.application.container.SimpleServiceLocator.getInstance();
        DeploymentConfiguration_1_0 configuration = DeploymentConfiguration_1.getInstance();
        for(
                int i = 0; 
                i < deploymentUnits.length; 
                i++
        ) {
            try {
                SysLog.trace("Deploying deployment unit", deploymentUnits[i]);
                DataproviderObject_1_0[] resourceAdapters =
                    configuration.getResourceAdapters(deploymentUnits[i]);
                for (int j = 0; j < resourceAdapters.length; j++) {
                    DataproviderObject_1_0 resourceAdapter = resourceAdapters[j];
                    String objectClass = (String)resourceAdapter.getValues(SystemAttributes.OBJECT_CLASS).get(0);
                    String registrationId = (String)resourceAdapter.getValues("registrationId").get(0);
                    SysLog.trace("Deploying resource adapter", resourceAdapter);

                    // DatabaseConnector
                    if (DATABASE_CONNECTOR_TYPES.contains(objectClass)) {
                        String connectionFactoryImpl = resourceAdapter.values("connectionFactoryImplementation").isEmpty()
                        ? null
                            : (String)resourceAdapter.values("connectionFactoryImplementation").get(0);
                        DbConnectionManager_1_0 connectionFactory = null;
                        if(THE_DATABASE_CONNECTION_FACTORY.equals(connectionFactoryImpl)) {

                            // userId
                            String userId = (String)configuration.get(
                                resourceAdapter.path().getDescendant(USER_ID_V1) == null
                                ? resourceAdapter.path().getDescendant(USER_ID_V2)
                                    : resourceAdapter.path().getDescendant(USER_ID_V1)
                            ).values("value").get(0);

                            // password
                            String password = (String)configuration.get(
                                resourceAdapter.path().getDescendant(PASSWORD_V1) == null
                                ? resourceAdapter.path().getDescendant(PASSWORD_V2)
                                    : resourceAdapter.path().getDescendant(PASSWORD_V1)
                            ).values("value").get(0);

                            connectionFactory = new DbConnectionManagerSimple_1(
                                (String)resourceAdapter.values("driver").get(0),
                                (String)resourceAdapter.values("uri").get(0),
                                userId,
                                password,
                                BasicException.Code.DEFAULT_DOMAIN
                            );
                        }
                        else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_SUPPORTED,
                                "No other database connection factory class than "
                                + THE_DATABASE_CONNECTION_FACTORY
                                + "is supported by this resource container",
                                new BasicException.Parameter("registrationId", registrationId),
                                new BasicException.Parameter("connectionFactoryImplementation", connectionFactoryImpl)
                            );
                        }
                        serviceLocator.bind(
                            registrationId, 
                            connectionFactory
                        );
                        SysLog.info(DATAPROVIDER_CONNECTOR_TYPES + " deployed", registrationId);
                    }

                    // HttpUrlConnector
                    else if (HTTP_URL_CONNECTOR_TYPES.contains(objectClass)) {
                        String connectionFactoryImpl = resourceAdapter.values("connectionFactoryImplementation").isEmpty()
                        ? null
                            : (String)resourceAdapter.values("connectionFactoryImplementation").get(0);
                        Dataprovider_1ConnectionFactory connectionFactory = null;

                        if(THE_WEBSERVICE_CONNECTION_FACTORY.equals(connectionFactoryImpl)) {

                            // userId
                            String userId = (String)configuration.get(
                                resourceAdapter.path().getDescendant(USER_ID_V1) == null
                                ? resourceAdapter.path().getDescendant(USER_ID_V2)
                                    : resourceAdapter.path().getDescendant(USER_ID_V1)
                            ).values("value").get(0);

                            // password
                            String password = (String)configuration.get(
                                resourceAdapter.path().getDescendant(PASSWORD_V1) == null
                                ? resourceAdapter.path().getDescendant(PASSWORD_V2)
                                    : resourceAdapter.path().getDescendant(PASSWORD_V1)
                            ).values("value").get(0);

                            // connectorFactory
                            connectionFactory = new WebService_1ConnectionFactoryImpl(
                                (String)resourceAdapter.values("uri").get(0),
                                userId,
                                password
                            );
                        }
                        else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_SUPPORTED,
                                "No other database connection factory class than "
                                + THE_DATABASE_CONNECTION_FACTORY
                                + "is supported by this resource container",
                                new BasicException.Parameter("registrationId", registrationId),
                                new BasicException.Parameter("connectionFactoryImplementation", connectionFactoryImpl)
                            );
                        }
                        serviceLocator.bind(
                            registrationId, 
                            connectionFactory
                        );
                        SysLog.info(DATAPROVIDER_CONNECTOR_TYPES + " deployed", registrationId);
                    }

                    // DataproviderConnector
                    else if(DATAPROVIDER_CONNECTOR_TYPES.contains(objectClass)) {
                        String connectionFactoryImpl = resourceAdapter.values("connectionFactoryImplementation").isEmpty()
                        ? null
                            : (String)resourceAdapter.values("connectionFactoryImplementation").get(0);
                        Dataprovider_1ConnectionFactory connectionFactory = null;
                        if(THE_DATAPROVIDER_CONNECTION_FACTORY.equals(connectionFactoryImpl)) {
                            String uri = (String)resourceAdapter.values("uri").get(0);
                            int separator = uri.indexOf('/', uri.indexOf("//") + 2);
                            String providerUrl = uri.substring(0, separator);
                            String jndiName = uri.substring(separator + 1);
                            String securityPrincipal = (String)configuration.get(
                                resourceAdapter.path().getDescendant(USER_ID_V1) == null
                                ? resourceAdapter.path().getDescendant(USER_ID_V2)
                                    : resourceAdapter.path().getDescendant(USER_ID_V1)
                            ).values("value").get(0);
                            String password = (String)configuration.get(
                                resourceAdapter.path().getDescendant(PASSWORD_V1) == null
                                ? resourceAdapter.path().getDescendant(PASSWORD_V2)
                                    : resourceAdapter.path().getDescendant(PASSWORD_V1)
                            ).values("value").get(0);
                            String initialContextFactory = (String)configuration.get(
                                resourceAdapter.path().getDescendant(
                                    INITIAL_CONTEXT_FACTORY_V1) == null
                                    ? resourceAdapter.path().getDescendant(
                                        INITIAL_CONTEXT_FACTORY_V2)
                                        : resourceAdapter.path().getDescendant(
                                            INITIAL_CONTEXT_FACTORY_V1)
                            ).values("value").get(0);
                            Hashtable environment = new Hashtable();
                            environment.put(Context.PROVIDER_URL, providerUrl);
                            environment.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
                            environment.put(Context.SECURITY_CREDENTIALS, password);
                            environment.put(
                                Context.INITIAL_CONTEXT_FACTORY,
                                initialContextFactory);
                            try {
                                Context context = new InitialContext(environment);
                                try {
                                    connectionFactory = new Dataprovider_1RemoteConnectionFactory(
                                        (Dataprovider_1Home)PortableRemoteObject.narrow(
                                            context.lookup(jndiName),
                                            Dataprovider_1Home.class
                                        )
                                    );
                                } finally {
                                    context.close();
                                }
                            }
                            catch (Exception exception) {
                                throw new ServiceException(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ACTIVATION_FAILURE,
                                    "Could not set up dataprovider connection factory",
                                    new BasicException.Parameter("uri", uri),
                                    new BasicException.Parameter(Context.PROVIDER_URL, providerUrl),
                                    new BasicException.Parameter(
                                        Context.SECURITY_PRINCIPAL,
                                        securityPrincipal)
                                );
                            }
                        }
                        else {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_SUPPORTED,
                                "No other dataprovider connection factory class than "
                                + THE_DATAPROVIDER_CONNECTION_FACTORY
                                + "is supported by this resource container",
                                new BasicException.Parameter("registrationId", registrationId),
                                new BasicException.Parameter("connectionFactoryImplementation", connectionFactoryImpl)
                            );
                        }
                        serviceLocator.bind(registrationId, connectionFactory);
                        SysLog.info(DATAPROVIDER_CONNECTOR_TYPES + " deployed", registrationId);
                    }
                    else if (DATAPROVIDER_RESOURCE_TYPES.contains(objectClass)) {
                        SysLog.trace("InboundCommunication ignored by ConnectorContainer_1", registrationId);
                    }
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "No other resource adapters than "
                            + DATAPROVIDER_CONNECTOR_TYPES
                            + " and "
                            + DATABASE_CONNECTOR_TYPES
                            + " are supported by this resource container",
                            new BasicException.Parameter("registrationId", registrationId),
                            new BasicException.Parameter("class", objectClass)
                        );
                    }
                }
            }
            catch (RuntimeException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Deployment failed",
                    new BasicException.Parameter("deploymentUnit", deploymentUnits[i])
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    final private static String[] INITIAL_CONTEXT_FACTORY_V1 = {"property", "JNDI:InitialContextFactory"};
    final private static String[] INITIAL_CONTEXT_FACTORY_V2 = {"propertySet", "JNDI", "property", "InitialContextFactory"};
    final private static String[] USER_ID_V1 = {"property", "BasicPassword:UserName"};
    final private static String[] USER_ID_V2 = {"propertySet", "BasicPassword", "property", "UserName"};
    final private static String[] PASSWORD_V1 = {"property", "BasicPassword:Password"};
    final private static String[] PASSWORD_V2 = {"propertySet", "BasicPassword", "property", "Password"};

    final static private List DATABASE_CONNECTOR_TYPES = Arrays.asList(
        "org:openmdx:deployment1:DatabaseConnector"
    );

    final static private List HTTP_URL_CONNECTOR_TYPES = Arrays.asList(
        "org:openmdx:deployment1:HttpUrlConnector"
    );

    final static private List DATAPROVIDER_CONNECTOR_TYPES = Arrays.asList(
        "org:openmdx:deployment1:DataproviderConnector"
    );

    final static private List DATAPROVIDER_RESOURCE_TYPES = Arrays.asList(
        "org:openmdx:deployment1:DataproviderResource"
    );  

    final private static String THE_DATABASE_CONNECTION_FACTORY = 
        "org.openmdx.compatibility.kernel.application.spi.DbConnectionManagerSimple_1";

    final private static String THE_DATAPROVIDER_CONNECTION_FACTORY = 
        "org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1RemoteConnectionFactory";

    final private static String THE_WEBSERVICE_CONNECTION_FACTORY = 
        "org.openmdx.compatibility.base.dataprovider.transport.webservices.WebService_1ConnectionFactoryImpl";

}

//--- End of File -----------------------------------------------------------
