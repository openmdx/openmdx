/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: NormalizingMarshaller.java,v 1.1 2008/09/25 23:38:24 hburger Exp $
 * Description: NormalizingMarshaller 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/25 23:38:24 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.marshalling.ReluctantUnmarshalling;
import org.openmdx.kernel.exception.BasicException;

/**
 * NormalizingMarshaller
 */
abstract class NormalizingMarshaller 
    implements Marshaller, ReluctantUnmarshalling 
{

    /**
     * Constructor 
     */
    protected NormalizingMarshaller(
    ) {
        // Avoid external initialization
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException {
        return normalize(source);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return normalize(source);
    }

    /**
     * Define the marshaller's target class
     * 
     * @return the marshaller's target class
     */
    protected abstract Class<?> targetClass(
    );

    /**
     * 
     * @return <code>true</code> if the marshaller accepts <code>String</code>s 
     * or <code>Number</code>s depending on the type.
     */
    protected abstract boolean isLenient();
        
    /**
     * Normalize the source value to the targetClass
     * 
     * @param targetClass
     * @param sourceValue
     * 
     * @return the normalized value
     */
    protected abstract Object normalize(
        Object source
    ) throws ServiceException;

    /**
     * Tells whether the <code>source</code> should be returned as is.
     * 
     * @param source the source
     * 
     * @return <code>true</code> if the <code>source</code> should be returned as is.
     * 
     * @throws ServiceException TRANSFORMATION_FAILURE if the marshaller is not 
     * lenient and and the source is not assignable to the target class.
     */
    protected boolean keep(
        Object source
    ) throws ServiceException {
        if(source == null || targetClass().isInstance(source)) {
            return true;
        }
        if(isLenient()) {
            return false;
        }
        throw newServiceException(null, source);
    }
        
    /**
     * Creates a transformation exception
     * 
     * @param exception
     * @param source
     * 
     * @return a transformation exception
     */
    protected ServiceException newServiceException(
        Exception exception,
        Object source
    ){
        return new ServiceException(
            exception,
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            "Could not normalize " + targetClass().getSimpleName(),
            new BasicException.Parameter("lenient", isLenient()),
            new BasicException.Parameter("target class", targetClass().getName()),
            new BasicException.Parameter("source class", source.getClass().getName()),
            new BasicException.Parameter("source value", source)
        );
    }

}
