/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JCA: IndexedRecord backed-up by an optional object
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations 
 * as listed in the NOTICE file.
 */
package org.openmdx.base.resource.spi;

import java.util.AbstractList;

import javax.resource.cci.IndexedRecord;

import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * An IndexedRecord of size 0 or 1 backed-up by an optional value
 */
@SuppressWarnings("rawtypes")
public final class TinyIndexedRecord 
	extends AbstractList
	implements IndexedRecord, MultiLineStringRepresentation, Freezable
{

    /**
	 * Creates an IndexedRecord with the specified name and the given content.  
	 * <p>
	 * This constructor does not declare any exceptions as it assumes that the
	 * necessary checks are made by the record factory.
	 *
	 * @param     recordName
	 *            The name of the record acts as a pointer to the meta 
	 *            information (stored in the metadata repository) for a
	 *            specific record type. 
	 * @param     recordShortDescription
	 *            The short description of the Record; or null.
	 * @param     values
	 *            The values of the indexed record.
	 */
	TinyIndexedRecord(
		String recordName,
		String recordShortDescription,
		Object value
	){
		this.name = recordName;
		this.shortDescription = recordShortDescription;
		this.value = value;
	}

    /**
     * The record's name
     */
    private String name;    

    /**
     * The record's short description
     */
    private String shortDescription;

    /**
     * If <code>null</code> the list is empty
     */
    private Object value;
    
    /**
     * Defines whether the record is mutable or immutable
     */
    private boolean immutable = false;
    
    
	//------------------------------------------------------------------------
	// Implements Serializable
	//------------------------------------------------------------------------

	/**
	 * Serial Version UID
	 */
    private static final long serialVersionUID = 8847220872014678877L;

    
	//------------------------------------------------------------------------
	// Implements Record
	//------------------------------------------------------------------------
		 
	/**
	 * Gets the name of the Record. 
	 *
	 * @return  String representing name of the Record
	 */
	public final String getRecordName(
	){
		return this.name;
	}

	/**
	 * Sets the name of the Record. 
	 *
	 * @param recordName
	 *        Name of the Record
	 */
	public final void setRecordName(
		String recordName
	){
	    assertMutability();
		this.name = recordName;
	}

	/**
	 * Gets a short description string for the Record.
	 * This property is used primarily by application development tools. 
	 *
	 * @return   String representing a short description of the Record
	 */
	public final String getRecordShortDescription(
	){
		return this.shortDescription;
	}

	/**
	 * Sets a short description string for the Record.
	 * This property is used primarily by application development tools. 
	 *
	 * @param recordShortDescription
	 *        Description of the Record
	 */
	public final void setRecordShortDescription(
		String recordShortDescription
	){
        assertMutability();
		this.shortDescription = recordShortDescription;
	}


	//------------------------------------------------------------------------
	// Extends Object
	//------------------------------------------------------------------------

	/**
	 * Returns a multi-line string representation of this IndexedRecord.
	 * <p>
	 * The string representation consists of the record name, follwed by the
	 * optional short description enclosed in parenthesis (" (...)"), followed 
	 * by a colon and the values enclosed in square brackets (": [...]"). Each
	 * value is written on a separate line and indented while embedded lines
	 * are indented as well.
	 *
	 * @return   a multi-line String representation of this Record.
	 */
    @Override
	public String toString(
	){
		return IndentingFormatter.toString(this);
	}
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
    	return IndexedRecords.getHashCode(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
    	return IndexedRecords.areEqual(this, that);
    }
    
    	
	//------------------------------------------------------------------------
	// Implements Freezable
	//------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.Freezable#makeImmutable()
     */
    @Override
    public void makeImmutable() {
        this.immutable = true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.Freezable#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return this.immutable;
    }

    private void assertMutability(){
        if(this.immutable) {
            throw new IllegalStateException("In immutable record can't be modififed");            
        }
    }

    
    //------------------------------------------------------------------------
    // Implements List
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public Object get(int index) {
        assertIndex(index);
        return this.value;
    }
    /* (non-Javadoc)
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    @Override
    public Object set(int index, Object element) {
        assertMutability();
        assertIndex(index);
        assertValue(element);
        Object oldValue = this.value;
        this.value = element;
        return oldValue;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, Object element) {
        assertMutability();
        assertIndex(index);
        assertSize();
        assertValue(element);
        this.value = element;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#remove(int)
     */
    @Override
    public Object remove(int index) {
        assertMutability();
        assertIndex(index);
        Object oldValue = this.value;
        this.value = null;
        return oldValue;
    }

    private void assertIndex(int index) {
        if(index > 0) {
            throw new IndexOutOfBoundsException("Index " + index + " is too big for size " + size());
        }
    }

    private void assertSize() {
        if(this.value != null) {
            throw new IndexOutOfBoundsException("The size can't exceed 1");
        }
    }
    
    private void assertValue(Object value) {
        if(value == null) {
            throw new NullPointerException("This list does not accept null values");
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return this.value == null ? 0 : 1;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone(
    ){
        return new TinyIndexedRecord(name, shortDescription, value);
    }

}
