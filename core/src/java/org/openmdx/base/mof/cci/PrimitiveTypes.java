/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Constants for PrimitiveTypes
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.mof.cci;


/**
 * The <code>Stereotypes</code> class contains the
 * supported primitive types.
 */
public class PrimitiveTypes {

  protected PrimitiveTypes() {
      // Avoid instantiation
  }

  /**
   */
  static public final String BOOLEAN = "org:w3c:boolean";



  /**
   */
  static public final String SHORT = "org:w3c:short";



  /**
   */
  static public final String INTEGER = "org:w3c:integer";



  /**
   */
  static public final String LONG = "org:w3c:long";



  /**
   */
  static public final String DECIMAL = "org:w3c:decimal";



  /**
   */
  static public final String STRING = "org:w3c:string";



  /**
   */
  static public final String DATETIME = "org:w3c:dateTime";



  /**
   */
  static public final String DATE = "org:w3c:date";



  /**
   */
  static public final String ANYURI = "org:w3c:anyURI";



  /**
   */
  static public final String BINARY = "org:w3c:binary";



  /**
   */
  static public final String OBJECT_ID = "org:openmdx:base:ObjectId";


  /**
   */
  static public final String DURATION = "org:w3c:duration";

  /**
   */
  static public final String OID = "org:ietf:OID";

  /**
   */
  static public final String UUID = "org:ietf:UUID";

  /**
   * 
   */
  static public final String XRI = "org:oasis-open:XRI";

}
