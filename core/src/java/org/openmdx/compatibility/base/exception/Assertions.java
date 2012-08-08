/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Assertions.java,v 1.7 2004/07/11 19:15:54 hburger Exp $
 * Description: Exception Framework 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/11 19:15:54 $
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
package org.openmdx.compatibility.base.exception;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;


/**
 * @deprecated without replacement
 */
public class Assertions 
{ 

    /**
     * Constructor
     * 
     * @deprecated without replacement
     */
    private Assertions(
    ) {
        // This class has no instances
    }

    /**
     * Throws a ServiceError if an assertion fails.
     * 
     * @param   assertion   the condition expected to be true.
     * @param   description description of the assertion;
     *                      or null
     * 
     * @exception   ServiceError    if assertion is false
     * 
     * @deprecated without replacement
     */
    public static void assertAtErrorLevel (
        boolean assertion,
        String description
    ) {
        if(assertion)return;
        throw toError(
            new Error("ASSERTION_FAILURE: " + description),
            description
        );
    }
    
    /**
     * Throws an RuntimeServiceException if an assertion fails and logs the fact
     * at ERROR_LEVEL.
     * 
     * @param   assertion   the condition expected to be true.
     * @param   description description of the assertion;
     *                      or null
     * 
     * @exception   RuntimeServiceException if assertion is false
     * 
     * @deprecated without replacement
     */
    public static void assertAtExceptionLevel (
        boolean assertion,
        String description
    ) {
        if(assertion)return;
        ServiceException exception = new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.ASSERTION_FAILURE,
            null, 
            description
        );
        SysLog.error(
            exception.getMessage(),
            exception.getExceptionStack()
        );
        throw new RuntimeServiceException(exception);
    }
    
    /**
     * Maps a throwable to a ServiceError unless it is itself an instance of
     * Error. This method is used when the program expects never to have
     * to catch such a throwable.
     * <p>
     *   Usage:
     *   <pre>   
     *     public methodName (ParameterClass parameterValue) {
     *         try {
     *             ...
     *         } catch (Throwable throwable) {
     *             // This statement is expected never to be reached    
     *             throw Assertions.toError (exception);
     *         } 
     *     }
     *   </pre>
     * 
     * @param   throwable   the unexpected exception
     * 
     * @return  the throwable if it's an Error;
     *          a ServiceError containing the throwable otherwise
     * 
     * @deprecated without replacement
     */
    public static Error toError (
        Throwable throwable
    ) {
        return toError(
            throwable,
            "Program error: Unexpected " + throwable.getClass().getName()
        );
    }

    /**
     * Maps a throwable to an Error
     * 
     * @param   throwable   the unexpected exception
     * @param   domain      the exception domain
     * @param   errorCode   the error code
     * @param   description a description of the error situation
     * 
     * @return  the throwable if it's an Error;
     *          a ServiceError containing the throwable otherwise
     * 
     * @deprecated without replacement
     */
    public static Error toError (
        Throwable throwable,
        String description
    ) {
        try {
            SysLog.error(
                "java.lang.Error: " + throwable.getMessage(),
                new BasicException(
                    throwable,
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    description
                )
            );
        } catch (Exception exception){
            // Ignore exception
        }
        return throwable instanceof Error ?
            (Error)throwable :
            new Error(description);
    }

}
