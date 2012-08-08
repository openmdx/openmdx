/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DataproviderObjectMarshaller.java,v 1.33 2009/02/19 19:41:08 hburger Exp $
 * Description: DataproviderObjectMarshaller
 * Revision:    $Revision: 1.33 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 19:41:08 $
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.delegation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import javax.jdo.PersistenceManager;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ietf.jgss.Oid;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.Multiplicities;
import org.openmdx.application.mof.cci.PrimitiveTypes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.LargeObject;
import org.w3c.cci2.SparseArray;
import org.w3c.spi2.Datatypes;

/**
 * DataproviderObjectMarshaller
 */
class DataproviderObjectMarshaller {

    /**
     * Constructor 
     */
    protected DataproviderObjectMarshaller(){
        // Avoid instantiation
    }

    /**
     * Maximal number of iterations to complete fetch group
     */
    private static final int FETCH_ITERATION_LIMIT = 5;

    /**
     * Return an object's path if it is an instance of RefObject_1_0
     * adding it to the bag unless the object is hollow or return the
     * object itself if it is not an instance of RefObject_1_0.
     * 
     * @param source
     * @param bag
     * 
     * @return the object itself or its path
     * 
     * @throws ServiceException
     */
    protected static final Object toDataproviderValue(
        Object source,
        Collection<Path> bag
    ) throws ServiceException{
        if(source instanceof RefObject_1_0){
            Path path = ((RefObject_1_0)source).refGetPath();
            if(
                    bag != null && // !isHollow  TODO 
                    (bag instanceof Set || !bag.contains(path))
            ) bag.add(path);
            return path;
        } else {
            return unmarshal(source);
        }
    }
    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static void setObjectValues(
        SparseList<Object> sourceValue,
        Marshaller marshaller,
        Object targetValue,
        boolean replace
    ) throws ServiceException {
        if(targetValue instanceof List) {
            if(replace) {
                ((List<?>)targetValue).clear();
            }
            for(
                    ListIterator<?> j = sourceValue.listIterator();
                    j.hasNext();
            ) {
                if(j.nextIndex() < ((List)targetValue).size()) {
                    ((List)targetValue).set(
                        j.nextIndex(),
                        marshaller.marshal(j.next())
                    );
                } else {
                    ((List)targetValue).add(
                        j.nextIndex(),
                        marshaller.marshal(j.next())
                    );
                }
            }
        } else if(targetValue instanceof Set) {
            if(replace) {
                ((Set)targetValue).clear();
            }
            for(
                    Iterator j = sourceValue.iterator();
                    j.hasNext();
            ) {
                ((Set)targetValue).add(
                    marshaller.marshal(j.next())
                );
            }
        } else if(targetValue instanceof SparseArray) {
            if(replace) {
                ((SparseArray)targetValue).clear();
            }  
            for(
                    ListIterator j = sourceValue.populationIterator();
                    j.hasNext();
            ) {
                ((SparseArray)targetValue).put(
                    j.nextIndex(),
                    marshaller.marshal(j.next())
                );
            }
        } else if(targetValue instanceof SortedMap) {
            if(replace) {
                ((SortedMap)targetValue).clear();
            }  
            for(
                    ListIterator j = sourceValue.populationIterator();
                    j.hasNext();
            ) {
                ((SortedMap)targetValue).put(
                    Integer.valueOf(j.nextIndex()),
                    marshaller.marshal(j.next())
                );
            }
        }
    }

    //------------------------------------------------------------------------
    private static Set<String> requiredAttributes(
        DataproviderRequest request,
        RefObject_1_0 source,
        Model_1_0 model
    ) throws ServiceException{
        switch(request.attributeSelector()) {
            case AttributeSelectors.NO_ATTRIBUTES: 
                return Collections.emptySet();
            case AttributeSelectors.ALL_ATTRIBUTES:
                Set<String> attributes = new HashSet<String>();
                String className = source.refClass().refMofId();
                ModelElement_1_0 classDef = model.getElement(className);
                List<?> allFeatures = classDef.objGetList("feature");
                for(
                    Iterator<?> f = allFeatures.listIterator();
                    f.hasNext();
                ){
                    ModelElement_1_0 feature = model.getElement(f.next());
                    if(isAttribute(feature, model)) {
                        attributes.add(
                            (String) feature.objGetValue("name")
                        );
                    }
                }
                return attributes;
            default: 
                return request.attributeSpecifierAsMap().keySet();
        }
    }

    //------------------------------------------------------------------------
    private static boolean isAttribute(
        ModelElement_1_0 feature,
        Model_1_0 model
    ) throws ServiceException{
        return !ModelAttributes.REFERENCE.equals(feature.objGetValue(SystemAttributes.OBJECT_CLASS)) || (
            "none".equals(model.getElement(feature.objGetValue("exposedEnd")).objGetValue("aggregation")) &&
            "none".equals(model.getElement(feature.objGetValue("referencedEnd")).objGetValue("aggregation"))
        );
    }

