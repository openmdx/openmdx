/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: UnorderedValues 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.rest;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Unordered Values
 */
class UnorderedValues extends AbstractProcessingCollection {

    /**
     * Constructor 
     */
    UnorderedValues(
    	Container_1_0 delegate
    ) {
    	this.delegate = delegate;
    }

    /**
     * The delegate
     */
    private final Container_1_0 delegate;

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    public Iterator<DataObject_1_0> iterator() {
        return new ValueIterator(
            this.delegate.entrySet().iterator()
        );
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    public int size() {
        return this.delegate.size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return this.delegate.containsValue(o);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return toArray(
			new Object[size()]
		);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		final Object[] values;
		final int s = size();
		if(a.length < s) {
			values = (Object[]) Array.newInstance(a.getClass().getComponentType(), s);
		} else {
			values = a;
		}
		int i = 0;
		for(Map.Entry<String, DataObject_1_0> entry : this.delegate.entrySet()) {
			values[i++] = entry.getValue();
		}
		while(i < s) {
			values[i++] = null;
		}
		return (T[]) values;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(DataObject_1_0 o) {
        throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		if(this.contains(o)) {
            final boolean persistent = ReducedJDOHelper.isPersistent(o);
            if(persistent) {
            	this.delegate.openmdxjdoGetDataObjectManager().deletePersistent(o);
            } else {
            	for(
            		Iterator<Map.Entry<String, DataObject_1_0>> i = this.delegate.entrySet().iterator();
            		i.hasNext();
            	){
            		if(o == i.next().getValue()) {
            			i.remove();
            			return true;
            		}
            	}
            }
            return persistent;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		for(Object value : c) {
			if(!this.delegate.containsValue(value)) return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends DataObject_1_0> c) {
        throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for(Object o : c) {
			modified |= remove(o);
		}
		return modified;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		for(
			Iterator<DataObject_1_0> i = this.iterator(); 
			i.hasNext();
		) {
			if(!c.contains(i.next())) {
				i.remove();
				modified = true;
			}
		}
		return modified;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		this.delegate.clear();
	}

	/**
     * Break the List contract to avoid round-trips
     */
    @Override
    public boolean equals(
        Object that
    ) {
        return this == that;
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public String toString(
    ){
        return this.getClass().getSimpleName() + " of " + this.delegate;
    }

    
    //--------------------------------------------------------------------
    // Class ValueIterator
    //--------------------------------------------------------------------
    
    /**
     * Value Iterator
     */
    private static class ValueIterator implements Iterator<DataObject_1_0> {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        ValueIterator(
            Iterator<Map.Entry<String, DataObject_1_0>> delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * An entry set iterator
         */
        private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public DataObject_1_0 next() {
            return this.delegate.next().getValue();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            this.delegate.remove();
        }
        
    }

}