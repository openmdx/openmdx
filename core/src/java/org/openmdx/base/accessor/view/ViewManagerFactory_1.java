/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ViewManagerFactory_1.java,v 1.7 2010/04/29 11:48:37 hburger Exp $
 * Description: View Manager Factory
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/29 11:48:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.view;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.jdo.FetchGroup;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.listener.InstanceLifecycleListener;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.aop1.PlugIn_1_0;


/**
 * View Manager Factory
 */
public class ViewManagerFactory_1 implements PersistenceManagerFactory {

    /**
     * Constructor 
     *
     * @param delegate
     */
    public ViewManagerFactory_1(
        PersistenceManagerFactory delegate,
        PlugIn_1_0... plugIns
    ){
        this.delegate = delegate;
        this.plugIns = plugIns;
    }
    
    /**
     * Implements {@link Serializable}
     */
    private static final long serialVersionUID = 7786530602854422222L;

    /**
     * The View Manager Plug-Ins
     */
    private final PlugIn_1_0[] plugIns;
    
    /**
     * The Data Object Manager Factory
     */
    private PersistenceManagerFactory delegate;

    @SuppressWarnings("unchecked")
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void close() {
        this.delegate = null;
    }

    public String getConnectionDriverName() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public Object getConnectionFactory() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public Object getConnectionFactory2() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public String getConnectionFactory2Name() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public String getConnectionFactoryName() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public String getConnectionURL() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public String getConnectionUserName() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public DataStoreCache getDataStoreCache() {
        return this.delegate.getDataStoreCache();
    }

    public String getMapping() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public String getName() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public PersistenceManager getPersistenceManager() {
        return new ViewManager_1(
            this,
            (DataObjectManager_1_0) this.delegate.getPersistenceManager(),
            this.plugIns
        );
    }

    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        return new ViewManager_1(
            this,
            (DataObjectManager_1_0) this.delegate.getPersistenceManager(userid, password),
            this.plugIns
        );
    }

    public PersistenceManager getPersistenceManagerProxy() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public String getPersistenceUnitName() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public Properties getProperties() {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public boolean isClosed() {
        return this.delegate == null;
    }

    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionDriverName(String driverName) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionFactory(Object connectionFactory) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionFactory2(Object connectionFactory) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionFactory2Name(String connectionFactoryName) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionPassword(String password) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionURL(String url) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setConnectionUserName(String userName) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setMapping(String mapping) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setName(String name) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public void setPersistenceUnitName(String name) {
        throw new UnsupportedOperationException("Operation not supported by ViewManagerFactory_1");
    }

    public boolean getCopyOnAttach() {
        return this.delegate.getCopyOnAttach();
    }

    public boolean getDetachAllOnCommit() {
        return this.delegate.getDetachAllOnCommit();
    }

    @SuppressWarnings("unchecked")
    public FetchGroup getFetchGroup(Class cls, String name) {
        return this.delegate.getFetchGroup(cls, name);
    }

    @SuppressWarnings("unchecked")
    public Set getFetchGroups() {
        return this.delegate.getFetchGroups();
    }

    public boolean getIgnoreCache() {
        return this.delegate.getIgnoreCache();
    }

    public boolean getMultithreaded() {
        return this.delegate.getMultithreaded();
    }

    public boolean getNontransactionalRead() {
        return this.delegate.getNontransactionalRead();
    }

    public boolean getNontransactionalWrite() {
        return this.delegate.getNontransactionalWrite();
    }

    public boolean getOptimistic() {
        return this.delegate.getOptimistic();
    }

    public boolean getReadOnly() {
        return this.delegate.getReadOnly();
    }

    public boolean getRestoreValues() {
        return this.delegate.getRestoreValues();
    }

    public boolean getRetainValues() {
        return this.delegate.getRetainValues();
    }

    public String getServerTimeZoneID() {
        return this.delegate.getServerTimeZoneID();
    }

    public String getTransactionIsolationLevel() {
        return this.delegate.getTransactionIsolationLevel();
    }

    public String getTransactionType() {
        return this.delegate.getTransactionType();
    }

    public void removeAllFetchGroups() {
        this.delegate.removeAllFetchGroups();
    }

    public void removeFetchGroups(FetchGroup... groups) {
        this.delegate.removeFetchGroups(groups);
    }

    public void setCopyOnAttach(boolean flag) {
        this.delegate.setCopyOnAttach(flag);
    }

    public void setDetachAllOnCommit(boolean flag) {
        this.delegate.setDetachAllOnCommit(flag);
    }

    public void setIgnoreCache(boolean flag) {
        this.delegate.setIgnoreCache(flag);
    }

    public void setMultithreaded(boolean flag) {
        this.delegate.setMultithreaded(flag);
    }

    public void setNontransactionalRead(boolean flag) {
        this.delegate.setNontransactionalRead(flag);
    }

    public void setNontransactionalWrite(boolean flag) {
        this.delegate.setNontransactionalWrite(flag);
    }

    public void setOptimistic(boolean flag) {
        this.delegate.setOptimistic(flag);
    }

    public void setReadOnly(boolean flag) {
        this.delegate.setReadOnly(flag);
    }

    public void setRestoreValues(boolean restoreValues) {
        this.delegate.setRestoreValues(restoreValues);
    }

    public void setRetainValues(boolean flag) {
        this.delegate.setRetainValues(flag);
    }

    public void setServerTimeZoneID(String timezoneid) {
        this.delegate.setServerTimeZoneID(timezoneid);
    }

    public void setTransactionIsolationLevel(String level) {
        this.delegate.setTransactionIsolationLevel(level);
    }

    public Collection<String> supportedOptions() {
        return this.delegate.supportedOptions();
    }

    public void setTransactionType(String name) {
        this.delegate.setTransactionType(name);
    }

    public void addFetchGroups(FetchGroup... groups) {
        this.delegate.addFetchGroups(groups);
    }

}
