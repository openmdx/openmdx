/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Remote REST Connection Port
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
package org.openmdx.application.rest.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.HomeHandle;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.spi.CommException;
import javax.resource.spi.ResourceAllocationException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.kernel.exception.BasicException;

/**
 * Remote REST Connection Port
 */
class Connection_2RemotePort implements Port<RestConnection> {

    /**
     * Constructor 
     * 
     * @param home
     * @param homeHandle
     */
    private Connection_2RemotePort(
        Connection_2Home home, 
        HomeHandle homeHandle
    ){
       this.home = home;
       this.homeHandle = homeHandle;
    }

    /**
     * The EJB Home
     */
    private transient Connection_2Home home;

    /**
     * The EJB Home handle
     */
    private final HomeHandle homeHandle;
    
    /**
     * Factory method
     * 
     * @param home
     * 
     * @throws ResourceException 
     * @throws ServiceException 
     */
    static Port<RestConnection> newInstance(
        Object delegate
    ) throws ServiceException{  
        //
        // Retrieve EJBHome
        //
        Connection_2Home home;
        try {
            home = (Connection_2Home) delegate;
        } catch (ClassCastException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Could not acquire the EJB's home"
            );
        }
        //
        // Retrieve HomeHandle
        //
        HomeHandle homeHandle;
        try {
            homeHandle = home.getHomeHandle();
        } catch (RemoteException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Could not acquire the EJB's home handle"
            );
        }
        //
        // Return RestPlugIn
        //
        return new Connection_2RemotePort(
            home,
            homeHandle
        );
    }

    /**
     * Retrieve the EJB home
     * 
     * @param retrieveFromHandle retrieve <code>EJBHome</code> from <code>HomeHandle</code>
     * 
     * @return the EJB Home
     * 
     * @throws ResourceException
     */
    private Connection_2Home getHome(
        boolean retrieveFromHandle
    ) throws ResourceException{
        if(retrieveFromHandle) {
            try {
                return this.home = (Connection_2Home) this.homeHandle.getEJBHome();
            } catch (RemoteException exception) {
                throw new ResourceException(
                    "Could not re-acquire EJB Home object",
                    exception
                );
            }
        } else {
            return this.home;
        }
    }
    
    private Connection_2_0Remote getConnection(
        ConnectionSpec properties
    ) throws ResourceException, RemoteException, CreateException {
        boolean retriable = this.home != null;
        try {
            return getHome(this.home == null).create(properties);
        } catch (ResourceException exception) {
            if(retriable) {
                return getHome(true).create(properties);
            } else {
                throw exception;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.PlugIn#getInteraction(javax.resource.cci.Connection)
     */
    public Interaction getInteraction(
    	RestConnection connection
    ) throws ResourceException {
        try {
            return new InteractionAdapter(
                getConnection(((ConnectionAdapter)connection).getMetaData().getConnectionSpec()),
                connection
            );
        } catch (CreateException exception) {
            throw ResourceExceptions.initHolder(
                new ResourceAllocationException(
                    "Remote EJB can't be created",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        } catch (RemoteException exception) {
            throw ResourceExceptions.initHolder(
                new CommException(
                    "Remote EJB connection can't be established",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        }
    }
    
    
    //------------------------------------------------------------------------
    // Class InteractionAdapter
    //------------------------------------------------------------------------

    /**
     * Interaction Adapter
     */
    protected static class InteractionAdapter extends AbstractInteraction<Connection> {
        
        /**
         * Constructor 
         *
         * @param connection
         */
        protected InteractionAdapter(
            Connection_2_0Remote delegate,
            Connection connection
        ) {
            super(connection);
            this.delegate = delegate;
        }

        /**
         * The delegate
         */
        private final Connection_2_0Remote delegate;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.Port#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        @Override
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            try{
                return this.delegate.execute(ispec, input);
            } catch (RemoteException exception) {
                throw ResourceExceptions.initHolder(
                    new CommException(
                        "Remote EJB Invocation Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            try{
                Record out = this.delegate.execute(ispec, input);
                if(out == null || output == null) {
                    return true;
                }
                if(out.getRecordName().equals(output.getRecordName())) {
                    if(out instanceof IndexedRecord && output instanceof IndexedRecord) {
                        if(out instanceof ResultRecord && output instanceof ResultRecord) {
                            ResultRecord target = (ResultRecord) output;
                            ResultRecord source = (ResultRecord) out;
                            target.addAll(source);
                            Boolean hasMore = source.getHasMore();
                            if(hasMore != null) {
                                target.setHasMore(hasMore.booleanValue());
                            }
                            Long total = source.getTotal();
                            if(total != null) {
                                target.setTotal(total.longValue());
                            }
                        } else {
                            ((IndexedRecord)output).addAll((IndexedRecord)out);
                        }
                        return true;
                    }
                    if (out instanceof MappedRecord && output instanceof MappedRecord) {
                        ((MappedRecord)output).putAll((MappedRecord)out);
                        return true;
                    }
                }
                throw ResourceExceptions.initHolder(
                    new CommException(
                        "Unmarshal failure",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            new BasicException.Parameter("source", out.getClass().getName(), out.getRecordName()),
                            new BasicException.Parameter("target", output.getClass().getName(), output.getRecordName())
                        )
                    )
                );
            } catch (RemoteException exception) {
                throw ResourceExceptions.initHolder(
                    new CommException(
                        "Remote EJB Invocation Failure", 
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }
                
    }

}
