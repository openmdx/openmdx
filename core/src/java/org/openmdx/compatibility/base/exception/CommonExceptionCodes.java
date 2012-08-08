/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CommonExceptionCodes.java,v 1.5 2007/10/10 16:06:02 hburger Exp $
 * Description: Generated constants for CommonExceptionCodes
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:02 $
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


public class CommonExceptionCodes {

  
  protected CommonExceptionCodes() {
   // Avoid instantiation
  }

  /**
   * Common exception codes
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#DEFAULT_DOMAIN
   */
  static public final String DOMAIN = "DefaultDomain";


  /**
   * An error code that signals a successful operation  
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#NONE
   */
  static public final int NONE = 0;



  /**
   * Technical internal error conditions such as NullPointerExceptions,
   * ClassNotFoundExceptions, etc.
   * <p>
   * This exception must never be a top level Exception in the stack of
   * exceptions, so immediately remap this exception to another application
   * oriented exception.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#GENERIC
   */
  static public final int INTERNAL = -23;



  /**
   * Activation failure.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#ACTIVATION_FAILURE
   */
  static public final int ACTIVATION_FAILURE = -10;



  /**
   * Failure during a components main activity, excluding the
   * activation and deactivation phase. 
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#PROCESSING_FAILURE
   */
  static public final int PROCESSING_FAILURE = -19;



  /**
   * Deactivation failure
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#DEACTIVATION_FAILURE
   */
  static public final int DEACTIVATION_FAILURE = -33;



  /**
   * Assertion error.
   * <p>
   * Assertion errors signal "unexpected" exceptions such as programming
   * errors.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#ASSERTION_FAILURE
   */
  static public final int ASSERTION_FAILURE = -2;



  /**
   * Invalid configuration.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#INVALID_CONFIGURATION
   */
  static public final int INVALID_CONFIGURATION = -32;



  /**
   * Unsupported operation or action.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#NOT_SUPPORTED
   */
  static public final int NOT_SUPPORTED = -36;



  /**
   * Access denied.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#AUTHORIZATION_FAILURE
   */
  static public final int AUTHORIZATION_FAILURE = -9;



  /**
   * Lookup failed.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#NOT_FOUND
   */
  static public final int NOT_FOUND = -34;



  /**
   * Information is not available.
   * <p>
   * This exception code means that the request itself is valid but
   * the requested data is not available at the moment.
   * (A specific stock quote for example might be unavailable due to the
   * fact that corresponding market is not opened yet.)
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#NOT_AVAILABLE
   */
  static public final int NOT_AVAILABLE = -22;



  /**
   * Persistent media access error.
   * Files, databases, or any other external nresource cannot be accessed
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#MEDIA_ACCESS_FAILURE
   */
  static public final int MEDIA_ACCESS_FAILURE = -13;



  /**
   * The resource usage exceeded the allowed range
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#QUOTA_EXCEEDED
   */
  static public final int QUOTA_EXCEEDED = -15;



  /**
   * The result of a query is too large to be handled.
   * <p>
   * This exception is thrown when a method produces a result that
   * exceeds a size-related limit. This can happen, for example, when
   * the size of the result exceeds some implementation-specific limit.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#TOO_LARGE_RESULT_SET
   */
  static public final int TOO_LARGE_RESULT_SET = -21;



  /**
   * An error code that signals bad/inconsistent parameters.  
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#BAD_PARAMETER
   */
  static public final int BAD_PARAMETER = -30;



  /**
   * Invalid/Unexpected length (nr of elements) of a CORBA sequence.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#BAD_SEQUENCE_LENGTH
   */
  static public final int BAD_SEQUENCE_LENGTH = -11;



  /**
   * Bad format or composition of selection criteria.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#BAD_QUERY_CRITERIA
   */
  static public final int BAD_QUERY_CRITERIA = -8;



  /**
   * Data conversion error.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#DATA_CONVERSION
   */
  static public final int DATA_CONVERSION = -5;



