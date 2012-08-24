/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: TestFlushing 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package test.openmdx.base.accessor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.FlushOrder;
import org.openmdx.base.accessor.spi.DelegatingObject_1;
import org.openmdx.base.naming.Path;

/**
 * Test Flushing
 */
public class TestFlushing {
    
    private List<DataObject_1_0> members;
    
    @Before
    public void setUp(){
        this.members = new ArrayList<DataObject_1_0>(
            Arrays.asList(
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::prozessinformation1/provider/shark/segment/css/prozessinformation/ref-JYMSMuTNJQ/arbeitsschritt/1/ausfuehrungsEintrag/b14fa58c-ecef-11df-bcc9-b724e704324d","persistent-dirty"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::prozessinformation1/provider/shark/segment/css/prozessinformation/ref-JYMSMuTNJQ/arbeitsschritt/1/ausfuehrungsEintrag/b14fa58b-ecef-11df-bcc9-b724e704324d","persistent-dirty"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::prozessinformation1/provider/shark/segment/css/prozessinformation/ref-JYMSMuTNJQ/arbeitsschritt/1/ausfuehrungsEintrag/b14fa58a-ecef-11df-bcc9-b724e704324d","persistent-dirty"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00001","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00002","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00003","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00004","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/shark/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/1","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/shark/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/2","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/shark/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/3","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/shark/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/4","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/shark/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::prozessinformation1/provider/shark/segment/css/prozessinformation/ref-JYMSMuTNJQ/arbeitsschritt/1 ","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ!LQZUMQDTGH0PRXA1J6I1T9EFH","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00003!LQZUMQNOM1ALBXA1J6I1T9EFH","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00001!LQZUMQNOM1ALBXA1J6I1T9EFH","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00004!LQZUMQNOM1ALBXA1J6I1T9EFH","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00002!LQZUMQNOM1ALBXA1J6I1T9EFH","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ!LQZUMQNOM1ALBXA1J6I1T9EFH","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/leistungsempfaengerabrechnung/ref-JYMSMuTNJQ/leistungsempfaengerabrechnungteil/b14fa592-ecef-11df-bcc9-b724e704324d","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/leistungsempfaengerabrechnung/ref-JYMSMuTNJQ/leistungsempfaengerabrechnungteil/b14fa591-ecef-11df-bcc9-b724e704324d","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/leistungsempfaengerabrechnung/ref-JYMSMuTNJQ","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::vorgang1/provider/shark/segment/css/vorgangstracker/ref-JYMSMuTNJQ","persistent-dirty"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00001","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00002","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00003-JYMSMuTNJQ/mutationslauf/00003","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00004","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/2-2","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/3-3","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/1-1","persistent-deleted"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/shark/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/4-4","persistent-deleted"),
                new DataObject_1("org::openmdx::audit2/provider/audit/segment/css","persistent-clean"),
                new DataObject_1("org::openmdx::audit2/provider/audit/segment/css/unitOfWork/LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::vorgang1/provider/audit/segment/css/vorgangstracker/ref-JYMSMuTNJQ!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00003!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/audit/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/2!LQZUMR3GUXQE7XA1J6I1T9EFH ","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00001!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00003!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/1-1!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/leistungsempfaengerabrechnung/ref-JYMSMuTNJQ/leistungsempfaengerabrechnungteil/b14fa591-ecef-11df-bcc9-b724e704324d!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/audit/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/3!LQZUMR3GUXQE7XA1J6I1T9EFH ","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/3-3!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00002!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00001!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/leistungsempfaengerabrechnung/ref-JYMSMuTNJQ!LQZUMR3GUXQE7XA1J6I1T9EFH ","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/leistungsempfaengerabrechnung/ref-JYMSMuTNJQ/leistungsempfaengerabrechnungteil/b14fa592-ecef-11df-bcc9-b724e704324d!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/abrechnungsposition/00004!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/audit/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d!LQZUMR3GUXQE7XA1J6I1T9EFH ","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/audit/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/1!LQZUMR3GUXQE7XA1J6I1T9EFH ","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::rechnung1/provider/audit/segment/css/beleg/b79b6e80-ecef-11df-bcc9-b724e704324d/rechnungsposition/4!LQZUMR3GUXQE7XA1J6I1T9EFH ","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00002!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/2-2!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/mutationslauf/00004!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new"),
                new DataObject_1("ch::css::versicherung::krankenversicherung::heilungskosten::abrechnung1/provider/audit/segment/css/abrechnung/ref-JYMSMuTNJQ/buchung/4-4!LQZUMR3GUXQE7XA1J6I1T9EFH","persistent-new")
            )
        );
    }
    
    /**
     * Tells whether adjacent members are ordered
     * @param comparator TODO
     * 
     * @return <code>true</code> if adjacent members are ordered
     */
    private boolean isPartiallyOrdered(Comparator<DataObject_1_0> comparator){
        for(int i = 0, iLimit = this.members.size() - 1; i < iLimit; i++) {
            if(comparator.compare(this.members.get(i), this.members.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Tells whether the members are ordered
     * @param comparator TODO
     * 
     * @return <code>true</code> if the members are ordered
     */
    private boolean isCompletelyOrdered(Comparator<DataObject_1_0> comparator){
        for(int i = 0, iLimit = this.members.size() - 1; i < iLimit; i++) {
            for(int j = i + 1, jLimit = this.members.size(); j < jLimit; j++) {
                if(comparator.compare(this.members.get(i), this.members.get(j)) > 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Test flushing
     */
    @Test
    public void testFlushing(){
        assertTrue("Former Precondition", isPartiallyOrdered(IncompleteOrder.getInstance()));
        assertFalse("Actual Precondition", isPartiallyOrdered(FlushOrder.getInstance()));
        assertFalse("Former Precondition", isCompletelyOrdered(IncompleteOrder.getInstance()));
        assertFalse("Actual Precondition", isCompletelyOrdered(FlushOrder.getInstance()));
        Collections.sort(this.members, FlushOrder.getInstance());
        assertTrue("Former Postcondition", isPartiallyOrdered(IncompleteOrder.getInstance()));
        assertTrue("Actual Postcondition", isPartiallyOrdered(FlushOrder.getInstance()));
        assertTrue("Former Postcondition", isCompletelyOrdered(IncompleteOrder.getInstance()));
        assertTrue("Actual Postcondition", isCompletelyOrdered(FlushOrder.getInstance()));
    }
    
    /**
     * A test data object
     */
    private static class DataObject_1 extends DelegatingObject_1 {

        /**
         * Constructor 
         *
         * @param jdoObjectId
         * @param jdoStatus
         */
        DataObject_1(
            String jdoObjectId,
            String jdoStatus
        ){
            this(
                new Path(jdoObjectId),
                jdoStatus.indexOf("-clean") < 0,
                jdoStatus.indexOf("-new") > 0,
                jdoStatus.indexOf("-deleted") > 0
            );
        }
        
        /**
         * Constructor 
         *
         * @param jdoObjectId
         * @param jdoNew
         * @param jdoDirty
         * @param jdoDeleted
         */
        private DataObject_1(
            Path jdoObjectId,
            boolean jdoDirty,
            boolean jdoNew,
            boolean jdoDeleted
         ){
            super();
            this.jdoObjectId = jdoObjectId;
            this.jdoDirty = jdoDirty;
            this.jdoNew = jdoNew;
            this.jdoDeleted = jdoDeleted;
        }

        private final Path jdoObjectId;
        private final boolean jdoDirty;
        private final boolean jdoNew;
        private final boolean jdoDeleted;
        
        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
         */
//      @Override
        public PersistenceManager jdoGetPersistenceManager(
        ) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoGetObjectId()
         */
        @Override
        public Path jdoGetObjectId() {
            return this.jdoObjectId;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoIsDetached()
         */
        @Override
        public boolean jdoIsDetached() {
            return false;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoIsDirty()
         */
        @Override
        public boolean jdoIsDirty() {
            return this.jdoDirty;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoIsPersistent()
         */
        @Override
        public boolean jdoIsPersistent() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoIsTransactional()
         */
        @Override
        public boolean jdoIsTransactional() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoIsDeleted()
         */
        @Override
        public boolean jdoIsDeleted() {
            return this.jdoDeleted;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.DelegatingObject_1#jdoIsNew()
         */
        @Override
        public boolean jdoIsNew() {
            return this.jdoNew;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.AbstractDataObject_1#toString()
         */
        @Override
        public String toString() {
            return this.jdoObjectId.toXRI() + " (" + JDOHelper.getObjectState(this)+")";
        }

    }

}
