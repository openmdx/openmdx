/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Hard-Wired Objects Layer
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.MappedRecord;
#endif

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.kernel.exception.BasicException;
#if CLASSIC_CHRONO_TYPES import org.w3c.format.DateTimeFormat;#endif

/**
 * Hard-wired Objects Layer
 */
public class HardWiredObjects_2 extends AccessControl_2 {

    
    /**
     * Constructor 
     * @throws ResourceException 
     *
     */
    public HardWiredObjects_2(
    ) throws ResourceException{
        //
        // Creation time of the hard-wired objects
        //
        String timestamp = DateTimeFormat.BASIC_UTC_FORMAT.format(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now()#endif);
        //
        // hard-wired NameFormat
        //
        this.nameFormats = new HashMap<>();
        MappedRecord nameFormatStandard = Records.getRecordFactory().createMappedRecord(NAME_FORMAT_TYPE_NAME);
        nameFormatStandard.put("description","default name format");
        nameFormatStandard.put(SystemAttributes.CREATED_AT,timestamp);
        nameFormatStandard.put(SystemAttributes.MODIFIED_AT,timestamp);
        this.nameFormats.put(
            "Standard",
            nameFormatStandard
        );
        //
        // hard-wired AddressFormat
        //
        this.addressFormats = new HashMap<>();
        MappedRecord addressFormatStandard = Records.getRecordFactory().createMappedRecord(ADDRESS_FORMAT_TYPE_NAME);
        addressFormatStandard.put("description","default address format");
        addressFormatStandard.put(SystemAttributes.CREATED_AT,timestamp);
        addressFormatStandard.put(SystemAttributes.MODIFIED_AT,timestamp);
        this.addressFormats.put(
            "Standard",
            addressFormatStandard
        );
    }

    private static final String ADDRESS_FORMAT_TYPE_NAME = "test:openmdx:app1:AddressFormat";
    private static final String NAME_FORMAT_TYPE_NAME = "test:openmdx:app1:NameFormat";
    private static final byte[] HARD_WIRED_OBJECT_VERSION = {'h','a','r','d','-','w','i','r','e','d'};

    private Map<String,MappedRecord> nameFormats;
    private Map<String,MappedRecord> addressFormats;
    final static Logger logger = Logger.getLogger(HardWiredObjects_2.class.getName());
    
    /**
     * Retrieve a copy of a format object with the given identity
     * 
     * @param objectId
     * @param formats
     * 
     * @return the copy
     * 
     * @throws ServiceException
     */
    protected ObjectRecord getFormat(
        Path objectId,
        Map<String,MappedRecord> formats
    ) throws ResourceException {
        try {
            MappedRecord original = formats.get(objectId.getLastSegment().toClassicRepresentation());
            if(original == null) {
                if("CR20020187".equals(objectId.getLastSegment().toClassicRepresentation())) {
                    throw new RuntimeException("CR20020187");
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "Format not found",
                        new BasicException.Parameter(BasicException.Parameter.XRI, objectId)
                    );        
                }
            }
            final ObjectRecord object = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
            object.setResourceIdentifier(objectId);
            object.setValue(original);
            object.setVersion(HARD_WIRED_OBJECT_VERSION);
            return object;
        } catch (ServiceException exception) {
            throw ResourceExceptions.toResourceException(exception);
        }
    }

    /**
     * Retrieve the reference name
     * 
     * @param xri the resource identifier
     * 
     * @return the reference name
     * @throws ServiceException 
     */
    protected String getReferenceName(
        Path xri
    ){
        final int size = xri.size();
        return xri.getSegment(size - 1 - size % 2).toClassicRepresentation();
    }
    
    private int getPosition(QueryRecord request) {
        final Long position = request.getPosition();
        return position == null ? 0 : position.intValue();
    }
    
    protected void getSlice(
        QueryRecord request,
        List<ObjectRecord> values,
        ResultRecord reply
    ){
        final boolean hasMore;
        final int position = getPosition(request);
        if(position >= values.size()) {
            values = Collections.emptyList();
            hasMore = false;
        } else {
            int fromPosition = position;
            int toPosition = fromPosition;
            toPosition += request.size();
            if(toPosition >= values.size()) {
                toPosition = values.size();
                hasMore = false;
            } else {
                hasMore = true;
            }
            if(fromPosition > 0 || hasMore) {
                values = values.subList(fromPosition, toPosition);
            }
        }
        reply.addAll(values);
        reply.setHasMore(Boolean.valueOf(hasMore));
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
        ) throws ResourceException {
        return new RestInteraction(connection, newDelegateInteraction(connection));
    }
    
