/*
 * ====================================================================
 * Name:        $Id: Object_1.java,v 1.5 2009/01/10 12:12:15 wfro Exp $
 * Description: Object_1 class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/10 12:12:15 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.accessor;

import static org.openmdx.model1.code.PrimitiveTypes.DATE;
import static org.openmdx.model1.code.PrimitiveTypes.DATETIME;
import static org.openmdx.model1.code.PrimitiveTypes.DURATION;
import static org.openmdx.model1.code.PrimitiveTypes.INTEGER;
import static org.openmdx.model1.code.PrimitiveTypes.LONG;
import static org.openmdx.model1.code.PrimitiveTypes.SHORT;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Array;
import java.rmi.Remote;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.spi.SystemOperations;
import org.openmdx.application.dataprovider.transport.rmi.RMIMapper;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.LargeObject_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.jmi.spi.IntegerMarshaller;
import org.openmdx.base.accessor.jmi.spi.LongMarshaller;
import org.openmdx.base.accessor.jmi.spi.ShortMarshaller;
import org.openmdx.base.accessor.spi.AbstractObject_1;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.spi.ListStructure_1;
import org.openmdx.base.accessor.spi.MarshallingStructure_1;
import org.openmdx.base.aop1.Aspect_1;
import org.openmdx.base.collection.CompactSparseList;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.PopulationMap;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.event.InstanceCallbackEvent;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.resource.Records;
import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.ModelUtils;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.state2.aop1.StateCapable_1;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.spi.DatatypeFactories;

/**
 * Object_1_0 implementation
 */
