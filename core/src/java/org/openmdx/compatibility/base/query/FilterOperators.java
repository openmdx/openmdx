/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: FilterOperators.java,v 1.7 2007/10/10 16:06:03 hburger Exp $
 * Description: Generated constants for FilterOperators
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:03 $
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
package org.openmdx.compatibility.base.query;


/**
 * The <code>FilterOperations</code> class contains filter operation codes.
 */




public class FilterOperators {

  
  protected FilterOperators() {
      // // Avoid instantiation
  }


  /**
   * The operator SOUNDS_UNLIKE expects one or more values as &laquo;right&raquo; operand.
   */
  static public final short SOUNDS_UNLIKE = -6;



  /**
   * The operator IS_UNLIKE expects one or more values with the wildcard
   * characters '%' replacing any number of characters and '_' replacing a
   * single character as &laquo;right&raquo; operands. The escape character is '\'.
   */
  static public final short IS_UNLIKE = -5;



  /**
   * The operator IS_OUTSIDE expects a range specified by two values as 
   * &laquo;right&raquo; operand.
   */
  static public final short IS_OUTSIDE = -4;



  /**
   * The operator IS_LESS_OR_EQUAL expects one value as &laquo;right&raquo; operand.
   */
  static public final short IS_LESS_OR_EQUAL = -3;



  /**
   * The operator IS_LESS expects one value as &laquo;right&raquo; operand.
   */
  static public final short IS_LESS = -2;



  /**
   * The operator IS_NOT_IN expects a set of zero, one or more values as 
   * &laquo;right&raquo; operand.
   * <p>
   * An IS_NOT_IN expression with zero values always evaluates to true.
   */
  static public final short IS_NOT_IN = -1;


  /**
   * Treat filter values as value list for context object
   */
  static public final short PIGGY_BACK = 0;


  /**
   * The operator IS_IN expects a set of zero, one or more values as &laquo;right&raquo; 
   * operand.
   * <p>
   * An IS_IN expression with zero values always evaluates to false.
   */
  static public final short IS_IN = 1;



  /**
   * The operator IS_GREATER_OR_EQUAL expects one value as &laquo;right&raquo;
   * operand.
   */
  static public final short IS_GREATER_OR_EQUAL = 2;



  /**
   * The operator IS_GREATER expects one value as &laquo;right&raquo; operand.
   */
  static public final short IS_GREATER = 3;



  /**
   * The operator IS_BETWEEN expects a range specified by two values as 
   * &laquo;right&raquo; operand.
   */
  static public final short IS_BETWEEN = 4;



  /**
   * The operator IS_LIKE expects one or more values with the wildcard
   * characters '%' replacing any number of characters and '_' replacing a
   * single character as &laquo;right&raquo; operands. The escape character is '\'.
   */
  static public final short IS_LIKE = 5;



  /**
   * The operator SOUNDS_LIKE expects one or more values as &laquo;right&raquo; operand.
   */
  static public final short SOUNDS_LIKE = 6;


  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
    return SOUNDS_UNLIKE;
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
    return SOUNDS_LIKE;
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
      case SOUNDS_UNLIKE: return "SOUNDS_UNLIKE";
      case IS_UNLIKE: return "IS_UNLIKE";
      case IS_OUTSIDE: return "IS_OUTSIDE";
      case IS_LESS_OR_EQUAL: return "IS_LESS_OR_EQUAL";
      case IS_LESS: return "IS_LESS";
      case IS_NOT_IN: return "IS_NOT_IN";
      case IS_IN: return "IS_IN";
      case IS_GREATER_OR_EQUAL: return "IS_GREATER_OR_EQUAL";
      case IS_GREATER: return "IS_GREATER";
      case IS_BETWEEN: return "IS_BETWEEN";
      case IS_LIKE: return "IS_LIKE";
      case SOUNDS_LIKE: return "SOUNDS_LIKE";
      case PIGGY_BACK : return "PIGGY_BACK";
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
   */
  static public int fromString(String code)
  {  
    if (code.equalsIgnoreCase("SOUNDS_UNLIKE")) return SOUNDS_UNLIKE;
    if (code.equalsIgnoreCase("IS_UNLIKE")) return IS_UNLIKE;
    if (code.equalsIgnoreCase("IS_OUTSIDE")) return IS_OUTSIDE;
    if (code.equalsIgnoreCase("IS_LESS_OR_EQUAL")) return IS_LESS_OR_EQUAL;
    if (code.equalsIgnoreCase("IS_LESS")) return IS_LESS;
    if (code.equalsIgnoreCase("IS_NOT_IN")) return IS_NOT_IN;
    if (code.equalsIgnoreCase("IS_IN")) return IS_IN;
    if (code.equalsIgnoreCase("IS_GREATER_OR_EQUAL")) return IS_GREATER_OR_EQUAL;
    if (code.equalsIgnoreCase("IS_GREATER")) return IS_GREATER;
    if (code.equalsIgnoreCase("IS_BETWEEN")) return IS_BETWEEN;
    if (code.equalsIgnoreCase("IS_LIKE")) return IS_LIKE;
    if (code.equalsIgnoreCase("SOUNDS_LIKE")) return SOUNDS_LIKE;
    if (code.equalsIgnoreCase("PIGGY_BACK")) return PIGGY_BACK;

    // Not found
    throw new IllegalArgumentException(
          "The code '" + code + "' is unkown to the class FilterOperators");
  }

  
}

// end-of-file
