/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: OperationDef.java,v 1.5 2007/10/10 16:06:11 hburger Exp $
 * Description: VelocityOperationDef class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:11 $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;

public class OperationDef 
  extends FeatureDef {
  
  //-------------------------------------------------------------------------
  public OperationDef(
    ModelElement_1_0 operationDef,
    Model_1_0 model, 
    boolean openmdx1
  ) throws ServiceException {
    this(
      (String)operationDef.values("name").get(0),
      (String)operationDef.values("qualifiedName").get(0),
      (String)operationDef.values("annotation").get(0),
      new HashSet(operationDef.values("stereotype")),
      (String)operationDef.values("visibility").get(0),
      getResultParamTypeName(operationDef, (Model_1_3)model, openmdx1),
      getInParamTypeName(operationDef, (Model_1_3)model, openmdx1),
      ((Boolean)operationDef.values("isQuery").get(0)).booleanValue(),
      getInParameterFields(operationDef, (Model_1_3)model, openmdx1),
      getExceptions(operationDef, model, openmdx1)
    );
  }
  
  //-------------------------------------------------------------------------
  private static List getExceptions(
    ModelElement_1_0 operationDef,
    Model_1_0 model, 
    boolean openmdx1
  ) throws ServiceException {  
    List exceptions = new ArrayList();
    for(
      Iterator i = operationDef.values("exception").iterator();
      i.hasNext();
    ) {
      exceptions.add(
        new ExceptionDef(
          model.getElement(i.next()),
          model, 
          openmdx1
        )
      );
    }
    return exceptions;    
  }
  
  //-------------------------------------------------------------------------
  private static String getResultParamTypeName(
    ModelElement_1_0 operationDef,
    Model_1_3 model, 
    boolean openmdx1
  )  throws ServiceException {
    for(
      Iterator i = operationDef.values("content").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 paramDef = model.getElement(i.next());
      if("result".equals(paramDef.values("name").get(0))) {
        return (String)model.getDereferencedType(
          paramDef.values("type").get(0),
          openmdx1
        ).values("qualifiedName").get(0);
      }
    }
    throw new ServiceException(
      BasicException.Code.DEFAULT_DOMAIN,
      BasicException.Code.ASSERTION_FAILURE,
      new BasicException.Parameter[]{
        new BasicException.Parameter("operation", operationDef.path())
      },
      "no parameter with name \"result\" defined for operation"
    );
  }
  
  //-------------------------------------------------------------------------
  private static String getInParamTypeName(
    ModelElement_1_0 operationDef,
    Model_1_3 model, boolean openmdx1
  )  throws ServiceException {
    for(
      Iterator i = operationDef.values("content").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 paramDef = model.getElement(i.next());
      if("in".equals(paramDef.values("name").get(0))) {
        return (String)model.getDereferencedType(
          paramDef.values("type").get(0),
          openmdx1
        ).values("qualifiedName").get(0);
      }
    }
    throw new ServiceException(
      BasicException.Code.DEFAULT_DOMAIN,
      BasicException.Code.ASSERTION_FAILURE,
      new BasicException.Parameter[]{
        new BasicException.Parameter("operation", operationDef.path())
      },
      "no parameter with name \"in\" defined for operation"
    );
  }
  
  //-------------------------------------------------------------------------
  private static List getInParameterFields(
    ModelElement_1_0 operationDef,
    Model_1_3 model, 
    boolean openmdx1
  ) throws ServiceException {

    HashMap params = new HashMap();
    for(
      Iterator i = operationDef.values("content").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 param = model.getElement(i.next());
      params.put(
        param.values("name").get(0),
        param
      );
    }
    // in and result must be there
    if(params.get("in") == null) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE,
        new BasicException.Parameter[]{
          new BasicException.Parameter("operation", operationDef.path()),
          new BasicException.Parameter("params", params)
        },
        "no parameter with name \"in\" defined for operation"
      );
    }

    // fields of in-parameter
    ModelElement_1_0 inParamType = model.getDereferencedType(
      ((ModelElement_1_0)params.get("in")).values("type").get(0),
      openmdx1
    );
    List fields = new ArrayList();
    for(
      Iterator it = inParamType.values("content").iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 inParam = model.getElement(it.next());
      fields.add(
        new AttributeDef(
          inParam, 
          model, 
          openmdx1
        )
      );
    }
    return fields;
  }
  
  //-------------------------------------------------------------------------
    public OperationDef(
    String name,
    String qualifiedName,
    String annotation,
    Set stereotype,
    String visibility,
    String qualifiedReturnTypeName,
    String qualifiedInParameterTypeName,
    boolean isQuery,
    List parameters,
    List exceptions
  ) {
    super(
      name, 
      qualifiedName, 
      annotation,
      stereotype,
      visibility
    );
    this.parameters = parameters;
    this.exceptions = exceptions;
    this.qualifiedReturnTypeName = qualifiedReturnTypeName;
    this.qualifiedInParameterTypeName = qualifiedInParameterTypeName;
    this.isQuery = isQuery;
    }
  
  //-------------------------------------------------------------------------
  public String getQualifiedReturnTypeName(
  ) {
    return this.qualifiedReturnTypeName;  
  }

  //-------------------------------------------------------------------------
  public String getQualifiedInParameterTypeName(
  ) {
    return this.qualifiedInParameterTypeName;  
  }

  //-------------------------------------------------------------------------
  public boolean isQuery(
  ) {
    return this.isQuery;
  }

  //-------------------------------------------------------------------------
  public List getParameters(
  ) {
    return this.parameters;
  }

  //-------------------------------------------------------------------------
  public List getExceptions(
  ) {
    return this.exceptions;
  }

  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private final List parameters;
  private final List exceptions;
  private final String qualifiedReturnTypeName;
  private final String qualifiedInParameterTypeName;
  private final boolean isQuery;

}
