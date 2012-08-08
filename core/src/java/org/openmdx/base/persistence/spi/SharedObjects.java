/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SharedObjects.java,v 1.7 2010/04/26 16:06:16 hburger Exp $
 * Description: Shared Objects
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/26 16:06:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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

import java.util.List;
import java.util.UUID;

import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.PersistenceManager;

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
     * @param key the user object's key
     * 
     * @return the plug-in provided user object
     */
    public static <T> T getPlugInObject(
        PersistenceManager persistenceManager,
        Class<T> key
    ){
        return key.cast(sharedObjects(persistenceManager).getPlugInObject(key));
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
     * Propagate the shared objects from the source ot the target
     * 
     * @param target target <code>PersistenceManager</code>
     * @param source source <code>PersistenceManager</code>
     */
    public static void propagate(
        PersistenceManager target,
        PersistenceManager source
    ){
        target.putUserObject(
            KEY,
            source.getUserObject(KEY)
        );
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
         * Retrieve a data manager plug-in provided user object
         * 
         * @param key the user object's key
         * 
         * @return the plug-in provided user object
         */
        Object getPlugInObject(
            Object key
        );
     
        /**
         * Retrieve the identifier of an active unit of work
         * 
         * @return the identifier of an active unit of work
         */
        String getUnitOfWorkIdentifier(
        );
        
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
