/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AliasType Record
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
package org.openmdx.base.mof.repository.spi;

import java.util.Collection;
import java.util.Date;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::AliasType Record
 */
public class AliasTypeRecord
  extends ClassifierRecord<org.openmdx.base.mof.repository.cci.AliasTypeRecord.Member>
  implements org.openmdx.base.mof.repository.cci.AliasTypeRecord
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6458160532979232117L;
    
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
             case supertype: return supertypes();
             case allSupertype: return allSupertypes();
             case subtype: return subtypes();
             case allSubtype: return allSubtypes();
             case visibility: return getVisibility();
             case isAbstract: return Boolean.valueOf(isAbstract());
             case feature: return feature();
             case content: return content();
             case compositeReference: return getCompositeReference();
             case attribute: return attribute();
             case type: return getType(); 
             case reference: return reference();
             case field: return field(); 
             case operation: return operation();
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
             case visibility:
                 setVisibility((String)value);
                 break;
             case supertype:
                 setSupertypes((Collection<?>)value);
                 break;
             case allSupertype:
                 setAllSupertypes((Collection<?>)value);
                 break;
             case subtype:
                 setSubtypes((Collection<?>)value);
                 break;
             case allSubtype:
                 setAllSubtypes((Collection<?>)value);
                 break;
             case isAbstract:
                 setAbstract((Boolean)value);
                 break;                 
             case content:
                 setContent((Collection<?>)value);
                 break;
             case feature:
                 setFeature((Collection<?>)value);
                 break;
             case compositeReference:
                 setCompositeReference((Path) value);
                 break;
             case attribute:
                 setAttribute((MappedRecord) value);
                 break;
             case type:
                 setType((Path) value);
                 break;
             case reference:
                 setReference((MappedRecord) value);
                 break;
             case field:
                 setField((MappedRecord) value);
                 break;
             case operation:
                 setOperation((MappedRecord) value);
                 break;
            default:
                 super.put(index, value);
         }
     }

    @Override
    protected Members<Member> members() {
        return MEMBERS;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public AliasTypeRecord clone() {
        return prepareClone(new AliasTypeRecord());
    }
    
}
