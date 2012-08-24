/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Enumeration Mappping
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.collection;

import java.util.Map;
import java.util.Set;


import java.util.EnumMap;







/**
 * Enumeration Mapping
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class EnumMapping extends EnumMap {

    /**
     * Constructor
     * 
     * @param keyType
     */
    public EnumMapping(Class keyType) {
        super(keyType);
    }

    /**
     * Constructor
     * 
     * @param m
     */
    public EnumMapping(EnumMap m) {
        super(m);
    }

    /**
     * Constructor
     * 
     * @param m
     */
    public EnumMapping(Map m) {
        super(m);
    }

    /**
     * Impements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3256442512536318776L;

    /**
     * The cached entry set
     */
    private transient Map.Entry[] entries;

    /**
     * Retrieve the key corresponding to a given value
     * 
     * @param value
     * 
     * @return the first key corresponding to a given value
     */
    public Enum getKey(
        Object value
    ){
        if(this.entries == null){
            Set entrySet = super.entrySet();
            this.entries = (Map.Entry[]) entrySet.toArray(
                new Map.Entry[entrySet.size()]
            );
        }
        for(
            int i = 0;
            i < entries.length;
            i++
        ) if (
             equal(this.entries[i].getValue(), value)
        ) return (Enum) this.entries[i].getKey();
        return null;
    }

    /**
     * Compare two object references
     * 
     * @param left
     * @param right
     * 
     * @return true if both the object references are null or equal
     */
    private static boolean equal(
        Object left,
        Object right
    ){
        return left == null ? right == null : left.equals(right);
    }

}
