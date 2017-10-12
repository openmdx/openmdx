/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract REST Interaction
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2017, OMSYEX AG, Switzerland
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

import java.util.UUID;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionMetaData;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;

/**
 * Abstract REST Interaction
 */
public class AbstractRestInteraction extends AbstractInteraction<RestConnection> {

    /**
     * Constructor 
     *
     * @param connection
     */
    protected AbstractRestInteraction(
        RestConnection connection
    ) {
        super(connection);
    }

    /**
     * Tells whether an object retrieval request shall throw a NOT_FOUND 
     * exception rather than returning and empty collection when a requested
     * object does not exist
     * 
     * @return <code>true</code> if an object retrieval request shall throw a NOT_FOUND 
     * exception rather than returning and empty collection when a requested
     * object does not exist
     */
    protected boolean isPreferringNotFoundException(){
        return false;
    }
    
    /**
     * Provide a request path by appending a UUID
     * 
     * @param target
     * 
     * @return a request path 
     */
    public Path newRequestId(
        Path target
    ){
        return target.getChild(UUIDs.newUUID().toString());
    }
    
    /**
     * Provide a response path by appending "*-";
     * 
     * @param requestId
     * 
     * @return a response path 
     */
    public Path newResponseId(
        Path requestId
    ){
        return requestId.getParent().getChild(requestId.getLastSegment().toClassicRepresentation() + "*-");
    }

    /**
     * Retrieve the REST connection specification
     * 
     * @return the REST ConnectionSpec
     * 
     * @throws ServiceException 
     */
    protected boolean isBulkLoad(
    ) throws ServiceException{
        try {
            ConnectionMetaData metaData = getConnection().getMetaData();
            return metaData instanceof RestConnectionMetaData && ((RestConnectionMetaData)metaData).isBulkLoad();
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }
    
    /**
     * Lazy model accessor retrieval
     * 
     * @return the model accessor
     */
    protected final Model_1_0 getModel(){
        return Model_1Factory.getModel();
    }
    
    /**
     * Pass the request to another handler
     * <p>
     * The default implementation returns <code>false</code>
     * because it doesn't handle the request.
     * 
     * @param xri
     * @param ispec
     * @param input
     * @param output
     * 
     * @return <code>false</code>
     */
    public boolean pass(
        Path xri, 
        RestInteractionSpec ispec,
        MappedRecord input, 
        Record output
    ) throws ServiceException {
        return false;
    }
    
    /**
     * GET Collection
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean find(
        RestInteractionSpec ispec, 
        Query_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }

    /**
     * Process Collection
     * 
     * @param ispec the interaction spec
     * @param input the query
     * @param consumer the record processor
     */
    public boolean consume(
        RestInteractionSpec ispec, 
        Query_2Facade input, 
        ConsumerRecord consumer
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            consumer
        );
    }
    
