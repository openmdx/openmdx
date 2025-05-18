/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: InvolvementPersistence EMBEDDED
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
package org.openmdx.audit2.aopa;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
#endif

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;

/**
 * InvolvementPersistence EMBEDDED
 */
public class InvolvementPersistenceEmbedded extends AbstractRestPort {

    /**
     * The involvement XRI pattern
     */
    private static final Path INVOLVEMENT_PATTERN = new Path(
        "xri://@openmdx*org:openmdx:audit2/provider/($..)/segment/($..)/unitOfWork/($..)/involvement/($..)"
    );
    
    protected boolean isInvolvement(
        RequestRecord request
    ) {
        return request.getResourceIdentifier().isLike(INVOLVEMENT_PATTERN);
    }



    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new InterceptingInteraction(connection, newDelegateInteraction(connection));
    }

    /**
     * Intercepting Interaction
     */
    protected class InterceptingInteraction extends AbstractRestInteraction {

        /**
         * Constructor 
         */
        protected InterceptingInteraction(
            RestConnection connection,
            Interaction delegate
        ) throws ResourceException {
            super(connection,  delegate);
        }

        @SuppressWarnings("unchecked")
        private boolean ignore(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        ){
            if(isOutgoingTrafficEnabled(ispec)) {
                output.add(input);
            }
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean create(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        ) throws ResourceException {
            return isInvolvement(input) ? ignore(
                ispec,
                input,
                output
            ) : super.create(
                ispec, 
                input, 
                output
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord)
         */
        @Override
        protected boolean delete(
            RestInteractionSpec ispec, 
            QueryRecord input
        )
            throws ResourceException {
            return isInvolvement(input) || super.delete(
                ispec, 
                input
            );
        }

    }

}
