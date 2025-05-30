/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Interaction 
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
package org.openmdx.base.resource.spi;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;
import javax.resource.spi.IllegalStateException;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.ResourceWarning;
import jakarta.resource.spi.IllegalStateException;
#endif

import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.time.SystemClock;

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
        this(connection, null);
    }

    /**
     * Constructor 
     *
     * @param connection
     */
    protected AbstractInteraction(
        C connection,
        Interaction delegate
    ) {
        this.connection = connection;
        this.delegate = delegate;
    }

    /**
     * The delegate, may be {@code null}
     */
    private final Interaction delegate;
    
    /**
     * The open flag
     */
    private volatile boolean opened = false;
    
    /**
     * The open flag
     */
    private volatile boolean closed = false;

    /**
     * The chain of warnings
     */
    private ResourceWarning warnings;
    
    /**
     * The {@code Interaction}'s owner
     */
    private final C connection;
    
    /**
     * The interaction time
     */
    private final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif interactionTime = SystemClock.getInstance().now();

    protected final boolean hasDelegate(){
    	return this.delegate != null;
    }
    
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
     * Assert that the {@code Interaction} is open
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
     * Assert that the {@code Interaction} is not closed
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
     * Add a warning to the head of the chain
     * 
     * @param warning
     */
    @SuppressWarnings("deprecation")
    protected final void addWarning(
        ResourceWarning warning
    ){
    	if(this.warnings != null) {
    		warning.setLinkedWarning(this.warnings);    		
    	} else if(hasDelegate()) {
			try {
				warning.setLinkedWarning(this.delegate.getWarnings());
			} catch (ResourceException exception) {
				warning.setLinkedWarning(
					new ResourceWarning(
						"Unable to retrieve the delegate's warnings", 
						exception
					)
				);
			} finally {
				try {
					this.delegate.clearWarnings();
				} catch (ResourceException exception) {
					SysLog.warning(
						"Unable to clear the delegate's warnings", 
						exception
					);
				}
			}
		}
        this.warnings = warning;
    }

    @Override
    public final void clearWarnings(
    ) throws ResourceException {
        this.warnings = null;
        if(hasDelegate()) {
	        this.delegate.clearWarnings();
        }
    }

    @Override
    public void close(
    ) throws ResourceException {
        assertNotClosed();
        if(hasDelegate()) {
	        this.delegate.close();
        }
        this.closed = true;
    }

    @Override
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
    	if(hasDelegate()) {
    		return this.delegate.execute(ispec, input, output);
    	}
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

    @Override
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
    	if(hasDelegate()) {
    		return this.delegate.execute(ispec, input);
    	}
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


    @Override
    public final C getConnection(
    ) {
        return this.connection;
    }

    @Override
    public final ResourceWarning getWarnings(
    ) throws ResourceException {
        return this.warnings;
    }

    /**
     * Provide the actual principal chain
     * 
     * @return the principal chain
     * 
     * @throws ResourceException
     */
    protected List<String> getPrincipalChain(
    ) throws ResourceException{
    	return PersistenceManagers.toPrincipalChain(connection.getMetaData().getUserName());
    }

    /**
     * The default value is the date and time when the interaction has been created
     * 
     * @return the interaction time
     */
    protected #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif getInteractionTime(){
    	return this.interactionTime;
    }
    
}
