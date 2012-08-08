/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ObjectRecord.java,v 1.6 2011/11/11 01:32:21 hburger Exp $
 * Description: ObjectRecord 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/11 01:32:21 $
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

import javax.resource.cci.MappedRecord;

import org.openmdx.base.naming.Path;


/**
 * Object Record
 */
public class ObjectRecord 
    extends AbstractMappedRecord
    implements org.openmdx.base.rest.cci.ObjectRecord 
{
    
    /**
     * Constructor 
     */
    public ObjectRecord() {
        super(KEYS);
    }

    /**
     * Constructor 
     *
     * @param that
     */
    private ObjectRecord(
        Map<?,?> that
    ){
        super(KEYS);
        putAll(that);
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -8775933562461890569L;

    /**
     * Alphabetically ordered keys
     */
    private static final String[] KEYS = {
        "path",
        "value",
        "version"
    };

    /**
     * The <code>"path"</code> entry
     */
    private Path path;
    
    /**
     * The <code>"value"</code> entry
     */
    private MappedRecord value;

    /**
     * The <code>"version"</code> entry
     */
    private Object version;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public Object clone(
    ){
        return new ObjectRecord(this);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getPath()
     */
//  @Override
    public Path getPath() {
        return this.path;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getValue()
     */
//  @Override
    public MappedRecord getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getVersion()
     */
//  @Override
    public Object getVersion() {
        return this.version;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setPath(org.openmdx.base.naming.Path)
     */
//  @Override
    public void setPath(Path path) {
        this.path = path;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setValue(javax.resource.cci.MappedRecord)
     */
//  @Override
    public void setValue(MappedRecord value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setVersion(java.lang.Object)
     */
//  @Override
    public void setVersion(Object version) {
        this.version = version;
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
            case 0: return this.path;
            case 1: return this.value;
            case 2: return this.version;
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
                this.path = (Path) value;
                break;
            case 1: 
                this.value = (MappedRecord) value;
                break;
            case 2:
                this.version = value;
                break;
        }
    }

    @Override
    public boolean equals(
        Object obj
    ) {
        if(obj instanceof ObjectRecord) {
            ObjectRecord that = (ObjectRecord)obj;
            return 
                this.path.equals(that.path) && 
                this.value.equals(that.value) && 
                (this.version == null ? that.version == null : this.version.equals(that.version));
        } else {
            return false;
        }
    }

	@Override
	public int hashCode() {
		return this.path == null ? 0 : this.path.hashCode(); 
	}
    
}
