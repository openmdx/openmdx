/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderOperations.java,v 1.4 2007/10/10 16:05:58 hburger Exp $
 * Description: Generated constants for DataproviderOperations
 * Revision:    $Revision: 1.4 $
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
 * The <code>DataproviderOperations</code> class contains the constants
 * identifying the dataprovider operations.
 */




public class DataproviderOperations {

  
  protected DataproviderOperations() {
   // Avoid instantiation
  }


  /**
   * Invoke named operation.
   */
  static public final short OBJECT_OPERATION = 0;



  /**
   * Create a complete object.
   */
  static public final short OBJECT_CREATION = 1;



  /**
   * Set or replace some of the object's attributes' values.
   */
  static public final short OBJECT_MODIFICATION = 2;



  /**
   * Set or replace some of the object's attributes.
   */
  static public final short OBJECT_REPLACEMENT = 3;



  /**
   * Object removal.
   * <p>
   * Removes an objects including its contained subtree.
   */
  static public final short OBJECT_REMOVAL = 4;



  /**
   * Object set request.
   * <p>
   * Replacement of an existsing object ignoring its digest respectively
   * creation of a new one.
   */
  static public final short OBJECT_SETTING = 5;



  /**
   * Iterates over the paths of a selection of objects.
   * <p>
   * Selection criteria are:
   * <ul>
   * <li> A set of references
   * <ul>
   * <li>	The selection is restricted to the objects contained
   * in the set of references.
   * <li>	The objects contained by the set of references must
   * be of the same type, i.e. they have to share a
   * common superclass.
   * </ul>
   * <li> An optional query
   * <ul>
   * <li>	A query is a string, whose syntax is based on a subset
   * of the SQL92 conditional expression syntax with the
   * extension to be able to handle multivalued attributes.
   * <li>	If the attribute selector is an empty string then all
   * paths matching the path selector are included in the
   * result set.
   * </ul>
   * </ul>
   * <p>
   * A query can contain:
   * <ul>
   * <li> Literals:
   * <ul>
   * <li>	A string literal is enclosed in single quotes with
   * single quote represented by doubled single quote such
   * as <code>'literal'</code> and <code>'literal''s'<
   * 	/code>; they use <a href="common.types.html#String_T"
   * >String_T</a>'s character encoding
   * <li>	A numeric literal is either a numeric value with or
   * without decimal point such as <code>57</code>, <code
   * >-957</code>, <code>+62</code>, <code>7.</code>, <
   * code>-95.7</code>, <code>+6.2</code> or a numeric
   * value in scientific notation such as <code>7E3</code>,
   * <code>-57.9E2</code>. 
   * </ul>
   * <li> Identifiers:
   * <ul>
   * <li>	An identifier is an unlimited length sequence of 
   * letters and digits, the first of which must be a
   * letter. 
   * <li>	Identifiers cannot be the names <code>NULL</code>,
   * <code>TRUE</code>, or <code>FALSE</code>.
   * <li>	Identifiers cannot be <code>NOT</code>, <code
   * >AND</code>, <code>LIKE</code>, <code>IN</code>, 
   * <code>IS</code>, <code>ANY</code> and <code>EACH</code>.
   * <li>	<code>object_id</code> refers to the last element of the 
   * object path, all other identifiers to object
   * attributes.
   * <li>	A dataprovider not supporting references to
   * specific attributes (e.g. a derived ones) is required
   * to throw an <a href=
   * "common.generic.html#EX_NOT_SUPPORTED"
   * >EX_NOT_SUPPORTED</a> exception if an identifier 
   * references such an attribute.
   * <li>	A dataprovider may treat references to undeclared
   * attributes either the same way as if the attribute
   * had no values or it may throw an 
   * <a href=
   * "dataprovider.generic.html#EX_INVALID_ATTRIBUTE"
   * >EX_INVALID_ATTRIBUTE</a> exception if an identifier 
   * does not matching any declared attribute.
   * <li>	Identifiers are case sensitive.
   * </ul>
   * <li> Whitespace:
   * <ul>
   * <li>	Whitspace is space, horizontal tab, form feed or
   * line terminator.
   * </ul>
   * <li> Expressions: 
   * <ul>
   * <li>	A selector is a conditional expression; a selector
   * that evaluates to true matches; a selector that
   * evaluates to false does not match.
   * <li>	<code>
   * conditional_expression := 
   * comparison_operation 
   * [ AND conditional_expression ]
   * </code>
   * </ul>
   * <li>Comparison operations:
   * <ul>
   * <li>	<code>ANY identifier < literal</code> evaluates to
   * true if any of the attribute's values is less than the 
   * literal; it evaluates to false in all other cases.
   * <li>	<code>EACH identifier < literal</code> evaluates to
   * true if all of the attribute's values are less than the 
   * literal; it evaluates to false in all other cases.
   * <li>	<code>ANY identifier <= literal</code>
   * <li>	<code>EACH identifier <= literal</code>
   * <li>	<code>ANY identifier = literal</code>
   * <li>	<code>EACH identifier = literal</code>
   * <li>	<code>ANY identifier >= literal</code>
   * <li>	<code>EACH identifier >= literal</code>
   * <li>	<code>ANY identifier > literal</code>
   * <li>	<code>EACH identifier > literal</code>
   * <li>	<code>ANY identifier <> literal</code> evaluates to
   * true  if any of the attribute's values is not equal to
   * the literal; it evaluates to false in all other cases.
   * <li>	<code>EACH identifier <> literal</code>
   * <li>	<code>ANY identifier IN literal1 .. literal2</code>
   * evaluates to true if any of the 
   * attribute's values is  greater than or equal to 
   * literal1 and less than or equal literal2; it evaluates
   * to false in all other cases.
   * <li>	<code>EACH identifier IN literal1 .. literal2</code> 
   * evaluates to true if none of the 
   * <li>	<code>ANY identifier NOT IN literal1 .. literal2</code>
   * evaluates to true if all of the
   * attribute's values are either less than literal1 or
   * greater than literal2; it evaluates to false in all
   * other cases.
   * <li>	<code>EACH identifier NOT IN literal1 .. literal2</code>
   * <li>  <code>ANY identifier IN (literal1, literal2, 
   * ...)</code> evaluates to true if any of the
   * attribute's values is equal to any of the literals;
   * it evaluates to false in all other cases.
   * <li>  <code>EACH identifier IN (literal1, literal2, 
   * ...)</code>
   * <li>  <code>ANY identifier NOT IN (literal1, literal2, 
   * ...)</code> evaluates to true if none of the
   * attribute's values is equal to any of the literals;
   * it evaluates to false in all other cases.
   * <li>  <code>EACH identifier NOT IN (literal1, literal2, 
   * ...)</code>
   * <li>  <code>identifier IS NULL</code> evaluates to true if
   * identifier references an attribute with no values;
   * it evaluates to false in all other cases.
   * <li>  <code>identifier IS NOT NULL</code> evaluates to true
   * if identifier references an attribute with at least
   * one value; it evaluates to false in all other cases.
   * <li>  <code>ANY identifier TRUE</code> evaluates to true
   * if any of the attribute's values is equal to the
   * boolean value <code>TRUE</code>; it evaluates to false
   * in all other cases.
   * <li>  <code>EACH identifier IS TRUE</code>
   * <li>  <code>ANY identifier IS FALSE</code>
   * <li>  <code>EACH identifier IS FALSE</code>
   * <li>	<code>ANY identifier IS LIKE pattern-value [ESCAPE
   * escape-character]</code> evaluates to true if any of
   * the attribute's values matches the pattern-value which
   * is a string literal where <code>_</code> stands for
   * any single character; <code>%</code> stands for any
   * sequence of characters (including the empty sequence);
   * and all other characters stand for themselves; it
   * evaluates to false in all other cases. The optional
   * escape-character is a single character string literal
   * whose character is used to escape the special meaning
   * of the <code>_</code> and <code>%</code> in
   * pattern-value.
   * <ul>
   * <li><code>phone LIKE '12%3'</code> for example matches
   * <code>123</code> and <code>12993</code> but not
   * <code>1234</code>.
   * <li><code>word LIKE 'l_se'</code> for example matches
   * <code>lose</code> but not <code>loose</code>.
   * <li><code>underscored LIKE '\_%' ESCAPE '\'</code> for
   * example matches <code>_foo</code> but not
   * <code>bar</code>
   * </ul>
   * <li>	<code>EACH identifier LIKE pattern-value [ESCAPE
   * escape-character]</code>
   * <li>	<code>ANY identifier NOT LIKE pattern-value [ESCAPE
   * escape-character]</code> is equivalent to <code>NOT (
   * identifier LIKE pattern-value [ESCAPE
   * escape-character])</code>, i.e. it evaluates to true
   * if none of the attribute's values matches the
   * pattern-value; it evaluates to false in all other
   * cases.
   * <li>	<code>EACH identifier NOT LIKE pattern-value [ESCAPE
   * escape-character]</code>
   * </ul>
   * </ul>
   */
  static public final short ITERATION_START = 6;



