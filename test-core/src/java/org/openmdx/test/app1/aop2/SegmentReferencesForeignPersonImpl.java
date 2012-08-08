/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SegmentReferencesForeignPersonImpl.java,v 1.1 2009/02/04 11:06:37 hburger Exp $
 * Description: SegmentReferencesForgeignPerson 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/04 11:06:37 $
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
package org.openmdx.test.app1.aop2;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

import javax.jmi.reflect.WrongSizeException;

import org.openmdx.test.app1.cci2.PersonQuery;
import org.openmdx.test.app1.cci2.SegmentHasPerson;
import org.openmdx.test.app1.cci2.SegmentReferencesForgeignPerson;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.Person;
import org.openmdx.test.app1.jmi1.Segment;
import org.w3c.cci2.AnyTypePredicate;

/**
 * Association Implementation <code>SegmentReferencesForgeignPerson</code> 
 */
public class SegmentReferencesForeignPersonImpl
    extends AbstractCollection<Person>
    implements SegmentReferencesForgeignPerson.ForeignPerson<Person>
{
    
    /**
     * Constructor 
     *
     * @param delegate
     */
    SegmentReferencesForeignPersonImpl(
        Segment segment
    ){
        this.segment = segment;
    }
    
    private final Segment segment;
    
    private static final String UNMODIFIABLE = 
        "This shared assoication shall by unmodifiable by implementation";
    
    
    /* (non-Javadoc)
     * @see org.openmdx.test.app1.cci2.SegmentReferencesForgeignPerson.ForeignPerson#get(org.oasisopen.cci2.QualifierType, java.lang.String)
     */
    public Person get(
       org.oasisopen.cci2.QualifierType foreignIdType,
       java.lang.String foreignId
    ){
        PersonQuery predicate = ((App1Package)this.segment.refImmediatePackage()).createPersonQuery();
        predicate.foreignId().equalTo(foreignId);
        List<Person> result = this.segment.getPerson(predicate);
        if(result.isEmpty()) {
            return null;
        } else {
            Iterator<Person> i = result.iterator();
            Person person = i.next();
            if(i.hasNext()) throw new WrongSizeException(
                segment,
                "The segment has more than one person with the foreign id '" + foreignId + "'"
             );
            return person;
        }
    }

     /* (non-Javadoc)
      * @see org.openmdx.test.app1.cci2.SegmentReferencesForgeignPerson.ForeignPerson#add(org.oasisopen.cci2.QualifierType, java.lang.String, java.lang.Object)
      */
     public void add(
       org.oasisopen.cci2.QualifierType foreignIdType,
       java.lang.String foreignId,
       Person foreignPerson
     ){
         throw new UnsupportedOperationException(UNMODIFIABLE);
     }

     /* (non-Javadoc)
      * @see org.openmdx.test.app1.cci2.SegmentReferencesForgeignPerson.ForeignPerson#remove(org.oasisopen.cci2.QualifierType, java.lang.String)
      */
     public void remove(
       org.oasisopen.cci2.QualifierType foreignIdType,
       java.lang.String foreignId
     ){
         throw new UnsupportedOperationException(UNMODIFIABLE);
     }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<Person> iterator() {
        SegmentHasPerson.Person<Person> collection = this.segment.getPerson();
        return collection.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        SegmentHasPerson.Person<Person> collection = this.segment.getPerson();
        return collection.size();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#getAll(org.w3c.cci2.AnyTypePredicate)
     */
    public List<Person> getAll(AnyTypePredicate predicate) {
        SegmentHasPerson.Person<Person> collection = this.segment.getPerson();
        return collection.getAll(predicate);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#removeAll(org.w3c.cci2.AnyTypePredicate)
     */
    public void removeAll(AnyTypePredicate predicate) {
        SegmentHasPerson.Person<Person> collection = this.segment.getPerson();
        collection.removeAll(predicate);
    }

}
