/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Reference Record
 * Description: org::omg::model1::Reference Record
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openmdx.base.marshalling.TypeSafeMarshaller;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.repository.cci.AttributeRecord;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Reference Record
 */
public class ReferenceRecord
  extends StructuralFeatureRecord<org.openmdx.base.mof.repository.cci.ReferenceRecord.Member>
  implements org.openmdx.base.mof.repository.cci.ReferenceRecord
{

    /**
     * Imolements {@code Serializable}
     */
    private static final long serialVersionUID = 1902793355832391232L;
    
    private Path exposedEnd;
    private Path referencedEnd;
    private boolean referencedEndIsNavigable;
    private transient Boolean referenceIsStoredAsAttribute;
    private transient AttributeRecord asAttribute;
    
    /*
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);

    /**
     * Defines which primitive types are numeric ones
     */
    private static final List<String> INDEX_TYPES = Arrays.asList(
        PrimitiveTypes.INTEGER,
        PrimitiveTypes.LONG,
        PrimitiveTypes.SHORT
    );
    
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
             case scope: return getScope();
             case isChangeable: return Boolean.valueOf(isChangeable());
             case visibility: return getVisibility();
             case referencedEndIsNavigable: return Boolean.valueOf(isReferencedEndIsNavigable());
             case type: return getType();
             case exposedEnd: return getExposedEnd();
             case referencedEnd: return getReferencedEnd();
             case multiplicity: return getMultiplicity();
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
                 setModifiedAt((#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif)value);
                 break;
             case createdBy:
                 setCreatedBy((Collection<?>)value);
                 break;
             case stereotype:
                 setStereotype((Collection<?>)value);
                 break;
             case createdAt:
                 setCreatedAt((#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif)value);
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
             case scope:
                 setScope((String) value);
                 break;
             case isChangeable:
                 setChangeable((Boolean) value);
                 break;
             case visibility:
                 setVisibility((String) value);
                 break;
             case referencedEndIsNavigable:
                 setReferencedEndIsNavigable((Boolean) value);
                 break;
             case multiplicity:
                 setMultiplicity((String) value);
                 break;
             case type:
                 setType((Path) value);
                 break;
             case exposedEnd:
                 setExposedEnd((Path) value);
                 break;
             case referencedEnd:
                 setReferencedEnd((Path) value);
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
     * Retrieve exposedEnd.
     *
     * @return Returns the exposedEnd.
     */
    @Override
    public Path getExposedEnd() {
        return this.exposedEnd;
    }

    
    /**
     * Set exposedEnd.
     * 
     * @param exposedEnd The exposedEnd to set.
     */
    protected void setExposedEnd(Path exposedEnd) {
        assertMutability();
        this.exposedEnd = exposedEnd;
    }

    
    /**
     * Retrieve referencedEnd.
     *
     * @return Returns the referencedEnd.
     */
    @Override
    public Path getReferencedEnd() {
        return this.referencedEnd;
    }

    
    /**
     * Set referencedEnd.
     * 
     * @param referencedEnd The referencedEnd to set.
     */
    protected void setReferencedEnd(Path referencedEnd) {
        assertMutability();
        this.referencedEnd = referencedEnd;
    }
    
    /**
     * Retrieve referencedEndIsNavigable.
     *
     * @return Returns the referencedEndIsNavigable.
     */
    @Override
    public boolean isReferencedEndIsNavigable() {
        return this.referencedEndIsNavigable;
    }
        
    /**
     * Set referencedEndIsNavigableIsNavigable.
     * 
     * @param referencedEndIsNavigableIsNavigable The referencedEndIsNavigableIsNavigable to set.
     */
    protected void setReferencedEndIsNavigable(
        Boolean referencedEndIsNavigable
    ) {
        assertMutability();
        this.referencedEndIsNavigable = Boolean.TRUE.equals(referencedEndIsNavigable);
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.ReferenceRecord#asAttribute()
     */
    @Override
    public AttributeRecord asAttribute(
        TypeSafeMarshaller<Path, ElementRecord> marshaller
    ) {
        return isReferenceStoredAsAttribute(marshaller) ? this.asAttribute : null;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.ReferenceRecord#isReferenceStoredAsAttribute(org.openmdx.base.marshalling.TypeSafeMarshaller)
     */
    @Override
    public boolean isReferenceStoredAsAttribute(
        TypeSafeMarshaller<Path, ElementRecord> marshaller
    ){
        if(this.referenceIsStoredAsAttribute == null) {
            final AssociationEndRecord referencedEnd = (AssociationEndRecord) marshaller.marshal(getReferencedEnd());
            final AssociationEndRecord exposedEnd = (AssociationEndRecord) marshaller.marshal(getExposedEnd());
            final List<Path> qualifierTypes = referencedEnd.getQualifierType();
            if(
                AggregationKind.NONE.equals(referencedEnd.getAggregation()) &&
                AggregationKind.NONE.equals(exposedEnd.getAggregation()) 
            ) {
                if(qualifierTypes.isEmpty()){
                    this.asAttribute = new ReferenceStoredAsAttibute(
                        this,
                        isDerived(marshaller), 
                        getMultiplicity()
                    );
                    this.referenceIsStoredAsAttribute = Boolean.TRUE;
                } else if (isListOfReferences(marshaller, qualifierTypes)) {
                    this.asAttribute = new ReferenceStoredAsAttibute(
                        this,
                        isDerived(marshaller), 
                        Multiplicity.LIST.code()
                    );
                    this.referenceIsStoredAsAttribute = Boolean.TRUE;
                } else {
                    this.referenceIsStoredAsAttribute = Boolean.FALSE;
                }
            } else {
                this.referenceIsStoredAsAttribute = Boolean.FALSE;
            }
        }
        return this.referenceIsStoredAsAttribute.booleanValue();
    }

    private boolean isDerived(
        TypeSafeMarshaller<Path, ElementRecord> marshaller
    ){
        final AssociationEndRecord referencedEnd = (AssociationEndRecord) marshaller.marshal(getReferencedEnd());
        final AssociationRecord association = (AssociationRecord) marshaller.marshal(referencedEnd.getContainer());
        return association.isDerived();
    }

    /**
     * TODO support alias types
     */
    private boolean isListOfReferences(
        TypeSafeMarshaller<Path, ElementRecord> marshaller,
        List<Path> qualifierTypes
    ) { 
        if(qualifierTypes.size() == 1) { 
            final ElementRecord dataType = marshaller.marshal(qualifierTypes.get(0));
            return INDEX_TYPES.contains(dataType.getQualifiedName());
        } else {
            return false;
        }
    }
    
    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public ReferenceRecord clone() {
        return prepareClone(new ReferenceRecord());
    }

}
