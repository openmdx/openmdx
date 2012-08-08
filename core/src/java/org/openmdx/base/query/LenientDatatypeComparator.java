/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LenientDatatypeComparator.java,v 1.8 2010/06/02 13:45:10 hburger Exp $
 * Description: Lenient Datatype Comparator
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:45:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.base.query;

import java.util.Comparator;
import java.util.Date;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.ImmutableDate;
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
        if(first instanceof Duration){
            Duration left = (Duration) first;
            Duration right;
            if(second instanceof Duration) {
                right = (Duration) second;
            } else if (second instanceof CharSequence){
                right = Datatypes.create(Duration.class, second.toString());
            } else throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The second argument can't be compared to the the first Duration argument", 
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
                left.compare(right)
            );
        }
        if (first instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar left;
            XMLGregorianCalendar right;
            if(second instanceof XMLGregorianCalendar) {
                if(first.getClass() == second.getClass()) {
                    left = (XMLGregorianCalendar) first;
                    right = (XMLGregorianCalendar) second;
                } else {
                    left = first instanceof ImmutableDate ? ((ImmutableDate)first).clone() : (XMLGregorianCalendar)first;
                    right = second instanceof ImmutableDate ? ((ImmutableDate)second).clone() : (XMLGregorianCalendar)second;
                }
            } else if (second instanceof CharSequence){
                left = (XMLGregorianCalendar) first;
                right = Datatypes.create(XMLGregorianCalendar.class, second.toString());
            } else throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The second argument can't be compared to the the first XMLGregorianCalendar argument", 
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
                left.compare(right)
            );
        }
        if (first instanceof Date){
            Date left = (Date) first;
            Date right;
            if(second instanceof Date) {
                right = (Date) second;
            } else if (second instanceof CharSequence){
                right = Datatypes.create(Date.class, second.toString());
            } else throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The second argument can't be compared to the the first date-time argument", 
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
     * @param the first value to be compared
     * @param the second value to be compared
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
