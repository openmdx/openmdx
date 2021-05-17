/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::Attribute Record
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

import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Attribute Record
 */
public class AttributeRecord
  extends StructuralFeatureRecord<org.openmdx.base.mof.repository.cci.AttributeRecord.Member>
  implements org.openmdx.base.mof.repository.cci.AttributeRecord
{

    private boolean derived;
    private int maxLength;
        
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4110656575531913526L;

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
             case isDerived: return Boolean.valueOf(isDerived());             
             case scope: return getScope();
             case isChangeable: return Boolean.valueOf(isChangeable());
             case visibility: return getVisibility();
             case multiplicity: return getMultiplicity();
             case type: return getType();
             case maxLength: return Integer.valueOf(getMaxLength());
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
             case isDerived:
                 setDerived((Boolean) value);
                 break;
             case scope:
                 setScope((String) value);
                 break;
             case isChangeable:
                 setChangeable((Boolean) value);
                 break;
             case visibility:
                 setVisibility((String) value);
                 break;
             case multiplicity:
                 setMultiplicity((String) value);
                 break;
             case type:
                 setType((Path) value);
                 break;
             case maxLength:
                 setMaxLength((Integer) value);
                 break;
            default:
                 super.put(index, value);
         }
     }
         

    @Override
    protected Members<Member> members() {
        return MEMBERS;
    }

    /**
     * Retrieve derived.
     *
     * @return Returns the derived.
     */
    @Override
    public boolean isDerived() {
        return this.derived;
    }
    
    /**
     * Set derived.
     * 
     * @param derived The derived to set.
     */
    protected void setDerived(Boolean derived) {
        assertMutability();
        this.derived = Boolean.TRUE.equals(derived);
    }
    
    /**
     * Retrieve maxLength.
     *
     * @return Returns the maxLength.
     */
    @Override
    public int getMaxLength() {
        return this.maxLength;
    }
    
    /**
     * Set maxLength.
     * 
     * @param maxLength The maxLength to set.
     */
    protected void setMaxLength(Integer maxLength) {
        assertMutability();
        this.maxLength = maxLength == null ? 0 : maxLength.intValue();
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public AttributeRecord clone() {
        return prepareClone(new AttributeRecord());
    }

}
