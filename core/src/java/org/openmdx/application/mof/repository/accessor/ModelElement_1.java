/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ModelElement_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2015, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.collection.Unmarshalling;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;

//---------------------------------------------------------------------------
@SuppressWarnings({"rawtypes","unchecked"})
public class ModelElement_1 implements ModelElement_1_0, Delegating_1_0<ObjectRecord> {

    public ModelElement_1(
        MappedRecord data,
        Model_1 model
    ) throws ServiceException {
            this.data = Facades.asObject(data);
            this.model = model;
    }

    //-------------------------------------------------------------------------
    public ModelElement_1(
        ModelElement_1_0 element
    ) throws ServiceException {
    	this.data = ((ModelElement_1)element).data.cloneObject();
        this.model = (Model_1)element.getModel();
    }

    //-------------------------------------------------------------------------
    public Model_1 getModel(
    ) {
        return this.model;
    }

    @Override
    public ObjectRecord objGetDelegate(
    ){
        return this.data.getDelegate();
    }

    //-------------------------------------------------------------------------
    public boolean isAliasType(
    ) {
        if(this.isAliasType == null) {
            this.isAliasType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.ALIAS_TYPE));
        }
        return this.isAliasType.booleanValue();
    }
  
    //-------------------------------------------------------------------------
    public boolean isPrimitiveType(
    ) {
        if(this.isPrimitiveType == null) {
            this.isPrimitiveType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.PRIMITIVE_TYPE));
        }
        return this.isPrimitiveType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    public boolean isStructureType(
    ) {
        if(this.isStructureType == null) {
            this.isStructureType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.STRUCTURE_TYPE));
        }
        return this.isStructureType.booleanValue();            
    }
    
    //-------------------------------------------------------------------------
    public boolean isStructureFieldType(
    ) {
        if(this.isStructureFieldType == null) {
            this.isStructureFieldType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.STRUCTURE_FIELD));
        }
        return this.isStructureFieldType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    public boolean isClassType(
    ) {
        if(this.isClassType == null) {
            this.isClassType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.CLASS));
        }
        return this.isClassType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    public boolean isReferenceType(
    ) {
        if(this.isReferenceType == null) {
            this.isReferenceType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.REFERENCE));
        }
        return this.isReferenceType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    public boolean isAttributeType(
    ) {
        if(this.isAttributeType == null) {
            this.isAttributeType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.ATTRIBUTE));            
        }
        return this.isAttributeType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    public boolean isOperationType(
    ) {
        if(this.isOperationType == null) {
            this.isOperationType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.OPERATION));
        }
        return this.isOperationType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    public boolean isPackageType(
    ) {
        if(this.isPackageType == null) {
            this.isPackageType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.PACKAGE));
        }
        return this.isPackageType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isAssociationEndType()
     */
    public boolean isAssociationEndType(
    ) {
        if(this.isAssociationEndType == null) {
            this.isAssociationEndType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.ASSOCIATION_END));
        }
        return this.isAssociationEndType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isCollectionType()
     */
    public boolean isCollectionType(
    ) {
        if(this.isCollectionType == null) {
            this.isCollectionType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.COLLECTION_TYPE));
        }
        return this.isCollectionType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isConstantType()
     */
    public boolean isConstantType(
    ) {
        if(this.isConstantType == null) {
            this.isConstantType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.CONSTANT));
        }
        return this.isConstantType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isConstraintType()
     */
    public boolean isConstraintType(
    ) {
        if(this.isConstraintType == null) {
            this.isConstraintType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.CONSTRAINT));
        }
        return this.isConstraintType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isEnumerationType()
     */
    public boolean isEnumerationType(
    ) {
        if(this.isEnumerationType == null) {
            this.isEnumerationType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.ENUMERATION_TYPE));
        }
        return this.isEnumerationType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isDataType()
     */
    public boolean isDataType(
    ) {
        return
            this.isPrimitiveType() ||
            this.isEnumerationType() ||
            this.isStructureType() ||
            this.isCollectionType();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isElementType()
     */
    public boolean isElementType(
    ) {
        return true;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isExceptionType()
     */
    public boolean isExceptionType(
    ) {
        if(this.isExceptionType == null) {
            this.isExceptionType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.EXCEPTION));
        }
        return this.isExceptionType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isGeneralizableElementType()
     */
    public boolean isGeneralizableElementType(
    ) {
        return
            this.isPackageType() ||
            this.isClassifierType();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isGeneralizableElementType()
     */
    public boolean isClassifierType(
    ) {
        return
            this.isAssociationType() ||
            this.isClassType() ||
            this.isDataType();            
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isGeneralizableElementType()
     */
    public boolean isBehaviouralFeatureType(
    ) {
        return
            this.isOperationType() ||
            this.isExceptionType();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isImportType()
     */
    public boolean isImportType(
    ) {
        if(this.isImportType == null) {
            this.isImportType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.IMPORT));
        }
        return this.isImportType.booleanValue();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isNamespaceType()
     */
    public boolean isNamespaceType(
    ) {
        return
            this.isGeneralizableElementType() ||
            this.isBehaviouralFeatureType();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isParameterType()
     */
    public boolean isParameterType(
    ) {
        if(this.isParameterType == null) {
            this.isParameterType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.PARAMETER));
        }
        return this.isParameterType.booleanValue();
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isStructuralFeatureType()
     */
    public boolean isStructuralFeatureType(
    ) {
        return
            this.isAttributeType() ||
            this.isReferenceType();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isTypedElementType()
     */
    public boolean isTypedElementType(
    ) {
        return
            this.isCollectionType() ||
            this.isAliasType() ||
            this.isStructuralFeatureType() ||
            this.isAssociationEndType() ||
            this.isConstantType() ||
            this.isStructureFieldType() ||
            this.isParameterType();
    }
    
    //-------------------------------------------------------------------------
    public boolean isReferenceStoredAsAttribute(
    ) throws ServiceException {
        if(this.isReferenceStoredAsAttribute == null){
            ModelElement_1_0 referencedEnd = model.findElement( 
                this.getReferencedEnd()
            );
            ModelElement_1_0 exposedEnd = model.findElement(
                this.getExposedEnd()
            );
            List qualifierTypes = referencedEnd.objGetList("qualifierType");
            this.isReferenceStoredAsAttribute = Boolean.valueOf(
                AggregationKind.NONE.equals(referencedEnd.getAggregation()) &&
                AggregationKind.NONE.equals(exposedEnd.getAggregation()) &&
                (qualifierTypes.isEmpty() || this.model.isPrimitiveType(qualifierTypes.get(0)))
            );
        }
        return this.isReferenceStoredAsAttribute.booleanValue();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValue(java.lang.String)
     */
    public Object objGetValue(
        String featureName
    ) throws ServiceException {
        return this.data.attributeValue(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValues(java.lang.String)
     */
    public List<Object> objGetList(
        String featureName
    ) throws ServiceException {
        return this.data.attributeValuesAsList(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetMap(java.lang.String)
     */
    @Override
    public Map objGetMap(
        String featureName
    ) throws ServiceException {
        return new MarshallingMap( 
            this.model,
            (Map)this.data.attributeValues(
                featureName,
                Multiplicity.MAP
            ),
            Unmarshalling.EAGER
        );
    }

    public void objSetValue(
        String featureName,
        Object value
    ) {
        if(SystemAttributes.OBJECT_CLASS.equals(featureName)) {
            this.data.getValue().setRecordName((String)value);
        }
        else {
            try {
                this.data.replaceAttributeValuesAsListBySingleton(
                    featureName,
                    value
                );
            }
            catch(Exception e) {
                throw new JDOUserException("Unable to set value", e);
            }
        }
    }
    
    //------------------------------------------------------------------------
    // Implements DataObject_1_0    
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Constructable#openmdxjdoIsUnderConstruction()
     */
    public boolean openmdxjdoIsInitializing(
    ){
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Constructable#openmdxjdoPostInitialize()
     */
    public void openmdxjdoPostInitialize(
    ){
        // Nothing to do 
    }

    //------------------------------------------------------------------------
    // Implements ModelElement_1_1    
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelElement_1_1#isAssociation()
     */
    public boolean isAssociationType() {
        if(this.isAssociationType == null) {
            this.isAssociationType = Boolean.valueOf(this.data.getObjectClass().equals(ModelAttributes.ASSOCIATION));
        }
        return this.isAssociationType.booleanValue();
    }

    //-------------------------------------------------------------------------
    public Path jdoGetObjectId(
    ) {
        return this.data.getPath();
    }
    
    //-------------------------------------------------------------------------
    public boolean isSet(
        String feature
    ) {
        return this.data.getValue().keySet().contains(feature);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#getInaccessibilityReason()
     */
    public ServiceException getInaccessibilityReason()
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void objAddEventListener(InstanceLifecycleListener listener)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        return this.data.getValue().keySet();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetClass()
     */
    public String objGetClass()
        throws ServiceException {
        return this.data.getObjectClass();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetContainer(java.lang.String)
     */
    public Container_1_0 objGetContainer(String feature)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     */
    public <T extends InstanceLifecycleListener> T[] objGetEventListeners(
        Class<T> listenerType)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetSet(java.lang.String)
     */
    public Set<Object> objGetSet(String featureName)
        throws ServiceException {
        try {
            return Sets.asSet(
                (Collection<Object>)this.data.attributeValues(
                    featureName,
                    Multiplicity.SET
                )
            );
        }
        catch(Exception e) {
            throw new JDOUserException("Unable to get value", e);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap<Integer, Object> objGetSparseArray(String feature)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        throw new NotSupportedException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objIsContained()
     */
    public boolean objIsContained(
    ){
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#getContainer(boolean)
     */
//  @Override
    public Container_1_0 getContainer(
        boolean forEviction
    ) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objIsInaccessible()
     */
    public boolean objIsInaccessible(
    ){
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objDoesNotExist()
     */
    public boolean objDoesNotExist() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objMove(org.openmdx.base.accessor.cci.Selection_1_0, java.lang.String)
     */
    public void objMove(Container_1_0 there, String criteria)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(InstanceLifecycleListener listener)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
     */
    public PersistenceManager jdoGetPersistenceManager() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public UUID jdoGetTransactionalObjectId() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    public boolean jdoIsDeleted() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    public boolean jdoIsDirty() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    public boolean jdoIsNew() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsPersistent()
     */
    public boolean jdoIsPersistent() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    public boolean jdoIsTransactional() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(StateManager sm)
        throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
     */
    public DataObject_1_0 openmdxjdoClone(String... exclude) {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    @Override
    public String toString(
    ) {
        return this.data == null ? "" : this.data.getPath() + " is " + this.data.getObjectClass() + " having attributes " + this.data.getValue().keySet().toString();
    }
    
    

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isInstanceOf(java.lang.String)
     */
    @Override
    public boolean isInstanceOf(String qualifiedName) throws ServiceException {
        for(Object supertype : objGetList("object_instanceof")){
            if(qualifiedName.equals(supertype)) {
                return true;
            }
        }
        return false;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getName()
	 */
	@Override
	public String getName(
	) throws ServiceException {
		if(this.name == null) {
			this.name = (String)this.objGetValue("name");
		}
		return this.name;
	}

	@Override
	public String getAggregation(
	) throws ServiceException {
		if(this.aggregation == null) {
			this.aggregation = (String)this.objGetValue("aggregation");
		}
		return this.aggregation;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getReferencedEnd()
	 */
	@Override
	public Path getReferencedEnd(
	) throws ServiceException {
		if(this.referencedEnd == null) {
			this.referencedEnd = (Path)objGetValue("referencedEnd");
		}
		return this.referencedEnd;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getExposedEnd()
	 */
	@Override
	public Path getExposedEnd(
	) throws ServiceException {
		if(this.exposedEnd == null) {
			this.exposedEnd = (Path)objGetValue("exposedEnd");
		}
		return this.exposedEnd;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getContainer()
	 */
	@Override
	public Path getContainer(
	) throws ServiceException {
		if(this.container == null) {
			this.container = (Path)objGetValue("container");
		}
		return this.container;
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isDerived()
	 */
	@Override
	public Boolean isDerived(
	) throws ServiceException {
		if(this.isDerived == null) {
			this.isDerived = (Boolean)objGetValue("isDerived");
		}
		return this.isDerived;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isChangeable()
	 */
	@Override
	public Boolean isChangeable(
	) throws ServiceException {
		if(this.isChangeable == null) {
			this.isChangeable = (Boolean)objGetValue("isChangeable");
		}
		return this.isChangeable;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isAbstract()
	 */
	@Override
	public Boolean isAbstract(
	) throws ServiceException {
		if(this.isAbstract == null) {
			this.isAbstract = (Boolean)objGetValue("isAbstract");
		}
		return this.isAbstract;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getType()
	 */
	@Override
	public Path getType(
	) throws ServiceException {	
		if(this.type == null) {
			this.type = (Path)objGetValue("type");
		}
		return this.type;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getQualifierType()
	 */
	@Override
	public Path getQualifierType(
	) throws ServiceException {	
		if(this.qualifierType == null) {
			this.qualifierType = (Path)objGetValue("qualifierType");
		}
		return this.qualifierType;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getQualifiedName()
	 */
	@Override
	public String getQualifiedName(
	) throws ServiceException {
		if(this.qualifiedName == null) {
			this.qualifiedName = (String)objGetValue("qualifiedName");
		}
		return this.qualifiedName;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getMultiplicity()
	 */
	@Override
	public String getMultiplicity(
	) throws ServiceException {
		if(this.multiplicity == null) {
			this.multiplicity = (String)objGetValue("multiplicity");
		}
		return this.multiplicity;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isReference()
	 */
	@Override
	public boolean isReference(
	) throws ServiceException {
	    final boolean reply;
		if(this.isReference == null) {
	    	reply = this.isReferenceType() || (
	    	    this.getExposedEnd() != null && this.getReferencedEnd() != null
	    	);
            this.isReference = Boolean.valueOf(reply);
	    } else {
	        reply = this.isReference.booleanValue();
	    }
        return reply;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getDereferencedType()
	 */
	@Override
	public ModelElement_1_0 getDereferencedType(
	) throws ServiceException {
		if(this.dereferencedType == null) {
			this.dereferencedType = ModelHelper.getElementType(
                this,
                this.model.getModelElements()
            );	
		}
		return this.dereferencedType;
	}

	//-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final Object_2Facade data;
    private Model_1 model;
    private Boolean isAliasType = null;
    private Boolean isPrimitiveType = null;
    private Boolean isStructureType = null;
    private Boolean isStructureFieldType = null;
    private Boolean isClassType = null;
    private Boolean isReferenceType = null;
    private Boolean isAttributeType = null;
    private Boolean isOperationType = null;
    private Boolean isPackageType = null;
    private Boolean isReferenceStoredAsAttribute = null;
    private Boolean isAssociationType = null;
    private Boolean isAssociationEndType = null;
    private Boolean isCollectionType = null;
    private Boolean isConstantType = null;
    private Boolean isConstraintType = null;
    private Boolean isExceptionType = null;
    private Boolean isEnumerationType = null;
    private Boolean isImportType = null;
    private Boolean isParameterType = null;
    private Boolean isReference = null;
    private String name = null;
    private String qualifiedName = null;
    private String aggregation = null;
    private Path referencedEnd = null;
    private Path exposedEnd = null;
    private Path container = null;
    private Boolean isDerived = null;
    private Boolean isChangeable = null;
    private Boolean isAbstract = null;
    private Path type = null;
    private Path qualifierType = null;
    private String multiplicity = null;
    private ModelElement_1_0 dereferencedType = null;
    
}

//--- End of File -----------------------------------------------------------
