/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DateTimeMarshaller.java,v 1.4 2008/04/09 12:34:01 hburger Exp $
 * Description: Date-Time Marshaller class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/09 12:34:01 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.text.ParseException;
import java.util.Date;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.marshalling.ReluctantUnmarshalling;
import org.openmdx.kernel.exception.BasicException;


//---------------------------------------------------------------------------
/**
 * Marshals Object -> Date and Date -> Object. Object must be instanceof String.
 */
public class DateTimeMarshaller
  implements Marshaller, ReluctantUnmarshalling 
{

  //-------------------------------------------------------------------------
  private DateTimeMarshaller(
    boolean forward
  ) {
    this.forward = forward;
  }
  
  //-------------------------------------------------------------------------
  public static DateTimeMarshaller getInstance(
    boolean forward
  ) {
    return forward
      ? DateTimeMarshaller.toMarshaller
      : DateTimeMarshaller.fromMarshaller;
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
        return DateFormat.getInstance().parse((String)source);
      }
      catch(ParseException e) {
        throw new ServiceException(
            e,
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            new BasicException.Parameter [] {
              new BasicException.Parameter("source", source),
              new BasicException.Parameter("source class", source.getClass().getName()),
            },
            "exception parsing date"
        );  
      }
    }
    else {
      if(source instanceof Date) {
        return DateFormat.getInstance().format((Date)source);
      }
      else {
        throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            new BasicException.Parameter[] {
              new BasicException.Parameter("source", source),
              new BasicException.Parameter("source class", source.getClass().getName()),
            },
            "Can only unmarshal objects of type " + Date.class.getName()
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
  
  static private final DateTimeMarshaller toMarshaller = new DateTimeMarshaller(true);
  static private final DateTimeMarshaller fromMarshaller = new DateTimeMarshaller(false);

}

//--- End of File -----------------------------------------------------------
