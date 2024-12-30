/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Connection Adapter
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

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.LocalTransactionException;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.Record;
import jakarta.resource.spi.EISSystemException;
import jakarta.resource.spi.LocalTransactionException;
#endif

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.loading.Classes;

/**
 * Wraps a REST Port into a JCA Connection
 * <p>
 * Note:<ul>
 * <li>This class does not supports the transaction attribute REQUIRES_NEW.
 * <li>This class caches the interaction for re-use.
 * </ul>
 */
public class ConnectionAdapter extends AbstractConnection {

	/**
	 * Constructor 
	 */
    protected ConnectionAdapter(
    	RestConnectionFactory connectionFactory,	
        RestConnectionSpec connectionSpec
    ){
    	super(connectionFactory, connectionSpec);
        this.transactionProxy = newTransactionProxy();
    }

    /**
     * 
     */
    private final LocalTransaction transactionProxy;
    
    /**
     * The REST interaction
     */
    protected Interaction interaction;
    
    /**
     * Uses the transaction manager
     */
    private static ConnectionAdapterFactory suspendingConnectionAdapterFactory;
    
    /**
     * Virtual Transaction Object Id Reference 
     */
    protected static final Path TRANSACTION_OBJECTS = new Path(
        "xri://@openmdx*org.openmdx.kernel/transaction"
    );

	/* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractConnection#getConnectionFactory()
     */
    @Override
    public RestConnectionFactory getConnectionFactory() {
        return (RestConnectionFactory) super.getConnectionFactory();
    }

    protected LocalTransaction newTransactionProxy() {
		return getConnectionFactory().isLocalTransactionDemarcationSupported() ?
			new LocalTransactionAdapter() : 
			new NoTransactionAdapter();
	}
    
    private static ConnectionAdapterFactory getSuspendingConnectionAdapterFactory(
    ) throws ResourceException {
        if(suspendingConnectionAdapterFactory == null) {
            try {
                suspendingConnectionAdapterFactory = Classes.newApplicationInstance(
                    ConnectionAdapterFactory.class, 
                    "org.openmdx.application.rest.adapter.JTAConnectionAdapterFactory"
                );
            } catch (Exception exception) {
                throw new EISSystemException(
                    "Unable to acquire the ConnectionAdapterFactory",
                    exception
                );
            }
        }
        return suspendingConnectionAdapterFactory;
    }
    
    /**
     * Create a connection using the given port
     * 
     * @param connectionFactory 
     * @param connectionSpec 
     * 
     * @return the corresponding JCA connection
     * 
     * @throws ResourceException  
     */
    static RestConnection newInstance(
        RestConnectionFactory connectionFactory, 
        RestConnectionSpec connectionSpec
    ) throws ResourceException{
        return isTransactionSuspensionRequired(connectionFactory) ? 
           getSuspendingConnectionAdapterFactory().newConnectionAdapter(
                connectionFactory, 
                connectionSpec
           ) : new ConnectionAdapter(
            	connectionFactory, 
            	connectionSpec
           );
    }

    private static boolean isTransactionSuspensionRequired(
        RestConnectionFactory connectionFactory
    ){
    	return 
    		!connectionFactory.isLocalTransactionDemarcationSupported() &&
    		connectionFactory.getTransactionAttribute() == TransactionAttributeType.REQUIRES_NEW;
    }
    	
