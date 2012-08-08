/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StackedException.java,v 1.10 2008/09/10 08:55:22 hburger Exp $
 * Description: Stacked Exceptions
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:22 $
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

import java.util.Date;

import org.openmdx.kernel.enumeration.IntegerEnumeration;
import org.openmdx.kernel.exception.BasicException;




/**
 * The StackedException implements stackable exceptions. Stackable exceptions
 * are useful to avoid hidden exceptions and to allow for remapping exceptions
 * while keeping the complete exception stack.
 *
 * <p>Instead of defining a central resource defining error codes the
 * <code>StackedException</code> follows the approach of a decentralized error
 * code management using error domains. An organization may define as many error
 * domains as required. Each error domain defines it's own error codes as
 * positive values starting with the value of '1'. An error code itself is not
 * unique, uniqness is only achieved together with an error domain.
 *
 * <p>To simplify things a number of common error codes are defined that are
 * are shared within all error domains. These error codes have negative values.
 * The <code>StackedException</code> provides error code mappers to map error
 * codes from foreign domains to strings. This mappers are used when requesting
 * a stringified form of the exception.
 *
 * <p>To enable interoperation with application domain specific exceptions
 * exception mappers are used to convert foreign exceptions to stacked
 * exceptions. These mappers are implicitely called when creating a new
 * <code>StackedException</code> passing an embedded exception that is not
 * of type <code>StackedException</code>.
 *
 * <p>A sample demonstrating the application of exception mappers in Corba
 * applications:
 *
 * <pre>
 *    Corba Server                         Corba Client
 *    -----------------------------------  -----------------------------------
 *    :    :
 *    1) throw StackedException
 *    2) convert to Corba User Exception
 *    3) throw Corba User Exception
 *
 *                                          4) catch Corba User Exception
 *                                          5) create StackedException passing
 *                                             the Corba Exception -> invokes
 *                                             the Corba Exception mapper
 *                                          6) throw StackException
 *                                          :    :
 * </pre>
 *
 * <p>Each <code>StackedException</code> provides several information elements:
 * <ul>
 * <li> an error domain
 * <li> an error code
 * <li> exception parameters
 * <li> a description
 * <li> an embedded exception (optionally)
 * </ul>
 *
 * @see StackedException
 */
