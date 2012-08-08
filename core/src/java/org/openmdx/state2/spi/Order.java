/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Order.java,v 1.2 2009/02/16 11:55:25 hburger Exp $
 * Description: ValidTimes 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/16 11:55:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

package org.openmdx.state2.spi;

import java.util.Date;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.spi.DatatypeFactories;

/**
 * Valid Times
 */
public class Order {

    /**
     * Constructor 
     */
    private Order() {
        // Avoid instantiation
    }

    /**
     * Plus one day
     */
    public static final Duration ONE_DAY = DatatypeFactories.xmlDatatypeFactory(
    ).newDurationDayTime(
        true, // isPositive
        1, // day
        0, // hour
        0, // minute
        0 // second
    );
    /**
     * Minus one day
     */
    public static final Duration MINUS_ONE_DAY = DatatypeFactories.xmlDatatypeFactory(
    ).newDurationDayTime(
        false, // isPositive
        1, // day
        0, // hour
        0, // minute
        0 // second
    );

    
    //------------------------------------------------------------------------
    // Date States
    //------------------------------------------------------------------------
    
    /**
     * Compare two XMLGregorianCalendar values where <code>null</code> is
     * considered to be smaller than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        XMLGregorianCalendar d1,
        XMLGregorianCalendar d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : -1
        ) : (
            d2 == null ? 1 : d1.compare(d2)
        );
    }

    /**
     * Compare two XMLGregorianCalendar values where <code>null</code> is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidTo(
        XMLGregorianCalendar d1,
        XMLGregorianCalendar d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : d1.compare(d2)
        );
    }

    
    //------------------------------------------------------------------------
    // Date-Time States
    //------------------------------------------------------------------------
    
    /**
     * Compare two Date values where <code>null</code> is
     * considered to be smaller than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareValidFrom(
        Date d1,
        Date d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : -1
        ) : (
            d2 == null ? 1 : d1.compareTo(d2)
        );
    }

    /**
     * Compare two Date values where <code>null</code> is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareInvalidFrom(
        Date d1,
        Date d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : d1.compareTo(d2)
        );
    }

    
    //------------------------------------------------------------------------
    // Existence
    //------------------------------------------------------------------------

    /**
     * Compare two Date values where <code>null</code> is
     * considered to be greater than every other value.
     * 
     * @param d1
     * @param d2
     * 
     * @return a negative integer, zero, or a positive integer as d1 is less 
     * than, equal to, or greater than d2. 
     */
    public static int compareRemovedAt(
        Date d1,
        Date d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : d1.compareTo(d2)
        );
    }
    
    
    //------------------------------------------------------------------------
    // Adjacence
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the previous day
     * 
     * @param date
     * 
     * @return the previous day
     */
    public static XMLGregorianCalendar predecessor(
        XMLGregorianCalendar date
    ){
        XMLGregorianCalendar predecessor = (XMLGregorianCalendar) date.clone();
        predecessor.add(MINUS_ONE_DAY);
        return predecessor;
    }

    /**
     * Retrieve the next day
     * 
     * @param date
     * 
     * @return the next day
     */
    public static XMLGregorianCalendar successor(
        XMLGregorianCalendar date
    ){
        XMLGregorianCalendar successor = (XMLGregorianCalendar) date.clone();
        successor.add(ONE_DAY);
        return successor;
    }

}
