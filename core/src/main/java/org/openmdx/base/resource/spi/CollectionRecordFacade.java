/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JCA: IndexedRecord backed-up by an optional object
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.resource.cci.IndexedRecord;

import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * An IndexedRecord of size 0 or 1 backed-up by an optional value
 */
@SuppressWarnings("rawtypes")
public abstract class CollectionRecordFacade
    extends AbstractList
    implements IndexedRecord, MultiLineStringRepresentation {

    /**
     * Creates an IndexedRecord with the specified name and the given content.
     * <p>
     * This constructor does not declare any exceptions as it assumes that the
     * necessary checks are made by the record factory.
     *
     * @param recordName
     *            The name of the record acts as a pointer to the meta
     *            information (stored in the metadata repository) for a
     *            specific record type.
     * @param recordShortDescription
     *            The short description of the Record; or null.
     */
    CollectionRecordFacade(
        Supplier<Object> getter,
        Consumer<Object> setter
    ) {
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * The getter for the optional value
     */
    private final Supplier<Object> getter;

    /**
     * The setter for the optional value
     */
    private final Consumer<Object> setter;

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -8235094691802123524L;

    
    //------------------------------------------------------------------------
    // Implements {code Record}
    //------------------------------------------------------------------------

    /**
	 * Sets the name of the Record. 
	 *
	 * @param recordName
	 *        Name of the Record
	 */
	public void setRecordName(
		String recordName
	){
	    if(!getRecordName().equals(recordName)) {
	        throw new IllegalArgumentException("The record name can't be changed from "
	            + getRecordName() + " to " + recordName);
	    }
	}

    /**
     * Gets a short description string for the Record.
     * This property is used primarily by application development tools.
     *
     * @return String representing a short description of the Record
     */
    public String getRecordShortDescription() {
        return null;
    }

    /**
     * Sets a short description string for the Record.
     * This property is used primarily by application development tools.
     *
     * @param recordShortDescription
     *            Description of the Record
     */
    public void setRecordShortDescription(
        String recordShortDescription
    ) {
        if(recordShortDescription != null) {
            throw new IllegalArgumentException("Tiny collection records of type "
                + getClass().getName() + " do not support short descriptions: " + recordShortDescription);
        }
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
     * @return a multi-line String representation of this Record.
     */
    @Override
    public String toString() {
        return IndentingFormatter.toString(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return IndexedRecords.getHashCode(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return IndexedRecords.areEqual(this, that);
    }

    
    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.getter.get() == null;
    }

    
    //------------------------------------------------------------------------
    // Implements List
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public Object get(int index) {
        assertIndex(index, false);
        return this.getter.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    @Override
    public Object set(
        int index,
        Object element
    ) {
        assertIndex(index, false);
        assertValue(element);
        final Object oldValue = this.getter.get();
        this.setter.accept(element);
        return oldValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#clear()
     */
    @Override
    public void clear() {
        if (!isEmpty()) {
            this.setter.accept(null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#add(int, java.lang.Object)
     */
    @Override
    public void add(
        int index,
        Object element
    ) {
        assertIndex(index, true);
        assertEmpty();
        assertValue(element);
        this.setter.accept(element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#remove(int)
     */
    @Override
    public Object remove(int index) {
        assertIndex(index, false);
        final Object oldValue = this.getter.get();
        this.setter.accept(null);
        return oldValue;
    }

    private void assertIndex(
        int index,
        boolean add
    ) {
        if (add ? index > size() : index >= size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is too big for size " + size());
        }
    }

    private void assertEmpty() {
        if (!isEmpty()) {
            throw new IllegalStateException("The size can't exceed 1");
        }
    }

    private void assertValue(Object value) {
        if (value == null) {
            throw new NullPointerException("This list does not accept null values");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return isEmpty() ? 0 : 1;
    }

    
    //------------------------------------------------------------------------
    // Implements {@code Cloneable}
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()new VariableSizeIndexedRecord(getRecordName(), getRecordShortDescription())
     */
    @Override
    public IndexedRecord clone(
    ){
        return isEmpty() ?
            new VariableSizeIndexedRecord(getRecordName(), getRecordShortDescription()) :
            new VariableSizeIndexedRecord(getRecordName(), getRecordShortDescription(), this);
    }

    
}
