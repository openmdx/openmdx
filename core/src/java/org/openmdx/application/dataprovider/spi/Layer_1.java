/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Layer_1.java,v 1.35 2010/04/13 17:15:13 wfro Exp $
 * Description: User Profile Service
 * Revision:    $Revision: 1.35 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/13 17:15:13 $
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
package org.openmdx.application.dataprovider.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.ResultSetInfo;

import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.Version;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

/**
 * A delegating rest interaction.
 */
public abstract class Layer_1 implements Dataprovider_1_0, Port {

    public Layer_1(
    ) {        
    }
    
    /**
     * Provide a response path by appending "*-";
     * 
     * @param requestId
     * 
     * @return a response path 
     */
    public Path newReplyId(
        Path requestId
    ){
        return requestId.getParent().getChild(requestId.getBase() + "*-");
    }
    
    /**
     * To replace the deprecated UIDFactory.create() calls
     * 
     * @return a UID as string
     */
    protected final String uidAsString(
    ){
        UUID uuid = UUIDs.newUUID();
        return this.compressUID ?
            UUIDConversion.toUID(uuid) :
            uuid.toString();
    }

    /**
     * Get the layer's id
     */
    protected final short getId(
    ){
        return this.id;
    }

    /**
     * To be supplied by the dataprovider
     *
     * @return  the (modifiable) configuration
     *  <dl>
     *      <dt>namespaceId</dt>            <dd>String</dd>
     *      <dt>exposedPath</dt>            <dd>Path[]</dd>
     *  </dl>
     */
    protected final Configuration getConfiguration(
    ){
        return this.configuration;
    }

    /**
     * The layer this layer delegates to
     *
     * @return  the layer to delegate to
     */
    protected final Layer_1 getDelegation(
    ){
        return this.delegation;
    }

    /**
     * Lazy model accessor retrieval
     * 
     * @return the model accessor
     */
    public final Model_1_0 getModel(){
        return Model_1Factory.getModel();
    }
    
    protected ConnectionFactory getConnectionFactory(
    ) {
        return this.connectionFactory;
    }
    
    /**
     * Says whether this layer is the terminal layer or not
     *
     * @return  true if there is no layer to delegate to
     */
    protected final boolean terminal(
    ){
        return this.delegation == null;
    }

    /**
     * This layer's specific configuration specifiers.
     * <p>
     * Usage:
     * <pre>
     *   Map specification = super.configurationSpecification();
     *   specification.put(
     *     "specificOption1", new ConfigurationSpecifier([...])
     *   );
     *   specification.put(
     *     "specificOption2", new ConfigurationSpecifier([...])
     *   );
     *   [...]
     *   return specification;
     * </pre>
     *
     * @return  a map with id/ConfigurationSpecifier entries
     */
    public Map<String,ConfigurationSpecifier> configurationSpecification(
    ){
        return new HashMap<String,ConfigurationSpecifier>();
    }

    /**
     * Activates a dataprovider layer
     * 
     * @param   id              the dataprovider layer's id
     * @param   configuration   the dataprovider'a configuration
     *  <dl>
     *      <dt>namespaceId</dt>            <dd>String</dd>
     *      <dt>exposedPath</dt>            <dd>Path[]</dd>
     *  </dl>
     * @param       delegation
     *              the layer to delegate to;
     *              or null if "persistenceLayer".equals(id)
     *
     * @exception   Exception
     *              unexpected exceptions
     * @exception   ServiceException
     *              expected exceptions
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException{
        this.compressUID = configuration.isOn(
            SharedConfigurationEntries.COMPRESS_UID
        );        
        this.configuration = configuration;
        this.delegation = delegation;
        this.id = id;
        SparseArray<Object> connectionFactories = configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION_FACTORY
        );        
        if(!connectionFactories.isEmpty()) {
            this.connectionFactory = (ConnectionFactory)connectionFactories.get(0);
        }        
        SysLog.detail(
            "Activating " + DataproviderLayers.toString(id) + " layer " + getClass().getName(),
            configuration
        );
    }

    /**
     * Deactivates a dataprovider layer
     * <p>
     * Subclasses overriding this method have to apply the following pattern:
     * <pre>
     *  public void deactivate(
     *  ) throws Exception, ServiceException {
     *      // local deactivation code
     *      super.deactivate();
     *  }
     * </pre>       
     *
     * @exception   Exception
     *              unexpected exceptions
     * @exception   ServiceException
     *              expected exceptions
     */
    public void deactivate(
    ) throws Exception, ServiceException{
        SysLog.info(
            DataproviderLayers.toString(getId()) + " layer deactivated"
        );
        this.configuration = null;
        this.delegation = null;
        this.connectionFactory = null;
    }
        
