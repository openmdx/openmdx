/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TransactionPolicies.java,v 1.1 2009/01/05 13:44:50 wfro Exp $
 * Description: Generated constants for TransactionPolicies
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
 * The <code>TransactionPolicies</code> class contains constants 
 * identifying the set of transaction policies.
 */




public class TransactionPolicies {

  
  protected TransactionPolicies() {
   // Avoid instantiation
  }


  /**
   * This transaction policy is the sames as MANAGING_TRANSACTION for 
   * transactional dataproviders respectively IGNORING_TRANSACTION for
   * non-transactional dataproviders.
   * @deprecated    use "NoOrNew"
   */
  static public final String NO_OR_NEW_TRANSACTION = "NoOrNewTransaction";



  /**
   * A provider with the transaction policy "NoOrNew" suspends the
   * current transaction if any and delegates eihter to a provider
   * with the transaction policy NEVER or SUPPORTS in case of a 
   * non-transactional unit of work or starts a new transaction and 
   * delegates to a provider with the transaction policy MANDATORY or 
   * SUPPORTS in case of a transactional unit of work.
   */
  static public final String NO_OR_NEW = "NoOrNew";



  /**
   * A provider with the transaction policy NEVER is allowed to delegate
   * requests belonging to the same unit of work to providers with one
   * of the transaction policies NEVER and SUPPORTS.
   */
  static public final String NEVER = "Never";



  /**
   * Ignoring transactions is offered by non-transactional dataproviders.
   * @deprecated use "Never"
   */
  static public final String IGNORING_TRANSACTION = "IgnoringTransaction";



  /**
   * @deprecated use "Never"
   */
  static public final String IGNORING = "ignoring";



  /**
   * Managing transactions is offered by transactional dataproviders
   * supporting local transactions.
   * @deprecated use "NoOrNew"
   */
  static public final String MANAGING_TRANSACTION = "ManagingTransaction";



  /**
   * Joining transactions is offered by transactional dataproviders able
   * to join transactions.
   * @deprecated use "Mandatory"
   */
  static public final String JOINING_TRANSACTION = "JoiningTransaction";



  /**
   * @deprecated use "Mandatory"
   */
  static public final String JOINING = "joining";



  /**
   * A provider with the transaction policy MANDATORY is allowed to 
   * delegate requests belonging to the same unit of work to providers
   * with one of the transaction policies NEVER and MANDATORY.
   */
  static public final String MANDATORY = "Mandatory";



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
          "The code '" + code + "' is unkown to the class TransactionPolicies");
  }



}

// end-of-file
