/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Auto-Closeable Persistence Manager Factory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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

package org.openmdx.kernel.jdo;

import javax.jdo.PersistenceManagerFactory;

/**
 * Auto-Closeable Persistence Manager Factory
 */
public interface JDOPersistenceManagerFactory extends PersistenceManagerFactory {

    /** Get an instance of <code>PersistenceManager</code> from this factory.  
     * The instance has default values for options.
     *
     * <P>After the first use of <code>getPersistenceManager</code>, no "set" 
     * methods will succeed.
     *
     * @return a <code>PersistenceManager</code> instance with default options.
     */
    JDOPersistenceManager getPersistenceManager();

    /** Get a thread-safe instance of a proxy that dynamically binds 
     * on each method call to an instance of <code>PersistenceManager</code>.
     * <P>When used with a <code>JDOPersistenceManagerFactory</code>
     * that uses TransactionType JTA,
     * the proxy can be used in a server to dynamically bind to an instance 
     * from this factory associated with the thread's current transaction.
     * In this case, the close method is ignored, as the 
     * <code>JDOPersistenceManager</code> is automatically closed when the
     * transaction completes.
     * <P>When used with a <code>JDOPersistenceManagerFactory</code>
     * that uses TransactionType RESOURCE_LOCAL, the proxy uses an inheritable
     * ThreadLocal to bind to an instance of <code>PersistenceManager</code>
     * associated with the thread. In this case, the close method executed
     * on the proxy closes the <code>PersistenceManager</code> and then
     * clears the ThreadLocal.
     * Use of this method does not affect the configurability of the
     * <code>PersistenceManagerFactory</code>.
     *
     * @return a <code>PersistenceManager</code> proxy.
     */
    JDOPersistenceManager getPersistenceManagerProxy();

    /** Get an instance of <code>JDOPersistenceManager</code> from this factory.  
     * The instance has default values for options.  
     * The parameters <code>userid</code> and <code>password</code> are used 
     * when obtaining datastore connections from the connection pool.
     *
     * <P>After the first use of <code>getPersistenceManager</code>, no "set" 
     * methods will succeed.
     *
     * @return a <code>JDOPersistenceManager</code> instance with default options.
     * @param userid the userid for the connection
     * @param password the password for the connection
     */
    JDOPersistenceManager getPersistenceManager(String userid, String password);

    /**
     * Return the {@link JDODataStoreCache} that this factory uses for
     * controlling a second-level cache. If this factory does not use
     * a second-level cache, the returned instance does nothing. This
     * method never returns <code>null</code>.
     * @return the DataStoreCache
     */
    @Override
    JDODataStoreCache getDataStoreCache();

    /**
     * Tell whether the transaction is container managed 
     * 
     * @return <code>true</code> if the transaction is container managed
     */
    public boolean getContainerManaged();

}
