/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: MOF repository accessor
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.cci.MappedRecord;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.cci.Stereotypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.LargeObject;
import org.w3c.spi2.Datatypes;

/**
 * Helper class to access MOF repository. The class adds utility 
 * functions and provides a cache for fast model element access.
 */
public class Model_1 implements Marshaller, Model_1_0 {

    /**
     * Constructor 
     *
     * @param modelElements
     * @param associationDefs
     */
    Model_1(
        Map<String,ModelElement_1_0> modelElements,
        Map<String,List<AssociationDef>> associationDefs
    ){
        this.modelElements = modelElements;
        this.associationDefMap = associationDefs;
    }

    /**
     * The repository's eagerly populated content
     */
    private final Map<String,ModelElement_1_0> modelElements;
    
    /**
     * The eagerly populated association definitions
     */
    private final Map<String,List<AssociationDef>> associationDefMap;
    
    /**
     * The lazily populated structural feature definitions
     */
    private final ConcurrentMap<Path,Map<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>>> structuralFeatureDefMap = new ConcurrentHashMap<Path,Map<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>>>();
    
    /**
     * The lazily populated shared associations
     */
    private final ConcurrentMap<Path,Boolean> sharedAssociations = new ConcurrentHashMap<Path,Boolean>();
    
    /**
     * To populate the root association definition
     */
    private static final String AUTHORITY_TYPE_NAME = "org:openmdx:base:Authority";
    
    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    @Override
    public Object marshal(
        Object source
    ) throws ServiceException {
        return source instanceof Path ? this.modelElements.get(((Path)source).getLastSegment().toClassicRepresentation()) : source;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return source instanceof ModelElement_1_0 ? 
            ((ModelElement_1_0)source).jdoGetObjectId() :
            source;
    }

    //---------------------------------------------------------------------------
    /**
     * Returns the AssociationDefs matching the object path. result[0] contains
     * the AssociationDefs corresponding to the last and second last reference 
     * elements of the path. result[1] is a list containing 1..n AssociationDefs
     * depending on whether the reference qualifies uniquely or not.
     */ 
    private AssociationDef[] getAssociationDefs(
        Path objectPath
    ) throws ServiceException {
        //
        // Iterate all reference names and follow the ReferenceLinks
        //     
        AssociationDef prev = null;
        // start from root association The association to the authority
        // is not modeled and is created is virtual association.
        AssociationDef current = new AssociationDef(
            null,
            this.getDereferencedType(AUTHORITY_TYPE_NAME),
            this.getDereferencedType(AUTHORITY_TYPE_NAME + ":provider"),
            this.modelElements
        );
        for(
            int i = 1; 
            i < objectPath.size(); 
            i+=2
        ) {
            String referenceName = objectPath.getSegment(i).toClassicRepresentation();
            // get candidate association definitions
            List<AssociationDef> candidates = this.associationDefMap.get(referenceName);
            if(candidates == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "unknown reference in path.",
                    new BasicException.Parameter("path", objectPath),
                    new BasicException.Parameter("reference", referenceName)
                );
            }    
            // get next associations
            List<AssociationDef> next = new ArrayList<AssociationDef>();
            for(AssociationDef associationDef : candidates){
                String exposedEndQualifiedName = associationDef.getExposedType().getQualifiedName();
                // Test whether one of the current referenced types matches the exposed type of 
                // the next candidate association
                if(current.getAllReferencedTypes().contains(exposedEndQualifiedName)) {
                    // Move prev forward only if not a root class is referenced. Referenced
                    // to root classes are interpreted as references to the concrete subclass, 
                    // e.g. org::openmdx::state2
                    if(
                        !current.getReferencedType().objGetList("stereotype").contains(Stereotypes.ROOT) || 
                        !current.getReferencedType().isAbstract().booleanValue()
                    ) {
                        prev = current;
                    }
                    // prev is now the 'proper' current association. Add to the set of 
                    // next associations only if one of the referenced types matches the exposed
                    // type of the next candidate
                    if(prev.getAllReferencedTypes().contains(exposedEndQualifiedName)) {
                        next.add(associationDef);
                    }
                }
            }      
            // No matching assocation found
            if(next.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "invalid reference. no matching association found",
                    new BasicException.Parameter("path", objectPath),
                    new BasicException.Parameter("reference/operation", referenceName),
                    new BasicException.Parameter("exposing class", current.getExposedType() == null ? null : current.getExposedType().getQualifiedName())
                );
            }

