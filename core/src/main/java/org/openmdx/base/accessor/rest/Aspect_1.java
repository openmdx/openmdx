/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Aspect Query
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.rest;

import java.util.Collection;
import java.util.Map;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.openmdx.base.persistence.spi.StandardFetchPlan;

/**
 * Aspect Query
 */
class Aspect_1 implements Query {
    
    /**
     * Constructor 
     *
     * @param persistenceManager
     */
    Aspect_1(
        DataObjectManager_1 persistenceManager
    ) {
        this.persistenceManager = persistenceManager;
    }

    /**
     * The query factory
     */
    private final DataObjectManager_1 persistenceManager;    

    /**
     * The actual fetch plan
     */
    private FetchPlan fetchPlan = null;

    /**
     * The number of milliseconds allowed for read operations to complete
     */
    private Integer datastoreReadTimeoutMillis;

    /**
     * The number of milliseconds allowed for write operations to complete
     */
    private Integer datastoreWriteTimeoutMillis;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7536537456223904973L;
    
    /* (non-Javadoc)
     * @see javax.jdo.Query#addExtension(java.lang.String, java.lang.Object)
     */
    public void addExtension(String key, Object value) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String)
     */
    public void addSubquery(
        Query sub,
        String variableDeclaration,
        String candidateCollectionExpression
    ) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addSubquery(
        Query sub,
        String variableDeclaration,
        String candidateCollectionExpression,
        String parameter
    ) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.lang.String[])
     */
    public void addSubquery(
        Query sub,
        String variableDeclaration,
        String candidateCollectionExpression,
        String... parameters
    ) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    public void addSubquery(
        Query sub,
        String variableDeclaration,
        String candidateCollectionExpression,
        Map parameters
   ) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#close(java.lang.Object)
     */
    public void close(
        Object queryResult
    ) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#closeAll()
     */
    public void closeAll() {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#compile()
     */
    public void compile() {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareImports(java.lang.String)
     */
    public void declareImports(String imports) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareParameters(java.lang.String)
     */
    public void declareParameters(String parameters) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareVariables(java.lang.String)
     */
    public void declareVariables(String variables) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll()
     */
    public long deletePersistentAll() {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll(java.lang.Object[])
     */
    public long deletePersistentAll(Object... parameters) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    public long deletePersistentAll(Map parameters) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute()
     */
    public Object execute() {
        throw new UnsupportedOperationException(
            "An aspect query's execute method requires the aspect class and the core object as arguments"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object)
     */
    public Object execute(Object p1) {
        throw new UnsupportedOperationException(
            "An aspect query's execute method requires the aspect class and the core object as arguments"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object)
     */
    public Object execute(Object p1, Object p2) {
        if(p1 instanceof String && p2 instanceof DataObject_1) {
            return ((DataObject_1) p2).getAspect((String)p1).values();
        } else throw new UnsupportedOperationException(
            "An aspect query's execute method requires the aspect class and the core object as arguments"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object execute(Object p1, Object p2, Object p3) {
        throw new UnsupportedOperationException(
            "An aspect query's execute method requires the aspect class and the core object as arguments"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#executeWithArray(java.lang.Object[])
     */
    public Object executeWithArray(Object... parameters) {
        if(parameters != null && parameters.length == 2) {
            return execute(parameters[0], parameters[1]);
        } else throw new UnsupportedOperationException(
            "An aspect query's execute method requires the aspect class and the core object as arguments"
        );

    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#executeWithMap(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    public Object executeWithMap(Map parameters) {
        throw new UnsupportedOperationException(
            "An aspect query's execute method requires the aspect class and the core object as arguments"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        if(this.fetchPlan == null) {
            this.fetchPlan = StandardFetchPlan.newInstance(getPersistenceManager());
        }
        return this.fetchPlan;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return this.persistenceManager.getIgnoreCache();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#isUnmodifiable()
     */
    public boolean isUnmodifiable() {
        return true;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setCandidates(javax.jdo.Extent)
     */
    @SuppressWarnings("rawtypes")
    public void setCandidates(Extent pcs) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setCandidates(java.util.Collection)
     */
    @SuppressWarnings("rawtypes")
    public void setCandidates(Collection pcs) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setClass(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    public void setClass(Class cls) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setExtensions(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    public void setExtensions(Map extensions) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setFilter(java.lang.String)
     */
    public void setFilter(String filter) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setGrouping(java.lang.String)
     */
    public void setGrouping(String group) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean ignoreCache) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setOrdering(java.lang.String)
     */
    public void setOrdering(String ordering) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setRange(java.lang.String)
     */
    public void setRange(String fromInclToExcl) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setRange(long, long)
     */
    public void setRange(long fromIncl, long toExcl) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setResult(java.lang.String)
     */
    public void setResult(String data) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setResultClass(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    public void setResultClass(Class cls) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setUnique(boolean)
     */
    public void setUnique(boolean unique) {
        throw new UnsupportedOperationException(
            "Method not supported by aspect query"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setUnmodifiable()
     */
    public void setUnmodifiable() {
        // Idempotent
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setDatastoreReadTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreReadTimeoutMillis(Integer interval) {
        this.datastoreReadTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getDatastoreReadTimeoutMillis()
     */
    @Override
    public Integer getDatastoreReadTimeoutMillis() {
        return this.datastoreReadTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setDatastoreWriteTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreWriteTimeoutMillis(Integer interval) {
        this.datastoreWriteTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getDatastoreWriteTimeoutMillis()
     */
    @Override
    public Integer getDatastoreWriteTimeoutMillis() {
        return this.datastoreWriteTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#cancelAll()
     */
    @Override
    public void cancelAll() {
        throw new JDOUnsupportedOptionException(
            "Cancel is not supported by openMDX"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#cancel(java.lang.Thread)
     */
    @Override
    public void cancel(Thread thread) {
        throw new JDOUnsupportedOptionException(
            "Cancel is not supported by openMDX"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setSerializeRead(java.lang.Boolean)
     */
    @Override
    public void setSerializeRead(Boolean serialize) {
        if(Boolean.TRUE.equals(serialize)) {
            throw new JDOFatalDataStoreException("openMDX does not support read serialization");
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getSerializeRead()
     */
    @Override
    public Boolean getSerializeRead() {
        return Boolean.FALSE;
    }

}
