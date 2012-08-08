/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ReplyParser.java,v 1.12 2008/03/19 17:10:05 hburger Exp $
 * Description: ClientParser class
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:10:05 $
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
package org.openmdx.compatibility.base.dataprovider.transport.webservices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

@SuppressWarnings("unchecked")
public class ReplyParser
  extends AbstractParser {

  /**
   * @param reader  the Reader to be parsed as Object to be compatible
   *                with .NET implementation
   * @throws ServiceException
   */
  public ReplyParser(
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
    this.tagBuffer.append(ch, offset, length);
  }

  //-------------------------------------------------------------------------
  void startElement(
    String rawname
  ) throws ServiceException {
      this.tagBuffer.setLength(0);
  }

  //-------------------------------------------------------------------------
  void endElement(
    String rawname
  ) throws ServiceException {
    this.tagValue = tagBuffer.toString();
    if ("str".equalsIgnoreCase(rawname)) {
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
    else if("bin".equalsIgnoreCase(rawname)) {
      binaryValues.add(Base64.decode(tagValue));
    }
    else if("path".equalsIgnoreCase(rawname)) {
      pathValues.add(new Path(tagValue));
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
        else if(indices.size() == numericValues.size()) {
          attributeValues = (ArrayList)numericValues.clone();
          numericValues.clear();
        }
        else if (indices.size() == booleanValues.size()) {
          attributeValues = (ArrayList)booleanValues.clone();
          booleanValues.clear();
        }
        else if(indices.size() == binaryValues.size()) {
          attributeValues = (ArrayList)binaryValues.clone();
          binaryValues.clear();
        }
        else if(indices.size() == pathValues.size()) {
          attributeValues = (ArrayList)pathValues.clone();
          pathValues.clear();
        }
        dataproviderObject.values(name);
        for (int index = 0; index < indices.size(); index++) {
          dataproviderObject.values(name).add(
            ((Integer)indices.get(index)).intValue(),
            attributeValues.get(index));
        }
        indices.clear();
        name = null;
      }
    }
    else if ("identity".equalsIgnoreCase(rawname)) {
      dataproviderObject = new DataproviderObject(
        new Path(tagValue)
      );
    }
    else if ("digest".equalsIgnoreCase(rawname)) {
      if(dataproviderObject != null && tagValue.length() > 0) {
        dataproviderObject.setDigest(Base64.decode(tagValue));
      }
    }
    else if ("DataproviderObject".equalsIgnoreCase(rawname)) {
      if(dataproviderObject != null) {
        dataproviderObjects.add(dataproviderObject);
      }
      dataproviderObject = null;

    }
    else if ("Context".equalsIgnoreCase(rawname)) {
      List values = new ArrayList();
      if (indices.size() == stringValues.size()) {
        values = (ArrayList)stringValues.clone();
        stringValues.clear();
      }
      else if (indices.size() == numericValues.size()) {
        values = (ArrayList)numericValues.clone();
        numericValues.clear();
      }
      else if (indices.size() == binaryValues.size()) {
        values = (ArrayList)binaryValues.clone();
        binaryValues.clear();
      }
      else if (indices.size() == booleanValues.size()) {
        values = (ArrayList)booleanValues.clone();
        booleanValues.clear();
      }
      OffsetArrayList context = new OffsetArrayList();
      for (int i = 0; i < indices.size(); i++) {
        context.set(
          ((Number)indices.get(i)).intValue(),
          values.get(i)
        );
      }
      if(name != null && !context.isEmpty()) {
        contexts.put(name, context);
      }
      indices.clear();
      name = null;
    }
    else if ("DataproviderReply".equalsIgnoreCase(rawname)) {
      DataproviderReply dataproviderReply =
        new DataproviderReply(
          dataproviderObjects.subList(0, dataproviderObjects.size()));
      dataproviderReply.contexts().putAll(
        contexts
      );
      contexts.clear();
      dataproviderReplies.add(dataproviderReply);
      dataproviderObjects.clear();

    }
    else if("domain".equalsIgnoreCase(rawname)) {
      domain = tagValue;
    }
    else if ("errorCode".equalsIgnoreCase(rawname)) {
      errorCode = Integer.parseInt(tagValue);
    }
    else if ("Property".equalsIgnoreCase(rawname)) {
      if (name != null && !stringValues.isEmpty()) {
        String[] propertyValueArray = new String[stringValues.size()];
        for(int i = 0; i < stringValues.size(); i++) {
          propertyValueArray[i] = (String)stringValues.get(i);
        }
        properties.add(
          new BasicException.Parameter(name, propertyValueArray)
        );
        name = null;
        stringValues.clear();
      }
    }
    else if ("descr".equalsIgnoreCase(rawname)) {
      description = tagValue;
    }
    else if ("StackedException".equalsIgnoreCase(rawname)) {
      if(
        (domain != null)
        && (errorCode != 0)
        && !properties.isEmpty()
        && (description != null)
      ) {
        this.stackedExceptions.add(
          new BasicException(
            domain,
            errorCode,
            (BasicException.Parameter[])properties.toArray(
              new BasicException.Parameter[properties.size()]
            ),
            description
          )
        );
        domain = null;
        errorCode = 0;
        properties.clear();
        description = null;
      }
    }
    else if("ServiceException".equalsIgnoreCase(rawname)) {
      boolean isFirst = true;
      int last = stackedExceptions.size()-1;
      for(
        int i = last-1;
        i >= 0;
        i--
      ) {
        if(isFirst) {
          ((BasicException)this.stackedExceptions.get(last))
            .initCause((BasicException)this.stackedExceptions.get(i));
          isFirst = false;
        }
        else {
          ((BasicException)this.stackedExceptions.get(last))
            .appendCause((BasicException)this.stackedExceptions.get(i));
        }
      }
      this.serviceException = new ServiceException(
        (BasicException)this.stackedExceptions.get(last)
      );
      this.stackedExceptions.clear();
    }
    else if ("UnitOfWorkReply".equalsIgnoreCase(rawname)) {
      if(dataproviderReplies.size() > 0 && serviceException == null) {
        DataproviderReply[] replies =
          new DataproviderReply[dataproviderReplies.size()];
        for(int i = 0; i < dataproviderReplies.size(); i++) {
          replies[i] = (DataproviderReply)dataproviderReplies.get(i);
        }
        unitOfWorkReplies.add(new UnitOfWorkReply(replies));
        dataproviderReplies.clear();
      }
      else {
        unitOfWorkReplies.add(new UnitOfWorkReply(serviceException));
        serviceException = null;
      }
    }
    tagValue = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Return the UnitOfWorkReply array.
   */
  public UnitOfWorkReply[] getReplies(
  ) throws ServiceException {
    UnitOfWorkReply[] unitOfWorkReplyArray =
      new UnitOfWorkReply[unitOfWorkReplies.size()];
    for(int i = 0; i < unitOfWorkReplies.size(); i++) {
      unitOfWorkReplyArray[i] = (UnitOfWorkReply)unitOfWorkReplies.get(i);
    }
    unitOfWorkReplies.clear();
    return unitOfWorkReplyArray;
  }

  //----------------------------------------------------------------------------------
  // Member variables
  //----------------------------------------------------------------------------------
  private StringBuilder tagBuffer = new StringBuilder();
  private String tagValue = null;

  private ArrayList stringValues = new ArrayList();
  private ArrayList numericValues = new ArrayList();
  private ArrayList booleanValues = new ArrayList();
  private ArrayList binaryValues = new ArrayList();
  private ArrayList pathValues = new ArrayList();
  private ArrayList indices = new ArrayList();

//private Path path = null;
  private String name = null;
  private DataproviderObject dataproviderObject = null;
  private ArrayList dataproviderObjects = new ArrayList();
  private Map contexts = new HashMap();
  private ArrayList dataproviderReplies = new ArrayList();

  private String domain = null;
  private int errorCode = 0;
  private ArrayList properties = new ArrayList();
  private String description = null;
  private List stackedExceptions = new ArrayList();
  private ServiceException serviceException = null;

  private ArrayList unitOfWorkReplies = new ArrayList();
}

//--- End of File -----------------------------------------------------------