    /**
     * GET Object
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean get(
        RestInteractionSpec ispec, 
        Query_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }
    
    /**
     * DELETE Collection
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean delete(
        RestInteractionSpec ispec, 
        Query_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }

    /**
     * Validate an object's version
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean validate(
        RestInteractionSpec ispec, 
        Object_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }
    
    /**
     * PUT Object
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean put(
        RestInteractionSpec ispec, 
        Object_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }

    /**
     * DELETE Object
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean delete(
        RestInteractionSpec ispec, 
        Object_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }
    
    /**
     * Invoke Method
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean invoke(
        RestInteractionSpec ispec, 
        MessageRecord input, 
        MessageRecord output
    ) throws ServiceException {
        return pass(
            input.getResourceIdentifier(), 
            ispec,
            input, 
            output
        );
    }

    /**
     * Create Object
     * 
     * @param ispec
     * @param input
     * @param output
     */
    public boolean create(
        RestInteractionSpec ispec, 
        Object_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            input.getPath(), 
            ispec,
            input.getDelegate(), 
            output
        );
    }
    
    /**
     * Make a transient object persistent
     * 
     * @param ispec the PUT interaction specification
     * @param xri the transient object id
     * @param input the object facade
     * @param output the output record, which may be <code>null</code>
     * 
     * @return <code>true</code> if the object has been made persistent
     */
    public boolean move(
        RestInteractionSpec ispec, 
        Path xri,
        Object_2Facade input, 
        IndexedRecord output
    ) throws ServiceException {
        return pass(
            xri, 
            ispec,
            input.getDelegate(),
            output
        );
    }
    
    /**
     * Treat the map's keys as resource identifiers
     * 
     * @param resourceIdentifier the map's key
     * 
     * @return the corresponding <code>Path</code>
     * 
     * @throws ResourceException
     */
    private Path toResourceIdentifier(
        Object resourceIdentifier
    ) throws ResourceException{
        if(resourceIdentifier instanceof Path) {
            return (Path) resourceIdentifier;
        } else if (resourceIdentifier instanceof String) {
            return new Path((String)resourceIdentifier);
        } else if (resourceIdentifier instanceof UUID) {
            return new Path((UUID)resourceIdentifier);
        } else {
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "The map's keys should be resource identifiers",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("actual", resourceIdentifier == null ? null : resourceIdentifier.getClass().getName()),
                        new BasicException.Parameter("supported", String.class.getName(), Path.class.getName(), UUID.class.getName())
                    )
                )
            );
        }
    }
    
    /**
     * Validate the record type
     * 
     * @param usage
     * @param to
     * @param value
     * @param optional
     * 
     * @return the validated record
     * 
     * @throws NotSupportedException
     */
    protected static <T> T cast(
        String usage,
        Class<T> to,
        Object value,
        boolean optional
    ) throws NotSupportedException{
        if(to.isInstance(value) || (optional && value == null)) {
            return to.cast(value);
        }
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Unexpected " + (usage == null ? to.getSimpleName() : usage),
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("supported", to.getName()),
                    new BasicException.Parameter("actual", value == null ? null : value.getClass().getName())
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    @Override
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        assertOpened();
        try {
            RestInteractionSpec interactionSpec = AbstractRestInteraction.cast(
                null, 
                RestInteractionSpec.class, 
                ispec, 
                false
            );            
            if(input instanceof MappedRecord) {
                if(MessageRecord.NAME.equals(input.getRecordName())) {
                    invoke(
                        interactionSpec,
                        AbstractRestInteraction.cast(
                            "Invocation Request", 
                            MessageRecord.class, 
                            input, 
                            false
                        ),
                        AbstractRestInteraction.cast(
                            "Invocation Reply", 
                            MessageRecord.class, 
                            output, 
                            true
                        )
                    );
                    return true;
                } else {
                    MappedRecord inputRecord = (MappedRecord) input;
                    if(org.openmdx.base.rest.spi.QueryRecord.isCompatible(inputRecord)){
                        Query_2Facade facade = Query_2Facade.newInstance(inputRecord, isPreferringNotFoundException());
                        switch(interactionSpec.getFunction()) {
                            case GET:
                                if(facade.isFindRequest()) {
                                    if(output == null || output instanceof IndexedRecord) {
                                        return find(
                                            interactionSpec, 
                                            facade, 
                                            (IndexedRecord) output
                                        );
                                    } else if (output instanceof ConsumerRecord) {
                                        return consume(
                                            interactionSpec, 
                                            facade, 
                                            (ConsumerRecord) output
                                        );
                                    }
                                } else {
                                    if(output == null || output instanceof IndexedRecord) {
                                        return get(
                                            interactionSpec, 
                                            facade, 
                                            (IndexedRecord) output
                                        );
                                    } else if (output instanceof ConsumerRecord) {
                                        for(Object object : (IndexedRecord) execute(interactionSpec, input)){
                                            ((ConsumerRecord)output).accept(
                                                AbstractRestInteraction.cast(
                                                    "GET reply to be consumed", 
                                                    ObjectRecord.class, 
                                                    object, 
                                                    true
                                                )
                                            );
                                            return true; // singleton consumed
                                        }
                                        return false; // nothing consumed
                                    }
                                }
                                throw ResourceExceptions.initHolder(
                                    new NotSupportedException(
                                        "Unexpected output Record",
                                        BasicException.newEmbeddedExceptionStack(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.BAD_PARAMETER,
                                            new BasicException.Parameter("supported", IndexedRecord.class.getName(), ConsumerRecord.class.getName()),
                                            new BasicException.Parameter("actual", output == null ? null : output.getClass().getName())
                                        )
                                    )
                                );
                            case DELETE: 
                                return delete(
                                    interactionSpec, 
                                    facade, 
                                    AbstractRestInteraction.cast(
                                        "Result Set", 
                                        IndexedRecord.class, 
                                        output, 
                                        true
                                    )
                                );
                            default: 
                                return false;
                        }
                    } else { 
                        final IndexedRecord outputRecord = AbstractRestInteraction.cast(
                            "GET reply to be consumed", 
                            IndexedRecord.class, 
                            output, 
                            true
                        );
                        if (org.openmdx.base.rest.spi.ObjectRecord.isCompatible(inputRecord)) {
                            Object_2Facade facade = Object_2Facade.newInstance(inputRecord);
                            if(facade.isProxyOperation()) {
                            	final Path transientObjectId = toResourceIdentifier(facade.getTransientObjectId());
                                switch(interactionSpec.getFunction()) {
    	                            case POST: {
    									final Object_2Facade compatibility = Facades.newObject(transientObjectId);
    	                                compatibility.setValue(
    	                                    AbstractRestInteraction.cast(
    	                                        "Value", 
    	                                        MappedRecord.class, 
    	                                        facade.getValue(), 
    	                                        false
    	                                    )
    	                                );
    	                                return create(
    	                                    interactionSpec,
    	                                    compatibility,
    	                                    outputRecord
    	                                );
    	                            }
    	                            case PUT: {
    	                                return move(
    	                                    interactionSpec,
    	                                    transientObjectId,
    	                                    facade,
    	                                    outputRecord
    	                                );
    	                            }
    	                            default:
    	                                return false;
    	                        }
                            } else {
    	                        switch(interactionSpec.getFunction()) {
    	                            case GET:
    	                                return validate(
    	                                    interactionSpec, 
    	                                    facade, 
    	                                    outputRecord
    	                                );
    	                            case PUT: 
    	                                return put(
    	                                    interactionSpec, 
    	                                    facade, 
    	                                    outputRecord
    	                                );
    	                            case DELETE:
    	                                return delete(
    	                                    interactionSpec, 
    	                                    facade, 
    	                                    outputRecord
    	                                );
    	                            case POST: 
    	                                return create(
    	                                    interactionSpec, 
    	                                    facade, 
    	                                    outputRecord
    	                                );
    	                            default: 
    	                                return false;
    	                        }
                            }
                        } else {
                            throw ResourceExceptions.initHolder(
                                new NotSupportedException(
                                    "Unexpected mapped input record name",
                                    BasicException.newEmbeddedExceptionStack(
                                        BasicException.Code.DEFAULT_DOMAIN,
                                        BasicException.Code.BAD_PARAMETER,
                                        new BasicException.Parameter("supported", QueryRecord.NAME, ObjectRecord.NAME, MessageRecord.NAME),
                                        new BasicException.Parameter("actual", input == null ? null : input.getRecordName())
                                    )
                                )                        
                            );
                        }
                    }
                }
            } else if (input instanceof IndexedRecord) {
                if("list".equals(input.getRecordName())){
                    switch(interactionSpec.getFunction()) {
                        case GET:
                            IndexedRecord entries = (IndexedRecord) input;
                            for(Object resourceIdentifier : entries){
                                Query_2Facade facade = Facades.newQuery(toResourceIdentifier(resourceIdentifier));
                                execute(ispec, facade.getDelegate(), output);
                            }
                            return !entries.isEmpty();
                        default:
                            return false;
                    }
                } else {
                    throw ResourceExceptions.initHolder(
                        new NotSupportedException(
                            "Unexpected indexed input record name",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("supported", "list"),
                                new BasicException.Parameter("actual", input == null ? null : input.getRecordName())
                            )
                        )                        
                    );
                }
            } else {
                throw ResourceExceptions.initHolder(
                    new NotSupportedException(
                        "Unexpected input record",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("supported", MappedRecord.class.getName(), IndexedRecord.class.getName()),
                            new BasicException.Parameter("actual", input == null ? null : input.getClass().getName())
                        )
                    )
                );
            }
        } catch (ServiceException exception) {
        	throw ResourceExceptions.toResourceException(exception);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    @Override
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        if(ispec instanceof RestInteractionSpec){
            final RestInteractionSpec interactionSpec = (RestInteractionSpec) ispec;
            final Record reply = newReply(interactionSpec, input);
            if(execute(ispec, input, reply)) {
                return reply;
            }
            throw ResourceExceptions.initHolder(
                new NotSupportedException(
                    "This interaction is not supported",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("interactionSpec", interactionSpec),
                        new BasicException.Parameter("input", input)
                    )
                )
            );
        }
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Unexpected Interaction Spec",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("supported", RestInteractionSpec.class.getName()),
                    new BasicException.Parameter("actual", ispec == null ? null : ispec.getClass().getName())
                )
            )
        );
   }

	/**
	 * Create a reply object for return value invocations
	 *
	 * @param interactionSpec the original interaction specification
	 * @param input the original input record
	 *  
	 * @return a new reply object for return value invocations
	 * 
	 * @throws ResourceException
	 */
	private Record newReply(
		final RestInteractionSpec interactionSpec,
		Record input
	) throws ResourceException {
		final Record reply;
		if(interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND){
		    reply = null;
		} else if(MessageRecord.NAME.equals(input.getRecordName())) {
		    reply = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
		} else {
		    reply = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		}
		return reply;
	}
    
}