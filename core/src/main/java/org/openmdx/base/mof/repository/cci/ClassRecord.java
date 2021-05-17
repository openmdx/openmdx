/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::Class Record
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
package org.openmdx.base.mof.repository.cci;

import java.util.Map;

import org.openmdx.base.marshalling.TypeSafeMarshaller;
import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Class Record
 */
public interface ClassRecord extends ClassifierRecord {
    
  String NAME = "org:omg:model1:Class";
  
  enum Member {
    modifiedAt,
    visibility,
    modifiedBy,
    attribute,
    supertype,
    annotation,
    operation,
    identity,
    isAbstract,
    allFeature,
    stereotype,
    createdAt,
    name,
    feature,
    allSupertype,
    allSubtype,
    qualifiedName,
    compositeReference,
    reference,
    content,
    field,
    isSingleton,
    createdBy,
    container,
    subtype,
    allFeatureWithSubtype
  }

  boolean isSingleton();
  Map<String, Path> getAllFeature();
  Map<String, Path> getAllFeatureWithSubtype();

  /**
   * Return the set of attributes and references of the specified class, 
   * and if specified its subtypes.
   *  
   * @param classDef class to get feature of.  
   * @param includeSubtypes if true, in addition returns the features
   *         of the subtypes of class.
   * @param includeDerived if false, only non-derived attributes are returned.
   *         if true, derived and non-derived attributes are returned.
   * @param attributesOnly 
   *         if true return the same result as getAttributeDefs;
   *         if false include references not stored as attributes
   * @return Map map of features of class, its supertypes and subtypes. The
   *          map contains an entry of the form (featureName, featurePath).
   */
  Map<String, Path> getStructuralFeature(
        TypeSafeMarshaller<Path, ElementRecord> marshaller,
        boolean includeSubtypes,
        boolean includeDerived,
        boolean attributesOnly
  );
  
}
