/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ReplyMapper.java,v 1.10 2004/07/11 19:15:26 hburger Exp $
 * Description: SoapServerMapper
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/11 19:15:26 $
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
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.kernel.exception.BasicException;

/**
 * RequestCollection to SOAP mapping for servers.
 */
public class ReplyMapper
  extends AbstractMapper {
    
  public ReplyMapper(
    Writer writer
  ) {
    this.writer = writer;
  }

  //-------------------------------------------------------------------------
  /**
   * Map UnitOfWorkReply to SOAP
   */
  void mapUnitOfWorkReply(
    UnitOfWorkReply source
  ) throws ServiceException, IOException {
    writer.write("<UnitOfWorkReply>");
    if(source.getStatus() == null) {
      this.mapDataproviderReplies(source.getReplies());
    }
    else {
      this.mapServiceException(source.getStatus());
    }
    writer.write("</UnitOfWorkReply>");
  }
    
  //-------------------------------------------------------------------------
  /**
   * Map UnitOfWorkReply[] to SOAP
   */
  public void mapUnitOfWorkReplies(
    UnitOfWorkReply[] source
  ) throws ServiceException, IOException {
    writer.write("<UnitOfWorkReplyList>");
    for (int index = 0; index < source.length; index++) {
      this.mapUnitOfWorkReply(source[index]);
    }
    writer.write("</UnitOfWorkReplyList>");
  }
    
  //-------------------------------------------------------------------------
  void mapDataproviderReply(
    DataproviderReply source
  ) throws ServiceException, IOException {
    writer.write("<DataproviderReply>");

    this.mapDataproviderObjects(source.getObjects());

    this.mapContexts(source.contexts());

    writer.write("</DataproviderReply>");
  }
        
  //-------------------------------------------------------------------------
  /**
   * Map DataproviderReply to SOAP
   */
  void mapDataproviderReplies(
    DataproviderReply[] source
  ) throws ServiceException, IOException {
    for(int i = 0; i < source.length; i++) {
      this.mapDataproviderReply(source[i]);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Map ServiceException to SOAP
   */    
  void mapServiceException(
    ServiceException source
  ) throws ServiceException, IOException {
    writer.write("<ServiceException>");
    for(
      Iterator i = source.getExceptionStack().getExceptionStack().iterator();
      i.hasNext();
    ) {
      BasicException e = (BasicException)i.next();
      writer.write("<StackedException>");
      writer.write("<domain>");
      writer.write(e.getExceptionDomain());
      writer.write("</domain>");
      writer.write("<errorCode>");
      writer.write(String.valueOf(e.getExceptionCode()));
      writer.write("</errorCode>");
      BasicException.Parameter[] parameters = e.getParameters();
      for (
        int j = 0; 
        j < parameters.length; 
        j++
      ) {
        writer.write("<Property>");
        writer.write("<name>");
        writer.write(parameters[j].getName());
        writer.write("</name>");
        writer.write("<str>");
        writer.write(parameters[j].getValue());
        writer.write("</str>");
        writer.write("</Property>");
      }
      writer.write("<descr>");
      writer.write(e.getDescription());
      writer.write("</descr>");
      writer.write("</StackedException>");
    }
    writer.write("</ServiceException>");
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
