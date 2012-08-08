/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Identifiable.java,v 1.13 2008/02/19 13:52:46 hburger Exp $
 * Description: Identifiable 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:52:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2007, OMEX AG, Switzerland
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

import org.oasisopen.cci2.Identity;
import org.oasisopen.spi2.DataObjectIdBuilder;
import org.oasisopen.spi2.ObjectId;
import org.oasisopen.spi2.ObjectIdBuilder;
import org.openmdx.model1.mapping.java.Identifier;

/**
 * Identifiable
 */
public abstract class Identifiable {

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
     * Create a persistent application identity
     * 
     * @param mixinParent tells whether the <code>parent</code> is a reference to a 
     * mix-in class<ul>
     * <li><code>null</code> if the object has no parent 
     * <li><code>TRUE</code> if the object is contained in a mix-in class 
     * <li><code>FALSE</code> otherwise
     * </ul> 
     * @param referenceName the name of the composite reference between the parent
     * instance and this instance, or <code>null</code> if mixinParent is <code>null</code>
     * @param persistentQualifier tells whether the corresponding <code>qualifier</code>
     * value is persistent or not
     * @param qualifier the qualifier values are instances of<ul>
     * <li><code>java.math.BigDecimal</code> for values of type<ul>
     * <li><code>org::w3c::decimal</code>
     * </ul>
     * <li><code>java.lang.Integer</code> for values of type<ul>
     * <li><code>org::w3c::integer</code>
     * </ul>
     * <li><code>java.lang.String</code> for values of type<ul>
     * <li><code>org::w3c::string</code>
     * </ul>
     * <li><code>java.net.URI</code> for values of type<ul>
     * <li><code>org::w3c::anyURI</code>
     * </ul>
     * <li><code>java.util.Date</code> for values of type<ul>
     * <li><code>org::w3c::dateTime</code>
     * </ul>
     * <li><code>java.util.UUID</code> for values of type<ul>
     * <li><code>org::ietf::uuid</code>
     * </ul>
     * <li><code>javax.xml.datatype.Duration</code> for values of type<ul>
     * <li><code>org::w3c::duration</code>
     * </ul>
     * <li><code>javax.xml.datatype.XMLGregorianCalendar</code> for values of type<ul>
     * <li><code>org::w3c::date</code>
     * <li><code>org::w3c::time</code>
     * </ul>
     * <li><code>org.ietf.jgss.Oid</code> for values of type<ul>
     * <li><code>org::ietf::oid</code>
     * </ul>
     * </ul>
     * @param baseClass the object's fully qualified base class name;
     * this argument may be <code>null</code> if all qualifiers are persistent
     * @param objectClass the object's fully qualified class name
     * @param parentObjectId the parent object unless <code>mixinParent</code> is <code>null</code>
     * @return a new persistent object id based on the given arguments
     * 
     * @throws IllegalArgumentException in case of an unsupported qualifier 
     * class
     */
    protected final String openmdxjdoNewObjectId(
        Boolean mixinParent, Object parent,
        String referenceName, 
        List<Boolean> persistentQualifier, List<?> qualifier, 
        List<String> baseClass, List<String>objectClass
    ){
        return openmdxjdoObjectIdBuilder(parent).newObjectId(
            mixinParent, openmdxjdoGetObjectId(parent),
            referenceName, 
            persistentQualifier, qualifier, 
            baseClass, objectClass
        );
    }
    
   /*
    * Create a new application identity
    * 
    * @param mixinParent tells whether the <code>parent</code> is a reference to a 
    * mix-in class<ul>
    * <li><code>null</code> if the object has no parent 
    * <li><code>TRUE</code> if the object is contained in a mix-in class 
    * <li><code>FALSE</code> otherwise
    * </ul> 
    * @param parentObjectId the parent object unless <code>mixin</code> is <code>null</code>
    * @param qualifierClass the qualifier classes are<ul>
    * <li><code>java.lang.String.class</code> to provide a persistent UUID value as<ul>
    * <li><code>org::w3c::string</code>
    * </ul>
    * <li><code>java.net.URI.class</code> to provide a persistent UUID value as<ul>
    * <li><code>org::w3c::anyURI</code>
    * </ul>
    * <li><code>java.util.Date.class</code> to provide a persistent date and time value as<ul>
    * <li><code>org::w3c::dateTime</code>
    * </ul>
    * <li><code>java.util.UUID.class</code> to provide a persistent UUID value as<ul>
    * <li><code>org::ietf::uuid</code>
    * </ul>
    * <li><code>org.ietf.jgss.Oid.class</code> to provide a persistent UUID value as<ul>
    * <li><code>org::ietf::oid</code>
    * </ul>
    * </ul>
    * @param baseClass the object's fully qualified base class name
    * @param objectClass the object's fully qualified class name
    * 
    * @return a new object id based on the given arguments
    * 
    * @throws IllegalArgumentException in case of an unsupported qualifier 
    * class
    */
    protected <T> String openmdxjdoNewObjectId(
        Boolean mixinParent, Object parent,
        String referenceName,
        List<Class<T>> qualifierClass, 
        List<String> objectClass
    ){
        return openmdxjdoObjectIdBuilder(parent).newObjectId(
            mixinParent, openmdxjdoGetObjectId(parent),
            referenceName,
            qualifierClass, objectClass
        );
    }
    