            // the assocationDef with matching authority wins. This case occurs
            // e.g. when a path can matches to different models
            else if(next.size() > 1) {
                int matching = 0;
                int index = 0;
                int jj = 0;
                for(AssociationDef assocationDef : next){
                    // model name (segment name) must match the path authority
                    String modelName = assocationDef.getReference().jdoGetObjectId().getSegment(4).toClassicRepresentation();
                    String authorityName = objectPath.getSegment(0).toClassicRepresentation();
                    if(modelName.equals(authorityName)) {
                        matching++;
                        index = jj; 
                    }
                    jj++;
                }
                if(matching != 1) {        
                    List<String> matches = new ArrayList<String>();
                    for(AssociationDef associationDef : next){
                        Path referencePath = associationDef.getReference().jdoGetObjectId(); 
                        if(referencePath.getSegment(4).toClassicRepresentation().equals(objectPath.getSegment(0).toClassicRepresentation())) {
                            matches.add(referencePath.getSegment(6).toClassicRepresentation());
                        }
                    }
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "invalid reference. #matching referenced classifiers must be 1",
                        new BasicException.Parameter("path", objectPath),
                        new BasicException.Parameter("reference/operation", referenceName),
                        new BasicException.Parameter("exposing class", current.getExposedType() == null ? null : current.getExposedType().getQualifiedName()),
                        new BasicException.Parameter("#matching referenced classifiers", matching),
                        new BasicException.Parameter("matching references", matches)
                    );
                }
                current = next.get(index);
            }

            // exact match
            else {
                current = next.get(0);
            }      
        }      
        return new AssociationDef[]{
            prev,
            current 
        };
    }

    //---------------------------------------------------------------------------
    private void verifyObjectCollection(
        Object values,
        Object type,
        Multiplicity multiplicity,
        boolean enforceRequired,
        Stack<List<?>> validationContext, 
        boolean attributesOnly, 
        boolean verifyDerived
    ) throws ServiceException {
        final int size;
        if(values instanceof Collection<?>){
            size = ((Collection<?>)values).size();
        } else if (values instanceof Map<?,?>){
            size = ((Map<?,?>)values).size();
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE, 
            "values expected to be in [Collectin|Map]",
            new BasicException.Parameter("multiplicity", multiplicity),
            new BasicException.Parameter("values", values),
            new BasicException.Parameter("values class", values == null ? "null" : values.getClass().getName()),
            new BasicException.Parameter("context", validationContext)
        );      
        // Verify multiplicity
        if(Multiplicity.OPTIONAL == multiplicity || Multiplicity.SINGLE_VALUE == multiplicity && size > 1) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "number of values exceeds multiplicity",
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("values", values),
                new BasicException.Parameter("values class", values == null ? "null" : values.getClass().getName()),
                new BasicException.Parameter("context", validationContext)
            );      
        }

        // verify stream muliplicity
        if(Multiplicity.STREAM == multiplicity && size > 2) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "stream values restricted to stream and optional length",
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("values class", values == null ? null : values.getClass().getName()),
                new BasicException.Parameter("context", validationContext)
            );      
        }
        // Verify collection and multiplicity type.
        // MULTI_VALUE is valid for openMDX compatibility
        if(
            (Multiplicity.SET == multiplicity && !(values instanceof Collection<?>)) ||
            (Multiplicity.LIST == multiplicity && !(values instanceof List<?>)) ||
            (Multiplicity.SPARSEARRAY == multiplicity && !(values instanceof SortedMap<?,?>)) 
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "multiplicity does not match value type",
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("values", values),
                new BasicException.Parameter("values class", values == null ? "null" : values.getClass().getName()),
                new BasicException.Parameter("context", validationContext)
            );
        }
        // validate all values of collection
        validationContext.push(
            Arrays.asList("validated values", values)
        );
        int index = 0;
        for(Object value : values instanceof Collection<?> ? (Collection<?>)values : ((Map<?,?>)values).values()){
            validationContext.push(
                Arrays.asList("index", Integer.valueOf(index++), "value", value)
            );
            this.verifyObject(
                value,
                type,
                multiplicity,
                enforceRequired,
                validationContext, 
                attributesOnly, 
                verifyDerived
            );
            validationContext.pop();
        }
        validationContext.pop();      
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void verifyObject(
        Object value,
        Object type,
        Multiplicity multiplicity,
        boolean enforceRequired,
        Stack<List<?>> validationContext, 
        boolean attributesOnly, 
        boolean verifyDerived
    ) throws ServiceException {
        // null --> no validation
        if((type == null) || (value == null)) {
            return;
        }
        ModelElement_1_0 typeDef = this.getDereferencedType(type);
        String typeName = typeDef.getQualifiedName(); 
        // Collection
        if(value instanceof Collection || value instanceof SortedMap) {
            this.verifyObjectCollection(
                value,
                typeDef,
                multiplicity,
                enforceRequired,
                validationContext, 
                attributesOnly, 
                verifyDerived
            );
        }
        // PrimitiveType
        else if(this.isPrimitiveType(type)) {
            if(
                (value instanceof Boolean) && 
                PrimitiveTypes.BOOLEAN.equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof String) && 
                (PrimitiveTypes.STRING.equals(typeName) || PrimitiveTypes.ANYURI.equals(typeName))
            ) {
                return;
            }
            else if(
                (value instanceof Number) && 
                (PrimitiveTypes.DECIMAL.equals(typeName) || PrimitiveTypes.LONG.equals(typeName) || PrimitiveTypes.SHORT.equals(typeName) || PrimitiveTypes.INTEGER.equals(typeName))
            ) {
                return;
            }
            else if(
                (value instanceof Date) && 
                PrimitiveTypes.DATETIME.equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof String) && 
                PrimitiveTypes.DATETIME.equals(typeName)
            ) {
                try {
                    Datatypes.create(Date.class, (String)value);
                } catch(IllegalArgumentException e) {
                    throw new ServiceException(e);
                }
                return;
            }
            else if(
                (value instanceof XMLGregorianCalendar) && 
                PrimitiveTypes.DATE.equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof String) && 
                PrimitiveTypes.DATE.equals(typeName)
            ) {
                try {
                    Datatypes.create(XMLGregorianCalendar.class, (String)value);
                } catch(IllegalArgumentException e) {
                    throw new ServiceException(e);
                }
                return;
            }
            else if(
                (value instanceof Duration) && 
                PrimitiveTypes.DURATION.equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof String) && 
                PrimitiveTypes.DURATION.equals(typeName)
            ) {
                try {
                    Datatypes.create(Duration.class, (String)value);
                } catch(IllegalArgumentException e) {
                    throw new ServiceException(e);
                }
                return;
            }
            else if(
                (value instanceof byte[] || value instanceof LargeObject) && 
                PrimitiveTypes.BINARY.equals(typeName)
            ) {
                return;
            }
            else if(
                (value instanceof Reader || value instanceof Long) && 
                PrimitiveTypes.STRING.equals(typeName) &&
                Multiplicity.STREAM == multiplicity
            ) {
                return;
            }
            else if(
                (value instanceof InputStream || value instanceof LargeObject || value instanceof Long) && 
                PrimitiveTypes.BINARY.equals(typeName) &&
                Multiplicity.STREAM == multiplicity
            ) {
                return;
            }
            else if(
                (value instanceof Path) && 
                (PrimitiveTypes.OBJECT_ID.equals(typeName) || this.isClassType(type))
            ) {
                if(!((Path)value).isObjectPath()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "not an object path. path.size() % 2 == 1",
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("context", validationContext)
                    );
                }
                return;
            }
            else if(
                (value instanceof String) && 
                (PrimitiveTypes.OBJECT_ID.equals(typeName) || this.isClassType(type))
            ) {
                Path p = new Path((String)value);
                if(!p.isObjectPath()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "not an object path. path.size() % 2 == 1",
                        new BasicException.Parameter("value", value),
                        new BasicException.Parameter("context", validationContext)
                    );
                }
                return;
            }
            // Unknown primitive type. Assume it is a string
            else if(value instanceof String) {
                return;
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "type mismatch or type not supported",
                    new BasicException.Parameter("value", value),
                    new BasicException.Parameter("value class", (value == null) ? "null" : value.getClass().getName()),
                    new BasicException.Parameter("type", typeName),
                    new BasicException.Parameter("context", validationContext)
                );
            }
        }
        // StructureType
        else if(this.isStructureType(type)) {
            Set<String> fieldNames;
            if(value instanceof MappedRecord) {
                Object_2Facade valueFacade = Facades.asObject((MappedRecord)value);
                fieldNames = new TreeSet<String>(valueFacade.getValue().keySet());
                // remove empty fields
                for(Iterator<String> i = fieldNames.iterator(); i.hasNext(); ) {
                    if(valueFacade.getAttributeValues(i.next()) == null) {
                        i.remove();
                    } 
                }
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "value type not supported. Allowed are [Structure_1_0|DataproviderObject_1_0]",
                    new BasicException.Parameter("value", value),
                    new BasicException.Parameter("value class", (value == null) ? "null" : value.getClass().getName()),
                    new BasicException.Parameter("context", validationContext)
                );        
            }       
            @SuppressWarnings("cast")
            Map<String,ModelElement_1_0> fieldDefs = (Map<String,ModelElement_1_0>)typeDef.objGetMap("field");
            // complete fieldNames with all required fields in case of includeRequired
            if((fieldDefs != null) && enforceRequired) {
                for(ModelElement_1_0 fieldDef : fieldDefs.values()){
                    if(Multiplicity.SINGLE_VALUE.code().equals(fieldDef.getMultiplicity())) {
                        fieldNames.add(
                            fieldDef.getName()
                        );
                    }
                }
            }
            // validate all fields contained in value
            for(String fieldName: fieldNames){
                // object_class for openMDX/2 compatibility
                if(
                    !fieldName.equals(SystemAttributes.OBJECT_CLASS) &&
                    !fieldName.equals(SystemAttributes.OBJECT_INSTANCE_OF)
                ) {
                    ModelElement_1_0 featureDef = fieldDefs.get(fieldName);        
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "attribute not member of class",
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("field", fieldName),
                            new BasicException.Parameter("structure type", typeDef.getQualifiedName()),
                            new BasicException.Parameter("context", validationContext)
                        );
                    }
                    else {
                        Object fieldValue = value instanceof Structure_1_0 ? 
                            ((Structure_1_0)value).objGetValue(fieldName) : 
                            Facades.asObject((MappedRecord)value).getAttributeValues(fieldName);
                        if(enforceRequired && (fieldValue == null)) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE, 
                                "field value is null. Either missing required field or value contains field with null value",
                                new BasicException.Parameter("value", value),
                                new BasicException.Parameter("field", fieldName),
                                new BasicException.Parameter("context", validationContext)
                            );                           
                        }
                        validationContext.push(
                            Arrays.asList("validated field", fieldName)
                        );
                        this.verifyObject(
                            fieldValue,
                            featureDef.getType(),
                            org.openmdx.base.mof.cci.ModelHelper.toMultiplicity(featureDef.getMultiplicity()),
                            enforceRequired,
                            validationContext, 
                            attributesOnly, 
                            verifyDerived
                        );
                        validationContext.pop();
                    }
                }       
            }
        }

        // Class
        else if(this.isClassType(type)) {
            Set<String> attributeNames = null;
            // validateObject does not support deep verify. Referenced objects
            // are not validated.
            if(value instanceof Path) {
                return;
            }
            else if(value instanceof DataObject_1_0) {
                attributeNames = new TreeSet<String>(((DataObject_1_0)value).objDefaultFetchGroup());
            }
            else if(value instanceof MappedRecord) {
                Object_2Facade valueFacade = Facades.asObject((MappedRecord)value);
                attributeNames = new TreeSet<String>(valueFacade.getValue().keySet());
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "value type not supported. Allowed are [Path|Object_1_0|DataproviderObject_1_0]",
                    new BasicException.Parameter("value", value),
                    new BasicException.Parameter("value class", value.getClass().getName()),
                    new BasicException.Parameter("context", validationContext)
                );        
            }
            Map<String,ModelElement_1_0> structuralFeatureDefs = this.getStructuralFeatureDefs(
                typeDef, 
                false, // includeSubtypes
                true, // includeDerived, 
                attributesOnly
            );
            // Complete attributeNames with all required fields in case of enforceRequired
            if(enforceRequired) {
                for(ModelElement_1_0 fieldDef : structuralFeatureDefs.values()){
                    if(
                        Multiplicity.SINGLE_VALUE.code().equals(fieldDef.getMultiplicity()) && 
                        (verifyDerived || !Boolean.TRUE.equals(fieldDef.isDerived()))
                    ) {
                        attributeNames.add(
                            fieldDef.getName()
                        );
                    }
                }
            }
            // Validate all attributes contained in value
            for(String attributeName : attributeNames){ 
                // object_class and object_instanceof for openMDX/2 compatibility
                // at the current time ignore namespaces
                if(
                    !attributeName.equals(SystemAttributes.OBJECT_CLASS) &&
                    !attributeName.equals(SystemAttributes.OBJECT_INSTANCE_OF) &&
                    (attributeName.indexOf(':') < 0)
                ) {
                    ModelElement_1_0 featureDef = structuralFeatureDefs.get(attributeName); 
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE, 
                            "attribute not member of class",
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("attribute", attributeName),
                            new BasicException.Parameter("object class", typeDef.getQualifiedName()),
                            new BasicException.Parameter("context", validationContext)
                        );
                    }
                    Object featureMultiplicity = featureDef.getMultiplicity();
                    Object attributeValue;
                    if (value instanceof DataObject_1_0) {
                        DataObject_1_0 object = (DataObject_1_0)value;
                        if(
                            Multiplicity.LIST.code().equals(featureMultiplicity) ||
                            org.openmdx.base.mof.cci.ModelHelper.UNBOUND.equals(featureMultiplicity) || (
                                this.isReferenceType(featureDef) &&
                                this.referenceIsStoredAsAttribute(featureDef) &&
                                Multiplicity.OPTIONAL.code().equals(featureMultiplicity)
                            )                      
                        ) {
                            attributeValue = object.objGetList(attributeName);
                        } 
                        else if(Multiplicity.SET.code().equals(featureMultiplicity)) {
                            attributeValue = object.objGetSet(attributeName);
                        } 
                        else if(Multiplicity.SPARSEARRAY.code().equals(featureMultiplicity)) {
                            attributeValue = object.objGetSparseArray(attributeName);
                        } 
                        else {
                            attributeValue = object.objGetValue(attributeName);                      
                            if(
                                enforceRequired && 
                                Multiplicity.SINGLE_VALUE.code().equals(featureMultiplicity) &&
                                attributeValue == null
                            ) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.VALIDATION_FAILURE, 
                                    "Attribute value is null. Required attribute is either missing or contains the null value",
                                    new BasicException.Parameter("value", value),
                                    new BasicException.Parameter("attribute", attributeName),
                                    new BasicException.Parameter("multiplicity", featureMultiplicity),
                                    new BasicException.Parameter("context", validationContext)
                                );
                            }
                        }
                    } 
                    else if (value instanceof MappedRecord) {
                        Object genericValue = Facades.asObject((MappedRecord)value).getAttributeValues(attributeName);
                        attributeValue = genericValue; 
                        if(
                            enforceRequired && 
                            Multiplicity.SINGLE_VALUE.code().equals(featureMultiplicity) &&
                            (attributeValue == null || ((List<?>)genericValue).isEmpty())
                        ) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.VALIDATION_FAILURE, 
                                "Attribute value is null. Required attribute is either missing or contains the null value",
                                new BasicException.Parameter("value", value),
                                new BasicException.Parameter("attribute", attributeName),
                                new BasicException.Parameter("multiplicity", featureMultiplicity),
                                new BasicException.Parameter("context", validationContext)
                            );
                        }
                    } 
                    else {
                        attributeValue = null;
                    }
                    validationContext.push(
                        Arrays.asList("validated attribute", attributeName)
                    ); 
                    // in case the feature is a reference stored as attribute check for qualifiers
                    String attributeMultiplicity = featureDef.getMultiplicity();
                    if(this.isReferenceType(featureDef)) {
                        ModelElement_1_0 referencedEnd = getElement(
                            featureDef.getReferencedEnd()
                        );
                        if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                            attributeMultiplicity = org.openmdx.base.mof.cci.ModelHelper.UNBOUND;
                        }
                    }
                    this.verifyObject(
                        attributeValue,
                        featureDef.getType(),
                        org.openmdx.base.mof.cci.ModelHelper.toMultiplicity(attributeMultiplicity),
                        enforceRequired,
                        validationContext, 
                        attributesOnly, 
                        verifyDerived
                    );
                    validationContext.pop();
                }
            }
        }
        // Unsupported type
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "unsupported model element. Must be [PrimitiveType|Class|StructureType]",
                new BasicException.Parameter("type", type),
                new BasicException.Parameter("value", value),
                new BasicException.Parameter("context", validationContext)
            );            
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public ModelElement_1_0 getElement(
        java.lang.Object element
    ) throws ServiceException {
        ModelElement_1_0 e = ModelHelper.findElement(
            element,
            this.modelElements
        ); 
        if(e == null) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_FOUND, 
                "element not found in model package",
                new BasicException.Parameter("element", element)
            );
        }
        return e;
    }

    //-------------------------------------------------------------------------
    @Override
    public ModelElement_1_0 findElement(
        Object element
    ) {
        return ModelHelper.findElement(
            element,
            this.modelElements
        );
    }

    //-------------------------------------------------------------------------
    @Override
    public Collection<ModelElement_1_0> getContent(
    ) throws ServiceException {
        return this.modelElements.values();
    }  

    //-------------------------------------------------------------------------
    @Override
    public boolean isLocal(
        Object type,
        Object modelPackage
    ) throws ServiceException {
        String packageNamePackage = this.toJavaPackageName(modelPackage, "-");
        String packageNameType = this.toJavaPackageName(type, "-");
        return packageNamePackage.equals(packageNameType);
    }

    //---------------------------------------------------------------------------
    /**
     * Tells whether the given XRI contains a shared association
     * 
     * @param xri the XRI to be analyzed
     * 
     * @return <code>true</code> if the given XRI contains a shared association 
     * @throws ServiceException 
     */
    @Override
    public boolean containsSharedAssociation(
        Path xri
    ) throws ServiceException{
        int size = xri.size();
        if(size <= 5) {
            return false;
        } else {
        	String[] k = new String[size / 2 + 1];
        	k[0] = xri.getSegment(0).toClassicRepresentation();
            for(int i = 1, j = 1; i < size; i+=2){
            	k[j++] = xri.getSegment(i).toClassicRepresentation();
            }
            Path key = new Path(k);
            Boolean sharedAssociation = this.sharedAssociations.get(key);
            if(sharedAssociation == null) {
                Boolean parentIsShared = this.sharedAssociations.get(key.getParent());
                if(parentIsShared == null ? containsSharedAssociation(xri.getPrefix(size - 2)) : parentIsShared.booleanValue()) {
                    sharedAssociation = Boolean.TRUE;
                } else {
                    Path reference = size % 2 == 0 ? xri : xri.getParent();
                    ModelElement_1_0 referenceType = this.getReferenceType(reference);
                    ModelElement_1_0 referencedEnd = this.getElement(referenceType.getReferencedEnd());
                    sharedAssociation = Boolean.valueOf("shared".equals(referencedEnd.getAggregation()));
                }
                this.sharedAssociations.put(key, sharedAssociation);
            }
            return sharedAssociation.booleanValue();
        }
    }
    
    //---------------------------------------------------------------------------
    @Override
    public ModelElement_1_0 getFeatureDef(
        ModelElement_1_0 classifierDef,
        String feature,
        boolean includeSubtypes
    ) throws ServiceException {
        if(this.isStructureType(classifierDef)) {
            //
            // Structure
            //
            return (ModelElement_1_0)classifierDef.objGetMap("field").get(feature);
        } else {
            //
            // Class
            //
            if(includeSubtypes) {
                //
                // references stored as attributes are in maps allReference and allAttribute. 
                // give allReference priority in case feature is a reference
                //
                return (ModelElement_1_0)classifierDef.objGetMap("allFeatureWithSubtype").get(feature);
            } else {
                //
                // references stored as attributes are in maps allReference and allAttribute. 
                // give allReference priority in case feature is a reference
                //
                return (ModelElement_1_0)classifierDef.objGetMap("allFeature").get(feature);
            }
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Return the set of attributes and references of the specified class, 
     * and if specified its subtypes.
     *  
     * @param classDef class to get feature of.  
     * @param includeSubtypes if true, in addition returns the features
     *         of the subtypes of class.
     * @param includeDerived if false, only non-derived attributes are returned.
     *         if true, derived and non-derived attributes are returned.
     * @return Map map of features of class, its supertypes and subtypes. The
     *          map contains an entry of the form (featureName, featureDef).
     */
    @Override
    public Map<String,ModelElement_1_0> getAttributeDefs(
        ModelElement_1_0 classDef,
        boolean includeSubtypes,
        boolean includeDerived
    ) throws ServiceException {
        return getStructuralFeatureDefs(
            classDef, 
            includeSubtypes, 
            includeDerived, 
            true // attributesOnly
        );
    }

    /**
     * Return the set of attributes and references of the specified class, 
     * and if specified its subtypes.
     *  
     * @param classDef class to get feature of.  
     * @param includeSubtypes if true, in addition returns the features
     *         of the subtypes of class.
     * @param includeDerived if false, only non-derived attributes are returned.
     *         if true, derived and non-derived attributes are returned.
     * @param attributesOnly 
     *         if true return the same result as getAttributeDefs;
     *         if false include references not stored as attributes
     * @return Map map of features of class, its supertypes and subtypes. The
     *          map contains an entry of the form (featureName, featureDef).
     */
    @SuppressWarnings({
        "cast", "rawtypes", "unchecked"
    })
    @Override
    public Map<String,ModelElement_1_0> getStructuralFeatureDefs(
        ModelElement_1_0 classDef,
        boolean includeSubtypes,
        boolean includeDerived,
        boolean attributesOnly
    ) throws ServiceException {
        Map<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>> allStructuralFeatureDefs = this.structuralFeatureDefMap.get(classDef.jdoGetObjectId());
        if(allStructuralFeatureDefs == null) {
            allStructuralFeatureDefs = new HashMap<Boolean,Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>>();
            for(int ii = 0; ii < 2; ii++) {
                boolean bIncludeSubtypes = ii == 0; 
                Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>> structuralFeatureDefsIncludeSubtypes = new HashMap<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>>();
                allStructuralFeatureDefs.put(
                    Boolean.valueOf(bIncludeSubtypes),
                    structuralFeatureDefsIncludeSubtypes
                );
                for(int jj = 0; jj < 2; jj++) {
                    boolean bIncludeDerived = jj == 0;
                    Map<Boolean,Map<String,ModelElement_1_0>> structuralFeatureDefsIncludeDerived = new HashMap<Boolean,Map<String,ModelElement_1_0>>();
                    structuralFeatureDefsIncludeSubtypes.put(
                        Boolean.valueOf(bIncludeDerived),
                        structuralFeatureDefsIncludeDerived
                    );
                    for(int kk = 0; kk < 2; kk++) {
                        boolean bAttributesOnly = kk == 0; 
                        Map<String,ModelElement_1_0> featureDefs = classDef.objGetMap(
                            bIncludeSubtypes ? "allFeatureWithSubtype" : "allFeature"
                        );
                        Map<String,ModelElement_1_0> structuralFeatureDefs = new HashMap<String,ModelElement_1_0>();
                        for(ModelElement_1_0 featureDef : featureDefs.values()){
                            if(
                                this.isAttributeType(featureDef) || 
                                (this.isReferenceType(featureDef) && (!bAttributesOnly || this.referenceIsStoredAsAttribute(featureDef)))          
                            ) {
                                Boolean isDerived = (Boolean)featureDef.isDerived();
                                if(bIncludeDerived || (isDerived == null) || !isDerived.booleanValue()) {
                                    structuralFeatureDefs.put(
                                        (String)featureDef.getName(),
                                        featureDef
                                    );
                                }
                            }
                        }          
                        structuralFeatureDefsIncludeDerived.put(
                            Boolean.valueOf(bAttributesOnly),
                            structuralFeatureDefs
                        );
                    }
                }
            }
            allStructuralFeatureDefs = Maps.putUnlessPresent(
                this.structuralFeatureDefMap,
                (Path)classDef.jdoGetObjectId(),
                allStructuralFeatureDefs
            );
        }
        Map<Boolean,Map<Boolean,Map<String,ModelElement_1_0>>> structuralFeatureDefsIncludeSubtypes = (Map)allStructuralFeatureDefs.get(Boolean.valueOf(includeSubtypes));
        Map<Boolean,Map<String,ModelElement_1_0>> structuralFeatureDefsIncludeDerived = (Map<Boolean,Map<String,ModelElement_1_0>>)structuralFeatureDefsIncludeSubtypes.get(Boolean.valueOf(includeDerived));
        Map<String,ModelElement_1_0> structuralFeatureDefs = (Map<String,ModelElement_1_0>)structuralFeatureDefsIncludeDerived.get(Boolean.valueOf(attributesOnly));
        return structuralFeatureDefs;
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean referenceIsStoredAsAttribute(
        java.lang.Object referenceType
    ) throws ServiceException {
        return ModelHelper.referenceIsStoredAsAttribute(
            referenceType,
            this.modelElements
        );
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean referenceIsDerived(
        java.lang.Object referenceType
    ) throws ServiceException {
        return ModelHelper.referenceIsDerived(
            referenceType,
            this.modelElements
        );
    }

    //-------------------------------------------------------------------------
    @Override
    public ModelElement_1_0 getDereferencedType(
        java.lang.Object element
    ) throws ServiceException {
        return ModelHelper.getDereferencedType(
            element,
            this.modelElements
        );
    }

    //-------------------------------------------------------------------------
    @Override
    public ModelElement_1_0 getElementType(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
    	ModelElement_1_0 elementType = null;
    	try {
	        elementType = (ModelElement_1_0)elementDef.objGetValue("dereferencedType");
    	} catch (IndexOutOfBoundsException exception) {
    		SysLog.info("CR20020817", exception.getMessage());
    	}
        if(elementType == null) {
            elementType = ModelHelper.getElementType(
                elementDef,
                this.modelElements
            );
            elementDef.objSetValue("dereferencedType",elementType);
        }
        return elementType; 
    }

    //-------------------------------------------------------------------------
    @Override
    public ModelElement_1_0 getReferenceType(
        Path path
    ) throws ServiceException {
        AssociationDef[] assocDefs = this.getAssociationDefs(path);
        return assocDefs[1].getReference();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isPrimitiveType(
        java.lang.Object type
    ) throws ServiceException {   
        return this.getDereferencedType(type).isPrimitiveType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isNumericType(
        java.lang.Object type
    ) throws ServiceException {
        ModelElement_1_0 typeDef = this.getDereferencedType(type);
        if(typeDef.isPrimitiveType()) {
            String typeName = typeDef.getQualifiedName();
            return 
            PrimitiveTypes.DECIMAL.equals(typeName) ||
            PrimitiveTypes.INTEGER.equals(typeName) ||
            PrimitiveTypes.LONG.equals(typeName) ||
            PrimitiveTypes.SHORT.equals(typeName);
        }
        return false;
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isStructureType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isStructureType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isStructureFieldType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isStructureFieldType();
    }

    //---------------------------------------------------------------------------
    @Override
    public void verifyObjectCollection(
        Object values,
        Object type,
        String multiplicity,
        boolean includeRequired
    ) throws ServiceException {
        Stack<List<?>> validationContext = new Stack<List<?>>();
        validationContext.push(
            Arrays.asList("values", values)
        );
        this.verifyObjectCollection(
            values,
            type,
            org.openmdx.base.mof.cci.ModelHelper.toMultiplicity(multiplicity),
            includeRequired,
            validationContext, 
            true, // attributesOnly
            true // verifyDerived
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Verifies an object
     * 
     * @param object to be verified
     * @param deepVerify When <code>deepVerify</code> is <code>true</code>, 
     * the refVerifyConstraints method carries out a shallowVerify on that
     * object and a deep verify through its containment hierarchy.
     * @param verifyDerived
     * tells whether derived features should be verified as well
     * 
     * @return the null value if no constraint is violated; 
     * otherwise, a list of <code>ServiceException</code> objects 
     * (each representing a constraint violation) is returned.
     */
    @Override
    public Collection<ServiceException> verifyObject(
        DataObject_1_0 object,
        boolean deepVerify, 
        boolean verifyDerived
    ){
        try {
            this.verifyObject(
                object,
                object.objGetClass(),
                null, // multiplicity
                true, // enforceRequired
                !deepVerify, // attributesOnly, 
                verifyDerived
            );
            return null;
        } catch (ServiceException exception) {
            return Collections.singleton(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.VALIDATION_FAILURE,
                    "Object validation failed",
                    new BasicException.Parameter("elementInError", getElementInError(object)),
                    new BasicException.Parameter("objectInError")
                )
            );
        }
    }

    //-------------------------------------------------------------------------
    private Object getElementInError (
        DataObject_1_0 object
    ){
        return object.jdoGetObjectId(); 
    }

    //-------------------------------------------------------------------------
    @Override
    public void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired
    ) throws ServiceException {
        this.verifyObject(
            value,
            type,
            multiplicity,
            enforceRequired,
            true // attributesOnly
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Verifies a single the value to be of the specified type. 
     * The multiplicity is required to validate Stereotypes.STREAM types only.
     * The values must be of well-known spice types which are:
     * <ul>
     *   <li>Structure_1_0</li>
     *   <li>Object_1_0</li>
     *   <li>DataproviderObject_1_0</li>
     *   <li>Primitive types</li>
     * </ul>
     * The verification is done recursively. In case of a violation
     * an exception is thrown containing the violation.
     * 
     * @param type if type == null then no verification is performed.
     * 
     * @param enforceRequired if true, all required feature of value 
     *         are verified. Otherwise verifies only the available features.
     * @param attributesOnly 
     *         if true return the same result as 
     *         verifyObject(Object,Object,String,boolean);
     *         if false allow references not stored as attributes
     * 
     * @param removeDerived removes all derived features.
     */
    @Override
    public void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired, 
        boolean attributesOnly
    ) throws ServiceException {
        this.verifyObject(
            value,
            type,
            multiplicity,
            enforceRequired,
            attributesOnly, 
            true // verifyDerived
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Verifies a single the value to be of the specified type. 
     * The multiplicity is required to validate Stereotypes.STREAM types only.
     * The values must be of well-known spice types which are:
     * <ul>
     *   <li>Structure_1_0</li>
     *   <li>Object_1_0</li>
     *   <li>DataproviderObject_1_0</li>
     *   <li>Primitive types</li>
     * </ul>
     * The verification is done recursively. In case of a violation
     * an exception is thrown containing the violation.
     * 
     * @param type if type == null then no verification is performed.
     * 
     * @param enforceRequired if true, all required feature of value 
     *         are verified. Otherwise verifies only the available features.
     * @param attributesOnly 
     *         if true return the same result as 
     *         verifyObject(Object,Object,String,boolean);
     *         if false allow references not stored as attributes
     * @param verifyDerived
     *        tells whether derived features should be verified as well
     * 
     * @param removeDerived removes all derived features.
     */
    @Override
    public void verifyObject(
        Object value,
        Object type,
        String multiplicity,
        boolean enforceRequired, 
        boolean attributesOnly,
        boolean verifyDerived
    ) throws ServiceException {
        Stack<List<?>> validationContext = new Stack<List<?>>();
        validationContext.push(
            Arrays.asList("object", value)
        );
        this.verifyObject(
            value,
            type,
            org.openmdx.base.mof.cci.ModelHelper.toMultiplicity(multiplicity),
            enforceRequired,
            validationContext, 
            attributesOnly, 
            verifyDerived
        );
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isClassType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isClassType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isStructuralFeatureType(
        java.lang.Object type
    ) throws ServiceException {
        ModelElement_1_0 typeDef = this.getDereferencedType(type);
        return typeDef.isReferenceType() || typeDef.isAttributeType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isReferenceType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isReferenceType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isAttributeType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isAttributeType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isOperationType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isOperationType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isPackageType(
        java.lang.Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isPackageType();
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean objectIsSubtypeOf(
        Object object,
        Object type
    ) throws ServiceException {
        String typeName = this.getElement(type).getQualifiedName();
        String objectClass = 
            object instanceof MappedRecord ? Object_2Facade.getObjectClass((MappedRecord)object) :
            object instanceof DataObject_1_0 ? ((DataObject_1_0)object).objGetClass() :
            null;
        for(Object supertype : this.getElement(objectClass).objGetList("allSupertype")){ 
            if(typeName.equals(((Path)supertype).getLastSegment().toClassicRepresentation())) {
                return true;
            }
        }
        return false;
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isSubtypeOf(
        Object objectType,
        Object type
    ) throws ServiceException {
        String typeName = this.getElement(type).getQualifiedName();
        for(Object supertype : this.getElement(objectType).objGetList("allSupertype")){
            if(typeName.equals(((Path)supertype).getLastSegment().toClassicRepresentation())) {
                return true;
            }
        }
        return false;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public boolean isInstanceof(
        DataObject_1_0 object,
        Object type
    ) throws ServiceException {
        return this.isSubtypeOf(
            object.objGetClass(),
            type
        );
    }

    //---------------------------------------------------------------------------  
    @Override
    public String toJavaPackageName(
        Object type,
        String packageSuffix
    ) throws ServiceException {
        return this.toJavaPackageName(
            type,
            packageSuffix,
            true
        );
    }

    //---------------------------------------------------------------------------  
    @Override
    public String toJavaPackageName(
        Object type,
        String packageSuffix,
        boolean dereferenceType
    ) throws ServiceException {
        ModelElement_1_0 element = dereferenceType ? 
            this.getDereferencedType(type) : 
            this.getElement(type);
        return ModelHelper.toJavaPackageName( 
            element.jdoGetObjectId().getSegment(4).toClassicRepresentation(),
            packageSuffix
        );
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.Model_1_3#getCompositeReference(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0)
     */
    @Override
    public ModelElement_1_0 getCompositeReference(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        for(ModelElement_1_0 elementDef : this.getContent()) {
            if(this.isReferenceType(elementDef)) {
                ModelElement_1_0 exposedEnd = this.getElement(elementDef.getReferencedEnd());
                Path type = exposedEnd.getType();
                if(
                    AggregationKind.COMPOSITE.equals(exposedEnd.getAggregation()) &&
                    this.isSubtypeOf(classDef, type)
                ) {
                    return elementDef;
                }
            }
        }
        return null;
    }

    //---------------------------------------------------------------------------
    public ModelElement_1_0[] getTypes(
        Path path
    ) throws ServiceException {
        AssociationDef[] assocDefs = this.getAssociationDefs(path);
        return new ModelElement_1_0[]{
            assocDefs[0].getReferencedType(), 
            assocDefs[1].getExposedType(), 
            assocDefs[1].getReferencedType()
        };
    }


    //------------------------------------------------------------------------
    // Implements Model_1_5
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.Model_1_5#isAssociationType(java.lang.Object)
     */
    public boolean isAssociationType(
        Object type
    ) throws ServiceException {
        return this.getDereferencedType(type).isAssociationType();
    }

    //------------------------------------------------------------------------
    // Implements Model_1_6
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.Model_1_6#getLeastDerived(java.lang.String)
     */
    public String getLeastDerived(
        String qualifiedClassName
    ) throws ServiceException {
        Types: for(
            ModelElement_1_0 type = this.getElement(qualifiedClassName);
            true;
        ){
            for(Object s : type.objGetList("supertype")) {
                ModelElement_1_0 superType = getElement(s);
                if(!superType.objGetList("stereotype").contains(Stereotypes.ROOT)) {
                    type = superType;
                    continue Types;
                }
            }
            return type.getQualifiedName();
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.cci.Model_1_0#getIdentityPattern(org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    public Path getIdentityPattern(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        if(!classDef.isClassifierType()) {
            return null;            
        }
        List<String> components = new ArrayList<String>();
        ModelElement_1_0 currentClassDef = classDef;
        while(!this.isSubtypeOf(currentClassDef, "org:openmdx:base:Authority")) {
            ModelElement_1_0 compositeReference = this.getCompositeReference(currentClassDef);
            if(compositeReference == null) {
                return null;
            }
            components.add(0, ":*");
            components.add(
                0,
                compositeReference.getName()
            );
            ModelElement_1_0 exposedEnd = this.getElement(compositeReference.getExposedEnd());     
            currentClassDef = this.getElement(exposedEnd.getType());
        }
        String className = classDef.getQualifiedName();        
        components.add(
            0,
            className.substring(0, className.lastIndexOf(":"))
        );
        Path pattern = new Path(
            components.toArray(new String[components.size()])
        );
        return pattern;
    }

    
    /**
     * This accessor is reserved for the model's builder
     *
     * @return Returns the modelElements.
     */
    Map<String, ModelElement_1_0> getModelElements() {
        return this.modelElements;
    }
    
}

//--- End of File -----------------------------------------------------------
