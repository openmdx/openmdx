/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Sequences.java,v 1.5 2009/01/05 13:48:10 wfro Exp $
 * Description: Sequences
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:48:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.dataprovider.layer.persistence.common;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.base.collection.SparseList;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;


/**
 * Sequences
 */
@SuppressWarnings("unchecked")
public class Sequences implements MultiLineStringRepresentation{

    /**
     * Constructor
     */
    public Sequences(
    ){
        this(DEFAULT_INITIAL_CAPACITY);
    }
    
    /**
     * Constructor
     * 
     * @param initialCapacity
     */
    public Sequences(
        int initialCapacity
    ) {
        this.names = new String[initialCapacity];
        this.values = new long[initialCapacity];
        this.size = 0;
    }

    /**
     * Add a new sequence
     * 
     * @param name the sequence's name
     * @param value the sequence's next value
     */
    public void add(
        String name,
        long value
    ){
        if(this.names.length == this.size){
            String[] names = new String[this.size + CAPACITY_INCREMENT];
            long[] values = new long[this.size + CAPACITY_INCREMENT];
            System.arraycopy(this.names, 0, names, 0, this.size);
            System.arraycopy(this.values, 0, values, 0, this.size);
            this.names = names;
            this.values = values;
        }
        this.names[this.size] = name;
        this.values[this.size] = value;
        this.size++;
    }

    /**
     * Tests whether a value is a member of a sequence
     */
    public static boolean isMember(
        String value
    ){
        if(value.length() > 18) return false;
        for(
            int i = 0, limit = value.length();
            i < limit;
            i++
        ){
            char c = value.charAt(i);
            if(c < '0' || c > '9') return false;            
        }
        return true;
    }
    
    /**
     * Update a sequence
     * <p>
     * The sequence is created if it does not yet exist and value is a member of it.
     * 
     * @param name the sequence's name
     * 
     * @return the updated sequence value; or -1L if the sequence value has not changed
     */
    public long update(
        String name,
        String candidate
    ){
        if(!isMember(candidate)) return -1;
        long value = Long.parseLong(candidate);
        int index = indexOf(name);
        if(index < 0) {
            add(name, ++value);
        } else {
            if(++value > this.values[index]){
	            this.values[index] = value;
            } else {
                return -1L;
            }
        }
        return value;
    }

    /**
     * Set an object's Sequence context
     *  
     * @param target
     */
    public void toContext(
        DataproviderObject target
    ){
        target.clearValues(SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.OBJECT_CLASS).set(
            0,
            SystemAttributes.SEQUENCE_CLASS
        );
        target.clearValues(SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.SEQUENCE_SUPPORTED).set(
            0,
            Boolean.TRUE
        );
        SparseList names = target.clearValues(
            SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.SEQUENCE_NAME
        );
        SparseList values = target.clearValues(
            SystemAttributes.SEQUENCE_PREFIX + SystemAttributes.SEQUENCE_NEXT_VALUE
        );
        for(
            int i = 0;
            i < this.size;
            i++
        ){
            names.set(i, this.names[i]);
            values.set(i, new Long(this.values[i]));
        }
    }
    
    /**
     * Find a sequence's index
     * 
     * @param name
     * 
     * @return the sequence's index; or -1
     */
    private int indexOf(
        String name
    ){
        for(
            int i = 0;
            i < this.size;
            i++
        ) if(this.names[i].equals(name)) return i;
        return -1;
    }

    /**
     * Return a multi-line string representation of this object.
     */
    public String toString(
    ){
        return getClass().getName() + ": " + IndentingFormatter.toString(
            ArraysExtension.asMap(
	            this.names,
	            this.values
	        )
        );
    }
    
    /**
     * 
     */
    private String[] names;
    
    /**
     * 
     */
    private long[] values;

    /**
     * 
     */
    private int size;
    
    /**
     * 
     */
    private final static int DEFAULT_INITIAL_CAPACITY = 4;
    
    /**
     * 
     */
    private final static int CAPACITY_INCREMENT = 8;
    
}
