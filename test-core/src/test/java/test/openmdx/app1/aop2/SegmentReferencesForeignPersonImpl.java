/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SegmentReferencesForgeignPerson 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

import javax.jmi.reflect.WrongSizeException;

import org.openmdx.base.aop2.PlugInContexts;
import org.w3c.cci2.AnyTypePredicate;

import test.openmdx.app1.cci2.PersonQuery;
import test.openmdx.app1.cci2.SegmentReferencesForgeignPerson;
import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.Person;
import test.openmdx.app1.jmi1.Segment;

	import java.util.function.Consumer; 

/**
 * Association Implementation {@code SegmentReferencesForgeignPerson} 
 */
public class SegmentReferencesForeignPersonImpl
    extends AbstractCollection<Person>
    implements SegmentReferencesForgeignPerson.ForeignPerson<Person>
{
    
    /**
     * Constructor 
     *
     * @param segment the {@code Segment}'s same layer JMI API
     */
    SegmentReferencesForeignPersonImpl(
        Segment segment
    ){
        this.segment = segment;
    }
    
    private final Segment segment;
    
    private static final String UNMODIFIABLE =  "This implementation shall make the shared assoication unmodifiable";
    
    
    /* (non-Javadoc)
     * @see test.openmdx.app1.cci2.SegmentReferencesForgeignPerson.ForeignPerson#get(org.oasisopen.cci2.QualifierType, java.lang.String)
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
            if(i.hasNext()) {
                throw new WrongSizeException(
                    segment,
                    "The segment has more than one person with the foreign id '" + foreignId + "'"
                 );
            } else {
            	//
            	// CR20019630
            	//
                #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif date = PlugInContexts.uncheckedGetPlugInContext(person, PersonImpl.class);
                System.out.println(
                    "The context of the person with the foreign id '" + foreignId + "' has been established at " + date
                );
            }
            return person;
        }
    }

     /* (non-Javadoc)
      * @see test.openmdx.app1.cci2.SegmentReferencesForgeignPerson.ForeignPerson#add(org.oasisopen.cci2.QualifierType, java.lang.String, java.lang.Object)
      */
     public void add(
       org.oasisopen.cci2.QualifierType foreignIdType,
       java.lang.String foreignId,
       Person foreignPerson
     ){
         throw new UnsupportedOperationException(UNMODIFIABLE);
     }

     /* (non-Javadoc)
      * @see test.openmdx.app1.cci2.SegmentReferencesForgeignPerson.ForeignPerson#remove(org.oasisopen.cci2.QualifierType, java.lang.String)
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
        return this.segment.<Person>getPerson().iterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return this.segment.<Person>getPerson().size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.segment.<Person>getPerson().isEmpty();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#getAll(org.w3c.cci2.AnyTypePredicate)
     */
    public List<Person> getAll(AnyTypePredicate predicate) {
        return this.segment.<Person>getPerson().getAll(predicate);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#removeAll(org.w3c.cci2.AnyTypePredicate)
     */
    public void removeAll(AnyTypePredicate predicate) {
        this.segment.<Person>getPerson().removeAll(predicate);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#processAll(org.w3c.cci2.AnyTypePredicate, java.util.function.Consumer)
     */
    @Override
    public void processAll(
        AnyTypePredicate predicate,
        Consumer<Person> consumer
    ) {
        this.segment.<Person>getPerson().processAll(predicate, consumer);
    }

}
