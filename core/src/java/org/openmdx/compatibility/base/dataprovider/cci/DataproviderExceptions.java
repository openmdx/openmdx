/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderExceptions.java,v 1.6 2007/10/10 16:05:58 hburger Exp $
 * Description: Generated constants for DataproviderExceptions
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:58 $
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
package org.openmdx.compatibility.base.dataprovider.cci;


/**
 * The <code>CommonException</code> class contains exceptions codes used in
 * CORBA and non-CORBA environments.
 * @deprecated org.openmdx.base.exception.BasicException.Code fields
 */




public class DataproviderExceptions extends org.openmdx.compatibility.base.exception.CommonExceptions {

  
  protected DataproviderExceptions() {
   // Avoid instantiation
  }


  /**
   * Dataprovider exception codes
   * @deprecated org.openmdx.base.exception.BasicException.Code#DEFAULT_DOMAIN
   */
  static public final String DOMAIN = "org:openmdx:compatibility:base::dataprovider";



  /**
   * Transient exception.
   * <p>
   * This exception code is deprecated without any replacement!
   * @deprecated without replacement
   */
  static public final int TRANSIENT = -1001;



  /**
   * @deprecated org.openmdx.base.exception.BasicException.Code#BAD_MEMBER_NAME
   */
  static public final int INVALID_ATTRIBUTE = -41;



  /**
   * @deprecated without replacement
   */
  static public final int NOT_A_REFERENCE = -1003;



  /**
   * @deprecated without replacement
   */
  static public final int NOT_AN_OBJECT = -1004;



  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
    int min = org.openmdx.compatibility.base.exception.CommonExceptions.min();
    return NOT_AN_OBJECT <= min ? NOT_AN_OBJECT : min;  // delegate
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
    int max = org.openmdx.compatibility.base.exception.CommonExceptions.max();
    return INVALID_ATTRIBUTE >= max ? INVALID_ATTRIBUTE : max;  // delegate
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   */
  static public String toString(int code)
  {
    switch(code) {
      case TRANSIENT: return "TRANSIENT";
      case INVALID_ATTRIBUTE: return "INVALID_ATTRIBUTE";
      case NOT_A_REFERENCE: return "NOT_A_REFERENCE";
      case NOT_AN_OBJECT: return "NOT_AN_OBJECT";
      default: return org.openmdx.compatibility.base.exception.CommonExceptions.toString(code);  // delegate
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
   */
  static public int fromString(String code)
  {  
    if (code.equalsIgnoreCase("TRANSIENT")) return TRANSIENT;
    if (code.equalsIgnoreCase("INVALID_ATTRIBUTE")) return INVALID_ATTRIBUTE;
    if (code.equalsIgnoreCase("NOT_A_REFERENCE")) return NOT_A_REFERENCE;
    if (code.equalsIgnoreCase("NOT_AN_OBJECT")) return NOT_AN_OBJECT;

    return org.openmdx.compatibility.base.exception.CommonExceptions.fromString(code);  // delegate
  }



}

// end-of-file
