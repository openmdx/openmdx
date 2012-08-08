/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ModelConstraintsChecker_1.java,v 1.17 2008/02/14 12:41:06 wfro Exp $
 * Description: check MOF Model Constraints
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/14 12:41:06 $
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
package org.openmdx.model1.layer.application;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.model1.code.DirectionKind;
import org.omg.model1.code.ScopeKind;
import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.ModelConstraints;
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.code.Multiplicities;

/**
 * This class allows you to verify model constraints for<p>
 * <ul>
 * <li>a single model element, or</li>
 * <li>all the model elements contained in the package content</li>
 * </ul><p>
 * It is mostly used to check the MOF Model Constraints but other constraints
 * can be added easily.<p> 
 * For details on MOF Model Constraints please refer to the MOF Specification 
 * which can be obtained from the OMG (http://www.omg.org)
 */
public class ModelConstraintsChecker_1 {
  
  /**
   * Constructor
   */
  public ModelConstraintsChecker_1(
    Model_1_0 model
  ) {
    this.model = model;
  }

  //---------------------------------------------------------------------------
  private ModelElement_1_0 getType(
    ModelElement_1_0 typedElementDef
  ) throws ServiceException {
    try {
      SysLog.trace("try to retrieve the dereferenced type for " + typedElementDef.values("type").get(0));
      ModelElement_1_0 typeDef = this.model.getDereferencedType(
        typedElementDef.values("type").get(0)
      );
      return typeDef;
    }
    catch(ServiceException ex) {
      if (ex.getExceptionStack().getExceptionCode() == BasicException.Code.NOT_FOUND)
      {
        throw new ServiceException(
          ex,
          ModelExceptions.MODEL_DOMAIN,
          ModelExceptions.REFERENCED_ELEMENT_TYPE_NOT_FOUND_IN_REPOSITORY,
          new BasicException.Parameter[]{
            new BasicException.Parameter("element", typedElementDef.path()),
            new BasicException.Parameter("referenced element type", typedElementDef.values("type").get(0))
          },
          "model repository does not contain the defined type" 
        );
      }
      throw ex;
    }
  }
  
