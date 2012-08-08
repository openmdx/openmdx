/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: VariableSizeMappedRecord.java,v 1.19 2010/02/16 18:39:30 hburger Exp $
 * Description: JCA: variable-size MappedRecord implementation
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/02/16 18:39:30 $
 * ====================================================================
 *
 * This software is published under the BSD license  as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * An variable-size MappedRecord implementation. Only keys of type string and Integers in the range [-128..127] are supported.
 */
@SuppressWarnings("unchecked")
class VariableSizeMappedRecord 
    extends AbstractMap
    implements MappedRecord, MultiLineStringRepresentation
{

    /**
     * Creates an <code>MappedRecord</code> with the specified name and the given content.  
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
     * Creates an <code>MappedRecord</code> with the specified name and the given content.  
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
     * Creates an <code>MappedRecord</code> with the specified name and the given content.  
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
     * Constructor
     */
    protected VariableSizeMappedRecord(){
        // for de-serialization
    }
    
    /**
     * Serial Version UID
     */
    static final long serialVersionUID = -511654274174846301L;


    private Object normalizeKey(
        Object key
    ) {
        if(key instanceof String) {
            return ((String)key).intern();
        } else if (key instanceof Integer) {
            Integer i = ((Integer) key).intValue();
            if(i < -128 || i > 127) throw BasicException.initHolder(
                new RuntimeException(
                    "Inappropriate key value",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("supported-range", -128, 127),
                        new BasicException.Parameter("actual-value", i)
                    )
                ) 
            );
            return Integer.valueOf(i);
        } else throw BasicException.initHolder(
            new RuntimeException(
                "Inappropriate key class",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("supported", String.class.getName(), Integer.class.getName()),
                    new BasicException.Parameter("actual", key == null ? null : key.getClass().getName())
                )
            )
        );
    }
    
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
        key = this.normalizeKey(key);
        return this.values.put(key, value);
    }

    /**
     * Returns a set view of the mappings contained in this map.
     * Each element in this set is a Map.Entry. 
     * 
     * @return  a set view of the mappings contained in this map.
     */
    public Set entrySet(
    ){
        return this.values.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear(
    ) {
        this.values.clear();        
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(
        Object key
    ) {
        key = this.normalizeKey(key);
        return this.values.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(
        Object value
    ) {
        return this.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(
        Object key
    ) {
        key = this.normalizeKey(key);        
        return this.values.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty(
    ) {
        return this.values.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(
        Object key
    ) {
        key = this.normalizeKey(key);
        return this.values.remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size(
    ) {
        return this.values.size();
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
        return this.values.hashCode();
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
    final Map values = new IdentityHashMap();
    
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
