/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Object Manager
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
 *   * Redistributions in binary form must reproduce the above copyright
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
package org.openmdx.base.accessor.rest;

import static org.openmdx.base.persistence.cci.Queries.ASPECT_QUERY;

import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.jdo.Constants;
import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.collection.Registry;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.collection.WeakRegistry;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.TransactionalSegment;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.InstanceLifecycleListenerRegistry;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.SharedObjects.Aspects;
import org.openmdx.base.persistence.spi.StandardFetchGroup;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.persistence.spi.Transactions;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Selector;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.Factory;

/**
 * A Data Object Manager 1.x implementation
 */
@SuppressWarnings("rawtypes")
public class DataObjectManager_1 implements Marshaller, DataObjectManager_1_0 {

    /**
     * Constructor
     * @param isolateThreads
     *            tells, whether each thread has its own unit of work
     */
    public DataObjectManager_1(
        JDOPersistenceManagerFactory factory,
        boolean proxy,
        PlugIn_1_0[] plugIns,
        Optional<Integer> optimalFetchSize,
        Optional<Integer> cacheThreshold,
        boolean isolateThreads,
        RestConnectionSpec connectionSpec
    ) throws ResourceException {
        this.factory = factory;
        this.proxy = proxy;
        this.interactionSpecs = InteractionSpecs.getRestInteractionSpecs(
            factory.getRetainValues()
        );
        this.principalChain = getPrincipalChain(connectionSpec);
        this.connection = getConnection((ConnectionFactory) factory.getConnectionFactory(), connectionSpec);
        this.connection2 = requiresNonTransactionalDataStoreConnection(factory) ? getConnection((ConnectionFactory) factory.getConnectionFactory2(), connectionSpec) : null;
        this.optimalFetchSize = optimalFetchSize.orElse(OPTIMAL_FETCH_SIZE_DEFAULT).intValue();
        this.cacheThreshold = cacheThreshold.orElse(CACHE_THRESHOLD_DEFAULT).intValue();
        this.workContext = new HashMap<Object, Object>();
        this.plugIns = getRegisteredPlugIns(plugIns);
        setCopyOnAttach(factory.getCopyOnAttach());
        setDetachAllOnCommit(factory.getDetachAllOnCommit());
        setIgnoreCache(factory.getIgnoreCache());
        setDatastoreReadTimeoutMillis(factory.getDatastoreReadTimeoutMillis());
        setDatastoreWriteTimeoutMillis(factory.getDatastoreWriteTimeoutMillis());
        this.connectionSpec = connectionSpec;
        if (isolateThreads) {
            this.threadSafetyRequired = true;
            this.aspectSpecificContexts = new AspectObjectDispatcher(this.threadSafetyRequired);
            this.unitsOfWork = new ThreadLocal<UnitOfWork_1>() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.ThreadLocal#initialValue()
                 */
                @Override
                protected UnitOfWork_1 initialValue() {
                    return new UnitOfWork_1(
                        DataObjectManager_1.this,
                        DataObjectManager_1.this.connection,
                        DataObjectManager_1.this.aspectSpecificContexts,
                        false // there is no need for the unit of work to be thread safe
                    );
                }

            };
            this.unitOfWork = null;
        } else {
            this.threadSafetyRequired = factory.getMultithreaded();
            this.aspectSpecificContexts = new AspectObjectDispatcher(this.threadSafetyRequired);
            this.unitsOfWork = null;
            this.unitOfWork = new UnitOfWork_1(
                this,
                this.connection,
                this.aspectSpecificContexts,
                this.threadSafetyRequired
            );
        }
        this.persistentRegistry = new WeakRegistry<Path, DataObject_1>(this.threadSafetyRequired);
        this.transientRegistry = new WeakRegistry<UUID, DataObject_1>(this.threadSafetyRequired);
    }

    /**
     * Tells whether the data objects their manager must be thread safe
     */
    private final boolean threadSafetyRequired;

    /**
     * The REST Connection Spec
     */
    private final RestConnectionSpec connectionSpec;

    /**
     * Tells, whether the data objects are proxies or not
     */
    private final boolean proxy;

    /**
     * 
     */
    private static final PlugIn_1_0[] NO_PLUG_INS = {};

    /**
     * The persistence manager's factory
     */
    private final JDOPersistenceManagerFactory factory;

    /**
     * 
     */
    private final static int OPTIMAL_FETCH_SIZE_DEFAULT = 64;

    /**
     * 
     */
    private final static int CACHE_THRESHOLD_DEFAULT = 256;

    /**
     * Restricted value for <code>DetachAllOnCommit</code>
     */
    private static final boolean DETACH_ALL_ON_COMMIT = false;

    /**
     * Restricted value for <code>CopyOnAttach</code>
     */
    private static final boolean COPY_ON_ATTACH = true;

    /**
     * Restricted value for <code>IgnoreCache</code>
     */
    private static final boolean IGNORE_CACHE = false;

    /**
     * 
     */
    final InteractionSpecs interactionSpecs;

    /**
     * 
     */
    final List<String> principalChain;

    /**
     * The work context is shared among all persistence manager layers.
     */
    final Map<Object, Object> workContext;

    /**
     * The number of milliseconds allowed for read operations to complete
     */
    private Integer datastoreReadTimeoutMillis;

    /**
     * The number of milliseconds allowed for write operations to complete
     */
    private Integer datastoreWriteTimeoutMillis;
    
    /**
     * The optimal fetch size is usually set in the persistence manager factory EJB configuration
     */
    private final int optimalFetchSize;

    /**
     * The cache threshold value is usually set in the persistence manager factory EJB configuration
     */
    private final int cacheThreshold;

    /**
     * Maps an object id to its data object
     */
    private final Registry<Path, DataObject_1> persistentRegistry;

    /**
     * Maps a transactional object id to its data object
     */
    private final Registry<UUID, DataObject_1> transientRegistry;

    /**
     * The underlying REST connection
     */
    private final JDOConnection jdoConnection = new JDOConnection() {

        public void close() {
            // Nothing to do
        }

        public Object getNativeConnection() {
            return connection2 != null && currentUnitOfWork().getInteraction() == null ? connection2 : connection;
        }
    };

    /**
     * Units of work for a thread isolated data object managers
     */
    private final ThreadLocal<UnitOfWork_1> unitsOfWork;

    /**
     * Unit of work for a single-threaded data object manager
     */
    private final UnitOfWork_1 unitOfWork;

    /**
     * The transaction adapter belonging to the unit of work
     */
    private transient Transaction transaction;

    /**
     * This connection has transaction policy <code>Mandatory</code>
     */
    Connection connection;

    /**
     * This connection has transaction policy <code>RequiresNew</code>
     */
    Connection connection2;

    /**
     * Non-transactional or unique interaction
     */
    private Interaction interaction;

    /**
     * The persistence manager's fetch groups
     */
    private final Map<String, Map<Class<?>, FetchGroup>> fetchGroups = new HashMap<String, Map<Class<?>, FetchGroup>>();

    /**
     * The persistence manager's fetch plan
     */
    private final FetchPlan fetchPlan = StandardFetchPlan.newInstance(null);

    /**
     * The Aspect Specific Context instance
     */
    protected final AspectObjectDispatcher aspectSpecificContexts;

    /**
     * The task identifier may be set by an application
     */
    protected Object taskIdentifier = null;

    /**
     * The transaction time factory may be set by an application
     */
    protected Factory<Date> transactionTime;

    /**
     * Multitenancy support is opaque to the persistence manager
     */
    protected Object tenant = null;

    /**
     * The bulk load flag
     */
    protected boolean bulkLoad = false;

    /**
     * The plug-ins
     */
    private final PlugIn_1_0[] plugIns;

    /**
     * 
     */
    private final InstanceLifecycleListenerRegistry instanceLifecycleListeners = new InstanceLifecycleListenerRegistry();

    /**
     * This object is shared among all <code>PersistenceManager</code>s in the stack.
     */
    private final SharedObjects.Accessor sharedObjects = new SharedObjects.Accessor() {

        @Override
        public Aspects aspectObjects() {
            return DataObjectManager_1.this.aspectSpecificContexts;
        }

        @Override
        public List<String> getPrincipalChain() {
            return DataObjectManager_1.this.principalChain;
        }

        @Override
        public Object getTaskIdentifier() {
            return DataObjectManager_1.this.taskIdentifier;
        }

        @Override
        public void setTaskIdentifier(
            Object taskIdentifier
        ) {
            DataObjectManager_1.this.taskIdentifier = taskIdentifier;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.SharedObjects.Accessor#getTenant()
         */
        @Override
        public Object getTenant() {
            return DataObjectManager_1.this.tenant;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.SharedObjects.Accessor#setTenant(java.lang.Object)
         */
        @Override
        public void setTenant(Object tenant) {
            DataObjectManager_1.this.tenant = tenant;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.SharedObjects.Accessor#setBulkLoad(boolean)
         */
        @Override
        public void setBulkLoad(boolean bulkLoad) {
            DataObjectManager_1.this.bulkLoad = bulkLoad;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.SharedObjects.Accessor#isBulkLoad()
         */
        @Override
        public boolean isBulkLoad() {
            return DataObjectManager_1.this.bulkLoad;
        }

        @Override
        public <T> T getPlugInObject(
            Class<T> type
        ) {
            return DataObjectManager_1.this.getPlugInObject(type);
        }

        @Override
        public String getUnitOfWorkIdentifier() {
            return DataObjectManager_1.this.currentUnitOfWork().getUnitOfWorkIdentifier();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.SharedObjects.Accessor#getTransactionTime()
         */
        @Override
        public Factory<Date> getTransactionTime() {
            return DataObjectManager_1.this.transactionTime;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.SharedObjects.Accessor#setTransactionTime(org.openmdx.kernel.loading.Factory)
         */
        @Override
        public void setTransactionTime(Factory<Date> transactionTime) {
            DataObjectManager_1.this.transactionTime = transactionTime;
        }

    };

    private static boolean requiresNonTransactionalDataStoreConnection(
        JDOPersistenceManagerFactory persistenceManagerFactory
    ) {
        return persistenceManagerFactory.getOptimistic() || (!persistenceManagerFactory.getContainerManaged() && (persistenceManagerFactory
            .getNontransactionalRead() || persistenceManagerFactory.getNontransactionalWrite()));
    }
    
    /**
     * Create a connection
     * 
     * @param connectionFactory the connection factory or {@code null}
     * @param connectionSpec the connection specification
     * @return a new connection or {@code null} if the {@code connectionFactory} is {@code null}
     * 
     * @throws ResourceException in case of failure
     */
    private static Connection getConnection(
        ConnectionFactory connectionFactory,
        RestConnectionSpec connectionSpec
    ) throws ResourceException {
        return connectionFactory == null ? null : connectionFactory.getConnection(connectionSpec);
    }
    
    /**
     * Register the plug-ins
     * 
     * @param plugIns
     *            the (maybe <code>null</code>) plug-in array to be registered
     * 
     * @return a maybe empty but never <code>null</code> plug-in array
     */
    private PlugIn_1_0[] getRegisteredPlugIns(PlugIn_1_0[] plugIns) {
        if (plugIns == null) {
            return NO_PLUG_INS;
        } else {
            for (PlugIn_1_0 plugIn : plugIns) {
                if (plugIn instanceof InstanceLifecycleListener) {
                    addInstanceLifecycleListener(
                        (InstanceLifecycleListener) plugIn,
                        (Class<?>[]) null
                    );
                }
            }
            return plugIns;
        }
    }

    /**
     * Convert the user name to a principal chain
     * 
     * @param connectionSpec the connection specification
     * 
     * @return the corresponding principal chain
     */
    private static List<String> getPrincipalChain(RestConnectionSpec connectionSpec){
        final String userName = connectionSpec.getUserName();;
        return userName == null ? null : PersistenceManagers.toPrincipalChain(userName);
    }
    
    /**
     * Provide the plug-ins
     * 
     * @return the plug-ins
     */
    PlugIn_1_0[] getPlugIns() {
        return this.plugIns;
    }

    /**
     * Retrieve a non-transactional interaction
     * 
     * @return a non-transactional interaction
     * @throws ResourceException
     */
    public Interaction getInteraction()
        throws ResourceException {
        Interaction transactionalInteraction = currentUnitOfWork().getInteraction();
        if (transactionalInteraction == null) {
            if (this.interaction == null) {
                this.interaction = this.newInteraction(
                    this.connection2 == null ? this.connection : this.connection2
                );
            }
            return this.interaction;
        } else {
            return transactionalInteraction;
        }
    }

    /**
     * Create an interaction and sets the tenant
     * 
     * @param connection
     * 
     * @return a new interaction
     * 
     * @throws ResourceException
     */
    protected Interaction newInteraction(
        Connection connection
    )
        throws ResourceException {
        this.connectionSpec.setTenant(UserObjects.getTenant(this));
        this.connectionSpec.setBulkLoad(UserObjects.isBulkLoad(this));
        return connection.createInteraction();
    }

    /**
     * Retrieve the interaction specifications
     * 
     * @return the interaction specifications
     */
    public InteractionSpecs getInteractionSpecs() {
        return this.interactionSpecs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5#isMultithreaded()
     */
    public boolean getMultithreaded() {
        return this.factory.getMultithreaded();
    }

    /**
     * Tells whether the data objects and their manager must be thread safe
     *
     * @return Returns threadSafetyRequired.
     */
    boolean isThreadSafetyRequired() {
        return this.threadSafetyRequired;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    @Override
    public void setMultithreaded(boolean flag) {
        if (flag != getMultithreaded())
            throw new javax.jdo.JDOUnsupportedOptionException(
                "The " + ConfigurableProperty.Multithreaded.qualifiedName() +
                    " property can be set at factory level only"
            );
    }

    /**
     * Retrieve model.
     *
     * @return Returns the model.
     */
    public final Model_1_0 getModel() {
        return Model_1Factory.getModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.StructureFactory_1_0#createStructure(java.lang.String, java.util.List, java.util.List)
     */
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    )
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /**
     * Tells whether the data objects are proxies or not
     * 
     * @return <code>true</code> if the data objects are proxies
     */
    boolean isProxy() {
        return this.proxy;
    }

    /**
     * Tells whether the values of transactional objects shall be retained
     * after the completion of a unit of work or after flushing
     * 
     * @return <code>true</code> if the values of transactional objects shall
     *         be retained
     */
    boolean isRetainValues() {
        return this.factory.getRetainValues();
    }

    /**
     * Fire an instance callback
     *
     * @param type
     * @param lenient
     *            exceptions are logged rather than thrown if
     *            <code>lenient</code> is <code>true</code>
     * @throws ServiceException
     * 
     * @throws ServiceException
     */
    void fireInstanceCallback(
        DataObject_1 source,
        int type,
        boolean lenient
    )
        throws ServiceException {
        if (!source.objIsInaccessible() &&
            (type != InstanceLifecycleEvent.CLEAR || !source.objIsHollow()))
            try {
                source.objGetClass(); // avoid lazy loading
                switch (type) {
                    case InstanceLifecycleEvent.CREATE:
                        this.instanceLifecycleListeners.postCreate(
                            new InstanceLifecycleEvent(source, type)
                        );
                        break;
                    case InstanceLifecycleEvent.LOAD:
                        this.instanceLifecycleListeners.postLoad(
                            new InstanceLifecycleEvent(source, type)
                        );
                        break;
                    case InstanceLifecycleEvent.STORE:
                        this.instanceLifecycleListeners.preStore(
                            new InstanceLifecycleEvent(source, type)
                        );
                        break;
                    case InstanceLifecycleEvent.CLEAR:
                        this.instanceLifecycleListeners.preClear(
                            new InstanceLifecycleEvent(source, type)
                        );
                        break;
                    case InstanceLifecycleEvent.DELETE:
                        this.instanceLifecycleListeners.preDelete(
                            new InstanceLifecycleEvent(source, type)
                        );
                        break;
                }
            } catch (RuntimeException exception) {
                if (lenient) {
                    Throwables.log(exception);
                } else {
                    throw new ServiceException(exception);
                }
            }
    }

    /**
     * Cache an object under its transient id
     * 
     * @param transientObjectId
     * @param object
     * 
     * @return <code>null</code> if the object has been added to the cache
     */
    void putUnlessPresent(
        UUID transientObjectId,
        DataObject_1 object
    ) {
        this.transientRegistry.putUnlessPresent(
            transientObjectId,
            object
        );
    }

    /**
     * Cache an object under its id
     * 
     * @param objectId
     * @param object
     */
    void putUnlessPresent(
        Path objectId,
        DataObject_1 object
    ) {
        this.persistentRegistry.putUnlessPresent(
            objectId,
            object
        );
    }

    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_1
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    @Override
    public void checkConsistency() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @Override
    public Set<?> getManagedObjects() {
        return this.transientRegistry.values();
    }

    @Override
    public void evictAll() {
        for (DataObject_1 pc : this.persistentRegistry.values()) {
            pc.evict();
        }
    }

    @Override
    public UnitOfWork_1 currentUnitOfWork() {
        return this.unitOfWork == null ? this.unitsOfWork.get() : this.unitOfWork;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    @Override
    public Transaction currentTransaction() {
        if (this.transaction == null) {
            this.transaction = Transactions.toTransaction(this.unitOfWork);
        }
        return this.transaction;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    @Override
    public void deletePersistent(
        Object pc
    ) {
        try {
            ((DataObject_1) pc).objRemove();
        } catch (Exception e) {
            throw new JDOUserException(
                "unable to delete object",
                e,
                pc
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @Override
    public void deletePersistentAll(Collection pcs) {
        PersistenceManagers.deletePersistentAll(this, pcs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @Override
    public void evictAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#flush()
     */
    @Override
    public void flush() {
        try {
            currentUnitOfWork().flush(false);
        } catch (ServiceException exception) {
            currentUnitOfWork().setRollbackOnly();
            throw BasicException.initHolder(
                new JDOUserException(
                    "Flushing failed, unit or work has been marked rollback-only",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ROLLBACK
                    )
                )
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    @Override
    public JDOConnection getDataStoreConnection() {
        return this.jdoConnection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    @Override
    public boolean getDetachAllOnCommit() {
        return this.getPersistenceManagerFactory().getDetachAllOnCommit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    @Override
    public FetchPlan getFetchPlan() {
        return this.fetchPlan;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    @Override
    public boolean getIgnoreCache() {
        return IGNORE_CACHE;
    }

    @Override
    public int getOptimalFetchSize() {
        return this.optimalFetchSize;
    }

    @Override
    public int getCacheThreshold() {
        return this.cacheThreshold;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    @Override
    public Object getObjectId(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @Override
    public Class getObjectIdClass(Class cls) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @Override
    public Collection getObjectsById(Collection oids) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    @Override
    public <T> T detachCopy(T arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    @Override
    public <T> Collection<T> detachCopyAll(Collection<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#detachCopyAll(T[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] detachCopyAll(T... arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    @Override
    public void evictAll(
        boolean arg0,
        Class arg1
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    @Override
    public boolean getCopyOnAttach() {
        return COPY_ON_ATTACH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    @Override
    public <T> Extent<T> getExtent(Class<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    @Override
    public <T> Extent<T> getExtent(
        Class<T> arg0,
        boolean arg1
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @Override
    public FetchGroup getFetchGroup(
        Class type,
        String name
    ) {
        FetchGroup fetchGroup;
        synchronized (this.fetchGroups) {
            Map<Class<?>, FetchGroup> fetchGroups = this.fetchGroups.get(name);
            if (fetchGroups == null) {
                this.fetchGroups.put(
                    name,
                    fetchGroups = new IdentityHashMap<Class<?>, FetchGroup>()
                );
            }
            fetchGroup = fetchGroups.get(type);
            if (fetchGroup == null) {
                fetchGroups.put(
                    type,
                    fetchGroup = new StandardFetchGroup(type, name)
                );
            }
        }
        return fetchGroup;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @Override
    public Set<?> getManagedObjects(
        final EnumSet<ObjectState> states
    ) {
        boolean transactional = false, nonTransactional = false;
        for (ObjectState state : states) {
            switch (state) {
                case TRANSIENT:
                case HOLLOW_PERSISTENT_NONTRANSACTIONAL:
                    nonTransactional = true;
                    break;
                case TRANSIENT_CLEAN:
                case TRANSIENT_DIRTY:
                case PERSISTENT_NEW:
                case PERSISTENT_CLEAN:
                case PERSISTENT_DIRTY:
                case PERSISTENT_DELETED:
                case PERSISTENT_NEW_DELETED:
                    transactional = true;
                    break;
                case PERSISTENT_NONTRANSACTIONAL_DIRTY:
                    throw new JDOUnsupportedOptionException(
                        "Unsupported optional state: " + ObjectState.PERSISTENT_NONTRANSACTIONAL_DIRTY
                    );
                case DETACHED_CLEAN:
                case DETACHED_DIRTY:
                    break; // detach not yet supported
            }
        }
        if (transactional || nonTransactional) {
            return Sets.subSet(
                nonTransactional ? this.transientRegistry.values() : currentUnitOfWork().getMembers(),
                new Selector() {

                    public boolean accept(Object candidate) {
                        return states.contains(ReducedJDOHelper.getObjectState(candidate));
                    }

                }
            );
        } else {
            return Collections.EMPTY_SET; // unused states only
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @Override
    public Set getManagedObjects(Class... arg0) {
        throw new UnsupportedOperationException("Unsupported because all objects are instances of the DataObject_1_0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @Override
    public Set getManagedObjects(
        EnumSet<ObjectState> arg0,
        Class... arg1
    ) {
        throw new UnsupportedOperationException("Unsupported because all objects are instances of the DataObject_1_0");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T getObjectById(
        Class<T> arg0,
        Object arg1
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    @Override
    public Object[] getObjectsById(
        boolean arg0,
        Object... arg1
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    @Override
    public Date getServerDate() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    @Override
    public void evictAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    @Override
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    @Override
    public void makeTransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /**
     * Make persistent supports attach copy only
     * 
     * @param pc
     *            the data object to be attached
     * 
     * @exception NullPointerException
     *                if pc is <code>null</code>
     * @exception ClassCastException
     *                if pc is not an instance of <code>DataObject_1_0</code>
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T makePersistent(T pc) {
        DataObject_1_0 source = (DataObject_1_0) pc;
        if (this.getCopyOnAttach() && source.jdoIsDetached()) {
            DataObject_1 target = getObjectById(source.jdoGetObjectId());
            if (target.jdoIsDirty()) {
                throw new JDOUserException(
                    "This object has already been modified in the current transaction",
                    pc
                );
            }
            target.version = source.jdoGetVersion();
            return (T) target;
        } else {
            throw new UnsupportedOperationException("The data object manager supports CopyOnAttach only");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makePersistentAll(T[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] makePersistentAll(T... arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    @Override
    public <T> Collection<T> makePersistentAll(Collection<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @Override
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes
    ) {
        if (classes == null || classes.length == 0) {
            this.instanceLifecycleListeners.addInstanceLifecycleListener(listener);
        } else {
            throw new JDOUnsupportedOptionException("The data object manager expects the classes argument to be null");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    @Override
    public void makeTransientAll(
        boolean arg0,
        Object... arg1
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    @Override
    public <T> T newInstance(Class<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    @Override
    public void retrieveAll(
        boolean arg0,
        Object... arg1
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    @Override
    public void setCopyOnAttach(boolean flag) {
        if (flag != COPY_ON_ATTACH)
            throw new JDOUnsupportedOptionException(
                "The current implementation restricts copyOnAttach to " + COPY_ON_ATTACH
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @Override
    public Collection getObjectsById(
        Collection oids,
        boolean validate
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    @Override
    public Object[] getObjectsById(
        Object[] oids,
        boolean validate
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    @Override
    public void evict(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    @Override
    public void deletePersistentAll(Object... pcs) {
        PersistenceManagers.deletePersistentAll(this, pcs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    @Override
    public void makeNontransactionalAll(Object... pcs) {
        PersistenceManagers.makeNontransactionalAll(
            this,
            Arrays.asList(pcs)
        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    @Override
    public void makeTransientAll(Object... pcs) {
        this.makeTransientAll(false, pcs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    @Override
    public void refreshAll(Object... pcs) {
        PersistenceManagers.refreshAll(this, pcs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    @Override
    public void retrieveAll(Object... pcs) {
        PersistenceManagers.retrieveAll(this, false, pcs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    @Override
    public JDOPersistenceManagerFactory getPersistenceManagerFactory() {
        return this.factory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    @Override
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    @Override
    public Object getTransactionalObjectId(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    @Override
    public Object getUserObject() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    @Override
    public Object getUserObject(Object key) {
        return SharedObjects.isKey(key) ? this.sharedObjects : key instanceof Class<?> ? getPlugInObject((Class<?>) key) : null;
    }

    /**
     * Retrieve the plug-in provided objects
     * 
     * @param type
     *            the plug-in object's type
     * 
     * @return the plug-in provided object
     */
    protected <T> T getPlugInObject(
        Class<T> type
    ) {
        for (PlugIn_1_0 plugIn : DataObjectManager_1.this.plugIns) {
            T userObject = plugIn.getPlugInObject(type);
            if (userObject != null) {
                return userObject;
            }
        }
        return null;
    }

    /**
     * This method is invoked by the data object validator
     * in order to determine whether the given feature is
     * exempt from the standard validation.
     * 
     * @param object
     *            the object to be validated
     * @param feature
     *            the feature's meta-data
     * 
     * @return <code>true</code> if the feature is exempt
     *         from the standard validation.
     * 
     * @throws ServiceException
     */
    protected boolean isExemptFromValidation(
        DataObject_1 object,
        ModelElement_1_0 feature
    )
        throws ServiceException {
        for (PlugIn_1_0 plugIn : DataObjectManager_1.this.plugIns) {
            if (plugIn.isExemptFromValidation(object, feature)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    @Override
    public void makeNontransactional(
        Object pc
    ) {
        try {
            if (pc instanceof DataObject_1) {
                ((DataObject_1) pc).objMakeNontransactional();
            }
        } catch (Exception e) {
            throw new JDOUserException(
                "Unable to make object non-transactional",
                e,
                pc
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @Override
    public void makeNontransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    @Override
    public void makeTransactional(
        Object pc
    ) {
        if (pc instanceof DataObject_1) {
            DataObject_1 dataObject = (DataObject_1) pc;
            if (dataObject.jdoIsPersistent() || dataObject.jdoGetPersistenceManager().getPersistenceManagerFactory().getProperties()
                .contains(Constants.OPTION_TRANSACTIONAL_TRANSIENT))
                try {
                    ((DataObject_1) pc).objMakeTransactional();
                } catch (ServiceException e) {
                    throw new JDOUserException(
                        "Unable to make object transactional",
                        e.getCause(),
                        pc
                    );
                }
            else
                throw new JDOUnsupportedOptionException(
                    Constants.OPTION_TRANSACTIONAL_TRANSIENT + " is not supported"
                );
        } else {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Unable to make object transactional",
                    BasicException.newEmbeddedExceptionStack(
                        null,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", DataObject_1.class.getName()),
                        new BasicException.Parameter("actual", pc == null ? null : pc.getClass().getName())
                    ),
                    pc
                )
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @Override
    public void makeTransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    @Override
    public void makeTransient(Object pc) {
        this.persistentRegistry.values().remove(pc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    @Override
    public void makeTransient(
        Object pc,
        boolean useFetchPlan
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @Override
    public void makeTransientAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    @Override
    public void makeTransientAll(
        Object[] pcs,
        boolean useFetchPlan
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @Override
    public void makeTransientAll(
        Collection pcs,
        boolean useFetchPlan
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @Override
    public Query newNamedQuery(
        Class cls,
        String queryName
    ) {
        if (ASPECT_QUERY.equals(queryName)) {
            return new Aspect_1(this);
        } else
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Unsupported query name",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("actual", queryName),
                        new BasicException.Parameter("supported", ASPECT_QUERY)
                    )
                )
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @Override
    public Object newObjectIdInstance(
        Class pcClass,
        Object key
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    @Override
    public Query newQuery() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    @Override
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    @Override
    public Query newQuery(String query) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @Override
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    @Override
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    @Override
    public Query newQuery(
        String language,
        Object query
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @Override
    public Query newQuery(
        Class cls,
        Collection cln
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @Override
    public Query newQuery(
        Class cls,
        String filter
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    @Override
    public Query newQuery(
        Extent cln,
        String filter
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @Override
    public Query newQuery(
        Class cls,
        Collection cln,
        String filter
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object putUserObject(
        Object key,
        Object val
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    @Override
    public void refresh(
        Object pc
    ) {
        if (pc instanceof DataObject_1) {
            DataObject_1 dataObject = (DataObject_1) pc;
            if (dataObject.jdoIsPersistent() &&
                !dataObject.jdoIsNew() &&
                !dataObject.jdoIsDeleted())
                try {
                    dataObject.refreshUnconditionally();
                } catch (ServiceException exception) {
                    throw new JDOUserException(
                        "Unable to refresh object",
                        exception,
                        pc
                    );
                }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    @Override
    public void refreshAll() {
        UnitOfWork_1 unitOfWork = currentUnitOfWork();
        if (unitOfWork.isActive()) {
            unitOfWork.refreshMembers();
        } else {
            PersistenceManagers.refreshAll(this, this.transientRegistry.values());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @Override
    public void refreshAll(Collection pcs) {
        if (pcs instanceof PersistenceCapableCollection) {
            ((PersistenceCapableCollection) pcs).openmdxjdoRefresh();
        } else {
            PersistenceManagers.refreshAll(this, pcs);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    @Override
    public void refreshAll(JDOException jdoe) {
        PersistenceManagers.refreshAll(this, jdoe);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    @Override
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        this.instanceLifecycleListeners.removeInstanceLifecycleListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    @Override
    public Object removeUserObject(Object key) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    @Override
    public void retrieve(Object pc) {
        retrieve(pc, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    @Override
    public void retrieve(
        Object pc,
        boolean useFetchPlan
    ) {
        if (pc instanceof DataObject_1)
            try {
                ((DataObject_1) pc).objRetrieve(
                    false, // reload
                    useFetchPlan ? this.getFetchPlan() : null,
                    null, // features
                    false, // beforeImage
                    true
                );
            } catch (ServiceException exception) {
                throw new JDOUserException(
                    "Retrieval failure",
                    exception,
                    pc
                );
            }
        else {
            throw new IllegalArgumentException(
                "The first argument should be a DataObject_1 instance: " +
                    (pc == null ? "" : pc.getClass().getName())
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @Override
    public void retrieveAll(Collection pcs) {
        retrieveAll(pcs, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @Override
    public void retrieveAll(
        Collection pcs,
        boolean useFetchPlan
    ) {
        if (pcs instanceof PersistenceCapableCollection) {
            ((PersistenceCapableCollection) pcs).openmdxjdoRetrieve(
                useFetchPlan ? this.getFetchPlan() : null
            );
        } else {
            PersistenceManagers.retrieveAll(this, useFetchPlan, pcs);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    @Override
    public void retrieveAll(
        Object[] pcs,
        boolean useFetchPlan
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    @Override
    public void setDetachAllOnCommit(boolean flag) {
        if (flag != DETACH_ALL_ON_COMMIT)
            throw new JDOUnsupportedOptionException(
                "The current implementation restricts detachAllOnCommit to " + DETACH_ALL_ON_COMMIT
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    @Override
    public void setIgnoreCache(boolean flag) {
        if (flag != IGNORE_CACHE)
            throw new JDOUnsupportedOptionException(
                "The current implementation restricts ignoreCache to " + IGNORE_CACHE
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    @Override
    public void setUserObject(Object o) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    //------------------------------------------------------------------------
    // Implements PersistenceManager_1_0
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#lock(java.security.PrivilegedExceptionAction)
     */
    @Override
    public <T> T lock(PrivilegedExceptionAction<T> action) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getPersistenceManager(javax.resource.cci.InteractionSpec)
     */
    @Override
    public PersistenceManager_1_0 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getFeatureReplacingObjectById(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getFeatureReplacingObjectById(
        UUID objectId,
        String featureName
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#isLoaded(java.util.UUID, java.lang.String)
     */
    @Override
    public boolean isLoaded(
        UUID transientObjectId,
        String fieldName
    ) {
        try {
            return this.getObjectById(transientObjectId).objDefaultFetchGroup().contains(fieldName);
        } catch (ServiceException exception) {
            throw new JDODataStoreException("Unable to determine a field's state", exception.getCause());
        }
    }

    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_0
    //------------------------------------------------------------------------

    /**
     * Close the basic accessor.
     * <p>
     * After the close method completes, all methods on the ObjectFactory_1_0
     * instance except isClosed throw an ILLEGAL_STATE ServiceException.
     */
    @Override
    public void close() {
        if (!isClosed()) {
            this.connection = null;
            this.connection2 = null;
            this.instanceLifecycleListeners.close();
            this.transientRegistry.close();
            this.persistentRegistry.close();
        }
    }

    /**
     * Tests wherther the persistence manager is closed
     * 
     * @return <code>true</code> if the persistence manager is closed
     */
    @Override
    public boolean isClosed() {
        return this.connection == null;
    }

    private final void validateState()
        throws ServiceException {
        if (isClosed())
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Connection is closed"
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    @Override
    public DataObject_1 getObjectById(
        Object oid
    ) {
        return getObjectById(oid, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    @Override
    public DataObject_1 getObjectById(
        Object objectId,
        boolean validate
    ) {
        if (objectId instanceof UUID) {
            return this.transientRegistry.get((UUID) objectId);
        } else if (objectId instanceof Path) {
            Path xri = (Path) objectId;
            try {
                DataObject_1 object = this.persistentRegistry.get(xri);
                if (object == null) {
                    Path id = xri;
                    final UUID transientObjectId;
                    if (xri.getLastSegment() instanceof TransactionalSegment) {
                        transientObjectId = ((TransactionalSegment) xri.getLastSegment()).getTransactionalObjectId();
                        object = this.transientRegistry.get(transientObjectId);
                    } else {
                        transientObjectId = null;
                    }
                    if (object == null) {
                        object = new DataObject_1(
                            this,
                            xri,
                            transientObjectId,
                            null, // objectClass
                            false // untouchable
                        );
                    }
                    if (validate) {
                        for (int i = id.size(); i > 6;) {
                            i -= 2;
                            id = id.getPrefix(i);
                            DataObject_1_0 ancestor = this.persistentRegistry.get(id);
                            if (ancestor != null &&
                                ancestor.jdoIsNew()) {
                                throw BasicException.initHolder(
                                    new JDOObjectNotFoundException(
                                        "There exists no object with the given id while one of its ancestors is new",
                                        BasicException.newEmbeddedExceptionStack(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_FOUND,
                                            new BasicException.Parameter(BasicException.Parameter.XRI, xri),
                                            new BasicException.Parameter("anchestor", id)
                                        )
                                    )
                                );
                            }
                        }
                        final DataObject_1 newObject = object.objRetrieve(false, this.getFetchPlan(), null, false, true);
                        if (object == newObject) {
                            object = this.persistentRegistry.putUnlessPresent(xri, object);
                        } else {
                            this.persistentRegistry.put(xri, object = newObject);
                        }
                    } else {
                        object = this.persistentRegistry.putUnlessPresent(xri, object);
                    }
                } else if (validate) {
                    object.objRetrieve(false, this.getFetchPlan(), null, false, true);
                }
                return object;
            } catch (ServiceException exception) {
                throw exception.getExceptionCode() == BasicException.Code.NOT_FOUND ? new JDOObjectNotFoundException(
                    "Requested object not found in the data store",
                    exception
                )
                    : new JDOUserException(
                        "Unable to retrieve the requested object from the data store",
                        exception
                    );
            } catch (JDOException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new JDOUserException(
                    "Unable to get object",
                    exception
                );
            }
        } else {
            throw BasicException.initHolder(
                objectId == null ? new JDOFatalUserException(
                    "Null Object Id",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", Path.class.getName())
                    )
                )
                    : new JDOFatalUserException(
                        "Unsupported Object Id Class",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            new BasicException.Parameter("expected", Path.class.getName()),
                            new BasicException.Parameter("actual", objectId.getClass().getName())
                        )
                    )
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.DataObjectManager_1_0#putInstance(java.lang.String)
     */
    @Override
    public DataObject_1_0 newInstance(
        String objectClass,
        UUID transientObjectId
    )
        throws ServiceException {
        validateState();
        return new DataObject_1(
            this,
            null,
            transientObjectId,
            objectClass,
            false // untouchable
        );
    }

    //------------------------------------------------------------------------
    // Implements ReplyListener
    //------------------------------------------------------------------------

    DataObject_1_0 receive(ObjectRecord record) {
        try {
            DataObject_1 dataObject = getObjectById(
                record.getResourceIdentifier(),
                false
            );
            dataObject.postLoad(record);
            return dataObject;
        } catch (ServiceException exception) {
            //
            // TODO add exception to resource warnings
            //
            Throwables.log(exception);
            return null;
        } catch (RuntimeException exception) {
            //
            // TODO add exception to resource warnings
            //
            Throwables.log(exception);
            return null;
        }
    }

    //------------------------------------------------------------------------
    // Implements Manager_1_0
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#invalidate(org.openmdx.compatibility.base.naming.Path,
     * boolean)
     */
    void invalidate(
        Path accessPath,
        boolean makeNonTransactional
    )
        throws ServiceException {
        if (isClosed())
            return;
        DataObject_1 parent = (DataObject_1) marshal(accessPath.getPrefix(accessPath.size() - 2));
        if (parent != null) {
            parent.setExistence(
                accessPath,
                false
            );
        }
        for (Iterator<DataObject_1> i = this.persistentRegistry.values().iterator(); i.hasNext();) {
            DataObject_1 pc = i.next();
            if (pc.jdoIsPersistent() && pc.jdoGetObjectId().startsWith(accessPath)) {
                pc.invalidate(makeNonTransactional);
                i.remove();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#move(org.openmdx.compatibility.base.naming.Path,
     * org.openmdx.compatibility.base.naming.Path)
     */
    void move(
        UUID transientObjectId,
        Path objectId
    ) {
        if (!isClosed()) {
            DataObject_1 object = this.transientRegistry.get(transientObjectId);
            if (object != null) {
                this.persistentRegistry.putUnlessPresent(objectId, object);
            }
        }
    }

    boolean containsKey(
        Path path
    ) {
        return this.persistentRegistry.get(path) != null;
    }

    //------------------------------------------------------------------------
    // Extends CachingMarshaller
    //------------------------------------------------------------------------

    /**
     * Marshals an object
     *
     * @param source
     *            The object to be marshalled
     * 
     * @return The marshalled object
     * 
     * @exception ServiceException
     *                DATA_CONVERSION: Object can't be marshalled
     */
    @Override
    public Object marshal(
        Object source
    )
        throws ServiceException {
        validateState();
        return source instanceof Path ? getObjectById(source, false) : source;
    }

    /**
     * Unmarshals an object
     *
     * @param source
     *            The marshalled object
     * 
     * @return The unmarshalled object
     * 
     * @exception ServiceException
     *                Object can't be unmarshalled
     */
    @Override
    public Object unmarshal(
        Object source
    )
        throws ServiceException {
        validateState();
        if (source instanceof DataObject_1_0) {
            DataObject_1_0 object = (DataObject_1_0) source;
            // Unmarshal of aspects is aspect-type specific. Only handle when
            // object is in state new.
            if (object.jdoIsNew()) {
                if (getModel().isInstanceof(object, "org:openmdx:base:Aspect")) {
                    DataObject_1_0 core = (DataObject_1_0) object.objGetValue(SystemAttributes.CORE);
                    if (core != null) {
                        return core.jdoGetObjectId();
                    }
                }
            }
            return object.jdoGetObjectId();
        } else {
            return source;
        }
    }

    /**
     * Retrieve the object's last segment XRI segment
     * 
     * @param the
     *            persistence capable object
     * 
     * @return the last segment of the actual or future XRI; or <code>null</code> if the object is not contained yet
     */
    public String getLastXRISegment(Object pc) {
        return pc instanceof DataObject_1 ? ((DataObject_1) pc).getQualifier() : null;
    }

    /**
     * Retrieve the transient id of the object's container
     * 
     * @param pc
     *            the persistent capable object
     * 
     * @return the transient id of the object's container
     */
    public TransientContainerId getContainerId(Object pc) {
        if (pc instanceof DataObject_1) {
            Container_1 container = ((DataObject_1) pc).getContainer(false);
            if (container != null) {
                return container.jdoGetTransactionalObjectId();
            }
        }
        return null;
    }

    public static LocalTransaction getLocalTransaction(
        PersistenceManager persistenceManager
    )
        throws ResourceException {
        return persistenceManager instanceof DataObjectManager_1 ? ((DataObjectManager_1) persistenceManager).connection
            .getLocalTransaction() : null;
    }

    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDatastoreReadTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreReadTimeoutMillis(Integer interval) {
        this.datastoreReadTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDatastoreReadTimeoutMillis()
     */
    @Override
    public Integer getDatastoreReadTimeoutMillis() {
        return this.datastoreReadTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDatastoreWriteTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreWriteTimeoutMillis(Integer interval) {
        this.datastoreWriteTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDatastoreWriteTimeoutMillis()
     */
    @Override
    public Integer getDatastoreWriteTimeoutMillis() {
        return this.datastoreWriteTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(
        String propertyName,
        Object value
    ) {
        PersistenceManagers.setProperty(this, propertyName, value);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getProperties()
     */
    @Override
    public Map<String, Object> getProperties() {
        return PersistenceManagers.getProperties(this);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSupportedProperties()
     */
    @Override
    public Set<String> getSupportedProperties() {
        return PersistenceManagers.getSupportedProperties(); 
    }

    //------------------------------------------------------------------------
    // Class AspectObjectDispatcher
    //------------------------------------------------------------------------

    /**
     * Aspect Object Dispatcher
     */
    class AspectObjectDispatcher implements SharedObjects.Aspects {

        AspectObjectDispatcher(
            boolean threadSafetyRequired
        ) {
            this.sharedContexts = Maps.newMap(threadSafetyRequired);
        }

        private final Map<UUID, Map<Class<?>, Object>> sharedContexts;

        /**
         * Retrieve the object's state
         * 
         * @param transactionalObjectId
         * @param optional
         * 
         * @return the object's state
         */
        private TransactionalState_1 getState(
            Object transactionalObjectId,
            boolean optional
        ) {
            DataObject_1 dataObject = getObjectById(transactionalObjectId, false);
            if (dataObject == null)
                throw BasicException.initHolder(
                    new JDOFatalInternalException(
                        "Aspect object access failure: Data object not found",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            new BasicException.Parameter("objectId", transactionalObjectId)
                        )
                    )
                );
            try {
                return dataObject.getState(optional);
            } catch (JDOException exception) {
                throw BasicException.initHolder(
                    new JDOFatalInternalException(
                        "Aspect object access failure: Transactional state inaccessable",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE,
                            new BasicException.Parameter("objectId", transactionalObjectId)
                        )
                    )
                );
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.AspectSpecificContexts#get(java.lang.Object, java.lang.Class)
         */
        @Override
        public Object get(
            UUID transactionalObjectId,
            Class<?> aspect
        ) {
            UnitOfWork_1 unitOfWork = DataObjectManager_1.this.currentUnitOfWork();
            if (unitOfWork.isActive()) {
                TransactionalState_1 state = this.getState(transactionalObjectId, true);
                return state == null ? null : state.getContext(aspect);
            } else {
                Map<Class<?>, Object> contexts = this.sharedContexts.get(transactionalObjectId);
                return contexts == null ? null : contexts.get(aspect);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.AspectSpecificContexts#put(java.lang.Object, java.lang.Class, java.lang.Object)
         */
        @Override
        public void put(
            UUID transactionalObjectId,
            Class<?> aspect,
            Object context
        ) {
            UnitOfWork_1 unitOfWork = DataObjectManager_1.this.currentUnitOfWork();
            if (unitOfWork.isActive()) {
                this.getState(transactionalObjectId, false).setContext(aspect, context);
            } else {
                Map<Class<?>, Object> contexts = this.sharedContexts.get(transactionalObjectId);
                if (contexts == null) {
                    contexts = Maps.putUnlessPresent(
                        this.sharedContexts,
                        transactionalObjectId,
                        new IdentityHashMap<Class<?>, Object>()
                    );
                }
                contexts.put(
                    aspect,
                    context
                );
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.AspectSpecificContexts#remove(java.lang.Object, java.lang.Class)
         */
        @Override
        public void remove(
            UUID objectId,
            Class<?> aspect
        ) {
            UnitOfWork_1 unitOfWork = DataObjectManager_1.this.currentUnitOfWork();
            if (unitOfWork.isActive()) {
                TransactionalState_1 state = getState(objectId, true);
                if (state != null) {
                    state.removeContext(aspect);
                }
            } else {
                Map<Class<?>, Object> contexts = this.sharedContexts.get(objectId);
                if (contexts != null) {
                    contexts.remove(aspect);
                }
            }
        }

        void clear() {
            this.sharedContexts.clear();
        }

    }

}
