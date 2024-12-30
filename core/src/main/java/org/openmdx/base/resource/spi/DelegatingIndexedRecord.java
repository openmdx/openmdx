/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Delegating Indexed Record
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.resource.spi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;

import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Delegating Indexed Record
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class DelegatingIndexedRecord 
	implements IndexedRecord, MultiLineStringRepresentation
{

    /**
     * Constructor
     */
    public DelegatingIndexedRecord(
        String recordName,
        String recordShortDescription,
        List delegate
    ) {
        this.recordName = recordName;
        this.recordShortDescription = recordShortDescription;
        this.delegate = delegate;
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 3761411910632878384L;
    
    private String recordShortDescription;
    
    private String recordName;
    
    private List delegate;
    
    

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return IndentingFormatter.toString(this);
    }


    //------------------------------------------------------------------------
    // Implements List
    //------------------------------------------------------------------------
    
    public Object set(int index, Object element) {
        return delegate.set(index, element);
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return delegate.listIterator();
    }

    public boolean addAll(int index, Collection c) {
        return delegate.addAll(index, c);
    }

    public boolean add(Object o) {
        return delegate.add(o);
    }
    
    public void add(int index, Object element) {
        delegate.add(index, element);
    }
    
    public Object[] toArray() {
        return delegate.toArray();
    }
    
    public Object remove(int index) {
        return delegate.remove(index);
    }
    
    public boolean addAll(Collection c) {
        return delegate.addAll(c);
    }
    
    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }
    
    public boolean contains(Object o) {
        return delegate.contains(o);
    }
    
    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }
    
    public void clear() {
        delegate.clear();
    }
    
    public Object get(int index) {
        return delegate.get(index);
    }
    
    public int size() {
        return delegate.size();
    }
    
    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }
    
    public ListIterator listIterator(int index) {
        return delegate.listIterator(index);
    }
    
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
    
    public boolean remove(Object o) {
        return delegate.remove(o);
    }
    
    public Iterator iterator() {
        return delegate.iterator();
    }
    
    public List subList(int fromIndex, int toIndex) {
        return new DelegatingIndexedRecord(
        	this.recordName, 
        	this.recordShortDescription, 
        	delegate.subList(fromIndex, toIndex)
        );
    }
    
    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }
    
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }
    

    //------------------------------------------------------------------------
    // Implements Record
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    public final String getRecordName() {
        return this.recordName;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;        
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    public void setRecordShortDescription(String recordShortDescription) {
        this.recordShortDescription = recordShortDescription;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    public final String getRecordShortDescription() {
        return this.recordShortDescription;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone(
    ) {
        return new DelegatingIndexedRecord(
            this.recordName,
            this.recordShortDescription,
            this.delegate
        );
    }
    
}
