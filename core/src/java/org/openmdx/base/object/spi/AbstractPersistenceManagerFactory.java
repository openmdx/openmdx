/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractPersistenceManagerFactory.java,v 1.8 2008/02/08 16:51:40 hburger Exp $
 * Description: Abstract Persistence Manager Factory
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:40 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.base.object.spi;

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

import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;

import org.openmdx.base.Version;
import org.openmdx.base.object.jdo.NonConfigurableProperties_2_0;
import org.openmdx.base.object.jdo.Options_2_0;
import org.openmdx.kernel.callback.CloseCallback;

/**
 * Abstract Persistence Manager Factory
 *
 * @since openMDX 2.0
 */
public abstract class AbstractPersistenceManagerFactory
    implements PersistenceManagerFactory, CloseCallback
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected AbstractPersistenceManagerFactory(
        Map<String,Object> configuration
    ) {
        setOptimistic(getFlag(configuration, Options_2_0.OPTIMISTIC));
        setRetainValues(getFlag(configuration,Options_2_0.RETAIN_VALUES));
        setRestoreValues(getFlag(configuration,Options_2_0.RESTORE_VALUES));
        setIgnoreCache(getFlag(configuration,Options_2_0.IGNORE_CACHE));
        setNontransactionalRead(getFlag(configuration,Options_2_0.NONTRANSACTIONAL_READ));
        setNontransactionalWrite(getFlag(configuration,Options_2_0.NONTRANSACTIONAL_WRITE));
        setMultithreaded(getFlag(configuration,Options_2_0.MULTITHREADED));
        if(configuration.containsKey(Options_2_0.CONNECTION_USER_NAME)) setConnectionUserName(
            (String)configuration.get(Options_2_0.CONNECTION_USER_NAME)
        );
        if(configuration.containsKey(Options_2_0.CONNECTION_PASSWORD)) setConnectionPassword(
            (String)configuration.get(Options_2_0.CONNECTION_PASSWORD)
        );
        if(configuration.containsKey(Options_2_0.CONNECTION_URL)) setConnectionURL(
            (String)configuration.get(Options_2_0.CONNECTION_URL)
        );
        if(configuration.containsKey(Options_2_0.CONNECTION_FACTORY_NAME)) setConnectionFactoryName(
            (String)configuration.get(Options_2_0.CONNECTION_FACTORY_NAME)
        );
        if(configuration.containsKey(Options_2_0.CONNECTION_FACTORY2_NAME)) setConnectionFactory2Name(
            (String)configuration.get(Options_2_0.CONNECTION_FACTORY2_NAME)
        );
        if(configuration.containsKey(Options_2_0.MAPPING)) setMapping(
            (String)configuration.get(Options_2_0.MAPPING)
        );
        if(configuration.containsKey(Options_2_0.BINDING_PACKAGE_SUFFIX)) setDefaultImplPackageSuffix(
            (String)configuration.get(Options_2_0.BINDING_PACKAGE_SUFFIX)
        );
    }

    /**
     * 
     */
    static final String CONNECTION_FACTORY = "org.openmdx.jdo.ConnectionFactory";
    
    /**
     * 
     */
    static final String CONNECTION_FACTORY2 = "org.openmdx.jdo.ConnectionFactory2";
    
    /**
     * A <code>PersistenceManagerFactory</code>'s configuration is its
     * <code>PersistenceManager</code>s' default configuration.
     */
    private Map<String,Object> configurableProperties = new HashMap<String,Object>();

    /**
     * <code>PersistentManager</code> book keeping.
     */
    private Set<PersistenceManager> persistenceManagers = new HashSet<PersistenceManager>();
    
    /**
     * The <code>PersistenceManagerFactory</code>'s properties.
     */
    private final static Properties NON_CONFIGURABLE_PROPERTIES = new Properties();

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
            Options_2_0.TRANSIENT_TRANSACTIONAL, // "javax.jdo.option.TransientTransactional"
            Options_2_0.NONTRANSACTIONAL_READ, // "javax.jdo.option.NontransactionalRead"
            Options_2_0.NONTRANSACTIONAL_WRITE, // "javax.jdo.option.NontransactionalWrite"
            Options_2_0.RETAIN_VALUES, // "javax.jdo.option.RetainValues"
            Options_2_0.OPTIMISTIC, // "javax.jdo.option.Optimistic"
            Options_2_0.APPLICATION_IDENTITY, // "javax.jdo.option.ApplicationIdentity"
            Options_2_0.CHANGE_APPLICATION_IDENTITY, // "javax.jdo.option.ChangeApplicationIdentity"
            Options_2_0.LIST, // "javax.jdo.option.List"
            Options_2_0.GET_DATA_STORE_CONNECTION // "javax.jdo.option.GetDataStoreConnection"
        )
    );
    
    /**
     * The <code>PersistenceManagerFactory</code>'s instance lifecycle notifiers 
     * to be propagated to its <code>PersistenceManager</code> instances.
     */
    private transient InstanceLifecycleNotifier instanceLifecycleNotifier = null;
    
    /**
     * Retrieve the <code>PersistenceManagerFactory</code>'s instance lifecycle notifiers 
     * to be propagated to its <code>PersistenceManager</code> instances.
     * 
     * @return the <code>PersistenceManagerFactory</code>'s instance lifecycle notifiers 
     * to be propagated to its <code>PersistenceManager</code> instances
     */
    private synchronized InstanceLifecycleNotifier getInstanceLifecycleNotifier(){
        if(this.instanceLifecycleNotifier == null) this.instanceLifecycleNotifier = new InstanceLifecycleNotifier();
        return this.instanceLifecycleNotifier;
    }
    
    /**
     * The <code>PersistenceManagerFactory/code>'s <code>DataStoreCache</code>.
     */
    private transient DataStoreCache datastoreCache = null;

    /**
     * Return <code>true</code> if the property's value is
     * <code>"true"</code> ignoring case.
     * 
     * @param properties
     * @param option
     * @return <code>true</code> if the flag is on
     */
    private static boolean getFlag (
        Map<String,Object> properties,
        String option
    ){
        return "true".equalsIgnoreCase(
            (String)properties.get(option)
        );
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
        String name,
        Object value
    ){
        if(isFrozen()) {
            throw new JDOFatalUserException(
                "Persistence Manager Factory is already opened"
            );
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
    public void postClose(Object closed) {
        this.persistenceManagers.remove(closed);
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#close()
     */
    public synchronized void close() {
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
        for(PersistenceManager p : this.persistenceManagers){
            p.close();
        }
        this.persistenceManagers = null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#isClosed()
     */
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
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     * 
     * @return a new persistence manager
     */
    protected abstract PersistenceManager newPersistenceManager(
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
    );

    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     * 
     * @return a new persistence manager
     */
    protected abstract PersistenceManager newPersistenceManager(
        InstanceLifecycleNotifier notifier
    );
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager()
     */
    public final PersistenceManager getPersistenceManager(
    ){
        freeze();
        String connectionUsername = this.getConnectionUserName();
        String connectionPassword = (String) this.configurableProperties.get(Options_2_0.CONNECTION_PASSWORD);
        return (
            (connectionUsername != null && connectionUsername.length() != 0) ||
            (connectionPassword != null && connectionPassword.length() != 0) 
        ) ? getPersistenceManager (
            connectionUsername,
            connectionPassword
        ) : newPersistenceManager (
            new InstanceLifecycleNotifier(getInstanceLifecycleNotifier())
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public final PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        freeze();
        return newPersistenceManager (
            new InstanceLifecycleNotifier(getInstanceLifecycleNotifier()),
            userid,
            password
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.DETACH_ALL_ON_COMMIT));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        setProperty(Options_2_0.DETACH_ALL_ON_COMMIT, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionUserName(java.lang.String)
     */
    public void setConnectionUserName(String userName) {
        setProperty(Options_2_0.CONNECTION_USER_NAME, userName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionUserName()
     */
    public String getConnectionUserName() {
        return (String) this.configurableProperties.get(Options_2_0.CONNECTION_USER_NAME);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionPassword(java.lang.String)
     */
    public void setConnectionPassword(String password) {
        setProperty(Options_2_0.CONNECTION_PASSWORD, password);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionURL(java.lang.String)
     */
    public void setConnectionURL(String URL) {
        setProperty(Options_2_0.CONNECTION_URL, URL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionURL()
     */
    public String getConnectionURL() {
        return (String) this.configurableProperties.get(Options_2_0.CONNECTION_URL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionDriverName(java.lang.String)
     */
    public void setConnectionDriverName(String driverName) {
        setProperty(Options_2_0.CONNECTION_DRIVER_NAME, driverName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionDriverName()
     */
    public String getConnectionDriverName() {
        return (String) this.configurableProperties.get(Options_2_0.CONNECTION_DRIVER_NAME);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactoryName(java.lang.String)
     */
    public void setConnectionFactoryName(String connectionFactoryName) {
        setProperty(Options_2_0.CONNECTION_FACTORY_NAME, connectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactoryName()
     */
    public String getConnectionFactoryName() {
        return (String) this.configurableProperties.get(Options_2_0.CONNECTION_FACTORY_NAME);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory(java.lang.Object)
     */
    public void setConnectionFactory(Object connectionFactory) {
        setProperty(CONNECTION_FACTORY, connectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory()
     */
    public Object getConnectionFactory() {
        return this.configurableProperties.get(CONNECTION_FACTORY);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2Name(java.lang.String)
     */
    public void setConnectionFactory2Name(String connectionFactoryName) {
        setProperty(Options_2_0.CONNECTION_FACTORY2_NAME, connectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2Name()
     */
    public String getConnectionFactory2Name() {
        return (String) this.configurableProperties.get(Options_2_0.CONNECTION_FACTORY2_NAME);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2(java.lang.Object)
     */
    public void setConnectionFactory2(Object connectionFactory) {
        setProperty(CONNECTION_FACTORY2, connectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2()
     */
    public Object getConnectionFactory2() {
        return this.configurableProperties.get(CONNECTION_FACTORY2);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        setProperty(Options_2_0.MULTITHREADED, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMultithreaded()
     */
    public boolean getMultithreaded() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.MULTITHREADED));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMapping(java.lang.String)
     */
    public void setMapping(String mapping) {
        setProperty(Options_2_0.MAPPING, mapping);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMapping()
     */
    public String getMapping() {
        return (String) this.configurableProperties.get(Options_2_0.MAPPING);
    }

    public void setDefaultImplPackageSuffix(
        String defaultImplPackageSuffix
    ) {
        this.configurableProperties.put(
            Options_2_0.BINDING_PACKAGE_SUFFIX, 
            defaultImplPackageSuffix
        );
    }
    
    public String getDefaultImplPackageSuffix(
    ) {
        return (String)this.configurableProperties.get(
            Options_2_0.BINDING_PACKAGE_SUFFIX
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setOptimistic(boolean)
     */
    public void setOptimistic(boolean flag) {
        setProperty(Options_2_0.OPTIMISTIC, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getOptimistic()
     */
    public boolean getOptimistic() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.OPTIMISTIC));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRetainValues(boolean)
     */
    public void setRetainValues(boolean flag) {
        setProperty(Options_2_0.RETAIN_VALUES, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRetainValues()
     */
    public boolean getRetainValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.RETAIN_VALUES));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRestoreValues(boolean)
     */
    public void setRestoreValues(boolean restoreValues) {
        setProperty(Options_2_0.RESTORE_VALUES, restoreValues ? Boolean.TRUE : Boolean.FALSE);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRestoreValues()
     */
    public boolean getRestoreValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.RESTORE_VALUES));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(boolean flag) {
        setProperty(Options_2_0.NONTRANSACTIONAL_READ, flag ? Boolean.TRUE : Boolean.FALSE);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.NONTRANSACTIONAL_READ));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(boolean flag) {
        setProperty(Options_2_0.NONTRANSACTIONAL_WRITE, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.NONTRANSACTIONAL_WRITE));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        setProperty(Options_2_0.IGNORE_CACHE, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return Boolean.TRUE.equals(this.configurableProperties.get(Options_2_0.IGNORE_CACHE));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getProperties()
     */
    public Properties getProperties() {
        return new Properties(NON_CONFIGURABLE_PROPERTIES);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#supportedOptions()
     */
    public Collection<String> supportedOptions() {
        return SUPPORTED_OPTIONS; 
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getDataStoreCache()
     */
    public synchronized DataStoreCache getDataStoreCache(
    ) {
        if(this.datastoreCache == null) this.datastoreCache = new DataStoreCache.EmptyDataStoreCache();
        return this.datastoreCache;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        this.instanceLifecycleNotifier.addInstanceLifecycleListener(listener, classes);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        this.instanceLifecycleNotifier.removeInstanceLifecycleListener(listener);
    }

    
    static {
        NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperties_2_0.VENDOR_NAME, 
            "OMEX AG"
        );
        NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperties_2_0.VERSION_NUMBER, 
            Version.getSpecificationVersion()
        );
    }

}
