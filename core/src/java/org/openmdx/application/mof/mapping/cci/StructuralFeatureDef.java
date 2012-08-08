/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StructuralFeatureDef.java,v 1.5 2011/07/08 13:20:51 wfro Exp $
 * Description: VelocityStructuralFeatureDef class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/08 13:20:51 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.mof.mapping.cci;

import java.util.Set;

import org.omg.mof.spi.AbstractNames;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;

@SuppressWarnings({"rawtypes"})
public abstract class StructuralFeatureDef
  extends FeatureDef {

  //-------------------------------------------------------------------------
  public StructuralFeatureDef(
    String name,
    String qualifiedName,
    String annotation,
    Set stereotype,
    String visibility,
    String qualifiedTypeName,
    String multiplicity,
    Boolean isChangeable,
    Boolean isDerived 
  ) {
    super(
      name, 
      qualifiedName, 
      annotation,
      stereotype,
      visibility
    );
    this.qualifiedTypeName = qualifiedTypeName;
    this.multiplicity = multiplicity;
    this.isChangeable = isChangeable;
    this.isDerived = isDerived;

    // bean setter/getter names
    boolean forBoolean = PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName);
    boolean singleValued = 
        Multiplicity.SINGLE_VALUE.toString().equals(multiplicity) ||
        Multiplicity.OPTIONAL.toString().equals(multiplicity);
    this.beanGenericName = AbstractNames.capitalize(name);
    this.beanGetterName = AbstractNames.openmdx2AccessorName(
        name,
        true, // forQuery
        forBoolean,
        singleValued
    ); 
    this.beanSetterName = AbstractNames.openmdx2AccessorName(
        name,
        false, // forQuery
        forBoolean,
        singleValued
    );
  }
  
  //-------------------------------------------------------------------------
  public String getQualifiedTypeName(
  ) {
    return this.qualifiedTypeName;  
  }

  //-------------------------------------------------------------------------
  public String getMultiplicity(
  ) {
    return this.multiplicity;  
  }
  
  //-------------------------------------------------------------------------
  public boolean isChangeable(
  ) {
    return (this.isChangeable != null) && this.isChangeable.booleanValue();
  }

  //-------------------------------------------------------------------------
  public boolean isDerived(
  ) {
    return (this.isDerived != null) && this.isDerived.booleanValue();
  }

  //-------------------------------------------------------------------------
  public String getBeanGetterName(
  ) {
    return this.beanGetterName;
  }

  //-------------------------------------------------------------------------
  public String getBeanSetterName(
  ) {
    return this.beanSetterName;
  }

  //-------------------------------------------------------------------------
  public String getBeanGenericName(
  ) {
    return this.beanGenericName;
  }

  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------  
  private final String qualifiedTypeName;
  private final String multiplicity;
  private final Boolean isChangeable;
  private final Boolean isDerived;
  private final String beanGetterName;
  private final String beanSetterName;
  private final String beanGenericName;
  
}
