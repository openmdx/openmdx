/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Model_1_0.java,v 1.2 2009/01/13 17:33:49 wfro Exp $
 * Description: Model_1_0 interface
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 17:33:49 $
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
package org.openmdx.base.mof.cci;

import java.util.Collection;
import java.util.Map;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Provides a simple interface to access org:omg:model1-compliant model 
 * elements. This interface is a very leightweight version of the MOF model 
 * interface and is used mainly for model-bootstraping.
 */
public interface Model_1_0 {

  /**
   * Adds to model the new model elements of the package specified by 
   * qualifiedPackageNames. The model is loaded from the corresponding XMI resource.
   *  
   * @throws ServiceException when model not found or not valid.
   */
  public void addModels(
    Collection<String> qualifiedModelNames
  ) throws ServiceException;
  
  /**
   * Returns the specified model element. The returned model elements are
   * org:omg:model1 compliant. Derived attributes are provided as well.
   * 
   * @throws ServiceException if model element not found.
   */
  public ModelElement_1_0 getElement(
    Object element
  ) throws ServiceException;

  /**
   * Returns the specified model element. The returned model elements are
   * org:omg:model1 compliant. Derived attributes are provided as well. Returns
   * null if the model element can not be found.
   */
  public ModelElement_1_0 findElement(
    Object element
  );

  /**
   * Returns all elements of the model package.
   */
  public Collection<ModelElement_1_0> getContent(
  ) throws ServiceException;

  /**
   * Dereferences the given 'element', i.e. if 'element' is an alias type returns the
   * referenced element recursively.
   */
  public ModelElement_1_0 getDereferencedType(
    Object element
  ) throws ServiceException;

  /**
   * Get dereferenced type of element.
   */
  public ModelElement_1_0 getElementType(
      ModelElement_1_0 element
  ) throws ServiceException;
  
  /**
   * Return the model element of type org:omg:model1:Reference corresponding to path.
   */
  public ModelElement_1_0 getReferenceType(
    Path path
  ) throws ServiceException;
   
  /**
   * Returns true if the given element is local to the given modelPackage.
   */
  public boolean isLocal(
    Object type,
    Object modelPackage
  ) throws ServiceException;

  /**
   * Looks up the definition of the structural feature of class, one of its
   * superclasses or subclasses.
   * 
   * @param classifierDef feature is looked up in specified class and the superclasses
   *         of the class.
   * @param feature name of the feature to look up.
   * @param includeSubtypes if true the subclasses are included in the search.
   * @return ModelElement_1_0 definition of the feature or null.
   */
  public ModelElement_1_0 getFeatureDef(
    ModelElement_1_0 classifierDef,
    String feature,
    boolean includeSubtypes
  ) throws ServiceException;

  /**
   * Return the set of attributes of the specified class, its supertypes
   * and if specified its subtypes.
   *  
   * @param classDef class to get attributes of.  
   * @param includeSubtypes if true, in addition returns the attributes
   *         of the subtypes of class.
   * @param includeDerived if false, only non-derived attributes are returned.
   *         if true, derived and non-derived attributes are returned.
   * @return Map map of attributes of class, its supertypes and subtypes. The
   *          map contains an entry of the form (attributeName, attributeDef).
   */
  public Map<String,ModelElement_1_0> getAttributeDefs(
    ModelElement_1_0 classDef,
    boolean includeSubtypes,
    boolean includeDerived
  ) throws ServiceException;

  /**
   * Returns true if the references of the referenced objects are stored
   * as path in the exposed object. These references can also be interpreted
   * attributes of complex type".
   */
  public boolean referenceIsStoredAsAttribute(
    Object referenceType
  ) throws ServiceException;

  /**
   * returns true if the association belonging to reference is derived. 
   */
  public boolean referenceIsDerived(
    Object referenceType
  ) throws ServiceException;

  /**
   * Checks whether the dereferenced 'type' is primitive.
   */
  public boolean isPrimitiveType(
    Object type
  ) throws ServiceException;

  /**
   * Checks whether the dereferenced 'type' is primitive and numeric.
   */
  public boolean isNumericType(
    Object type
  ) throws ServiceException;

