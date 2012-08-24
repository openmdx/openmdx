/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: SingletonMappedRecord 
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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

/**
 * SingletonMappedRecord
 */
@SuppressWarnings("rawtypes")
final class SingletonMappedRecord
    extends AbstractMap
    implements MappedRecord 
{
    
    /**
     * Constructor 
     *
     * @param name
     * @param description
     * @param key
     * @param value
     */
    SingletonMappedRecord(
        String name,
        String description,
        Object key,
        Object value) {
        this.name = name;
        this.description = description;
        this.key = key;
        this.value = value;
    }

    /**
     * Constructor 
     */
    protected SingletonMappedRecord(
    ) {
        // for de-serialization
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 11576609986427014L;

    /**
     * The MOF id
     */
    private String name;
    
    /**
     * An optional short description
     */
    private String description;
    
    /**
     * The single entry's key
     */
    protected Object key;

    /**
     * The single entry's vale
     */
    protected Object value;

    /**
     * 
     */
    private transient Set entries;
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    @Override
    public Set entrySet(
    ) {
        return this.entries == null ? this.entries = Collections.singleton(
            new Map.Entry(){

                public Object getKey() {
                    return SingletonMappedRecord.this.key;
                }

                public Object getValue() {
                    return SingletonMappedRecord.this.value;
                }

                public Object setValue(Object value) {
                    Object old = SingletonMappedRecord.this.value;
                    SingletonMappedRecord.this.value = value;
                    return old;
                }
                
            }
        ) : this.entries;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#size()
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
        return new SingletonMappedRecord(
            this.name,
            this.description,
            this.key,
            this.value
        );
    }

}
