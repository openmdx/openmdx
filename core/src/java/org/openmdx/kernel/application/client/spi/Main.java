/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Main.java,v 1.3 2005/09/16 00:46:09 hburger Exp $
 * Description: Main Class Wrapper
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/09/16 00:46:09 $
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
package org.openmdx.kernel.application.client.spi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Main class wrapper
 */
public class Main 
	implements Runnable
{

    /**
     * Constructor
     * 
     * @param memthod
     * @param arguments
     */
    public Main(
        Method method,
        String[] arguments,
        ClassLoader classLoader
    ) {
        this.method = method;
        this.arguments = arguments;
        this.classLoader = classLoader;
    }

    /**
     * 
     */
    private final Method method;
    
    /**
     * 
     */
    private final String[] arguments;

    /**
     * 
     */
    private final ClassLoader classLoader;
    
    
    //------------------------------------------------------------------------
    // Implements Runnable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        Thread thread = Thread.currentThread();
        ClassLoader callerClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(this.classLoader);
        try {
            method.invoke(null, new Object[]{arguments});
        } catch (RuntimeException exception) {
            throw exception;
        } catch (IllegalAccessException exception) {
            throw new UndeclaredThrowableException(
                 exception,
                 "Method invocation failed"
            );
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getTargetException(); 
            if(cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new UndeclaredThrowableException(
                    exception,
                    "Method execution failed"
               );
            }
        } finally {
            thread.setContextClassLoader(callerClassLoader);
        }
    }

}