    /**
     * Intercepting Interaction
     */
    protected class RestInteraction extends AccessControl_2.RestInteraction {

        /**
         * Constructor 
         */
        protected RestInteraction(
            RestConnection connection,
            Interaction delegate
        ) throws ResourceException {
            super(connection,  delegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            logger.log(Level.FINEST,"Get request for {0}", xri);
            String referenceName = getReferenceName(xri);
            if("nameFormat".equals(referenceName)) {
                //
                // hard-wired nameFormat
                //
                output.add(
                    getFormat(
                        xri,
                        HardWiredObjects_2.this.nameFormats
                    )
                );
                return true;
            } else if("addressFormat".equals(referenceName)) {
                //
                // hard-wired addressFormat
                //
                output.add(
                    getFormat(
                        xri,
                        HardWiredObjects_2.this.addressFormats
                    )
                );
                return true;
            } else {
                //
                // non hard-wired objects
                //
                return super.get(ispec, input, output);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean create(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            logger.log(Level.FINEST,"Create request for {0}", xri);
            String referenceName = getReferenceName(xri);
            if("nameFormat".equals(referenceName) || "addressFormat".equals(referenceName)) {
                //
                // hard-wired nameFormat|addressFormat
                //
                throw ResourceExceptions.toResourceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "update not allowed on references with constraint isFrozen",
                        new BasicException.Parameter("reference", referenceName)
                    )
                );
            } else {
                //
                // non hard-wired objects
                //
                return super.create(
                    ispec,
                    input,
                    output
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord)
         */
        @Override
        protected boolean delete(RestInteractionSpec ispec, ObjectRecord input)
            throws ResourceException {
            String referenceName = getReferenceName(input.getResourceIdentifier());
            if("nameFormat".equals(referenceName) || "addressFormat".equals(referenceName)) {
                //
                // hard-wired nameFormat|addressFormat
                //
                throw ResourceExceptions.toResourceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED, 
                        "update not allowed on references with constraint isFrozen",
                        new BasicException.Parameter("reference", referenceName)
                    )
                );
            } else {
                //
                // non hard-wired objects
                //
                return super.delete(
                    ispec,
                    input
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#update(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean update(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output)
            throws ResourceException {
            String referenceName = getReferenceName(input.getResourceIdentifier());
            if("nameFormat".equals(referenceName) || "addressFormat".equals(referenceName)) {
                //
                // hard-wired nameFormat|addressFormat
                //
                throw ResourceExceptions.toResourceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "update not allowed on references with constraint isFrozen",
                        new BasicException.Parameter("reference", referenceName)
                    )
                );
            } else {
                //
                // non hard-wired objects
                //
                return super.update(
                    ispec,
                    input,
                    output
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output)
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            String referenceName = getReferenceName(xri);
            if("nameFormat".equals(referenceName)) {
                //
                // hard-wired NameFormarequestt
                //
                final List<ObjectRecord> formats = new ArrayList<ObjectRecord>();
                for(String id : HardWiredObjects_2.this.nameFormats.keySet()) {
                    formats.add(
                        getFormat(
                            xri.getChild(id),
                            HardWiredObjects_2.this.nameFormats
                        )
                    );
                }
                HardWiredObjects_2.this.getSlice(input, formats, output);
                return true;
            } else if("addressFormat".equals(referenceName)) {
                //
                // hard-wired AddressFormat
                //
                final List<ObjectRecord> formats = new ArrayList<ObjectRecord>();
                for(String id : HardWiredObjects_2.this.addressFormats.keySet()) {
                    formats.add(
                        getFormat(
                            xri.getChild(id),
                            HardWiredObjects_2.this.addressFormats
                        )
                    );
                }
                HardWiredObjects_2.this.getSlice(input, formats, output);
                return true;                    
            } else {
                //
                // non hard-wired objects
                //
                return super.find(
                    ispec,
                    input,
                    output
                );
            }
        }

        
        
    }

}