  //---------------------------------------------------------------------------
  /**
   * Verifies all the constraints for all the elements that are contained in the
   * current packageContent
   * @exception ServiceException If a constraint was violated 
   */  
  public void verify(
  ) throws ServiceException {

    List violations = new ArrayList();
    
    for (
      Iterator it = this.model.getContent().iterator();
      it.hasNext();
    ) {
      verify((ModelElement_1_0)it.next(), violations);
    }
    
    if (!violations.isEmpty()) {
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.CONSTRAINT_VIOLATION,
        (BasicException.Parameter[]) violations.toArray(new BasicException.Parameter[0]),
        "at least one model constraint is violated, for details refer to parameters" 
      );
    }
  }
  
  //---------------------------------------------------------------------------  
  /**
   * Verifies all the constraints for a selected model element
   * @param modelElement The model element for which you want to verify the
   *                      applicable constraints
   * @exception ServiceException If a constraint was violated 
   */  
  public void verifyModelElement(
    ModelElement_1_0 elementDef
  ) throws ServiceException {

    List violations = new ArrayList();
    
    verify(elementDef, violations);
    
    if (!violations.isEmpty()) {
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.CONSTRAINT_VIOLATION,
        (BasicException.Parameter[]) violations.toArray(new BasicException.Parameter[0]),
        "at least one model constraint is violated, for details refer to parameters" 
      );
    }
  }

  //---------------------------------------------------------------------------  
  private void verify(
    ModelElement_1_0 elementDef,
    List violations
  ) throws ServiceException {
    
    SysLog.trace("checking all applicable constraints for element " + elementDef.values("qualifiedName").get(0));
    
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ATTRIBUTE)) {
      SysLog.trace("checking all ATTRIBUTE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyCannotBeDerivedAndChangeable(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ELEMENT)) {
      SysLog.trace("checking all ELEMENT constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyMustBeContainedUnlessPackage(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.NAMESPACE)) {
      SysLog.trace("checking all NAMESPACE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyContentNamesMustNotCollide(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.GENERALIZABLE_ELEMENT)) {
      SysLog.trace("checking all GENERALIZABLE_ELEMENT constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifySupertypeKindMustBeSame(elementDef, violations);
      this.verifyContentsMustNotCollideWithSupertypes(elementDef, violations);
      this.verifyDiamondRuleMustBeObeyed(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.TYPED_ELEMENT)) {
      SysLog.trace("checking all TYPED_ELEMENT constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyAssociationsCannotBeTypes(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASS)) {
      SysLog.trace("checking all CLASS constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyAbstractClassesCannotBeSingleton(elementDef, violations);
      this.verifyClassContainmentRules(elementDef, violations);
    }
    
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.DATATYPE)) {
      SysLog.trace("checking all DATATYPE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyDataTypeContainmentRules(elementDef, violations);
      this.verifyDataTypesHaveNoSupertypes(elementDef, violations);
      this.verifyDataTypesCannotBeAbstract(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.REFERENCE)) {
      SysLog.trace("checking all REFERENCE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyReferenceMultiplicityMustMatchEnd(elementDef, violations);
      this.verifyReferenceMustBeInstanceScoped(elementDef, violations);
      this.verifyChangeableReferenceMustHaveChangeableEnd(elementDef, violations);
      this.verifyReferenceTypeMustMatchEndType(elementDef, violations);
      this.verifyReferencedEndMustBeNavigable(elementDef, violations);
      this.verifyContainerMustMatchExposedType(elementDef, violations);
    }
    
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.OPERATION)) {
      SysLog.trace("checking all OPERATION constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyOperationContainmentRules(elementDef, violations);
      this.verifyOperationsHaveAtMostOneReturn(elementDef, violations);
      this.verifyOperationParametersMustBeParameterClasses(elementDef, violations);
      this.verifyOperationExceptionsMustBeExceptionClasses(elementDef, violations);
    }    
    
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.EXCEPTION)) {
      SysLog.trace("checking all EXCEPTION constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyExceptionContainmentRules(elementDef, violations);
      this.verifyExceptionsHaveOnlyOutParameters(elementDef, violations);
    }    

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ASSOCIATION)) {
      SysLog.trace("checking all ASSOCIATION constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyAssociationContainmentRules(elementDef, violations);
      this.verifyAssociationsHaveNoSupertypes(elementDef, violations);
      this.verifyAssociationsCannotBeAbstract(elementDef, violations);
      this.verifyAssociationsMustBePublic(elementDef, violations);
      this.verifyAssociationsMustBeBinary(elementDef, violations);
      this.verifyAssociationEnds(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ASSOCIATION_END)) {
      SysLog.trace("checking all ASSOCIATION_END constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyEndTypeMustBeClass(elementDef, violations);
      this.verifyCannotHaveTwoAggregateEnds(elementDef, violations);
      this.verifyCannotHaveMoreThanOneQualifier(elementDef, violations);
      this.verifyMultiplicityForNonPrimitiveQualifier(elementDef, violations);
      this.verifyMultiplicityForPrimitiveQualifier(elementDef, violations);
      this.verifyChangeabilityForNonPrimitiveQualifier(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PACKAGE)) {
      SysLog.trace("checking all PACKAGE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyPackageContainmentRules(elementDef, violations);
      this.verifyPackagesCannotBeAbstract(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.IMPORT)) {
      SysLog.trace("checking all IMPORT constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyCanOnlyImportPackagesAndClasses(elementDef, violations);
      this.verifyCannotImportSelf(elementDef, violations);
      this.verifyCannotImportNestedComponents(elementDef, violations);
      this.verifyNestedPackagesCannotImport(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CONSTRAINT)) {
      SysLog.trace("checking all CONSTRAINT constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyCannotConstrainThisElement(elementDef, violations);
      this.verifyConstraintsLimitedToContainer(elementDef, violations);
    }
    
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CONSTANT)) {
      SysLog.trace("checking all CONSTANT constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyConstantsTypeMustBePrimitive(elementDef, violations);
    }
    
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_FIELD)) {
      SysLog.trace("checking all STRUCTURE_FIELD constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyStructureFieldContainmentRules(elementDef, violations);
    }

    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_TYPE)) {
      SysLog.trace("checking all STRUCTURE_TYPE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyMustHaveFields(elementDef, violations);
    }

    if(
      elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURAL_FEATURE) ||
      elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ASSOCIATION_END) ||
      elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PARAMETER) ||
      elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_FIELD) ||
      elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.COLLECTION_TYPE)
    ) {
      SysLog.trace("checking all STRUCTURAL_FEATURE/ASSOCIATION_END/PARAMETER/STRUCTURE_FIELD/COLLECTION_TYPE constraints for element " + elementDef.values("qualifiedName").get(0));
      this.verifyMultiplicity(elementDef, violations);
    }
    
  }

  //---------------------------------------------------------------------------  
  /**
   * An Attribute cannot be derived and changeable.
   * (openMDX Constraint)
   */
  private void verifyCannotBeDerivedAndChangeable(
    ModelElement_1_0 attributeDef,
    List violations
  ) {
    if (
      ((Boolean)attributeDef.values("isDerived").get(0)).booleanValue() &&
      ((Boolean)attributeDef.values("isChangeable").get(0)).booleanValue()
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.CANNOT_BE_DERIVED_AND_CHANGEABLE,
          attributeDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An AssociationEnd can have at most one qualifier.
   * (openMDX Constraint)
   */
  private void verifyCannotHaveMoreThanOneQualifier(
    ModelElement_1_0 associationEndDef,
    List violations
  ) {
    if (associationEndDef.values("qualifierName").size() > 1) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.CANNOT_HAVE_MORE_THAN_ONE_QUALIFIER,
          associationEndDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An AssociationEnd with a non primitive type qualifier (class) must have 
   * multiplicity 0..n.
   * (openMDX Constraint)
   */
  private void verifyMultiplicityForNonPrimitiveQualifier(
    ModelElement_1_0 associationEndDef,
    List violations
  ) throws ServiceException {    
    if(associationEndDef.values("qualifierType").size() > 0) {
        ModelElement_1_0 qualifierType = this.model.getDereferencedType(associationEndDef.values("qualifierType").get(0));      
      if(
        !qualifierType.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PRIMITIVE_TYPE) &&
        !Multiplicities.MULTI_VALUE.equals(associationEndDef.values("multiplicity").get(0))
      ) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.NON_PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_TO_N,
            associationEndDef.values("qualifiedName").get(0)
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An AssociationEnd with a primitive type qualifier must have multiplicity 
   * 0..1 or 1..1.
   * (openMDX Constraint)
   */
  private void verifyMultiplicityForPrimitiveQualifier(
    ModelElement_1_0 associationEndDef,
    List violations
  ) throws ServiceException {
    
    if(associationEndDef.values("qualifierType").size() > 0) {
        ModelElement_1_0 qualifierType = this.model.getDereferencedType(associationEndDef.values("qualifierType").get(0));      
      if(
        qualifierType.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PRIMITIVE_TYPE) &&
        !((String)associationEndDef.values("multiplicity").get(0)).endsWith("..1")
      ) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_OR_1_TO_1,
            associationEndDef.values("qualifiedName").get(0)
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * The parameters of an operation must be parameter classes.
   * (openMDX Constraint)
   */
  private void verifyOperationParametersMustBeParameterClasses(
    ModelElement_1_0 operationDef,
    List violations
  ) throws ServiceException {
    
    if(!operationDef.values("parameter").isEmpty()) {
      for(
        Iterator it = operationDef.values("parameter").iterator();
        it.hasNext();
      ) {        
        ModelElement_1_0 parameterDef = this.model.getElement(it.next());
        
        // check argument to be PARAMETER
        if(!ModelAttributes.PARAMETER.equals(parameterDef.values(SystemAttributes.OBJECT_CLASS).get(0))) {
          violations.add(
            new BasicException.Parameter(
              ModelConstraints.OPERATION_ARGUMENTS_MUST_BE_PARAMETER,
              new Object[] { 
                operationDef.values("qualifiedName").get(0), 
                parameterDef.values("name").get(0)
              }
            )
          );
          return;
        }

        // check type of parameter to be STRUCTURE
        if(!this.model.isStructureType(this.model.getDereferencedType(parameterDef.values("type").get(0)))) {      
          violations.add(
            new BasicException.Parameter(
              ModelConstraints.PARAMETER_TYPE_MUST_BE_STRUCTURE_TYPE,
              new Object[] { 
                operationDef.values("qualifiedName").get(0), 
                parameterDef.values("name").get(0)
              }
            )
          );
          return;
        }        
      }      
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * The exceptions of an operation must be exception classes.
   * (openMDX Constraint)
   */
  private void verifyOperationExceptionsMustBeExceptionClasses(
      ModelElement_1_0 operationDef,
      List violations
  ) throws ServiceException {
      if(!operationDef.values("exception").isEmpty()) {
          for(
              Iterator it = operationDef.values("exception").iterator();
              it.hasNext();
          ) {        
              Path exceptionPath = (Path)it.next();
              String qualifiedExceptionName = exceptionPath.getBase();
              try {
                  ModelElement_1_0 exceptionDef = this.model.getElement(qualifiedExceptionName);
                  // check argument to be PARAMETER
                  if(!ModelAttributes.EXCEPTION.equals(exceptionDef.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                      violations.add(
                          new BasicException.Parameter(
                              ModelConstraints.OPERATION_EXCEPTION_MUST_BE_EXCEPTION,
                              new Object[] { 
                                  operationDef.values("qualifiedName").get(0), 
                                  exceptionDef.values("name").get(0)
                              }
                          )
                      );
                      return;
                  }
              }
              catch(ServiceException ex) {
                  if (ex.getExceptionStack().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                      throw ex;
                  }
                  else {
                      throw new ServiceException(
                          ex,
                          ModelExceptions.MODEL_DOMAIN,
                          ModelExceptions.EXCEPTION_TYPE_NOT_FOUND_IN_REPOSITORY,
                          new BasicException.Parameter[]{
                              new BasicException.Parameter("operation", operationDef),
                              new BasicException.Parameter("exception", ">" + exceptionPath + "<")
                          },
                          "Exception " + qualifiedExceptionName + " not found thrown by operation " + operationDef.path().getBase() 
                      );
                  }
              }
          }      
      }
  }

  //---------------------------------------------------------------------------  
  /**
   * A ModelElement that is not a Package must have a container
   * (for details refer to MOF Spec. MOF Model Constraints [C-1])
   */
  private void verifyMustBeContainedUnlessPackage(
    ModelElement_1_0 elementDef,
    List violations
  ) {
    if(!elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PACKAGE)) {
      if (elementDef.values("container").size() != 1) {
          violations.add(
            new BasicException.Parameter(
              ModelConstraints.MUST_BE_CONTAINED_UNLESS_PACKAGE,
              elementDef.values("qualifiedName").get(0)
            )
          );
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  /**
   * The names of the contents of a Namespace must not collide
   * (for details refer to MOF Spec. MOF Model Constraints [C-5])
   */
  private void verifyContentNamesMustNotCollide(
    ModelElement_1_0 modelNamespace,
    List violations
  ) throws ServiceException {
    
    if (!modelNamespace.values("content").isEmpty()) {
      Set contentNames = new HashSet();
      for(
        Iterator it = modelNamespace.values("content").iterator();
        it.hasNext();
      ) {        
        ModelElement_1_0 elementDef = this.model.getElement(it.next());
        if (contentNames.contains(elementDef.values("name").get(0))) {
            violations.add(
              new BasicException.Parameter(
                ModelConstraints.CONTENT_NAMES_MUST_NOT_COLLIDE,
                new Object[] {
                modelNamespace.values("qualifiedName").get(0),
                elementDef.values("name").get(0)
              }                
              )
            );
        } else {
          contentNames.add(elementDef.values("name").get(0));
        }
      }
    }
  }
  
  //---------------------------------------------------------------------------  
//  /**
//   * A Generalizable Element cannot be its own direct or indirect supertype
//   * (for details refer to MOF Spec. MOF Model Constraints [C-6])
//   */
//  private void verifySupertypeMustNotBeSelf(
//    ModelElement_1_0 modelGeneralizableElement,
//    List violations
//  ) throws ServiceException {
//      
//    for(
//      Iterator it = modelGeneralizableElement.values("allSupertype").iterator();
//      it.hasNext();
//    ) {        
//      ModelElement_1_0 elementDef = this.model.getElement(it.next());
//      if (modelGeneralizableElement.equals(elementDef)) {
//        violations.add(
//          new BasicException.Parameter(
//            ModelConstraints.SUPERTYPE_MUST_NOT_BE_SELF,
//            modelGeneralizableElement.values("qualifiedName").get(0)
//          )
//        );
//      }
//    }
//  }
  
  //---------------------------------------------------------------------------  
  /**
   * A supertype of a GeneralizableElement must be of the same kind as the 
   * GeneralizableElement itself
   * (for details refer to MOF Spec. MOF Model Constraints [C-7])
   */
  private void verifySupertypeKindMustBeSame(
    ModelElement_1_0 modelGeneralizableElement,
    List violations
  ) throws ServiceException {
    
    for(
      Iterator it = modelGeneralizableElement.values("supertype").iterator();
      it.hasNext();
    ) {        
      ModelElement_1_0 elementDef = this.model.getElement(it.next());
      if (
        !modelGeneralizableElement.values(SystemAttributes.OBJECT_CLASS).get(0).equals(
          elementDef.values(SystemAttributes.OBJECT_CLASS).get(0)
        )
      ) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.SUPERTYPE_KIND_MUST_BE_SAME,
            new Object[] {
              modelGeneralizableElement.values("qualifiedName").get(0),
              elementDef.values("qualifiedName").get(0)
            }
          )
        );
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  /**
   * The names of the contents of a GeneralizableElement should not collide 
   * with the names of the contents of any direct or indirect supertype
   * (for details refer to MOF Spec. MOF Model Constraints [C-8])
   */
  private void verifyContentsMustNotCollideWithSupertypes(
    ModelElement_1_0 modelGeneralizableElement,
    List violations
  ) throws ServiceException {
    
    if(!modelGeneralizableElement.values("feature").isEmpty()) {
      
      // instead of collecting all contents of all direct and indirect 
      // supertypes, the BasicException.Parameter 'feature' is used; this works because 
      // 'feature' contains all the contents of all direct and indirect
      // supertypes and the type itself
      Set featureNames = new HashSet();
      for(
        Iterator it = modelGeneralizableElement.values("feature").iterator();
        it.hasNext();
      ) {        
        ModelElement_1_0 elementDef = this.model.getElement(it.next());
        if (featureNames.contains(elementDef.values("name").get(0))) {
          violations.add(
            new BasicException.Parameter(
              ModelConstraints.CONTENTS_MUST_NOT_COLLIDE_WITH_SUPERTYPES,
              new Object[] {
                modelGeneralizableElement.values("qualifiedName").get(0),
                elementDef.values("name").get(0)
              }                
            )
          );
        } else {
          featureNames.add(elementDef.values("name").get(0));
        }
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  /**
   * Multiple inheritance must obey the 'Diamond Rule'
   * (for details refer to MOF Spec. MOF Model Constraints [C-9])
   */
  private void verifyDiamondRuleMustBeObeyed(
    ModelElement_1_0 generalizableElementDef,
    List violations
  ) throws ServiceException {
    if(generalizableElementDef.getValues("allSupertype") != null) {     
      Map allFeatures = new HashMap();
      for(
        Iterator i = generalizableElementDef.values("allSupertype").iterator();
        i.hasNext();
      ) {
        ModelElement_1_0 supertype = this.model.getElement(i.next());
        for(
          Iterator j = supertype.values("feature").iterator();
          j.hasNext();
        ) {
          ModelElement_1_0 feature = this.model.getElement(j.next());
          ModelElement_1_0 inspected = (ModelElement_1_0)allFeatures.get(feature.values("name").get(0));
          if(
            (inspected != null) && 
            !(inspected.values("qualifiedName").get(0).equals(feature.values("qualifiedName").get(0)))
          ) {
            violations.add(
              new BasicException.Parameter(
                ModelConstraints.DIAMOND_RULE_MUST_BE_OBEYED,
                new Object[] {
                  generalizableElementDef.values("qualifiedName").get(0),
                  feature.values("name").get(0)
                }                
              )
            );
          }
          else {
            allFeatures.put(
              feature.values("name").get(0),
              feature
            );
          }
        }
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  /**
   * An Association cannot be the type of a TypedElement
   * (for details refer to MOF Spec. MOF Model Constraints [C-13])
   */
  private void verifyAssociationsCannotBeTypes(
    ModelElement_1_0 typedElementDef,
    List violations
  ) throws ServiceException {
    
    ModelElement_1_0 elementDef = this.getType(typedElementDef);
    if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ASSOCIATION)) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ASSOCIATIONS_CANNOT_BE_TYPES,
          typedElementDef.values("qualifiedName").get(0)
        )
      );
    }    
  }
  
  //---------------------------------------------------------------------------  
  private void verifyChangeabilityForNonPrimitiveQualifier(
    ModelElement_1_0 associationEndDef,
    List violations
  ) throws ServiceException {
    if(associationEndDef.values("qualifierType").size() > 0) {
      ModelElement_1_0 qualifierType = this.model.getDereferencedType(associationEndDef.values("qualifierType").get(0));      
      if(
        !qualifierType.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PRIMITIVE_TYPE) &&
        ((Boolean)associationEndDef.values("isChangeable").get(0)).booleanValue()
      ) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.ASSOCIATION_END_WITH_COMPLEX_QUALIFIER_MUST_BE_FROZEN,
            associationEndDef.values("qualifiedName").get(0)
          )
        );
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  /**
   * A Class may contain only Classes, DataTypes, Attributes, References,
   * Operations, Exceptions, Constants, Constraints, and Tags
   * (for details refer to MOF Spec. MOF Model Constraints [C-15])
   */
  private void verifyClassContainmentRules(
    ModelElement_1_0 classDef,
    List violations
  ) throws ServiceException {
    
    verifyContainmentRules(
      classDef,
      new String[] { 
        ModelAttributes.CLASS, ModelAttributes.DATATYPE, 
        ModelAttributes.ATTRIBUTE, ModelAttributes.REFERENCE,
        ModelAttributes.OPERATION, ModelAttributes.EXCEPTION,
        ModelAttributes.CONSTANT, ModelAttributes.CONSTRAINT,
        ModelAttributes.TAG
      },
      ModelConstraints.CLASS_CONTAINMENT_RULES,
      violations
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * A Class that is marked as abstract cannot also be marked as singleton
   * (for details refer to MOF Spec. MOF Model Constraints [C-16])
   */
  private void verifyAbstractClassesCannotBeSingleton(
    ModelElement_1_0 classDef,
    List violations
  ) {
    if (
      ((Boolean) classDef.values("isAbstract").get(0)).booleanValue() &&
      ((Boolean) classDef.values("isSingleton").get(0)).booleanValue()
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ABSTRACT_CLASSES_CANNOT_BE_SINGLETON,
          classDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * A DataType may contain only TypeAliases, Constraints, Tags (or in the case 
   * of StructureTypes) StructureFields
   * (for details refer to MOF Spec. MOF Model Constraints [C-17])
   */
  private void verifyDataTypeContainmentRules(
    ModelElement_1_0 dataTypeDef,
    List violations
  ) throws ServiceException {
    
    for(
      Iterator it = dataTypeDef.values("content").iterator();
      it.hasNext();
    ) {        
      ModelElement_1_0 elementDef = this.model.getElement(it.next());
      if(
        (!elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ALIAS_TYPE)) &&
        (!elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CONSTRAINT)) &&
        (!elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.TAG))
      ) {

          if (
          !dataTypeDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_TYPE) ||
          !elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_FIELD)
        ) {
          violations.add(
            new BasicException.Parameter(
              ModelConstraints.DATA_TYPE_CONTAINMENT_RULES,
              new Object[] { 
                dataTypeDef.values("qualifiedName").get(0), 
                elementDef.values("name").get(0)
              }
            )
          );
        }
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Inheritance / generalization is not applicable to DataTypes
   * (for details refer to MOF Spec. MOF Model Constraints [C-19])
   */
  private void verifyDataTypesHaveNoSupertypes(
    ModelElement_1_0 modelDataType,
    List violations
  ) {
    if (!modelDataType.values("supertype").isEmpty()) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.DATA_TYPES_HAVE_NO_SUPERTYPES,
          modelDataType.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * A DataType cannot be abstract
   * (for details refer to MOF Spec. MOF Model Constraints [C-20])
   */
  private void verifyDataTypesCannotBeAbstract(
    ModelElement_1_0 dataTypeDef,
    List violations
  ) {
    if (((Boolean)dataTypeDef.values("isAbstract").get(0)).booleanValue()) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.DATA_TYPES_CANNOT_BE_ABSTRACT,
          dataTypeDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * The multiplicity for a Reference must be the same as the multiplicity for 
   * the referenced AssociationEnd
   * (for details refer to MOF Spec. MOF Model Constraints [C-21])
   */
  private void verifyReferenceMultiplicityMustMatchEnd(
    ModelElement_1_0 referenceDef,
    List violations
  ) throws ServiceException {

    ModelElement_1_0 referencedEnd = this.model.getElement(
      referenceDef.values("referencedEnd").get(0)
    );
    if (
      !referenceDef.values("multiplicity").get(0).equals(
        referencedEnd.values("multiplicity").get(0)
      )
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.REFERENCE_MULTIPLICITY_MUST_MATCH_END,
          referenceDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Classifier scoped References are not meaningful in the current M1 level 
   * computational model
   * (for details refer to MOF Spec. MOF Model Constraints [C-22])
   */
  private void verifyReferenceMustBeInstanceScoped(
    ModelElement_1_0 referenceDef,
    List violations
  ) {
    if (!referenceDef.values("scope").get(0).equals(ScopeKind.INSTANCE_LEVEL)) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.REFERENCE_MUST_BE_INSTANCE_SCOPED,
          referenceDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * A Reference can be changeable only if the referenced AssociationEnd is also 
   * changeable
   * (for details refer to MOF Spec. MOF Model Constraints [C-23])
   */
  private void verifyChangeableReferenceMustHaveChangeableEnd(
    ModelElement_1_0 referenceDef,
    List violations
  ) throws ServiceException {

    ModelElement_1_0 referencedEnd = this.model.getElement(
      referenceDef.values("referencedEnd").get(0)
    );
    if (
      !((Boolean)referenceDef.values("isChangeable").get(0)).booleanValue() ==
       ((Boolean)referencedEnd.values("isChangeable").get(0)).booleanValue()
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.CHANGEABLE_REFERENCE_MUST_HAVE_CHANGEABLE_END,
          referenceDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * The type attribute of a Reference and its referenced AssociationEnd must be
   * the same
   * (for details refer to MOF Spec. MOF Model Constraints [C-24])
   */
  private void verifyReferenceTypeMustMatchEndType(
    ModelElement_1_0 referenceDef,
    List violations
  ) throws ServiceException {

    ModelElement_1_0 referencedEnd = this.model.getElement(
      referenceDef.values("referencedEnd").get(0)
    );
    if (
      !referenceDef.values("type").get(0).equals(
        referencedEnd.values("type").get(0)
      )
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.REFERENCE_TYPE_MUST_MATCH_END_TYPE,
          referenceDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * A Reference is only allowed for a navigable AssociationEnd
   * (for details refer to MOF Spec. MOF Model Constraints [C-25])
   */
  private void verifyReferencedEndMustBeNavigable(
    ModelElement_1_0 referenceDef,
    List violations
  ) throws ServiceException {

    ModelElement_1_0 referencedEnd = this.model.getElement(
      referenceDef.values("referencedEnd").get(0)
    );
    if (!((Boolean)referencedEnd.values("isNavigable").get(0)).booleanValue()) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.REFERENCED_END_MUST_BE_NAVIGABLE,
          referenceDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------
  private void verifyAssociationEnds(
    ModelElement_1_0 associationDef,
    List violations
  ) throws ServiceException {
    
    List ends = this.getAssociationEnds(associationDef);
    if(ends.size() != 2) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ASSOCIATIONS_CONTAINMENT_RULES,
          associationDef.values("qualifiedName").get(0)
        )
      );
      return;
    }
    ModelElement_1_0 end1 = (ModelElement_1_0)ends.get(0);
    ModelElement_1_0 end2 = (ModelElement_1_0)ends.get(1);
    
    if(
      !AggregationKind.NONE.equals(end1.values("aggregation").get(0)) &&
      !AggregationKind.NONE.equals(end2.values("aggregation").get(0))
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ONE_ASSOCIATION_END_MUST_HAVE_AGGREGATION_NONE,
          associationDef.values("qualifiedName").get(0)
        )
      );
    }
    
    // !NONE --> primitive qualifier and multiplicity 0..1|1..1
    if(
      !AggregationKind.NONE.equals(end1.values("aggregation").get(0)) &&
      ((end1.values("qualifierType").size() < 1) || 
      !this.model.isPrimitiveType(end1.values("qualifierType").get(0)) ||
      (!Multiplicities.OPTIONAL_VALUE.equals(end1.values("multiplicity").get(0))) && !Multiplicities.SINGLE_VALUE.equals(end1.values("multiplicity").get(0)))
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY,
          end1.values("qualifiedName").get(0)
        )
      );
    }

    // !NONE --> primitive qualifier and multiplicity 0..1|1..1
    if(
      !AggregationKind.NONE.equals(end2.values("aggregation").get(0)) &&
      ((end2.values("qualifierType").size() < 1) || 
      !this.model.isPrimitiveType(end2.values("qualifierType").get(0)) ||
      (!Multiplicities.OPTIONAL_VALUE.equals(end2.values("multiplicity").get(0))) && !Multiplicities.SINGLE_VALUE.equals(end2.values("multiplicity").get(0)))
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY,
          end2.values("qualifiedName").get(0)
        )
      );
    }

    // qualifier -> isNavigable
    if(
      (end1.values("qualifierType").size() >= 1) &&
      !((Boolean)end1.values("isNavigable").get(0)).booleanValue()
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.QUALIFIER_REQUIRES_NAVIGABILITY,
          end1.values("qualifiedName").get(0)
        )
      );
    }

    // qualifier -> isNavigable
    if(
      (end2.values("qualifierType").size() >= 1) &&
      !((Boolean)end2.values("isNavigable").get(0)).booleanValue()
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.QUALIFIER_REQUIRES_NAVIGABILITY,
          end2.values("qualifiedName").get(0)
        )
      );
    }
           
    // NONE -->    no qualifier & any multiplicity
    //          || primitive qualifier & multiplicity 0..1 ||
    //          || class qualifier & multiplicity 0..n    
    if(
      AggregationKind.NONE.equals(end1.values("aggregation").get(0)) &&
      (end1.values("qualifierType").size() >= 1) &&
      (!this.model.isPrimitiveType(end1.values("qualifierType").get(0)) || !Multiplicities.OPTIONAL_VALUE.equals(end1.values("multiplicity").get(0))) &&
      (!this.model.isClassType(end1.values("qualifierType").get(0)) || !Multiplicities.MULTI_VALUE.equals(end1.values("multiplicity").get(0)))      
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.AGGREGATION_NONE_REQUIRES_NO_OR_UNIQUE_PRIMITIVE_OR_NON_UNIQUE_CLASS_QUALIFIER,
          end1.values("qualifiedName").get(0)
        )
      );
    }

    // NONE -->    no qualifier & any multiplicity
    //          || primitive qualifier & multiplicity 0..1
    //          || class qualifier & multiplicity 0..n    
    if(
      AggregationKind.NONE.equals(end2.values("aggregation").get(0)) &&
      (end2.values("qualifierType").size() >= 1) &&
      (!this.model.isPrimitiveType(end2.values("qualifierType").get(0)) || !Multiplicities.OPTIONAL_VALUE.equals(end2.values("multiplicity").get(0))) &&
      (!this.model.isClassType(end2.values("qualifierType").get(0)) || !Multiplicities.MULTI_VALUE.equals(end2.values("multiplicity").get(0)))      
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.AGGREGATION_NONE_REQUIRES_NO_OR_UNIQUE_PRIMITIVE_OR_NON_UNIQUE_CLASS_QUALIFIER,
          end1.values("qualifiedName").get(0)
        )
      );
    }

    // end1:class qualifier --> end2:no or primitive qualifier
    if(
      (end1.values("qualifierType").size() >= 1) &&
      this.model.isClassType(end1.values("qualifierType").get(0)) &&
      (end2.values("qualifierType").size() >= 1) && 
      !this.model.isPrimitiveType(end2.values("qualifierType").get(0))
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER,
          new Object[]{
            end1.values("qualifiedName").get(0),
            end2.values("qualifiedName").get(0)
          } 
        )
      );
    }

    // end2:class qualifier --> end1:no or numeric qualifier
    if(
      (end2.values("qualifierType").size() >= 1) &&
      this.model.isClassType(end2.values("qualifierType").get(0)) &&
      (end1.values("qualifierType").size() >= 1) && 
      !this.model.isPrimitiveType(end1.values("qualifierType").get(0))
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER,
          new Object[]{
            end1.values("qualifiedName").get(0),
            end2.values("qualifiedName").get(0)
          } 
        )
      );
    }
    
  }

  //---------------------------------------------------------------------------  
  /**
   * The containing Class for a Reference must be equal to or a subtype of the 
   * type of the Reference's exposed AssociationEnd
   * (for details refer to MOF Spec. MOF Model Constraints [C-26])
   */
  private void verifyContainerMustMatchExposedType(
    ModelElement_1_0 referenceDef,
    List violations
  ) throws ServiceException {
    
    ModelElement_1_0 container = this.model.getElement(
      referenceDef.values("container").get(0)
    );
    ModelElement_1_0 exposedEnd = this.model.getElement(
      referenceDef.values("exposedEnd").get(0)
    );

    // precondition: container->allSupertype includes container itself
    if (!container.values("allSupertype").contains(
          exposedEnd.values("type").get(0)
        )
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.CONTAINER_MUST_MATCH_EXPOSED_TYPE,
          referenceDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An Operation may only contain Parameters, Constraints, and Tags
   * (for details refer to MOF Spec. MOF Model Constraints [C-28])
   */
  private void verifyOperationContainmentRules(
    ModelElement_1_0 operationDef,
    List violations
  ) throws ServiceException {
    
    verifyContainmentRules(
      operationDef,
      new String[] { 
        ModelAttributes.PARAMETER, ModelAttributes.CONSTRAINT,
        ModelAttributes.TAG
      },
      ModelConstraints.OPERATION_CONTAINMENT_RULES,
      violations
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * An Operation may have at most one Parameter whose direction is "return"
   * (for details refer to MOF Spec. MOF Model Constraints [C-29])
   */
  private void verifyOperationsHaveAtMostOneReturn(
    ModelElement_1_0 operationDef,
    List violations
  ) throws ServiceException {
    
    if (!operationDef.values("parameter").isEmpty()) {
      boolean foundReturnParam = false;
      for(
        Iterator it = operationDef.values("parameter").iterator();
        it.hasNext();
      ) {        
        ModelElement_1_0 modelParameter = this.model.getElement(it.next());
        if(modelParameter.values("direction").get(0).equals(DirectionKind.RETURN_DIR)) {
          if (!foundReturnParam) { foundReturnParam = true; }
          else {
              violations.add(
                new BasicException.Parameter(
                  ModelConstraints.OPERATIONS_HAVE_AT_MOST_ONE_RETURN,
                  new Object[] { 
                    operationDef.values("qualifiedName").get(0), 
                    modelParameter.values("name").get(0)
                  }
                )
              );
            return;
          }          
        }
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An Exception may only contain Parameters and Tags
   * (for details refer to MOF Spec. MOF Model Constraints [C-31])
   */
  private void verifyExceptionContainmentRules(
    ModelElement_1_0 exceptionDef,
    List violations
  ) throws ServiceException {
    
    verifyContainmentRules(
      exceptionDef,
      new String[] { 
        ModelAttributes.PARAMETER, ModelAttributes.TAG
      },
      ModelConstraints.EXCEPTION_CONTAINMENT_RULES,
      violations
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * An Exception's Parameters must all have the direction "out"
   * (for details refer to MOF Spec. MOF Model Constraints [C-32])
   */
  private void verifyExceptionsHaveOnlyOutParameters(
    ModelElement_1_0 exceptionDef,
    List violations
  ) throws ServiceException {
    
    for(
      Iterator it = exceptionDef.values("parameter").iterator();
      it.hasNext();
    ) {        
      ModelElement_1_0 modelParameter = this.model.getElement(it.next());
      if(!modelParameter.values("direction").get(0).equals(DirectionKind.OUT_DIR)) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.EXCEPTIONS_HAVE_ONLY_OUT_PARAMETERS,
            new Object[] { 
              exceptionDef.values("qualifiedName").get(0), 
              modelParameter.values("name").get(0)
            }
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An Association may only contain AssociationEnds, Constraints, and Tags
   * (for details refer to MOF Spec. MOF Model Constraints [C-33])
   */
  private void verifyAssociationContainmentRules(
    ModelElement_1_0 associationDef,
    List violations
  ) throws ServiceException {
    verifyContainmentRules(
      associationDef,
      new String[] { 
        ModelAttributes.ASSOCIATION_END, ModelAttributes.CONSTRAINT, 
        ModelAttributes.TAG
      },
      ModelConstraints.ASSOCIATIONS_CONTAINMENT_RULES,
      violations
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * Inheritance / generalization is not applicable to Associations
   * (for details refer to MOF Spec. MOF Model Constraints [C-34])
   */
  private void verifyAssociationsHaveNoSupertypes(
    ModelElement_1_0 associationDef,
    List violations
  ) {
    if (!associationDef.values("supertype").isEmpty()) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ASSOCIATIONS_HAVE_NO_SUPERTYPES,
          associationDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An Association cannot be abstract
   * (for details refer to MOF Spec. MOF Model Constraints [C-36])
   */
  private void verifyAssociationsCannotBeAbstract(
    ModelElement_1_0 associationDef,
    List violations
  ) {
    if (((Boolean) associationDef.values("isAbstract").get(0)).booleanValue()) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ASSOCIATIONS_CANNOT_BE_ABSTRACT,
          associationDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Associations must have visibility of "public"
   * (for details refer to MOF Spec. MOF Model Constraints [C-37])
   */
  private void verifyAssociationsMustBePublic(
    ModelElement_1_0 associationDef,
    List violations
  ) {
    if (!associationDef.values("visibility").get(0).equals(VisibilityKind.PUBLIC_VIS)) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ASSOCIATIONS_MUST_BE_PUBLIC,
          associationDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An Association must be binary; that is, it must have exactly two
   * AssociationEnds
   * (for details refer to MOF Spec. MOF Model Constraints [C-38])
   */
  private void verifyAssociationsMustBeBinary(
    ModelElement_1_0 associationDef,
    List violations
  ) throws ServiceException {
    if (getAssociationEnds(associationDef).size() != 2) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.ASSOCIATIONS_MUST_BE_BINARY,
          associationDef.values("qualifiedName").get(0)
        )
      );
    }     
  }

  //---------------------------------------------------------------------------  
  /**
   * The type of an AssociationEnd must be Class
   * (for details refer to MOF Spec. MOF Model Constraints [C-39])
   */
  private void verifyEndTypeMustBeClass(
    ModelElement_1_0 associationEndDef,
    List violations
  ) throws ServiceException {
    ModelElement_1_0 type = this.getType(associationEndDef);
    if (!type.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASS)) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.END_TYPE_MUST_BE_CLASS,
          associationEndDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * An Association cannot have an aggregation semantic specified for both 
   * AssociationEnds
   * (for details refer to MOF Spec. MOF Model Constraints [C-42])
   */
  private void verifyCannotHaveTwoAggregateEnds(
    ModelElement_1_0 associationEndDef,
    List violations
  ) throws ServiceException {
    // Note: the introduction of a derived BasicException.Parameter 'otherEnd' on an 
    //       AssociationEnd would simplify this operation
    
    if (!associationEndDef.values("aggregation").get(0).equals(AggregationKind.NONE)) {
      // get association this association end belongs to
        ModelElement_1_0 associationDef = this.model.getElement(
          associationEndDef.values("container").get(0)
        );

      for (
        Iterator it = getAssociationEnds(associationDef).iterator();
        it.hasNext();
      ) {
        DataproviderObject assEnd = (DataproviderObject) it.next();
            if (!associationEndDef.equals(assEnd)) {
              if (assEnd.values("aggregation").get(0).equals(AggregationKind.NONE)) {
                return;
              }
            }
      }

      violations.add(
        new BasicException.Parameter(
          ModelConstraints.CANNOT_HAVE_TWO_AGGREGATE_ENDS,
          associationDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * A Package may only contain Packages, Classes, DataTypes, Associations, 
   * Exceptions, Constants, Constraints, Imports, and Tags
   * (for details refer to MOF Spec. MOF Model Constraints [C-43])
   */
  private void verifyPackageContainmentRules(
    ModelElement_1_0 packageDef,
    List violations
  ) throws ServiceException {
    verifyContainmentRules(
      packageDef,
      new String[] { 
        ModelAttributes.PACKAGE, ModelAttributes.CLASS, 
        ModelAttributes.DATATYPE, ModelAttributes.ASSOCIATION, 
        ModelAttributes.EXCEPTION, ModelAttributes.CONSTANT, 
        ModelAttributes.CONSTRAINT, ModelAttributes.IMPORT, 
        ModelAttributes.TAG
      },
      ModelConstraints.PACKAGE_CONTAINMENT_RULES,
      violations
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * Packages cannot be declared as abstract
   * (for details refer to MOF Spec. MOF Model Constraints [C-44])
   */
  private void verifyPackagesCannotBeAbstract(
    ModelElement_1_0 packageDef,
    List violations
  ) {
    if (((Boolean)packageDef.values("isAbstract").get(0)).booleanValue()) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.DATA_TYPES_CANNOT_BE_ABSTRACT,
          packageDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * It is only legal for a Package to import or cluster Packages or Classes
   * (for details refer to MOF Spec. MOF Model Constraints [C-46])
   */
  private void verifyCanOnlyImportPackagesAndClasses(
    ModelElement_1_0 importDef,
    List violations
  ) throws ServiceException {
    for (
      Iterator it = importDef.values("importedNamespace").iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 elementDef = this.model.getElement(
        it.next()
      );
        if (
        !elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASS) &&
        !elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PACKAGE)
      ) {
          violations.add(
            new BasicException.Parameter(
              ModelConstraints.CAN_ONLY_IMPORT_PACKAGES_AND_CLASSES,
              importDef.values("qualifiedName").get(0)
            )
          );
        }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Packages cannot import or cluster themselves
   * (for details refer to MOF Spec. MOF Model Constraints [C-47])
   */
  private void verifyCannotImportSelf(
    ModelElement_1_0 importDef,
    List violations
  ) {
    for (
      Iterator it = importDef.values("importedNamespace").iterator();
      it.hasNext();
    ) {
      if (importDef.values("container").get(0).equals(it.next())) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.CANNOT_IMPORT_SELF,
            importDef.values("qualifiedName").get(0)
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Packages cannot import or cluster Packages or Classes that they contain
   * (for details refer to MOF Spec. MOF Model Constraints [C-48])
   */
  private void verifyCannotImportNestedComponents(
    ModelElement_1_0 importDef,
    List violations
  ) throws ServiceException {
    Set allContainerContents = getAllContents(
      this.model.getElement(
        importDef.values("container").get(0)
      )
    );

    for (
      Iterator it = importDef.values("importedNamespace").iterator();
      it.hasNext();
    ) {
      Path path = (Path) it.next();
      if (allContainerContents.contains(path)) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.CANNOT_IMPORT_NESTED_COMPONENTS,
            new Object[] {
              importDef.values("qualifiedName").get(0),
              (this.model.getElement(path)).values("qualifiedName").get(0)
            }
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Nested Packages cannot import or cluster other Packages or Classes
   * (for details refer to MOF Spec. MOF Model Constraints [C-49])
   */
  private void verifyNestedPackagesCannotImport(
    ModelElement_1_0 importDef,
    List violations
  ) throws ServiceException {
    if (!importDef.values("container").isEmpty()) {
        ModelElement_1_0 firstContainerElement = this.model.getElement(
          importDef.values("container").get(0)
        );
      if (!firstContainerElement.values("container").isEmpty()) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.NESTED_PACKAGES_CANNOT_IMPORT,
            importDef.values("qualifiedName").get(0)
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Constraints, Tags, Imports, and Constants cannot be constrained
   * (for details refer to MOF Spec. MOF Model Constraints [C-50])
   */
  private void verifyCannotConstrainThisElement(
    ModelElement_1_0 constraintDef,
    List violations
  ) {
    // due to the lack of the association 'Constrains' between classes
    // ModelElement and ModelConstraint this constraint cannot be implemented
  }

  //---------------------------------------------------------------------------  
  /**
   * A Constraint can only constrain ModelElements that are defined by or
   * inherited by its immediate container
   * (for details refer to MOF Spec. MOF Model Constraints [C-51])
   */
  private void verifyConstraintsLimitedToContainer(
    ModelElement_1_0 constraintDef,
    List violations
  ) {
    // due to the lack of the association 'Constrains' between classes
    // ModelElement and ModelConstraint this constraint cannot be implemented
  }

  //---------------------------------------------------------------------------  
  /**
   * The type of a Constant must be a PrimitiveType
   * (for details refer to MOF Spec. MOF Model Constraints [C-53])
   */
  private void verifyConstantsTypeMustBePrimitive(
    ModelElement_1_0 constantDef,
    List violations
  ) throws ServiceException {

    ModelElement_1_0 type = this.getType(constantDef);
    if (!type.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(
      ModelAttributes.PRIMITIVE_TYPE)
    ) {
      violations.add(
        new BasicException.Parameter(
          ModelConstraints.CONSTANTS_TYPE_MUST_BE_PRIMITIVE,
          constantDef.values("qualifiedName").get(0)
        )
      );
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * The "lower" bound of a MultiplicityType cannot be negative or "Unbounded"
   * (for details refer to MOF Spec. MOF Model Constraints [C-54])
   * 
   * The "lower" bound of a MultiplicityType cannot exceed the "upper" bound
   * (for details refer to MOF Spec. MOF Model Constraints [C-55])
   * 
   * The "upper" bound of a MultiplicityType cannot be less than 1
   * (for details refer to MOF Spec. MOF Model Constraints [C-56])
   */
  private void verifyMultiplicity(
    ModelElement_1_0 elementDef,
    List violations
  ) throws ServiceException {
    
    /**
     * get type of model element to verify (if any)
     */
    ModelElement_1_0 type = this.getType(elementDef);
    String multiplicity = (String) elementDef.values("multiplicity").get(0);

    /**
     * <<stream>> implies primitive types
     */
    if(
      Multiplicities.STREAM.equals(multiplicity) &&
      !type.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.PRIMITIVE_TYPE)
    ) {
        violations.add(
          new BasicException.Parameter(
            ModelConstraints.STEREOTYPE_STREAM_IMPLIES_PRIMITIVE_TYPE,
            elementDef.values("qualifiedName").get(0) + "; type=" + type
          )
        );
    }     

    // check for valid multiplicities
    if(
      Multiplicities.LIST.equals(multiplicity) || 
      Multiplicities.SET.equals(multiplicity) || 
      Multiplicities.SPARSEARRAY.equals(multiplicity) ||
      Multiplicities.STREAM.equals(multiplicity) ||
      Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ||
      Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
      Multiplicities.MULTI_VALUE.equals(multiplicity) ||      
      Multiplicities.MAP.equals(multiplicity)      
    ) {
      return;
    }
    
    violations.add(
      new BasicException.Parameter(
        ModelConstraints.INVALID_MULTIPLICITY,
        elementDef.values("qualifiedName").get(0)
      )
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * A StructureField contains Constraints and Tags
   * (for details refer to MOF Spec. MOF Model Constraints [C-58])
   */
  private void verifyStructureFieldContainmentRules(
    ModelElement_1_0 modelStructureField,
    List violations
  ) throws ServiceException {

    verifyContainmentRules(
      modelStructureField,
      new String[] { 
        ModelAttributes.CONSTRAINT, ModelAttributes.TAG
      },
      ModelConstraints.STRUCTURE_FIELD_CONTAINMENT_RULES,
      violations
    );
  }

  //---------------------------------------------------------------------------  
  /**
   * A StructureType must contain at least one StructureField
   * (for details refer to MOF Spec. MOF Model Constraints [C-59])
   */
  private void verifyMustHaveFields(
    ModelElement_1_0 modelStructureType,
    List violations
  ) throws ServiceException {

    for(
      Iterator it = modelStructureType.values("content").iterator();
      it.hasNext();
    ) {        
      if((this.model.getElement(it.next())).values(
        SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.STRUCTURE_FIELD)
      ) {
        return;
      }
    }
    return;
    /*
     * Structs may have 0 fields (e.g. required to model Void)
     */
    /*
    violations.add(
      new BasicException.Parameter(
        ModelConstraints.MUST_HAVE_FIELDS,
        modelStructureType.values("qualifiedName").get(0)
      )
    );
    */
  }

  //--------------------------------------------------------------------------- 
  /**
   * Verifies the containment rules for a given ModelElement
   */ 
  private void verifyContainmentRules(
    ModelElement_1_0 elementDef,
    String[] allowedContainments,
    String violatedConstraint,
    List violations
  ) throws ServiceException {

    for(
      Iterator it = elementDef.values("content").iterator();
      it.hasNext();
    ) {        
      ModelElement_1_0 contentElement = this.model.getElement(it.next());

      // build intersection of allowedContainments in elementDef and all
      // the types elementDef is an instance of
      // if the intersection is empty, then elementDef is not an allowed
      // containment instance
      Set intersection = new HashSet(Arrays.asList(allowedContainments));
      intersection.retainAll(
        contentElement.values(SystemAttributes.OBJECT_INSTANCE_OF)
      );

      if(intersection.isEmpty()) {
        violations.add(
          new BasicException.Parameter(
            violatedConstraint,
            new Object[] { 
              elementDef.values("qualifiedName").get(0), 
              contentElement.values("name").get(0)
            }
          )
        );
      }
    }
  }

  //---------------------------------------------------------------------------  
  /**
   * Computes all AssociationEnds for a given Association
   */
  private List getAssociationEnds(
    ModelElement_1_0 associationDef
  ) throws ServiceException {
    List associationEnds = new ArrayList();
    for(
      Iterator it = associationDef.values("content").iterator();
      it.hasNext();
    ) {        
      ModelElement_1_0 elementDef = this.model.getElement(it.next());
      if(elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(
          ModelAttributes.ASSOCIATION_END
        )
      ) {
        associationEnds.add(elementDef);
      }
    }
    return associationEnds;
  }

  //---------------------------------------------------------------------------  
  /**
   * Computes recursively the contents of a given ModelNamespace. This 
   * computation is based on the 'Contains' association between the classes
   * ModelElement and ModelNamespace
   */
  private Set getAllContents(
    ModelElement_1_0 namespaceDef
  ) throws ServiceException {
    
    Set allContents = new HashSet(namespaceDef.values("content"));
    for (
      Iterator it = namespaceDef.values("content").iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 elementDef = this.model.getElement(it.next());
      if (elementDef.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.NAMESPACE)
      ) {
        allContents.addAll(getAllContents(elementDef));
      }
    }
      
    return allContents; 
  }

  //---------------------------------------------------------------------------  
  // Constants and Variables
  //---------------------------------------------------------------------------  

  private Model_1_0 model = null;
}
