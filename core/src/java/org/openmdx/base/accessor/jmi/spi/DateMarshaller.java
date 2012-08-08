/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DateMarshaller.java,v 1.15 2008/04/09 12:34:01 hburger Exp $
 * Description: DateMarshaller class
 * Revision:    $Revision: 1.15 $
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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.marshalling.ReluctantUnmarshalling;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.Datatypes;

//---------------------------------------------------------------------------
/**
 * Marshals Object -> XMLGregorianCalendar and XMLGregorianCalendar -> Object. 
 */
public class DateMarshaller
  extends Datatypes
  implements Marshaller, ReluctantUnmarshalling 
{

  //-------------------------------------------------------------------------
  private DateMarshaller(
    boolean forward
  ) {
    this.forward = forward;
  }

  //-------------------------------------------------------------------------
  public static DateMarshaller getInstance(
    boolean forward
  ) {
    return forward
      ? DateMarshaller.toMarshaller
      : DateMarshaller.fromMarshaller;
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
        String date = (String)source;
        int limit = date.indexOf('T');
        if(limit >= 0) date = date.substring(0, limit);
        int length = date.length();
        switch (length) {
          case 0: return getFactory().newXMLGregorianCalendarDate(
            DatatypeConstants.FIELD_UNDEFINED, // year
            DatatypeConstants.FIELD_UNDEFINED, // month
            DatatypeConstants.FIELD_UNDEFINED, // day 
            DatatypeConstants.FIELD_UNDEFINED // timezone
          );
          case 1: case 2: return getFactory().newXMLGregorianCalendarDate(
            DatatypeConstants.FIELD_UNDEFINED, // year
            DatatypeConstants.FIELD_UNDEFINED, // month
            Integer.parseInt(date), // day
            DatatypeConstants.FIELD_UNDEFINED // timezone
          );
          case 3: case 4: return getFactory().newXMLGregorianCalendarDate(
            DatatypeConstants.FIELD_UNDEFINED, // year
            Integer.parseInt(date.substring(0, length - 2)), // month
            Integer.parseInt(date.substring(length - 2)), // day
            DatatypeConstants.FIELD_UNDEFINED // timezone
          );
          default: return getFactory().newXMLGregorianCalendarDate(
            Integer.parseInt(date.substring(0, length - 4)), // year
            Integer.parseInt(date.substring(length - 4, length - 2)), // month
            Integer.parseInt(date.substring(length - 2)), // day
            DatatypeConstants.FIELD_UNDEFINED // timezone
          );
        }
      }
      catch(IllegalArgumentException e) {
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
      if(source instanceof XMLGregorianCalendar) {
        XMLGregorianCalendar date = (XMLGregorianCalendar)source;
        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();
        return
          (year < 10 ? "000" : year < 100 ? "00" : year < 1000 ? "0" : "") + year +
          (month < 10 ? "0" : "") + month +
          (day < 10 ? "0" : "") + day;
      }
      else {
        throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.TRANSFORMATION_FAILURE,
            new BasicException.Parameter[] {
              new BasicException.Parameter("source", source),
              new BasicException.Parameter("source class", source.getClass().getName()),
            },
            "Can only unmarshal objects of type " + XMLGregorianCalendar.class.getName()
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

  static private final DateMarshaller toMarshaller = new DateMarshaller(true);
  static private final DateMarshaller fromMarshaller = new DateMarshaller(false);

}

//--- End of File -----------------------------------------------------------
