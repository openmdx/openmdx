/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.1 2009/01/13 02:10:40 wfro Exp $
 * Description: model1 application plugin
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:40 $
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
package org.openmdx.application.mof.repository.layer.model;

import java.util.List;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.mof.repository.utils.ModelUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.layer.model.OptimisticLocking_1;

@SuppressWarnings("unchecked")
public class Model_1 
  extends OptimisticLocking_1 {

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
  void completeObject(
    ServiceHeader header,
    DataproviderObject object
  ) throws ServiceException {

    //SysLog.trace("> completeObject for " + object);
    if(object.containsAttributeName(SystemAttributes.OBJECT_CLASS)) {
      List supertype = ModelUtils.getallSupertype(
        (String)object.values(SystemAttributes.OBJECT_CLASS).get(0)
      );
      if(supertype != null) {
        object.clearValues(SystemAttributes.OBJECT_INSTANCE_OF).addAll(
          supertype
        );
      }
      else {
        object.clearValues(SystemAttributes.OBJECT_INSTANCE_OF).addAll(
          object.values(SystemAttributes.OBJECT_CLASS)
        );
      }
    }
    //SysLog.trace("< completeObject");
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
  public DataproviderReply get(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    return completeReply(
      header,
      super.get(
        header,
        request
      )
    );
  }

  //---------------------------------------------------------------------------
  public DataproviderReply find(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    return completeReply(
      header,
      super.find(
        header,
        request
      )
    );
  }

  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------

}

//--- End of File -----------------------------------------------------------
