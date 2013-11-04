/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Bean Factory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.loading;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.kernel.exception.BasicException;

/**
 * Bean Factory
 */
public class BeanFactory<T> implements Factory<T> {

    /**
     * Constructor 
     *
     * @param beanClass
     * @param properties
     */
    private BeanFactory(
        Class<T> beanClass,
        Map<String,?> properties
    ){
        this.beanClass = beanClass;
        this.properties = properties;
    }

    /**
     * Constructor 
     *
     * @param beanClassKey
     * @param properties
     */
    public BeanFactory(
        String beanClassKey,
        Map<String,?> properties
    ){
        Object beanClassName = getSingleton(properties.get(beanClassKey));
        if(beanClassName instanceof String) try {
            this.beanClass = Classes.getApplicationClass((String)beanClassName);
        } catch (ClassNotFoundException exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Missing bean class",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE,
                        new BasicException.Parameter("classKey", beanClassKey),
                        new BasicException.Parameter("class", beanClassName)
                    )
                )
            );
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Missing or invalid bean class entry",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("classKey", beanClassKey),
                        new BasicException.Parameter("class", beanClassName)
                    )
                )
            );
        }
        this.properties = new HashMap<String,Object>(properties);
        this.properties.remove(beanClassKey);
    }
    
    /**
     * 
     */
    private final Class<T> beanClass;

    /**
     * 
     */
    private final Map<String,?> properties;

    /**
     * The eagerly acquired Java bean introspector
     */
    private static final BeanIntrospector introspector = Classes.newPlatformInstance(
        "org.openmdx.base.beans.StandardBeanIntrospector",
        BeanIntrospector.class
    );

    /**
     * Create a factory for the given class
     * 
     * @param beanClass
     * @param properties
     * 
     * @return a factory for the given class
     */
    public static <T> Factory<T> newInstance(
        Class<T> beanClass,
        Map<String,?> properties
    ){
        return new BeanFactory<T>(beanClass, properties);
    }

    /**
     * Create a factory for the given class
     * 
     * @param beanClassName
     * @param properties
     * 
     * @return a factory for the given class
     */
    public static <T> Factory<T> newInstance(
        String beanClassName,
        Map<String,?> properties
    ){
        try {
            return newInstance(
                Classes.<T>getApplicationClass(beanClassName), 
                properties
            );
        } catch (ClassNotFoundException exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Missing bean class",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE,
                        new BasicException.Parameter("class", beanClassName)
                    )
                )
            );
        }
    }

    /**
     * Retrieve a single value
     * 
     * @param source
     * 
     * @return the first or only value
     */
    private static Object getSingleton(
        Object source
    ){
        if(source instanceof Collection<?>) {
            for(Object value : (Collection<?>)source){
                return value;
            }
            return null;
        } else if(source instanceof Map<?,?>) {
            for(Object value : ((Map<?,?>)source).values()){
                return value;
            }
            return null;
        } else {
            return source;
        }
    }
     
    /* (non-Javadoc)
     * @see org.openmdx.base.bean.Factory#instantiate()
     */
    public T instantiate(
    ){
        try {
            T instance = this.beanClass.newInstance();
            for(Map.Entry<String, ?> e: this.properties.entrySet()) {
                Method setter = introspector.getPropertyModifier(this.beanClass, e.getKey()); 
                Object value = e.getValue();
                if(setter.getParameterTypes()[0].isArray()) {
                    Collection<?> values = 
                        value instanceof Collection<?> ? (Collection<?>)value :
                        value instanceof Map<?,?> ? ((Map<?,?>)value).values() :
                        Collections.singleton(value);
                    value = Array.newInstance(
                        setter.getParameterTypes()[0].getComponentType(),
                        values.size()
                    );
                    int i = 0;
                    for(Object v : values) {
                        Array.set(value, i++, v);
                    }
                } else {
                    if(value instanceof Collection<?>) {
                        value = ((Collection<?>)value).iterator().next();
                    }
                    else if(value instanceof Map<?,?>) {
                        value = ((Map<?,?>)value).values().iterator().next();
                    }
                }
                setter.invoke(
                    instance, 
                    value
                );
            }
            return instance;
        }  catch (Exception exception) {
            List<BasicException.Parameter> parameters = new ArrayList<BasicException.Parameter>();
            parameters.add(new BasicException.Parameter("beanClassName", beanClass.getName()));
            for(Map.Entry<String, ?> property : this.properties.entrySet()) {
                parameters.add(new BasicException.Parameter(property.getKey(), property.getValue()));
            }
            throw BasicException.initHolder(
                new RuntimeException(
                    "Bean instatiation failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE,
                        parameters.toArray(new BasicException.Parameter[parameters.size()])
                    )
                )
            );
        }
    }

    /**
     * Get the bean instances' class
     * 
     * @return the bean instances' class
     */
    public Class<T> getInstanceClass(){
        return this.beanClass;
    }

}