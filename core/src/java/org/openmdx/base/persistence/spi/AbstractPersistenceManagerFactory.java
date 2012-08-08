/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractPersistenceManagerFactory.java,v 1.23 2010/04/28 11:11:34 hburger Exp $
 * Description: Abstract Manager Factory
 * Revision:    $Revision: 1.23 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/28 11:11:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2009, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.persistence.spi;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jdo.FetchGroup;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;

import org.openmdx.base.collection.MapBackedSet;
import org.openmdx.kernel.Version;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.openmdx.kernel.persistence.cci.NonConfigurableProperty;
import org.openmdx.kernel.resource.spi.CloseCallback;

/**
 * Abstract Data Access Service Factory
 *
 * @since openMDX 2.0
 */
public abstract class AbstractPersistenceManagerFactory<P extends PersistenceManager>
    implements PersistenceManagerFactory, CloseCallback
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected AbstractPersistenceManagerFactory(
        Map<?,?> configuration
    ) {
        setOptimistic(getFlag(configuration, ConfigurableProperty.Optimistic));
        setRetainValues(getFlag(configuration,ConfigurableProperty.RetainValues));
        setRestoreValues(getFlag(configuration,ConfigurableProperty.RestoreValues));
        setIgnoreCache(getFlag(configuration,ConfigurableProperty.IgnoreCache));
        setNontransactionalRead(getFlag(configuration,ConfigurableProperty.NontransactionalRead));
        setNontransactionalWrite(getFlag(configuration,ConfigurableProperty.NontransactionalWrite));
        setMultithreaded(getFlag(configuration,ConfigurableProperty.Multithreaded));
        setCopyOnAttach(getFlag(configuration,ConfigurableProperty.CopyOnAttach));
        if(configuration.containsKey(ConfigurableProperty.ConnectionUserName.qualifiedName())) setConnectionUserName(
            (String)configuration.get(ConfigurableProperty.ConnectionUserName.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ConnectionPassword.qualifiedName())) setConnectionPassword(
            (String)configuration.get(ConfigurableProperty.ConnectionPassword.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ConnectionURL.qualifiedName())) setConnectionURL(
            (String)configuration.get(ConfigurableProperty.ConnectionURL.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ConnectionFactoryName.qualifiedName())) setConnectionFactoryName(
            (String)configuration.get(ConfigurableProperty.ConnectionFactoryName.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ConnectionFactory2Name.qualifiedName())) setConnectionFactory2Name(
            (String)configuration.get(ConfigurableProperty.ConnectionFactory2Name.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.Mapping.qualifiedName())) setMapping(
            (String)configuration.get(ConfigurableProperty.Mapping.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ConnectionFactory.qualifiedName())) setConnectionFactory(
            configuration.get(ConfigurableProperty.ConnectionFactory.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ConnectionFactory2.qualifiedName())) setConnectionFactory2(
            configuration.get(ConfigurableProperty.ConnectionFactory2.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.TransactionType.qualifiedName())) setTransactionType(
            (String)configuration.get(ConfigurableProperty.TransactionType.qualifiedName())
        );
        
    }
    
    /**
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = new HashMap<String, Object>();

    /**
     * A <code>PersistenceManagerFactory</code>'s configuration is its
     * <code>PersistenceManager</code>s' default configuration.
     */
    private Map<ConfigurableProperty,Object> configurableProperties = 
        new HashMap<ConfigurableProperty,Object>();

    /**
     * <code>PersistentManager</code> book keeping.
     */
    @SuppressWarnings("unchecked")
    private Set<PersistenceManager> persistenceManagers = MapBackedSet.decorate( 
        new WeakHashMap<PersistenceManager,Object>()
    );
    
    /**
     * The <code>PersistenceManagerFactory</code>'s properties.
     */
    private final static Properties NON_CONFIGURABLE_PROPERTIES = new Properties();

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4344984435301460319L;

    /**
     * The persistence manager factory scoped fetch groups
     */
    private Set<FetchGroup> fetchGroups = new HashSet<FetchGroup>();
    
    /**
     * The following optional features are supported<ul>
     * <li>javax.jdo.option.TransientTransactional
     * <li>javax.jdo.option.NontransactionalRead
     * <li>javax.jdo.option.NontransactionalWrite
     * <li>javax.jdo.option.RetainValues
     * <li>javax.jdo.option.Optimistic
     * <li>javax.jdo.option.ApplicationIdentity
     * <li>javax.jdo.option.ChangeApplicationIdentity
     * <li>javax.jdo.option.List
     * <li>javax.jdo.option.GetDataStoreConnection
     * </ul>
     * <p>
     * The following optional features are <b>not</b> supported<ul>
     * <li>javax.jdo.option.ArrayList
     * <li>javax.jdo.option.HashMap
     * <li>javax.jdo.option.Hashtable
     * <li>javax.jdo.option.LinkedList
     * <li>javax.jdo.option.TreeMap
     * <li>javax.jdo.option.TreeSet
     * <li>javax.jdo.option.Vector
     * <li>javax.jdo.option.Array
     * <li>javax.jdo.option.NullCollection
     * <li>javax.jdo.option.DatastoreIdentity
     * <li>javax.jdo.option.NonDurableIdentity
     * <li>javax.jdo.option.BinaryCompatibility
     * <li>javax.jdo.option.UnconstrainedQueryVariables
     * </ul>
     */
    private final static Collection<String> SUPPORTED_OPTIONS = Collections.unmodifiableCollection(
        Arrays.asList(
            ConfigurableProperty.TransientTransactional.qualifiedName(), // "javax.jdo.option.TransientTransactional"
            ConfigurableProperty.NontransactionalRead.qualifiedName(), // "javax.jdo.option.NontransactionalRead"
            ConfigurableProperty.RetainValues.qualifiedName(), // "javax.jdo.option.RetainValues"
            ConfigurableProperty.Optimistic.qualifiedName(), // "javax.jdo.option.Optimistic"
            ConfigurableProperty.ApplicationIdentity.qualifiedName(), // "javax.jdo.option.ApplicationIdentity"
            ConfigurableProperty.GetDataStoreConnection.qualifiedName(), // "javax.jdo.option.GetDataStoreConnection"
            "javax.jdo.option.List"
        )
    );
    
    /**
     * A fatal user exception message
     */
    private static final String FROZEN = 
        "The persistence manager factory is no longer configurable";
    
    private static final String VENDOR_NAME = "openMDX";
    
    /**
     * The <code>PersistenceManagerFactory/code>'s <code>DataStoreCache</code>.
     */
    private transient DataStoreCache datastoreCache = null;

    /**
     * The listeners to be propagated to the children
     */
    private final InstanceLifecycleListenerRegistry instanceLifecycleListenerRegistry = new InstanceLifecycleListenerRegistry();
    
    /**
     * 
     */
    protected static final Set<? extends Principal> NO_PRINCIPALS = Collections.emptySet();

    /**
     * 
     */
    protected static final Set<Object> NO_CREDENTIALS = Collections.emptySet();
    
    /**
     * Return <code>true</code> if the property's value is
     * <code>"true"</code> ignoring case.
     * 
     * @param properties
     * @param option
     * @return <code>true</code> if the flag is on
     */
    private static boolean getFlag (
        Map<?,?> properties,
        ConfigurableProperty option,
        boolean defaultValue
    ){
        Object flag = properties.get(option.qualifiedName());
        return flag == null ?
            defaultValue :
                flag instanceof Boolean ? 
                    ((Boolean)flag).booleanValue() : 
                        Boolean.valueOf((String)flag
        );
    }

    private static boolean getFlag (
        Map<?,?> properties,
        ConfigurableProperty option
    ){
        return getFlag(properties, option, false);
    }
        
    /**
     * Freeze the <code>PersistenceManagerFactory</code>'s configurable properties.
     * 
     * @exception JDOFatalUserException if the <code>PersistenceManagerFactory</code>
     * is closed
     */
    protected void freeze(){        
        if(isClosed()) throw new JDOFatalUserException(
            "Persistence Manager Factory closed"
        );
        if(!isFrozen()) {
            this.configurableProperties = Collections.unmodifiableMap(this.configurableProperties);
        }
    }
    
    /**
     * Set a configurable property
     * 
     * @param name the property
     * @param value the value of the property
     * 
     * @exception JDOFatalUserException If the factory is no longer configurable
     */
    protected void setProperty(
        ConfigurableProperty name,
        Object value
    ){
        if(isFrozen()) {
            throw new JDOFatalUserException(FROZEN);
        } else {
            this.configurableProperties.put(name, value);
        }
    }

    
    //------------------------------------------------------------------------
    // Implements CloseCallback
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.callback.CloseCallback#postClose(java.lang.Object)
     */
    @Override
    public synchronized void postClose(Object closed) {
        if(!isClosed()) {
            this.persistenceManagers.remove(closed);
        }
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#close()
     */
    @Override
    public synchronized void close() {
        if(!isClosed()) {
            List<JDOUserException> exceptions = new ArrayList<JDOUserException>();
            for(PersistenceManager p : this.persistenceManagers){
                if(p.currentTransaction().isActive()) exceptions.add(
                    new JDOUserException("PersistenceManager has active transaction", p)
                );
            }
            if(!exceptions.isEmpty()) throw new JDOUserException(
                "PersistenceManager with active transaction prevents close",
                exceptions.toArray(
                    new JDOUserException[exceptions.size()]
                )
            );
            Set<PersistenceManager> persistenceManagers = this.persistenceManagers;
            this.persistenceManagers = null;
            for(PersistenceManager p : persistenceManagers){
                p.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#isClosed()
     */
    @Override
    public boolean isClosed() {
        return this.persistenceManagers == null;
    }

    /**
     * Test whether the configurable properties are frozen or not
     * 
     * @return <code>true</code> if the configurable properties are frozen
     */
    protected boolean isFrozen(){
        return this.configurableProperties.getClass() != HashMap.class;
    }

    /**
     * The sub-class specific factory method
     * 
     * @param userid
     * @param password
     * 
     * @return a new <code>PersistenceManager</code>
     */
    protected abstract P newPersistenceManager(
        String userid,
        String password
    );

    /**
     * The sub-class specific factory method
     * 
     * @return a new <code>PersistenceManager</code>
     */
    protected abstract P newPersistenceManager(
    );
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager()
     */
    @Override
    public final P getPersistenceManager(
    ){
        freeze();
        P persistenceManager = newPersistenceManager ();
        initialize(persistenceManager);
        return persistenceManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    public final P getPersistenceManager(
        String userid,
        String password
    ) {
        freeze();
        P persistenceManager = newPersistenceManager (
            userid,
            password
        );
        initialize(persistenceManager);
        return persistenceManager;
    }

    /**
     * Initialize a newly crated persisetnce manager
     * 
     * @param persistenceManager
     */
    protected void initialize(
        PersistenceManager persistenceManager
    ){
        if(persistenceManager instanceof Connection_2) {
            ((Connection_2)persistenceManager).setPersistenceManagerFactory(this);
        }
        this.instanceLifecycleListenerRegistry.propagateTo(persistenceManager);
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getDetachAllOnCommit()
     */
    @Override
    public boolean getDetachAllOnCommit() {
        return Boolean.TRUE.equals(
            this.configurableProperties.get(ConfigurableProperty.DetachAllOnCommit)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setDetachAllOnCommit(boolean)
     */
    @Override
    public void setDetachAllOnCommit(boolean flag) {
        setProperty(ConfigurableProperty.DetachAllOnCommit, 
            Boolean.valueOf(flag)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionUserName(java.lang.String)
     */
    @Override
    public void setConnectionUserName(String userName) {
        setProperty(
            ConfigurableProperty.ConnectionUserName, 
            userName
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionUserName()
     */
    @Override
    public String getConnectionUserName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionUserName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionPassword(java.lang.String)
     */
    @Override
    public void setConnectionPassword(String password) {
        setProperty(ConfigurableProperty.ConnectionPassword, password);
    }

    protected String getConnectionPassword(){
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionPassword);
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionURL(java.lang.String)
     */
    @Override
    public void setConnectionURL(String URL) {
        setProperty(ConfigurableProperty.ConnectionURL, URL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionURL()
     */
    @Override
    public String getConnectionURL() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionURL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionDriverName(java.lang.String)
     */
    @Override
    public void setConnectionDriverName(String driverName) {
        setProperty(ConfigurableProperty.ConnectionDriverName, driverName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionDriverName()
     */
    @Override
    public String getConnectionDriverName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionDriverName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactoryName(java.lang.String)
     */
    @Override
    public void setConnectionFactoryName(String connectionFactoryName) {
        setProperty(ConfigurableProperty.ConnectionFactoryName, connectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactoryName()
     */
    @Override
    public String getConnectionFactoryName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory(java.lang.Object)
     */
    @Override
    public void setConnectionFactory(Object connectionFactory) {
        setProperty(ConfigurableProperty.ConnectionFactory, connectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory()
     */
    @Override
    public Object getConnectionFactory() {
        return this.configurableProperties.get(ConfigurableProperty.ConnectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2Name(java.lang.String)
     */
    @Override
    public void setConnectionFactory2Name(String connectionFactoryName) {
        setProperty(ConfigurableProperty.ConnectionFactory2Name, connectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2Name()
     */
    @Override
    public String getConnectionFactory2Name() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionFactory2Name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2(java.lang.Object)
     */
    @Override
    public void setConnectionFactory2(Object connectionFactory) {
        setProperty(ConfigurableProperty.ConnectionFactory2, connectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2()
     */
    @Override
    public Object getConnectionFactory2() {
        return this.configurableProperties.get(ConfigurableProperty.ConnectionFactory2);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMultithreaded(boolean)
     */
    @Override
    public void setMultithreaded(boolean flag) {
        setProperty(ConfigurableProperty.Multithreaded, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMultithreaded()
     */
    @Override
    public boolean getMultithreaded() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.Multithreaded));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMapping(java.lang.String)
     */
    @Override
    public void setMapping(String mapping) {
        setProperty(ConfigurableProperty.Mapping, mapping);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMapping()
     */
    @Override
    public String getMapping() {
        return (String) this.configurableProperties.get(ConfigurableProperty.Mapping);
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#addFetchGroups(javax.jdo.FetchGroup[])
     */
    @Override
    public void addFetchGroups(FetchGroup... arg0) {
        synchronized(this.fetchGroups) {
            for(FetchGroup fetchGroup : fetchGroups) {
                fetchGroup.setUnmodifiable();
                fetchGroups.remove(fetchGroup);
                fetchGroups.add(fetchGroup);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public FetchGroup getFetchGroup(Class type, String name) {
        return new StandardFetchGroup(type, name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroups()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set getFetchGroups() {
        Set<FetchGroup> fetchGroups = new HashSet<FetchGroup>();
        synchronized(this.fetchGroups) {
            for(FetchGroup fetchGroup : this.fetchGroups) {
                fetchGroups.add(new StandardFetchGroup(fetchGroup));
            }
        }
        return fetchGroups;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManagerProxy()
     */
    @Override
    public PersistenceManager getPersistenceManagerProxy() {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManagerFactory_1");
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getReadOnly()
     */
    @Override
    public boolean getReadOnly() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.ReadOnly));    
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionIsolationLevel()
     */
    @Override
    public String getTransactionIsolationLevel() {
        return this.configurableProperties.get(ConfigurableProperty.TransactionIsolationLevel).toString();     
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeAllFetchGroups()
     */
    @Override
    public void removeAllFetchGroups() {
        synchronized(this.fetchGroups) {
            this.fetchGroups.clear();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeFetchGroups(javax.jdo.FetchGroup[])
     */
    @Override
    public void removeFetchGroups(FetchGroup... arg0) {
        synchronized(this.fetchGroups) {
            for(FetchGroup fetchGroup : fetchGroups) {
                fetchGroups.remove(fetchGroup);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean arg0) {
        setProperty(ConfigurableProperty.ReadOnly, Boolean.valueOf(arg0));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionIsolationLevel(java.lang.String)
     */
    @Override
    public void setTransactionIsolationLevel(String arg0) {
        setProperty(ConfigurableProperty.TransactionIsolationLevel, arg0);
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setOptimistic(boolean)
     */
    @Override
    public void setOptimistic(boolean flag) {
        setProperty(ConfigurableProperty.Optimistic, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getOptimistic()
     */
    @Override
    public boolean getOptimistic() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.Optimistic));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRetainValues(boolean)
     */
    @Override
    public void setRetainValues(boolean flag) {
        setProperty(ConfigurableProperty.RetainValues, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRetainValues()
     */
    @Override
    public boolean getRetainValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.RetainValues));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRestoreValues(boolean)
     */
    @Override
    public void setRestoreValues(boolean restoreValues) {
        setProperty(ConfigurableProperty.RestoreValues, restoreValues ? Boolean.TRUE : Boolean.FALSE);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRestoreValues()
     */
    @Override
    public boolean getRestoreValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.RestoreValues));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalRead(boolean)
     */
    @Override
    public void setNontransactionalRead(boolean flag) {
        setProperty(ConfigurableProperty.NontransactionalRead, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalRead()
     */
    @Override
    public boolean getNontransactionalRead() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.NontransactionalRead));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalWrite(boolean)
     */
    @Override
    public void setNontransactionalWrite(boolean flag) {
        setProperty(ConfigurableProperty.NontransactionalWrite, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalWrite()
     */
    @Override
    public boolean getNontransactionalWrite() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.NontransactionalWrite));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setIgnoreCache(boolean)
     */
    @Override
    public void setIgnoreCache(boolean flag) {
        setProperty(ConfigurableProperty.IgnoreCache, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getIgnoreCache()
     */
    @Override
    public boolean getIgnoreCache() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.IgnoreCache));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getProperties()
     */
    @Override
    public Properties getProperties() {
        return new Properties(NON_CONFIGURABLE_PROPERTIES);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#supportedOptions()
     */
    @Override
    public Collection<String> supportedOptions() {
        return SUPPORTED_OPTIONS; 
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getDataStoreCache()
     */
    @Override
    public synchronized DataStoreCache getDataStoreCache(
    ) {
        if(this.datastoreCache == null) this.datastoreCache = new DataStoreCache.EmptyDataStoreCache();
        return this.datastoreCache;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        if(isFrozen()) {
            throw new JDOFatalUserException(FROZEN);
        } else {
            this.instanceLifecycleListenerRegistry.addInstanceLifecycleListener(listener, classes);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    @Override
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        if(isFrozen()) {
            throw new JDOFatalUserException(FROZEN);
        } else {
            this.instanceLifecycleListenerRegistry.removeInstanceLifecycleListener(listener);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getCopyOnAttach()
     */
    @Override
    public boolean getCopyOnAttach() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.CopyOnAttach));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setCopyOnAttach(boolean)
     */
    @Override
    public void setCopyOnAttach(boolean flag) {
        this.configurableProperties.put(
            ConfigurableProperty.CopyOnAttach, 
            Boolean.valueOf(flag)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getName()
     */
    @Override
    public String getName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.Name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.Name, 
            name
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceUnitName()
     */
    @Override
    public String getPersistenceUnitName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.PersistenceUnitName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setPersistenceUnitName(java.lang.String)
     */
    @Override
    public void setPersistenceUnitName(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.PersistenceUnitName, 
            name
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getServerTimeZoneID()
     */
    @Override
    public String getServerTimeZoneID() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ServerTimeZoneID);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setServerTimeZoneID(java.lang.String)
     */
    @Override
    public void setServerTimeZoneID(String timezoneid) {
        this.configurableProperties.put(
            ConfigurableProperty.ServerTimeZoneID, 
            timezoneid
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionType()
     */
    @Override
    public String getTransactionType() {
        return (String) this.configurableProperties.get(ConfigurableProperty.TransactionType);
    }


    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionType(java.lang.String)
     */
    @Override
    public void setTransactionType(
        String name
    ) {
        this.configurableProperties.put(
            ConfigurableProperty.TransactionType, 
            name
        );
    }    
    
    static {
        NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperty.VendorName.qualifiedName(), 
            VENDOR_NAME
        );
        NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperty.VersionNumber.qualifiedName(), 
            Version.getSpecificationVersion()
        );
        DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.Optimistic.qualifiedName(), 
            Boolean.TRUE.toString()
        );
        DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.Multithreaded.qualifiedName(), 
            Boolean.TRUE.toString()
        );
        DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.CopyOnAttach.qualifiedName(),
            Boolean.TRUE.toString()
        );    
        DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.NontransactionalRead.qualifiedName(),
            Boolean.TRUE.toString()
        );    
    }

}
