/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: WebService_1ConnectionFactoryImpl.java,v 1.6 2004/08/24 01:37:23 hburger Exp $
 * Description: WebService_1ConnectionFactoryImpl
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/08/24 01:37:23 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;

public class WebService_1ConnectionFactoryImpl 
  implements Dataprovider_1ConnectionFactory {

	//-------------------------------------------------------------------------
	public WebService_1ConnectionFactoryImpl(      
    String url, 
    String userId, 
    String passwd
  ) throws ServiceException {
    this.url = url;
    this.userId = userId;
    this.passwd = passwd;
  }
  
  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory#createConnection()
   */
  public Dataprovider_1_1Connection createConnection(
  ) throws ServiceException {
    return new Dataprovider_1Connection(
      this.url,
      this.userId,
      this.passwd
    );
  }
  
	//-------------------------------------------------------------------------  
	private final String url; 
	private final String userId; 
  private final String passwd;  

}

//--- End of File -----------------------------------------------------------