/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Abstract Filter Class
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.base.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;


/**
 * Allows comparison of not directly comparable classes
 */
public class LenientNumberComparator extends LenientCharacterComparator {

    /**
     * Use specific CharSequence comparator
     */
    protected LenientNumberComparator(
        Comparator<Object> charSequenceComparator
    ) {
        super(charSequenceComparator);
    }

    //------------------------------------------------------------------------
    // Implements Comparator
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(
        Object first, 
        Object second
    ) {
        return first instanceof Number && second instanceof Number ?
            compare((Number)first,(Number)second) :
            super.compare(first, second) ;
    }

    /**
     * Compares two numbers for order. Returns a negative integer, zero, or a 
     * positive integer as the first object is less than, equal to, or greater 
     * than the second object.
     * 
     * @param first
     * @param second
     * 
     * @return      a negative integer, zero, or a positive integer as the first 
     *              argument is less than, equal to, or greater than the second.
     */
    private static int compare(
        Number first,
        Number second
    ){
        return
          isAssignableToLong(first) && isAssignableToLong(second) ? 
            compare(first.longValue(), second.longValue()) :
          isAssignableToDouble(first) || isAssignableToDouble(second) ?
            compare(first.doubleValue(), second.doubleValue()) :
            toBigDecimal(first).compareTo(toBigDecimal(second));
    }

    /**
     * Compares two long values for order. Returns a negative integer, zero, or a 
     * positive integer as the first value is less than, equal to, or greater 
     * than the second value.
     * 
     * @param first
     * @param second
     * 
     * @return      a negative integer, zero, or a positive integer as the first 
     *              argument is less than, equal to, or greater than the second.
     */
    private static int compare(
        long first,
        long second
    ){
        return first == second ? 0 :
            first < second ? -1 : +1;
    }

    /**
     * Compares two double values for order. Returns a negative integer, zero, or a 
     * positive integer as the first value is less than, equal to, or greater 
     * than the second value.
     * 
     * @param first
     * @param second
     * 
     * @return      a negative integer, zero, or a positive integer as the first 
     *              argument is less than, equal to, or greater than the second.
     */
    private static int compare(
        double first,
        double second
    ){
        return first == second ? 0 :
            first < second ? -1 : +1;
    }


    //------------------------------------------------------------------------
    // Type tests
    //------------------------------------------------------------------------

    /**
     * 
     * @param number
     */
    private static boolean isAssignableToLong(
       Number number
    ){
        Class<? extends Number> type = number.getClass();
        return 
            type == Byte.TYPE ||
            type == Short.TYPE ||
            type == Integer.TYPE ||
            type == Long.TYPE;
    }    

    /**
     * 
     * @param number
     */
    private static boolean isAssignableToDouble(
        Number number
    ){
        Class<? extends Number> type = number.getClass();
        return 
            type == Float.TYPE ||
            type == Double.TYPE;
    }    


    //------------------------------------------------------------------------
    // Type conversions
    //------------------------------------------------------------------------

    /**
     * 
     * @param number
     */
    private static BigDecimal toBigDecimal(
        Number number
    ){
        return 
            number instanceof BigDecimal ? (BigDecimal)number : 
            number instanceof BigInteger ? new BigDecimal((BigInteger)number) :
            isAssignableToLong(number) ? BigDecimal.valueOf(number.longValue()) :
            isAssignableToDouble(number) ? new BigDecimal(number.doubleValue()) :
            new BigDecimal(number.toString());
    }
     
}
