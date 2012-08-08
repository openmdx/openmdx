/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ShortMarshaller.java,v 1.7 2007/10/10 16:05:52 hburger Exp $
 * Description: ShortMarshaller class
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:52 $
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
package org.openmdx.base.accessor.jmi.spi;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;


//---------------------------------------------------------------------------
/**
 * Number <-> Short marshaller. Marshals objects which are instance of
 * Number to the specific type Short (which is also a Number).
 */
public class ShortMarshaller implements Marshaller {

    //-------------------------------------------------------------------------
    private ShortMarshaller() {
        super();
    }

    //-------------------------------------------------------------------------
    public static ShortMarshaller getInstance(boolean forward) {
        return ShortMarshaller.instance;
    }

    //-------------------------------------------------------------------------
    public Object marshal(
        Object source
    ) throws ServiceException {
        try {
            return source == null
                ? null
                : new Short(((Number) source).shortValue());
        } 
    catch(RuntimeException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("source", source),
                    new BasicException.Parameter(
                        "source class",
                        source.getClass().getName()),
                    },
                "Could not marshal source to Short");
        }
    }

    //-------------------------------------------------------------------------
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        try {
            return source == null
                ? null
                : new Short(((Number) source).shortValue());
        } 
    catch (RuntimeException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("source", source),
                    new BasicException.Parameter(
                        "source class",
                        source.getClass().getName()),
                    },
                "Could not marshal source to Short");
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    static private ShortMarshaller instance = new ShortMarshaller();

}

//--- End of File -----------------------------------------------------------
