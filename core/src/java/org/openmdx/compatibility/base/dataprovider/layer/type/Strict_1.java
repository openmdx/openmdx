/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Strict_1.java,v 1.16 2007/10/24 07:50:15 hburger Exp $
 * Description: Strict_1 class performing type checking of DataproviderRequest/DataproviderReply
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/24 07:50:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.layer.type;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1;
import org.openmdx.compatibility.base.dataprovider.spi.SystemOperations;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_1;
import org.openmdx.model1.accessor.basic.spi.ModelElement_1;

/** 
 * Layer_1 plugin which performs strict type checking on DataproviderReplys.
 * and DataproviderRequests.
 * <p>
 * The plugin accepts the following configuration options:
 * <p>
 * <ul>
 *   <li>modelPackage: Model_1_0 model accessor.</li>
 *   <li>verifyReply: true --> reply objects are verified for model compliance.</li>
 * </ul>
 */
public class Strict_1
  extends StreamOperationAwareLayer_1 {

  //---------------------------------------------------------------------------
  public void activate(
    short id,
    Configuration configuration,
    Layer_1_0 delegation
  ) throws Exception, ServiceException {
      
    super.activate(
      id, 
      configuration, 
      delegation
    );

    // get model
    List models = configuration.values(SharedConfigurationEntries.MODEL);
    if(models.isEmpty()) throw new ServiceException(
      BasicException.Code.DEFAULT_DOMAIN,
      BasicException.Code.INVALID_CONFIGURATION, 
      null,
      "missing model. Must be configured with options 'modelPackage' and 'packageImpl'"
    );
    this.model = (Model_1_1)models.get(0);
    
    // verifyReply
    this.verifyReply = configuration.values(LayerConfigurationEntries.VERIFY_REPLY).isEmpty() ?
      true :
      ((Boolean)configuration.values(LayerConfigurationEntries.VERIFY_REPLY).get(0)).booleanValue();

    // allowEnumerationOfChildren
    this.allowEnumerationOfChildren = configuration.isOn(LayerConfigurationEntries.ALLOW_ENUMERATION_OF_CHILDREN);
    
    // genericTypes
    this.genericTypes = configuration.values(
      LayerConfigurationEntries.GENERIC_TYPE_PATH
    );
    
    // initialize genericObjectType to BasicObject to ensure that the 
    // system attributes are present.
    this.basicObjectClassDef = new ModelElement_1(
      this.model.getDereferencedType("org:openmdx:base:BasicObject")
    );
  }
  
  //---------------------------------------------------------------------------
  private void removeForeignAndDerivedAttributes(
    DataproviderObject object,
    ModelElement_1_0 typeDef,
    boolean removeDerived,
    boolean allowChangeable
  ) throws ServiceException {   

    // remove derived attributes but not SystemAttributes
    if(this.model.isClassType(typeDef)) {
      Map structuralFeatureDefs = this.model.getStructuralFeatureDefs(
        typeDef, false, true, !this.allowEnumerationOfChildren
      );
      for(
        Iterator i = object.attributeNames().iterator();
        i.hasNext();
      ) {
        boolean isDerived = false;
        boolean isChangeable = true;
        boolean isForeign = true;
        
        String featureName = (String)i.next();
        
        // ignore namespaces
        if(featureName.indexOf(':') < 0) {
          ModelElement_1_0 featureDef = (ModelElement_1_0) structuralFeatureDefs.get(featureName);
          
          if (featureDef != null) {
              isDerived = 
                (featureDef.values("isDerived").size() > 0) && 
                ((Boolean)featureDef.values("isDerived").get(0)).booleanValue();
              isChangeable = 
                (featureDef.values("isChangeable").size() > 0) && 
                ((Boolean)featureDef.values("isChangeable").get(0)).booleanValue();          
              isForeign = false;
          }
          boolean isSystemAttribute = 
            SystemAttributes.CREATED_AT.equals(featureName)
            || SystemAttributes.MODIFIED_AT.equals(featureName)
            || SystemAttributes.CREATED_BY.equals(featureName)
            || SystemAttributes.MODIFIED_BY.equals(featureName);

          // Authority, Provider, Segment
          boolean isBaseObject = object.path().size() <= 5;
          if(
            !SystemAttributes.OBJECT_CLASS.equals(featureName)
            && ((isDerived && removeDerived) || (!isChangeable && !allowChangeable) || isForeign) 
            && (!isSystemAttribute || isBaseObject)
          ) {
            i.remove();
          }
        }
      }
    }
  }
  
  //---------------------------------------------------------------------------
  private boolean isGenericTypePath(
    Path objectPath  
  ) {
    for (
      Iterator i = this.genericTypes.iterator(); 
      i.hasNext(); 
    ) {
      if(objectPath.isLike((Path)i.next())) {
        return true;
      }
    }
    return false;
  }

  //---------------------------------------------------------------------------
  /**
   * This guard is to protect SPICE/2 transport in combination with the SPICE/3
   * Plugin_1. It guarantees that the DataproviderObject contains only well-known
   * SPICE/2 primitive types 
   */
  private void assertBasicTypesOnly(
    DataproviderObject object
  ) throws ServiceException {
    
    for(
      Iterator i = object.attributeNames().iterator();
      i.hasNext();
    ) {
      String attributeName = (String)i.next();
      SparseList attributeValues = object.getValues(attributeName);
      if(attributeValues != null) {
        for(
          Iterator j = attributeValues.iterator();
          j.hasNext();
        ) {
          Object value = j.next();
          if(
            (value != null) &&
            !(value instanceof String) &&
            !(value instanceof Number) &&
            !(value instanceof Boolean) &&
            !(value instanceof byte[]) &&
            !(value instanceof InputStream) &&
            !(value instanceof Path)
          ) {
            throw new ServiceException(
              BasicException.Code.DEFAULT_DOMAIN,
              BasicException.Code.ASSERTION_FAILURE, 
              new BasicException.Parameter[]{
                new BasicException.Parameter("object", object),
                new BasicException.Parameter("attribute", attributeName),
                new BasicException.Parameter("value class", (value == null ? null : value.getClass().getName())),
                new BasicException.Parameter("value", value)
              },
              "DataproviderObject must only contain primitive types [String|Number|Boolean|Path|byte[]|InputStream]"
            );          
          }
        }
      }
    }
  }
  
  //---------------------------------------------------------------------------
  private void completeAndVerifyReplyObject(
    DataproviderObject object, 
    short attributeSelector,
    AttributeSpecifier[] specifiers
  ) throws ServiceException {

    // remove all attributes if any present silently. Do not complain anymore
    if(attributeSelector == AttributeSelectors.NO_ATTRIBUTES) {
      object.attributeNames().clear();    
      return;   
    }
    
    this.removeForeignAndDerivedAttributes(
        object,
        getObjectClass(object),
        false,
        true
    );
    
    this.assertBasicTypesOnly(
      object
    );
    
    if(this.verifyReply) {
      this.model.verifyObject(
        object, 
        object.values(SystemAttributes.OBJECT_CLASS).get(0),
        null,
        false,
        !this.allowEnumerationOfChildren
      );
    }
  }

  //---------------------------------------------------------------------------
  private ModelElement_1_0 getObjectClass(
    DataproviderObject object
  ) throws ServiceException {
    
    if(
      !object.attributeNames().contains(SystemAttributes.OBJECT_CLASS) ||
      object.values(SystemAttributes.OBJECT_CLASS).size() == 0
    ) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("object", object)
        },
        "attribute " + SystemAttributes.OBJECT_CLASS + " missing"
      );
    }

    ModelElement_1_0 typeDef = null;
    if(isGenericTypePath(object.path())) {
      typeDef = this.basicObjectClassDef; 
    }
    else {        
      typeDef = this.model.getDereferencedType(
        object.values(SystemAttributes.OBJECT_CLASS).get(0)
      );
    }
    return typeDef;
  }
  
  //---------------------------------------------------------------------------
  private DataproviderReply completeAndVerifyReply(
    ServiceHeader header,
    DataproviderRequest request,
    DataproviderReply reply
  ) throws ServiceException {
    DataproviderObject[] objects = reply.getObjects();
    short attributeSelector = request.attributeSelector();
    AttributeSpecifier[] attributeSpecifier = request.attributeSpecifier();
    for (
      int i = 0;
      i < objects.length;
      i++
    ) {            
        this.completeAndVerifyReplyObject(
        objects[i], 
        attributeSelector, 
        attributeSpecifier
        );
    }
    return reply;
  }

  //---------------------------------------------------------------------------
  public DataproviderReply find(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    return this.completeAndVerifyReply(
      header,
      request,
      super.find(
        header, 
        request
      )
    );
  }
  
  //---------------------------------------------------------------------------
  public DataproviderReply get(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    return this.completeAndVerifyReply(
      header,
      request,
      super.get(
        header, 
        request
      )
    );
  }


  //---------------------------------------------------------------------------
  public DataproviderReply create(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
          
    DataproviderObject object = request.object();
    ModelElement_1_0 objClassDef = this.getObjectClass(object);

    SysLog.trace("create object", object);
    SysLog.trace("create objClass", objClassDef.values("qualifiedName"));

    // remove all attributes with empty value list
    Set attributeNames = object.attributeNames();
    List attributesToBeRemoved = new ArrayList();
    for(
      Iterator i = attributeNames.iterator();
      i.hasNext();
    ) {
      String attributeName = (String)i.next();
      if(object.getValues(attributeName).size() == 0) {
        attributesToBeRemoved.add(attributeName);
      }
    }
    object.attributeNames().removeAll(attributesToBeRemoved);
        
    // check whether referenced type matches objClass
    ModelElement_1_0 typeDef = this.model.getTypes(object.path())[2];
    if(!this.model.isSubtypeOf(object, typeDef)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("object class", objClassDef),
          new BasicException.Parameter("type", typeDef),
        },
        "object not instance of type"
      );      
    }
    this.removeForeignAndDerivedAttributes(
      object, 
      objClassDef, 
      true, 
      true
    );
    this.model.verifyObject(
      object, 
      objClassDef,
      null,
      false 
    );    
    return this.completeAndVerifyReply(
      header,
      request,
      super.create(
        header, 
        request
      )
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply modify(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    
    DataproviderObject object = request.object();
    ModelElement_1_0 objClassDef = this.getObjectClass(object);

    // check whether referenced type matches objClass
    ModelElement_1_0 typeDef = this.model.getTypes(object.path())[2];
    if(!this.model.isSubtypeOf(object, typeDef)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("object class", objClassDef),
          new BasicException.Parameter("type", typeDef),
        },
        "object not instance of type"
      );      
    }
    this.removeForeignAndDerivedAttributes(
      object, 
      objClassDef, 
      true, 
      false
    );
    this.model.verifyObject(
      object, 
      objClassDef,
      null,
      false 
    );    
    return this.completeAndVerifyReply(
      header,
      request,
      super.modify(
        header, 
        request
      )
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply set(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    
    DataproviderObject object = request.object();
    ModelElement_1_0 objClassDef = this.getObjectClass(object);

    // check whether referenced type matches objClass
    ModelElement_1_0 typeDef = this.model.getTypes(object.path())[2];
    if(!this.model.isSubtypeOf(object, typeDef)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("object class", objClassDef),
          new BasicException.Parameter("type", typeDef),
        },
        "object not instance of type"
      );      
    }
    this.removeForeignAndDerivedAttributes(
      object, 
      objClassDef, 
      true, 
      false
    );
    this.model.verifyObject(
      object, 
      objClassDef,
      null,
      false 
    );    
    return this.completeAndVerifyReply(
      header,
      request,
      super.set(
        header, 
        request
      )
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply replace(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    DataproviderObject object = request.object();
    ModelElement_1_0 objClassDef = getObjectClass(object);

    // check whether referenced type matches objClass
    ModelElement_1_0 typeDef = this.model.getTypes(object.path())[2];
    if(!this.model.isSubtypeOf(object, typeDef)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("object class", objClassDef),
          new BasicException.Parameter("type", typeDef),
        },
        "object not instance of type"
      );      
    }
    this.removeForeignAndDerivedAttributes(
      object, 
      objClassDef, 
      true, 
      false
    );
    this.model.verifyObject(
      object, 
      objClassDef,
      null,
      false 
    );    
    return this.completeAndVerifyReply(
      header,
      request,
      super.replace(
        header, 
        request
      )
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply remove(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
      return super.remove(
          header,
          request
      );
  }

  //---------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#getStreamOperation(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.io.OutputStream, long)
   */
  protected DataproviderObject getStreamOperation(
    ServiceHeader header,
    Path objectPath,
    String feature,
    OutputStream value, long position, Path replyPath
  ) throws ServiceException {
    try {
	  if(feature == null) throw new NullPointerException(
	    SystemOperations.GET_STREAM_FEATURE + " must not be null"
	  );
	  if(value == null) throw new NullPointerException(
	    SystemOperations.GET_STREAM_VALUE + " must not be null"
	  );
	  if(position < 0L) throw new IndexOutOfBoundsException(
	    SystemOperations.GET_STREAM_POSITION + "must not be negative"
	  );
    } catch (RuntimeException exception) {
	  throw new ServiceException(
	    exception, 
	    BasicException.Code.DEFAULT_DOMAIN,
	    BasicException.Code.ASSERTION_FAILURE,
	    new BasicException.Parameter[]{
	       new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, objectPath),
	       new BasicException.Parameter(SystemOperations.GET_STREAM_FEATURE, feature),
	       new BasicException.Parameter(SystemOperations.GET_STREAM_POSITION, position)
	    },
	    "Invalid binary stream retrieval arguments"
      );	
    }
    return null;
  }
  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#getStreamOperation(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.io.Writer, long)
   */
  protected DataproviderObject getStreamOperation(
    ServiceHeader header,
    Path objectPath,
    String feature,
    Writer value, long position, Path replyPath
  ) throws ServiceException {
    try {
  	  if(feature == null) throw new NullPointerException(
   	    SystemOperations.GET_STREAM_FEATURE + " must not be null"
   	  );
   	  if(value == null) throw new NullPointerException(
   	    SystemOperations.GET_STREAM_VALUE + " must not be null"
   	  );
   	  if(position < 0L) throw new IndexOutOfBoundsException(
   	    SystemOperations.GET_STREAM_POSITION + "must not be negative"
   	  );
    } catch (RuntimeException exception) {
   	  throw new ServiceException(
   	    exception, 
   	    BasicException.Code.DEFAULT_DOMAIN,
   	    BasicException.Code.ASSERTION_FAILURE,
   	    new BasicException.Parameter[]{
   	       new BasicException.Parameter(SystemAttributes.OBJECT_IDENTITY, objectPath),
   	       new BasicException.Parameter(SystemOperations.GET_STREAM_FEATURE, feature),
   	       new BasicException.Parameter(SystemOperations.GET_STREAM_POSITION, position)
   	    },
   	    "Invalid character stream retrieval arguments"
      );	
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#otherOperation(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest, java.lang.String, org.openmdx.compatibility.base.naming.Path)
   */
  protected DataproviderObject otherOperation(
    ServiceHeader header,
    DataproviderRequest request,
    String operation, Path replyPath
  ) throws ServiceException {
    DataproviderObject object = request.object();
    //
    // rewrite namespace '../view:<namespaceId>:<operationName>/<requestId>' 
    // to .../view/<namespaceId>/<operationName>/<requestId>. This way the
    // path matches the model
    //
    Path p = request.path();
    String featureName = p.get(p.size()-2);
    Path operationPath = null;
    if(featureName.startsWith(SystemAttributes.VIEW_PREFIX)) {
      String namespaceId = featureName.substring(5, featureName.lastIndexOf(':'));
      String operationName = featureName.substring(featureName.lastIndexOf(':') + 1);
      operationPath = p.getPrefix(p.size()-2).getDescendant(
        new String[]{
          SystemAttributes.VIEW_CAPABLE_VIEW,
          namespaceId, 
          operationName, 
          p.getBase()
        }
      );
    }
    else {
      operationPath = p;
    }    
    String operationName = operationPath.get(operationPath.size()-2);
    //
    // rewrite reference name with namespace to .../view/<namespaceId>/...
    //
    ModelElement_1_0 inParamTypeDef = this.getObjectClass(object);
    // 
    // Find operation and corresponding inParamDef and resultParamDef
    // which is to be invoked. The lookup is made based on the type name
    // of the parameter and the operation name.
    //  
    ModelElement_1_0 targetClassDef = this.model.getTypes(
      operationPath.getPrefix(operationPath.size()-2
    ))[2];
    ModelElement_1_0 inParamDef = null;
    //
    // collect all types which could contain operation
    //
    Collection allTypes = new HashSet();
    for(Iterator i = targetClassDef.values("allSubtype").iterator(); i.hasNext(); ) {
      ModelElement_1_0 subtype = this.model.getElement(i.next());
      allTypes.addAll(
        subtype.values("allSupertype")
      );
    }
    //  
    // lookup operation
    //
    for(
      Iterator i = allTypes.iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 classDef = this.model.getDereferencedType(i.next());
      ModelElement_1_0 operationDef = null;
      try {
        operationDef = this.model.getElement(
          classDef.values("qualifiedName").get(0) + ":" + operationName
        );
      }
      catch(Exception e) {
          // ignore
      }
      if(
        (operationDef != null) &&
        this.model.isOperationType(operationDef)
      ) {
        // lookup parameters definition
        for(
          Iterator j = operationDef.values("content").iterator();
          j.hasNext();
        ) {
          ModelElement_1_0 e = this.model.getElement(j.next());
          if("in".equals(e.values("name").get(0))) {
            inParamDef = e;
          }
        }
        // operation found where type in parameter matches the request's in parameter
        if(this.model.getDereferencedType(inParamDef.values("type").get(0)) == inParamTypeDef) {
          break;
        }
      }  
    }
    //     
    // no matching operation found
    //
    if(inParamDef == null) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("request", request.path()),
          new BasicException.Parameter("in param type", inParamTypeDef)
        },
        "no matching operation found for request"
      );      
    }
    //
    // validate parameter
    //
    this.model.verifyObject(
      object, 
      inParamTypeDef,
      null,
      true
    );
    return null;
  }
  
  /**
   * Retrieve model.
   *
   * @return Returns the model.
   */
  protected final Model_1_1 getModel() {
      return this.model;
  }
  
  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------
  private Model_1_1 model = null;
  private List genericTypes = null;
  private ModelElement_1_0 basicObjectClassDef = null;
  private boolean verifyReply = true;
  private boolean allowEnumerationOfChildren = false;


}

//--- End of File -----------------------------------------------------------
