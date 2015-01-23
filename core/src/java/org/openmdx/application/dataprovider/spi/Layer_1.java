/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Layer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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

import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.ResultSetInfo;

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
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

/**
 * An abstract Dataprovider Layer
 */
public abstract class Layer_1 implements Dataprovider_1_0, Port<RestConnection> {

	/**
	 * Constructor
	 */
    protected Layer_1(
    ) {        
    	super();
    }
    
    protected Layer_1 delegation;

    private Configuration configuration;

    /**
     * The layer's id
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    private short id;
    
    /**
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    private ConnectionFactory connectionFactory;
    
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
     * @exception   ServiceException in case of an activation failure
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException{
    	SysLog.detail(
			"Activating " + DataproviderLayers.toString(id) + " layer " + getClass().getName(),
			configuration
		);
        this.configuration = configuration;
        this.delegation = delegation;
        legacyActivation(id, configuration);
    }

    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }

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
                new DataproviderConnection(getConnectionFactory(), header)
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
                            Facades.asQuery(request.object(), header.isPreferringNotFoundException()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.ITERATION_START: {
                        interaction.find(
                            request.getInteractionSpec(), 
                            Facades.asQuery(request.object(), header.isPreferringNotFoundException()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_REPLACEMENT: {
                        interaction.put(
                            request.getInteractionSpec(), 
                            Facades.asObject(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_CREATION: {
                        interaction.create(
                            request.getInteractionSpec(), 
                            Facades.asObject(request.object()), 
                            reply.getResult()
                        );
                        break;
                    }
                    case DataproviderOperations.OBJECT_REMOVAL: {
                        interaction.delete(
                            request.getInteractionSpec(), 
                            Facades.asQuery(request.object(), header.isPreferringNotFoundException()), 
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
                            input = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
                            if(MessageRecord.NAME.equals(object.getRecordName())) {
                                input.putAll(object);
                            } else if(org.openmdx.base.rest.spi.ObjectRecord.isCompatible(object)) {
                                input.setResourceIdentifier(Object_2Facade.getPath(object));
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
        } catch(ServiceException e) {
            return e;
        } catch(Exception e) {
            return new ServiceException(e);
        }
    }

    /**
     * Provide a response path by appending "*-";
     * 
     * @param requestId
     * 
     * @return a response path 
     * 
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    public Path newReplyId(
        Path requestId
    ){
        return requestId.getParent().getChild(requestId.getLastSegment().toClassicRepresentation() + "*-");
    }
    
    @Deprecated
    private void legacyActivation(short id, Configuration configuration)
    		throws ServiceException {
    	this.id = id;
    	SparseArray<Object> connectionFactories = configuration.values(
    			SharedConfigurationEntries.DATAPROVIDER_CONNECTION_FACTORY
    			);        
    	if(!connectionFactories.isEmpty()) {
    		this.connectionFactory = (ConnectionFactory)connectionFactories.get(Integer.valueOf(0));
    	}        
    	if(configuration.isEnabled(SharedConfigurationEntries.LEGACY_CONFIGURATION, false)) {
    		applyLegacyConfiguration(configuration);
    	}
    }
    
    /**
     * Get the layer's id
     * 
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    protected final short getId(
    ){
        return this.id;
    }

    /**
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    protected ConnectionFactory getConnectionFactory(
    ) {
        return this.connectionFactory;
    }
    
    /**
     * Says whether this layer is the terminal layer or not
     *
     * @return  true if there is no layer to delegate to
     * 
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    protected final boolean terminal(
    ){
        return this.delegation == null;
    }

    /**
     * Activates a dataprovider layer with its legacy configuration
     * 
     * @param   configuration   the dataprovider'a configuration
     *
     * @exception   ServiceException in case of an activation failure
     * 
     * @deprecated will not be supported by the Dataprovider 2 stack
     */
    @Deprecated
    protected void applyLegacyConfiguration(
        Configuration configuration
    ) throws ServiceException{
        // Subclasses should override this method and propagate their
        // configuration values to corresponding java beans setters.
    }
    
    /**
     * Class LayerInteraction
     */
    public class LayerInteraction extends AbstractRestInteraction {
        
        public LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
            this.serviceHeader = ServiceHeader.toServiceHeader(
                connection.getMetaData().getUserName(), 
                null, // connectionPassword
                isPreferringNotFoundException()
            );            
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
            Object_2Facade objectFacade = Facades.newObject(input.getResourceIdentifier());
			objectFacade.setValue(input.getBody());
			return new DataproviderRequest(
			    ispec,
			    objectFacade.getDelegate()
			);
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
        
        protected final Layer_1 getDelegatingLayer(
        ) {
            return Layer_1.this.getDelegation();
        }
        
        protected final LayerInteraction getDelegatingInteraction(
        ) throws ServiceException {
            try {
                Port<RestConnection> delegation = this.getDelegatingLayer();
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
            return interaction != null && interaction.get(ispec, input, output);
        }

        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            LayerInteraction interaction = this.getDelegatingInteraction();
            return interaction != null && interaction.find(ispec, input, output);
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
            return interaction != null && interaction.delete(ispec, input, output);
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

    /**
     * Class DataproviderConnection
     */
    static class DataproviderConnection implements RestConnection {
    
		DataproviderConnection(
        	ConnectionFactory connectionFactory,	
            ServiceHeader serviceHeader            
        ) {
        	this.connectionFactory = connectionFactory;
            this.serviceHeader = serviceHeader;
        }

		final ConnectionFactory connectionFactory;
		final ServiceHeader serviceHeader;
        ConnectionMetaData metaData = null;
		
        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#close()
         */
        public void close(
        ) throws ResourceException {
            // Nothing to do
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#createInteraction()
         */
        public Interaction createInteraction(
        ) throws ResourceException {
            return null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Connection#getLocalTransaction()
         */
        public LocalTransaction getLocalTransaction(
        ) throws ResourceException {
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
        public ResultSetInfo getResultSetInfo(
        ) throws ResourceException {
            return null;
        }
        
        @Override
        public ConnectionFactory getConnectionFactory() {
        	return this.connectionFactory;
        }
        
    }
    
}
