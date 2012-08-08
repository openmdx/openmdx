/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LenientCharacterComparator.java,v 1.10 2008/09/22 23:38:20 hburger Exp $
 * Description: Abstract Filter Class
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/22 23:38:20 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.query;

import java.util.Comparator;

/**
 * Allows comparison of not directly comparable classes
 */
public class LenientCharacterComparator implements Comparator<Object> {

    /**
     * Factory for a LenientComparator using the default CharSequence comparator
     * 
     * @return  a linient comparator using the default CharSequence comparator
     */
    public static Comparator<Object> getInstance(
    ){
        return LenientCharacterComparator.instance;
    }

    /**
     * Use specific CharSequence camparator
     */
    public LenientCharacterComparator(
        Comparator<Object> charSequenceComparator
    ) {
        this.charSequenceComparator = charSequenceComparator;
    }

    /**
     * 
     */
    private final Comparator<Object> charSequenceComparator;

    /**
     * 
     */
    private static final Comparator<Object> instance = new LenientCharacterComparator(null);


    //------------------------------------------------------------------------
    // Implements Comparator
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public int compare(
        Object first,
        Object second
    ) {
        return isCharSequence(first) && isCharSequence(second) ?
            compare(toString(first), toString(second)) :
            ((Comparable)first).compareTo(second);
    }

    /**
     * Compares two String values for order. Returns a negative integer, zero, or a 
     * positive integer as the first value is less than, equal to, or greater 
     * than the second value.
     * 
     * @param first
     * @param second
     * 
     * @return      a negative integer, zero, or a positive integer as the first 
     *              argument is less than, equal to, or greater than the second.
     */
    private int compare(
        String first,
        String second
    ){
        return this.charSequenceComparator == null ?
            first.compareTo(second) :
            this.charSequenceComparator.compare(first, second);
    }


    //------------------------------------------------------------------------
    // Type tests
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static boolean isCharSequence(
       Object object
    ){
        return
            object instanceof CharSequence ||
            object instanceof char[];
    }


    //------------------------------------------------------------------------
    // Type conversions
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static String toString(
        Object object
    ){
        return object instanceof char[] ? new String((char[])object) : object.toString();
    }

}
