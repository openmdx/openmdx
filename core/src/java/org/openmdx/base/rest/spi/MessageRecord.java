/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: MessageRecord.java,v 1.5 2010/06/02 13:45:10 hburger Exp $
 * Description: ObjectRecord 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:45:10 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;


/**
 * Object Record
 */
public class MessageRecord 
    extends AbstractMappedRecord
    implements org.openmdx.base.rest.cci.MessageRecord 
{
    
    /**
     * Constructor 
     */
    public MessageRecord() {
        super(KEYS);
    }

    /**
     * Constructor 
     *
     * @param that
     */
    private MessageRecord(
        Map<?,?> that
    ){
        super(KEYS);
        putAll(that);
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6030385859226659109L;

    /**
     * Alphabetically ordered keys
     */
    private static final String[] KEYS = {
        "body",
        "path"
    };

    /**
     * The <code>"path"</code> entry
     */
    private Path path;
    
    /**
     * The <code>"body"</code> entry
     */
    private MappedRecord body;

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public MessageRecord clone(
    ){
        return new MessageRecord(this);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getBody()
     */
//  @Override
    public MappedRecord getBody() {
        return this.body;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getDestination()
     */
//  @Override
    public Path getPath() {
        return this.path;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#setBody(javax.resource.cci.MappedRecord)
     */
//  @Override
    public void setBody(MappedRecord body) {
        if(body != null) {
            this.body = body;
        } else try {
            this.body = Records.getRecordFactory().createMappedRecord("org:openmdx:base:Void");
        } catch (ResourceException exception) {
            throw new RuntimeException(exception);
        } 
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#setDestination(org.openmdx.base.naming.Path)
     */
//  @Override
    public void setPath(Path destination) {
        this.path = destination;
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
            case 0: return this.body;
            case 1: return this.path;
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
                this.body = (MappedRecord) value;
                break;
            case 1: 
                this.path = (Path) value;
                break;
        }
    }

    /**
     * Tells whether the message id is set
     * 
     * @return <code>true</code> if the message id is set
     */
    private final boolean hasId(){
        return this.path != null && this.path.size() % 2 == 1;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getTarget()
     */
//  @Override
    public Path getTarget() {
        return this.hasId() ? this.path.getParent() : this.path;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getMessageId()
     */
//  @Override
    public String getMessageId() {
        return this.hasId() ? this.path.getBase() : null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getCorellationId()
     */
//  @Override
    public String getCorellationId() {
        String messageId = getMessageId();
        int d = Math.max(
            messageId.lastIndexOf('!'), 
            messageId.lastIndexOf('*')
        );
        return d < 0 ? null : messageId.substring(0, d);
    }
    
}
