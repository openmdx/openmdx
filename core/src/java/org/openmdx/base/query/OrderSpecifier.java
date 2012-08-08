/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: OrderSpecifier.java,v 1.9 2009/01/06 10:21:19 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 10:21:19 $
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
package org.openmdx.base.query;

import java.io.Serializable;

import javax.resource.ResourceException;

import org.openmdx.application.dataprovider.cci.Orders;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.resource.Records;

public class OrderSpecifier
  implements Serializable {
  
public OrderSpecifier(
  ) {
    this(null, (short)-1);
  }
  
  public OrderSpecifier(
      String feature,
      short order
  ) {
    this.feature = feature;
    this.order = order;
  }

  public String getFeature() {
    return this.feature;
  }
  
  public void setFeature(
      String feature
  ) {
    this.feature = feature;
  }

  public short getOrder() {
    return this.order;
  }
  
  public void setOrder(
      short order
  ) {
    this.order = order;
  }

  public String toString(
  ) {
    try {
      return Records.getRecordFactory().asMappedRecord(
        getClass().getName(), 
        this.feature + ' ' + Orders.toString(this.order),
        TO_STRING_FIELDS,
        new Object[]{
          feature,
          Orders.toString(this.order)
        }
      ).toString();
    } catch (ResourceException exception) {
      throw new RuntimeServiceException(exception);
    }
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private static final long serialVersionUID = 3258134635077645875L;
  private String feature;
  private short order;
  private static final String[] TO_STRING_FIELDS = {
      "feature",
      "order"
  };

  
}
