/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RequestParser.java,v 1.14 2007/10/10 16:06:02 hburger Exp $
 * Description: RequestParser
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:02 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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

/**
 * @author wfro
 */
package org.openmdx.compatibility.base.dataprovider.transport.webservices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

import org.openmdx.kernel.text.StringBuilders;

public class RequestParser
  extends AbstractParser {

  /**
   * @param reader    the Reader to be parsed as Object to be compatible
   *                  with .NET implementation
   * @throws ServiceException
   */
  public RequestParser(
    Object reader
  ) throws ServiceException {
    this.parse(
      reader
    );
  }

  //-------------------------------------------------------------------------   
  void characters(
    char[] ch,
    int offset,
    int length
  ) throws ServiceException {
      StringBuilders.asStringBuilder(this.tagBuffer).append(
      ch,
      offset,
      length
    );
  }

  //-------------------------------------------------------------------------   
  void startElement(
    String rawname
  ) throws ServiceException {
      StringBuilders.asStringBuilder(this.tagBuffer).setLength(0);
  }

  //-------------------------------------------------------------------------   
  void endElement(
    String rawname
  ) throws ServiceException {
    this.tagValue = tagBuffer.toString();
    if("str".equalsIgnoreCase(rawname)) {
      stringValues.add(Entities.XML.unescape(tagValue));
    }
    else if ("short".equalsIgnoreCase(rawname)) {
      numericValues.add(new Short(tagValue));
    }
    else if ("int".equalsIgnoreCase(rawname)) {
      numericValues.add(new Integer(tagValue));
    }
    else if ("long".equalsIgnoreCase(rawname)) {
      numericValues.add(new Long(tagValue));
    }
    else if ("dec".equalsIgnoreCase(rawname)) {
      numericValues.add(new BigDecimal(tagValue));
    }
    else if ("bool".equalsIgnoreCase(rawname)) {
      booleanValues.add(new Boolean(tagValue));
    }
    else if ("path".equalsIgnoreCase(rawname)) {
      pathValues.add(new Path(tagValue));
    }
    else if ("bin".equalsIgnoreCase(rawname)) {
      binaryValues.add(Base64.decode(tagValue));
    }
    else if ("principal".equalsIgnoreCase(rawname)) {
      principalChain.add(tagValue);
    }
    else if ("session".equalsIgnoreCase(rawname)) {
      session = tagValue;
    }
    else if ("requestedAt".equalsIgnoreCase(rawname)) {
      requestedAt = tagValue;
    }
    else if ("requestedFor".equalsIgnoreCase(rawname)) {
      requestedFor = tagValue;
    }
    else if ("trace".equalsIgnoreCase(rawname)) {
      trace = Boolean.getBoolean(tagValue);
    }
    else if ("identity".equalsIgnoreCase(rawname)) {
      dataproviderObject = new DataproviderObject(
        new Path(tagValue)
      );
    }
    else if ("ServiceHeader".equalsIgnoreCase(rawname)) {
      String[] principalArray = new String[principalChain.size()];
      for (int i = 0; i < principalChain.size(); i++) {
        principalArray[i] = (String)principalChain.get(i);
      }
      if (session == null) {
        session = "";
      }
      header = new ServiceHeader(
        principalArray,
        session,
        trace,
        new QualityOfService(),
        requestedAt,
        requestedFor
      );
      principalChain.clear();
      session = null;
      trace = false;
      requestedAt = null;
      requestedFor = null;
    }
    else if ("name".equalsIgnoreCase(rawname)) {
      name = tagValue;
    }
    else if ("idx".equalsIgnoreCase(rawname)) {
      indices.add(new Integer(tagValue));
    }
    else if ("Attribute".equalsIgnoreCase(rawname)) {
      if (dataproviderObject != null && name != null) {
        ArrayList attributeValues = new ArrayList();
        if (indices.size() == stringValues.size()) {
          attributeValues = (ArrayList)stringValues.clone();
          stringValues.clear();
        }
        else if (indices.size() == numericValues.size()) {
          attributeValues = (ArrayList)numericValues.clone();
          numericValues.clear();
        }
        else if (indices.size() == booleanValues.size()) {
          attributeValues = (ArrayList)booleanValues.clone();
          booleanValues.clear();
        }
        else if (indices.size() == binaryValues.size()) {
          attributeValues = (ArrayList)binaryValues.clone();
          binaryValues.clear();
        }
        else if (indices.size() == pathValues.size()) {
          attributeValues = (ArrayList)pathValues.clone();
          pathValues.clear();
        }
        // touch attribute to add an empty list
        dataproviderObject.values(name);
        for(int index = 0; index < indices.size(); index++) {
          try {
            dataproviderObject.values(name).add(
              ((Integer)indices.get(index)).intValue(),
              attributeValues.get(index)
            );
          }
          catch(IndexOutOfBoundsException e) {
            System.out.println("stop");
          }
        }
        indices.clear();
        name = null;
      }
    }
    else if("digest".equalsIgnoreCase(rawname)) {
      if (dataproviderObject != null) {
        dataproviderObject.setDigest(Base64.decode(tagValue));
      }
    }
    else if("operation".equalsIgnoreCase(rawname)) {
      operation = (short)DataproviderOperations.fromString(tagValue);
    }
    else if("quantor".equalsIgnoreCase(rawname)) {
      quantor = (short)Quantors.fromString(tagValue);
    }
    else if("operator".equalsIgnoreCase(rawname)) {
      operator = (short)FilterOperators.fromString(tagValue);
    }
    else if("size".equalsIgnoreCase(rawname)) {
      size = Integer.parseInt(tagValue);
    }
    else if("direction".equalsIgnoreCase(rawname)) {
      direction = (short)Directions.fromString(tagValue);
    }
    else if("selector".equalsIgnoreCase(rawname)) {
      selector = (short)AttributeSelectors.fromString(tagValue);
    }
    else if("position".equalsIgnoreCase(rawname)) {
      position = Integer.parseInt(tagValue);
    }
    else if("order".equalsIgnoreCase(rawname)) {
      order = (short)Orders.fromString(tagValue);
    }
    else if("FilterProperty".equalsIgnoreCase(rawname)) {
      if(
        (quantor != -1)
        && (name != null)
        && (operator != 0)
      ) {
        ArrayList filterPropertyValues = new ArrayList();
        if(!stringValues.isEmpty()) {
          filterPropertyValues = (ArrayList)stringValues.clone();
          stringValues.clear();
        }
        else if(!numericValues.isEmpty()) {
          filterPropertyValues = (ArrayList)numericValues.clone();
          numericValues.clear();
        }
        else if(!booleanValues.isEmpty()) {
          filterPropertyValues = (ArrayList)booleanValues.clone();
          booleanValues.clear();
        }
        else if(!binaryValues.isEmpty()) {
          filterPropertyValues = (ArrayList)binaryValues.clone();
          binaryValues.clear();
        }
        else if(!pathValues.isEmpty()) {
          filterPropertyValues = (ArrayList)pathValues.clone();
          pathValues.clear();
        }
        Object[] filterPropertyArray = new Object[filterPropertyValues.size()];
        for (int i = 0; i < filterPropertyValues.size(); i++) {
          filterPropertyArray[i] = filterPropertyValues.get(i);
        }
        filterProperties.add(
          new FilterProperty(
            quantor,
            name,
            operator,
            filterPropertyArray)
          );
      }
      quantor = -1;
      name = null;
      operator = 0;
    }
    else if("AttributeSpecifier".equalsIgnoreCase(rawname)) {
      if(name != null) {
        if(
          (order != Orders.ANY)
          || ((size == 1) && (direction == Directions.ASCENDING))
        ) {
          attributeSpecifiers.add(
            new AttributeSpecifier(
              name,
              position,
              order
            )
          );
        }
        else {
          attributeSpecifiers.add(
            new AttributeSpecifier(
              name,
              position,
              size,
              direction
            )
          );
        }
      }
      name = null;
      position = 0;
      size = Integer.MAX_VALUE;
      direction = Directions.ASCENDING;
      order = Orders.ANY;
    }
    else if("Context".equalsIgnoreCase(rawname)) {
      List values = new ArrayList();
      if (indices.size() == stringValues.size()) {
        values = (ArrayList)stringValues.clone();
        stringValues.clear();
      }
      else if(indices.size() == numericValues.size()) {
        values = (ArrayList)numericValues.clone();
        numericValues.clear();
      }
      else if(indices.size() == booleanValues.size()) {
        values = (ArrayList)booleanValues.clone();
        booleanValues.clear();
      }
      else if(indices.size() == binaryValues.size()) {
        values = (ArrayList)binaryValues.clone();
        binaryValues.clear();
      }
      OffsetArrayList context = new OffsetArrayList();
      for(int i = 0; i < indices.size(); i++) {
        context.set(
          ((Number)indices.get(i)).intValue(),
          values.get(i)
        );
      }
      if(name != null && context.size() > 0) {
        contexts.put(name, context);
      }
      indices.clear();
      name = null;
    }
    else if("Request".equalsIgnoreCase(rawname)) {
      AttributeSpecifier[] attributeSpecifiersArray = new AttributeSpecifier[attributeSpecifiers.size()];
      for (int i = 0; i < attributeSpecifiers.size(); i++) {
        attributeSpecifiersArray[i] = (AttributeSpecifier)attributeSpecifiers.get(i);
      }
      FilterProperty[] filterPropertyArray = new FilterProperty[filterProperties.size()];
      for (int i = 0; i < filterProperties.size(); i++) {
        filterPropertyArray[i] = (FilterProperty)filterProperties.get(i);
      }
      DataproviderRequest dataproviderRequest = null;
      if(dataproviderObject != null && operation != -1) {
        dataproviderRequest =
          new DataproviderRequest(
            dataproviderObject,
            operation,
            filterPropertyArray,
            this.position,
            this.size,
            this.direction,
            this.selector,
            attributeSpecifiersArray
          );
        dataproviderRequest.contexts().putAll(
          contexts
        );
      }
      contexts.clear();
      dataproviderRequests.add(dataproviderRequest);
      dataproviderObject = null;
      operation = -1;
      filterProperties.clear();
      this.position = 0;
      this.size = Integer.MAX_VALUE;
      this.direction = Directions.ASCENDING;
      this.selector = -1;
      attributeSpecifiers.clear();
    }
    else if("RequestList".equalsIgnoreCase(rawname)) {
        //
    }

    else if("transactionalUnit".equalsIgnoreCase(rawname)) {
      transactionalUnit = Boolean.valueOf(tagValue).booleanValue();
      SysLog.detail("transactionalUnit", "" + transactionalUnit + "; tagValue=" + tagValue);
    }
    else if ("UnitOfWorkRequest".equalsIgnoreCase(rawname)) {
      DataproviderRequest[] dataproviderRequestArray = new DataproviderRequest[dataproviderRequests.size()];
      for(int i = 0; i < dataproviderRequests.size(); i++) {
        dataproviderRequestArray[i] = (DataproviderRequest)dataproviderRequests.get(i);
      }
      UnitOfWorkRequest unitOfWorkRequest = new UnitOfWorkRequest(
        transactionalUnit,
        dataproviderRequestArray
      );
      unitOfWorkRequest.contexts().putAll(
        contexts
      );
      unitOfWorkRequests.add(
        unitOfWorkRequest
      );
      transactionalUnit = false;
      dataproviderRequests.clear();
    }
    tagValue = null;
  }

  //-------------------------------------------------------------------------   
  /**
   * Return the header.
   */
  public ServiceHeader getHeader(
  ) throws ServiceException {
    if(header == null) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.PROCESSING_FAILURE,
        null,
        "Service Header is not initialised"
      );
    }
    return header;
  }

  //-------------------------------------------------------------------------   
  /**
   * Return the UnitOfWorkRequest array.
   */
  public UnitOfWorkRequest[] getUnitOfWorkRequests(
  ) throws ServiceException {
    UnitOfWorkRequest[] unitOfWorkRequestArray = new UnitOfWorkRequest[unitOfWorkRequests.size()];
    for (int i = 0; i < unitOfWorkRequests.size(); i++) {
      unitOfWorkRequestArray[i] = (UnitOfWorkRequest)unitOfWorkRequests.get(i);
    }
    unitOfWorkRequests.clear();
    return unitOfWorkRequestArray;
  }

  //----------------------------------------------------------------------------------
  // Member variables
  //----------------------------------------------------------------------------------

  private CharSequence tagBuffer = StringBuilders.newStringBuilder();
  private String tagValue = null;

  private ArrayList stringValues = new ArrayList();
  private ArrayList numericValues = new ArrayList();
  private ArrayList booleanValues = new ArrayList();
  private ArrayList binaryValues = new ArrayList();
  private ArrayList pathValues = new ArrayList();

  private String name = null;

  private List principalChain = new ArrayList();
  private String session = null;
  private boolean trace = false;
  private String requestedAt = null;
  private String requestedFor = null;
  private ServiceHeader header = null;

//private Path path = null;
  private List indices = new ArrayList();
  private DataproviderObject dataproviderObject = null;

  private List filterProperties = new ArrayList();
  private List attributeSpecifiers = new ArrayList();

  private short operation = -1;
  private short quantor = -1;
  private short operator = 0;
  private int position = 0;
  private int size = Integer.MAX_VALUE;
  private short direction = Directions.ASCENDING;
  private short selector = -1;
  private short order = Orders.ANY;

  private Map contexts = new HashMap();

  private List dataproviderRequests = new ArrayList();
  private boolean transactionalUnit = false;
  private List unitOfWorkRequests = new ArrayList();

}

//--- End of File -----------------------------------------------------------
