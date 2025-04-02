/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lenient Datatype Comparator
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.query;

import java.util.Comparator;

import javax.xml.datatype.DatatypeConstants;
import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif
import org.w3c.spi2.Datatypes;

/**
 * Allows comparison of XML Datatype classes
 */
public class LenientDatatypeComparator extends LenientNumberComparator {

    /**
     * Use specific CharSequence comparator
     */
    protected LenientDatatypeComparator(
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
        if(Datatypes.DURATION_CLASS.isInstance(first)){
            Duration left = (Duration) first;
            Duration right;
            if(Datatypes.DURATION_CLASS.isInstance(second)) {
                right = (Duration) second;
            } else if (second instanceof CharSequence){
                right = Datatypes.create(Datatypes.DURATION_CLASS, second.toString());
            } else throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The second argument can't be compared to the first Duration argument",
                new BasicException.Parameter(
                    "values",
                    first,
                    second    
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second == null ? "<null>" : second.getClass().getName()   
                )
            );
            return toComparatorReply(
                first,
                second,
                left.#if CLASSIC_CHRONO_TYPES compare #else compareTo#endif(right)
            );
        }
        if (Datatypes.DATE_CLASS.isInstance(first)) {
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif left;
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif right;
            if(Datatypes.DATE_CLASS.isInstance(second)) {
                #if CLASSIC_CHRONO_TYPES
                if(first.getClass() == second.getClass()) {
                    left = Datatypes.DATE_CLASS.cast(first);
                    right = Datatypes.DATE_CLASS.cast(second);
                } else {
                    left = first instanceof org.w3c.cci2.ImmutableDate
                            ? ((org.w3c.cci2.ImmutableDate)first).clone()
                            : Datatypes.DATE_CLASS.cast(first);

                    right = second instanceof org.w3c.cci2.ImmutableDate
                            ? ((org.w3c.cci2.ImmutableDate)second).clone()
                            : Datatypes.DATE_CLASS.cast(second);
                }
                #else
                left = Datatypes.DATE_CLASS.cast(first);
                right = Datatypes.DATE_CLASS.cast(second);
                #endif
            } else if (second instanceof CharSequence){
                left = Datatypes.DATE_CLASS.cast(first);
                right = Datatypes.create(Datatypes.DATE_CLASS, second.toString());
            } else throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The second argument can't be compared to the first XMLGregorianCalendar argument",
                new BasicException.Parameter(
                    "values",
                    first,
                    second    
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second == null ? "<null>" : second.getClass().getName()   
                )
            );
            return toComparatorReply(
                first,
                second,
                left.#if CLASSIC_CHRONO_TYPES compare #else compareTo#endif(right)
            );
        }
        if (Datatypes.DATE_TIME_CLASS.isInstance(first)){
            #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif left = Datatypes.DATE_TIME_CLASS.cast(first);
            #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif right;
            if(Datatypes.DATE_TIME_CLASS.isInstance(second)) {
                right = Datatypes.DATE_TIME_CLASS.cast(second);
            } else if (second instanceof CharSequence){
                right = Datatypes.create(Datatypes.DATE_TIME_CLASS, second.toString());
            } else throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The second argument can't be compared to the first date-time argument",
                new BasicException.Parameter(
                    "values",
                    first,
                    second
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second == null ? "<null>" : second.getClass().getName()
                )
            );
            //
            // date comparison result values are equal to datatype result values
            //
            return toComparatorReply(
                first,
                second,
                left.compareTo(right) // date comparison result values are equal to datatype result values
            );
        }
        return super.compare(
            first, 
            second
        ); 
    }

    /**
     * Convert result to either a comparator return value or an exception
     * 
     * @param first the first value to be compared
     * @param second the second value to be compared
     * @param datatypeResult the data type comparison result value
     * 
     * @return the comparator return value
     */
    private int toComparatorReply(
        Object first,
        Object second,
        int datatypeResult
    ){
        switch (datatypeResult) {
            case DatatypeConstants.LESSER: return -1;
            case DatatypeConstants.EQUAL: return 0;
            case DatatypeConstants.GREATER: return 1;
            case DatatypeConstants.INDETERMINATE: throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_QUERY_CRITERIA,
                "The relationship between the two given values is indeterminate", 
                new BasicException.Parameter(
                    "values",
                    first,
                    second    
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second == null ? "<null>" : second.getClass().getName()   
                )
            );
            default: throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unexpected datatype compare result", 
                new BasicException.Parameter(
                    "values",
                    first,
                    second    
                ),
                new BasicException.Parameter(
                    "class",
                    first.getClass().getName(),
                    second == null ? "<null>" : second.getClass().getName()   
                ),
                new BasicException.Parameter(
                    "result",
                    datatypeResult
                )
            );
        }
    }
}