public final class StackedException
    extends BasicException
{
    /**
     * 
     */
    private static final long serialVersionUID = 3257285816395837490L;

    /**
     * The default domain. Use this domain if there is no specific error domain
     * defined and the error codes are negativ.
     */
    public static final String DEFAULT_DOMAIN = BasicException.Code.DEFAULT_DOMAIN;
    
    /**
     * No error condition
     */
    public static final int NONE = BasicException.Code.NONE;
    
    /**
     * An ABORT exception is thrown to report that a non-transactional unit of
     * work has been aborted.
     */
    public static final int ABORT = BasicException.Code.ABORT;
    
    /**
     * Activation failure
     */
    public static final int ACTIVATION_FAILURE = BasicException.Code.ACTIVATION_FAILURE;
    
    /**
     * Assertion error.
     * <p>
     * Assertion errors signal "unexpected" exceptions such as programming
     * errors.
     */
    public static final int ASSERTION_FAILURE = BasicException.Code.ASSERTION_FAILURE;
    
    /**
     * authentication failure.
     */
    public static final int AUTHENTICATION_FAILURE = BasicException.Code.AUTHENTICATION_FAILURE;
    
    /**
     * authorization failure.
     */
    public static final int AUTHORIZATION_FAILURE = BasicException.Code.AUTHORIZATION_FAILURE;
    
    /**
     * Error processing context
     */
    public static final int BAD_CONTEXT = BasicException.Code.BAD_CONTEXT;
    
    /**
     * Non-existing attribute or property.
     */
    public static final int BAD_MEMBER_NAME = BasicException.Code.BAD_MEMBER_NAME;
    
    /**
     * An error code that signals bad/inconsistent parameters.
     */
    public static final int BAD_PARAMETER = BasicException.Code.BAD_PARAMETER;
    
    /**
     * Wrong or bad resource.
     */
    public static final int BAD_RESOURCE = BasicException.Code.BAD_RESOURCE;
    
    /**
     * Bad query criteria.
     */
    public static final int BAD_QUERY_CRITERIA = BasicException.Code.BAD_QUERY_CRITERIA;
    
    /**
     * Invalid/Unexpected length (nr of elements) of a sequence.
     */
    public static final int BAD_SEQUENCE_LENGTH = BasicException.Code.BAD_SEQUENCE_LENGTH;
    
    /**
     * A bind to an object or service failed
     */
    public static final int BIND_FAILURE = BasicException.Code.BIND_FAILURE;
    
    /**
     * A communication failure
     */
    public static final int COMMUNICATION_FAILURE = BasicException.Code.COMMUNICATION_FAILURE;
    
    /**
     * A concurrent access error condition.
     */
    public static final int CONCURRENT_ACCESS_FAILURE = BasicException.Code.CONCURRENT_ACCESS_FAILURE;
    
    /**
     * The creation of a resource failed
     */
    public static final int CREATION_FAILURE = BasicException.Code.CREATION_FAILURE;
    
    /**
     * Transformation failure
     */
    public static final int DATA_CONVERSION = BasicException.Code.TRANSFORMATION_FAILURE;
    
    /**
     * Deactivation failure
     */
    public static final int DEACTIVATION_FAILURE = BasicException.Code.DEACTIVATION_FAILURE;
    
    /**
     * Duplicate element
     */
    public static final int DUPLICATE = BasicException.Code.DUPLICATE;
    
    /**
     * This code is used for technical error conditions such as
     * NullPointerExceptions, ClassNotFoundExceptions, ... .
     */
    public static final int GENERIC = BasicException.Code.GENERIC;
    
    /**
     * A HEURISTIC exception is thrown by the commit operation to indicate that
     * a heuristic decision was made and that some relevant updates have been
     * committed and others have been rolled back.
     */
    public static final int HEURISTIC = BasicException.Code.HEURISTIC;
    
    /**
     * Signals that a method has been invoked at an illegal or
     * inappropriate time. In other words, the environment or application
     * is not in an appropriate state for the requested operation.
     */
    public static final int ILLEGAL_STATE = BasicException.Code.ILLEGAL_STATE;
    
    /**
     * A initialization failure
     */
    public static final int INITIALIZATION_FAILURE = BasicException.Code.INITIALIZATION_FAILURE;
    
    /**
     * Invalid cardinality.
     */
    public static final int INVALID_CARDINALITY = BasicException.Code.INVALID_CARDINALITY;
    
    /**
     * Invalid configuration.
     */
    public static final int INVALID_CONFIGURATION = BasicException.Code.INVALID_CONFIGURATION;
    
    /**
     * Transformation failure
     */
    public static final int MARSHAL_FAILURE = BasicException.Code.TRANSFORMATION_FAILURE;
    
    /**
     * Persistent media access error. Files, databases, or any other external
     * resource cannot be accessed
     */
    public static final int MEDIA_ACCESS_FAILURE = BasicException.Code.MEDIA_ACCESS_FAILURE;
    
    /**
     * A resource does not exist.
     */
    public static final int NO_RESOURCE = BasicException.Code.NO_RESOURCE;
    
    /**
     * Information is not available.
     * <p>
     * This exception code means that the request itself is valid but
     * the requested data is not available at the moment.
     * (A specific stock quote for example might be unavailable due to the
     * fact that corresponding market is not opened yet.)
     */
    public static final int NOT_AVAILABLE = BasicException.Code.NOT_AVAILABLE;
    
    /**
     * An information or resource could not be found.
     */
    public static final int NOT_FOUND = BasicException.Code.NOT_FOUND;
    
    /**
     * Unimplemented operation or action.
     */
    public static final int NOT_IMPLEMENTED = BasicException.Code.NOT_IMPLEMENTED;
    
    /**
     * No response available
     */
    public static final int NO_RESPONSE = BasicException.Code.NO_RESPONSE;
    
    /**
     * Unsupported operation or action.
     */
    public static final int NOT_SUPPORTED = BasicException.Code.NOT_SUPPORTED;
    
    /**
     * Parse error
     */
    public static final int PARSE_FAILURE = BasicException.Code.PARSE_FAILURE;
    
    /**
     * Processing failure
     */
    public static final int PROCESSING_FAILURE = BasicException.Code.PROCESSING_FAILURE;
    
    /**
     * The resource usage exceeded the allowed range
     */
    public static final int QUOTA_EXCEEDED = BasicException.Code.QUOTA_EXCEEDED;
    
    /**
     * A ROLLBACK exception is thrown when the transaction has been marked for
     * rollback only or the transaction has been rolled back instead of
     * committed.
     */
    public static final int ROLLBACK = BasicException.Code.ROLLBACK;
    
    /**
     * A security failure
     */
    public static final int SECURITY_FAILURE = BasicException.Code.SECURITY_FAILURE;
    
    /**
     * A system exception (Corba, EJB, ...)
     */
    public static final int SYSTEM_EXCEPTION = BasicException.Code.SYSTEM_EXCEPTION;
    
    /**
     * A timeout.
     */
    public static final int TIMEOUT = BasicException.Code.TIMEOUT;
    
    /**
     * The result of a query is too large to be handled.
     * <p>
     * This exception is thrown when a method produces a result that
     * exceeds a size-related limit. This can happen, for example, when
     * the size of the result exceeds some implementation-specific limit.
     */
    public static final int TOO_LARGE_RESULT_SET = BasicException.Code.TOO_LARGE_RESULT_SET;
    
    /**
     * The presence of a "TOO_MANY_EVENT_LISTENERS"
     * clause on any given concrete implementation of the normally multicast
     * "void addEventListener" event listener registration pattern is used
     * to annotate that interface as implementing a unicast Listener special
     * case, that is, that one and only one Listener may be registered on the
     * particular event listener source concurrently.
     */
    public static final int TOO_MANY_EVENT_LISTENERS = BasicException.Code.TOO_MANY_EVENT_LISTENERS;
    
    /**
     * A transaction failure. Used with severe transaction manager problems. For
     * all other causes use specific exceptions like
     * <code>CONCURRENT_ACCESS_FAILURE</code> or <code>TIMEOUT</code>
     *
     * @see #CONCURRENT_ACCESS_FAILURE
     * @see #TIMEOUT
     */
    public static final int TRANSACTION_FAILURE = BasicException.Code.TRANSACTION_FAILURE;
    
    /**
     * Validation failure
     */
    public static final int VALIDATION_FAILURE = BasicException.Code.VALIDATION_FAILURE;


    /**
     * An <code>CodeMapper</code> implementation maps exception domain
     * specific exception codes to a string representation. A mapper must only
     * map positive exception domain specific exception codes. Negative
     * exception codes are mapped always by the default mapper that can't be
     * overruled.
     *
     * <p>Each exception domain can if it wishes so, register a mapper with the
     * <code>StackedException</code> class
     */
    public interface CodeMapper extends IntegerEnumeration.Mapper {
        //
    }

    /**
     * @param exceptionDomain
     * @param exceptionCode
     * @param parameters
     * @param description
     */
    public StackedException(
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description) {
        super(exceptionDomain, exceptionCode, description, parameters);
    }

    /**
     * @param throwable
     * @param exceptionDomain
     * @param exceptionCode
     * @param parameters
     * @param description
     */
    public StackedException(
        Throwable throwable,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description) {
        super(
            throwable,
            exceptionDomain,
            exceptionCode,
            description,
            parameters
        );
    }

    /**
     * @param className
     * @param methodName
     * @param lineNr
     * @param exceptionDomain
     * @param exceptionCode
     * @param parameters
     * @param description
     * @param callStack
     * @param timestamp
     */
    public StackedException(
        String className,
        String methodName,
        int lineNr,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        String[] callStack,
        Date timestamp) {
        super(
            className,
            methodName,
            lineNr,
            exceptionDomain,
            exceptionCode,
            parameters,
            description,
            callStack,
            timestamp
        );
    }

    /**
     * @param throwable
     * @param className
     * @param methodName
     * @param lineNr
     * @param exceptionDomain
     * @param exceptionCode
     * @param parameters
     * @param description
     * @param callStack
     * @param timestamp
     */
    public StackedException(
        Throwable throwable,
        String className,
        String methodName,
        int lineNr,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        String[] callStack,
        Date timestamp) {
        super(
            throwable,
            className,
            methodName,
            lineNr,
            exceptionDomain,
            exceptionCode,
            parameters,
            description,
            callStack,
            timestamp
        );
    }

    /**
     * @param throwable
     * @param exceptionDomain
     * @param exceptionCode
     * @param parameters
     * @param description
     * @param that
     */
    public StackedException(
        Throwable throwable,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        Throwable that) {
        super(
            throwable,
            exceptionDomain,
            exceptionCode,
            parameters,
            description,
            that
        );
    }

}

