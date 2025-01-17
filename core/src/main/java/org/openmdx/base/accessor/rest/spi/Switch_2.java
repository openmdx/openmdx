/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Router
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
package org.openmdx.base.accessor.rest.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Record;
import javax.resource.cci.Interaction;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Record;
import jakarta.resource.cci.Interaction;
#endif

import org.openmdx.base.caching.port.CachingPort;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.kernel.exception.BasicException;

/**
 * REST Switch
 */
public class Switch_2 implements Port<RestConnection> {

    /**
     * Constructor 
     * 
     * @param virtualObjects 
     * @param destinations
     * 
     * @throws ResourceException 
     */
    public Switch_2(
        CachingPort cachingPort, 
        Map<Path,Port<RestConnection>> destinations
    ) throws ResourceException{
        this.cachingPort = cachingPort;
        this.destinations = destinations; 
    }
    
    /**
     * The Switch's destinations
     */
    protected final Map<Path,Port<RestConnection>> destinations;
    
    /**
     * Virtual object port
     */
    protected final CachingPort cachingPort;
    
    
    //------------------------------------------------------------------------
    // Implements Port
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.sp#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new SwitchingInteraction(connection);
    }

    private ConcurrentHashMap<Port<RestConnection>, Interaction> createInteractionRegistry(
        RestConnection connection
    ) throws ResourceException {
        final ConcurrentHashMap<Port<RestConnection>, Interaction> interactionRegistry = new ConcurrentHashMap<Port<RestConnection>,Interaction>();
        interactionRegistry.put(this.cachingPort, this.cachingPort.getInteraction(connection));
        return interactionRegistry;
        
    }

    private boolean isCached(Path xri) {
        return
            !xri.isTransactionalObjectId() &&
            Switch_2.this.cachingPort.containsKey(xri);
    }
    
    
    //------------------------------------------------------------------------
    // Class SwitchingInteraction
    //------------------------------------------------------------------------
    
    /**
     * Switching Interaction
     */
    class SwitchingInteraction extends AbstractRestInteraction {
       
        /**
         * Constructor 
         *
         * @param connection the connection which requested this interaction
         * @throws ResourceException 
         */
        SwitchingInteraction(
            RestConnection connection
        ) throws ResourceException{
            super(connection);
            this.enlisted = createInteractionRegistry(connection);
        }

        /**
         * The enlisted interactions
         */
        private final ConcurrentMap<Port<RestConnection>,Interaction> enlisted;

        /**
         * Retrieve the destination for the given resource identifier
         * 
         * @param xri the resource identifier
         * 
         * @return a destination for the given resource identifier
         * 
         * @throws ResourceException if there is no destination for the given resource identifier
         */
        protected Port<RestConnection> getDestination(
            Path xri
        ) throws ResourceException {
            if(isCached(xri)) {
                return Switch_2.this.cachingPort;
            }
            for(Map.Entry<Path,Port<RestConnection>> entry : Switch_2.this.destinations.entrySet()) {
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
                            BasicException.Parameter.XRI, 
                            xri
                        ),
                        new BasicException.Parameter(
                            "destinations", 
                            Switch_2.this.destinations.keySet())
                    )
                )
            );
        }

        /**
         * Retrieve an interaction for getInteractionthe given resource identifier
         * 
         * @param destination the destination
         * 
         * @return a (maybe newly created) interaction for the given resource identifier
         * 
         * @throws ResourceException if there is no destination for the given resource identifier
         */
        protected Interaction getInteraction(
            Port<RestConnection> destination
        ) throws ResourceException {
            Interaction interaction = this.enlisted.get(destination);
            return interaction == null ? Maps.putUnlessPresent(
                this.enlisted,
                destination,
                destination.getInteraction(super.getConnection())
            ) : interaction;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#pass(org.openmdx.base.naming.Path, org.openmdx.base.resource.spi.RestInteractionSpec, javax.resource.cci.MappedRecord, javax.resource.cci.IndexedRecord)
         */
        @Override
        protected boolean pass(
            RestInteractionSpec ispec,
            RequestRecord input,
            Record output
        ) throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            final Port<RestConnection> destination = getDestination(xri);
            final Interaction interaction = getInteraction(destination);
            final boolean executed = interaction.execute(ispec, input, output);
            if(
                executed && 
                destination != cachingPort && 
                ispec.getFunction() == RestFunction.GET && 
                output instanceof ResultRecord
                // TODO && Fetch Group is ALL
            ){
                for(Object object : (ResultRecord)output) {
                    cachingPort.offer((ObjectRecord)object);
                }
            }
            return executed;
        }
        
    }

}
