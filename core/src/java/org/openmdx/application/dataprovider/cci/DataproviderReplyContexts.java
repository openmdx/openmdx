/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderReplyContexts.java,v 1.1 2009/01/05 13:44:50 wfro Exp $
 * Description: Generated constants for DataproviderReplyContexts
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:50 $
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
package org.openmdx.application.dataprovider.cci;


/**
 * The <code>DataproviderRequestContexts</code> class contains constants 
 * identifying the dataprovider request contexts.
 */




public class DataproviderReplyContexts {

  
  protected DataproviderReplyContexts() {
   // Avoid instantiation
  }


  /**
   * A non-negative Integer value defining the total number of selected
   * elements.
   */
  static public final String TOTAL = "total";



  /**
   * A Boolean value specifying whether there are more elements to be
   * returned
   */
  static public final String HAS_MORE = "hasMore";



  /**
   * An iterator is returned from find requests and passed to continue find
   * requests.
   */
  static public final String ITERATOR = "iterator";



  /**
   * The reference filter is returned from find requests and passed to
   * continue find requests.
   */
  static public final String REFERENCE_FILTER = "referenceFilter";



  /**
   * The attribute selector is returned from find requests and passed to
   * continue find requests.
   */
  static public final String ATTRIBUTE_SELECTOR = "attributeSelector";



  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
  return Integer.MAX_VALUE; // no constants defined
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
  return Integer.MIN_VALUE; // no constants defined
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   */
  static public String toString(int code)
  {
      // no integer constants defined
      return String.valueOf(code);
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

    // Not found
    throw new IllegalArgumentException(
          "The code '" + code + "' is unkown to the class DataproviderReplyContexts");
  }



}

// end-of-file