  /**
   * Duplicate element
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#DUPLICATE
   */
  static public final int DUPLICATE = -26;



  /**
   * A concurrent access error condition.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#CONCURRENT_ACCESS_FAILURE
   */
  static public final int CONCURRENT_ACCESS_FAILURE = -20;



  /**
   * An optimistic locking error condition.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#TIMEOUT
   */
  static public final int TIMEOUT = -16;



  /**
   * Signals that a method has been invoked at an illegal or
   * inappropriate time. In other words, the environment or application
   * is not in an appropriate state for the requested operation. 
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#ILLEGAL_STATE
   */
  static public final int ILLEGAL_STATE = -6;



  /**
   * The TOO_MANY_EVENT_LISTENERS exception is used as part of the 
   * SPICE event model to annotate and implement a unicast special case of a
   * multicast Event Source. 
   * <p>
   * The presence of a "throws ServiceException TOO_MANY_EVENT_LISTENERS"
   * clause on any given concrete implementation of the normally multicast
   * "void addEventListener" event listener registration pattern is used
   * to annotate that interface as implementing a unicast Listener special
   * case, that is, that one and only one Listener may be registered on the
   * particular event listener source concurrently.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#TOO_MANY_EVENT_LISTENERS
   */
  static public final int TOO_MANY_EVENT_LISTENERS = -40;



  /**
   * Bad member name specified, e.g. non-existing attribute or property.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#BAD_MEMBER_NAME
   */
  static public final int BAD_MEMBER_NAME = -41;



  /**
   * A ROLLBACK exception is thrown when the transaction has been marked for
   * rollback only or the transaction has been rolled back instead of
   * committed. 
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#ROLLBACK
   */
  static public final int ROLLBACK = -42;



  /**
   * A HEURISTIC exception is thrown by the commit operation to indicate
   * that a heuristic decision was made and that some relevant updates have
   * been committed and others have been rolled back. 
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#HEURISTIC
   */
  static public final int HEURISTIC = -43;



