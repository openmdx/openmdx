/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Classes.java,v 1.23 2009/05/15 00:26:36 hburger Exp $
 * Description: Application Framework: Classes 
 * Revision:    $Revision: 1.23 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/15 00:26:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * Generic class loader access
 */
public class Classes
{ 

    /**
     * Constructor 
     */
    private Classes(
    ){
        // Avoid instantiation
    }
    
    /**
     * Retrieve the context class loader
     * 
     * @return the context class loader
     */
    private static ClassLoader getClassLoader(
    ){
        return Thread.currentThread().getContextClassLoader();
    }


    //------------------------------------------------------------------------
    // Class loading
    //------------------------------------------------------------------------
    
    /**
     * Retrieve information about the the class loaders failing to provide the given class
     * 
     * @param type The entity type
     * @param name The entity name
     * @param classLoader
     *  
     * @return <code>BasicException.Parameter</code>s
     */
    private static final BasicException.Parameter[] getInfo(
        String type,
        String name, 
        ClassLoader classLoader
    ){
        List<BasicException.Parameter> info = new ArrayList<BasicException.Parameter>();
        info.add(new BasicException.Parameter(type, name));
        try {
            int i = 0;
            for(
                ClassLoader current = classLoader;
                current != null;
                current = current.getParent(), i++
            ){
                info.add(new BasicException.Parameter("classLoader[" + i + "]", current.getClass().getName()));
                if(current instanceof URLClassLoader) {
                    int j = 0;
                    for(URL url : ((URLClassLoader)current).getURLs()) {
                        info.add(new BasicException.Parameter("url[" + i + "," + j++ + "]", url));
                    }
                }
            }
        } catch (RuntimeException ignore) {
            // just end info generation
        }
        return info.toArray(
            new BasicException.Parameter[info.size()]
        );
    }
    
    /**
     * This method may be overridden by a specific Classes instance.
     *
     * @param     name
     *            fully qualified name of the desired class
     * @param     classLoader
     *            the classLoader ot be used           
     *
     * @exception LinkageError
     *            if the linkage fails 
     * @exception ExceptionInInitializerError
     *            if the initialization provoked by this method fails 
     * @exception ClassNotFoundException
     *            if the class cannot be located by the kernel class loader
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T>  getClass(
        String name,
        ClassLoader classLoader
    ) throws ClassNotFoundException {
    	try {
	        return (Class<T>) Class.forName(
	            name,
	            true,
				classLoader
	        );
    	} catch (NoClassDefFoundError error) {
    	    throw Throwables.initCause(
    	        new NoClassDefFoundError(
        			"Could not load " + name + 
        			"; maybe it depends on another class which is " +
        			"either missing or to be found in a child class loader"
        		),
        		error,
        		BasicException.Code.DEFAULT_DOMAIN,
        		BasicException.Code.INITIALIZATION_FAILURE,
        		getInfo(
        		    "class",
        		    name, 
        		    classLoader
        		)
    		);
    	} catch (ClassNotFoundException exception) {
            throw BasicException.initHolder(
                new ClassNotFoundException(
                    "Could not load " + name,
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NO_RESOURCE,
                        getInfo(
                            "class",
                            name, classLoader
                        )
                    )
                )
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
    public static <T> Class<T> getSystemClass(
        String name
    ) throws ClassNotFoundException {
        return getClass(
            name,
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
    public static <T> Class<T> getKernelClass(
        String name
    ) throws ClassNotFoundException {
        return getClass(
            name,
            Classes.class.getClassLoader()
        );
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
        return getClass(
            name,
            getClassLoader()
        );
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
        URL resource = getClassLoader().getResource(name);
        return resource == null ? getKernelResource(name) : resource;
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
     * @throws IOException 
     */
    public static URL getRequiredResource(
        String name
    ) throws IOException{
        URL resource = getApplicationResource(name);
        if(resource == null) {
    	    throw Throwables.initCause(
    	    	new FileNotFoundException("Could not locate resource " + name),
    	    	null,
        		BasicException.Code.DEFAULT_DOMAIN,
        		BasicException.Code.INITIALIZATION_FAILURE,
        		getInfo(
        		    "resource",
        		    name, 
        		    getClassLoader()
        		)
    		);
        }
        return resource == null ? getKernelResource(name) : resource;
    }

    

