/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Java Clock Provider
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004.2010, OMEX AG, Switzerland
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
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.layer.application.Standard_1;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Java Clock Provider
 */
public class JavaClock_1 extends Layer_1 {
	
    /**
     * Constructor 
     */
    public JavaClock_1() {
        super();
    }

    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
                        
    // --------------------------------------------------------------------------
    public class LayerInteraction extends Standard_1.LayerInteraction {
        
        /**
         * Constructor 
         *
         * @param connection
         * @throws ResourceException
         */
        protected LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }
	
    	/* (non-Javadoc)
    	 * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
    	 */
        @SuppressWarnings("unchecked")
		@Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
    		if(request.path().isLike(A_SEGMENT_PATH)) try {
        		Object_2Facade segment = Object_2Facade.newInstance(request.path(), "test:openmdx:clock1:Segment");
                InputStream stream = Thread.currentThread(
                ).getContextClassLoader(
                ).getResourceAsStream(
                    "test/openmdx/clock1/segment.txt"
                );
                String description;
                if(stream == null) {
                    description = "n/a";
                } else try {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(stream)
                    );
                    description = in.readLine();
                } catch (IOException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE,
                        "Segment description acquisition failure"
                    );
                }
                segment.getValue().put("description", description);
                reply.getResult().add(
                    segment.getDelegate()
                );
                return true;
    		} catch (ResourceException exception) {
    		    throw new ServiceException(exception);
    		} else {
    		    return super.get(
    		        ispec,
    		        input,
    		        output
    		    );		
    		}
    	}
    
    	/* (non-Javadoc)
    	 * @see org.openmdx.compatibility.base.dataprovider.spi.Operation_1_0#operation(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
    	 */
        @SuppressWarnings("unchecked")
		@Override
        public boolean invoke(
            RestInteractionSpec ispec, 
            MessageRecord input, 
            MessageRecord output
        ) throws ServiceException {
    	    try {
    	        Path target = input.getTarget();
        	    String operationName = target.getBase();
        	    Path accessPath = target.getParent();
        		if(
        			!accessPath.isLike(A_SEGMENT_PATH) ||
        			!"currentDateAndTime".equals(operationName)
        		) {
        		    super.invoke(
        		        ispec,
        		        input,
        		        output
        		    );
        		    return true;
        		}
        		output.setPath(super.newResponseId(input.getPath()));
        		MappedRecord body = Records.getRecordFactory().createMappedRecord("test:openmdx:clock1:Time");
        		output.setBody(body);
        		body.put("utc", new Date());
        	    return true;
    	    } catch (ResourceException exception) {
    	        throw new ServiceException(exception);
    	    }
    	}
        
    }

	private final static Path A_SEGMENT_PATH = new Path(
		"xri://@openmdx*test.openmdx.clock1/provider/($..)/segment/($..)"
	);

}
