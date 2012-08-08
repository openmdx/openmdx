/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: QueryRecord.java,v 1.3 2010/06/02 13:45:10 hburger Exp $
 * Description: Query Record 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:45:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
    private static final long serialVersionUID = -1745943351805409146L;

    /**
     * Alphabetically ordered keys
     */
    private static final String[] KEYS = {
        "group",
        "groups",
        "parameters",
        "path",
        "position",
        "query",
        "queryType",
        "size"
    };

    /**
     * The default fetch groups
     */
    private static final String[] DEFAULT_GROUPS = {
        FetchPlan.DEFAULT
    };

    /**
     * The fields included in the active fetch groups
     */
    private String[] group;
    
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
     * Retrieve group.
     *
     * @return Returns the group.
     */
    public Set<String> getGroup() {
        return Sets.asSet(this.group);
    }
    
    /**
     * Set group.
     * 
     * @param group The group to set.
     */
    public void setGroup(Set<String> group) {
        this.group = toArray(group);
    }

    
    /**
     * Retrieve groups.
     *
     * @return Returns the groups.
     */
    public Set<String> getGroups() {
        return Sets.asSet(this.groups);
    }
    
    /**
     * Set groups.
     * 
     * @param groups The groups to set.
     */
    public void setGroups(Set<String> groups) {
        this.groups = toArray(groups);
    }
    
    /**
     * Retrieve path.
     *
     * @return Returns the path.
     */
    public Path getPath() {
        return this.path;
    }
    
    /**
     * Set path.
     * 
     * @param path The path to set.
     */
    public void setPath(Path path) {
        this.path = path;
    }
    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
    public Long getPosition() {
        return this.position;
    }
    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
    public void setPosition(Long position) {
        this.position = position;
    }
    
    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    public String getQuery() {
        return this.query;
    }
    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
    public String getQueryType() {
        return this.queryType;
    }

    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
    
    /**
     * Retrieve parameters.
     *
     * @return Returns the parameters.
     */
    public Record getParameters() {
        return this.parameters;
    }
    
    /**
     * Set parameters.
     * 
     * @param parameters The parameters to set.
     */
    public void setParameters(Record parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
    public Long getSize() {
        return this.size;
    }
    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
    public void setSize(Long size) {
        this.size = size;
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
            case 0: return asSet(this.group);
            case 1: return asSet(this.groups);
            case 2: return this.parameters;
            case 3: return this.path;
            case 4: return this.position;
            case 5: return this.query;
            case 6: return this.queryType;
            case 7: return this.size;
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
                this.group = toArray(value);
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
                this.size = toLong(value);
                break;
        }
    }
    
}
