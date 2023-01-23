/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Lightweight Initial Context Factory Builder 
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
package org.openmdx.kernel.lightweight.naming;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

import org.openmdx.kernel.lightweight.naming.java.javaURLContextFactory;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;


/**
 * Lightweight Context Factory Builder 
 * <p>
 * Note:<b>
 * This class must not be used in a managed environment!
 */
public class LightweightInitialContextFactoryBuilder implements InitialContextFactoryBuilder {

	/**
     * Constructor 
     */
    private LightweightInitialContextFactoryBuilder() {
        //
        // URL set-up
        //
        prependSystemPropertyValue(
            "java.protocol.handler.pkgs",
            "org.openmdx.kernel.url.protocol",
            '|'
        );
        //
        // VM shutdown hook
        // 
    	Runtime.getRuntime().addShutdownHook(
    		new Thread(
	    		this::shutDown,
	    		"Lightweight Container Shutdown"
	    	)
    	);
    }
    
	/**
	 * The standard {@code InitialContextFactory} uses Atomikos as {@code TransactionManager}.
	 */
    private static final String STANDARD_INITIAL_CONTEXT_FACTORY = AtomikosInitialContextFactory.class.getName();

    /**
     * The Initial Context Factories already provided by this builder.
     */
    private final Map<String,InitialContextFactory> initialContextFactories = new HashMap<>();
    
    /**
     * Tells, whether the {@code LightweightInitialContextFactoryBuilder} is
     * already registered with the {@code NamingManager} or not.
     */
    private static boolean installed = false;
    
    /**
     * Test, whether the {@code LightweightInitialContextFactory} is already installed 
     * 
     * @return {@code true} if the {@code LightweightInitialContextFactory} is already installed 
     */
    public static boolean isInstalled() {
		return installed;
	}

	/**
     * Install the {@code LightweightInitialContextFactoryBuilder} by registering it 
     * with the {@code NamingManager}
     * <p>
     * The <code>java:comp/env</code> environment may be populated through system properties.
     * <p>
     * Note:<br>
     * This method is idempotent, i.e. it may be invoked repeatedly but does the registration
     * upon its first invocation only.
     * 
     * @throws NamingException 
     */
    public static synchronized void install(
    ) throws NamingException {
    	if(!isInstalled()) {
	        NamingManager.setInitialContextFactoryBuilder(
	            new LightweightInitialContextFactoryBuilder()
	        );
	        installed = true;
    	}
    }

    /**
     * Install the <code>NonManagedContextFactoryBuilder</code> singleton.
     * <p>
     * The <code>java:comp/env</code> environment may be populated either through
     * the <code>componentEnvironment</code> argument or through system properties.
     * 
     * @param componentEnvironment to initialize the <code>java:comp/env</code> 
     * environment.<p>
     * <code>java:comp/env/jdbc/MyDataSource</code> for example might be specified 
     * by an entry mapping <code>org.openmdx.comp.env.jdbc.MyDataSource</code>
     * to <code>jdbc:xa:org.postgresql.xa.PGXADataSource?user=scott&password=tiger</code> 
     * 
     * @throws NamingException 
     */
    public static synchronized void install(
        Map<?,?> componentEnvironment
    ) throws NamingException {
    	install();
        javaURLContextFactory.populate(componentEnvironment);
    }
    
    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactoryBuilder#createInitialContextFactory(java.util.Hashtable)
     */
    public InitialContextFactory createInitialContextFactory(
        Hashtable<?, ?> environment
    ) throws NamingException {
    	try {
	    	return this.initialContextFactories.computeIfAbsent(
	    		getInitialContextFactoryClassName(environment), 
	    		this::createInitialContextFactory
	    	);
    	} catch (UndeclaredThrowableException wrapper) {
    		final Throwable exception = wrapper.getUndeclaredThrowable();
    		if(exception instanceof NamingException) {
    			throw (NamingException)exception;
    		} else if (exception instanceof RuntimeException){
    			throw (RuntimeException)exception;
    		} else {
    			throw new RuntimeException(exception);
    		}
    	}
    }
    
    /**
     * Reflection allows the processing of foreign Initial Contexts, too.
     * 
     * @param initialContextFactoryClassName the Initial Context's class name
     * 
     * @return an {@code InitialContext} instance
     * @throws NamingException 
     */
    private InitialContextFactory createInitialContextFactory(String initialContextFactoryClassName){
    	return STANDARD_INITIAL_CONTEXT_FACTORY.equals(initialContextFactoryClassName) ?
    		createStandardInitialContextFactory() :
    		createGenericInitialContextFactory(initialContextFactoryClassName);
    }

    /**
     * Reflection allows the processing of foreign Initial Contexts, too.
     * 
     * @param initialContextFactoryClassName the Initial Context's class name
     * 
     * @return an {@code InitialContext} instance
     */
	private InitialContextFactory createGenericInitialContextFactory(String initialContextFactoryClassName) {
		try {
			return Classes.getKernelClass(initialContextFactoryClassName).asSubclass(InitialContextFactory.class).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException cause) {
			final NoInitialContextException namingException = new NoInitialContextException("Unable to create the initial context factory");
			namingException.setRootCause(cause);
			throw new UndeclaredThrowableException(namingException);
		}
	}

	/**
	 * The Lightweight Container's Standard {@code InitialContextFactory} uses Atomokos as {@code TransactionManager}.
	 * 
	 * @return the Lightweight Container's Standard {@code InitialContextFactory} 
	 * 
	 * @throws NamingException 
	 */
	private InitialContextFactory createStandardInitialContextFactory(){
		return new AtomikosInitialContextFactory();
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private String getInitialContextFactoryClassName(Hashtable environment) {
    	if(environment == null) {
    		return STANDARD_INITIAL_CONTEXT_FACTORY;
    	} else {
    		return(String) environment.getOrDefault(Context.INITIAL_CONTEXT_FACTORY, STANDARD_INITIAL_CONTEXT_FACTORY);
    	}
    }

    /**
     * This method is passed to the VM's shutdown hook
     */
    private void shutDown() {
    	initialContextFactories
    		.values()
    		.stream()
    		.filter(AbstractInitialContextFactory.class::isInstance)
    		.map(AbstractInitialContextFactory.class::cast)
    		.forEach(AbstractInitialContextFactory::shutDown);
    	SysLog.info("Lightweight Container has shut down the registered resources");
    }
    
    /**
     * Assert that a specific value is among the values in a system property's 
     * value list.
     * 
     * @param name the system property's name
     * @param value the required value
     * @param separator the value separator
     */
    private static void prependSystemPropertyValue (
        String name,
        String value,
        char separator
    ){
        String values = System.getProperty(name);
        if(values == null || values.length() == 0){
            SysLog.log(
                Level.INFO,
                "Set system property {0} to \"{1}\"",
                name,value
            );
            System.setProperty(
                name, 
                value
            );
        } else if ((separator + values + separator).indexOf(separator + value + separator) < 0) {
            String newValue = value + separator + values; 
            SysLog.log(
                Level.INFO,
                "Change system property {0} from \"{1}\" to \"{2}\"",
                name, values, newValue
            );
            System.setProperty(
                name,
                newValue
            );
        }
    }
    
    
}