    //------------------------------------------------------------------------
    public static DataproviderObject toDataproviderObject(
        Path path,
        RefObject_1_0 source,
        DataproviderRequest request,
        Model_1_0 model 
    ) throws ServiceException {
        return toDataproviderObject(
            path,
            source,
            requiredAttributes(request, source, model),
            model
        );
    }

    //------------------------------------------------------------------------
    static DataproviderObject toDataproviderObject(
        Path path,
        RefObject_1_0 source,
        Set<String> requiredSet,
        Model_1_0 model 
    ) throws ServiceException {
        return toDataproviderObject(
            path,
            source,
            false, // sourceIsView
            requiredSet,
            model,
            null, // do not collect the referenced objects' paths
            0
        );
    }

    //------------------------------------------------------------------------
    static DataproviderObject toDataproviderObject(
        Path path,
        RefObject_1_0 source,
        DataproviderRequest request,
        Model_1_0 model, 
        Collection<Path> bag, 
        int bagCapacity
    ) throws ServiceException {
        return toDataproviderObject(
            path,
            source,
            false, // sourceIsView
            requiredAttributes(request, source, model),
            model,
            bag, 
            bagCapacity
        );
    }

    //------------------------------------------------------------------------
    static DataproviderObject toDataproviderObject(
        Path path,
        RefObject_1_0 source,
        Set<String> requiredSet,
        Model_1_0 model, 
        Collection<Path> bag, 
        int bagCapacity
    ) throws ServiceException {
        return toDataproviderObject(
            path,
            source,
            false, // sourceIsView
            requiredSet,
            model,
            bag, 
            bagCapacity
        );
    }

    //------------------------------------------------------------------------
    static Path extractPath(
        Exception e
    ) throws ServiceException {
        ServiceException e0 = new ServiceException(e);
        if(
                (e0.getExceptionCode() != BasicException.Code.NOT_FOUND) && 
                (e0.getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE)
        ) {
            throw e0;
        }
        String path = e0.getCause().getParameter("path");
        return path == null ?
            null :
                new Path(path);
    }

