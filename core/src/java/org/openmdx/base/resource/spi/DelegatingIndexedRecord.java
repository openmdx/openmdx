/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DelegatingIndexedRecord.java,v 1.9 2011/04/12 15:44:02 hburger Exp $
 * Description: Delegating Indexed Record
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/12 15:44:02 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
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

import javax.resource.cci.IndexedRecord;

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
     * 
     */
    private static final long serialVersionUID = 3761411910632878384L;

    /**
     * 
     */
    private String recordShortDescription;

    /**
     * 
     */
    private String recordName;

    /**
     * 
     */
    private List source;
    
    /**
     * Constructor
     */
    public DelegatingIndexedRecord(
        String recordName,
        String recordShortDescription,
        List source
    ) {
        this.recordName = recordName;
        this.recordShortDescription = recordShortDescription;
        this.source = source;
    }

    /**
     * Constructor 
     */
    protected DelegatingIndexedRecord(){
        // for de-serialization
    }

    

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return source.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return source.equals(that);
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
    
    /**
     * @param index
     * @param element
     * @return
     */
    public Object set(int index, Object element) {
        return source.set(index, element);
    }

    /**
     * @param o
     * @return
     */
    public int lastIndexOf(Object o) {
        return source.lastIndexOf(o);
    }

    /**
     * @return
     */
    public ListIterator listIterator() {
        return source.listIterator();
    }

    /**
     * @param index
     * @param c
     * @return
     */
    public boolean addAll(int index, Collection c) {
        return source.addAll(index, c);
    }

    /**
     * @param o
     * @return
     */
    public boolean add(Object o) {
        return source.add(o);
    }
    
    /**
     * @param index
     * @param element
     */
    public void add(int index, Object element) {
        source.add(index, element);
    }
    
    /**
     * @return
     */
    public Object[] toArray() {
        return source.toArray();
    }
    
    /**
     * @param index
     * @return
     */
    public Object remove(int index) {
        return source.remove(index);
    }
    
    /**
     * @param c
     * @return
     */
    public boolean addAll(Collection c) {
        return source.addAll(c);
    }
    
    /**
     * @param c
     * @return
     */
    public boolean retainAll(Collection c) {
        return source.retainAll(c);
    }
    
    /**
     * @param o
     * @return
     */
    public boolean contains(Object o) {
        return source.contains(o);
    }
    
    /**
     * @param c
     * @return
     */
    public boolean containsAll(Collection c) {
        return source.containsAll(c);
    }
    
    /**
     * 
     */
    public void clear() {
        source.clear();
    }
    
    /**
     * @param index
     * @return
     */
    public Object get(int index) {
        return source.get(index);
    }
    
    /**
     * @return
     */
    public int size() {
        return source.size();
    }
    
    /**
     * @param c
     * @return
     */
    public boolean removeAll(Collection c) {
        return source.removeAll(c);
    }
    
    /**
     * @param index
     * @return
     */
    public ListIterator listIterator(int index) {
        return source.listIterator(index);
    }
    
    /**
     * @return
     */
    public boolean isEmpty() {
        return source.isEmpty();
    }
    
    /**
     * @param o
     * @return
     */
    public boolean remove(Object o) {
        return source.remove(o);
    }
    
    /**
     * @return
     */
    public Iterator iterator() {
        return source.iterator();
    }
    
    /**
     * @param fromIndex
     * @param toIndex
     * @return
     */
    
    public List subList(int fromIndex, int toIndex) {
        return source.subList(fromIndex, toIndex);
    }
    
    /**
     * @param a
     * @return
     */
    public Object[] toArray(Object[] a) {
        return source.toArray(a);
    }
    
    /**
     * @param o
     * @return
     */
    public int indexOf(Object o) {
        return source.indexOf(o);
    }
    

    //------------------------------------------------------------------------
    // Implements Record
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    public String getRecordName() {
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
    public String getRecordShortDescription() {
        return this.recordShortDescription;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return new DelegatingIndexedRecord(
            this.recordName,
            this.recordShortDescription,
            this.source
        );
    }
    
}
