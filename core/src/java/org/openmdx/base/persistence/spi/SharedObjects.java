/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Shared Objects
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2012, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.PersistenceManager;

import org.openmdx.kernel.loading.Factory;

/**
 * User Objects
 */
public class SharedObjects {
    
    /**
     * Constructor 
     */
    protected SharedObjects() {
        // Avoid instantiation
    }

    /**
     * The <code>PersistenceManager</code>'s user object key for shared objects
     */
    private static final Object KEY = Accessor.class;

    /**
     * Retrieve a data manager plug-in provided user object
     * 
     * @param persistenceManager
     * @param type the plug-in object's type
     * 
     * @return a plug-in object of the given type
     */
    public static <T> T getPlugInObject(
        PersistenceManager persistenceManager,
        Class<T> type
    ){
        return sharedObjects(persistenceManager).getPlugInObject(type);
    }
    
    /**
     * Retrieve the identifier of an active unit of work
     * 
     * @return the identifier of an active unit of work
     */
    public static String getUnitOfWorkIdentifier(
        PersistenceManager persistenceManager
    ){
        return sharedObjects(persistenceManager).getUnitOfWorkIdentifier();
    }
    
    /**
     * Provide the shared object accessor.
     * 
     * @return the shared object accessor
     * 
     * @exception JDOException if the shared object accessor is missing
     */
    protected static Accessor sharedObjects(
        PersistenceManager persistenceManager
    ) throws JDOException {
        Accessor sharedObjects = (Accessor) persistenceManager.getUserObject(KEY);
        if(sharedObjects == null) throw new JDOFatalInternalException(
            "Shared object accessor is missing"
        );
        return sharedObjects;
    }
    
    /**
     * Retrieve the accessor for aspect specific objects
     * 
     * @param persistenceManager
     * 
     * @return the accessor for aspect specific objects
     * 
     * @exception JDOException if the aspect specific object accessor is missing
     */
    public static Aspects aspectObjects(
        PersistenceManager persistenceManager
    ){
        Aspects aspectObjects = sharedObjects(persistenceManager).aspectObjects();
        if(aspectObjects == null) throw new JDOFatalInternalException(
            "Aspect specifc object accessor is missing"
        );
        return aspectObjects;
    }

    /**
     * Compare the key with the shared object's key
     * 
     * @param key the key to be tested
     * 
     * @return <code>true</code> if the key is the shared object's key
     */
    public static boolean isKey(
        Object key
    ){
        return key == KEY;
    }
    
    //------------------------------------------------------------------------
    // Interface Accessor
    //------------------------------------------------------------------------
    
    /**
     * Shared Object Accessor
     */
    public static interface Accessor {

        /**
         * Retrieve the principal chain
         * 
         * @return the principal chain
         */
        List<String> getPrincipalChain();
        
        /**
         * Retrieve the accessor for aspect specific objects
         * 
         * @return the accessor for aspect specific objects
         */
        Aspects aspectObjects();

        /**
         * If set the task identifier's <code>toString()</code> method is evaluated 
         * at the beginning of each unit of work.
         * <p>
         * An application may therefore<ul>
         * <li>either replace <em>unmodifiable</em> task identifiers 
         * (e.g. <code>java.langString</code> instances) to change the task id 
         * <li>use a <em>stateful</em> task identifier providing the current task id each time its
         * <code>toString()</code> method is invoked
         * </ul>
         * 
         * @param taskIdentifier
         * 
         * @see Accessor#getTaskIdentifier()
         */
        void setTaskIdentifier(
            Object taskIdentifier
        );

        /**
         * Retrieve the current task identifier 
         * 
         * @return the current task identifier 
         * 
         * @see Accessor#setTaskIdentifier(Object)
         */
        Object getTaskIdentifier(
        );

        /**
         * Replace the tenant information
         * 
         * @param tenant information
         */
        void setTenant(
            Object tenant
        );
        
        /**
         * Retrieve the tenant information
         *  
         * @return the tenant information
         */
        Object getTenant(
        );
        
        /**
         * Set the bulk load flag
         * 
         * @param bulkLoad <code>true</code> in case of bulk load
         */
        void setBulkLoad(
            boolean bulkLoad
        );
        
        /**
         * Retrieve the bulk load flag value
         * 
         * @return <code>true</code> in case of bulk load
         */
        boolean isBulkLoad(
        );
        
        /**
         * Retrieve a data manager plug-in provided object
         * 
         * @param type the plug-in object's type
         * 
         * @return the plug-in provided object, or <code>null</code> if no 
         * plug-in provides an object of the given type
         */
        <T> T getPlugInObject(
            Class<T> type
        );
     
        /**
         * Retrieve the identifier of an active unit of work
         * 
         * @return the identifier of an active unit of work
         */
        String getUnitOfWorkIdentifier(
        );
     
        /**
         * Set the transaction time factory
         * 
         * @param transactionTime
         */
        void setTransactionTime(
            Factory<Date> transactionTime
        );
        
        /**
         * Retrieve the transaction time factory
         * 
         * @return the transaction time factory
         */
        Factory<Date> getTransactionTime();
        
    }

    
    //------------------------------------------------------------------------
    // Interface Aspects
    //------------------------------------------------------------------------
    
    /**
     * Accessor For Aspect Specific Objects
     */
    public static interface Aspects {
        
        /**
         * Retrieve an aspect specific object
         * 
         * @param transactionalObjectId
         * @param aspect
         * 
         * @return the aspect specific object
         */
        Object get(
            UUID transactionalObjectId,
            Class<?> aspect
        );
            
        /**
         * Set an aspect specific object
         * 
         * @param transactionalObjectId
         * @param aspect
         * @param value the object to be stored
         */
        void put(
            UUID transactionalObjectId,
            Class<?> aspect,
            Object value
        );
        
        /**
         * Remove an aspect specific context
         * 
         * @param transactionalObjectId
         * @param aspect
         */
        void remove(
            UUID transactionalObjectId,
            Class<?> aspect
        );

    }

}
