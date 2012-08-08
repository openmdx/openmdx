/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PostalAddressImpl.java,v 1.10 2005/07/27 19:55:27 hburger Exp $
 * Description: PostalAddress implementation for test.app1 plugin
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/27 19:55:27 $
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
package org.openmdx.test.test.app1.plugin.object;


import java.util.Collections;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;

//---------------------------------------------------------------------------
public class PostalAddressImpl
  extends AddressImpl { 

    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 7455838703662268064L;

//-------------------------------------------------------------------------
  public PostalAddressImpl(
    Object_1_0 delegation,
    ObjectFactory_1_0 objectFactory,
    Marshaller marshaller
  ) {
    super(
      delegation,
      objectFactory,
      marshaller
    );
  }
    
  //-------------------------------------------------------------------------
  public Structure_1_0 objInvokeOperation(
    String operation,
    Structure_1_0 params
  ) throws ServiceException {
    
    // formatAs
    if("formatAs".equals(operation)) {
      if("Standard".equals(params.objGetValue("type"))) {
        StringBuffer formattedAddress = new StringBuffer();
        formattedAddress.append(
          this.objGetList("addressLine").get(0) + "\n" +
          this.objGetList("addressLine").get(1) + "\n" +
          this.objGetValue("street") + " " + this.objGetValue("houseNumber") + "\n" +
          this.objGetValue("postalCode") + " " + this.objGetValue("city") + "\n"
        );
        return super.objectFactory.createStructure(
            "org:openmdx:test:app1:AddressFormatAsResult",
            Collections.singletonList("formattedAddress"),
            Collections.singletonList(formattedAddress.toString())
        );
      }
      
      // format not supported
      else {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("format type", params.objGetValue("type"))
          },
          "name format not supported. Supported are [Standard]"
        );
      }  
    }
    
    else if("sendMessage".equals(operation)) {
      System.out.println("sending message " + params.objGetValue("document"));
      return super.objectFactory.createStructure(
        "org:openmdx:base:Void", Collections.EMPTY_LIST, Collections.EMPTY_LIST
      );
    }
    
    // operation not supported 
    else {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.NOT_SUPPORTED, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("operation", operation)
        },
        "operation not supported"
      );
    }
  }  
}

//--- End of File ------------------------------------------------------------
