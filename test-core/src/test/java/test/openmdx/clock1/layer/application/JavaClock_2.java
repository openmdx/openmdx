/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Java Clock Provider
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2017, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package test.openmdx.clock1.layer.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.cci.VoidRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Java Clock Provider
 */
public class JavaClock_2 extends AbstractRestPort {

    /**
     * Constructor
     */
    public JavaClock_2() {
        super();
    }

    private final static Path A_SEGMENT_PATH = new Path(
        "xri://@openmdx*test.openmdx.clock1/provider/($..)/segment/($..)");

    protected String getSegmentDescription()
        throws ResourceException {
        final InputStream stream = Thread.currentThread(
        ).getContextClassLoader(
        ).getResourceAsStream(
            "test/openmdx/clock1/segment.txt"
        );
        if(stream == null) {
            return "n/a";
        } else try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(stream)
            )
        ){
            return in.readLine();
        } catch (IOException exception) {
            throw ResourceExceptions.toResourceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "Segment description acquisition failure"
                )
            );
        }
    }

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
        extends AbstractRestInteraction
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

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.QueryRecord,
         * org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            if (xri.isLike(A_SEGMENT_PATH)) {
                Object_2Facade segment = Object_2Facade
                    .newInstance(xri, "test:openmdx:clock1:Segment");
                String description = getSegmentDescription();
                segment.getValue().put("description", description);
                output.add(segment.getDelegate());
                return true;
            } else {
                return super.get(ispec, input, output);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.openmdx.base.rest.spi.AbstractRestInteraction#invoke(org.openmdx.
         * base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.cci.MessageRecord,
         * org.openmdx.base.rest.cci.MessageRecord)
         */
        @Override
        protected boolean invoke(
            RestInteractionSpec ispec,
            MessageRecord input,
            MessageRecord output
        ) throws ResourceException {
            final Path xri = input.getTarget();
            final String operationName = xri
                .getLastSegment()
                .toClassicRepresentation();
            final Path accessPath = xri.getParent();
            if (accessPath.isLike(A_SEGMENT_PATH)) {
                if("currentDateAndTime".equals(operationName)) {
                    output.setResourceIdentifier(
                        super.newResponseId(input.getResourceIdentifier()));
                    final MappedRecord body = Records.getRecordFactory().createMappedRecord(
                        "test:openmdx:clock1:Time");
                    body.put("utc", new Date());
                    output.setBody(body);
                    return true;
                } else if("setDateAndTime".equals(operationName)) {
                    // This method does nothing on purpose
                    output.setResourceIdentifier(
                        super.newResponseId(input.getResourceIdentifier()));
                    output.setBody(Records.getRecordFactory().createMappedRecord(VoidRecord.NAME));
                    return true;
                }
            }
            return super.invoke(ispec, input, output);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#update(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean update(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        ) throws ResourceException {
            final Path xir = input.getResourceIdentifier();
            return xir.isLike(A_SEGMENT_PATH) ||
                super.update(ispec, input, output);
        }

    }

}
