/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CommonExceptions.java,v 1.5 2007/10/10 16:06:02 hburger Exp $
 * Description: Generated constants for CommonExceptions
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


/**
 * Alias can't refer to superclass!
 * @deprecated org.openmdx.base.exception.BasicException.Code
 */
public class CommonExceptions extends org.openmdx.compatibility.base.exception.CommonExceptionCodes {

  
  protected CommonExceptions() {
   // Avoid instantiation
  }


  /**
   * Common exception codes
   * @deprecated org.openmdx.base.exception.BasicException.Code#DEFAULT_DOMAIN
   */
  static public final String DOMAIN = "ch:omex:spice:common";



  /**
   * @see CommonExceptionCodes#PROCESSING_FAILURE
   * @deprecated org.openmdx.base.exception.BasicException.Code#PROCESSING_FAILURE
   */
  static public final int EXECUTION_FAILURE = -19;



  /**
   * @see CommonExceptionCodes#ASSERTION_FAILURE
   * @deprecated org.openmdx.base.exception.BasicException.Code#ASSERTION_FAILURE
   */
  static public final int ASSERTION = -2;



  /**
   * @see CommonExceptionCodes#INVALID_CONFIGURATION
   * @deprecated org.openmdx.base.exception.BasicException.Code#INVALID_CONFIGURATION
   */
  static public final int CONFIGURATION = -32;



  /**
   * @see CommonExceptionCodes#AUTHORIZATION_FAILURE
   * @deprecated org.openmdx.base.exception.BasicException.Code#AUTHORIZATION_FAILURE
   */
  static public final int ACCESS_DENIED = -9;



  /**
   * @see CommonExceptionCodes#MEDIA_ACCESS_FAILURE
   * @deprecated org.openmdx.base.exception.BasicException.Code#MEDIA_ACCESS_FAILURE
   */
  static public final int MEDIA_ACCESS = -13;



  /**
   * @see CommonExceptionCodes#QUOTA_EXCEEDED
   * @deprecated org.openmdx.base.exception.BasicException.Code#QUOTA_EXCEEDED
   */
  static public final int QUOTA = -15;



  /**
   * @see CommonExceptionCodes#BAD_QUERY_CRITERIA
   * @deprecated org.openmdx.base.exception.BasicException.Code#BAD_QUERY_CRITERIA
   */
  static public final int BAD_SELECTION_CRITERIA = -8;



  /**
   * @see CommonExceptionCodes#CONCURRENT_ACCESS_FAILURE
   * @deprecated org.openmdx.base.exception.BasicException.Code#CONCURRENT_ACCESS_FAILURE
   */
  static public final int OPTIMISTIC_LOCKING = -20;



  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   * @deprecated 
   */
  static public int min()
  {
    int min = org.openmdx.compatibility.base.exception.CommonExceptionCodes.min();
    return CONFIGURATION <= min ? CONFIGURATION : min;  // delegate
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
    int max = org.openmdx.compatibility.base.exception.CommonExceptionCodes.max();
    return ASSERTION >= max ? ASSERTION : max;  // delegate
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   * @deprecated org.openmdx.base.exception.BasicException.Code#toString(int)
   */
  static public String toString(int code)
  {
    switch(code) {
      case EXECUTION_FAILURE: return "EXECUTION_FAILURE";
      case ASSERTION: return "ASSERTION";
      case CONFIGURATION: return "CONFIGURATION";
      case ACCESS_DENIED: return "ACCESS_DENIED";
      case MEDIA_ACCESS: return "MEDIA_ACCESS";
      case QUOTA: return "QUOTA";
      case BAD_SELECTION_CRITERIA: return "BAD_SELECTION_CRITERIA";
      case OPTIMISTIC_LOCKING: return "OPTIMISTIC_LOCKING";
      default: return org.openmdx.compatibility.base.exception.CommonExceptionCodes.toString(code);  // delegate
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
   * 
   * @deprecated org.openmdx.base.exception.BasicException.Code#toValue(String)
   */
  static public int fromString(String code)
  {  
    if (code.equalsIgnoreCase("EXECUTION_FAILURE")) return EXECUTION_FAILURE;
    if (code.equalsIgnoreCase("ASSERTION")) return ASSERTION;
    if (code.equalsIgnoreCase("CONFIGURATION")) return CONFIGURATION;
    if (code.equalsIgnoreCase("ACCESS_DENIED")) return ACCESS_DENIED;
    if (code.equalsIgnoreCase("MEDIA_ACCESS")) return MEDIA_ACCESS;
    if (code.equalsIgnoreCase("QUOTA")) return QUOTA;
    if (code.equalsIgnoreCase("BAD_SELECTION_CRITERIA")) return BAD_SELECTION_CRITERIA;
    if (code.equalsIgnoreCase("OPTIMISTIC_LOCKING")) return OPTIMISTIC_LOCKING;

    return org.openmdx.compatibility.base.exception.CommonExceptionCodes.fromString(code);  // delegate
  }



}

// end-of-file
