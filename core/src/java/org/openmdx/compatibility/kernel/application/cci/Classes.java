/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Classes.java,v 1.10 2008/02/19 13:44:37 hburger Exp $
 * Description: Application Framework: Classes 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:44:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.kernel.application.cci;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Collection;

/**
 * Generic class loader access
 */
public class Classes
{ 

    private Classes(
    ){
        // Avoid instantiation
    }
    
    /**
     * 
     * @return
     */
    private static ClassLoader getClassLoader(
    ){
        return Thread.currentThread().getContextClassLoader();
    }


    //------------------------------------------------------------------------
    // Class loading
    //------------------------------------------------------------------------
    
    /**
     * This method may be overridden by a specific Classes instance.
     *
     * @param     name
     *            fully qualified name of the desired class
     *
     * @exception LinkageError
     *            if the linkage fails 
     * @exception ExceptionInInitializerError
     *            if the initialization provoked by this method fails 
     * @exception ClassNotFoundException
     *            if the class cannot be located by the kernel class loader
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T>  findApplicationClass(
        String name
    ) throws ClassNotFoundException {
		ClassLoader classloader = getClassLoader(); 
    	try {
	        return (Class<T>) Class.forName(
	            name,
	            true,
				classloader
	        );
    	} catch (NoClassDefFoundError error) {
    		throw new NoClassDefFoundError(
    			"An error occured when attempting to load '" + name + 
				"' from " + classloader + ",\nhint=\"Maybe '" + name +
				"' depends on another class which is either missing " +
				"or to be found in a child class loader\",\ncause=" +
 				error   
    		);
    	} catch (ClassNotFoundException exception) {
    		throw new ClassNotFoundException(
    			"An error occured when attempting to load '" + name + 
				"' from " + classloader + ",\ncause=" +
 				exception   
    		);
    	}
    }
    
    /**
     * Load a system class
     *
     * @param     name
     *            fully qualified name of the desired class
     *
     * @return    the Class object for the class with the specified name. 
     *
     * @exception LinkageError
     *            if the linkage fails 
     * @exception ExceptionInInitializerError
     *            if the initialization provoked by this method fails 
     * @exception ClassNotFoundException
     *            if the class cannot be located by the kernel class loader
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getSystemClass(
        String name
    ) throws ClassNotFoundException {
        return (Class<T>) Class.forName(
            name,
            true,
            ClassLoader.getSystemClassLoader()
        );
    }
    
    /**
     * Load a kernel class
     *
     * @param     name
     *            fully qualified name of the desired class
     *
     * @return    the Class object for the class with the specified name. 
     *
     * @exception LinkageError
     *            if the linkage fails 
     * @exception ExceptionInInitializerError
     *            if the initialization provoked by this method fails 
     * @exception ClassNotFoundException
     *            if the class cannot be located by the kernel class loader
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getKernelClass(
        String name
    ) throws ClassNotFoundException {
        return (Class<T>) Class.forName(name);
    }
    
    /**
     * Load an application class
     *
     * @param     name
     *            fully qualified name of the desired class
     *
     * @return    the Class object for the class with the specified name. 
     *
     * @exception LinkageError
     *            if the linkage fails 
     * @exception ExceptionInInitializerError
     *            if the initialization provoked by this method fails 
     * @exception ClassNotFoundException
     *            if the class cannot be located by the kernel class loader
     */
    public static <T> Class<T>  getApplicationClass(
        String name
    ) throws ClassNotFoundException {
        return findApplicationClass(name);
    }
    
    
    //------------------------------------------------------------------------
    // Resource loading
    //------------------------------------------------------------------------
    
    /**
     * This method may be overridden by a specific Classes instance.
     *
     * @param     name
     *            fully qualified name of the desired resource
     *
     * @return    a URL for reading the resource,
     *            or <code>null</code> if the resource could not be found or
     *            the caller doesn't have adequate privileges to get the
     *            resource.
     */
    private static URL findApplicationResource(
        String name
    ){
        return getClassLoader().getResource(name);
    }
    
    /**
     * Load a system resource
     *
     * @param     name
     *            fully qualified name of the desired resource
     *
     * @return    a URL for reading the resource,
     *            or <code>null</code> if the resource could not be found or
     *            the caller doesn't have adequate privileges to get the
     *            resource.
     */
    public static URL getSystemResource(
        String name
    ){
        return ClassLoader.getSystemResource(name);
    }
    
    /**
     * Load a kernel resource
     *
     * @param     name
     *            fully qualified name of the desired resource
     *
     * @return    a URL for reading the resource,
     *            or <code>null</code> if the resource could not be found or
     *            the caller doesn't have adequate privileges to get the
     *            resource.
     */
    public static URL getKernelResource(
        String name
    ){
        URL resource = Classes.class.getResource(name);
        return resource == null ? getSystemResource(name) : resource;
    }
    
    /**
     * Load an application resource
     *
     * @param     name
     *            fully qualified name of the desired resource
     *
     * @return    a URL for reading the resource,
     *            or <code>null</code> if the resource could not be found or
     *            the caller doesn't have adequate privileges to get the
     *            resource.
     */
    public static URL getApplicationResource(
        String name
    ){
        URL resource = findApplicationResource(name);
        return resource == null ? getKernelResource(name) : resource;
    }

    /**
     * Create a new proxy instance
     * 
     * @param invocationHandler
     * @param interfaces
     * 
     * @return a newly created proxy instance
     * 
     * @throws  IllegalArgumentException if any of the restrictions on the
     *      parameters that may be passed to <code>getProxyClass</code>
     *      are violated
     * @throws  NullPointerException if the <code>interfaces</code> array
     *      argument or any of its elements are <code>null</code>, or
     *      if the invocation handler, <code>invocationHandler</code>, is
     *      <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(
        InvocationHandler invocationHandler,
        Class<?>... interfaces
    ){
        return (T) Proxy.newProxyInstance(
            getClassLoader(),
            interfaces,
            invocationHandler
        );
    }
    
    /**
     * Create a new proxy instance
     * 
     * @param invocationHandler
     * @param interfaces
     * 
     * @return a newly created proxy instance
     * 
     * @throws  IllegalArgumentException if any of the restrictions on the
     *      parameters that may be passed to <code>getProxyClass</code>
     *      are violated
     * @throws  NullPointerException if the <code>interfaces</code> array
     *      argument or any of its elements are <code>null</code>, or
     *      if the invocation handler, <code>invocationHandler</code>, is
     *      <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public static <T> T newProxyInstance(
        InvocationHandler invocationHandler,
        Collection<Class<?>> interfaces
    ){
        return (T) Proxy.newProxyInstance(
            getClassLoader(),
            interfaces.toArray(new Class<?>[interfaces.size()]),
            invocationHandler
        );
    }

}
