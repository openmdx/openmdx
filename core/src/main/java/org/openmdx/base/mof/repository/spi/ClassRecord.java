/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::Class Record
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.marshalling.TypeSafeMarshaller;
import org.openmdx.base.mof.repository.cci.AssociationRecord;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.mof.repository.cci.FeatureRecord;
import org.openmdx.base.mof.repository.cci.StructuralFeatureRecord;
import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Class Record
 */
public class ClassRecord
  extends ClassifierRecord<org.openmdx.base.mof.repository.cci.ClassRecord.Member>
  implements org.openmdx.base.mof.repository.cci.ClassRecord
{

    private boolean singleton;

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 7333278087117151396L;
    
    private MappedRecord allFeature;
    private MappedRecord allFeatureWithSubtype;
    private transient Map<Set<FeatureKind>,Map<String,Path>> structuralFeature;
    
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
             case attribute: return attribute();
             case operation: return operation();
             case allFeature: return allFeature();
             case feature: return feature();
             case compositeReference: return getCompositeReference();
             case reference: return reference();
             case field: return field();
             case content: return content();
             case isSingleton: return Boolean.valueOf(isSingleton());
             case allFeatureWithSubtype: return allFeatureWithSubtype();
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
             case isSingleton:
                 setSingleton((Boolean) value);
                 break;
             case attribute:
                 setAttribute((MappedRecord) value);
                 break;
             case operation:
                 setOperation((MappedRecord) value);
                 break;
             case feature:
                 setFeature((Collection<?>) value);
                 break;
             case compositeReference:
                 setCompositeReference((Path) value);
                 break;
             case reference:
                 setReference((MappedRecord) value);
                 break;
             case field:
                 setField((MappedRecord) value);
                 break;
             case content:
                 setContent((Collection<?>) value);
                 break;
             case allFeature:
                 setAllFeature((MappedRecord) value);
                 break;
             case allFeatureWithSubtype:
                 setAllFeatureWithSubtype((MappedRecord) value);
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
     * Retrieve singleton.
     *
     * @return Returns the singleton.
     */
    @Override
    public boolean isSingleton() {
        return this.singleton;
    }
    
    /**
     * Set singleton.
     * 
     * @param singleton The singleton to set.
     */
    protected void setSingleton(Boolean singleton) {
        this.singleton = Boolean.TRUE.equals(singleton);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String,Path> getAllFeature(){
        return allFeature();
    }
    
    protected MappedRecord allFeature(){
        if(this.allFeature == null) {
            this.allFeature = newMap();
        }
        return this.allFeature;
    }
    
    protected void setAllFeature(
        MappedRecord allFeature
    ){
        replaceValues(allFeature(), allFeature);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String,Path> getAllFeatureWithSubtype(){
        return allFeatureWithSubtype();
    }
    
    protected MappedRecord allFeatureWithSubtype(){
        if(this.allFeatureWithSubtype == null) {
            this.allFeatureWithSubtype = newMap();
        }
        return this.allFeatureWithSubtype;
    }
    
    protected void setAllFeatureWithSubtype(
        MappedRecord allFeatureWithSubtype
    ){
        replaceValues(allFeatureWithSubtype(), allFeatureWithSubtype);
    }


    private Map<String,Path> createFeatureMap(
        TypeSafeMarshaller<Path, ElementRecord> marshaller,
        Set<FeatureKind> key
    ){
        final Map<String,Path> structuralFeatures = new HashMap<String,Path>();
        final boolean includeSubtypes = key.contains(FeatureKind.INCLUDE_SUBTYPES);
        final boolean attributesOnly = key.contains(FeatureKind.ATTRIBUTES_ONLY);
        final boolean includeDerived = key.contains(FeatureKind.INCLUDE_DERIVED);
        final Map<String,Path> allFeatures = includeSubtypes ? getAllFeatureWithSubtype() : getAllFeature();
        for(Path featureId : allFeatures.values()){
            final FeatureRecord featureDef = (FeatureRecord) marshaller.marshal(featureId);
            if(
                featureDef instanceof AttributeRecord || 
                (featureDef instanceof ReferenceRecord && (
                    !attributesOnly || ((ReferenceRecord)featureDef).isReferenceStoredAsAttribute(marshaller))
                )          
            ) {
                if(includeDerived || !isDerived(marshaller, (StructuralFeatureRecord)featureDef)) {
                    structuralFeatures.put(
                        featureDef.getName(),
                        featureDef.getObjectId()
                    );
                }
            }
        }          
        return structuralFeatures;
    }
    
    private boolean isDerived(
        TypeSafeMarshaller<Path, ElementRecord> marshaller,
        StructuralFeatureRecord feature
    ) {
        if(feature instanceof AttributeRecord) {
            return ((AttributeRecord)feature).isDerived();
        }
        if(feature instanceof ReferenceRecord) {
            AssociationEndRecord exposedEnd = (AssociationEndRecord) marshaller.marshal(((ReferenceRecord)feature).getExposedEnd());
            AssociationRecord association = (AssociationRecord) marshaller.marshal(exposedEnd.getContainer());
            return association.isDerived();
        }
        return false;
    }
    
    protected Map<Set<FeatureKind>,Map<String,Path>> structuralFeatureMaps(
        TypeSafeMarshaller<Path, ElementRecord> marshaller
    ){
        if(this.structuralFeature == null) {
            final Map<Set<FeatureKind>, Map<String, Path>> structuralFeatureMaps = new HashMap<Set<FeatureKind>,Map<String,Path>>();
            for(Set<FeatureKind> key : FeatureKind.allKeys()) {
                structuralFeatureMaps.put(key, createFeatureMap(marshaller, key));
            }
            this.structuralFeature = structuralFeatureMaps;
        }
        return this.structuralFeature;
    }

    public Map<String, Path> getStructuralFeature(
        TypeSafeMarshaller<Path, ElementRecord> marshaller,
        boolean includeSubtypes,
        boolean includeDerived,
        boolean attributesOnly
    ) {
        return structuralFeatureMaps(marshaller).get(FeatureKind.asKey(includeSubtypes, includeDerived, attributesOnly));
    }
    
    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public ClassRecord clone() {
        return prepareClone(new ClassRecord());
    }

    
    //------------------------------------------------------------------------
    // Enum FeatureKiind
    //------------------------------------------------------------------------
    
    private static enum FeatureKind {
        INCLUDE_SUBTYPES,
        INCLUDE_DERIVED,
        ATTRIBUTES_ONLY;
        
        static Set<FeatureKind> asKey(
            boolean includeSubtypes,
            boolean includeDerived,
            boolean attributesOnly
        ){
            Set<FeatureKind> set = EnumSet.noneOf(FeatureKind.class);
            if(includeSubtypes) {
                set.add(INCLUDE_SUBTYPES);
            }
            if(includeDerived) {
                set.add(INCLUDE_DERIVED);
            }
            if(attributesOnly) {
                set.add(ATTRIBUTES_ONLY);
            }
            return set;
        }

        static Iterable<Set<FeatureKind>> allKeys() {
            return allKeys;
        }
        
        static Iterable<Set<FeatureKind>> allKeys = new Iterable<Set<FeatureKind>>(){

            @Override
            public Iterator<Set<FeatureKind>> iterator() {
                return new Iterator<Set<FeatureKind>>(){

                    int i = 0;
                    
                    @Override
                    public boolean hasNext() {
                        return i < 8;
                    }

                    @Override
                    public Set<FeatureKind> next() {
                        final int bitset = i++;
                        return FeatureKind.asKey(
                            ((bitset & 1) == 1), // includeSubtypes
                            ((bitset & 2) == 2), // includeDerived
                            ((bitset & 4) == 4) // attributesOnly
                        );
                    }

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
                    
                };
            }
            
        };
        
    }

}
