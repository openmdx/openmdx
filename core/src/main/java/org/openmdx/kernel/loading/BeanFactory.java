/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Bean Factory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Bean Factory
 * <p>
 * The following configuration entry names are reserved<ul>
 * <li><code>class</code>, the class to be instantiated
 * <li><code>interface</code>, the class to be exposed by the factory
 * </ul>
 * <p>
 * The instance is treated as java bean, i.e. it is instantiated via
 * default constructor and the configuration is applied through its
 * setters.
 */
public class BeanFactory<T> implements Factory<T> {

	/**
     * Constructor 
     *
     * @param declaredClass the declared class, often an interface
     * @param actualClass the Java bean class
     * @param configuration the configuration to be kept by the bean factory
     */
    protected BeanFactory(
    	Class<T> declaredClass,	
        Class<? extends T> actualClass,
        Configuration configuration
    ){
        this.declaredClass = declaredClass;
        this.actualClass = actualClass;
        this.configuration = configuration;
    }

    /**
     * Constructor 
     *
     * @param beanClass the Java Bean and instance class
     * @param settings the settings to be kept by the bean factory
     */
    private BeanFactory(
        Class<T> beanClass,
        Configuration configuration
    ){
    	this(beanClass, beanClass, configuration);
    }
    
    /**
     * The instance class
     */
    protected final Class<T> declaredClass;
    
    /**
     * The Java Bean class
     */
    protected final Class<? extends T> actualClass;

    /**
     * The configuration is applied to the Java Bean
     */
    protected final Configuration configuration;

    /**
     * The class configuration entries are not propagated to the Java Bean.
     */
    private final static Collection<String> RESERVED_ENTRIES = Arrays.asList(
    	"class",
    	"interface"
	);
    
    /**
     * The eagerly acquired Java bean introspector
     */
    private static final BeanIntrospector introspector = Classes.newPlatformInstance(
        "org.openmdx.base.beans.StandardBeanIntrospector",
        BeanIntrospector.class
    );

    /**
     * Get the bean instances' class
     * 
     * @return the bean instances' class
     */
    public Class<T> getInstanceClass(){
    	return this.declaredClass;
    }
    
    /**
     * Create a factory for the given class.
     * <p>
     * The class names are retrieved from the following configuration keys<ul>
     * <li><code>"class"</code> <em>(mandatory)</em>
     * <li><code>"interface"</code> <em>(optional)</em>
     * </ul>
     * 
     * @param configuration the Java Bean Factory configuration includes the class name(s)
     * 
     * @return a factory for the given class
     */
    public static Factory<?> newInstance(
		Configuration configuration
	){
    	final Class<Object> actualClass = getClass(configuration, "class");
    	return getOptionalClass(
    	    configuration, 
    	    "interface"
    	).map(
    	    declaredClass -> new BeanFactory<Object>(
                declaredClass,
                actualClass,
                configuration
            )
    	).orElseGet(
    	    () -> new BeanFactory<Object>(
                actualClass,
                configuration
            )
    	);
    }

    
    /**
     * Create a factory for the given class.
     * <p>
     * The bean class name is retrieved from the following configuration key<ul>
     * <li><code>"class"</code> <em>(mandatory)</em>
     * </ul>
     * 
     * @param instanceClass the instance class
     * @param configuration the Java Bean Factory configuration
     * 
     * @return a factory for the given class
     */
    public static <T> Factory<T> newInstance(
    	Class<T> instanceClass,
		Configuration configuration
	){
        return new BeanFactory<T>(
		    instanceClass,
		    BeanFactory.<T>getClass(configuration, "class"),
		    configuration
		);
    }
    
    /**
     * Create a factory for the given class.
     * <p>
     * The bean class name is retrieved from the following configuration key<ul>
     * <li><code>"class"</code> <em>(mandatory)</em>
     * </ul>
     * 
     * @param declaredClass the instance class
     * @param configuration the Java Bean Factory configuration
     * @param defaultClass the default class if the configured class can't be found
     * 
     * @return a factory for the given class
     */
    public static <T, D extends T> Factory<T> newInstance(
        Class<T> declaredClass,
        Configuration configuration,
        Class<D> defaultClass
    ){
        return new BeanFactory<T>(
            declaredClass,
            BeanFactory.<D>getOptionalClass(configuration, "class").orElse(defaultClass),
            configuration
        );
    }
    