  /**
   * returns true, if the given type is instanceof STRUCTURE_TYPE
   */
  public boolean isStructureType(
    Object type
  ) throws ServiceException;

  /**
   * returns true, if the given type is instanceof STRUCTURE_FIELD
   */
  public boolean isStructureFieldType(
    Object type
  ) throws ServiceException;

  /**
   * returns true, if the given type is instanceof CLASS
   */
  public boolean isClassType(
    Object type
  ) throws ServiceException;

  /**
   * Returns true, if the given type is instanceof ATTRIBUTE or REFERENCE.
   */
  public boolean isStructuralFeatureType(
    Object type
  ) throws ServiceException;

  /**
   * Returns true, if the given type is instanceof REFERENCE.
   */
  public boolean isReferenceType(
    Object type
  ) throws ServiceException;

  /**
   * Returns true, if the given type is instanceof ATTRIBUTE.
   */
  public boolean isAttributeType(
    Object type
  ) throws ServiceException;

  /**
   * Returns true, if the given type is instanceof OPERATION.
   */
  public boolean isOperationType(
    Object type
  ) throws ServiceException;

  /**
   * Returns true, if the given type is instanceof PACKAGE.
   */
  public boolean isPackageType(
    Object type
  ) throws ServiceException;

  /**
   * Tests whether classDef is a role, i.e. has either stereotype Stereotypes.ROLE
   * or inherits from a class with stereotype Stereotypes.ROLE.
   */
  public boolean isRoled(
    ModelElement_1_0 classDef
  ) throws ServiceException;

  /**
   * Verifies a single the value to be of the specified type. 
   * The multiplicity is required to validate Stereotypes.STREAM types only.
   * The values must be of well-known spice types which are:
   * <ul>
   *   <li>Structure_1_0</li>
   *   <li>Object_1_0</li>
   *   <li>Primitive types</li>
   *   <li>DataproviderObject</li>
   * </ul>
   * The verification is done recursively. In case of a violation
   * an exception is thrown containing the violation.
   * 
   * @param type if type == null then no verification is performed.
   * 
   * @param enforceRequired if true, all required feature of value 
   *         are verified. Otherwise verifies only the available features.
   * 
   * @param removeDerived removes all derived features.
   */
  public void verifyObject(
    Object value,
    Object type,
    String multiplicity,
    boolean enforceRequired
  ) throws ServiceException;

  /**
   * Validates the value to be of the specified type. Depending
   * on the multiplicity the value is validated to be a single value
   * or a collection. In case of a collection each element of the 
   * is validated. validateValue() is called to validate a single
   * value.
   */
  public void verifyObjectCollection(
    Object values,
    Object type,
    String multiplicity,
    boolean includeRequired
  ) throws ServiceException;
  
  /**
   * returns true, if the given DataproviderObject or DataObject_1_0 is instance of
   * the given type.
   */
  public boolean objectIsSubtypeOf(
    Object object,
    Object ofType
  ) throws ServiceException;

  public boolean isInstanceof(
    DataObject_1_0 object,
    Object type
  ) throws ServiceException;

  /**
   * Returns true if type is subtype of ofType. 
   */
  public boolean isSubtypeOf(
    Object type,
    Object ofType
  ) throws ServiceException;

  /**
   * Get the types referenced by path. Returned are the exposed and the 
   * referenced type. In case of operations the exposed type is relevant, 
   * in case of get, create, modify, remove, replace operations the referenced
   * type is relevant. In cases where referencedType = exposedType the model 
   * contains a recursion. In these cases the lastReferencedType might be important. 
   * E.g. to set the OBJECT_TYPE in cases of roles, the exposedClass and referencedClass 
   * are both 'Role' which is not the correct OBJECT_TYPE. In cases of
   * model recursions OBJECT_TYPE = lastReferencedClass.
   */
  public ModelElement_1_0[] getTypes(
    Path path
  ) throws ServiceException;

  /**
   * returns the Java package name of the given model element.
   */
  public String toJavaPackageName(
    Object type,
    String packageSuffix
  ) throws ServiceException;

  public String toJavaPackageName(
    Object type,
    String packageSuffix,
    boolean dereferenceType
  ) throws ServiceException;

}

//--- End of File -----------------------------------------------------------
