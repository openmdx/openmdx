/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SharedConfigurationEntries.java,v 1.17 2008/07/01 00:26:18 hburger Exp $
 * Description: Generated constants for SharedConfigurationEntries
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/07/01 00:26:18 $
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
 * The <code>SharedConfigurationEntries</code> class contains
 * constants identifying the dataprovider configuration entries
 * shared by all layers.
 */
public class SharedConfigurationEntries {

  
  protected SharedConfigurationEntries() {
   // Avoid instantiation
  }

  /**
   * Sun Application Server 8.1 uses URLs as <code>Binding</code> names even 
   * if <code>isRelative()</code> evauluates to <code>true</code>.
   */
  static public boolean WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS = true;
  
  /**
   * The NAMESPACE_ID configuration entry
   */
  static public final String NAMESPACE_ID = "namespaceId";

  
  
  /**
   * The THIS configuration entry
   */
  static public final String THIS_DATAPROVIDER = "thisDataprovider";

  
  
  /**
   * The SERVER_ID configuration entry
   */
  static public final String SERVER_ID = "serverId";

  
  
  /**
   * The EXPOSED_PATH configuration entry
   */
  static public final String EXPOSED_PATH = "exposedPath";

  
  
  /**
   * The LAYER_STATISTICS configuration entry  
   */
  static public final String LAYER_STATISTICS = "statistics";



  /**
   * The DATABASE_CONNECTION_FACTORY configuration entry  
   */
  static public final String DATABASE_CONNECTION_FACTORY = "datasource";



  /**
   * The PERSISTENCE_MANAGER_BINDING configuration entry, defaults to "cci2".
   */
  static public final String PERSISTENCE_MANAGER_BINDING = "persistenceManagerBinding";

  
  
  /**
   * The DATAPROVIDER_CONNECTION configuration entry  
   * <p>
   * <p>
   * Each DATAPROVIDER_CONNECTION entry requires a matching {@link 
   * org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries#DELEGATION_PATH 
   * DELEGATION_PATH} entry in case of automatic {@link 
   * org.openmdx.compatibility.base.dataprovider.transport.adapter.Switch_1 
   * Switch_1} configuration in {@link 
   * org.openmdx.compatibility.base.dataprovider.transport.dispatching.Plugin_1 
   * Plugin_1}.
   */
  static public final String DATAPROVIDER_CONNECTION = "dataprovider";

  
  
  /**
   * The DELEGATION_PATH configuration entry.
   * <p>
   * Each {@link 
   * org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries#DATAPROVIDER_CONNECTION 
   * DATAPROVIDER_CONNECTION} entry requires a matching DELEGATION_PATH entry 
   * in case of automatic {@link 
   * org.openmdx.compatibility.base.dataprovider.transport.adapter.Switch_1 
   * Switch_1} configuration in {@link 
   * org.openmdx.compatibility.base.dataprovider.transport.dispatching.Plugin_1 
   * Plugin_1}.
   */
  static public final String DELEGATION_PATH = "delegationPath";

  
  
  /**
   * The MODEL_PACKAGE configuration entry. Contains
   * the fully qualified class name of a MOF model package, i.e.
   * 'org:omg:model1'.
   */
  static public final String MODEL_PACKAGE = "modelPackage";



  /**
   * The PACKAGE_IMPL configuration entry. Contains the
   * fully qualified package name of the MODEL_PACKAGE 
   * implementation classes, i.e. 'org.omg.model1.plugin.application'.
   */
  static public final String PACKAGE_IMPL = "packageImpl";



  /**
   * The MODEL configuration entry. Contains an object of type
   * Model_1_0. This configuration entry is set by the runtime.
   */
  static public final String MODEL = "model";



  /**
   * Defines whether the UID's should be in compressed or UUID format 
   * (default). 
   */
  static public final String COMPRESS_UID = "compressUID";

  
  
  /**
   * SEQUENCE_SUPPORTED flag
   */
  static public final String SEQUENCE_SUPPORTED = "sequenceSupported";



  /**
   * BATCH_SIZE for find operations
   */
  static public final String BATCH_SIZE = "batchSize";
    

  
  /**
   * Flag telling whether values for<ol>
   * <li><code>org::w3c::date</code>
   * <li><code>org::w3c::dateTime</code>
   * <li><code>org::w3c::duration</code>
   * </ol>should be represented by to their corresponding XML datatypes<ol>
   * <li><code>javax.xml.XMLGregorianCalendar</code>
   * <li><code>javax.xml.XMLGregorianCalendar</code>
   * <li><code>javax.xml.Duration</code>
   * </ol>
   * <p>
   * The default value is <code>false</code>.
   */
  static public final String XML_DATATYPES = "xmlDatatypes";



  /**
   * Defines whether a given layer is bypassed by lenient requests.
   * 
   * @see DataproviderRequestContexts#LENIENT
   */
  static public final String BYPASSED_BY_LENIENT_REQUESTS = "bypassedByLenientRequests";

  
  
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
          "The code '" + code + "' is unkown to the class SharedConfigurationEntries");
  }


}

// end-of-file
