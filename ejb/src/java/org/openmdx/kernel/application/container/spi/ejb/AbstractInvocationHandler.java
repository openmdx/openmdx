/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractInvocationHandler.java,v 1.4 2009/08/25 13:47:02 hburger Exp $
 * Description: Abstract Invocation Handler
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/08/25 13:47:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.LoggerFactory;

/**
 * Abstract Invocation Handler
 */
abstract class AbstractInvocationHandler implements InvocationHandler {

    /**
     * Constructor
     */
    protected AbstractInvocationHandler (
    ){
        super();
    }

    /**
     * Maps methods to actions
     */
    private final Map<Method,Action> actions = new HashMap<Method,Action>();


    /**
     * Used to prepare exception messages
     * 
     * @param arguments
     * 
     * @return the arguments' class names
     */
    protected static String[] getActualTypes(
        Object[] arguments
    ){
        if(arguments == null) {
            return null;
        } else {
            String[] classNames = new String[arguments.length];
            for(
                    int i = 0;
                    i < arguments.length;
                    i++
            ){
                classNames[i] = arguments[i] == null ? null : arguments[i].getClass().getName();
            }
            return classNames;
        }
    }


    /**
     * Class array to String array
     * 
     * @param classes
     * 
     * @return the arguments' class names
     */
    protected static String[] getFormalTypes(
        Class<?>[] classes
    ){
        String[] classNames = new String[classes.length];
        for(
                int i = 0;
                i < classes.length;
                i++
        ){
            classNames[i] = classes[i].getName();
        }
        return classNames;
    }

    /**
     * Class array to String array
     * 
     * @param classNames
     * 
     * @return the arguments' class names
     * 
     * @throws ClassNotFoundException  
     */
    protected static Class<?>[] getFormalClasses(
        String[] classNames
    ) throws ClassNotFoundException {
        Class<?>[] classes = new Class<?>[classNames.length];
        for(
                int i = 0;
                i < classNames.length;
                i++
        ){
            classes[i] = Classes.getApplicationClass(classNames[i]);
        }
        return classes;
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
        if(Object.class == method.getDeclaringClass()) {
            String methodName = method.getName();
            if ("toString".equals(methodName)) {
                Class<?>[] interfaces = proxy.getClass().getInterfaces();
                StringBuilder reply = new StringBuilder(
                    "Proxy["
                );
                if (interfaces.length > 0) {
                    String interfaceName = interfaces[
                                                      interfaces[0] == Remote.class && interfaces.length > 1 ? 1 : 0
                                                          ].getName();
                    reply.append(
                        interfaceName.substring(interfaceName.lastIndexOf('.') + 1)
                    ).append(
                        ','
                    );
                }
                return reply.append(
                    this
                ).append(
                    ']'
                ).toString();
            } else if ("hashCode".equals(methodName)) {
                return super.hashCode();
            } else if ("equals".equals(methodName)) {
                Object that = args[0];
                return proxy == that || (
                        that != null &&
                        Proxy.isProxyClass(that.getClass()) &&
                        equals(Proxy.getInvocationHandler(that))
                );
            } else throw BasicException.newStandAloneExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unexpected method for class " + Object.class,
                new BasicException.Parameter("object", this),
                new BasicException.Parameter("methodName", this)
            );
        } else {
            return getAction(method).invoke(args);
        }
    }

    /**
     * Get a method catching exceptions
     * 
     * @param interface
     * @param name
     * @param parameterTypes
     * 
     * @return the method, or <code>null</code> in case of an exception
     */
    protected static final <T> Method getMethod(
        Class<T> source,
        String name,
        Class<?>... parameterTypes
    ){
        try {
            return source.getMethod(name, parameterTypes);
        } catch (Exception exception) {
            LoggerFactory.getLogger().log(
            	Level.SEVERE,
                "Unable to acquire method '" + name + "' from class '" + source.getName() + "'",
                exception
            );
            return null;
        }        
    }

    /**
     * Retrieves and caches the action corresponding to a method
     * 
     * @param method
     * 
     * @return the action corresponding to a method
     */
    protected synchronized Action getAction(
        Method method
    ) throws BasicException {
        Action action = this.actions.get(method);
        if(action == null) this.actions.put(
            method,
            action = getAction(method.getName(), method.getParameterTypes())
        );
        return action;
    }

    /**
     * Action factory
     * 
     * @param name
     * @param argumentClasses
     * 
     * @return the requested actiona
     * 
     * @throws BasicException 
     */
    private Action getAction(
        String name,
        Class<?>[] argumentClasses
    ) throws BasicException {
        return getAction(
            name, 
            argumentClasses, 
            getFormalTypes(argumentClasses)
        );
    }

    /**
     * Action factory
     * 
     * @param methodName
     * @param argumentClasses
     * @param argumentClassNames
     * 
     * @return the requested instance method
     * 
     * @throws BasicException 
     */
	protected Action getAction(
        String methodName,
        Class<?>[] argumentClasses,
        String[] argumentClassNames
    ) throws BasicException {
        throw BasicException.newStandAloneExceptionStack(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "The requested method is not supported",
            new BasicException.Parameter("object", this),
            new BasicException.Parameter("methodName", this),
            new BasicException.Parameter("argumentClasses", (Object[])argumentClassNames)
        );
    }


    //------------------------------------------------------------------------
    // Class Action
    //------------------------------------------------------------------------

    /**
     * Action
     */
    protected static interface Action {

        /**
         * Remote method invocation
         * 
         * @param arguments
         * 
         * @return the return value
         * 
         * @throws Throwable
         */
        Object invoke(
            Object[] arguments
        ) throws Exception;

    }

}
