/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UML1Operation.java,v 1.1 2009/01/13 02:10:39 wfro Exp $
 * Description: lab client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:39 $
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

package org.openmdx.application.mof.externalizer.xmi.uml1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public class UML1Operation
  extends UML1BehavioralFeature
{
  public UML1Operation(
      String id,
      String name,
      String qualifiedName,
      UML1VisibilityKind visibility,
      boolean isSpecification,
      UML1ScopeKind ownerScope,
      boolean isQuery,
      boolean isRoot,
      boolean isLeaf,
      boolean isAbstract
  ) {
      super(id, name, qualifiedName, visibility, isSpecification, ownerScope, isQuery);
      this.setRoot(isRoot);
      this.setLeaf(isLeaf);
      this.setAbstract(isAbstract);
      parameters = new ArrayList();
  }
  
  public List getParameters()
  {
    return parameters;
  }

  // openMDX extension for convenience
  public List getParametersWithoutReturnParameter()
  {
    List params = new ArrayList(parameters);
    params.remove(this.getReturnParameter());
    return params;
  }

  // openMDX extension for convenience
  public UML1Parameter getReturnParameter(
  ) {
    for(
      Iterator it = this.parameters.iterator();
      it.hasNext();
    ) {
      UML1Parameter param = (UML1Parameter)it.next();
      if (UML1ParameterDirectionKind.RETURN.equals(param.getKind()))
      {
        return param;
      }
    }
    return null;
  }
  
  public boolean isAbstract() {
    return isAbstract;
  }

  public void setAbstract(boolean b) {
    isAbstract = b;
  }

  public boolean isLeaf() {
    return isLeaf;
  }

  public boolean isRoot() {
    return isRoot;
  }

  public void setLeaf(boolean b) {
    isLeaf = b;
  }

  public void setRoot(boolean b) {
    isRoot = b;
  }

  private boolean isRoot = false;
  private boolean isLeaf = false;
  private boolean isAbstract = false;
  private List parameters = null;
}
