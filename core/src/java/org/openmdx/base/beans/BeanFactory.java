/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: BeanFactory.java,v 1.5 2009/05/27 22:49:16 hburger Exp $
 * Description: BeanFactory 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/27 22:49:16 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
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
    public BeanFactory(
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
     * 
     * @throws ServiceException 
     */
    public BeanFactory(
        String beanClassKey,
        Map<String,?> properties
    ) throws ServiceException{
        Object beanClass = getSingleton(properties.get(beanClassKey));
        if(beanClass instanceof String) try {
            this.beanClass = Classes.getApplicationClass((String)beanClass);
        } catch (ClassNotFoundException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Missing bean class",
                new BasicException.Parameter("class", beanClass)
            );
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Missing or invalid bean class entry",
                new BasicException.Parameter("beanClassKey", beanClassKey)
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
        } else {
            return source;
        }
    }
     
    /* (non-Javadoc)
     * @see org.openmdx.base.bean.Factory#instantiate()
     */
    public T instantiate(
    ) throws ServiceException {
        try {
            T instance = this.beanClass.newInstance();
            for(Map.Entry<String, ?> e: this.properties.entrySet()) {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                    e.getKey(),
                    this.beanClass
                );
                Method setter = propertyDescriptor.getWriteMethod();
                if(setter != null) {
                    Object value = e.getValue();
                    if(setter.getParameterTypes()[0].isArray()) {
                        Collection<?> values = value instanceof Collection<?> ? (Collection<?>)value : Collections.singleton(value);
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
                    }
                    setter.invoke(
                        instance, 
                        value
                    );
                }
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "Could not set property",
                        new BasicException.Parameter("beanClassName", this.beanClass.getName()),
                        new BasicException.Parameter("propertyName", e.getKey()),
                        new BasicException.Parameter("propertyValue", e.getValue())
                    );                    
                }
            }
            return instance;
        } 
        catch (Exception exception) {
            List<BasicException.Parameter> parameters = new ArrayList<BasicException.Parameter>();
            parameters.add(new BasicException.Parameter("beanClassName", beanClass.getName()));
            for(Map.Entry<String, ?> property : this.properties.entrySet()) {
                parameters.add(new BasicException.Parameter(property.getKey(), property.getValue()));
            }
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Bean instatiation failure",
                parameters.toArray(new BasicException.Parameter[parameters.size()])
            );
        }
    }

}