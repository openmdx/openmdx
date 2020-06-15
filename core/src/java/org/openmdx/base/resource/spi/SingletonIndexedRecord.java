/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Singleton Indexed Record 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.resource.spi;

import java.util.AbstractList;

import javax.resource.cci.IndexedRecord;

/**
 * Singleton Indexed Record
 */
@Deprecated
@SuppressWarnings("rawtypes")
final class SingletonIndexedRecord
    extends AbstractList
    implements IndexedRecord 
{

    /**
     * Constructor 
     */
    SingletonIndexedRecord(
        String name,
        String description,
        Object value
    ) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    /**
     * Constructor 
     */
    protected SingletonIndexedRecord(
    ) {
        // for de-serialization
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -4379113944913582714L;

    /**
     * The MOF id
     */
    private String name;
    
    /**
     * An optional short description
     */
    private String description;
    
    /**
     * The single entry's vale
     */
    protected Object value;

    /**
     * Validate the index 
     * 
     * @param index the index to be validated
     */
    private void validate(
        int index
    ){
        if(index != 0) throw new IndexOutOfBoundsException(
            "There is a single entry with index 0: " + index
        );
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public Object get(int index) {
        validate(index);
        return this.value;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    @Override
    public Object set(int index, Object element) {
        validate(index);
        Object old = this.value;
        this.value = element;
        return old;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return 1;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    public String getRecordName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    public String getRecordShortDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    public void setRecordName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    public void setRecordShortDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#clone()
     */
    @Override
    public Object clone(
    ) throws CloneNotSupportedException {
        return new SingletonIndexedRecord(
            this.name,
            this.description,
            this.value
        );
    }

}
