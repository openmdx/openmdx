/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Duration Marshaller
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.accessor.spi;

import javax.xml.datatype.Duration;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.w3c.spi.DatatypeFactories;

/**
 * Duration Marshaller
 */
public class DurationMarshaller {

    /**
     * Constructor 
     */
    private DurationMarshaller(
    ) {
        // Avoid instantiation
    }

    /**
     * Datatype Instance <-> ISO 8601 Basic String
     */
    public static final Marshaller BASIC_FORMAT_TO_DATATYPE = new DatatypeMarshaller(
        Duration.class
    ){

        @Override
        protected String toBasicFormat(Object datatype) {
            return ((Duration)datatype).toString();
        }
        
    };

    /**
     * Normalizing Marshaller
     */
    public static final Marshaller NORMALIZE = new NormalizingMarshaller(
        Duration.class
    ){

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.NormalizingMarshaller#normalize(java.lang.Object)
         */
        @Override
        protected Object normalize(
            Object source
        ) throws ServiceException {
            return source instanceof Duration ? DatatypeFactories.immutableDatatypeFactory().toNormalizedDuration(
                (Duration)source
            ) : super.normalize(
                source
            );
        }
        
    };
    
}
