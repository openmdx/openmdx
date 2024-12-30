/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: VirtualObjects_2
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
package test.openmdx.app1.layer.application;

import java.util.logging.Level;
import java.util.logging.Logger;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
#endif

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Hard-wired Objects Layer
 * <p>
 * This layer implementation shall be replaced by an aspect oriented persistence
 * plug-in in future.
 */
public class VirtualObjects_2
    extends HardWiredObjects_2
{

    /**
     * Constructor
     *
     * @throws ResourceException
     */
    public VirtualObjects_2()
        throws ResourceException {
        super();
    }

    private static final Path PRODUCT_GROUP_PATTERN = new Path(new String[] {
        "test:openmdx:app1", "provider", ":*", "segment", ":*", "productGroup",
        ":*"
    });

    private static final Path PRODUCT_PATTERN = PRODUCT_GROUP_PATTERN
        .getDescendant(new String[] {
            "product", ":*"
    });

    final static Logger logger = Logger
        .getLogger(VirtualObjects_2.class.getName());

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.
     * Connection)
     */
    @Override
    public Interaction getInteraction(RestConnection connection)
        throws ResourceException {
        return new RestInteraction(
            connection,
            newDelegateInteraction(connection));
    }

    /**
     * Intercepting Interaction
     */
    protected class RestInteraction
        extends HardWiredObjects_2.RestInteraction
    {

        /**
         * Constructor
         */
        protected RestInteraction(
            RestConnection connection,
            Interaction delegate)
            throws ResourceException {
            super(connection, delegate);
        }

        @Override
        protected boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            VirtualObjects_2.logger
                .log(Level.FINEST, "Get request for {0}", xri);
            if (xri.isLike(PRODUCT_GROUP_PATTERN)) {
                //
                // virtual ProductGroup instances
                //
                output.add(
                    Object_2Facade
                        .newInstance(xri, "test:openmdx:app1:ProductGroup")
                        .getDelegate());
                return true;
            } else if (xri.isLike(PRODUCT_PATTERN)) {
                //
                // virtual Product instances
                //
                output.add(
                    Object_2Facade
                        .newInstance(xri, "test:openmdx:app1:Product")
                        .getDelegate());
                return true;
            } else {
                //
                // non-virtual objects
                //
                return super.get(ispec, input, output);
            }

        }

        @Override
        protected boolean create(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            VirtualObjects_2.logger
                .log(Level.FINEST, "Create request for {0}", xri);
            if (xri.isLike(PRODUCT_GROUP_PATTERN)
                || xri.isLike(PRODUCT_PATTERN)) {
                //
                // virtual Product|ProductGroup
                //
                throw ResourceExceptions.toResourceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Virtual objects can't be created",
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri))

                );
            } else {
                //
                // non-virtual objects
                //
                return super.create(ispec, input, output);
            }
        }

        @Override
        protected boolean delete(RestInteractionSpec ispec, ObjectRecord input)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            VirtualObjects_2.logger
                .log(Level.FINEST, "Remove request for {0}", xri);
            if (xri.isLike(PRODUCT_GROUP_PATTERN)
                || xri.isLike(PRODUCT_PATTERN)) {
                //
                // virtual Product|ProductGroup
                //
                throw ResourceExceptions.toResourceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Virtual objects can't be removed",
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri)));
            } else {
                //
                // non-virtual objects
                //
                return super.delete(ispec, input);
            }
        }

        @Override
        protected boolean update(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            VirtualObjects_2.logger
                .log(Level.FINEST, "Replace request for {0}", xri);
            if (xri.isLike(PRODUCT_GROUP_PATTERN)
                || xri.isLike(PRODUCT_PATTERN)) {
                //
                // virtual Product|ProductGroup
                //
                throw ResourceExceptions.toResourceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Virtual objects can't be replaced",
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri)));
            } else {
                //
                // non-virtual objects
                //
                return super.update(ispec, input, output);
            }
        }

        @Override
        protected boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            logger.log(Level.FINEST, "Find request for {0}", xri);
            if (xri.isLike(PRODUCT_GROUP_PATTERN)
                || xri.isLike(PRODUCT_PATTERN)) {
                //
                // virtual Product|ProductGroup
                //
                output.setHasMore(Boolean.FALSE);
                return true;
            } else {
                //
                // non hard-wired objects
                //
                return super.find(ispec, input, output);
            }
        }

    }

}
