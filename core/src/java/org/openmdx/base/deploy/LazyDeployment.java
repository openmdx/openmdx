/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LazyDeployment.java,v 1.11 2009/04/02 14:56:21 hburger Exp $
 * Description: Lazy Deployment
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/02 14:56:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.deploy;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.LoggerFactory;
import org.openmdx.kernel.url.URLInputStream;

/**
 * Lazy Deployment
 */
public class LazyDeployment implements InitialContextFactory {

    /**
     * Constructor 
     *
     * @param resourceArchiveURLs
     * @param enterpriseApplicationArchiveURLs
     * @param defaultProviderURI
     */
    public LazyDeployment(
        URL[] resourceArchiveURLs,
        URL[] enterpriseApplicationArchiveURLs, 
        String defaultProviderURI
    ){
        this.resourceArchives = resourceArchiveURLs == null ? new URL[]{} : resourceArchiveURLs;
        this.enterpriseApplicationArchives = enterpriseApplicationArchiveURLs == null ? new URL[]{} : enterpriseApplicationArchiveURLs;
        this.defaultProviderURI = defaultProviderURI; 
    }

    /**
     * Constructor 
     *
     * @param resourceArchiveURIs
     * @param enterpriseApplicationArchiveURIs
     * @param defaultProviderURI
     */
    public LazyDeployment(
        String resourceArchiveURIs,
        String enterpriseApplicationArchiveURIs, 
        String defaultProviderURI
    ){
        this(
            toURLs(resourceArchiveURIs),
            toURLs(enterpriseApplicationArchiveURIs), 
            defaultProviderURI
        );
    }

    /**
     * Timeout in milliseconds
     */
    private final long TIMEOUT = 60 * 1000l;

    /**
     * Time between polls in milliseconds
     */
    private final long POLL = 5 * 1000l;
    
    
    private final String defaultProviderURI;
    
    /**
     * The factory's initial context
     */
    private Context initialContext = null;
    
    /**
     * URL of the resource archives to be deployed
     */
    private final URL[] resourceArchives;

    /**
     * URL of the enterprise application archives to be deployed
     */
    private final URL[] enterpriseApplicationArchives;

    /**
     * Status in case of failures.
     */
    private BasicException exception = null;

    /**
     * 
     */
    private static Logger logger = LoggerFactory.getLogger();
    
