/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UML1ModelElement.java,v 1.1 2009/01/13 02:10:38 wfro Exp $
 * Description: lab client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:38 $
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public abstract class UML1ModelElement {

  public UML1ModelElement(
      String id,
      String name,
      String qualifiedName,
      UML1VisibilityKind visibility,
      boolean isSpecification
  ) {
      this.id = id;
      this.setName(name);
      this.setQualifiedName(qualifiedName);
      this.setVisiblity(visibility);
      this.setSpecification(isSpecification);
      this.taggedValues = new HashSet();
      this.stereotypes = new HashSet();
      this.comment = new ArrayList<String>();
  }
  
  public Set getStereotypes() {
    return stereotypes;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String string) {
    name = string;
  }

  public List<String> getComment() {
    return this.comment;
  }

  // this is an openMDX extension
  public String getQualifiedName() {
    return qualifiedName;
  }

  // this is an openMDX extension
  public void setQualifiedName(String string) {
    qualifiedName = string;
  }

  public boolean isSpecification() {
    return isSpecification;
  }

  public UML1VisibilityKind getVisiblity() {
    return visiblity;
  }

  public void setSpecification(boolean b) {
    isSpecification = b;
  }

  public void setVisiblity(UML1VisibilityKind kind) {
    visiblity = kind;
  }

  public Set getTaggedValues() {
    return this.taggedValues;
  }

  private final String id;
  private Set stereotypes = null;
  private String name = null;
  private String qualifiedName = null;  // openMDX extension
  private UML1VisibilityKind visiblity = null;
  private boolean isSpecification = false;
  private Set taggedValues = null;
  private List<String> comment = null;
}
