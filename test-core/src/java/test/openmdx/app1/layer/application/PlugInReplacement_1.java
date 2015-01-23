/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: TestApp_1's standard stack
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;

import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * TestApp_1's standard stack
 * <p>
 * This layer implementation shall be replaced by an aspect oriented
 * persistence plug-in in future.
 */
public class PlugInReplacement_1 extends VirtualObjects_1 {

    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
                        
    // --------------------------------------------------------------------------
    public class LayerInteraction extends VirtualObjects_1.LayerInteraction {
        
        public LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
                
        /* (non-Javadoc)
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            Path objectId = request.path();
            if(objectId.isLike(MESSAGE_TEMPLATE_PATTERN)) {
				Object_2Facade facade = Facades.asObject(request.object());
                String text = (String) facade.getSingletonFromAttributeValuesAsList("text");
                if(text == null || text.length() == 0) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "The message template text must not be null",
                        new BasicException.Parameter("xri", objectId),
                        new BasicException.Parameter("text", text)
                    );
                }
			}
            super.create(
                ispec,
                input,
                output
            );
            return true;
        }
        
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------        
    private final Path MESSAGE_TEMPLATE_PATTERN = new Path(
        "xri://@openmdx*test.openmdx.app1/provider/($..)/segment/($..)/messageTemplate/($..)"
    );
    
}