    /**
     * Create a factory for the given Java Bean class
     * 
     * @param beanClassName
     * @param properties
     * 
     * @return a factory for the given class
     */
    public static Factory<?> newInstance(
        String beanClassName,
        Map<String, ?> properties
    ){
        return newInstance(
        	Object.class,
        	beanClassName, 
            Configurations.getBeanConfiguration(properties)
        );
    }

    /**
     * Create a factory for the given Java Bean class
     * 
     * @param instanceClass
     * @param beanClassName
     * @param configuration the Java Bean settings
     * 
     * @return a factory for the given class
     */
    @SuppressWarnings("unchecked")
	protected static <T> Factory<T> newInstance(
    	Class<T> instanceClass,
        String beanClassName,
        Configuration configuration
    ){
        return new BeanFactory<T>(
		    instanceClass,
		    (Class<? extends T>) BeanFactory.getClass("class", beanClassName), 
		    configuration
		);
    }

    /**
     * Create a factory for the given Java Bean class
     * 
     * @param instanceClass
     * @param beanClassName
     * @param configuration the Java Bean configuration
     * 
     * @return a factory for the given class
     */
    public static <T> Factory<T> newInstance(
    	Class<T> instanceClass,
        String beanClassName,
        Map<String,?> configuration
    ){
    	return newInstance(
    		instanceClass, 
    		beanClassName, 
            Configurations.getBeanConfiguration(configuration)
    	);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.bean.Factory#instantiate()
     */
    @Override
    public T instantiate(
    ){
        try {
        	return build();
        }  catch (Exception exception) {
            final List<BasicException.Parameter> parameters = new ArrayList<BasicException.Parameter>();
            parameters.add(new BasicException.Parameter("class", actualClass.getName()));
            for(String key : this.configuration.singleValuedEntryNames()) {
                parameters.add(new BasicException.Parameter(key, configuration.getOptionalValue(key, Object.class).orElse(null)));
            }
            for(String key : this.configuration.multiValuedEntryNames()) {
                parameters.add(new BasicException.Parameter(key, configuration.getSparseArray(key, Object.class)));
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

	protected T build(
	) throws Exception {
		final T javaBeanInstance = this.actualClass.newInstance();
		populateJavaBean(javaBeanInstance);
		return javaBeanInstance;
	}

	/**
	 * @param instance
	 * @throws IllegalAccessExceptionunderbirdrd
	 * @throws InvocationTargetException
	 */
	private void populateJavaBean(
		T instance
	) throws IllegalAccessException, InvocationTargetException {
		for(String entryName: this.configuration.singleValuedEntryNames()) {
			setPropertyLeniently(instance, entryName);
		}
		for(String entryName: this.configuration.multiValuedEntryNames()) {
			setPropertyLeniently(instance, entryName);
		}
	}

	private void setPropertyLeniently(
		T instance,
		String entryName
	) throws IllegalAccessException, InvocationTargetException {
		if(!RESERVED_ENTRIES.contains(entryName)) {
			final Method setter = introspector.getPropertyModifier(this.actualClass, entryName); 
			final Class<?> parameterType = setter.getParameterTypes()[0];
			if(parameterType.isArray()) {
	            setter.invoke(instance, toArray(entryName, parameterType));
			} else if(parameterType == SparseArray.class) {
			    final Class<?> elementType = getElementType(setter.getGenericParameterTypes()[0]);
			    if(this.configuration.multiValuedEntryNames().contains(entryName)) {
			        setter.invoke(instance, this.configuration.getSparseArray(entryName, elementType));
			    } else if(this.configuration.singleValuedEntryNames().contains(entryName)){ 
			        final Optional<?> optionalValue = this.configuration.getOptionalValue(entryName, elementType);
			        if(optionalValue.isPresent()) {
			            setter.invoke(SortedMaps.singletonSparseArray(optionalValue.get()));
			        }
			    }
			} else {
			    final Optional<?> optionalValue = toSingleValue(entryName, parameterType);
                if(optionalValue.isPresent()) {
                    setter.invoke(instance, optionalValue.get());
                }
			}
		}
	}

    private Class<?> getElementType(
        Type genericParameterType
    ) {
        final Type[] actualTypeArguments = ((ParameterizedType)genericParameterType).getActualTypeArguments();
        if(actualTypeArguments != null && actualTypeArguments.length > 0) {
            final Type elementTypeArgument = actualTypeArguments[0];
            if(elementTypeArgument instanceof Class<?>) {
                return (Class<?>) elementTypeArgument;
            } else {
                try {
                    return Class.forName(elementTypeArgument.toString().split(" ")[1]);
                } catch(Exception ignore) {
                    return String.class; // default
                }
            }
        } else {
            return String.class; // default
        }
    }

	/**
	 * Retrieve the value for the setter
	 * 
	 * @param parameterName the name of the parameter corresponds to its configuration entry 
	 * @param parameterType the parameter type, a primitive or object type
	 * 
	 * @return the single-valued parameter value
	 */
	private Optional<?> toSingleValue(
		String parameterName, 
		Class<?> parameterType
	) {
		final Class<?> elementType = Classes.toObjectClass(parameterType);
		final Optional<?> optionalValue = configuration.getOptionalValue(
			parameterName, 
			elementType
		);
		if(!optionalValue.isPresent() && configuration.multiValuedEntryNames().contains(parameterName)) {
		    final SparseArray<?> values = configuration.getSparseArray(parameterName, elementType);
		    if(!values.isEmpty()) {
		        return Optional.ofNullable(values.get(0));
		    }
		}
        return optionalValue;
	}

	/**
	 * Retrieve the value for the setter
	 * 
	 * @param parameterName the name of the parameter corresponds to its configuration entry 
	 * @param parameterType the parameter type, an array type
	 * 
	 * @return the multi-valued parameter value
	 */
	private Object toArray(
		String parameterName, 
		Class<?> parameterType
	) {
		return toArray(
			parameterType, 
			configuration.getSparseArray(
				parameterName, 
				Classes.toObjectClass(
					parameterType.getComponentType()
				)
			)
		);
	}
	
	/**
	 * Convert a sparse array to an array
	 * 
	 * @param argumentClass
	 * @param value the raw value
	 * 
	 * @return an array populated with the given values
	 */
	private static Object toArray(
		final Class<?> argumentClass,
		final SparseArray<?> values
	) {
		final int length = values.isEmpty() ? 0 : values.lastKey().intValue() + 1;
		final Object target = Array.newInstance(
			argumentClass.getComponentType(),
			length
		);
		for(ListIterator<?> i = values.populationIterator(); i.hasNext(); ) {
			final int index = i.nextIndex();
			final Object value = i.next();
			Array.set(target, index, value);
		}
		return target;
	}

	/**
     * Retrieve the class specified by its name
     * 
     * @param configuration the configuration is used to retrieve the class name
	 * @param kind the kind corresponds to the configuration entry name
	 * @param className the name of the class
     * @return the requested class
     */
    protected static <C> Class<C> getClass(
    	Configuration configuration, 
    	String kind
    ){
    	return configuration.getOptionalValue(
    	    kind, 
    	    String.class
    	).map(
    	    className -> BeanFactory.<C>getClass(kind, className)
    	).orElseThrow(
    	    () -> BasicException.initHolder(
    	        new IllegalArgumentException(
    	            "Missing class name",
    	            BasicException.newEmbeddedExceptionStack(
    	                BasicException.Code.DEFAULT_DOMAIN,
    	                BasicException.Code.INVALID_CONFIGURATION,
    	                new BasicException.Parameter(kind)
    	            )
    	        )
    	    )
    	);
    }

    /**
     * Retrieve the class specified by its name
     * 
     * @param configuration the configuration is used to retrieve the class name
     * @param kind the kind corresponds to the configuration entry name
     * 
     * @return the requested class
     */
    protected static <C> Optional<Class<C>> getOptionalClass(
        Configuration configuration, 
        String kind
    ){
        return configuration.getOptionalValue(
            kind, 
            String.class
        ).map(
            className -> getClass(kind, className)
        );
    }
    
    /**
     * Retrieve the class specified by its name
     * 
     * @param kind tells whether we were looking for the instance or the bean class
     * @param className the name of the class
     * 
     * @return the requested class
     */
    protected static <C> Class<C> getClass(
    	String kind, 
    	String className
    ){
    	try {
			return Classes.<C>getApplicationClass(className);
		} catch (ClassNotFoundException exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Class retrieval failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter(kind, className)
                    )
                )
            );
		}
    }
    
}