  /**
   */
  static public final short ITERATION_CONTINUATION = 7;



  /**
   * Object monitoring.
   * <p>
   * An event will be fired when a an update operation is performed on an
   * object or reference specified by the request.
   */
  static public final short OBJECT_MONITORING = 8;



  /**
   * Object retrieval.
   * <p>
   * Returns an object specified by its path.
   */
  static public final short OBJECT_RETRIEVAL = 9;



  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
    return OBJECT_OPERATION;
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
    return OBJECT_RETRIEVAL;
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
      case OBJECT_OPERATION: return "OBJECT_OPERATION";
      case OBJECT_CREATION: return "OBJECT_CREATION";
      case OBJECT_MODIFICATION: return "OBJECT_MODIFICATION";
      case OBJECT_REPLACEMENT: return "OBJECT_REPLACEMENT";
      case OBJECT_REMOVAL: return "OBJECT_REMOVAL";
      case OBJECT_SETTING: return "OBJECT_SETTING";
      case ITERATION_START: return "ITERATION_START";
      case ITERATION_CONTINUATION: return "ITERATION_CONTINUATION";
      case OBJECT_MONITORING: return "OBJECT_MONITORING";
      case OBJECT_RETRIEVAL: return "OBJECT_RETRIEVAL";
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
    if (code.equalsIgnoreCase("OBJECT_OPERATION")) return OBJECT_OPERATION;
    if (code.equalsIgnoreCase("OBJECT_CREATION")) return OBJECT_CREATION;
    if (code.equalsIgnoreCase("OBJECT_MODIFICATION")) return OBJECT_MODIFICATION;
    if (code.equalsIgnoreCase("OBJECT_REPLACEMENT")) return OBJECT_REPLACEMENT;
    if (code.equalsIgnoreCase("OBJECT_REMOVAL")) return OBJECT_REMOVAL;
    if (code.equalsIgnoreCase("OBJECT_SETTING")) return OBJECT_SETTING;
    if (code.equalsIgnoreCase("ITERATION_START")) return ITERATION_START;
    if (code.equalsIgnoreCase("ITERATION_CONTINUATION")) return ITERATION_CONTINUATION;
    if (code.equalsIgnoreCase("OBJECT_MONITORING")) return OBJECT_MONITORING;
    if (code.equalsIgnoreCase("OBJECT_RETRIEVAL")) return OBJECT_RETRIEVAL;

    // Not found
    throw new IllegalArgumentException(
          "The code '" + code + "' is unkown to the class DataproviderOperations");
  }



}

// end-of-file