    /**
     * Retrieve the object id builder associated with this instance
     * 
     * @param object 
     * 
     * @return the object id builder associated with this instance
     */
    private static ObjectIdBuilder openmdxjdoObjectIdBuilder(
        Object object
    ){
        return openmdxjdoObjectIdBuilder(
            JDOHelper.getPersistenceManager(object)
        );
    }

    /**
     * Retrieve the object id builder associated with this persistence manager
     * 
     * @param persistenceManager 
     * 
     * @return the object id builder associated with this persistence manager
     */
    private static ObjectIdBuilder openmdxjdoObjectIdBuilder(
        PersistenceManager persistenceManager
    ){
        ObjectIdBuilder objectIdBuilder = persistenceManager == null ?
            null :
            (ObjectIdBuilder)persistenceManager.getUserObject(ObjectIdBuilder.class.getName());
        return objectIdBuilder == null ? 
            DataObjectIdBuilder.getInstance() :
            objectIdBuilder;
    }
    
    protected static Identity openmdxjdoToIdentity(
        ObjectId objectId
    ){
        return null; // TODO
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
     * @param objectId the object id as String
     * 
     * @return the corresponding object instance
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static Object openmdxjdoGetObjectById(
        PersistenceManager persistenceManager,
        String objectId
    ) {
        if(objectId == null || persistenceManager == null) {
            return null;
        } else {
            String className = jdoClassName(
                openmdxjdoObjectIdBuilder(
                    persistenceManager
                ).toObjectId(
                    objectId
                ).getTargetClass(
                )
            ); 
            try {
                return persistenceManager.getObjectById(
                    Class.forName(className),
                    objectId                
                );
            } catch (ClassNotFoundException exception) {
                throw new JDOFatalUserException(
                    "Object id '" + objectId + "' refers to unavailable class '" + className + "'",
                    exception
                );
            } catch (NoClassDefFoundError exception) {
                throw new JDOFatalUserException(
                    "Object id '" + objectId + "' refers to unavailable class '" + className + "'",
                    exception
                );
            }
        }
    }
    
    /**
     * Convert a model class name to the corresponding jdo class name
     * 
     * @param modelClass a model class
     * @return the corresponding jdo class name
     */
    private static String jdoClassName (
        List<String> modelClass
    ){
        StringBuilder javaClass = new StringBuilder();
        int iLimit = modelClass.size() - 1;
        for(
            int i = 0;
            i < iLimit;
            i++
        ){
            javaClass.append(
                Identifier.PACKAGE_NAME.toIdentifier(modelClass.get(i))
            ).append(
                '.'
            );
        }
        return javaClass.append(
            JDO_PACKAGE_SUFFIX
        ).append(
            '.'
        ).append(
            Identifier.CLASS_PROXY_NAME.toIdentifier(modelClass.get(iLimit))
        ).toString(
        );
    }

    
//    /**
//     * Retrieve an object
//     * 
//     * @param object the object id holder
//     * 
//     * @return the corresponding object instance
//     * 
//     * @throws InstantiationException
//     * @throws IllegalAccessException
//     * @throws ClassNotFoundException
//     */
//    protected final AbstractObject openmdxjdoGetObject(
//        AbstractObject object
//    ) {
//        if(object == null) {
//            return null;
//        } else {
//            PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(this);
//            if(persistenceManager == null) {
//                return null;
//            } else {
//                return (AbstractObject) persistenceManager.getObjectById(
//                    object.getClass(),
//                    object.openmdxjdoGetIdentity()                
//                );
//            }
//        }
//    }
//    

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

    /**
     * The last component of <code>Identifiable.class.getPackage().getName()</code>.
     */
    private final static String JDO_PACKAGE_SUFFIX = "jdo2";
    
}