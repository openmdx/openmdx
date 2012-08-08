/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.2 2009/06/01 15:41:25 wfro Exp $
 * Description: model1 type plugin
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:41:25 $
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
package org.openmdx.application.mof.repository.layer.type;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

public class Model_1 
  extends Layer_1 {

  //---------------------------------------------------------------------------
  public void activate(
    short id,
    Configuration configuration,
    Layer_1_0 delegation
  ) throws ServiceException {
    
    super.activate(
      id, 
      configuration, 
      delegation
    );

  }

  //---------------------------------------------------------------------------
  String getReferenceName(
    DataproviderRequest request
  ) throws ServiceException {
    Path path = request.path();    
    return
      path.size() % 2 == 0 ?
      (String)path.get(path.size()-1) :
      (String)path.get(path.size()-2);
  }

  //---------------------------------------------------------------------------
  void setObjectType (
    DataproviderRequest request
  ) throws ServiceException {

    // path to authority is empty
    if(request.path().size() == 0) {
      request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
        0,
        "org:openmdx:base:Authority"
      );
    }
    else {   
      String referenceName = getReferenceName(
        request
      );
      //SysLog.trace("referenceName", referenceName);
      if("content".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.ELEMENT
        );
      }
      else if("element".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.ELEMENT
        );
      }
      else if("allSupertype".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.GENERALIZABLE_ELEMENT
        );
      }
      else if("allFeature".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.FEATURE
        );
      }
      else if("packageContent".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.ELEMENT
        );
      }
      else if("lookupElement".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.ELEMENT
        );
      }
      else if("resolveQualifiedName".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.ELEMENT
        );
      }
      else if("findElementsByType".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.ELEMENT
        );
      }
      else if("externalizePackage".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
          0,
          ModelAttributes.PACKAGE
        );
      }
      else if("externalizeClassifier".equals(referenceName)) {
        request.context(DataproviderRequestContexts.OBJECT_TYPE).set(
            0,
            ModelAttributes.CLASSIFIER
        );
      }
    }
  }

  //---------------------------------------------------------------------------
  public DataproviderReply get(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    setObjectType(request);
    return super.get(
      header,
      request
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply find(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    setObjectType(request);
    return super.find(
      header,
      request
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply operation(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    setObjectType(request);
    return super.operation(
      header,
      request
    );
  }

  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------

}

//--- End of File -----------------------------------------------------------
