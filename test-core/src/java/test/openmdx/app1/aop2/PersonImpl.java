/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Person 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package test.openmdx.app1.aop2;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.listener.StoreCallback;
import javax.jmi.reflect.RefObject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.aop2.AbstractObject;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.jmi1.Void;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SortedMaps;

import test.openmdx.app1.jmi1.Address;
import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.CanNotFormatNameException;
import test.openmdx.app1.jmi1.InternationalPostalAddress;
import test.openmdx.app1.jmi1.Person;
import test.openmdx.app1.jmi1.PersonAssignAddressParams;
import test.openmdx.app1.jmi1.PersonDateOpParams;
import test.openmdx.app1.jmi1.PersonDateOpResult;
import test.openmdx.app1.jmi1.PersonFormatNameAsParams;
import test.openmdx.app1.jmi1.PersonFormatNameAsResult;


/**
 * Person
 */
public class PersonImpl <S extends test.openmdx.app1.jmi1.Person, N extends test.openmdx.app1.cci2.Person, C extends Date>  
    extends AbstractObject<S,N,C> 
    implements StoreCallback, NaturalPerson
{

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public PersonImpl(
        S same,
        N next
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
        "\u00D6sterreich", "Deutschland", "Schweiz",
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
     * @see test.openmdx.app1.cci2.Person#getAge()
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
     * @see test.openmdx.app1.cci2.Person#formatNameAs(test.openmdx.app1.cci2.PersonFormatNameAsParams)
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
            return app1Package.createPersonFormatNameAsResult(
                asString,
                Collections.singletonList(asString),
                Collections.singleton(asString),
                SortedMaps.singletonSparseArray(asString)
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
     * @see test.openmdx.app1.cci2.Person#assignAddress(test.openmdx.app1.cci2.PersonAssignAddressParams)
     */
    public Void assignAddress(PersonAssignAddressParams in) {
        RefPackage_1_0 nextPackage = (RefPackage_1_0) ((RefObject)nextObject()).refOutermostPackage();
        @SuppressWarnings("unused")
        test.openmdx.app1.cci2.PersonAssignAddressParams nextInput = (test.openmdx.app1.cci2.PersonAssignAddressParams) nextPackage.refCreateStruct(in.refDelegate());
//        nextObject().assignAddress(nextInput);
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
     * @see test.openmdx.app1.cci2.Person#voidOp()
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
     * @see test.openmdx.app1.jmi1.Person#dateOp(test.openmdx.app1.cci2.PersonDateOpParams)
     */
    public PersonDateOpResult dateOp(PersonDateOpParams in) {
        App1Package app1Package = samePackage();
        System.out.println("dateOp.dateIn=" + in.getDateIn());
        System.out.println("dateOp.dateTimeIn=" + in.getDateTimeIn());
        return app1Package.createPersonDateOpResult(in.getDateIn(), in.getDateTimeIn());        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.AbstractObject#jdoPreStore()
     */
    @Override
    public void jdoPreStore() {
      //System.out.println(this.getClass().getName() + ".objPreStore"); 
      short sex = nextObject().getSex();
      String salutation = nextObject().getSalutation();
      if(
        0 == sex && 
        !"Herr".equals(salutation) && 
        !"Mister".equals(salutation) &&
        !"Monsieur".equals(salutation)
      ) {
        throw new RuntimeServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
          "sex 0 implies salutation [Herr|Mister|Monsieur]",
          new BasicException.Parameter("xri", super.sameObject().refGetPath()),
          new BasicException.Parameter("Sex", sex),
          new BasicException.Parameter("salutation", salutation)
        );
      }
      super.jdoPreStore();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.AbstractObject#newContext()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected C newContext() {
        return (C) new Date();
    }

    /* (non-Javadoc)
     * @see test.openmdx.app1.aop2.NaturalPerson#isRetired()
     */
//  @Override
    public boolean isRetired() {
        return false;
    }

}
