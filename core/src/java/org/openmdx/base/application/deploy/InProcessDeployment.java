/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InProcessDeployment.java,v 1.3 2008/01/25 00:58:53 hburger Exp $
 * Description: In-Process Deployment
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 00:58:53 $
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * In-Process Deployment
 */
public class InProcessDeployment implements Deployment {

    /**
     * Constructor
     * 
     * @param connector
     * @param application
     * @param detailLog
     * @param exceptionLog
     */
    public InProcessDeployment(
        String connector,
        String application,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(
            connector == null ? null : new String[]{connector},
            application == null ? null : new String[]{application},
            detailLog,
            exceptionLog
        );
    }

    /**
     * Constructor
     * 
     * @param connectors
     * @param applications
     * @param detailLog
     * @param exceptionLog
     */
    public InProcessDeployment(
        String[] connectors,
        String[] applications,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        try {
            this.connectors = toURL(connectors);
            this.applications = toURL(applications);
            this.detailLog = detailLog;
            this.exceptionLog = exceptionLog;
        } catch (ServiceException exception) {
            this.exception = exception;
        }
    }

    /**
     * Constructor
     * 
     * @param connectors
     * @param applications
     * @param logWriter
     * @param exceptionLog
     */
    public InProcessDeployment(
        URL[] connectors,
        URL[] applications,
        PrintStream logWriter,
        PrintStream exceptionLog
    ){
        this.connectors = connectors;
        this.applications = applications;
        this.detailLog = logWriter;
        this.exceptionLog = exceptionLog;
    }

    /**
     * URL of the resource archives to be deployed
     */
    private  URL[] connectors;
    
    /**
     * URL of the resource archives to be deployed
     */
    private  URL[] applications;
    
    /**
     * The in-process deployment's detail log writer
     */
    private  PrintStream detailLog;

    /**
     * The in-process deployment's exception log writer
     */
    private  PrintStream exceptionLog;

    /**
     * Status in case of failures.
     */
    private ServiceException exception = null;

    /**
     * The in-process lightweight container instance
     */
    private LightweightContainer container = null;
    
    /**
     * Convert a String[] to the corresponding URL[]
     * 
     * @param source the String array
     * @return the corresponding URL[]
     * @throws BasicException 
     * 
     * @throws MalformedURLException if conversion fails
     */
    private static URL[] toURL(
        String[] source
    ) throws ServiceException{
        if(source == null) return null;
        URL[] target = new URL[source.length];
        for(
            int i = 0;
            i < source.length;
            i++
        ) try {
            target[i] = new URL(source[i]);
        } catch (MalformedURLException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("url", source[i]) 
                },
                "Could not parse deployment URL"
           );
        }
        return target;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Deployment
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.application.deploy.Deployment#getInitialContext()
     */
    public Context context() throws ServiceException {
        if(this.container == null) {
            if(this.exception == null) try {
                this.container = LightweightContainer.getInstance(
                    LightweightContainer.Mode.ENTERPRISE_APPLICATION_CONTAINER
                );
                List reports = new ArrayList();
                if(this.connectors != null) for(
                    int i = 0;
                    i < this.connectors.length;
                    i++
                ) reports.add(
                    log(
                        log(this.connectors[i], "Deploying Connector"),    
                        container.deployConnector(this.connectors[i])
                    )
                ); 
                if(this.applications != null) for(
                    int i = 0;
                    i < this.applications.length;
                    i++
                ) reports.addAll(
                    log(
                        log(this.applications[i], "Deploying Application"),    
                        container.deployApplication(this.applications[i])
                    )
                ); 
                for(
                    Iterator i = reports.iterator();
                    i.hasNext();
                ) if(
                   ((Report)i.next()).isSuccess()
                ) i.remove();
                if(! reports.isEmpty()) this.exception = new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("container", container),
                        new BasicException.Parameter("connectors", this.connectors),
                        new BasicException.Parameter("applications", this.applications),
                        new BasicException.Parameter("failures", reports)
                    },
                    "Deployment tool signals invalid configuration"
                );
            } catch (Exception exception) {
                this.exception = new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("container", container),
                        new BasicException.Parameter("connectors", this.connectors),
                        new BasicException.Parameter("applications", this.applications)
                    },
                    "Deployment tool could not load deployment units"
                );
            }
            if(this.exception != null) log("Deployment failed", this.exception);
        }
        if(this.exception != null) throw this.exception;
        try {
            return new InitialContext();
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.CREATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("container", container)
                },
                "Initial context creation failed"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.client.Deployment#destroy()
     */
    public void destroy() {
        // Nothing to be done for in-process deployment
    }

    
    //------------------------------------------------------------------------
    // Logging
    //------------------------------------------------------------------------
    
    /**
     * Log an exception
     * 
     * @param message
     * @parame detail
     */
    private void log(
        String message,
        Exception detail
    ){
        if(this.exceptionLog != null) {
            this.exceptionLog.print(message);
            this.exceptionLog.print('|');
            this.exceptionLog.print(detail);
            this.exceptionLog.println();
        }
    }

    /**
     * Log a message
     * 
     * @param message
     * @parame detail
     */
    private void log(
        String message,
        Object detail
    ){
        if(this.detailLog != null) {
            this.detailLog.print(message);
            if(detail != null) {
                this.detailLog.print('|');
                this.detailLog.print(new IndentingFormatter(detail));
            }
            this.detailLog.println();
        }
    }
    
    /**
     * Log a given report
     * 
     * @param url deployment unit
     * @param report
     * 
     * @return the report
     */
    private Report log(
        URL url,
        Report report
    ){
        log(url.toExternalForm(), report);
        return report;
    }

    /**
     * Log given reports
     * 
     * @param url deployment unit
     * @param reports
     * 
     * @return a list of reports
     */
    private List log(
        URL url,
        Report[] reports
    ){
        log(url.toExternalForm(), reports);
        return Arrays.asList(reports);
    }

    /**
     * Log the given URL
     * 
     * @param message the deployment type
     * @param url the deployment unit
     * 
     * @return a list of reports
     */
    private URL log(
        URL url,
        String message
    ){
        log(message, url);
        return url;
    }

}
