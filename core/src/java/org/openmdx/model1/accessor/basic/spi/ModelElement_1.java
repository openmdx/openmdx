/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ModelElement_1.java,v 1.8 2008/04/04 01:12:15 hburger Exp $
 * Description: ModelElement_1 class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 01:12:15 $
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
package org.openmdx.model1.accessor.basic.spi;

import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_1;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;

//---------------------------------------------------------------------------
public class ModelElement_1
    extends DataproviderObject
    implements ModelElement_1_1 {

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
        super(element);
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
                this.values("referencedEnd").get(0),
                elements
            );
            ModelElement_1_0 exposedEnd = this.model.getElement(
                this.values("exposedEnd").get(0),
                elements
            );
            List qualifierTypes = referencedEnd.values("qualifierType");
            this.isReferenceStoredAsAttribute =
                AggregationKind.NONE.equals(referencedEnd.values("aggregation").get(0)) &&
                AggregationKind.NONE.equals(exposedEnd.values("aggregation").get(0)) &&
                ((qualifierTypes.size() == 0) || this.model.isPrimitiveType(qualifierTypes.get(0), elements));
        }
        return this.isReferenceStoredAsAttribute;
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
