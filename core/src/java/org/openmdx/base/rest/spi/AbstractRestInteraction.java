/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract REST Interaction
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
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
        @Nonnull RestConnection connection
    ){
        super(connection);
    }

    /**
     * Constructor with delegate
     */
    protected AbstractRestInteraction(
    	@Nonnull RestConnection connection,
		@Nullable Interaction delegate
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * GET Object
     * 
     * @param ispec
     * @param input
     * @param output
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * 
     * @param ispec
     * @param input
     * @param output
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
     * @param output the output record, which may be <code>null</code>
     * @return <code>true</code> if the object has been made persistent
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
        RestInteractionSpec interactionSpec = AbstractRestInteraction.cast(
            "interaction spec", 
            RestInteractionSpec.class, 
            ispec, 
            false // optional
        );            
        if(input instanceof MessageRecord) {
            return handleOperation(interactionSpec, input, output);
        } else {
            final ResultRecord outputRecord = AbstractRestInteraction.cast(
                "output record", 
                ResultRecord.class, 
                output, 
                true
            );
            if(input instanceof QueryRecord){
                return handleQuery(interactionSpec, (QueryRecord) input, outputRecord);
            } else if (input instanceof ObjectRecord) {
                return handleObject(interactionSpec, (ObjectRecord) input, outputRecord);
            }
        }
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

	private boolean handleOperation(
		RestInteractionSpec interactionSpec,
		Record input,
		Record output
	) throws ResourceException {
		final MessageRecord outputRecord = AbstractRestInteraction.cast(
		    "Invocation Reply", 
		    MessageRecord.class, 
		    output, 
		    true
		);
		return invoke(
		    interactionSpec,
		    (MessageRecord)input,
		    outputRecord
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

	private boolean handleQuery(
		RestInteractionSpec interactionSpec,
		QueryRecord queryRecord, 
		ResultRecord outputRecord
	) throws ResourceException {
		switch(interactionSpec.getFunction()) {
		    case GET:
		        final Path xri = queryRecord.getResourceIdentifier();
		        final boolean findRequest = xri.isContainerPath() || xri.isPattern();
		        return findRequest ? find(
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