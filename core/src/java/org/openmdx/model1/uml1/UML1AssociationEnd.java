/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UML1AssociationEnd.java,v 1.1 2005/06/14 21:27:36 wfro Exp $
 * Description: lab client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/06/14 21:27:36 $
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

package org.openmdx.model1.uml1;

import java.util.ArrayList;
import java.util.List;



public class UML1AssociationEnd
  extends UML1ModelElement
{
  public UML1AssociationEnd(
      String id,
      String name,
      String qualifiedName,
      UML1VisibilityKind visibility,
      boolean isSpecification,
      UML1AggregationKind aggregation,
      UML1ChangeableKind changeability,
      boolean isNavigable
  ) {
    super(id, name, qualifiedName, visibility, isSpecification);
    this.setAggregation(aggregation);
    this.setChangeability(changeability);
    this.setNavigable(isNavigable);
    this.qualifier = new ArrayList();
    this.multiplicityRange = new UML1MultiplicityRange("1", "1");
  }

  public UML1AggregationKind getAggregation() {
    return aggregation;
  }

  public UML1ChangeableKind getChangeability() {
    return changeability;
  }

  public boolean isNavigable() {
    return isNavigable;
  }

  public void setAggregation(UML1AggregationKind kind) {
    aggregation = kind;
  }

  public void setChangeability(UML1ChangeableKind kind) {
    changeability = kind;
  }

  public void setNavigable(boolean b) {
    isNavigable = b;
  }

  public UML1MultiplicityRange getMultiplicityRange() {
    return multiplicityRange;
  }

  public void setMultiplicityRange(UML1MultiplicityRange range) {
    multiplicityRange = range;
  }

  public String getParticipant() {
    return participant;
  }

  public void setParticipant(String className) {
    participant = className;
  }

  public List getQualifier() {
    return this.qualifier;
  }

  private UML1MultiplicityRange multiplicityRange = null;
  private String participant = null;
  private UML1AggregationKind aggregation;
  private UML1ChangeableKind changeability;
  private boolean isNavigable = false;
  private List qualifier = null;
}