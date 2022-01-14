/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Bean Factory 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 *   distribution.//        if(section == null) {
//            return new Configuration();
//        } else {
//            String name = source.getProperty(Constants.PROPERTY_NAME);
//            Configuration target = name == null ? new Configuration(
//            ) : new PropertiesConfigurationProvider(
//                Resources.toMetaInfXRI(name + ".properties"),
//                true // strict
//            ).getConfiguration(
//                section
//            );
//            amendConfiguration(
//                source,
//                target,
//                "\\.",
//                section
//            );
//            return target;
//        }

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.cci.Configuration;

/**
 * Bean Factory
 * <p>
 * The following configuration entry names are reserved<ul>
 * <li><code>class</code>, the class to be instantiated
 * <li><code>interface</code>, the class to be exposed by the factory
 * </ul>
 * <p>
 * If a class has a public <code>Configuration</code> constructor then 
 * this constructor is used to inject the configuration, otherwise
 * the instance is treated as java bean, i.e. it is instantiated via
 * default constructor and the configuration is applied through its
 * setters.
 */
public class PlugInFactory<T> extends BeanFactory<T> {

	/**
     * Constructor 
     *
     * @param instanceClass the instance class, often an interface
     * @param beanClass the Java bean class
     * @param configuration the configuration to be kept by the bean factory
     */
    private PlugInFactory(
    	Class<T> instanceClass,	
        Class<? extends T> beanClass,
        Configuration configuration
    ){
    	super(instanceClass, beanClass, configuration);
        this.nonJavaBeanConstructor = getConstructorWithConfiguration(beanClass);
    }

    /**
     * Constructor 
     *
     * @param beanClass the Java Bean and instance class
     * @param settings the settings to be kept by the bean factory
     */
    private PlugInFactory(
        Class<T> beanClass,
        Configuration configuration
    ){
    	this(beanClass, beanClass, configuration);
    }
    
    /**
     * The alternative constructor
     */
    private final Constructor<? extends T> nonJavaBeanConstructor;
    
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
    	final Class<Object> beanClass = getClass(configuration, "class");
    	return getOptionalClass(
    	    configuration, 
    	    "interface"
    	).map(
    	    interfaceClass -> new PlugInFactory<Object>(
                interfaceClass,
                beanClass,
                configuration
            )
    	).orElseGet(
    	    () -> new PlugInFactory<Object>(
                beanClass,
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
        return new PlugInFactory<T>(
		    instanceClass,
		    BeanFactory.<T>getClass(configuration, "class"),
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
        Configuration properties
    ){
        return newInstance(
        	Object.class,
        	beanClassName, 
            properties
        );
    }
    
    /**
     * Create a factory for the given Java Bean class
     * 
     * @param beanClassName
     * @param configuration the Java Bean configuration
     * 
     * @return a factory for the given class
     */
    public static Factory<?> newInstance(
        String beanClassName,
        Map<String, ?> configuration
    ){
        return newInstance(
        	Object.class,
        	beanClassName, 
            Configurations.getBeanConfiguration(configuration)
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
	public static <T> Factory<T> newInstance(
    	Class<T> instanceClass,
        String beanClassName,
        Configuration configuration
    ){
        return new PlugInFactory<T>(
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
	 * @see org.openmdx.kernel.loading.BeanFactory#build()
	 */
	@Override
	protected T build() throws Exception {
		return this.nonJavaBeanConstructor == null ? 
			super.build(): 
			this.nonJavaBeanConstructor.newInstance(this.configuration);
	}

	/**
	 * Retrieve the (optional) alternative constructor using a 
	 * <code>Configuration</code> argument
	 * 
	 * @param beanClass the class to be inspected
	 * 
	 * @return the constructor unless the class is a plain java bean class
	 */
    @SuppressWarnings("unchecked")
	private Constructor<? extends T> getConstructorWithConfiguration(
    	Class<? extends T> beanClass	
    ){
    	for(Constructor<?> constructor: beanClass.getConstructors()){
    		if(isPublic(constructor)) {
	    		final Class<?>[] parameterTypes = constructor.getParameterTypes();
	    		if(parameterTypes.length == 1) {
	    			if(parameterTypes[0] == Configuration.class) {
	    				return (Constructor<? extends T>) constructor;
	    			}
	    		}
    		}
    	}
    	return null;
    }
    
    /**
     * Tests whether the constructor is public
     * 
     * @param constructor the constructor to be tested
     * 
     * @return <code>true</code> if he constructor is public
     */
    private static boolean isPublic(
    	Constructor<?> constructor
    ){
    	return (constructor.getModifiers() & Modifier.PUBLIC) != 0;
    }

}