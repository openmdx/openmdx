/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AssociationEndDef.java,v 1.3 2009/06/09 12:45:18 hburger Exp $
 * Description: VelocityClassDef class
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:18 $
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
package org.openmdx.application.mof.mapping.cci;

import java.util.HashSet;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

public class AssociationEndDef extends ElementDef {

    /**
     * Constructor 
     *
     * @param associationEndDef
     * @param model
     * @throws ServiceException
     */
    public AssociationEndDef(
        ModelElement_1_0 associationEndDef,
        Model_1_0 model
    ) throws ServiceException {
        super( 
            (String)associationEndDef.objGetValue("name"),
            (String)associationEndDef.objGetValue("qualifiedName"),
            (String)associationEndDef.objGetValue("annotation"),
            new HashSet<Object>(associationEndDef.objGetList("stereotype"))
        );      
        this.associationEndDef = associationEndDef;        
        this.model = model;
        this.aggregation = (String)associationEndDef.objGetValue("aggregation");
        this.navigable = (Boolean)associationEndDef.objGetValue("isNavigable");
        this.multiplicity = (String)associationEndDef.objGetValue("multiplicity");
        this.type = this.getQualifiedTypeName(associationEndDef.objGetValue("type"));
        this.qualifierType = this.getQualifiedTypeName(associationEndDef.objGetValue("qualifierType"));
        this.qualifierName = (String)associationEndDef.objGetValue("qualifierName");
    }

    private String getQualifiedTypeName(
        Object type
    ) throws ServiceException{
        return type == null ? 
            null : 
            (String)this.model.getElement(type).objGetValue("qualifiedName");
    }

    private final ModelElement_1_0 associationEndDef;    
    private final boolean navigable;
    private final String  multiplicity;
    private final String type;
    private final String qualifierName;
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
            Model_1_0 model = this.associationEndDef.getModel();
            for(ModelElement_1_0 element : model.getContent()) {
                if(model.isReferenceType(element)) {
                    if(element.objGetValue("referencedEnd").equals(this.associationEndDef.jdoGetObjectId())) {
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
