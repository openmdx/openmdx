/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestApp_1Layer.java,v 1.9 2005/01/10 03:12:15 hburger Exp $
 * Description: TestApp_1 Layer
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/01/10 03:12:15 $
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
package org.openmdx.test.test.app1.plugin.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

public class TestApp_1Layer
  extends Layer_1 {
    
  
  //---------------------------------------------------------------------------
  public void activate(
    short id,
    Configuration configuration,
    Layer_1_0 delegation
  ) throws Exception, ServiceException {
    
    super.activate(
      id, 
      configuration, 
      delegation
    );
  }     

  //---------------------------------------------------------------------------
  private String getReferenceName(
    DataproviderRequest request
  ) {
    Path path = request.path();    
    return
      path.size() % 2 == 0 ?
      (String)path.get(path.size()-1) :
      (String)path.get(path.size()-2);
  }

  //---------------------------------------------------------------------------
  private DataproviderObject completeObject(
    ServiceHeader header,
    DataproviderRequest request,
    DataproviderObject object
  ) throws ServiceException {
    
    String objectClass = (String)object.values(SystemAttributes.OBJECT_CLASS).get(0);
    
    // derived attribute Person.age
    if(PERSON_TYPE_NAME.equals(objectClass)) {
      if(object.getValues("birthdate") != null) {
          int currentYear = Calendar.getInstance().get(Calendar.YEAR);
          int birthdateYear = new Integer(((String)object.values("birthdate").get(0)).substring(0,4)).intValue();
        object.values("age").add(new Short((short)(currentYear - birthdateYear)));
      }
    }
    return object;
  }

  //---------------------------------------------------------------------------
  private DataproviderReply completeReply(
    ServiceHeader header,
    DataproviderRequest request,
    DataproviderReply reply
  ) throws ServiceException {
    
    DataproviderObject[] objects = reply.getObjects();
    for(int i = 0; i < objects.length; i++) {
      completeObject(
        header,
        request,
        objects[i]
      );
    }
    return reply; 
  }  
  
  //---------------------------------------------------------------------------
  public DataproviderReply get(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    String referenceName = getReferenceName(request);

    // segment
    if("segment".equals(referenceName)) {
      DataproviderObject segment = new DataproviderObject(
        request.path()
      );
      segment.values(SystemAttributes.OBJECT_CLASS).add(
        SEGMENT_TYPE_NAME
      );
      return new DataproviderReply(
        segment
      );
    }
    
    // matchingAddress
    else if("matchingAddress".equals(referenceName)) {
      RequestCollection requests = new RequestCollection(header, getDelegation());
      return completeReply(
        header,
        request,
        new DataproviderReply(
          new DataproviderObject(
            requests.addGetRequest(
              request.path().getPrefix(request.path().size()-4).getDescendant(
                new String[]{"address", request.path().getBase()}
              )
            )
          )
        )
      );
    }
    
    // non-derived references
    else {
      return completeReply(
        header,
        request,
        super.get(header, request)
      );
    }
  }

  //---------------------------------------------------------------------------
  public DataproviderReply create(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    
    return completeReply(
      header,
      request,
      super.create(header, request)
    );
  }
  
    //---------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
    
        return completeReply(
            header,
            request,
            super.replace(header, request)
        );
    }
  
    //---------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
    
        return completeReply(
            header,
            request,
            super.modify(header, request)
        );
    }
  
  //---------------------------------------------------------------------------
  public DataproviderReply find(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {
    
    String referenceName = getReferenceName(request);

    // derived reference matchingAddress
    if("matchingAddress".equals(referenceName)) {

      if(request.attributeFilter().length > 0) {
        throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("path", request.path()),
            new BasicException.Parameter("attribute filter", request.attributeFilter())
          },
          "attribute filter not supported"
        );
      }
        
      RequestCollection requests = new RequestCollection(header, getDelegation());
      
      // get person
      DataproviderObject_1_0 person = requests.addGetRequest(
        request.path().getParent(),
        AttributeSelectors.ALL_ATTRIBUTES,
        null
      );
      
      // get all addresses
      List addresses = requests.addFindRequest(
        request.path().getPrefix(request.path().size()-4).getChild("address"),
        null,
        AttributeSelectors.ALL_ATTRIBUTES,
        0,
        Integer.MAX_VALUE,
        Directions.ASCENDING
      );
      
      // prepare reply
      List matchingAddresses = new ArrayList(); 
      String nameToMatch = (String)person.values("lastName").get(0);
      for(
        Iterator i = addresses.iterator();
        i.hasNext();
      ) {
        DataproviderObject address = (DataproviderObject)i.next();
        if(((String)address.values("addressLine").get(0)).indexOf(nameToMatch) >= 0) {
          matchingAddresses.add(address);
        }
      } 
      return new DataproviderReply(matchingAddresses);
    }
        
    // non-derived references
    else {
      return completeReply(
        header,
        request,
        super.find(header, request)
      );
    }
  }

  //---------------------------------------------------------------------------
  public DataproviderReply operation(
    ServiceHeader header,
    DataproviderRequest request
  ) throws ServiceException {

    String operationName = getReferenceName(request);
    DataproviderObject params = request.object();
    RequestCollection requests = new RequestCollection(header, getDelegation());
    DataproviderObject_1_0 object = requests.addGetRequest(
      request.path().getPrefix(request.path().size()-2),
      AttributeSelectors.ALL_ATTRIBUTES,
      null
    );
     
    // Person.formatNameAs
    if("formatNameAs".equals(operationName)) {
      DataproviderObject result = createResultRecord(
        request, 
        FORMAT_PERSON_NAME_AS_RESULT_TYPE_NAME
      );
      String formatType = (String)params.values("type").get(0);
      
      // format "Default"
      if("Standard".equals(formatType)) {
        StringBuffer givenNames = new StringBuffer();
        for(int i = 0; i < object.getValues("givenName").size(); i++) {
          if(i > 0) {
            givenNames.append(" ");
          }
          givenNames.append((String)object.getValues("givenName").get(i));
        }
        result.values("formattedName").add(
          object.getValues("salutation").get(0) + " " +
          givenNames.toString() + " " +
          object.getValues("lastName").get(0)
        );
      }
      
      // format not supported
      else {
        throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("format type", formatType)
          },
          "name format not supported. Supported are [Default]"
        );
      }  
      return new DataproviderReply(result); 
    }
    
    // Address.formatAs
    else if("formatAs".equals(operationName)) {
      DataproviderObject result = createResultRecord(
        request,
        FORMAT_ADDRESS_AS_RESULT_TYPE_NAME
      );
      String formatType = (String)params.values("type").get(0);
      String objectClass = (String)object.values(SystemAttributes.OBJECT_CLASS).get(0);
      
      // format "Default"
      if("Standard".equals(formatType)) {

        // EMailAddress
        if(EMAIL_ADDRESS_TYPE_NAME.equals(objectClass)) {
          result.values("formattedAddress").add(
            object.values("address").get(0)
          );
        }

        // PostalAddress
        else if(POSTAL_ADDRESS_TYPE_NAME.equals(objectClass)) {
          StringBuffer formattedAddress = new StringBuffer();
          formattedAddress.append(
            object.values("addressLine").get(0) + "\n" +
            object.values("addressLine").get(1) + "\n" +
            object.values("street").get(0) + " " + object.values("number").get(0) + "\n" +
            object.values("postalCode").get(0) + " " + object.values("city").get(0) + "\n" +
            object.values("country").get(0)
          );
          result.values("formattedAddress").add(
            formattedAddress.toString()
          );
        }
      
        else {
          throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("object class", objectClass),
              new BasicException.Parameter("object", object)
            },
            "unsupported address type. Can not format. Supported are [EMailAddress|PostalAddress]"
          );
        }
      }

      // format not supported
      else {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("format type", formatType)
          },
          "name format not supported. Supported are [Default]"
        );
      }  
      return new DataproviderReply(result); 
    }
    
    // Person.voidOp
    if("voidOp".equals(operationName)) {
      RequestCollection requestCollection = new RequestCollection(header, getDelegation());
      DataproviderObject_1_0 person = requestCollection.addGetRequest(
        request.path().getPrefix(request.path().size()-2), 
        AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES, 
        new AttributeSpecifier[]{
            new AttributeSpecifier("assignedAddress")
        }
      ); 
      for(
        Iterator i = person.values("assignedAddress").populationIterator();
        i.hasNext();
      ){
        DataproviderObject_1_0 address = requestCollection.addGetRequest(
            (Path)i.next(),
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
        if(INTERNATIONAL_POSTAL_ADDRESS_TYPE_NAME.equals(address.values(SystemAttributes.OBJECT_CLASS))){
            int j = Arrays.asList(COUNTRY_NAME).indexOf(address.values("country").get(0));
            if(j >= 0) {
                String countryPrefix = COUNTRY_CODE[j % COUNTRY_CODE.length] + '-';
                SparseList postalCode = address.values("postalCode");
                if(! ((String)postalCode.get(0)).startsWith(countryPrefix)) postalCode.set(
                    0,
                    countryPrefix + postalCode
                );
            }
            
        }
      }
      return new DataproviderReply(
        createResultRecord(request, VOID_TYPE_NAME)
      );    
    }

    // unknown operation
    else {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.ASSERTION_FAILURE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("operation", operationName),
          new BasicException.Parameter("object type", request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0))
        },
        "unknown operation"
      );
    } 
  }

  private DataproviderObject createResultRecord(
    DataproviderRequest request,
    String recordName
  ){
    DataproviderObject result = new DataproviderObject(
      request.path().getDescendant(
        new String[]{ "reply", super.uidAsString()}
      )
    );
    result.clearValues(SystemAttributes.OBJECT_CLASS).add(
      VOID_TYPE_NAME
    );
    return result;
  }
   
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  
  private final static String[] COUNTRY_CODE = new String[]{
    "AT", "DE", "CH"
  };
  private final static String[] COUNTRY_NAME = new String[]{
    "Austria", "Germany", "Switzerland", 
    "Österreich", "Deutschland", "Schweiz",
    "Autriche", "Allemagne", "Suisse",
    "Austria", "Germania", "Svizzera"
  };

  private static final String SEGMENT_TYPE_NAME = "org:openmdx:test:app1:Segment";
  private static final String PERSON_TYPE_NAME = "org:openmdx:test:app1:Person";
  private static final String EMAIL_ADDRESS_TYPE_NAME = "org:openmdx:test:app1:EMailAddress";
  private static final String INTERNATIONAL_POSTAL_ADDRESS_TYPE_NAME = "org:openmdx:test:app1:InternationalPostalAddress";
  private static final String POSTAL_ADDRESS_TYPE_NAME = "org:openmdx:test:app1:PostalAddress";
  private static final String FORMAT_PERSON_NAME_AS_RESULT_TYPE_NAME = "org:openmdx:test:app1:PersonFormatNameAsResult";
  private static final String FORMAT_ADDRESS_AS_RESULT_TYPE_NAME = "org:openmdx:test:app1:AddressFormatAsResult";
  private static final String VOID_TYPE_NAME = "org:openmdx:base:Void";
    
}

//--- End of File -----------------------------------------------------------
