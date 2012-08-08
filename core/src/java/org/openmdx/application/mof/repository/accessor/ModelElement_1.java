/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ModelElement_1.java,v 1.25 2010/06/30 12:48:31 hburger Exp $
 * Description: ModelElement_1 class
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/30 12:48:31 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;

//---------------------------------------------------------------------------
public class ModelElement_1 implements ModelElement_1_0 {

    private static final long serialVersionUID = 3257002159609753654L;

    //-------------------------------------------------------------------------
    public ModelElement_1(
        MappedRecord data,
        Model_1 model
    ) throws ServiceException {
        try {
            this.data = Object_2Facade.newInstance(data);
            this.model = model;
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public ModelElement_1(
        ModelElement_1_0 element
    ) throws ServiceException {
        try {
            this.data = Object_2Facade.newInstance(
                Object_2Facade.cloneObject(((ModelElement_1)element).data.getDelegate())
            );
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        this.model = (Model_1)element.getModel();
    }

    //-------------------------------------------------------------------------
    public Model_1 getModel(
    ) {
        return this.model;
    }

    //-------------------------------------------------------------------------
    public boolean isAliasType(
    ) {
        if(this.isAliasType == null) {
            this.isAliasType = this.data.getObjectClass().equals(ModelAttributes.ALIAS_TYPE);
        }
        return this.isAliasType;
    }
  
    //-------------------------------------------------------------------------
    public boolean isPrimitiveType(
    ) {
        if(this.isPrimitiveType == null) {
            this.isPrimitiveType = this.data.getObjectClass().equals(ModelAttributes.PRIMITIVE_TYPE);
        }
        return this.isPrimitiveType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isStructureType(
    ) {
        if(this.isStructureType == null) {
            this.isStructureType = this.data.getObjectClass().equals(ModelAttributes.STRUCTURE_TYPE);
        }
        return this.isStructureType;            
    }
    
    //-------------------------------------------------------------------------
    public boolean isStructureFieldType(
    ) {
        if(this.isStructureFieldType == null) {
            this.isStructureFieldType = this.data.getObjectClass().equals(ModelAttributes.STRUCTURE_FIELD);
        }
        return this.isStructureFieldType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isClassType(
    ) {
        if(this.isClassType == null) {
            this.isClassType = this.data.getObjectClass().equals(ModelAttributes.CLASS);
        }
        return this.isClassType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isReferenceType(
    ) {
        if(this.isReferenceType == null) {
            this.isReferenceType = this.data.getObjectClass().equals(ModelAttributes.REFERENCE);
        }
        return this.isReferenceType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isAttributeType(
    ) {
        if(this.isAttributeType == null) {
            this.isAttributeType = this.data.getObjectClass().equals(ModelAttributes.ATTRIBUTE);            
        }
        return this.isAttributeType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isOperationType(
    ) {
        if(this.isOperationType == null) {
            this.isOperationType = this.data.getObjectClass().equals(ModelAttributes.OPERATION);
        }
        return this.isOperationType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isPackageType(
    ) {
        if(this.isPackageType == null) {
            this.isPackageType = this.data.getObjectClass().equals(ModelAttributes.PACKAGE);
        }
        return this.isPackageType;
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isAssociationEndType()
     */
    public boolean isAssociationEndType(
    ) {
        if(this.isAssociationEndType == null) {
            this.isAssociationEndType = this.data.getObjectClass().equals(ModelAttributes.ASSOCIATION_END);
        }
        return this.isAssociationEndType;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isCollectionType()
     */
    public boolean isCollectionType(
    ) {
        if(this.isCollectionType == null) {
            this.isCollectionType = this.data.getObjectClass().equals(ModelAttributes.COLLECTION_TYPE);
        }
        return this.isCollectionType;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isConstantType()
     */
    public boolean isConstantType(
    ) {
        if(this.isConstantType == null) {
            this.isConstantType = this.data.getObjectClass().equals(ModelAttributes.CONSTANT);
        }
        return this.isConstantType;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isConstraintType()
     */
    public boolean isConstraintType(
    ) {
        if(this.isConstraintType == null) {
            this.isConstraintType = this.data.getObjectClass().equals(ModelAttributes.CONSTRAINT);
        }
        return this.isConstraintType;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isEnumerationType()
     */
    public boolean isEnumerationType(
    ) {
        if(this.isEnumerationType == null) {
            this.isEnumerationType = this.data.getObjectClass().equals(ModelAttributes.ENUMERATION_TYPE);
        }
        return this.isEnumerationType;
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
            this.isExceptionType = this.data.getObjectClass().equals(ModelAttributes.EXCEPTION);
        }
        return this.isExceptionType;
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
            this.isImportType = this.data.getObjectClass().equals(ModelAttributes.IMPORT);
        }
        return this.isImportType;
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
            this.isParameterType = this.data.getObjectClass().equals(ModelAttributes.PARAMETER);
        }
        return this.isParameterType;
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
    @SuppressWarnings("unchecked")
    public boolean isReferenceStoredAsAttribute(
        Map elements
    ) throws ServiceException {
        if(this.isReferenceStoredAsAttribute == null) {
            ModelElement_1_0 referencedEnd = this.model.getElement(
                this.objGetValue("referencedEnd"),
                elements
            );
            ModelElement_1_0 exposedEnd = this.model.getElement(
                this.objGetValue("exposedEnd"),
                elements
            );
            List qualifierTypes = referencedEnd.objGetList("qualifierType");
            this.isReferenceStoredAsAttribute =
                AggregationKind.NONE.equals(referencedEnd.objGetValue("aggregation")) &&
                AggregationKind.NONE.equals(exposedEnd.objGetValue("aggregation")) &&
                ((qualifierTypes.size() == 0) || this.model.isPrimitiveType(qualifierTypes.get(0), elements));
        }
        return this.isReferenceStoredAsAttribute;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValue(java.lang.String)
     */
    public Object objGetValue(
        String featureName
    ) {
        try {
            return this.data.attributeValue(featureName);
        }
        catch(Exception e) {
            throw new JDOUserException("Unable to get value", e);
        }
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValues(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Object> objGetList(
        String featureName
    ) {
        try {
            return (List<Object>)this.data.attributeValues(
                featureName,
                Multiplicities.LIST
            );
        }
        catch(Exception e) {
            throw new JDOUserException("Unable to get value", e);
        }
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
                this.data.attributeValuesAsList(featureName).clear();
                this.data.attributeValuesAsList(featureName).add(value);
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
    }

    //------------------------------------------------------------------------
    // Implements ModelElement_1_1    
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelElement_1_1#isAssociation()
     */
    public boolean isAssociationType() {
        if(this.isAssociationType == null) {
            this.isAssociationType = this.data.getObjectClass().equals(ModelAttributes.ASSOCIATION);
        }
        return this.isAssociationType;
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
    public Set<String> objDefaultFetchGroup()
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
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
    public Set<Object> objGetSet(String feature)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
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
    public DataObject_1_0 openmdxjdoClone() {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    @Override
    public String toString(
    ) {
        return this.data == null ? null : this.data.getPath() + " is " + this.data.getObjectClass() + " having attributes " + this.data.getValue().keySet().toString();
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final Model_1 model;
    private final Object_2Facade data;
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
}

//--- End of File -----------------------------------------------------------
