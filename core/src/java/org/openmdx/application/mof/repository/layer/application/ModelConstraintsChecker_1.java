/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: check MOF Model Constraints
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.mof.repository.layer.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.mof.cci.DirectionKind;
import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.ModelConstraints;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

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
@SuppressWarnings({"rawtypes","unchecked"})
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
        return this.model.getElementType(
            typedElementDef
        );
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
                "at least one model constraint is violated, for details refer to parameters",
                (BasicException.Parameter[]) violations.toArray(new BasicException.Parameter[0]) 
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
                "at least one model constraint is violated, for details refer to parameters",
                (BasicException.Parameter[]) violations.toArray(new BasicException.Parameter[0]) 
            );
        }
    }

    //---------------------------------------------------------------------------  
    private void verify(
        ModelElement_1_0 elementDef,
        List violations
    ) throws ServiceException {

        SysLog.trace("checking all applicable constraints for element " + elementDef.objGetValue("qualifiedName"));

        if(elementDef.isAttributeType()) {
            SysLog.trace("checking all ATTRIBUTE constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyCannotBeDerivedAndChangeable(elementDef, violations);
        }
        if(elementDef.isElementType()) {
            SysLog.trace("checking all ELEMENT constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyMustBeContainedUnlessPackage(elementDef, violations);
        }
        if(elementDef.isNamespaceType()) {
            SysLog.trace("checking all NAMESPACE constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyContentNamesMustNotCollide(elementDef, violations);
        }
        if(elementDef.isGeneralizableElementType()) {
            SysLog.trace("checking all GENERALIZABLE_ELEMENT constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifySupertypeKindMustBeSame(elementDef, violations);
            this.verifyContentsMustNotCollideWithSupertypes(elementDef, violations);
            this.verifyDiamondRuleMustBeObeyed(elementDef, violations);
        }
        if(elementDef.isTypedElementType() && !elementDef.isPrimitiveType()) {
            SysLog.trace("checking all TYPED_ELEMENT constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyAssociationsCannotBeTypes(elementDef, violations);
        }
        if(elementDef.isClassType()) {
            SysLog.trace("checking all CLASS constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyAbstractClassesCannotBeSingleton(elementDef, violations);
            this.verifyClassContainmentRules(elementDef, violations);
        }
        if(elementDef.isDataType()) {
            SysLog.trace("checking all DATATYPE constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyDataTypeContainmentRules(elementDef, violations);
            this.verifyDataTypesHaveNoSupertypes(elementDef, violations);
            this.verifyDataTypesCannotBeAbstract(elementDef, violations);
        }
        if(elementDef.isReferenceType()) {
            SysLog.trace("checking all REFERENCE constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyReferenceMultiplicityMustMatchEnd(elementDef, violations);
            this.verifyReferenceMustBeInstanceScoped(elementDef, violations);
            this.verifyChangeableReferenceMustHaveChangeableEnd(elementDef, violations);
            this.verifyReferenceTypeMustMatchEndType(elementDef, violations);
            this.verifyReferencedEndMustBeNavigable(elementDef, violations);
            this.verifyContainerMustMatchExposedType(elementDef, violations);
        }
        if(elementDef.isOperationType()) {
            SysLog.trace("checking all OPERATION constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyOperationContainmentRules(elementDef, violations);
            this.verifyOperationsHaveAtMostOneReturn(elementDef, violations);
            this.verifyOperationParametersMustBeParameterClasses(elementDef, violations);
            this.verifyOperationExceptionsMustBeExceptionClasses(elementDef, violations);
        }    
        if(elementDef.isExceptionType()) {
            SysLog.trace("checking all EXCEPTION constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyExceptionContainmentRules(elementDef, violations);
            this.verifyExceptionsHaveOnlyOutParameters(elementDef, violations);
        }    
        if(elementDef.isAssociationType()) {
            SysLog.trace("checking all ASSOCIATION constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyAssociationContainmentRules(elementDef, violations);
            this.verifyAssociationsHaveNoSupertypes(elementDef, violations);
            this.verifyAssociationsCannotBeAbstract(elementDef, violations);
            this.verifyAssociationsMustBePublic(elementDef, violations);
            this.verifyAssociationsMustBeBinary(elementDef, violations);
            this.verifyAssociationEnds(elementDef, violations);
        }
        if(elementDef.isAssociationEndType()) {
            SysLog.trace("checking all ASSOCIATION_END constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyEndTypeMustBeClass(elementDef, violations);
            this.verifyCannotHaveTwoAggregateEnds(elementDef, violations);
            this.verifyCannotHaveMoreThanOneQualifier(elementDef, violations);
            this.verifyMultiplicityForNonPrimitiveQualifier(elementDef, violations);
            this.verifyMultiplicityForPrimitiveQualifier(elementDef, violations);
            this.verifyChangeabilityForNonPrimitiveQualifier(elementDef, violations);
        }
        if(elementDef.isPackageType()) {
            SysLog.trace("checking all PACKAGE constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyPackageContainmentRules(elementDef, violations);
            this.verifyPackagesCannotBeAbstract(elementDef, violations);
        }
        if(elementDef.isImportType()) {
            SysLog.trace("checking all IMPORT constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyCanOnlyImportPackagesAndClasses(elementDef, violations);
            this.verifyCannotImportSelf(elementDef, violations);
            this.verifyCannotImportNestedComponents(elementDef, violations);
            this.verifyNestedPackagesCannotImport(elementDef, violations);
        }
        if(elementDef.isConstraintType()) {
            SysLog.trace("checking all CONSTRAINT constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyCannotConstrainThisElement(elementDef, violations);
            this.verifyConstraintsLimitedToContainer(elementDef, violations);
        }
        if(elementDef.isConstantType()) {
            SysLog.trace("checking all CONSTANT constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyConstantsTypeMustBePrimitive(elementDef, violations);
        }
        if(elementDef.isStructureFieldType()) {
            SysLog.trace("checking all STRUCTURE_FIELD constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyStructureFieldContainmentRules(elementDef, violations);
        }
        if(elementDef.isStructureType()) {
            SysLog.trace("checking all STRUCTURE_TYPE constraints for element " + elementDef.objGetValue("qualifiedName"));
            this.verifyMustHaveFields(elementDef, violations);
        }
        if(
            elementDef.isStructuralFeatureType() ||
            elementDef.isAssociationEndType() ||
            elementDef.isParameterType() ||
            elementDef.isStructureFieldType() ||
            elementDef.isCollectionType()
        ) {
            SysLog.trace("checking all STRUCTURAL_FEATURE/ASSOCIATION_END/PARAMETER/STRUCTURE_FIELD/COLLECTION_TYPE constraints for element " + elementDef.objGetValue("qualifiedName"));
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
    ) throws ServiceException {
        if (
                ((Boolean)attributeDef.objGetValue("isDerived")).booleanValue() &&
                ((Boolean)attributeDef.objGetValue("isChangeable")).booleanValue()
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_BE_DERIVED_AND_CHANGEABLE,
                    attributeDef.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        if (associationEndDef.objGetList("qualifierName").size() > 1) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_HAVE_MORE_THAN_ONE_QUALIFIER,
                    associationEndDef.objGetValue("qualifiedName")
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
        if(associationEndDef.objGetList("qualifierType").size() > 0) {
            ModelElement_1_0 qualifierType = this.model.getDereferencedType(associationEndDef.objGetValue("qualifierType"));      
            if(
                !qualifierType.isPrimitiveType() &&
                !ModelHelper.UNBOUND.equals(associationEndDef.objGetValue("multiplicity"))
            ) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.NON_PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_TO_N,
                        associationEndDef.objGetValue("qualifiedName")
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

        if(associationEndDef.objGetList("qualifierType").size() > 0) {
            ModelElement_1_0 qualifierType = this.model.getDereferencedType(associationEndDef.objGetValue("qualifierType"));      
            if(
                qualifierType.isPrimitiveType() &&
                !((String)associationEndDef.objGetValue("multiplicity")).endsWith("..1")
            ) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_OR_1_TO_1,
                        associationEndDef.objGetValue("qualifiedName")
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

        if(!operationDef.objGetList("parameter").isEmpty()) {
            for(
                Iterator it = operationDef.objGetList("parameter").iterator();
                it.hasNext();
            ) {        
                ModelElement_1_0 parameterDef = this.model.getElement(it.next());
                // check argument to be PARAMETER
                if(!ModelAttributes.PARAMETER.equals(parameterDef.objGetClass())) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.OPERATION_ARGUMENTS_MUST_BE_PARAMETER,
                            operationDef.objGetValue("qualifiedName"), 
                            parameterDef.objGetValue("name")
                        )
                    );
                    return;
                }

                // check type of parameter to be STRUCTURE
                if(!this.model.isStructureType(this.model.getElementType(parameterDef))) {      
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.PARAMETER_TYPE_MUST_BE_STRUCTURE_TYPE,
                            operationDef.objGetValue("qualifiedName"), 
                            parameterDef.objGetValue("name")
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
        if(!operationDef.objGetList("exception").isEmpty()) {
            for(
                Iterator it = operationDef.objGetList("exception").iterator();
                it.hasNext();
            ) {        
                Path exceptionPath = (Path)it.next();
                String qualifiedExceptionName = exceptionPath.getBase();
                try {
                    ModelElement_1_0 exceptionDef = this.model.getElement(qualifiedExceptionName);
                    // check argument to be PARAMETER
                    if(!ModelAttributes.EXCEPTION.equals(exceptionDef.objGetClass())) {
                        violations.add(
                            new BasicException.Parameter(
                                ModelConstraints.OPERATION_EXCEPTION_MUST_BE_EXCEPTION,
                                operationDef.objGetValue("qualifiedName"), 
                                exceptionDef.objGetValue("name")
                            )
                        );
                        return;
                    }
                }
                catch(ServiceException ex) {
                    if (ex.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                        throw ex;
                    }
                    else {
                        throw new ServiceException(
                            ex,
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.EXCEPTION_TYPE_NOT_FOUND_IN_REPOSITORY,
                            "Exception " + qualifiedExceptionName + " not found thrown by operation " + operationDef.jdoGetObjectId(),
                            new BasicException.Parameter("operation", operationDef),
                            new BasicException.Parameter("exception", ">" + exceptionPath + "<")
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
    ) throws ServiceException {
        if(!elementDef.isPackageType()) {
            if (elementDef.objGetList("container").size() != 1) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.MUST_BE_CONTAINED_UNLESS_PACKAGE,
                        elementDef.objGetValue("qualifiedName")
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

        if (!modelNamespace.objGetList("content").isEmpty()) {
            Set contentNames = new HashSet();
            for(
                Iterator it = modelNamespace.objGetList("content").iterator();
                it.hasNext();
            ) {        
                ModelElement_1_0 elementDef = this.model.getElement(it.next());
                if (contentNames.contains(elementDef.objGetValue("name"))) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.CONTENT_NAMES_MUST_NOT_COLLIDE,
                            modelNamespace.objGetValue("qualifiedName"),
                            elementDef.objGetValue("name")
                        )
                    );
                } else {
                    contentNames.add(elementDef.objGetValue("name"));
                }
            }
        }
    }

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
            Iterator it = modelGeneralizableElement.objGetList("supertype").iterator();
            it.hasNext();
        ) {        
            ModelElement_1_0 elementDef = this.model.getElement(it.next());
            if (!modelGeneralizableElement.objGetClass().equals(elementDef.objGetClass())) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.SUPERTYPE_KIND_MUST_BE_SAME,
                        modelGeneralizableElement.objGetValue("qualifiedName"),
                        elementDef.objGetValue("qualifiedName")
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

        if(!modelGeneralizableElement.objGetList("feature").isEmpty()) {

            // instead of collecting all contents of all direct and indirect 
            // supertypes, the BasicException.Parameter 'feature' is used; this works because 
            // 'feature' contains all the contents of all direct and indirect
            // supertypes and the type itself
            Set featureNames = new HashSet();
            for(
                Iterator it = modelGeneralizableElement.objGetList("feature").iterator();
                it.hasNext();
            ) {        
                ModelElement_1_0 elementDef = this.model.getElement(it.next());
                if (featureNames.contains(elementDef.objGetValue("name"))) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.CONTENTS_MUST_NOT_COLLIDE_WITH_SUPERTYPES,
                            modelGeneralizableElement.objGetValue("qualifiedName"),
                            elementDef.objGetValue("name")
                        )
                    );
                } else {
                    featureNames.add(elementDef.objGetValue("name"));
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
        if(generalizableElementDef.objGetList("allSupertype") != null) {     
            Map allFeatures = new HashMap();
            for(
                Iterator i = generalizableElementDef.objGetList("allSupertype").iterator();
                i.hasNext();
            ) {
                ModelElement_1_0 supertype = this.model.getElement(i.next());
                for(
                    Iterator j = supertype.objGetList("feature").iterator();
                    j.hasNext();
                ) {
                    ModelElement_1_0 feature = this.model.getElement(j.next());
                    ModelElement_1_0 inspected = (ModelElement_1_0)allFeatures.get(feature.objGetValue("name"));
                    if(
                        (inspected != null) && 
                        !(inspected.objGetValue("qualifiedName").equals(feature.objGetValue("qualifiedName")))
                    ) {
                        violations.add(
                            new BasicException.Parameter(
                                ModelConstraints.DIAMOND_RULE_MUST_BE_OBEYED,
                                generalizableElementDef.objGetValue("qualifiedName"),
                                feature.objGetValue("name")
                            )
                        );
                    }
                    else {
                        allFeatures.put(
                            feature.objGetValue("name"),
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
        if(elementDef.isAssociationType()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_CANNOT_BE_TYPES,
                    typedElementDef.objGetValue("qualifiedName")
                )
            );
        }    
    }

    //---------------------------------------------------------------------------  
    private void verifyChangeabilityForNonPrimitiveQualifier(
        ModelElement_1_0 associationEndDef,
        List violations
    ) throws ServiceException {
        if(associationEndDef.objGetList("qualifierType").size() > 0) {
            ModelElement_1_0 qualifierType = this.model.getDereferencedType(associationEndDef.objGetValue("qualifierType"));      
            if(
                !qualifierType.isPrimitiveType() &&
                ((Boolean)associationEndDef.objGetValue("isChangeable")).booleanValue()
            ) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.ASSOCIATION_END_WITH_COMPLEX_QUALIFIER_MUST_BE_FROZEN,
                        associationEndDef.objGetValue("qualifiedName")
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
            CLASS_CONTAINMENT,
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
    ) throws ServiceException {
        if (
            ((Boolean) classDef.objGetValue("isAbstract")).booleanValue() &&
            ((Boolean) classDef.objGetValue("isSingleton")).booleanValue()
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ABSTRACT_CLASSES_CANNOT_BE_SINGLETON,
                    classDef.objGetValue("qualifiedName")
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
            Iterator it = dataTypeDef.objGetList("content").iterator();
            it.hasNext();
        ) {        
            ModelElement_1_0 elementDef = this.model.getElement(it.next());
            if(
                (!elementDef.objGetClass().equals(ModelAttributes.ALIAS_TYPE)) &&
                (!elementDef.objGetClass().equals(ModelAttributes.CONSTRAINT)) &&
                (!elementDef.objGetClass().equals(ModelAttributes.TAG))
            ) {

                if (
                    !dataTypeDef.objGetClass().equals(ModelAttributes.STRUCTURE_TYPE) ||
                    !elementDef.objGetClass().equals(ModelAttributes.STRUCTURE_FIELD)
                ) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.DATA_TYPE_CONTAINMENT_RULES,
                            dataTypeDef.objGetValue("qualifiedName"), 
                            elementDef.objGetValue("name")
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
    ) throws ServiceException {
        if (!modelDataType.objGetList("supertype").isEmpty()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.DATA_TYPES_HAVE_NO_SUPERTYPES,
                    modelDataType.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        if (((Boolean)dataTypeDef.objGetValue("isAbstract")).booleanValue()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.DATA_TYPES_CANNOT_BE_ABSTRACT,
                    dataTypeDef.objGetValue("qualifiedName")
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
            referenceDef.objGetValue("referencedEnd")
        );
        if (
            !referenceDef.objGetValue("multiplicity").equals(
                referencedEnd.objGetValue("multiplicity")
            )
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCE_MULTIPLICITY_MUST_MATCH_END,
                    referenceDef.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        if (!referenceDef.objGetValue("scope").equals(ScopeKind.INSTANCE_LEVEL)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCE_MUST_BE_INSTANCE_SCOPED,
                    referenceDef.objGetValue("qualifiedName")
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
            referenceDef.objGetValue("referencedEnd")
        );
        if (
                !((Boolean)referenceDef.objGetValue("isChangeable")).booleanValue() ==
                    ((Boolean)referencedEnd.objGetValue("isChangeable")).booleanValue()
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CHANGEABLE_REFERENCE_MUST_HAVE_CHANGEABLE_END,
                    referenceDef.objGetValue("qualifiedName")
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
            referenceDef.objGetValue("referencedEnd")
        );
        if (
                !referenceDef.objGetValue("type").equals(
                    referencedEnd.objGetValue("type")
                )
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCE_TYPE_MUST_MATCH_END_TYPE,
                    referenceDef.objGetValue("qualifiedName")
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
            referenceDef.objGetValue("referencedEnd")
        );
        if (!((Boolean)referencedEnd.objGetValue("isNavigable")).booleanValue()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCED_END_MUST_BE_NAVIGABLE,
                    referenceDef.objGetValue("qualifiedName")
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
                    associationDef.objGetValue("qualifiedName")
                )
            );
            return;
        }
        ModelElement_1_0 end1 = (ModelElement_1_0)ends.get(0);
        ModelElement_1_0 end2 = (ModelElement_1_0)ends.get(1);

        if(
            !AggregationKind.NONE.equals(end1.objGetValue("aggregation")) &&
            !AggregationKind.NONE.equals(end2.objGetValue("aggregation"))
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ONE_ASSOCIATION_END_MUST_HAVE_AGGREGATION_NONE,
                    associationDef.objGetValue("qualifiedName")
                )
            );
        }

        // !NONE --> primitive qualifier and multiplicity 0..1|1..1
        if(
            !AggregationKind.NONE.equals(end1.objGetValue("aggregation")) && (
                end1.objGetList("qualifierType").size() < 1 || 
                !this.model.isPrimitiveType(end1.objGetValue("qualifierType")) || (
                    !Multiplicity.OPTIONAL.toString().equals(end1.objGetValue("multiplicity")) &&
                    !Multiplicity.SINGLE_VALUE.toString().equals(end1.objGetValue("multiplicity"))
                )
            )
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY,
                    end1.objGetValue("qualifiedName")
                )
            );
        }

        // !NONE --> primitive qualifier and multiplicity 0..1|1..1
        if(
                !AggregationKind.NONE.equals(end2.objGetValue("aggregation")) &&
                ((end2.objGetList("qualifierType").size() < 1) || 
                        !this.model.isPrimitiveType(end2.objGetValue("qualifierType")) ||
                        (!Multiplicity.OPTIONAL.toString().equals(end2.objGetValue("multiplicity"))) && !Multiplicity.SINGLE_VALUE.toString().equals(end2.objGetValue("multiplicity")))
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY,
                    end2.objGetValue("qualifiedName")
                )
            );
        }

        // qualifier -> isNavigable
        if(
                (end1.objGetList("qualifierType").size() >= 1) &&
                !((Boolean)end1.objGetValue("isNavigable")).booleanValue()
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.QUALIFIER_REQUIRES_NAVIGABILITY,
                    end1.objGetValue("qualifiedName")
                )
            );
        }

        // qualifier -> isNavigable
        if(
                (end2.objGetList("qualifierType").size() >= 1) &&
                !((Boolean)end2.objGetValue("isNavigable")).booleanValue()
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.QUALIFIER_REQUIRES_NAVIGABILITY,
                    end2.objGetValue("qualifiedName")
                )
            );
        }

        // NONE -->    no qualifier & any multiplicity
        //          || primitive qualifier & multiplicity 0..1 ||
        //          || class qualifier & multiplicity 0..n    
        if(
                AggregationKind.NONE.equals(end1.objGetValue("aggregation")) &&
                (end1.objGetList("qualifierType").size() >= 1) &&
                (!this.model.isPrimitiveType(end1.objGetValue("qualifierType")) || !Multiplicity.OPTIONAL.toString().equals(end1.objGetValue("multiplicity"))) &&
                (!this.model.isClassType(end1.objGetValue("qualifierType")) || !ModelHelper.UNBOUND.equals(end1.objGetValue("multiplicity")))      
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NONE_REQUIRES_NO_OR_UNIQUE_PRIMITIVE_OR_NON_UNIQUE_CLASS_QUALIFIER,
                    end1.objGetValue("qualifiedName")
                )
            );
        }

        // NONE -->    no qualifier & any multiplicity
        //          || primitive qualifier & multiplicity 0..1
        //          || class qualifier & multiplicity 0..n    
        if(
            AggregationKind.NONE.equals(end2.objGetValue("aggregation")) &&
            end2.objGetList("qualifierType").size() >= 1 && (
                !this.model.isPrimitiveType(end2.objGetValue("qualifierType")) || 
                !Multiplicity.OPTIONAL.toString().equals(end2.objGetValue("multiplicity"))
            ) && (
                !this.model.isClassType(end2.objGetValue("qualifierType")) || 
                !ModelHelper.UNBOUND.equals(end2.objGetValue("multiplicity"))
            )      
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NONE_REQUIRES_NO_OR_UNIQUE_PRIMITIVE_OR_NON_UNIQUE_CLASS_QUALIFIER,
                    end1.objGetValue("qualifiedName")
                )
            );
        }

        // end1:class qualifier --> end2:no or primitive qualifier
        if(
            end1.objGetList("qualifierType").size() >= 1 &&
            this.model.isClassType(end1.objGetValue("qualifierType")) &&
            end2.objGetList("qualifierType").size() >= 1 && 
            !this.model.isPrimitiveType(end2.objGetValue("qualifierType"))
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER,
                    end1.objGetValue("qualifiedName"),
                    end2.objGetValue("qualifiedName")
                )
            );
        }

        // end2:class qualifier --> end1:no or numeric qualifier
        if(
            end2.objGetList("qualifierType").size() >= 1 &&
            this.model.isClassType(end2.objGetValue("qualifierType")) &&
            end1.objGetList("qualifierType").size() >= 1 && 
            !this.model.isPrimitiveType(end1.objGetValue("qualifierType"))
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER,
                    end1.objGetValue("qualifiedName"),
                    end2.objGetValue("qualifiedName")
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
            referenceDef.objGetValue("container")
        );
        ModelElement_1_0 exposedEnd = this.model.getElement(
            referenceDef.objGetValue("exposedEnd")
        );

        // precondition: container->allSupertype includes container itself
        if (!container.objGetList("allSupertype").contains(
            exposedEnd.objGetValue("type")
        )
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CONTAINER_MUST_MATCH_EXPOSED_TYPE,
                    referenceDef.objGetValue("qualifiedName")
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

        if (!operationDef.objGetList("parameter").isEmpty()) {
            boolean foundReturnParam = false;
            for(
                    Iterator it = operationDef.objGetList("parameter").iterator();
                    it.hasNext();
            ) {        
                ModelElement_1_0 modelParameter = this.model.getElement(it.next());
                if(modelParameter.objGetValue("direction").equals(DirectionKind.RETURN_DIR)) {
                    if (!foundReturnParam) { foundReturnParam = true; }
                    else {
                        violations.add(
                            new BasicException.Parameter(
                                ModelConstraints.OPERATIONS_HAVE_AT_MOST_ONE_RETURN,
                                operationDef.objGetValue("qualifiedName"), 
                                modelParameter.objGetValue("name")
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
                Iterator it = exceptionDef.objGetList("parameter").iterator();
                it.hasNext();
        ) {        
            ModelElement_1_0 modelParameter = this.model.getElement(it.next());
            if(!modelParameter.objGetValue("direction").equals(DirectionKind.OUT_DIR)) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.EXCEPTIONS_HAVE_ONLY_OUT_PARAMETERS,
                        exceptionDef.objGetValue("qualifiedName"), 
                        modelParameter.objGetValue("name")
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
    ) throws ServiceException {
        if (!associationDef.objGetList("supertype").isEmpty()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_HAVE_NO_SUPERTYPES,
                    associationDef.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        if (((Boolean) associationDef.objGetValue("isAbstract")).booleanValue()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_CANNOT_BE_ABSTRACT,
                    associationDef.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        if (!associationDef.objGetValue("visibility").equals(VisibilityKind.PUBLIC_VIS)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_MUST_BE_PUBLIC,
                    associationDef.objGetValue("qualifiedName")
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
                    associationDef.objGetValue("qualifiedName")
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
        if (!type.isClassType()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.END_TYPE_MUST_BE_CLASS,
                    associationEndDef.objGetValue("qualifiedName")
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

        if (!associationEndDef.objGetValue("aggregation").equals(AggregationKind.NONE)) {
            // get association this association end belongs to
            ModelElement_1_0 associationDef = this.model.getElement(
                associationEndDef.objGetValue("container")
            );
            for (
                Iterator it = getAssociationEnds(associationDef).iterator();
                it.hasNext();
            ) {
                ModelElement_1_0 assEnd = (ModelElement_1_0) it.next();
                if (!associationEndDef.equals(assEnd)) {
                    if (assEnd.objGetValue("aggregation").equals(AggregationKind.NONE)) {
                        return;
                    }
                }
            }
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_HAVE_TWO_AGGREGATE_ENDS,
                    associationDef.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        if (((Boolean)packageDef.objGetValue("isAbstract")).booleanValue()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.DATA_TYPES_CANNOT_BE_ABSTRACT,
                    packageDef.objGetValue("qualifiedName")
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
            Iterator it = importDef.objGetList("importedNamespace").iterator();
            it.hasNext();
        ) {
            ModelElement_1_0 elementDef = this.model.getElement(
                it.next()
            );
            if (
                !elementDef.isClassType() &&
                !elementDef.isPackageType()
            ) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.CAN_ONLY_IMPORT_PACKAGES_AND_CLASSES,
                        importDef.objGetValue("qualifiedName")
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
    ) throws ServiceException {
        for (
            Iterator it = importDef.objGetList("importedNamespace").iterator();
            it.hasNext();
        ) {
            if (importDef.objGetValue("container").equals(it.next())) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.CANNOT_IMPORT_SELF,
                        importDef.objGetValue("qualifiedName")
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
                importDef.objGetValue("container")
            )
        );

        for (
            Iterator it = importDef.objGetList("importedNamespace").iterator();
            it.hasNext();
        ) {
            Path path = (Path) it.next();
            if (allContainerContents.contains(path)) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.CANNOT_IMPORT_NESTED_COMPONENTS,
                        importDef.objGetValue("qualifiedName"),
                        (this.model.getElement(path)).objGetValue("qualifiedName")
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
        if (!importDef.objGetList("container").isEmpty()) {
            ModelElement_1_0 firstContainerElement = this.model.getElement(
                importDef.objGetValue("container")
            );
            if (!firstContainerElement.objGetList("container").isEmpty()) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.NESTED_PACKAGES_CANNOT_IMPORT,
                        importDef.objGetValue("qualifiedName")
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
        if(!type.isPrimitiveType()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CONSTANTS_TYPE_MUST_BE_PRIMITIVE,
                    constantDef.objGetValue("qualifiedName")
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
        //
        // Validate the multiplicity
        //
        Multiplicity multiplicity = ModelHelper.toMultiplicity((String) elementDef.objGetValue("multiplicity"));
        if(multiplicity == null) {
	        violations.add(
	            new BasicException.Parameter(
	                ModelConstraints.INVALID_MULTIPLICITY,
	                elementDef.objGetValue("qualifiedName")
	            )
	        );
        } else {
        	//
        	// get type of model element to verify (if any)
        	//
            ModelElement_1_0 type = this.getType(elementDef);
            // 
        	// <<stream>> implies primitive types
            //
            if(Multiplicity.STREAM == multiplicity && !type.isPrimitiveType()) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.STEREOTYPE_STREAM_IMPLIES_PRIMITIVE_TYPE,
                        elementDef.objGetValue("qualifiedName") + "; type=" + type
                    )
                );
            }     
        }
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
            Iterator it = modelStructureType.objGetList("content").iterator();
            it.hasNext();
        ) {        
            if(this.model.getElement(it.next()).isStructureFieldType()) {
                return;
            }
        }
        return;
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
            Iterator it = elementDef.objGetList("content").iterator();
            it.hasNext();
        ) {        
            ModelElement_1_0 contentElement = this.model.getElement(it.next());

            // build intersection of allowedContainments in elementDef and all
            // the types elementDef is an instance of
            // if the intersection is empty, then elementDef is not an allowed
            // containment instance
            Set intersection = new HashSet(Arrays.asList(allowedContainments));
            intersection.retainAll(
                contentElement.objGetList(SystemAttributes.OBJECT_INSTANCE_OF)
            );
            if(intersection.isEmpty()) {
                violations.add(
                    new BasicException.Parameter(
                        violatedConstraint,
                        elementDef.objGetValue("qualifiedName"), 
                        contentElement.objGetValue("name")
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
            Iterator it = associationDef.objGetList("content").iterator();
            it.hasNext();
        ) {        
            ModelElement_1_0 elementDef = this.model.getElement(it.next());
            if(elementDef.isAssociationEndType()) {
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
        Set allContents = new HashSet(namespaceDef.objGetList("content"));
        for (
            Iterator it = namespaceDef.objGetList("content").iterator();
            it.hasNext();
        ) {
            ModelElement_1_0 elementDef = this.model.getElement(it.next());
            if (elementDef.isNamespaceType()) {
                allContents.addAll(getAllContents(elementDef));
            }
        }
        return allContents; 
    }

    //---------------------------------------------------------------------------  
    // Constants and Variables
    //---------------------------------------------------------------------------  

    private Model_1_0 model = null;

    private final String[] CLASS_CONTAINMENT = {
        ModelAttributes.CLASS, ModelAttributes.DATATYPE, 
        ModelAttributes.ATTRIBUTE, ModelAttributes.REFERENCE,
        ModelAttributes.OPERATION, ModelAttributes.EXCEPTION,
        ModelAttributes.CONSTANT, ModelAttributes.CONSTRAINT,
        ModelAttributes.TAG
    };

}
