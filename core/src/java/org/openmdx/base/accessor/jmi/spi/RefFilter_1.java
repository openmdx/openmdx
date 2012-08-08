/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefFilter_1.java,v 1.23 2008/05/13 08:54:06 wfro Exp $
 * Description: RefFilter_1 class
 * Revision:    $Revision: 1.23 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/13 08:54:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefPackage;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_1;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;

/**
 * RefFilter_1_0 implementation
 */
public class RefFilter_1
  implements RefFilter_1_1 {

    //-------------------------------------------------------------------------
    public RefFilter_1(
        RefPackage_1_0 refPackage,
        String filterType,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers
    ) {
        this.filterProperties = filterProperties == null
            ? new ArrayList<FilterProperty>()
            : new ArrayList<FilterProperty>(Arrays.asList(filterProperties));
        this.attributeSpecifiers = attributeSpecifiers == null
            ? new ArrayList<AttributeSpecifier>()
            : new ArrayList<AttributeSpecifier>(Arrays.asList(attributeSpecifiers));
        this.filterType = filterType;
        this.filterProperties.add(
            this.filterTypeProperty = new FilterProperty(
                Quantors.THERE_EXISTS,
                SystemAttributes.OBJECT_INSTANCE_OF,
                FilterOperators.IS_IN,
                new String[]{filterType}
            )
        );
        this.refPackage = refPackage;
    }

    //-------------------------------------------------------------------------
    private Model_1_0 getModel(
    ) {
        return this.refPackage.refModel();
    }

    // -------------------------------------------------------------------------
    final private ModelElement_1_0 getFeature(
        String featureName
    ) throws ServiceException {

        // full-qualified feature name. Lookup in model
        if (featureName.indexOf(':') >= 0) {
            return this.getModel().getElement(featureName);
        }
        // get all features of class and find feature with featureName
        else {
            ModelElement_1_0 feature = this.getModel().getFeatureDef(
                this.getModel().getElement(this.filterType),
                featureName,
                false
            );
            if (feature == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_FOUND,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("class name", this.filterType),
                        new BasicException.Parameter("feature", featureName)
                    },
                    "feature not found"
                ); 
            }
            return feature;
        }
    }
  
  //-------------------------------------------------------------------------
  private void assertAttributeOrReferenceStoredAsAttribute(
    ModelElement_1_0 elementDef
  ) throws ServiceException {

    if(
      !elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE) &&
      !this.getModel().referenceIsStoredAsAttribute(elementDef)
    ) {
      throw new ServiceException (
        StackedException.DEFAULT_DOMAIN,
        StackedException.ASSERTION_FAILURE,
        new BasicException.Parameter [] {
          new BasicException.Parameter("model element", elementDef)
        },
        "model element not of type " + ModelAttributes.ATTRIBUTE + " and not " + ModelAttributes.REFERENCE + " stored as attribute"
      );
    }
  }

  //-------------------------------------------------------------------------
  public void refAddValue(
    ModelElement_1_0 featureDef,
    short quantor,
    short operator,
    Collection<?> value
  ) {

    try {
      this.assertAttributeOrReferenceStoredAsAttribute(featureDef);
      String featureName = (String)featureDef.values("name").get(0);
      
      SysLog.trace("feature", featureName);
      SysLog.trace("quantor", Quantors.toString(quantor));
      SysLog.trace("operator", FilterOperators.toString(operator));
      SysLog.trace("value", value);

      if(this.getModel().isReferenceType(featureDef)) {
        if("org:openmdx:base:ContextCapable:context".equals(featureDef.values("qualifiedName").get(0))) {
            if(
                quantor == Quantors.THERE_EXISTS &&
                operator == FilterOperators.IS_IN 
            ){
                Model_1_0 m = getModel();
                int ii = 0;
                for(
                  Iterator<?> i = value.iterator();
                  i.hasNext();
                  ii++
                ) {
                    Object c = i.next();
                    if(c instanceof RefObject_1_0){
                        RefObject_1_0 e = (RefObject_1_0) c;
                        String objectClass = e.refClass().refMofId();
                        String namespace = featureName + ':' + uuidGenerator().next() + ':';
                        if(m.isSubtypeOf(objectClass, "org:openmdx:base:Context")) {
                            filterProperties.add(
                                new FilterProperty(
                                    Quantors.PIGGY_BACK,
                                    namespace + SystemAttributes.OBJECT_CLASS,
                                    FilterOperators.PIGGY_BACK,
                                    new String[]{objectClass}
                                )
                            );
                            for(
                                Iterator<String> j = e.refDefaultFetchGroup().iterator();
                                j.hasNext();
                            ){
                                String attribute = j.next();
                                Object v = e.refGetValue(attribute);
                                filterProperties.add(
                                    new FilterProperty(
                                        Quantors.PIGGY_BACK,
                                        namespace + attribute,
                                        FilterOperators.PIGGY_BACK,
                                        v instanceof Collection ?
                                            ((Collection<?>)v).toArray() :
                                            new Object[]{v}
                                    )
                                );
                            }
                        } else throw new ServiceException (
                            StackedException.DEFAULT_DOMAIN,
                            StackedException.ASSERTION_FAILURE,
                            new BasicException.Parameter [] {
                              new BasicException.Parameter("quantor", Quantors.toString(quantor)),
                              new BasicException.Parameter("feature", featureName),
                              new BasicException.Parameter("operator", FilterOperators.toString(operator)),
                              new BasicException.Parameter("index", ii),
                              new BasicException.Parameter("class", objectClass)
                            },
                            "Object can't be piggy backed as context unless it is an instance of org::openmdx::base::Context"
                        );
                    }
                }
            } else throw new ServiceException (
                StackedException.DEFAULT_DOMAIN,
                StackedException.NOT_SUPPORTED,
                new BasicException.Parameter [] {
                  new BasicException.Parameter("quantor", Quantors.toString(quantor)),
                  new BasicException.Parameter("feature", featureName),
                  new BasicException.Parameter("operator", FilterOperators.toString(operator))
                },
                "The context feature supports piggy backing with 'THERE EXISTS context EQUAL TO' clauses only"
            );
        } else {
            List<Path> paths = new ArrayList<Path>();
            for(
              Iterator<?> i = value.iterator();
              i.hasNext();
            ) {
              Object v = i.next();
              if(v instanceof RefObject_1_0){
                  RefObject_1_0 e = (RefObject_1_0) v;
                  String objectClass = e.refClass().refMofId();
                  Model_1_0 m = getModel();
                  if(
                      m.isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable") &&
                      m.isSubtypeOf(objectClass, "org:openmdx:compatibility:state1:BasicState") &&
                      JDOHelper.isPersistent(e) &&
                      !JDOHelper.isNew(e) &&
                      !JDOHelper.isDeleted(e)
                  ) try {
                      paths.add(new Path((String)e.refGetValue(SystemAttributes.OBJECT_IDENTITY)));
                  } catch (Exception exception) {
                      paths.add(e.refGetPath());
                  } else {
                      paths.add(e.refGetPath());
                  }
              } else if (v instanceof Path){
                  paths.add((Path)v);
              } else {
                  paths.add(new Path((String)v));
              }
            }
            filterProperties.add(
              new FilterProperty(
                quantor,
                featureName,
                operator,
                paths.toArray()
              )
            );
        }   
      } else if(this.getModel().isAttributeType(featureDef)) {
        ModelElement_1_0 featureType = this.getModel().getElement(featureDef.values("type").get(0));
        FilterProperty p = null;

        // dateTime
        if(PrimitiveTypes.DATETIME.equals(featureType.values("qualifiedName").get(0))) {
          p = new FilterProperty(
            quantor,
            featureName,
            operator,
            new MarshallingList<Object>(
              DateTimeMarshaller.getInstance(false),
              new ArrayList<Object>(value)
            ).toArray()
          );
        }

        // date
        else if(PrimitiveTypes.DATE.equals(featureType.values("qualifiedName").get(0))) {
          p = new FilterProperty(
            quantor,
            featureName,
            operator,
            new MarshallingList<Object>(
              DateMarshaller.getInstance(false),
              new ArrayList<Object>(value)
            ).toArray()
          );
        }

        // duration
        else if(PrimitiveTypes.DURATION.equals(featureType.values("qualifiedName").get(0))) {
          p = new FilterProperty(
            quantor,
            featureName,
            operator,
            new MarshallingList<Object>(
              DurationMarshaller.getInstance(false),
              new ArrayList<Object>(value)
            ).toArray()
          );
        }

        // other primitive types require no unmarshalling
        else {
          p = new FilterProperty(
            quantor,
            featureName,
            operator,
            value.toArray()
          );
        }
        this.filterProperties.add(p);
      }

      // unsupported feature type
      else {
        throw new ServiceException (
          StackedException.DEFAULT_DOMAIN,
          StackedException.ASSERTION_FAILURE,
          new BasicException.Parameter [] {
            new BasicException.Parameter("feature", featureName)
          },
          "unsupported feature type. Must be [Attribute|Reference]"
        );
      }
    }
    catch(ServiceException e) {
      throw new JmiServiceException(e);
    }
  }

  //-------------------------------------------------------------------------
  public void refAddValue(
    ModelElement_1_0 featureDef,
    int index, 
    short order
  ) {

    try {
      assertAttributeOrReferenceStoredAsAttribute(featureDef);
      SysLog.trace("feature", featureDef);
      SysLog.trace("order", Orders.toString(order));
      this.attributeSpecifiers.add(
        new AttributeSpecifier(
          (String)featureDef.values("name").get(0),
          index,
          order
        )
      );
    }
    catch(ServiceException e) {
      throw new JmiServiceException(e);
    }
  }

    //-------------------------------------------------------------------------
    public Object refGetOrder(
        String fieldName
    ){
        try {
            return refGetOrder(
                this.getFeature(fieldName)
            );
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetOrder(
        ModelElement_1_0 featureDef
    ) {
        String multiplicity = (String)featureDef.values("multiplicity").get(0);
        return 
            Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
            Multiplicities.OPTIONAL_VALUE.equals(multiplicity) 
                ? (Object)new SimpleTypeOrder_1(this, featureDef) 
                : new MultivaluedTypeOrder_1(this, featureDef);
    }
  
    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        String fieldName
    ){
        try {
            return refGetPredicate(
                this.getFeature(fieldName)
            );
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        ModelElement_1_0 featureDef
    ){
        String multiplicity = (String)featureDef.values("multiplicity").get(0);
        return Multiplicities.SINGLE_VALUE.equals(multiplicity) ? refGetPredicate(
            Quantors.THERE_EXISTS, // Quantors.FOR_ALL would give the same result but is very inefficient
            featureDef
        ) : Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ? (Object) new OptionalAttributePredicate_1(
            this,
            featureDef
        ) : new MultiValuedAttributePredicate_1(
            this,
            featureDef
        );
    }
  
    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        short quantor,
        String fieldName
    ) {
        try {
            return refGetPredicate(
                quantor,
                this.getFeature(fieldName)
          );
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetPredicate(
        short quantor,
        ModelElement_1_0 featureDef
    ){
      try {
          String name = (String) featureDef.values("qualifiedName").get(0);
          ModelElement_1_0 typeDef = this.getModel().getDereferencedType(
              this.getModel().getElement(featureDef.values("type").get(0))
          );
          String type = (String) typeDef.values("qualifiedName").get(0);
          if(this.getModel().isPrimitiveType(typeDef)) {
              return PrimitiveTypes.BOOLEAN.equals(type) ? (AbstractPredicate_1) new BooleanTypePredicate_1(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.STRING.equals(type) ? new StringTypePredicate_1(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.ANYURI.equals(type) ? new ResourceIdentifierTypePredicate_1<URI>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.DATETIME.equals(type) ? new ComparableTypePredicate_1<Date>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.DECIMAL.equals(type) ? new ComparableTypePredicate_1<BigDecimal>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.INTEGER.equals(type) ? new ComparableTypePredicate_1<BigInteger>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.LONG.equals(type) ? new ComparableTypePredicate_1<Long>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.SHORT.equals(type) ? new ComparableTypePredicate_1<Short>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.DATE.equals(type) ? new PartiallyOrderedTypePredicate_1<XMLGregorianCalendar>(
                  this,
                  featureDef,
                  quantor
              ) : PrimitiveTypes.DURATION.equals(type) ? new PartiallyOrderedTypePredicate_1<Duration>(
                  this,
                  featureDef,
                  quantor
              ) : new SimpleTypePredicate_1(
                  this,
                  featureDef,
                  quantor
              ); 
          } else {
              RefPackage outermostPackage = this.refPackage.refOutermostPackage();
              RefPackage_1_1 filterPackage = (RefPackage_1_1)outermostPackage.refPackage(
                 type.substring(0, type.lastIndexOf(':'))
              );
              return filterPackage.refCreateFilter(
                  type,
                  null,
                  null,
                  this, 
                  new Short(quantor), 
                  name
              );
          }
      } catch (ServiceException exception) {
          throw new JmiServiceException(exception);
      }
  }
  
    //-------------------------------------------------------------------------
    // RefFilter_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public void clear(
    ) {
        this.filterProperties.clear();
        this.attributeSpecifiers.clear();
        this.filterProperties.add(this.filterTypeProperty);
    }

    //-------------------------------------------------------------------------
    public Collection<FilterProperty> refGetFilterProperties(
    ) {
        return this.filterProperties;
    }

    //-------------------------------------------------------------------------
    public Collection<AttributeSpecifier> refGetAttributeSpecifiers(
    ) {
        return this.attributeSpecifiers;
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        String fieldName,
        short quantor,
        short operator,
        Collection<?> value
    ) {
        try {
            this.refAddValue(
                this.getFeature(fieldName),
                quantor,
                operator,
                value
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        String fieldName,
        short quantor,
        short operator,
        RefFilter_1_0 filter
    ) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        String fieldName,
        short order
    ) throws JmiException {
        try {
            this.refAddValue(
                this.getFeature(fieldName),
                0, 
                order
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
        String fieldName,
        int index,
        short order
    ) throws JmiException {
        try {
            this.refAddValue(
                this.getFeature(fieldName),
                index, 
                order
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public String toString(
    ) {
        return
        "filter={" +
        "filterProperties=" + this.filterProperties + ", " +
        "attributeSpecifiers=" + this.attributeSpecifiers +
        "}";
    }
   
    //-------------------------------------------------------------------------
    private static UUIDGenerator uuidGenerator(
    ){
        return RefFilter_1.uuidGenerator == null 
            ? RefFilter_1.uuidGenerator = UUIDs.getGenerator() 
            : RefFilter_1.uuidGenerator;
    }
  
    //------------------------------------------------------------------------
    public FeatureMapper getFeatureMapper(
    ) {
        try {
            ModelElement_1_0 classDef = this.refPackage.refModel().getElement(this.filterType);   
            String qualifiedClassName = (String)classDef.values("qualifiedName").get(0);
            FeatureMapper featureMapper = featureMappers.get(qualifiedClassName);
            if(featureMapper == null) {
                String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
                String className = Identifier.CLASS_PROXY_NAME.toIdentifier(
                    (String)classDef.values("name").get(0)
                );
                Class<?> queryIntf = Classes.getApplicationClass(
                    packageName.replace(':', '.') + "." + Names.CCI2_PACKAGE_SUFFIX + "." + className + "Query"
                );
                featureMappers.putIfAbsent(
                    qualifiedClassName,
                    featureMapper = new FeatureMapper(
                        classDef,
                        queryIntf
                    )
                );
            }
            return featureMapper;
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }
      
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 5901724265321809315L;
    private static UUIDGenerator uuidGenerator = null;
    protected final static ConcurrentMap<String,FeatureMapper> featureMappers = 
        new ConcurrentHashMap<String,FeatureMapper>();

    private final String filterType;
    private final FilterProperty filterTypeProperty;
    private final List<FilterProperty> filterProperties;
    private final List<AttributeSpecifier> attributeSpecifiers;
    private final RefPackage_1_0 refPackage;
    
}

//--- End of File -----------------------------------------------------------
