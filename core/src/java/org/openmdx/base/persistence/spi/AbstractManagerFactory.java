/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractManagerFactory.java,v 1.1 2008/06/27 13:56:09 hburger Exp $
 * Description: Abstract Manager Factory
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/27 13:56:09 $
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

import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.kernel.Version;
import org.openmdx.kernel.callback.CloseCallback;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.openmdx.kernel.persistence.cci.NonConfigurableProperty;
import org.openmdx.kernel.persistence.resource.Connection_2;

/**
 * Abstract Manager Factory
 *
 * @since openMDX 2.0
 */
public abstract class AbstractManagerFactory
    implements PersistenceManagerFactory, CloseCallback
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected AbstractManagerFactory(
        Map<String,Object> configuration
    ) {
        setOptimistic(getFlag(configuration, ConfigurableProperty.Optimistic));
        setRetainValues(getFlag(configuration,ConfigurableProperty.RetainValues));
        setRestoreValues(getFlag(configuration,ConfigurableProperty.RestoreValues));
        setIgnoreCache(getFlag(configuration,ConfigurableProperty.IgnoreCache));
        setNontransactionalRead(getFlag(configuration,ConfigurableProperty.NontransactionalRead));
        setNontransactionalWrite(getFlag(configuration,ConfigurableProperty.NontransactionalWrite));
        setMultithreaded(getFlag(configuration,ConfigurableProperty.Multithreaded));
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
        if(configuration.containsKey(ConfigurableProperty.BindingPackageSuffix.qualifiedName())) setBindingPackageSuffix(
            (String)configuration.get(ConfigurableProperty.BindingPackageSuffix.qualifiedName())
        );
        if(configuration.containsKey(ConfigurableProperty.ContainerManaged.qualifiedName())) setContainerManaged(
            Boolean.valueOf((String)configuration.get(ConfigurableProperty.ContainerManaged.qualifiedName()))
        );
    }
    
    /**
     * A <code>PersistenceManagerFactory</code>'s configuration is its
     * <code>PersistenceManager</code>s' default configuration.
     */
    private Map<ConfigurableProperty,Object> configurableProperties = 
        new HashMap<ConfigurableProperty,Object>();

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
    
    /**
     * The <code>PersistenceManagerFactory/code>'s <code>DataStoreCache</code>.
     */
    private transient DataStoreCache datastoreCache = null;

    /**
     * The listeners to be propagated to the children
     */
    private Map<InstanceLifecycleListener,Class<?>[]> listeners = null;
    
    /**
     * 
     */
    private static final Set<? extends Principal> NO_PRINCIPALS = Collections.emptySet();
    
    /**
     * 
     */
    private static final char[] NO_PASSWORD = new char[]{};
    
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
        ConfigurableProperty option
    ){
        return Boolean.valueOf(
            (String)properties.get(option.qualifiedName())
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
     * 
     * @param subject
     * 
     * @return a new persistence manager
     */
    protected abstract PersistenceManager newManager(
        Subject subject
    );

    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * @param notifier
     * 
     * @return a new persistence manager
     */
    protected abstract PersistenceManager newManager(
    );
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager()
     */
    public final PersistenceManager getPersistenceManager(
    ){
        freeze();
        String connectionUsername = this.getConnectionUserName();
        String connectionPassword = (String) this.configurableProperties.get(
            ConfigurableProperty.ConnectionPassword.qualifiedName()
        );
        PersistenceManager persistenceManager = (
            (connectionUsername != null && connectionUsername.length() != 0) ||
            (connectionPassword != null && connectionPassword.length() != 0) 
        ) ? newManager (
            toSubject(connectionUsername,connectionPassword)
        ) : newManager (
        );
        initialize(persistenceManager);
        return persistenceManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public final PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        freeze();
        PersistenceManager persistenceManager = newManager (toSubject(userid,password));
        initialize(persistenceManager);
        return persistenceManager;
    }

    protected final void initialize(
        PersistenceManager persistenceManager,
        boolean setFactory
    ){
        if(setFactory) {
            ((Connection_2)persistenceManager).setPersistenceManagerFactory(this);
        }
        if(this.listeners != null) {
            for(Map.Entry<InstanceLifecycleListener,Class<?>[]> e : this.listeners.entrySet()) {
                persistenceManager.addInstanceLifecycleListener(e.getKey(), e.getValue());
            }
        }
    }

    protected void initialize(
        PersistenceManager persistenceManager
    ){
        initialize(
            persistenceManager,
            persistenceManager instanceof Connection_2
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        return Boolean.TRUE.equals(this.configurableProperties.get(
            ConfigurableProperty.DetachAllOnCommit)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        setProperty(ConfigurableProperty.DetachAllOnCommit, 
            Boolean.valueOf(flag)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionUserName(java.lang.String)
     */
    public void setConnectionUserName(String userName) {
        setProperty(
            ConfigurableProperty.ConnectionUserName, 
            userName
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionUserName()
     */
    public String getConnectionUserName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionUserName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionPassword(java.lang.String)
     */
    public void setConnectionPassword(String password) {
        setProperty(ConfigurableProperty.ConnectionPassword, password);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionURL(java.lang.String)
     */
    public void setConnectionURL(String URL) {
        setProperty(ConfigurableProperty.ConnectionURL, URL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionURL()
     */
    public String getConnectionURL() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionURL);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionDriverName(java.lang.String)
     */
    public void setConnectionDriverName(String driverName) {
        setProperty(ConfigurableProperty.ConnectionDriverName, driverName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionDriverName()
     */
    public String getConnectionDriverName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionDriverName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactoryName(java.lang.String)
     */
    public void setConnectionFactoryName(String connectionFactoryName) {
        setProperty(ConfigurableProperty.ConnectionFactoryName, connectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactoryName()
     */
    public String getConnectionFactoryName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory(java.lang.Object)
     */
    public void setConnectionFactory(Object connectionFactory) {
        setProperty(ConfigurableProperty.ConnectionFactory, connectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory()
     */
    public Object getConnectionFactory() {
        return this.configurableProperties.get(ConfigurableProperty.ConnectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2Name(java.lang.String)
     */
    public void setConnectionFactory2Name(String connectionFactoryName) {
        setProperty(ConfigurableProperty.ConnectionFactory2Name, connectionFactoryName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2Name()
     */
    public String getConnectionFactory2Name() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ConnectionFactory2Name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2(java.lang.Object)
     */
    public void setConnectionFactory2(Object connectionFactory) {
        setProperty(ConfigurableProperty.ConnectionFactory2, connectionFactory);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2()
     */
    public Object getConnectionFactory2() {
        return this.configurableProperties.get(ConfigurableProperty.ConnectionFactory2);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        setProperty(ConfigurableProperty.Multithreaded, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMultithreaded()
     */
    public boolean getMultithreaded() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.Multithreaded));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMapping(java.lang.String)
     */
    public void setMapping(String mapping) {
        setProperty(ConfigurableProperty.Mapping, mapping);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMapping()
     */
    public String getMapping() {
        return (String) this.configurableProperties.get(ConfigurableProperty.Mapping);
    }

    public void setBindingPackageSuffix(
        String bindingPackageSuffix
    ) {
        this.configurableProperties.put(
            ConfigurableProperty.BindingPackageSuffix, 
            bindingPackageSuffix
        );
    }
    
    public String getBindingPackageSuffix(
    ) {
        return (String)this.configurableProperties.get(
            ConfigurableProperty.BindingPackageSuffix
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setOptimistic(boolean)
     */
    public void setOptimistic(boolean flag) {
        setProperty(ConfigurableProperty.Optimistic, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getOptimistic()
     */
    public boolean getOptimistic() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.Optimistic));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRetainValues(boolean)
     */
    public void setRetainValues(boolean flag) {
        setProperty(ConfigurableProperty.RetainValues, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRetainValues()
     */
    public boolean getRetainValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.RetainValues));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRestoreValues(boolean)
     */
    public void setRestoreValues(boolean restoreValues) {
        setProperty(ConfigurableProperty.RestoreValues, restoreValues ? Boolean.TRUE : Boolean.FALSE);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRestoreValues()
     */
    public boolean getRestoreValues() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.RestoreValues));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(boolean flag) {
        setProperty(ConfigurableProperty.NontransactionalRead, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.NontransactionalRead));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(boolean flag) {
        setProperty(ConfigurableProperty.NontransactionalWrite, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.NontransactionalWrite));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        setProperty(ConfigurableProperty.IgnoreCache, Boolean.valueOf(flag));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.IgnoreCache));
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
        if(isFrozen()) {
            throw new JDOFatalUserException(FROZEN);
        } else {
            Class<?>[] newClasses;
            if(this.listeners == null) {
                this.listeners = new HashMap<InstanceLifecycleListener,Class<?>[]>();
                newClasses = classes;
            } else if (classes != null && this.listeners.containsKey(listener)){
                Class<?>[] oldClasses = this.listeners.get(listener);
                if(oldClasses == null) {
                    newClasses = null;
                } else {
                    Set<Class<?>> classSet = new HashSet<Class<?>>(
                        Arrays.asList(oldClasses)
                    );
                    classSet.addAll(
                        Arrays.asList((Class<?>[])classes)
                    );
                    newClasses = classSet.toArray(new Class<?>[classSet.size()]);
                }
            } else {
                newClasses = classes;
            }
            this.listeners.put(listener, newClasses);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        if(isFrozen()) {
            throw new JDOFatalUserException(FROZEN);
        } else if (this.listeners != null){
            this.listeners.remove(listener);
        }
    }
    
    /**
     * Retrieve the container-managed flag.
     * 
     * @return the value of the container-managed flag
     */
    protected boolean isContainerManaged() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.ContainerManaged));
    }

    /**
     * Set the container-managed flag.
     * 
     * @param flag the value of the container-managed flag
     */
    protected void setContainerManaged(
        boolean flag
    ){
        this.configurableProperties.put(
            ConfigurableProperty.ContainerManaged, 
            Boolean.valueOf(flag)
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getCopyOnAttach()
     */
    public boolean getCopyOnAttach() {
        return Boolean.TRUE.equals(this.configurableProperties.get(ConfigurableProperty.CopyOnAttach));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag) {
        this.configurableProperties.put(
            ConfigurableProperty.ContainerManaged, 
            Boolean.valueOf(flag)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getName()
     */
    public String getName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.Name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setName(java.lang.String)
     */
    public void setName(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.Name, 
            name
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceUnitName()
     */
    public String getPersistenceUnitName() {
        return (String) this.configurableProperties.get(ConfigurableProperty.PersistenceUnitName);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setPersistenceUnitName(java.lang.String)
     */
    public void setPersistenceUnitName(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.PersistenceUnitName, 
            name
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getServerTimeZoneID()
     */
    public String getServerTimeZoneID() {
        return (String) this.configurableProperties.get(ConfigurableProperty.ServerTimeZoneID);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setServerTimeZoneID(java.lang.String)
     */
    public void setServerTimeZoneID(String timezoneid) {
        this.configurableProperties.put(
            ConfigurableProperty.ServerTimeZoneID, 
            timezoneid
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionType()
     */
    public String getTransactionType() {
        return (String) this.configurableProperties.get(ConfigurableProperty.TransactionType);
    }


    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionType(java.lang.String)
     */
    public void setTransactionType(String name) {
        this.configurableProperties.put(
            ConfigurableProperty.TransactionType, 
            name
        );
    }    /**
     * Convert the stringified principal chain into an array
     * 
     * @param connectionUsername a principal or the stringified principal list 
     * 
     * @return a principal array
     */
    public static String[] getPrincipalChain(
        String connectionUsername
    ){
        if(
            connectionUsername == null || 
            "".equals(connectionUsername)
        ) {
            return new String[]{};
        } else if (
            (connectionUsername.startsWith("[") && connectionUsername.endsWith("]")) ||
            (connectionUsername.startsWith("{") && connectionUsername.endsWith("}"))
        ) {
            List<String> principalChain = new ArrayList<String>();
            for(
                int j = 0, i = 1, iLimit = connectionUsername.length() - 1;
                i < iLimit;
                i = j + 2
            ){
                j = connectionUsername.indexOf(", ", i);
                if(j < 0) j = iLimit;
                principalChain.add(connectionUsername.substring(i, j));
            }
            return principalChain.toArray(
                new String[principalChain.size()]
            );
        } else {
            return new String[]{connectionUsername};
        }
    }

    /**
     * Create a read only subject based on the given credentials
     * 
     * @param username
     * @param password
     * 
     * @return a new subject
     */
    protected static Subject toSubject(
        String username,
        String password
    ){
        return new Subject(
            true, // readOnly, 
            NO_PRINCIPALS,
            Collections.emptySet(), // pubCredentials, 
            Collections.singleton(
                new PasswordCredential(
                    username,
                    password == null ? NO_PASSWORD : password.toCharArray()
                )
            ) //  privCredentials                
        );
    }

    /**
     * Retrieve a subject's password credential
     * 
     * @param subject
     * 
     * @return the subject's password credential
     */
    public static PasswordCredential getCredential(
        Subject subject
    ){
        Set<PasswordCredential> credentials = subject.getPrivateCredentials(PasswordCredential.class);
        switch(credentials.size()) {
            case 0: 
                return null;
            case 1: 
                return credentials.iterator().next();
            default: 
                throw new JDOFatalUserException(
                    "The subject should contain exactly one password credential: " + credentials.size()
                );
        }
    }
    
    /**
     * Create a service header populated with a principal list encoded as 
     * Subject embedded PasswordCredential. 
     * 
     * @param subject
     * 
     * @return a new service header
     */
    public static ServiceHeader toServiceHeader(
        Subject subject
    ){
        PasswordCredential credential = getCredential(subject);
        return credential == null ? new ServiceHeader() : toServiceHeader(
            credential.getUserName(), 
            new String(credential.getPassword())
        );            
    }
    
    /**
     * Create a service header populated with a principal list encoded as 
     * user name.
     * 
     * @param connectionUsername a principal or the stringified principal list 
     * @param connectionPassword the correlation id
     * 
     * @return a principal array
     */
    public static ServiceHeader toServiceHeader(
        String connectionUsername, 
        String connectionPassword
    ){
        return  new ServiceHeader(
            getPrincipalChain(connectionUsername),
            "".equals(connectionPassword) ? null : connectionPassword, // correlationId,
            false, // traceRequest
            new QualityOfService(),
            null, // requestedAt,
            null // requestedFor
        );
    }
    
    static {
        NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperty.VendorName.qualifiedName(), 
            "OMEX AG"
        );
        NON_CONFIGURABLE_PROPERTIES.setProperty(
            NonConfigurableProperty.VersionNumber.qualifiedName(), 
            Version.getSpecificationVersion()
        );
    }

}
