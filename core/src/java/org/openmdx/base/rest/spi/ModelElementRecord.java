/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ModelElementRecord 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.rest.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmdx.base.naming.Path;

/**
 * ModelElementRecord
 *
 */
public class ModelElementRecord extends AbstractMappedRecord<org.openmdx.base.rest.spi.ModelElementRecord.Member>
    implements Serializable {

    public enum Member {
        isDerived,
        maxLength,
        container,
        scope,
        visibility,
        isAbstract,
        type,
        createdAt,
        createdBy,
        modifiedAt,
        modifiedBy,
        aggregation,
        isChangeable,
        isNavigable,
        multiplicity,
        isSingleton,
        supertype,
        exposedEnd,
        referencedEnd,
        qualifierName,
        qualifierType,
        isQuery,
        direction,
        stereotype,
        object_class,
        allSupertype,
        subtype,
        object_instanceof,
        qualifiedName,
        name,
        content,
        feature,
        attribute,
        reference,
        field,
        operation,
        allFeature,
        allFeatureWithSubtype,
        allSubtype,
        compositeReference,
        referencedEndIsNavigable,
        parameter,
        format,
        annotation,
        uniqueValues,
        isLanguageNeutral,
        exception,
        semantics,
        packageAsJar,
        identity
    }

    /**
     * Constructor 
     *
     * @param recordName
     */
    private ModelElementRecord(
        String recordName
    ) {
        this.recordName = recordName;
    }
    
    /**
     * Get new instance of ModelElementRecord.
     * 
     * @param recordName
     * @return
     */
    public static ModelElementRecord getInstance(
       String recordName
    ) {
        return new ModelElementRecord(recordName);
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
    public String getRecordName() {
        return this.recordName;
    }

    @Override
    public void setRecordName(String value) {
        this.recordName = value;
    }

    /**
     * Retrieve isDerived.
     *
     * @return Returns the isDerived.
     */
    public Boolean getIsDerived() {
        return this.isDerived;
    }

    /**
     * Set isDerived.
     * 
     * @param isDerived The isDerived to set.
     */
    public void setIsDerived(Boolean isDerived) {
        this.isDerived = isDerived;
    }

    /**
     * Retrieve maxLength.
     *
     * @return Returns the maxLength.
     */
    public Integer getMaxLength() {
        return this.maxLength;
    }
    
    /**
     * Set maxLength.
     * 
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
    
    /**
     * Retrieve container.
     *
     * @return Returns the container.
     */
    public Path getContainer() {
        return this.container;
    }

    /**
     * Set container.
     * 
     * @param container The container to set.
     */
    public void setContainer(Path container) {
        this.container = container;
    }
    
    /**
     * Retrieve scope.
     *
     * @return Returns the scope.
     */
    public String getScope() {
        return this.scope;
    }
    
    /**
     * Set scope.
     * 
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    /**
     * Retrieve visibility.
     *
     * @return Returns the visibility.
     */
    public String getVisibility() {
        return this.visibility;
    }
    
    /**
     * Set visibility.
     * 
     * @param visibility The visibility to set.
     */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
        
    /**
     * Retrieve isAbstract.
     *
     * @return Returns the isAbstract.
     */
    public Boolean getIsAbstract() {
        return this.isAbstract;
    }
    
    /**
     * Set isAbstract.
     * 
     * @param isAbstract The isAbstract to set.
     */
    public void setIsAbstract(Boolean isAbstract) {
        this.isAbstract = isAbstract;
    }
    
    /**
     * Retrieve type.
     *
     * @return Returns the type.
     */
    public Path getType() {
        return this.type;
    }
    
    /**
     * Set type.
     * 
     * @param type The type to set.
     */
    public void setType(Path type) {
        this.type = type;
    }

    /**
     * Retrieve createdAt.
     *
     * @return Returns the createdAt.
     */
    public Date getCreatedAt() {
        return CREATED_AT;
    }
    
    /**
     * Set createdAt.
     * 
     * @param createdAt The createdAt to set.
     */
    public void setCreatedAt(Date createdAt) {
        // do not store
    }
    
    /**
     * Retrieve createdBy.
     *
     * @return Returns the createdBy.
     */
    public List<String> getCreatedBy() {
        return this.<String>asList(this.createdBy);
    }

    /**
     * Set createdBy.
     * 
     * @param createdBy The createdBy to set.
     */
    public void setCreatedBy(List<String> createdBy) {
        if(createdBy == null || createdBy.isEmpty()) {
            this.createdBy = null;
        } else {
            this.createdBy = createdBy.get(0);
        }
    }
    
    /**
     * Retrieve modifiedAt.
     *
     * @return Returns the modifiedAt.
     */
    public Date getModifiedAt() {
        return MODIFIED_AT;
    }

    /**
     * Set modifiedAt.
     * 
     * @param modifiedAt The modifiedAt to set.
     */
    public void setModifiedAt(Date modifiedAt) {
        // do not store
    }

    /**
     * Retrieve modifiedBy.
     *
     * @return Returns the modifiedBy.
     */
    public List<String> getModifiedBy() {
        return this.<String>asList(this.modifiedBy);
    }
    
    /**
     * Set modifiedBy.
     * 
     * @param modifiedBy The modifiedBy to set.
     */
    public void setModifiedBy(List<String> modifiedBy) {
        if(modifiedBy == null || modifiedBy.isEmpty()) {
            this.modifiedBy = null;
        } else {
            this.modifiedBy = modifiedBy.get(0);
        }
    }
    
    /**
     * Retrieve aggregation.
     *
     * @return Returns the aggregation.
     */
    public String getAggregation() {
        return this.aggregation;
    }
    
    /**
     * Set aggregation.
     * 
     * @param aggregation The aggregation to set.
     */
    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }    
    
    /**
     * Retrieve isChangeable.
     *
     * @return Returns the isChangeable.
     */
    public Boolean getIsChangeable() {
        return this.isChangeable;
    }
    
    /**
     * Set isChangeable.
     * 
     * @param isChangeable The isChangeable to set.
     */
    public void setIsChangeable(Boolean isChangeable) {
        this.isChangeable = isChangeable;
    }
        
    /**
     * Retrieve isNavigable.
     *
     * @return Returns the isNavigable.
     */
    public Boolean getIsNavigable() {
        return this.isNavigable;
    }

    /**
     * Set isNavigable.
     * 
     * @param isNavigable The isNavigable to set.
     */
    public void setIsNavigable(Boolean isNavigable) {
        this.isNavigable = isNavigable;
    }
    
    /**
     * Retrieve multiplicity.
     *
     * @return Returns the multiplicity.
     */
    public String getMultiplicity() {
        return this.multiplicity;
    }

    /**
     * Set multiplicity.
     * 
     * @param multiplicity The multiplicity to set.
     */
    public void setMultiplicity(String multiplicity) {
        this.multiplicity = multiplicity;
    }
    
    /**
     * Retrieve isSingleton.
     *
     * @return Returns the isSingleton.
     */
    public Boolean getIsSingleton() {
        return this.isSingleton;
    }
    
    /**
     * Set isSingleton.
     * 
     * @param isSingleton The isSingleton to set.
     */
    public void setIsSingleton(Boolean isSingleton) {
        this.isSingleton = isSingleton;
    }

    /**
     * Retrieve supertype.
     *
     * @return Returns the supertype.
     */
    public List<Object> getSupertype() {
        return this.supertype;
    }
    
    /**
     * Set supertype.
     * 
     * @param supertype The supertype to set.
     */
    public void setSupertype(List<Object> supertype) {
        this.supertype = supertype;
    }

    /**
     * Retrieve exposedEnd.
     *
     * @return Returns the exposedEnd.
     */
    public Path getExposedEnd() {
        return this.exposedEnd;
    }
    
    /**
     * Set exposedEnd.
     * 
     * @param exposedEnd The exposedEnd to set.
     */
    public void setExposedEnd(Path exposedEnd) {
        this.exposedEnd = exposedEnd;
    }
    
    /**
     * Retrieve referencedEnd.
     *
     * @return Returns the referencedEnd.
     */
    public Path getReferencedEnd() {
        return this.referencedEnd;
    }
    
    /**
     * Set referencedEnd.
     * 
     * @param referencedEnd The referencedEnd to set.
     */
    public void setReferencedEnd(Path referencedEnd) {
        this.referencedEnd = referencedEnd;
    }
        
    /**
     * Retrieve qualifierName.
     *
     * @return Returns the qualifierName.
     */
    public String getQualifierName() {
        return this.qualifierName;
    }
    
    /**
     * Set qualifierName.
     * 
     * @param qualifierName The qualifierName to set.
     */
    public void setQualifierName(String qualifierName) {
        this.qualifierName = qualifierName;
    }

    /**
     * Retrieve qualifierType.
     *
     * @return Returns the qualifierType.
     */
    public Path getQualifierType() {
        return this.qualifierType;
    }
    
    /**
     * Set qualifierType.
     * 
     * @param qualifierType The qualifierType to set.
     */
    public void setQualifierType(Path qualifierType) {
        this.qualifierType = qualifierType;
    }

    /**
     * Retrieve isQuery.
     *
     * @return Returns the isQuery.
     */
    public Boolean getIsQuery() {
        return this.isQuery;
    }
    
    /**
     * Set isQuery.
     * 
     * @param isQuery The isQuery to set.
     */
    public void setIsQuery(Boolean isQuery) {
        this.isQuery = isQuery;
    }
        
    /**
     * Retrieve direction.
     *
     * @return Returns the direction.
     */
    public String getDirection() {
        return this.direction;
    }
    
    /**
     * Set direction.
     * 
     * @param direction The direction to set.
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }
        
    /**
     * Retrieve stereotype.
     *
     * @return Returns the stereotype.
     */
    public List<String> getStereotype() {
        return this.stereotype;
    }
    
    /**
     * Set stereotype.
     * 
     * @param stereotype The stereotype to set.
     */
    public void setStereotype(List<String> stereotype) {
        this.stereotype = stereotype;
    }
    
    /**
     * Retrieve object_class.
     *
     * @return Returns the object_class.
     */
    public String getObject_class() {
        return this.object_class;
    }
    
    /**
     * Set object_class.
     * 
     * @param object_class The object_class to set.
     */
    public void setObject_class(String object_class) {
        this.object_class = object_class;
    }
        
    /**
     * Retrieve allSupertype.
     *
     * @return Returns the allSupertype.
     */
    public List<Path> getAllSupertype() {
        return this.allSupertype;
    }
    
    /**
     * Set allSupertype.
     * 
     * @param allSupertype The allSupertype to set.
     */
    public void setAllSupertype(List<Path> allSupertype) {
        this.allSupertype = allSupertype;
    }
    
    /**
     * Retrieve suptype.
     *
     * @return Returns the suptype.
     */
    public List<Path> getSubtype() {
        return this.subtype;
    }
    
    /**
     * Set suptype.
     * 
     * @param suptype The suptype to set.
     */
    public void setSubtype(List<Path> suptype) {
        this.subtype = suptype;
    }
        
    /**
     * Retrieve object_instanceof.
     *
     * @return Returns the object_instanceof.
     */
    public List<String> getObject_instanceof() {
        return this.object_instanceof;
    }
    
    /**
     * Set object_instanceof.
     * 
     * @param object_instanceof The object_instanceof to set.
     */
    public void setObject_instanceof(List<String> object_instanceof) {
        this.object_instanceof = object_instanceof;
    }
    
    /**
     * Retrieve qualifiedName.
     *
     * @return Returns the qualifiedName.
     */
    public String getQualifiedName() {
        return this.qualifiedName;
    }
    
    /**
     * Set qualifiedName.
     * 
     * @param qualifiedName The qualifiedName to set.
     */
    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
    
    /**
     * Retrieve name.
     *
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Set name.
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Retrieve content.
     *
     * @return Returns the content.
     */
    public List<Path> getContent() {
        return this.content;
    }
    
    /**
     * Set content.
     * 
     * @param content The content to set.
     */
    public void setContent(List<Path> content) {
        this.content = content;
    }
    
    /**
     * Retrieve feature.
     *
     * @return Returns the feature.
     */
    public List<Path> getFeature() {
        return this.feature;
    }
    
    /**
     * Set feature.
     * 
     * @param feature The feature to set.
     */
    public void setFeature(List<Path> feature) {
        this.feature = feature;
    }
    
    /**
     * Retrieve attribute.
     *
     * @return Returns the attribute.
     */
    public Map<String,Object> getAttribute() {
        return this.attribute;
    }
    
    /**
     * Set attribute.
     * 
     * @param attribute The attribute to set.
     */
    public void setAttribute(Map<String,Object> attribute) {
        this.attribute = attribute;
    }
    
    /**
     * Retrieve reference.
     *
     * @return Returns the reference.
     */
    public Map<String, Object> getReference() {
        return this.reference;
    }
    
    /**
     * Set reference.
     * 
     * @param reference The reference to set.
     */
    public void setReference(Map<String, Object> reference) {
        this.reference = reference;
    }
        
    /**
     * Retrieve field.
     *
     * @return Returns the field.
     */
    public Map<String, Object> getField() {
        return this.field;
    }
    
    /**
     * Set field.
     * 
     * @param field The field to set.
     */
    public void setField(Map<String, Object> field) {
        this.field = field;
    }
    
    
    /**
     * Retrieve operation.
     *
     * @return Returns the operation.
     */
    public Map<String, Object> getOperation() {
        return this.operation;
    }
    
    /**
     * Set operation.
     * 
     * @param operation The operation to set.
     */
    public void setOperation(Map<String, Object> operation) {
        this.operation = operation;
    }
    
    /**
     * Retrieve allFeature.
     *
     * @return Returns the allFeature.
     */
    public Map<String, Object> getAllFeature() {
        return this.allFeature;
    }
    
    /**
     * Set allFeature.
     * 
     * @param allFeature The allFeature to set.
     */
    public void setAllFeature(Map<String, Object> allFeature) {
        this.allFeature = allFeature;
    }    
    
    /**
     * Retrieve allFeatureWithSubtype.
     *
     * @return Returns the allFeatureWithSubtype.
     */
    public Map<String, Object> getAllFeatureWithSubtype() {
        return this.allFeatureWithSubtype;
    }

    /**
     * Set allFeatureWithSubtype.
     * 
     * @param allFeatureWithSubtype The allFeatureWithSubtype to set.
     */
    public void setAllFeatureWithSubtype(
        Map<String, Object> allFeatureWithSubtype) {
        this.allFeatureWithSubtype = allFeatureWithSubtype;
    }
        
    /**
     * Retrieve allSubtype.
     *
     * @return Returns the allSubtype.
     */
    public List<Path> getAllSubtype() {
        return this.allSubtype;
    }
    
    /**
     * Set allSubtype.
     * 
     * @param allSubtype The allSubtype to set.
     */
    public void setAllSubtype(List<Path> allSubtype) {
        this.allSubtype = allSubtype;
    }
    
    /**
     * Retrieve compositeReference.
     *
     * @return Returns the compositeReference.
     */
    public Path getCompositeReference() {
        return this.compositeReference;
    }
    
    /**
     * Set compositeReference.
     * 
     * @param compositeReference The compositeReference to set.
     */
    public void setCompositeReference(Path compositeReference) {
        this.compositeReference = compositeReference;
    }
        
    /**
     * Retrieve referencedEndIsNavigable.
     *
     * @return Returns the referencedEndIsNavigable.
     */
    public Boolean getReferencedEndIsNavigable() {
        return this.referencedEndIsNavigable;
    }
    
    /**
     * Set referencedEndIsNavigable.
     * 
     * @param referencedEndIsNavigable The referencedEndIsNavigable to set.
     */
    public void setReferencedEndIsNavigable(Boolean referencedEndIsNavigable) {
        this.referencedEndIsNavigable = referencedEndIsNavigable;
    }
    
    /**
     * Retrieve parameter.
     *
     * @return Returns the parameter.
     */
    public List<Path> getParameter() {
        return this.parameter;
    }
    
    /**
     * Set parameter.
     * 
     * @param parameter The parameter to set.
     */
    public void setParameter(List<Path> parameter) {
        this.parameter = parameter;
    }
    
    /**
     * Retrieve format.
     *
     * @return Returns the format.
     */
    public List<String> getFormat() {
        return this.format;
    }
    
    /**
     * Set format.
     * 
     * @param format The format to set.
     */
    public void setFormat(List<String> format) {
        this.format = format;
    }    
    
    /**
     * Retrieve annotation.
     *
     * @return Returns the annotation.
     */
    public String getAnnotation() {
        return this.annotation;
    }

    /**
     * Set annotation.
     * 
     * @param annotation The annotation to set.
     */
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    
    /**
     * Retrieve uniqueValues.
     *
     * @return Returns the uniqueValues.
     */
    public Boolean getUniqueValues() {
        return this.uniqueValues;
    }

    
    /**
     * Set uniqueValues.
     * 
     * @param uniqueValues The uniqueValues to set.
     */
    public void setUniqueValues(Boolean uniqueValues) {
        this.uniqueValues = uniqueValues;
    }
    
    /**
     * Retrieve isLanguageNeutral.
     *
     * @return Returns the isLanguageNeutral.
     */
    public Boolean getIsLanguageNeutral() {
        return this.isLanguageNeutral;
    }
    
    /**
     * Set isLanguageNeutral.
     * 
     * @param isLanguageNeutral The isLanguageNeutral to set.
     */
    public void setIsLanguageNeutral(Boolean isLanguageNeutral) {
        this.isLanguageNeutral = isLanguageNeutral;
    }
    
    /**
     * Retrieve exception.
     *
     * @return Returns the exception.
     */
    public List<Path> getException() {
        return this.exception;
    }
    
    /**
     * Set exception.
     * 
     * @param exception The exception to set.
     */
    public void setException(List<Path> exception) {
        this.exception = exception;
    }
    
    /**
     * Retrieve semantics.
     *
     * @return Returns the semantics.
     */
    public String getSemantics() {
        return this.semantics;
    }
    
    /**
     * Set semantics.
     * 
     * @param semantics The semantics to set.
     */
    public void setSemantics(String semantics) {
        this.semantics = semantics;
    }
    
    /**
     * Retrieve packageAsJar.
     *
     * @return Returns the packageAsJar.
     */
    public byte[] getPackageAsJar() {
        return this.packageAsJar;
    }
    
    /**
     * Set packageAsJar.
     * 
     * @param packageAsJar The packageAsJar to set.
     */
    public void setPackageAsJar(byte[] packageAsJar) {
        this.packageAsJar = packageAsJar;
    }
                    
    /**
     * Retrieve identity.
     *
     * @return Returns the identity.
     */
    public String getIdentity() {
        return this.identity;
    }

    /**
     * Set identity.
     * 
     * @param identity The identity to set.
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
     */
    @Override
    protected org.openmdx.base.rest.spi.AbstractMappedRecord.Members<Member> members(
    ) {
        return MEMBERS;        
    }    

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#get(java.lang.Enum)
     */
    @Override
    protected Object get(
        Member index
    ){
        switch(index) {
            case isDerived: return getIsDerived();
            case maxLength: return getMaxLength();
            case container: return getContainer();
            case scope: return getScope();
            case visibility: return getVisibility();
            case isAbstract: return getIsAbstract();
            case type: return getType();
            case createdAt: return getCreatedAt();
            case createdBy: return getCreatedBy();
            case modifiedAt: return getModifiedAt();
            case modifiedBy: return getModifiedBy();
            case aggregation: return getAggregation();
            case isChangeable: return getIsChangeable();
            case isNavigable: return getIsNavigable();
            case multiplicity: return getMultiplicity();
            case isSingleton: return getIsSingleton();
            case supertype: return getSupertype();
            case exposedEnd: return getExposedEnd();
            case referencedEnd: return getReferencedEnd();
            case qualifierName: return getQualifierName();
            case qualifierType: return getQualifierType();
            case isQuery: return getIsQuery();
            case direction: return getDirection();
            case stereotype: return getStereotype();
            case object_class: return getObject_class();
            case allSupertype: return getAllSupertype();
            case subtype: return getSubtype();
            case object_instanceof: return getObject_instanceof();
            case qualifiedName: return getQualifiedName();
            case name: return getName();
            case content: return getContent();
            case feature: return getFeature();
            case attribute: return getAttribute();
            case reference: return getReference();
            case field: return getField();
            case operation: return getOperation();
            case allFeature: return getAllFeature();
            case allFeatureWithSubtype: return getAllFeatureWithSubtype();
            case allSubtype: return getAllSubtype();
            case compositeReference: return getCompositeReference();
            case referencedEndIsNavigable: return getReferencedEndIsNavigable();
            case parameter: return getParameter();
            case format: return getFormat();
            case annotation: return getAnnotation();
            case uniqueValues: return getUniqueValues();
            case isLanguageNeutral: return getIsLanguageNeutral();
            case exception: return getException();
            case semantics: return getSemantics();
            case packageAsJar: return getPackageAsJar();
            case identity: return getIdentity();
            default: return super.get(index);
        }
    }

    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    private <T> List<T> asList(
        Object value
    ) {
        return value == null ? null
            : value instanceof List ? (List)value
            : new ArrayList(Collections.singletonList(value));
    }

    @SuppressWarnings({
        "unchecked",
        "rawtypes"
    })
    private <T> T asSingleValue(
        Object value
    ) {
        if(value instanceof List) {
            List values = (List)value;
            return values.isEmpty() ? null : (T)values.get(0);
        } else {
            return (T)value;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#put(java.lang.Enum, java.lang.Object)
     */
    @Override
    protected void put(
        Member index,
        Object value
    ){
        switch(index) {
            case isDerived:
                setIsDerived(this.<Boolean>asSingleValue(value));
                break;
            case maxLength:
                setMaxLength(this.<Integer>asSingleValue(value));
                break;
            case container:
                setContainer((Path)value);
                break;
            case scope:
                setScope((String)value);
                break;
            case visibility:
                setVisibility((String)value);
                break;
            case isAbstract:
                setIsAbstract((Boolean)value);
                break;
            case type:
                setType((Path)value);
                break;
            case createdAt:
                setCreatedAt(this.<Date>asSingleValue(value));
                break;
            case createdBy:
                setCreatedBy(this.<String>asList(value));
                break;
            case modifiedAt:
                setModifiedAt(this.<Date>asSingleValue(value));
                break;
            case modifiedBy:
                setModifiedBy(this.<String>asList(value));
                break;
            case aggregation:
                setAggregation(this.<String>asSingleValue(value));
                break;
            case isChangeable:
                setIsChangeable(this.<Boolean>asSingleValue(value));
                break;
            case isNavigable:
                setIsNavigable(this.<Boolean>asSingleValue(value));
                break;
            case multiplicity:
                setMultiplicity(this.<String>asSingleValue(value));
                break;
            case isSingleton:
                setIsSingleton((Boolean)value);
                break;
            case supertype:
                setSupertype(this.asList(value));
                break;
            case exposedEnd:
                setExposedEnd(this.<Path>asSingleValue(value));
                break;
            case referencedEnd:
                setReferencedEnd(this.<Path>asSingleValue(value));
                break;
            case qualifierName:
                setQualifierName(this.<String>asSingleValue(value));
                break;
            case qualifierType:
                setQualifierType(this.<Path>asSingleValue(value));
                break;
            case isQuery:
                setIsQuery(this.<Boolean>asSingleValue(value));
                break;
            case direction:
                setDirection(this.<String>asSingleValue(value));
                break;
            case stereotype:
                setStereotype(this.<String>asList(value));
                break;
            case object_class:
                setObject_class(this.<String>asSingleValue(value));
                break;
            case allSupertype:
                setAllSupertype(this.<Path>asList(value));
                break;
            case subtype:
                setSubtype(this.<Path>asList(value));
                break;
            case object_instanceof:
                setObject_instanceof(this.<String>asList(value));
                break;
            case qualifiedName:
                setQualifiedName(this.<String>asSingleValue(value));
                break;
            case name:
                setName(this.<String>asSingleValue(value));
                break;
            case content:
                setContent(this.<Path>asList(value));
                break;
            case feature:
                setFeature(this.<Path>asList(value));
                break;
            case attribute:
                setAttribute((Map<String,Object>)value);
                break;
            case reference:
                setReference((Map<String,Object>)value);
                break;
            case field:
                setField((Map<String,Object>)value);
                break;
            case operation:
                setOperation((Map<String,Object>)value);
                break;
            case allFeature:
                setAllFeature((Map<String,Object>)value);
                break;
            case allFeatureWithSubtype:
                setAllFeatureWithSubtype((Map<String,Object>)value);
                break;
            case allSubtype:
                setAllSubtype(this.<Path>asList(value));
                break;
            case compositeReference:
                setCompositeReference(this.<Path>asSingleValue(value));
                break;
            case referencedEndIsNavigable:
                setReferencedEndIsNavigable(this.<Boolean>asSingleValue(value));
                break;
            case parameter:
                setParameter(this.<Path>asList(value));
                break;
            case format:
                setFormat(this.<String>asList(value));
                break;
            case annotation:
                setAnnotation(this.<String>asSingleValue(value));
                break;
            case uniqueValues:
                setUniqueValues(this.<Boolean>asSingleValue(value));
                break;
            case isLanguageNeutral:
                setIsLanguageNeutral(this.<Boolean>asSingleValue(value));
                break;
            case exception:
                setException(this.<Path>asList(value));
                break;
            case semantics:
                setSemantics(this.<String>asSingleValue(value));
                break;
            case packageAsJar:
                setPackageAsJar((byte[])value);
                break;
            case identity:
                setIdentity(this.<String>asSingleValue(value));
                break;
            default:
                super.put(index, value);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#clone()
     */
    @Override
    public AbstractMappedRecord<Member> clone(
    ) throws CloneNotSupportedException {
        ModelElementRecord that = new ModelElementRecord(this.recordName);
        for(Member member: MEMBERS) {
            that.put(member, this.get(member));
        }
        return that;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ModelElementRecord) {
            ModelElementRecord that = (ModelElementRecord) obj;
            if(
                this.getRecordName().equals(that.getRecordName()) &&
                this.keySet().equals(that.keySet())
            ) {
                for(Object key : keySet()) {
                    if(!eq(this.get(key), that.get(key))) return false;
                }
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // Members
    // ----------------------------------------------------------------------    
    private static final long serialVersionUID = 3968423736858544108L;
    public static final Members<Member> MEMBERS = Members.newInstance(Member.class);
    private static Date CREATED_AT = new Date();
    private static Date MODIFIED_AT = new Date();
    private String recordName;
    private Boolean isDerived;
    private Integer maxLength;
    private Path container;
    private String scope;
    private String visibility;
    private Boolean isAbstract;
    private Path type;
    private String createdBy;
    private String modifiedBy;
    private String aggregation;
    private Boolean isChangeable;
    private Boolean isNavigable;
    private String multiplicity;
    private Boolean isSingleton;
    private List<Object> supertype;
    private Path exposedEnd;
    private Path referencedEnd;
    private String qualifierName;
    private Path qualifierType;
    private Boolean isQuery;
    private String direction;
    private List<String> stereotype;
    private String object_class;
    private List<Path> allSupertype;
    private List<Path> subtype;
    private List<String> object_instanceof;
    private String qualifiedName;
    private String name;
    private List<Path> content;
    private List<Path> feature;
    private Map<String,Object> attribute;
    private Map<String,Object> reference;
    private Map<String,Object> field;
    private Map<String,Object> operation;
    private Map<String,Object> allFeature;
    private Map<String,Object> allFeatureWithSubtype;
    private List<Path> allSubtype;
    private Path compositeReference;
    private Boolean referencedEndIsNavigable;
    private List<Path> parameter;
    private List<String> format;
    private String annotation;
    private Boolean uniqueValues;
    private Boolean isLanguageNeutral;
    private List<Path> exception;
    private String semantics;
    private byte[] packageAsJar;
    private String identity;
    
}
