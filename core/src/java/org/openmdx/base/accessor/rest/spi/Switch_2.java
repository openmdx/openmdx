/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Switch_2.java,v 1.12 2010/03/23 09:09:37 hburger Exp $
 * Description: REST Router
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/23 09:09:37 $
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
package org.openmdx.base.accessor.rest.spi;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.accessor.rest.spi.VirtualObjects_2.VirtualObjectProvider;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.kernel.exception.BasicException;

/**
 * REST Switch
 */
public class Switch_2 implements Port {

    /**
     * Constructor 
     * 
     * @param virtualObjects 
     * @param destinations
     * 
     * @throws ResourceException 
     */
    public Switch_2(
        VirtualObjects_2 virtualObjects, 
        Map<Path,Port> destinations
    ) throws ResourceException{
        this.virtualObjectPlugIn = virtualObjects;
        this.destinations = destinations; 
    }
    
    /**
     * The Switch's destinations
     */
    protected final Map<Path,Port> destinations;
    
    /**
     * Virtual object port
     */
    protected final VirtualObjects_2 virtualObjectPlugIn;
    
    
    //------------------------------------------------------------------------
    // Implements Port
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.sp#getInteraction(javax.resource.cci.Connection)
     */
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new SwitchingInteraction(connection);
    }

    
    //------------------------------------------------------------------------
    // Class SwitchingInteraction
    //------------------------------------------------------------------------
    
    /**
     * Switching Interaction
     */
    class SwitchingInteraction extends AbstractRestInteraction implements VirtualObjects_2_0 {
       
        /**
         * Constructor 
         *
         * @param connection the connection which requested this interaction
         * @throws ResourceException 
         */
        SwitchingInteraction(
            Connection connection
        ) throws ResourceException{
            super(connection);
            this.enlisted.put(
                Switch_2.this.virtualObjectPlugIn, 
                this.virtualObjectProvider  = Switch_2.this.virtualObjectPlugIn.getInteraction(connection)
            );
        }
        
        /**
         * The virtual object provider instance
         */
        private final VirtualObjectProvider virtualObjectProvider;
        
        /**
         * The enlisted interactions
         */
        private final ConcurrentMap<Port,Interaction> enlisted = new ConcurrentHashMap<Port,Interaction>();

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.VirtualObjects_2_0#sowSeed(org.openmdx.base.naming.Path, java.lang.String)
         */
        @Override
        public void putSeed(
            Path xri, 
            String objectClass
        ) throws ResourceException {
            this.virtualObjectProvider.putSeed(xri, objectClass);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.VirtualObjects_2_0#isVirtual(org.openmdx.base.naming.Path)
         */
        @Override
        public boolean isVirtual(
            Path xri
        ) {
            return this.virtualObjectProvider.isVirtual(xri);
        }

        /**
         * Retrieve the destination for the given resource identifier
         * 
         * @param xri the resource identifier
         * 
         * @return a destination for the given resource identifier
         * 
         * @throws ResourceException if there is no destination for the given resource identifier
         */
        protected Port getDestination(
            Path xri
        ) throws ResourceException {
            if(this.virtualObjectProvider.isVirtual(xri)) {
                return Switch_2.this.virtualObjectPlugIn;
            }
            for(Map.Entry<Path,Port> entry : Switch_2.this.destinations.entrySet()) {
                if(xri.isLike(entry.getKey())) {
                    return entry.getValue();
                }
            } 
            throw BasicException.initHolder(
                new ResourceException(
                    "No destination found for the given resource identifier",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter(
                            "xri", 
                            xri
                        ),
                        new BasicException.Parameter(
                            "final-objects", 
                            Switch_2.this.virtualObjectPlugIn.getFinalPattern() == null ? null : Arrays.asList(Switch_2.this.virtualObjectPlugIn.getFinalPattern())
                        ),
                        new BasicException.Parameter(
                            "destinations", 
                            destinations.keySet())
                    )
                )
            );
        }
        
        /**
         * Retrieve an interaction for the given resource identifier
         * 
         * @param xri the resource identifier
         * 
         * @return a (maybe newly created) interaction for the given resource identifier
         * 
         * @throws ResourceException if there is no destination for the given resource identifier
         */
        protected Interaction getInteraction(
            Path xri
        ) throws ResourceException {
            Port destination = getDestination(xri);
            Interaction interaction = this.enlisted.get(destination);
            if(interaction == null) {
                Interaction enlisted = this.enlisted.putIfAbsent(
                    destination,
                    interaction = destination.getInteraction(super.getConnection())
                );
                return enlisted == null ? interaction : enlisted;
            } else {
                return interaction;
            }
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#pass(org.openmdx.base.naming.Path, org.openmdx.base.resource.spi.RestInteractionSpec, javax.resource.cci.MappedRecord, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean pass(
            Path xri,
            RestInteractionSpec ispec,
            MappedRecord input,
            Record output
        ) throws ServiceException {
            try {
                return getInteraction(xri).execute(ispec, input, output);
            } catch(ResourceException e) {
                throw new ServiceException(e);
            }
        }
        
    }

}
