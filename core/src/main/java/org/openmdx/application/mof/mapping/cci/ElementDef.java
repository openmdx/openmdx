/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: VelocityElementDef class
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.mof.mapping.cci;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"rawtypes","unchecked"})
public abstract class ElementDef {

  //-------------------------------------------------------------------------
  public ElementDef(
          String name,
          String qualifiedName,
          String annotation,
          Set stereotype
  ) {
    this.name = name;
    this.qualifiedName = qualifiedName;
    this.annotation = annotation;
    this.stereotype = stereotype;
    this.isDeprecatedAnnotation = this.annotation != null && this.annotation.contains("@deprecated");
  }

  //-------------------------------------------------------------------------
  public String getName(
  ) {
    return this.name;  
  }

  //-------------------------------------------------------------------------
  public String getQualifiedName(
  ) {
    return this.qualifiedName;  
  }

  //-------------------------------------------------------------------------
  public String getAnnotation(
  ) {
    return this.annotation;  
  }

  //-------------------------------------------------------------------------
  public Set getStereotype(
  ) {
    return this.stereotype;  
  }
  
  //-------------------------------------------------------------------------
  public void setStereotype(
    Set stereotype
  ) {
    this.stereotype = stereotype;
  }

  //-------------------------------------------------------------------------
  public void setStereotype(
    String stereotype
  ) {
    this.stereotype = new HashSet();
    this.stereotype.add(stereotype);
  }

  //-------------------------------------------------------------------------
  public boolean isDeprecatedAnnotation() {
    return this.isDeprecatedAnnotation;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString(
  ) {
    return this.qualifiedName;
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private final String name;
  private final String qualifiedName;
  private final String annotation;
  private Set stereotype = null;
  private final boolean isDeprecatedAnnotation;

}
