/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Stereotypes.java,v 1.4 2007/10/10 16:06:09 hburger Exp $
 * Description: Constants for Stereotypes
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:09 $
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
package org.openmdx.model1.code;


/**
 * The <code>Stereotypes</code> class contains the
 * known stereotypes.
 */
public class Stereotypes {

  protected Stereotypes() {
      // Avoid instantiation
  }


  /**
   * Stereotype used for definining primitive types. Primitive types
   * can be used as attribute types.
   */
  static public final String PRIMITIVE = "primitive";



  /**
   * Stereotype used for definining alias types. 
   */
  static public final String ALIAS = "alias";



  /**
   * Allows to define a root class. A root class is not required to
   * be member of a container (referenced by a "composite" reference).
   */
  static public final String ROOT = "root";



  /**
   * Stereotype allowing to define StructureTypes. StructureTypes must
   * not have associations attached.
   */
  static public final String STRUCT = "struct";



  /**
   * Stereotype allowing to define roles.
   */
  static public final String ROLE = "role";



  /**
   * Stereotype to mark operations as exceptions. MOF defines exceptions
   * as behavioural features. Therefore exceptions are modeled as operations
   * with stereotype <<exception>>. The exception can be used as exceptions
   * in user-defined operations.
   */
  static public final String EXCEPTION = "exception";

}
