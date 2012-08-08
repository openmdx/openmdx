/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractDataprovider_1Bean.java,v 1.2 2009/06/08 17:11:37 hburger Exp $
 * Description: Abstract EJB With Dataprovider 1 Support
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:11:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
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
package org.openmdx.application.dataprovider.transport.ejb.server;

import java.util.Map;

import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
import javax.resource.spi.LocalTransactionException;
import javax.sql.DataSource;
import javax.transaction.Synchronization;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.application.dataprovider.spi.Dataprovider_2Connection;
import org.openmdx.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.application.rest.ejb.AbstractRest_2Bean;
import org.openmdx.application.spi.LateBindingDataSource;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.resource.spi.TransactionManager;
import org.openmdx.kernel.exception.BasicException;

/**
 * Abstract EJB With Dataprovider 1 Support
 */
abstract public class AbstractDataprovider_1Bean 
    extends AbstractRest_2Bean
    implements Dataprovider_1_0, TransactionManager
{
    
    /**
     * Constructor 
     */
    protected AbstractDataprovider_1Bean() {
        super();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -5565242066669850464L;

    /**
     * The dataprovider accessor callback
     *
     * @return the dataprovider accessor
     */
    protected abstract Dataprovider_1_0 getDelegate();
    
    /**
     * Activates the EJB
     * 
     * @param configuration
     */
    protected void activate(
        Configuration configuration
    ) throws Exception {
        //
        // Provide datasources
        //
        Iterable<Map.Entry<Integer,String>> datasourceEntries = envEntries(
            "jdbc", 
            SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY
        );
        SparseList<DataSource> datasources = configuration.values(
            SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY
        );
        for(Map.Entry<Integer, String> e : datasourceEntries) {
            datasources.set(
                e.getKey(),
                new LateBindingDataSource(e.getValue())
            );
         }
        //
        // Provide dataprovider connections
        //
        Iterable<Map.Entry<Integer,String>> dataproviderConnectionEntries = envEntries(
            "ejb", 
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        );
        SparseList<Dataprovider_1_0> dataproviderConnections = configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        );
        InitialContext initialContext = new InitialContext();
        for(Map.Entry<Integer, String> e : dataproviderConnectionEntries) {
            dataproviderConnections.set(
                e.getKey(),
                Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                    initialContext.lookup(e.getValue())
                )
            );
         }
    }    

    //------------------------------------------------------------------------
    // Extends AbstractDataprovider_2Bean
    //------------------------------------------------------------------------
  
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.transport.ejb.spi.AbstractDataprovider_2Bean#activate()
     */
    @Override
    public final void activate(
    ) throws Exception {
        super.activate();
        activate(new Configuration());
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    // -----------------------------------------------------------------------
  
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.cci.Dataprovider_1_0#process(org.openmdx.application.dataprovider.cci.ServiceHeader, org.openmdx.application.dataprovider.cci.UnitOfWorkRequest[])
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest... workingUnits
    ) {
        return getDelegate().process(header, workingUnits);
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_2_0
    // -----------------------------------------------------------------------
  
    /** 
     * Executes an interaction represented by the InteractionSpec.
     * This form of invocation takes an input Record and returns an 
     * output Record if the execution of the Interaction has been
     * successful.
     *  
     * @param   ispec   InteractionSpec representing a target EIS 
     *                  data/function module   
     * @param   input   Input Record
     *
     * @return  output Record if execution of the EIS function has been 
     *          successful; null otherwise
     *
     * @throws  ResourceException   Exception if execute operation
     *                              fails. Examples of error cases
     *                              are:
     *         <UL>
     *           <LI> Resource adapter internal, EIS-specific or 
     *                communication error
     *           <LI> Invalid specification of an InteractionSpec
     *                or input record structure
     *           <LI> Errors in use of input Record or creation
     *                of an output Record
     *           <LI> Invalid connection associated with this
     *                Interaction
     *         </UL>
     * @throws NotSupportedException Operation not supported
     */
    public Record execute(
        InteractionSpec ispec,
        Record input
    ) throws ResourceException {
        return new Dataprovider_2Connection(
            this.getDelegate()
        ).execute(
            ispec,
            input
        );
    }

    //------------------------------------------------------------------------
    // Implements OptimisticTransaction
    // -----------------------------------------------------------------------
    public void commit(
        Synchronization synchronization
    ) throws LocalTransactionException {
        SessionContext sessionContext = super.getSessionContext();
        if(sessionContext.getRollbackOnly()) throw BasicException.initHolder(
            new LocalTransactionException(
                "Unit of work was marked for rollback only",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK
                )
            )
        );  
        try {
            synchronization.beforeCompletion();
        } catch (RuntimeException exception) {
            sessionContext.setRollbackOnly();
            throw BasicException.initHolder(
                new LocalTransactionException(
                    "Unit of work set to rollback-only during commit",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ROLLBACK
                    )
                )
            );
        }
   }
 
}
