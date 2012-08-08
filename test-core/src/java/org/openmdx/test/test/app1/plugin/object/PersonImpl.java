/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PersonImpl.java,v 1.13 2008/02/14 13:07:19 wfro Exp $
 * Description: class PersonImpl
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/14 13:07:19 $
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.event.InstanceCallbackEvent;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.test.test.app1.plugin.object.SegmentImpl.SegmentReferencesForeignPerson;

//---------------------------------------------------------------------------
public class PersonImpl
  extends ObjectImpl 
  implements InstanceCallbackListener 
{ 
    
    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 3540769389135406681L;
	
//---------------------------------------------------------------------------
  public PersonImpl(
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

  //---------------------------------------------------------------------------
  public Set objDefaultFetchGroup(
  ) throws ServiceException {
    Set group = super.objDefaultFetchGroup();
    group.add("age");
    group.add("creationDateTime");
    return group;
  }
  

  //------------------------------------------------------------------------
  // Implements InstanceCallbackListener
  //------------------------------------------------------------------------

  public void preStore(InstanceCallbackEvent event) throws ServiceException {
      //System.out.println(this.getClass().getName() + ".objPreStore"); 
      int sex = ((Number)super.objGetValue("sex")).intValue();
      String salutation = (String)super.objGetValue("salutation");
      if((0 == sex) && !("Herr".equals(salutation) || "Mister".equals(salutation) || "Monsieur".equals(salutation))) {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("object", this)
          },
          "sex 0 implies salutation [Herr|Mister|Monsieur]"
        );
      }
  }

  //-------------------------------------------------------------------------
  // Object_1_0
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------------
  public void objMove(
    FilterableMap there,
    String criteria
  ) throws ServiceException {

    // application-specific implementation of objMove(). All containers
    // returned by objGetContainer() must be intercepted here.
    if(there instanceof SegmentReferencesForeignPerson) {
      // remap reference 'foreignPerson' to 'person'      
      super.objMove(
        ((SegmentReferencesForeignPerson)there).person,
        criteria 
      );
    }
    else {
      super.objMove(
        there,
        criteria
      );
    }
  }
  
  //-------------------------------------------------------------------------
  public Object objGetValue(
    String feature
  ) throws ServiceException {
    AppLog.trace("objGetValue", "feature=" + feature);
    if("age".equals(feature)) {
      int currentYear = Calendar.getInstance().get(Calendar.YEAR);
      int birthdateYear = new Integer(((String)super.objGetValue("birthdate")).substring(0,4)).intValue();
      return new Integer(currentYear - birthdateYear);
    }
    else if("creationDateTime".equals(feature)) {
      return super.objGetValue(SystemAttributes.CREATED_AT);
    }
    else {
      return super.objGetValue(feature);
    }
  }
    
  //-------------------------------------------------------------------------
  public Structure_1_0 objInvokeOperation(
    String operation,
    Structure_1_0 params
  ) throws ServiceException {
    
    // formatNameAs
    if("formatNameAs".equals(operation)) {
      if((params.objGetValue("type") == null) || "Standard".equals(params.objGetValue("type"))) {
        StringBuffer givenNames = new StringBuffer();
        int ii = 0;
        for(
          Iterator i = this.objGetList("givenName").iterator();
          i.hasNext();
          ii++
        ) {
          if(ii > 0) {
            givenNames.append(" ");
          }
          givenNames.append((String)i.next());
        }

        List resultFields = new ArrayList();
        List resultValues = new ArrayList();
        String formattedName = this.objGetValue("salutation") + " " + givenNames.toString() + " " + this.objGetValue("lastName");
          resultFields.add("formattedName"); resultValues.add(formattedName);
          resultFields.add("formattedNameAsSet"); resultValues.add(new HashSet(Collections.singletonList(formattedName)));
        resultFields.add("formattedNameAsList"); resultValues.add(Collections.singletonList(formattedName));
        SortedMap formattedNameAsSparseArray = new TreeMap();
        formattedNameAsSparseArray.put(new Integer(0), formattedName);
        resultFields.add("formattedNameAsSparseArray"); resultValues.add(formattedNameAsSparseArray);
        return super.objectFactory.createStructure(
          "org:openmdx:test:app1:PersonFormatNameAsResult", 
          resultFields,
          resultValues
        );
      }
      
      // format not supported
      else {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("typeName", "org:openmdx:test:app1:Person:CanNotFormatNameException"),
            new BasicException.Parameter("formatType", params.objGetValue("type"))
          },
          "name format not supported. Supported are [Standard]"
        );
      }  
    }
    
    // assignAddress
    else if("assignAddress".equals(operation)) {
      List addresses = (List)params.objGetValue("address");
      System.out.println("assigning addresses=" + addresses);
      return super.objectFactory.createStructure(
        "org:openmdx:base:Void",
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST
      );
    }

    // voidOp
    else if("voidOp".equals(operation)){    
      for(
        Iterator i = objGetList("assignedAddress").iterator();
        i.hasNext();
      ) {
        Object_1_0 address = (Object_1_0)i.next();
        if("org:openmdx:test:app1:InternationalPostalAddress".equals(address.objGetClass())){
          int j = Arrays.asList(COUNTRY_NAME).indexOf(address.objGetValue("country"));
          if(j >= 0) {
            String countryPrefix = COUNTRY_CODE[j % COUNTRY_CODE.length] + '-';
            String postalCode = (String)address.objGetValue("postalCode");
            if(! postalCode.startsWith(countryPrefix)) address.objSetValue(
              "postalCode",
              countryPrefix + postalCode
            );
          }
        }
      }
      return super.objectFactory.createStructure(
        "org:openmdx:base:Void",
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST
      );
    }

    // dateOp
    else if("dateOp".equals(operation)){    
      System.out.println("dateOp.dateIn=" + params.objGetValue("dateIn"));
      System.out.println("dateOp.dateTimeIn=" + params.objGetValue("dateTimeIn"));
      return super.objectFactory.createStructure(
        "org:openmdx:test:app1:PersonDateOpResult",
        Arrays.asList(new String[]{"dateResult", "dateTimeResult"}),
        Arrays.asList(
          new Object[]{
            params.objGetValue("dateIn"),
            params.objGetValue("dateTimeIn")
          }
        )
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

  private final static String[] COUNTRY_CODE = new String[]{
    "AT", "DE", "CH"
  };
  private final static String[] COUNTRY_NAME = new String[]{
    "Austria", "Germany", "Switzerland", 
    "Österreich", "Deutschland", "Schweiz",
    "Autriche", "Allemagne", "Suisse",
    "Austria", "Germania", "Svizzera"
  };

}

//--- End of File ------------------------------------------------------------
