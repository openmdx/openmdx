/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: State_1_Attributes.java,v 1.10 2007/10/19 18:37:48 hburger Exp $
 * Description: Generated constants for State_1_Attributes
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/19 18:37:48 $
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
package org.openmdx.compatibility.base.dataprovider.layer.model;

import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;


/**
 * Attributes used by State_1.
 */




public class State_1_Attributes {

  
  protected State_1_Attributes() {
   // Avoid instantiation
  }


  /**
   * Attribute name.
   */
  static public final String VALID_FROM = "object_validFrom";



  /**
   * Attribute name.
   */
  static public final String VALID_TO = "object_validTo";



  /**
   * Attribute name.
   */
  static public final String STATE_VALID_FROM = "stateValidFrom";



  /**
   * Attribute name.
   */
  static public final String STATE_VALID_TO = "stateValidTo";



  /**
   * Attribute name.
   */
  static public final String STATED_OBJECT = "statedObject";



  /**
   * Attribute name.
   */
  static public final String UNDERLYING_STATE = "underlyingState";



  /**
   * Attribute name.
   */
  static public final String INVALIDATED_AT = "object_invalidatedAt";


  
  /**
   * Attribute name.
   */
  static public final String KEEPING_INVALIDATED_STATES = "keepingInvalidatedStates";

  
  
  /**
   * Reference name.
   */
  static public final String REF_HISTORY = "historyState";



  /**
   * Reference name.
   */
  static public final String REF_VALID = "validState";



  /**
   * Reference name.
   */
  static public final String REF_STATE = "state";



  /**
   * Reference name.
   */
  static public final String STATE_CONTEXT = "State";

  
  
  /**
   * Operation parameter name
   */
  static public final String OP_STATE = "state";



  /**
   * Operation parameter name
   */
  static public final String OP_VALID_FROM = "validFrom";



  /**
   * Operation parameter name
   */
  static public final String OP_VALID_TO = "validTo";



  /**
   * Operation parameter name
   */
  static public final String OP_SKIP_MISSING_STATES = "skipMissingStates";



  /**
   * Operation parameter value
   */
  static public final String OP_VAL_EVER = "ever";



  /**
   * Operation parameter value
   */
  static public final String OP_VAL_FIRST = "first";



  /**
   * Operation parameter value
   */
  static public final String OP_VAL_LAST = "last";



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
          "The code '" + code + "' is unkown to the class State_1_Attributes");
  }


  /**
   * Search for stated object filter property
   * 
   * @param attributeFilter the attribute filter to be searched for
   * 
   * @return the index of the stated object filter property, or <code>-1</code>
   * if it is not found
   */
  public static int indexOfStatedObject(
      FilterProperty[] attributeFilter
  ){
      for(
          int i = 0;
          i < attributeFilter.length;
          i++
      ){
          FilterProperty filter = attributeFilter[i];
          if (
              Quantors.THERE_EXISTS == filter.quantor() &&
              State_1_Attributes.STATED_OBJECT.equals(filter.name()) &&
              filter.operator() == (
                  filter.values().isEmpty() ? FilterOperators.IS_NOT_IN : FilterOperators.IS_IN  
              )
          ) {
              return i;
          }                
      }
      return -1;
  }

}

// end-of-file
