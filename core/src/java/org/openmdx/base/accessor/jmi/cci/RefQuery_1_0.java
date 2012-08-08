/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefQuery_1_0.java,v 1.3 2009/12/20 18:42:47 wfro Exp $
 * Description: RefFilter_1_0 interface
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/20 18:42:47 $
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

import javax.jdo.Query;

import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.Filter;

/**
 * The RefQuery_1_0 is an extension to JMI and allows to query the Collections
 * returned by JMI methods.
 */
public interface RefQuery_1_0 extends Query, Serializable {

  /**
   * This operation allows to set a filter value with the semantics
   * <quantor> <fieldName> <operator> <values>. The value constants for
   * the quantor and operator parameters are implementation-specific.
   */
  public void refAddValue(
    String featureName,
    short quantor,
    short operator,
    Collection<?> values
  );

  /**
   * Allows to specify the sort order for a field.
   */
  public void refAddValue(
    String featureName,
    short order
  );

  /**
   * Allows to specify the sort order for a field.
   * 
   * @param featureDef
   * @param index
   * @param order
   */
  void refAddValue(
    ModelElement_1_0 featureDef,
    int index,
    short order
  );
  
  /**
   * Allows to specify the sort order for a field.
   * 
   * @param featureDef
   * @param index
   * @param order
   */
  void refAddValue(
    String featureName,
    int index,
    short order
  );
  
  /**
   * This operation allows to set a filter value with the semantics
   * <quantor> <fieldName> <operator> <values>. The value constants for
   * the quantor and operator parameters are implementation-specific.
   * 
   * @param featureDef
   * @param quantor
   * @param operator
   * @param values
   */
  void refAddValue(
    ModelElement_1_0 featureDef,
    short quantor,
    short operator,
    Collection<?> values
  );

  /**
   * Returns the collection of added filter properties, i.e. attributes
   * added with refAddValue(fieldName, quantor, operator).
   * 
   * @return a collection of filter properties
   */
  public Filter refGetFilter(
  );

  /**
   * Retrieve the filter's MOF id
   * 
   * @return the filter's MOF id
   */
  public String refMofId();
    
}

//--- End of File -----------------------------------------------------------
