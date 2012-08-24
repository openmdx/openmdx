/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Interaction 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.resource.spi;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;
import javax.resource.spi.IllegalStateException;

import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract Interaction
 * <p>
 * At least one of the two execute() methods must be overridden.
 */
public abstract class AbstractInteraction<C extends Connection> implements Interaction {

    /**
     * Constructor 
     *
     * @param connection
     */
    protected AbstractInteraction(
        C connection
    ) {
        this.connection = connection;
    }

    /**
     * The open flag
     */
    private volatile boolean closed = false;

    /**
     * The open flag
     */
    private volatile boolean opened = false;
    
    /**
     * The chain of warnings
     */
    private ResourceWarning warnings;
    
    /**
     * The <code>Interaction</code>'s owner
     */
    private final C connection;

	/**
     * The interaction is opened lazily
     * 
     * @throws ResourceException
     */
    protected void open(
    ) throws ResourceException { 
        // Code added by subclasses if necessary
    }
    
    /**
     * Assert that the <code>Interaction</code> is open
     * 
     * @throws ResourceException
     */
    protected void assertOpened(
    ) throws ResourceException {
        assertNotClosed();
        if(!this.opened) synchronized(this){
            if(!this.opened) {
                open();
                this.opened = true;
            }
        }
    }
    
    /**
     * Assert that the <code>Interaction</code> is not closed
     * 
     * @throws ResourceException
     */
    protected void assertNotClosed(
    ) throws ResourceException {
        if(this.closed) {
            throw ResourceExceptions.initHolder(
                new IllegalStateException(
                    "This interaction is already closed",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
               )
           );
        }
    }
    
    /**
     * Add a warning to the chain
     * 
     * @param warning
     */
    @SuppressWarnings("deprecation")
    protected final void addWarning(
        ResourceWarning warning
    ){
        if(this.warnings == null) {
            this.warnings = warning;
        } else {
            ResourceWarning chain = this.warnings;
            while(chain.getLinkedWarning() != null) {
                chain = chain.getLinkedWarning();
            }
            chain.setLinkedWarning(warning);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#clearWarnings()
     */
    public final void clearWarnings(
    ) throws ResourceException {
        this.warnings = null;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#close()
     */
    public void close(
    ) throws ResourceException {
        assertNotClosed();
        this.closed = true;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Execution with input and output record is not supported",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
           )
       );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Execution with input record and return value is not supported",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
           )
       );
    }

    
    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#getConnection()
     */
    public final C getConnection(
    ) {
        return this.connection;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Interaction#getWarnings()
     */
    public final ResourceWarning getWarnings(
    ) throws ResourceException {
        return this.warnings;
    }

}
