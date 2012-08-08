/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ImmutableDatatypeFactory.java,v 1.2 2009/10/19 12:40:35 hburger Exp $
 * Description: Immutable Datatype Factory 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/19 12:40:35 $
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
package org.w3c.spi;

import java.util.Date;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Immutable Datatype Factory
 */
public interface ImmutableDatatypeFactory {

    //------------------------------------------------------------------------
    // From String Representation
    //------------------------------------------------------------------------
    
    /**
     * Create an UTC based immutable date-time instance
     * 
     * @param value the basic or extended representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value can't be parse
     */
    Date newDateTime(
        String value
    );
    
    /**
     * Create a new immutable date instance
     * 
     * @param value the basic or extended representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value can't be parse
     */
    XMLGregorianCalendar newDate(
        String value
    );

    /**
     * Create a new immutable duration instance
     * 
     * @param value the representation with designators
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value can't be parse
     */
    Duration newDuration(
        String value
    );

    
    //------------------------------------------------------------------------
    // From Internal Representation
    //------------------------------------------------------------------------
    
    /**
     * Retrieve an immutable date-time instance
     * 
     * @param value an internal representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::dateTime instance
     */
    Date toDateTime(
        Date value
    );
    
    /**
     * Retrieve an immutable date instance
     * 
     * @param value an internal representation
     * 
     * @return a corresponding date-time instance
     * 
     * @exception IllegalArgumentException
     * if the value is not an org::w3c::date instance
     */
    XMLGregorianCalendar toDate(
        XMLGregorianCalendar value
    );

    /**
     * Retrieve a normalized duration instance
     * 
     * @param value an internal representation
     * 
     * @return a duration instance containing seconds and months only
     */
    Duration toNormalizedDuration(
        Duration value
    );

}
