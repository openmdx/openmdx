/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AbstractConnection_2.java,v 1.8 2010/03/19 12:32:54 hburger Exp $
 * Description: Abstract Connectoon
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/19 12:32:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import javax.jmi.reflect.RefException;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract Connection
 */
public abstract class AbstractConnection_2 extends AbstractConnection {

    /**
     * Constructor 
     * 
     * @param connectionSpec 
     */
    protected AbstractConnection_2(
        RestConnectionSpec connectionSpec
    ){
        super(connectionSpec);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    public Interaction createInteraction(
    ) throws ResourceException {
        return new DispatchingInteraction(this);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        throw ResourceExceptions.initHolder( 
            new NotSupportedException(
                "This REST connection does not support local transaction demarcation",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
            )
        );
    }

    /**
     * Handle data object 
     * 
     * @param function the REST function to be executed
     * @param request the request's data object facade
     * @param replies the reply holder, or <code>null</code> if the interaction verb is {@link InteractionSpec#SYNC_SEND}
     * 
     * @throws ServiceException
     * @throws RefException
     * @throws ResourceException
     */
    protected abstract void handle(
        RestFunction function,
        Object_2Facade request,
        IndexedRecord replies
    ) throws ResourceException;

    /**
     * Handle data object 
     * 
     * @param function the REST function to be executed
     * @param request the request's data object facade
     * @param replies the reply holder, or <code>null</code> if the interaction verb is {@link InteractionSpec#SYNC_SEND}
     * 
     * @throws ServiceException
     * @throws RefException
     * @throws ResourceException
     */
    protected abstract void handle(
        RestFunction function,
        Query_2Facade request,
        IndexedRecord replies
    ) throws ResourceException;
    
    
    //------------------------------------------------------------------------
    // Class InboundInteraction
    //------------------------------------------------------------------------

    /**
     * Inbound Interaction
     */
    static class DispatchingInteraction extends AbstractInteraction<AbstractConnection_2> {

        /**
         * Constructor 
         *
         * @param connection
         */
        DispatchingInteraction(
            AbstractConnection_2 connection
        ){
            super(connection);
        }

        /**
         * Validate the output record
         * 
         * @param output the output record to be validated
         * 
         * @return the validated output record 
         * 
         * @throws ResourceException
         */
        private IndexedRecord restOutput(
            Record output
        ) throws ResourceException{
            if(output == null || output instanceof IndexedRecord) {
                return (IndexedRecord) output;
            } else throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported output record class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", IndexedRecord.class.getName()),
                        new BasicException.Parameter("actual", output == null ? null : output.getClass().getName())
                    )
                )
            );
        }

        /**
         * Validate the interaction specification
         * 
         * @param interactionSpec the interaction specification to be validated
         * 
         * @return the validated interaction specification
         * 
         * @throws ResourceException
         */
        private RestInteractionSpec toRestInteractionSpec(
            InteractionSpec interactionSpec
        ) throws ResourceException{
            if(interactionSpec instanceof RestInteractionSpec) {
                return (RestInteractionSpec) interactionSpec;
            } else throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported interaction specification class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", RestInteractionSpec.class.getName()),
                        new BasicException.Parameter("actual", interactionSpec == null ? null : interactionSpec.getClass().getName())
                    )
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        protected boolean execute(
            RestInteractionSpec interactionSpec,
            MappedRecord input,
            IndexedRecord output
        ) throws ResourceException {
            RestFunction function = interactionSpec.getFunction();
            if(Query_2Facade.isDelegate(input)) {
                this.getConnection().handle(
                    function,
                    Query_2Facade.newInstance(input),
                    output
                );
            } else if (Object_2Facade.isDelegate(input)) {
                this.getConnection().handle(
                    function,
                    Object_2Facade.newInstance(input),
                    output
                );
            } else {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "Only POST requests accept pure maps as input",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("function", function),
                            new BasicException.Parameter("recordName", input.getRecordName())
                       )
                   )
                ); 
                
            }
            return output != null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        protected boolean execute(
            RestInteractionSpec interactionSpec,
            Record input,
            IndexedRecord output
        ) throws ResourceException {
            if (input instanceof MappedRecord) {
                return this.execute(
                    interactionSpec,
                    (MappedRecord) input,
                    interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND ? 
                        null : 
                            output
                );
            } 
            else throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported input record class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", MappedRecord.class.getName()),
                        new BasicException.Parameter("actual", input == null ? null : input.getClass().getName())
                    )
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            return this.execute(
                toRestInteractionSpec(ispec),
                input,
                restOutput(output)
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            RestInteractionSpec interactionSpec = this.toRestInteractionSpec(ispec);
            ResultRecord output = interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND ? 
                null :
                (ResultRecord) Records.getRecordFactory().createIndexedRecord(ResultRecord.NAME);
            this.execute(
                interactionSpec,
                input,
                output
            );
            return output;
        }
        
    }

}

