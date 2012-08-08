/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ArraysExtension.java,v 1.6 2008/02/28 13:57:35 hburger Exp $
 * Description: Arrays Extension 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/28 13:57:35 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.collection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Array Utility Class
 */
@SuppressWarnings("unchecked")
public class ArraysExtension {

	private ArraysExtension(
	){	
	    // Avoid instantiation
	}
	
	/**
	 * Wraps an array into a list
	 * 
	 * @param array the array backing up the list
	 * 
	 * @return a List backed-up by the given array
	 * 
	 * @exception NullPointerException
	 * 			  if the array argument is <code>null</code>
	 * @exception ClassCastException 
	 * 			  if the array argument is not an array
	 */
    public static List asList(
		Object array
	){
		return new AsList(array);
	}
	
	/**
     * Wraps two arrays into a map.  
	 * 
	 * @param keys
	 * @param values
     * 
	 * @return a Map backed-up by the given arrays
	 * 
	 * @exception NullPointerException
	 * 			  if either argument is <code>null</code>
	 * @exception IllegalArgumentException
     *            if the values argument is not an array
	 */
    public static Map asMap(
        Object keys,
        Object values
    ){
    	return new AsMap(keys,values);
    }

    
    private static Object clone(
        Object original
    ){
        int length = Array.getLength(original);
        Object copy = Array.newInstance(
            original.getClass().getComponentType(), 
            Array.getLength(original)
        );
        System.arraycopy(original, 0, copy, 0, length);
        return copy;
    }
    

    //------------------------------------------------------------------------
    // Class AsList
    //------------------------------------------------------------------------

    /**
     * Wraps an array into a list.
     */
    public static class AsList 
        extends AbstractList
        implements Cloneable, Serializable
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3258134669471267120L;


        /**
         * Constructor 
         * 
         * @param array the array backing up the list
         * @param size the list's size
         * 
         * @exception NullPointerException
         *            if the array argument is <code>null</code>
         */
        protected AsList(
            Object array,
            int size
        ){
            this.array = array;
            this.size = size;
        }

        /**
         * Constructor 
         * 
         * @param array the array backing up the list
         * 
         * @exception NullPointerException
         *            if the array argument is <code>null</code>
         * @exception IllegalArgumentExceptionv 
         *            if the array argument is not an array
         */
        protected AsList(
            Object array
        ){
            this(array, Array.getLength(array));
        }
        
        /**
         * Get the array backing-up the List.
         * <p>
         * Changes to the array are reflected by the List and vice versa.
         *
         * @return  the array backing-up the List
         */
        protected Object getDelegate(
        ){
            return this.array;
        }

        protected AsList(
        ){  
            // Deserialization
        }
        
        /**
         * @serial
         */
        private Object array;

        /**
         * @serial
         */
        private int size;
        
        /**
         * Returns the element at the specified position in this list.
         *
         * @param     index
         *            index of element to return.
         *
         * @return    the element at the specified position in this list.
         *
         * @exception IndexOutOfBoundsException
         *            if the index is out of range (index < 0 || index >= size()).
         * @exception ClassCastException
         *            if this FixedSizeIndexedRecord is not backed-up by a 
         *            one-dimensional array of primitive types.
         */
        public Object get(
            int index
        ){
            return index < this.size ? Array.get(this.array,index) : null;
        }

        /**
         * Returns the number of elements in this list. 
         *
         * @return  the number of elements in this list.
         */
        public int size(
        ){
            return this.size;
        }

        /**
         * Replaces the element at the specified position in this list with the
         * specified element (optional operation).
         *
         * @param     index
         *            index of element to replace.
         * @param     element
         *            element to be stored at the specified position.
         *
         * @return    the element previously at the specified position.
         *
         * @exception UnsupportedOperationException
         *            if the set method is not supported by this list.
         * @exception ClassCastException
         *            if the class of the specified element prevents it from being
         *            added to this list.
         * @exception IllegalArgumentException
         *            if some aspect of the specified element prevents it from
         *            being added to this list.
         * @exception IndexOutOfBoundsException
         *            if the index is out of range (index < 0 || index >= size()).
         */ 
        public Object set(
            int index,
            Object element
        ){
            if(index >= this.size) throw new IndexOutOfBoundsException(
                "Index " + index + " exceeds the size " + this.size
            );
            Object result=get(index);
            Array.set(this.array,index,element);
            return result;
        }

