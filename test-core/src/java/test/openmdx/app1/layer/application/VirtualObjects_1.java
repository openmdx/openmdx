/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Hard-Wired Objects Layer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package test.openmdx.app1.layer.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Hard-wired Objects Layer
 * <p>
 * This layer implementation shall be replaced by an aspect oriented
 * persistence plug-in in future.
 */
public class VirtualObjects_1 extends HardWiredObjects_1 {

    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
                        
    // --------------------------------------------------------------------------    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
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
    }     

    // --------------------------------------------------------------------------
    public class LayerInteraction extends HardWiredObjects_1.LayerInteraction {
        
        public LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
                
         /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        @SuppressWarnings("unchecked")
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            try {
                Path objectId = request.path();
                VirtualObjects_1.this.logger.log(Level.FINEST,"Get request for {0}", objectId);
                if(objectId.isLike(PRODUCT_GROUP_PATTERN)) {
                    //
                    // virtual ProductGroup instances
                    //
                    reply.getResult().add(
                        Object_2Facade.newInstance(
                            objectId,
                            "test:openmdx:app1:ProductGroup"
                        ).getDelegate()
                    );
                    return true;
                } else if(objectId.isLike(PRODUCT_PATTERN)) {
                    //
                    // virtual Product instances
                    //
                    reply.getResult().add(
                        Object_2Facade.newInstance(
                            objectId,
                            "test:openmdx:app1:Product"
                        ).getDelegate()
                    );
                    return true;
                } else {
                    //
                    // non-virtual objects
                    //
                    super.get(
                        ispec,
                        input,
                        output
                    );
                    return true;
                }
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.layer.application.ProvidingUid_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            Path objectId = request.path();
            VirtualObjects_1.this.logger.log(Level.FINEST,"Create request for {0}", objectId);
            if(
                    objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                    objectId.isLike(PRODUCT_PATTERN)
            ) {
                //
                // virtual Product|ProductGroup
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Virtual objects can't be created",
                    new BasicException.Parameter("xri", objectId)
                );
            } else {
                //
                // non-virtual objects
                //
                super.create(
                    ispec,
                    input,
                    output
                );
                return true;
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            Path objectId = request.path();
            VirtualObjects_1.this.logger.log(Level.FINEST,"Remove request for {0}", objectId);
            if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
            ) {
                //
                // virtual Product|ProductGroup
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Virtual objects can't be removed",
                    new BasicException.Parameter("xri", objectId)
                );
            } else {
                //
                // non-virtual objects
                //
                return super.delete(
                    ispec,
                    input,
                    output
                );
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            Path objectId = request.path();
            VirtualObjects_1.this.logger.log(Level.FINEST,"Replace request for {0}", objectId);
            if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
            ) {
                //
                // virtual Product|ProductGroup
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Virtual objects can't be replaced",
                    new BasicException.Parameter("xri", objectId)
                );
            } else {
                //
                // non-virtual objects
                //
                return super.put(
                    ispec,
                    input,
                    output
                );
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            Path objectId = request.path();
            VirtualObjects_1.this.logger.log(Level.FINEST,"Find request for {0}", objectId);
            if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
            ) {
                //
                // virtual Product|ProductGroup
                //
                reply.setHasMore(Boolean.FALSE);
                return true;
            } else {
                //
                // non hard-wired objects
                //
                return super.find(
                    ispec,
                    input,
                    output
                );
            }
        }
        
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private static final Path PRODUCT_GROUP_PATTERN = new Path(
        new String[]{
            "test:openmdx:app1",
            "provider",
            ":*",
            "segment",
            ":*",
            "productGroup",
            ":*"
        }
    );
    private static final Path PRODUCT_PATTERN = PRODUCT_GROUP_PATTERN.getDescendant(
        new String[]{
            "product",
            ":*"
        }
    );

    private final Logger logger = Logger.getLogger(VirtualObjects_1.class.getName());

}
