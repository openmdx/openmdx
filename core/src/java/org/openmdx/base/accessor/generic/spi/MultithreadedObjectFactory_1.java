/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MultithreadedObjectFactory_1.java,v 1.11 2008/06/28 00:21:39 hburger Exp $
 * Description: Multi-threaded object factory
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:39 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.spi;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.xa.Xid;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.State;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.base.transaction.UnitOfWork_1_2;
import org.openmdx.compatibility.base.event.InstanceCallbackEvent;
import org.openmdx.compatibility.base.event.InstanceCallbackListener;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.application.container.transaction.TransactionIdFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multi-threaded object factory
 * <p><em>
 * Note:<br><ul>
 * <li><b>Queries</b> ignore the content of the current unit of work!
 * <li><b>ReadableLargeObject</b>s ignore the content of the current unit of work!
 * </ul>
 */
public class MultithreadedObjectFactory_1
    implements ObjectFactory_1_0
{

    public MultithreadedObjectFactory_1(
        ObjectFactory_1_4 delegate,
        OptimisticTransaction_2_0 transaction
    ){
        this.delegate = delegate;
        this.unitOfWork = new MultithreadedUnitOfWork_1(transaction);
    }
    
    /**
     * 
     */
    final ObjectFactory_1_4 delegate;

    /**
     * 
     */
    final MultithreadedUnitOfWork_1 unitOfWork;

    /**
     * 
     */
    final Logger logger = LoggerFactory.getLogger(MultithreadedObjectFactory_1.class);
    
    /**
     * Should be false in compatibility mode
     */
    protected final static boolean BLOCK_OPERATIONS = false;
    
    /**
     * 
     */
    private final ConcurrentMap<Object_1_0,MultithreadedObject_1> cache = 
        new ConcurrentHashMap<Object_1_0,MultithreadedObject_1>();    
    
    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#close()
     */
    public void close(
    ) throws ServiceException {
        this.unitOfWork.close(); // fails in case of active units of work 
        this.delegate.close();
    }

    /**
     * @param objectClass
     * @param initialValues
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#createObject(java.lang.String, org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public Object_1_0 createObject(
        String objectClass, 
        Object_1_0 initialValues
    ) throws ServiceException {
        return marshal(
            this.delegate.createObject(
                objectClass, unmarshal((MultithreadedObject_1)initialValues)
            )
        );
    }

    /**
     * @param objectClass
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#createObject(java.lang.String)
     */
    public Object_1_0 createObject(
        String objectClass
    ) throws ServiceException {
        return marshal(
            this.delegate.createObject(objectClass)
        );
    }

    /**
     * @param type
     * @param fieldNames
     * @param fieldValues
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.StructureFactory_1_0#createStructure(java.lang.String, java.util.List, java.util.List)
     */
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    ) throws ServiceException {
        List<Object> unmarshalledValues = new ArrayList<Object>(fieldValues.size());
        for(Object fieldValue : fieldValues) {
            unmarshalledValues.add(unmarshal(fieldValue));
        }
        return this.delegate.createStructure(type, fieldNames, unmarshalledValues);
    }

    /**
     * @param accessPath
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#getObject(java.lang.Object)
     */
    public Object_1_0 getObject(
        Object accessPath
    ) throws ServiceException {
        UnitOfWork_1_3 unitOfWork = this.unitOfWork.get();
        Object_1_0 newObject = accessPath instanceof Path && unitOfWork.isActive() ? unitOfWork.get((Path)accessPath) : null;
        return newObject == null ? marshal(
            this.delegate.getObject(accessPath)
        ) : newObject;
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#getUnitOfWork()
     */
    public UnitOfWork_1_0 getUnitOfWork(
    ) throws ServiceException {
        return this.unitOfWork;
    }

    /**
     * @param source
     * @return
     * @throws ServiceException
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException {
        return source instanceof Object_1_0 ?
            marshal((Object_1_0)source) : 
            source;
    }

    /**
     * @param source
     * @return
     * @throws ServiceException
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return source instanceof MultithreadedObject_1 ?
            unmarshal((MultithreadedObject_1)source) :
            source;
    }

    /**
     * 
     * @param source
     * @return
     * @throws ServiceException
     */
    MultithreadedObject_1 marshal(
        Object_1_0 source
    ) throws ServiceException {
        MultithreadedObject_1 oldValue =  this.cache.get(source);
        if(oldValue == null) {
            MultithreadedObject_1 newValue = new MultithreadedObject_1(source);
            oldValue = this.cache.putIfAbsent(
                source,
                newValue 
            );
            if(oldValue == null) return newValue;
        }
        return oldValue;
    }

    boolean delete(
        Object_1_0 object
    ){
        if(object == null) {
            return false;
        } else {
            if(object instanceof MultithreadedObject_1) {
                throw new IllegalArgumentException();
            } else try {
                if(object.objIsPersistent()) {
                    marshal(object).objRemove();
                    return true;
                } else {
                    return false;
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
    }
    
    /**
     * 
     * @param source
     * @return
     * @throws ServiceException
     */
    Object_1_0 unmarshal(
        MultithreadedObject_1 source
    ){
        return source.clean;
    }
            

    //------------------------------------------------------------------------
    // Class UnitsOfWork
    //------------------------------------------------------------------------
    
    /**
     * Provide thread local units of work
     */
    class MultithreadedUnitOfWork_1 
        extends ThreadLocal<UnitOfWork_1_3>
        implements UnitOfWork_1_2
    {
        
        MultithreadedUnitOfWork_1(
            OptimisticTransaction_2_0 transaction
        ){
            this.transaction = transaction;
        }
        
        /**
         * 
         */
        final OptimisticTransaction_2_0 transaction;
            
        /**
         * 
         */
        final TransactionIdFactory unitOfWorkIdFactory = new TransactionIdFactory();
        
        /**
         * 
         */
        final ConcurrentMap<UnitOfWork_1_3,Xid> activeUnitsOfWork = new ConcurrentHashMap<UnitOfWork_1_3,Xid>();
        
        /**
         * 
         */
        final Object lock = new Object();
        
        /**
         * Test whether there are active units of work or not
         * @throws ServiceException  
         * 
         * @throws ServiceException if there are  active units of work
         */
        void close(
        ) throws ServiceException {
            if(!this.activeUnitsOfWork.isEmpty()) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("active", this.activeUnitsOfWork.values())
                },
                "A " + MultithreadedObjectFactory_1.class.getSimpleName() +
                " with actove units of work can't be closed"
            );
        }
        
        /* (non-Javadoc)
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected UnitOfWork_1 initialValue() {
            return new UnitOfWork_1();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbckOnly()
         */
        public boolean getRollbackOnly() {
            return get().getRollbackOnly();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
         */
        public void setRollbackOnly() {
            get().setRollbackOnly();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
         */
        public void begin(
        ) throws ServiceException {
            get().begin();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
         */
        public void commit(
        ) throws ServiceException {
            get().commit();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
         */
        public boolean isActive() {
            return get().isActive();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isOptimistic()
         */
        public boolean isOptimistic() {
            return get().isOptimistic();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
         */
        public boolean isTransactional() {
            return get().isTransactional();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
         */
        public void rollback(
        ) throws ServiceException {
            get().rollback();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.UnitOfWork_1_0#verify()
         */
        public void verify(
        ) throws ServiceException {
            get().verify();
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
         */
        public void afterBegin(
        ) throws ServiceException {
            new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "The " + MultithreadedObjectFactory_1.class.getSimpleName() + 
                " can not participate in an ongoing transaction"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
         */
        public void afterCompletion(
            boolean committed
        ) throws ServiceException {
            new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "The " + MultithreadedObjectFactory_1.class.getSimpleName() + 
                " can not participate in an ongoing transaction"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
         */
        public void beforeCompletion(
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                null,
                "The " + MultithreadedObjectFactory_1.class.getSimpleName() + 
                " can not participate in an ongoing transaction"
            );
        }

        
        //--------------------------------------------------------------------
        // Class UnitOfWork_1
        //--------------------------------------------------------------------
        
        /**
         * 
         */
        class UnitOfWork_1 implements UnitOfWork_1_3, Synchronization {

            /**
             * The rollback-only flag
             */
            private boolean rollbackOnly = false;
            
            /**
             * The unit of work id
             */
            private Xid id = null;
            
            /**
             * The UUID generator
             */
            private UUIDGenerator uuidGenerator = null;

            /**
             * The members of the unit of work
             */
            private final Map<MultithreadedObject_1,PersistenceCapable_1_1> members = 
                new LinkedHashMap<MultithreadedObject_1,PersistenceCapable_1_1>();

            /**
             * 
             */
            private final Map<Path,MultithreadedObject_1> newObjects = 
                new HashMap<Path,MultithreadedObject_1>();

            /**
             * 
             */
            private final Set<Path> noObjects = new HashSet<Path>();
                
            /**
             * Add an object to the current unit of work
             * 
             * @param object
             * 
             * @throws ServiceException if no unit of work is active
             */
            public PersistenceCapable_1_1 add(
                MultithreadedObject_1 object
            ) throws ServiceException{
                if(!isActive()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("unitOfWorkId", null),
                    },
                    "No unit of work in progress"
                );
                PersistenceCapable_1_1 transactionalObject = this.members.get(object);
                if(transactionalObject == null) this.members.put(
                    object,
                    transactionalObject = new TransactionalObject_1(object.clean)
                );
                return transactionalObject;
            }

            /**
             * 
             * @param object
             * @return
             */
            final boolean contains(
                MultithreadedObject_1 object
            ){
                return this.members.containsKey(object);
            }

            /**
             * 
             * @param object
             */
            public void remove(
                MultithreadedObject_1 object
            ){
                this.members.remove(object);
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#get(org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.MultithreadedObject_1)
             */
            public PersistenceCapable_1_1 get(MultithreadedObject_1 object) {
                return isActive() ? this.members.get(object) : null;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbackOnly()
             */
            public boolean getRollbackOnly() {
                return this.rollbackOnly;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
             */
            public void setRollbackOnly() {
                this.rollbackOnly = true;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
             */
            public void begin(
            ) throws ServiceException {
                if(isActive()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("unitOfWorkId", this.id),
                    },
                    "Unit of work active"
                );
                this.rollbackOnly = false;
                this.id = unitOfWorkIdFactory.createTransactionId();
                MultithreadedUnitOfWork_1.this.activeUnitsOfWork.put(this,this.id);
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
             */
            public void commit(
            )throws ServiceException {
                if(!isActive()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("unitOfWorkId", null),
                    },
                    "No unit of work in progress"
                );
                int status = Status.STATUS_UNKNOWN;
                try {
                    if(getRollbackOnly()) {
                        status = Status.STATUS_ROLLEDBACK;
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("unitOfWorkId", null),
                            },
                            "Transaction is marked for rollback only"
                        );
                    }
                    synchronized (MultithreadedUnitOfWork_1.this.lock) {
                        try {
                            MultithreadedUnitOfWork_1.this.transaction.commit(this);
                            status = Status.STATUS_COMMITTED;
                        } catch (ServiceException exception) {
                            if(exception.getExceptionCode() == BasicException.Code.ROLLBACK) {
                                status = Status.STATUS_ROLLEDBACK;
                            }
                            throw exception;
                        } finally {
                            afterCompletion(status);
                        }
                    }                    
                } finally {
                    afterCompletion();
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
             */
            public final boolean isActive(
            ) {
                return this.id != null;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#isOptimistic()
             */
            public boolean isOptimistic() {
                return true; // can not handle pessimistic transaction
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
             */
            public boolean isTransactional(
            ) {
                return true; // can not participate in an ongoing transaction
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
             */
            public void rollback(
            ) throws ServiceException {
                afterCompletion();
            }
            
            private void afterCompletion(){
                this.members.clear();
                this.newObjects.clear();
                this.noObjects.clear();
                MultithreadedUnitOfWork_1.this.activeUnitsOfWork.remove(this);
                this.id = null;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.UnitOfWork_1_0#verify()
             */
            public void verify(
            ) throws ServiceException {
                if(!isActive()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("unitOfWorkId", null),
                    },
                    "No unit of work in progress"
                );
                // No other checks performed at the moment...
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
             */
            public void afterBegin(
            ) throws ServiceException {
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "The " + MultithreadedObjectFactory_1.class.getSimpleName() + 
                    " can not participate in an ongoing transaction"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
             */
            public void afterCompletion(
                boolean committed
            ) throws ServiceException {
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "The " + MultithreadedObjectFactory_1.class.getSimpleName() + 
                    " can not participate in an ongoing transaction"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
             * @see javax.transaction.Synchronization#beforeCompletion()
             */
            public void beforeCompletion(
            ) {
                try {
                    UnitOfWork_1_0 unitOfWork = MultithreadedObjectFactory_1.this.delegate.getUnitOfWork();  
                    unitOfWork.afterBegin();
                    for(PersistenceCapable_1_1 object : UnitOfWork_1.this.members.values()) {
                        object.objFlush();
                    }
                    unitOfWork.beforeCompletion();
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

            /* (non-Javadoc)
             * @see javax.transaction.Synchronization#afterCompletion(int)
             */
            public void afterCompletion(
                int status
            ){
                try {
                    MultithreadedObjectFactory_1.this.delegate.getUnitOfWork().afterCompletion(
                        status == Status.STATUS_COMMITTED
                    );
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return "Unit of work " + (
                    isActive() ? 
                    this.id + " is active and has " + this.members.size() + " members" :
                    "is inactive"
                );                 
            }

            /* (non-Javadoc)
             * @see org.openmdx.kernel.id.cci.UUIDGenerator#next()
             */
            public UUID next() {
                return (
                    this.uuidGenerator == null ? this.uuidGenerator = UUIDs.getGenerator() : this.uuidGenerator
                ).next();
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#makePersistent(org.openmdx.compatibility.base.naming.Path, org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.MultithreadedObject_1)
             */
            public PersistenceCapable_1_1 add(
                Path path,
                MultithreadedObject_1 object
            ) throws ServiceException {
                PersistenceCapable_1_1 transactionalObject = add(object);
                this.newObjects.put(path, object);
                transactionalObject.objMakePersistent(path);
                return transactionalObject;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#get(org.openmdx.compatibility.base.naming.Path)
             */
            public MultithreadedObject_1 get(Path path) {
                return this.newObjects.get(path);
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#isInaccessable(org.openmdx.compatibility.base.naming.Path)
             */
            public boolean isInaccessible(Path path) {
                return this.noObjects.contains(path);
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#setInaccessable(org.openmdx.compatibility.base.naming.Path)
             */
            public void setInaccessible(Path path, boolean to) {
                if(to) {
                    this.noObjects.add(path);
                } else {
                    this.noObjects.remove(path);
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#countPersistentDeleted(org.openmdx.compatibility.base.naming.Path)
             */
            public int countPersistentDeleted(Path container) {
                int d = 0;
                if(this.isActive()) for(PersistenceCapable_1_1 transactional : this.members.values()) try {
                    if(
                        transactional.objIsDeleted() && 
                        !transactional.objIsNew() &&
                        transactional.objGetPath().startsWith(container) &&
                        transactional.objGetPath().size() - container.size() == 1
                    ) d++;
                } catch (ServiceException exception) {
                    logger.error("Object status evaluation failure", exception);
                }
                return d;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.accessor.generic.spi.MultithreadedObjectFactory_1.UnitOfWork_1_3#getLock()
             */
            public Object getLock() {
                return MultithreadedUnitOfWork_1.this.lock;
            }

        }

    }

    
    //------------------------------------------------------------------------
    // Class Object_1
    //------------------------------------------------------------------------
    
    /**
     * Object_1_0 decorator
     */
    public class MultithreadedObject_1
        implements Object_1_2
    {
        
        /**
         * Constructor 
         */
        MultithreadedObject_1(
            Object_1_0 clean
        ) {
            this.clean = clean;
        }

        /**
         * 
         */
        final Object_1_0 clean;

        /**
         * 
         */
        private InstanceCallbackDecorator instanceCallbackListener = null;
        
        /**
         * 
         */
        private final ConcurrentMap<String,Object> proxies = new ConcurrentHashMap<String,Object>();
        
        /**
         * @return
         * @see org.openmdx.base.accessor.generic.cci.Object_1_2#getInaccessabilityReason()
         */
        public ServiceException getInaccessabilityReason() {
            return objIsInaccessable() ? 
                ((Object_1_2)this.clean).getInaccessabilityReason() :
                null;
        }

        /**
         * @param feature
         * @param listener
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
         */
        public void objAddEventListener(
            String feature, 
            EventListener listener
        ) throws ServiceException {
            if("org.openmdx.base.persistence.spi.InstanceLifecycleAdapter_1".equals(listener.getClass().getName())) {
                this.clean.objAddEventListener(
                    feature, 
                    this.instanceCallbackListener = new InstanceCallbackDecorator((InstanceCallbackListener)listener)
                );
            } else {
                logger.warn(
                    "Listener is not a InstanceLifecycleDecorator_1: {}", 
                    listener.getClass().getName()
                );
                this.clean.objAddEventListener(feature, listener);
            }
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
         */
        public void objAddToUnitOfWork(
        ) throws ServiceException {
            MultithreadedObjectFactory_1.this.unitOfWork.get().add(this);
        }

        public Object_1_0 objCopy(
            FilterableMap<String, Object_1_0> there,
            String criteria
        ) throws ServiceException {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                    new BasicException.Parameter("criteria", criteria)
                },
                "Copy not implemented"
            ); 
        }

        /**
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
         */
        public String objGetClass(
        ) throws ServiceException {
            return this.clean.objGetClass();
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetContainer(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        public FilterableMap<String, Object_1_0> objGetContainer(
            String feature
        ) throws ServiceException {
            FilterableMap<String, Object_1_0> oldValue = (FilterableMap<String, Object_1_0>) this.proxies.get(feature);
            if(oldValue == null) {
                FilterableMap<String, Object_1_0> newValue = new MultithreadedContainerDecorator(
                    feature,
                    MultithreadedObjectFactory_1.this, 
                    new MultithreadedContainer(feature)
                );
                oldValue = (FilterableMap<String, Object_1_0>) this.proxies.putIfAbsent(feature, newValue);
                if(oldValue == null) return newValue;
            }
            return oldValue;
        }

        /**
         * @param feature
         * @param listenerType
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
         */
        public EventListener[] objGetEventListeners(
            String feature,
            Class<? extends EventListener> listenerType
        ) throws ServiceException {
            return this.clean.objGetEventListeners(feature, listenerType);
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetLargeObject(java.lang.String)
         */
        public LargeObject_1_0 objGetLargeObject(
            String feature
        ) throws ServiceException {
            LargeObject_1_0 oldValue = (LargeObject_1_0) this.proxies.get(feature);
            if(oldValue == null) {
                LargeObject_1_0 newValue = new MultithreadedLargeObject(feature);
                oldValue = (LargeObject_1_0) this.proxies.putIfAbsent(feature, newValue);
                if(oldValue == null) return newValue;
            }
            return oldValue;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetList(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        public List<Object> objGetList(
            String feature
        ) throws ServiceException {
            List<Object> oldValue = (List<Object>) this.proxies.get(feature);
            if(oldValue == null) {
                List<Object> newValue = new MarshallingList<Object>(
                    MultithreadedObjectFactory_1.this,
                    new MultithreadedList(feature)
                );
                oldValue = (List<Object>) this.proxies.putIfAbsent(feature, newValue);
                if(oldValue == null) return newValue;
            }
            return oldValue;
        }

        /**
         * @return
         * @throws ServiceException 
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
         */
        public Path objGetPath(
        ) throws ServiceException{
            return accessor().objGetPath();
        }

        /**
         * @return
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetResourceIdentifier()
         */
        public Object objGetResourceIdentifier() {
            try {
                return objIsPersistent() && !objIsNew() ? objGetPath().toUri() : null;
            } catch (ServiceException exception) {
                return null;
            }
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSet(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        public Set<Object> objGetSet(
            String feature
        ) throws ServiceException {
            Set<Object> oldValue = (Set) this.proxies.get(feature);
            if(oldValue == null) {
                Set<Object> newValue = new MarshallingSet<Object>(
                    MultithreadedObjectFactory_1.this,
                    new MultithreadedSet(feature)
                );
                oldValue = (Set<Object>) this.proxies.putIfAbsent(feature, newValue);
                if(oldValue == null) return newValue;
            }
            return oldValue;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSparseArray(java.lang.String)
         */
        @SuppressWarnings("unchecked")
        public SortedMap<Integer, Object> objGetSparseArray(
            String feature
        ) throws ServiceException {
            SortedMap<Integer, Object> oldValue = (SortedMap) this.proxies.get(feature);
            if(oldValue == null) this.proxies.put(
                feature,
                oldValue = new MarshallingSortedMap<Integer,Object>(
                    MultithreadedObjectFactory_1.this,
                    new MultithreadedSortedMap(feature)
                )
            );
            if(oldValue == null) {
                SortedMap<Integer, Object> newValue = new MarshallingSortedMap<Integer,Object>(
                    MultithreadedObjectFactory_1.this,
                    new MultithreadedSortedMap(feature)
                );
                oldValue = (SortedMap<Integer, Object>) this.proxies.putIfAbsent(feature, newValue);
                if(oldValue == null) return newValue;
            }
            return oldValue;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetValue(java.lang.String)
         */
        public Object objGetValue(
            String feature
        ) throws ServiceException {
            PersistenceCapable_1_1 transactional = currentUnitOfWork().get(this);
            return (
                transactional == null ? this.clean : transactional
            ).objGetValue(
                feature
            );
        }

        /**
         * @param operation
         * @param arguments
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
         */
        public Structure_1_0 objInvokeOperation(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            if(BLOCK_OPERATIONS) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                    new BasicException.Parameter("operation", operation)
                },
                "Operations can't be handled by the persistence manager"
            );
            synchronized(currentUnitOfWork().getLock()) {
                return this.clean.objInvokeOperation(
                    operation,
                    arguments
                );
            }
        }

        /**
         * @param operation
         * @param arguments
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
         */
        public Structure_1_0 objInvokeOperationInUnitOfWork(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            if(BLOCK_OPERATIONS) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                    new BasicException.Parameter("operation", operation)
                },
                "Operations can't be handled by the persistence manager"
            );
            synchronized(currentUnitOfWork().getLock()) {
                return this.clean.objInvokeOperationInUnitOfWork(
                    operation,
                    arguments
                );
            }
        }

        final UnitOfWork_1_3 currentUnitOfWork(
        ){
            return MultithreadedObjectFactory_1.this.unitOfWork.get();
        }
        
        private PersistenceCapable_1_0 accessor(){
            PersistenceCapable_1_0 transactional = currentUnitOfWork().get(this); 
            return transactional == null ? this.clean : transactional;
        }
        
        /**
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDeleted()
         */
        public final boolean objIsDeleted(
        ) throws ServiceException {
            return accessor().objIsDeleted();
        }

        /**
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDirty()
         */
        public final boolean objIsDirty(
        )throws ServiceException {
            
            return accessor().objIsDirty();
        }

        /**
         * @return
         * @see org.openmdx.base.accessor.generic.cci.Object_1_2#objIsInaccessable()
         */
        public boolean objIsInaccessable() {
            return 
                this.clean instanceof Object_1_2 &&
                ((Object_1_2)this.clean).objIsInaccessable();
        }

        /**
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsInUnitOfWork()
         */
        public final boolean objIsInUnitOfWork(
        ) throws ServiceException {
            return currentUnitOfWork().get(this) != null;
        }

        /**
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsNew()
         */
        public final boolean objIsNew(
        ) throws ServiceException{
            return accessor().objIsNew();
        }

        /**
         * @return
         * @throws ServiceException 
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsPersistent()
         */
        public final boolean objIsPersistent(
        ) throws ServiceException {
            return accessor().objIsPersistent();
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMakeVolatile()
         */
        public void objMakeVolatile(
        ) throws ServiceException {
            this.clean.objMakeVolatile();
        }

        /**
         * @param there
         * @param criteria
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
         */
        public void objMove(
            FilterableMap<String, Object_1_0> there, 
            String criteria
        ) throws ServiceException {
            String qualifier;
            if(criteria == null) {
                qualifier = currentUnitOfWork().next().toString();
            } else if(!there.containsKey(criteria)) {
                qualifier = criteria;
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.DUPLICATE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("qualifier", criteria)
                },
                "There is already an object with the given criteria"
            );
            try {
                MultithreadedContainerDecorator container = (MultithreadedContainerDecorator) there;
                container.put(
                     qualifier,
                     this
                );
                if(container.isPersistent()) {
                    objMakePersistent(container.getPath().getChild(qualifier));
                }
            } catch (RuntimeException exception) {
                throw new ServiceException(exception);
            }
        }

        void objMakePersistent(
            Path path
        ) throws ServiceException{
            currentUnitOfWork().add(path, this);
            for(Object proxy : this.proxies.values()) {
                if(proxy instanceof MultithreadedContainerDecorator) {
                    ((MultithreadedContainerDecorator)proxy).makePersistent();
                }
            }
            if(this.instanceCallbackListener != null) {
                this.instanceCallbackListener.optimisticPostCreate(
                    new InstanceCallbackEvent(
                        InstanceCallbackEvent.POST_CREATE,
                        this,
                        null
                    )
                );
            }
        }
        
        /**
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRefresh()
         */
        public void objRefresh(
        ) throws ServiceException {
            currentUnitOfWork().remove(this);
            this.clean.objRefresh(); 
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemove()
         */
        public void objRemove(
        ) throws ServiceException {
            Path objectId = objGetPath();
            if(objectId != null) {
                int componentCount = objectId.size();
                if(componentCount < 3) throw new IllegalArgumentException(
                    "Object is not removable: " + objectId
                );
                getObject(objectId.getPrefix(componentCount - 2)).objAddToUnitOfWork();
                currentUnitOfWork().add(this).objRemove();
            }
        }

        /**
         * @param feature
         * @param listener
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
         */
        public void objRemoveEventListener(
            String feature, 
            EventListener listener
        ) throws ServiceException {
            this.clean.objRemoveEventListener(feature, listener);
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
         */
        public void objRemoveFromUnitOfWork(
        ) throws ServiceException {
            if(objIsDirty()) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                },
                "Dirty objects can't be removed from the unit of work"
            );
            currentUnitOfWork().remove(this);
        }

        /**
         * @param feature
         * @param to
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objSetValue(java.lang.String, java.lang.Object)
         */
        public void objSetValue(
            String feature, 
            Object to
        ) throws ServiceException {
            currentUnitOfWork().add(this).objSetValue(feature, to);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objDefaultFetchGroup()
         */
        public Set<String> objDefaultFetchGroup(
        ) throws ServiceException {
            return accessor().objDefaultFetchGroup();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objFlush()
         */
        public boolean objFlush(
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                },
                "Flushing is not supported by " + MultithreadedObjectFactory_1.class.getSimpleName()
            );
        }

        PersistenceCapable_1_0 objGetDelegate(
            boolean readOnly
        ) throws ServiceException{
            UnitOfWork_1_3 unitOfWork = currentUnitOfWork();
            if(readOnly) {
                PersistenceCapable_1_0 transactional = currentUnitOfWork().get(this);
                return transactional == null ? this.clean : transactional;
            } else {
                return unitOfWork.add(MultithreadedObject_1.this);
            }
        }
        
        //--------------------------------------------------------------------
        // Class MultithreadedContainer
        //--------------------------------------------------------------------
        
        class MultithreadedContainer 
            extends AbstractMap<String, Object_1_0>
            implements FilterableMap<String, Object_1_0> 
        {
            
            MultithreadedContainer(
                String feature
            ) throws ServiceException {
                this.feature = feature;
            }
            
            private final String feature;
            
            private final Set<Map.Entry<String, Object_1_0>> entries = new MultithreadedEntrySet();

            final FilterableMap<String, Object_1_0> getDelegate(
                boolean readOnly
            ){
                try {
                    return objGetDelegate(readOnly).objGetContainer(this.feature);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            
            /* (non-Javadoc)
             * @see java.util.Map#entrySet()
             */
            public Set<Map.Entry<String, Object_1_0>> entrySet() {
                return this.entries;
            }

            /* (non-Javadoc)
             * @see java.util.Map#get(java.lang.Object)
             */
            public Object_1_0 get(Object key) {
                return getDelegate(true).get(key);
            }

            /* (non-Javadoc)
             * @see java.util.Map#put(java.lang.Object, java.lang.Object)
             */
            public Object_1_0 put(String key, Object_1_0 value) {                
                return getDelegate(false).put(key, value);
                
            }

            /* (non-Javadoc)
             * @see java.util.Map#remove(java.lang.Object)
             */
            public Object_1_0 remove(Object key) {
                Object_1_0 value = get(key);
                try {
                    if(value != null && value.objIsPersistent()) {
                        value.objRemove();
                    } else {
                        getDelegate(false).remove(key);
                    }
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
                return value;
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
             */
            public FilterableMap<String, Object_1_0> subMap(Object filter) {
                return new ObjectMapDecorator(getDelegate(true).subMap(filter));
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
             */
            public List<Object_1_0> values(Object criteria) {
                return new ObjectListDecorator(getDelegate(true).values(criteria));
            }

            
            //----------------------------------------------------------------
            // Class OptimisticEntrySet
            //----------------------------------------------------------------

            class MultithreadedEntrySet extends AbstractSet<Map.Entry<String, Object_1_0>> {

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#iterator()
                 */
                @Override
                public Iterator<java.util.Map.Entry<String, Object_1_0>> iterator() {
                    return new OptimisticIterator(
                        getDelegate(true).entrySet().iterator()
                    );
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#size()
                 */
                @Override
                public int size() {
                    return getDelegate(true).size();
                }
                
            }
                
            //----------------------------------------------------------------
            // Class OptimisticIterator
            //----------------------------------------------------------------
            
            class OptimisticIterator implements Iterator<Map.Entry<String, Object_1_0>> {
                
                /**
                 * Constructor 
                 * @param delegate 
                 *
                 */
                OptimisticIterator(
                    Iterator<Map.Entry<String, Object_1_0>> delegate
                ) {
                    try {
                        this.transactional = objIsInUnitOfWork();
                        this.delegate = delegate;
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                }

                /**
                 * 
                 */
                private final boolean transactional;
                
                /**
                 * 
                 */
                private final Iterator<Map.Entry<String, Object_1_0>> delegate;
                
                /**
                 * The current entry
                 */
                private Map.Entry<String, Object_1_0> current = null;
                
                /* (non-Javadoc)
                 * @see java.util.Iterator#hasNext()
                 */
                public boolean hasNext() {
                    return this.delegate.hasNext();
                }

                /* (non-Javadoc)
                 * @see java.util.Iterator#next()
                 */
                public java.util.Map.Entry<String, Object_1_0> next() {
                    return this.current = this.delegate.next();
                }

                /* (non-Javadoc)
                 * @see java.util.Iterator#remove()
                 */
                public void remove() {
                    if(this.current == null) throw new IllegalStateException(
                        "The iterator has no current element"
                    );
                    try {
                        Object_1_0 value = this.current.getValue();
                        if(this.transactional || !value.objIsPersistent()) {
                            this.delegate.remove();
                        } else {
                            objAddToUnitOfWork();
                            delete(value);
                        }
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    } finally {
                        this.current = null;
                    }
                }
                
            }
                
        }
            
        //--------------------------------------------------------------------
        // Class ContainerDecorator
        //--------------------------------------------------------------------

        class MultithreadedContainerDecorator extends MarshallingFilterableMap<String,Object_1_0,MultithreadedContainer> {

            /**
             * Constructor 
             * 
             * @param feature 
             * @param marshaller
             * @param container
             */
            MultithreadedContainerDecorator(
                String feature,
                org.openmdx.compatibility.base.marshalling.Marshaller marshaller, 
                MultithreadedContainer container
            ) {
                super(marshaller, container);
                this.feature = feature;
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = 7636128825174630002L;

            private final String feature;
            
            private Path path = null;
            
            boolean isPersistent() throws ServiceException{
                return objIsPersistent();
            }
            
            boolean isNew() throws ServiceException{
                return objIsNew();
            }

            Path getPath() throws ServiceException{
                if(this.path == null) {
                    Path object = objGetPath();
                    if(object != null) {
                        this.path = object.getChild(this.feature);
                    }
                }
                return this.path;
            }
            
            void makePersistent (
            ) throws ServiceException{
                for(Map.Entry<String,Object_1_0> e : entrySet()) {
                    if(e.getValue() instanceof MultithreadedObject_1) {
                        ((MultithreadedObject_1)e.getValue()).objMakePersistent(
                            getPath().getChild(e.getKey())
                        );
                    } else {
                        logger.warn(
                            "Object is not an instance of MultithreadedObject_1: {}", 
                            e.getValue().getClass().getName()
                        );
                    }
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingFilterableMap#values(java.lang.Object)
             */
            @Override
            public List<Object_1_0> values(Object criteria) {
                return new ObjectListDecorator(super.values(criteria));
            }

            private Path getPath(
                Object key
            ){
                if(key instanceof String) try {
                    Path path = getPath();
                    return path == null ? null : path.getChild((String)key);
                } catch (ServiceException exception) {
                    return null;
                } else {
                    return null;
                }
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingMap#get(java.lang.Object)
             */
            @Override
            public Object_1_0 get(Object key) {
                UnitOfWork_1_3 unitOfWork = currentUnitOfWork();
                if(unitOfWork.isActive()) {
                    Path path = getPath(key);
                    if(path == null) {
                        return super.get(key);
                    } else if (unitOfWork.isInaccessible(path)){
                        return null;
                    } else {
                        Object_1_0 object = super.get(key);
                        if(object == null) {
                            unitOfWork.setInaccessible(path, true);
                        }
                        return object;
                    }
                } else {
                    return super.get(key);
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingMap#containsKey(java.lang.Object)
             */
            @Override
            public boolean containsKey(Object key) {
                UnitOfWork_1_3 unitOfWork = currentUnitOfWork();
                if(unitOfWork.isActive()) {
                    Path path = getPath(key);
                    if(path == null) {
                        return super.containsKey(key);
                    } else if (unitOfWork.isInaccessible(path)){
                        return false;
                    } else {
                        boolean accessible = super.containsKey(key);
                        if(!accessible) {
                            unitOfWork.setInaccessible(path, true);
                        }
                        return accessible;
                    }
                } else {
                    return super.containsKey(key);
                }
            }

            
            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingMap#put(java.lang.Object, java.lang.Object)
             */
            @Override
            public Object_1_0 put(String key, Object_1_0 value) {
                Path path = getPath(key);
                if(path != null) {
                    currentUnitOfWork().setInaccessible(path, false);
                }
                return super.put(key, value);
            }

            
        }
        
        //--------------------------------------------------------------------
        // Class MultithreadedList
        //--------------------------------------------------------------------

        @SuppressWarnings("unchecked")
        class MultithreadedList 
            extends AbstractList<Object>
        {

            /**
             * Constructor 
             *
             * @param oldObjects
             */
            MultithreadedList(
                String feature
            ){
                this.feature = feature;
            }
            
            /**
             * 
             */
            private final String feature;
            
            /**
             * 
             * @return
             */
            private final List<Object> getDelegate(
                boolean readOnly
            ){
                try {
                    return objGetDelegate(readOnly).objGetList(this.feature);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            
            /* (non-Javadoc)
             * @see java.util.AbstractList#get(int)
             */
            @Override
            public Object get(int index) {
                return getDelegate(true).get(index);
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#size()
             */
            @Override
            public int size() {
                return getDelegate(true).size();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractList#add(int, java.lang.Object)
             */
            @Override
            public void add(int index, Object element) {
                getDelegate(false).add(index, element);
            }

            /* (non-Javadoc)
             * @see java.util.AbstractList#remove(int)
             */
            @Override
            public Object remove(int index) {
                return getDelegate(false).remove(index);
            }

            /* (non-Javadoc)
             * @see java.util.AbstractList#set(int, java.lang.Object)
             */
            @Override
            public Object set(int index, Object element) {
                return getDelegate(false).set(index, element);
            }

        }

        
        //--------------------------------------------------------------------
        // Class MultithreadedSortedMap
        //--------------------------------------------------------------------

        @SuppressWarnings("unchecked")
        class MultithreadedSortedMap 
            extends AbstractMap<Integer,Object>
            implements SortedMap<Integer,Object>
        {

            /**
             * Constructor 
             *
             * @param feature
             */
            MultithreadedSortedMap(
                String feature
            ) throws ServiceException {
                this.feature = feature;
                this.clean = MultithreadedObject_1.this.clean.objGetSparseArray(feature); 
                this.entries = new OptimisticEntrySet(null, null); 
            }
            
            /**
             * Constructor 
             *
             * @param feature
             */
            private MultithreadedSortedMap(
                String feature,
                SortedMap<Integer,Object> clean,
                Integer fromKey,
                Integer toKey
            ){
                this.feature = feature;
                this.clean = clean; 
                this.entries = new OptimisticEntrySet(fromKey, toKey); 
            }
            
            /**
             * 
             */
            final String feature;
            
            /**
             * 
             */
            final SortedMap<Integer,Object> clean;
            
            /**
             * 
             */
            private final OptimisticEntrySet entries;
            
            /* (non-Javadoc)
             * @see java.util.AbstractMap#entrySet()
             */
            @Override
            public Set<java.util.Map.Entry<Integer, Object>> entrySet() {
                return this.entries;
            }
            
            /* (non-Javadoc)
             * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
             */
            @Override
            public Object put(Integer key, Object value) {
                try {
                    return currentUnitOfWork(
                    ).add(
                        MultithreadedObject_1.this
                    ).objGetSparseArray(
                        MultithreadedSortedMap.this.feature
                    ).put(
                        key,
                        value
                    );
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#comparator()
             */
            public Comparator<? super Integer> comparator() {
                return this.clean.comparator();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#firstKey()
             */
            public Integer firstKey() {
                return this.entries.getDelegate(true).firstKey();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#headMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> headMap(
                Integer toKey
            ) {
                return new  MultithreadedSortedMap(
                    this.feature,
                    this.clean,
                    null, // fromKey
                    toKey
                 );
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#lastKey()
             */
            public Integer lastKey() {
                return this.entries.getDelegate(true).lastKey();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
             */
            public SortedMap<Integer, Object> subMap(
                Integer fromKey,
                Integer toKey
            ) {
                return new  MultithreadedSortedMap(
                    this.feature,
                    this.clean,
                    fromKey, 
                    toKey
                 );
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#tailMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> tailMap(Integer fromKey) {
                return new  MultithreadedSortedMap(
                    this.feature,
                    this.clean,
                    fromKey, 
                    null // toKey
                 );
            }

            
            //----------------------------------------------------------------
            // Class OptimisticEntrySet
            //----------------------------------------------------------------

            /**
             * Optimistic Entry Set
             */
            class OptimisticEntrySet extends AbstractSet<Map.Entry<Integer,Object>> {

                OptimisticEntrySet(
                    Integer fromKey,
                    Integer toKey
                ){
                    this.fromKey = null;
                    this.toKey = null;
                }
                
                private final Integer fromKey;
                private final Integer toKey;
                
                /**
                 * 
                 * @param readOnly 
                 * 
                 * @return
                 */
                final SortedMap<Integer,Object> getDelegate(
                    boolean readOnly
                ){
                    PersistenceCapable_1_1 transactional = currentUnitOfWork().get(MultithreadedObject_1.this);
                    try {
                        SortedMap<Integer,Object> delegate = transactional == null ?
                            (readOnly ? MultithreadedSortedMap.this.clean : null) :
                            transactional.objGetSparseArray(MultithreadedSortedMap.this.feature);
                        return 
                            fromKey == null && toKey == null ? delegate :
                            fromKey == null ? delegate.headMap(toKey) :
                            toKey == null ? delegate.tailMap(fromKey) :
                            delegate.subMap(fromKey, toKey);
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#iterator()
                 */
                @Override
                public Iterator<java.util.Map.Entry<Integer, Object>> iterator() {
                    SortedMap<Integer,Object> transactional = getDelegate(false);
                    return transactional == null ? new OptimisticIterator() : transactional.entrySet().iterator();
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#size()
                 */
                @Override
                public int size() {
                    return getDelegate(true).size();
                }

                
                //------------------------------------------------------------
                // Class OptimisticIterator
                //------------------------------------------------------------
                
                /**
                 * Optimistic Iterator
                 */
                class OptimisticIterator implements Iterator<Map.Entry<Integer, Object>> {
                    
                    /**
                     * 
                     */
                    private final Iterator<Map.Entry<Integer, Object>> clean = getDelegate(true).entrySet().iterator();

                    /**
                     * 
                     */
                    private Map.Entry<Integer, Object> current = null;
                    
                    /* (non-Javadoc)
                     * @see java.util.Iterator#hasNext()
                     */
                    public boolean hasNext() {
                        return this.clean.hasNext();
                    }

                    /* (non-Javadoc)
                     * @see java.util.Iterator#next()
                     */
                    public Map.Entry<Integer, Object> next() {
                        return this.current = this.clean.next();
                    }

                    /* (non-Javadoc)
                     * @see java.util.Iterator#remove()
                     */
                    public void remove() {
                        if(this.current == null) throw new IllegalStateException(
                            "Ierator has no current element"
                        );
                        MultithreadedSortedMap.this.put(current.getKey(), null);
                        this.current = null;
                    }
                    
                }
                
            }

        }

        
        //--------------------------------------------------------------------
        // Class MultithreadedSet
        //--------------------------------------------------------------------

        @SuppressWarnings("unchecked")
        class MultithreadedSet 
            extends AbstractSet<Object>
        {

            /**
             * Constructor 
             *
             * @param oldObjects
             */
            MultithreadedSet(
                String feature
            ){
                this.feature = feature;
            }
            
            /**
             * 
             */
            private final String feature;
            
            final Set<Object> getDelegate(
                boolean readOnly
            ){
                try {
                    return objGetDelegate(readOnly).objGetSet(this.feature);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
            
            /* (non-Javadoc)
             * @see java.util.AbstractCollection#size()
             */
            @Override
            public int size() {
                return getDelegate(true).size();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#iterator()
             */
            @Override
            public Iterator<Object> iterator() {
                PersistenceCapable_1_1 transactional = currentUnitOfWork().get(MultithreadedObject_1.this);
                return transactional == null ? new OptimisticIterator() : getDelegate(false).iterator();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#add(java.lang.Object)
             */
            @Override
            public boolean add(Object o) {
                return getDelegate(false).add(o);
            }

            
            //----------------------------------------------------------------
            // Class OptimisticIterator
            //----------------------------------------------------------------

            /**
             * 
             */
            class OptimisticIterator implements Iterator {

                /**
                 * The read-only delegate
                 */
                private final Iterator delegate = getDelegate(true).iterator();

                /**
                 * The iterator's current object
                 */
                private Object current;
                
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
                    if(this.current == null) throw new IllegalStateException(
                        "No current element"
                    );
                    getDelegate(false).remove(this.current);
                    this.current = null;
                }
                
            }
            
        }

        
        //--------------------------------------------------------------------
        // Class MultithreadedLargeObject
        //--------------------------------------------------------------------
        
        /**
         * Multithreaded Large Object
         */
        class MultithreadedLargeObject implements LargeObject_1_0 {

            /**
             * Constructor 
             *
             * @param feature
             * @throws ServiceException  
             */
            MultithreadedLargeObject(
                String feature
            ) throws ServiceException {
                this.feature = feature;
                this.clean = MultithreadedObject_1.this.clean.objGetLargeObject(feature);
            }
            
            /**
             * 
             */
            private final String feature;

            /**
             * 
             */
            private final LargeObject_1_0 clean;
            
            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBinaryStream()
             */
            public InputStream getBinaryStream(
            ) throws ServiceException {
                return this.clean.getBinaryStream();
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBinaryStream(java.io.OutputStream, long)
             */
            public void getBinaryStream(
                OutputStream stream, 
                long position
            ) throws ServiceException {
                this.clean.getBinaryStream(stream, position);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBytes(long, int)
             */
            public byte[] getBytes(
                long position, 
                int capacity
            ) throws ServiceException {
                return this.clean.getBytes(position, capacity);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream()
             */
            public Reader getCharacterStream(
            ) throws ServiceException {
                return this.clean.getCharacterStream();
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream(java.io.Writer, long)
             */
            public void getCharacterStream(
                Writer writer, 
                long position
            ) throws ServiceException {
                this.clean.getCharacterStream(writer, position);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacters(long, int)
             */
            public char[] getCharacters(
                long position, 
                int capacity
            ) throws ServiceException {
                return this.clean.getCharacters(position, capacity);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#length()
             */
            public long length(
            ) throws ServiceException {
                return this.clean.length();
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(java.io.InputStream, long)
             */
            public void setBinaryStream(
                InputStream stream, 
                long size
            ) throws ServiceException {
                currentUnitOfWork(
                ).add(
                    MultithreadedObject_1.this
                ).objGetLargeObject(
                    this.feature
                ).setBinaryStream(
                    stream,
                    size
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(long)
             */
            public OutputStream setBinaryStream(
                long position
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                        new BasicException.Parameter("feature", this.feature)
                    },
                    "OutputStream not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBytes(long, byte[])
             */
            public void setBytes(
                long position, 
                byte[] content
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                        new BasicException.Parameter("feature", this.feature)
                    },
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(java.io.Reader, long)
             */
            public void setCharacterStream(
                Reader stream, 
                long size
            ) throws ServiceException {
                currentUnitOfWork(
                ).add(
                    MultithreadedObject_1.this
                ).objGetLargeObject(
                    this.feature
                ).setCharacterStream(
                    stream,
                    size
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(long)
             */
            public Writer setCharacterStream(
                long position
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                        new BasicException.Parameter("feature", this.feature)
                    },
                    "Writer not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacters(long, char[])
             */
            public void setCharacters(
                long position, 
                char[] content
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                        new BasicException.Parameter("feature", this.feature)
                    },
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#truncate(long)
             */
            public void truncate(
                long length
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                        new BasicException.Parameter("feature", this.feature)
                    },
                    "Partial update not supported"
                );
            }
            
        }

    }
    
    
    //------------------------------------------------------------------------
    // Class TransactionalObject_1
    //------------------------------------------------------------------------
    
    /**
     * Persistence Capable 1.1 Implementation
     */
    @SuppressWarnings("unchecked")
    public class TransactionalObject_1
        implements PersistenceCapable_1_1
    {
        
        /**
         * Constructor 
         * @throws ServiceException 
         */
        TransactionalObject_1(
            Object_1_0 clean
        ) throws ServiceException {
            this.clean = clean;
            this.state = clean.objIsPersistent() ? State.PERSISTENT_CLEAN : State.TRANSIENT_CLEAN;
        }

        /**
         * 
         */
        final Object_1_0 clean;

        /**
         * 
         */
        private State state;
        
        /**
         * 
         */
        private Path transactionalObjectId;
        
        /**
         * 
         */
        private final Map<String,Object> values = new HashMap<String,Object>();
        
        /**
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objDefaultFetchGroup()
         */
        public Set<String> objDefaultFetchGroup(
        ) throws ServiceException {
            Set<String> defaultFetchGroup = this.clean.objDefaultFetchGroup();
            defaultFetchGroup.addAll(this.values.keySet());
            return defaultFetchGroup;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
         */
        public FilterableMap<String, Object_1_0> objGetContainer(
            String feature
        ) throws ServiceException {
            FilterableMap<String, Object_1_0> container = (FilterableMap<String, Object_1_0>) this.values.get(feature);
            if(container == null) {
                this.values.put(
                    feature,
                    container = new TransactionalContainer_1(feature)
                );
            }
            return container;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetLargeObject(java.lang.String)
         */
        public LargeObject_1_0 objGetLargeObject(
            String feature
        ) throws ServiceException {
            LargeObject_1_0 copy = (LargeObject_1_0) this.values.get(feature);
            if(copy == null) {
                this.values.put(
                    feature,
                    copy = new TransactionalLargeObject_1()
                );
            }
            return copy;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetList(java.lang.String)
         */
        public List<Object> objGetList(
            String feature
        ) throws ServiceException {
            List<Object> copy = (List<Object>) this.values.get(feature);
            if(copy == null) {
                this.values.put(
                    feature,
                    copy = new ArrayList(this.clean.objGetList(feature))
                );
            }
            return copy;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetSet(java.lang.String)
         */
        public Set<Object> objGetSet(
            String feature
        ) throws ServiceException {
            Set<Object> copy = (Set<Object>) this.values.get(feature);
            if(copy == null) {
                this.values.put(
                    feature,
                    copy = new HashSet(this.clean.objGetSet(feature))
                );
            }
            return copy;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetSparseArray(java.lang.String)
         */
        public SortedMap<Integer, Object> objGetSparseArray(
            String feature
        ) throws ServiceException {
            SortedMap<Integer, Object> copy = (SortedMap<Integer, Object>) this.values.get(feature);
            if(copy == null) {
                this.values.put(
                    feature,
                    copy = new TreeMap<Integer,Object>(this.clean.objGetSparseArray(feature))
                );
            }
            return copy;
        }

        /**
         * @param feature
         * @return
         * @throws ServiceException
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetValue(java.lang.String)
         */
        public Object objGetValue(
            String feature
        ) throws ServiceException {
            Object copy = this.values.get(feature);
            if(copy == null && !this.values.containsKey(feature)) {
                this.values.put(
                    feature,
                    copy = this.clean.objGetValue(feature)
                );
            }
            return copy;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objSetValue(java.lang.String, java.lang.Object)
         */
        public void objSetValue(
            String feature, 
            Object to
        ) throws ServiceException {
            this.values.put(feature, to);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.PersistenceCapable_1_1#objRemove()
         */
        public void objRemove(
        ) {
            switch(this.state) {
                case PERSISTENT_NONTRANSACTIONAL: 
                case PERSISTENT_CLEAN: 
                    this.state = State.PERSISTENT_DELETED; 
                    break;
                case PERSISTENT_NEW: 
                    this.state = State.PERSISTENT_NEW_DELETED; 
                    break;
                case PERSISTENT_DELETED: 
                case PERSISTENT_NEW_DELETED: 
                    break;
                default:
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                            new BasicException.Parameter("state", this.state)
                        },
                        "Only persistent objects may be deleted"
                    );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.PersistenceCapable_1_1#objMakeDirty()
         */
        public boolean objMakeDirty(
        ) {
            switch(this.state) {
                case TRANSIENT_CLEAN: 
                    this.state = State.TRANSIENT_DIRTY; 
                    break;
                case PERSISTENT_CLEAN: 
                    this.state = State.PERSISTENT_DIRTY; 
                    break;
                case PERSISTENT_DELETED: 
                case PERSISTENT_NEW_DELETED: 
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                            new BasicException.Parameter("state", this.state)
                        },
                        "Deleted objects can't be modified"
                    );
                case DETACHED_CLEAN: 
                    this.state = State.DETACHED_DIRTY; 
                    break;
            }
            return true;
        }

        public Object_1_0 objCopy(
            FilterableMap<String, Object_1_0> there,
            String criteria
        ) throws ServiceException {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                    new BasicException.Parameter("criteria", criteria)
                },
                "Copy not implemented"
            ); 
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
         */
        public void objMove(
            FilterableMap<String, Object_1_0> there,
            String criteria
        ) throws ServiceException {
            System.out.println("Wait a moment");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objGetPath()
         */
        public Path objGetPath(
        ) throws ServiceException {
            return objIsNew() ?
                this.transactionalObjectId :
                this.clean.objGetPath();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.PersistenceCapable_1_1#objMakePersistent(org.openmdx.compatibility.base.naming.Path)
         */
        public void objMakePersistent(Path path) {
            this.state = State.PERSISTENT_NEW;
            this.transactionalObjectId = path;
        }

        /**
         * Retrieve the object id to be embedded into exceptions
         * 
         * @return the object id as string
         */
        private String objGetResourceIdentifier (
        ){
            try {
                Path path = objGetPath();
                return path == null ? null : path.toXri();
            } catch (ServiceException exception) {
                return "";
            }
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objFlush()
         */
        public boolean objFlush(
        ) throws ServiceException {
            if(!this.clean.objIsDeleted()) {
                if(this.objIsDeleted()) {
                    if(this.clean.objIsPersistent()) {
                        this.clean.objRemove();                
                    }
                } else {
                    for(Map.Entry<String, Object> e : this.values.entrySet()) {
                        Object value = e.getValue();
                        String feature = e.getKey();
                        if(value instanceof List) {
                            List<Object> dirty = (List<Object>) value;
                            List<Object> clean = this.clean.objGetList(feature);
                            if(!clean.equals(dirty)) {
                                clean.clear();
                                clean.addAll(dirty);
                            }
                        } else if (value instanceof Set) {
                            Set<Object> dirty = (Set<Object>) value;
                            Set<Object> clean = this.clean.objGetSet(feature);
                            if(!clean.equals(dirty)) {
                                clean.clear();
                                clean.addAll(dirty);
                            }
                        } else if (value instanceof SortedMap) {
                            SortedMap<Integer,Object> dirty = (SortedMap<Integer, Object>) value;
                            SortedMap<Integer,Object> clean = this.clean.objGetSparseArray(feature);
                            if(!clean.equals(dirty)) {
                                clean.clear();
                                clean.putAll(dirty);
                            }
                        } else if (value instanceof LargeObject_1_0) {
                            LargeObject_1_0 dirty = (LargeObject_1_0) value;
                            LargeObject_1_0 clean = this.clean.objGetLargeObject(feature);
                            if(dirty.getBinaryStream() != null) {
                                clean.setBinaryStream(
                                    dirty.getBinaryStream(), 
                                    dirty.length()
                                );
                            } else if (dirty.getCharacterStream() != null) {
                                clean.setCharacterStream(
                                    dirty.getCharacterStream(),
                                    dirty.length()
                                );
                            }
                        } else if (value instanceof FilterableMap) {
                            ((TransactionalContainer_1)value).objFlush();
                        } else {
                            Object dirty = value;
                            Object clean = this.clean.objGetValue(feature);
                            if(
                                dirty != clean && (
                                    dirty == null || clean == null || !dirty.equals(clean)
                                )
                            ){
                                this.clean.objSetValue(feature, dirty);
                            }
                        }
                    }
                }
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
         */
        public Structure_1_0 objInvokeOperation(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                    new BasicException.Parameter("state", this.state),
                    new BasicException.Parameter("operation", operation)
                },
                "Operations can't be handled by the persistence manager"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
         */
        public Structure_1_0 objInvokeOperationInUnitOfWork(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objGetResourceIdentifier()),
                    new BasicException.Parameter("state", this.state),
                    new BasicException.Parameter("operation", operation)
                },
                "Operations can't be handled by the persistence manager"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDeleted()
         */
        public boolean objIsDeleted()
            throws ServiceException {
            return this.state.interrogation().contains(State.Interrogation.DELETED);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
         */
        public boolean objIsDirty()
            throws ServiceException {
            return this.state.interrogation().contains(State.Interrogation.DIRTY);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsInUnitOfWork()
         */
        public boolean objIsInUnitOfWork(
        ) throws ServiceException {
            return this.state.interrogation().contains(State.Interrogation.TRANSACTIONAL);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsNew()
         */
        public boolean objIsNew()
            throws ServiceException {
            return this.state.interrogation().contains(State.Interrogation.NEW);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsPersistent()
         */
        public boolean objIsPersistent()
            throws ServiceException {
            return this.state.interrogation().contains(State.Interrogation.PERSISTENT);
        }

        
        //--------------------------------------------------------------------
        // Class TransactionalContainer
        //--------------------------------------------------------------------
        
        /**
         * 
         */
        class TransactionalContainer_1
            extends AbstractMap<String, Object_1_0>
            implements FilterableMap<String, Object_1_0> 
        {

            TransactionalContainer_1(
                String feature
            ) throws ServiceException{
                this.feature = feature;
                this.clean = TransactionalObject_1.this.clean.objGetContainer(feature);
                this.newObjects = new HashMap<String, Object_1_0>();
            }
            
            final String feature;
            
            /**
             * 
             */
            private static final long serialVersionUID = 3659671141522658667L;

            /**
             * 
             */
            final FilterableMap<String, Object_1_0> clean;

            /**
             * 
             */
            final Map<String, Object_1_0> newObjects;
            
            /**
             * 
             */
            private final Set<Entry<String, Object_1_0>> entries = new EntrySet();
            
            /**
             * 
             */
            boolean cleared = false;
            
            /* (non-Javadoc)
             * @see org.openmdx.uses.org.apache.commons.collections.map.ListOrderedMap#clear()
             */
            @Override
            public void clear() {   
                this.cleared = true;
                this.newObjects.clear();
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
             */
            public FilterableMap<String, Object_1_0> subMap(Object filter) {
                return this.clean.subMap(filter); // ignore cache
                // TODO take cache into consideration!
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
             */
            public List<Object_1_0> values(Object criteria) {
                if(criteria == null) {
                    return Collections.unmodifiableList(
                        new ArrayList(values())
                    );
                } else {
                    return this.clean.values(criteria); // ignore cache
                    // TODO take cache into consideration!
                }
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.uses.org.apache.commons.collections.map.ListOrderedMap#values()
             */
            @Override
            public Collection values() {
                return super.values(); // ignore cache
                // TODO take cache into consideration!
            }

            void objFlush(){
                if(this.cleared) {
                    this.clean.clear();
                }
                this.clean.putAll(this.newObjects);
            }
            
            
            /* (non-Javadoc)
             * @see java.util.AbstractMap#entrySet()
             */
            @Override
            public Set<Entry<String, Object_1_0>> entrySet() {
                return this.entries;
            }

            /* (non-Javadoc)
             * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
             */
            @Override
            public Object_1_0 put(String key, Object_1_0 value) {
                return this.newObjects.put(key, value);
            }


            //----------------------------------------------------------------
            // Class EntrySet
            //----------------------------------------------------------------
            
            class EntrySet extends AbstractSet<Entry<String, Object_1_0>> {

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#iterator()
                 */
                @Override
                public Iterator<Map.Entry<String, Object_1_0>> iterator() {
                    Iterator<Map.Entry<String, Object_1_0>> transactional = TransactionalContainer_1.this.newObjects.entrySet().iterator();
                    return new EntryIterator(
                        TransactionalContainer_1.this.cleared ? new Iterator[]{
                            transactional 
                        } : new Iterator[]{
                            transactional,
                            TransactionalContainer_1.this.clean.entrySet().iterator()
                        }
                    );
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#size()
                 */
                @Override
                public int size() {
                    try {
                        int n = 0;
                        for(Object_1_0 o : TransactionalContainer_1.this.newObjects.values()) {
                            if(!o.objIsDeleted()) n++;
                        }
                        if(objIsPersistent() && !objIsNew()) {
                            int c = TransactionalContainer_1.this.cleared ? 0 : TransactionalContainer_1.this.clean.size();
                            UnitOfWork_1_3 unitOfWork = MultithreadedObjectFactory_1.this.unitOfWork.get();
                            int d = unitOfWork.countPersistentDeleted(
                                objGetPath().getChild(TransactionalContainer_1.this.feature)
                            );
                            return c + n - d;
                        } else {
                            return n;
                        }
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                }
                
            }
            
            //----------------------------------------------------------------
            // Class EntrySet
            //----------------------------------------------------------------
            
            class EntryIterator implements Iterator<Map.Entry<String, Object_1_0>> {

                /**
                 * Constructor 
                 *
                 * @param delegate
                 */
                EntryIterator(
                    Iterator<Map.Entry<String, Object_1_0>>[] delegate
                ) {
                    this.delegate = delegate;
                }                
                
                private final Iterator<Entry<String, Object_1_0>>[] delegate;
                
                private int i = 0;
                
                private Map.Entry<String, Object_1_0> current = null;

                private Map.Entry<String, Object_1_0> next = null;
                
                /* (non-Javadoc)
                 * @see java.util.Iterator#hasNext()
                 */
                public boolean hasNext() {
                    if(this.next == null) try {
                        while(this.i < this.delegate.length) {
                            while(this.delegate[this.i].hasNext()) {
                                Map.Entry<String, Object_1_0> candidate = this.delegate[i].next();
                                if(accept(candidate.getValue())) {
                                    this.next = candidate;
                                    return true;
                                }
                            }
                            this.i++;
                        }
                        return false;
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    } else {
                        return true;
                    }
                }

                protected boolean accept(
                    Object_1_0 object
                ) throws ServiceException{
                    return !object.objIsDeleted();
                }
                
                /* (non-Javadoc)
                 * @see java.util.Iterator#next()
                 */
                public Entry<String, Object_1_0> next() {
                    if(hasNext()) {
                        this.current = this.next;
                        this.next = null;
                        return this.current;
                    } else throw new NoSuchElementException(
                        "The iterator has no more elements"
                    );
                }

                /* (non-Javadoc)
                 * @see java.util.Iterator#remove()
                 */
                public void remove() {
                    if(this.current == null) throw new IllegalStateException(
                        "The iterator has no current element"
                    );
                    if(!delete(this.current.getValue())) {
                        this.delegate[this.i].remove();
                    }
                    this.current = null;
                }

            }
            
        }
        
        //--------------------------------------------------------------------
        // Class TransactionalLargeObject_1
        //--------------------------------------------------------------------

        class TransactionalLargeObject_1 implements LargeObject_1_0 {
           
            /**
             * 
             */
            private long size = 0;
            
            /**
             * 
             */
            private InputStream binaryStream = null;

            /**
             * 
             */
            private Reader characterStream = null;
            
            /**
             * This method is internally used to commit changes
             */
            public InputStream getBinaryStream(
            ) throws ServiceException {
                return this.binaryStream;
            }

            /**
             * 
             */
            public void getBinaryStream(OutputStream stream, long position) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    "Large objects are expected to retrieve persistent-clean values only"
                );
            }
            
            /**
             * @param position
             * @param capacity
             * @return
             * @throws ServiceException
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBytes(long, int)
             */
            public byte[] getBytes(long position, int capacity) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    "Large objects are expected to retrieve persistent-clean values only"
                );
            }
            
            /**
             * @param position
             * @param capacity
             * @return
             * @throws ServiceException
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacters(long, int)
             */
            public char[] getCharacters(long position, int capacity) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    "Large objects are expected to retrieve persistent-clean values only"
                );
            }
            
            /**
             * This method is internally used to commit changes
             */
            public Reader getCharacterStream() throws ServiceException {
                return this.characterStream;
            }
            
            /**
             * @param writer
             * @param position
             * @throws ServiceException
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream(java.io.Writer, long)
             */
            public void getCharacterStream(Writer writer, long position) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    "Large objects are expected to retrieve persistent-clean values only"
                );
            }
            
            /**
             * @return
             * @throws ServiceException
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#length()
             */
            public long length() throws ServiceException {
                return this.size;
            }
            
            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(java.io.InputStream, long)
             */
            public void setBinaryStream(
                InputStream stream, 
                long size
            ) throws ServiceException {
                objMakeDirty();
                this.size = size;
                this.binaryStream = stream;
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(long)
             */
            public OutputStream setBinaryStream(
                long position
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "OutputStream not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBytes(long, byte[])
             */
            public void setBytes(
                long position, 
                byte[] content
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(java.io.Reader, long)
             */
            public void setCharacterStream(
                Reader stream, 
                long size
            ) throws ServiceException {
                objMakeDirty();
                this.size = size;
                this.characterStream = stream;
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(long)
             */
            public Writer setCharacterStream(
                long position
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "Writer not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacters(long, char[])
             */
            public void setCharacters(
                long position, 
                char[] content
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#truncate(long)
             */
            public void truncate(
                long length
            ) throws ServiceException {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    null,
                    "Partial update not supported"
                );
            }
            
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Interface UnitOfWork_1_3
    //------------------------------------------------------------------------
    
    interface UnitOfWork_1_3
        extends UnitOfWork_1_2, UUIDGenerator
    {

        PersistenceCapable_1_1 add(
            MultithreadedObject_1 object
        ) throws ServiceException;

        void remove(
            MultithreadedObject_1 object
        );

        PersistenceCapable_1_1 get(
            MultithreadedObject_1 object
        );

        PersistenceCapable_1_1 add(
            Path path,
            MultithreadedObject_1 object
        ) throws ServiceException;

        MultithreadedObject_1 get(
            Path path
        );
        
        boolean isInaccessible(
            Path path
        );

        void setInaccessible(
            Path path, boolean to
        );
        
        int countPersistentDeleted(
            Path container
        );
        
        /**
         * To serialize persistence manager calls
         * 
         * @return the lock object to serialize persistence manager calls
         */
        Object getLock();
        
    }

    
    //------------------------------------------------------------------------
    // Class InstanceCallbackDecorator
    //------------------------------------------------------------------------
    
    static class InstanceCallbackDecorator implements InstanceCallbackListener {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        InstanceCallbackDecorator(InstanceCallbackListener delegate) {
            this.delegate = delegate;
        }
        
        /**
         * 
         */
        private final InstanceCallbackListener delegate;

        /**
         * 
         * @param event
         * @throws ServiceException
         */
        void optimisticPostCreate(
            InstanceCallbackEvent event
        ) throws ServiceException {
            this.delegate.postCreate(event);
        }

        /**
         * @param event
         * @throws ServiceException
         * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#postCreate(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
         */
        public void postCreate(
            InstanceCallbackEvent event
        ) throws ServiceException {
            // Do nothing
        }

        /**
         * @param event
         * @throws ServiceException
         * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#postLoad(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
         */
        public void postLoad(InstanceCallbackEvent event)
            throws ServiceException {
            this.delegate.postLoad(event);
        }

        /**
         * @param event
         * @throws ServiceException
         * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#preClear(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
         */
        public void preClear(InstanceCallbackEvent event)
            throws ServiceException {
            this.delegate.preClear(event);
        }

        /**
         * @param event
         * @throws ServiceException
         * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#preDelete(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
         */
        public void preDelete(InstanceCallbackEvent event)
            throws ServiceException {
            this.delegate.preDelete(event);
        }

        /**
         * @param event
         * @throws ServiceException
         * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#preStore(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
         */
        public void preStore(InstanceCallbackEvent event)
            throws ServiceException {
            this.delegate.preStore(event);
        }

    }

    
    //----------------------------------------------------------------
    // Class ObjectCollectionDecorator
    //----------------------------------------------------------------
    
    class ObjectCollectionDecorator extends AbstractCollection<Object_1_0> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        ObjectCollectionDecorator(Collection<Object_1_0> delegate) {
            this.delegate = delegate;
        }
        
        /**
         * 
         */
        private final Collection<Object_1_0> delegate;
        
        /**
         * @return
         * @see java.util.Collection#iterator()
         */
        public Iterator<Object_1_0> iterator() {
            return new ObjectIteratorDecorator<Iterator<Object_1_0>>(this.delegate.iterator());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }

    }
    
    
    //----------------------------------------------------------------
    // Class ObjectListDecorator
    //----------------------------------------------------------------
    
    class ObjectListDecorator extends AbstractSequentialList<Object_1_0> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        ObjectListDecorator(List<Object_1_0> delegate) {
            this.delegate = delegate;
        }
        
        /**
         * 
         */
        private final List<Object_1_0> delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator<Object_1_0> listIterator(int index) {
            return new ObjectListIteratorDecorator(this.delegate.listIterator(index));
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }

    }
    
    
    
    //----------------------------------------------------------------
    // Class ObjectIteratorDecorator
    //----------------------------------------------------------------
    
    class ObjectIteratorDecorator<T extends Iterator<Object_1_0>>
        implements Iterator<Object_1_0> 
    {
       
        /**
         * Constructor 
         *
         * @param delegate
         */
        ObjectIteratorDecorator(T delegate) {
            this.delegate = delegate;
        }
        
        /**
         * 
         */
        protected final T delegate;

        /**
         * The current element.
         */
        protected Object_1_0 current = null;
        
        /**
         * @return
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /**
         * @return
         * @see java.util.Iterator#next()
         */
        public Object_1_0 next() {
            return this.current = this.delegate.next();
        }

        /**
         * 
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            if(this.current == null) throw new IllegalStateException(
                "The iterator has no current element"
            );
            if(!delete(this.current)) {
                this.delegate.remove();
            }
            this.current = null;
        }
        
    }
    
    
    //----------------------------------------------------------------
    // Class ObjectListIteratorDecorator
    //----------------------------------------------------------------
    
    class ObjectListIteratorDecorator
        extends ObjectIteratorDecorator<ListIterator<Object_1_0>>
        implements ListIterator<Object_1_0> 
    {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        ObjectListIteratorDecorator(ListIterator<Object_1_0> delegate) {
            super(delegate);
        }        
                
        /**
         * @param o
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object_1_0 o) {
            throw new UnsupportedOperationException();
        }

        /**
         * @return
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return this.delegate.hasPrevious();
        }

        /**
         * @return
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return this.delegate.nextIndex();
        }

        /**
         * @return
         * @see java.util.ListIterator#previous()
         */
        public Object_1_0 previous() {
            return this.current = this.delegate.previous();
        }

        /**
         * @return 
         * 
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.delegate.previousIndex();
        }

        /**
         * @param o
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(Object_1_0 o) {
            throw new UnsupportedOperationException();
        }

    }

    
    //----------------------------------------------------------------
    // Class ObjectMapDecorator
    //----------------------------------------------------------------
    
    /**
     * 
     */
    class ObjectMapDecorator implements FilterableMap<String, Object_1_0> {
     
        /**
         * Constructor 
         *
         * @param delegate
         */
        ObjectMapDecorator(FilterableMap<String, Object_1_0> delegate) {
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final FilterableMap<String, Object_1_0> delegate;

        /**
         * 
         * @see java.util.Map#clear()
         */
        public void clear() {
            values().clear();
        }

        /**
         * @param key
         * @return
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return this.delegate.containsKey(key);
        }

        /**
         * @param value
         * @return
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            return this.delegate.containsValue(value);
        }

        /**
         * @return
         * @see java.util.Map#entrySet()
         */
        public Set<Entry<String, Object_1_0>> entrySet() {
            return new FilterableMapEntrySetDecorator(this.delegate.entrySet());
        }

        /**
         * @param key
         * @return
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object_1_0 get(Object key) {
            return this.delegate.get(key);
        }

        /**
         * @return
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        /**
         * @return
         * @see java.util.Map#keySet()
         */
        public Set<String> keySet() {
            return this.delegate.keySet();
        }

        /**
         * @param key
         * @param value
         * @return
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object_1_0 put(String key, Object_1_0 value) {
            return this.delegate.put(key, value);
        }

        /**
         * @param t
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map<? extends String, ? extends Object_1_0> t) {
            this.delegate.putAll(t);
        }

        /**
         * @param key
         * @return
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object_1_0 remove(Object key) {
            Object_1_0 value = get(key);
            if(!delete(value)) {
                this.delegate.remove(key);
            }
            return value;
        }

        /**
         * @return
         * @see java.util.Map#size()
         */
        public int size() {
            return this.delegate.size();
        }

        /**
         * @param filter
         * @return
         * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
         */
        public FilterableMap<String, Object_1_0> subMap(Object filter) {
            return new ObjectMapDecorator(this.delegate.subMap(filter));
        }

        /**
         * @return
         * @see java.util.Map#values()
         */
        public Collection<Object_1_0> values() {
            return new ObjectCollectionDecorator(this.delegate.values());
        }

        /**
         * @param criteria
         * @return
         * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
         */
        public List<Object_1_0> values(Object criteria) {
            return new ObjectListDecorator(this.delegate.values(criteria));
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class FilterableMapEntrySetDecorator
    //------------------------------------------------------------------------
    
    class FilterableMapEntrySetDecorator extends AbstractSet<Map.Entry<String, Object_1_0>> {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        FilterableMapEntrySetDecorator(Set<Entry<String, Object_1_0>> delegate) {
            this.delegate = delegate;
        }
        
        private final Set<Map.Entry<String, Object_1_0>> delegate;

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<Entry<String, Object_1_0>> iterator() {
            return new FilterableMapEntrySetIterator(this.delegate.iterator());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }
        
    }


    //------------------------------------------------------------------------
    // Class FilterableMapEntrySetIterator
    //------------------------------------------------------------------------
    
    class FilterableMapEntrySetIterator implements Iterator<Map.Entry<String, Object_1_0>> {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        FilterableMapEntrySetIterator(Iterator<Entry<String, Object_1_0>> delegate) {
            this.delegate = delegate;
        }
        
        private final Iterator<Map.Entry<String, Object_1_0>> delegate;

        private Map.Entry<String, Object_1_0> current = null;
        
        /**
         * @return
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /**
         * @return
         * @see java.util.Iterator#next()
         */
        public Entry<String, Object_1_0> next() {
            return this.current = this.delegate.next();
        }

        /**
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if(this.current == null) throw new IllegalStateException(
                "The iterator has no current element"
            );
            if(!delete(this.current.getValue())) {
                this.delegate.remove();
            }
            this.current = null;
        }
        
    }

}