/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SegmentImpl 
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

import org.openmdx.base.aop2.AbstractObject;
import test.openmdx.app1.cci2.SegmentReferencesForgeignPerson;
import test.openmdx.app1.jmi1.Person;

/**
 * SegmentImpl
 */
public class SegmentImpl extends AbstractObject<test.openmdx.app1.jmi1.Segment,test.openmdx.app1.cci2.Segment,Void> {

    /**
     * Constructor 
     *
     * @param same the same layer JMI API
     * @param next the next layer CCI API
     */
    public SegmentImpl(
        test.openmdx.app1.jmi1.Segment same,
        test.openmdx.app1.cci2.Segment next
    ) {
        super(same, next);
    }
        
    /**
     * 
     */
    private transient SegmentReferencesForgeignPerson.ForeignPerson<Person> foreignPerson = null;

    /* (non-Javadoc)
     * @see test.openmdx.app1.cci2.Segment#getForeignPerson()
     */
    @SuppressWarnings("unchecked")
    public <T extends test.openmdx.app1.cci2.Person> SegmentReferencesForgeignPerson.ForeignPerson<T> getForeignPerson() {
        if(this.foreignPerson == null) {
            this.foreignPerson = new SegmentReferencesForeignPersonImpl(sameObject());   
        }
        return (SegmentReferencesForgeignPerson.ForeignPerson<T>) this.foreignPerson;
    }

}
