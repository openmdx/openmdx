/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Duration.java,v 1.9 2008/09/10 08:55:25 hburger Exp $
 * Description: Duration class
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:25 $
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
package org.openmdx.base.time;

import java.math.BigDecimal;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.StringBuilders;


/**
 * A duration is specified in either months or seconds.
 * 
 * @deprecated
 */
public final class Duration {

    /**
     * Create a duration of time from two of the basic formats defined by
     * the ISO 8601 standard.
     * <ul>
     *   <li>Formats:
     * <pre>
     * "PnM" or "PTn.uS"
     * ^ ^      ^^ ^ ^ 
     *       
     * n  1 or more digits     [>= 0]
     * u  3-digit milliseconds [0..999]
     * </pre>
     *   <li>Examples:
     *     <ul> 
     *       <li>"P24M" represents 2 years
     *       <li>"P6M" represents 6 months
     *       <li>"PT172800.000S" represents 2 days
     *       <li>"PT1.500S" represents 1.5 seconds
     *     </ul>
     * </ul>
     */
    public Duration(
        String value
    ) throws ServiceException {
        try {
            if(value.charAt(0) != 'P') throw new NumberFormatException(
                "Duration values must start with 'P'"
            );
            switch (value.charAt(value.length()-2)) {
                case 'M':               
                    this.months = Integer.parseInt(
                        value.substring(1, value.length()-1)
                    );
                    this.milliseconds = 0;
                    break;
                case 'S':
                    if(value.charAt(1) != 'T') throw new NumberFormatException(
                        "Specification of seconds must start with 'T'"
                    );
                    this.months = 0;
                    this.milliseconds = new BigDecimal(
                        value.substring(2, value.length()-1)
                    ).movePointRight(3).longValue(); 
                    break;
                default: 
                    throw new NumberFormatException(
                        "Unit must be either month ('M') or second ('S')"
                    );
            }   
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.TRANSFORMATION_FAILURE,
                "A duration must be specified in either months ('PnM') or ('PTn.uS')",
                new BasicException.Parameter("value", value)
            );
        }
    }

    /**
     * Create a duration specfied by a specific number of months
     *
     * @param months  The duration expressed in months
     */
    public Duration(
        int months
    ) {
        this(months,0);
    }

    /**
     * Create a duration specfied by a specific number of milliseconds
     *
     * @param milliseconds    The duration expressed in milliseconds
     */
    public Duration(
        long milliseconds
    ) {
        this(0,milliseconds);
    }

    /**
     * This constructor is private as a combination of months and milliseconds
     * is not supported at the Moment
     */
    private Duration(
        int months,
        long milliseconds
    ) {
        this.months = months;
        this.milliseconds = milliseconds;
    }

    public int hashCode() {
        int hash = (int)milliseconds;
        return 31 * hash + months;
    }

    public boolean equals(
        Object object
    ){
        if (! (object instanceof Duration)) return false;
        final Duration that = (Duration)object;
        return 
        this.getMonths() == that.getMonths() && 
        this.getMilliseconds() == that.getMilliseconds();
    }

    public String toString(
    ){
        final CharSequence target = StringBuilders.newStringBuilder("P");
        if(getMonths() != 0) StringBuilders.asStringBuilder(
            target
        ).append(
            getMonths()
        ).append(
            'M'
        ); 
        if(getMilliseconds() != 0) StringBuilders.asStringBuilder(
            target
        ).append(
            'T'
        ).append(
            getMilliseconds()
        ).insert(
            target.length()-3, '.'
        ).append('S');
        return target.toString();
    }

    public long getMilliseconds(){
        return this.milliseconds;
    }

    public long getMonths(){
        return this.months;
    }

    private final int months;
    private final long milliseconds;

}

//--- End of File -----------------------------------------------------------
