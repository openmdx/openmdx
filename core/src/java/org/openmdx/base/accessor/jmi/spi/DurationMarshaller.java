/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DurationMarshaller.java,v 1.13 2008/02/08 16:51:25 hburger Exp $
 * Description: DurationMarshaller class
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;

/**
 * DurationMarshaller
 */
public class DurationMarshaller
  implements Marshaller {

  //-------------------------------------------------------------------------
  private DurationMarshaller(
    boolean forward
  ) {
    this.forward = forward;
  }

  //-------------------------------------------------------------------------
  public static DurationMarshaller getInstance(
    boolean forward
  ) {
    return forward
      ? DurationMarshaller.toMarshaller
      : DurationMarshaller.fromMarshaller;
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
      try {
        return this.xmlDatatypeFactory().newDuration((String)source);
      } catch(IllegalArgumentException e) {
        throw new ServiceException(
            e,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.TRANSFORMATION_FAILURE,
            new BasicException.Parameter [] {
              new BasicException.Parameter("source", source),
              new BasicException.Parameter("source class", source.getClass().getName()),
            },
            "exception parsing duration"
        );
      }
    }
    else {
      if(source instanceof Duration) {
        return ((Duration)source).toString();
      }
      else {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.TRANSFORMATION_FAILURE,
            new BasicException.Parameter [] {
              new BasicException.Parameter("source", source),
              new BasicException.Parameter("source class", source.getClass().getName()),
            },
            "Can only unmarshal objects of type " + Duration.class.getName()
        );
      }
    }
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public Object marshal(
    Object source
  ) throws ServiceException {
    return marshalGeneric(
      source,
      this.forward
    );
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
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
  private final boolean forward;

  static private DurationMarshaller toMarshaller = new DurationMarshaller(true);
  static private DurationMarshaller fromMarshaller = new DurationMarshaller(false);

  /**
   * A lazy initialized DatatypeFactory instance
   */
  private DatatypeFactory datatypeFactory = null;

  /**
   * @return a Datatype Factory Instance
   */
  protected synchronized DatatypeFactory xmlDatatypeFactory(
  ){
    if(datatypeFactory == null) try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeServiceException(e);
    }
    return datatypeFactory;
  }

}

//--- End of File -----------------------------------------------------------
