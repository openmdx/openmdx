/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ModelElement_1 class
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import org.openmdx.base.collection.Sets;
import org.openmdx.base.collection.TypeSafeMarshallingMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.repository.cci.AliasTypeRecord;
import org.openmdx.base.mof.repository.cci.AssociationEndRecord;
import org.openmdx.base.mof.repository.cci.AssociationRecord;
import org.openmdx.base.mof.repository.cci.AttributeRecord;
import org.openmdx.base.mof.repository.cci.BehavioralFeatureRecord;
import org.openmdx.base.mof.repository.cci.ClassRecord;
import org.openmdx.base.mof.repository.cci.ClassifierRecord;
import org.openmdx.base.mof.repository.cci.CollectionTypeRecord;
import org.openmdx.base.mof.repository.cci.ConstantRecord;
import org.openmdx.base.mof.repository.cci.ConstraintRecord;
import org.openmdx.base.mof.repository.cci.DataTypeRecord;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.mof.repository.cci.EnumerationTypeRecord;
import org.openmdx.base.mof.repository.cci.ExceptionRecord;
import org.openmdx.base.mof.repository.cci.GeneralizableElementRecord;
import org.openmdx.base.mof.repository.cci.ImportRecord;
import org.openmdx.base.mof.repository.cci.NamespaceRecord;
import org.openmdx.base.mof.repository.cci.OperationRecord;
import org.openmdx.base.mof.repository.cci.PackageRecord;
import org.openmdx.base.mof.repository.cci.ParameterRecord;
import org.openmdx.base.mof.repository.cci.PrimitiveTypeRecord;
import org.openmdx.base.mof.repository.cci.ReferenceRecord;
import org.openmdx.base.mof.repository.cci.StructuralFeatureRecord;
import org.openmdx.base.mof.repository.cci.StructureFieldRecord;
import org.openmdx.base.mof.repository.cci.StructureTypeRecord;
import org.openmdx.base.mof.repository.cci.TagRecord;
import org.openmdx.base.mof.repository.cci.TypedElementRecord;
import org.openmdx.base.naming.Path;

@SuppressWarnings({"unchecked"})
public class ModelElement_1 implements ModelElement_1_0 {

    public ModelElement_1(
        ElementRecord delegate,
        Model_1 model
    ){
        this.delegate = delegate;
        this.model = model;
    }

    private ModelElement_1(
        ModelElement_1_0 element,
        ElementRecord delegate
    ){
        this(delegate, (Model_1)element.getModel());
    }
        
    public ModelElement_1(
        ModelElement_1_0 element
    ) throws ServiceException {
        this(element, cloneDelegate(element));
    }

    //-------------------------------------------------------------------------

