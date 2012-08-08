/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RequestMapper.java,v 1.9 2008/03/19 17:10:05 hburger Exp $
 * Description: SoapClientMapper class
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:10:05 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.webservices;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;

/**
 * RequestCollection to SOAP mapping for clients.
 */
public class RequestMapper 
  extends AbstractMapper {

  public RequestMapper(
    Writer writer
  ) {
    this.writer = writer;
  }

  //-------------------------------------------------------------------------
  /**
   * Map ServiceHeader to SOAP
   */    
  @SuppressWarnings("unchecked")
void mapServiceHeader(
    ServiceHeader source
  ) throws ServiceException, IOException {
    writer.write("<ServiceHeader>");
    String sessionId = (source.getCorrelationId() == null) ? "" : source.getCorrelationId();
    if(source.getPrincipalChain() != null) {
      for(Iterator i = source.getPrincipalChain().iterator(); i.hasNext();) {
        writer.write("<principal>");
        writer.write((String)i.next());
        writer.write("</principal>");
      }
    }
    if(source.getRequestedAt() != null) {
      writer.write("<requestedAt>");
      writer.write(source.getRequestedAt());
      writer.write("</requestedAt>");
    }
    if(source.getRequestedFor() != null) {
      writer.write("<requestedFor>");
      writer.write(source.getRequestedFor());
      writer.write("</requestedFor>");
    }
    writer.write("<session>");
    writer.write(sessionId);
    writer.write("</session>");
    writer.write("<trace>");
    writer.write((new Boolean(source.traceRequest())).toString());
    writer.write("</trace>");
    writer
      .write("</ServiceHeader>");
  }

  //-------------------------------------------------------------------------
  /**
   * Map AttributeSpecifier to SOAP.
   */
  private void mapAttributeSpecifier(
    AttributeSpecifier source  ) throws ServiceException, IOException {
    writer.write("<AttributeSpecifier>");

    writer.write("<name>");
    writer.write(source.name());
    writer.write("</name>");

    writer.write("<position>");
    writer.write(String.valueOf(source.position()));
    writer.write("</position>");

    writer.write("<size>");
    writer.write(String.valueOf(source.size()));
    writer.write("</size>");

    writer.write("<order>");
    writer.write(Orders.toString(source.order()));
    writer.write("</order>");

    writer.write("<direction>");
    writer.write(Directions.toString(source.direction()));
    writer.write("</direction>");

    writer.write("</AttributeSpecifier>");
  }

  //-------------------------------------------------------------------------
  /**
   * Map AttributeSpecifier[] to SOAP
   */
  private void mapAttributeSpecifiers(
    AttributeSpecifier[] source
  ) throws ServiceException, IOException {
    for (int index = 0; index < source.length; index++)
      this.mapAttributeSpecifier(source[index]);
  }

  //-------------------------------------------------------------------------
  /**
   * Map DataproviderRequest to SOAP
   */
  private void mapDataproviderRequest(
    DataproviderRequest source
  ) throws ServiceException, IOException {
    writer.write("<Request>");
    
    this.mapDataproviderObject(
      source.object()
    );
    writer.write("<operation>");
    writer.write(DataproviderOperations.toString(source.operation()));
    writer.write("</operation>");

    this.mapFilterProperties(
      source.attributeFilter()
    );
    writer.write("<position>");
    writer.write(String.valueOf(source.position()));
    writer.write("</position>");

    writer.write("<size>");
    writer.write(String.valueOf(source.size()));
    writer.write("</size>");

    writer.write("<direction>");
    writer.write(Directions.toString(source.direction()));
    writer.write("</direction>");

    writer.write("<selector>");
    writer.write(AttributeSelectors.toString(source.attributeSelector()));
    writer.write("</selector>");

    this.mapAttributeSpecifiers(source.attributeSpecifier());
    this.mapContexts(
      source.contexts()
    );
    writer.write("</Request>");
  }
        
  //-------------------------------------------------------------------------
  /**
   * Map DataproviderRequest[] to SOAP
   */    
  private void mapDataproviderRequests(
    DataproviderRequest[] source
  ) throws ServiceException, IOException {
    writer.write("<RequestList>");

    for(int i = 0; i < source.length; i++) {
      this.mapDataproviderRequest(source[i]);
    }

    writer.write("</RequestList>");
  }
    
  //-------------------------------------------------------------------------
  /**
   * Map UnitOfWorkRequest to SOAP
   */
  private void mapUnitOfWorkRequest(
    UnitOfWorkRequest source
  ) throws ServiceException, IOException {
    writer.write("<UnitOfWorkRequest>");

    this.mapDataproviderRequests(source.getRequests());
    this.mapContexts(
      source.contexts()
    );

    writer.write("<transactionalUnit>");
    writer.write((new Boolean(source.isTransactionalUnit())).toString());
    writer.write("</transactionalUnit>");
    
    writer.write("</UnitOfWorkRequest>");
  }
        
  //-------------------------------------------------------------------------
  /**
   * Map UnitOfWorkRequest[] to SOAP
   */    
  void mapUnitOfWorkRequests(
    UnitOfWorkRequest[] source
  ) throws ServiceException, IOException {
    writer.write("<UnitOfWorkRequestList>");

    for(int i = 0; i < source.length; i++) {
      this.mapUnitOfWorkRequest(source[i]);
    }

    writer.write("</UnitOfWorkRequestList>");
  }
    
  //-------------------------------------------------------------------------
  /* (non-Javadoc)
   */
  public void mapProlog(
    String prolog
  ) throws IOException {
    writer.write(prolog);
  }

  //-------------------------------------------------------------------------
  /* (non-Javadoc)
   */
  public void mapEpilog(String epilog
  ) throws IOException {
    writer.write(epilog);
  }

}

//--- End of File -----------------------------------------------------------
