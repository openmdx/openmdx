/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderObjectMarshaller.java,v 1.22 2008/06/27 15:04:34 hburger Exp $
 * Description: DataproviderObjectMarshaller
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/27 15:04:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.dispatching;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_1;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.PopulationIterator;
import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class DataproviderObjectMarshaller {

    /**
     * Return an object's path if it is an instance of Object_1_0
     * adding it to the bag unless the object is hollow or return the
     * object itself if it is not an instance of Object_1_0.
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
        Collection bag
    ) throws ServiceException{
        if(source instanceof Object_1_0){
            Path path = ((Object_1_0)source).objGetPath();
            if(
                bag != null &&
                source instanceof Object_1_1 &&
                !((Object_1_1)source).objIsHollow() &&
                !bag.contains(path)
            ) bag.add(path);
            return path;
        } else {
            return source;
        }
    }

    //-------------------------------------------------------------------------
  static class ToPathMarshaller 
    implements Marshaller {
      
    /**
     * Constructor
     * 
     * @param bag
     */
    ToPathMarshaller(Collection bag) {
        this.bag = bag;
    }
    
    private final Collection bag;
    
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
  static class ToObjectMarshaller 
    implements Marshaller {

    ToObjectMarshaller(
        Map objectCache,
        ObjectFactory_1_0 objectFactory
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
                this.objectFactory.getObject(source)
        ) : source;
    }

    public Object unmarshal(
      Object source
    ) throws ServiceException {
      return source instanceof Object_1_0 ?
        ((Object_1_0)source).objGetPath() :
        source;
    }
    
    private final Map objectCache;
    
    private final ObjectFactory_1_0 objectFactory;
    
  }

  //---------------------------------------------------------------------------
  public static void setObjectValues(
    SparseList sourceValue,
      Marshaller marshaller,
    Object targetValue,
    boolean replace
  ) throws ServiceException {
    if(targetValue instanceof List) {
      if(replace) {
        ((List)targetValue).clear();
      }
      for(
        ListIterator j = sourceValue.listIterator();
        j.hasNext();
      ) {
        if(j.nextIndex() < ((List)targetValue).size()) {
          ((List)targetValue).set(
            j.nextIndex(),
            marshaller.marshal(j.next())
          );
        }
        else {
          ((List)targetValue).add(
            j.nextIndex(),
            marshaller.marshal(j.next())
          );
        }
      }
    }
    else if(targetValue instanceof Set) {
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
    }
    else if(targetValue instanceof SparseArray) {
      if(replace) {
        ((SparseArray)targetValue).clear();
      }  
      for(
        ListIterator j = sourceValue.populationIterator();
        j.hasNext();
      ) {
        ((SparseArray)targetValue).set(
          j.nextIndex(),
          marshaller.marshal(j.next())
        );
      }
    }
    else if(targetValue instanceof SortedMap) {
      if(replace) {
        ((SortedMap)targetValue).clear();
      }  
      for(
        ListIterator j = sourceValue.populationIterator();
        j.hasNext();
      ) {
        ((SortedMap)targetValue).put(
          new Integer(j.nextIndex()),
          marshaller.marshal(j.next())
        );
      }
    }
  }


  //------------------------------------------------------------------------
  private static Set requiredAttributes(
      DataproviderRequest request,
      Object_1_0 source,
      Model_1_0 model
  ) throws ServiceException{
      switch(request.attributeSelector()) {
          case AttributeSelectors.NO_ATTRIBUTES: 
              return Collections.EMPTY_SET;
          case AttributeSelectors.ALL_ATTRIBUTES:
              Set attributes = new HashSet();
              String className = source.objGetClass();
              ModelElement_1_0 classDef = model.getElement(className);
              SparseList allFeatures = classDef.getValues("feature");
              for(
                  Iterator f = allFeatures.populationIterator();
                  f.hasNext();
              ){
                  ModelElement_1_0 feature = model.getElement(f.next());
                  if(isAttribute(feature, model)) {
                      attributes.add(feature.values("name").get(0));
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
      return !ModelAttributes.REFERENCE.equals(feature.values(SystemAttributes.OBJECT_CLASS).get(0)) || (
           "none".equals(model.getElement(feature.values("exposedEnd").get(0)).values("aggregation").get(0)) &&
           "none".equals(model.getElement(feature.values("referencedEnd").get(0)).values("aggregation").get(0))
      );
  }
      
  //------------------------------------------------------------------------
  public static DataproviderObject toDataproviderObject(
      Path path,
      Object_1_0 source,
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
  
  static DataproviderObject toDataproviderObject(
    Path path,
    Object_1_0 source,
    Set requiredSet,
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
    Object_1_0 source,
    DataproviderRequest request,
    Model_1_0 model, 
    Collection bag, 
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
  
  static DataproviderObject toDataproviderObject(
      Path path,
      Object_1_0 source,
      Set requiredSet,
      Model_1_0 model, 
      Collection bag, 
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
  static DataproviderObject toDataproviderObject(
    Path path,
    Object_1_0 source,
    boolean sourceIsView,
    Set requiredSet,
    Model_1_0 model,
    Collection bag, 
    int bagCapacity
  ) throws ServiceException {

    ModelElement_1_0 classDef = model.getElement(
      source.objGetClass()
    );
    DataproviderObject target = new DataproviderObject(path);
    //
    // object class
    // 
    target.values(SystemAttributes.OBJECT_CLASS).add(source.objGetClass());
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
            Multiplicities.MAP.equals(featureDef.values("multiplicity").get(0))
        ){
            Object_1_0 lock = source.objGetContainer(
                SystemAttributes.CONTEXT_CAPABLE_CONTEXT
            ).get(
                SystemAttributes.LOCK_CONTEXT
            );
            if(
                lock!=null &&
                SystemAttributes.OPTIMISTIC_LOCK_CLASS.equals(lock.objGetClass())
            ) target.setDigest((byte[])lock.objGetValue(SystemAttributes.OBJECT_DIGEST));
        }
    }
    //
    // iterate until all features of objDefaultGroup are fetched
    // Restrict to 5 iterations to prevent endless loops
    //
    Set fetchedGroup = new HashSet();
    Set fetchGroup = requiredSet;
    for(
      int step = 0;
       step < 5;
       step++
    ) {
      for(
        Iterator i = fetchGroup.iterator();
        i.hasNext();
      ) {
        String feature = (String)i.next();

        Map namespaces = null;
        String namespaceType = null;
        if(!sourceIsView){ // test for attribute with namespace, ignore nested namespaces
            if(
                SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(feature) ||
                SystemAttributes.ROLE_CAPABLE_ROLE.equals(feature) ||
                SystemAttributes.VIEW_CAPABLE_VIEW.equals(feature)
            ){
                ModelElement_1_0 featureDef = model.getFeatureDef(
                    classDef,
                    feature,
                    true
                );
                if(featureDef == null) throw new ServiceException(
                      BasicException.Code.DEFAULT_DOMAIN,
                      BasicException.Code.NOT_FOUND, 
                      new BasicException.Parameter[]{
                          new BasicException.Parameter("class", classDef.values("qualifiedName").get(0)),
                          new BasicException.Parameter("feature", feature)
                      },
                      "feature not member of classifier"
                );
                if(
                    ModelAttributes.REFERENCE.equals(featureDef.values(SystemAttributes.OBJECT_CLASS).get(0)) && ( // isReference
                        SystemAttributes.VIEW_CAPABLE_VIEW.equals(feature) || 
                        model.referenceIsStoredAsAttribute(featureDef) && Multiplicities.MAP.equals(featureDef.values("multiplicity").get(0))
                    )
                 ){ 
                    namespaceType = feature;
                    namespaces = source.objGetContainer(feature);
                }
            } else if (
                feature.startsWith(SystemAttributes.CONTEXT_PREFIX) ||
                feature.startsWith(SystemAttributes.ROLE_PREFIX) ||
                feature.startsWith(SystemAttributes.VIEW_PREFIX)
            ) {
                namespaceType = feature.substring(0, feature.indexOf(':'));
                String namespaceId = feature.substring(feature.indexOf(':') + 1, feature.lastIndexOf(':'));                
                namespaces = new HashMap();
                namespaces.put(
                    namespaceId,
                    source.objGetContainer(namespaceType).get(namespaceId)
               );
            }
        }
        if(namespaces != null) {
            // attribute with namespace
            for(
                Iterator j = namespaces.entrySet().iterator();
                j.hasNext();
            ) {
                Map.Entry namespace = (Entry) j.next();
                String namespaceId = (String) namespace.getKey();
                Object_1_0 namespaceObject = (Object_1_0)namespace.getValue();
                DataproviderObject view = toDataproviderObject(
                    path,
                    namespaceObject,
                    true,
                    new HashSet(),
                    model,
                    bag, 
                    bagCapacity
                );
                // move attribute values of view to target
                for(
                    Iterator k = view.attributeNames().iterator();
                    k.hasNext();
                ) {
                    String attributeName = (String)k.next();
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
              new BasicException.Parameter[]{
                new BasicException.Parameter("class", classDef.values("qualifiedName").get(0)),
                new BasicException.Parameter("feature", feature)
              },
              "feature not member of classifier"
            );
          }
          boolean isAttribute = ModelAttributes.ATTRIBUTE.equals(featureDef.values(SystemAttributes.OBJECT_CLASS).get(0));
          boolean isReference = ModelAttributes.REFERENCE.equals(featureDef.values(SystemAttributes.OBJECT_CLASS).get(0));
          boolean isReferenceStoredAsAttribute = isReference && model.referenceIsStoredAsAttribute(featureDef);
          
          // attributes and references stored as attributes
          if(isAttribute || isReferenceStoredAsAttribute) {
            
            // structure types are not supported
            ModelElement_1_0 featureType = model.getDereferencedType(
              featureDef.values("type").get(0)
            );
            if(ModelAttributes.STRUCTURE_TYPE.equals(featureType.values(SystemAttributes.OBJECT_CLASS).get(0))) {
              throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                new BasicException.Parameter[]{
                  new BasicException.Parameter("object", source),
                  new BasicException.Parameter("feature", feature),
                  new BasicException.Parameter("type", featureType)
                },
                "structure types can not be transferred to DataproviderObjects"
              );
            }
         
            /**
             * determine multiplicity of feature. In case of an attribute it
             * is the modeled multiplicity. In case of a reference with a qualifier
             * the multiplicity is <<list>> else the modeled multiplicity.
             */
            String multiplicity = (String)featureDef.values("multiplicity").get(0);
            if(isReference) {
              ModelElement_1_0 referencedEnd = model.getElement(
                featureDef.values("referencedEnd").get(0)
              );
              if(referencedEnd.values("qualifierType").size() > 0) {
                ModelElement_1_0 qualifierType = model.getDereferencedType(referencedEnd.values("qualifierType").get(0));
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
            
            // copy value 
            SparseList targetValues = target.clearValues(feature);
            if(
              Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
              Multiplicities.OPTIONAL_VALUE.equals(multiplicity) 
            ) {
              Object sourceValue = source.objGetValue(feature);
              if(sourceValue == null) {
                if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                  targetValues.clear();
                }
                else {
                  throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    new BasicException.Parameter[]{
                      new BasicException.Parameter("path", path),
                      new BasicException.Parameter("feature", feature),
                      new BasicException.Parameter("multiplicity", multiplicity)
                    },
                    "value of non-optional multiplicity is null"
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
              targetValues.addAll(
                new MarshallingList(
                  new ToPathMarshaller(bag),
                  source.objGetList(feature)
                )
              );
            }
            else if(Multiplicities.SET.equals(multiplicity)) {
              targetValues.addAll(
                new MarshallingSet(
                  new ToPathMarshaller(bag),
                  source.objGetSet(feature)
                )
              );
            }
            else if(Multiplicities.MAP.equals(multiplicity)) {
              // TODO: map Container as MAP to DataproviderObject
              //... FilterableMap map = source.objGetContainer(feature);
            }
            else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
              SortedMap sourceValues = new MarshallingSortedMap(
                new ToPathMarshaller(bag),
                source.objGetSparseArray(feature)
              );
              for(
                Iterator j = sourceValues.entrySet().iterator();
                j.hasNext();
              ) {
                Map.Entry entry = (Entry)j.next();
                targetValues.set(
                  ((Integer)entry.getKey()).intValue(), 
                  entry.getValue()
                );
              } 
            }
            else if(Multiplicities.STREAM.equals(multiplicity)){
              LargeObject_1_0 sourceValue = source.objGetLargeObject(feature);
              String streamType = (String)featureType.values("qualifiedName").get(0);
              if(PrimitiveTypes.BINARY.equals(streamType)){
                targetValues.add(sourceValue.getBinaryStream());                  
              } else if (PrimitiveTypes.STRING.equals(streamType)){
                  targetValues.add(sourceValue.getCharacterStream());                  
              } else {  
                throw new UnsupportedOperationException("unsupported stream type " + streamType);
              }
              targetValues.add(new Long(sourceValue.length()));
            }
            else {
              throw new UnsupportedOperationException("unsupported multiplicity \u00ab" + multiplicity + "\u00bb");
            }          
          }           
          // references
          else if(isReference) {
            if (bag != null && bag.size() < bagCapacity) {
              boolean incomplete;
              Collection children = new ArrayList();
              for(
                Iterator j = source.objGetContainer(feature).values().iterator();
                (incomplete = j.hasNext()) && bag.size() < bagCapacity;
              ) children.add(toDataproviderValue(j.next(), bag));
              if(!incomplete) target.clearValues(feature).addAll(children);
            }
          }
        }
      }
      fetchedGroup.addAll(fetchGroup);
      fetchGroup = source.objDefaultFetchGroup();
      fetchGroup.removeAll(fetchedGroup);
      if(fetchGroup.isEmpty()) break;
    }
    return target;
  }
  
  //------------------------------------------------------------------------
  public static DataproviderObject toDataproviderObject(
    Path path,
    Structure_1_0 source
  ) throws ServiceException {
    DataproviderObject target = new DataproviderObject(path);
    target.values(SystemAttributes.OBJECT_CLASS).add(
      source.objGetType()
    );
    for(
      Iterator i = source.objFieldNames().iterator();
      i.hasNext();
    ) {
      String fieldName = (String)i.next();
      Object fieldValue = source.objGetValue(fieldName);
      if(fieldName.startsWith(SystemAttributes.OBJECT_LOCK_PREFIX)) {
          if(fieldName.endsWith(SystemAttributes.OBJECT_DIGEST)){
              target.setDigest((byte[])fieldValue);
          }
      }
      else {
        SparseList targetValue = target.values(fieldName);  
        if(fieldValue instanceof SparseArray){
          for(
            PopulationIterator j = ((SparseArray)fieldValue).populationIterator();
            j.hasNext();
          ) {
            targetValue.set(
              j.nextIndex(), 
              j.next()
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
              k.getValue()
            );
          }
        } 
        else if(fieldValue instanceof Collection) {
          targetValue.addAll((Collection)fieldValue);
        } 
        else {
          targetValue.set(0, fieldValue);
        }   
      }
    }
    return target;
  }
  
  //------------------------------------------------------------------------
  public static void toObject(
    String _namespacePrefix,
    DataproviderObject source,
    Object_1_0 target,
    Map objectCache,
    ObjectFactory_1_0 objectFactory,
    Model_1_0 model, boolean replace
  ) throws ServiceException {
    String namespacePrefix = _namespacePrefix == null ? "" : _namespacePrefix;
    String typeName = (String)source.values(namespacePrefix + SystemAttributes.OBJECT_CLASS).get(0);
    Marshaller marshaller = new ToObjectMarshaller(objectCache, objectFactory);
    Set contexts = new HashSet();
    Set roles = new HashSet();
    Set views = new HashSet();
    for(
      Iterator i = source.attributeNames().iterator();
      i.hasNext();
    ) try {
      String featureName = (String)i.next();
      SparseList featureValues = source.values(featureName);
      if(!featureName.startsWith(namespacePrefix)) continue;
      featureName = featureName.substring(namespacePrefix.length());
      if(SystemAttributes.OBJECT_CLASS.equals(featureName)) continue;
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
      } else if(featureName.startsWith(SystemAttributes.ROLE_PREFIX)){
          //
          // Remember the roles to be processed
          //
          roles.add(
            featureName.substring(
              SystemAttributes.ROLE_PREFIX.length(), 
              featureName.indexOf(':', SystemAttributes.ROLE_PREFIX.length())
            )
          );
      } else if(featureName.startsWith(SystemAttributes.VIEW_PREFIX)){
          //
          // Remember the views to be processed
          //
          views.add(
            featureName.substring(
              SystemAttributes.VIEW_PREFIX.length(), 
              featureName.indexOf(':', SystemAttributes.VIEW_PREFIX.length())
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
          new BasicException.Parameter[]{
            new BasicException.Parameter("class", classDef),
            new BasicException.Parameter("attribute name", featureName)
          },
          "attribute not found in class"
        );        
        String multiplicity = (String)featureDef.values("multiplicity").get(0);
        if(model.isReferenceType(featureDef)) {
          ModelElement_1_0 referencedEnd = model.getElement(
            featureDef.values("referencedEnd").get(0)
          );
          if(referencedEnd.values("qualifierType").size() > 0) {
            multiplicity = Multiplicities.LIST;
          }
          // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
          // in case <<list>> semantic is required it must be modeled as 
          // aggregation none, multiplicita 0..1, numeric qualifier
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
          target.objSetValue(
            featureName,
            featureValues.size() > 0 
              ? marshaller.marshal(featureValues.get(0))
              : null
          );
        }
        
        // SINGLE_VALUE
        else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
          target.objSetValue(
            featureName,
            marshaller.marshal(featureValues.get(0))
          );
        }
        
        // STREAM
        else if(Multiplicities.STREAM.equals(multiplicity)) {
          LargeObject_1_0 targetValue = target.objGetLargeObject(featureName);
          Object sourceValue = featureValues.get(0); 
          if (sourceValue == null){
            targetValue.setBinaryStream(null,0);
          } else if(sourceValue instanceof InputStream) {
            targetValue.setBinaryStream((InputStream)sourceValue,-1);
          } else if (sourceValue instanceof Reader) {
            targetValue.setCharacterStream((Reader)sourceValue,-1);
          } else {
            throw new ServiceException(
              BasicException.Code.DEFAULT_DOMAIN,
              BasicException.Code.NOT_SUPPORTED, 
              new BasicException.Parameter[]{
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("attribute name", featureName)
              },
              "Stream value not supported. Supported are [Reader|InputStream]"
            );        
          }
        }
        
        // LIST
        else if(Multiplicities.MULTI_VALUE.equals(multiplicity) || Multiplicities.LIST.equals(multiplicity)) {
          List targetValue = target.objGetList(featureName);
          setObjectValues(
            featureValues,
            marshaller,
            targetValue,
            replace
          );
        }
        
        // SET
        else if(Multiplicities.SET.equals(multiplicity)) {
          Set targetValue = target.objGetSet(featureName);
          setObjectValues(
            featureValues,
            marshaller,
            targetValue,
            replace
          );
        }
        
        // SPARSEARRAY
        else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
          SortedMap targetValue = target.objGetSparseArray(featureName);
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
            new BasicException.Parameter[]{
              new BasicException.Parameter("multiplicity", multiplicity),
              new BasicException.Parameter("attribute name", featureName)
            },
            "multiplicity not supported. Supported are [0..1|1..1|0..n|list|set|sparsearray]"
          );        
        }
      }
    } catch (RuntimeException exception) {
        new RuntimeServiceException(exception).printStackTrace();
    }
    
    /**
     * Process contexts
     */
    byte[] digest = source.getDigest();
    if(digest != null) contexts.add(SystemAttributes.LOCK_CONTEXT);
    if(! contexts.isEmpty()) {
      FilterableMap container = target.objGetContainer(SystemAttributes.CONTEXT_CAPABLE_CONTEXT);
      for(
        Iterator i = contexts.iterator();
        i.hasNext();
      ){
        String name = (String)i.next();
        Object_1_0 object = (Object_1_0)container.get(name); 
        if(object == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter(SystemAttributes.CONTEXT_CAPABLE_CONTEXT,name)
            },
            "This embedded object can't be created or modified"
        );
        if(SystemAttributes.LOCK_CONTEXT.equals(name)){
           object.objSetValue(SystemAttributes.OBJECT_DIGEST, digest);
        }else{
           toObject(
              SystemAttributes.CONTEXT_PREFIX + name,
              source,
              object,
              objectCache,
              objectFactory,
              model, 
              replace
           );
        }
      }
    }

    /**
     * Process roles
     */
    if(! roles.isEmpty()) {
        FilterableMap container = target.objGetContainer(SystemAttributes.ROLE_CAPABLE_ROLE);
        for(
          Iterator i = roles.iterator();
          i.hasNext();
        ){
          String name = (String)i.next();
          Object_1_0 object = (Object_1_0)container.get(name); 
          if(object == null){
              String roleClass = (String) source.getValues(
                  SystemAttributes.ROLE_PREFIX + name + ':' + SystemAttributes.OBJECT_CLASS
              ).get(0); 
              if(roleClass == null) throw new ServiceException(
                  BasicException.Code.DEFAULT_DOMAIN,
                  BasicException.Code.BAD_PARAMETER,
                  new BasicException.Parameter[]{
                      new BasicException.Parameter(SystemAttributes.ROLE_CAPABLE_ROLE,name)
                  },
                  "Role class missing" 
              );
              //... Handle nested roles
              if(objectFactory instanceof ObjectFactory_1_1){
                  object = ((ObjectFactory_1_1)objectFactory).createObject(roleClass, name, target);
              } else throw new ServiceException(
                  BasicException.Code.DEFAULT_DOMAIN,
                  BasicException.Code.NOT_SUPPORTED,
                  new BasicException.Parameter[]{
                      new BasicException.Parameter(SystemAttributes.ROLE_CAPABLE_ROLE,name),
                      new BasicException.Parameter("objectFactoryClass", objectFactory.getClass().getName())
                  },
                  "This object factory does not support role creation" 
              );
          }
          toObject(
            SystemAttributes.ROLE_PREFIX + name + ':',
            source,
            object,
            objectCache,
            objectFactory,
            model, 
            replace
          );
        }
      }

    /**
     * Process views
     */
    if(! views.isEmpty()) {
        FilterableMap container = target.objGetContainer(SystemAttributes.VIEW_CAPABLE_VIEW);
        for(
          Iterator i = views.iterator();
          i.hasNext();
        ){
          String name = (String)i.next();
          Object_1_0 object = (Object_1_0)container.get(name); 
          if(object == null) throw new ServiceException(
              BasicException.Code.DEFAULT_DOMAIN,
              BasicException.Code.NOT_SUPPORTED,
              new BasicException.Parameter[]{
                  new BasicException.Parameter(SystemAttributes.VIEW_CAPABLE_VIEW,name)
              },
              "This embedded object can't be created or modified"
          );
          toObject(
            SystemAttributes.VIEW_PREFIX + name,
            source,
            object,
            objectCache,
            objectFactory,
            model, 
            replace
          );
        }
      }
    
  }
  
  //------------------------------------------------------------------------
  public static Structure_1_0 toStructure(
    DataproviderObject source,
      Map objectCache,
    ObjectFactory_1_0 objectFactory,
    Model_1_0 model
  ) throws ServiceException {
    List targetFields = new ArrayList();
    List targetValues = new ArrayList();
    String typeName = (String)source.getValues(SystemAttributes.OBJECT_CLASS).get(0);
      Marshaller marshaller = new ToObjectMarshaller(objectCache, objectFactory);
    for(
      Iterator i = source.attributeNames().iterator();
      i.hasNext();
    ) {
      String fieldName = (String)i.next();
      
      if(!SystemAttributes.OBJECT_CLASS.equals(fieldName)) {

        /**
         * Get attribute definition from model. The attribute multiplicity is
         * required to invoke objGetValue(), objSetValue() correctly
         */
        ModelElement_1_0 classDef = model.getElement(typeName);
        ModelElement_1_0 fieldDef = model.getFeatureDef(
          classDef,
          fieldName,
          true
        ); 
        if(fieldDef == null) {
          throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_MEMBER_NAME, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("class", classDef),
              new BasicException.Parameter("attribute name", fieldName)
            },
            "attribute not found in class"
          );        
        }
        String multiplicity = (String)fieldDef.values("multiplicity").get(0);
        if(model.isReferenceType(fieldDef)) {
          ModelElement_1_0 referencedEnd = model.getElement(
            fieldDef.values("referencedEnd").get(0)
          );
          if(referencedEnd.values("qualifierType").size() > 0) {
            multiplicity = Multiplicities.LIST;
          }
          // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
          // in case <<list>> semantic is required it must be modeled as 
          // aggregation none, multiplicita 0..1, numeric qualifier
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
          targetFields.add(fieldName);
          targetValues.add(
            source.values(fieldName).size() > 0 
              ? marshaller.marshal(source.values(fieldName).get(0))
              : null
          );
        }
        
        // SINGLE_VALUE
        else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
          targetFields.add(fieldName);
          targetValues.add(
            marshaller.marshal(source.values(fieldName).get(0))
          );
        }
        
        // LIST
        else if(Multiplicities.MULTI_VALUE.equals(multiplicity) || Multiplicities.LIST.equals(multiplicity)) {
          targetFields.add(fieldName);
          List targetValue = new ArrayList();
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
          targetFields.add(fieldName);
          Set targetValue = new HashSet();
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
          targetFields.add(fieldName);
          SortedMap targetValue = new TreeMap();
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
            new BasicException.Parameter[]{
              new BasicException.Parameter("multiplicity", multiplicity),
              new BasicException.Parameter("attribute name", fieldName)
            },
            "multiplicity not supported. Supported are [0..1|1..1|0..n|list|set|sparsearray]"
          );        
        }
      }
    }
    return objectFactory.createStructure(
      typeName,
      targetFields,
      targetValues
    );
  }

}

//--- End of File -----------------------------------------------------------
