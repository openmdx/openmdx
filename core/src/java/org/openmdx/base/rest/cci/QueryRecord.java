/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Record
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.base.rest.cci;

import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.naming.Path;

/**
 * <code>org::openmdx::kernel::Query</code>
 */
public interface QueryRecord extends RequestRecord {
    
    /**
     * An object record's name
     */
    String NAME = "org:openmdx:kernel:Query";
    
    /**
     * Retrieve the fetch group name
     *
     * @return Returns the fetch group name
     */
    String getFetchGroupName();
    
    /**
     * Set the fetch group names
     * 
     * @param fetchGroupName The fetch group name
     */
    void setFetchGroupName(String fetchGroupName);
    
    /**
     * Retrieve the explicitly requested features
     *
     * @return Returns the set of explicitly requested features
     */
    Set<String> getFeatureName();
    
    /**
     * Set the names of the explicitly requested features
     * 
     * @param featureNames Names of the explicitly requested features
     */
    void setFeatureName(Set<String> featureNames);
    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
    Long getPosition();
    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
    void setPosition(Long position);
    
    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    QueryFilterRecord getQueryFilter();
    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    void setQueryFilter(QueryFilterRecord query);
    
    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
    String getQueryType();

    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
    void setQueryType(String queryType);
    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
    Long getSize();
    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
    void setSize(Long size);

    /**
     * Tells whether the object shall be refreshed before answering the query.
     * 
     * @param refresh <code>true</code> if the object shall be refreshed before 
     * answering the query
     */
    void setRefresh(
        boolean refresh
    );

    /**
     * Tells whether the object shall be refreshed before answering the query.
     * 
     * @return <code>true</code> if the object shall be refreshed before 
     * answering the query
     */
    boolean isRefresh();

    /**
     * Set the openMDX Query Language statements to be applied to the query
     * 
     * @param query the openMDX Query Language statements to be applied to the query
     */
    void setQuery(String query);
    
    /**
     * Retrieve the openMDX Query Language statements to be applied to the query
     * 
     * @return the openMDX Query Language statements to be applied to the query
     */
    String getQuery();
    
    /**
     * Deep clone
     * 
     * @return a clone of this record
     */
    QueryRecord clone();
    
    enum Member {
        featureName,
    	fetchGroupName,
    	position,
    	query,
    	queryFilter,
    	queryType,
        refresh,
        resourceIdentifier,
    	size
    }

}
