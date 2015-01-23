/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Message Record 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2014, OMEX AG, Switzerland
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

import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.naming.Path;


/**
 * Object Record
 */
public class MessageRecord 
    extends AbstractMappedRecord<org.openmdx.base.rest.cci.MessageRecord.Member>
    implements org.openmdx.base.rest.cci.MessageRecord 
{
    
    /**
     * Constructor 
     */
    public MessageRecord() {
    	super();
    }

    /**
     * Constructor 
     *
     * @param that
     */
    private MessageRecord(
    	MessageRecord that
    ){
        super(that);
    }
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6030385859226659109L;

    /**
     * The <code>"path"</code> entry
     */
    private Path resourceIdentifier;
    
    /**
     * The <code>"body"</code> entry
     */
    private MappedRecord body = VoidRecord.getInstance();

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		freeze(this.body);
	};
    
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
    @Override
    public MappedRecord getBody() {
        return this.body;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getDestination()
     */
    @Override
    public Path getResourceIdentifier() {
        return this.resourceIdentifier;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#setBody(javax.resource.cci.MappedRecord)
     */
    @Override
    public void setBody(MappedRecord body) {
    	assertMutability();
        this.body = body == null ? VoidRecord.getInstance() : body;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#setDestination(org.openmdx.base.naming.Path)
     */
    @Override
    public void setResourceIdentifier(Path destination) {
    	assertMutability();
        this.resourceIdentifier = destination;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
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
        Member index
    ){
        switch(index) {
            case body: return getBody();
            case resourceIdentifier: return getResourceIdentifier();
            default: return super.get(index);
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
        Member index,
        Object value
    ){
        switch(index) {
            case body:
                setBody((MappedRecord) value);
                break;
            case resourceIdentifier: 
                setResourceIdentifier((Path) value);
                break;
            default:
            	super.put(index, value);
        }
    }

    /**
     * Tells whether the message id is set
     * 
     * @return <code>true</code> if the message id is set
     */
    private final boolean hasId(){
        return this.resourceIdentifier != null && this.resourceIdentifier.isObjectPath();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getTarget()
     */
    @Override
    public Path getTarget() {
        return this.hasId() ? this.resourceIdentifier.getParent() : this.resourceIdentifier;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getMessageId()
     */
    @Override
    public String getMessageId() {
        return this.hasId() ? this.resourceIdentifier.getLastSegment().toClassicRepresentation() : null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.MessageRecord#getCorellationId()
     */
    @Override
    public String getCorellationId() {
        final String messageId = getMessageId();
        if(messageId != null) {
            int d = Math.max(
                messageId.lastIndexOf('!'), 
                messageId.lastIndexOf('*')
            );
            if(d > 0){
                return messageId.substring(0, d);
            }
        } 
        return null; 
    }
    
    /**
     * Tells whether the candidate is a message record
     * 
     * @param record the record to be inspected
     * 
     * @return <code>true</code> if the record's name equals to <code>org:openmdx:kernel:Message</code>.
     */
    public static boolean isCompatible(Record record) {
        return NAME.equals(record.getRecordName());
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
	 */
	@Override
	protected Members<Member> members() {
		return MEMBERS;
	}

}
