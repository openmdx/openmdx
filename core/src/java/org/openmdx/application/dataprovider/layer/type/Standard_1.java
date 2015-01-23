/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Strict_1 class performing type checking of DataproviderRequest/DataproviderReply
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.application.mof.repository.accessor.ModelElement_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

/** 
 * Standard type plug-in
 * <p>
 * The plugin accepts the following configuration options:
 * <p>
 * <ul>
 *   <li>modelPackage: Model_1_0 model accessor.</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public class Standard_1 extends OperationAwareLayer_1 {

    //---------------------------------------------------------------------------
    public Standard_1(
    ) {
    }
    
    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
                
    //---------------------------------------------------------------------------
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        setUpGenericTypeSupport(configuration);
    }

	/**
     * @deprecated will not be supported by the dataprovider 2 stack
	 */
	private void setUpGenericTypeSupport(Configuration configuration)
			throws ServiceException {
		// genericTypes
        this.genericTypes = new ArrayList<Object>(
            configuration.values(
                LayerConfigurationEntries.GENERIC_TYPE_PATH
            ).values()
        );
        // initialize genericObjectType to BasicObject to ensure that the 
        // system attributes are present.
        this.basicObjectClassDef = new ModelElement_1(
            getModel().getDereferencedType("org:openmdx:base:BasicObject")
        );
	}

	/**
     * @deprecated will not be supported by the dataprovider 2 stack
	 */
    private boolean isGenericTypePath(
        Path objectPath  
    ) {
        for (
            Iterator<?> i = this.genericTypes.iterator(); 
            i.hasNext(); 
        ) {
            if(objectPath.isLike((Path)i.next())) {
                return true;
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------
    protected void completeAndVerifyReplyObject(
        MappedRecord object, 
        short attributeSelector
    ) throws ServiceException {
        try {
            if(attributeSelector == AttributeSelectors.NO_ATTRIBUTES) {
                // remove all attributes if any present silently. Do not complain anymore
                Object_2Facade.getValue(object).clear();    
                return;   
            }
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //---------------------------------------------------------------------------
    protected ModelElement_1_0 getObjectClass(
        MappedRecord object
    ) throws ServiceException {
        String objectClass = Object_2Facade.getObjectClass(object);
        if(objectClass == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "attribute " + SystemAttributes.OBJECT_CLASS + " missing",
                new BasicException.Parameter("object", object)
            );
        }
        if(this.isGenericTypePath(Object_2Facade.getPath(object))) {
            return this.basicObjectClassDef; 
        } else {        
            return getModel().getDereferencedType(objectClass);
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Check whether referenced type matches objClass
     * 
     * @param objectFacade
     * @param classDef
     * @throws ServiceException
     */
    private void validateTypeAndClass(
        Object_2Facade objectFacade,
        ModelElement_1_0 classDef
    ) throws ServiceException {
        final Model_1_0 model = getModel();
        final ModelElement_1_0 typeDef = model.getTypes(objectFacade.getPath())[2];
        final String objectClass = objectFacade.getObjectClass();
        if(!model.isSubtypeOf(objectClass, typeDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "object not instance of type",
                new BasicException.Parameter(
                    "object class", 
                    objectClass
                ),
                new BasicException.Parameter(
                    "classDef", 
                    classDef == null ? null : classDef.jdoGetObjectId()
                ),
                new BasicException.Parameter(
                    "type", 
                    typeDef == null ? null : typeDef.jdoGetObjectId()
                )
            );      
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Prepares and validates a create request
     * 
     * @param objectFacade
     * @param classDef
     * 
     * @throws ServiceException
     */
    protected void prepareAndValidateCreateRequest(
        Object_2Facade objectFacade,
        ModelElement_1_0 classDef
    ) throws ServiceException {
        removeAttributesWithEmptyValueLists(objectFacade);
        validateTypeAndClass(objectFacade, classDef);
    }

    /**
     * Remove all attributes with empty value list
     * 
     * @param objectFacade
     */
    private void removeAttributesWithEmptyValueLists(
        Object_2Facade objectFacade
    ) {
        Set<String> attributeNames = objectFacade.getValue().keySet();
        List<String> attributesToBeRemoved = new ArrayList<String>();
        for(
            Iterator<String> i = attributeNames.iterator();
            i.hasNext();
        ) {
            String attributeName = i.next();
            Object value = objectFacade.getValue().get(attributeName);
            if(
                (value == null) || 
                (value instanceof List && ((List<?>)value).isEmpty()) ||
                (value instanceof SparseArray && ((SparseArray<?>)value).isEmpty())                        
            ) {
                attributesToBeRemoved.add(attributeName);
            }
        }
        objectFacade.getValue().keySet().removeAll(attributesToBeRemoved);
    }
    
    //---------------------------------------------------------------------------
    /**
     * Prepares and validates a put request
     * 
     * @param objectFacade
     * @param classDef
     * 
     * @throws ServiceException
     */
    protected void prepareAndValidatePutRequest(
        Object_2Facade objectFacade,
        ModelElement_1_0 classDef
    ) throws ServiceException {
        validateTypeAndClass(objectFacade, classDef);
    }    
    
    //---------------------------------------------------------------------------
    protected DataproviderReply completeAndVerifyReply(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        if(reply.getResult() != null) {
            MappedRecord[] objects = reply.getObjects();
            short attributeSelector = request.attributeSelector();
            for (
                int i = 0;
                i < objects.length;
                i++
            ) {            
                this.completeAndVerifyReplyObject(
                    objects[i], 
                    attributeSelector 
                );
            }
        }
        return reply;
    }

    // --------------------------------------------------------------------------
    public class LayerInteraction extends OperationAwareLayer_1.LayerInteraction {
        
        public LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
                
        //---------------------------------------------------------------------------
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader(); 
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);      
            super.find(ispec, input, output);
            Standard_1.this.completeAndVerifyReply(
                header,
                request,
                reply
            );
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader(); 
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);        
            super.get(ispec, input, output);
            Standard_1.this.completeAndVerifyReply(
                header,
                request,
                reply
            );
            return true;
        }

        //---------------------------------------------------------------------------
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader(); 
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);            
            try {
                MappedRecord object = request.object();
                Object_2Facade facade = Facades.asObject(object);
                ModelElement_1_0 objClassDef = Standard_1.this.getObjectClass(object);
                SysLog.trace("create object", object);
                SysLog.trace("create objClass", objClassDef.getQualifiedName());
                prepareAndValidateCreateRequest(facade, objClassDef);
                super.create(
                    ispec, 
                    input, 
                    output
                );
                completeAndVerifyReply(
                    header,
                    request,
                    reply
                );
                return true;
            } catch(RuntimeException e) {
                throw new ServiceException(e);
            }
        }

        //---------------------------------------------------------------------------
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);            
            MappedRecord object = request.object();
            ModelElement_1_0 objClassDef = getObjectClass(object);
            Object_2Facade facade = Facades.asObject(object);
            prepareAndValidatePutRequest(facade, objClassDef);
            super.put(
                ispec, 
                input, 
                output
            );
            completeAndVerifyReply(
                header,
                request,
                reply
            );
            return true;
        }
    
        //---------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#otherOperation(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest, java.lang.String, org.openmdx.compatibility.base.naming.Path)
         */
        @Override
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
            String operationName = operationPath.getSegment(operationPath.size()-2).toClassicRepresentation();
            //
            // rewrite reference name with namespace to .../view/<namespaceId>/...
            //
            ModelElement_1_0 inParamTypeDef = Standard_1.this.getObjectClass(object);
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
            Collection<Object> allTypes = new HashSet<Object>();
            for(Iterator<?> i = targetClassDef.objGetList("allSubtype").iterator(); i.hasNext(); ) {
                ModelElement_1_0 subtype = getModel().getElement(i.next());
                allTypes.addAll(
                    subtype.objGetList("allSupertype")
                );
            }
            //  
            // lookup operation
            //
            for(
                Iterator<?> i = allTypes.iterator();
                i.hasNext();
            ) {
                ModelElement_1_0 classDef = getModel().getDereferencedType(i.next());
                ModelElement_1_0 operationDef = null;
                try {
                    operationDef = getModel().getElement(
                        classDef.getQualifiedName() + ":" + operationName
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
                        Iterator<?> j = operationDef.objGetList("content").iterator();
                        j.hasNext();
                    ) {
                        ModelElement_1_0 e = getModel().getElement(j.next());
                        if("in".equals(e.getName())) {
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
            return null;
        }
        
    }
    
    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    
    /**
     * @deprecated will not be supported by the dataprovider 2 stack
     */
    private List<Object> genericTypes = null;
    
    /**
     * @deprecated will not be supported by the dataprovider 2 stack
     */
    private ModelElement_1_0 basicObjectClassDef = null;

}

//--- End of File -----------------------------------------------------------
