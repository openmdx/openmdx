/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ClassLoaderAwareDelegator.java,v 1.2 2007/12/27 18:37:42 hburger Exp $
 * Description: ClassLoaderAwareMarkerFactory 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/27 18:37:42 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.slf4j.lenient;

import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.Util;

/**
 * Class Loader Aware Delegator
 */
abstract class ClassLoaderAwareDelegator {

    /**
     * Constructor 
     */
    protected ClassLoaderAwareDelegator() {
        super();
    }

    /**
     * Class loader dependent delegates
     */
    private final Map delegates = new WeakHashMap();
    
    /**
     * If no static binder is visible to a given class loader
     */
    private Object fallback = null;
    

    /**
     * The static binder should be loaded dynamically
     * 
     * @return the binder's class name
     */
    abstract protected String getStaticBinderName();
    
    /**
     * Ask the binder for the delegate
     * 
     * @param binder
     * 
     * @return the standard delegate
     */
    abstract protected Object getStandardDelegate(
        Class binderClass,
        Object binderInstance
    ) throws Exception;

    /**
     * Ask for the fallback delegate
     * 
     * @return the fallback delegate
     */
    abstract protected Object getFallbackDelegate(
    );
    
    /**
     * Retrieve the delegator's logger
     * 
     * @return the logger
     */
    private final Logger getLogger(
    ){
        return this.fallback instanceof ILoggerFactory ? 
            ((ILoggerFactory) this.fallback).getLogger(getClass().getName()) :
             LoggerFactory.getLogger(getClass());
    }

    /**
     * Retrieve the class loader dependent delegate
     * 
     * @return the class loader dependent delegate
     */
    protected final Object getDelegate() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        synchronized(this.delegates) {
            Object delegate = this.delegates.get(classLoader);
            if(delegate == null) {
                try {
                    Class binderClass = Class.forName(
                        getStaticBinderName(), // name
                        true, // initialize, 
                        classLoader
                    );
                    Object binderInstance = binderClass.getField(
                        "SINGLETON"
                    ).get(
                        null // static
                    );
                    delegate = getStandardDelegate(
                        binderClass,
                        binderInstance
                    );
                } catch (Exception ignore) {
                    if(this.fallback == null) try {
                        this.fallback = getFallbackDelegate();
                        getLogger().info(
                            "Use {} as {} is unavailable to SLF4J's current classloader", 
                            this.fallback.getClass().getName(),
                            getStaticBinderName()
                        );
                    } catch (RuntimeException exception) {
                        Util.reportFailure(
                            "Neither \"" + getStaticBinderName() + "\" nor its fallback could be acquired", 
                            exception
                        );
                        throw exception;
                    } catch (Error error) {
                        Util.reportFailure(
                            "Neither \"" + getStaticBinderName() + "\" nor its fallback could be acquired", 
                            error
                        );
                        throw error;
                    }
                    delegate = this.fallback;
                }
                this.delegates.put(classLoader, delegate);
            }
            return delegate;
        }
    }

}
