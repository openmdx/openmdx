/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AttributeDef.java,v 1.5 2007/03/26 16:09:17 hburger Exp $
 * Description: VelocityAttributeDef class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/26 16:09:17 $
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
package org.openmdx.model1.mapping;

import java.util.HashSet;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;

public class AttributeDef 
  extends StructuralFeatureDef {

  //-------------------------------------------------------------------------
  public AttributeDef(
    ModelElement_1_0 attributeDef,
    Model_1_3 model, 
    boolean openmdx1
  ) throws ServiceException {
    this(
        (String)attributeDef.values("name").get(0),
        (String)attributeDef.values("qualifiedName").get(0),
        (String)attributeDef.values("annotation").get(0),
        new HashSet(attributeDef.values("stereotype")),
        (String)attributeDef.values("visibility").get(0),
        (String)model.getDereferencedType(
            attributeDef.values("type").get(0),
            openmdx1
         ).values("qualifiedName").get(0),
        (String)attributeDef.values("multiplicity").get(0),
        (Boolean)attributeDef.values("isChangeable").get(0),
        (Boolean)attributeDef.values("isDerived").get(0), 
        openmdx1
    );
  }

  /**
   * Constructor 
   *
   * @param attributeDef
   * @param model
   * 
   * @deprecated use AttributeDef(ModelElement_1_0, Model_1_3, boolean)
   * 
   * @throws ServiceException
   */
  public AttributeDef(
      ModelElement_1_0 attributeDef,
      Model_1_0 model
    ) throws ServiceException {
      this(
          (String)attributeDef.values("name").get(0),
          (String)attributeDef.values("qualifiedName").get(0),
          (String)attributeDef.values("annotation").get(0),
          new HashSet(attributeDef.values("stereotype")),
          (String)attributeDef.values("visibility").get(0),
          (String)model.getDereferencedType(
              attributeDef.values("type").get(0)
           ).values("qualifiedName").get(0),
          (String)attributeDef.values("multiplicity").get(0),
          (Boolean)attributeDef.values("isChangeable").get(0),
          (Boolean)attributeDef.values("isDerived").get(0), 
          true
      );
    }
  
  //-------------------------------------------------------------------------
    public AttributeDef(
        String name,
        String qualifiedName,
        String annotation,
        Set stereotype,
        String visibility,
        String qualifiedTypeName,
        String multiplicity,
        Boolean isChangeable,
        Boolean isDerived, 
        boolean openmdx1
  ) {
   super(
      name, 
      qualifiedName, 
      annotation, 
      stereotype,
      visibility,
      qualifiedTypeName, 
      multiplicity,
      isChangeable,
      isDerived, openmdx1
    );
    this.maxLength = 0;
    }

  //-------------------------------------------------------------------------
  public AttributeDef(
    String name,
    String qualifiedName,
    String annotation,
    Set stereotype,
    String visibility,
    String qualifiedTypeName,
    String multiplicity,
    Boolean isChangeable,
    Boolean isDerived,
    int maxLength, boolean openmdx1
  ) {
    super(
      name, 
      qualifiedName, 
      annotation, 
      stereotype,
      visibility,
      qualifiedTypeName, 
      multiplicity,
      isChangeable,
      isDerived, openmdx1
    );
    this.maxLength = maxLength;
  }

  //-------------------------------------------------------------------------
  public int getMaxLength(
  ) {
    return this.maxLength;
  }
  
  //-------------------------------------------------------------------------
  private final int maxLength;
  
}
