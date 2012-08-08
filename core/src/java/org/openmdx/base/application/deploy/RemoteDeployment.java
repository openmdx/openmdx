/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RemoteDeployment.java,v 1.8 2008/01/09 15:55:06 hburger Exp $
 * Description: Remote Deployment
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/09 15:55:06 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.base.application.deploy;

import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.deploy.cci.DeploymentProperties;
import org.openmdx.kernel.application.process.Subprocess;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.initial.ContextFactory;

import org.openmdx.kernel.text.StringBuilders;

/**
 * Deployment in a separate virtual machine
 */
public class RemoteDeployment implements Deployment {

    /**
     * Constructor
     * 
     * @param properties the properties are never <code>null</code> and may 
     * include for example<ul>
     * <li>org.openmdx.deploy.connector.urls
     * <li>org.openmdx.deploy.application.urls
     * <li>org.openmdx.rmi.registry.port
     * <li>org.openmdx.rmi.naming.service
     * </ul>
     * @param connectors connectors overrides the corresponding properties 
     * entry unless it is <code>null</code>
     * @param applications applications overrides the corresponding properties 
     * entry unless it is <code>null</code>
     * @param detailLog the detail log may be <code>null</code>
     * @param exceptionLog the exception log may be <code>null</code>
     */
    protected RemoteDeployment(
        Map properties,
        String connectors,
        String applications,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this.properties = new HashMap(properties);
        if(connectors != null) this.properties.put(
            DeploymentProperties.CONNECTOR_URLS,
            connectors
        );
        if(applications != null) this.properties.put(
            DeploymentProperties.APPLICATION_URLS,
            applications
        );
        if(!properties.containsKey(Contexts.REGISTRY_PORT)) this.properties.put(
            Contexts.REGISTRY_PORT,
            new Integer(Contexts.getRegistryPort())
        );
        if(!properties.containsKey(Contexts.NAMING_SERVICE)) this.properties.put(
            Contexts.NAMING_SERVICE,
            Contexts.getNamingService()
        );
        this.detailLog = detailLog;
        this.exceptionLog = exceptionLog;
    }

    /**
     * Constructor
     * 
     * @param properties the properties are never <code>null</code> and may 
     * include for example<ul>
     * <li>org.openmdx.deploy.connector.urls
     * <li>org.openmdx.deploy.application.urls
     * <li>org.openmdx.rmi.registry.port
     * <li>org.openmdx.rmi.naming.service
     * </ul>
     * @param detailLog the detail log may be <code>null</code>
     * @param exceptionLog the exception log may be <code>null</code>
     */
    public RemoteDeployment(
        Map properties,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(properties, null, null, detailLog, exceptionLog);
    }

    /**
     * Constructor
     * 
     * @param connector may be <code>null</code> or empty
     * @param application may be <code>null</code> or empty
     * @param detailLog the detail log may be <code>null</code>
     * @param exceptionLog the exception log may be <code>null</code>
     */
    public RemoteDeployment(
        String connector,
        String application,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(
            Collections.EMPTY_MAP,
            connector,
            application,
            detailLog,
            exceptionLog
        );
    }

    /**
     * Constructor
     * 
     * @param connectors connectors may be <code>null</code> or empty
     * @param applications applications may be <code>null</code> or empty
     * @param detailLog the detail log may be <code>null</code>
     * @param exceptionLog the exception log may be <code>null</code>
     */
    public RemoteDeployment(
        String[] connectors,
        String[] applications,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(
            Collections.EMPTY_MAP,
            toPropertyValue(connectors),
            toPropertyValue(applications),
            detailLog,
            exceptionLog
        );
    }

    /**
     * Constructor
     * 
     * @param connectors connectors may be <code>null</code> or empty
     * @param applications applications may be <code>null</code> or empty 
     * @param detailLog the detail log may be <code>null</code>
     * @param exceptionLog the exception log may be <code>null</code>
     */
    public RemoteDeployment(
        URL[] connectors,
        URL[] applications,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(
            Collections.EMPTY_MAP,
            toPropertyValue(connectors),
            toPropertyValue(applications),
            detailLog,
            exceptionLog
        );
    }

    /**
     * The launch properties
     */
    private final Map<String,Object> properties;

    /**
     * The in-process deployment's detail log writer
     */
    private final PrintStream detailLog;

    /**
     * The in-process deployment's exception log writer
     */
    private final PrintStream exceptionLog;

    /**
     * Status in case of failures
     */
    private ServiceException exception = null;

    /**
     * The lightweight container process instance
     */
    private Subprocess container = null;

    /**
     * Convert a String[] to the corresponding URL[]
     * 
     * @param source the String array
     * @return the corresponding URL[]
     * @throws BasicException 
     */
    private static String toPropertyValue(
        Object[] source
    ){
        if(source == null || source.length == 0) return null;
        CharSequence target = StringBuilders.newStringBuilder().append(source[0]);
        for(
            int i = 1;
            i < source.length;
            i++
        ) StringBuilders.asStringBuilder(target).append(' ').append(source[i]);
        return target.toString();
    }

    /**
     * Retrive the exception parameters based on the properties
     * 
     * @return he exception parameters
     */
    private BasicException.Parameter[] getExceptionParameters (
    ){
        BasicException.Parameter[] parameters = new BasicException.Parameter[this.properties.size()];
        int i = 0;
        for(
            Iterator j = this.properties.entrySet().iterator();
            j.hasNext();
        ){
            Map.Entry e = (Entry) j.next();
            parameters[i++] = new BasicException.Parameter(
                e.getKey().toString(),
                e.getValue().toString()
            );
        }
        return parameters;
    }


    //------------------------------------------------------------------------
    // Implements Deployment
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.client.Deployment#destroy()
     */
    public synchronized void destroy() {
        if(this.container != null) try {
            this.container.destroy();
            this.exception = null;
        } catch (Exception exception) {
            this.exception = new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.DEACTIVATION_FAILURE,
                getExceptionParameters(),
                "Destruction of lightweight container failed"
            );
        } finally {
            this.container = null;
        }
    }

    /**
     * Retrieve the Exeption in case of a failure
     * 
     * @return the exception or <code>null</code> if no exception has occured yet
     */
    public ServiceException getStatus (
    ){
        return this.exception;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.deploy.Deployment#getInitialContext()
     */
    public Context context() throws ServiceException {
        if(this.container == null) try {
            this.container = LightweightContainer.fork(
                null, // jre
                null, // classpath
                this.properties,
                this.detailLog, // outputStream
                this.exceptionLog // exceptionStream
            );
            Integer runValue = this.container.runValue();
            if(!Subprocess.SUCCESS.equals(runValue)) this.exception = new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                getExceptionParameters(),
                "Deployment in lightweight container failed"
            );
        } catch (Exception exception) {
            this.exception = new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                getExceptionParameters(),
                "Forking lightweight container process faild"
            );
        }
        if(this.exception != null) throw exception;
        String providerURL = "//localhost:" +
            this.properties.get(Contexts.REGISTRY_PORT) + '/' +
            this.properties.get(Contexts.NAMING_SERVICE);
        try {
            Properties environment = new Properties();
            environment.put(
                Context.INITIAL_CONTEXT_FACTORY,
                ContextFactory.class.getName()
            );
            environment.setProperty(
                Context.PROVIDER_URL,
                providerURL
            );
            return new InitialContext(environment);
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.CREATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("container", container),
                    new BasicException.Parameter(Context.PROVIDER_URL, providerURL)
                },
                "Initial context creation failed"
            );
        }
    }

}
