/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::AssociationEnd Record
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.mof.repository.spi;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.resource.cci.IndexedRecord;

import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::AssociationEnd Record
 */
public class AssociationEndRecord
  extends TypedElementRecord<org.openmdx.base.mof.repository.cci.AssociationEndRecord.Member>
  implements org.openmdx.base.mof.repository.cci.AssociationEndRecord
{

    private IndexedRecord qualifierType;
    private IndexedRecord qualifierName;
    private boolean changeable;
    private boolean navigable;
    private String multiplicity;
    private String aggregation;
    
    /**
     * Implements <code>Serilaizable</code>
     */
    private static final long serialVersionUID = -2748803551515336911L;
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);
    
    @Override
    public String getRecordName() {
        return NAME;
    }

    /**
     * Retrieve a value by index
     *
     * @param index the index
     * @return the value
     */
     @Override
     protected Object get(
         Member index
){
         switch(index) {
             case annotation: return getAnnotation();
             case identity: return getIdentity();
             case modifiedAt: return getModifiedAt();
             case createdBy: return createdBy();
             case stereotype: return stereotype(); 
             case createdAt: return getCreatedAt(); 
             case modifiedBy: return modifiedBy(); 
             case container: return getContainer(); 
             case name: return getName(); 
             case qualifiedName: return getQualifiedName();
             case type: return getType();
             case qualifierType: return qualifierType(); 
             case qualifierName: return qualifierName();
             case isChangeable: return Boolean.valueOf(isChangeable()); 
             case isNavigable: return Boolean.valueOf(isNavigable());
             case multiplicity: return getMultiplicity();
             case aggregation: return getAggregation();
             default: return super.get(index);
         }
     }

    /**
     * Retrieve a value by index
     * 
     * @param index the index
     * @param value the new value
     * 
     * @return the old value
     */
     @Override
     protected void put(
         Member index,
         Object value
     ){
         assertMutability();
         switch(index) {
             case annotation:
                 setAnnotation((String)value);
                 break;
             case identity:
                 setIdentity(value);
                 break;
             case modifiedAt:
                 setModifiedAt((Date)value);
                 break;
             case createdBy:
                 setCreatedBy((Collection<?>)value);
                 break;
             case stereotype:
                 setStereotype((Collection<?>)value);
                 break;
             case createdAt:
                 setCreatedAt((Date)value);
                 break;
             case modifiedBy:
                 setModifiedBy((Collection<?>)value);
                 break;
             case container:
                 setContainer((Path)value);
                 break;
             case name:
                 setName((String)value);
                 break;
             case qualifiedName:
                 setQualifiedName((String)value);
                 break;
             case type:
                 setType((Path) value);
                 break;
             case qualifierType:
                 setQualifierType((Collection<?>) value);
                 break;
             case qualifierName:
                 setQualifierName((Collection<?>) value);
                 break;
             case isChangeable:
                 setChangeable((Boolean) value);
                 break;
             case isNavigable:
                 setNavigable((Boolean) value);
                 break;
             case multiplicity:
                 setMultiplicity((String) value);
                 break;
             case aggregation:
                 setAggregation((String) value);
                 break;
            default:
                 super.put(index, value);
         }
     }
         
    @Override
    protected Members<Member> members() {
        return MEMBERS;
    }
       
    protected IndexedRecord qualifierType(){
        if(this.qualifierType == null) {
            this.qualifierType = newList();
        }
        return this.qualifierType;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Path> getQualifierType(){
        return qualifierType();
    }
    
    protected void setQualifierType(
        Collection<?> qualifierType
    ){
        replaceValues(qualifierType(), qualifierType);
    }

    protected IndexedRecord qualifierName(){
        if(this.qualifierName == null) {
            this.qualifierName = newList();
        }
        return this.qualifierName;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getQualifierName(){
        return qualifierName();
    }
    
    protected void setQualifierName(
        Collection<?> qualifierName
    ){
        replaceValues(qualifierName(), qualifierName);
    }
    
    /**
     * Retrieve changeable.
     *
     * @return Returns the changeable.
     */
    @Override
    public  boolean isChangeable() {
        return this.changeable;
    }

    
    /**
     * Set changeable.
     * 
     * @param changeable The changeable to set.
     */
    protected void setChangeable(Boolean changeable) {
        assertMutability();
        this.changeable = Boolean.TRUE.equals(changeable);
    }

    /**
     * Retrieve navigable.
     *
     * @return Returns the navigable.
     */
    @Override
    public boolean isNavigable() {
        return this.navigable;
    }
    
    /**
     * Set navigable.
     * 
     * @param navigable The navigable to set.
     */
    protected void setNavigable(Boolean navigable) {
        assertMutability();
        this.navigable = Boolean.TRUE.equals(navigable);
    }
   
    /**
     * Retrieve multiplicity.
     *
     * @return Returns the multiplicity.
     */
    @Override
    public String getMultiplicity() {
        return this.multiplicity;
    }

    
    /**
     * Set multiplicity.
     * 
     * @param multiplicity The multiplicity to set.
     */
    protected void setMultiplicity(String multiplicity) {
        assertMutability();
        this.multiplicity = internalize(multiplicity);
    }

    
    /**
     * Retrieve aggregation.
     *
     * @return Returns the aggregation.
     */
    @Override
    public String getAggregation() {
        return this.aggregation;
    }

    /**
     * Set aggregation.
     * 
     * @param aggregation The aggregation to set.
     */
    protected void setAggregation(String aggregation) {
        assertMutability();
        this.aggregation = internalize(aggregation);
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public AssociationEndRecord clone() {
        return prepareClone(new AssociationEndRecord());
    }

}