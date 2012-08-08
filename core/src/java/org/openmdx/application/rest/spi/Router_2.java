/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Router_2.java,v 1.5 2009/06/01 15:42:15 wfro Exp $
 * Description: REST Router
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:42:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.rest.spi;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.Version;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.rest.spi.RestConnection;
import org.openmdx.base.rest.spi.RestPlugIn;
import org.openmdx.kernel.exception.BasicException;

/**
 * REST Router
 */
public class Router_2
    implements RestConnection 
{

    /**
     * Constructor 
     *
     * @param userName
     * @param destinations
     * @throws ResourceException 
     */
    private Router_2(
        final String userName,
        Map<Path,ConnectionFactory> destinations
    ) throws ResourceException{
        //
        // Meta Data
        //
        this.metaData = new ConnectionMetaData(){

            /**
             * It's an openMDX REST connection
             */
            public String getEISProductName(
            ) throws ResourceException {
                return "openMDX/REST";
            }

            /**
             * with the given openMDX version
             */
            public String getEISProductVersion(
            ) throws ResourceException {
                return Version.getSpecificationVersion();
            }

            /**
             * Propagate the factory's security information
             */
            public String getUserName(
            ) throws ResourceException {
                return userName;
            }
            
        };
        //
        // Destinations
        //
        this.destinations = new TreeMap<Path,Connection>();        
        Connection sameConnection = null;
        for(Map.Entry<Path, ConnectionFactory> entry : destinations.entrySet()){
            Connection connection = entry.getValue().getConnection();
            if(connection instanceof ConnectionAdapter) {
                RestConnection restConnection = ((ConnectionAdapter)connection).getDelegate();
                if(restConnection instanceof RestPlugIn) {
                    ((RestPlugIn)restConnection).setSame(
                        sameConnection == null ? sameConnection = ConnectionAdapter.newInstance(this) : sameConnection
                    );
                }
            }
            this.destinations.put(
                entry.getKey(),
                connection
            );
        }
    }
    
    /**
     * Create a REST router instance
     * 
     * @param userName
     * @param destinations
     * @throws ResourceException 
     * @throws ResourceException 
     */
    public static Connection newInstance(
        String userName,
        Map<Path,ConnectionFactory> destinations
    ) throws ResourceException { 
        return ConnectionAdapter.newInstance(
            new Router_2(userName, destinations)
        );
    }
    
    /**
     * The Switch's meta-data
     */
    private final ConnectionMetaData metaData;
    
    /**
     * The Switch's destinations
     */
    private final Map<Path,Connection> destinations;

    /**
     * Retrieve a connection for the given resource identifier
     * 
     * @param xri the resource identifier
     * 
     * @return a connection for the given resource identifier
     * 
     * @throws ResourceException
     */
    protected Connection getConnection(
        Path xri
    ) throws ResourceException {
        for(Map.Entry<Path, Connection> entry : this.destinations.entrySet()) {
            if(xri.isLike(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw BasicException.initHolder(
            new ResourceException(
                "No destination found for the given resource identifier",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("xri", xri)
                )
            )
        );
    }
    
    /**
     * Route a request to its destination
     * 
     * @param interactionSpec
     * @param xri
     * @param input
     * 
     * @return the reply
     * @throws ResourceException
     */
    protected Record execute(
        InteractionSpec interactionSpec,
        Path xri,
        Record input
    ) throws ResourceException{
        Interaction interaction = getConnection(xri).createInteraction();
        try {
            return interaction.execute(interactionSpec, input);
        } finally {
            interaction.close();
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    @SuppressWarnings("unchecked")
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        if(ObjectHolder_2Facade.isDelegate(input)) {
            return execute(
                ispec,
                ObjectHolder_2Facade.newInstance((MappedRecord)input).getPath(),
                input
            );
        } else if (Query_2Facade.isDelegate(input)) {
            return execute(
                ispec,
                Query_2Facade.newInstance((MappedRecord)input).getPath(),
                input
            );
        } else if (input instanceof IndexedRecord) {
            IndexedRecord reply = Records.getRecordFactory().createIndexedRecord(Multiplicities.LIST);
            for(Object xri : (IndexedRecord)input) {
                reply.addAll(
                    (IndexedRecord)execute(
                        ispec, 
                        new Path(xri.toString()), 
                        Records.getRecordFactory().singletonIndexedRecord(
                            Multiplicities.LIST, 
                            null, // recordShortDescription
                            xri
                        )
                    )
                );
            }
            return reply;
        } else if (input instanceof MappedRecord) {
            IndexedRecord reply = Records.getRecordFactory().createIndexedRecord(Multiplicities.LIST);
            for(Object e : ((MappedRecord)input).entrySet()) {
                Map.Entry<?, ?> entry = (Entry<?, ?>) e;
                reply.add(
                    execute(ispec, new Path(entry.getKey().toString()), input)
                );
            }
            return reply;
        } else throw new ResourceException(
            "Execute with output record not yet implemented",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter("expected", MappedRecord.class.getName(), IndexedRecord.class.getName()),
                new BasicException.Parameter("actual", input == null ? null : input.getClass().getName())
            )
        ); 
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        throw new ResourceException(
            "Execute with output record not yet implemented",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED
            )
        ); // TODO
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#getMetaData()
     */
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }
    
}
