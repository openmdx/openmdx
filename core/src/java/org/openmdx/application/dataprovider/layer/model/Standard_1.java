/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Standard_1.java,v 1.41 2010/06/04 19:31:30 hburger Exp $
 * Description: Model layer
 * Revision:    $Revision: 1.41 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 19:31:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.model;

import static org.openmdx.base.accessor.cci.SystemAttributes.CONTEXT_PREFIX;
import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_BY;
import static org.openmdx.base.accessor.cci.SystemAttributes.MODIFIED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.MODIFIED_BY;
import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_CLASS;
import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_IDENTITY;
import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_INSTANCE_OF;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_BY;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;

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
public class Standard_1 extends Layer_1 {

    // --------------------------------------------------------------------------
    public Standard_1(
    ) {
    }
    
    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
        
    // --------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.model.SystemAttributes_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        this.optimisticLocking = configuration.isOn(
            LayerConfigurationEntries.OPTIMISTIC_LOCKING
         ) || "whenModified".equalsIgnoreCase(
             configuration.getFirstValue(LayerConfigurationEntries.OPTIMISTIC_LOCKING)
        );       
    }

    // --------------------------------------------------------------------------
    protected String getObjectClassName(
        MappedRecord object
    ){
        return object == null ? null : Object_2Facade.getObjectClass(object);
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
    protected void triggeredRemoveDerivedAttributes(
        MappedRecord object
    ) throws ServiceException {
        if(this.getObjectClassName(object) != null) {
            this.removeNonPersistentAttributes(object);
        }
    }

    // --------------------------------------------------------------------------
    /**
     * remove the attributes which are in the model as derived
     */
    @SuppressWarnings("unchecked")
    protected void removeNonPersistentAttributes(
        MappedRecord object
    ) throws ServiceException {
        try {
            String objectClassName = this.getObjectClassName(object);
            if(objectClassName != null) {
                Object_2Facade facade = Object_2Facade.newInstance(object);
                Map<?,?> modelAttributes = (Map<?,?>)getModel().getDereferencedType(objectClassName).objGetValue("attribute");
                for (
                    Iterator<String> i = facade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = i.next();
                    // remove derived attributes except the attributes listed below 
                    // NOTE: This is a hack and must be fixed as soon as each layer can have 
                    // its own model, i.e. persistence layer uses persistence model, application 
                    // layer uses application model, etc.
                    if (
                        facade.getAttributeValues(attributeName) != null &&
                        !PERSISTENT_ATTRIBUTES.contains(attributeName)
                     ) {
                        ModelElement_1_0 attributeDef = (ModelElement_1_0)modelAttributes.get(attributeName);
                        // non-modeled attributes are not removed. 
                        if(
                            attributeDef == null ? attributeName.equals(OBJECT_INSTANCE_OF) : (
                                Boolean.TRUE.equals(attributeDef.objGetValue("isDerived")) ||
                                NON_PERSISTENT_ATTRIBUTES.contains(attributeName) // TODO use jpa3 meta data
                            )
                        ) {
                            // remove derived attributes
                            i.remove();
                        }
                    }
                }
            }
        } catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    // --------------------------------------------------------------------------
    protected Boolean attributeIsInstanceOf(
        Map<?,?> attributeDefs,
        String attributeName,
        Collection<?> candidates
    ) throws ServiceException {
        ModelElement_1_0 attributeDef = (ModelElement_1_0) attributeDefs.get(attributeName);
        return attributeDef == null ? null : Boolean.valueOf(
            candidates.contains(
                getModel().getElementType(
                    attributeDef
                ).objGetValue("qualifiedName")
            )
        );
    }

    // --------------------------------------------------------------------------
    protected boolean isTimeDateDuration(
        Map<?,?> attributeDefs,
        String attributeName
    ) throws ServiceException {
        Boolean xmlDatatype = attributeIsInstanceOf(
            attributeDefs,
            attributeName,
            TIME_OR_DATE_DATATYPES
        );
        return xmlDatatype == null ? (
                CREATED_AT.equals(attributeName) ||
                MODIFIED_AT.equals(attributeName)
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
        MappedRecord object
    ) throws ServiceException {
        Object_2Facade facade = null;
        try {
            facade = Object_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        if(facade.getObjectClass() != null) {
            if(
                this.getModel().isSubtypeOf(facade.getObjectClass(), "org:openmdx:base:ExtentCapable") &&
                (!facade.getValue().keySet().contains(OBJECT_IDENTITY))                      
            ) {
                facade.attributeValuesAsList(OBJECT_IDENTITY).clear();
                facade.attributeValuesAsList(OBJECT_IDENTITY).add(
                    facade.getPath().toXRI()
                );
            }
        }      
    }

    // --------------------------------------------------------------------------
    /**
     * Touches all object features with object.values(featureName) for all 
     * non-derived features. This way the feature is added to the set
     * of attributes of object. This allows to minimize roundtrips.
     * @param touchNonDerivedFeatures 
     * @param features 
     */
    private void adjustEmptyFeatureSet(
        MappedRecord object,
        boolean touchNonDerivedFeatures, 
        Set<String> features
    ) throws ServiceException {
        Object_2Facade facade = null;
        try {
            facade = Object_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        String objectClass = facade.getObjectClass();
        ModelElement_1_0 classDef = null;
        try {
            ModelElement_1_0 elementDef = getModel().getElement(
                objectClass
            ); 
            classDef = ModelAttributes.STRUCTURE_TYPE.equals(elementDef.objGetClass()) ? null : elementDef;
        } 
        catch(ServiceException e){
            classDef = null;
        }
        if(classDef == null) {
            return;
        }
        for(
            Iterator<?> i = ((Map<?,?>)classDef.objGetValue("allFeature")).values().iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 featureDef = (ModelElement_1_0)i.next();
            boolean touch;
            boolean attribute;
            if(this.getModel().isAttributeType(featureDef)) {
                attribute = true;
                touch = touchNonDerivedFeatures && !((Boolean)featureDef.objGetValue("isDerived")).booleanValue();
            } 
            else if (
                this.getModel().isReferenceType(featureDef) && 
                this.getModel().referenceIsStoredAsAttribute(featureDef)
            ) {
                attribute = true;
                touch = touchNonDerivedFeatures && !this.getModel().referenceIsDerived(featureDef); 
            } 
            else {
                attribute = false;
                touch = false;
            }
            if(attribute) {
                String feature = (String)featureDef.objGetValue("name");
                if(touch) {
                    facade.attributeValues(feature);
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void adjustEmptyFeatureSet(
        MappedRecord object, 
        boolean touchNonDerivedFeatures
    ) throws ServiceException {
        Object_2Facade facade = null;
        try {
            facade = Object_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        Set<String> features = new HashSet<String>(facade.getValue().keySet());
        for(String attributeName : features) {
            if(attributeName.lastIndexOf(':') > 0 || attributeName.indexOf('$') > 0) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Scoped features not supported",
                    new BasicException.Parameter("object", object),
                    new BasicException.Parameter("attribute", attributeName)
                );
            } 
        }
        this.adjustEmptyFeatureSet(
            object,
            touchNonDerivedFeatures, 
            features
        );
    }

    // --------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected void removeContexts(
        MappedRecord object
    ) throws ServiceException {
        Object_2Facade facade;
        try {
            facade = Object_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        for(Iterator<String> i = facade.getValue().keySet().iterator(); i.hasNext(); ) {
            String attributeName = i.next();
            if(attributeName.startsWith(CONTEXT_PREFIX)) {
                i.remove();
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
        MappedRecord object
    ) throws ServiceException {
        this.setIdentity(request, object);
        this.removeContexts(object);
        this.adjustEmptyFeatureSet(
            object, 
            request.attributeSelector() == AttributeSelectors.ALL_ATTRIBUTES
        );
    }

    // --------------------------------------------------------------------------
    protected DataproviderReply completeReply(
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        if(reply.getResult() != null) {
            for(MappedRecord object : reply.getObjects()) {
                this.completeObject(
                    request,
                    object
                );
            }
        }
        return reply;
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected boolean isModified(
        MappedRecord afterImage
    ) throws ServiceException {
        Object_2Facade facade = null;
        try {
            facade = Object_2Facade.newInstance(afterImage);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        for(String attribute : (Set<String>)facade.getValue().keySet()) {
            if(
                !OBJECT_CLASS.equals(attribute) &&
                !MODIFIED_AT.equals(attribute) &&
                !MODIFIED_BY.equals(attribute) &&
                !attribute.startsWith(CONTEXT_PREFIX)
            ){
                return true;
            }
        }
        return false;
    }
    
    //--------------------------------------------------------------------------
    protected ModelElement_1_0 getObjectClass(
        MappedRecord object
    ) throws ServiceException {
        String objectClassName = this.getObjectClassName(object);
        return objectClassName == null ?
            null :
            this.getModel().getDereferencedType(objectClassName);
    }

    // --------------------------------------------------------------------------
    /**
     * Called before given object is deleted. 
     */
    protected void notifyPreDelete(
        Path objectIdentity
    ) {
        //
    }

    // --------------------------------------------------------------------------
    public class LayerInteraction extends Layer_1.LayerInteraction {
        
        public LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }
        
        /**
         * Verify the digest of an object to be modified
         * 
         * @param header
         * @param request
         * 
         * @throws ServiceException CONCURRENT_ACCESS_FAILURE
         * in case of a digest mismatch.
         */
        protected void verifyDigest(
            ServiceHeader header,
            DataproviderRequest request
        ) throws ServiceException {
            if(optimisticLocking){
                MappedRecord afterImage = request.object();
                if(Standard_1.this.isModified(afterImage)){
                    MappedRecord beforeImage = this.getBeforeImage(header, request.path());
                    Object_2Facade beforeImageFacade = null;
                    Object_2Facade afterImageFacade = null;
                    try {
                        beforeImageFacade = Object_2Facade.newInstance(beforeImage);
                        afterImageFacade = Object_2Facade.newInstance(afterImage);
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                    if(!Arrays.equals((byte[])beforeImageFacade.getVersion(), (byte[])afterImageFacade.getVersion())) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN, 
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            "Digest mismatch",
                            new BasicException.Parameter(
                                "path",
                                request.path()
                            ),
                            new BasicException.Parameter(
                                "beforeImage.version",
                                beforeImageFacade.getVersion()
                            ),
                            new BasicException.Parameter(
                                "afterImage.version",
                                afterImageFacade.getVersion()
                            ),
                            new BasicException.Parameter(
                                "beforeImage",
                                beforeImage
                            ),
                            new BasicException.Parameter(
                                "afterImage",
                                afterImage
                            )
                        );
                    }
                }
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.BeforeImageCachingLayer_1#getBeforeImage(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        protected MappedRecord getBeforeImage(
            ServiceHeader header, 
            Path path
        ) throws ServiceException {
            try {
                DataproviderRequest getRequest = new DataproviderRequest(
                    Query_2Facade.newInstance(path).getDelegate(),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                    null
                );
                DataproviderReply getReply = super.newDataproviderReply();
                super.get(
                    getRequest.getInteractionSpec(), 
                    Query_2Facade.newInstance(getRequest.object()), 
                    getReply.getResult()
                );            
                MappedRecord beforeImage = getReply.getObject();
                return beforeImage;
            }
            catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }
        
        // --------------------------------------------------------------------------
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            try {
                super.get(
                    request.getInteractionSpec(), 
                    Query_2Facade.newInstance(request.object()), 
                    reply.getResult()
                );
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
            Standard_1.this.completeReply(
                request,
                reply
            );
            return true;
        }
    
        // --------------------------------------------------------------------------
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            try {
                super.find(
                    request.getInteractionSpec(), 
                    Query_2Facade.newInstance(request.object()), 
                    reply.getResult()
                );
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
            Standard_1.this.completeReply(
                request,
                reply
            );
            return true;
        }
    
        // --------------------------------------------------------------------------
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            Standard_1.this.triggeredRemoveDerivedAttributes(request.object());
            try {
                super.create(
                    request.getInteractionSpec(), 
                    Object_2Facade.newInstance(request.object()), 
                    reply.getResult()
                );
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
            Standard_1.this.completeReply(
                request,
                reply
            );
            return true;
        }
    
        //--------------------------------------------------------------------------
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            Standard_1.this.triggeredRemoveDerivedAttributes(request.object());
            this.verifyDigest(header,request);        
            try {
                super.put(
                    request.getInteractionSpec(), 
                    Object_2Facade.newInstance(request.object()), 
                    output
                );
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
            Standard_1.this.completeReply(
                request,
                reply
            );
            return true;
        }
    
        // --------------------------------------------------------------------------
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            try {
                super.delete(
                    request.getInteractionSpec(),
                    Object_2Facade.newInstance(request.object()),
                    output
                );
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
            Standard_1.this.completeReply(
                request,
                reply
            );
            return true;
        }
    
    }
    
    //--------------------------------------------------------------------------

    /**
     * Tells whether the plug-in is active of inactive.
     */
    protected boolean optimisticLocking = false;

    protected final static Collection<String> TIME_OR_DATE_DATATYPES = Arrays.asList(
        PrimitiveTypes.DATE,
        PrimitiveTypes.DATETIME,
        PrimitiveTypes.DURATION
    );

    /**
     * TODO get the information from the jpa3 meta data
     */
    protected final static Collection<String> NON_PERSISTENT_ATTRIBUTES = Arrays.asList(
        "transactionTimeUnique",
        "validTimeUnique"
    );
    
    /**
     * TODO get the information from the jpa3 meta data
     */
    protected final static Collection<String> PERSISTENT_ATTRIBUTES = Arrays.asList(
        MODIFIED_AT,
        MODIFIED_BY,
        CREATED_AT,
        CREATED_BY,
        REMOVED_AT,
        REMOVED_BY,
        OBJECT_CLASS,
        "stateVersion",
        "modifiedFeature"
    );
    
}
