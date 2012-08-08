/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LenientBinder.java,v 1.1 2008/03/13 17:16:04 hburger Exp $
 * Description: Lenient Binder
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/13 17:16:04 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.slf4j.helpers;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * The lenient binder uses a fallback for all classloaders for which the 
 * standard binding fails.
 */
public abstract class LenientBinder<D,B> {

    /**
     * Constructor 
     */
    protected LenientBinder(
        String staticBinderName
    ) {
        this.staticBinderName = staticBinderName;
        this.staticDelegate = getStandardDelegate(getClass().getClassLoader());
        if(this.staticDelegate == null) {
            this.fallbackDelegate = getFallbackDelegate();
            this.dynamicDelegates = new WeakHashMap<ClassLoader,D>();
        } else {
            this.fallbackDelegate = null;
            this.dynamicDelegates = null;
        }
    }

    /**
     * The static binder should be loaded dynamically
     */
    private final String staticBinderName;
    
    /**
     * The static delegate
     */
    private final D staticDelegate;
    
    /**
     * Class loader dependent delegates
     */
    private final Map<ClassLoader,D> dynamicDelegates;
    
    /**
     * If no static binder is visible to a given class loader
     */
    private final D fallbackDelegate;

    /**
     * Ask the binder for the delegate
     * 
     * @param binder
     * 
     * @return the standard delegate
     */
    abstract protected D getStandardDelegate(
        B binder
    ) throws Exception;

    /**
     * Ask for the fallback delegate
     * 
     * @return the fallback delegate
     */
    abstract protected D getFallbackDelegate(
    );
    
    @SuppressWarnings("unchecked")
    private D getStandardDelegate(
        ClassLoader classLoader
    ){
        try {
            Class<B> binderClass = (Class<B>) Class.forName(
                this.staticBinderName, // name
                true, // initialize, 
                classLoader
            );
            B binderInstance = (B) binderClass.getField(
                "SINGLETON"
            ).get(
                null // static
            );
            return getStandardDelegate(
                binderInstance
            );
        } catch (ClassNotFoundException ignore) {
            return null;
        } catch (Exception exception) {
            Util.reportFailure(
                "Could not retrieve the field \"" + this.staticBinderName + ".SINGLETON\"",
                exception
            );
            return null;
        }
    }

    /**
     * Return this object and the standard delegate in case of dynamic 
     * and static binding, respectively. 
     * 
     * @return this object in case of dynamic binding, the standard delegate
     * in case of static binding.
     */
    @SuppressWarnings("unchecked")
    public D narrow(){
        return this.staticDelegate == null ? (D)this : this.staticDelegate;
    }
    
    /**
     * Retrieve the class loader dependent delegate
     * 
     * @return the class loader dependent delegate
     */
    protected final D getDelegate() {
        if(this.staticDelegate == null) {
            ClassLoader classLoader = getClassLoader();
            synchronized(this.dynamicDelegates) {
                D delegate = this.dynamicDelegates.get(classLoader);
                if(delegate == null) {
                    delegate = getStandardDelegate(classLoader);
                    if(delegate == null) delegate = this.fallbackDelegate;
                    this.dynamicDelegates.put(classLoader, delegate);
                }
                return delegate;
            }
        } else {
            return this.staticDelegate;
        }
    }

    /** 
     * Get the context class loader associated with the current thread. 
     * This is done in a doPrivileged block because it is a secure method.
     * @return the current thread's context class loader, or this class'
     * loader if there is no context class loader set. 
     */
    private static ClassLoader getClassLoader() {
        return AccessController.doPrivileged(
            new PrivilegedAction<ClassLoader> () {
                public ClassLoader run () {
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); 
                    return classLoader == null ? 
                        LenientBinder.class.getClassLoader() : 
                        classLoader;
                }
            }
        );
    }

}
