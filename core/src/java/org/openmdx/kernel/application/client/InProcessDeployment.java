/*
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InProcessDeployment.java,v 1.4 2006/10/30 15:00:06 hburger Exp $
 * Description: In-Process Deployment
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/10/30 15:00:06 $
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
package org.openmdx.kernel.application.client;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;

/**
 * In-Process Deployment
 * 
 * @deprecated in favour of org.openmdx.base.application.deploy.InProcessDeployment
 */
public class InProcessDeployment implements Runnable {

    /**
     * Constructor
     * 
     * @param classes
     * @param values
     */
    private InProcessDeployment(
        Class[] classes,
        Object[] values
    ){
        try {
            this.delegate = Classes.getApplicationClass(
                "org.openmdx.base.application.deploy.InProcessDeployment"
            ).getConstructor(
                classes
            ).newInstance(
                values
            );
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UndeclaredThrowableException(exception);
        }
    }

    /**
     * Constructor
     * 
     * @param connector
     * @param application
     * @param detailLog
     * @param exceptionLog
     * 
     * @deprecated in favour of org.openmdx.base.application.deploy.InProcessDeployment#InProcessDeployment(String,String,PrintStream,PrintStream)
     */
    public InProcessDeployment(
        String connector,
        String application,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(
            new Class[]{
                String.class, 
                String.class,
                PrintStream.class, 
                PrintStream.class
            },
            new Object[]{
                connector,
                application,
                detailLog,
                exceptionLog
            }
        );
    }

    /**
     * Constructor
     * 
     * @param connectors
     * @param applications
     * @param detailLog
     * @param exceptionLog
     * 
     * @deprecated in favour of org.openmdx.base.application.deploy.InProcessDeployment#InProcessDeployment(String[],String[],PrintStream,PrintStream)
     */
    public InProcessDeployment(
        String[] connectors,
        String[] applications,
        PrintStream detailLog,
        PrintStream exceptionLog
    ){
        this(
            new Class[]{
                String[].class, 
                String[].class,
                PrintStream.class, 
                PrintStream.class
            },
            new Object[]{
                connectors,
                applications,
                detailLog,
                exceptionLog
            }
        );
    }

    /**
     * Constructor
     * 
     * @param connectors
     * @param applications
     * @param logWriter
     * @param exceptionLog
     * 
     * @deprecated in favour of org.openmdx.base.application.deploy.InProcessDeployment#InProcessDeployment(URL[],URL[],PrintStream,PrintStream)
     */
    public InProcessDeployment(
        URL[] connectors,
        URL[] applications,
        PrintStream logWriter,
        PrintStream exceptionLog
    ){
        this(
            new Class[]{
                URL[].class, 
                URL[].class,
                PrintStream.class, 
                PrintStream.class
            },
            new Object[]{
                connectors,
                applications,
                logWriter,
                exceptionLog
            }
        );
    }
    
    /**
     * The non-deprecated version
     */
    final Object delegate;

    /**
     * 
     */
    private BasicException exception = null;
    
    /**
     * Test whether the deployment was successful 
     * 
     * @return <code>true</code> if the deployment was successful
     */
    public boolean isSuccess (            
    ){
        run();
        return this.exception == null;
    }
    
    /**
     * Retrieve the Exeption in case of a failure
     * 
     * @return the exception or <code>null</code> in case of success
     */
    public BasicException getException (
    ){
        run();
        return this.exception;
    }

    
    //------------------------------------------------------------------------
    // Implements Runnable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     * 
     * @deprecated in favour of org.openmdx.base.application.deploy.InProcessDeployment#context()
     */
    public void run(
    ){
        try {
            this.delegate.getClass().getMethod(
                "context", 
                (Class[])null
            ).invoke(
                this.delegate, 
                (Object[])null
            );
        } catch (InvocationTargetException exception) {
            this.exception = BasicException.toStackedException(
                exception.getTargetException(),
                exception
            );
        } catch (Exception exception) {
            this.exception = BasicException.toStackedException(
                exception
            );
        }
    }

}