        /**
         * Creates and returns a copy of this object.
         * <p>
         * The array is cloned.
         *
         * @return  a copy of this ArrayAsList instance
         */
        public Object clone(
        ){      
            return new AsList(
                ArraysExtension.clone(this.array),
                this.size
            );
        }

    }

    //------------------------------------------------------------------------
    // Class AsMap
    //------------------------------------------------------------------------

    /**
     * A map backed-up by two arrays.
     */
    public static class AsMap 
        extends AbstractMap
        implements Serializable, Cloneable
    {

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3202169295074577277L;

        /**
         * Constructor  
         * 
         * @param keys
         * @param values
         * 
         * @exception NullPointerException
         *            if either argument is <code>null</code>
         * @exception IllegalArgumentException
         *            if the values argument is not an array
         */
        protected AsMap(
            Object keys,
            Object values
        ){
            this.keys = keys;
            this.values = values;
        }

        /**
         *
         */
        Object keys;
       
        /**
         * An array
         */
        Object values;

        private transient List<?> valueList = null;
        

        protected AsMap(){  
            // Deserialization
        }
        
        /**
         * Serial Version UID
         */
        // static final long serialVersionUID = 817033812973215784L;
        
        /**
         * Returns a set view of the mappings contained in this map.
         * Each element in this set is a Map.Entry. 
         * 
         * @return  a set view of the mappings contained in this map.
         */
        public Set entrySet(
        ){
            return new EntrySet();
        }

        /**
         * 
         */
        private int slotOf(
            Object object
        ){
            for(
                int index = 0, iLimit = size(); 
                index < iLimit;
                index++
            ) {
                if (areEqual(Array.get(this.keys, index),object)) {
                    return index;
                }
            }
            return -1;
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key) {
            int slot = slotOf(key);
            return slot == -1 || slot >= Array.getLength(this.values) ? 
                null : 
                Array.get(this.values,slot);
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Object key, Object value) {
            int slot = slotOf(key);
            if (slot == -1) throw new IllegalArgumentException(
                "This key value is not among the fixed set of keys"
            );
            Object oldValue = Array.get(this.values,slot);
            Array.set(this.values, slot, value); 
            return oldValue;
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#size()
         */
        public int size() {
            return Array.getLength(this.keys);
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection values() {
            if(this.valueList == null){
                this.valueList = new AsList(this.values, size());
            }
            return this.valueList;
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object object) {
            return slotOf(object) >= 0;
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object object) {
            int vLimit = Array.getLength(this.values); 
            if(vLimit < size() && object == null) {
                return true;
            } else {
                for(
                    int index = 0; 
                    index < vLimit;
                    index++
                ) {
                    if (areEqual(Array.get(this.values, index),object)) {
                        return true;
                    }
                }
                return false;
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return size() == 0;
        }
        
        private class EntrySet extends AbstractSet{
        
            public Iterator iterator(
            ){
                return new EntryIterator();
            }

            public int size(
            ){
                return AsMap.this.size();
            } 
        
        }

        private final class EntryIterator implements Iterator{
        
            public boolean hasNext(
            ){
                return index < size();
            }
            
            public Map.Entry next(
            ){
                if(!hasNext())throw new NoSuchElementException();
                return new MapEntry(index++);
            }

            public void remove(
            ){
                throw new UnsupportedOperationException();
            }
            
            int index = 0;

        }

        /**
         * 
         */
        private final class MapEntry implements Map.Entry {
            
            MapEntry(
                int slot
            ){
                this.slot = slot;
            }
                
            public Object getKey(
            ){
                return Array.get(AsMap.this.keys, this.slot);
            }
        
            public Object getValue(
            ){
                return this.slot < Array.getLength(AsMap.this.values) ? 
                    Array.get(AsMap.this.values, this.slot) : 
                    null;
            } 
        
            @SuppressWarnings("unchecked")
            public boolean equals(
                Object other
            ){
                Map.Entry that;
                return other instanceof Map.Entry &&
                    areEqual(this.getKey(),(that=(Map.Entry)other).getKey()) && 
                    areEqual(this.getValue(),that.getValue());
            }
        
            public int hashCode(
            ){
                return AsMap.hashCode(getKey()) ^ 
                    AsMap.hashCode(getValue());
            }
        
            public Object setValue(
                Object value
            ){
                Object result = getValue();
                Array.set(AsMap.this.values,this.slot, value);
                return result;
            }
            
            private final int slot;
            
            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            public String toString() {
                return String.valueOf(getKey()) + "=" + String.valueOf(getValue());
            }

        }

        /**
         * Member equality
         *
         * @param left
         *        one object
         * @param right
         *        another object
         *
         * @return  true if both objects are either <code>null</code> or equal.
         */
        protected static boolean areEqual(
            Object left,  
            Object right
        ){
            return left==null ? right==null : left.equals(right);
        }

        /**
         * Retrieve an object's hash code
         *
         * @param object
         *        the relevant object
         *
         * @return  the object's hash code;
         *          or 0 if object is <code>null</code>.
         */
        protected static int hashCode(
            Object object
        ){
            return object==null ? 0 : object.hashCode();
        }

        /**
         * Creates and returns a copy of this object.
         * <p>
         * The arrays are not cloned.
         *
         * @return  a copy of this ArrayAsMap instance.
         */
        public Object clone(
        ){      
            return new AsMap(
                ArraysExtension.clone(this.keys),
                ArraysExtension.clone(this.values)
            );
        }

    }
    
}
