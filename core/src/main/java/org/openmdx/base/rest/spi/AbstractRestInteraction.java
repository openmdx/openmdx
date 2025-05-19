/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract REST Interaction
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.rest.spi;

import java.util.Collection;
import java.util.List;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
#endif

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionMetaData;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract REST Interaction
 */
public class AbstractRestInteraction extends AbstractInteraction<RestConnection> {

    /**
     * Constructor without delegate
     */
    protected AbstractRestInteraction(
        RestConnection connection
    ){
        super(connection);
    }

    /**
     * Constructor with delegate
     */
    protected AbstractRestInteraction(
    	RestConnection connection,
		Interaction delegate
	){
		super(connection, delegate);
	}

	/**
     * Provide a response path by appending "*-";
     * 
     * @param requestId
     * 
     * @return a response path 
     */
    protected Path newResponseId(
        Path requestId
    ){
        return requestId.getParent().getChild(requestId.getLastSegment().toClassicRepresentation() + "*-");
    }

    protected QueryRecord newQuery(
        Path resourceIdentifier
    ){
        QueryRecord query = new org.openmdx.base.rest.spi.QueryRecord();
        query.setResourceIdentifier(resourceIdentifier);
        return query;
    }
    
    protected ObjectRecord newObject(
        Path path
    ){
        ObjectRecord object = new org.openmdx.base.rest.spi.ObjectRecord();
        object.setResourceIdentifier(path);
        return object;
    }
    
    /**
     * Retrieve the REST connection specification
     * 
     * @return the REST ConnectionSpec
     * 
     * @throws ServiceException 
     */
    protected boolean isBulkLoad(
    ) throws ResourceException {
        ConnectionMetaData metaData = getConnection().getMetaData();
        return metaData instanceof RestConnectionMetaData && ((RestConnectionMetaData)metaData).isBulkLoad();
    }
    
    /**
     * Pass the request to another handler
     */
    protected boolean pass(
        RestInteractionSpec ispec, 
        RequestRecord input,
        Record output
    ) throws ResourceException {
        return hasDelegate() && super.execute(ispec, input, output);
    }
    
    /**
     * GET Collection
     */
    protected boolean find(
        RestInteractionSpec ispec, 
        QueryRecord input, 
        ResultRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }

    /**
     * GET and consume Collection
     */
    protected boolean consume(
        RestInteractionSpec ispec, 
        QueryRecord input, 
        ConsumerRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }

    
    /**
     * GET Object
     */
    protected boolean get(
        RestInteractionSpec ispec, 
        QueryRecord input, 
        ResultRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }
    