    //------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    static DataproviderObject toDataproviderObject(
        Path path,
        RefObject_1_0 source,
        boolean sourceIsView,
        Set<String> requiredSet,
        Model_1_0 model,
        Collection<Path> bag, 
        int bagCapacity
    ) throws ServiceException {

        ModelElement_1_0 classDef = model.getElement(
            source.refClass().refMofId()
        );
        DataproviderObject target = new DataproviderObject(path);
        //
        // object class
        // 
        target.values(SystemAttributes.OBJECT_CLASS).add(source.refClass().refMofId());
        //
        // object digest
        // 
        if(!sourceIsView){
            ModelElement_1_0 featureDef = model.getFeatureDef(
                classDef,
                SystemAttributes.CONTEXT_CAPABLE_CONTEXT,
                true
            );
            if(
                featureDef != null &&
                model.referenceIsStoredAsAttribute(featureDef) && 
                Multiplicities.MAP.equals(featureDef.objGetValue("multiplicity"))
            ){
                RefObject_1_0 lock = lenientGetContext(source,SystemAttributes.LOCK_CONTEXT);
                if(
                    (lock != null) &&
                    SystemAttributes.OPTIMISTIC_LOCK_CLASS.equals(lock.refClass().refMofId())
                ) {
                    target.setDigest((byte[])lock.refGetValue(SystemAttributes.OBJECT_DIGEST));
                }
            }
        }
        //
        // iterate until all features of objDefaultGroup are fetched
        // Restrict to 5 iterations to prevent endless loops
        //
        Set<String> fetchedGroup = new HashSet<String>();
        List<String> fetchGroup = new ArrayList<String>(requiredSet);
        // Assert that identity is fetched first
        for(
            int step = 0;
            step < FETCH_ITERATION_LIMIT;
            step++
        ) {
            for(String feature : fetchGroup){
                Map<String,RefObject_1_0> namespaces = null;
                String namespaceType = null;
                if(!sourceIsView){ // test for attribute with namespace, ignore nested namespaces
                    if(
                            SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(feature)
                    ){
                        ModelElement_1_0 featureDef = model.getFeatureDef(
                            classDef,
                            feature,
                            true
                        );
                        if(featureDef == null) throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND, 
                            "feature not member of classifier",
                            new BasicException.Parameter("class", classDef.objGetValue("qualifiedName")),
                            new BasicException.Parameter("feature", feature)
                        );
                        if(
                            ModelAttributes.REFERENCE.equals(featureDef.objGetValue(SystemAttributes.OBJECT_CLASS)) && ( // isReference
                                model.referenceIsStoredAsAttribute(featureDef) && Multiplicities.MAP.equals(featureDef.objGetValue("multiplicity"))
                            )
                        ){ 
                            namespaceType = feature;
                            source.refGetValue(feature);
                        }
                    } else if (
                            feature.startsWith(SystemAttributes.CONTEXT_PREFIX)
                    ) {
                        namespaceType = feature.substring(0, feature.indexOf(':'));
                        String namespaceId = feature.substring(feature.indexOf(':') + 1, feature.lastIndexOf(':'));                
                        namespaces = new HashMap<String,RefObject_1_0>();
                        namespaces.put(
                            namespaceId,
                            lenientGetContext(source, namespaceId)
                        );
                    }
                }
                if(namespaces != null) {
                    // attribute with namespace
                    for(Map.Entry<String,RefObject_1_0> namespace : namespaces.entrySet()) {
                        String namespaceId = namespace.getKey();
                        RefObject_1_0 namespaceObject = namespace.getValue();
                        DataproviderObject view = toDataproviderObject(
                            path,
                            namespaceObject,
                            true,
                            new HashSet<String>(),
                            model,
                            bag, 
                            bagCapacity
                        );
                        // move attribute values of view to target
                        for(
                                Iterator<String> k = view.attributeNames().iterator();
                                k.hasNext();
                        ) {
                            String attributeName = k.next();
                            target.clearValues(namespaceType + ':' + namespaceId + ":" + attributeName).addAll(
                                view.values(attributeName)
                            );
                        }
                    }
                    target.attributeNames().remove(namespaceType);
                } else if(namespaceType != null) { 
                    // digest
                } else if(
                        !SystemAttributes.OBJECT_INSTANCE_OF.equals(feature) &&
                        !SystemAttributes.OBJECT_CLASS.equals(feature)
                ){          
                    // feature
                    ModelElement_1_0 featureDef = model.getFeatureDef(
                        classDef,
                        feature,
                        true
                    );
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND, 
                            "feature not member of classifier",
                            new BasicException.Parameter("class", classDef.objGetValue("qualifiedName")),
                            new BasicException.Parameter("feature", feature)
                        );
                    }
                    boolean isAttribute = ModelAttributes.ATTRIBUTE.equals(featureDef.objGetValue(SystemAttributes.OBJECT_CLASS));
                    boolean isReference = ModelAttributes.REFERENCE.equals(featureDef.objGetValue(SystemAttributes.OBJECT_CLASS));
                    boolean isReferenceStoredAsAttribute = isReference && model.referenceIsStoredAsAttribute(featureDef);
                    boolean isCoreAttribute = // TODO Handle state2 and other aspects as well!
                        "org:openmdx:state2:StateCapable".equals(((Path)featureDef.objGetValue("container")).getBase()) &&
                        !"org:openmdx:compatibility:state1:StateCapable".equals(classDef.objGetValue("qualifiedName"));

                    // attributes and references stored as attributes
                    if((isAttribute || isReferenceStoredAsAttribute) && !isCoreAttribute) {

                        // structure types are not supported
                        ModelElement_1_0 featureType = model.getElementType(
                            featureDef
                        );
                        if(ModelAttributes.STRUCTURE_TYPE.equals(featureType.objGetValue(SystemAttributes.OBJECT_CLASS))) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.INVALID_CONFIGURATION, 
                                "structure types can not be transferred to DataproviderObjects",
                                new BasicException.Parameter("object", source),
                                new BasicException.Parameter("feature", feature),
                                new BasicException.Parameter("type", featureType)
                            );
                        }

                        /**
                         * determine multiplicity of feature. In case of an attribute it
                         * is the modeled multiplicity. In case of a reference with a qualifier
                         * the multiplicity is <<list>> else the modeled multiplicity.
                         */
                        String multiplicity = (String)featureDef.objGetValue("multiplicity");
                        if(isReference) {
                            ModelElement_1_0 referencedEnd = model.getElement(
                                featureDef.objGetValue("referencedEnd")
                            );
                            if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                                ModelElement_1_0 qualifierType = model.getDereferencedType(referencedEnd.objGetValue("qualifierType"));
                                if(model.isNumericType(qualifierType)) {
                                    multiplicity = Multiplicities.LIST;
                                }
                                else {
                                    multiplicity = Multiplicities.MAP;
                                }
                            }
                            // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
                            // in case <<list>> semantic is required it must be modeled as 
                            // aggregation none, multiplicita 0..1, numeric qualifier
                            else if(Multiplicities.MULTI_VALUE.equals(multiplicity)) {
                                multiplicity = Multiplicities.SET;
                            }
                        }
                        // Copy value 
                        SparseList<Object> targetValues = target.clearValues(feature);
                        if(
                                Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
                                Multiplicities.OPTIONAL_VALUE.equals(multiplicity) 
                        ) {
                            Object sourceValue = null;
                            try {
                                sourceValue = source.refGetValue(feature);
                            }
                            catch(Exception e) {
                                SysLog.warning("Error when retrieving object value", 
                                    Arrays.asList(
                                        (Object)path, 
                                        feature,
                                        e.getMessage()
                                    )
                                );
                            }
                            if(sourceValue == null) {
                                if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                                    SysLog.warning(
                                        "value of non-optional multiplicity is null", 
                                        Arrays.asList(
                                            (Object)path, 
                                            feature, 
                                            multiplicity
                                        )
                                    );
                                }
                            }
                            else {
                                targetValues.add(
                                    toDataproviderValue(sourceValue, bag)
                                );
                            }
                        }
                        else if(
                                Multiplicities.LIST.equals(multiplicity) ||
                                Multiplicities.MULTI_VALUE.equals(multiplicity)
                        ) {
                            List<?> sourceValues = (List<?>)source.refGetValue(feature);
                            Iterator<?> i = sourceValues.iterator();
                            while(true) {
                                Object sourceValue = null;
                                try {
                                    if(!i.hasNext()) break;
                                    sourceValue = i.next();
                                }
                                catch(Exception e) {
                                    sourceValue = extractPath(e);
                                    SysLog.warning("Error when retrieving object value", Arrays.asList(path, feature, e.getMessage(), sourceValue));
                                }
                                targetValues.add(
                                    toDataproviderValue(
                                        sourceValue, 
                                        bag
                                    )
                                );
                            }
                        }
                        else if(Multiplicities.SET.equals(multiplicity)) {
                            Set<?> sourceValues = (Set<?>)source.refGetValue(feature);
                            Iterator<?> i = sourceValues.iterator();
                            while(true) {
                                Object sourceValue = null;
                                try {
                                    if(!i.hasNext()) break;
                                    sourceValue = i.next();
                                }
                                catch(Exception e) {
                                    sourceValue = extractPath(e);
                                    SysLog.warning("Error when retrieving object value", Arrays.asList(path, feature, e.getMessage(), sourceValue));
                                }
                                targetValues.add(
                                    toDataproviderValue(
                                        sourceValue, 
                                        bag
                                    )
                                );
                            }
                        }
                        else if(Multiplicities.MAP.equals(multiplicity)) {
                            Map<?,?> map = (Map<?, ?>) source.refGetValue(feature);
                            for(Map.Entry<?,?> namespace : map.entrySet()){
                                String namespaceId = (String) namespace.getKey();
                                RefObject_1_0 namespaceObject = (RefObject_1_0) namespace.getValue();
                                DataproviderObject view = toDataproviderObject(
                                    path,
                                    namespaceObject,
                                    true,
                                    new HashSet<String>(),
                                    model,
                                    bag, 
                                    bagCapacity
                                );
                                // move attribute values of view to target
                                for(
                                        Iterator<String> k = view.attributeNames().iterator();
                                        k.hasNext();
                                ) {
                                    String attributeName = k.next();
                                    target.clearValues(feature + ':' + namespaceId + ":" + attributeName).addAll(
                                        view.values(attributeName)
                                    );
                                }
                            }
                        }
                        else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                            SortedMap<Integer,Object> sourceValues = new MarshallingSortedMap(
                                    new ToPathMarshaller(bag),
                                    (SparseArray<Object>)source.refGetValue(feature)
                            );
                            for(Map.Entry<Integer,Object> entry :  sourceValues.entrySet()) {
                                targetValues.set(
                                    entry.getKey().intValue(), 
                                    entry.getValue()
                                );
                            } 
                        }
                        else if(Multiplicities.STREAM.equals(multiplicity)){
                            try {
                                Object sourceValue = source.refGetValue(feature);
                                if(sourceValue != null) {
                                    LargeObject lob = null;
                                    String streamType = (String)featureType.objGetValue("qualifiedName");
                                    if(PrimitiveTypes.BINARY.equals(streamType)){
                                        if(sourceValue instanceof BinaryLargeObject) {
                                            lob = (BinaryLargeObject)sourceValue;
                                        }
                                        else if(sourceValue instanceof InputStream) {
                                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                            BinaryLargeObjects.streamCopy(
                                                (InputStream)sourceValue, 
                                                0L, 
                                                bytes
                                            );
                                            bytes.close();
                                            lob = BinaryLargeObjects.valueOf(
                                                bytes.toByteArray()
                                            );
                                        }
                                        else if(sourceValue instanceof byte[]) {
                                            lob = BinaryLargeObjects.valueOf(
                                                (byte[])sourceValue
                                            );
                                        }
                                        else {
                                            throw new UnsupportedOperationException("Unsupported stream class " + sourceValue.getClass().getName());                                        
                                        }
                                        targetValues.add(
                                            ((BinaryLargeObject)lob).getContent()
                                        );                  
                                    }
                                    else if(PrimitiveTypes.STRING.equals(streamType)){
                                        lob = (LargeObject)sourceValue;
                                        targetValues.add(
                                            ((CharacterLargeObject)lob).getContent()
                                        );                  
                                    } 
                                    else {  
                                        throw new UnsupportedOperationException("Unsupported stream type " + streamType);
                                    }
                                    targetValues.add(lob.getLength());
                                }
                            } 
                            catch (IOException exception) {
                                throw new ServiceException(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.TRANSFORMATION_FAILURE,
                                    "Large object propagation failure"
                                );
                            }
                        }
                        else {
                            throw new UnsupportedOperationException("unsupported multiplicity \u00ab" + multiplicity + "\u00bb");
                        }          
                    }           
                    // references
                    else if(isReference) {
                        if (bag != null && bag.size() < bagCapacity) {
                            boolean incomplete;
                            Collection<Path> children = new ArrayList<Path>();
                            Collection<?> container = (Collection<?>) source.refGetValue(feature);
                            for(
                                    Iterator<?> j = container.iterator();
                                    (incomplete = j.hasNext()) && bag.size() < bagCapacity;
                            ) {
                                children.add((Path) toDataproviderValue(j.next(), bag));
                            }
                            if(!incomplete) {
                                target.clearValues(feature).addAll(children);
                            }
                        }
                    }
                }
            }
            fetchedGroup.addAll(fetchGroup);
            fetchGroup = new ArrayList<String>(source.refDefaultFetchGroup());
            fetchGroup.removeAll(fetchedGroup);
            if(fetchGroup.isEmpty()) break;
        }
        return target;
    }

    //------------------------------------------------------------------------
    public static String toMofName(
        List<String> name
    ) {
        StringBuilder mofName = new StringBuilder();
        String separator = "";
        for(String n: name) {
            mofName.append(separator).append(n);
            separator = ":";            
        }
        return mofName.toString();
    }

    //------------------------------------------------------------------------
    @SuppressWarnings("unchecked")    
    public static DataproviderObject toDataproviderObject(
        Path path,
        RefStruct_1_0 source,
        Collection<Path> bag
    ) throws ServiceException {
        DataproviderObject target = new DataproviderObject(path);
        target.values(SystemAttributes.OBJECT_CLASS).add(
            toMofName(source.refTypeName())
        );
        for(
                Iterator i = source.refFieldNames().iterator();
                i.hasNext();
        ) {
            String fieldName = (String)i.next();
            if(!fieldName.equals(SystemAttributes.OBJECT_INSTANCE_OF)) {
                Object fieldValue = source.refGetValue(fieldName);
                SparseList targetValue = target.values(fieldName);  
                if(fieldValue instanceof SparseArray){
                    for(
                            ListIterator j = ((SparseArray)fieldValue).populationIterator();
                            j.hasNext();
                    ) {
                        targetValue.set(
                            j.nextIndex(),
                            toDataproviderValue(j.next(), bag)
                        );
                    }
                } 
                else if(fieldValue instanceof SortedMap){
                    for(
                            Iterator j = ((SortedMap)fieldValue).entrySet().iterator();
                            j.hasNext();
                    ) {
                        Map.Entry k = (Entry)j.next();
                        targetValue.set(
                            ((Integer)k.getKey()).intValue(), 
                            toDataproviderValue(k.getValue(), bag)
                        );
                    }
                } 
                else if(fieldValue instanceof Collection) {
                    for(
                            Iterator j = ((Collection)fieldValue).iterator();
                            j.hasNext();
                    ) {
                        targetValue.add(
                            toDataproviderValue(j.next(), bag)
                        );
                    }
                } 
                else {
                    targetValue.set(
                        0, 
                        toDataproviderValue(fieldValue, bag)
                    );
                }
            }                
        }
        return target;
    }

    //------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static void toObject(
        String _namespacePrefix,
        DataproviderObject source,
        RefObject_1_0 target,
        Map objectCache,
        PersistenceManager pm,
        Model_1_0 model, 
        boolean replace
    ) throws ServiceException {
        String namespacePrefix = _namespacePrefix == null ? "" : _namespacePrefix;
        String typeName = (String)source.values(namespacePrefix + SystemAttributes.OBJECT_CLASS).get(0);
        Marshaller marshaller = new ToObjectMarshaller(objectCache, pm);
        Set<String> contexts = new HashSet<String>();
        for(
            Iterator<String> i = source.attributeNames().iterator();
            i.hasNext();
        ) try {
            String featureName = i.next();
            SparseList<Object> featureValues = source.values(featureName);
            if(!featureName.startsWith(namespacePrefix)) continue;
            featureName = featureName.substring(namespacePrefix.length());
            if(
                SystemAttributes.OBJECT_CLASS.equals(featureName) ||
                SystemAttributes.CREATED_AT.equals(featureName) ||
                SystemAttributes.MODIFIED_AT.equals(featureName)
            ) {
                continue;
            }
            if(featureName.startsWith(SystemAttributes.CONTEXT_PREFIX)){
                //
                // Remember the contexts to be processed
                //
                contexts.add(
                    featureName.substring(
                        SystemAttributes.CONTEXT_PREFIX.length(), 
                        featureName.indexOf(':', SystemAttributes.CONTEXT_PREFIX.length())
                    )
                );
            } else {

                /**
                 * Get attribute definition from model. The attribute multiplicity is
                 * required to invoke objGetValue(), objSetValue() correctly
                 */
                ModelElement_1_0 classDef = model.getElement(typeName);
                ModelElement_1_0 featureDef = model.getFeatureDef(
                    classDef,
                    featureName,
                    true
                ); 
                if(featureDef == null) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_MEMBER_NAME, 
                    "attribute not found in class",
                    new BasicException.Parameter("class", classDef),
                    new BasicException.Parameter("attribute name", featureName)
                );        
                String multiplicity = (String)featureDef.objGetValue("multiplicity");
                if(model.isReferenceType(featureDef)) {
                    ModelElement_1_0 referencedEnd = model.getElement(
                        featureDef.objGetValue("referencedEnd")
                    );
                    if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                        multiplicity = Multiplicities.LIST;
                    }
                    // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
                    // in case <<list>> semantic is required it must be modeled as 
                    // aggregation none, multiplicity 0..1, numeric qualifier
                    else if(Multiplicities.MULTI_VALUE.equals(multiplicity)) {
                        multiplicity = Multiplicities.SET;
                    }
                }
                /**
                 * Store the attribute value to target according to the attribute's
                 * multiplicity
                 */     
                // OPTIONAL_VALUE
                if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                    target.refSetValue(
                        featureName,
                        marshal(model, pm, featureDef, featureValues)
                    );
                }
                // SINGLE_VALUE
                else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                    target.refSetValue(
                        featureName,
                        marshal(model, pm, featureDef, featureValues)
                    );
                }
                // STREAM
                else if(Multiplicities.STREAM.equals(multiplicity)) {
                    Object sourceValue = featureValues.get(0); 
                    Long sourceLength = (Long) featureValues.get(1);
                    if (sourceValue == null){
                        target.refSetValue(
                            featureName, 
                            null
                        );
                    } else if(sourceValue instanceof InputStream) {
                        target.refSetValue(
                            featureName,
                            new BinaryReadOnce(
                                (InputStream)sourceValue,
                                sourceLength
                            )
                        );
                    } else if (sourceValue instanceof Reader) {
                        target.refSetValue(
                            featureName,
                            new CharacterReadOnce(
                                (Reader)sourceValue,
                                sourceLength
                            )
                        );
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED, 
                            "Stream value not supported. Supported are [Reader|InputStream]",
                            new BasicException.Parameter("multiplicity", multiplicity),
                            new BasicException.Parameter("attribute name", featureName)
                        );        
                    }
                }
                // LIST
                else if(Multiplicities.MULTI_VALUE.equals(multiplicity) || Multiplicities.LIST.equals(multiplicity)) {
                    List<Object> targetValue = (List<Object>) target.refGetValue(featureName);
                    setObjectValues(
                        featureValues,
                        marshaller,
                        targetValue,
                        replace
                    );
                }
                // SET
                else if(Multiplicities.SET.equals(multiplicity)) {
                    Set<Object> targetValue = (Set<Object>) target.refGetValue(featureName);
                    setObjectValues(
                        featureValues,
                        marshaller,
                        targetValue,
                        replace
                    );
                }
                // SPARSEARRAY
                else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                    SortedMap<Integer,Object> targetValue = (SortedMap<Integer, Object>) target.refGetValue(featureName);
                    setObjectValues(
                        featureValues,
                        marshaller,
                        targetValue,
                        replace
                    );
                }
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED, 
                        "multiplicity not supported. Supported are [0..1|1..1|0..n|list|set|sparsearray]",
                        new BasicException.Parameter("multiplicity", multiplicity),
                        new BasicException.Parameter("attribute name", featureName)
                    );        
                }
            }
        } catch (RuntimeException exception) {
            throw new ServiceException(exception).log();
        }
        /**
         * Process contexts
         */
        byte[] digest = source.getDigest();
        if(digest != null) contexts.add(SystemAttributes.LOCK_CONTEXT);
        if(! contexts.isEmpty()) {
            Map container = (Map) target.refGetValue(SystemAttributes.CONTEXT_CAPABLE_CONTEXT);
            for(
                Iterator i = contexts.iterator();
                i.hasNext();
            ){
                String name = (String)i.next();
                RefObject_1_0 object = (RefObject_1_0)container.get(name); 
                if(object == null) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "This embedded object can't be created or modified",
                    new BasicException.Parameter(SystemAttributes.CONTEXT_CAPABLE_CONTEXT,name)
                );
                if(SystemAttributes.LOCK_CONTEXT.equals(name)){
                    object.refSetValue(SystemAttributes.OBJECT_DIGEST, digest);
                }else{
                    toObject(
                        SystemAttributes.CONTEXT_PREFIX + name,
                        source,
                        object,
                        objectCache,
                        pm,
                        model, 
                        replace
                    );
                }
            }
        }
    }

    //------------------------------------------------------------------------
    public static List<?> toStructureValues(
        DataproviderObject source,
        Map<Path,RefObject_1_0> objectCache,
        PersistenceManager entityManager,
        Model_1_0 model
    ) throws ServiceException {
        List<Object> targetValues = new ArrayList<Object>();
        String typeName = (String)source.getValues(SystemAttributes.OBJECT_CLASS).get(0);
        ModelElement_1_0 structDef = model.getElement(typeName);
        Marshaller marshaller = new ToObjectMarshaller(objectCache, entityManager);
        for(Object i: structDef.objGetList("content")) {
            Path e = (Path)i;
            ModelElement_1_0 fieldDef = model.getElement(e);
            String fieldName = (String)fieldDef.objGetValue("name");
            String multiplicity = (String)fieldDef.objGetValue("multiplicity");
            if(model.isReferenceType(fieldDef)) {
                ModelElement_1_0 referencedEnd = model.getElement(
                    fieldDef.objGetValue("referencedEnd")
                );
                if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                    multiplicity = Multiplicities.LIST;
                }
                // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
                // in case <<list>> semantic is required it must be modeled as 
                // aggregation none, multiplicity 0..1, numeric qualifier
                else if(Multiplicities.MULTI_VALUE.equals(multiplicity)) {
                    multiplicity = Multiplicities.SET;
                }
            }
            /**
             * Store the attribute value to target according to the attribute's
             * multiplicity
             */     
            // OPTIONAL_VALUE
            if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                targetValues.add(
                    source.values(fieldName).size() > 0 
                    ? marshaller.marshal(source.values(fieldName).get(0))
                        : null
                );
            }
            // SINGLE_VALUE
            else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                targetValues.add(
                    marshaller.marshal(source.values(fieldName).get(0))
                );
            }
            // LIST
            else if(Multiplicities.MULTI_VALUE.equals(multiplicity) || Multiplicities.LIST.equals(multiplicity)) {
                List<Object> targetValue = new ArrayList<Object>();
                targetValues.add(targetValue);
                setObjectValues(
                    source.values(fieldName),
                    marshaller,
                    targetValue,
                    false
                );
            }
            // SET
            else if(Multiplicities.SET.equals(multiplicity)) {
                Set<Object> targetValue = new HashSet<Object>();
                targetValues.add(targetValue);
                setObjectValues(
                    source.values(fieldName),
                    marshaller,
                    targetValue,
                    false
                );
            }
            // SPARSEARRAY
            else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                SortedMap<Integer,Object> targetValue = new TreeMap<Integer,Object>();
                targetValues.add(targetValue);
                setObjectValues(
                    source.values(fieldName),
                    marshaller,
                    targetValue,
                    false
                );
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "multiplicity not supported. Supported are [0..1|1..1|0..n|list|set|sparsearray]",
                    new BasicException.Parameter("multiplicity", multiplicity),
                    new BasicException.Parameter("attribute name", fieldName)
                );        
            }
        }
        return targetValues;
    }

    //------------------------------------------------------------------------
    /**
     * Normalize date time values to basic format
     * 
     * @param date time value in either basic or extended format
     * 
     * @return date time values in basic format
     */
    private static String toBasicFormat(
        String dateTime
    ){
        return dateTime.replace(
            "-", ""
        ).replace(
            ":", ""
        ).replace(
            ',', 
            '.'
        );
    }

    //------------------------------------------------------------------------
    private static Class<?> toClass(
        String qualifiedTypeName
    ){
        return 
        PrimitiveTypes.STRING.equals(qualifiedTypeName) ? String.class :
            PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? Boolean.class :
                PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? Date.class :
                    PrimitiveTypes.DATE.equals(qualifiedTypeName) ? XMLGregorianCalendar.class :
                        PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? URI.class :
                            PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? Duration.class :
                                PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? Short.class :
                                    PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? Integer.class :
                                        PrimitiveTypes.LONG.equals(qualifiedTypeName) ? Long.class :
                                            PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? BigDecimal.class :
                                                PrimitiveTypes.OID.equals(qualifiedTypeName) ? Oid.class :
                                                    PrimitiveTypes.UUID.equals(qualifiedTypeName) ? UUID.class :
                                                        Object.class;
    }

    //------------------------------------------------------------------------
    private static Object marshal(
        Model_1_0 model,
        PersistenceManager pm,
        ModelElement_1_0 featureDef, 
        SparseList<Object> values
    ) throws ServiceException{
        Object value =  values == null ? null : values.get(0);
        return value instanceof String ? 
            Datatypes.create(
                toClass(
                    (String) model.getElementType(
                        featureDef
                    ).objGetValue("qualifiedName")
                ),
                (String) value
            ) : 
            value instanceof Path ?
                pm.getObjectById(value) :
                value;
    }

    //------------------------------------------------------------------------
    private static Object unmarshal(
        Object source
    ){
        return 
        source instanceof String || source instanceof Path || source instanceof Boolean || source instanceof Number || source instanceof byte[] ? source :
            source instanceof Date ? DateFormat.getInstance().format((Date)source) :
                source instanceof XMLGregorianCalendar ? toBasicFormat(source.toString()) : 
                    source == null ? null : source.toString();
    }

    //------------------------------------------------------------------------
    // Class ToPathMarshaller
    //------------------------------------------------------------------------

    static class ToPathMarshaller 
    implements Marshaller 
    {

        /**
         * Constructor
         * 
         * @param bag
         */
        ToPathMarshaller(Collection<Path> bag) {
            this.bag = bag;
        }

        private final Collection<Path> bag;

        public Object marshal(
            Object source
        ) throws ServiceException {
            return toDataproviderValue(source, this.bag);
        }

        public Object unmarshal(
            Object source
        ) {
            return source;
        }

    }


    //-------------------------------------------------------------------------
    // class ToObjectMarshaller
    //-------------------------------------------------------------------------

    static class ToObjectMarshaller 
    implements Marshaller 
    {

        /**
         * Constructor 
         *
         * @param objectCache
         * @param objectFactory
         */
        ToObjectMarshaller(
            Map<Path,RefObject_1_0> objectCache,
            PersistenceManager objectFactory
        ){
            this.objectCache = objectCache;
            this.objectFactory = objectFactory;
        }

        public Object marshal(
            Object source
        ) throws ServiceException {
            return source instanceof Path ? (
                    this.objectCache.containsKey(source) ? 
                        this.objectCache.get(source) :
                            this.objectFactory.getObjectById(source)
            ) : source;
        }

        public Object unmarshal(
            Object source
        ) throws ServiceException {
            return source instanceof RefObject_1_0 ?
                ((RefObject_1_0)source).refGetPath() :
                    source;
        }

        private final Map<Path,RefObject_1_0>  objectCache;

        private final PersistenceManager objectFactory;

    }


    //------------------------------------------------------------------------
    // class BinaryReadOnce
    //------------------------------------------------------------------------

    static class BinaryReadOnce implements BinaryLargeObject {

        /**
         * Constructor 
         *
         * @param source
         */
        BinaryReadOnce(
            InputStream source,
            Long length
        ) {
            this.source = source;
            this.length = null;
        }

        /**
         * 
         */
        InputStream source;

        /**
         * 
         */
        Long length;

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            if(this.source == null) throw new IllegalStateException(
                "The content may be retrieved once only"
            );
            InputStream content = this.source;
            this.source = null;
            return content;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream target, 
            long position
        ) throws IOException {
            this.length = BinaryLargeObjects.streamCopy(
                getContent(), 
                position, 
                target
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return this.length;
        }

    }


    //------------------------------------------------------------------------
    // class CharacterReadOnce
    //------------------------------------------------------------------------

    static class CharacterReadOnce implements CharacterLargeObject {

        /**
         * Constructor 
         *
         * @param source
         */
        CharacterReadOnce(
            Reader source,
            Long length
        ) {
            this.source = source;
            this.length = null;
        }

        /**
         * 
         */
        Reader source;

        /**
         * 
         */
        Long length;

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public Reader getContent(
        ) throws IOException {
            if(this.source == null) throw new IllegalStateException(
                "The content may be retrieved once only"
            );
            Reader content = this.source;
            this.source = null;
            return content;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            Writer target, 
            long position
        ) throws IOException {
            this.length = CharacterLargeObjects.streamCopy(
                getContent(), 
                position, 
                target
            );
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return this.length;
        }

    }

    /**
     * Lenient context accessor, accepting former RefContainer as well as current Map container
     * 
     * @param source
     * @param name
     * 
     * @return a named context
     */
    private static RefObject_1_0 lenientGetContext(
        RefObject_1_0 source,
        String name
    ){
        Object container = source.refGetValue(
            SystemAttributes.CONTEXT_CAPABLE_CONTEXT
        );
        Object context = container instanceof RefContainer ? 
            ((RefContainer) container).refGet(RefContainer.REASSIGNABLE, name) :
                ((Map<?,?>)container).get(name);
            return (RefObject_1_0)context;
    }

}