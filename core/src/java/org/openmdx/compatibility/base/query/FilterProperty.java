/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: FilterProperty.java,v 1.9 2008/03/21 20:14:51 hburger Exp $
 * Description: Filter Property
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 20:14:51 $
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
package org.openmdx.compatibility.base.query;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.resource.ResourceException;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.DataInput;
import org.openmdx.base.io.DataOutput;
import org.openmdx.base.io.Externalizable;
import org.openmdx.base.resource.Records;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;


/**
 * A Filter Property consists of<ol>
 * <li>a quantor
 * <li>the name of the attribute
 * <li>an operator
 * <li>an array of values (its minimal and maximal length is defined by the operator
 * </ol> 
 */
@SuppressWarnings("unchecked")
public final class FilterProperty 
  implements Serializable, Externalizable {

    /**
     * Do NOT use! Required for Externalizable.
     *
     */
    public FilterProperty(
    ) {        
    }
    
    /**
     * Creates a new FilterProperty
     *
     * @param     quantor
     *            defines which quantor has to be applied
     * @param     name
     *            defines to which attribute to filter has to be applied
     * @param     operator
     *            defines which operator has to be applied
     * @param     values
     *            Defines the "right" operands of the operator   
     */
    public FilterProperty(
        short quantor,
        String name,
        short operator,
        Object[] values
    ) {
        this.quantor = quantor;
        this.name = name.intern();
        this.operator = operator;
        this.values = values;
    }

    /**
     * Creates a new FilterProperty
     *
     * @param     quantor
     *            defines which quantor has to be applied
     * @param     name
     *            defines to which attribute to filter has to be applied
     * @param     operator
     *            defines which operator has to be applied
     */
    public FilterProperty(
        short quantor,
        String name,
        short operator
    ) {
        this(quantor,name,operator,new Object[0]);
    }

    /**
     * The quantor
     * 
     * @see org.openmdx.compatibility.base.query.Quantors
     */
    public final short quantor(){
        return this.quantor;
    }

    /**
     *
     */
    public final String name(){
      return this.name;
    }
    
    /**
     * The operator
     *
     * @see org.openmdx.compatibility.base.query.FilterOperators
     */
    public final short operator(
    ){
        return this.operator;
    }
     
    /**
     * 
     */
    public Object[] getValues(){
        return this.values;
    }

    /**
     * @param index
     */
    public Object getValue(
        int index
    ){
        return this.values[index];
    }

    /**
     * 
     */
    public List values(){
        return Arrays.asList(this.values);
    }
  
    //------------------------------------------------------------------------
    // Extends Externalizable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.io.Externalizable#readExternal(org.openmdx.base.io.DataInput)
     */
    public void readExternal(
        DataInput in
    ) throws IOException {
        this.name = in.readInternalizedString();
        this.operator = in.readShort();
        this.quantor = in.readShort();
        this.values = new Object[in.readShort()];
        for(int i = 0; i < this.values.length; i++) {
            byte tc = in.readByte();
            if(tc == TC_NULL) {
                this.values[i] = null;
            }
            else if(tc == TC_STRING) {
                this.values[i] = in.readString();
            }
            else if(tc == TC_NUMBER) {
                this.values[i] = in.readNumber();
            }
            else if(tc == TC_PATH) {
                Path p = new Path();
                p.readExternal(in);
                this.values[i] = p;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.io.Externalizable#writeExternal(org.openmdx.base.io.DataOutput)
     */
    public void writeExternal(
        DataOutput out
    ) throws IOException {
        out.writeInternalizedString(this.name);
        out.writeShort(this.operator);
        out.writeShort(this.quantor);
        out.writeShort(this.values.length);
        for(int i = 0; i < this.values.length; i++) {
            Object value = this.values[i];
            if(value == null) {
                out.writeByte(TC_NULL);
            }
            else if(value instanceof String) {
                out.writeByte(TC_STRING);
                out.writeString((String)value);
            }
            else if(value instanceof Number) {
                out.writeByte(TC_NUMBER);
                out.writeNumber((Number)value);
            }
            else if(value instanceof Path) {
                out.writeByte(TC_PATH);
                ((Path)value).writeExternal(out);
            }
            else {
                throw new ExtendedIOException(
                    new BasicException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("name", this.name),
                            new BasicException.Parameter("value", value)
                        },
                        "FilterProperty serialization failed"
                    )
                );                        
            }            
        }
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the filter property's string representation
     */
    public String toString (
    ){
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(), 
                Quantors.toString(quantor()) + ' ' + name() + ' ' + 
                    FilterOperators.toString(operator()) + ' ' + values(),
                new String[]{
                    "quantor",
                    "name",
                    "operator",
                    "values"
                }, 
                new Object[]{
                    Quantors.toString(quantor()),
                    name(), 
                    FilterOperators.toString(operator()), 
                    values()
                }
            ).toString();
        } catch (ResourceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if(!(object instanceof FilterProperty)) return false;
        FilterProperty that = (FilterProperty)object; 
        return this.quantor == that.quantor &&
            this.name.equals(that.name) &&
            this.operator == that.operator &&
            valuesAreEqual(this.values, that.values); 
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.name.hashCode();
    }
    
    /**
     * Compare the arrays as list or set, depending on the filter operator
     * 
     * @param left
     * @param right
     * 
     * @return true if the values are equal
     */
    private boolean valuesAreEqual(
        Object[] left,
        Object[] right
    ){
        switch (this.operator){
            case FilterOperators.IS_IN: 
            case FilterOperators.IS_LIKE: 
            case FilterOperators.SOUNDS_LIKE: 
            case FilterOperators.IS_NOT_IN: 
            case FilterOperators.IS_UNLIKE: 
            case FilterOperators.SOUNDS_UNLIKE: 
                return new HashSet(Arrays.asList(left)).equals(
                    new HashSet(Arrays.asList(right))
                );
            
            default:
                return Arrays.equals(left, right);
        }
    }

    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------
    private static final long serialVersionUID = 3976738060186890550L;

    private static final byte TC_NULL = 0;
    private static final byte TC_STRING = 1;
    private static final byte TC_NUMBER = 2;
    private static final byte TC_PATH = 3;
    
    private String name;
    private short operator;
    private short quantor;
    private Object[] values;

}