    private static ElementRecord cloneDelegate(ModelElement_1_0 element)
        throws ServiceException {
        try {
            return (ElementRecord) ((ModelElement_1)element).delegate.clone();
        } catch (CloneNotSupportedException exception) {
            throw new ServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getDelegate()
     */
    @Override
    public ElementRecord getDelegate() {
        return this.delegate;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public Model_1 getModel(
    ) {
        return this.model;
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isAliasType(
    ) {
        return this.delegate instanceof AliasTypeRecord;
    }
  
    //-------------------------------------------------------------------------
    @Override
    public boolean isPrimitiveType(
    ) {
        return this.delegate instanceof PrimitiveTypeRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isStructureType(
    ) {
        return this.delegate instanceof StructureTypeRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isStructureFieldType(
    ) {
        return this.delegate instanceof StructureFieldRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isClassType(
    ) {
        return this.delegate instanceof ClassRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isReferenceType(
    ) {
        return this.delegate instanceof ReferenceRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isAttributeType(
    ) {
        return this.delegate instanceof AttributeRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isOperationType(
    ) {
        return this.delegate instanceof OperationRecord;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isPackageType(
    ) {
        return this.delegate instanceof PackageRecord;
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isAssociationEndType()
     */
    @Override
    public boolean isAssociationEndType(
    ) {
        return this.delegate instanceof AssociationEndRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isCollectionType()
     */
    @Override
    public boolean isCollectionType(
    ) {
        return this.delegate instanceof CollectionTypeRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isConstantType()
     */
    @Override
    public boolean isConstantType(
    ) {
        return this.delegate instanceof ConstantRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isConstraintType()
     */
    @Override
    public boolean isConstraintType(
    ) {
        return this.delegate instanceof ConstraintRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isEnumerationType()
     */
    @Override
    public boolean isEnumerationType(
    ) {
        return this.delegate instanceof EnumerationTypeRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isDataType()
     */
    @Override
    public boolean isDataType(
    ) {
        return this.delegate instanceof DataTypeRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isElementType()
     */
    @Override
    public boolean isElementType(
    ) {
        return true;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isExceptionType()
     */
    @Override
    public boolean isExceptionType(
    ) {
        return this.delegate instanceof ExceptionRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isGeneralizableElementType()
     */
    @Override
    public boolean isGeneralizableElementType(
    ) {
        return this.delegate instanceof GeneralizableElementRecord;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isGeneralizableElementType()
     */
    @Override
    public boolean isClassifierType(
    ) {
        return this.delegate instanceof ClassifierRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isGeneralizableElementType()
     */
    @Override
    public boolean isBehaviouralFeatureType(
    ) {
        return this.delegate instanceof BehavioralFeatureRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isImportType()
     */
    @Override
    public boolean isImportType(
    ) {
        return this.delegate instanceof ImportRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isNamespaceType()
     */
    @Override
    public boolean isNamespaceType(
    ) {
        return this.delegate instanceof NamespaceRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isParameterType()
     */
    @Override
    public boolean isParameterType(
    ) {
        return this.delegate instanceof ParameterRecord;
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isStructuralFeatureType()
     */
    @Override
    public boolean isStructuralFeatureType(
    ) {
        return this.delegate instanceof StructuralFeatureRecord;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isTypedElementType()
     */
    @Override
    public boolean isTypedElementType(
    ) {
        return this.delegate instanceof TypedElementRecord;
    }
    
    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isTagType()
     */
    @Override
    public boolean isTagType() {
        return this.delegate instanceof TagRecord;
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isReferenceStoredAsAttribute(
    ) throws ServiceException {
        return
            isReference() &&
            ((ReferenceRecord)this.delegate).asAttribute(getModel().getRecordMarshaller()) != null;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String featureName
    ){
        return this.delegate.get(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getValues(java.lang.String)
     */
    @Override
    public List<Object> objGetList(
        String featureName
    ){
        return (List<Object>) objGetValue(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetMap(java.lang.String)
     */
    @Override
    public Map<String, ModelElement_1_0> objGetMap(
        String featureName
    ){
        return new TypeSafeMarshallingMap<String, Path, ModelElement_1_0>(
            this.model.getElementMarshaller(), (Map<String, Path>)objGetValue(featureName)
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelElement_1_1#isAssociation()
     */
    @Override
    public boolean isAssociationType() {
        return this.delegate instanceof AssociationRecord;
    }

    //-------------------------------------------------------------------------
    @Override
    public Path jdoGetObjectId(
    ) {
        return this.delegate.getObjectId();
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isSet(
        String feature
    ) {
        return this.delegate.containsKey(feature);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetClass()
     */
    @Override
    public String objGetClass(
    ){
        return this.delegate.getRecordName();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objGetSet(java.lang.String)
     */
    @Override
    public Set<Object> objGetSet(
        String featureName
    ){
        return Sets.asSet((Collection<Object>)this.delegate.get(featureName));
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objDefaultFetchGroup()
     */
    @Override
    public Set<String> objDefaultFetchGroup(
    ){
        return this.delegate.keySet();
    }

    @Override
    public String toString(
    ) {
        return this.delegate.toString();
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getName()
	 */
	@Override
	public String getName(
	){
	    return this.delegate.getName();
	}

	@Override
	public String getAggregation(
	) throws ServiceException {
	    return isAssociationEndType() ? ((AssociationEndRecord)this.delegate).getAggregation() : null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getReferencedEnd()
	 */
	@Override
	public Path getReferencedEnd(
	) throws ServiceException {
	    return this.isReferenceType() ? ((ReferenceRecord)this.delegate).getReferencedEnd() : null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getExposedEnd()
	 */
	@Override
	public Path getExposedEnd(
	) throws ServiceException {
	    return this.isReferenceType() ? ((ReferenceRecord)this.delegate).getExposedEnd() : null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getContainer()
	 */
	@Override
	public Path getContainer(
	) throws ServiceException {
	    return this.delegate.getContainer();
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isDerived()
	 */
	@Override
	public Boolean isDerived(
	) throws ServiceException {
	    return 
	        isAssociationType() ? Boolean.valueOf(((AssociationRecord)this.delegate).isDerived()) :
	        isAttributeType() ? Boolean.valueOf(((AttributeRecord)this.delegate).isDerived()) :
	        null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isChangeable()
	 */
	@Override
	public Boolean isChangeable(
	) throws ServiceException {
	    return
	        isStructuralFeatureType() ? Boolean.valueOf(((StructuralFeatureRecord)this.delegate).isChangeable()) : 
	        isAssociationEndType() ? Boolean.valueOf(((AssociationEndRecord)this.delegate).isChangeable()) :
	        null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isAbstract()
	 */
	@Override
	public Boolean isAbstract(
	) throws ServiceException {
	    return
	        isGeneralizableElementType() ? Boolean.valueOf(((GeneralizableElementRecord)this.delegate).isAbstract()) :
	        null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getType()
	 */
	@Override
	public Path getType(
	) throws ServiceException {	
	    return 
	        isClassifierType() ? ((ClassifierRecord)this.delegate).getType() :
	        isTypedElementType() ? ((TypedElementRecord)this.delegate).getType() :
	        null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getQualifierType()
	 */
	@Override
	public Path getQualifierType(
	) throws ServiceException {	
	    if(isAssociationEndType()) {
	        List<Path> qualifierTypes = ((AssociationEndRecord)this.delegate).getQualifierType();
	        return qualifierTypes.isEmpty() ? null : qualifierTypes.get(0);
	    } else {
	        return null;
	    }
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getQualifiedName()
	 */
	@Override
	public String getQualifiedName(
	){
	    return this.delegate.getQualifiedName();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getMultiplicity()
	 */
	@Override
	public String getMultiplicity(
	) throws ServiceException {
	    return
	        isStructuralFeatureType() ? ((StructuralFeatureRecord)this.delegate).getMultiplicity() :
	        isStructureFieldType() ? ((StructureFieldRecord)this.delegate).getMultiplicity() :
	        isParameterType() ? ((ParameterRecord)this.delegate).getMultiplicity() :
	        isAssociationEndType() ? ((AssociationEndRecord)this.delegate).getMultiplicity() :
	        null;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#isReference()
	 */
	@Override
	public boolean isReference(
	) throws ServiceException {
	    return isReferenceType(); // Matches for ReferenceStoredAsAttribute, too
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.mof.cci.ModelElement_1_0#getDereferencedType()
	 */
	@Override
	public ModelElement_1_0 getDereferencedType(
	) throws ServiceException {
		if(this.dereferencedType == null) {
			this.dereferencedType = ModelHelper_1.getElementType(
                this,
                this.model.getModelElements()
            );	
		}
		return this.dereferencedType;
	}

	/* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isInstanceOf(java.lang.Class)
     */
    @Override
    public boolean isInstanceOf(Class<? extends ElementRecord> type) {
        return type.isInstance(this.delegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#isInstanceOf(java.util.List)
     */
    @Override
    public boolean isInstanceOf(Collection<Class<? extends ElementRecord>> types) {
        for(Class<? extends ElementRecord> type : types){
            if(isInstanceOf(type)){
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.ModelElement_1_0#getModelSegmentName()
     */
    @Override
    public String getSegmentName(
    ){
        return this.delegate.getObjectId().getSegment(4).toClassicRepresentation();
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final ElementRecord delegate;
    private final Model_1 model;
    private ModelElement_1_0 dereferencedType = null;
    
}
