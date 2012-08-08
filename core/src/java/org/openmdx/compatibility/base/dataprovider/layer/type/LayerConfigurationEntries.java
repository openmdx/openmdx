/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LayerConfigurationEntries.java,v 1.5 2007/10/10 16:06:00 hburger Exp $
 * Description: Generated constants for LayerConfigurationEntries
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:00 $
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
package org.openmdx.compatibility.base.dataprovider.layer.type;


/**
 * The <code>TypeLayerConfigurationEntries</code> class contains
 * constants identifying the configuration entries of the
 * dataprovider's type layer.
 */




public class LayerConfigurationEntries extends org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries {

  
  protected LayerConfigurationEntries() {
   // Avoid instantiation
  }


  /**
   * GENERIC_TYPE_PATH allows to specify a list of paths. Objects at one
   * of these paths are generic. This means that no type checking occurs. 
   * The objects may be of any type and may have any attributes. The 
   * Application Layer is responsible for handling these objects. 
   */
  static public final String GENERIC_TYPE_PATH = "genericTypePath";

  /**
   * Tells whether the replies should be verified.
   */
  static public final String VERIFY_REPLY = "verifyReply";

  /**
   * ALLOW_ENUMERATION_OF_CHILDREN allows to return the children's paths
   * as part of their parent object to inform the client about the complete 
   * set of children belonging to a given reference.
   */
  static public final String ALLOW_ENUMERATION_OF_CHILDREN = "allowEnumerationOfChildren";

  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
  return org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries.min();  // delegate
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
  return org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries.max();  // delegate
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   */
  static public String toString(int code)
  {
      return org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries.toString(code);  // delegate
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

    return org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries.fromString(code);  // delegate
  }



}

// end-of-file
