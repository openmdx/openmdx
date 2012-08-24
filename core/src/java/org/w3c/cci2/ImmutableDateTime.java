/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Unmodifiable Date-Time 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2011, OMEX AG, Switzerland
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
package org.w3c.cci2;

import java.io.ObjectStreamException;
import java.util.Date;

import org.w3c.format.DateTimeFormat;


/**
 * Unmodifiable Date-Time
 */
public final class ImmutableDateTime
    extends Date
    implements ImmutableDatatype<Date>
{

    /**
     * Constructor 
     *
     * @param date
     */
    public ImmutableDateTime(
        long date
    ) {
        super(date);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6757337127635200014L;

    /**
     * Value in ISO8601:2000 Basic Format
     */
    private transient String basicValue;

    /**
     * Value in SO8601:2000 Extended Format
     */
    private transient String extendedValue;
    
    /**
     * 
     */
    private static final String READONLY = 
        "This " + Date.class.getName() + 
        " instance is read-only, use clone() to get a modifiable copy.";
    
    
    /* (non-Javadoc)
     * @see java.util.Date#setDate(int)
     */
    @Override
    public void setDate(int date) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see java.util.Date#setHours(int)
     */
    @Override
    public void setHours(int hours) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see java.util.Date#setMinutes(int)
     */
    @Override
    public void setMinutes(int minutes) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see java.util.Date#setMonth(int)
     */
    @Override
    public void setMonth(int month) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see java.util.Date#setSeconds(int)
     */
    @Override
    public void setSeconds(int seconds) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see java.util.Date#setTime(long)
     */
    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException(READONLY);
    }

    /* (non-Javadoc)
     * @see java.util.Date#setYear(int)
     */
    @Override
    public void setYear(int year) {
        throw new UnsupportedOperationException(READONLY);
    }

	/**
	 * Create a mutable counterpart
	 * 
	 * @return the mutable counterpart
	 */
	private Date toMutableDateTime() {
		return new Date(this.getTime());
	}

	
    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------
    
    /**
     * There is no need for the deserialized object to be immutable
     * 
     * @return a mutable counterpart of this object
     * 
     * @throws ObjectStreamException
     */
    private Object writeReplace() throws ObjectStreamException {
        return toMutableDateTime();
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.Date#clone()
     */
    @Override
    public Date clone(
    ) {
        return toMutableDateTime();
    }


    //------------------------------------------------------------------------
    // Implements ImmutableDatatype
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.w3c.spi.ImmutableDatatype#toBasicFormat()
     */
    public String toBasicFormat() {
        if(this.basicValue == null){
            this.basicValue = DateTimeFormat.BASIC_UTC_FORMAT.format(this);
        }
        return this.basicValue;
    }

    /* (non-Javadoc)
     * @see org.w3c.spi.ImmutableDatatype#toXMLFormat()
     */
    public String toXMLFormat() {
        if(this.extendedValue == null) {
            this.extendedValue = DateTimeFormat.EXTENDED_UTC_FORMAT.format(this);
        }
        return this.extendedValue;
    }
    
}
