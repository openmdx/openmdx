/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: URIMarshaller.java,v 1.1 2008/09/17 15:27:23 hburger Exp $
 * Description: URI Marshaller class
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/17 15:27:23 $
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
package org.openmdx.base.accessor.jmi.spi;

import java.net.URI;
import java.net.URISyntaxException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.marshalling.ReluctantUnmarshalling;
import org.openmdx.kernel.exception.BasicException;


//---------------------------------------------------------------------------
/**
 * Marshals Object -> Date and Date -> Object. Object must be instanceof String.
 */
public class URIMarshaller
    implements Marshaller, ReluctantUnmarshalling 
{

    //-------------------------------------------------------------------------
    private URIMarshaller(
        boolean forward
    ) {
        this.forward = forward;
    }

    //-------------------------------------------------------------------------
    public static URIMarshaller getInstance(
        boolean forward
    ) {
        return forward
        ? URIMarshaller.toMarshaller
            : URIMarshaller.fromMarshaller;
    }

    //-------------------------------------------------------------------------
    public Object marshalGeneric(
        Object source,
        boolean forward
    ) throws ServiceException {
        if(source == null) {
            return null;
        }
        if(forward) {
            try {
                return new URI((String)source);
            } catch (URISyntaxException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.TRANSFORMATION_FAILURE, 
                    "exception parsing URI",
                    new BasicException.Parameter [] {
                        new BasicException.Parameter("source", source),
                        new BasicException.Parameter("source class", source.getClass().getName()),
                    }
                );  
            }
        } else {
            if(source instanceof URI) {
                return ((URI)source).toString();
            }else {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.TRANSFORMATION_FAILURE, 
                    "Can only unmarshal objects of type " + URI.class.getName(),
                    new BasicException.Parameter("source", source),
                    new BasicException.Parameter("source class", source.getClass().getName())
                );  
            }
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Object marshal(
        Object source
    ) throws ServiceException {
        return marshalGeneric(
            source, 
            this.forward
        );
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Object unmarshal (
        Object source
    ) throws ServiceException {
        return marshalGeneric(
            source, 
            !this.forward
        );
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final boolean forward;

    static private final URIMarshaller toMarshaller = new URIMarshaller(true);
    static private final URIMarshaller fromMarshaller = new URIMarshaller(false);

}

//--- End of File -----------------------------------------------------------
