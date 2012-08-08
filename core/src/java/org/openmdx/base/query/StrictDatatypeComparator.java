/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StrictDatatypeComparator.java,v 1.4 2008/09/10 08:55:30 hburger Exp $
 * Description: Abstract Filter Class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:30 $
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
package org.openmdx.base.query;

import java.util.Comparator;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;


/**
 * Allows comparison of XML Datatype classes
 */
@SuppressWarnings("unchecked")
public class StrictDatatypeComparator extends LenientNumberComparator {

    /**
     * Factory for a LenientNumberComparator using the default CharSequence comparator
     * 
     * @return  a lenient comparator using the default CharSequence comparator
     */
    public static Comparator getInstance(
    ){
        return StrictDatatypeComparator.instance;
    }

    /**
     * Use specific CharSequence comparator
     */
    public StrictDatatypeComparator(
        Comparator charSequenceComparator
    ) {
        super(charSequenceComparator);
    }

    /**
     * 
     */
    private static final Comparator instance = new StrictDatatypeComparator(null);


    //------------------------------------------------------------------------
    // Implements Comparator
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(
        Object first, 
        Object second
    ) {    
        boolean duration = first instanceof Duration; 
        if(duration || first instanceof XMLGregorianCalendar
        ) try {
            if(!first.getClass().isInstance(second)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The two classes are not comparable",
                new BasicException.Parameter(
                    "classes",
                    first == null ? null : first.getClass().getName(),
                        second == null ? null : second.getClass().getName()    
                )
            );
            int result = duration ?   
                ((Duration)first).compare((Duration)second) :
                    ((XMLGregorianCalendar)first).compare((XMLGregorianCalendar)second); 
                switch (result) {
                    case DatatypeConstants.LESSER: return -1;
                    case DatatypeConstants.EQUAL: return 0;
                    case DatatypeConstants.GREATER: return 1;
                    case DatatypeConstants.INDETERMINATE: throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_QUERY_CRITERIA,
                        "The relationship between the two given values is indeterminate", 
                        new BasicException.Parameter(
                            "values",
                            first.toString(),
                            second.toString()    
                        )
                    );
                    default: throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unexpected compare result", 
                        new BasicException.Parameter(
                            "result",
                            result
                        )
                    );
                }
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        } else {
            return super.compare(first, second); 
        }            
    }

}
