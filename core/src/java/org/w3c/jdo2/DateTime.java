/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DateTime.java,v 1.1 2007/05/13 16:47:45 hburger Exp $
 * Description: DateTime 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/13 16:47:45 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.w3c.jdo2;

/**
 * DateTime
 */
public class DateTime {

    /**
     * Avoid instantiation 
     */
    protected DateTime() {
    }

    /**
     * Convert a <code>java.util.Date</code> to an <code>SQL TIMESTAMP</code>
     * 
     * @param cciDateTime the <code>java.util.Date</code> to be converted
     * 
     * @return a corresponding <code>SQL TIMESTAMP</code>
     */
    public static final java.sql.Timestamp toJDO (
        java.util.Date cciDateTime
    ){
        return cciDateTime == null ? null : new java.sql.Timestamp(
            cciDateTime.getTime()
        );
    }

    /**
     * Convert an <code>SQL TIMESTAMP</code> to a <code>java.util.Date</code>
     * 
     * @param jdoDateTime the <code>SQL TIMESTAMP</code to be converted
     * 
     * @return a corresponding <code>java.util.Date</code>
     */
    public static final java.util.Date toCCI (
        java.sql.Timestamp jdoDateTime
    ){
        return jdoDateTime == null ? null : new java.util.Date(
            jdoDateTime.getTime()
        );
    }
    
}
