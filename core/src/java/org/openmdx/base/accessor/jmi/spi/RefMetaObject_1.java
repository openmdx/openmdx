/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefMetaObject_1.java,v 1.2 2008/02/08 16:51:25 hburger Exp $
 * Description: RefMetaObject_1 class
 * Revision:    $Revision: 1.2 $
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Collection;
import java.util.List;

import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;

//---------------------------------------------------------------------------
public class RefMetaObject_1
  implements RefObject {

  //-------------------------------------------------------------------------
  public RefMetaObject_1(
    ModelElement_1_0 delegation
  ) {
    this.elementDef = delegation;
  }

  //-------------------------------------------------------------------------
  // RefMetaObject_1  
  //-------------------------------------------------------------------------
  public ModelElement_1_0 getElementDef(
  ) {
    return this.elementDef;
  }

  //-------------------------------------------------------------------------
  // RefObject  
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  public boolean refIsInstanceOf(
    RefObject objType,
    boolean considerSubtypes
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public RefClass refClass(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public RefFeatured refImmediateComposite(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public RefFeatured refOutermostComposite(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public void refDelete(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  // RefFeatured  
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  public void refSetValue(
    RefObject feature,
    java.lang.Object value
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public void refSetValue(
    String featureName,
    java.lang.Object value
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public java.lang.Object refGetValue(
    RefObject feature
  ) throws JmiException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public java.lang.Object refGetValue(
    String featureName
  ) throws JmiException {
    return this.elementDef.values(featureName);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public Object refInvokeOperation(
    RefObject requestedOperation,
    List args
  ) throws RefException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public Object refInvokeOperation(
    String operationName,
    List args
  ) throws RefException {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  // RefBaseObject  
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  public RefObject refMetaObject(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public RefPackage refImmediatePackage(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public RefPackage refOutermostPackage(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public String refMofId(
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public Collection<?> refVerifyConstraints(
    boolean deepVerify
  ) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  public boolean equals(
    Object other
  ) {
    return this.elementDef.equals(other);
  }

  //-------------------------------------------------------------------------
  public int hashCode(
  ) {
    return this.elementDef.hashCode();
  }

  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private final ModelElement_1_0 elementDef;

}
