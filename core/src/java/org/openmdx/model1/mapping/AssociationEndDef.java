/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AssociationEndDef.java,v 1.4 2008/04/18 11:56:16 hburger Exp $
 * Description: VelocityClassDef class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/18 11:56:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.model1.mapping;

import java.util.HashSet;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_4;

public class AssociationEndDef extends ElementDef {

    /**
     * Constructor 
     *
     * @param associationEndDef
     * @param model
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public AssociationEndDef(
        ModelElement_1_0 associationEndDef,
        Model_1_0 model
    ) throws ServiceException {
        super( 
            getString(associationEndDef.values("name")),
            getString(associationEndDef.values("qualifiedName")),
            getString(associationEndDef.values("annotation")),
            new HashSet<String>(associationEndDef.values("stereotype"))
        );      
        this.associationEndDef = associationEndDef;        
        this.model = model;
        this.aggregation = getString(associationEndDef.values("aggregation"));
        this.navigable = getBoolean(associationEndDef.values("isNavigable"));
        this.multiplicity = (String)associationEndDef.values("multiplicity").get(0);
        this.type = getQualifiedTypeName(associationEndDef.getValues("type"));
        this.qualifierType = getQualifiedTypeName(associationEndDef.getValues("qualifierType"));
        this.qualifierName = getString(associationEndDef.values("qualifierName"));
    }

    private String getQualifiedTypeName(
        SparseList<?> attribute
    ) throws ServiceException{
        Object value = attribute == null ? null : attribute.get(0);
        return value == null ? null : (String) this.model.getElement(value).values("qualifiedName").get(0);
        
    }

    private static String getString(
        SparseList<?> attribute
    ) throws ServiceException{
        return attribute == null ? null : (String)attribute.get(0);
    }

    private static boolean getBoolean(
        SparseList<?> attribute
    ) throws ServiceException{ 
        Boolean value = (Boolean) attribute.get(0);
        return Boolean.TRUE.equals(value);
    }
    
    /**
     * 
     */
    private final ModelElement_1_0 associationEndDef;
    
    /**
     * 
     */
    private final boolean navigable;
    
    /**
     * 
     */
    private final String  multiplicity;

    /**
     * 
     */
    private final String type;

    /**
     * 
     */
    private final String qualifierName;

    /**
     * 
     */
    private final String qualifierType;
    
    /**
     * 
     */
    private final String aggregation;
    
    /**
     * The reference definition is lazily fetched
     */
    private ReferenceDef referenceDef = null;
    
    /**
     * 
     */
    @SuppressWarnings("unused")
    private final Model_1_0 model;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
        return 
        that instanceof AssociationEndDef && 
        this.associationEndDef.equals(((AssociationEndDef)that).associationEndDef);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.associationEndDef.hashCode();
    }

    /**
     * Retrieve associationEndDef.
     *
     * @return Returns the associationEndDef.
     */
    public final ModelElement_1_0 getAssociationEndDef() {
        return this.associationEndDef;
    }


    /**
     * Retrieve the corresponding reference
     *
     * @return the corresponding reference
     * @throws ServiceException 
     */
    public final ReferenceDef getReference() throws ServiceException {        
        if(this.referenceDef == null) {
            Model_1_4 model = this.associationEndDef.getModel();
            for(ModelElement_1_0 element : model.getContent()) {
                if(model.isReferenceType(element)) {
                    if(element.values("referencedEnd").get(0).equals(this.associationEndDef.path())) {
                        this.referenceDef = new ReferenceDef(
                            element,
                            model,
                            false // openmdx1
                        );
                        break;
                    }
                }
            }
        }
        return this.referenceDef;
    }


    /**
     * Retrieve navigable.
     *
     * @return Returns the navigable.
     */
    public final boolean isNavigable() {
        return this.navigable;
    }


    /**
     * Retrieve multiplicity.
     *
     * @return Returns the multiplicity.
     */
    public final String getMultiplicity() {
        return this.multiplicity;    
    }


    /**
     * Retrieve type.
     *
     * @return Returns the type.
     */
    public final String getType() {
        return this.type;
    }


    /**
     * Retrieve aggregation.
     *
     * @return Returns the aggregation.
     */
    public final String getAggregation() {
        return this.aggregation;
    }

    /**
     * Retrieve qualifierName.
     *
     * @return Returns the qualifierName.
     */
    public final String getQualifierName() {
        return this.qualifierName;
    }
    

    /**
     * Retrieve qualifierType.
     *
     * @return Returns the qualifierType.
     */
    public final String getQualifierType() {
        return this.qualifierType;
    }
    

}
