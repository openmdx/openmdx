/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DelegatingProductImpl.java,v 1.8 2004/06/22 18:27:02 hburger Exp $
 * Description: DelegatingProductImpl class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/06/22 18:27:02 $
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
package org.openmdx.test.test.app1.plugin.jmi;

import org.openmdx.base.accessor.generic.spi.AbstractObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;

/**
 * @author wfro
 * 
 * This class is a sample implementation for a delegating object.
 * The member 'delegation' can be replaced by any remote object
 * which can be used for delegation, e.g. a CORBA or EJB proxy.
 */
public class DelegatingProductImpl 
  extends AbstractObject_1 {

  //-------------------------------------------------------------------------
  public DelegatingProductImpl(
    Path identity
  ){
    super(
      identity,
      "org:openmdx:test:app1:Product"
    );
  }
  
  //-------------------------------------------------------------------------
  public Object objGetValue(
    String feature
  ) throws ServiceException {
    if("tarif".equals(feature)) {
      // simulate an expensive invocation on this.delegation
      try {
        Thread.sleep(1000L);
      }
      catch(Exception e) {}
        return "T99";
    }
    else {
      return super.objGetValue(feature);
    }
  }

}

//--- End of File ------------------------------------------------------------
