/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InvolvementPersistenceEmbedded.java,v 1.1 2010/10/19 21:58:57 hburger Exp $
 * Description: InvolvementPersistence EMBEDDED
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/10/19 21:58:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.audit2.aopa;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;

import org.openmdx.application.dataprovider.layer.application.Standard_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.Object_2Facade;

/**
 * InvolvementPersistence EMBEDDED
 */
public class InvolvementPersistenceEmbedded extends Standard_1 {

    /**
     * The involvement XRI pattern
     */
    protected static Path INVOLVEMENT_PATTERN = new Path(
        "xri://@openmdx*org:openmdx:audit2/provider/($..)/segment/($..)/unitOfWork/($..)/involvement/($..)"
    );
    
    /**
     * Interaction factory
     */
    @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
 
    
    //------------------------------------------------------------------------
    // Class LayerInteraction
    //------------------------------------------------------------------------

    /**
     * This interaction ignores Involvement modification requests
     */
    public class LayerInteraction extends StandardLayerInteraction {

        /**
         * Constructor 
         *
         * @param connection
         * @throws ResourceException
         */
        public LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }

        /**
         * Do nothing other than providing the proper reply
         * 
         * @param input
         * @param output
         * 
         * @return <code>true</code>
         */
        @SuppressWarnings("unchecked")
        private boolean ignore(
            Object_2Facade input,
            IndexedRecord output
        ){
            if(output != null) {
                output.add(input.getDelegate());
            }
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            return input.getPath().isLike(INVOLVEMENT_PATTERN) ? ignore(
                input,
                output
            ) : super.create(
                ispec, 
                input, 
                output
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            return input.getPath().isLike(INVOLVEMENT_PATTERN) ? ignore(
                input,
                output
            ) : super.delete(
                ispec, 
                input, 
                output
            );
        }

    }
    
}