  /**
   * An ABORT exception is thrown to report that a non-transactional unit of
   * work has been aborted.
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#ABORT
   */
  static public final int ABORT = -44;



  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   * @deprecated
   */
  static public int min()
  {
    return ABORT;
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   * @deprecated
   */
  static public int max()
  {
    return NONE;
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#toString(int)
   */
  static public String toString(int code)
  {
    switch(code) {
      case NONE: return "NONE";
      case INTERNAL: return "INTERNAL";
      case ACTIVATION_FAILURE: return "ACTIVATION_FAILURE";
      case PROCESSING_FAILURE: return "PROCESSING_FAILURE";
      case DEACTIVATION_FAILURE: return "DEACTIVATION_FAILURE";
      case ASSERTION_FAILURE: return "ASSERTION_FAILURE";
      case INVALID_CONFIGURATION: return "INVALID_CONFIGURATION";
      case NOT_SUPPORTED: return "NOT_SUPPORTED";
      case AUTHORIZATION_FAILURE: return "AUTHORIZATION_FAILURE";
      case NOT_FOUND: return "NOT_FOUND";
      case NOT_AVAILABLE: return "NOT_AVAILABLE";
      case MEDIA_ACCESS_FAILURE: return "MEDIA_ACCESS_FAILURE";
      case QUOTA_EXCEEDED: return "QUOTA_EXCEEDED";
      case TOO_LARGE_RESULT_SET: return "TOO_LARGE_RESULT_SET";
      case BAD_PARAMETER: return "BAD_PARAMETER";
      case BAD_SEQUENCE_LENGTH: return "BAD_SEQUENCE_LENGTH";
      case BAD_QUERY_CRITERIA: return "BAD_QUERY_CRITERIA";
      case DATA_CONVERSION: return "DATA_CONVERSION";
      case DUPLICATE: return "DUPLICATE";
      case CONCURRENT_ACCESS_FAILURE: return "CONCURRENT_ACCESS_FAILURE";
      case TIMEOUT: return "TIMEOUT";
      case ILLEGAL_STATE: return "ILLEGAL_STATE";
      case TOO_MANY_EVENT_LISTENERS: return "TOO_MANY_EVENT_LISTENERS";
      case BAD_MEMBER_NAME: return "BAD_MEMBER_NAME";
      case ROLLBACK: return "ROLLBACK";
      case HEURISTIC: return "HEURISTIC";
      case ABORT: return "ABORT";
      default:
        return String.valueOf(code);
    }
  }



  /**
   * Returns the code of the passed code's string representation.
   * The string representation is case insensitive.
   *
   * @exception  throws an <code>IllegalArgumentException</code> 
   *             if the stringified code cannot be resolved
   * @param code a stringified code
   * @return a code
   * @deprecated org.openmdx.base.exception.BaseExceptionCode#toValue(String)
   */
  static public int fromString(String code)
  {  
    if (code.equalsIgnoreCase("NONE")) return NONE;
    if (code.equalsIgnoreCase("INTERNAL")) return INTERNAL;
    if (code.equalsIgnoreCase("ACTIVATION_FAILURE")) return ACTIVATION_FAILURE;
    if (code.equalsIgnoreCase("PROCESSING_FAILURE")) return PROCESSING_FAILURE;
    if (code.equalsIgnoreCase("DEACTIVATION_FAILURE")) return DEACTIVATION_FAILURE;
    if (code.equalsIgnoreCase("ASSERTION_FAILURE")) return ASSERTION_FAILURE;
    if (code.equalsIgnoreCase("INVALID_CONFIGURATION")) return INVALID_CONFIGURATION;
    if (code.equalsIgnoreCase("NOT_SUPPORTED")) return NOT_SUPPORTED;
    if (code.equalsIgnoreCase("AUTHORIZATION_FAILURE")) return AUTHORIZATION_FAILURE;
    if (code.equalsIgnoreCase("NOT_FOUND")) return NOT_FOUND;
    if (code.equalsIgnoreCase("NOT_AVAILABLE")) return NOT_AVAILABLE;
    if (code.equalsIgnoreCase("MEDIA_ACCESS_FAILURE")) return MEDIA_ACCESS_FAILURE;
    if (code.equalsIgnoreCase("QUOTA_EXCEEDED")) return QUOTA_EXCEEDED;
    if (code.equalsIgnoreCase("TOO_LARGE_RESULT_SET")) return TOO_LARGE_RESULT_SET;
    if (code.equalsIgnoreCase("BAD_PARAMETER")) return BAD_PARAMETER;
    if (code.equalsIgnoreCase("BAD_SEQUENCE_LENGTH")) return BAD_SEQUENCE_LENGTH;
    if (code.equalsIgnoreCase("BAD_QUERY_CRITERIA")) return BAD_QUERY_CRITERIA;
    if (code.equalsIgnoreCase("DATA_CONVERSION")) return DATA_CONVERSION;
    if (code.equalsIgnoreCase("DUPLICATE")) return DUPLICATE;
    if (code.equalsIgnoreCase("CONCURRENT_ACCESS_FAILURE")) return CONCURRENT_ACCESS_FAILURE;
    if (code.equalsIgnoreCase("TIMEOUT")) return TIMEOUT;
    if (code.equalsIgnoreCase("ILLEGAL_STATE")) return ILLEGAL_STATE;
    if (code.equalsIgnoreCase("TOO_MANY_EVENT_LISTENERS")) return TOO_MANY_EVENT_LISTENERS;
    if (code.equalsIgnoreCase("BAD_MEMBER_NAME")) return BAD_MEMBER_NAME;
    if (code.equalsIgnoreCase("ROLLBACK")) return ROLLBACK;
    if (code.equalsIgnoreCase("HEURISTIC")) return HEURISTIC;
    if (code.equalsIgnoreCase("ABORT")) return ABORT;

    // Not found
    throw new IllegalArgumentException(
          "The code '" + code + "' is unkown to the class CommonExceptionCodes");
  }



}

// end-of-file