class Object_1 
    implements DataObject_1_0, Serializable, Evictable 
{

    //------------------------------------------------------------------------
    // Class EvictablePersistentObjects
    //------------------------------------------------------------------------

    /**
     * Evictable Persistent Objects
     */
    private class EvictablePersistentObjects
        extends MarshallingSequentialList<Object>
        implements Evictable, Serializable
    {

        /**
         *
         */
        private static final long serialVersionUID = 3257285812084159024L;

        /**
         * Constructor 
         *
         * @param feature
         */
        EvictablePersistentObjects(
            String feature
        ){
            super(connection, null);
            this.feature = feature;
        }

        /* (non-Javadoc)
         */
        @SuppressWarnings("unchecked")
        protected List<Object> getDelegate() {
            if(super.list == null) {
                try {
                    if(jdoIsNew()){
                        super.list = Collections.emptyList();
                    } else {
                        if(persistentValues != null) {
                            super.list = (List)persistentValues.get(feature);
                        }
                        if(super.list == null) {
                            super.list = channel.find(
                                identity.getChild(feature),
                                null,
                                null,
                                connection
                            );
                        }
                    }
                } catch (ServiceException e){
                    throw new RuntimeServiceException(e);
                }
            }
            return super.list;
        }

        /**
         *
         */
        private String feature;

        /* (non-Javadoc)
         */
        public void evict() {
            super.list = null;
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            return super.list == null ? Integer.MAX_VALUE : super.size();
        }

    }

    //------------------------------------------------------------------------
    // Class ManagedLargeObject
    //------------------------------------------------------------------------

    /**
     * Managed Large Object
     */
    final class ManagedLargeObject
        implements LargeObject_1_0, Flushable
    {

        /**
         *
         */
        public ManagedLargeObject(
            String feature
        ){
            super();
            this.feature = feature;
        }

        /**
         *
         */
        private final String feature;

        /**
         *
         */
        private static final long UNKNOWN = -1L;

        /**
         *
         */
        private static final long UNINITIALIZED = -1L;

        /**
         *
         */
        long transientLength = UNINITIALIZED;

        /**
         *
         */
        private Object persistentSource = null;

        /**
         *
         */
        Object transientStream = null;

        /* (non-Javadoc)
         */
        public long length() throws ServiceException {
            if(this.persistentSource == null) {
                this.persistentSource = getPersistentAttribute(
                    this.feature,
                    true, // stream
                    true, // single-value
                    false // clear
                );
            }
            if(this.persistentSource instanceof Source_1_0) {
                try {
                    this.persistentSource = new Long(
                        ((Source_1_0)this.persistentSource).length()
                    );
                } catch (IOException exception) {
                    throw new ServiceException(exception);
                }
            } else if (this.persistentSource instanceof List) {
                List<?> source = (List<?>) this.persistentSource;
                this.persistentSource = source.isEmpty() ? null : source.get(0);
            }
            return this.persistentSource instanceof Long ?
                ((Long)this.persistentSource).longValue() :
                    UNKNOWN;
        }

        /* (non-Javadoc)
         */
        public byte[] getBytes(long position, int capacity) throws ServiceException {
            byte[] buffer = new byte[capacity];
            InputStream stream = getBinaryStream();
            try {
                stream.skip(position);
                int length = stream.read(buffer, 0, capacity);
                if(length < capacity) {
                    byte[] result = new byte[length];
                    System.arraycopy(buffer, 0, result, 0, length);
                    return result;
                } else {
                    return buffer;
                }
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         */
        public InputStream getBinaryStream() throws ServiceException {
            Object stream = this.persistentSource = getPersistentAttribute(
                this.feature,
                true, // stream
                true, // single-valued 
                false // clear
            );
            return stream instanceof BinaryLargeObjects.BinaryHolder ? 
                ((BinaryLargeObjects.BinaryHolder)stream).toStream() :
                    (InputStream)stream;
        }

        /* (non-Javadoc)
         */
        public void getBinaryStream(OutputStream stream, long position) throws ServiceException {
            this.persistentSource = objInvokeOperation(
                SystemOperations.GET_BINARY_STREAM,
                new ListStructure_1(
                    SystemOperations.GET_BINARY_STREAM_ARGUMENTS,
                    GET_STREAM_ARGUMENTS,
                    new Object[]{
                        this.feature,
                        Long.valueOf(position),
                        stream
                    }
                )
            ).objGetValue(SystemOperations.GET_STREAM_LENGTH);
        }

        /* (non-Javadoc)
         */
        public char[] getCharacters(long position, int capacity) throws ServiceException {
            char[] buffer = new char[capacity];
            Reader stream = getCharacterStream();
            try {
                stream.skip(position);
                int length = stream.read(buffer, 0, capacity);
                if(length < capacity) {
                    char[] result = new char[length];
                    System.arraycopy(buffer, 0, result, 0, length);
                    return result;
                } else {
                    return buffer;
                }
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         */
        public Reader getCharacterStream() throws ServiceException {
            Object stream = this.persistentSource = getPersistentAttribute(
                this.feature,
                true, // stream
                true, // single-valued
                false // clear
            );
            return stream instanceof CharacterLargeObjects.CharacterHolder ? 
                ((CharacterLargeObjects.CharacterHolder)stream).toStream() : 
                    (Reader)stream;
        }

        /* (non-Javadoc)
         */
        public void getCharacterStream(Writer writer, long position) throws ServiceException {
            this.persistentSource = objInvokeOperation(
                SystemOperations.GET_CHARACTER_STREAM,
                new ListStructure_1(
                    SystemOperations.GET_CHARACTER_STREAM_ARGUMENTS,
                    GET_STREAM_ARGUMENTS,
                    new Object[]{
                        this.feature,
                        new Long(position),
                        writer
                    }
                )
            ).objGetValue(SystemOperations.GET_STREAM_LENGTH);
        }

        /* (non-Javadoc)
         */
        public void truncate(long length) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "Partial update not supported"
            );
        }

        /* (non-Javadoc)
         */
        public void setBytes(long position, byte[] content) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "Partial update not supported"
            );
        }

        /* (non-Javadoc)
         */
        public void setBinaryStream(InputStream stream, long size) throws ServiceException {
            getUnitOfWorkIfTransactional(this.feature);
            this.transientLength = size;
            this.transientStream = stream;
        }

        /* (non-Javadoc)
         */
        public OutputStream setBinaryStream(long position) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "Output streams not supported"
            );
        }

        /* (non-Javadoc)
         */
        public void setCharacters(long position, char[] content) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "Partial update not supported"
            );
        }

        /* (non-Javadoc)
         */
        public void setCharacterStream(Reader stream, long size) throws ServiceException {
            getUnitOfWorkIfTransactional(this.feature);
            this.transientLength = size;
            this.transientStream = stream;
        }

        /* (non-Javadoc)
         */
        public Writer setCharacterStream(long position) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "Writer not supported"
            );
        }


        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manageable#flush()
         */
        @SuppressWarnings("unchecked")
        public void flush() {
            persistentValues.put(
                this.feature, 
                Arrays.asList(this.transientStream, this.transientLength)
            );
        }

    }


    //------------------------------------------------------------------------
    // Class ManagedAspects
    //------------------------------------------------------------------------

    /**
     * Managed Aspects
     */
    final class ManagedAspect
        extends AbstractMap<String,DataObject_1_0> 
        implements Flushable, Evictable
    {

        /**
         * Constructor 
         *
         * @param aspectClass
         * 
         * @throws ServiceException 
         */
        ManagedAspect(
            String aspectClass
        ) throws ServiceException {
            this.aspectClass = aspectClass;
        }

        private final String aspectClass;
        
        /**
         * 
         */
        private Map<String, DataObject_1_0> aspects;

        /**
         * 
         */
        final Set<java.util.Map.Entry<String, DataObject_1_0>> entries = new EntrySet();

        /**
         * Aspect accessor
         * 
         * @return an accessor for a given aspect class
         * 
         * @throws ServiceException
         */
        Map<String, DataObject_1_0> getAspects(
        ) throws ServiceException {
            if(this.aspects == null) {
                FilterableMap<String, DataObject_1_0> aspects = getContainer();
                if(aspects != null) {
                    this.aspects = aspects.subMap(
                        new FilterProperty[]{
                            new FilterProperty(
                                Quantors.THERE_EXISTS,
                                SystemAttributes.OBJECT_INSTANCE_OF,
                                FilterOperators.IS_IN,
                                this.aspectClass
                            ),
                            new FilterProperty(
                                Quantors.THERE_EXISTS,
                                Aspect_1.CORE,
                                FilterOperators.IS_IN,
                                jdoGetObjectId()
                            )
                        }
                    );
                }
            }
            return this.aspects;
        }
        
        //--------------------------------------------------------------------
        // Extends AbstractSet
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
            return this.entries;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public DataObject_1_0 put(
            String key, 
            DataObject_1_0 value
        ) {
            try {
                objAddToUnitOfWork();
                Map<String, DataObject_1_0> container = getContainer();
                return container == null ? getState(
                    false // optional
                ).aspects(
                    false // readOnly
                ).put(
                    key, 
                    value
                ) : container.put(
                    toObjectId(getQualifier(), key),
                    value
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        
        //--------------------------------------------------------------------
        // Implements Flushable
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush(
        ) throws IOException {
            // state2 support to be added
        }
        
        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public boolean prepare(
        ) throws ServiceException {
            // state2 support to be added
            return true;
        }

        private String toObjectId(
            String coreId,
            String aspectId
        ){
            return
                aspectId.startsWith(":") ? ":" + coreId + aspectId :
                aspectId.startsWith("!") ? coreId + aspectId :
                coreId + '*' + aspectId;    
        }
        
        
        //--------------------------------------------------------------------
        // Implements Evictable
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            if(jdoIsPersistent()) {
                this.aspects = null;
            } else if (this.aspects instanceof Evictable){
                ((Evictable)this.aspects).evict();
            }
        }

        
        //--------------------------------------------------------------------
        // Class EntrySet
        //--------------------------------------------------------------------
        
        /**
         * Entry Set
         */
        class EntrySet extends AbstractSet<java.util.Map.Entry<String, DataObject_1_0>> {

            /**
             * Retrieve either the transactional or persistent entry set
             * 
             * @return the delegate entry set
             */
            private Set<java.util.Map.Entry<String, DataObject_1_0>> getDelegate(
            ){
                try {
                    Map<String, DataObject_1_0> persistent = getAspects();
                    if(persistent == null) {
                        TransactionalState_1 state = getState(true);
                        return state == null ? NO_ASPECT : state.aspects(true).entrySet();
                    } else {
                        flush();
                        return persistent.entrySet();
                    }
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                } catch (IOException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            
            /* (non-Javadoc)
             * @see java.util.AbstractCollection#iterator()
             */
            @Override
            public Iterator<java.util.Map.Entry<String, DataObject_1_0>> iterator() {
                return getDelegate().iterator();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#size()
             */
            @Override
            public int size() {
                return getDelegate().size();
            }
            
        }

    }
    
    //--------------------------------------------------------------------------
    // Class ManagedList
    //--------------------------------------------------------------------------

    /**
     * The managed list delegates<ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedList
        extends AbstractList<Object>
        implements Flushable
    {

        /**
         * Constructor 
         *
         * @param feature
         * @param marshaller 
         * 
         * @throws ServiceException
         */
        ManagedList(
            String feature,
            Marshaller marshaller
        ) throws ServiceException {
            this.feature = feature;
            this.nonTransactional = new NonTransactional(marshaller);
        }

        /**
         * 
         */
        final String feature;

        /**
         * 
         */
        private final List<Object> nonTransactional;

        @SuppressWarnings("unchecked")
        private List<Object> getDelegate(
            boolean makeDirty, 
            boolean clear
        ){
            try {
                UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
                if(unitOfWork == null) {
                    if(transientValues != null) {
                        List<Object> transientValue = (List<Object>) Object_1.this.transientValues.get(this.feature);
                        if(makeDirty && transientValue == null) {
                            transientValues.put(
                                this.feature,
                                transientValue = new ArrayList<Object>()
                            );
                            return transientValue;
                        }
                        if(transientValue != null) {
                            if(clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if(clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String,Object> transactionalValues = unitOfWork.getState(Object_1.this, false).values(false); 
                    List<Object> transactionalValue = (List<Object>) transactionalValues.get(this.feature);
                    if(transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = clear ? new ArrayList() : new ArrayList<Object>(this.nonTransactional)
                        );
                    } else if (clear) {
                        transactionalValue.clear();
                    }
                    return transactionalValue;
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }


        //--------------------------------------------------------------------
        // Extends AbstractList
        //--------------------------------------------------------------------

        public Object get(
            int index
        ) {
            return getDelegate(false, false).get(index);
        }

        public int size(
        ){
            return getDelegate(false, false).size();
        }

        public Object set(
            int index,
            Object element
        ){
            return getDelegate(true, false).set(index, element);
        }

        public void add(
            int index,
            Object element
        ){
            getDelegate(true, false).add(index, element);
        }

        public Object remove(
            int index
        ){
            return getDelegate(true, false).remove(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#clear()
         */
        @Override
        public void clear() {
            getDelegate(true, true);
        }

        
        //--------------------------------------------------------------------
        // Implements Flushable
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush() throws IOException {
            List<Object> source = getDelegate(false, false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.addAll(source);
            }
        }

        //--------------------------------------------------------------------
        // Class MarshalledPersistentValues
        //--------------------------------------------------------------------

        /**
         * Persistent Values Accessor
         */
        @SuppressWarnings("unchecked")
        class NonTransactional extends MarshallingList<Object> {

            /**
             * Constructor 
             * 
             * @param marshaller 
             *
             * @throws ServiceException 
             */
            NonTransactional(
                Marshaller marshaller
            ) throws ServiceException {
                super(
                    marshaller, 
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -5790864813300543661L;

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingList#getDelegate()
             */
            @Override
            protected List getDelegate() {
                return getPersistentCollection(ManagedList.this.feature, false);
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingList#clear()
             */
            @Override
            public void clear() {
                getPersistentCollection(ManagedList.this.feature, true);
            }

        }

    }


    //------------------------------------------------------------------------
    // Class ManagedSet
    //------------------------------------------------------------------------

    /**
     * The managed set delegates<ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedSet
        extends AbstractSet<Object>
        implements Flushable
    {

        /**
         * Constructor 
         *
         * @param feature
         * @param transactional
         * @param marshaller 
         * @throws ServiceException
         */
        ManagedSet(
            String feature,
            Marshaller marshaller
        ) throws ServiceException {
            this.feature = feature;
            this.nonTransactional = new NonTransactional(marshaller);
        }

        /**
         * 
         */
        final String feature;

        /**
         * 
         */
        private final Set<Object> nonTransactional;

        @SuppressWarnings("unchecked")
        Set<Object> getDelegate(
            boolean makeDirty
        ){
            try {
                UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
                if(unitOfWork == null) {
                    return this.nonTransactional;
                } else {
                    Map<String,Object> transactionalValues = unitOfWork.getState(Object_1.this, false).values(false); 
                    Set<Object> transactionalValue = (Set<Object>) transactionalValues.get(this.feature);
                    if(transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = new HashSet<Object>(this.nonTransactional)
                        );
                    }
                    return transactionalValue;
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        //--------------------------------------------------------------------
        // Extends AbstractSet
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<Object> iterator() {
            Set<Object> delegate = getDelegate(false);
            return new SetIterator(
                getDelegate(false).iterator(),
                delegate != this.nonTransactional
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return getDelegate(false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(Object o) {
            return getDelegate(true).add(o);
        }

        //--------------------------------------------------------------------
        // Implements Flushable
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush(
        ) throws IOException {
            Set<Object> source = getDelegate(false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.addAll(source);
            }
        }

        //--------------------------------------------------------------------
        // Class MarshalledPersistentValues
        //--------------------------------------------------------------------

        /**
         * Persistent Values Accessor
         */
        @SuppressWarnings("unchecked")
        class NonTransactional extends MarshallingSet<Object> {

            /**
             * Constructor 
             * 
             * @param marshaller 
             *
             * @throws ServiceException 
             */
            NonTransactional(
                Marshaller marshaller
            ) throws ServiceException {
                super(
                    marshaller, 
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -5790864813300543661L;

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingCollection#getDelegate()
             */
            @Override
            protected Collection getDelegate() {
                return getPersistentCollection(ManagedSet.this.feature, false);
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingCollection#clear()
             */
            @Override
            public void clear() {
                getPersistentCollection(ManagedSet.this.feature, true);
            }

        }


        //--------------------------------------------------------------------
        // Class SetIterator
        //--------------------------------------------------------------------

        /**
         * 
         */
        class SetIterator implements Iterator<Object> {

            SetIterator(
                Iterator<?> delegate,
                boolean transactional
            ){
                this.delegate = delegate;
                this.transactional = transactional;
            }

            /**
             * 
             */
            private final Iterator<?> delegate;

            /**
             * 
             */
            private final boolean transactional;

            /**
             * 
             */
            private Object current = null;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Object next() {
                return this.current = this.delegate.next();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                if(this.transactional) {
                    this.delegate.remove();
                } else if(this.current == null){
                    throw new IllegalStateException(
                        "There is no current element to be removed"
                    );
                } else {
                    getDelegate(true).remove(this.current);
                    this.current = null;
                }

            }

        }

    }

    //------------------------------------------------------------------------
    // Class ManagedSparseArray
    //------------------------------------------------------------------------

    /**
     * The managed sparse array delegates<ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedSortedMap
    extends TreeMap<Integer,Object>
    implements SortedMap<Integer,Object>, Flushable
    {

        /**
         * Constructor 
         *
         * @param feature
         * @param transactional
         * @param marshaller 
         * 
         * @throws ServiceException
         */
        ManagedSortedMap(
            final String feature,
            Marshaller marshaller
        ) throws ServiceException {
            this.feature = feature;
            SortedMap<Integer,Object> nonTransactional = new PopulationMap<Object>(){

                /* (non-Javadoc)
                 * @see org.openmdx.compatibility.base.collection.PopulationMap#getDelegate()
                 */
                @Override
                protected SparseList<Object> getDelegate() {
                    return getPersistentCollection(feature, false);
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractMap#clear()
                 */
                @Override
                public void clear() {
                    getPersistentCollection(feature, true);
                }

            }; 
            this.nonTransactional = new MarshallingSortedMap(
                marshaller,
                nonTransactional
            );
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -5406002308998595406L;

        /**
         * 
         */
        final String feature;

        /**
         * 
         */
        private final SortedMap<Integer,Object> nonTransactional;


        //--------------------------------------------------------------------
        // Implements Flushable
        //--------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        @SuppressWarnings("unchecked")
        public void flush(
        ) throws IOException{
            try {
                UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(null);
                if(unitOfWork != null) {
                    TransactionalState_1 state = unitOfWork.getState(Object_1.this, true);
                    if(state != null) {
                        SortedMap<Integer,Object> transactional = (SortedMap<Integer,Object>) state.values(true).get(this.feature);
                        if(transactional != null) {
                            this.nonTransactional.clear();
                            this.nonTransactional.putAll(transactional);
                        }
                    }
                } else if (transientValues != null) {
                    SortedMap<Integer,Object> transientValue = (SortedMap<Integer,Object>) transientValues.get(this.feature);
                    if(transientValue != null) {
                        this.nonTransactional.clear();
                        this.nonTransactional.putAll(transientValue);
                    }
                }
            } catch (ServiceException exception) {
                throw new ExtendedIOException(exception);
            }
        }
    }

    //------------------------------------------------------------------------
    // Class Operation
    //------------------------------------------------------------------------

    /**
     * Operation
     */
    final class Operation extends MarshallingStructure_1 {

        /**
         * Constructor 
         *
         * @param operation
         * @param arguments
         * @throws ServiceException
         */
        @SuppressWarnings("unchecked")
        Operation(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            super(
                null // delegate
            );
            this.operation = operation;
            String argumentsType = arguments.objGetType();
            this.arguments=Object_1.createMappedRecord(argumentsType);
            for(String fieldName : arguments.objFieldNames()){
                Marshaller marshaller = Object_1.this.getMarshaller(
                    argumentsType,
                    "field", 
                    fieldName
                );
                Object source = arguments.objGetValue(fieldName);
                if(source instanceof Collection){
                    if(source instanceof List){
                        List<?> collection = (List<?>)source;
                        List<Object> target = new ArrayList<Object>();
                        for(Object j : collection){
                            target.add(
                                marshaller.unmarshal(j)
                            );
                        }
                        this.arguments.put(fieldName, target);
                    }else if(source instanceof SortedMap){
                        SortedMap<Integer,Object> target = new TreeMap<Integer,Object>();
                        for(Map.Entry<?,?> k : ((SortedMap<?,?>)source).entrySet()) {
                            target.put(
                                (Integer)k.getKey(),
                                marshaller.unmarshal(k.getValue())
                            );
                        }
                        this.arguments.put(fieldName, target);
                    }else if(source instanceof Set){
                        Set<Object> target = new HashSet<Object>();
                        for(Object j : (Set<?>)source) {
                            target.add(
                                marshaller.unmarshal(j)
                            );
                        }
                        this.arguments.put(fieldName, target);
                    }else{
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ASSERTION_FAILURE,
                            "Structure_1_0 field has an invalid Collection class",
                            new BasicException.Parameter("class",source.getClass().getName())
                        );
                    }
                }else{
                    this.arguments.put(fieldName, marshaller.unmarshal(source));
                }
            }
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3257849887319143218L;

        /**
         * The operation name
         */
        final String operation;

        /**
         * The operation arguments
         */
        final MappedRecord arguments;

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.MarshallingStructure_1#getMarshaller(java.lang.String)
         */
        @Override
        protected Marshaller getMarshaller(
            String feature
        ) throws ServiceException {
            return Object_1.this.getMarshaller(objGetType(), "field", feature);
        }

        /**
         * Invoke the operation
         * 
         * @throws ServiceException
         */
        void invoke(
        ) throws ServiceException {
            try {
                super.setDelegate(
                    new MapStructure_1(
                        channel.invokeOperation(
                            identity,
                            this.operation,
                            this.arguments
                        )
                    )
                );
            } catch(ServiceException e) {
                throw e;
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    exception instanceof BasicException.Wrapper ? ((BasicException.Wrapper)exception).getCause().getExceptionDomain() : BasicException.Code.DEFAULT_DOMAIN,
                    exception instanceof BasicException.Wrapper ? ((BasicException.Wrapper)exception).getCause().getExceptionCode() : BasicException.Code.GENERIC,
                    "Operation failed",
                    new BasicException.Parameter("object", identity),
                    new BasicException.Parameter("operation",this.operation),
                    new BasicException.Parameter("arguments",this.arguments)
                );
            }
        }

    }
    
    //------------------------------------------------------------------------
    // Class DelegatingContainer
    //------------------------------------------------------------------------

    /**
     * Delegating Container
     */
    @SuppressWarnings("unchecked")
    public class DelegatingContainer
        extends AbstractMap<String, DataObject_1_0>  
        implements Serializable, Delegating_1_0, Container_1_0, FetchSize, Flushable, Evictable
    {

        /**
         * Constructor 
         */
        public DelegatingContainer(
        ) {
            this.feature = ".";
            this.delegate = null;
            this.collection = this;
        }

        /**
         * Constructor 
         *
         * @param feature
         * @param delegate
         */
        @SuppressWarnings("deprecation")
        DelegatingContainer(
            String feature, 
            org.openmdx.base.collection.Container<DataObject_1_0> delegate
        ) {
            this.feature = feature;
            this.delegate = delegate;
            this.collection = this;
        }

        /**
         * Constructor 
         *
         * @param feature
         * @param container
         */
        @SuppressWarnings("deprecation")
        DelegatingContainer(
            String feature, 
            org.openmdx.base.collection.Container<DataObject_1_0> container,
            DelegatingContainer collection
        ) {
            this.feature = feature;
            this.delegate = container;
            this.collection = collection;
        }
        
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3907208269167474229L;

        /**
         * 
         */
        private final String feature;
                
        /**
         * @serial
         */
        @SuppressWarnings("deprecation")
        private org.openmdx.base.collection.Container<DataObject_1_0> delegate;

        /**
         * @serial
         */
        protected DelegatingContainer collection;
        
        /* (non-Javadoc)
         */
        public void clear() {
            values().clear();
        }

        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public boolean containsKey(Object key) {
            return values().get(key) != null;
        }

        /* (non-Javadoc)
         */
        public boolean containsValue(Object value) {
            return values().contains(value);
        }

        /* (non-Javadoc)
         */
        public Set entrySet() {
            return new MarshallingSet(
                ContainerMarshaller.getInstance(),
                values()
            ); 
        }

        /**
         * Test whether lookup for this object has already failed in the same
         * unit of work.
         * 
         * @param key
         * 
         * @return <code>true</code> if lookup for this object has already 
         * failed in the same unit of work
         */
        private boolean doesNotExist(
            Object key
        ){
            try {
                UnitOfWork_1 unitOfWork = getUnitOfWork();
                if(unitOfWork.isActive()) {
                    TransactionalState_1 state = unitOfWork.getState(Object_1.this, true);
                    if(state != null) {
                        Set<?> notFound = (Set<?>) state.values(true).get(this.feature);
                        return notFound != null && notFound.contains(key);
                    }
                }
                return false;
            } catch (ServiceException ignore) {
                return false;
            }
        }
        
        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public DataObject_1_0 get(Object key) {
            return doesNotExist(key) ? null : values().get(key); 
        }

        /* (non-Javadoc)
         */
        public boolean isEmpty() {
            return values().isEmpty();
        }

        /* (non-Javadoc)
         */
        public DataObject_1_0 put(String key, DataObject_1_0 value) {
            try {
                ((Object_1)value).objMove(this, key);
                return null;
            } catch (ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }

        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public DataObject_1_0 remove(Object key) {
            DataObject_1_0 object = values().get(key);
            if(object != null) try {
                if(object.jdoIsPersistent()) {
                    object.objGetClass(); // validation
                    JDOHelper.getPersistenceManager(object).deletePersistent(object);
                } else {
                    object.objMove(null, null);
                }
            } catch (ServiceException exception) {
                return null;
            }
            return object;
        }

        /* (non-Javadoc)
         */
        public int size() {
            return values().size();
        }

        /* (non-Javadoc)
         */
        public FilterableMap subMap(
            final Object filter
        ) {
            return new DelegatingContainer(
                this.feature, 
                this.delegate,
                this
            ){

                /**
                 * Implements <code>Serializable</code>
                 */
                private static final long serialVersionUID = 3447804548754409495L;

                /* (non-Javadoc)
                 * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Object_1.DelegatingContainer#values()
                 */
                @Override
                @SuppressWarnings("deprecation")
                public org.openmdx.base.collection.Container<DataObject_1_0> values() {
                    return super.collection.values().subSet(filter);
                }
                
            };
        }

        /* (non-Javadoc)
         */
        public String toString() {
            return values().toString();
        }

        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public org.openmdx.base.collection.Container<DataObject_1_0> values() {
            return objGetDelegate();
        }

        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public List values(Object criteria) {
            return values().toList(criteria);
        }

        /**
         * Retrieve the super-set
         * 
         * @return the super-set
         */
        public DelegatingContainer superSet(
        ){
            return getClass() == DelegatingContainer.class ? this : new DelegatingContainer(
                this.feature, 
                this.delegate
            );
        }        
        
        @SuppressWarnings("deprecation")
        public Object getContainerId(
        ) {
            org.openmdx.base.collection.Container<DataObject_1_0> delegate = this.objGetDelegate();
            Object containerId = delegate instanceof Container_1_0 ?
                ((Container_1_0)delegate).getContainerId() :
                    delegate instanceof AbstractContainer ?
                    ((AbstractContainer)delegate).getContainerId() :
                     null;
            return containerId != null ?
                containerId :                
                Object_1.this.transientObjectId != null ? 
                    Object_1.this.transientObjectId.getChild(this.feature) :
                    null;
        }
        
        String getFeatureName(){
            return this.feature;
        }
                
        //------------------------------------------------------------------------
        // Implements FetchSize
        //------------------------------------------------------------------------

        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public int getFetchSize(
        ) {
            org.openmdx.base.collection.Container<DataObject_1_0> container = objGetDelegate();
            if(container instanceof FetchSize){
                this.fetchSize = ((FetchSize)container).getFetchSize();
            }
            return this.fetchSize;
        }

        /* (non-Javadoc)
         */
        @SuppressWarnings("deprecation")
        public void setFetchSize(
            int fetchSize
        ){
            this.fetchSize = fetchSize;
            org.openmdx.base.collection.Container<DataObject_1_0> container = objGetDelegate();
            if(container instanceof FetchSize){
                ((FetchSize)container).setFetchSize(fetchSize);
            }
        }

        /**
         * The proposed fetch size
         */
        private int fetchSize = DEFAULT_FETCH_SIZE;
        
        //------------------------------------------------------------------------
        // Implements Flushable
        //------------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        @SuppressWarnings("deprecation")
        public void flush(
        ) throws IOException {
            org.openmdx.base.collection.Container<DataObject_1_0> container = objGetDelegate();
            if(container instanceof Flushable) {
                ((Flushable)container).flush();
            }

        }
        
        //------------------------------------------------------------------------
        // Implements Evictable
        //------------------------------------------------------------------------

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        @SuppressWarnings("deprecation")
        public void evict() {
            org.openmdx.base.collection.Container<DataObject_1_0> container = objGetDelegate();
            if(container instanceof Evictable) {
                ((Evictable)container).evict();
            }
        }
        
        //------------------------------------------------------------------------
        // Implements Delegating_1_0
        //------------------------------------------------------------------------

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
         */
        @SuppressWarnings("deprecation")
        public final org.openmdx.base.collection.Container<DataObject_1_0> objGetDelegate(
        ){
            return this.delegate == null ? getDelegateContainer() : this.delegate;
        }

        @SuppressWarnings("deprecation")
        final void objSetDelegate(
            org.openmdx.base.collection.Container<DataObject_1_0> delegate
        ){
            this.delegate = delegate;
        }
        
    }
        
    //------------------------------------------------------------------------
    // Class ContainerMarshaller
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    @SuppressWarnings("unchecked")
    static class ContainerMarshaller
        implements Marshaller
    {

        /* (non-Javadoc)
         */
        public Object marshal(Object source) throws ServiceException {
            return source instanceof DataObject_1_0 ? 
                new ContainerEntry((DataObject_1_0) source) :
                    source;
        }

        /* (non-Javadoc)
         */
        public Object unmarshal(Object source) throws ServiceException {
            return source instanceof Map.Entry ? 
                ((Map.Entry) source).getValue() :
                    source;
        }  

        /* (non-Javadoc)
         */
        static Marshaller getInstance(){
            return instance;
        }

        /* (non-Javadoc)
         */
        static Marshaller instance = new ContainerMarshaller();

    }

    //------------------------------------------------------------------------
    // Class ContainerEntry
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    @SuppressWarnings("unchecked")
    static class ContainerEntry implements Map.Entry {

        /* (non-Javadoc)
         */
        private final DataObject_1_0 value;

        /* (non-Javadoc)
         */
        ContainerEntry(
            DataObject_1_0 value
        ){
            this.value = value;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getKey()
         */
        public Object getKey() {
            if(value.jdoIsPersistent()) {
                return ((Path)value.jdoGetObjectId()).getBase();
            } else if (value instanceof Object_1) {
                return ((Object_1)value).getQualifier();
            } else {
                return null;
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getValue()
         */
        public Object getValue() {
            return this.value;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

    }
    
    //--------------------------------------------------------------------------
    // Class ValueView
    //--------------------------------------------------------------------------

    /**
     * Value Collection
     */
    class ValueCollection implements Iterable<Object> {

        /**
         * Constructor 
         *
         * @param featureName
         */
        ValueCollection(
            String featureName
        ){
            this.featureName = featureName;     
        }
        
        /**
         * 
         */
        private final String featureName;

        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        public Iterator<Object> iterator() {
            try {
                Object value = objGetValue(this.featureName);
                return (
                    value == null ? Collections.emptySet() : Collections.singleton(value)
                ).iterator();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
    }

    @SuppressWarnings("unchecked")
    static class MapStructure_1
    implements Serializable, Structure_1_0 
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3978429117693702704L;

        /**
         * Constructor
         *
         * @param       structureClass
         *              The model class of the structure to be created
         * @param       fieldMappings
         *              maps field names to field values
         */
        MapStructure_1(
            MappedRecord record
        ){
            this.record = record;
        }


        //------------------------------------------------------------------------
        // Implements Structure_1_0
        //------------------------------------------------------------------------

        /**
         * Returns the structure's model class.
         *
         * @return  the structure's model class
         */
        public String objGetType(
        ){
            return this.record.getRecordName();
        }

        /**
         * Return the field names in this structure.
         *
         * @return  the (String) field names contained in this structure
         */
        public List objFieldNames(
        ){
            if(this.fieldNames==null)this.fieldNames = Collections.unmodifiableList(
                new ArrayList(this.record.keySet())
            );
            return this.fieldNames;
        }

        /**
         * Get a field.
         *
         * @param       field
         *              the fields's name
         *
         * @return      the fields value which may be null.
         *
         * @exception   ServiceException BAD_MEMBER_NAME
         *              if the structure has no such field
         */
        public Object objGetValue(
            String fieldName
        ) throws ServiceException {
            Object value = this.record.get(fieldName);
            if(
                    value == null &&
                    !this.record.containsKey(fieldName)
            ) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_MEMBER_NAME,
                "This structure has no such field",
                new BasicException.Parameter("fieldName", fieldName)
            );
            return value;
        }


        //------------------------------------------------------------------------
        // Extends Object
        //------------------------------------------------------------------------

        public String toString(
        ){
            return this.record.toString();
        }


        //------------------------------------------------------------------------
        // Instance Members
        //------------------------------------------------------------------------

        /**
         * The structure's content
         */
        protected final MappedRecord record;

        /**
         * Field
         */  
        private transient List fieldNames = null;

    }
    
    /**
     * DurationMarshaller
     */
    abstract static class DatatypeMarshaller
        implements Marshaller
    {

        /**
         * Constructor 
         */
        protected DatatypeMarshaller() {
            super();
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
         */
        public Object unmarshal(
            Object source
        ){
            return DatatypeFactories.immutableDatatypeFactory().toBasicFormat(source);
        }

    }
    
    /**
     * Date Marshaller
     */
    static class DateMarshaller extends DatatypeMarshaller {

        /**
         * Constructor 
         */
        private DateMarshaller() {
            super();
        }

        /**
         * A singleton
         */
        private final static Marshaller instance = new DateMarshaller();

        /**
         * Retrieve an instance
         * 
         * @return an instance
         */
        public static Marshaller getInstance(){
            return instance;
        }
        
        /* (non-Javadoc)
         * @see oorg.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(Object source) throws ServiceException {
            try {
                return DatatypeFactories.immutableDatatypeFactory().newDate((String) source);
            } catch (RuntimeException exception) {
                throw new ServiceException(exception);
            }
        }

    }

    /**
     * Date/Time Marshaller
     */
    static class DateTimeMarshaller extends DatatypeMarshaller {

        /**
         * Constructor 
         */
        private DateTimeMarshaller() {
            super();
        }

        /**
         * A singleton
         */
        private final static Marshaller instance = new DateTimeMarshaller();

        /**
         * Retrieve an instance
         * 
         * @return an instance
         */
        public static Marshaller getInstance(){
            return instance;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(Object source) throws ServiceException {
            try {
                return DatatypeFactories.immutableDatatypeFactory().newDateTime((String) source);
            } catch (RuntimeException exception) {
                throw new ServiceException(exception);
            }
        }

    }
    
    /**
     * DurationMarshaller
     */
    static class DurationMarshaller extends DatatypeMarshaller {

        /**
         * Constructor 
         */
        private DurationMarshaller() {
            super();
        }

        /**
         * A singleton
         */
        private final static Marshaller instance = new DurationMarshaller();

        /**
         * Retrieve an instance
         * 
         * @return an instance
         */
        public static Marshaller getInstance(){
            return instance;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(Object source) throws ServiceException {
            try {
                return DatatypeFactories.immutableDatatypeFactory().newDuration((String) source);
            } catch (RuntimeException exception) {
                throw new ServiceException(exception);
            }
        }

    }
    
    /**
     * Constructor for transient objects
     * @param       objectClass
     *              The model class id
     * @param       transientObjectId 
     *
     * @exception   ServiceException  BAD_PARAMETER
     *              if objectClass is null
     */
    Object_1(
        String objectClass,
        Connection_1 manager, 
        Path transientObjectId
    ) throws ServiceException{
        this.identity = null;
        this.connection = manager;
        this.channel = null;
        this.deleted = false;
        this.transactionalValuesRecordName = objectClass;
        this.transientValues = new HashMap<String,Object>();
        this.transientObjectId = transientObjectId;
        if(objectClass == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                null,
                "Argument objectClass is null"
            );
        }
    }

    /**
     * Constructor for persistent objects
     */
    Object_1(
        Path identity,
        Connection_1 objectFactory,
        Channel channel
    ) throws ServiceException{
        this.identity = identity;
        this.channel = channel;
        this.connection = objectFactory;
        this.deleted = false;
        this.transactionalValuesRecordName = null;
        this.transientValues = null;
        if(
            identity == null || objectFactory == null || channel == null
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Invalid null argument",
                new BasicException.Parameter("accessPath",identity == null ? "null" : ""),
                new BasicException.Parameter("manager",identity == null ? "null" : ""),
                new BasicException.Parameter("provider",identity == null ? "null" : "")
            );
        }
    }

    /**
     * Constructor
     *
     * @param identity
     * @param that
     * @param completelyDirty
     * @throws ServiceException
     */
    Object_1(
        Path identity,
        Object_1 that,
        boolean completelyDirty
    ) throws ServiceException{
        if(
            that.transactionalValuesRecordName == null &&
            that.objIsHollow()
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                null,
                "The object is hollow"
            );
        }
        this.channel = that.channel;
        this.connection = that.connection;
        if(identity != null) {
            propagate(that, completelyDirty);
            int p = identity.size() - 2;
            DataObject_1_0 parent = (DataObject_1_0)this.connection.getObjectById(identity.getPrefix(p));
            Container_1_0 there = parent.objGetContainer(identity.get(p));
            objMove(
                there,
                identity.get(p + 1)
            );
        } else {
            this.transientValues = new HashMap<String,Object>();
            propagate(that, completelyDirty);
            if(this.connection.getModel().isInstanceof(this, "org:openmdx:base:ExtentCapable")) {
                this.transientObjectId = StateCapables.newTransientObjectId();
                this.connection.cacheObject(this.transientObjectId, this);  
            }
        }
    }

    /**
     * Add a new value unless it has already been set
     * 
     * @param key the feature name
     * @param value the new value to be used if no previous value is set
     * @return the previous value if one exists, the new value otherwise
     */
    private final Flushable putFlushable(
        String key,
        Flushable value
    ){
        Flushable concurrent = this.flushableValues.putIfAbsent(
            key,
            value
        );
        return concurrent == null ? value : concurrent;
    }
    
    /**
     * Retrieve the object's transatcional state
     * 
     * @param optional
     * 
     * @return the object's transactional state
     * 
     * @throws ServiceException
     */
    final TransactionalState_1 getState(
        boolean optional
    ) throws ServiceException{
        return optional && (this.connection == null || this.connection.isClosed()) ? 
            null :
                getUnitOfWork().getState(this,optional);        
    }

    @SuppressWarnings("unchecked")
    void setExistence(
        Path identity,
        boolean existence
    ) throws ServiceException{
        UnitOfWork_1 unitOfWork = getUnitOfWork();
        if(unitOfWork.isActive()) {
            if(!identity.startsWith(this.identity) || identity.size() - this.identity.size() != 2) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "The given path does not refer to a potential child",
                    new BasicException.Parameter("object", this.identity),
                    new BasicException.Parameter("child", identity),
                    new BasicException.Parameter("existence", existence)
                );
            }
            Set<String> notFound = null;
            TransactionalState_1 state = unitOfWork.getState(Object_1.this, existence);
            if(state != null) {
                String feature = identity.get(identity.size() - 2);
                Map<String,Object> values = state.values(existence);
                notFound = (Set<String>) values.get(feature);
                if(notFound == null && !existence) {
                    values.put(
                        feature,
                        notFound = new HashSet<String>()
                    );
                }
            }
            if(notFound != null) {
                String qualifier = identity.get(identity.size() - 1);
                if(existence) {
                    notFound.remove(qualifier);
                } else {
                    notFound.add(qualifier);
                }
            }
        }
    }

    /**
     * 
     * @param that
     * @param completelyDirty
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    void propagate(
        Object_1 that,
        boolean completelyDirty
    ) throws ServiceException{
        this.deleted = false;
        this.digest = null;
        //
        // Record Name
        //
        this.transactionalValuesRecordName = that.transactionalValuesRecordName != null ?
            that.transactionalValuesRecordName :
                that.persistentValues.getRecordName();
        // 
        // Persistent Values
        //
        if(objIsHollow()) {
            this.persistentValues = Object_1.createMappedRecord(this.transactionalValuesRecordName);
        }
        if(that.persistentValues != null) {
            this.persistentValues.putAll(that.persistentValues);
        }
        //
        // Transactional Values
        //
        TransactionalState_1 thisState = this.getState(false);
        TransactionalState_1 thatState = that.getState(true);
        if(thatState != null) {
            Set<String> thatDirty = thatState.dirtyFeatures(true);
            for(Map.Entry<String,Object> e : thatState.values(true).entrySet()){
                String feature = e.getKey();
                Object candidate = e.getValue();
                if(completelyDirty || thatDirty.contains(feature)) {
                    if(candidate instanceof Set) {
                        Set<Object> target = this.objGetSet(feature);
                        target.clear();
                        target.addAll((Set)candidate);
                    } else if (candidate instanceof List) {
                        List<Object> target = this.objGetList(feature);
                        target.clear();
                        target.addAll((List)candidate);
                    } else if (candidate instanceof SortedMap) {
                        SortedMap<Integer,Object> target = this.objGetSparseArray(feature);
                        target.clear();
                        target.putAll((SortedMap)candidate);
                    } else {
                        this.objSetValue(feature, candidate);
                    }
                }
            }
        }
        //
        // Transient Values
        //
        if (
            that.transientValues != null &&
            this.transientValues != null
         ) {
            this.transientValues.putAll(that.transientValues);
        } 
        //
        // Dirty Features
        //
        if(completelyDirty) {
            Set<String> thisDirty = thisState.dirtyFeatures(false);
            thisDirty.addAll(thisState.values(false).keySet());
            if(that.persistentValues != null) {
                thisDirty.addAll(that.persistentValues.keySet());
            }
            if(this.transientValues != null) {
                thisDirty.addAll(this.transientValues.keySet());
            }
        }
    }

    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     *
     * @exception   ServiceException
     *              if the information is unavailable
     */
    public String objGetClass(
    ) throws ServiceException {
        if(this.transactionalValuesRecordName == null) {
            try {
                assertObjectIsAccessible();
                this.transactionalValuesRecordName = this.persistentValues.getRecordName();
            } catch (NullPointerException exception){
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_AVAILABLE,
                    "Object class can't be determined"
                );
            }
        }
        return this.transactionalValuesRecordName;
    }

    /**
     * Returns the object's identity.
     *
     * @return    the object's identity;
     *            or null for transient objects
     */
    public Path jdoGetObjectId(
    ){
        return 
            this.identity != null ? this.identity :
            this.transientObjectId != null ? this.transientObjectId :
            null;
    }

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return  the names of the features in the default fetch group
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException{
        Set<String> result = new HashSet<String>();
        TransactionalState_1 state = getState(true);
        if(state != null) {
            result.addAll(state.values(true).keySet());
        } else if (this.transientValues != null) {
            result.addAll(this.transientValues.keySet());
        } 
        if(!objIsHollow()) {
            result.addAll(this.persistentValues.keySet());
        }            
        return result;
    }

    /**
     * Corresponds to the JDO version
     * 
     * @param source
     * 
     * @return the digest
     */
    private static byte[] getDigest(
        MappedRecord source
    ){
        SparseList<?> holder = (SparseList<?>)source.get(
            SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_DIGEST
        );
        return holder == null ? null : (byte[])holder.get(0);
    }

    /**
     * Get a sequence
     *
     * @param reference
     * @return
     * @throws ServiceException
     */
    long getSequence(
        String reference
    ) throws ServiceException{
        if(jdoIsNew()) {
            return AbstractContainer.SEQUENCE_MIN_VALUE;
        }
        assertObjectIsAccessible();
        SparseList<?> supported = (SparseList<?>)this.persistentValues.get(
            SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.SEQUENCE_SUPPORTED
        );
        if(supported != null && ((Boolean)supported.get(0)).booleanValue()) {
            SparseList<?> names = (SparseList<?>)this.persistentValues.get(
                SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.SEQUENCE_NAME
            );
            int index = names.indexOf(reference);
            if(index < 0) {
                return AbstractContainer.SEQUENCE_MIN_VALUE;
            }
            Long value = (Long) (
                    (SparseList<?>)this.persistentValues.get(
                        SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.SEQUENCE_NEXT_VALUE
                    )
            ).get(index);
            return value == null ?
                AbstractContainer.SEQUENCE_MIN_VALUE :
                    value.longValue();
        } else {
            return AbstractContainer.SEQUENCE_NOT_SUPPORTED;
        }
    }

    /**
     * Set the default fetch group
     * @param defaultFetchGroup
     *
     * @throws ServiceException
     */
    private void postLoad(
        MappedRecord defaultFetchGroup
    ) throws ServiceException {
        this.persistentValues = defaultFetchGroup;
        if(this.digest == null) {
            this.digest = getDigest(defaultFetchGroup);
        }
        fireInstanceCallback(InstanceCallbackEvent.POST_LOAD);
    }

    /**
     * Merge the current and newly retrieved values
     *
     * @param logMessage
     * @param primary
     * @param secondary
     * @param notify
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private void merge(
        String logMessage,
        MappedRecord primary,
        MappedRecord secondary,
        boolean notify
    ) throws ServiceException {
        MappedRecord target = Object_1.createMappedRecord(objGetClass());
        target.putAll(secondary);
        target.putAll(primary);
        this.persistentValues = target;
        byte[] digest = getDigest(primary);
        if(digest != null) {
            this.digest = digest;
        }
        if(notify) {
            fireInstanceCallback(InstanceCallbackEvent.POST_RELOAD);
        }
    }

    /**
     *
     * @param fetchGroup
     */
    void fetched(
        MappedRecord fetchGroup
    ){
        try {
            if(objIsHollow()){
                postLoad(fetchGroup);
            } else {
                merge(
                    "pre-loaded",
                    this.persistentValues,
                    fetchGroup,
                    false
                );
            }
        } catch (Exception exception) {
            new RuntimeServiceException(exception).log();
        }
    }

    /**
     *
     * @param pushGroup
     */
    void updated(
        MappedRecord pushGroup
    ){
        if(this.refreshAsynchronously) {
            try {
                if(pushGroup == null) {
                    evict();
                } else if(objIsHollow()){
                    postLoad(pushGroup);
                } else {
                    merge(
                        "updated",
                        pushGroup,
                        this.persistentValues,
                        true // notify
                    );
                }
            } catch (Exception exception) {
                new RuntimeServiceException(exception).log();
            }
        }
    }

    /**
     *
     * @param makeInaccessible
     */
    public void invalidate(
        boolean makeInaccessible
    ){
        if(makeInaccessible) {
            try {
                evict();
            } catch (Exception e) {
                // Just ignore it
            }
            this.connection = null;
        }
        this.identity = null;
        this.channel = null;
    }

    /**
     * Ask the persistence framework for the object's content
     */
    private void load(
    ) throws ServiceException {
        try {
            postLoad(
                this.channel.getDefaultFetchGroup(
                    this.identity,
                    null,
                    this.connection
                )
            );
            this.inaccessabilityReason = null;
        } catch (ServiceException exception){            
            this.inaccessabilityReason = exception;
            if(this.connection != null && exception.getExceptionCode() == BasicException.Code.NOT_FOUND){
                this.connection.invalidate(this.identity, true);
            } else {
                this.invalidate(true);
            }
            throw exception;
        }
    }

    /**
     * Refresh the state of the instance from its provider.
     *
     * @exception   ServiceException
     *              if the object can't be synchronized
     */
    public void objRefresh(
    ) throws ServiceException {
        if(jdoIsPersistent() && !jdoIsNew()) {
            TransactionalState_1 state = getState(true);
            if(state != null) {
                state.clear();
            }
            load();
        }
    }

    @SuppressWarnings("deprecation")
    final org.openmdx.base.collection.Container<DataObject_1_0> getDelegateContainer(
    ){
        return this.transientContainer == null ? this.persistentContainer : this.transientContainer;
    }
    
    /**
     * Retrieve this object's persistent aspects
     * 
     * @return the persistent aspects
     * 
     * @throws ServiceException 
     */
    FilterableMap<String, DataObject_1_0> getContainer(
    ) throws ServiceException{
        if(this.container == null) {
            if(
                this.persistentContainer != null ||
                this.transientContainer != null
            ){
                this.container = new DelegatingContainer();
            } else if(this.jdoIsPersistent()) {
                Path identity = jdoGetObjectId();
                int size = identity.size() - 2;
                if(size > 0) {
                    this.container = ((DataObject_1_0)this.connection.getObjectById(
                        identity.getPrefix(size)
                    )).objGetContainer(
                        identity.get(size)
                    );
                }
            }
        }
        return this.container;
    }
    
    String getQualifier(){
        return jdoIsPersistent() ? jdoGetObjectId().getBase() : this.qualifier;
    }

    
    //------------------------------------------------------------------------
    // Unit of work boundaries
    //------------------------------------------------------------------------

    /**
     * Retrieve the unit of work
     * 
     * @return the layer specific unit of work
     * 
     * @throws ServiceException 
     */
    final UnitOfWork_1 getUnitOfWork(
    ) throws ServiceException {
        if(this.connection != null) {
            return this.connection.getUnitOfWork();
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Invalid object. Can not get unit of work.",
                new BasicException.Parameter("xri", this.identity)
            );
        }
    }

    /**
     * Retrieve the unit of work
     * 
     * @param a feature to be marked dirty, or <code>null</code>
     * 
     * @return the unit of work or <ocde>null</code> for non-transactional access
     * 
     * @throws ServiceException 
     */
    final UnitOfWork_1 getUnitOfWorkIfTransactional(
        String makeDirty
    ) throws ServiceException {
        UnitOfWork_1 unitOfWork = getUnitOfWork(); 
        boolean transactional = makeDirty != null && jdoIsPersistent();
        if(transactional) {
            addTo(unitOfWork);
        } else {
            transactional = unitOfWork.contains(this);
        }
        if(transactional) {
            if(makeDirty != null) {
                TransactionalState_1 state = unitOfWork.getState(this,false);
                state.setPrepared(false);
                state.dirtyFeatures(false).add(makeDirty);
            }
            return unitOfWork;
        } else {
            return null;
        }
    }

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException
     *              if the object can't be added to the unit of work for
     *                another reason.
     */
    public void objAddToUnitOfWork(
    ) throws ServiceException {
        addTo(getUnitOfWork()); 
    }

    /**
     * Add to unit of work
     * 
     * @param unitOfWork
     * @throws ServiceException 
     */
    private final void addTo(
        UnitOfWork_1 unitOfWork
    ) throws ServiceException{
        if(unitOfWork.add(this)) {
            if(this.transientValues == null){
                assertObjectIsAccessible();
            } else {
                unitOfWork.getState(this,false).setValues(this.transientValues);
                this.transientValues = null;
            }
        }
    }
    
    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty.
     * @exception   ServiceException
     *              if the object can't be removed from its unit of work for
     *                another reason
     */
    public void objRemoveFromUnitOfWork(
    ) throws ServiceException {
        if(jdoIsDirty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                null,
                "A dirty object can't be removed from the unit of work"
            );
        }
        getUnitOfWork().remove(this);
    }


    //------------------------------------------------------------------------
    // Life Cycle Operations
    //------------------------------------------------------------------------

    /**
     * This method clones an object
     * @param original
     * @return the clone
     * 
     * @throws ServiceException
     */
    public DataObject_1_0 openmdxjdoClone(
    ) {
        try {
            return new Object_1(
                null,
                this,
                true
            );
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to clone object",
                e,
                this
            );
        }
    }
    
    /**
     * The move operation moves the object to the scope of the container
     * passed as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is not an instance of <code>DelegatingContainer</code>.
     * @exception ServiceException
     *            if the move operation fails.
     */
    public void objMove(
        Container_1_0 there,
        String criteria
    ) throws ServiceException{
        if(jdoIsPersistent() && !jdoIsNew()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Attempt to move a persistent object",
                new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY,jdoGetObjectId().toUri())
            );
        }
        if(there != null && !(there instanceof DelegatingContainer)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "There is not an instance of DelegatingContainer",
                new BasicException.Parameter("there",there == null ? null : there.getClass().getName()),
                new BasicException.Parameter("criteria",criteria)
            );
        }
        @SuppressWarnings("deprecation")
        org.openmdx.base.collection.Container<?> container = there == null ? null : ((DelegatingContainer)there).objGetDelegate();
        if(this.transientContainer != null){
            this.transientContainer.remove(this);
            this.transientContainer = null;
        }
        if(container instanceof PersistentContainer_1){
            TransactionalState_1 state = getState(false);
            state.setLifeCycleEventPending(true);
            this.persistentContainer=(PersistentContainer_1)container;
            if(this.container instanceof DelegatingContainer) {
                ((DelegatingContainer)this.container).objSetDelegate(this.persistentContainer);
            }
            Path containerPath = persistentContainer.getReferenceFilter();
            PathComponent qualifier = criteria == null ? null : new PathComponent(criteria);
            String base;
            if(qualifier == null) {
                if(getUnitOfWork().isOptimistic()){
                    base = PathComponent.createPlaceHolder().toString();
                } else {
                    base = createDefaultQualifier(
                        (AbstractContainer<?>)container,
                        this.connection.getDefaultQualifierType(),
                        qualifier
                    );
                }
            } else if (qualifier.isPlaceHolder()){
                if(
                    qualifier.size() > 2 &&
                    this.connection.getModel().isInstanceof(this, StateCapable_1.CLASS)
                ){
                    DataObject_1_0 core = getCore();
                    Integer id = (Integer) core.objGetValue(SystemAttributes.VERSION);
                    if(id == null) {
                        //
                        // Just in case...
                        //
                        id = Integer.valueOf(0);
                    }
                    base = new PathComponent(
                        qualifier.getParent().getSuffix(1)
                    ) + "!" + id;
                    core.objSetValue(
                        SystemAttributes.VERSION,
                        Integer.valueOf(id.intValue() + 1)
                    );
                } else if(getUnitOfWork().isOptimistic()){
                    base = criteria;
                } else {
                    base = createDefaultQualifier(
                        (AbstractContainer<?>)container,
                        this.connection.getDefaultQualifierType(),
                        qualifier
                    );
                }
            } else {
                base = criteria;
            }
            this.identity = containerPath.getChild(base);
            if(this.connection.containsKey(this.identity)){
                try {
                    ((DataObject_1_0)this.connection.getObjectById(this.identity)).objGetClass();
                } catch (Exception exception) {
                    // The candidate could be evicted now
                }
                if(this.connection.containsKey(this.identity)){
                    String resourceIdentifier = this.identity.toResourceIdentifier();
                    this.identity = null;
                    state.setLifeCycleEventPending(false);
                    this.persistentContainer = null;
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DUPLICATE,
                        "There is already an object with the same qualifier in the container",
                        new BasicException.Parameter("containerIdentifier",containerPath.toResourceIdentifier()),
                        new BasicException.Parameter("resourceIdentifier",resourceIdentifier)
                    );
                }
            }
            Object_1 parent = (Object_1) this.connection.getObjectById(containerPath.getParent()); 
            parent.objAddToUnitOfWork();
            parent.setExistence(this.identity, true);
            if(this.transientObjectId == null) {
                this.connection.cacheObject(this.identity, this);
            } 
            else {
                this.connection.move(this.transientObjectId,this.identity);
                this.transientObjectId = null;
            }
            this.fireInstanceCallback(InstanceCallbackEvent.POST_CREATE);
            objAddToUnitOfWork();
            for(Map.Entry<String,Flushable> e : this.flushableValues.entrySet()){
                Flushable flushable = e.getValue();
                if(flushable instanceof DelegatingContainer){
                    DelegatingContainer delegatingContainer = (DelegatingContainer)flushable;
                    if(delegatingContainer.objGetDelegate() instanceof TransientContainer_1) {
                        Path referenceIdentity;
                        String q = this.identity.getBase();
                        int i = q.lastIndexOf('!');
                        if(i > 0) {
                            referenceIdentity = this.identity.getParent().getDescendant(
                                q.substring(0, i),
                                e.getKey()
                            );
                        } else {
                            referenceIdentity = this.identity.getChild(e.getKey());
                        }
                        TransientContainer_1 source = (TransientContainer_1)delegatingContainer.objGetDelegate();
                        DelegatingContainer target = delegatingContainer;
                        Object_1 parentOfTarget = this;
                        DataObject_1_0 core = getCore();
                        if(core != null) {
                            Object value = core.objGetContainer(delegatingContainer.getFeatureName());
                            if(value instanceof DelegatingContainer) {
                                target = (DelegatingContainer) value;
                                parentOfTarget = (Object_1) core;
                                delegatingContainer.objSetDelegate(null);
                            }
                        }
                        target.objSetDelegate(
                            new PersistentContainer_1(
                                referenceIdentity,
                                this.persistentContainer.getManager(),
                                this.persistentContainer.getProvider(),
                                new EvictablePersistentObjects(e.getKey()),
                                parentOfTarget
                            )
                        );
                        prepare(
                            source.getEntrySet(),
                            target,
                            false // aspect
                        );
                        prepare(
                            source.getEntrySet(),
                            target,
                            true // aspect
                        );
                    }
                }
            }
        } else if(container!=null){
            this.transientContainer = (TransientContainer_1)container;
            this.transientContainer.add(criteria,this);
            this.qualifier = criteria;
            if(this.container instanceof DelegatingContainer) {
                ((DelegatingContainer)this.container).objSetDelegate(this.transientContainer);
            }
            if(this.connection.getModel().isInstanceof(this, Aspect_1.CLASS)){
                Object value = this.objGetValue(Aspect_1.CORE);
                if(value instanceof Object_1) {
                    Object_1 core = (Object_1) value;
                    for(Map.Entry<String,Flushable> e : this.flushableValues.entrySet()){
                        Flushable flushable = e.getValue();
                        if(flushable instanceof DelegatingContainer){
                            DelegatingContainer delegatingContainer = (DelegatingContainer)flushable;
                            value = delegatingContainer.objGetDelegate();
                            if(value instanceof TransientContainer_1) {
                                TransientContainer_1 transientContainer = (TransientContainer_1) value;
                                value = core.objGetContainer(delegatingContainer.getFeatureName());
                                if(value instanceof DelegatingContainer) {
                                    DelegatingContainer target = (DelegatingContainer) value;
                                    delegatingContainer.objSetDelegate(null);
                                    target.objSetDelegate(transientContainer);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private DataObject_1_0 getCore(
    ) throws ServiceException{
        return this.connection.getModel().isInstanceof(this, Aspect_1.CLASS) ? 
            (DataObject_1_0) this.objGetValue(Aspect_1.CORE) :
            null;
    }
        
    private void prepare(
        Set<Map.Entry<String, DataObject_1_0>> source,
        DelegatingContainer target,
        boolean aspect
    ) throws ServiceException{
        Map<String,DataObject_1_0> children = new LinkedHashMap<String,DataObject_1_0>();
        for(Map.Entry<String,DataObject_1_0> child : source) {
            PathComponent key = new PathComponent(child.getKey());
            if(aspect == (key.isPlaceHolder() && key.size() == 3)){    
                children.put(
                    child.getKey(),
                    child.getValue()
                );
            }
        }
        for(Map.Entry<String,DataObject_1_0> child : children.entrySet()) {
            child.getValue().objMove(
                target,
                child.getKey()
            );
        }
    }
    
    /**
     * Create a new qualifier depending on the default qualifier type.
     *
     * @param container
     * @param defaultQualifierType
     *
     * @return a newly created default qualifier
     *
     * @throws ServiceException
     */
    private String createDefaultQualifier(
        AbstractContainer<?> container,
        String defaultQualifierType,
        PathComponent placeHolder
    ) throws ServiceException{
        //
        // Handle Aspects
        // 
        if(placeHolder != null && this.connection.getModel().isInstanceof(this, Aspect_1.CLASS)) {
            DataObject_1_0 core = (DataObject_1_0) objGetValue(Aspect_1.CORE);
            if(core != null) {
                Path corePath = (Path)core.jdoGetObjectId();
                if(corePath != null) {
                    StringBuilder aspectQualifier = new StringBuilder(corePath.getBase());
                    for(
                        int i = 1;
                        i < placeHolder.size();
                        i++
                    ){
                        String aspectId = placeHolder.get(i);
                        if(!aspectId.startsWith("!") && !aspectId.startsWith("*")) {
                            aspectQualifier.append('*');
                        }
                        aspectQualifier.append(aspectId);
                    }
                    return aspectQualifier.toString();
                }
            }
        }
        //
        // Handle Sequences
        // 
        if("SEQUENCE".equals(defaultQualifierType)) {
            String sequenceNumber = container.nextQualifier();
            if(sequenceNumber != null) {
                return sequenceNumber;
            }
        }
        //
        // Handle UIDs
        // 
        if(this.uuidGenerator == null) {
            this.uuidGenerator = UUIDs.getGenerator();
        }
        UUID uuid = this.uuidGenerator.next();
        if("UUID".equals(defaultQualifierType)){
            return uuid.toString();
        } else if ("UID".equals(defaultQualifierType)) {
            return UUIDConversion.toUID(uuid);
        } else if (
                "URN".equals(defaultQualifierType) ||
                "SEQUENCE".equals(defaultQualifierType)
        ) {
            return '(' + UUIDConversion.toURN(uuid) + ')';
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "The following default qualifier types are supported: [UUID, UID, URN, SEQUENCE]",
                new BasicException.Parameter("defaultQualifierType",defaultQualifierType)
            );
        }
    }

    /**
     * Removes an object.
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has beeen transient.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException
     *              if the object can't be removed
     */
    void objRemove(
    ) throws ServiceException {
        if(!jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                null,
                "Attempt to remove a transient object"
            );
        }
        this.objAddToUnitOfWork();
        ((DataObject_1_0)this.connection.getObjectById(
            this.identity.getPrefix(this.identity.size()-2)
        )).objAddToUnitOfWork();
        this.fireInstanceCallback(InstanceCallbackEvent.PRE_DELETE);
        this.deleted = true;
        this.getState(false).setLifeCycleEventPending(true);
    }

    /**
     * Flush the state of the instance to its provider.
     *
     * @return      true if all attributes could be flushed,
     *              false if some attributes contained placeholders
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the unit of work is optimistic
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is not persistent
     * @exception   ServiceException
     *              if the object can't be synchronized
     */
    protected boolean objFlush(
    ) throws ServiceException {
        UnitOfWork_1 unitOfWork = getUnitOfWork(); 
        if(unitOfWork.isOptimistic()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "Optimistic units of work can't flush"
            );
        }
        if(!jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                null,
                "Transient objects can't be flushed"
            );
        }
        Path current = jdoGetObjectId();
        if(current.size() == 1) {
            return true; // No need to flush the authority
        }
        Path parent = current.getPrefix(current.size() - 2);
        TransactionalState_1 parentState = ((Object_1)this.connection.getObjectById(parent)).getState(true);
        if(
            parentState != null &&
            parentState.isLifeCycleEventPending()
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Life cycle event pending for parent object",
                new BasicException.Parameter("current", current),
                new BasicException.Parameter("parent", current)
            );
        }
        prepare();
        flush();
        return !jdoIsDirty();
    }

    /**
     * Prepare the object for flushing
     * 
     * @param transactionTime
     * 
     * @throws ServiceException
     */
    void prepare(
    ) throws ServiceException {
        boolean prepared = true;
        if(jdoIsNew() || jdoIsDirty() || jdoIsDeleted()) {
            UnitOfWork_1 unitOfWork = getUnitOfWork();
            if(!jdoIsDeleted()) {
                fireInstanceCallback(
                    InstanceCallbackEvent.PRE_STORE
                );
            }
            Model_1_0 model = this.connection.getModel();
            if(model.isInstanceof(this, "org:openmdx:base:Creatable")) {
                if(jdoIsNew()) {
                    objSetValue(SystemAttributes.CREATED_AT, unitOfWork.getTransactionTime());
                    Collection<Object> createdBy = objGetSet(SystemAttributes.CREATED_BY);
                    createdBy.clear();
                    createdBy.addAll(this.connection.getPrincipalChain());
                } else {
                    Set<String> dirtyFeatures = unitOfWork.getState(this,false).dirtyFeatures(false);
                    dirtyFeatures.remove(SystemAttributes.CREATED_AT);
                    dirtyFeatures.remove(SystemAttributes.CREATED_BY);
                }
            }
            if(model.isInstanceof(this, "org:openmdx:base:Modifiable")) {
                if(!jdoIsDeleted()) {
                    objSetValue(SystemAttributes.MODIFIED_AT, unitOfWork.getTransactionTime());
                    Collection<Object> modifiedBy = objGetSet(SystemAttributes.MODIFIED_BY);
                    modifiedBy.clear();
                    modifiedBy.addAll(this.connection.getPrincipalChain());                
                }
            }
            if(model.isInstanceof(this, "org:openmdx:base:Removable")) {
                if(jdoIsDeleted() && !jdoIsNew()) {
                    this.deleted = false;
                    objSetValue(SystemAttributes.REMOVED_AT, unitOfWork.getTransactionTime());
                    Collection<Object> removedBy = objGetSet(SystemAttributes.REMOVED_BY);
                    removedBy.clear();
                    removedBy.addAll(this.connection.getPrincipalChain());
                } else {
                    Set<String> dirtyFeatures = unitOfWork.getState(this,false).dirtyFeatures(false);
                    dirtyFeatures.remove(SystemAttributes.REMOVED_AT);
                    dirtyFeatures.remove(SystemAttributes.REMOVED_BY);
                }
            }            	
        }
        getState(false).setPrepared(prepared);
    }

    /**
     * Flush the state of the instance to its provider.
     * 
     * @exception   ServiceException
     *              if the object can't be synchronized
     */
    @SuppressWarnings("unchecked")
    void flush(
    ) throws ServiceException {
        TransactionalState_1 state = getState(false);
        if(jdoIsDeleted()){
            if(state.isLifeCycleEventPending()){
                if(!jdoIsNew()) {
                    this.channel.removeObject(jdoGetObjectId(),this.connection);
                }
                state.setLifeCycleEventPending(false);
            }
        } else if(jdoIsNew() && state.isLifeCycleEventPending()){
            this.connection = this.persistentContainer.getManager();
            this.channel = this.persistentContainer.getProvider();
            Map<String,Object> transactionalValues = state.values(false);
            if(this.persistentValues == null) {
                this.persistentValues = createMappedRecord(this.transactionalValuesRecordName);
            }
            for(Map.Entry<String,Object> e : transactionalValues.entrySet()) {
                String feature = e.getKey();
                Object source = e.getValue();
                Flushable flushable = this.flushableValues.get(feature);
                if(flushable != null) {
                    try {
                        flushable.flush();
                    } catch (IOException exception) {
                        throw new ServiceException(exception);
                    }
                } else {
                    this.persistentValues.put(
                        feature,
                        source == null ? null : getMarshaller(feature).unmarshal(source)
                    );                    
                }
            }
            this.channel.createObject(
                this.identity,
                this.persistentValues,
                this.connection
            );
            this.persistentContainer = null;
            state.setLifeCycleEventPending(false);
            this.transientOnRollback = this.channel.doPeristentNewObjectsBecomeTransientUponRollback();
        } else if(
            jdoIsDirty() || jdoIsPersistent() && this.digest != null &&
            jdoGetObjectId().size() > 4 // exclude Authorities and Providers
        ){
            MappedRecord beforeImage = this.persistentValues;
            this.persistentValues = Object_1.createMappedRecord(objGetClass());
            this.persistentValues.put(
                SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_CLASS,
                SystemAttributes.OPTIMISTIC_LOCK_CLASS
            );
            this.persistentValues.put(
                SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_DIGEST,
                digest
            );
            Map<String,Object> transactionalValues = state.values(false);
            for(String feature : state.dirtyFeatures(true)){
                Flushable flushable = this.flushableValues.get(feature);
                if(flushable != null) {
                    try {
                        flushable.flush();
                    } catch (IOException exception) {
                        throw new ServiceException(exception);
                    }
                } else if(transactionalValues.containsKey(feature)) {
                    Object source = transactionalValues.get(feature);
                    if(source instanceof Flushable) {
                        try {
                            ((Flushable)source).flush();
                        } catch (IOException exception) {
                            throw new ServiceException(exception);
                        }
                    } else {
                        this.persistentValues.put(
                            feature,
                            getMarshaller(feature).unmarshal(source)
                        );                    
                    }
                } else {
                    this.persistentValues.put(
                        feature,
                        beforeImage.get(feature)
                    );
                }
            }
            MappedRecord arguments = this.persistentValues;
            this.persistentValues = beforeImage;
            this.channel.editObject(
                this.identity,
                arguments,
                this.connection
            );
        }
        Queue<Operation> operations = state.operations(true);
        for(
            Operation operation = operations.poll();
            operation != null;
            operation = operations.poll()
        ){
            operation.invoke();
        }
        state.dirtyFeatures(false).clear();
    }

    void afterCompletion(boolean committed) throws ServiceException {
        if (committed) {
            if(jdoIsDeleted()){
                this.connection.invalidate(this.identity, true);
            } else {
                evict(); //... depending on configuration
            }
        } else {
            getState(false).setLifeCycleEventPending(false);
            if(jdoIsDeleted()){
                this.deleted = false;
            } else {
                evict(); //... depending on configuration
            }
            if(isTransientOnRollback()){
                if(this.connection != null) {
                    this.connection.evictObject(this);
                }
                this.persistentContainer = null;
                this.identity = null;
                this.channel = null;
            } else if (this.transientContainer != null){
                this.transientContainer.remove(this);
                this.transientContainer = null;
            }
        }
        this.transientOnRollback = false;
    }

    /**
     * 
     */
    public void evict(
    ){
        if(jdoIsPersistent() && !jdoIsNew()) {
            InstanceCallbackEvent event = new InstanceCallbackEvent(
                InstanceCallbackEvent.PRE_CLEAR,
                this, 
                null
            );
            InstanceCallbackListener[] listeners;
            try {
                listeners = objGetEventListeners(
                    null,
                    InstanceCallbackListener.class
                );
                for(
                        int i = 0;
                        i < listeners.length;
                        i++
                ) {
                    try {
                        listeners[i].preClear(event);
                    } catch (ServiceException preclearException) {
                        preclearException.log();
                    }
                }
            } catch (ServiceException listenerException) {
                listenerException.log();
            }
            try {
                TransactionalState_1 state = getState(true);
                if(state != null){ 
                    Map<String,Object> values = state.values(false);
                    if(!values.isEmpty()) {
                        values.clear();
                    }
                }
            } catch (ServiceException ignore) {
                // Eviction should nevertheless be successful
            }
            for(Map.Entry<String, Flushable> entry : this.flushableValues.entrySet()) {
                Flushable value = entry.getValue();
                if(value instanceof ManagedAspect) {
                    ((Evictable)value).evict();
                } else if(
                    value instanceof DelegatingContainer &&
                    ((DelegatingContainer)value).objGetDelegate() instanceof PersistentContainer_1
                ){
                    if(this.identity == null){ // Maybe there was a BasicException.Code.DUPLICATE...
                        ((DelegatingContainer)value).objSetDelegate(new TransientContainer_1(this.connection));
                        //... forget about potential children
                    } else {
                        String feature = entry.getKey();
                        PersistentContainer_1 container = (PersistentContainer_1)((DelegatingContainer)value).objGetDelegate();
                        Path expectedIdentity = this.identity.getChild(feature);
                        if(expectedIdentity.equals(container.getReferenceFilter())){
                            container.evict();
                        } else {
                            ((DelegatingContainer)value).objSetDelegate(
                                new PersistentContainer_1(
                                    this.identity.getChild(feature),
                                    this.connection,
                                    this.channel,
                                    new EvictablePersistentObjects(feature),
                                    this
                                )
                            );
                        }
                    }
                }
            }
        }
        this.digest = null;
        this.persistentValues = null;
        if(jdoIsPersistent()){
            this.container = null;            
        } else if(this.container instanceof Evictable) {
            ((Evictable)this.container).evict();
        }
    }


    //------------------------------------------------------------------------
    // Event Handling
    //------------------------------------------------------------------------

    private void verifyListenerArguments(
        String feature,
        EventListener listener
    ) throws ServiceException {
        verifyListenerArguments(
            feature,
            listener == null ? null : listener.getClass()
        );
    }

    private void verifyListenerArguments(
        String feature,
        Class<? extends EventListener> listenerType
    ) throws ServiceException {
        if(listenerType == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                null,
                "The listener argument must not be null"
            );
        }
        if(InstanceCallbackListener.class.isAssignableFrom(listenerType)){
            if(feature != null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Instance level events must not be associated with a feature",
                    new BasicException.Parameter(
                        "feature",
                        feature
                    ),
                    new BasicException.Parameter(
                        "listenerType",
                        listenerType.getName()
                    )
                );
            }
        } else if (
                PropertyChangeListener.class.isAssignableFrom(listenerType) ||
                VetoableChangeListener.class.isAssignableFrom(listenerType)
        ){
            // Feature scope o.k.
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported listener class",
                new BasicException.Parameter(
                    "listenerType",
                    listenerType.getName()
                ),
                new BasicException.Parameter(
                    "supported",
                    InstanceCallbackListener.class.getName(),
                    PropertyChangeListener.class.getName(),
                    VetoableChangeListener.class.getName()
                )
            );
        }
    }

    /**
     * Add an event listener.
     *
     * @param feature
     *        restrict the listener to this feature;
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be added
     * <p>
     * It is implementation dependent whether the feature name is verified or
     * not.
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener's class is not supported
     * @exception   ServiceException TOO_MANY_EVENT_LISTENERS
     *              if an attempt is made to register more than one
     *              listener for a unicast event.
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener is null
     */
    public void objAddEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException {
        verifyListenerArguments(feature,listener);
        if(feature == null){
            this.listeners.put(listener, null);
        } else {
            Set<String> features = this.listeners.get(listener);
            if(features == null){
                if (this.listeners.containsKey(listener)) {
                    return;
                }
                this.listeners.put(
                    listener,
                    features = new HashSet<String>()
                );
            }
            features.add(feature);
        }
    }

    /**
     * Remove an event listener.
     * <p>
     * It is implementation dependent whether feature name and listener
     * class are verified.
     *
     * @param feature
     *        the name of the feature that was listened on,
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be removed
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener's class is not supported
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener is null
     */
    public void objRemoveEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException {
        verifyListenerArguments(feature,listener);
        if(feature == null){
            this.listeners.remove(listener);
        } else {
            Set<String> features = this.listeners.get(listener);
            if(features != null) {
                features.remove(feature);
            }
        }
    }

    /**
     * Get event listeners.
     * <p>
     * The <code>feature</code> argument is ignored for listeners registered
     * with a <code>null</code> feature argument.
     * <p>
     * It is implementation dependent whether feature name and listener
     * type are verified.
     *
     * @param feature
     *        the name of the feature that was listened on,
     *        or null for listeners interested in all features
     * @param listenerType
     *        the type of the event listeners to be returned
     *
     * @return an array of listenerType containing the matching event
     *         listeners
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener's type is not a subtype of EventListener
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener type is not supported
     */
    @SuppressWarnings("unchecked")
    public <T extends EventListener> T[] objGetEventListeners(
        String feature,
        Class<T> listenerType
    ) throws ServiceException {
        verifyListenerArguments(feature,listenerType);
        List<T> matchingListeners = new ArrayList<T>();
        for(Map.Entry<EventListener,Set<String>> e : this.listeners.entrySet()) {
            EventListener l = e.getKey();
            Set<String> f = e.getValue();
            if(
                    listenerType.isInstance(l) && (
                            f == null || f.contains(feature)
                    )
            ) {
                matchingListeners.add(listenerType.cast(l));
            }
        }
        return matchingListeners.toArray(
            (T[]) Array.newInstance(
                listenerType,
                matchingListeners.size()
            )
        );
    }

    /**
     * Fire an instance callback
     *
     * @param type
     * @throws ServiceException
     */
    protected void fireInstanceCallback (
        short type
    ) throws ServiceException {
        InstanceCallbackListener[] listeners = objGetEventListeners(
            null,
            InstanceCallbackListener.class
        );
        if(listeners.length != 0) {
            InstanceCallbackEvent event = new InstanceCallbackEvent(
                type,
                this, 
                null
            );
            for(InstanceCallbackListener listener : listeners) {
                switch (type) {
                    case InstanceCallbackEvent.POST_LOAD :
                    case InstanceCallbackEvent.POST_RELOAD :
                        listener.postLoad(event);
                        break;
                    case InstanceCallbackEvent.PRE_CLEAR :
                        listener.preClear(event);
                        break;
                    case InstanceCallbackEvent.PRE_DELETE :
                        listener.preDelete(event);
                        break;
                    case InstanceCallbackEvent.PRE_STORE :
                        listener.preStore(event);
                        break;
                    case InstanceCallbackEvent.POST_CREATE :
                        listener.postCreate(event);
                        break;
                }
            }
        }
    }

    /**
     * Tests whether this object is dirty. Instances that have been modified,
     * deleted, or newly made persistent in the current unit of work return
     * true.
     * <p>
     * Transient instances return false.
     *
     * @return true if this instance has been modified in the current unit
     *         of work.
     * @throws ServiceException 
     */
    public boolean jdoIsDirty(
    ) {
        try {
            if(jdoIsPersistent()) {
                TransactionalState_1 state = getState(true);
                return state != null && !state.dirtyFeatures(true).isEmpty();
            } 
            else {
                return false;
            }
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object state",
                e,
                this
            );
        }
    }

    /**
     * Tests whether this object is persistent. Instances that represent
     * persistent objects in the data store return true.
     *
     * @return true if this instance is persistent.
     */
    public boolean jdoIsPersistent(
    ){
        return this.identity != null;
    }

    public boolean objIsContained(){
        return jdoIsPersistent() || this.transientContainer != null;
    }
    
    /**
     * Tests whether this object has been newly made persistent. Instances
     * that have been made persistent in the current unit of work return true.
     * <p>
     * Transient instances return false.
     *
     * @return  true if this instance was made persistent in the current unit
     *          of work.
     */
    public boolean jdoIsNew(
    ){
        return this.persistentContainer != null;
    }

    /**
     * Tests whether this object is hollow
     * 
     * @return <code>true</code> if the object is hollow
     */
    private boolean objIsHollow(){
        return this.persistentValues == null;
    }

    /**
     * Tests whether this object becomes transient on rollback.
     *
     * @return  true if this instance becomes transient on rollback
     */
    public boolean isTransientOnRollback(
    ){
        return jdoIsNew() || this.transientOnRollback;
    }

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true.
     * Transient instances return false.
     *
     * @return  true if this instance was deleted in the current unit of work.
     */
    public boolean jdoIsDeleted(
    ){
        return this.deleted;
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     */
    public boolean jdoIsTransactional(
    ) {
        try {
            return getUnitOfWork().contains(this);
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object state",
                e,
                this
            );            
        }
    }

    /**
     * Tests whether this object can't leave its hollow state
     *
     * @return  true if this instance is inaccessible
     */
    public boolean objIsInaccessible(
    ){
        return this.inaccessabilityReason != null;
    }

    /**
     * Retrieve the reason for the objects inaccessibility
     *
     * @return Returns the inaccessibility reason
     */
    public ServiceException getInaccessibilityReason() {
        return this.inaccessabilityReason;
    }

    /**
     * Ensure that the object is read enabled
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *                  if the object is deleted
     */
    void assertObjectIsAccessible(
    ) throws ServiceException {
        if (jdoIsDeleted()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The object is deleted",
                new BasicException.Parameter("identitity", this.identity)
            );
        }
        if(objIsInaccessible()){
            throw new ServiceException(
                getInaccessibilityReason()
            );
        }
        if(
            jdoIsPersistent() && 
            !jdoIsNew() && 
            objIsHollow()
        ){
            load();
        }
    }

    /**
     * Ensure that the attribute is fetched.
     *
     * @param     name
     *            attribute name
     * @param     stream
     *            defines whether the value is a stream or not
     * @param clear 
     * 
     * @exception   ServiceException    ILLEGAL_STATE
     *                  if the object is deleted
     */
    @SuppressWarnings("unchecked")
    final Object getPersistentAttribute(
        String name,
        boolean stream,
        boolean singleValued, 
        boolean clear
    ) throws ServiceException {
        boolean loadable = !clear && jdoIsPersistent() && !jdoIsNew(); 
        Object attribute;
        if(loadable){
            assertObjectIsAccessible();
            attribute = this.persistentValues.get(name);
            if(
                attribute == null &&
                !this.persistentValues.containsKey(name)
            ) {
                MappedRecord persistentValues = createMappedRecord(
                    this.persistentValues
                );
                this.channel.getAttribute(
                    this.identity,
                    name,
                    persistentValues,
                    this.connection
                );
                attribute = stream ? persistentValues.remove(name) : persistentValues.get(name);
                this.persistentValues = persistentValues;
            }
        } else if (this.persistentValues == null) {
            attribute = null;
        } else {    
            attribute = this.persistentValues.get(name);
        }
        if(singleValued) {
            if(attribute instanceof SparseList) {
                attribute = ((SparseList<?>)attribute).get(0);
                if(this.persistentValues != null) {
                    this.persistentValues.put(
                        name,
                        attribute
                    );
                }
            }
        } else if (attribute instanceof SparseList) {
            if(clear) {
                ((SparseList<?>)attribute).clear();
            }
        } else {
            attribute = new CompactSparseList(clear ? null : attribute);
            if(this.persistentValues != null) {
                this.persistentValues.put(
                    name,
                    attribute
                );
            }
        }
        return attribute instanceof Remote ? RMIMapper.unmarshal((Remote)attribute) : attribute;
    }

    @SuppressWarnings("unchecked")
    final SparseList<Object> getPersistentCollection(
        String feature, 
        boolean clear
    ){
        try {
            return (SparseList<Object>) getPersistentAttribute(
                feature, 
                false, // stream
                false, // single-valud
                clear 
            );
        } catch (ServiceException exception) { 
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Ensure that the argument is single valued
     *
     * @param argument
     *        the argument to be checked
     *
     * @exception   ServiceException    BAD_PARAMETER
     *                  if the argument is multi-valued
     */
    protected void assertSingleValued(
        Object argument
    ) throws ServiceException {
        if(argument instanceof Collection) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_PARAMETER,
                "Single valued argument expected",
                new BasicException.Parameter("class", argument.getClass().getName())
            );
        }
    }


    //------------------------------------------------------------------------
    // Values
    //------------------------------------------------------------------------

    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the
     * feature is single valued or a stream.
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected.
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException
     *              if the object is not accessible
     */
    public void objSetValue(
        String feature,
        Object to
    ) throws ServiceException {
        assertSingleValued(to);
        if(feature.startsWith(SYSTEM_ATTRIBUTE_MODIFIER)) {
            //
            // Do not make the object dirty
            //
            String specialFeature = feature.substring(1);
            if(SystemAttributes.OBJECT_IDENTITY.equals(specialFeature)) {
                this.identity = (Path)to;
            } else if(SystemAttributes.OBJECT_CLASS.equals(specialFeature)) {
                this.transactionalValuesRecordName = (String) to;
            } else if (
                SystemAttributes.CREATED_AT.equals(specialFeature) ||
                State_1_Attributes.STATE_VALID_FROM.equals(specialFeature) ||
                State_1_Attributes.STATE_VALID_TO.equals(specialFeature)
            ) {
                getState(false).values(false).put(specialFeature, to);
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The given feature has no private accessor starting with '$'",
                new BasicException.Parameter("feature", specialFeature)
            );
        } else {
            UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(feature);
            if(unitOfWork == null) {
                this.transientValues.put(
                    feature,
                    to
                );
            } else {
                unitOfWork.getState(this,false).values(false).put(feature, to);
            }
        }
    }

    /**
     * Retrieve the object's classifier
     * 
     * @return  the object's classifier
     * 
     * @throws ServiceException
     */
    protected ModelElement_1_0 getClassifier(
    ) throws ServiceException{
        return this.classifier == null ?
            this.classifier = this.connection.getModel().getElement(objGetClass()) :
            this.classifier;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetIterable(java.lang.String)
     */
    public Iterable<?> objGetIterable(
        String featureName
    ) throws ServiceException {
        Iterable<?> reply = this.featureQueries.get(featureName);
        if(reply == null) {
            ModelElement_1_0 featureDef = this.connection.getModel().getFeatureDef(getClassifier(), featureName, false);
            String multiplicity = ModelUtils.getMultiplicity(featureDef);            
            Iterable<?> concurrent = this.featureQueries.putIfAbsent(
                featureName, 
                reply = 
                    Multiplicities.LIST.equals(multiplicity) ? objGetList(featureName) :
                    Multiplicities.SET.equals(multiplicity) ? objGetSet(featureName) :
                    Multiplicities.SPARSEARRAY.equals(multiplicity) ? objGetSparseArray(featureName).values() :
                    new ValueCollection(featureName)
            );
            if(concurrent != null) {
                reply = concurrent;
            }
        }
        return reply;
    }
    
    /**
     * Get an attribute.
     * <p>
     * Note: This specific implementation may allow to return multivalued
     * attributes as well!
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException
     *              if the object is not accessible
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(RECORD_NAME_REQUEST.equals(feature)) {
            return this.transactionalValuesRecordName;
        } else if (this.transientValues != null) {
            if(
                this.persistentValues == null ||
                this.transientValues.containsKey(feature)
            ) {
                return this.transientValues.get(feature);
            } else {
                Object clonedValue = getPersistentAttribute(
                    feature, 
                    false, // stream
                    true, // single-valued
                    false // clear
                );
                ModelElement_1_0 featureDef = getFeature(feature);
                Object transientValue = 
                    clonedValue == null ? null :  
                    featureDef == null ? clonedValue : // an embedded object's class
                    getMarshaller(featureDef).marshal(clonedValue);
                transientValues.put(feature, transientValue);
                return transientValue;
            }
        } else {
            UnitOfWork_1 unitOfWork = getUnitOfWork();
            Map<String,Object> transactionalValues = unitOfWork.contains(this) ? 
                unitOfWork.getState(Object_1.this, false).values(false) : 
                null;  
            if(transactionalValues != null) {
                Object transactionalValue = transactionalValues.get(feature);
                if(transactionalValue != null || transactionalValues.containsKey(feature)) {
                    return transactionalValue;
                }
            }
            Object nonTransactionalValue = getPersistentAttribute(feature, false, true, false);
            ModelElement_1_0 featureDef = getFeature(feature);
            Object transactionalValue = 
                nonTransactionalValue == null ? null :  
                featureDef == null ? nonTransactionalValue : // an embedded object's class
                getMarshaller(featureDef).marshal(nonTransactionalValue);
            if(transactionalValues != null) {
                transactionalValues.put(feature, transactionalValue);
            }
            return transactionalValue;
        }
    }

    /**
     * Get a List attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    @SuppressWarnings("unchecked")
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        Flushable flushable = this.flushableValues.get(feature);
        if(flushable == null) {
            flushable = putFlushable(
                feature,
                new ManagedList(
                    feature,
                    getMarshaller(feature)
                )
            );
        }
        return (List<Object>)flushable;
    }

    /**
     * Get a Set attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    @SuppressWarnings("unchecked")
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        Flushable flushable = this.flushableValues.get(feature);
        if(flushable == null) {
            flushable = putFlushable(
                feature,
                new ManagedSet(
                    feature,
                    getMarshaller(feature)
                )
            );
        }
        return (Set<Object>) flushable;
    }

    /**
     * Get a SparseArray attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    @SuppressWarnings("unchecked")
    public SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        Flushable flushable = this.flushableValues.get(feature);
        if(flushable == null) {
            flushable = putFlushable(
                feature,
                new ManagedSortedMap(
                    feature,
                    getMarshaller(feature)
                )
            );
        }
        return (SortedMap<Integer,Object>) flushable;
    }

    /**
     * Get a large object feature
     * <p>
     * This method returns a new LargeObject.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a large object which may be empty but never is null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a large object
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    public LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException {
        Flushable flushable = this.flushableValues.get(feature);
        if(flushable == null) {
            flushable = putFlushable(
                feature,
                new ManagedLargeObject(
                    feature
                )
            );
        }
        return (LargeObject_1_0) flushable;
    }

    /**
     * Get a reference feature.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException{
        Flushable flushable = this.flushableValues.get(feature);
        if(flushable == null) {
            if(this.connection.getModel().isInstanceof(this, "org:openmdx:state2:BasicState")){
                Object value = this.objGetValue("core");
                if(value instanceof DataObject_1_0) {
                    DataObject_1_0 core = (DataObject_1_0) value;
                    return core.objGetContainer(feature);
                }
            }
            @SuppressWarnings("deprecation")
            org.openmdx.base.collection.Container<DataObject_1_0> delegate = !jdoIsPersistent() ? new TransientContainer_1(
                this.connection
            ) : objIsInaccessible() || jdoIsNew() ? new PersistentContainer_1(
                this.identity.getChild(feature),
                this.persistentContainer.getManager(),
                this.persistentContainer.getProvider(),
                new EvictablePersistentObjects(feature),
                this
            ) : new PersistentContainer_1(
                this.identity.getChild(feature),
                this.connection,
                this.channel,
                new EvictablePersistentObjects(feature),
                this
            );
            flushable = putFlushable(
                feature,
                new DelegatingContainer(
                    feature, 
                    delegate
                )
            );
        }
        return (Container_1_0) flushable;
    }

    //------------------------------------------------------------------------
    // Operations
    //------------------------------------------------------------------------

    /**
     * Invokes an operation asynchronously.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object.
     *
     * @return      a structure with the result's values if the manager is
     *              going to populate it after the unit of work has committed
     *              or null if the operation's return value(s) will never be
     *              available to the manager.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException NOT_SUPPORTED
     *              if either asynchronous calls are not supported by the
     *              manager or the requested operation is not supportd by the
     *              object.
     * @exception   ServiceException
     *              if the invocation fails for another reason
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        Operation entry = new Operation(
            operation,
            arguments
        );
        objAddToUnitOfWork();
        getState(false).operations(false).offer(entry);
        return entry;
    }

    /**
     * Invokes an operation synchronously.
     * <p>
     * Only query operations can be invoked synchronously unless the unit of
     * work is non-optimistic or committing.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object.
     *
     * @return      the operation's return object
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if a non-query operation is called in an inappropriate
     *              state of the unit of work.
     * @exception   ServiceException NOT_SUPPORTED
     *              if either synchronous calls are not supported by the
     *              manager or the requested operation is not supportd by the
     *              object.
     * @exception   ServiceException
     *              if a checked exception is thrown by the implementation or
     *              the invocation fails for another reason.
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        Operation entry = new Operation(
            operation,
            arguments
        );
        entry.invoke();
        return entry;
    }


    //------------------------------------------------------------------------
    // Implements Object
    //------------------------------------------------------------------------

    Object noContent (
        Object source
    ){
        if(source instanceof Object_1){
            Object_1 object = (Object_1) source;
            return AbstractObject_1.toString(
                object, 
                object.transactionalValuesRecordName, 
                null
            );
        } else {
            return source;
        }
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public String toString(
    ){
        if(this.objIsInaccessible()) {
            return AbstractObject_1.toString(this, null);
        } else {
            try {
                Map content = new HashMap();
                TransactionalState_1 state = getState(true);
                String description;
                if(state == null) {
                    description = null;
                } else {
                    for(Map.Entry<String,Object> e : state.values(false).entrySet()) {
                        Object v = e.getValue();
                        if(v instanceof Collection) {
                            if (v instanceof List) {
                                List t = new ArrayList();
                                for(
                                        Iterator j = ((List)v).iterator();
                                        j.hasNext();
                                ) {
                                    t.add(
                                        noContent(j.next())
                                    );
                                }
                            } else if (v instanceof Set) {
                                Set t = new HashSet();
                                for(
                                        Iterator j = ((Set)v).iterator();
                                        j.hasNext();
                                ) {
                                    t.add(
                                        noContent(j.next())
                                    );
                                }
                            } // else ignore
                        } else if (v instanceof SortedMap) {
                            SortedMap t = new TreeMap();
                            for(
                                    Iterator j = ((SortedMap)v).entrySet().iterator();
                                    j.hasNext();
                            ){
                                Map.Entry k = (Map.Entry)j.next();
                                t.put(
                                    k.getKey(),
                                    noContent(k.getValue())
                                );
                            }
                        } else {
                            content.put(e.getKey(), noContent(v));
                        }
                    }
                    description = state.isPrepared() ? "prepared" : "not prepared";
                }
                return AbstractObject_1.toString(
                    this, 
                    this.transactionalValuesRecordName, 
                    description
                ) + ", attributes=" + IndentingFormatter.toString(
                    content
                );
            } catch (Exception exception) {
                return AbstractObject_1.toString(
                    this, 
                    this.transactionalValuesRecordName, 
                    exception.getMessage()
                );
            }
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Object_1_5
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        return this.connection;
    }

    //------------------------------------------------------------------------
    // Class Methods
    //------------------------------------------------------------------------

    static MappedRecord createMappedRecord(
        String name
    )throws ServiceException{
        try{
            return Records.getRecordFactory().createMappedRecord(name);
        }catch(ResourceException exception){
            throw new ServiceException(exception);
        }
    }

    static MappedRecord createMappedRecord(
        MappedRecord that
    )throws ServiceException{
        try{
            return Records.getRecordFactory().createMappedRecord(
                that.getRecordName(),
                that.getRecordShortDescription(),
                that
            );
        } catch(ResourceException exception){
            throw new ServiceException(exception);
        }
    }

    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------

    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        try {
            TransactionalState_1 state = getState(true);
            stream.defaultWriteObject();
            if(state == null) {
                stream.writeInt(0);
            } else {
                Set<String> features = state.dirtyFeatures(true);
                Map<String,Object> source = state.values(false);
                stream.writeInt(features.size());
                for(String feature : features) {
                    stream.writeObject(feature);
                    stream.writeObject(source.get(feature));
                }
            }
        } catch (ServiceException exception) {
            throw new ExtendedIOException(exception);
        }
    }

    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.listeners = new WeakHashMap<EventListener,Set<String>>();
        this.flushableValues = new ConcurrentHashMap<String,Flushable>();
        this.featureQueries = new ConcurrentHashMap<String,Iterable<?>>();
        int count = stream.readInt();
        if(count > 0) {
            TransactionalState_1 state;
            try {
                state = getState(false);
            } catch (ServiceException exception) {
                throw new ExtendedIOException(exception);
            }
            Set<String> features = state.dirtyFeatures(false);
            Map<String,Object> target = state.values(false);
            for(int i = 0; i < count; i++) {
                String feature = (String) stream.readObject();
                features.add(feature);
                target.put(feature, stream.readObject());
            }
        }
        this.connection.cacheObject(this.identity, this);
    }


    @SuppressWarnings("unchecked")
    private final ModelElement_1_0 getFeature(
        String featured,
        String kind, 
        String feature
    ) throws ServiceException{
        Map<String,ModelElement_1_0> features = (
                SystemOperations.GET_BINARY_STREAM_ARGUMENTS.equals(featured) ||
                SystemOperations.GET_CHARACTER_STREAM_ARGUMENTS.equals(featured)
        ) ? null : (Map<String, ModelElement_1_0>) this.connection.getModel(
        ).getElement(
            featured
        ).values(
            kind
        ).get(
            0
        ); 
        return features == null ? null : features.get(feature);
    }

    /**
     * Determine the marshaller to be used
     * 
     * @param feature
     * 
     * @return an object or datatype marshaller
     * 
     * @throws ServiceException 
     */
    private final Marshaller getMarshaller(
        ModelElement_1_0 feature
    ) throws ServiceException{
        if(feature == null) {
            return this.connection;
        }
        Path typeId = (Path) feature.values("type").get(0);
        String typeName = typeId.getBase();
        return 
            DATE.equals(typeName) ? DateMarshaller.getInstance() : 
            DATETIME.equals(typeName) ? DateTimeMarshaller.getInstance() : 
            DURATION.equals(typeName) ? DurationMarshaller.getInstance() : 
            SHORT.equals(typeName) ? ShortMarshaller.getInstance() :
            INTEGER.equals(typeName) ? IntegerMarshaller.getInstance() :
            LONG.equals(typeName) ? LongMarshaller.getInstance() :
            this.connection;
    }

    /**
     * Determine the marshaller to be used
     * 
     * @param kind 
     * @param feature
     * @param feature
     * 
     * @return an object or datatype marshaller
     * @throws ServiceException 
     */
    final Marshaller getMarshaller(
        String featured,
        String kind, 
        String feature
    ) throws ServiceException{
        return getMarshaller(getFeature(featured, kind, feature));
    }

    /**
     * Retrieve the model element for a given object feature
     * 
     * @param feature
     * 
     * @return the model element for the given feature
     * 
     * @throws ServiceException
     */
    private final ModelElement_1_0 getFeature(
        String feature
    ) throws ServiceException {
        int i = feature.lastIndexOf(':');
        if(i < 0) {
            return getFeature(
                objGetClass(), 
                "attribute", 
                feature
            );
        } else {
            this.assertObjectIsAccessible();
            Object raw = this.persistentValues.get(
                feature.substring(0, ++i) + SystemAttributes.OBJECT_CLASS
            );
            String embeddedClass = raw instanceof SparseList ?
                (String) ((SparseList<?>)raw).get(0) :
                (String)raw;
            String embeddedFeature = feature.substring(i);
            return SystemAttributes.OBJECT_CLASS.equals(embeddedFeature) ? null : getFeature(
                embeddedClass, 
                "attribute",
                embeddedFeature
            );
        }
    }

    /**
     * Determine the marshaller to be used
     * 
     * @param feature
     *  
     * @return an object or datatype marshaller
     * 
     * @throws ServiceException 
     */
    final Marshaller getMarshaller(
        String feature
    ) throws ServiceException{
        return getMarshaller(getFeature(feature));
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataObject_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        Flushable flushable = this.flushableValues.get(aspectClass);
        if(flushable == null) {
            flushable = putFlushable(
                aspectClass,
                new ManagedAspect(
                    aspectClass
                )
            );
        }
        return (Map<String, DataObject_1_0>)flushable;
    }

    //------------------------------------------------------------------------
    // Implements PersistenceCapable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public Object jdoGetTransactionalObjectId(
    ) {
        return this.transientObjectId;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(StateManager sm)
        throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6246865175754052117L;

    static final String[] GET_STREAM_ARGUMENTS = {
        SystemOperations.GET_STREAM_FEATURE,
        SystemOperations.GET_STREAM_POSITION,
        SystemOperations.GET_STREAM_VALUE
    };

    /**
     * The object's identity
     *
     * @serial
     */
    protected Path identity;

    /**
     * @serial
     */
    protected Channel channel;

    /**
     * @serial
     */
    protected Connection_1 connection;

    /**
     * @serial
     */
    Path transientObjectId;
    
    /**
     * @serial
     */
    private String transactionalValuesRecordName;

    /**
     * @serial
     */
    private TransientContainer_1 transientContainer = null;

    /**
     * @serial
     */
    private PersistentContainer_1 persistentContainer = null;

    /**
     * 
     */
    private String qualifier = null;
    
    /**
     * @serial
     */
    private boolean refreshAsynchronously = false;

    /**
     * @serial
     */
    private boolean deleted;

    /**
     *
     */
    protected transient MappedRecord persistentValues = null;

    /**
    *
    */
    protected Map<String,Object> transientValues;
    
    /**
     * @serial
     */
    protected byte[] digest = null;

    /**
     * @serial
     */
    private boolean transientOnRollback = false;

    /**
     * Such an object cant't leave its hollow state
     */
    private transient ServiceException inaccessabilityReason = null;

    /**
     *
     */
    private transient Map<EventListener,Set<String>> listeners = new WeakHashMap<EventListener,Set<String>>();

    /**
     * To generate default qualifiers if no sequences are used.
     */
    private transient UUIDGenerator uuidGenerator = null;

    /**
     * 
     */
    private transient ConcurrentMap<String,Flushable> flushableValues = new ConcurrentHashMap<String,Flushable>();

    /**
     * The persistent aspects
     */
    private transient FilterableMap<String, DataObject_1_0> container = null;
    
    /**
     * The object's classifier
     */
    private transient ModelElement_1_0 classifier;

    /**
     * Cache the feature queries
     */
    private transient ConcurrentMap<String,Iterable<?>> featureQueries = new ConcurrentHashMap<String,Iterable<?>>();
    
    /**
     * 
     */
    static final Set<java.util.Map.Entry<String, DataObject_1_0>> NO_ASPECT = Collections.emptySet();
    
}
