/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: StateComparator 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.state2.spi.Order;

/**
 * StateComparator
 */
public class StateComparator
    implements Comparator<DataObject_1_0>, Serializable
{

    /**
     * Constructor 
     */
    private StateComparator(){
        // Avoid external instantiation
    }

    /**
     * A singleton
     */
    private final static Comparator<DataObject_1_0> instance = new StateComparator();
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 8801286952504763272L;

    /**
     * Implements <code>Comparable</code>
     */
    public int compare(DataObject_1_0 o1, DataObject_1_0 o2) {
        if(o1.jdoIsDeleted()) {
            return o2.jdoIsDeleted() ? 0 : 1;
        }
        if(o2.jdoIsDeleted()) {
            return -1;
        }
        try {
            int validFrom = Order.compareValidFrom(
                (XMLGregorianCalendar)o1.objGetValue("stateValidFrom"),
                (XMLGregorianCalendar)o2.objGetValue("stateValidFrom")
            ); 
            if(validFrom != 0) {
                return validFrom;
            }
            int removedAt = Order.compareRemovedAt(
                (Date)o1.objGetValue(SystemAttributes.REMOVED_AT),
                (Date)o2.objGetValue(SystemAttributes.REMOVED_AT)
            );  
            return removedAt;
        } catch (ServiceException exception) {
            throw new IllegalArgumentException(
                "The two data objects can't be compared",
                exception
            );
        }
    }

    /**
     * Retrieve an instance of this class
     * 
     * @return the singleton
     */
    public static Comparator<DataObject_1_0> getInstance(){
        return instance;
    }

}
