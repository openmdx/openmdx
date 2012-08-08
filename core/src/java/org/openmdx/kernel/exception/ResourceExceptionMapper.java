/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ResourceExceptionMapper.java,v 1.5 2008/03/21 18:35:32 hburger Exp $
 * Description: Resource Exception Mapper
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:35:32 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.exception;

import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;

/**
 * Resource Exception Mapper
 */
@SuppressWarnings("unchecked")
final class ResourceExceptionMapper 
    implements BasicException.Mapper
{

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.BasicException.Mapper#map(java.lang.Throwable)
     */
    @SuppressWarnings("deprecation")
    public BasicException map(Throwable throwable) {
        ResourceException exception = (ResourceException) throwable;
        List parameters = new ArrayList();
        if (exception.getErrorCode() != null) parameters.add(
            new BasicException.Parameter(
                "errorCode",
                exception.getErrorCode()
            )
        );
        Throwable linkedException = exception.getLinkedException(); 
        return new BasicException(
            linkedException == null ? exception.getCause() : linkedException,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.GENERIC, //... Could be more specific
            (BasicException.Parameter[])parameters.toArray(
                new BasicException.Parameter[parameters.size()]
            ),
            exception.getMessage(),
            exception
        );
    }
    
}