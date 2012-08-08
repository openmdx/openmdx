/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ModelElement_1.java,v 1.4 2009/03/01 12:29:56 wfro Exp $
 * Description: ModelElement_1 class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/01 12:29:56 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.mof.cci.AggregationKind;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.LargeObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_1;
import org.openmdx.base.naming.Path;

//---------------------------------------------------------------------------
public class ModelElement_1
    extends DataproviderObject
    implements ModelElement_1_1 
{

    private static final long serialVersionUID = 3257002159609753654L;

    //-------------------------------------------------------------------------
    public ModelElement_1(
        DataproviderObject element,
        Model_1 model
    ) {
        super(element);
        this.model = model;
    }
  
    //-------------------------------------------------------------------------
    public ModelElement_1(
        ModelElement_1_0 element
    ) {
        super(
            element instanceof ModelElement_1 ? 
                (ModelElement_1)element :
                element instanceof DataproviderObject ?
                     new DataproviderObject((DataproviderObject)element) :
                     null
        );
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
            this.isAliasType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ALIAS_TYPE);
        }
        return this.isAliasType;
    }
  
    //-------------------------------------------------------------------------
    public boolean isPrimitiveType(
    ) {
        if(this.isPrimitiveType == null) {
            this.isPrimitiveType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PRIMITIVE_TYPE);
        }
        return this.isPrimitiveType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isStructureType(
    ) {
        if(this.isStructureType == null) {
            this.isStructureType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_TYPE);
        }
        return this.isStructureType;            
    }
    
    //-------------------------------------------------------------------------
    public boolean isStructureFieldType(
    ) {
        if(this.isStructureFieldType == null) {
            this.isStructureFieldType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD);
        }
        return this.isStructureFieldType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isClassType(
    ) {
        if(this.isClassType == null) {
            this.isClassType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.CLASS);
        }
        return this.isClassType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isReferenceType(
    ) {
        if(this.isReferenceType == null) {
            this.isReferenceType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE);
        }
        return this.isReferenceType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isAttributeType(
    ) {
        if(this.isAttributeType == null) {
            this.isAttributeType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE);            
        }
        return this.isAttributeType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isOperationType(
    ) {
        if(this.isOperationType == null) {
            this.isOperationType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION);
        }
        return this.isOperationType;
    }
    
    //-------------------------------------------------------------------------
    public boolean isPackageType(
    ) {
        if(this.isPackageType == null) {
            this.isPackageType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PACKAGE);
        }
        return this.isPackageType;
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
        return super.values(featureName).get(0);
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValues(java.lang.String)
     */
    public List<Object> objGetList(
        String featureName
    ) {
        return super.values(featureName);
    }
        
    public void objSetValue(
        String featureName,
        Object value
    ) {
        super.clearValues(featureName).add(value);
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
        if(this.associationType == null) {
            this.associationType = this.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ASSOCIATION);
        }
        return this.associationType;
    }

    //-------------------------------------------------------------------------
    public Path jdoGetObjectId(
    ) {
        return super.path();
    }
    
    //-------------------------------------------------------------------------
    public boolean isSet(
        String feature
    ) {
        return super.attributeNames().contains(feature);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#getAspect(java.lang.String)
     */
    public Map<String, DataObject_1_0> getAspect(String aspectClass)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
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
    public void objAddEventListener(String feature, EventListener listener)
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
        return (String)this.objGetValue(SystemAttributes.OBJECT_CLASS);
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
    public <T extends EventListener> T[] objGetEventListeners(
        String feature,
        Class<T> listenerType)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetLargeObject(java.lang.String)
     */
    public LargeObject_1_0 objGetLargeObject(String feature)
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
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objIsContained()
     */
    public boolean objIsContained()
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objIsInaccessible()
     */
    public boolean objIsInaccessible()
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objMove(org.openmdx.base.accessor.cci.Container_1_0, java.lang.String)
     */
    public void objMove(Container_1_0 there, String criteria)
        throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by ModelElement_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(String feature, EventListener listener)
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
    public Object jdoGetTransactionalObjectId() {
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

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final Model_1 model;
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
    private Boolean associationType = null;

}

//--- End of File -----------------------------------------------------------
