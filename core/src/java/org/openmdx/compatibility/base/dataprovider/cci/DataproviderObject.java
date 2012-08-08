/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderObject.java,v 1.32 2008/06/27 16:59:28 hburger Exp $
 * Description: spice: dataprovider object
 * Revision:    $Revision: 1.32 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/27 16:59:28 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.DataInput;
import org.openmdx.base.io.DataOutput;
import org.openmdx.base.io.Externalizable;
import org.openmdx.compatibility.base.collection.CompactSparseList;
import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.format.IndentingFormatter;


/**
 * Note that this implementation is not synchronized. If multiple threads
 * access this dataprovider object concurrently, and at least one of the
 * threads modifies it structurally, it must be synchronized externally. 
 * (A structural modification is any operation that adds or deletes one or
 * more attributes; merely modifying the values associated with an attribute
 * that an instance already contains is not a structural modification.) This
 * is typically accomplished by synchronizing on the dataprovider object
 * itself. 
 */
@SuppressWarnings("unchecked")
public class DataproviderObject
    implements DataproviderObject_1_0, Serializable, Cloneable, Externalizable {

    //-----------------------------------------------------------------------    
    final int getAttributeIndex(
        String attributeName,
        boolean forceCreation
    ) throws ServiceException {
        String internalName = attributeName.intern();
        for(int i = 0; i < this.attributeNames.length; i++) {
            if(this.attributeNames[i] == internalName) return i;
        }
        if(!forceCreation) return -1;
        int len = this.attributeNames.length;
        String[] newAttributeNames = new String[len + 1];
        System.arraycopy(this.attributeNames, 0, newAttributeNames, 0, len);
        this.attributeNames = newAttributeNames;
        this.attributeNames[len] = internalName;
        return len;        
    }
    
    //-----------------------------------------------------------------------
    SparseList getAttributeValue(
        int attributeIndex
    ) throws ServiceException {
        return this.attributeValues == null || attributeIndex >= this.attributeValues.length
            ? null
            : this.attributeValues[attributeIndex];
    }
    
    //-----------------------------------------------------------------------
    void setAttributeValue(
        int attributeIndex,
        SparseList value
    ) throws ServiceException {
        if(this.attributeValues == null) {
            this.attributeValues = new SparseList[attributeIndex+1];
        }
        else if(attributeIndex >= this.attributeValues.length) {
            SparseList[] newValues = new SparseList[attributeIndex << 1];
            System.arraycopy(this.attributeValues, 0, newValues, 0, this.attributeValues.length);
            this.attributeValues = newValues;                    
        }
        this.attributeValues[attributeIndex] = value;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Returns the dataprovider object's path object.
     */
    final public Path path(
    ) {
        return this.path; 
    }

    //-----------------------------------------------------------------------
    /**
     * Do NOT use! Required for Externalizable.
     */
    public DataproviderObject(
    ) {        
    }
    
    //-----------------------------------------------------------------------
    /**
     * Creates a dataprovider object referencing a specific path object.
     * Subsequent changes to the path object are reflected by the dataprovider
     * object.
     */
    public DataproviderObject(
        Path path
    ) {
        this.path = path;
        this.attributeNames = new String[]{};
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a dataprovider object referencing a specific path object.
     * Subsequent changes to the path object are reflected by the dataprovider
     * object.
     * <p>
     * Subsequent changes to the attributes of <code>that</code>
     * object will not affect the new copy, and vice versa
     * while the contexts are shared.
     */
    public DataproviderObject(
        Path path,
        String objectClass,
        List objectInstanceOf,
        DataproviderObject_1_0 that,
        Collection attributeNames
    ) {
        this(path);        
        if(that != null) {
            try {
                this.setDigest(that.getDigest());
                for(
                    Iterator iterator = (
                        attributeNames == null ? that.attributeNames() : attributeNames
                    ).iterator();
                    iterator.hasNext();
                ){
                    final String name = (String)iterator.next();
                    SparseList values = that.getValues(name);
                    if(values != null) {
                        int attributeIndex = this.getAttributeIndex(name, true);
                        this.setAttributeValue(
                            attributeIndex,
                            COMPACT 
                                ? new CompactSparseList(values) :
                                (SparseList) new OffsetArrayList(values)
                        );
                    }
                }
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
            this.clearValues(SystemAttributes.OBJECT_CLASS).add(objectClass);
            this.clearValues(SystemAttributes.OBJECT_INSTANCE_OF).addAll(objectInstanceOf);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Generates a new copy of this dataprovider object.
     * Subsequent changes to the attributes of this
     * object will not affect the new copy, and vice versa
     * while the contexts are shared.
     */
    public DataproviderObject(
        DataproviderObject_1_0 that
    ) {
        this(that, true);
    }

    //-----------------------------------------------------------------------
    /**
     * Generates a new copy of this dataprovider object.
     * Subsequent changes to the attributes of this
     * object will not affect the new copy, and vice versa
     * while the contexts are shared.
     * 
     * @param that object to copy.
     * 
     * @param includeAttributes if true copies the attributes, otherwise
     *         copies only the path and digest.
     */
    public DataproviderObject(
        DataproviderObject_1_0 that,
        boolean includeAttributes
    ) {
        this((Path)that.path().clone());
        this.setDigest(that.getDigest());
        if(includeAttributes) {
            this.addClones(that,true);
        }
    }

    //------------------------------------------------------------------------
    // Value list interface
    //------------------------------------------------------------------------

    /**
     * Returns the attribute value list.
     * This method returns null if no such attribute exists.
     */
    final public SparseList getValues(
        String attributeName
    ) {
        try {
            int attributeIndex = this.getAttributeIndex(attributeName, false);
            return attributeIndex == -1
                ? null
                : this.getAttributeValue(attributeIndex);
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * Returns the modifiable attribute value list.
     * This method never returns null.
     */
    final public SparseList<Object> values(
        String attributeName
    ) {
        try {
            int attributeIndex = this.getAttributeIndex(attributeName, false);
            if(attributeIndex == -1) {
                synchronized(this.attributeNames) {
                    attributeIndex = this.getAttributeIndex(attributeName, true);
                    this.setAttributeValue(
                        attributeIndex, 
                        COMPACT 
                            ? (SparseList)new CompactSparseList(1) 
                            : (SparseList)new OffsetArrayList(1)
                    );
                }
            }
            return this.getAttributeValue(attributeIndex);
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a modifiable value-less attribute object.
     * This method never returns null.
     */
    final public SparseList<Object> clearValues(
        String attributeName
    ) {
        SparseList values = this.values(attributeName);
        values.clear();
        return values;
    }

    //------------------------------------------------------------------------
    // Attribute collection interface
    //------------------------------------------------------------------------

    //-----------------------------------------------------------------------
    /**
     * Clear the dataprovider object's attributes (optional operation).
     */
    final public void clear(
    ) {
        this.attributeNames = new String[]{};
    }
    
    //-----------------------------------------------------------------------
    /**
     * Get a set view of the dataprovider object's attribute names
     */
    final public Set<String> attributeNames(
    ) {
        return new AttributeNames();
    }
        
    //-----------------------------------------------------------------------
    /**
     * Checks whether the dataprovider object contains an attribute with the
     * given name.
     */
    final public boolean containsAttributeName(
        String attributeName
    ){
        try {
            return this.getAttributeIndex(attributeName, false) != -1;
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * The attributes are copied, subsequent changes to the original are
     * therefore not reflected in the dataprovider object and vice versa.
     *
     * @param   source
     *          The source object
     * @param   overwrite
     *          Defines whether existing attribute should be overwritten.
     * 
     * @return  true if the target object has been modified
     * <p>
     */ 
    final public boolean addClones(
        DataproviderObject_1_0 source,
        boolean overwrite
    ) {
        try {
            boolean modified = false;
            for(
                Iterator iterator = source.attributeNames().iterator();
                iterator.hasNext();
            ) {
                final String attributeName = (String)iterator.next();
                if (overwrite || ! containsAttributeName(attributeName)) {
                    SparseList values = source.getValues(attributeName);
                    int attributeIndex = this.getAttributeIndex(attributeName, true);
                    this.setAttributeValue(
                        attributeIndex, 
                        COMPACT 
                            ? new CompactSparseList(values) 
                            : (SparseList) new OffsetArrayList(values)
                    );
                    modified = true;
                }
            }
            return modified;
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    } 
    
    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------
    
    private void writeObject(
        java.io.ObjectOutputStream stream
    ) throws IOException {
        try {
            stream.writeUnshared(this.path.getComponents());
            stream.writeUnshared(this.digest);
            stream.writeObject(this.attributeNames);
            for(int i = 0; i < this.attributeNames.length; i++) {
                try {
                    stream.writeObject(this.getAttributeValue(i));          
                } 
                catch (IOException exception) {
                    throw new ExtendedIOException(
                        new BasicException(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("path", this.path),
                                new BasicException.Parameter("attribute", this.attributeNames[i]),
                                new BasicException.Parameter("values", this.getAttributeValue(i))
                            },
                            "DataproviderObject serialization failed"
                        )
                    );
                }
            }
        }
        catch(ServiceException e) {
            e.log();
            throw new IOException(e.getMessage());
        }
    }
    
    //-----------------------------------------------------------------------
    private void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        try {
            this.path = new Path((String[])stream.readUnshared());
            this.digest = (byte[])stream.readUnshared();
            this.attributeNames = (String[])stream.readObject();
            for(int i = 0; i < this.attributeNames.length; i++) {
                this.attributeNames[i] = this.attributeNames[i].intern();
            }
            for(int i = 0; i < this.attributeNames.length; i++) {
                this.setAttributeValue(
                    i,
                    (SparseList)stream.readObject()
                );
            }
        }
        catch(ServiceException e) {
            e.log();
            throw new IOException(e.getMessage());
        }
    }    

    //-----------------------------------------------------------------------
    // Externalizable
    //-----------------------------------------------------------------------
    
    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.Externalizable#readExternal(org.openmdx.base.io.DataInput)
     */
    public void readExternal(
        DataInput in
    ) throws IOException {
        try {
            this.path = new Path();
            this.path.readExternal(in);
            int digestLen = in.readShort();
            if(digestLen == -1) {
                this.digest = null;
            }
            else {
                this.digest = new byte[digestLen];
                in.readFully(this.digest, 0, digestLen);
            }
            this.attributeNames = in.readInternalizedStrings();
            for(int i = 0; i < this.attributeNames.length; i++) {
                short tc = in.readShort();
                if(tc == TC_NULL) {
                    // already null
                    // attributeValues[i] = null;
                }
                else {
                    SparseList values = new CompactSparseList(1);
                    if(tc == TC_STRING) {
                        String[] strings = in.readStrings();
                        values.addAll(Arrays.asList(strings));
                    }
                    else if(tc == TC_NUMBER) {
                        Number[] numbers = in.readNumbers();
                        values.addAll(Arrays.asList(numbers));                    
                    }
                    else if(tc == TC_PATH) {
                        int len = in.readShort();
                        for(int j = 0; j < len; j++) {
                            Path p = new Path();
                            p.readExternal(in);
                            values.add(p);
                        }
                    }
                    else {
                        throw new ExtendedIOException(
                            new BasicException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.TRANSFORMATION_FAILURE,
                                new BasicException.Parameter[]{
                                    new BasicException.Parameter("path", this.path),
                                    new BasicException.Parameter("attribute", this.attributeNames[i]),
                                    new BasicException.Parameter("tc", tc)
                                },
                                "DataproviderObject externalize failed. Unsupported type code"
                            )
                        );                        
                    }
                    this.setAttributeValue(
                        i,
                        values
                    );
                }
            }
            String[] singledValuedStrings = in.readStrings();
            for(int i = 0; i < this.attributeNames.length; i++) {
                if(singledValuedStrings[i] != null) {
                    this.setAttributeValue(
                        i,
                        new CompactSparseList(singledValuedStrings[i])
                    );
                }
            }
        }
        catch(ServiceException e) {
            e.log();
            throw new IOException(e.getMessage());
        }
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.Externalizable#writeExternal(org.openmdx.base.io.DataOutput)
     */
    public void writeExternal(
        DataOutput out
    ) throws IOException {
        try {
            this.path.writeExternal(out);
            if(this.digest == null) {
                out.writeShort(-1);
            }
            else {
                out.writeShort(this.digest.length);
                out.write(this.digest, 0, this.digest.length);
            }
            out.writeInternalizedStrings(this.attributeNames);
            String[] singledValuedStrings = new String[this.attributeNames.length];
            for(int i = 0; i < this.attributeNames.length; i++) {
                SparseList values = this.getAttributeValue(i);
                if(values == null) {
                    out.writeShort(TC_NULL);
                }
                else {
                    int size = values.size();
                    Object firstValue = size == 0 
                        ? null 
                        : values.get(values.firstIndex());
                    if(
                        (size == 0) || 
                        (firstValue instanceof String)
                    ) {
                        if(size == 1) {
                            out.writeShort(TC_NULL);
                            singledValuedStrings[i] = (String)firstValue;
                        }
                        else {
                            String[] strings = (String[])values.toArray(new String[size]);
                            out.writeShort(TC_STRING);
                            out.writeStrings(strings);
                        }
                    }
                    else if(firstValue instanceof Number) {
                        Number[] numbers = (Number[])values.toArray(new Number[size]);
                        out.writeShort(TC_NUMBER);
                        out.writeNumbers(numbers);
                    }
                    else if(firstValue instanceof Path) {
                        Path[] paths = (Path[])values.toArray(new Number[size]);
                        out.writeShort(TC_PATH);
                        out.writeShort(size);
                        for(int j = 0; j < paths.length; j++) {
                            paths[j].writeExternal(out);
                        }
                    }
                    else {
                        throw new ExtendedIOException(
                            new BasicException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.TRANSFORMATION_FAILURE,
                                new BasicException.Parameter[]{
                                    new BasicException.Parameter("path", this.path),
                                    new BasicException.Parameter("attribute", this.attributeNames[i]),
                                    new BasicException.Parameter("values", this.getAttributeValue(i))
                                },
                                "DataproviderObject serialization failed"
                            )
                        );                        
                    }
                }
            }
            out.writeStrings(singledValuedStrings);
        }
        catch(ServiceException e) {
            e.log();
            throw new IOException(e.getMessage());
        }
    }
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the dataprovider object's string representation
     */
    public String toString (
    ) {
        try {
            List attributeValues = new ArrayList();
            for(int i = 0; i < this.attributeNames.length; i++) {
                attributeValues.add(this.getAttributeValue(i));
            }
            return this.path.toString() + ':' + IndentingFormatter.toString(
            	ArraysExtension.asMap(
            	    new String[]{
            	        "digest", 
            	        "attributes"
            	    },	
            		new Object[]{
            	        this.digest, 
            	        ArraysExtension.asMap(this.attributeNames, attributeValues.toArray())
            	    }
    			)
    		);
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }
    
    //------------------------------------------------------------------------
    /**
     * Indicates whether some other object is "equal to" this one. 
     * Two dataprovider objects are considered to be equal if path, attributes
     * and digest are equal. 
     *
     * @param   object - the reference object with which to compare.
     *
     * @return  true if this object is the same as the object argument;
     *          false otherwise.
     */
    final public boolean equals(
        Object object
    ) {
        if (this == object) return true;
        if (!(object instanceof DataproviderObject_1_0)) return false;
        DataproviderObject_1_0 that = (DataproviderObject_1_0)object;
        if (
            ! this.path().equals(that.path()) ||
            ! Arrays.equals(this.getDigest(), that.getDigest())
        ) return false;
        Set attributeNames = new HashSet(this.attributeNames());
        attributeNames.addAll(that.attributeNames());
        for (
            Iterator i = attributeNames.iterator();
            i.hasNext();
        ){
            String attributeName = (String)i.next();
            List thisAttribute = this.getValues(attributeName);
            List thatAttribute = that.getValues(attributeName);
            //
            // Previous attribute comparison raised a NullPointer in case
            // (thatAttribute == null) and !thisAttribute.isEmpty()
            //
            if(
                !(
                  (thisAttribute == null || thisAttribute.isEmpty()) && (thatAttribute == null || thatAttribute.isEmpty()) ||
                  ((thisAttribute != null) && (thatAttribute != null) && thisAttribute.equals(thatAttribute))
                )
            ) return false;
        }
        return true;
    }

    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    /**
     * Generates a new copy of this dataprovider object.
     * Subsequent changes to the path or the attributes of this
     * object will not affect the new copy, and vice versa.
     *
     * @return    a clone of this instance.
     */
    final public Object clone(  
    ) {
        return new DataproviderObject(this);
    }


    //------------------------------------------------------------------------
    // Digest
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    /**
     * Get the object's digest (which must not be modified!)
     */
    final public byte[] getDigest(){
        return this.digest;
    }

    //------------------------------------------------------------------------
    /**
     * Set the object's digest (optional operation)
     */
    final public void setDigest(
        byte[] digest
    ) {
        this.digest = digest;
    }

    //------------------------------------------------------------------------
    // Modifiable set for AttributeNames
    //------------------------------------------------------------------------
    private class AttributeNamesIterator implements Iterator {

        public AttributeNamesIterator(
        ) {
            this.pos = 0;
        }
        
        public boolean hasNext(
        ) {
            return this.pos < DataproviderObject.this.attributeNames.length;
        }

        public Object next(
        ) {
            return DataproviderObject.this.attributeNames[this.pos++];
        }

        public void remove(
        ) {
            try {
                int len = DataproviderObject.this.attributeNames.length;
                this.pos--;
                // Remove attribute name at current position
                String[] newAttributeNames = new String[len-1];
                System.arraycopy(DataproviderObject.this.attributeNames, 0, newAttributeNames, 0, this.pos);
                System.arraycopy(DataproviderObject.this.attributeNames, this.pos+1, newAttributeNames, this.pos, len-this.pos-1);
                DataproviderObject.this.attributeNames = newAttributeNames;
                // Remove attribute value at current position
                for(int i = this.pos; i < len-1; i++) {
                    DataproviderObject.this.setAttributeValue(
                        i, 
                        DataproviderObject.this.getAttributeValue(i+1)
                    );
                }
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }

        private int pos = 0;
    }
    
    class AttributeNames extends AbstractSet<String> {

        public Iterator iterator(
        ) {
            return new AttributeNamesIterator();
        }

        public int size(
        ) {
            return DataproviderObject.this.attributeNames.length;
        }

        public boolean add(
            String name
        ) {
            try {
                if(DataproviderObject.this.getAttributeIndex(name, false) == -1) {
                    DataproviderObject.this.getAttributeIndex(name, true);
                    return true;
                }
                else {
                    return false;
                }
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }

        public boolean removeAll(
            Collection c
        ) {
            try {
                int newLen = DataproviderObject.this.attributeNames.length;
                boolean modified = false;
                for(Iterator i = c.iterator(); i.hasNext(); ) {
                    String name = (String)i.next();
                    int attributeIndex = DataproviderObject.this.getAttributeIndex(name, false);
                    if(attributeIndex >= 0) {
                        newLen--;
                        for(int j = attributeIndex; j < newLen; j++) {
                            DataproviderObject.this.attributeNames[j] = DataproviderObject.this.attributeNames[j+1];
                            DataproviderObject.this.setAttributeValue(
                                j, 
                                DataproviderObject.this.getAttributeValue(j+1)
                            );
                        }
                        modified = true;
                    }
                }
                if(modified) {
                    String[] newAttributeNames = new String[newLen];
                    System.arraycopy(DataproviderObject.this.attributeNames, 0, newAttributeNames, 0, newLen);
                    DataproviderObject.this.attributeNames = newAttributeNames;
                }
                return modified;
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }
                
    }
    
    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    private static final long serialVersionUID = 4819423359125977957L;

    private static final short TC_NULL = 0;
    private static final short TC_STRING = 1;
    private static final short TC_NUMBER = 2;
    private static final short TC_PATH = 3;
    
    /**
     * Tells whether the values are<ul>
     * <li><code>OffsetArrayList</code>s
     * <li><code>CompactSparseList</code>s
     * </ul>
     */
    private final static boolean COMPACT = true;
    
    private transient Path path;
    private transient byte[] digest;
    transient String[] attributeNames;
    private transient SparseList[] attributeValues;

}
