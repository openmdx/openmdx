/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestTestApp_1Jmi.java,v 1.75 2006/04/18 14:49:39 hburger Exp $
 * Description: Unit test for model app1
 * Revision:    $Revision: 1.75 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/04/18 14:49:39 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
package org.openmdx.test.test.app1;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
//---------------------------------------------------------------------------  
/**
 * 
 */
public class TestTestApp_1Jmi
  extends AbstractTestApp_1Jmi 
{

    /**
     * Constructor 
     *
     * @param name
     */
    public TestTestApp_1Jmi(
        String name
    ) {
        super(name);
    }

    /**
     * LightweightContainer is only required in case of in-process deployment. In
     * case of EJB deployment main() is not invoked.
     */
    public static void main(
        String[] args
    ){
        TestRunner.run(suite());
    }

    /**
     * 
     * @return
     */
    public static Test suite(
    ) {
        TestSuite suite = new TestSuite();
        for(
            Iterator i = TestTestApp_1Jmi.testNames.iterator();
            i.hasNext();
        ) {
            suite.addTest(new TestTestApp_1Jmi((String)i.next()));
        }
        return suite;
    }

    /**
     * 
     */
    static private List testNames = Arrays.asList(
      new String[]{
        "JmiNone",
        "JmiJdbc"
      }
    );

}
