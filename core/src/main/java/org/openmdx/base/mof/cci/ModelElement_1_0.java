/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ModelElement_1_0 interface
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.cci;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.naming.Path;

public interface ModelElement_1_0 {

    Model_1_0 getModel();
  
    boolean isAliasType();
  
    boolean isPrimitiveType();
  
    boolean isStructureType();
  
    boolean isStructureFieldType();
    
    boolean isClassType();
    
    boolean isReferenceType();
    
    boolean isAttributeType();
    
    boolean isOperationType();
    
    boolean isPackageType();
    
    boolean isAssociationType();
    
    boolean isElementType();
    
    boolean isNamespaceType();
    
    boolean isGeneralizableElementType();
    
    boolean isTypedElementType();
    
    boolean isDataType();
    
    boolean isExceptionType();
    
    boolean isAssociationEndType();
    
    boolean isImportType();
    
    boolean isConstraintType();
    
    boolean isConstantType();
    
    boolean isStructuralFeatureType();
    
    boolean isParameterType();
    
    boolean isCollectionType();

    boolean isClassifierType();
    
    boolean isEnumerationType();

    boolean isBehaviouralFeatureType();

    boolean isTagType();
    
    boolean isReferenceStoredAsAttribute();
    
    boolean isSet(String feature);
    
    boolean isInstanceOf(Class<? extends ElementRecord> type);

    boolean isInstanceOf(Collection<Class<? extends ElementRecord>> types);
    
    String getName();
    
    String getQualifiedName();
    
    String getSegmentName();
    
    Path getReferencedEnd();
    
    Path getExposedEnd();
    
    String getAggregation();
    
    Path getContainer();
    
    Boolean isDerived();
    
    Boolean isChangeable();
    
    Boolean isAbstract();
    
    Path getType();

    String getMultiplicity();
    
    Path getQualifierType();
    
    /**
     * Tells whether the given feature is a reference. The same as 
     * isReferenceType() || (getReferencedEnd() != && exposedEnd != null)
     * 
     * @return {@code true} if the given feature is a reference
     */
    boolean isReference();
        
    /**
     * CR20020817 support
     * 
     * @return the dereferenced type
     * @throws ServiceException 
     */
    ModelElement_1_0 getDereferencedType() throws ServiceException;

    /**
     * Retrieves the model element's data record
     * 
     * @return  the model element's data record
     */
    ElementRecord getDelegate();
    
    /** 
     * Return the openMDX identity associated with this instance <em>(i.e. not a copy 
     * as prescribed by JDO)</em>.
     * <P>
     * Transient instances return null.
     * 
     * @return the object id
     */
    Path jdoGetObjectId();

    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    String objGetClass(
    );

    /**
     * Get a single-valued attribute.
     * <p>
     * This method returns a {@code BAD_PARAMETER} exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    Object objGetValue(
        String feature
    );

    /**
     * Get a List attribute.
     * <p> 
     * This method never returns {@code null} as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    List<Object> objGetList(
        String feature
    );
    
    /**
     * Get a Set attribute.
     * <p> 
     * This method never returns {@code null} as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    Set<Object> objGetSet(
        String feature
    );

    /**
     * Get a Map attribute.
     * <p> 
     * This method never returns {@code null} as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a map which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    Map<String, ModelElement_1_0> objGetMap(
        String feature
    );
    
    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     */
    Set<String> objDefaultFetchGroup(
    );

}
