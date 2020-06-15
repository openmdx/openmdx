/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: check MOF Model Constraints
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.omg.mof.cci.DirectionKind;
import org.omg.mof.cci.ScopeKind;
import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.mof.cci.ModelConstraints;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.repository.cci.AliasTypeRecord;
import org.openmdx.base.mof.repository.cci.AssociationEndRecord;
import org.openmdx.base.mof.repository.cci.AssociationRecord;
import org.openmdx.base.mof.repository.cci.AttributeRecord;
import org.openmdx.base.mof.repository.cci.ClassRecord;
import org.openmdx.base.mof.repository.cci.ClassifierRecord;
import org.openmdx.base.mof.repository.cci.CollectionTypeRecord;
import org.openmdx.base.mof.repository.cci.ConstantRecord;
import org.openmdx.base.mof.repository.cci.ConstraintRecord;
import org.openmdx.base.mof.repository.cci.DataTypeRecord;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.mof.repository.cci.ExceptionRecord;
import org.openmdx.base.mof.repository.cci.GeneralizableElementRecord;
import org.openmdx.base.mof.repository.cci.ImportRecord;
import org.openmdx.base.mof.repository.cci.NamespaceRecord;
import org.openmdx.base.mof.repository.cci.OperationRecord;
import org.openmdx.base.mof.repository.cci.PackageRecord;
import org.openmdx.base.mof.repository.cci.ParameterRecord;
import org.openmdx.base.mof.repository.cci.PrimitiveTypeRecord;
import org.openmdx.base.mof.repository.cci.ReferenceRecord;
import org.openmdx.base.mof.repository.cci.Repository;
import org.openmdx.base.mof.repository.cci.StructuralFeatureRecord;
import org.openmdx.base.mof.repository.cci.StructureFieldRecord;
import org.openmdx.base.mof.repository.cci.StructureTypeRecord;
import org.openmdx.base.mof.repository.cci.TagRecord;
import org.openmdx.base.mof.repository.cci.TypedElementRecord;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.VoidRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * This class allows you to verify model constraints for
 * <p>
 * <ul>
 * <li>a single model element, or</li>
 * <li>all the model elements contained in the package content</li>
 * </ul>
 * <p>
 * It is mostly used to check the MOF Model Constraints but other constraints
 * can be added easily.
 * <p>
 * For details on MOF Model Constraints please refer to the MOF Specification
 * which can be obtained from the OMG (http://www.omg.org)
 */
public class ModelConstraintsChecker_2 {

    /**
     * Constructor
     */
    public ModelConstraintsChecker_2(
        Repository repository
    ) {
        this.repository = repository;
    }

    private final Repository repository;

    private final static Collection<Class<? extends ElementRecord>> CLASS_CONTAINMENT = Arrays.asList(
        ClassRecord.class,
        DataTypeRecord.class,
        AttributeRecord.class,
        ReferenceRecord.class,
        OperationRecord.class,
        ExceptionRecord.class,
        OperationRecord.class,
        ExceptionRecord.class,
        ConstantRecord.class,
        TagRecord.class
    );
    
    //---------------------------------------------------------------------------
    /**
     * Verifies all the constraints for all the elements that are contained in the
     * current packageContent
     * 
     * @exception ServiceException
     *                If a constraint was violated
     */
    public void verify()
        throws ServiceException {

        final List<BasicException.Parameter> violations = new ArrayList<>();

        for (ElementRecord element : this.repository.getContent()) {
            verify(element, violations);
        }

        if (!violations.isEmpty()) {
            throw new ServiceException(
                ModelExceptions.MODEL_DOMAIN,
                ModelExceptions.CONSTRAINT_VIOLATION,
                "at least one model constraint is violated, for details refer to parameters",
                violations.toArray(new BasicException.Parameter[violations.size()])
            );
        }
    }

    //---------------------------------------------------------------------------  
    private void verify(
        ElementRecord elementDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        SysLog.trace("checking all ELEMENT constraints for element " + elementDef.getQualifiedName());
        this.verifyMustBeContainedUnlessPackage(elementDef, violations);

        if (elementDef instanceof AttributeRecord) {
            final AttributeRecord attributeDef = (AttributeRecord) elementDef;
            SysLog.trace("checking all ATTRIBUTE constraints for element " + attributeDef.getQualifiedName());
            this.verifyCannotBeDerivedAndChangeable(attributeDef, violations);
        }
        if (elementDef instanceof NamespaceRecord) {
            final NamespaceRecord namespaceDef = (NamespaceRecord) elementDef;
            SysLog.trace("checking all NAMESPACE constraints for element " + namespaceDef.getQualifiedName());
            this.verifyContentNamesMustNotCollide(namespaceDef, violations);
        }
        if (elementDef instanceof GeneralizableElementRecord) {
            GeneralizableElementRecord generalizableElementDef = (GeneralizableElementRecord) elementDef;
            SysLog.trace("checking all GENERALIZABLE_ELEMENT constraints for element " + generalizableElementDef.getQualifiedName());
            this.verifySupertypeKindMustBeSame(generalizableElementDef, violations);
            this.verifyContentsMustNotCollideWithSupertypes(generalizableElementDef, violations);
            this.verifyDiamondRuleMustBeObeyed(generalizableElementDef, violations);
        }
        if (elementDef instanceof TypedElementRecord && !(elementDef instanceof PrimitiveTypeRecord)) {
            TypedElementRecord typedElementDef = (TypedElementRecord) elementDef;
            SysLog.trace("checking all TYPED_ELEMENT constraints for element " + typedElementDef.getQualifiedName());
            this.verifyAssociationsCannotBeTypes(typedElementDef, violations);
        }
        if (elementDef instanceof ClassRecord) {
            ClassRecord classDef = (ClassRecord) elementDef;
            SysLog.trace("checking all CLASS constraints for element " + classDef.getQualifiedName());
            this.verifyAbstractClassesCannotBeSingleton(classDef, violations);
            this.verifyClassContainmentRules(classDef, violations);
        }
        if (elementDef instanceof DataTypeRecord) {
            DataTypeRecord dataTypeDef = (DataTypeRecord) elementDef;
            SysLog.trace("checking all DATATYPE constraints for element " + dataTypeDef.getQualifiedName());
            this.verifyDataTypeContainmentRules(dataTypeDef, violations);
            this.verifyDataTypesHaveNoSupertypes(dataTypeDef, violations);
            this.verifyDataTypesCannotBeAbstract(dataTypeDef, violations);
        }
        if (elementDef instanceof ReferenceRecord) {
            ReferenceRecord referenceDef = (ReferenceRecord) elementDef;
            SysLog.trace("checking all REFERENCE constraints for element " + referenceDef.getQualifiedName());
            this.verifyReferenceMultiplicityMustMatchEnd(referenceDef, violations);
            this.verifyReferenceMustBeInstanceScoped(referenceDef, violations);
            this.verifyChangeableReferenceMustHaveChangeableEnd(referenceDef, violations);
            this.verifyReferenceTypeMustMatchEndType(referenceDef, violations);
            this.verifyReferencedEndMustBeNavigable(referenceDef, violations);
            this.verifyContainerMustMatchExposedType(referenceDef, violations);
        }
        if (elementDef instanceof OperationRecord) {
            OperationRecord operationDef = (OperationRecord) elementDef;
            SysLog.trace("checking all OPERATION constraints for element " + operationDef.getQualifiedName());
            this.verifyOperationContainmentRules(operationDef, violations);
            this.verifyOperationsHaveAtMostOneReturn(operationDef, violations);
            this.verifyOperationParametersMustBeParameterClasses(operationDef, violations);
            this.verifyOperationExceptionsMustBeExceptionClasses(operationDef, violations);
        }
        if (elementDef instanceof ExceptionRecord) {
            ExceptionRecord exceptionDef = (ExceptionRecord) elementDef;
            SysLog.trace("checking all EXCEPTION constraints for element " + exceptionDef.getQualifiedName());
            this.verifyExceptionContainmentRules(exceptionDef, violations);
            this.verifyExceptionsHaveOnlyOutParameters(exceptionDef, violations);
        }
        if (elementDef instanceof AssociationRecord) {
            AssociationRecord associationDef = (AssociationRecord) elementDef;
            SysLog.trace("checking all ASSOCIATION constraints for element " + associationDef.getQualifiedName());
            this.verifyAssociationContainmentRules(associationDef, violations);
            this.verifyAssociationsHaveNoSupertypes(associationDef, violations);
            this.verifyAssociationsCannotBeAbstract(associationDef, violations);
            this.verifyAssociationsMustBePublic(associationDef, violations);
            this.verifyAssociationsMustBeBinary(associationDef, violations);
            this.verifyAssociationEnds(associationDef, violations);
        }
        if (elementDef instanceof AssociationEndRecord) {
            AssociationEndRecord associationEndDef = (AssociationEndRecord) elementDef;
            SysLog.trace("checking all ASSOCIATION_END constraints for element " + associationEndDef.getQualifiedName());
            this.verifyEndTypeMustBeClass(associationEndDef, violations);
            this.verifyCannotHaveTwoAggregateEnds(associationEndDef, violations);
            this.verifyCannotHaveMoreThanOneQualifier(associationEndDef, violations);
            this.verifyMultiplicityForNonPrimitiveQualifier(associationEndDef, violations);
            this.verifyMultiplicityForPrimitiveQualifier(associationEndDef, violations);
            this.verifyChangeabilityForNonPrimitiveQualifier(associationEndDef, violations);
            this.verifyMultiplicity(associationEndDef, associationEndDef.getMultiplicity(), violations);
        }
        if (elementDef instanceof PackageRecord) {
            PackageRecord packageDef = (PackageRecord) elementDef;
            SysLog.trace("checking all PACKAGE constraints for element " + packageDef.getQualifiedName());
            this.verifyPackageContainmentRules(packageDef, violations);
            this.verifyPackagesCannotBeAbstract(packageDef, violations);
        }
        if (elementDef instanceof ImportRecord) {
            ImportRecord importDef = (ImportRecord) elementDef;
            SysLog.trace("checking all IMPORT constraints for element " + importDef.getQualifiedName());
            this.verifyCanOnlyImportPackagesAndClasses(importDef, violations);
            this.verifyCannotImportSelf(importDef, violations);
            this.verifyCannotImportNestedComponents(importDef, violations);
            this.verifyNestedPackagesCannotImport(importDef, violations);
        }
        if (elementDef instanceof ConstraintRecord) {
            ConstraintRecord constraintDef = (ConstraintRecord) elementDef;
            SysLog.trace("checking all CONSTRAINT constraints for element " + constraintDef.getQualifiedName());
            this.verifyCannotConstrainThisElement(constraintDef, violations);
            this.verifyConstraintsLimitedToContainer(constraintDef, violations);
        }
        if (elementDef instanceof ConstantRecord) {
            ConstantRecord constantDef = (ConstantRecord) elementDef;
            SysLog.trace("checking all CONSTANT constraints for element " + constantDef.getQualifiedName());
            this.verifyConstantsTypeMustBePrimitive(constantDef, violations);
        }
        if (elementDef instanceof StructureFieldRecord) {
            StructureFieldRecord structureFieldDef = (StructureFieldRecord) elementDef;
            SysLog.trace("checking all STRUCTURE_FIELD constraints for element " + elementDef.getQualifiedName());
            this.verifyStructureFieldContainmentRules(structureFieldDef, violations);
            this.verifyMultiplicity(structureFieldDef, structureFieldDef.getMultiplicity(), violations);
        }
        if (elementDef instanceof StructureTypeRecord) {
            StructureTypeRecord structureTypeDef = (StructureTypeRecord) elementDef;
            SysLog.trace("checking all STRUCTURE_TYPE constraints for element " + elementDef.getQualifiedName());
            this.verifyMustHaveFields(structureTypeDef, violations);
        }
        if (elementDef instanceof StructuralFeatureRecord) {
            StructuralFeatureRecord structuralFeatureDef = (StructuralFeatureRecord) elementDef;
            SysLog.trace(
                "checking all STRUCTURAL_FEATURE constraints for element "
                    + structuralFeatureDef.getQualifiedName()
            );
            this.verifyMultiplicity(structuralFeatureDef, structuralFeatureDef.getMultiplicity(), violations);
        }
        if (elementDef instanceof ParameterRecord) {
            ParameterRecord parameterDef = (ParameterRecord) elementDef;
            SysLog.trace(
                "checking all PARAMETER constraints for element "
                    + elementDef.getQualifiedName()
            );
            this.verifyMultiplicity(parameterDef, parameterDef.getMultiplicity(), violations);
        }
        if (elementDef instanceof CollectionTypeRecord) {
            CollectionTypeRecord collectionTypeDef = (CollectionTypeRecord) elementDef;
            SysLog.trace(
                "checking all COLLECTION_TYPE constraints for element "
                    + elementDef.getQualifiedName()
            );
            this.verifyMultiplicity(collectionTypeDef, collectionTypeDef.getMultiplicity(), violations);
        }

    }

    //---------------------------------------------------------------------------  
    /**
     * An Attribute cannot be derived and changeable.
     * (openMDX Constraint)
     */
    private void verifyCannotBeDerivedAndChangeable(
        AttributeRecord attributeDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (attributeDef.isDerived() &&
            attributeDef.isChangeable()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_BE_DERIVED_AND_CHANGEABLE,
                    attributeDef.getQualifiedName()
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
        AssociationEndRecord associationEndDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (associationEndDef.getQualifierName().size() > 1) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_HAVE_MORE_THAN_ONE_QUALIFIER,
                    associationEndDef.getQualifiedName()
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
        AssociationEndRecord associationEndDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        for (Path qualifierTypeId : associationEndDef.getQualifierType()) {
            ElementRecord qualifierType = getDereferencedType(qualifierTypeId);
            if (!(qualifierType instanceof PrimitiveTypeRecord) &&
                !Multiplicity.UNBOUNDED.equals(associationEndDef.getMultiplicity())) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.NON_PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_TO_N,
                        associationEndDef.getQualifiedName()
                    )
                );
            }
        }
        // TODO validate cardinality, too
    }

    //---------------------------------------------------------------------------  
    /**
     * An AssociationEnd with a primitive type qualifier must have multiplicity
     * 0..1 or 1..1.
     * (openMDX Constraint)
     */
    private void verifyMultiplicityForPrimitiveQualifier(
        AssociationEndRecord associationEndDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        for (Path qualifierTypeId : associationEndDef.getQualifierType()) {
            final ElementRecord dereferencedType = this.getDereferencedType(qualifierTypeId);
            if (dereferencedType instanceof PrimitiveTypeRecord && !Multiplicity.parse(associationEndDef.getMultiplicity()).isSingleValued()) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.PRIMITIVE_QUALIFIER_MUST_HAVE_MULTIPLICITY_0_OR_1_TO_1,
                        associationEndDef.getQualifiedName()
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
        OperationRecord operationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        if (!operationDef.getParameter().isEmpty()) {
            for (Path parameterId : operationDef.getParameter()) {
                ElementRecord parameterDef = getModelElement(parameterId);
                // check argument to be PARAMETER
                if (!(parameterDef instanceof ParameterRecord)) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.OPERATION_ARGUMENTS_MUST_BE_PARAMETER,
                            operationDef.getQualifiedName(),
                            parameterDef.getName()
                        )
                    );
                    return;
                }

                // check type of parameter to be STRUCTURE
                final ElementRecord parameterType = getType((ParameterRecord) parameterDef);
                if (!(parameterType instanceof StructureTypeRecord)) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.PARAMETER_TYPE_MUST_BE_STRUCTURE_TYPE,
                            operationDef.getQualifiedName(),
                            parameterDef.getName()
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
        OperationRecord operationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!operationDef.getExceptions().isEmpty()) {
            for (Path exceptionPath : operationDef.getExceptions()) {
                try {
                    ElementRecord exceptionDef = getModelElement(exceptionPath);
                    // check argument to be PARAMETER
                    if (!(exceptionDef instanceof ExceptionRecord)) {
                        violations.add(
                            new BasicException.Parameter(
                                ModelConstraints.OPERATION_EXCEPTION_MUST_BE_EXCEPTION,
                                operationDef.getQualifiedName(),
                                exceptionDef.getName()
                            )
                        );
                        return;
                    }
                } catch (ServiceException ex) {
                    if (ex.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                        throw ex;
                    } else {
                        throw new ServiceException(
                            ex,
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.EXCEPTION_TYPE_NOT_FOUND_IN_REPOSITORY,
                            "Exception " + exceptionPath.getLastSegment().toClassicRepresentation() + " not found thrown by operation "
                                + operationDef.getQualifiedName(),
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
        ElementRecord elementDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!(elementDef instanceof PackageRecord)) {
            if (elementDef.getContainer() == null) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.MUST_BE_CONTAINED_UNLESS_PACKAGE,
                        elementDef.getQualifiedName()
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
        NamespaceRecord modelNamespace,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        final Set<Path> content = modelNamespace.getContent();
        if (!content.isEmpty()) {
            final Set<String> contentNames = new HashSet<>();
            for (Path contentId : content) {
                ElementRecord elementDef = getModelElement(contentId);
                if (!contentNames.add(elementDef.getName())) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.CONTENT_NAMES_MUST_NOT_COLLIDE,
                            modelNamespace.getQualifiedName(),
                            elementDef.getName()
                        )
                    );
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
        GeneralizableElementRecord modelGeneralizableElement,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        for (Path elementId : modelGeneralizableElement.getSupertypes()) {
            ElementRecord elementDef = getModelElement(elementId);
            if (!modelGeneralizableElement.getRecordName().equals(elementDef.getRecordName())) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.SUPERTYPE_KIND_MUST_BE_SAME,
                        modelGeneralizableElement.getQualifiedName(),
                        elementDef.getQualifiedName()
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
        GeneralizableElementRecord modelGeneralizableElement,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        if (!modelGeneralizableElement.getFeature().isEmpty()) {

            // instead of collecting all contents of all direct and indirect 
            // supertypes, the BasicException.Parameter 'feature' is used; this works because 
            // 'feature' contains all the contents of all direct and indirect
            // supertypes and the type itself
            Set<String> featureNames = new HashSet<>();
            for (Path elementId : modelGeneralizableElement.getFeature()) {
                ElementRecord elementDef = getModelElement(elementId);
                if (!featureNames.add(elementDef.getName())) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.CONTENTS_MUST_NOT_COLLIDE_WITH_SUPERTYPES,
                            modelGeneralizableElement.getQualifiedName(),
                            elementDef.getName()
                        )
                    );
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
        GeneralizableElementRecord generalizableElementDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (generalizableElementDef.getAllSupertypes().isEmpty()) {
            Map<String, ElementRecord> allFeatures = new HashMap<>();
            for (Path elementId : generalizableElementDef.getAllSupertypes()) {
                GeneralizableElementRecord supertype = getModelElement(elementId);
                for (Path featureId : supertype.getFeature()) {
                    ElementRecord feature = getModelElement(featureId);
                    ElementRecord inspected = allFeatures.get(feature.getName());
                    if ((inspected != null) &&
                        !(inspected.getQualifiedName().equals(feature.getQualifiedName()))) {
                        violations.add(
                            new BasicException.Parameter(
                                ModelConstraints.DIAMOND_RULE_MUST_BE_OBEYED,
                                generalizableElementDef.getQualifiedName(),
                                feature.getName()
                            )
                        );
                    } else {
                        allFeatures.put(
                            feature.getName(),
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
        TypedElementRecord typedElementDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        ElementRecord elementDef = this.getType(typedElementDef);
        if (elementDef instanceof AssociationRecord) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_CANNOT_BE_TYPES,
                    typedElementDef.getQualifiedName()
                )
            );
        }
    }

    //---------------------------------------------------------------------------  
    private void verifyChangeabilityForNonPrimitiveQualifier(
        AssociationEndRecord associationEndDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        for (Path elementId : associationEndDef.getQualifierType()) {
            final ElementRecord dereferencedType = getDereferencedType(elementId);
            if (!(dereferencedType instanceof PrimitiveTypeRecord) && associationEndDef.isChangeable()) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.ASSOCIATION_END_WITH_COMPLEX_QUALIFIER_MUST_BE_FROZEN,
                        associationEndDef.getQualifiedName()
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
        ClassRecord classDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

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
        ClassRecord classDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (classDef.isAbstract() && classDef.isSingleton()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ABSTRACT_CLASSES_CANNOT_BE_SINGLETON,
                    classDef.getQualifiedName()
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
        DataTypeRecord dataTypeDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        for (Path elementId : dataTypeDef.getContent()) {
            ElementRecord elementDef = getModelElement(elementId);
            if (!(elementDef instanceof AliasTypeRecord) &&
                !(elementDef instanceof ConstraintRecord) &&
                !(elementDef instanceof TagRecord)) {

                if (!(dataTypeDef instanceof StructureTypeRecord) ||
                    !(elementDef instanceof StructureFieldRecord)) {
                    violations.add(
                        new BasicException.Parameter(
                            ModelConstraints.DATA_TYPE_CONTAINMENT_RULES,
                            dataTypeDef.getQualifiedName(),
                            elementDef.getName()
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
        DataTypeRecord modelDataType,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!modelDataType.getSupertypes().isEmpty()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.DATA_TYPES_HAVE_NO_SUPERTYPES,
                    modelDataType.getQualifiedName()
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
        DataTypeRecord dataTypeDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (dataTypeDef.isAbstract()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.DATA_TYPES_CANNOT_BE_ABSTRACT,
                    dataTypeDef.getQualifiedName()
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
        ReferenceRecord referenceDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        AssociationEndRecord referencedEnd = getModelElement(referenceDef.getReferencedEnd());
        if (!referenceDef.getMultiplicity().equals(
            referencedEnd.getMultiplicity()
        )) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCE_MULTIPLICITY_MUST_MATCH_END,
                    referenceDef.getQualifiedName()
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
        ReferenceRecord referenceDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!referenceDef.getScope().equals(ScopeKind.INSTANCE_LEVEL)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCE_MUST_BE_INSTANCE_SCOPED,
                    referenceDef.getQualifiedName()
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
        ReferenceRecord referenceDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        AssociationEndRecord referencedEnd = getModelElement(referenceDef.getReferencedEnd());
        if (!referenceDef.isChangeable() == referencedEnd.isChangeable()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CHANGEABLE_REFERENCE_MUST_HAVE_CHANGEABLE_END,
                    referenceDef.getQualifiedName()
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
        ReferenceRecord referenceDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        AssociationEndRecord referencedEnd = getModelElement(referenceDef.getReferencedEnd());
        if (!referenceDef.getType().equals(
            referencedEnd.getType()
        )) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCE_TYPE_MUST_MATCH_END_TYPE,
                    referenceDef.getQualifiedName()
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
        ReferenceRecord referenceDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        final AssociationEndRecord referencedEnd = getModelElement(
            referenceDef.getReferencedEnd()
        );
        if (!referencedEnd.isNavigable()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.REFERENCED_END_MUST_BE_NAVIGABLE,
                    referenceDef.getQualifiedName()
                )
            );
        }
    }

    //---------------------------------------------------------------------------
    private void verifyAssociationEnds(
        AssociationRecord associationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        final List<AssociationEndRecord> ends = this.getAssociationEnds(associationDef);
        if (ends.size() != 2) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_CONTAINMENT_RULES,
                    associationDef.getQualifiedName()
                )
            );
            return;
        }
        AssociationEndRecord end1 = ends.get(0);
        AssociationEndRecord end2 = ends.get(1);

        if (!AggregationKind.NONE.equals(end1.getAggregation()) &&
            !AggregationKind.NONE.equals(end2.getAggregation())) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ONE_ASSOCIATION_END_MUST_HAVE_AGGREGATION_NONE,
                    associationDef.getQualifiedName()
                )
            );
        }

        // !NONE --> primitive qualifier and multiplicity 0..1|1..1
        if (!AggregationKind.NONE.equals(end1.getAggregation()) && (end1.getQualifierType().isEmpty() ||
            !arePrimitiveTypes(end1.getQualifierType()) || !Multiplicity.parse(end1.getMultiplicity()).isSingleValued())) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY,
                    end1.getQualifiedName()
                )
            );
        }

        // !NONE --> primitive qualifier and multiplicity 0..1|1..1
        if (!AggregationKind.NONE.equals(end2.getAggregation()) &&
            ((end2.getQualifierType().isEmpty()) ||
                !arePrimitiveTypes(end2.getQualifierType()) ||
                !Multiplicity.parse(end2.getMultiplicity()).isSingleValued())) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NOT_EQUAL_NONE_REQUIRES_PRIMITIVE_TYPE_QUALIFIER_AND_SINGLE_MULTIPLICITY,
                    end2.getQualifiedName()
                )
            );
        }

        // qualifier -> isNavigable
        if (!end1.getQualifierType().isEmpty() &&
            !end1.isNavigable()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.QUALIFIER_REQUIRES_NAVIGABILITY,
                    end1.getQualifiedName()
                )
            );
        }

        // qualifier -> isNavigable
        if (!end2.getQualifierType().isEmpty() &&
            !end2.isNavigable()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.QUALIFIER_REQUIRES_NAVIGABILITY,
                    end2.getQualifiedName()
                )
            );
        }

        // NONE -->    no qualifier & any multiplicity
        //          || primitive qualifier & multiplicity 0..1
        //          || class qualifier & multiplicity 0..n    
        if(
            AggregationKind.NONE.equals(end2.getAggregation()) &&
            !end2.getQualifierType().isEmpty() && (
                !arePrimitiveTypes(end2.getQualifierType()) || 
                !Multiplicity.OPTIONAL.code().equals(end2.getMultiplicity())
            ) && (
                !isClassType(end2.getQualifierType().get(0)) || 
                !Multiplicity.UNBOUNDED.equals(end2.getMultiplicity())
            )      
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.AGGREGATION_NONE_REQUIRES_NO_OR_UNIQUE_PRIMITIVE_OR_NON_UNIQUE_CLASS_QUALIFIER,
                    end1.getQualifiedName()
                )
            );
        }

        // end1:class qualifier --> end2:no or primitive qualifier
        if(
            !end1.getQualifierType().isEmpty() &&
            isClassType(end1.getQualifierType().get(0)) &&
            !end2.getQualifierType().isEmpty() && 
            !arePrimitiveTypes(end2.getQualifierType())
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER,
                    end1.getQualifiedName(),
                    end2.getQualifiedName()
                )
            );
        }

        // end2:class qualifier --> end1:no or primitive qualifier
        if(
            !end2.getQualifierType().isEmpty() &&
            isClassType(end2.getQualifierType().get(0)) &&
            !end1.getQualifierType().isEmpty() && 
            !arePrimitiveTypes(end1.getQualifierType())
        ) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.END1_CLASS_QUALIFIER_REQUIRES_END2_NONE_OR_PRIMITIVE_QUALIFIER,
                    end2.getQualifiedName(),
                    end1.getQualifiedName()
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
        ReferenceRecord referenceDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        GeneralizableElementRecord container = getModelElement(referenceDef.getContainer());
        AssociationEndRecord exposedEnd = getModelElement(referenceDef.getExposedEnd());

        // precondition: container->allSupertype includes container itself
        if (!container.getAllSupertypes().contains(exposedEnd.getType())) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CONTAINER_MUST_MATCH_EXPOSED_TYPE,
                    referenceDef.getQualifiedName()
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
        OperationRecord operationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        verifyContainmentRules(
            operationDef,
            Arrays.asList(
                ParameterRecord.class,
                ConstraintRecord.class,
                TagRecord.class
            ),
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
        OperationRecord operationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        if (!operationDef.getParameter().isEmpty()) {
            boolean foundReturnParam = false;
            for (Path parameterId : operationDef.getParameter()) {
                ParameterRecord modelParameter = getModelElement(parameterId);
                if (modelParameter.getDirection().equals(DirectionKind.RETURN_DIR)) {
                    if (!foundReturnParam) {
                        foundReturnParam = true;
                    } else {
                        violations.add(
                            new BasicException.Parameter(
                                ModelConstraints.OPERATIONS_HAVE_AT_MOST_ONE_RETURN,
                                operationDef.getQualifiedName(),
                                modelParameter.getName()
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
        ExceptionRecord exceptionDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        verifyContainmentRules(
            exceptionDef,
            Arrays.asList(
                ParameterRecord.class,
                TagRecord.class
            ),
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
        ExceptionRecord exceptionDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {

        for (Path parameterId : exceptionDef.getParameter()) {
            ParameterRecord modelParameter = getModelElement(parameterId);
            if (!modelParameter.getDirection().equals(DirectionKind.OUT_DIR)) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.EXCEPTIONS_HAVE_ONLY_OUT_PARAMETERS,
                        exceptionDef.getQualifiedName(),
                        modelParameter.getName()
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
        AssociationRecord associationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        verifyContainmentRules(
            associationDef,
            Arrays.asList(
                AssociationEndRecord.class,
                ConstraintRecord.class,
                TagRecord.class
            ),
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
        AssociationRecord associationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!associationDef.getSupertypes().isEmpty()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_HAVE_NO_SUPERTYPES,
                    associationDef.getQualifiedName()
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
        AssociationRecord associationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (associationDef.isAbstract()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_CANNOT_BE_ABSTRACT,
                    associationDef.getQualifiedName()
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
        AssociationRecord associationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!associationDef.getVisibility().equals(VisibilityKind.PUBLIC_VIS)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_MUST_BE_PUBLIC,
                    associationDef.getQualifiedName()
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
        AssociationRecord associationDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (getAssociationEnds(associationDef).size() != 2) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.ASSOCIATIONS_MUST_BE_BINARY,
                    associationDef.getQualifiedName()
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
        AssociationEndRecord associationEndDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        ElementRecord type = this.getType(associationEndDef);
        if (!(type instanceof ClassRecord)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.END_TYPE_MUST_BE_CLASS,
                    associationEndDef.getQualifiedName()
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
        AssociationEndRecord associationEndDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        // Note: the introduction of a derived BasicException.Parameter 'otherEnd' on an 
        //       AssociationEnd would simplify this operation

        if (!associationEndDef.getAggregation().equals(AggregationKind.NONE)) {
            // get association this association end belongs to
            AssociationRecord associationDef = getModelElement(associationEndDef.getContainer());
            for (AssociationEndRecord assEnd : getAssociationEnds(associationDef)) {
                if (!associationEndDef.equals(assEnd)) {
                    if (assEnd.getAggregation().equals(AggregationKind.NONE)) {
                        return;
                    }
                }
            }
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_HAVE_TWO_AGGREGATE_ENDS,
                    associationDef.getQualifiedName()
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
        PackageRecord packageDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        verifyContainmentRules(
            packageDef,
            Arrays.asList(
                PackageRecord.class,
                ClassRecord.class,
                DataTypeRecord.class,
                AssociationRecord.class,
                ExceptionRecord.class,
                ConstantRecord.class,
                ConstraintRecord.class,
                ImportRecord.class,
                TagRecord.class
            ),
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
        PackageRecord packageDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (packageDef.isAbstract()) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.DATA_TYPES_CANNOT_BE_ABSTRACT,
                    packageDef.getQualifiedName()
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
        ImportRecord importDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        final ElementRecord importedNamespace = getModelElement(importDef.getImportedNamespace());
        if (!(importedNamespace instanceof ClassRecord) && !(importedNamespace instanceof PackageRecord)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CAN_ONLY_IMPORT_PACKAGES_AND_CLASSES,
                    importDef.getQualifiedName()
                )
            );
        }
    }

    //---------------------------------------------------------------------------  
    /**
     * Packages cannot import or cluster themselves
     * (for details refer to MOF Spec. MOF Model Constraints [C-47])
     */
    private void verifyCannotImportSelf(
        ImportRecord importDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (importDef.getContainer().equals(importDef.getImportedNamespace())) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_IMPORT_SELF,
                    importDef.getQualifiedName()
                )
            );
        }
    }

    //---------------------------------------------------------------------------  
    /**
     * Packages cannot import or cluster Packages or Classes that they contain
     * (for details refer to MOF Spec. MOF Model Constraints [C-48])
     */
    private void verifyCannotImportNestedComponents(
        ImportRecord importDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        final Set<Path> allContainerContents = getAllContents(
            getModelElement(
                importDef.getContainer()
            )
        );
        if (allContainerContents.contains(importDef.getImportedNamespace())) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CANNOT_IMPORT_NESTED_COMPONENTS,
                    importDef.getQualifiedName(),
                    importDef.getImportedNamespace()
                )
            );
        }
    }

    //---------------------------------------------------------------------------  
    /**
     * Nested Packages cannot import or cluster other Packages or Classes
     * (for details refer to MOF Spec. MOF Model Constraints [C-49])
     */
    private void verifyNestedPackagesCannotImport(
        ImportRecord importDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        final Path firstContainerId = importDef.getContainer();
        if (firstContainerId != null) {
            final ElementRecord firstContainerDef = getModelElement(firstContainerId);
            if (firstContainerDef.getContainer() != null) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.NESTED_PACKAGES_CANNOT_IMPORT,
                        importDef.getQualifiedName()
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
        ConstraintRecord constraintDef,
        List<BasicException.Parameter> violations
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
        ConstraintRecord constraintDef,
        List<BasicException.Parameter> violations
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
        ConstantRecord constantDef,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        final ElementRecord type = getType(constantDef);
        if (!(type instanceof PrimitiveTypeRecord)) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.CONSTANTS_TYPE_MUST_BE_PRIMITIVE,
                    constantDef.getQualifiedName()
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
        TypedElementRecord elementDef,
        String rawMultiplicity,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        //
        // Validate the multiplicity
        //
        final Multiplicity multiplicity = ModelHelper.toMultiplicity(rawMultiplicity);
        if (multiplicity == null) {
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.INVALID_MULTIPLICITY,
                    elementDef.getQualifiedName() + " with invalid multiplicity " + rawMultiplicity
                )
            );
        } else {
            //
            // get type of model element to verify (if any)
            //
            ElementRecord type = this.getType(elementDef);
            // 
            // <<stream>> implies primitive types
            //
            if (Multiplicity.STREAM == multiplicity && !(type instanceof PrimitiveTypeRecord)) {
                violations.add(
                    new BasicException.Parameter(
                        ModelConstraints.STEREOTYPE_STREAM_IMPLIES_PRIMITIVE_TYPE,
                        elementDef.getQualifiedName() + "; type=" + type
                    )
                );
            }
        }
    }

    //---------------------------------------------------------------------------  
    /**
     * TODO how to support with the given meta model?
     * 
     * A StructureField contains Constraints and Tags
     * (for details refer to MOF Spec. MOF Model Constraints [C-58])
     */
    private void verifyStructureFieldContainmentRules(
        StructureFieldRecord modelStructureField,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
//        verifyContainmentRules(
//            modelStructureField,
//            Arrays.asList( 
//                ConstraintRecord.class, 
//                TagRecord.class
//            ),
//            ModelConstraints.STRUCTURE_FIELD_CONTAINMENT_RULES,
//            violations
//        );
    }

    //---------------------------------------------------------------------------  
    /**
     * A StructureType must contain at least one StructureField
     * (for details refer to MOF Spec. MOF Model Constraints [C-59])
     */
    private void verifyMustHaveFields(
        StructureTypeRecord modelStructureType,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        if (!VoidRecord.NAME.equals(modelStructureType.getQualifiedName())) {
            for (Path elementId : modelStructureType.getContent()) {
                final ElementRecord elementDef = getModelElement(elementId);
                if (elementDef instanceof StructureFieldRecord) {
                    return;
                }
            }
            violations.add(
                new BasicException.Parameter(
                    ModelConstraints.MUST_HAVE_FIELDS,
                    modelStructureType.getQualifiedName()
                )
            );
        }
    }

    //--------------------------------------------------------------------------- 
    /**
     * Verifies the containment rules for a given ModelElement
     */
    private void verifyContainmentRules(
        NamespaceRecord elementDef,
        Collection<Class<? extends ElementRecord>> allowedContainments,
        String violatedConstraint,
        List<BasicException.Parameter> violations
    )
        throws ServiceException {
        for (Path contentElementId : elementDef.getContent()) {
            final ElementRecord contentElement = getModelElement(contentElementId);
            if (!isInstanceOfAllowedClasses(allowedContainments, contentElement)) {
                violations.add(
                    new BasicException.Parameter(
                        violatedConstraint,
                        elementDef.getQualifiedName(),
                        contentElement.getName()
                    )
                );
            }
        }
    }

    private static boolean isInstanceOfAllowedClasses(
        Collection<Class<? extends ElementRecord>> allowedClasses,
        ElementRecord candidate
    ) {
        for (Class<? extends ElementRecord> allowedClass : allowedClasses) {
            if (allowedClass.isInstance(candidate)) {
                return true;
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------  
    /**
     * Computes all AssociationEnds for a given Association
     */
    private List<AssociationEndRecord> getAssociationEnds(
        AssociationRecord associationDef
    )
        throws ServiceException {
        List<AssociationEndRecord> associationEnds = new ArrayList<>();
        for (Path elementId : associationDef.getContent()) {
            ElementRecord elementDef = getModelElement(elementId);
            if (elementDef instanceof AssociationEndRecord) {
                associationEnds.add((AssociationEndRecord) elementDef);
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
    private Set<Path> getAllContents(
        NamespaceRecord namespaceDef
    )
        throws ServiceException {
        final Set<Path> namespaceContent = namespaceDef.getContent();
        final Set<Path> allContents = new HashSet<>(namespaceContent);
        for (Path elementId : namespaceContent) {
            final ElementRecord elementDef = getModelElement(elementId);
            if (elementDef instanceof NamespaceRecord) {
                allContents.addAll(getAllContents((NamespaceRecord) elementDef));
            }
        }
        return allContents;
    }

    //---------------------------------------------------------------------------  
    
    private ElementRecord getDereferencedType(
        Path type
    )
        throws ServiceException {
        return this.repository.getDereferencedType(type);
    }

    private ClassifierRecord getType(
        TypedElementRecord typedElementDef
    )
        throws ServiceException {
        return this.repository.getElementType(typedElementDef);
    }

    @SuppressWarnings("unchecked")
    private <T extends ElementRecord> T getModelElement(Path elementId)
        throws ServiceException {
        return (T) this.repository.getElement(elementId);
    }

    private boolean arePrimitiveTypes(Collection<Path> typeIds)
        throws ServiceException {
        for (Path typeId : typeIds) {
            if (!(this.repository.getDereferencedType(typeId) instanceof PrimitiveTypeRecord)) {
                return false;
            }
        }
        return true;
    }

    private boolean isClassType(Path typeId)
        throws ServiceException {
        return this.repository.getDereferencedType(typeId) instanceof ClassRecord;
    }
}
