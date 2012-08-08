/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ListStructure_1.java,v 1.9 2008/03/19 17:06:09 hburger Exp $
 * Description: Structure_1_0 standard implementation
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:06:09 $
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * ListStructure_1
 */
@SuppressWarnings("unchecked")
class ListStructure_1
  implements Serializable, Structure_1_0 
{

  /**
     * 
     */
    private static final long serialVersionUID = 3762537819129853747L;

/**
   * Constructor
   *
   * @param structureClass
   *        The model class of the structure to be created
   * @param fieldNames
   *        the structure's field names
   * @param values
   *        the structure's values
   *
   * @exception ServiceException BAD_PARAMETER
   *            if (fieldNames.size() != values.size())
   * @exception NullPointerException
   *            if either of the arguments is null
   */
  ListStructure_1(
    String structureType,
    List fieldNames,
    List values
  ){
    this.structureType = structureType;
    this.fieldNames = Collections.unmodifiableList(fieldNames);
    this.values = values;
  }

  /**
   * Constructor
   *
   * @param structureClass
   *        The model class of the structure to be created
   * @param fieldNames
   *        the structure's field names
   * @param values
   *        the structure's values
   *
   * @exception ServiceException BAD_PARAMETER
   *            if (fieldNames.length != values.length)
   * @exception NullPointerException
   *            if either of the arguments is null
   */
  ListStructure_1(
    String structureClass,
    String[] fieldNames,
    Object[] values
  ) throws ServiceException {
    this(
      structureClass,
      Arrays.asList(fieldNames),
      Arrays.asList(values)
    );
    if(fieldNames.length != values.length) throw new ServiceException(
    BasicException.Code.DEFAULT_DOMAIN, 
    BasicException.Code.BAD_PARAMETER,
      new BasicException.Parameter[]{
        new BasicException.Parameter("fieldNames", fieldNames.length),
        new BasicException.Parameter("values", values.length)
      },
      "The lengths of fieldNames and values do not match"
    );  
  }
  

  //--------------------------------------------------------------------------
  // Implements Structure_1_0
  //--------------------------------------------------------------------------

  /**
   * Returns the object's model class.
   *
   * @return    the object's model class
   */
  public String objGetType(
  ){
    return this.structureType;
  }

    /**
     * Return the field names in this structure.
     *
     * @return  the (String) field names contained in this structure
     */
    public List objFieldNames(
    ){
      return this.fieldNames;
  }
   
  /**
   * Get a field.
   *
   * @param       field
   *              the fields's name
   *
   * @return      the fields value which may be null.
   *
   * @exception   ServiceException BAD_PARAMETER
   *              if the structure has no such field
   */
  public Object objGetValue(
      String feature
  ) throws ServiceException {
    int index = this.fieldNames.indexOf(feature);
    if(index == -1) throw new ServiceException(
    BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_PARAMETER,
      new BasicException.Parameter[]{
        new BasicException.Parameter("feature", feature)
      },
      "This structure has no such field"
    );
    return this.values.get(index);
  }

  //--------------------------------------------------------------------------
  // Object
  //--------------------------------------------------------------------------
  public String toString(
  ) {
    return
      "[structureType=" + this.structureType + ", " +
      "content=" + values.toString() + "]";
  }
  
  //--------------------------------------------------------------------------
  // Instance Members
  //--------------------------------------------------------------------------

  /**
   * The structure's model class
   */
  private final String structureType;
  
  /**
   * The structure's values.
   */
  protected final List values;

  /**
   * The structure's field names.
   */  
  private final List fieldNames;
  
}
