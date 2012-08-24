/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Hard-Wired Objects Layer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package test.openmdx.app1.layer.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.layer.application.Standard_1;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Hard-wired Objects Layer
 * <p>
 * This layer implementation shall be replaced by an aspect oriented
 * persistence plug-in in future.
 */
@SuppressWarnings("unchecked")
public class HardWiredObjects_1 extends Standard_1 {

    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
                        
    // --------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        try {
            //
            // Creation time of the hard-wired objects
            //
            String timestamp = org.w3c.format.DateTimeFormat.BASIC_UTC_FORMAT.format(new Date());
            //
            // hard-wired NameFormat
            //
            this.nameFormats = new HashMap<String,MappedRecord>();
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
            this.addressFormats = new HashMap<String,MappedRecord>();
            MappedRecord addressFormatStandard = Records.getRecordFactory().createMappedRecord(ADDRESS_FORMAT_TYPE_NAME);
            addressFormatStandard.put("description","default address format");
            addressFormatStandard.put(SystemAttributes.CREATED_AT,timestamp);
            addressFormatStandard.put(SystemAttributes.MODIFIED_AT,timestamp);
            this.addressFormats.put(
                "Standard",
                addressFormatStandard
            );
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }     

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
    private MappedRecord getFormat(
        Path objectId,
        Map<String,MappedRecord> formats
    ) throws ServiceException {
        MappedRecord original = formats.get(objectId.getBase());
        if(original == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND, 
                "Format not found",
                new BasicException.Parameter("path", objectId)
            );        
        }
        Object_2Facade facade;
        facade = Facades.newObject(objectId);
		facade.setValue(original);
		return facade.getDelegate();
    }

    /**
     * Retrieve the reference name
     * 
     * @param request
     * 
     * @return the internalized reference name
     * @throws ServiceException 
     */
    private String getReferenceName(
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path(); 
        int size = path.size();
        return path.get(
            size - 1 - size % 2
        ).intern();
    }

    // --------------------------------------------------------------------------
    protected void getSlice(
        DataproviderRequest request,
        List<MappedRecord> values,
        DataproviderReply reply
    ) throws ServiceException {
        boolean hasMore;
        if(request.position() >= values.size()) {
            values = Collections.emptyList();
            hasMore = false;
        } else {
            int fromPosition = request.position();
            long toPosition = fromPosition;
            toPosition += request.size();
            if(toPosition >= values.size()) {
                toPosition = values.size();
                hasMore = false;
            } else {
                hasMore = true;
            }
            if(fromPosition > 0 || hasMore) {
                values = values.subList(fromPosition, (int)toPosition);
            }
        }
        reply.getResult().addAll(values);
        reply.setHasMore(Boolean.valueOf(hasMore));
    }
    
    // --------------------------------------------------------------------------
    public class LayerInteraction extends Standard_1.LayerInteraction {
        
        public LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }
                

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            HardWiredObjects_1.this.logger.log(Level.FINEST,"Get request for {0}", request.path());
            String referenceName = getReferenceName(request);
            if("nameFormat" == referenceName) {
                //
                // hard-wired nameFormat
                //
                reply.getResult().add(
                    getFormat(
                        request.path(),
                        HardWiredObjects_1.this.nameFormats
                    )
                );
                return true;
            } else if("addressFormat" == referenceName) {
                //
                // hard-wired addressFormat
                //
                reply.getResult().add(
                    getFormat(
                        request.path(),
                        HardWiredObjects_1.this.addressFormats
                    )
                );
                return true;
            } else {
                //
                // non hard-wired objects
                //
                return super.get(
                    ispec,
                    input,
                    output
                );
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.layer.application.ProvidingUid_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            HardWiredObjects_1.this.logger.log(Level.FINEST,"Create request for {0}", request.path());
            String referenceName = getReferenceName(request);
            if("nameFormat" == referenceName || "addressFormat" == referenceName) {
                //
                // hard-wired nameFormat|addressFormat
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "update not allowed on references with constraint isFrozen",
                    new BasicException.Parameter("reference", referenceName)
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
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            String referenceName = getReferenceName(request);
            if("nameFormat" == referenceName || "addressFormat" == referenceName) {
                //
                // hard-wired nameFormat|addressFormat
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED, 
                    "update not allowed on references with constraint isFrozen",
                    new BasicException.Parameter("reference", referenceName)
                );
            } else {
                //
                // non hard-wired objects
                //
                return super.delete(
                    ispec,
                    input,
                    output
                );
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            String referenceName = getReferenceName(request);
            if("nameFormat" == referenceName || "addressFormat" == referenceName) {
                //
                // hard-wired nameFormat|addressFormat
                //
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "update not allowed on references with constraint isFrozen",
                    new BasicException.Parameter("reference", referenceName)
                );
            } else {
                //
                // non hard-wired objects
                //
                return super.put(
                    ispec,
                    input,
                    output
                );
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
         */
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            String referenceName = getReferenceName(request);
            if("nameFormat" == referenceName) {
                //
                // hard-wired NameFormat
                //
                List<MappedRecord> formats = new ArrayList<MappedRecord>();
                for(String id : HardWiredObjects_1.this.nameFormats.keySet()) {
                    formats.add(
                        getFormat(
                            request.path().getChild(id),
                            HardWiredObjects_1.this.nameFormats
                        )
                    );
                }
                HardWiredObjects_1.this.getSlice(request, formats, reply);
                return true;
            } else if("addressFormat" == referenceName) {
                //
                // hard-wired AddressFormat
                //
                List<MappedRecord> formats = new ArrayList<MappedRecord>();
                for(String id : HardWiredObjects_1.this.addressFormats.keySet()) {
                    formats.add(
                        getFormat(
                            request.path().getChild(id),
                            HardWiredObjects_1.this.addressFormats
                        )
                    );
                }
                HardWiredObjects_1.this.getSlice(request, formats, reply);
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

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private static final String ADDRESS_FORMAT_TYPE_NAME = "test:openmdx:app1:AddressFormat";
    private static final String NAME_FORMAT_TYPE_NAME = "test:openmdx:app1:NameFormat";

    private Map<String,MappedRecord> nameFormats;
    private Map<String,MappedRecord> addressFormats;
    private final Logger logger = Logger.getLogger(HardWiredObjects_1.class.getName());
    
}
