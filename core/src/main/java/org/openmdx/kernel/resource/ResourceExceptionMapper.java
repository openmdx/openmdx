/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Resource Exception Mapper
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.resource;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.openmdx.kernel.exception.BasicException;

/**
 * Resource Exception Mapper
 */
public class ResourceExceptionMapper 
    implements BasicException.Mapper
{

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.BasicException.Mapper#map(java.lang.Throwable)
     */
    public BasicException map(
        Throwable throwable
    ) {
        if(throwable instanceof ResourceException) {
            ResourceException exception = (ResourceException) throwable;
            String errorCode = exception.getErrorCode();
            return BasicException.toStackedException(
                getCause(exception),
                exception,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.GENERIC, 
                null, 
                errorCode == null ? new BasicException.Parameter[]{
                } : new BasicException.Parameter[]{
                	new BasicException.Parameter("errorCode", errorCode)
                }
            );
        } else {
            return null;
        }
    }

    /**
     * Retrieve the cause
     * 
     * @param exception
     * 
     * @return the cause
     */
    @SuppressWarnings("deprecation")
    private static Throwable getCause(
        ResourceException exception
    ){
        Throwable cause = exception.getCause();
        return cause == null ? exception.getLinkedException() : cause;
    }

}