    /**
     * Load a package local resource
     *
     * @param     sibling
     *            a class in the same package
     * @param     simpleName
     *            the unqualified resource name
     *
     * @return    a URL for reading the resource,
     *            or <code>null</code> if the resource could not be found or
     *            the caller doesn't have adequate privileges to get the
     *            resource.
     */
    public static URL getPackageResource(
    	Class<?> sibling,
        String simpleName
    ){
    	String qualifiedClassName = sibling.getName();
    	return getApplicationResource(
    		qualifiedClassName.substring(0, qualifiedClassName.length() - sibling.getSimpleName().length()).replace('.', '/') + simpleName
    	);
    }
    
    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     *
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param  name
     *         The resource name
     *
     * @return  An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
     *          the resource.  If no resources could  be found, the enumeration
     *          will be empty.  Resources that the class loader doesn't have
     *          access to will not be in the enumeration.
     *
     * @throws  IOException
     *          If I/O errors occur
     */
    public static Enumeration<URL> getResources(String name) throws IOException {
        return getClassLoader().getResources(name);
    }

    
    //------------------------------------------------------------------------
    // Proxy Factory
    //------------------------------------------------------------------------
    
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
    public static Object newProxyInstance(
        InvocationHandler invocationHandler,
        Class<?>... interfaces
    ){
        return Proxy.newProxyInstance(
            getClassLoader(),
            interfaces,
            invocationHandler
        );
    }
    
    /**
     * Combine the interfaces
     * 
     * @param append
     * @param prepend
     * 
     * @return the combined interfaces
     */
    public static Class<?>[] combineInterfaces(
        Set<Class<?>> append,
        Class<?>... prepend
    ){
        if(append.isEmpty()) {
            return prepend;
        } else {
            Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>(
                Arrays.asList(prepend)
            );
            interfaces.addAll(append);
            return interfaces.toArray(
                new Class<?>[interfaces.size()]
            );
            
        }
    }

    /**
     * Retrieve the interfaces implemented by an object class 
     * 
     * @param objectClass
     * 
     * @return the interfaces implemented by the given object class
     */
    public static Set<Class<?>> getInterfaces(
        Class<?> objectClass
    ){
        Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        for(
            Class<?> currentClass = objectClass;
            currentClass != null;
            currentClass = currentClass.getSuperclass()
        ){
            for(Class<?> currentInterface : currentClass.getInterfaces()) {
                interfaces.add(currentInterface);
            }
        }
        return interfaces;
    }
    
    
    //------------------------------------------------------------------------
    // Instance Factory
    //------------------------------------------------------------------------
    
    /**
     * Create a new application class instance 
     * 
     * @param interfaceClass
     * @param className
     * @param arguments
     * 
     * @return a new Instance
     * 
     * @throws ClassNotFoundException
     * @throws IllegalArgumentException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    public static <T> T newApplicationInstance(
        Class<T> interfaceClass,
        String className,
        Object... arguments
    ) throws ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> instanceClass = getApplicationClass(className);
        if(interfaceClass.isAssignableFrom(instanceClass)) { 
            int argumentCount = arguments == null ? 0 : arguments.length;
            Constructors: for(Constructor<?> constructor: instanceClass.getConstructors()) {
                Class<?>[] parameters = constructor.getParameterTypes();
                int parameterCount = parameters == null ? 0 : parameters.length;
                if(argumentCount == parameterCount) {
                    for(
                        int i = 0;
                        i < argumentCount;
                        i++
                    ){
                        if(
                            arguments[i] != null && 
                            !parameters[i].isInstance(arguments[i]) 
                        ) continue Constructors;
                    }
                    return (T) constructor.newInstance(arguments);
                }
            }
            throw new IllegalArgumentException(
                className + " has no constructor for the given arguments"
            );
        } else {
            throw new ClassCastException (
                className + " is not an instance of " + interfaceClass.getName()
            );
        }
    }

}
