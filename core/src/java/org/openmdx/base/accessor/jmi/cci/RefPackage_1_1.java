/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RefPackage_1_1.java,v 1.4 2007/09/28 17:54:53 hburger Exp $
 * Description: RefPackage_1_1 interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/09/28 17:54:53 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.cci;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;

import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.query.FilterProperty;

/**
 * This interface extends the javax.jmi.reflect.RefPackage interface by
 * openMDX-specific helpers. This methods must not be used
 * by 100% JMI-compliant applications. 
 */
public interface RefPackage_1_1 
  extends RefPackage_1_0 {

  /**
   * Retrieves the JDO Persistence Manager delegating to this package.
   * 
   * @return the JDO Persistence Manager delegating to this package.
   */
  PersistenceManager refPersistenceManager(
  );

  /**
   * Returns a proxy implementing the same interfaces as the secondary
   * <code>RefObject and delegating to<ol>
   * <li>the primary <code>RefObject</code> if possible
   * <li>the secondary <code>RefObject</code> as fallback
   * </ol>
   * 
   * @param primary its methods override the secondary object's methods
   * @param secondary its interfacse are implemented by the proxy object as well
   * 
   * @return a proxy object delgating to the primary or secondary object as appropriate
   */
   RefObject refObject (
      RefObject primary,
      RefObject secondary
   );

   /**
    * Create a filter
    * 
    * @param filterClassName
    * @param filterProperties
    * @param attributeSpecifiers
    * @param delegateFilter
    * @param delegateQuantor
    * @param delegateName
    * 
    * @return a filter
    */
   RefFilter_1_0 refCreateFilter(
       String filterClassName,
       FilterProperty[] filterProperties,
       AttributeSpecifier[] attributeSpecifiers,
       RefFilter_1_0 delegateFilter, 
       Short delegateQuantor, 
       String delegateName
   );

}