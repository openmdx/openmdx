/*
 * ====================================================================
 * Project:     openMEX, http://www.openmdx.org/
 * Description: Naming Exception Mapper
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
package org.openmdx.kernel.naming;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.openmdx.kernel.exception.BasicException;

/**
 * Naming Exception Manager
 */
public class NamingExceptionMapper 
    implements BasicException.Mapper
{

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.BasicException.Mapper#map(java.lang.Throwable)
     */
    public BasicException map(
        Throwable throwable
    ) {
        if(throwable instanceof NamingException) {
            NamingException exception = (NamingException) throwable;
            List<BasicException.Parameter> parameters = new ArrayList<BasicException.Parameter>();
            amend(parameters, "explanation", exception.getExplanation());
            amend(parameters, "remainingName", exception.getRemainingName());
            amend(parameters, "resolvedName",exception.getResolvedName());
            Object resolvedObject = exception.getResolvedObj();
            if(resolvedObject != null) amend(parameters, "resolvedObjectClass",resolvedObject.getClass());
            return BasicException.toStackedException(
                exception.getRootCause(),
                exception,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.GENERIC, 
                null,  // description
                parameters.toArray(
                    new BasicException.Parameter[parameters.size()]
                )
            );
        } else {
            return null;
        }
    }
    
    /**
     * Add a parameter unless its value would be <code>null</code>
     * 
     * @param target
     * @param key
     * @param value
     */
    private static void amend(
        List<BasicException.Parameter> target,
        String key,
        Object value
    ){
        if(value != null) target.add(
            new BasicException.Parameter(key, value)
        );
    }

}