/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LayerConfigurationEntries.java,v 1.16 2007/12/14 15:45:52 hburger Exp $
 * Description: Generated constants for LayerConfigurationEntries
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/14 15:45:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.dataprovider.layer.model;


/**
 * The <code>ModelLayerConfigurationEntries</code> class contains
 * constants identifying the dataprovider configuration entries.
 */




public class LayerConfigurationEntries extends org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries {

  
  protected LayerConfigurationEntries() {
   // Avoid instantiation
  }


  /**
   * Path to real role types. (Where they are stored on db)
   */
  static public final String ROLE_TYPE_MAPPING_REAL_PATH = "RoleTypeMappingRealPath";



  /**
   * Reference path to virtual role types. (The ones which get mapped to the ones above)
   */
  static public final String ROLE_TYPE_MAPPING_VIRTUAL_REFERENCE_PATH = "RoleTypeMappingVirtualReferencePath";



  /**
   * The class of the real role type at this path.
   */
  static public final String ROLE_TYPE_MAPPING_REAL_CLASS = "RoleTypeMappingRealClass";



  /**
   * The class of the virtual role type at this path.
   */
  static public final String ROLE_TYPE_MAPPING_VIRTUAL_CLASS = "RoleTypeMappingVirtualClass";



  /**
   * Use this setting to set if holes in the validity of the objects are allowed.
   * Default is true.
   */
  static public final String ENABLE_HOLES_IN_OBJECT_VALIDITY = "EnableHolesInObjectValidity";

  
  
  /**
   * Enable the creation of another valid state.
   * Default is false.
   */
  static public final String ENABLE_DISJUNCT_STATE_CREATION = "EnableDisjunctStateCreation";

  
  
  /**
   * Enables showDB if at trace level
   * Default is false.
   */
  static public final String ENABLE_SHOW_DB = "EnableShowDB";

  
  
  /**
   * Defines, which state is selected in absence of a validity specification other than validAt.
   * <p>
   * Valid values are<ul>
   * <li><code>current</code> (default)
   * <li><code>initial</code>
   * </ul>
   * @see #CURRENT_STATE 
   * @see #INITIAL_STATE 
   */
  static public final String DEFAULT_STATE = "DefaultState";

  /**
   * Defines, that the current state is the default state
   */
  static public final String CURRENT_STATE = "CurrentState";
  
  /**
   * Defines, that the initial state is the default state
   */
  static public final String INITIAL_STATE = "InitialState";
  
  
 
  
  /**
   * @deprecated use DISABLE_HISTORY_REFERENCE_PATTERN instead
   */
  static public final String COMPATIBILITY_DISABLE_HISTORY_REFERENCE_PATTERN = "DisableHistoryPathPattern";
  
  
  
  /**
   * Use this setting to disable history states for the specified references.
   */
  static public final String DISABLE_HISTORY_REFERENCE_PATTERN = "DisableHistoryReferencePattern";

  
  
  /**
   * Use this setting to define the maximum length for createdBy's and 
   * modifiedBy's principal chains. 
   */
  static public final String PRINCIPAL_LIMIT = "principalLimit";
  
  
  
  /**
   * Tells whether warnings should be propagated to the client
   */
  static public final String THROW_WARNING = "throwWarning";

  /**
   * If true, calls notifyPreDelete() before an object is removed.
   * notifiyPreDelete() is called for the removed object and recursively
   * for each of its composite objects.   
   */
  static public final String NOTIFY_PRE_DELETE = "notifyPreDelete";

  
  
  /**
   * Tells whether the <code>NoState_1</code> plug-in holds a single state per 
   * object instead of an object which is valid forever.
   * <p> 
   * Default is <code>false</code>, i.e. no state mode where each object is
   * valid forever.
   */
  static public final String SINGLE_STATE_MODE = "SingleStateMode";

  
  
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
