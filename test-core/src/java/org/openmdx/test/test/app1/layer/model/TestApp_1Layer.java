/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestApp_1Layer.java,v 1.9 2004/07/11 20:37:55 hburger Exp $
 * Description: ch:omex:testApp1 persistence plugin for SPICE 2.x
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/11 20:37:55 $
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
package org.openmdx.test.test.app1.layer.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1;

//-----------------------------------------------------------------------------
public class TestApp_1Layer
  extends Standard_1 {
    
  //---------------------------------------------------------------------------
  public void activate(
    short id,
    Configuration configuration,
    Layer_1_0 delegation
  ) throws Exception, ServiceException {
    
    super.activate(
      id, 
      configuration, 
      delegation
    );

    String timestamp = org.openmdx.base.text.format.DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
    
    // hard-wired NameFormat
    this.nameFormats = new HashMap();
    DataproviderObject nameFormatStandard = new DataproviderObject(new Path("Standard"));
    nameFormatStandard.values("description").add("default name format");
    nameFormatStandard.values(SystemAttributes.OBJECT_CLASS).add(NAME_FORMAT_TYPE_NAME);
    nameFormatStandard.values(SystemAttributes.CREATED_AT).add(timestamp);
    nameFormatStandard.values(SystemAttributes.MODIFIED_AT).add(timestamp);
    this.nameFormats.put(
      "Standard",
      nameFormatStandard
    );
    
    // hard-wired AddressFormat
    this.addressFormats = new HashMap();
    DataproviderObject addressFormatStandard = new DataproviderObject(new Path("Standard"));
    addressFormatStandard.values("description").add("default address format");
    addressFormatStandard.values(SystemAttributes.OBJECT_CLASS).add(ADDRESS_FORMAT_TYPE_NAME);
    addressFormatStandard.values(SystemAttributes.CREATED_AT).add(timestamp);
    addressFormatStandard.values(SystemAttributes.MODIFIED_AT).add(timestamp);
    this.addressFormats.put(
      "Standard",
      addressFormatStandard
    );
    
  }     

  //---------------------------------------------------------------------------
  private DataproviderObject getAddressFormat(
    Path reference,
    String id
  ) throws ServiceException {

    Path objectPath = reference.getChild(id);
    DataproviderObject original = (DataproviderObject)this.addressFormats.get(id);
    if(original == null) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.NOT_FOUND, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("path", objectPath)
        },
        "AddressFormat not found"
      );        
    }
    DataproviderObject reply = new DataproviderObject(original);
    reply.path().setTo(objectPath);
    return reply;
  }
  
  //---------------------------------------------------------------------------
  private DataproviderObject getNameFormat(
    Path reference,
    String id
  ) throws ServiceException {

    Path objectPath = reference.getChild(id);
    DataproviderObject original = (DataproviderObject)this.nameFormats.get(id);
    if(original == null) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.NOT_FOUND, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("path", objectPath)
        },
        "NameFormat not found"
      );        
    }
    DataproviderObject reply = new DataproviderObject(original);
    reply.path().setTo(objectPath);
    return reply;
  }
  
  //---------------------------------------------------------------------------
  private String getReferenceName(
    DataproviderRequest request
  ) {
    Path path = request.path();    
    return
      path.size() % 2 == 0 ?
      (String)path.get(path.size()-1) :
      (String)path.get(path.size()-2);
  }

  //---------------------------------------------------------------------------
  public DataproviderReply get(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    SysLog.warning("get request", request.path());
    
    String referenceName = getReferenceName(request);

    // hard-wired segment
//    if("segment".equals(referenceName)) {
//      DataproviderObject segment = new DataproviderObject(
//        request.path()
//      );
//      segment.values(SystemAttributes.OBJECT_CLASS).add(
//        SEGMENT_TYPE_NAME
//      );
//      return new DataproviderReply(
//        segment
//      );
//    }
    
    // hard-wired nameFormat
    if("nameFormat".equals(referenceName)) {
      return new DataproviderReply(
        getNameFormat(
          request.path().getParent(),
          request.path().getBase()
        )
      );
    }
    
    // hard-wired addressFormat
    else if("addressFormat".equals(referenceName)) {
      return new DataproviderReply(
        getAddressFormat(
          request.path().getParent(),
          request.path().getBase()
        )
      );
    }
    
    // non hard-wired objects
    else {
      return super.get(header, request);
    }
  }

  //---------------------------------------------------------------------------
  public DataproviderReply create(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    AppLog.trace("creating object", request.path());
    
    String referenceName = getReferenceName(request);

    // hard-wired nameFormat|addressFormat
    if("nameFormat".equals(referenceName) || "addressFormat".equals(referenceName)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("reference", referenceName)
        },
        "update not allowed on references with constraint isFrozen"
      );
    }
    
    // non hard-wired objects
    else {
      return super.create(header, request);
    }
  }
  
  //---------------------------------------------------------------------------
  public DataproviderReply remove(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    String referenceName = getReferenceName(request);

    // hard-wired nameFormat|addressFormat
    if("nameFormat".equals(referenceName) || "addressFormat".equals(referenceName)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.NOT_SUPPORTED, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("reference", referenceName)
        },
        "update not allowed on references with constraint isFrozen"
      );
    }
    
    // non hard-wired objects
    else {
      return super.remove(header, request);
    }
  }

  //---------------------------------------------------------------------------
  public DataproviderReply replace(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    String referenceName = getReferenceName(request);

    // hard-wired nameFormat|addressFormat
    if("nameFormat".equals(referenceName) || "addressFormat".equals(referenceName)) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("reference", referenceName)
        },
        "update not allowed on references with constraint isFrozen"
      );
    }
    
    // non hard-wired objects
    else {
      return super.replace(header, request);
    }
  }
    
  //---------------------------------------------------------------------------
  public DataproviderReply find(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    
    String referenceName = getReferenceName(request);

    // hard-wired NameFormat
    if("nameFormat".equals(referenceName)) {
      /*
      if(request.attributeFilter().length > 0) {
        throw new ServiceException(
          StackedException.DEFAULT_DOMAIN,
          DataproviderExceptions.ASSERTION, 
          new Property[]{
            new Property("path", request.path()),
            new Property("attribute filter", request.attributeFilter())
          },
          "attribute filter not supported"
        );
      }
      */
      List nameFormats = new ArrayList();
      nameFormats.add(getNameFormat(request.path(), "Standard"));
      DataproviderReply reply = new DataproviderReply(nameFormats);
      reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.FALSE);
      return reply;
    }
        
    // hard-wired AddressFormat
    else if("addressFormat".equals(referenceName)) {
      /*
      if(request.attributeFilter().length > 0) {
        throw new ServiceException(
          StackedException.DEFAULT_DOMAIN,
          StackedException.ASSERTION_FAILURE, 
          new Property[]{
            new Property("path", request.path()),
            new Property("attribute filter", request.attributeFilter())
          },
          "attribute filter not supported"
        );
      }
      */
      List addressFormats = new ArrayList();
      addressFormats.add(getAddressFormat(request.path(), "Standard"));
      DataproviderReply reply = new DataproviderReply(addressFormats);
      reply.context(DataproviderReplyContexts.HAS_MORE).add(new Boolean(false));
      return reply;
    }
    
    // non hard-wired objects
    else {
      return super.find(header, request);
    }
  }

  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private static final String ADDRESS_FORMAT_TYPE_NAME = "org:openmdx:test:app1:AddressFormat";
  private static final String NAME_FORMAT_TYPE_NAME = "org:openmdx:test:app1:NameFormat";

  private HashMap nameFormats = null;
  private HashMap addressFormats = null;
      
}

//--- End of File -----------------------------------------------------------
