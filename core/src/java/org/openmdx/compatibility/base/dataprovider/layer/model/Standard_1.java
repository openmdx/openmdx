/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Standard_1.java,v 1.48 2008/06/28 00:21:28 hburger Exp $
 * Description: Model layer Standard_1 plugin
 * Revision:    $Revision: 1.48 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:28 $
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
package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DatatypeFormat;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.spi.SystemOperations;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.PrimitiveTypes;

/**
 * Standard implementation for model layer.
 * 
 * What is done here:
 * <ul>
 * <li> replacement of find requests for OBJECT_INSTANCE_OF by OBJECT_CLASS IS_IN
 *      all subtypes of original class </li>
 * <li> set OBJECT_INSTANCE_OF attribute on DataproviderObjects bound for the
 *      application layer </li>
 * <li> remove derived attributes, except some technical ones. This also removes
 *      OBJECT_INSTANCE_OF on objects bound for the persistence layer. </li>
 * <li> conversion of strings to Path, where needed. </li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public class Standard_1 extends OptimisticLocking_1 {

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.OptimisticLocking_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        super.activate(id, configuration, delegation);
        //
        // Warnings
        //
        this.throwWarning = configuration.isOn(
            LayerConfigurationEntries.THROW_WARNING
        );
        //
        // Model
        //
        List models = configuration.values(SharedConfigurationEntries.MODEL);
        if (models.isEmpty()) {
            throw new ServiceException(
                StackedException.DEFAULT_DOMAIN,
                StackedException.INVALID_CONFIGURATION,
                null,
                "A model must be configured with option '" + 
                SharedConfigurationEntries.MODEL + 
                "'"
            );
        } else {
            this.model = (Model_1_0)models.get(0);
        }
        //
        // XML Datatype Usage
        //
        this.datatypeFormat = configuration.isOn(
            SharedConfigurationEntries.XML_DATATYPES
        ) ? DatatypeFormat.newInstance(true) : null;
        //
        // Notify preDelete
        //
        this.notifyPreDelete = configuration.isOn(
            LayerConfigurationEntries.NOTIFY_PRE_DELETE
        );
        
    }

    // --------------------------------------------------------------------------
    /**
     * Tells whether XML datatype formatting is required
     * @return
     */
    protected boolean useDatatypes(){
        return this.datatypeFormat != null;
    }
    
    // --------------------------------------------------------------------------
    /**
     * An object is freed from derived attributes in the RoleObject_1. But if an
     * object is retrieved and not returned to the client but only to a 
     * higher layer, and the same object or a copy of it is saved again, the 
     * derived attributes of this object have to be removed again. 
     * <p>
     * For example Standard itself adds the derived attribute object_instanceOf.
     * But there may be other derived attributes added in state or role.
     * <p> 
     * To avoid multiple scanning of the entire object, object_instanceOf is 
     * used as a trigger for a new scan for derived attributes. 
     * 
     * @param  object  object to remove derived attributes from 
     */
    private void triggeredRemoveDerivedAttributes(
        DataproviderObject object
    ) throws ServiceException {
        if(this.getObjectClassName(object) != null) {
            this.removeNonPersistentAttributes(object);
        }
    }
            
    // --------------------------------------------------------------------------
    /**
     * remove the attributes which are in the model as derived
     */
    protected void removeNonPersistentAttributes(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        boolean roleContained = false;
        String objectClassName = this.getObjectClassName(object);
        ModelElement_1_0 objClass = objectClassName == null ?
            null :
            this.model.getDereferencedType(objectClassName);
        ArrayList missingClassesForRoles = null; // used for error reporting
        if (object != null) {
          // System.out.println("\n objectClass: " + objectClassName);
          // create a role dependent map
          HashMap roleClasses = new HashMap();

          roleClasses.put(null, objClass);

          Map modelAttributes =
            objClass == null ? null : (Map)objClass.values("attribute").get(0);

          String attributeName = null;
          for (Iterator i = object.attributeNames().iterator(); i.hasNext();) {
            attributeName = (String)i.next();
            // System.out.println("     " +  attributeName +":"+ object.getValues(attributeName));     

            // remove derived attributes except the attributes listed below 
            // NOTE: This is a hack and must be fixed as soon as each layer can have 
            // its own model, i.e. persistence layer uses persistence model, application 
            // layer uses application model, etc.
            if (object.getValues(attributeName) != null
              && object.getValues(attributeName).size() > 0
              && !attributeName.equals(SystemAttributes.MODIFIED_AT)
              && !attributeName.equals(SystemAttributes.MODIFIED_BY)
              && !attributeName.equals(SystemAttributes.CREATED_AT)
              && !attributeName.equals(SystemAttributes.CREATED_BY)
              && !attributeName.equals(SystemAttributes.OBJECT_CLASS)
              && !attributeName.equals(State_1_Attributes.INVALIDATED_AT)
              && !attributeName.equals(AbstractState_1.STATE_NUMBER)
              && !attributeName.equals(AbstractState_1.ID_ATTRIBUTE_NAME)) {
              int sep = attributeName.indexOf("$");
              String role = null;
              if (sep > 0) { // there is a role
                role = attributeName.substring(0, sep);
                attributeName = attributeName.substring(sep + 1);
                roleContained = true;
              }

              // when roles are present the loading of the correct modelAttributes
              // has to be done each time
              if (roleContained) {
                objClass = (ModelElement_1_0)roleClasses.get(role);
                if (objClass == null
                  && !roleClasses.containsKey(role) // entry may be null
                ) {
                  if (object
                    .getValues(
                      role
                        + "$"
                        + SystemAttributes.OBJECT_CLASS)
                    != null) {
                    objectClassName =
                      (String)object
                        .getValues(
                          role
                            + "$"
                            + SystemAttributes.OBJECT_CLASS)
                        .get(0);
                  }
                  if (objectClassName != null) {
                    objClass = this.model.getDereferencedType(objectClassName);
                  }
                  roleClasses.put(role, objClass);
                }

                modelAttributes =
                  objClass == null
                    ? null
                    : (Map)objClass.values("attribute").get(0);
              }
              if (modelAttributes != null) {
                if (!SystemAttributes.OBJECT_CLASS.equals(attributeName)) {
                  ModelElement_1_0 attributeDef = (ModelElement_1_0)modelAttributes.get(attributeName);
                  // non-modeled attributes are not removed. 
                  // NOTE: This is a hack and must be fixed as soon as each layer can have 
                  // its own model, i.e. persistence layer uses persistence model, application 
                  // layer uses application model, etc.
                  if (attributeDef == null) {
                    if(attributeName.equals(SystemAttributes.OBJECT_INSTANCE_OF)) {
                      i.remove();
                    }
                  }
                  // remove derived attributes
                  else if(
                      (attributeDef.getValues("isDerived") != null) && 
                      ((Boolean)attributeDef.getValues("isDerived").get(0)).booleanValue()
                  ) {
                    i.remove();
                  }
                }
              }
              else {
                if (missingClassesForRoles == null) {
                  missingClassesForRoles = new ArrayList();
                }
                if (role != null && !missingClassesForRoles.contains(role)) {
                  missingClassesForRoles.add(role);
                }
              }
            }
            if (missingClassesForRoles != null) {
              ServiceException warning = new ServiceException(
                  StackedException.DEFAULT_DOMAIN,
                  StackedException.ASSERTION_FAILURE,
                  new BasicException.Parameter[] {
                     new BasicException.Parameter(
                      "roles",
                      missingClassesForRoles
                    )
                  },
                  "No class specified (missing object_class or role.object_class)."
              ).log();
              if (this.throwWarning) {
                throw warning;
              }
            }

          }
        }
    }
                    
    // --------------------------------------------------------------------------
    /**
     * Set object_instanceOf to the object
     */
    private void setInstanceOf(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        Set classes = getInstanceOf(object);
        if(classes != null) object.clearValues(
            SystemAttributes.OBJECT_INSTANCE_OF
        ).addAll(classes);
    }
  
    // --------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.SystemAttributes_1#instanceOfBasicObject(org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject)
     */
    protected boolean isInstanceOfBasicObject(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        Set classes = getInstanceOf(object);
        return classes != null ?
            classes.contains("org:openmdx:base:BasicObject") :
            super.isInstanceOfBasicObject(object);
    }

    // --------------------------------------------------------------------------
    /**
     * Evaluate an object's superclasses.
     * 
     * @param object
     * 
     * @return the object's class and superclasses.
     * 
     * @throws ServiceException 
     */
    protected Set getInstanceOf(
        DataproviderObject_1_0 object
    ) throws ServiceException{
      String objectClassName = getObjectClassName(object);
      //
      // objectClassName may be null if for the attribute selector said
      // no_attributes, just don't set instanceOf.
      //
      if(objectClassName == null) return null;
      ModelElement_1_0 objClass = this.model.getDereferencedType(objectClassName);
      if(objClass == null) throw new ServiceException(
            StackedException.DEFAULT_DOMAIN,
            StackedException.ASSERTION_FAILURE, 
            new BasicException.Parameter[]{
                new BasicException.Parameter("object", object),
                new BasicException.Parameter("class", objClass)
            },
            "class not found"
      ).log();

      // evaluate the set of classes
      Set classes = new HashSet();
      for (
          Iterator i = objClass.values("allSupertype").iterator();
          i.hasNext();
      ) classes.add(((Path)i.next()).getBase());
      return classes;
    }

    // --------------------------------------------------------------------------
    /** 
     * Convert attribtue values of type String to Path, if the model demands 
     * a Path.
     * <p>
     * All entries in the reference HashMap are converted.
     * <p>
     * <i><u>CR20006520</u><br>
     * Remove & Touch no longer works since some non-derived
     * attributes might not be in the default fetch set.</i>
     * 
     * @param object object to treat
     */
    private void convertPaths(
        DataproviderObject object
    ) throws ServiceException {
      ModelElement_1_0 baseObjClass = getObjectClass(object);
       if(
          baseObjClass != null &&
          model.isClassType(baseObjClass)
       ) {      
	      ModelElement_1_0 objectClass = null;      
	      for(
	        Iterator i = object.attributeNames().iterator();
	        i.hasNext();
	      ) {
	        String attributeName = (String)i.next();
//	        // no values --> remove attribute
//	        if(object.getValues(attributeName).size() == 0) {
//	          i.remove();
//	        }
//	        else {
            if(!object.getValues(attributeName).isEmpty()) {
	          String role = null;
	          int sep = attributeName.indexOf("$");
	          if (sep > 0) {
	            role = attributeName.substring(0,sep);
	            attributeName = attributeName.substring(sep + 1);
	          }                
	          if(role == null) {
	            objectClass = baseObjClass;
	          }
	          else {
	            try {
	              String attributeRoledObjectClass = role + "$" + SystemAttributes.OBJECT_CLASS;
	              if(object.getValues(attributeRoledObjectClass) == null) {
	                throw new ServiceException(
	                    StackedException.DEFAULT_DOMAIN,
	                    StackedException.ASSERTION_FAILURE, 
	                    new BasicException.Parameter[]{
	                      new BasicException.Parameter("object", object),
	                      new BasicException.Parameter("attribute role object class", attributeRoledObjectClass)
	                    },
	                    "object class not set for specified role"
	                );
	              }
	              objectClass = model.getDereferencedType(
	                object.getValues(attributeRoledObjectClass).get(0)
	              );
	            }
	            catch(ServiceException e) {
	              if(e.getExceptionCode() == StackedException.NOT_FOUND) {
	                System.out.println("not found");
	              }
	              throw e;
	            }
	          }
	          if(
	            !objectClass.values("reference").isEmpty() &&
	            ((HashMap) objectClass.values("reference").get(0)).containsKey(attributeName) &&
	            (object.getValues(attributeName) != null)
	          ) {
	            // it must be a path, convert all values
	            for(
	              ListIterator v = object.getValues(attributeName).populationIterator();
	              v.hasNext(); 
	            ) {
	              Object value = v.next();
	              if(value instanceof String) {
	                v.set(new Path((String) value));
	              }
	            }
	          }
	        }
	      }
       }
    }
                
    // --------------------------------------------------------------------------
    /** 
     * Convert values of type path to the their string representation if 
     * they are of type string or anyURI.
     * 
     * @param  object to treat
     */
    private boolean convertPathValues(
        DataproviderObject object
    ) throws ServiceException {

        Object value = null;
    
        // Tells if the object contains any paths in attributes, in which case 
        // the persistence layer is still returning paths and the conversion 
        // from strings to path must not be tried
        boolean containsPath = false;
        boolean doConvert = false; // the attribute's values must be converted
        boolean doConvertSet = false; // indicates if doConvert has been set
    
        // iterate through the attributes values until a path is found
        // then check for the right type
    
        for(String attributeName: object.attributeNames()) {
          doConvert = false;
          doConvertSet = false;
          for (ListIterator v =
            object.getValues(attributeName).populationIterator();
            v.hasNext() && !(doConvertSet && !doConvert);
            // the values must stay as path
          ) {
            value = v.next();
            if (value instanceof Path) {
              containsPath = true;
              if (!doConvertSet) {
                doConvertSet = true;
                doConvert = isOfTypePath(attributeName, object);
              }
              if (doConvert) {
                v.set(((Path)value).toUri());
              }
    
            }    
          }
        }
        return containsPath;
    }
      
    // --------------------------------------------------------------------------
    private boolean isOfTypePath(
        String _attributeName, 
        DataproviderObject object
    ) throws ServiceException {
        String attributeName = _attributeName;
        ModelElement_1_0 type = null;
        boolean doConvert = false;
    
        // get object class. In case of attribute name with namespace
        // prefix get object_class attribute of same namespace    
        List typeNameList = null;
        // TODO: alternate namespace separator for compatibility mode
        if(attributeName.indexOf("$") != -1) {
          typeNameList = object.getValues(
            attributeName.substring(
              0,
              attributeName.lastIndexOf("$") + 1
            ) + SystemAttributes.OBJECT_CLASS
          );
        }    
        else if(attributeName.indexOf(":") != -1) {
          typeNameList = object.getValues(
            attributeName.substring(
              0,
              attributeName.lastIndexOf(":") + 1
            ) + SystemAttributes.OBJECT_CLASS
          );
        }
        else {
          typeNameList = object.getValues(SystemAttributes.OBJECT_CLASS);
        }
    
        // get unqualified attribute name
        attributeName = attributeName.substring(
          attributeName.lastIndexOf(":") + 1
        );
        attributeName = attributeName.substring(
          attributeName.lastIndexOf("$") + 1
        );
    
        if(typeNameList != null && !typeNameList.isEmpty()) {
          if(typeNameList.get(0) == null) {
              throw new ServiceException(
                  StackedException.DEFAULT_DOMAIN,
                  StackedException.ASSERTION_FAILURE,
                  new BasicException.Parameter[] {
                      new BasicException.Parameter("object", object),
                      new BasicException.Parameter("attribute", attributeName)
                  },
                  SystemAttributes.OBJECT_CLASS + " must not be null"
              );
          }
          type = model.getDereferencedType(typeNameList.get(0));
          if (type != null) {
            Boolean nonPathType = attributeIsInstanceOf(
              (Map)type.values("attribute").get(0),
              attributeName,
              NON_PATH_TYPES
            );
            if(nonPathType == null) {
                if("object_stateId".equals(attributeName)) {
                    doConvert = true;
                } else {
                    throw new ServiceException(
                        StackedException.DEFAULT_DOMAIN,
                        StackedException.ASSERTION_FAILURE,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("attribute", attributeName),
                            new BasicException.Parameter("object_class", typeNameList)
                        },
                        "Unknown attribute for object class."
                    );
                }            
            } else {
                if(nonPathType.booleanValue()) doConvert = true;
            }
          }
        }
        return doConvert;   
    }        

    // --------------------------------------------------------------------------
    protected Boolean attributeIsInstanceOf(
        Map attributeDefs,
        String attributeName,
        Collection candidates
    ) throws ServiceException {
        ModelElement_1_0 attributeType = (ModelElement_1_0) attributeDefs.get(attributeName);
        return attributeType == null ? null : Boolean.valueOf(
            candidates.contains(
                model.getDereferencedType(
                    attributeType.values("type").get(0)
                ).values(
                    "qualifiedName"
                ).get(
                    0
               )
            )
        );
    }
  
    // --------------------------------------------------------------------------
    protected boolean attributeMightBeInstanceOfAnXMLDatatype(
        Map attributeDefs,
        String attributeName
    ) throws ServiceException {
        Boolean xmlDatatype = attributeIsInstanceOf(
            attributeDefs,
            attributeName,
            XML_DATATYPE_TYPES
        );
        return xmlDatatype == null ? (
            SystemAttributes.CREATED_AT.equals(attributeName) ||
            SystemAttributes.MODIFIED_AT.equals(attributeName)
        ) : xmlDatatype.booleanValue();
    }

    // --------------------------------------------------------------------------

    /**
     * Set the derived attribute 'identity' in case the class supports this feature,
     * e.g. ch:omex:generic:BasicObject
     * 
     * @param request
     * @param object
     */
    protected void setIdentity(
        DataproviderRequest request, 
        DataproviderObject object
    ) throws ServiceException {
      if(object.getValues(SystemAttributes.OBJECT_CLASS) != null) {
          String objectClass = (String)object.getValues(SystemAttributes.OBJECT_CLASS).get(0);
          if(
              this.model.isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable") &&
              (!object.attributeNames().contains(SystemAttributes.OBJECT_IDENTITY) || this.model.isSubtypeOf(objectClass, "org:openmdx:compatibility:role1:Role"))                      
          ) {
              object.clearValues(SystemAttributes.OBJECT_IDENTITY).add(
                  object.path().toXri()
              );
          }
      }      
    }

    // --------------------------------------------------------------------------
    /**
     * Touches all object features with object.values(featureName) for all 
     * non-derived features. This way the feature is added to the set
     * of attributes of object. This allows to minimize roundtrips for v3
     * clients.
     * @param touchNonDerivedFeatures 
     * @param objectFeaturesToBeVerified 
     */
    private void adjustEmptyFeatureSet(
        DataproviderObject object,
        String viewPrefix, 
        boolean touchNonDerivedFeatures, 
        Set objectFeaturesToBeVerified
    ) throws ServiceException {
        String viewClassAttribute = viewPrefix + SystemAttributes.OBJECT_CLASS;
        SparseList viewClassValues = object.getValues(viewClassAttribute);
        ModelElement_1_0 classDef = null;
        if(viewClassValues == null) {
          classDef = null;
        } else {
          objectFeaturesToBeVerified.remove(viewClassAttribute);
          try {
            ModelElement_1_0 elementDef = this.model.getElement(
              viewClassValues.get(0)
            ); 
            classDef = ModelAttributes.STRUCTURE_TYPE.equals(
                elementDef.values(SystemAttributes.OBJECT_CLASS).get(0)
            ) ? null : elementDef;
          } catch(ServiceException e){
            classDef = null;
          }
        }
        if(classDef == null) {
          for(
            Iterator i = objectFeaturesToBeVerified.iterator();
            i.hasNext();
          ){
            if(attributeBelongsToView(viewPrefix, (String) i.next())) {
             i.remove();
            }
          }
        } else {
          // Cache scoped features. This reduces string allocations
          Map scopedFeatures = (Map)this.scopedFeatures.get(viewPrefix);
          if(scopedFeatures == null) {
            this.scopedFeatures.put(
              viewPrefix,
              scopedFeatures = new HashMap()
            );
          }
          String scopedInstanceOf = (String)scopedFeatures.get(
            SystemAttributes.OBJECT_INSTANCE_OF
          );
          if(scopedInstanceOf == null) {
            scopedFeatures.put(
              SystemAttributes.OBJECT_INSTANCE_OF,
              scopedInstanceOf = viewPrefix + SystemAttributes.OBJECT_INSTANCE_OF
            );
          }
          objectFeaturesToBeVerified.remove(scopedInstanceOf);
          for(
            Iterator i = ((Map)classDef.values("allFeature").get(0)).values().iterator();
            i.hasNext();
          ) {
            ModelElement_1_0 featureDef = (ModelElement_1_0)i.next();
            boolean touch;
            boolean attribute;
            if(
              this.model.isAttributeType(featureDef)
            ) {
              attribute = true;
              touch = touchNonDerivedFeatures && !((Boolean)featureDef.values("isDerived").get(0)).booleanValue();
            } else if (
              this.model.isReferenceType(featureDef) && 
              this.model.referenceIsStoredAsAttribute(featureDef)
            ) {
              attribute = true;
              touch = touchNonDerivedFeatures && !this.model.referenceIsDerived(featureDef); 
            } else {
              attribute = false;
              touch = false;
            }
            if(attribute) {
              String feature = (String) featureDef.values("name").get(0);
              String scopedFeature = (String)scopedFeatures.get(feature);
              if(scopedFeature == null) {
                scopedFeatures.put(
                  feature,
                  scopedFeature = viewPrefix + feature
                );
              }
              if(touch) {
                object.values(scopedFeature);
              }
              objectFeaturesToBeVerified.remove(scopedFeature);
              if(State_1_Attributes.INVALIDATED_AT.equals(feature)) {
                scopedFeature = (String)scopedFeatures.get(AbstractState_1.STATE_NUMBER);
                if(scopedFeature == null) {
                  scopedFeatures.put(
                    AbstractState_1.STATE_NUMBER,
                    scopedFeature = viewPrefix + AbstractState_1.STATE_NUMBER
                  );
                }
                objectFeaturesToBeVerified.remove(scopedFeature);
                scopedFeature = (String)scopedFeatures.get(AbstractState_1.ID_ATTRIBUTE_NAME);
                if(scopedFeature == null) {
                  scopedFeatures.put(
                    AbstractState_1.ID_ATTRIBUTE_NAME,
                    scopedFeature = viewPrefix + AbstractState_1.ID_ATTRIBUTE_NAME
                  );
                }
                objectFeaturesToBeVerified.remove(scopedFeature);
              }
            }
          }
        }
    }

    // --------------------------------------------------------------------------
    private boolean attributeBelongsToView(
        String viewPrefix,
        String attributeName
    ) {
        int i = attributeName.lastIndexOf(':');
        if("".equals(viewPrefix)) {
            return i < 0;
        } 
        else {
            return i > 0 && viewPrefix.equals(attributeName.substring(0, i));  
        }
    }

    // --------------------------------------------------------------------------
    private void adjustEmptyFeatureSet(
        DataproviderObject object, 
        boolean touchNonDerivedFeatures
    ) throws ServiceException {
        Set views = new HashSet();
        Set roles = new HashSet();
        views.add("");
        Set objectFeaturesToBeVerified = new HashSet(object.attributeNames());
        for(
          Iterator i = objectFeaturesToBeVerified.iterator();
          i.hasNext();
        ) {
          String attributeName = (String)i.next();
          int j = attributeName.lastIndexOf(':');
          if(j>=0) {
              views.add(attributeName.substring(0,j+1));
          } else {
              j = attributeName.indexOf('$');
              if(j > 0) {
                  roles.add(attributeName.substring(0,j+1));
              }
          }
        }
        for(
          Iterator i = views.iterator();
          i.hasNext();
        ) {
          this.adjustEmptyFeatureSet(
            object,
            (String)i.next(), 
            touchNonDerivedFeatures, 
            objectFeaturesToBeVerified
          );
        }
        for(
            Iterator i = roles.iterator();
            i.hasNext();
          ) {
            this.adjustEmptyFeatureSet(
              object,
              (String)i.next(), 
              false, // touchNonDerivedFeatures 
              objectFeaturesToBeVerified
            );
          }
        if(!objectFeaturesToBeVerified.isEmpty()) {
          object.attributeNames().removeAll(objectFeaturesToBeVerified);
        }
    }
  
    //--------------------------------------------------------------------------
    /**
     * Converting<ul>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.Duration</code>
     * </ul>values to <code>String</code> values.
     * 
     * @param object the <code>DataproviderObject</code> to be converted 
     * 
     * @throws ServiceException 
     */
    private void convertXMLDatatypeValues(
        DataproviderObject_1_0 object
    ) throws ServiceException{
        for(String attributeName: (Set<String>)object.attributeNames()) {
            for(
                ListIterator j = object.values(attributeName).populationIterator();
                j.hasNext();
            ) {
                Object value = j.next();
                if(
                    value instanceof Duration || 
                    value instanceof XMLGregorianCalendar
                ) {
                    j.set(this.datatypeFormat.unmarshal(value));
                } else {
                    break;
                }
            }
        }
    }
  
    // --------------------------------------------------------------------------
    /**
     * Set known derived features and do some v2 -> v3 compatibility handling.
     * <p>
     * <i><u>CR20006520</u><br>
     * Remove & Touch no longer works since some non-derived
     * attributes might not be in the default fetch set.</i>
     */
    protected void completeObject(
        DataproviderRequest request,
        DataproviderObject object
    ) throws ServiceException {
        // first try converting paths to strings if necessary
        if(!this.convertPathValues(object)) {
          this.convertPaths(object);                
        }
        this.setInstanceOf(object);
        this.setIdentity(request, object);
        this.adjustEmptyFeatureSet(
            object, 
            request.attributeSelector() == AttributeSelectors.ALL_ATTRIBUTES
        );
        if(useDatatypes()) this.convertXMLDatatypeValues(object);
    }
  
    // --------------------------------------------------------------------------
    protected DataproviderReply completeReply(
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        DataproviderObject[] objects = reply.getObjects();
        for(
          int i = 0, iLimit = objects.length;
          i < iLimit;
          i++ 
        ) {
          this.completeObject(
              request,
              objects[i]
          );
        }
        return reply;
    }
  
    // --------------------------------------------------------------------------
    protected DataproviderRequest prepareRequest(
        DataproviderRequest request
    ) throws ServiceException {
        if(
            this.datatypeFormat != null &&
            request.object() != null
        ) {
            DataproviderObject object = request.object();
            String objectType = getObjectClassName(object);
            if(objectType == null) {
                objectType = (String) request.context(
                    DataproviderRequestContexts.OBJECT_TYPE
                ).get(
                    0
                );
            }
            if(
                !SystemOperations.GET_BINARY_STREAM_ARGUMENTS.equals(objectType) &&
                !SystemOperations.GET_CHARACTER_STREAM_ARGUMENTS.equals(objectType)
            ) {
                ModelElement_1_0 objectClass = objectType == null ? 
                    null : 
                    this.model.getDereferencedType(objectType);
                Map featureDefs = objectClass == null ?
                    null :
                    (Map) objectClass.values("allFeature").get(0);
                if(featureDefs != null) { 
                    for(
                        Iterator i = object.attributeNames().iterator();
                        i.hasNext();
                    ) {
                        String attributeName = (String) i.next();
                        if(
                            attributeMightBeInstanceOfAnXMLDatatype(
                                featureDefs,
                                attributeName
                            )            
                        ) {
                            for (
                                ListIterator j = object.values(attributeName).populationIterator();
                                j.hasNext();                        
                            ) {
                                j.set(
                                    this.datatypeFormat.marshal(j.next())                            
                                );
                            }
                        }
                    }
                }
            }
        }
        if(request.operation() == DataproviderOperations.ITERATION_START) {        
            DataproviderRequest findRequest = request;
            List mappedFilterProperties = new ArrayList();
            int typeIndex = -1;
            ModelElement_1_0 classDef = null;
            for(
                int i = 0;
                i < request.attributeFilter().length;
                i++
            ) {
                FilterProperty requestedFilterProperty = request.attributeFilter()[i];
                if(SystemAttributes.OBJECT_INSTANCE_OF.equals(requestedFilterProperty.name())) {
                    //
                    // Add all subtypes to OBJECT_CLASS
                    //
                    if(
                        (requestedFilterProperty.operator() == FilterOperators.IS_IN) &&
                        (requestedFilterProperty.quantor()  == Quantors.THERE_EXISTS) &&
                        (requestedFilterProperty.getValues().length == 1)  
                    ) {
                        classDef = this.model.getDereferencedType(requestedFilterProperty.getValue(0));
                        Set subClasses = new HashSet();
                        // Adding the filter property OBJECT_CLASS for BasicObject typically results
                        // in a long list of subclasses which is expensive to process for database 
                        // systems. Eliminating the BasicObject filter could result in returning objects
                        // which are not instance of BasicObject. However this should never happen because
                        // BasicObject's and non-BasicObject's must never be mixed in the same database table.
                        if(
                            (classDef != null) && 
                            !"org:openmdx:base:BasicObject".equals(classDef.values("qualifiedName").get(0))
                        ) {
                            typeIndex = i;
                            Path path = null;                          
                            for(
                                Iterator subIter = classDef.values("allSubtype").iterator();
                                subIter.hasNext();
                            ) {
                                path = (Path) subIter.next(); 
                                subClasses.add(path.getBase());
                            }                           
                            mappedFilterProperties.add(
                                new FilterProperty(
                                    Quantors.THERE_EXISTS ,
                                    SystemAttributes.OBJECT_CLASS,  
                                    FilterOperators.IS_IN,
                                    subClasses.toArray()
                                )
                            );
                        } 
                        else {
                            SysLog.info(
                                "Skipping filter property OBJECT_INSTANCE_OF", 
                                requestedFilterProperty.getValue(0)
                            );
                        }
                    } 
                    else {
                        throw new UnsupportedOperationException(
                            getClass().getName() + 
                            " supports 'THERE_EXISTS object_instanceOf IS_IN' clauses with with exactly one class only"
                        );
                    }                                     
                } 
                else {
                    mappedFilterProperties.add(
                        requestedFilterProperty
                    );  
                }
            }
            if (this.datatypeFormat != null) {
                if(classDef == null) {
                    String objectType = (String) request.context(
                        DataproviderRequestContexts.OBJECT_TYPE
                    ).get(0);
                    if(objectType != null) classDef = model.getDereferencedType(
                        objectType
                    );
                    if(classDef == null) {
                        classDef = this.model.getTypes(request.path())[2];
                    }
                }
                if(classDef != null) {
                    Map featureDefs = (Map)classDef.values("allFeature").get(0); 
                    if (featureDefs != null) {
                        for(int i = 0; i < mappedFilterProperties.size(); i++) {
                            if(i != typeIndex) {
                                FilterProperty requestedFilterProperty = request.attributeFilter()[i];
                                if(this.attributeMightBeInstanceOfAnXMLDatatype(featureDefs, requestedFilterProperty.name())) {
                                    Object[] requestValues = requestedFilterProperty.getValues();
                                    Object[] mappedValues = new Object[requestValues.length];
                                    for(int j = 0; j < requestValues.length; j++) {
                                        mappedValues[j] = this.datatypeFormat.marshal(requestValues[j]);
                                    }
                                    mappedFilterProperties.set(
                                        i,
                                        new FilterProperty(
                                            requestedFilterProperty.quantor(),
                                            requestedFilterProperty.name(),
                                            requestedFilterProperty.operator(),
                                            mappedValues
                                        )
                                    );
                                }
                            }
                        }
                    }
                }
            }
            findRequest = new DataproviderRequest(
                new DataproviderObject(request.path()),
                DataproviderOperations.ITERATION_START,
                (FilterProperty[])mappedFilterProperties.toArray(new FilterProperty[mappedFilterProperties.size()]),
                request.position(),
                request.size(),
                request.direction(),
                request.attributeSelector(),
                request.attributeSpecifier()
            );
            findRequest.contexts().putAll(
                request.contexts()
            );
            return findRequest;
        } 
        else {
            return request;
        }
    }

    // --------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return this.completeReply(
            request,
            super.get(header,prepareRequest(request))
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        return completeReply(
            request,
            super.find(header,prepareRequest(request))
        );
    }
        
    // --------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.triggeredRemoveDerivedAttributes(request.object());
        return completeReply(
            request,
            super.create(header,prepareRequest(request))
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.triggeredRemoveDerivedAttributes(request.object());
        return completeReply(
            request,
            super.modify(header,prepareRequest(request))
        );
    }    

    //--------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.triggeredRemoveDerivedAttributes(request.object());
        return completeReply(
            request,
            super.replace(header,prepareRequest(request))
        );
    }

    //--------------------------------------------------------------------------
    /**
     * Called before given object is deleted. 
     */
    protected void notifyPreDelete(
        Path objectIdentity
    ) {
        //
    }
    
    // --------------------------------------------------------------------------
    private void deleteComposites(
        ServiceHeader header,
        DataproviderObject_1_0 _object
    ) throws ServiceException {
        DataproviderObject_1_0 object = _object;
        String objectClass = this.getObjectClassName(object);
        if(objectClass == null) {
            object = super.get(
                header,
                new DataproviderRequest(
                    new DataproviderObject(object.path()),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                    null
                )
            ).getObject();
            objectClass = this.getObjectClassName(object);
        }
        Map references = (Map)this.model.getElement(
            objectClass
        ).values("reference").get(0);
        for(
            Iterator i = references.values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 featureDef = (ModelElement_1_0)i.next();
            ModelElement_1_0 referencedEnd = this.model.getElement(
                featureDef.values("referencedEnd").get(0)
            );
            boolean referenceIsCompositeAndChangeable = 
                this.model.isReferenceType(featureDef) &&
                AggregationKind.COMPOSITE.equals(referencedEnd.values("aggregation").get(0)) &&
                ((Boolean)referencedEnd.values("isChangeable").get(0)).booleanValue();
            if(referenceIsCompositeAndChangeable) {
                String reference = (String)featureDef.values("name").get(0);
                DataproviderObject[] composites = super.find(
                    header,
                    new DataproviderRequest(
                        new DataproviderObject(object.path().getChild(reference)),
                        DataproviderOperations.ITERATION_START,
                        null,
                        0,
                        Integer.MAX_VALUE,
                        Directions.ASCENDING,
                        AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                        null
                    )
                ).getObjects();
                List compositeIdentities = new ArrayList();
                // Remove composites of composites
                for(
                    int j = 0;
                    j < composites.length;
                    j++
                ) {
                    DataproviderObject composite = composites[j];
                    this.deleteComposites(
                        header,
                        composite
                    );
                    compositeIdentities.add(
                        composite.path()
                    );
                }
                // Remove composites
                for(
                    Iterator j = compositeIdentities.iterator();
                    j.hasNext();
                ) {
                    Path compositeIdentity = (Path)j.next();
                    if(this.notifyPreDelete) {
                        this.notifyPreDelete(
                            compositeIdentity
                        );
                    }
                    super.remove(
                        header,
                        new DataproviderRequest(
                            new DataproviderObject(compositeIdentity),
                            DataproviderOperations.OBJECT_REMOVAL,
                            AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                            null
                        )
                    );
                }
            }
        }
    }
  
    // --------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(this.notifyPreDelete) {
            this.deleteComposites(
                header,
                request.object()
            );
            this.notifyPreDelete(
                request.path()
            );
        }
        return completeReply(
            request,
            super.remove(
                header,
                this.prepareRequest(request)
            )
        );
    }

    // --------------------------------------------------------------------------
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return completeReply(
            request,
            super.operation(header,prepareRequest(request))
        );
    }
  
    //--------------------------------------------------------------------------
    protected String getObjectClassName(
        DataproviderObject_1_0 object
    ){
        if (object != null) {
            //
            // OBJECT_CLASS may be null in delete operations
            //
            SparseList objectClassAttribute = object.getValues(SystemAttributes.OBJECT_CLASS);
            if (objectClassAttribute != null) return (String)objectClassAttribute.get(0);
        }
        return null;
    }
    
    //--------------------------------------------------------------------------
    protected ModelElement_1_0 getObjectClass(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        String objectClassName = getObjectClassName(object);
        return objectClassName == null ?
            null :
            this.model.getDereferencedType(objectClassName);
    }
    
    // --------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.BeforeImageCachingLayer_1#getBeforeImage(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    protected DataproviderObject_1_0 getBeforeImage(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderObject_1_0 object = super.getBeforeImage(header, request);
        if(useDatatypes()) this.convertXMLDatatypeValues(object);
        return object;
    }

    //--------------------------------------------------------------------------
    private Model_1_0 model = null;

    /**
     * 2-dim map with entries <view, <feature name, scoped feature name>> 
     * In order to reduce String operations in touchNonDerivedFeatures()
     * the map contains scoped feature names. 
     */
    private final Map scopedFeatures = new HashMap();
    
    /**
     * Tells whether warnings should be propagated to the client
     * 
     * @see LayerConfigurationEntries#THROW_WARNING
     */
    private boolean throwWarning;
  
    /**
     * If true, calls notifyPreDelete() before an object is removed.
     * notifiyPreDelete() is called for the removed object and recursively
     * for each of its composite objects.   
     */
    private boolean notifyPreDelete;
    
    /**
     * Not <code>null</code> if <code>String</code> values for<ol>
     * <li><code>org::w3c::date</code>
     * <li><code>org::w3c::dateTime</code>
     * <li><code>org::w3c::duration</code>
     * </ol>should be converted to their corresponding XML datatypes<ol>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.XMLGregorianCalendar</code>
     * <li><code>javax.xml.Duration</code>
     * </ol>and vice versa.
     * 
     * @see LayerConfigurationEntries#XML_DATATYPES
     */
    private DatatypeFormat datatypeFormat;
    
    private final static Collection NON_PATH_TYPES = Arrays.asList(
        new String[]{
            PrimitiveTypes.STRING,
            PrimitiveTypes.ANYURI
        }
    );

    private final static Collection XML_DATATYPE_TYPES = Arrays.asList(
        new String[]{
            PrimitiveTypes.DATE,
            PrimitiveTypes.DATETIME,
            PrimitiveTypes.DURATION
        }
    );

}
