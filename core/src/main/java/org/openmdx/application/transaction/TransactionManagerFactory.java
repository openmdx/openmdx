/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: TransactionManagerFactory 
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
package org.openmdx.application.transaction;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.log.SysLog;

/**
 * This {@code TransactionManager{@code  factory supports<ol>
 * <li>{@code java:comp/TransactionManager} JNDI lookup <em>(Standard)</em>
 * <li>{@code java:/TransactionManager} JNDI lookup <em>(JBoss)</em>
 * <li>{@code java:appserver/TransactionManager JNDI lookup <em>(GlassFish)</em>
 * <li>{@code com.ibm.ws.Transaction.TransactionManagerFactory.getTransactionManager()} <em>(WebSphere)</em> 
 * </ul>
 * <p>
 * This class is registered in the org.openmdx.kernel.naming.ComponentEnvironment by
 * org.openmdx.base.rest.adapter.ConnectionAdapter.
 * 
 * @see org.openmdx.base.rest.adapter.ConnectionAdapter
 * @see org.openmdx.kernel.naming.ComponentEnvironment
 */
public class TransactionManagerFactory
    implements Factory<TransactionManager>
{

    /**
     * Constructor 
     *
     * @param jndiNames
     * @param methodNames
     */
    public TransactionManagerFactory(
        String[] jndiNames,
        String[] methodNames
    ) {
        this.jndiNames = jndiNames == null ? new String[]{} : jndiNames;
        this.methodNames = methodNames == null ? new String[]{} : methodNames;
    }
    
    /**
     * Constructor 
     */
    public TransactionManagerFactory() {
        this(
            JNDI_NAMES,
            METHOD_NAMES
        );
    }

    /**
     * The initial JNDI names to try
     */
    private final static String[] JNDI_NAMES = {
        "java:comp/TransactionManager", // 1st
        "java:/TransactionManager", // 2nd
        "java:appserver/TransactionManager" // 3rd
    };
    
    /**
     * The initial vendor specific methods to try
     */
    private final static String[] METHOD_NAMES = {
        "com.ibm.ws.Transaction.TransactionManagerFactory.getTransactionManager" // 4th
    };
    
    /**
     * The actual JNDI names to try
     */
    private String[] jndiNames;
    
    /**
     * The actual method names to try
     */
    private String[] methodNames;

    /**
     * The name is fixed after the first successful retrieval
     */
    private boolean fixed = false;

    /**
     * Replace the JNDI and method name lists by a singleton list and an
     * empty list respectively.
     * 
     * @param jndi {@code true} if the JNDI lookup was successful,
     * {@code false} if the method call was successful
     * @param names the single elemen array to be used in future
     */
    private void fix(
        String jndiName,
        String methodName
    ){
        if(jndiName == null) {
            this.jndiNames = new String[]{};
            SysLog.detail("JNDI names disabled");
        } else {
            this.jndiNames = new String[]{jndiName};
            SysLog.detail("JNDI names fixed", jndiName);
        }
        if(methodName == null) {
            this.methodNames = new String[]{};
            SysLog.detail("Method names disabled");
        } else {
            this.methodNames = new String[]{methodName};
            SysLog.detail("Method name fixed", methodName);
        }
        this.fixed = true;
    }
    
    /**
     * Try to find the {@code TransactionManager} in the JNDI tree
     * 
     * @param context the initial context
     * @param name the JNDI name to be tried
     */
    private TransactionManager byLookup(
        Context context, 
        String name
    ) {
        try {
            return (TransactionManager) context.lookup(name);
        } catch (Exception exception) {
            SysLog.trace(
                "Trying to find the TransactionManager in the JNDI tree: " + name,
                exception
            );
            return null;
        }
    }

    /**
     * Try to find the {@code TransactionManager} in the JNDI tree
     *  
     * @param names the JNDI names to be tried
     * 
     * @return the {@code TransactionManager} or {@code null}
     */
    private TransactionManager byLookup(
        String[] names
    ){
        TransactionManager transactionManager = null;
        if(names.length > 0) try {
            Context context = new InitialContext();
            for(String name : names) {
                transactionManager = byLookup(context, name);
                if(transactionManager != null) {
                    if(!this.fixed) fix(name, null);
                    break;
                }
            }
            context.close();
        } catch (Exception exception) {
            SysLog.trace(
                "Initial context management failure while " +
                "trying to find the TransactionManager in the JNDI tree",
                exception
            );
        }
        return transactionManager;
    }

    /**
     * Try to find the {@code TransactionManager} by through a vendor specific factory
     * 
     * @param name the method name to be tried
     */
    private TransactionManager byFactory(
        String name
    ) {
        try {
            int i = name.lastIndexOf('.');
            return (TransactionManager) Classes.getApplicationClass(
                name.substring(0, i) // class name
            ).getMethod(
                name.substring(i + 1) // simple method name
            ).invoke(
                null
            );
        } catch (Exception exception) {
            SysLog.trace(
                "Trying to find the TransactionManager through a vendor specific factory: " + name,
                exception
            );
            return null;
        }
    }
    
    /**
     * Try to find the {@code TransactionManager} through a vendor specific factory
     *  
     * @param names the method names to be tried
     * 
     * @return the {@code TransactionManager} or {@code null}
     */
    private TransactionManager byFactory(
        String[] names
    ){
        TransactionManager transactionManager = null;
        for(String name : names) {
            transactionManager = byFactory(name);
            if(transactionManager != null) {
                if(!this.fixed) fix(null, name);
                break;
            }
        }
        return transactionManager;
    }

    @Override
    public TransactionManager instantiate(
    ) {
        TransactionManager transactionManager = null; 
        transactionManager = byLookup(this.jndiNames);
        if(transactionManager != null) return transactionManager; 
        transactionManager = byFactory(this.methodNames);
        if(transactionManager != null) return transactionManager; 
        throw new RuntimeServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NO_RESOURCE,
            "TransactionManager retrieval failure",
            new BasicException.Parameter("jndiNames", (Object[])this.jndiNames),
            new BasicException.Parameter("methodNames", (Object[])this.methodNames)
        );
    }

    @Override
    public Class<? extends TransactionManager> getInstanceClass() {
        return TransactionManager.class;
    }

}