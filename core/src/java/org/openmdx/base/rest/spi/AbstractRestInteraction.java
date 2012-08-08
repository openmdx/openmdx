/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AbstractRestInteraction.java,v 1.27 2011/11/26 01:34:57 hburger Exp $
 * Description: Abstract REST Interaction
 * Revision:    $Revision: 1.27 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
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
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;

/**
 * Abstract REST Interaction
 */
public class AbstractRestInteraction extends AbstractInteraction<Connection> {

    /**
     * Constructor 
     *
     * @param connection
     */
    protected AbstractRestInteraction(
        Connection connection
    ) {
        super(connection);
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
        return requestId.getParent().getChild(requestId.getBase() + "*-");
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
            input.getPath(), 
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
            Records.getRecordFactory().singletonMappedRecord( 
                "map",
                null, // recordShortDescription, 
                xri,
                input.getDelegate()
            ), 
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
    @SuppressWarnings("unchecked")
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
                    IndexedRecord outputRecord = AbstractRestInteraction.cast(
                        "Result Set", 
                        IndexedRecord.class, 
                        output, 
                        true
                    );
                    if(Query_2Facade.isDelegate(inputRecord)){
                        Query_2Facade facade = Query_2Facade.newInstance(inputRecord);
                        Path objectId = facade.getPath();
                        switch(interactionSpec.getFunction()) {
                            case GET:
                                return objectId.size() % 2 == 0 || objectId.containsWildcard() ? find(
                                    interactionSpec, 
                                    facade, 
                                    outputRecord
                                ) : get(
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
                            default: 
                                return false;
                        }
                    } else if (Object_2Facade.isDelegate(inputRecord)) {
                        Object_2Facade facade = Object_2Facade.newInstance(inputRecord);
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
                    } else if ("map".equals(input.getRecordName())){
                        switch(interactionSpec.getFunction()) {
                            case POST: {
                                for(Map.Entry<?, ?> e : (Set<Map.Entry<?, ?>>) inputRecord.entrySet()) {
                                    Object_2Facade facade = Facades.newObject(toResourceIdentifier(e.getKey()));
                                    facade.setValue(
                                        AbstractRestInteraction.cast(
                                            "Value", 
                                            MappedRecord.class, 
                                            e.getValue(), 
                                            false
                                        )
                                    );
                                    create(
                                        interactionSpec,
                                        facade,
                                        outputRecord
                                    );
                                }
                                return !inputRecord.isEmpty();
                            }
                            case PUT: {
                                for(Map.Entry<?, ?> e : (Set<Map.Entry<?, ?>>) inputRecord.entrySet()) {
                                    MappedRecord record = AbstractRestInteraction.cast(
                                        "Value", 
                                        MappedRecord.class, 
                                        e.getValue(), 
                                        false
                                    );
                                    if(Object_2Facade.isDelegate(record)) {
                                        move(
                                            interactionSpec,
                                            toResourceIdentifier(e.getKey()),
                                            Object_2Facade.newInstance(record),
                                            outputRecord
                                        );
                                    } else {
                                        throw ResourceExceptions.initHolder(
                                            new ResourceException(
                                                "The map's values should be an object holder",
                                                BasicException.newEmbeddedExceptionStack(
                                                    BasicException.Code.DEFAULT_DOMAIN,
                                                    BasicException.Code.BAD_PARAMETER,
                                                    new BasicException.Parameter("actual", record.getRecordName()),
                                                    new BasicException.Parameter("supported", ObjectRecord.NAME)
                                                )
                                            )
                                        );
                                    }
                                }
                                return !inputRecord.isEmpty();
                            }
                            default:
                                return false;
                        }
                    } else {
                        throw ResourceExceptions.initHolder(
                            new NotSupportedException(
                                "Unexpected mapped input record name",
                                BasicException.newEmbeddedExceptionStack(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.BAD_PARAMETER,
                                    new BasicException.Parameter("supported", QueryRecord.NAME, ObjectRecord.NAME, MessageRecord.NAME, "map"),
                                    new BasicException.Parameter("actual", input == null ? null : input.getRecordName())
                                )
                            )                        
                        );
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
            throw new ResourceException(
                exception.getCause().getDescription(),
                exception.getCause()
            );
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
            RestInteractionSpec interactionSpec = (RestInteractionSpec) ispec;
            Record reply;
            if(interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND){
                reply = null;
            } else if(MessageRecord.NAME.equals(input.getRecordName())) {
                MessageRecord request = (MessageRecord) input;
                if(request.getMessageId() == null) try {
                    request = request.clone();
                    request.setPath(newRequestId(request.getTarget()));
                } catch (CloneNotSupportedException exception) {
                    throw new ResourceException(exception); 
                }
                MessageRecord response = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                reply = response;
            } else {
                reply = Records.getRecordFactory().createIndexedRecord(ResultRecord.NAME);
            }
            if(execute(ispec, input, reply)) {
                return reply;
            } else {
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
        } else {
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
   }
    
}