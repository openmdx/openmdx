/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: VariableSizeMappedRecord.java,v 1.10 2008/06/28 00:21:57 hburger Exp $
 * Description: JCA: variable-size MappedRecord implementation
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:57 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * An variable-size MappedRecord implementation.
 */
@SuppressWarnings("unchecked")
class VariableSizeMappedRecord 
    extends AbstractMap
    implements MappedRecord, MultiLineStringRepresentation
{

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     * <p>
     * This constructor does not declare any exceptions as it assumes that the
     * necessary checks are made by the record factory: The arguments keys
     * and values for example must have the same length.
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     recordShortDescription
     *            The short description of the Record; or null.
     *
     * @exception ArrayIndexOutOfBoundsException
     *            if the arguments keys and values do not have the same size.
     */
    VariableSizeMappedRecord(
        String recordName,
        String recordShortDescription
    ){
        super();
        this.recordName = recordName;
        this.recordShortDescription = recordShortDescription;
    }

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     * <p>
     * This constructor does not declare any exceptions as it assumes that the
     * necessary checks are made by the record factory: The arguments keys
     * and values for example must have the same length.
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     *
     * @exception ArrayIndexOutOfBoundsException
     *            if the arguments keys and values do not have the same size.
     */
    VariableSizeMappedRecord(
        String recordName
    ){
        this(recordName,null);
    }

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     * <p>
     * This constructor does not declare any exceptions as it assumes that the
     * necessary checks are made by the record factory: The arguments keys
     * and values for example must have the same length.
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     recordShortDescription
     *            The short description of the Record; or null.
     * @param     initialContent
     *            The map's initial content
     *
     * @exception ArrayIndexOutOfBoundsException
     *            if the arguments keys and values do not have the same size.
     */
    VariableSizeMappedRecord(
        String recordName,
        String recordShortDescription,
        Map initialContent
    ){
        this(recordName, recordShortDescription);
        putAll(initialContent);
    }


    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------

    /**
     *
     */
    static final long serialVersionUID = -511654274174846301L;
    
    
    //--------------------------------------------------------------------------
    // Extends AbstractMap
    //--------------------------------------------------------------------------
    
    /**
     * Associates the specified value with the specified key in this map. 
     * If the map previously contained a mapping for this key, the old value
     * is replaced.
     *
     * @param     key
     *            key with which the specified value is to be associated.
     * @param     value
     *            value to be associated with the specified key.
     *
     * @return    previous value associated with specified key, or null if there
     *            was no mapping for key. (A null return can also indicate that
     *            the map previously associated null with the specified key, if
     *            the implementation supports null values.)
     *
     * @exception ClassCastException
     *            if the class of the specified key or value prevents it from
     *            being stored in this map.
     * @exception IllegalArgumentException
     *            if some aspect of this key or value prevents it from being
     *            stored in this map.
     * @exception NullPointerException
     *            this map does not permit null keys or values, and the
     *            specified key or value is null.
     */
    public Object put(
        Object key,
        Object value
    ){
        int index=this.keys.indexOf(key);
        if(index==-1){
            this.keys.add(key);
            this.values.add(value);
            return null;
        }else{
            return this.values.set(index,value);
        }
    }

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

	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		this.values.clear();
		this.keys.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return this.keys.contains(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return this.values.contains(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		int index=this.keys.indexOf(key);
		return index == -1 ? null : this.values.get(index);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return this.keys.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object key) {
		int index=this.keys.indexOf(key);
		if(index==-1){
			return null;
		}else{
			this.keys.remove(index);
			return this.values.remove(index);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		return this.keys.size();
	}


	//--------------------------------------------------------------------------
	// Class EntrySet
	//--------------------------------------------------------------------------

    class EntrySet extends AbstractSet{
    
        public Iterator iterator(
        ){
            return new EntryIterator();
        }

        public int size(
        ){
            return keys.size();
        } 
    
    }


	//--------------------------------------------------------------------------
	// Class EntryIterator
	//--------------------------------------------------------------------------

    final class EntryIterator implements Iterator{
    
        public boolean hasNext(
        ){
            return index < keys.size();
        }
        
        public Object next(
        ){
            if(!hasNext())throw new NoSuchElementException();
            return new MapEntry(last=index++);
        }

        public void remove(
        ){
            if(last==MISSING_NEXT_CALL)throw new IllegalStateException(
                "next() has not been called yet"
            );
            if(last==DUPLICATE_REMOVE_CALL)throw new IllegalStateException(
                "remove() has already been called after next()"
            );
            keys.remove(last);
            values.remove(last);
            index=last;
            last=DUPLICATE_REMOVE_CALL;
        }
        
        int index = 0;
        int last = MISSING_NEXT_CALL;

        private static final int MISSING_NEXT_CALL = -1;
        private static final int DUPLICATE_REMOVE_CALL = -2;
        
    }


	//--------------------------------------------------------------------------
	// Class MapEntry
	//--------------------------------------------------------------------------

    private final class MapEntry implements Map.Entry {
        
        MapEntry(
            int index
        ){
            this.index = index;
        }
            
        public Object getKey(
        ){
            return keys.get(this.index);
        }
    
        public Object getValue(
        ){
            return values.get(this.index);
        } 
    
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
            return VariableSizeMappedRecord.hashCode(getKey()) ^ 
                VariableSizeMappedRecord.hashCode(getValue());
        }
    
        public Object setValue(
            Object value
        ){
            Object result = getValue();
            values.set(this.index, value);
            return result;
        }
        
        private final int index;
        
    }
             
    //--------------------------------------------------------------------------
    // Implements Record
    //--------------------------------------------------------------------------

    /**
     * Gets the name of the Record. 
     *
     * @return  String representing name of the Record
     */
    public final String getRecordName(
    ){
        return this.recordName;
    }

    /**
     * Sets the name of the Record. 
     *
     * @param name
     *        Name of the Record
     */
    public final void setRecordName(
        String name
    ){
        this.recordName = name;
    }

    /**
     * Gets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @return   String representing a short description of the Record
     */
    public final String getRecordShortDescription(
    ){
        return this.recordShortDescription;
    }

    /**
     * Sets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @param description
     *        Description of the Record
     */
    public final void setRecordShortDescription(
        String description
    ){
        this.recordShortDescription = description;
    }

    /**
     * Check whether this instance has the same content as another Map.
     * <p>
     * The Record's name and short description are ignored.
     *
     * @return  true if two instances are equal
     */
    public boolean equals(
        Object other
    ){
        if (other == this) return true;
        if (!(other instanceof Map)) return false;
        Map that = (Map) other;
        if (that.size() != size()) return false;
        for(
            Iterator i = entrySet().iterator();
            i.hasNext();
        ) {
            Entry e = (Entry) i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            if (value == null) {
            if (!(that.get(key)==null && that.containsKey(key)))
                return false;
            } else {
            if (!value.equals(that.get(key)))
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the hash code for the Record instance. 
     *
     * @return hash code
     */
    public int hashCode(
    ){
        int h = 0;
        for(
            Iterator i = entrySet().iterator();
            i.hasNext();
        ){
            h += i.next().hashCode();
        }
        return h;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return  a copy of this MappedRecord instance
     */
    public Object clone(
    ){
        return new VariableSizeMappedRecord(
            this.recordName,
            this.recordShortDescription,
            this
        );
    }

    /**
     * Returns a multi-line string representation of this MappedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the mappings enclosed in braces (": {...}"). Each
     * key-value mapping is rendered as the key followed by an equals sign ("=")
     * followed by the associated value written on a separate line and indented
     * while embedded lines are indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }

    /**
     * Returns a multi-line string representation of this MappedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the mappings enclosed in braces (": {...}"). Each
     * key-value mapping is rendered as the key followed by an equals sign ("=")
     * followed by the associated value written on a separate line and indented
     * while embedded lines are indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    static String toString(
        MappedRecord source
    ){
        StringBuilder result = new StringBuilder(
            source.getRecordName()
        );
        String recordShortDescription=source.getRecordShortDescription();
        if(
            recordShortDescription!=null
        ) result.append(
            " ("
        ).append(
            recordShortDescription
        ).append(
            ')'
        );
        result.append(
            ": {"
        );
        boolean empty=true;
        for(
            Iterator i=source.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry e=(Map.Entry)i.next();
            int j= result.append(
                "\n\t"
            ).length();
            result.append(
                e.getKey()
            ).append(
                '='
            ).append(
                e.getValue()
            );
            while(
                j<result.length()
            )if(
                result.charAt(j++)=='\n'
            ) result.insert(
                j++,
                '\t'
            );
            empty=false;
        }
        return result.append(
            empty?"}":"\n}"
        ).toString();
    }

    
    //--------------------------------------------------------------------------
    // Instance members
    //--------------------------------------------------------------------------
    
    /**
     * 
     */
    private String recordName;    

    /**
     *
     */
    private String recordShortDescription;

    /**
     *
     */
    final ArrayList keys = new ArrayList();
    
    /**
     * 
     */
    final ArrayList values = new ArrayList();


    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

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
    static boolean areEqual(
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
    static int hashCode(
        Object object
    ){
        return object==null ? 0 : object.hashCode();
    }
    
}
