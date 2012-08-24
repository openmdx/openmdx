/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Request Message
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.kernel.exception.BasicException;


/**
 * An operation response listener
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class RequestedMessage
    implements DataproviderReplyListener, Serializable, MessageRecord
{

    protected MessageRecord getMessage(
    ) {
        if (this.message == null) {
            throw new RuntimeServiceException(
                this.exception == null ?
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.ILLEGAL_STATE,
                        "The corresponding request has not been processed yet"
                    ) :
                    this.exception
            );
        }
        return this.message;
    }

    //------------------------------------------------------------------------
    public ServiceException getException(
    ) {
        return this.exception;
    }
    
    //------------------------------------------------------------------------
    // Implements DataproviderReplyListener
    //------------------------------------------------------------------------

    /**
     * Called if the work unit has been processed successfully
     */
    public void onReply(
        DataproviderReply reply
    ){
        this.message = reply.getResponse();
        this.exception = null;
    }

    /**
     * Called if the work unit processing failed
     */
    public void onException(
        ServiceException exception
    ){
        this.message = null;
        this.exception = exception;
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the requested dataprovider object's string representation
     */
    @Override
    public String toString(
    ){
        return this.message != null ? this.message.toString() :
            this.exception != null ? this.exception.toString() :
                "n/a";
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6070277313698787163L;
    /**
     * The message if the request succeeds; null otherwise.
     */
    protected MessageRecord message = null;

    /**
     * The exception if the request fails; null otherwise.
     */
    protected ServiceException exception = null;

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
//  @Override
    public String getRecordName(
    ) {
        return this.getMessage().getRecordName();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
//  @Override
    public String getRecordShortDescription() {
        return this.getMessage().getRecordShortDescription();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
//  @Override
    public void setRecordName(String name) {
        this.getMessage().setRecordName(name);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
//  @Override
    public void setRecordShortDescription(String description) {
        this.getMessage().setRecordShortDescription(description);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
//  @Override
    public void clear() {
       this.getMessage().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
//  @Override
    public boolean containsKey(Object key) {
        return this.getMessage().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
//  @Override
    public boolean containsValue(Object value) {
        return this.getMessage().containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
//  @Override
    public Set entrySet() {
        return this.getMessage().entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
//  @Override
    public Object get(Object key) {
        return this.getMessage().get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
//  @Override
    public boolean isEmpty() {
        return this.getMessage().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
//  @Override
    public Set keySet() {
        return this.getMessage().keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
//  @Override
    public Object put(Object key, Object value) {
        return this.getMessage().put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
//  @Override
    public void putAll(Map t) {
        this.getMessage().putAll(t);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
//  @Override
    public Object remove(Object key) {
        return this.getMessage().remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
//  @Override
    public int size() {
        return this.getMessage().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
//  @Override
    public Collection values() {
        return this.getMessage().values();
    }

    @Override
    public MessageRecord clone(
    ) throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getPath()
     */
//  @Override
    public Path getPath() {
        return this.getMessage().getPath();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setPath(org.openmdx.base.naming.Path)
     */
//  @Override
    public void setPath(Path path) {
        this.getMessage().setPath(path);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getBody()
     */
//  @Override
    public MappedRecord getBody() {
        return this.getMessage().getBody();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getMessageId()
     */
//  @Override
    public String getMessageId() {
        return this.getMessage().getMessageId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getTarget()
     */
//  @Override
    public Path getTarget() {
        return this.getMessage().getTarget();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#setBody(javax.resource.cci.MappedRecord)
     */
//  @Override
    public void setBody(MappedRecord body) {
        this.getMessage().setBody(body);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getCorellationId()
     */
//  @Override
    public String getCorellationId() {
        return getMessage().getCorellationId();
    }

}
