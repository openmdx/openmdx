/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StructMarshaller.java,v 1.8 2008/09/25 23:38:10 hburger Exp $
 * Description: StructMarshaller class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/25 23:38:10 $
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
package org.openmdx.base.accessor.jmi.spi;

import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;


//---------------------------------------------------------------------------
public class StructMarshaller
  implements Marshaller {

  //-------------------------------------------------------------------------
  public StructMarshaller(
    String typeName,
    RefPackage_1_0 pkg
  ) {
    this.typeName = typeName;
    this.pkg = pkg;
    this.forward = true;
  }
  
  //-------------------------------------------------------------------------
  public StructMarshaller(
    String typeName,
    RefPackage_1_0 pkg,
    boolean forward
  ) {
    this.typeName = typeName;
    this.pkg = pkg;
    this.forward = forward;
  }
  
  //-------------------------------------------------------------------------
  public Object marshalGeneric(
    Object source,
    boolean forward
  ) throws ServiceException {
    if(source == null) {
      return null;
    }
    if(forward) {
      return this.pkg.refCreateStruct(
        this.typeName,
        source
      );
    }
    else {
      if(source instanceof RefStruct_1_0) {
        return ((RefStruct_1_0)source).refDelegate();
      }
      else {
        throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            "Can only unmarshal objects of type RefStruct_1_0",
            new BasicException.Parameter("source", source),
            new BasicException.Parameter("source class", source.getClass().getName())
        );  
      }
    }
  }

  //-------------------------------------------------------------------------
  public Object marshal(
    Object source
  ) throws ServiceException {
    return marshalGeneric(
      source, 
      this.forward
    );
  }
  
  //-------------------------------------------------------------------------
  public Object unmarshal (
    Object source
  ) throws ServiceException {
    return marshalGeneric(
      source, 
      !this.forward
    );
  }
     
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private final String typeName;
  private final boolean forward;
  private final RefPackage_1_0 pkg;
}
  
//--- End of File -----------------------------------------------------------
