/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DateStateViewContext.java,v 1.6 2008/02/29 14:42:02 hburger Exp $
 * Description: Date State View
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 14:42:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.state1.view;

import java.util.Date;

import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;

/**
 * Date State View Context
 */
class DateStateViewContext 
    implements InteractionSpec, DateStateContext 
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7926199723788579722L;

    /**
     * Constructor 
     *
     * @param stateCache
     * @param validFor
     * @param validAt
     */
    DateStateViewContext(
        XMLGregorianCalendar validFor,
        Date validAt
    ) {
        this(
            validFor, 
            validAt,
            null, // validFrom
            null, // validTo
            false // writable
        );
    }

    /**
     * Constructor 
     * 
     * @param stateCache
     * @param validFor
     * @param validAt
     * @param readable
     */
    DateStateViewContext(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) {
        this(
            null, // validFor
            null, // validAt
            validFrom, 
            validTo,
            true // writable
        );
    }

    /**
     * Constructor
     * 
     * @param validFor
     * @param validAt
     * @param validFrom 
     * @param validTo 
     * @param writable
     */
    private DateStateViewContext(
        XMLGregorianCalendar validFor,
        Date validAt,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo, 
        boolean writable
     ) {
        StringBuilder id = new StringBuilder();
        if(validFor != null || validAt != null) {
            id.append(
                validFor == null ? "?" : validFor.toXMLFormat()
            ).append(
                validAt == null ? "" : '@' + DateFormat.getInstance().format(validAt)
            );
        }
        if(validFrom != null || validTo != null) {
            id.append(
                '['
            ).append(
                validFrom == null ? State_1_Attributes.OP_VAL_EVER : validFrom.toXMLFormat()
            ).append(
                '/'
            ).append(
                validTo == null ? State_1_Attributes.OP_VAL_EVER : validTo.toXMLFormat()
            ).append(
                ']'
            );
        }
        this.id = id.toString();
        this.validFrom = validFrom;
        this.validFor = validFor;
        this.validTo = validTo;
        this.validAt = validAt;
        this.writable = writable;
    }

    /**
     * 
     */
    private final String id;
    
    /**
     * Defines for which date on changes are valid
     */
    private final XMLGregorianCalendar validFrom;

    /**
     * Defines for which date the associated states are valid
     */
    private final XMLGregorianCalendar validFor;

    /**
     * Defines up to which date changes are valid
     */
    private final XMLGregorianCalendar validTo;

    /**
     * Defines up to which date and time changes are reflected
     */
    private final Date validAt;
    
    /**
     * Defines whether the DateState view is writable
     */
    private final boolean writable;

    
    //------------------------------------------------------------------------
    // Implements DateStateContext
    //------------------------------------------------------------------------
    
    /**
     * Retrieve validFrom.
     *
     * @return Returns the validFrom.
     */
    public final XMLGregorianCalendar getValidFrom(
    ) {
         return this.validFrom;
    }        

    /**
     * Retrieve validTo.
     *
     * @return Returns the validTo.
     */
    public final XMLGregorianCalendar getValidTo(
    ) {
         return this.validTo;
    }        
     
    /**
     * Retrieve validFor.
     *
     * @return Returns the validFor.
     */
    public final XMLGregorianCalendar getValidFor() {
        return this.validFor;
    }
    
    /**
     * Retrieve validAt.
     *
     * @return Returns the validAt.
     */
    public final Date getValidAt() {
        return this.validAt;
    }

    /**
     * Retrieve writable.
     *
     * @return Returns the writable.
     */
    public final boolean isWritable() {
        return this.writable;
    }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(
        Object obj
    ) {
        return 
            obj instanceof DateStateViewContext &&
            this.id.equals(obj.toString());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.id.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.id;
    }

}