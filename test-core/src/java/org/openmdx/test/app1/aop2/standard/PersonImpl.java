/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersonImpl.java,v 1.2 2008/04/25 15:33:07 hburger Exp $
 * Description: Person 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/25 15:33:07 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.test.app1.aop2.standard;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.aop2.standard.AbstractObject;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.jmi1.Void;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.test.app1.jmi1.Address;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.CanNotFormatNameException;
import org.openmdx.test.app1.jmi1.InternationalPostalAddress;
import org.openmdx.test.app1.jmi1.Person;
import org.openmdx.test.app1.jmi1.PersonAssignAddressParams;
import org.openmdx.test.app1.jmi1.PersonDateOpParams;
import org.openmdx.test.app1.jmi1.PersonDateOpResult;
import org.openmdx.test.app1.jmi1.PersonFormatNameAsParams;
import org.openmdx.test.app1.jmi1.PersonFormatNameAsResult;
import org.w3c.cci2.SparseArray;


/**
 * Person
 */
public class PersonImpl extends AbstractObject {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public PersonImpl(
        org.openmdx.test.app1.jmi1.Person same,
        org.openmdx.test.app1.cci2.Person next
    ) {
        super(same, next);
    }

    /**
     * Postal code for countries
     */
    private final static String[] COUNTRY_CODE = new String[]{
        "AT", "DE", "CH"
    };
    
    private final static String[] COUNTRY_NAME = new String[]{
        "Austria", "Germany", "Switzerland", 
        "Österreich", "Deutschland", "Schweiz",
        "Autriche", "Allemagne", "Suisse",
        "Austria", "Germania", "Svizzera"
    };

    /**
     * Age Calculation
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.cci2.Person#getAge()
     */
    public short getAge() {
        Person same = sameObject();
        XMLGregorianCalendar birthdate = same.getBirthdate();
        if(birthdate == null) {
            return - 1;
        } else {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return (short) (currentYear - birthdate.getYear());
        }
    }

    /**
     * Format Name Operation
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @throws CanNotFormatNameException
     * 
     * @see org.openmdx.test.app1.cci2.Person#formatNameAs(org.openmdx.test.app1.cci2.PersonFormatNameAsParams)
     */
    public PersonFormatNameAsResult formatNameAs(
        PersonFormatNameAsParams in
    ) throws CanNotFormatNameException {
        // default format "Standard"
        Person same = sameObject();
        String formatType = in.getType();
        App1Package app1Package = samePackage();
        if((formatType == null) || "Standard".equals(formatType)) {
            StringBuilder formattedName = new StringBuilder(
                same.getSalutation()
            );
            for(String givenName : same.getGivenName()) {
                formattedName.append(
                    ' '
                ).append(
                    givenName
                );
            }
            formattedName.append(
                same.getLastName()
            );
            String asString = formattedName.toString();
            SparseArray<String> asSparseArray = new TreeSparseArray<String>();
            asSparseArray.put(0, asString);
            return app1Package.createPersonFormatNameAsResult(
                asString,
                Collections.singletonList(asString),
                Collections.singleton(asString),
                asSparseArray
            );
        } else throw new CanNotFormatNameException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "name format not supported. Supported are [Standard]",
            formatType
        );
    }

    /**
     * Assign Address Operation Doing Nothing
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.cci2.Person#assignAddress(org.openmdx.test.app1.cci2.PersonAssignAddressParams)
     */
    public Void assignAddress(PersonAssignAddressParams in) {
        System.out.println("Assigning addresses to " + sameObject().refMofId() + ": " + in.getAddress());
//        List<Address> target = this.same.getAssignedAddress();
//        List<Address> source = in.getAddress();
//        List<Address> set = new ArrayList<Address>(source);
//        set.removeAll(target);
//        target.addAll(set);
        return newVoid();
    }

    /**
     * Void Operation
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.cci2.Person#voidOp()
     */
    public Void voidOp() {
        Person same = sameObject();
        List<Address> assignedAddresses = same.getAssignedAddress(); 
        for(Address address : assignedAddresses) {
            if(address instanceof InternationalPostalAddress){
                InternationalPostalAddress postalAddress = (InternationalPostalAddress)address;
                int j = Arrays.asList(COUNTRY_NAME).indexOf(postalAddress.getCountry());
                if(j >= 0) {
                      String countryPrefix = COUNTRY_CODE[j % COUNTRY_CODE.length] + '-';
                      String postalCode = postalAddress.getPostalCode();
                      if(! postalCode.startsWith(countryPrefix)) {
                          postalAddress.setPostalCode(countryPrefix + postalCode);
                      }
                      
                }
                
            }
        }
        return newVoid();
    }

    
    /**
     * Date Operation
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.jmi1.Person#dateOp(org.openmdx.test.app1.cci2.PersonDateOpParams)
     */
    public PersonDateOpResult dateOp(PersonDateOpParams in) {
        App1Package app1Package = samePackage();
        System.out.println("dateOp.dateIn=" + in.getDateIn());
        System.out.println("dateOp.dateTimeIn=" + in.getDateTimeIn());
        return app1Package.createPersonDateOpResult(in.getDateIn(), in.getDateTimeIn());        
    }

    
//    public void preStore(InstanceCallbackEvent event) throws ServiceException {
//        //System.out.println(this.getClass().getName() + ".objPreStore"); 
//        int sex = ((Number)super.objGetValue("sex")).intValue();
//        String salutation = (String)super.objGetValue("salutation");
//        if((0 == sex) && !("Herr".equals(salutation) || "Mister".equals(salutation) || "Monsieur".equals(salutation))) {
//          throw new ServiceException(
//            BasicException.Code.DEFAULT_DOMAIN,
//            BasicException.Code.ASSERTION_FAILURE, 
//            new BasicException.Parameter[]{
//              new BasicException.Parameter("object", this)
//            },
//            "sex 0 implies salutation [Herr|Mister|Monsieur]"
//          );
//        }
//    }

}
