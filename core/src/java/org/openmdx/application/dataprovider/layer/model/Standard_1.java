/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Model layer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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

import static org.openmdx.base.accessor.cci.SystemAttributes.OBJECT_IDENTITY;

import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Facades;
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
    
    /**
     * Constructor 
     */
    public Standard_1() {
        super();
    }

    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
        
    // --------------------------------------------------------------------------
    protected String getObjectClassName(
        MappedRecord object
    ){
        return object == null ? null : Object_2Facade.getObjectClass(object);
    }
    

    /**
     * Set the derived attribute 'identity' in case the class supports this feature,
     * e.g. ch:omex:generic:BasicObject
     * 
     * @param request
     * @param object
     */
    private void setIdentity(
        DataproviderRequest request, 
        MappedRecord object
    ) throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        if(facade.getObjectClass() != null) {
            if(
                this.getModel().isSubtypeOf(facade.getObjectClass(), "org:openmdx:base:ExtentCapable") &&
                (!facade.getValue().containsKey(OBJECT_IDENTITY))                      
            ) {
                facade.replaceAttributeValuesAsListBySingleton(OBJECT_IDENTITY, facade.getPath().toXRI());
            }
        }      
    }

    // --------------------------------------------------------------------------
    
    /**
     * @param facade the data object facade
     * 
     * @return the corresponding class definition
     */
    private ModelElement_1_0 getClassDef(
        Object_2Facade facade
    ) {
        try {
            ModelElement_1_0 elementDef = getModel().getElement(facade.getObjectClass()); 
            return elementDef.isStructureType() ? null : elementDef;
        } catch(ServiceException e){
            return null;
        }
    }
    
    // --------------------------------------------------------------------------
    
    
    /**
     * Touches all object features with object.values(featureName) for all 
     * non-derived features. This way the feature is added to the set
     * of attributes of object. This allows to minimize round trips.
     * 
     * @param object
     * @param touchNonDerivedFeatures 
     */
    @SuppressWarnings("unchecked")
    private void adjustEmptyFeatureSet(
        MappedRecord object, 
        boolean touchNonDerivedFeatures
    ) throws ServiceException {
        Object_2Facade facade = Facades.asObject(object);
        //
        // Validate Feature Set
        //
        for(String attributeName : (Set<String>)facade.getValue().keySet()) {
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
        //
        // Update Feature Set
        //
        if(touchNonDerivedFeatures) {
            ModelElement_1_0 classDef = this.getClassDef(facade);
            if(classDef != null) {
                for(Map.Entry<String,ModelElement_1_0> feature : classDef.objGetMap("allFeature").entrySet()) {
                    ModelElement_1_0 featureDef = feature.getValue();
                    if(
                        ModelHelper.isStoredAsAttribute(featureDef) &&
                        !ModelHelper.isDerived(featureDef)
                    ){
                        facade.attributeValues(feature.getKey());
                    }
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
        MappedRecord object
    ) throws ServiceException {
        this.setIdentity(request, object);
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

    // --------------------------------------------------------------------------
    public class LayerInteraction extends Layer_1.LayerInteraction {

    	/**
    	 * Constructor
    	 * 
    	 * @param connection
    	 * @throws ResourceException
    	 */
        protected LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
        
        /**
         * Tells whether reply data is expected
         * 
         * @param ispec
         * 
         * @return <code>true</code> if a reply data is expected
         */
        protected final boolean expectsReplyData(
            RestInteractionSpec ispec
        ){
        	int interactionVerb = ispec.getInteractionVerb();
        	return interactionVerb == InteractionSpec.SYNC_SEND_RECEIVE || interactionVerb == InteractionSpec.SYNC_RECEIVE;
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
            super.get(
                request.getInteractionSpec(), 
                Facades.asQuery(request.object()), 
                reply.getResult()
            );
            if(expectsReplyData(ispec)) {
            	Standard_1.this.completeReply(
	                request,
	                reply
	            );
            }
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
            super.find(
                request.getInteractionSpec(), 
                Facades.asQuery(request.object()), 
                reply.getResult()
            );
            if(expectsReplyData(ispec)) {
            	Standard_1.this.completeReply(
	                request,
	                reply
	            );
            }
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
            super.create(
                request.getInteractionSpec(), 
                Facades.asObject(request.object()), 
                reply.getResult()
            );
            if(expectsReplyData(ispec)) {
            	Standard_1.this.completeReply(
	                request,
	                reply
	            );
            }
            return true;
        }
    
        //--------------------------------------------------------------------------
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            super.put(
                request.getInteractionSpec(), 
                Facades.asObject(request.object()), 
                output
            );
            if(expectsReplyData(ispec)) {
            	Standard_1.this.completeReply(
	                request,
	                reply
	            );
            }
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
            super.delete(
                request.getInteractionSpec(),
                Facades.asObject(request.object()),
                output
            );
            if(expectsReplyData(ispec)) {
            	Standard_1.this.completeReply(
	                request,
	                reply
	            );
            }
            return true;
        }
    
    }
    
}
