/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConnectionAdapter.java,v 1.38 2010/12/22 00:14:28 hburger Exp $
 * Description: REST Connection Adapter
 * Revision:    $Revision: 1.38 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/22 00:14:28 $
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

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.LocalTransactionException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openmdx.base.accessor.rest.spi.CacheAccessor_2_0;
import org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0;
import org.openmdx.base.accessor.rest.spi.ManagedConnectionCache_2_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.LocalTransactions;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.UserTransactions;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * Wraps a REST Port into a JCA Connection
 */
public class ConnectionAdapter 
    extends AbstractConnection 
    implements CacheAccessor_2_0
{

	/**
	 * Constructor 
	 *
	 * @param metaData
	 * @param connectionSpec
	 * @param transactionAttributeType 
	 * @param delegate
	 * 
	 * @throws ResourceException
	 */
    private ConnectionAdapter(
    	ResourceAdapterMetaData metaData,
        RestConnectionSpec connectionSpec,
        TransactionAttributeType transactionAttribute, 
        Port delegate
    ) throws ResourceException{
        super(connectionSpec);
        this.restPlugIn = delegate;
        switch(this.transactionAttribute = transactionAttribute) {
            case REQUIRES_NEW:
                if(metaData != null && metaData.supportsLocalTransactionDemarcation()) {
                    this.transactionManager = null;
                    this.transactionProxy = LocalTransactions.getLocalTransaction(
                        this.getLocalTransaction()
                    );
                } else {
                    this.transactionManager = ConnectionAdapter.getTransactionManager(); 
                    this.transactionProxy = this.transactionManager == null ? LocalTransactions.getLocalTransaction(
                        UserTransactions.getUserTransaction()
                    ) : LocalTransactions.getLocalTransaction(
                        this.transactionManager
                    );
                }
                break;
            default:
                this.transactionManager = null;
                this.transactionProxy = null;
        }
    }

    /**
     * The REST interaction
     */
    protected Interaction interaction;
    
    /**
     * The REST plug-in
     */
    private Port restPlugIn;

    /**
     * 
     */
    private final TransactionManager transactionManager;

    /**
     * The local transaction
     */
    protected final javax.resource.spi.LocalTransaction transactionProxy;

    /**
     * The transaction attribute
     */
    protected final TransactionAttributeType transactionAttribute;
    
    /**
     * The local transaction instance
     */
    protected javax.resource.cci.LocalTransaction localTransaction;
    
    /**
     * Virtual Transaction Object Id Reference 
     */
    protected static final Path TRANSACTION_OBJECTS = new Path(
        "xri://@openmdx*org.openmdx.kernel/transaction"
    );
    
    /**
     * Acquire the <code>TransactionManager</code>
     * 
     * @return the <code>TransactionManager</code>
     */
    private static TransactionManager getTransactionManager(
    ){
        try {
            return ComponentEnvironment.lookup(TransactionManager.class);
        } catch (BasicException exception) {
            SysLog.error(
                "REQUIRES_NEW requires a TransactionManager which could not be acquired",
                exception
            );
            return null;
        }
    };
    
    /**
     * Suspend the current transaction and return it
     * 
     * @return the suspended transaction
     * 
     * @throws LocalTransactionException 
     */
    protected Transaction suspendCurrentTransaction(
    ) throws LocalTransactionException{
    	if(this.transactionManager == null) {
            return null;
    	} else try {
            return this.transactionManager.suspend();
        } catch(Exception exception) {
            throw new LocalTransactionException(
                "Transaction could not be suspended",
                exception
            );
        }
    }
    
    /**
     * Resume the suspended transaction
     * 
     * @param transaction
     * 
     * @throws ResourceException
     */
    protected void resumeSuspendedTransaction(
        Transaction transaction
    ) throws ResourceException {
        if(transaction != null) try {
            this.transactionManager.resume(transaction);
        } catch(Exception exception) {
            throw new LocalTransactionException(
                "Transaction could not be resumed",
                exception
            );
        }
    }
    
    /**
     * Create a connection using the given port
     * 
     * @param connectionFactory 
     * @param connectionSpec 
     * @param transactionAttribute
     * @param delegate the REST plug-in 
     * 
     * @return the corresponding JCA connection
     * 
     * @throws ResourceException  
     */
    public static Connection newInstance(
        ConnectionFactory connectionFactory, 
        ConnectionSpec connectionSpec, 
        TransactionAttributeType transactionAttribute,
        Port delegate
    ) throws ResourceException{
        return new ConnectionAdapter(
        	connectionFactory == null ? null : connectionFactory.getMetaData(),
            (RestConnectionSpec) connectionSpec,
            transactionAttribute,
            delegate
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#close()
     */
    @Override
    public void close(
    ) throws ResourceException {
        super.close();
        this.restPlugIn = null;
    }
    
    /**
     * Retrieve the delegate interaction, which may be shared by 
     * different interaction adapters.
     * 
     * @return the delegate interaction
     * 
     * @throws ResourceException
     */
    private Interaction getDelegate(
    ) throws ResourceException {
        if(this.interaction == null) {
            this.interaction = this.restPlugIn.getInteraction(this);
        }
        return this.interaction;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    public Interaction createInteraction(
    ) throws ResourceException {
        this.assertOpen();
        return new InteractionAdapter(this.getDelegate());
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    public javax.resource.cci.LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        if(this.localTransaction == null) {
            this.localTransaction = new TransactionAdapter();
        }
        return this.localTransaction;
    }

    
    //------------------------------------------------------------------------
    // Implements CacheProvider_2_0
    //------------------------------------------------------------------------

    private CacheAccessor_2_0 getCacheAccessor(
    ) throws ServiceException {
        try {
            Interaction delegate = this.getDelegate();
            if(delegate instanceof CacheAccessor_2_0) {
                return (CacheAccessor_2_0)delegate;
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "The delegate interaction is not a cache accessor",
                new BasicException.Parameter("expected", CacheAccessor_2_0.class.getName()),
                new BasicException.Parameter("actual", delegate.getClass().getName())
            );
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.CacheAccessor_2_0#getDataStoreCache()
     */
//  @Override
    public DataStoreCache_2_0 getDataStoreCache(
    ) throws ServiceException {
        return this.getCacheAccessor().getDataStoreCache();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.CacheProvider_2_0#getCache()
     */
//  @Override
    public ManagedConnectionCache_2_0 getManagedConnectionCache(
    ) throws ServiceException {
        return this.getCacheAccessor().getManagedConnectionCache();
    }

    
    //------------------------------------------------------------------------
    // Class TransactionAdapter
    //------------------------------------------------------------------------
    
    /**
     * Transaction Adapter
     */
    class TransactionAdapter implements javax.resource.cci.LocalTransaction {

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
                MessageRecord input = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
                input.setPath(Object_2Facade.getPath(this.currentTransaction).getChild("commit"));
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
    // Class InteractionAdapter
    //------------------------------------------------------------------------
    
    /**
     * Interaction Adapter
     */
    class InteractionAdapter extends AbstractInteraction<Connection> {

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
        @Override
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            this.assertOpened();
            switch(ConnectionAdapter.this.transactionAttribute){
                case REQUIRES_NEW:
                    Transaction suspendedTransaction = ConnectionAdapter.this.suspendCurrentTransaction();
                    try {
                        RuntimeException error = null;
                        ResourceException failure = null;
                        Record reply = null;
                        ConnectionAdapter.this.transactionProxy.begin();
                        try {
                            reply = this.delegate.execute(ispec, input);
                        } catch (ResourceException exception){
                            failure = exception;
                        } catch (RuntimeException exception) {
                            error = exception;
                        }
                        try {
                            if(error == null) {
                                ConnectionAdapter.this.transactionProxy.commit();
                            } else {
                                ConnectionAdapter.this.transactionProxy.rollback();
                            }
                        } catch (Exception exception) {
                            Throwables.log(exception);
                        }
                        if(failure == null) {
                            return reply;
                        } else {
                            throw failure;
                        }
                    } finally {
                        resumeSuspendedTransaction(suspendedTransaction);
                    }
                default:
                    return this.delegate.execute(ispec, input);
            }
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
            this.assertOpened();
            switch(ConnectionAdapter.this.transactionAttribute){
                case REQUIRES_NEW:
                    Transaction suspendedTransaction = ConnectionAdapter.this.suspendCurrentTransaction();
                    try {
                        RuntimeException error = null;
                        ResourceException failure = null;
                        Boolean reply = null;
                        ConnectionAdapter.this.transactionProxy.begin();
                        try {
                            reply = Boolean.valueOf(this.delegate.execute(ispec, input, output));
                        } catch (ResourceException exception){
                            failure = exception;
                        } catch (RuntimeException exception) {
                            error = exception;
                        }
                        try {
                            if(error == null) {
                                ConnectionAdapter.this.transactionProxy.commit();
                            } else {
                                ConnectionAdapter.this.transactionProxy.rollback();
                            }
                        } catch (Exception exception) {
                            Throwables.log(exception);
                        }
                        if(failure == null) {
                            return reply.booleanValue();
                        } else {
                            throw failure;
                        }
                    } finally {
                        ConnectionAdapter.this.resumeSuspendedTransaction(suspendedTransaction);
                    }
                default:
                    return this.delegate.execute(ispec, input, output);
            }
        }

    }

}