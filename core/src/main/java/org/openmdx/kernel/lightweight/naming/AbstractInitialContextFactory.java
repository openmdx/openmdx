/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract Initial Context Factory
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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
#if JAVA_8
	import javax.transaction.TransactionManager;
	import javax.transaction.TransactionSynchronizationRegistry;
	import javax.transaction.UserTransaction;
#else
	import jakarta.transaction.TransactionManager;
	import jakarta.transaction.TransactionSynchronizationRegistry;
	import jakarta.transaction.UserTransaction;
#endif
import org.openmdx.kernel.lightweight.naming.java.javaURLContextFactory;
import org.openmdx.kernel.lightweight.naming.jdbc.AbstractDataSourceContext;
import org.openmdx.kernel.lightweight.naming.spi.DelegatingContext;
import org.openmdx.kernel.log.SysLog;

/**
 * Abstract Initial Context Factory
 */
public abstract class AbstractInitialContextFactory implements InitialContextFactory {

	protected AbstractInitialContextFactory(){
    	this.dataSourceContext = createDataSourceContext();
	}

	/**
	 * The lightweight JNDI implementation's URL package prefix
	 */
	private static final String LIGHTWEIGHT_URL_PKG_PREFIX = "org.openmdx.kernel.lightweight.naming";

	/**
	 * The Initial Context
	 */
	private Context initialContext;
	
	/**
	 * The Data Source Context
	 */
	private AbstractDataSourceContext dataSourceContext;

	/**
     * Create a lightweight initial context
     * 
     * @throws NoInitialContextException 
     */
    protected LightweightInitialContext createInitialContext(
        TransactionManager transactionManager,
        TransactionSynchronizationRegistry transactionSynchronizationRegistry,
        UserTransaction userTransaction
    ) throws NoInitialContextException {
        try {
            //
            // Component Context Set-Up
            //
            Map<String,Object> transactionEnvironment = new HashMap<>();
            transactionEnvironment.put(
                "org.openmdx.comp.TransactionManager",
                transactionManager
            );
            transactionEnvironment.put(
                "org.openmdx.comp.TransactionSynchronizationRegistry",
                transactionSynchronizationRegistry
            );
            transactionEnvironment.put(
                "org.openmdx.comp.UserTransaction",
                userTransaction
            );
            javaURLContextFactory.populate(transactionEnvironment);
            javaURLContextFactory.populate(System.getProperties());
            //
            // JDNI set-up
            //
            Map<String,String> initialContextEnvironment = new HashMap<String,String>();
            initialContextEnvironment.put(
                Context.INITIAL_CONTEXT_FACTORY,
                getClass().getName()
            );
            initialContextEnvironment.put(
                Context.URL_PKG_PREFIXES,
                LIGHTWEIGHT_URL_PKG_PREFIX
            );
            return new LightweightInitialContext(initialContextEnvironment);
        } catch (NamingException exception) {
            final NoInitialContextException noInitialContextException = new NoInitialContextException(
                "Unable to build the lightweight containers initial context"
            );
            noInitialContextException.setRootCause(exception);
			throw noInitialContextException;
        }
    }

    protected abstract LightweightInitialContext createInitialContext(
    ) throws NamingException;
    
    /**
     * Create the Data Source {@code Context}
     * 
     * @return the Transaction Manager specific Data Source {@code Context}
     */
    protected abstract AbstractDataSourceContext createDataSourceContext();
    
    @Override
    public Context getInitialContext(
        Hashtable<?, ?> environment
    ) throws NamingException {
        if(initialContext == null) {
        	this.initialContext = createInitialContext();
        }
        if(environment != null) {
            javaURLContextFactory.populate(environment);
        }
        dataSourceContext.activate();
        return new DelegatingContext(environment, initialContext);
    }
        
    public void shutDown() {
    	try {
			this.dataSourceContext.close();
		} catch (Exception e) {
			SysLog.error("Data Source shut down failure", e);
		}
    }

}
