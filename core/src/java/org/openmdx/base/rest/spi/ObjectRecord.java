/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Record 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2012, OMEX AG, Switzerland
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
import java.util.UUID;

import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.naming.Path;


/**
 * Object Record
 */
public class ObjectRecord 
    extends AbstractMappedRecord<org.openmdx.base.rest.cci.ObjectRecord.Member>
    implements org.openmdx.base.rest.cci.ObjectRecord 
{
    
    /**
     * Constructor 
     */
    public ObjectRecord() {
        super();
    }

    /**
     * Constructor 
     *
     * @param that
     */
    private ObjectRecord(
		ObjectRecord that
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
    private static final long serialVersionUID = 1436275899255948883L;

    /**
     * The <code>"resourceIdentifier"</code> entry
     */
    private Path resourceIdentifier;
    
    /**
     * The <code>"value"</code> entry
     */
    private MappedRecord value;

    /**
     * The <code>"version"</code> entry
     */
    private byte[] version;

    /**
     * The <code>"lock"</code> entry
     */
    private Object lock;
    
    /**
     * The object's transient id
     */
    private UUID transientObjectId;
    
    /* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		freeze(value);
	}

	/* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public ObjectRecord clone(
    ){
        return new ObjectRecord(this);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getPath()
     */
    @Override
    public Path getResourceIdentifier() {
        return this.resourceIdentifier;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getValue()
     */
    @Override
    public MappedRecord getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getVersion()
     */
    @Override
    public byte[] getVersion() {
        return this.version;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setPath(org.openmdx.base.naming.Path)
     */
    @Override
    public void setResourceIdentifier(Path path) {
    	assertMutability();
        this.resourceIdentifier = path;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setValue(javax.resource.cci.MappedRecord)
     */
    @Override
    public void setValue(MappedRecord value) {
    	assertMutability();
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setVersion(java.lang.Object)
     */
    @Override
    public void setVersion(byte[] version) {
        this.version = version;
    }

    /* (non-Javadoc)
	 * @see org.openmdx.base.rest.cci.ObjectRecord#getLock()
	 */
    @Override
	public Object getLock() {
		return this.lock;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.cci.ObjectRecord#setLock(java.lang.Object)
	 */
    @Override
	public void setLock(Object lock) {
		this.lock = lock;
	}

	/**
	 * @return the transientObjectId
	 */
    @Override
	public UUID getTransientObjectId() {
		return this.transientObjectId;
	}

	/**
	 * @param transientObjectId the transientObjectId to set
	 */
    @Override
	public void setTransientObjectId(UUID transientObjectId) {
		this.transientObjectId = transientObjectId;
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
            case lock: return getLock();
            case resourceIdentifier: return getResourceIdentifier();
            case transientObjectId: return getTransientObjectId();
            case value: return getValue();
            case version: return getVersion();
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
            case lock:
                setLock(value);
                break;
            case resourceIdentifier:
                setResourceIdentifier((Path) value);
                break;
            case transientObjectId:
            	setTransientObjectId((UUID)value);
            	break;
            case value: 
                setValue((MappedRecord) value);
                break;
            case version:
                setVersion((byte[])value);
                break;
            default:
            	super.put(index, value);
        }
    }

    /**
     * Null-safe comparison
     * 
     * @param thisValue
     * @param thatValue
     * 
     * @return <code>true</code> of both values are either equal or <code>null</code>
     */
    private static boolean areMatching(Object thisValue, Object thatValue) {
    	if(thisValue == null) {
    		return thatValue == null;
    	} else if (thisValue instanceof byte[]) {
    		return thatValue instanceof byte[] && Arrays.equals((byte[])thisValue, (byte[])thatValue);
    	} else {
    		return thisValue.equals(thatValue);
    	}
    }
    
    @Override
    public boolean equals(
        Object obj
    ) {
        if(obj instanceof ObjectRecord) {
            ObjectRecord that = (ObjectRecord)obj;
            return 
            	areMatching(this.resourceIdentifier, that.resourceIdentifier) &&
            	areMatching(this.value, that.value) &&
            	areMatching(this.version, that.version) &&
            	areMatching(this.lock, that.lock);
        } else {
            return false;
        }
    }

	@Override
	public int hashCode() {
		return this.resourceIdentifier == null ? 0 : this.resourceIdentifier.hashCode(); 
	}

	/**
	 * Tells whether the candidate is an object record
	 * 
	 * @param record the record to be inspected
	 * 
	 * @return <code>true</code> if the record's name equals to <code>org:openmdx:kernel:Object</code>.
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