    /**
     * Retrieve the delegate interaction, which may be shared by 
     * different interaction adapters.
     * 
     * @return the delegate interaction
     * 
     * @throws ResourceException
     */
    protected Interaction getDelegate(
    ) throws ResourceException {
        if(this.interaction == null) {
            this.interaction = getConnectionFactory().getPort().getInteraction(this);
        }
        return this.interaction;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    @Override
    public Interaction createInteraction(
    ) throws ResourceException {
        this.assertOpen();
        return new InteractionAdapter(this.getDelegate());
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
	@Override
    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        return this.transactionProxy;
    }


	//------------------------------------------------------------------------
    // Class LocalTransactionAdapter
    //------------------------------------------------------------------------
    
    /**
     * Local Transaction Adapter
     */
    class LocalTransactionAdapter implements LocalTransaction {

        /**
         * The current transaction object
         */
        private MappedRecord currentTransaction = null;
        
        /* (non-Javadoc)
         * @see javax.resource.cci.LocalTransaction#begin()
         */
        public void begin(
        ) throws ResourceException {
            if(this.currentTransaction != null) {
                throw new LocalTransactionException("There is already an active transaction");
            }
            this.currentTransaction = (MappedRecord) ((IndexedRecord) 
                ConnectionAdapter.this.interaction.execute(
                    InteractionSpecs.getRestInteractionSpecs(true).CREATE,
                    Object_2Facade.newInstance(
                        ConnectionAdapter.TRANSACTION_OBJECTS,
                        "org:openmdx:kernel:UnitOfWork"
                    ).getDelegate()
                )
            ).get(0);
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.LocalTransaction#commit()
         */
        public void commit(
        ) throws ResourceException {
            if(this.currentTransaction == null) {
                throw new LocalTransactionException("There is no active transaction");
            }
            try {
                MessageRecord input = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
                input.setResourceIdentifier(Object_2Facade.getPath(this.currentTransaction).getChild("commit"));
                input.setBody(null);
                ConnectionAdapter.this.interaction.execute(
                    InteractionSpecs.getRestInteractionSpecs(true).INVOKE,
                    input
                );
            } finally {
                this.currentTransaction = null;
            }
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.LocalTransaction#rollback()
         */
        public void rollback(
        ) throws ResourceException {
            if(this.currentTransaction == null) {
                throw new LocalTransactionException("There is no active transaction");
            }
            try {
                ConnectionAdapter.this.interaction.execute(
                    InteractionSpecs.getRestInteractionSpecs(true).DELETE,
                    this.currentTransaction
                );
            } finally {
                this.currentTransaction = null;
            }
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class NoTransactionAdapter
    //------------------------------------------------------------------------
    
    /**
     * No Transaction Adapter
     */
    static class NoTransactionAdapter implements LocalTransaction {

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#begin()
         */
        @Override
        public void begin(
        ) throws ResourceException {
            // nothing to do
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#commit()
         */
        @Override
        public void commit(
        ) throws ResourceException {
            // nothing to do
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#rollback()
         */
        @Override
        public void rollback(
        ) throws ResourceException {
            // nothing to do
        }

    }

    
    //------------------------------------------------------------------------
    // Class InteractionAdapter
    //------------------------------------------------------------------------
    
    /**
     * Interaction Adapter
     */
    private class InteractionAdapter extends AbstractInteraction<Connection> {

        /**
         * Constructor 
         *
         * @param restInteraction
         */
        InteractionAdapter(
            Interaction restInteraction
        ){
            super(ConnectionAdapter.this);
            this.delegate = restInteraction;
        }
        
        /**
         * The interaction's connection
         */
        private final Interaction delegate;
        
        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        private <T> T execute(
            Class<T> returnType,
            InteractionSpec ispec, 
            Record input,
            Record output
        ) throws ResourceException {
            this.assertOpened();
            try {
                return returnType.cast(
                    Boolean.class == returnType ? Boolean.valueOf(
                        this.delegate.execute(ispec, input, output)
                    ) : this.delegate.execute(ispec, input)
                );
            } catch (RuntimeException exception) {
                throw new EISSystemException(
                    "Runtime exception lead to JCA request abort",
                    exception
                );
            }
        }
        
        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        @Override
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            return execute(
                Record.class,
                ispec,
                input,
                null
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.AbstractInteraction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        @Override
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            return execute(
                Boolean.class,
                ispec,
                input,
                output
            ).booleanValue();
        }

    }


}