/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.20 2010/06/02 13:42:52 hburger Exp $
 * Description: model1 application plugin
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:42:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.layer.model;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.mof.repository.utils.ModelUtils;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.w3c.format.DateTimeFormat;

@SuppressWarnings("unchecked")
public class Model_1 extends Layer_1 {

    //---------------------------------------------------------------------------
    public Model_1(
    ) {
    }
    
    //---------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
    
    //---------------------------------------------------------------------------
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
    }

    //---------------------------------------------------------------------------
    void completeObject(
        ServiceHeader header,
        MappedRecord object
    ) throws ServiceException {
        Object_2Facade facade;
        try {
            facade = Object_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        //SysLog.trace("> completeObject for " + object);
        if(facade.getObjectClass() != null) {
            List supertype = ModelUtils.getallSupertype(
                facade.getObjectClass()
            );
            if(supertype != null) {
                facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).clear();
                facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).addAll(
                    supertype
                );
            }
            else {
                facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).clear();
                facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).addAll(
                    facade.attributeValuesAsList(SystemAttributes.OBJECT_CLASS)
                );
            }
        }
    }

    //---------------------------------------------------------------------------
    DataproviderReply completeReply(
        ServiceHeader header,
        DataproviderReply reply
    ) throws ServiceException {

        //SysLog.trace("> completing reply");
        for(
            int i = 0;
            i < reply.getObjects().length;
            i++
        ) {
            completeObject(
                header,
                reply.getObjects()[i]
            );
        }
        //SysLog.trace("< reply completed");
        return reply; 
    }

    //---------------------------------------------------------------------------
    protected boolean isInstanceOfBasicObject(
        MappedRecord object
    ) throws ServiceException {
        return Object_2Facade.getPath(object).size() > 5;
    }
  
    //---------------------------------------------------------------------------
    protected Date getTransactionTime(
        ServiceHeader header
    ) throws ParseException{
        String requestedAt = header.getRequestedAt();
        return requestedAt == null ? new Date(
        ) : DateTimeFormat.BASIC_UTC_FORMAT.parse(
            requestedAt
        );
    }
  
    //--------------------------------------------------------------------------
    public class LayerInteraction extends Layer_1.LayerInteraction {
      
        public LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }
                
        //---------------------------------------------------------------------------
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderReply reply = this.newDataproviderReply(output);            
            super.get(
                ispec, 
                input, 
                output
            );
            Model_1.this.completeReply(
              header,
              reply
            );
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderReply reply = this.newDataproviderReply(output);
            super.find(
                ispec, 
                input, 
                output
            );
            Model_1.this.completeReply(
              header,
              reply
            );
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            // exclude Authority, Provider, Segment
            if(Model_1.this.isInstanceOfBasicObject(request.object())) try {
                List<String> by = header.getPrincipalChain();
                Date at = getTransactionTime(header);
                Object_2Facade facade = Object_2Facade.newInstance(request.object());                        
                facade.attributeValuesAsList(SystemAttributes.CREATED_BY).clear();
                facade.attributeValuesAsList(SystemAttributes.CREATED_BY).addAll(by);
                facade.attributeValuesAsList(SystemAttributes.CREATED_AT).clear();
                facade.attributeValuesAsList(SystemAttributes.CREATED_AT).add(at);
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_BY).clear();
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_BY).addAll(by);
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_AT).clear();
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_AT).add(at);
            } catch (Exception exception) {
                throw new ServiceException(exception);
            }
            super.create(
                ispec, 
                input, 
                output
            );
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            // exclude Authority, Provider, Segment
            if(Model_1.this.isInstanceOfBasicObject(request.object())) try {
                List<String> by = header.getPrincipalChain();
                Date at = getTransactionTime(header);
                Object_2Facade facade = Object_2Facade.newInstance(request.object());                        
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_BY).clear();
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_BY).addAll(by);
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_AT).clear();
                facade.attributeValuesAsList(SystemAttributes.MODIFIED_AT).add(at);
            } catch (Exception exception) {
                throw new ServiceException(exception);
            }
            super.put(
                ispec, 
                input, 
                output
            );
            return true;
        }
        
  }
    
}

//--- End of File -----------------------------------------------------------
