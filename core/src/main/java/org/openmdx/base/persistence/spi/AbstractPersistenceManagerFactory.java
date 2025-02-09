/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Persistence Manager Factory
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.persistence.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jdo.Constants;
import javax.jdo.FetchGroup;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.metadata.JDOMetadata;
import javax.jdo.metadata.TypeMetadata;

import org.openmdx.base.Version;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.collection.MapBackedSet;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.NonConfigurableProperty;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.openmdx.kernel.janitor.Finalizer;
import org.openmdx.kernel.jdo.JDOPersistenceManager;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.resource.spi.CloseCallback;

/**
 * Abstract Data Access Service Factory
 *
 * @since openMDX 2.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractPersistenceManagerFactory<P extends PersistenceManager_1_0>
    implements JDOPersistenceManagerFactory, CloseCallback {

    /**
     * Constructor
     *
     * @param overrides
     *            the configuration properties
     * @param configuration
     *            the configuration properties
     * @param defaults
     *            for missing configuration and override properties
     */
    protected AbstractPersistenceManagerFactory(
        Map<?, ?> overrides,
        Map<?, ?> configuration,
        Map<?, ?> defaults
    ) {
        this.configurationProvider = Configurations.getPersistenceManagerFactoryConfigurationProvider(
            overrides,
            configuration,
            defaults
        );
        this.configuration = configurationProvider.getSelection(
            qualifiedName -> ConfigurableProperty.fromQualifiedName(qualifiedName).isPresent()
        );
        setOptimistic(getFlag(ConfigurableProperty.Optimistic));
        setRetainValues(getFlag(ConfigurableProperty.RetainValues));
        setRestoreValues(getFlag(ConfigurableProperty.RestoreValues));
        setIgnoreCache(getFlag(ConfigurableProperty.IgnoreCache));
        setNontransactionalRead(getFlag(ConfigurableProperty.NontransactionalRead));
        setNontransactionalWrite(getFlag(ConfigurableProperty.NontransactionalWrite));
        setMultithreaded(getFlag(ConfigurableProperty.Multithreaded));
        setCopyOnAttach(getFlag(ConfigurableProperty.CopyOnAttach));
        setContainerManaged(getFlag(ConfigurableProperty.ContainerManaged));
        setIsolateThreads(getFlag(ConfigurableProperty.IsolateThreads));
        setFinalizePersistenceManagers(getFlag(ConfigurableProperty.FinalizePersistenceManagers));
        getString(ConfigurableProperty.ConnectionUserName).ifPresent(this::setConnectionUserName);
        getString(ConfigurableProperty.ConnectionPassword).ifPresent(this::setConnectionPassword);
        getString(ConfigurableProperty.ConnectionURL).ifPresent(this::setConnectionURL);
        getString(ConfigurableProperty.ConnectionFactoryName).ifPresent(this::setConnectionFactoryName);
        getString(ConfigurableProperty.ConnectionFactory2Name).ifPresent(this::setConnectionFactory2Name);
        getString(ConfigurableProperty.Mapping).ifPresent(this::setMapping);
        getObject(ConfigurableProperty.ConnectionFactory).ifPresent(this::setConnectionFactory);
        getObject(ConfigurableProperty.ConnectionFactory2).ifPresent(this::setConnectionFactory2);
        getString(ConfigurableProperty.TransactionType).ifPresent(this::setTransactionType);
        getString(ConfigurableProperty.TransactionIsolationLevel).ifPresent(this::setTransactionIsolationLevel);
        getString(ConfigurableProperty.ConnectionDriverName).ifPresent(this::setConnectionDriverName);
    }

    /**
     * The configuration provider in behalf of the subclasses
     */
    private final ConfigurationProvider configurationProvider;

    /**
     * The configuration for the standard properties
     */
    private final Configuration configuration;

    /**
     * A {@code PersistenceManagerFactory}'s configuration is its
     * {@code PersistenceManager}s' default configuration.
     */
    private Map<ConfigurableProperty, Object> configurableProperties = new HashMap<>();

    /**
     * The {@code Finalizer} if {@code ConfigurableProerty.FinalizePersistenceManagers} is {@code TRUE}
     */
    private Finalizer finalizer;

    /**
     * {@code PersistentManager} book keeping.
     */
    private Set<PersistenceManager> persistenceManagers = MapBackedSet.decorate(
        new WeakHashMap<PersistenceManager, Object>()
    );

    /**
     * The persistence manager factory scoped fetch groups
     */
    private final Set<FetchGroup> fetchGroups = new HashSet<>();

    /**
     * The listeners to be propagated to the children
     */
    private final InstanceLifecycleListenerRegistry instanceLifecycleListenerRegistry = new InstanceLifecycleListenerRegistry();

    /**
     * The lazily retrieved connection factory finder
     */
    private transient Factory<?> connectionFactoryFinder;

    /**
     * The default configuration
     */
    private static final Map<String, Object> DEFAULT_CONFIGURATION = new HashMap<>();

    /**
     * The {@code PersistenceManagerFactory}'s properties.
     */
    private final static Properties NON_CONFIGURABLE_PROPERTIES = new Properties();

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 4344984435301460319L;

    /**
     * Return {@code true} if the property's value is
     * {@code "true"} ignoring case.
     * 
     * @param option
     *            the option
     * @param defaultValue
     *            tells whether the flag is by default on or off
     * 
     * @return {@code true} if the flag is on (explicitly or by default)
     */
    private boolean getFlag(
        ConfigurableProperty option,
        boolean defaultValue
    ) {
        return configuration.getOptionalValue(
            option.qualifiedName(),
            Boolean.class
        ).orElse(
            defaultValue
        );
    }

    /**
     * Return {@code true} if the property's value is
     * {@code "true"} ignoring case.
     * 
     * @param option
     *            the option
     * 
     * @return {@code true} if the flag is on
     */
    private boolean getFlag(
        ConfigurableProperty option
    ) {
        return getFlag(option, false);
    }

    private Optional<String> getString(
        ConfigurableProperty option
    ) {
        return configuration.getOptionalValue(option.qualifiedName(), String.class);
    }

    private Optional<?> getObject(
        ConfigurableProperty option
    ) {
        return configuration.getOptionalValue(option.qualifiedName(), Object.class);
    }

    /**
     * Freeze the {@code PersistenceManagerFactory}'s configurable properties.
     * 
     * @exception JDOFatalUserException
     *                if the {@code PersistenceManagerFactory}
     *                is closed
     */
    protected void freeze() {
        if (this.isClosed())
            throw new JDOFatalUserException(
                "Persistence Manager Factory closed"
            );
        if (!this.isFrozen()) {
            this.configurableProperties = Collections.unmodifiableMap(this.configurableProperties);
            if(this.getFinalizePersistenceManagers()) {
                this.finalizer = Finalizer.getInstance();
            }
        }
    }

    /**
     * Set a configurable property
     * 
     * @param name
     *            the property
     * @param value
     *            the value of the property
     * 
     * @exception JDOFatalUserException
     *                If the factory is no longer configurable
     */
    protected void setProperty(
        ConfigurableProperty name,
        Object value
    ) {
        if (this.isFrozen()) {
            throw new JDOFatalUserException("The persistence manager factory is no longer configurable");
        } else {
            this.configurableProperties.put(name, value);
        }
    }

    protected Configuration getConfiguration(String section) {
        return section == null ? Configurations.getBeanConfiguration(Collections.emptyMap())
            : this.configurationProvider.getSection(section);
    }

    /**
     * Get a specific configuration
     * 
     * @param defaults
     *            Taken into account for missing entries
     * @param section
     *            The qualified name of the section to be parsed, where
     *            {@code ""} represents root section and {code null} is
     *            forbidden
     * @param overrides
     *            overrides the given entries or amends them by new ones
     * 
     * @return the requested configuration which may be empty but never
     *         is {@code null}
     * 
     * @throws NullPointerException
     *             if {@code section} is {@code null}
     */
    protected Configuration getConfiguration(
        Map<String, ?> defaults,
        String section,
        Map<String, ?> overrides
    ) {
        return this.configurationProvider.getSection(defaults, section, overrides);
    }

    //------------------------------------------------------------------------
    // Implements CloseCallback
    //------------------------------------------------------------------------

    @Override
    public synchronized void postClose(Object closed) {
        if (!isClosed()) {
            this.persistenceManagers.remove(closed);
        }
    }

    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory
    //------------------------------------------------------------------------

    @Override
    public synchronized void close() {
        if (!this.isClosed()) {
            List<JDOUserException> exceptions = new ArrayList<>();
            for (PersistenceManager p : this.persistenceManagers) {
                if (PersistenceHelper.currentUnitOfWork(p).isActive()) {
                    exceptions.add(
                        new JDOUserException("PersistenceManager has active transaction", p)
                    );
                }
            }
            if (exceptions.isEmpty()) {
                Set<PersistenceManager> persistenceManagers = this.persistenceManagers;
                this.persistenceManagers = null;
                for (PersistenceManager p : persistenceManagers) {
                    p.close();
                }
            } else {
                throw new JDOUserException(
                    "PersistenceManager with active transaction prevents close",
                    exceptions.toArray(
                        new JDOUserException[exceptions.size()]
                    )
                );
            }
        }
    }

    @Override
    public boolean isClosed() {
        return this.persistenceManagers == null;
    }

    /**
     * Test whether the configurable properties are frozen or not
     * 
     * @return {@code true} if the configurable properties are frozen
     */
    protected boolean isFrozen() {
        return this.configurableProperties.getClass() != HashMap.class;
    }

    /**
     * The sub-class specific factory method
     *
     * @return a new {@code PersistenceManager}
     */
    protected abstract P newPersistenceManager(
        String userid,
        String password
    );

    /**
     * The sub-class specific factory method
     * 
     * @return a new {@code PersistenceManager}
     */
    protected abstract P newPersistenceManager();

    @Override
    public final P getPersistenceManager() {
        this.freeze();
        P persistenceManager = this.newPersistenceManager();
        this.initialize(persistenceManager);
        return persistenceManager;
    }

    @Override
    public final P getPersistenceManager(
        String userid,
        String password
    ) {
        this.freeze();
        P persistenceManager = this.newPersistenceManager(
            userid,
            password
        );
        this.initialize(persistenceManager);
        return persistenceManager;
    }

    /**
     * Initialize a newly created persistence manager
     */
    protected void initialize(
        PersistenceManager_1_0 persistenceManager
    ) {
        this.instanceLifecycleListenerRegistry.propagateTo(persistenceManager);
        if(this.getFinalizePersistenceManagers()){
            persistenceManager.registerForFinalization(this.finalizer);
        }
    }

    @Override
    public boolean getDetachAllOnCommit() {
        return Boolean.TRUE.equals(
            this.configurableProperties.get(ConfigurableProperty.DetachAllOnCommit)
        );
    }

    @Override
    public void setDetachAllOnCommit(boolean flag) {
        this.setProperty(
            ConfigurableProperty.DetachAllOnCommit,
            flag
        );
    }

    @Override
    public void setConnectionUserName(String userName) {
        this.setProperty(
            ConfigurableProperty.ConnectionUserName,
            userName
        );
    }

    @Override
    public String getConnectionUserName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionUserName);
    }

    @Override
    public void setConnectionPassword(String password) {
        this.setProperty(ConfigurableProperty.ConnectionPassword, password);
    }

    protected String getConnectionPassword() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionPassword);
    }

    @Override
    public void setConnectionURL(String URL) {
        this.setProperty(ConfigurableProperty.ConnectionURL, URL);
    }

    @Override
    public String getConnectionURL() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionURL);
    }

    @Override
    public void setConnectionDriverName(String driverName) {
        this.setProperty(ConfigurableProperty.ConnectionDriverName, driverName);
    }

    @Override
    public String getConnectionDriverName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionDriverName);
    }

    @Override
    public void setConnectionFactoryName(String connectionFactoryName) {
        this.setProperty(ConfigurableProperty.ConnectionFactoryName, connectionFactoryName);
    }

    @Override
    public String getConnectionFactoryName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionFactoryName);
    }

    @Override
    public void setConnectionFactory(Object connectionFactory) {
        this.setProperty(ConfigurableProperty.ConnectionFactory, connectionFactory);
    }

    @Override
    public Object getConnectionFactory() {
        return this.configurableProperties.get(ConfigurableProperty.ConnectionFactory);
    }

    @Override
    public void setConnectionFactory2Name(String connectionFactoryName) {
        this.setProperty(ConfigurableProperty.ConnectionFactory2Name, connectionFactoryName);
    }

    @Override
    public String getConnectionFactory2Name() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionFactory2Name);
    }

    @Override
    public void setConnectionFactory2(Object connectionFactory) {
        this.setProperty(ConfigurableProperty.ConnectionFactory2, connectionFactory);
    }

    @Override
    public Object getConnectionFactory2() {
        return this.configurableProperties.get(ConfigurableProperty.ConnectionFactory2);
    }

    @Override
    public void setMultithreaded(boolean flag) {
        this.setProperty(ConfigurableProperty.Multithreaded, flag);
    }

    @Override
    public boolean getMultithreaded() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.Multithreaded));
    }

    @Override
    public void setMapping(String mapping) {
        this.setProperty(ConfigurableProperty.Mapping, mapping);
    }

    @Override
    public String getMapping() {
        return (String) this.configurableProperties.get(ConfigurableProperty.Mapping);
    }

    @Override
    public void addFetchGroups(FetchGroup... fetchGroups) {
        synchronized (this.fetchGroups) {
            for (FetchGroup fetchGroup : fetchGroups) {
                fetchGroup.setUnmodifiable();
                this.fetchGroups.add(fetchGroup);
            }
        }
    }

    @Override
    public FetchGroup getFetchGroup(
        Class type,
        String name
    ) {
        return new StandardFetchGroup(type, name);
    }

    @Override
    public Set getFetchGroups() {
        Set<FetchGroup> fetchGroups = new HashSet<>();
        synchronized (this.fetchGroups) {
            for (FetchGroup fetchGroup : this.fetchGroups) {
                fetchGroups.add(new StandardFetchGroup(fetchGroup));
            }
        }
        return fetchGroups;
    }

    @Override
    public JDOPersistenceManager getPersistenceManagerProxy() {
        throw new UnsupportedOperationException("Persistence Manager Proxy instances are not supported");
    }

    @Override
    public boolean getReadOnly() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.ReadOnly));
    }

    @Override
    public String getTransactionIsolationLevel() {
        return (String) this.configurableProperties.get(ConfigurableProperty.TransactionIsolationLevel);
    }

    @Override
    public void removeAllFetchGroups() {
        synchronized (this.fetchGroups) {
            this.fetchGroups.clear();
        }
    }

    @Override
    public void removeFetchGroups(FetchGroup... fetchGroups) {
        synchronized (this.fetchGroups) {
            for (FetchGroup fetchGroup : fetchGroups) {
                this.fetchGroups.remove(fetchGroup);
            }
        }
    }

    @Override
    public void setReadOnly(boolean arg0) {
        this.setProperty(ConfigurableProperty.ReadOnly, arg0);
    }

    @Override
    public void setTransactionIsolationLevel(String transactionIsolationLevel) {
        this.setProperty(ConfigurableProperty.TransactionIsolationLevel, transactionIsolationLevel);
    }

    @Override
    public void setOptimistic(boolean flag) {
        this.setProperty(ConfigurableProperty.Optimistic, flag);
    }

    @Override
    public boolean getOptimistic() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.Optimistic));
    }

    @Override
    public void setRetainValues(boolean flag) {
        this.setProperty(ConfigurableProperty.RetainValues, flag);
    }

    @Override
    public boolean getRetainValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.RetainValues));
    }

    @Override
    public void setRestoreValues(boolean restoreValues) {
        this.setProperty(ConfigurableProperty.RestoreValues, restoreValues);
    }

    @Override
    public boolean getRestoreValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.RestoreValues));
    }

    @Override
    public void setNontransactionalRead(boolean flag) {
        this.setProperty(ConfigurableProperty.NontransactionalRead, flag);
    }

    @Override
    public boolean getNontransactionalRead() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.NontransactionalRead));
    }

    @Override
    public void setNontransactionalWrite(boolean flag) {
        this.setProperty(ConfigurableProperty.NontransactionalWrite, flag);
    }

    @Override
    public boolean getNontransactionalWrite() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.NontransactionalWrite));
    }

    @Override
    public void setIgnoreCache(boolean flag) {
        this.setProperty(ConfigurableProperty.IgnoreCache, flag);
    }

    @Override
    public boolean getIgnoreCache() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.IgnoreCache));
    }

    /**
     * Define whether the transaction is container managed
     * 
     * @param flag
     *            the container managed transaction flag
     */
    public void setContainerManaged(boolean flag) {
        this.setProperty(ConfigurableProperty.ContainerManaged, flag);
    }

    /**
     * Tell whether the transaction is container managed
     * 
     * @return {@code true} if the transaction is container managed
     */
    public boolean getContainerManaged() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.ContainerManaged));
    }

    /**
     * Defines, whether each thread sees his own unit of work
     * 
     * @param flag
     *            the isolate threads flag
     */
    public void setIsolateThreads(boolean flag) {
        this.setProperty(ConfigurableProperty.IsolateThreads, flag);
    }

    /**
     * Tells, whether each thread sees his own unit of work
     */
    public boolean getIsolateThreads() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.IsolateThreads));
    }

    /**
     * Defines, whether open persistence managers shall be closed during garbage collection
     *
     * @param flag
     *            the finalize persistence managers flag
     */
    public void setFinalizePersistenceManagers(boolean flag) {
        this.setProperty(ConfigurableProperty.FinalizePersistenceManagers, flag);
    }

    /**
     * Tells, whether open persistence managers shall be closed during garbage collection
     */
    public boolean getFinalizePersistenceManagers() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.FinalizePersistenceManagers));
    }

    @Override
    public Properties getProperties() {
        return new Properties(AbstractPersistenceManagerFactory.NON_CONFIGURABLE_PROPERTIES);
    }

    @Override
    public Collection<String> supportedOptions() {
        return PersistenceManagers.getSupportedProperties();
    }

    @Override
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        if (this.isFrozen()) {
            throw new JDOFatalUserException("The persistence manager factory is no longer configurable");
        } else {
            this.instanceLifecycleListenerRegistry.addInstanceLifecycleListener(listener, classes);
        }
    }

    @Override
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        if (this.isFrozen()) {
            throw new JDOFatalUserException("The persistence manager factory is no longer configurable");
        } else {
            this.instanceLifecycleListenerRegistry.removeInstanceLifecycleListener(listener);
        }
    }

    @Override
    public boolean getCopyOnAttach() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.CopyOnAttach));
    }

    @Override
    public void setCopyOnAttach(boolean flag) {
        this.configurableProperties.put(
            ConfigurableProperty.CopyOnAttach,
                flag
        );
    }

    @Override
    public String getName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.Name);
    }

    @Override
    public void setName(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.Name,
            name
        );
    }

    @Override
    public String getPersistenceUnitName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.PersistenceUnitName);
    }

    @Override
    public void setPersistenceUnitName(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.PersistenceUnitName,
            name
        );
    }

    @Override
    public String getServerTimeZoneID() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ServerTimeZoneID);
    }

    @Override
    public void setServerTimeZoneID(String timezoneid) {
        this.configurableProperties.put(
            ConfigurableProperty.ServerTimeZoneID,
            timezoneid
        );
    }

    @Override
    public String getTransactionType() {
        return (String) this.configurableProperties.get(ConfigurableProperty.TransactionType);
    }

    @Override
    public void setTransactionType(
        String name
    ) {
        this.configurableProperties.put(
            ConfigurableProperty.TransactionType,
            name
        );
    }

    /**
     * Acquire the connection factory by its JNDI name
     * 
     * @param connectionFactoryName
     *            the connection factory's JNDI name
     * 
     * @return the connection factory
     * 
     * @throws JDOFatalDataStoreException
     *             in case of lookup failure
     * @throws ServiceException
     *             in case of factory finder invocation failure
     */
    protected Object getConnectionFactoryByName(
        String connectionFactoryName
    )
        throws ServiceException {
        try {
            if (connectionFactoryFinder == null) {
                connectionFactoryFinder = Classes.newApplicationInstance(
                    Factory.class,
                    "org.openmdx.application.naming.JNDIAccessor",
                    connectionFactoryName,
                    Object.class
                );
            }
            return connectionFactoryFinder.instantiate();
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }

    @Override
    public void setDatastoreReadTimeoutMillis(Integer interval) {
        this.configurableProperties.put(
            ConfigurableProperty.DatastoreReadTimeoutMillis,
            interval
        );
    }

    @Override
    public Integer getDatastoreReadTimeoutMillis() {
        return (Integer) this.configurableProperties.get(ConfigurableProperty.DatastoreReadTimeoutMillis);
    }

    @Override
    public void setDatastoreWriteTimeoutMillis(Integer interval) {
        this.configurableProperties.put(
            ConfigurableProperty.DatastoreWriteTimeoutMillis,
            interval
        );
    }

    @Override
    public Integer getDatastoreWriteTimeoutMillis() {
        return (Integer) this.configurableProperties.get(ConfigurableProperty.DatastoreWriteTimeoutMillis);
    }

    @Override
    public void registerMetadata(JDOMetadata metadata) {
        throw new JDOFatalDataStoreException(
            "openMDX doesn't support JDO's native meta data API yet"
        );
    }

    @Override
    public JDOMetadata newMetadata() {
        throw new JDOFatalDataStoreException(
            "openMDX doesn't support JDO's native meta data API yet"
        );
    }

    @Override
    public TypeMetadata getMetadata(String className) {
        throw new JDOFatalDataStoreException(
            "openMDX doesn't support JDO's native meta data API yet"
        );
    }

    @Override
    public Collection<Class> getManagedClasses() {
        throw new JDOFatalDataStoreException(
            "openMDX doesn't support JDO's native meta data API yet"
        );
    }

    protected static Map<String, Object> createDefaultConfiguration(
        Map<String, ?> amendments
    ) {
        final Map<String, Object> configuration = new HashMap<>(DEFAULT_CONFIGURATION);
        configuration.putAll(amendments);
        return Collections.unmodifiableMap(configuration);
    }

    static {
        AbstractPersistenceManagerFactory.NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperty.VendorName.qualifiedName(),
            "openMDX"
        );
        AbstractPersistenceManagerFactory.NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperty.VersionNumber.qualifiedName(),
            Version.getSpecificationVersion()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.Optimistic.qualifiedName(),
            Boolean.TRUE.toString()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.Multithreaded.qualifiedName(),
            Boolean.TRUE.toString()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.IsolateThreads.qualifiedName(),
            Boolean.FALSE.toString()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.FinalizePersistenceManagers.qualifiedName(),
            Boolean.FALSE.toString()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.CopyOnAttach.qualifiedName(),
            Boolean.TRUE.toString()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.NontransactionalRead.qualifiedName(),
            Boolean.TRUE.toString()
        );
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.TransactionIsolationLevel.qualifiedName(),
            Constants.TX_REPEATABLE_READ
        );
        for (ConfigurableProperty configurableProperty : ConfigurableProperty.values()) {
            String qualifiedName = configurableProperty.qualifiedName();
            Object value = System.getProperty(qualifiedName);
            if (value != null) {
                DEFAULT_CONFIGURATION.put(qualifiedName, value);
            }
        }
    }

}
