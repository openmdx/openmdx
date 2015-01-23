/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: DataproviderReply class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.ResultRecord;

/**
 * Dataprovider Reply
 */
public class DataproviderReply implements Serializable {

    /**
     * Constructor 
     *
     * @throws ServiceException
     */
    public DataproviderReply(
    ) throws ServiceException {
        this(false);
    }
    
    /**
     * Constructor 
     *
     * @throws ServiceException
     */
    public DataproviderReply(
        boolean message
    ) throws ServiceException {
        try {
            this.delegate = message ? 
                Records.getRecordFactory().createMappedRecord(MessageRecord.class) :
                Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
        } catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }
    
    
    /**
     * Constructor 
     *
     * @param that
     */
    private DataproviderReply(
        DataproviderReply that
    ){
        this.delegate = that.delegate;
    }
        
    /**
     * Constructor 
     *
     * @param delegate
     * @param clear
     */
    public DataproviderReply(
        ResultRecord delegate,
        boolean clear
    ) {
        if(clear) {
            delegate.clear();
        }
        this.delegate = delegate;
    }

    /**
     * Constructor 
     *
     * @param delegate
     * @param clear
     */
    public DataproviderReply(
        MappedRecord delegate,
        boolean clear
    ) {
        if(clear) {
            delegate.clear();
        }
        this.delegate = delegate;
    }
    
    /**
     * Constructor 
     *
     * @param object
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public DataproviderReply(
        MappedRecord object
    ) throws ServiceException {
        this();
        ((ResultRecord)this.delegate).add(object);
    }

    /**
     * Constructor 
     *
     * @param objects
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public DataproviderReply(
        List<MappedRecord> objects
    ) throws ServiceException {
        this();
        ((ResultRecord)this.delegate).addAll(objects);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4120848863151075378L;

    /**
     * The delegate
     */
    private final Record delegate;

    @SuppressWarnings("unchecked")
    public MappedRecord[] getObjects(
    ) {
        ResultRecord delegate = (ResultRecord) this.delegate;
        return (MappedRecord[])delegate.toArray(
            new MappedRecord[delegate.size()]
        );
    }

    public ObjectRecord getObject(
    ){
        return (ObjectRecord)((ResultRecord)this.delegate).get(0);
    }
                
    @Override
    public Object clone(
    ){
        return new DataproviderReply(this);
    }

    public void setTotal(
        long total
    ) {
        ((ResultRecord)this.delegate).setTotal(total);
    }
    
    public void setHasMore(
        boolean hasMore
    ) {
        ((ResultRecord)this.delegate).setHasMore(hasMore);
    }
    
    public Long getTotal(
    ) {
        return ((ResultRecord)this.delegate).getTotal();
    }
    
    public Boolean getHasMore(
    ) {
        return ((ResultRecord)this.delegate).getHasMore();
    }
    
    public IndexedRecord getResult(
    ) {
        return (IndexedRecord) this.delegate;
    }

    public MessageRecord getResponse(
    ){
        return (MessageRecord) this.delegate;
    }

}
