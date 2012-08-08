/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DelegatingPersistenceManagerFactory.java,v 1.3 2011/04/12 15:44:01 hburger Exp $
 * Description: Delegating Persistence Manager Factory 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/12 15:44:01 $
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
package org.openmdx.base.persistence.spi;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.jdo.FetchGroup;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;

import org.openmdx.base.accessor.view.ViewManagerFactory_1;

/**
 * Delegating Persistence Manager Factory
 */
@SuppressWarnings({"rawtypes"})
public abstract class DelegatingPersistenceManagerFactory
    implements PersistenceManagerFactory
{

    /**
     * Constructor 
     */
    protected DelegatingPersistenceManagerFactory(
    ){
        super();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 8157804936933793541L;

    protected abstract PersistenceManagerFactory delegate();

    /**
     * @param listener
     * @param classes
     * @see javax.jdo.PersistenceManagerFactory#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        delegate().addInstanceLifecycleListener(listener, classes);
    }

    /**
     * 
     * @see javax.jdo.PersistenceManagerFactory#close()
     */
    public void close() {
        delegate().close();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionDriverName()
     */
    public String getConnectionDriverName() {
        return delegate().getConnectionDriverName();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory()
     */
    public Object getConnectionFactory() {
        return delegate().getConnectionFactory();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2()
     */
    public Object getConnectionFactory2() {
        return delegate().getConnectionFactory2();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2Name()
     */
    public String getConnectionFactory2Name() {
        return delegate().getConnectionFactory2Name();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactoryName()
     */
    public String getConnectionFactoryName() {
        return delegate().getConnectionFactoryName();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionURL()
     */
    public String getConnectionURL() {
        return delegate().getConnectionURL();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getConnectionUserName()
     */
    public String getConnectionUserName() {
        return delegate().getConnectionUserName();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getDataStoreCache()
     */
    public DataStoreCache getDataStoreCache() {
        return delegate().getDataStoreCache();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        return delegate().getDetachAllOnCommit();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return delegate().getIgnoreCache();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getMapping()
     */
    public String getMapping() {
        return delegate().getMapping();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getMultithreaded()
     */
    public boolean getMultithreaded() {
        return delegate().getMultithreaded();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        return delegate().getNontransactionalRead();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        return delegate().getNontransactionalWrite();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getOptimistic()
     */
    public boolean getOptimistic() {
        return delegate().getOptimistic();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return delegate().getPersistenceManager();
    }

    /**
     * @param userid
     * @param password
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        return delegate().getPersistenceManager(userid, password);
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getProperties()
     */
    public Properties getProperties() {
        return delegate().getProperties();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getRestoreValues()
     */
    public boolean getRestoreValues() {
        return delegate().getRestoreValues();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#getRetainValues()
     */
    public boolean getRetainValues() {
        return delegate().getRetainValues();
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#isClosed()
     */
    public boolean isClosed() {
        return delegate().isClosed();
    }

    /**
     * @param listener
     * @see javax.jdo.PersistenceManagerFactory#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        delegate().removeInstanceLifecycleListener(listener);
    }

    /**
     * @param driverName
     * @see javax.jdo.PersistenceManagerFactory#setConnectionDriverName(java.lang.String)
     */
    public void setConnectionDriverName(String driverName) {
        delegate().setConnectionDriverName(driverName);
    }

    /**
     * @param connectionFactory
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory(java.lang.Object)
     */
    public void setConnectionFactory(Object connectionFactory) {
        delegate().setConnectionFactory(connectionFactory);
    }

    /**
     * @param connectionFactory
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2(java.lang.Object)
     */
    public void setConnectionFactory2(Object connectionFactory) {
        delegate().setConnectionFactory2(connectionFactory);
    }

    /**
     * @param connectionFactoryName
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2Name(java.lang.String)
     */
    public void setConnectionFactory2Name(String connectionFactoryName) {
        delegate().setConnectionFactory2Name(connectionFactoryName);
    }

    /**
     * @param connectionFactoryName
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactoryName(java.lang.String)
     */
    public void setConnectionFactoryName(String connectionFactoryName) {
        delegate().setConnectionFactoryName(connectionFactoryName);
    }

    /**
     * @param password
     * @see javax.jdo.PersistenceManagerFactory#setConnectionPassword(java.lang.String)
     */
    public void setConnectionPassword(String password) {
        delegate().setConnectionPassword(password);
    }

    /**
     * @param url
     * @see javax.jdo.PersistenceManagerFactory#setConnectionURL(java.lang.String)
     */
    public void setConnectionURL(String url) {
        delegate().setConnectionURL(url);
    }

    /**
     * @param userName
     * @see javax.jdo.PersistenceManagerFactory#setConnectionUserName(java.lang.String)
     */
    public void setConnectionUserName(String userName) {
        delegate().setConnectionUserName(userName);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        delegate().setDetachAllOnCommit(flag);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        delegate().setIgnoreCache(flag);
    }

    /**
     * @param mapping
     * @see javax.jdo.PersistenceManagerFactory#setMapping(java.lang.String)
     */
    public void setMapping(String mapping) {
        delegate().setMapping(mapping);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        delegate().setMultithreaded(flag);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(boolean flag) {
        delegate().setNontransactionalRead(flag);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(boolean flag) {
        delegate().setNontransactionalWrite(flag);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setOptimistic(boolean)
     */
    public void setOptimistic(boolean flag) {
        delegate().setOptimistic(flag);
    }

    /**
     * @param restoreValues
     * @see javax.jdo.PersistenceManagerFactory#setRestoreValues(boolean)
     */
    public void setRestoreValues(boolean restoreValues) {
        delegate().setRestoreValues(restoreValues);
    }

    /**
     * @param flag
     * @see javax.jdo.PersistenceManagerFactory#setRetainValues(boolean)
     */
    public void setRetainValues(boolean flag) {
        delegate().setRetainValues(flag);
    }

    /**
     * @return
     * @see javax.jdo.PersistenceManagerFactory#supportedOptions()
     */
    public Collection<String> supportedOptions() {
        return delegate().supportedOptions();
    }    

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#addFetchGroups(javax.jdo.FetchGroup[])
     */
    public void addFetchGroups(FetchGroup... groups) {
        delegate().addFetchGroups(groups);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getCopyOnAttach()
     */
    public boolean getCopyOnAttach() {
        return delegate().getCopyOnAttach();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroup(java.lang.Class, java.lang.String)
     */
    public FetchGroup getFetchGroup(Class cls, String name) {
        return delegate().getFetchGroup(cls, name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroups()
     */
    public Set getFetchGroups() {
        return delegate().getFetchGroups();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getName()
     */
    public String getName() {
        return delegate().getName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManagerProxy()
     */
    public PersistenceManager getPersistenceManagerProxy() {
        return delegate().getPersistenceManagerProxy();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceUnitName()
     */
    public String getPersistenceUnitName() {
        return delegate().getPersistenceUnitName();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getReadOnly()
     */
    public boolean getReadOnly() {
        return delegate().getReadOnly();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getServerTimeZoneID()
     */
    public String getServerTimeZoneID() {
        return delegate().getServerTimeZoneID();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionIsolationLevel()
     */
    public String getTransactionIsolationLevel() {
        return delegate().getTransactionIsolationLevel();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionType()
     */
    public String getTransactionType() {
        return delegate().getTransactionType();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeAllFetchGroups()
     */
    public void removeAllFetchGroups() {
        delegate().removeAllFetchGroups();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeFetchGroups(javax.jdo.FetchGroup[])
     */
    public void removeFetchGroups(FetchGroup... groups) {
        delegate().removeFetchGroups(groups);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag) {
        delegate().setCopyOnAttach(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setName(java.lang.String)
     */
    public void setName(String name) {
        delegate().setName(name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setPersistenceUnitName(java.lang.String)
     */
    public void setPersistenceUnitName(String name) {
        delegate().setPersistenceUnitName(name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setReadOnly(boolean)
     */
    public void setReadOnly(boolean flag) {
        delegate().setReadOnly(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setServerTimeZoneID(java.lang.String)
     */
    public void setServerTimeZoneID(String timezoneid) {
        delegate().setServerTimeZoneID(timezoneid);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionIsolationLevel(java.lang.String)
     */
    public void setTransactionIsolationLevel(String level) {
        delegate().setTransactionIsolationLevel(level);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionType(java.lang.String)
     */
    public void setTransactionType(String name) {
        delegate().setTransactionType(name);
    }
    
    /**
     * Tells whether the transactions are container managed
     * 
     * @param persistenceManagerFactory
     * 
     * @return <code>true</code> if the persistenceManagerFactory is an instance of
     * <code>AbstractPersistenceManagerFactory</code> and its transactions are 
     * container managed
     */
    public static boolean isTransactionContainerManaged(
        PersistenceManagerFactory persistenceManagerFactory
    ){
        PersistenceManagerFactory current = persistenceManagerFactory;
        while(current instanceof DelegatingPersistenceManagerFactory) {
            current = ((DelegatingPersistenceManagerFactory)current).delegate();
        }
        return ViewManagerFactory_1.isTransactionContainerManaged(current); 
    }
    
}