    //-----------------------------------------------------------------------
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }

    //-----------------------------------------------------------------------
    public class LayerInteraction extends AbstractRestInteraction {
        
        public LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
            this.serviceHeader = ServiceHeader.toServiceHeader(connection.getMetaData().getUserName(), null);            
        }
    
        public ServiceHeader getServiceHeader(
        ) {
            return this.serviceHeader;
        }
        
        public DataproviderReply newDataproviderReply(
        ) throws ServiceException {
            return new DataproviderReply();
        }
        
        public DataproviderRequest newDataproviderRequest(
            RestInteractionSpec ispec,
            Query_2Facade input
        ) {
            return new DataproviderRequest(
                ispec, 
                input.getDelegate()
            );
        }
        
        public DataproviderRequest newDataproviderRequest(
            RestInteractionSpec ispec,
            Object_2Facade input
        ) {
            return new DataproviderRequest(
                ispec, 
                input.getDelegate()
            );
        }
        
        public DataproviderRequest newDataproviderRequest(
            RestInteractionSpec ispec,
            MessageRecord input
        ) throws ServiceException {
            try {
                Object_2Facade objectFacade = Object_2Facade.newInstance();
                objectFacade.setPath(input.getPath());
                objectFacade.setValue(input.getBody());
                return new DataproviderRequest(
                    ispec,
                    objectFacade.getDelegate()
                );
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }
        
        /**
         * Create a dataprovider reply
         * 
         * @param result an org::openmdx::kernel::ResulSet instance
         * 
         * @return a dataprovider reply wrapping the result
         * 
         * @throws ClassCastException unless result is a ResultRecord instance
         */
        public DataproviderReply newDataproviderReply(
            IndexedRecord result
        ) {
            return new DataproviderReply(
                (ResultRecord) result, 
                false
            );
        }
        
        /**
         * Create a dataprovider reply
         * 
         * @param result an org::openmdx::kernel::Message instance
         * 
         * @return a dataprovider reply wrapping the result
         * 
         * @throws ClassCastException unless result is a MappedRecord instance
         */
        public DataproviderReply newDataproviderReply(
            MessageRecord result
        ) {
            return new DataproviderReply(
                result,
                false
            );
        }
        
        
        public ResourceException newResourceException(
            ServiceException e
        ) {
            return ResourceExceptions.initHolder(
                new ResourceException(
                    e.getCause().getDescription(),
                    BasicException.newEmbeddedExceptionStack(e)
                )
            );        
        }
        
        protected final String uidAsString(
        ){
            UUID uuid = UUIDs.newUUID();
            return Layer_1.this.compressUID ?
                UUIDConversion.toUID(uuid) :
                uuid.toString();
        }
        
        protected final Layer_1 getDelegatingLayer(
        ) {
            return Layer_1.this.getDelegation();
        }
        
        protected final LayerInteraction getDelegatingInteraction(
        ) throws ServiceException {
            try {
                Port delegation = this.getDelegatingLayer();
                return delegation == null ?
                    null :
                        (LayerInteraction)delegation.getInteraction(this.getConnection());
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }
        
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction == null ? false : interaction.get(ispec, input, output);
        }

        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction == null ? false : interaction.find(ispec, input, output);
        }
    
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction == null ? false : interaction.create(ispec, input, output);
        }
    
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction == null ? false : interaction.put(ispec, input, output);
        }
    
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction == null ? false : interaction.delete(ispec, input, output);
        }
    
        @Override
        public boolean invoke(
            RestInteractionSpec ispec, 
            MessageRecord input, 
            MessageRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction == null ? false : interaction.invoke(ispec, input, output);
        }
    
        private final ServiceHeader serviceHeader;
        
    }

    //-----------------------------------------------------------------------
    static class DataproviderConnection implements Connection {
    
        public DataproviderConnection(
            ServiceHeader serviceHeader
        ) {
            this.serviceHeader = serviceHeader;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#close()
         */
        public void close()
            throws ResourceException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#createInteraction()
         */
        public Interaction createInteraction()
            throws ResourceException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#getLocalTransaction()
         */
        public LocalTransaction getLocalTransaction()
            throws ResourceException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#getMetaData()
         */
        public ConnectionMetaData getMetaData(
        ) throws ResourceException {
            if(this.metaData == null) {
                this.metaData = new ConnectionMetaData(){

                    /**
                     * It's an openMDX connection
                     */
                    public String getEISProductName(
                    ) throws ResourceException {
                        return "openMDX/REST";
                    }

                    /**
                     * with the given openMDX version
                     */
                    public String getEISProductVersion(
                    ) throws ResourceException {
                        return Version.getSpecificationVersion();
                    }

                    /**
                     * Use the stringified principal chain
                     */
                    public String getUserName(
                    ) throws ResourceException {
                        return DataproviderConnection.this.serviceHeader.getPrincipalChain().toString();
                    }

                };
            }
            return this.metaData;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#getResultSetInfo()
         */
        public ResultSetInfo getResultSetInfo()
            throws ResourceException {
            // TODO Auto-generated method stub
            return null;
        }
        
        protected ConnectionMetaData metaData = null;
        protected final ServiceHeader serviceHeader;
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.cci.Dataprovider_1_0#process(org.openmdx.application.dataprovider.cci.ServiceHeader, java.util.List, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public ServiceException process(
        ServiceHeader header,
        List<DataproviderRequest> requests,
        List<DataproviderReply> replies
    ) {
        try {
            LayerInteraction interaction = (LayerInteraction)this.getInteraction(
                new DataproviderConnection(header)
            );
            for(DataproviderRequest request: requests) {
                DataproviderReply reply = new DataproviderReply(
                    request.operation() == DataproviderOperations.OBJECT_OPERATION
                );
                // Dispatch to layer interaction methods here. Do not let AbstractRestInteraction
                // do the work because it requires Model_1 which might not be available at this time.
                switch(request.operation()) {
                    case DataproviderOperations.OBJECT_RETRIEVAL: {
                        interaction.get(
                            request.getInteractionSpec(), 
                            Query_2Facade.newInstance(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.ITERATION_START: {
                        interaction.find(
                            request.getInteractionSpec(), 
                            Query_2Facade.newInstance(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_REPLACEMENT: {
                        interaction.put(
                            request.getInteractionSpec(), 
                            Object_2Facade.newInstance(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_CREATION: {
                        interaction.create(
                            request.getInteractionSpec(), 
                            Object_2Facade.newInstance(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_REMOVAL: {
                        interaction.delete(
                            request.getInteractionSpec(), 
                            Query_2Facade.newInstance(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_OPERATION: {
                        MappedRecord object = request.object();
                        MessageRecord input;
                        if(object instanceof MessageRecord) {
                            input = (MessageRecord) object;                            
                        } else {
                            input = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                            if(MessageRecord.NAME.equals(object.getRecordName())) {
                                input.putAll(object);
                            } else if(Object_2Facade.isDelegate(object)) {
                                input.setPath(Object_2Facade.getPath(object));
                                input.setBody(Object_2Facade.getValue(object));
                            } else {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.BAD_PARAMETER,
                                    "Unsupported operation argument",
                                    new BasicException.Parameter("actual", object.getRecordName()),
                                    new BasicException.Parameter("supported", MessageRecord.NAME, ObjectRecord.NAME)
                                );                            
                            }
                        }
                        interaction.invoke(
                            request.getInteractionSpec(), 
                            input,
                            reply.getResponse()
                        );                            
                        break;
                    }
                    default:
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Unsupported operation",
                            new BasicException.Parameter("actual", DataproviderOperations.toString(request.operation())),
                            new BasicException.Parameter("supported", "OBJECT_RETRIEVAL|OBJECT_RETRIEVAL|OBJECT_REPLACEMENT|OBJECT_REPLACEMENT|OBJECT_REMOVAL")
                        );                            
                }              
                replies.add(reply);
            }
            return null;
        }
        catch(Exception e) {
            return new ServiceException(e);
        }
    }
    
    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * Defines whether the UID's should be in compressed or UUID format.
     */
    protected boolean compressUID;

    /**
     * The layer's id
     */
    private short id;

    protected Layer_1 delegation;

    private Configuration configuration;

    private ConnectionFactory connectionFactory;
    
}
