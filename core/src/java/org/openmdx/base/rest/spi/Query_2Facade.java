/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Query_2Facade.java,v 1.1 2009/06/01 15:43:32 wfro Exp $
 * Description: Query Facade
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:43:32 $
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
package org.openmdx.base.rest.spi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;

/**
 * Query Facade
 */
public class Query_2Facade {

    /**
     * Constructor 
     *
     * @param record
     */
    private Query_2Facade(
        MappedRecord delegate
    ) throws ResourceException {
        if(!isDelegate(delegate)) throw BasicException.initHolder(
            new ResourceException(
                "Query name should be the candidate class' fully qualified MOF identifier amendet by the suffix \"Query\"",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_QUERY_CRITERIA,
                    new BasicException.Parameter("actual", delegate.getRecordName())
                )
            )
        );
        this.delegate = delegate;
    }
    
    /**
     * Constructor 
     *
     * @param queryType the candidate class' fully MOF identifier amended by the suffix "Query"
     * 
     * @throws ResourceException 
     */
    private Query_2Facade(
    ) throws ResourceException {
        this(
            Records.getRecordFactory().asMappedRecord(
                "org:openmdx:kernel:Query",
                null,
                MEMBERS,
                new Object[MEMBERS.length]
            )
        );
    }

    /**
     * The data object members
     */
    private static final String[] MEMBERS = {
        "path",
        "queryType",
        "query",
        "position",
        "size",
        "parameters"
    };    
    
    /**
     * The query record
     */
    private final MappedRecord delegate;

    /**
     * Retrieve delegate.
     *
     * @return Returns the delegate.
     */
    public final MappedRecord getDelegate() {
        return this.delegate;
    }

    /**
     * Create a facade for the given record
     * 
     * @param record
     * 
     * @return the query facade
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
        MappedRecord record
    ) throws ResourceException {
        return new Query_2Facade(record);
    }
    
    /**
     * Create a facade for the given query type
     * 
     * @param queryType the candidate class' fully MOF identifier amendet by the suffix "Query"
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newInstance(
    ) throws ResourceException {
        return new Query_2Facade();
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
        return "org:openmdx:kernel:Query".equals(record.getRecordName());
    }
    
    
    /**
     * Retrieve resourceIdentifier.
     *
     * @return Returns the resourceIdentifier.
     */
    public final Path getPath() {
        return new Path((String)this.delegate.get("path"));
    }

    
    /**
     * Set resourceIdentifier.
     * 
     * @param path The resourceIdentifier to set.
     */
    @SuppressWarnings("unchecked")
    public final void setPath(Path path) {
        this.delegate.put("path", path.toXRI());
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
        return (Record) this.delegate.get("parameters");
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
        Record jcaParameters;
        if (parameters instanceof List<?>) {
            jcaParameters = Records.getRecordFactory().createIndexedRecord(
                "list",
                "Query Parameters",
                (List<?>)parameters
            );
        } else if (parameters instanceof Map<?,?>) {
            jcaParameters = Records.getRecordFactory().createMappedRecord(
                "map",
                "Query Parameters"
            );
            ((MappedRecord)jcaParameters).putAll(
                (Map<?,?>)parameters
            );
        } else if(parameters instanceof Object[]) {
            jcaParameters = Records.getRecordFactory().createIndexedRecord(
                "list",
                "Query Parameters",
                Arrays.asList(parameters)
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
        this.delegate.put(
            "parameters", 
            jcaParameters
        );
    }

    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
    public final String getQueryType() {
        return (String) this.delegate.get("queryType");
    }

    
    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
    @SuppressWarnings("unchecked")
    public final void setQueryType(String queryType) {
        this.delegate.put("queryType", queryType);
    }

    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    public final String getQuery() {
        return (String) this.delegate.get("query");
    }

    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    @SuppressWarnings("unchecked")
    public final void setQuery(String query) {
        this.delegate.put("query", query);
    }

    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
    public final Number getPosition() {
        return (Number) this.delegate.get("position");
    }

    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
    @SuppressWarnings("unchecked")
    public final void setPosition(Number position) {
        this.delegate.put("position", position);
    }

    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
    public final Number getSize() {
        return (Number) this.delegate.get("size");
    }

    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
    @SuppressWarnings("unchecked")
    public final void setSize(Number size) {
        this.delegate.put("size", size);
    }

    
}
