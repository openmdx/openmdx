/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceCapableProxyHandler_2.java,v 1.1 2008/02/19 14:20:47 hburger Exp $
 * Description: Persistence Capable Proxy Handler 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 14:20:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.object.spi;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.jdo.spi.PersistenceCapable;

import org.openmdx.compatibility.kernel.application.cci.Classes;

/**
 * Persistence Capable Proxy Handler 
 */
public class PersistenceCapableProxyHandler_2
    implements InvocationHandler, Serializable
{

    /**
     * Constructor 
     *
     * @param factory
     * @param delegate
     */
    protected PersistenceCapableProxyHandler_2(
        ProxyFactory_2_0 factory,
        Object delegate
    ){
        this.factory = factory;
        this.delegate = delegate;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3220546317611194238L;

    /**
     * The proxy's factory
     */
    private final ProxyFactory_2_0 factory;

    /**
     * The proxy's delegate
     */
    private final Object delegate;

    /**
     * The proxy's object id
     */
    private Object objectId;
    
    /**
     * Create a new proxy instance
     * 
     * @param persistenceManager
     * @param objectId 
     * @param delegate
     * 
     * @return a newly created proxy instance
     */
    static Object newProxy(
        ProxyFactory_2_0 factory,
        Object objectId, 
        Object delegate
    ){
        Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        interfaces.add(PersistenceCapable_2_0.class);
        for(
            Class<?> currentClass = delegate.getClass();
            currentClass != null;
            currentClass = currentClass.getSuperclass()
        ){
            for(Class<?> currentInterface : currentClass.getInterfaces()) {
                interfaces.add(currentInterface);
            }
        }
        return Classes.newProxyInstance(
            new PersistenceCapableProxyHandler_2(factory, delegate), 
            interfaces
        );
    }

    /**
     * Return a proxy's delegate
     * 
     * @param proxy a proxy instance
     * 
     * @return the proxy's delegate
     * 
     * @throws ClassCastException if the proxy is not an instance of
     * <code>PersistenceCapable_2_0</code>.
     */
    static Object getDelegate(
        Object proxy
    ){
        return ((PersistenceCapable_2_0)proxy).openmdxjdoGetDelegate();
    }
    
    
    //------------------------------------------------------------------------
    // Implements InvocationHandler
    //------------------------------------------------------------------------    

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        if(Object.class == declaringClass) {
            if("equals".equals(methodName)) {
                Object that = args[0];
                return 
                    that instanceof PersistenceCapable_2_0 &&
                    this.delegate.equals(((PersistenceCapable_2_0)that).openmdxjdoGetDelegate());
            } else if ("toString".equals(methodName)) {
                return new StringBuilder(
                    "Proxy for "
                ).append(
                    this.delegate
                ).toString();
            }
        } else if(PersistenceCapable.class == declaringClass) {
            if("jdoGetPersistenceManager".equals(methodName)) {
                return this.factory.getPersistenceManager();
            } else if(
                "jdoGetObjectId".equals(methodName) ||
                "jdoGetTransactionalObjectId".equals(methodName)
            ) {
                return this.objectId;
            }
        } else if(PersistenceCapable_2_0.class == declaringClass) {
            if("openmdxjdoGetDelegate".equals(methodName)) {
                return this.delegate;
            }
        }
        return this.factory.marshal(
            invoke(
                this.delegate, 
                method, 
                unmarshal(args)
            )
        );
    }
    
    /**
     * Unmarshal an array of objects
     * 
     * @param source
     * 
     * @return an array containing the unmarshalled objects
     */
    protected Object[] unmarshal(
        Object[] source
    ){
        for(
            int i = 0, l = source.length;
            i < l;
            i++
        ){ 
            Object s = source[i];
            Object t = this.factory.unmarshal(s);
            if(s != t) {
                Object[] target = new Object[source.length];
                System.arraycopy(source, 0, target, 0, i);
                target[i] = t;
                for(
                    int j = i + 1;
                    j < l;
                    j++
                ){
                    target[j] = this.factory.unmarshal(source[j]);
                }
                return target;
            }
        }
        return source;
    }

}
