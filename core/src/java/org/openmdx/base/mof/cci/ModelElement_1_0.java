/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ModelElement_1_0 interface
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.cci;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

public interface ModelElement_1_0 extends DataObject_1_0 {

    public Model_1_0 getModel();
  
    public boolean isAliasType();
  
    public boolean isPrimitiveType();
  
    public boolean isStructureType();
  
    public boolean isStructureFieldType();
    
    public boolean isClassType();
    
    public boolean isReferenceType();
    
    public boolean isAttributeType();
    
    public boolean isOperationType();
    
    public boolean isPackageType();
    
    public boolean isAssociationType();
    
    public boolean isElementType();
    
    public boolean isNamespaceType();
    
    public boolean isGeneralizableElementType();
    
    public boolean isTypedElementType();
    
    public boolean isDataType();
    
    public boolean isExceptionType();
    
    public boolean isAssociationEndType();
    
    public boolean isImportType();
    
    public boolean isConstraintType();
    
    public boolean isConstantType();
    
    public boolean isStructuralFeatureType();
    
    public boolean isParameterType();
    
    public boolean isCollectionType();

    public boolean isClassifierType();
    
    public boolean isEnumerationType();

    public boolean isBehaviouralFeatureType();

    public boolean isReferenceStoredAsAttribute() throws ServiceException;
    
    public boolean isSet(String feature);
    
    public boolean isInstanceOf(String qualifiedName) throws ServiceException;

    public String getName() throws ServiceException;
    
    public String getQualifiedName() throws ServiceException;
    
    public Path getReferencedEnd() throws ServiceException;
    
    public Path getExposedEnd() throws ServiceException;
    
    public String getAggregation() throws ServiceException;
    
    public Path getContainer() throws ServiceException;
    
    public Boolean isDerived() throws ServiceException;
    
    public Boolean isChangeable() throws ServiceException;
    
    public Boolean isAbstract() throws ServiceException;
    
    public Path getType() throws ServiceException;

    public String getMultiplicity() throws ServiceException;
    
    public Path getQualifierType() throws ServiceException;
    
    /**
     * Tells whether the given feature is a reference. The same as 
     * isReferenceType() || (getReferencedEnd() != && exposedEnd != null)
     * 
     * @param feature the feature to be inspected
     * 
     * @return <code>true</code> if the given feature is a reference
     * 
     * @throws ServiceException
     */
    public boolean isReference() throws ServiceException;
        
}
