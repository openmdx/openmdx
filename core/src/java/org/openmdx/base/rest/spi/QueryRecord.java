/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Record 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2011, OMEX AG, Switzerland
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

import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.resource.cci.Record;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.naming.Path;


/**
 * Query Record
 */
public class QueryRecord 
    extends AbstractMappedRecord
    implements org.openmdx.base.rest.cci.QueryRecord 
{

    /**
     * Constructor 
     *
     * @param keys
     */
    public QueryRecord() {
        super(KEYS);
    }

    /**
     * Constructor 
     *
     * @param that
     */
    QueryRecord(
        Map<?,?> that
    ){
        super(KEYS);
        putAll(that);
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -4816775342123136224L;

    /**
     * Alphabetically ordered keys
     */
    private static final String[] KEYS = {
        "features",
        "groups",
        "parameters",
        "path",
        "position",
        "query",
        "queryType",
        "refresh",
        "size"
    };

    /**
     * No explicitly requested features
     */
    private static final String[] NO_FEATURES = {
    };
    
    /**
     * The default fetch groups
     */
    private static final String[] DEFAULT_GROUPS = {
        FetchPlan.DEFAULT
    };

    /**
     * The explicitly requested features
     */
    private String[] features = NO_FEATURES;
    
    /**
     * The fetch groups
     */
    private String[] groups = DEFAULT_GROUPS;
    
    /**
     * 
     */
    private Path path;
    
    /**
     * 
     */
    private Long position;
    
    /**
     * 
     */
    private String query;
    
    /**
     * 
     */
    private String queryType;
    
    /**
     * 
     */
    private Record parameters;
    
    /**
     * 
     */
    private Long size;    
    
    /**
     * Tells whether the object shall be refreshed before answering the query
     */
    private Boolean refresh;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#getFeatures()
     */
//  @Override
    public Set<String> getFeatures() {
        return Sets.asSet(this.features);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#setFeatures(java.util.Set)
     */
//  @Override
    public void setFeatures(Set<String> features) {
        this.features = toArray(features);
    }

    /**
     * Retrieve groups.
     *
     * @return Returns the groups.
     */
//  @Override
    public Set<String> getGroups() {
        return Sets.asSet(this.groups);
    }
    
    /**
     * Set groups.
     * 
     * @param groups The groups to set.
     */
//  @Override
    public void setGroups(Set<String> groups) {
        this.groups = toArray(groups);
    }
    
    /**
     * Retrieve path.
     *
     * @return Returns the path.
     */
//  @Override
    public Path getPath() {
        return this.path;
    }
    
    /**
     * Set path.
     * 
     * @param path The path to set.
     */
//  @Override
    public void setPath(Path path) {
        this.path = path;
    }
    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
//  @Override
    public Long getPosition() {
        return this.position;
    }
    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
//  @Override
    public void setPosition(Long position) {
        this.position = position;
    }
    
    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
//  @Override
    public String getQuery() {
        return this.query;
    }
    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
//  @Override
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
//  @Override
    public String getQueryType() {
        return this.queryType;
    }

    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
//  @Override
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    
    /**
     * Retrieve parameters.
     *
     * @return Returns the parameters.
     */
//  @Override
    public Record getParameters() {
        return this.parameters;
    }
    
    /**
     * Set parameters.
     * 
     * @param parameters The parameters to set.
     */
//  @Override
    public void setParameters(Record parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
//  @Override
    public Long getSize() {
        return this.size;
    }
    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
//  @Override
    public void setSize(Long size) {
        this.size = size;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#setRefresh(boolean)
     */
//  @Override
    public void setRefresh(boolean refresh) {
        this.refresh = Boolean.valueOf(refresh);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.QueryRecord#isRefresh()
     */
//  @Override
    public boolean isRefresh() {
        return Boolean.TRUE.equals(this.refresh);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public Object clone(
    ){
        return new QueryRecord(this);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
//  @Override
    public String getRecordName() {
        return NAME;
    }

    /**
     * Retrieve a value by index
     * 
     * @param index the index
     * @return the value
     */
    @Override
    protected Object get(
        int index
    ){
        switch(index) {
            case 0: return asSet(this.features);
            case 1: return asSet(this.groups);
            case 2: return this.parameters;
            case 3: return this.path;
            case 4: return this.position;
            case 5: return this.query;
            case 6: return this.queryType;
            case 7: return this.refresh;
            case 8: return this.size;
            default: return null;
        }
    }

    /**
     * Set a value by index 
     * 
     * @param index the index
     * @param value the new value
     * 
     * @return the old value
     */
    @Override
    protected void put(
        int index,
        Object value
    ){
        switch(index) {
            case 0: 
                this.features = toArray(value);
                break;
            case 1: 
                this.groups = toArray(value);
                break;
            case 2:
                this.parameters = (Record) value;
                break;
            case 3:
                this.path = toPath(value);
                break;
            case 4:
                this.position = toLong(value);
                break;
            case 5:
                this.query = (String) value;
                break;
            case 6:
                this.queryType = (String) value;
                break;
            case 7: 
                this.refresh = toBoolean(value);
                break;
            case 8: 
                this.size = toLong(value);
                break;
        }
    }
    
}