    /**
     * DELETE Collection
     */
    protected boolean delete(
        RestInteractionSpec ispec, 
        QueryRecord input
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            null
        );
    }

    /**
     * Validate an object's version
     */
    protected boolean verify(
        RestInteractionSpec ispec, 
        ObjectRecord input
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            null
        );
    }
    
    /**
     * PUT Object
     */
    protected boolean update(
        RestInteractionSpec ispec, 
        ObjectRecord input, 
        ResultRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }

    /**
     * DELETE Object
     */
    protected boolean delete(
        RestInteractionSpec ispec, 
        ObjectRecord input
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            null
        );
    }
    
    /**
     * Invoke Method
     */
    protected boolean invoke(
        RestInteractionSpec ispec, 
        MessageRecord input, 
        MessageRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }

    /**
     * Create Object
     */
    protected boolean create(
        RestInteractionSpec ispec, 
        ObjectRecord input, 
        ResultRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }
    
    /**
     * Make a transient object persistent
     * 
     * @param ispec the PUT interaction specification
     * @param input the object record
     * @param output the output record, which may be {@code null}
     * @return {@code true} if the object has been made persistent
     */
    protected boolean move(
        RestInteractionSpec ispec, 
        ObjectRecord input,
        ResultRecord output
    ) throws ResourceException {
        return pass(
            ispec, 
            input,
            output
        );
    }
    
    /**
     * Validate the record type
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

    @Override
    public boolean execute(
        InteractionSpec ispec,
        Record input,
        Record output
    ) throws ResourceException {
    	assertOpened();
        RestInteractionSpec interactionSpec = AbstractRestInteraction.cast(
            "interaction spec", 
            RestInteractionSpec.class, 
            ispec, 
            false // optional
        );            
        if(input instanceof MessageRecord) {
            if(output == null || output instanceof MessageRecord) {
                return handleOperation(interactionSpec, (MessageRecord)input, (MessageRecord)output);
            }
        } else if (input instanceof QueryRecord) {
            if(output instanceof ConsumerRecord) {
                return handleConsumer(interactionSpec, (QueryRecord)input, (ConsumerRecord)output);
            } else if (output == null || output instanceof ResultRecord) {
                return handleQuery(interactionSpec, (QueryRecord) input, (ResultRecord)output);
            }
        } else if (input instanceof ObjectRecord) {
            if (output == null || output instanceof ResultRecord) {
                return handleObject(interactionSpec, (ObjectRecord) input, (ResultRecord)output);
            }
        }
        throw ResourceExceptions.initHolder(
    		new NotSupportedException(
				"Unexpected record ",
				BasicException.newEmbeddedExceptionStack(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.BAD_PARAMETER,
					new BasicException.Parameter("supported-input", QueryRecord.NAME, ObjectRecord.NAME, MessageRecord.NAME),
					new BasicException.Parameter("actual-input", input == null ? null : input.getRecordName()),
                    new BasicException.Parameter("supported-output", ResultRecord.NAME, ConsumerRecord.NAME),
                    new BasicException.Parameter("actual-output", output == null ? null : output.getRecordName())
				)
			)                        
		);
    }

    private boolean handleOperation(
		RestInteractionSpec interactionSpec,
		MessageRecord input,
		MessageRecord output
	) throws ResourceException {
		return invoke(
		    interactionSpec,
		    input,
		    output
		);
	}

	private boolean handleObject(
		RestInteractionSpec interactionSpec,
		ObjectRecord objectRecord, 
		ResultRecord outputRecord
	) throws ResourceException {
		return objectRecord.getTransientObjectId() == null ? handlePersistentObject(
	    	interactionSpec, 
	    	objectRecord,
			outputRecord
		) : handleTransientObject(
	    	interactionSpec, 
	    	objectRecord,
			outputRecord
		);
	}

	private boolean handleTransientObject(
		RestInteractionSpec interactionSpec,
		ObjectRecord objectRecord, 
		ResultRecord outputRecord
	) throws ResourceException {
		switch(interactionSpec.getFunction()) {
		    case POST: {
		    	final Path transientObjectId = new Path(objectRecord.getTransientObjectId());
		    	final ObjectRecord compatibilityObject = objectRecord.clone();
		    	compatibilityObject.setResourceIdentifier(transientObjectId);
		        return create(
		            interactionSpec,
		            compatibilityObject,
		            outputRecord
		        );
		    }
		    case PUT: {
				return move(
		            interactionSpec,
		            objectRecord,
		            outputRecord
		        );
		    }
		    default:
		        return false;
		}
	}

	private boolean handlePersistentObject(
		RestInteractionSpec interactionSpec,
		ObjectRecord objectRecord, 
		ResultRecord outputRecord
	) throws ResourceException {
		switch(interactionSpec.getFunction()) {
		    case GET:
		        return verify(
		            interactionSpec, 
		            objectRecord
		        );
		    case PUT: 
		        return update(
		            interactionSpec, 
		            objectRecord, 
		            outputRecord
		        );
		    case DELETE:
		        return delete(
		            interactionSpec, 
		            objectRecord
		        );
		    case POST: 
		        return create(
		            interactionSpec, 
		            objectRecord, 
		            outputRecord
		        );
		    default: 
		        return false;
		}
	}

	/**
	 * Retrieves or deletes the matching objects
	 * 
	 * @throws ResourceException
	 */
	private boolean handleQuery(
		RestInteractionSpec interactionSpec,
		QueryRecord queryRecord, 
		ResultRecord outputRecord
	) throws ResourceException {
		switch(interactionSpec.getFunction()) {
		    case GET:
		        return isCollectionRequest(queryRecord) ? find(
		            interactionSpec, 
		            queryRecord, 
		            outputRecord
		        ) : get(
		            interactionSpec, 
		            queryRecord, 
		            outputRecord
		        );
		    case DELETE: 
		        return delete(
		            interactionSpec, 
		            queryRecord
		        );
		    default: 
		        return false;
		}
	}

    /**
     * Consumes the matching objects
     * 
     * @throws ResourceException
     */
    private boolean handleConsumer(
        RestInteractionSpec interactionSpec,
        QueryRecord queryRecord,
        ConsumerRecord consumer
    ) throws ResourceException {
        switch(interactionSpec.getFunction()) {
            case GET:
                return isCollectionRequest(queryRecord) ? consume(
                    interactionSpec, 
                    queryRecord, 
                    consumer
                ) : handleSingletonConsumer(
                    interactionSpec,
                    queryRecord,
                    consumer
                );
          default:
              return false;
        }
    }

    private boolean handleSingletonConsumer(
        RestInteractionSpec interactionSpec,
        QueryRecord queryRecord,
        ConsumerRecord consumer
    ) throws ResourceException {
        final ResultRecord outputRecord = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
        final boolean success = get(
            interactionSpec, 
            queryRecord, 
            outputRecord
        );
        if(success) {
            for(Object object : outputRecord) {
                final ObjectRecord objectRecord = AbstractRestInteraction.cast(
                    "singleton object", 
                    ObjectRecord.class, 
                    object, 
                    true
                );
                consumer.accept(objectRecord);
            }
        }
        return success;
    }

    private boolean isCollectionRequest(QueryRecord queryRecord) {
        final Path xri = queryRecord.getResourceIdentifier();
        return xri.isContainerPath() || xri.isPattern();
    }
	
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    @Override
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
    	RestInteractionSpec interactionSpec = AbstractRestInteraction.cast(
		    "interaction spec", 
		    RestInteractionSpec.class, 
		    ispec, 
		    true
		);
        final Record reply = createReply(
        	input, 
        	interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND
        );
        return execute(interactionSpec, input, reply) ? reply : null;
   }

	public static Record createReply(
		Record input, 
		boolean sendOnly
	) throws ResourceException {
		if(sendOnly){
            return null;
        } else if(input instanceof MessageRecord) {
            return Records.getRecordFactory().createMappedRecord(MessageRecord.class);
        } else {
            return Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
        }
	}
	
	@SuppressWarnings("unchecked")
	protected IndexedRecord toIndexedRecordList(
		List<?> values	
	) throws ResourceException{
		final IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.code());
		target.addAll(values);
		return target;
	}

	@SuppressWarnings("unchecked")
	protected IndexedRecord toIndexedRecordSet(
		Collection<?> values	
	) throws ResourceException{
		final IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicity.SET.code());
		target.addAll(values);
		return target;
	}

}