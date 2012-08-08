/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Strict_1.java,v 1.4 2009/06/14 00:28:38 wfro Exp $
 * Description: Strict_1 class performing type checking of DataproviderRequest/DataproviderReply
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/14 00:28:38 $
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
package org.openmdx.application.dataprovider.layer.type;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.application.mof.repository.accessor.ModelElement_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

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
@SuppressWarnings("unchecked")
public class Strict_1
    extends OperationAwareLayer_1 
{

    //---------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {

        super.activate(
            id, 
            configuration, 
            delegation
        );

        // verifyReply
        this.verifyReply = 
            configuration.values(LayerConfigurationEntries.VERIFY_REPLY).isEmpty() ||
            Boolean.TRUE.equals(configuration.values(LayerConfigurationEntries.VERIFY_REPLY).get(0));

        // allowEnumerationOfChildren
        this.allowEnumerationOfChildren = configuration.isOn(LayerConfigurationEntries.ALLOW_ENUMERATION_OF_CHILDREN);

        // genericTypes
        this.genericTypes = configuration.values(
            LayerConfigurationEntries.GENERIC_TYPE_PATH
        );

        // initialize genericObjectType to BasicObject to ensure that the 
        // system attributes are present.
        this.basicObjectClassDef = new ModelElement_1(
            getModel().getDereferencedType("org:openmdx:base:BasicObject")
        );
    }

    //---------------------------------------------------------------------------
    private void removeForeignAndDerivedAttributes(
        MappedRecord object,
        ModelElement_1_0 typeDef,
        boolean removeDerived,
        boolean allowChangeable
    ) throws ServiceException {   
        try {
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(object);
            // remove derived attributes but not SystemAttributes
            if(this.getModel().isClassType(typeDef)) {
                Map structuralFeatureDefs = getModel().getStructuralFeatureDefs(
                    typeDef, false, true, !this.allowEnumerationOfChildren
                );
                for(
                    Iterator i = facade.getValue().keySet().iterator();
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
                                (!featureDef.objGetList("isDerived").isEmpty()) && 
                                ((Boolean)featureDef.objGetValue("isDerived")).booleanValue();
                            isChangeable = 
                                (!featureDef.objGetList("isChangeable").isEmpty()) && 
                                ((Boolean)featureDef.objGetValue("isChangeable")).booleanValue();          
                            isForeign = false;
                        }
                        boolean isSystemAttribute = (
                            SystemAttributes.CREATED_AT.equals(featureName) ||
                            SystemAttributes.REMOVED_AT.equals(featureName) ||
                            SystemAttributes.CREATED_BY.equals(featureName) ||
                            SystemAttributes.REMOVED_BY.equals(featureName) ||
                            SystemAttributes.VERSION.equals(featureName) 
                        ) || (
                            (SystemAttributes.MODIFIED_AT.equals(featureName) || SystemAttributes.MODIFIED_BY.equals(featureName)) &&
                            !isState1BasicState(typeDef)
                        );
                        // Authority, Provider, Segment
                        boolean isBaseObject = facade.getPath().size() <= 5;
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
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }
    
    /**
     * Test whether the given type is an org::openmdx::compatibility::state1::BasicState instance
     * 
     * @param typeDef
     * 
     * @return <code>true</code> if typeDef represents an org::openmdx::compatibility::state1::BasicState instance
     * @throws ServiceException
     */
    private boolean isState1BasicState(
    	ModelElement_1_0 typeDef
    ) throws ServiceException{
    	for(Object superTypeId : typeDef.objGetList("allSupertype")) {
    		if(STATE1_BASIC_STATE_CLASS.equals(((Path)superTypeId).getBase())) {
    			return true;
    		}
    	}
    	return false;
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
     * This method asserts that the object contains only supported primitive types 
     */
    private void assertBasicTypesOnly(
        MappedRecord object
    ) throws ServiceException {
        try {
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(object);
            for(
                Iterator i = facade.getValue().keySet().iterator();
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                SparseList attributeValues = facade.getAttributeValues(attributeName);
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
                            !(value instanceof Date) &&
                            !(value instanceof XMLGregorianCalendar) &&
                            !(value instanceof byte[]) &&
                            !(value instanceof InputStream) &&
                            !(value instanceof Path)
                        ) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE, 
                                "DataproviderObject must only contain primitive types [String|Number|Boolean|Path|byte[]|InputStream]",
                                new BasicException.Parameter("object", object),
                                new BasicException.Parameter("attribute", attributeName),
                                new BasicException.Parameter("value class", (value == null ? null : value.getClass().getName())),
                                new BasicException.Parameter("value", value)
                            );          
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    private void completeAndVerifyReplyObject(
        MappedRecord object, 
        short attributeSelector,
        AttributeSpecifier[] specifiers
    ) throws ServiceException {
        try {
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(object);
            // remove all attributes if any present silently. Do not complain anymore
            if(attributeSelector == AttributeSelectors.NO_ATTRIBUTES) {
                facade.getValue().keySet().clear();    
                return;   
            }
    
            this.removeForeignAndDerivedAttributes(
                object,
                this.getObjectClass(object),
                false,
                true
            );
            this.assertBasicTypesOnly(
                object
            );
            if(this.verifyReply) {
                getModel().verifyObject(
                    object, 
                    ObjectHolder_2Facade.getObjectClass(object),
                    null,
                    false,
                    !this.allowEnumerationOfChildren
                );
            }
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    private ModelElement_1_0 getObjectClass(
        MappedRecord object
    ) throws ServiceException {
        String objectClass = ObjectHolder_2Facade.getObjectClass(object);
        if(objectClass == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "attribute " + SystemAttributes.OBJECT_CLASS + " missing",
                new BasicException.Parameter("object", object)
            );
        }
        ModelElement_1_0 typeDef = null;
        if(this.isGenericTypePath(ObjectHolder_2Facade.getPath(object))) {
            typeDef = this.basicObjectClassDef; 
        }
        else {        
            typeDef = getModel().getDereferencedType(objectClass);
        }
        return typeDef;
    }

    //---------------------------------------------------------------------------
    private DataproviderReply completeAndVerifyReply(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        MappedRecord[] objects = reply.getObjects();
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
        try {
            MappedRecord object = request.object();
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(request.object());
            ModelElement_1_0 objClassDef = this.getObjectClass(object);
            SysLog.trace("create object", object);
            SysLog.trace("create objClass", objClassDef.objGetValue("qualifiedName"));
            // remove all attributes with empty value list
            Set attributeNames = facade.getValue().keySet();
            List attributesToBeRemoved = new ArrayList();
            for(
                Iterator i = attributeNames.iterator();
                i.hasNext();
            ) {
                String attributeName = (String)i.next();
                if(
                    facade.getValue().get(attributeName) == null || 
                    (facade.getValue() instanceof SparseList && ((SparseList)facade.getValue().get(attributeName)).isEmpty())
                ) {
                    attributesToBeRemoved.add(attributeName);
                }
            }
            facade.getValue().keySet().removeAll(attributesToBeRemoved);
            // check whether referenced type matches objClass
            if(!STATE1_STATE_CAPABLE_CLASS.equals(objClassDef.objGetValue("qualifiedName"))) {    
                ModelElement_1_0 typeDef = getModel().getTypes(facade.getPath())[2];
                if(!getModel().objectIsSubtypeOf(object, typeDef)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "object not instance of type",
                        new BasicException.Parameter(
                            "object class", 
                            objClassDef == null ? null : objClassDef.jdoGetObjectId()
                        ),
                        new BasicException.Parameter(
                            "type", 
                            typeDef == null ? null : typeDef.jdoGetObjectId()
                        )
                    );      
                }
            }
            this.removeForeignAndDerivedAttributes(
                object, 
                objClassDef, 
                true, 
                true
            );
            getModel().verifyObject(
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
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        MappedRecord object = request.object();
        ModelElement_1_0 objClassDef = this.getObjectClass(object);
        if(!STATE1_STATE_CAPABLE_CLASS.equals(objClassDef.objGetValue("qualifiedName"))) {    
            //
            // check whether referenced type matches objClass
            //
            ModelElement_1_0 typeDef = getModel().getTypes(ObjectHolder_2Facade.getPath(object))[2];
            if(!getModel().objectIsSubtypeOf(object, typeDef)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "object not instance of type",
                    new BasicException.Parameter("object class", objClassDef),
                    new BasicException.Parameter("type", typeDef)
                );      
            }
        }
        this.removeForeignAndDerivedAttributes(
            object, 
            objClassDef, 
            true, 
            false
        );
        getModel().verifyObject(
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
        MappedRecord object = request.object();
        ModelElement_1_0 objClassDef = this.getObjectClass(object);
        // check whether referenced type matches objClass
        ModelElement_1_0 typeDef = this.getModel().getTypes(ObjectHolder_2Facade.getPath(object))[2];
        if(!this.getModel().objectIsSubtypeOf(object, typeDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "object not instance of type",
                new BasicException.Parameter("object class", objClassDef),
                new BasicException.Parameter("type", typeDef)
            );      
        }
        this.removeForeignAndDerivedAttributes(
            object, 
            objClassDef, 
            true, 
            false
        );
        this.getModel().verifyObject(
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
        MappedRecord object = request.object();
        ModelElement_1_0 objClassDef = getObjectClass(object);
        if(!STATE1_STATE_CAPABLE_CLASS.equals(objClassDef.objGetValue("qualifiedName"))) {    
            //
            // check whether referenced type matches objClass
            //
            ModelElement_1_0 typeDef = this.getModel().getTypes(ObjectHolder_2Facade.getPath(object))[2];
            if(!getModel().objectIsSubtypeOf(object, typeDef)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "object not instance of type",
                    new BasicException.Parameter("object class", objClassDef),
                    new BasicException.Parameter("type", typeDef)
                );      
            }
        }
        this.removeForeignAndDerivedAttributes(
            object, 
            objClassDef, 
            true, 
            false
        );
        this.getModel().verifyObject(
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
     * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#otherOperation(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest, java.lang.String, org.openmdx.compatibility.base.naming.Path)
     */
    protected MappedRecord otherOperation(
        ServiceHeader header,
        DataproviderRequest request,
        String operation, Path replyPath
    ) throws ServiceException {
        MappedRecord object = request.object();
        //
        // rewrite namespace '../view:<namespaceId>:<operationName>/<requestId>' 
        // to .../view/<namespaceId>/<operationName>/<requestId>. This way the
        // path matches the model
        //
        Path operationPath = request.path();
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
        ModelElement_1_0 targetClassDef = getModel().getTypes(
            operationPath.getPrefix(operationPath.size()-2
            ))[2];
        ModelElement_1_0 inParamDef = null;
        //
        // collect all types which could contain operation
        //
        Collection allTypes = new HashSet();
        for(Iterator i = targetClassDef.objGetList("allSubtype").iterator(); i.hasNext(); ) {
            ModelElement_1_0 subtype = getModel().getElement(i.next());
            allTypes.addAll(
                subtype.objGetList("allSupertype")
            );
        }
        //  
        // lookup operation
        //
        for(
            Iterator i = allTypes.iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 classDef = getModel().getDereferencedType(i.next());
            ModelElement_1_0 operationDef = null;
            try {
                operationDef = getModel().getElement(
                    classDef.objGetValue("qualifiedName") + ":" + operationName
                );
            }
            catch(Exception e) {
                // ignore
            }
            if(
                (operationDef != null) &&
                this.getModel().isOperationType(operationDef)
            ) {
                // lookup parameters definition
                for(
                    Iterator j = operationDef.objGetList("content").iterator();
                    j.hasNext();
                ) {
                    ModelElement_1_0 e = getModel().getElement(j.next());
                    if("in".equals(e.objGetValue("name"))) {
                        inParamDef = e;
                    }
                }
                // operation found where type in parameter matches the request's in parameter
                if(getModel().getElementType(inParamDef) == inParamTypeDef) {
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
                "no matching operation found for request",
                new BasicException.Parameter("request", request.path()),
                new BasicException.Parameter("in param type", inParamTypeDef)
            );      
        }
        //
        // validate parameter
        //
        this.getModel().verifyObject(
            object, 
            inParamTypeDef,
            null,
            true
        );
        return null;
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private List genericTypes = null;
    private ModelElement_1_0 basicObjectClassDef = null;
    private boolean verifyReply = true;
    private boolean allowEnumerationOfChildren = false;
    public static String STATE1_STATE_CAPABLE_CLASS = "org:openmdx:compatibility:state1:StateCapable";
    public static String STATE1_BASIC_STATE_CLASS = "org:openmdx:compatibility:state1:BasicState";

}

//--- End of File -----------------------------------------------------------
