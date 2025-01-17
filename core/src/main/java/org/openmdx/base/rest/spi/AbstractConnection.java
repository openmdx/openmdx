/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Connection
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

import org.openmdx.base.Version;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionMetaData;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.exception.BasicException;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ResultSetInfo;
import javax.resource.spi.IllegalStateException;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ResultSetInfo;
import jakarta.resource.spi.IllegalStateException;
#endif

/**
 * Abstract Connection
 */
public abstract class AbstractConnection implements RestConnection {

    /**
     * Constructor
     * 
     * @param connectionFactory
     *            the connection's factory
     * @param connectionSpec
     *            the connection specification
     */
    protected AbstractConnection(
        final ConnectionFactory connectionFactory,
        final RestConnectionSpec connectionSpec) {
        this.connectionFactory = connectionFactory;
        this.metaData = new MetaData(connectionSpec);
        this.closed = false;
    }

    /**
     * The JCA connection factory
     */
    private final ConnectionFactory connectionFactory;

    /**
     * The REST connection meta data
     */
    private final MetaData metaData;

    /**
     * Tells whether the
     */
    private boolean closed;

    // ------------------------------------------------------------------------
    // Implements Connection
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getMetaData()
     */
    @Override
    public final RestConnectionMetaData getMetaData() {
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.cci.RestConnection#getConnectionFactory()
     */
    @Override
    public org.openmdx.base.resource.cci.ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getResultSetInfo()
     */
    @Override
    public final ResultSetInfo getResultSetInfo() throws ResourceException {
        throw ResourceExceptions.initHolder(
            new NotSupportedException(
                "Result sets are not supported by REST connections", BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.NOT_SUPPORTED)));
            }

    /**
     * Test whether the connection is open
     * 
     * @exception IllegalStateException
     *                if the connection is closed
     */
    protected void assertOpen() throws ResourceException {
        if (this.closed) {
            throw ResourceExceptions.initHolder(
                new IllegalStateException(
                    "This REST connection is closed", BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ILLEGAL_STATE)));
                }
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#close()
     */
    @Override
    public void close() throws ResourceException {
        this.assertOpen();
        this.closed = true;
    }

    // ------------------------------------------------------------------------
    // Class MetaData
    // ------------------------------------------------------------------------

    /**
     * REST Connection Meta Data
     */
    static class MetaData implements RestConnectionMetaData {

        /**
         * Constructor
         *
         * @param connectionSpec
         */
        MetaData(
            RestConnectionSpec connectionSpec) {
            this.connectionSpec = connectionSpec;
        }

        /**
         * The connection's spec
         */
        final RestConnectionSpec connectionSpec;

        /**
         * It's an openMDX connection
         */
        @Override
        public String getEISProductName() throws ResourceException {
            return "openMDX/REST";
        }

        /**
         * with the given openMDX version
         */
        @Override
        public String getEISProductVersion() throws ResourceException {
            return Version.getSpecificationVersion();
        }

        /**
         * Use the stringified principal chain
         */
        @Override
        public String getUserName() throws ResourceException {
            return this.connectionSpec == null ? null : this.connectionSpec.getUserName();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.cci.RestConnectionMetaData#isBulkLoad()
         */
        @Override
        public boolean isBulkLoad() {
            return this.connectionSpec != null && this.connectionSpec.isBulkLoad();
        }

        @Override
        public RestConnectionSpec getConnectionSpec() {
            return this.connectionSpec;
        }

    }

}
