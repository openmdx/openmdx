/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: UserObjects 
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
package org.openmdx.base.persistence.cci;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.kernel.loading.Factory;

/**
 * User Objects
 */
public class UserObjects extends SharedObjects {
    
    /**
     * Constructor 
     */
    protected UserObjects() {
        // Avoid instantiation
    }

    /**
     * Retrieve the principal chain
     * 
     * @param persistenceManager
     * 
     * @return the principal chain
     */
    public static List<String> getPrincipalChain(
        PersistenceManager persistenceManager
    ){
        List<String> principalChain = SharedObjects.sharedObjects(persistenceManager).getPrincipalChain(); 
        return principalChain == null ? Collections.<String>emptyList() : principalChain;
    }

    /**
     * If set the task identifier's {@code toString()} method is evaluated 
     * at the beginning of each unit of work.
     * <p>
     * An application may therefore<ul>
     * <li>either replace <em>unmodifiable</em> task identifiers 
     * (e.g. {@code java.langString} instances) to change the task id 
     * <li>use a <em>stateful</em> task identifier providing the current task id each time its
     * {@code toString()} method is invoked
     * </ul>
     * 
     * @param persistenceManager
     * @param taskIdentifier
     */
    public static void setTaskIdentifier(
        PersistenceManager persistenceManager,
        Object taskIdentifier
    ){
        SharedObjects.sharedObjects(persistenceManager).setTaskIdentifier(taskIdentifier);
    }
    
    /**
     * If set the transaction time's {@code instantiate()} method is evaluated 
     * at the beginning of each unit of work.
     * 
     * @param persistenceManager
     * @param transactionTime
     */
    public static void setTransactionTime(
        PersistenceManager persistenceManager,
        Factory<#if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif> transactionTime
    ){
        SharedObjects.sharedObjects(persistenceManager).setTransactionTime(transactionTime);
    }

    /**
     * Replaces the tenant information
     * 
     * @param persistenceManager
     * @param tenant
     */
    public static void setTenant(
        PersistenceManager persistenceManager,
        Object tenant
    ){
        SharedObjects.sharedObjects(persistenceManager).setTenant(tenant);
    }

    /**
     * Retrieve the tenant information
     * 
     * @param persistenceManager
     * 
     * @return the tenant information
     */
    public static Object getTenant(
        PersistenceManager persistenceManager
    ){
        return SharedObjects.sharedObjects(persistenceManager).getTenant();
    }

    /**
     * Set the bulk load flag
     * 
     * @param bulkLoad {@code true} in case of bulk load
     */
    public static void setBulkLoad(
        PersistenceManager persistenceManager,
        boolean bulkLoad
    ){
        SharedObjects.sharedObjects(persistenceManager).setBulkLoad(bulkLoad);
    }
    
    /**
     * Retrieve the bulk load flag value
     * 
     * @return {@code true} in case of bulk load
     */
    public static boolean isBulkLoad(
        PersistenceManager persistenceManager
    ){
        return SharedObjects.sharedObjects(persistenceManager).isBulkLoad();
    }
    
}
