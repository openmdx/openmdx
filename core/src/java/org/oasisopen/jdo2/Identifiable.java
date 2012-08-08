/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Identifiable.java,v 1.16 2008/12/31 01:59:26 wfro Exp $
 * Description: Identifiable 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/31 01:59:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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
package org.oasisopen.jdo2;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.openmdx.compatibility.kernel.application.cci.Classes;

/**
 * Identifiable
 */
public abstract class Identifiable {

    /**
     * The last component of <code>Identifiable.class.getPackage().getName()</code>.
     */
    private final static String JDO_PACKAGE_SUFFIX = "jdo2";

    /**
     * Retrieve the <code>openmdxjdoIdentity</code> field value
     * 
     * @return the <code>openmdxjdoIdentity</code> field value
     */
    protected abstract String openmdxjdoGetObjectId();

    /**
     * Retrieve the <code>openmdxjdoIdentity</code> field value
     * 
     * @param identifiable
     * 
     * @return the <code>openmdxjdoIdentity</code> field value
     */
    public static String openmdxjdoGetObjectId(
        Object identifiable
    ){
        return identifiable instanceof Identifiable ?
            ((Identifiable)identifiable).openmdxjdoGetObjectId() :
            null;
    }
    
    /**
     * Retrieve an object
     * 
     * @param objectClass the object's class
     * @param objectId the object id as String
     * 
     * @return the corresponding object instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    protected final <T> T openmdxjdoGetObject(
        Class<T> objectClass,
        String objectId
    ) {
        return (T) openmdxjdoGetObjectById(
            JDOHelper.getPersistenceManager(this),
            objectClass, 
            objectId
        );
    }

    /**
     * Retrieve an object set
     * 
     * @param objectClass the objects' class
     * @param objectIdSet the object id set
     * 
     * @return the corresponding object instance
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    protected final <T> Set<T> openmdxjdoGetObjectSet(
        Class<T> objectClass,
        Set<String> objectIdSet
    ) {
        return new ObjectSet(
            objectClass,
            objectIdSet
        );
    }

    /**
     * Retrieve an object
     * 
     * @param persistenceManager the persistence manager to retrieve the object from
     * @param objectClass 
     * @param objectId the object id as String
     * @return the corresponding object instance
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static Object openmdxjdoGetObjectById(
        PersistenceManager persistenceManager,
        Class<?> objectClass, 
        String objectId
    ) {
        if(objectId == null || persistenceManager == null) {
            return null;
        } else {
            try {
                return persistenceManager.getObjectById(
                    jdoClass(objectClass),
                    objectId                
                );
            } catch (ClassNotFoundException exception) {
                throw new JDOFatalUserException(
                    "Could not find the data object class corresponding to '" + objectClass.getName() + "'",
                    exception
                );
            } catch (NoClassDefFoundError exception) {
                throw new JDOFatalUserException(
                    "The data object class corresponding to '" + objectClass.getName() + "' is unavailable",
                    exception
                );
            }
        }
    }
    
    /**
     * Retrieve the generated model classes
     * 
     * @param javaClass
     * 
     * @return {modelClass, modelBaseClass}; or
     * {null, null} in case of failure
     * @throws ClassNotFoundException 
     */
    protected static Class<?> jdoClass(
        Class<?> javaClass
    ) throws ClassNotFoundException{
        if(javaClass == null) {
            return null;
        } else {
            String javaClassName = javaClass.getName();
            int c = javaClassName.lastIndexOf('.');
            int p = javaClassName.lastIndexOf('.', c - 1);
            String jdoClassName = javaClassName.substring(0, p + 1) + JDO_PACKAGE_SUFFIX + javaClassName.substring(c);
            return Classes.getApplicationClass(jdoClassName);
        }
    }

    /**
     * Retrieve the generated model classes
     * 
     * @param javaClass
     * 
     * @return {modelClass, modelBaseClass}; or
     * {null, null} in case of failure
     */
    @SuppressWarnings("unchecked")
    protected static List<String>[] modelClasses(
        Class<?> javaClass
    ){
        List<String>[] modelClasses = new List[2];
        try {
            Class jdoClass = jdoClass(javaClass);
            modelClasses[0] = (List<String>) jdoClass.getField("BASE_CLASS").get(null);
            modelClasses[1] = (List<String>) jdoClass.getField("CLASS").get(null);
        } catch (Exception exception) {
            // Return array with null values
        }    
        return modelClasses;
    }
        
    
    /**
     * Object Set
     */
    class ObjectSet<E> extends AbstractSet<E> {

        /**
         * Constructor 
         *
         * @param objectClass
         * @param objectIdSet
         */
        ObjectSet(
            final Class<E> objectClass,
            final Set<String> objectIdSet
        ) {
            this.objectClass = objectClass;
            this.objectIdSet = objectIdSet;
        }

        /**
         * 
         */
        final Class<E> objectClass;
        
        /**
         * 
         */
        final Set<java.lang.String> objectIdSet;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<E> iterator() {
            return new ObjectIterator(
                this.objectIdSet.iterator()
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.objectIdSet.size();
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractSet#hashCode()
         */
        @Override
        public int hashCode() {
            return this.objectIdSet.hashCode();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        @Override
        public boolean contains(
            Object object
        ) {
            Object objectId = JDOHelper.getObjectId(object); 
            return
                objectId != null &&
                this.objectIdSet.contains(objectId.toString());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.objectIdSet.isEmpty();
        }

        /**
         * Object Iterator
         */
        class ObjectIterator implements Iterator<E> {

            /**
             * Constructor 
             *
             * @param objectIdIterator
             */
            ObjectIterator(final Iterator<String> objectIdIterator) {
                this.objectIdIterator = objectIdIterator;
            }
            
            /**
             * 
             */
            final Iterator<java.lang.String> objectIdIterator;
            
            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.objectIdIterator.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public E next() {
                return openmdxjdoGetObject(
                    ObjectSet.this.objectClass,
                    this.objectIdIterator.next()
                );
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                this.objectIdIterator.remove();
            }

        }
        
    }

}