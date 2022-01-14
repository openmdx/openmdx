/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Facade
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
package org.openmdx.base.rest.spi;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.text.conversion.JavaBeans;
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
        if(!org.openmdx.base.rest.spi.QueryRecord.isCompatible(delegate)) {
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
        this.delegate = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
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
     * Retrieve resourceIdentifier.
     *
     * @return Returns the resourceIdentifier.
     */
    public final Path getPath(
    ) {
        return this.delegate.getResourceIdentifier();
    }
    
    /**
     * Set resourceIdentifier.
     * 
     * @param path The resourceIdentifier to set.
     */
    public final void setPath(
        Path path
    ) {
        this.delegate.setResourceIdentifier(path);
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
     * Set query.
     * 
     * @param query The query to set.
     * 
     * @deprecated use {@link #setQueryFilter(QueryFilterRecord)} or {@link #setExtension(String, Object)}
     */
    @Deprecated
    public final void setQuery(String query) {
        if(query == null) {
            this.delegate.setQuery(null);
            this.delegate.setQueryFilter(null);
        } else if (query.startsWith("<?xml)")){
            this.delegate.setQuery(null);
            try {
                this.delegate.setQueryFilter(
                    (QueryFilterRecord)JavaBeans.fromXML(query)
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        } else {
            this.delegate.setQuery(query);
            this.delegate.setQueryFilter(null);
        }
    }

    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    public final QueryFilterRecord getQueryFilter() {
        return this.delegate.getQueryFilter();
    }

    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    public final void setQueryFilter(QueryFilterRecord queryFilter) {
        this.delegate.setQueryFilter(queryFilter);
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
        String fetchGroupName = this.delegate.getFetchGroupName();
        return fetchGroupName == null ? Collections.<String>emptySet() : Collections.singleton(fetchGroupName);
    }

    /**
     * Set the fetch groups
     * 
     * @throws ResourceException 
     * 
     * @deprecated use {@link #setFetchGroupName(String)}
     */
    @Deprecated
    public final void setGroups(
        Set<String> groups
    ){
        if(groups == null) {
            setFetchGroupName(null);
        } else {
            switch(groups.size()) {
                case 0: 
                    setFetchGroupName(null);
                    break;
                case 1: 
                    setFetchGroupName(groups.iterator().next());
                    break;
                default: 
                	throw new RuntimeServiceException(
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.NOT_SUPPORTED,
	                    "At most one fetch group may be specified",
	                    new BasicException.Parameter("groups", groups)
	                );
            }
        }
    }
    
    /**
     * Set the fetch group name
     * 
     * @param fetchGroupName the fetch group name
     * 
     * @throws ResourceException 
     */
    public final void setFetchGroupName(
        String fetchGroupName
    ){
        this.delegate.setFetchGroupName(fetchGroupName);
    }
    

    public Set<String> getFeatures() {
        return this.delegate.getFeatureName();
    }

    /**
     * @param features
     * @see org.openmdx.base.rest.cci.QueryRecord#setFeatures(java.util.Set)
     */
    public void setFeatures(Set<String> featureNames) {
        this.delegate.setFeatureName(featureNames);
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
    	return isFindRequest(getPath());
    }
    
    /**
     * Tells whether a collection of objects or a single object shall be retrieved
     * 
     * @return <code>true</code> if a collection of objects shall be retrieved
     */
    public static boolean isFindRequest(
    	Path xri	
    ){
        return xri.isContainerPath() || xri.isPattern();
    }
    
    /**
     * Tells whether refresh is required before answering the query
     * 
     * @return <code>true</code> if refresh is required before answering the query
     */
    public boolean isRefresh(){
        return this.delegate.isRefresh();
    }
    
    /**
     * Tells whether the object shall be refreshed before answering the query.
     * 
     * @param refresh <code>true</code> if the object shall be refreshed before 
     * answering the query
     * @throws ResourceException 
     */
    public void setRefresh(
        boolean refresh
    ) throws ResourceException {
        this.delegate.setRefresh(refresh);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.delegate.toString();
    }

}
