/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ProxyConnection_1.java,v 1.7 2004/08/23 12:46:29 hburger Exp $
 * Description: ProxyConnection_1 class
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/08/23 12:46:29 $
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
package org.openmdx.compatibility.base.dataprovider.proxy;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderLayers;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.delegation.Delegation_1;
import org.openmdx.kernel.exception.BasicException;

/**
 * Dispatches all process() requests to 'layer' which in turn delegates its
 * requests to 'dataprovider'.
 */
public class ProxyConnection_1
  implements Dataprovider_1_1Connection {

  public ProxyConnection_1(
    Layer_1_0 layer,
    Dataprovider_1_0 dataprovider
  ) throws ServiceException {

    try {
      this.layer = layer;
      Delegation_1 proxy = new Delegation_1();
      proxy.activate(
        DataproviderLayers.PERSISTENCE,
        dataprovider
      );
      layer.activate(
        DataproviderLayers.APPLICATION,
        new Configuration(),
        proxy
      );
    }
    catch(Exception e) {
      throw new ServiceException(
        e,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ACTIVATION_FAILURE,
        null,
        "Establishment of dataprovider proxy connection failed"
      );
    }
  }

  /**
   * Process a set of working units
   *
   * @param header          the service header
   * @param workingUnits    a collection of working units
   *
   * @return    a collection of working unit replies
   */
  public UnitOfWorkReply[] process(
    ServiceHeader header,
    UnitOfWorkRequest[] workingUnits
  ){
    return layer.process(header,workingUnits);
  }

  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection#remove()
   */
  public void remove(
  ) throws ServiceException {

    try {
      layer.deactivate();
    }
    catch(Exception e) {
      throw new ServiceException(
        e,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.DEACTIVATION_FAILURE,
        null,
        "Removal of dataprovider proxy connection failed"
      );
    }
  }

  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection#close()
   */
  public void close() {
    try {
      remove();
    } catch (ServiceException e) {
      throw new RuntimeServiceException(e);
    }
  }
    
  //------------------------------------------------------------------------
  // Variables
  //------------------------------------------------------------------------
  
  /**
   *
   */
  private Layer_1_0 layer;

}

//--- End of File -----------------------------------------------------------
