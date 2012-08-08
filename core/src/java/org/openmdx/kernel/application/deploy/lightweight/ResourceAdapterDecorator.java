/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ResourceAdapterDecorator.java,v 1.9 2008/01/13 21:37:34 hburger Exp $
 * Description: Resource Adapter Decorator
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/13 21:37:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.lightweight;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.AuthenticationMechanism;
import org.openmdx.kernel.application.deploy.spi.Deployment.Connector;
import org.openmdx.kernel.application.deploy.spi.Deployment.ResourceAdapter;

/**
 * Resource Adapter Decorator
 */
public class ResourceAdapterDecorator
  implements ResourceAdapter
{

    public ResourceAdapterDecorator(
        ResourceAdapter delegate,
        Connector owner
    ) {
        this.delegate = delegate;
        this.owner = owner;
    }

    public Map<String,Object> getConfigProperties(
    ) {
        return this.delegate.getConfigProperties();
    }

    public String getManagedConnectionFactoryClass(
    ) {
        return this.delegate.getManagedConnectionFactoryClass();
    }

    public String getConnectionFactoryInterface(
    ) {
        return this.delegate.getConnectionFactoryInterface();
    }

    public String getConnectionFactoryImplClass(
    ) {
        return this.connectionFactoryImplClass;
    }

    public String getConnectionInterface(
    ) {
        return this.delegate.getConnectionInterface();
    }

    public String getConnectionImplClass(
    ) {
        return this.delegate.getConnectionImplClass();
    }

    public String getTransactionSupport(
    ) {
        return this.delegate.getTransactionSupport();
    }

    public List<AuthenticationMechanism> getAuthenticationMechanism(
    ) {
        return this.delegate.getAuthenticationMechanism();
    }

    public boolean getReauthenticationSupport(
    ) {
        return this.delegate.getReauthenticationSupport();
    }

    public void deploy(
        Context containerContext,
        Context applicationContext,
        Reference reference
    ) throws NamingException {
        this.delegate.deploy(containerContext, applicationContext, reference);
    }

    public Integer getInitialCapacity(
    ) {
        this.owner.validate();
        return this.initialCapacity;
    }

    public Integer getMaximumCapacity(
    ) {
        this.owner.validate();
        return this.maximumCapacity;
    }

    public Long getMaximumWait(
    ) {
        this.owner.validate();
        return this.maximumWait;
    }

    public void validate(
        Report report
    ) {
        this.maximumCapacity = this.delegate.getMaximumCapacity();
        if (this.maximumCapacity == null) {
            // set default value
            this.maximumCapacity = new Integer(MAXIMUM_CAPACITY_DEFAULT);
            report.addInfo("unset attribute 'MaximumCapacity' was overriden with default value " + this.maximumCapacity);
        }

        this.initialCapacity = this.delegate.getInitialCapacity();
        if (this.initialCapacity == null) {
            // set default value
            this.initialCapacity = new Integer(INITIAL_CAPACITY_DEFAULT);
            report.addInfo("unset attribute 'InitialCapacity' was overriden with default value " + this.initialCapacity);
        }

        this.connectionFactoryImplClass = this.delegate.getConnectionFactoryImplClass();
        if("org.openmdx.kernel.application.container.spi.sql.ContainerAuthenticatedDatabaseConnectionFactory".equals(this.connectionFactoryImplClass)) {
            this.connectionFactoryImplClass = "org.openmdx.kernel.application.container.spi.sql.DatabaseConnectionFactory";
            report.addWarning(
                "Deprecated connectionfactory-impl-class '" + 
                "org.openmdx.kernel.application.container.spi.sql.ContainerAuthenticatedDatabaseConnectionFactory" +
                "' replaced by '" +
                "org.openmdx.kernel.application.container.spi.sql.DatabaseConnectionFactory'" 
            );
        }

        this.maximumWait = this.delegate.getMaximumWait();
        if (this.maximumWait == null) {
            // set default value
            this.maximumWait = new Long(MAXIMUM_WAIT_DEFAULT);
            report.addInfo("unset attribute 'MaximumWait' was overriden with default value " + this.maximumWait);
        }

        String connectionURL = (String) this.getConfigProperties().get("ConnectionURL");
        String driver = (String) this.getConfigProperties().get("Driver");
        if (driver == null && connectionURL != null && connectionURL.toLowerCase().startsWith("jdbc:")){
            synchronized(ResourceAdapterDecorator.class){
                if(ResourceAdapterDecorator.jdbcDriverUrlProperties == null) {
                    ResourceAdapterDecorator.jdbcDriverUrlProperties = new Properties();
                    URL propertiesUrl = Classes.getApplicationResource(JDBC_DRIVER_URL_PROPERTIES);
                    if(propertiesUrl == null){
                        report.addError("Resource '" + JDBC_DRIVER_URL_PROPERTIES + "' not found");
                    } else try {
                        ResourceAdapterDecorator.jdbcDriverUrlProperties.load(
                            propertiesUrl.openStream()
                        );
                    } catch (IOException e) {
                        report.addError("Loading resource '" + JDBC_DRIVER_URL_PROPERTIES + "' failed", e);
                    }	      	   
                }
            }
            for(
                    Iterator<?> i = ResourceAdapterDecorator.jdbcDriverUrlProperties.entrySet().iterator();
                    i.hasNext();
            ){
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) i.next();
                if(connectionURL.startsWith((String) e.getValue())) {
                    driver = (String) e.getKey();
                    break;
                }
            }
            if(driver == null){
                report.addWarning("no default driver found for the specified ConnectionURL: " + connectionURL);
            } else {
                this.getConfigProperties().put("Driver",driver);
                report.addInfo("'Driver' property was set to its 'ConnectionURL' property dependend default value '" + driver + "'");
            }
        }

        for(
                Iterator<?> i = this.getAuthenticationMechanism().iterator();
                i.hasNext();
        ){
            AuthenticationMechanism authenticationMechanism = (AuthenticationMechanism) i.next();
            if(
                    !"BasicPassword".equals(authenticationMechanism.getAuthenticationMechanismType()) &&
                    !"TokenCookie".equals(authenticationMechanism.getAuthenticationMechanismType())
            ) report.addError(
                "Authentication mechanism type '" + authenticationMechanism.getAuthenticationMechanismType() + "' not supported"
            );
            if(
                    !"javax.resource.spi.security.PasswordCredential".equals(authenticationMechanism.getCredentialInterface())
            ) report.addError(
                "Credential interface '" + authenticationMechanism.getCredentialInterface() + "' not supported"
            );
        }

    }

    private final ResourceAdapter delegate;
    private final Connector owner;

    private String connectionFactoryImplClass = null;
    private Long maximumWait = null;
    private Integer initialCapacity = null;
    private Integer maximumCapacity = null;
    private static Properties jdbcDriverUrlProperties;
    private static final long MAXIMUM_WAIT_DEFAULT = java.lang.Long.MAX_VALUE;
    private static final int INITIAL_CAPACITY_DEFAULT = 1;
    private static final int MAXIMUM_CAPACITY_DEFAULT = java.lang.Integer.MAX_VALUE;
    private static final String JDBC_DRIVER_URL_PROPERTIES = "org/openmdx/kernel/application/deploy/jdbc-driver-url.properties";  

}
