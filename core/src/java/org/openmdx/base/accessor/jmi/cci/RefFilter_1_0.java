/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefFilter_1_0.java,v 1.6 2008/02/08 16:51:25 hburger Exp $
 * Description: RefFilter_1_0 interface
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.cci;

import java.io.Serializable;
import java.util.Collection;

import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.query.FilterProperty;

/**
 * The RefFilter is an extension to JMI and allows to filter the Collections
 * returned by JMI methods. The Collections returned are FilterableCollections 
 * which allow to filter the result set. The subSet() methods takes as parameter 
 * the refFilterProperties() and the orderBy() the refAttributeSpecifiers().
 * <p>
 * FilterableCollections and RefFilter_1_0 are an extension of JMI 1.0. 
 * 100% JMI-compliant applications should not use these methods.
 */
public interface RefFilter_1_0 
  extends Serializable {

  /**
   * This operation allows to set a filter value with the semantics
   * <quantor> <fieldName> <operator> <values>. The value constants for
   * the quantor and operator parameters are implementation-specific.
   */
  public void refAddValue(
    String fieldName,
    short quantor,
    short operator,
    Collection<?> values
  );

  /**
   * Allows to construct complex/nested filters.
   */
  public void refAddValue(
    String fieldName,
    short quantor,
    short operator,
    RefFilter_1_0 filter
  );

  /**
   * Allows to specify the sort order for a field.
   */
  public void refAddValue(
    String fieldName,
    short order
  );

  /**
   * Returns the collection of added filter properties, i.e. attributes
   * added with refAddValue(fieldName, quantor, operator).
   * 
   * @return a collection of filter properties
   */
  public Collection<FilterProperty> refGetFilterProperties(
  );

  /**
   * Returns the collection of added attribute specifiers, i.e. attributes
   * added with refAddValue(fieldName, order).
   * 
   * @return a collection of attribute specifiers
   */
  public Collection<AttributeSpecifier> refGetAttributeSpecifiers(
  );

  /**
   * Clears the filter and removes all previously added filter values. 
   */
  public void clear(
  );
  
}

//--- End of File -----------------------------------------------------------