    /**
     * Convert a String[] to the corresponding URL[]
     * 
     * @param source the String array
     * @return the corresponding URL[]
     * @throws BasicException 
     * 
     * @throws MalformedURLException if conversion fails
     */
    private static URL[] toURLs(
        String uris
    ){
        if(uris == null || uris.length() == 0) {
            return new URL[]{};
        }
        List<URL> urls = new ArrayList<URL>();
        for(
            StringTokenizer tokenizer = new StringTokenizer(uris);
            tokenizer.hasMoreTokens();
        ){
            String uri = tokenizer.nextToken();
            try {
                urls.add(
                    new URL(uri)
                );
            } catch (MalformedURLException exception) {
                throw new IllegalArgumentException(
                    "Could not parse deployment URI: " + uri,
                    exception
                );
            }
        }
        return urls.toArray(
            new URL[urls.size()]
        );
    }

    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(
        Hashtable<?, ?> environment
    ) throws NamingException {
        if(this.initialContext == null) {
            String providerURL = null;
            String userName = null;
            String password = null;
            try {
                if(environment != null) {
                    providerURL = (String) environment.get(Context.PROVIDER_URL);
                    userName = (String) environment.get(Context.SECURITY_PRINCIPAL);
                    password = (String) environment.get(Context.SECURITY_CREDENTIALS);
                }
                if(providerURL == null) {
                    providerURL = this.defaultProviderURI;
                }
                DeploymentManager deploymentManager = DeploymentFactoryManager.getInstance().getDeploymentManager(
                    providerURL,
                    userName,
                    password
                );
                Target[] inProcess = deploymentManager.getTargets();
                List<TargetModuleID> deploymentUnits = new ArrayList<TargetModuleID>();
                logger.log(Level.INFO, "Deploy {0}", Arrays.asList(this.resourceArchives));
                if(this.resourceArchives != null) {
                    for(URL resourceArchive : this.resourceArchives) {
                        ProgressObject selectionProgress = deploymentManager.distribute(
                            inProcess, 
                            ModuleType.RAR,
                            new URLInputStream(resourceArchive),
                            null // deploymentPlan)
                        );
                        for(TargetModuleID id : selectionProgress.getResultTargetModuleIDs()) {
                            deploymentUnits.add(id);
                            logger.log(Level.FINER,"Selecting resource adapter {0}", id);
                        }
                    }
                }
                logger.log(Level.INFO,"Deploy {0}", Arrays.asList(this.enterpriseApplicationArchives));
                if(this.enterpriseApplicationArchives != null) {
                    for(URL enterpriseApplicationArchive : this.enterpriseApplicationArchives) {
                        ProgressObject selectionProgress = deploymentManager.distribute(
                            inProcess, 
                            ModuleType.EAR,
                            new URLInputStream(enterpriseApplicationArchive),
                            null // deploymentPlan)
                        );
                        for(TargetModuleID id : selectionProgress.getResultTargetModuleIDs()) {
                            deploymentUnits.add(id);
                            logger.log(Level.FINER,"Selecting enterprise application {0}", id);
                        }
                    }
                }
                logger.log(Level.INFO,"Starting {0}", deploymentUnits);
                ProgressObject startProgress = deploymentManager.start(
                    deploymentUnits.toArray(
                        new TargetModuleID[deploymentUnits.size()]
                    )
                );
                logger.log(Level.INFO,"Waiting for {0}", deploymentUnits);
                for(
                	long timer = TIMEOUT;
                	startProgress.getDeploymentStatus().isRunning();
                	timer -= POLL
                ){
                	if(timer < 0l) throw BasicException.newStandAloneExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE,
                        "Deployment did not finish before timeout has been reached",
                        new BasicException.Parameter(Context.PROVIDER_URL, providerURL),
                        new BasicException.Parameter("Timeout", BigDecimal.valueOf(TIMEOUT,3))
                    );
                	wait(POLL);
                }
                if(startProgress.getDeploymentStatus().isFailed()) {
                    this.exception = BasicException.newStandAloneExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "Deployment manager signals failure",
                        new BasicException.Parameter("command", startProgress.getDeploymentStatus().getCommand()),
                        new BasicException.Parameter("state", startProgress.getDeploymentStatus().getState()),
                        new BasicException.Parameter("requested", deploymentUnits),
                        new BasicException.Parameter("started", (Object[])startProgress.getResultTargetModuleIDs())                    
                    );
                    logger.log(Level.WARNING,
                        "Deployment failure",
                        this.exception
                    );
                } else {
                	this.initialContext = new InitialContext(environment);
                }
            } catch (DeploymentManagerCreationException exception) {
                this.exception = BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Deployment Manager Acquisition Failure",
                    new BasicException.Parameter(Context.PROVIDER_URL, providerURL)
                );
                logger.log(Level.WARNING,
                    "Deployment Manager Acquisition Failure",
                    this.exception
                );
            } catch (InterruptedException exception) {
                this.exception = BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Interrupted while deployment is running",
                    new BasicException.Parameter(Context.PROVIDER_URL, providerURL),
                    new BasicException.Parameter("Timeout", BigDecimal.valueOf(TIMEOUT,3))
                );
            } catch (BasicException exception) {
            	this.exception = exception;
            }
        }
        if(this.exception == null) {
            return this.initialContext;
        }
        throw Throwables.initCause(
            new NoInitialContextException("Initial Context Acquisition Failure"), 
            exception,
            exception.getExceptionDomain(), 
            exception.getExceptionCode()
        );
    }


    /**
     * Discover J2EE deployment factories
     */
    static {
        try {
            Enumeration<URL> manifestURLs = Classes.getResources("META-INF/MANIFEST.MF");
            while(manifestURLs.hasMoreElements()) {
                URL manifestURL = manifestURLs.nextElement();
                Manifest manifest = new Manifest(manifestURL.openStream());
                String classNames = manifest.getMainAttributes().getValue(
                    "J2EE-DeploymentFactory-Implementation-Class"
                );
                if(classNames != null) {
                    logger.log(Level.INFO,"Registering deployment factory {0}", classNames);
                    for(
                        StringTokenizer tokenizer = new StringTokenizer(classNames);
                        tokenizer.hasMoreTokens();
                    ) {
                        String className = tokenizer.nextToken();
                        try {
                            DeploymentFactoryManager.getInstance().registerDeploymentFactory(
                                Classes.newApplicationInstance(
                                    DeploymentFactory.class,
                                    className
                                )
                            );
                        } catch (Throwable throwable) {
                              logger.log(Level.INFO,
                                  "J2EE deployment factory class '" + className + 
                                  "' specified in '" + manifestURL + 
                                  "' could not be instantiated and registered",
                                  throwable
                              );
                        }
                    }
                }
            }
        } catch (Exception exception) {
            logger.log(Level.INFO,
                "J2EE deployment factory discovery failure",
                exception
            );
        }
    }

}
