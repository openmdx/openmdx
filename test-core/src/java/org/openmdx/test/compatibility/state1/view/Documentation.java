/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Documentation.java,v 1.1 2008/08/20 23:45:07 hburger Exp $
 * Description: Documentation 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/20 23:45:07 $
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

package org.openmdx.test.compatibility.state1.view;

import java.text.ParseException;
import java.util.Collection;
import java.util.Set;

import javax.jmi.reflect.RefObject;
import javax.xml.datatype.DatatypeFactory;

import junit.framework.TestCase;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.state1.cci.DateState;
import org.openmdx.compatibility.state1.view.DateStateContext;
import org.openmdx.compatibility.state1.view.DateStateViews;
import org.slf4j.Logger;

/**
 * Documentation
 *
 */
public class Documentation extends TestCase {
    
    DatatypeFactory datatypeFactory = null;
    
    public void context(){
 
        RefObject refObject = null; 
        Logger logger = null;
        
        DateStateContext context = DateStateViews.getContext(refObject);
        if(context == null) {
            logger.warn("The object is not accessed through a date state view!");
        } else if(context.isWritable()) {
            logger.info("Time Range View {}/{}", context.getValidFrom(), context.getValidTo());
        } else {
            logger.info("Time Point View {}@{}", context.getValidFor(), context.getValidAt());
        }
        
    }
    
    public void view() throws ParseException{
        DateState a = null;
        {
            DateState v = (DateState) DateStateViews.getView(
                a,
                datatypeFactory.newXMLGregorianCalendar("2008-02-01"),
                DateFormat.getInstance().parse("20061232T170000.000Z")
            );
            Set<String> b = null; // v.getB();
            assertEquals(1, b.size());
            assertTrue(b.contains("b1"));
        }
        {
            DateState v = (DateState) DateStateViews.getViewForTimeRange(
                a, 
                datatypeFactory.newXMLGregorianCalendar("2008-02-01"), 
                datatypeFactory.newXMLGregorianCalendar("2008-03-31")
            );
            Set<String> b = null; // v.getB();
            b.add("b2");
        }
        {
            DateState i = (DateState) DateStateViews.getView(
                a, 
                datatypeFactory.newXMLGregorianCalendar("2008-02-01"),
                DateFormat.getInstance().parse("20061232T170000.000Z")
            );
            RefObject v = DateStateViews.getViewForPropagatedState(
                i,
                datatypeFactory.newXMLGregorianCalendar("2008-02-01"), 
                datatypeFactory.newXMLGregorianCalendar("2008-03-31")
            );
        }
        {
            DateState c = (DateState) DateStateViews.getView(
                a, 
                datatypeFactory.newXMLGregorianCalendar("2008-02-01")
            );
            DateState l = (DateState) DateStateViews.getViewForContiguousStates(
                c
            ); 
        }
        {
            Collection<DateState> r = null; // p.getA();
            Collection<DateState> s = DateStateViews.getStates(
                r, 
                "a0", 
                Boolean.FALSE, // invalidated, 
                Boolean.FALSE // deleted
            );
            s = DateStateViews.getStates(
                r, 
                "a0", 
                Boolean.FALSE, // invalidated, 
                Boolean.FALSE // deleted
            );            
            s = DateStateViews.getStates(
                r, 
                "a0", 
                datatypeFactory.newXMLGregorianCalendar("2008-01-01"), // validFrom
                datatypeFactory.newXMLGregorianCalendar("2008-12-31") // validTo
            );            
        }
        {
            DateState r = (DateState) DateStateViews.getViewForTimeRange(
                a, 
                datatypeFactory.newXMLGregorianCalendar("2008-03-15"), 
                datatypeFactory.newXMLGregorianCalendar("2008-05-15")
            );
            r.refDelete();
        }
    }
        
}
