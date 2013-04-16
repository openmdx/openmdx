/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Facade
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Query Facade
 */
public class Query_2Facade {

    /**
     * Constructor 
     *
     * @param delegate the query record
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     */
    private Query_2Facade(
        MappedRecord delegate, 
        boolean preferringNotFoundException
    ) throws ResourceException {
        if(!isDelegate(delegate)) {
            throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported query record",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_QUERY_CRITERIA,
                        new BasicException.Parameter("actual", delegate.getRecordName()),
                        new BasicException.Parameter("expected", QueryRecord.NAME)
                    )
                )
            );
        }
        this.delegate = (QueryRecord) delegate;
        this.preferringNotFoundException = preferringNotFoundException;
    }
    
    /**
     * Constructor 
     * 
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @throws ResourceException 
     */
    private Query_2Facade(
        boolean preferringNotFoundException
    ) throws ResourceException {
        this.delegate = (QueryRecord) Records.getRecordFactory().createMappedRecord(QueryRecord.NAME);
        this.preferringNotFoundException = preferringNotFoundException;
    }
    
    /**
     * The query record
     */
    private final QueryRecord delegate;

    /**
     * Tells whether a NOT_FOUND exception shall be thrown rather than 
     * returning an empty result set in case a requested object does not exist.
     */
    private final boolean preferringNotFoundException;
    
    /**
     * Retrieve delegate.
     *
     * @return Returns the delegate.
     */
    public final QueryRecord getDelegate() {
        return this.delegate;
    }

    /**
     * Create a facade for the given record
     * 
     * @param record
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return the query facade
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        MappedRecord record, 
        boolean preferringNotFoundException
    ) throws ResourceException {
        return new Query_2Facade(record, preferringNotFoundException);
    }

    /**
     * Create a facade for the given record
     * 
     * @param record
     * @return the query facade
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        MappedRecord record
    ) throws ResourceException {
        return new Query_2Facade(record, false);
    }
    
    /**
     * Create a facade for the given object id
     * 
     * @param transientObjectId
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return a facade for the given object id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        Path path,
        boolean preferringNotFoundException
    ) throws ResourceException {
        Query_2Facade facade = new Query_2Facade(preferringNotFoundException);
        facade.setPath(path);
        return facade;
    }
    
    /**
     * Create a facade for the given object id
     * 
     * @param transientObjectId
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return a facade for the given object id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        Path path
    ) throws ResourceException {
        return newInstance(path, false);
    }

    /**
     * Create a facade for the given transient object id
     * 
     * @param transientObjectId
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return a facade for the given transient object id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        UUID transientObjectId,
        boolean preferringNotFoundException
    ) throws ResourceException{
        return Query_2Facade.newInstance(
            new Path(transientObjectId),
            preferringNotFoundException
        );
    }

    /**
     * Create a facade for the given transient object id
     * 
     * @param transientObjectId
     * 
     * @return a facade for the given transient object id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        UUID transientObjectId
    ) throws ResourceException{
        return newInstance(transientObjectId, false);
    }

    /**
     * Create a query facade for the object's id
     * 
     * @param object
     * 
     * @param preferringNotFoundException
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return a facade for the object's id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade forObjectId(
        Object object,
        boolean preferringNotFoundException
    ) throws ResourceException {
        return ReducedJDOHelper.isPersistent(object) ? Query_2Facade.newInstance(
            (Path)ReducedJDOHelper.getObjectId(object),
            preferringNotFoundException
         ) : Query_2Facade.newInstance(
             (UUID)ReducedJDOHelper.getTransactionalObjectId(object),
            preferringNotFoundException
         );
    }

    /**
     * Create a query facade for the object's id
     * 
     * @param object
     * 
     * @param preferringNotFoundException
     * 
     * @return a facade for the object's id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade forObjectId(
        Object object
    ) throws ResourceException {
        return forObjectId(object, false);
    }
        
    /**
     * Test whether the given record is an object facade delegate
     * 
     * @param record the record to be tested
     * 
     * @return <code>true</code> if the given record is an object facade delegate
     */
    public static boolean isDelegate(
        Record record
    ){
        return QueryRecord.NAME.equals(record.getRecordName());
    }
    
    
    /**
     * Retrieve resourceIdentifier.
     *
     * @return Returns the resourceIdentifier.
     */
    public final Path getPath(
    ) {
        return this.delegate.getPath();
    }
    
    /**
     * Set resourceIdentifier.
     * 
     * @param path The resourceIdentifier to set.
     */
    public final void setPath(
        Path path
    ) {
        this.delegate.setPath(path);
    }
    
    /**
     * Retrieve parameters.
     *
     * @return Returns the parameters.
     * 
     * @throws ResourceException 
     */
    public final Record getParameters(
    ) throws ResourceException {
        return this.delegate.getParameters();
    }
    
    /**
     * Set parameters.
     * 
     * @param parameters The parameters to set.
     * 
     * @throws ResourceException 
     */
    @SuppressWarnings("unchecked")
    public final void setParameters(
        Object parameters
    ) throws ResourceException {
        if (parameters instanceof MappedRecord || parameters instanceof IndexedRecord) {
            this.delegate.setParameters((Record) parameters);
        } else if (parameters instanceof List<?>) {
            IndexedRecord jcaParameters = Records.getRecordFactory().createIndexedRecord("list");
            jcaParameters.addAll((List<?>)parameters);
            this.delegate.setParameters(jcaParameters);
        } else if (parameters instanceof Map<?,?>) {
            MappedRecord jcaParameters = Records.getRecordFactory().createMappedRecord("map");
            jcaParameters.putAll((Map<?,?>)parameters);
            this.delegate.setParameters(jcaParameters);
        } else if(parameters instanceof Object[]) {
            this.delegate.setParameters(
                Records.getRecordFactory().asIndexedRecord("list",null,parameters)
            );
        } else throw BasicException.initHolder(
            new ResourceException(
                "Unexpected parameters class",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("expected", List.class.getName(), Map.class.getName(), Object[].class.getName()),
                    new BasicException.Parameter("actual", parameters == null ? null : parameters.getClass().getName())
                )
            )
        );
    }

    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
    public final String getQueryType() {
        return this.delegate.getQueryType();
    }

    
    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
    public final void setQueryType(String queryType) {
        this.delegate.setQueryType(queryType);
    }

    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    public final String getQuery() {
        return this.delegate.getQuery();
    }

    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    public final void setQuery(String query) {
        this.delegate.setQuery(query);
    }

    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
    public final Long getPosition() {
        return this.delegate.getPosition();
    }

    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
    public final void setPosition(Number position) {
        this.delegate.setPosition(
            position == null || position instanceof Long ? (Long)position : Long.valueOf(position.longValue())
        );
    }

    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
    public final Long getSize() {
        return this.delegate.getSize();
    }

    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
    public final void setSize(Number size) {
        this.delegate.setSize(
            size == null || size instanceof Long ? (Long)size : Long.valueOf(size.longValue())
        );
    }

    /**
     * Retrieve the fetch groups
     * 
     * @return the fetch groups
     */
    public final Set<String> getGroups(){
        return this.delegate.getGroups();
    }

    /**
     * Set the fetch groups
     * 
     * @throws ResourceException 
     */
    public final void setGroups(
        Set<String> groups
    ){
        this.delegate.setGroups(groups);
    }

    /**
     * @return
     * @see org.openmdx.base.rest.cci.QueryRecord#getFeatures()
     */
    public Set<String> getFeatures() {
        return this.delegate.getFeatures();
    }

    /**
     * @param features
     * @see org.openmdx.base.rest.cci.QueryRecord#setFeatures(java.util.Set)
     */
    public void setFeatures(Set<String> features) {
        this.delegate.setFeatures(features);
    }

    /**
     * Tells whether the object shall be refreshed before answering the query.
     * 
     * @param refresh <code>true</code> if the object shall be refreshed before 
     * answering the query
     * 
     * @see org.openmdx.base.rest.cci.QueryRecord#setRefresh(boolean)
     */
    public void setRefresh(boolean refresh) {
        this.delegate.setRefresh(refresh);
    }

    /**
     * Tells whether the object shall be refreshed before answering the query.
     * 
     * @return <code>true</code> if the object shall be refreshed before 
     * answering the query
     * 
     * @see org.openmdx.base.rest.cci.QueryRecord#isRefresh()
     */
    public boolean isRefresh() {
        return this.delegate.isRefresh();
    }

    /**
     * Tells whether a NOT_FOUND exception shall be thrown rather than 
     * returning an empty result set in case a requested object does not exist.
     * 
     * @return <code>true</code> if a NOT_FOUND exception shall be thrown when 
     * the requested object does not exist
     */
    public boolean isPreferringNotFoundException(){
        return this.preferringNotFoundException;
    }
    
    /**
     * Tells whether a collection of objects or a single object shall be retrieved
     * 
     * @return <code>true</code> if a collection of objects shall be retrieved
     */
    public boolean isFindRequest(
    ){
        Path xri = getPath();
        return xri.size() % 2 == 0 || xri.containsWildcard();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.delegate.toString();
    }

}
