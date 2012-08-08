/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UUIDFormatter.java,v 1.9 2010/06/02 13:45:39 hburger Exp $
 * Description: UUID Formatter
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:45:39 $
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.text.format;

import java.util.UUID;

import javax.resource.ResourceException;

import org.openmdx.base.resource.Records;




/**
 * UUID Formatter
 */
public class UUIDFormatter {

    /**
     * Constructor
     * 
     * @param uuid
     */
    public UUIDFormatter(
        UUID uuid
    ){
        this.uuid = uuid;
    }

    /**
     * 
     */
    private final UUID uuid;

    /**
     * 
     */
    private static final String[] TIME_BASED_FIELDS = {
        "variant",
        "version",
        "node",
        "clockSequence",
        "timestamp"
    };
    
    /**
     * 
     */
    private static final String[] COMMON_FIELDS = {
        "variant",
        "version"
    };
    
    /**
     * Returns a string representation of the byte buffer
     *
     * @return  a String
     */
    @Override
    public String toString()
    {
        if(this.uuid == null) {
            return null;
        }
        boolean timeBased = this.uuid.version() == 1;
        try {
            return Records.getRecordFactory().asMappedRecord(
                this.uuid.getClass().getName(), // recordName, 
                this.uuid.toString(), // recordShortDescription, 
                timeBased ? TIME_BASED_FIELDS : COMMON_FIELDS, 
                timeBased ? new Object[]{
                    Integer.valueOf(this.uuid.variant()),
                    Integer.valueOf(this.uuid.version()),
                    Long.valueOf(this.uuid.node()),
                    Integer.valueOf(this.uuid.clockSequence()),
                    Long.valueOf(this.uuid.timestamp())
                 } : new Object[]{
                        Integer.valueOf(this.uuid.variant()),
                        Integer.valueOf(this.uuid.version()),
                 }
            ).toString();
        } catch (ResourceException exception) {
            return this.uuid.toString();
        }
    }

}
