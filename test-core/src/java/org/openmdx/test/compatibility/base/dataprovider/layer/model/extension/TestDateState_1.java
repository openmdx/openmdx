/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestDateState_1.java,v 1.8 2007/11/19 17:22:06 hburger Exp $
 * Description: TestDateState_1 JUnit class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/19 17:22:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.test.compatibility.base.dataprovider.layer.model.extension;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.compatibility.base.naming.Path;

/**
 * Test State With Holes
 */
public class TestDateState_1
    extends AbstractTestDateState_1 
{

    /**
     * Constructor 
     *
     * @param name
     */
    public TestDateState_1(String name) {
        super(name);
    }

    /**
     * To run the test suite from the command line
     * 
     * @param args
     */
    public static void main(
        String[] args
    ){
        TestRunner.run(suite());
    }

    /**
     * Define the Test Suite
     * 
     * @return the test suite to be executed
     */
    public static Test suite(
    ) {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestDateState_1("lenientNone_CR20006291_newManagers"));
        suite.addTest(new TestDateState_1("lenientNone_CR20006291_sameManager"));        
        suite.addTest(new TestDateState_1("lenientJdbc_CR20006291_newManagers"));        
        suite.addTest(new TestDateState_1("lenientJdbc_CR20006291_sameManager"));        
        suite.addTest(new TestDateState_1("None_Standard"));
        suite.addTest(new TestDateState_1("None_WithoutHistory"));
        suite.addTest(new TestDateState_1("Jdbc_Standard"));
        suite.addTest(new TestDateState_1("Jdbc_WithoutHistory"));
        suite.addTest(new TestDateState_1("Jdbc_Standard_ExportImport"));
        suite.addTest(new TestDateState_1("Jdbc_WithoutHistory_ExportImport"));
        suite.addTest(new TestDateState_1("Jdbc_CR20006725"));
        return suite;
    }

    /**
     * Connector Deployment Unit Callback
     * 
     * @return the URLs of the connector deployment units
     */
    protected Path[] connectorDeploymentUnits(){
        return new Path[]{
            new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/extendedconnectors")
        };
    }
  
    /** 
     * Provider Deployment Unit Callback
     * 
     * @return the URLs of the provider deployment units
     */
    protected Path[] providerDeploymentUnits(){
        return new Path[]{
            new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/testdatestate")
        };
    }

    public void None_Standard(
    ) throws Throwable {
      testDateState(
          false, // supportsView
          true, // supportsHistory
          false, // readOnly
          false // xmlImport
      );
    }

    public void Jdbc_Standard(
    ) throws Throwable {
        testDateState(
            true, // supportsView
            true, // supportsHistory
            false, // xmlExport
            false // xmlImport
        );
    }

    public void Jdbc_Standard_ReadOnly(
    ) throws Throwable {
        testDateState(
            true, // supportsView
            true, // supportsHistory
            true, // xmlExport
            false // xmlImport
        );
    }

    public void Jdbc_Standard_ExportImport(
    ) throws Throwable {
        testDateState(
            true, // supportsView
            true, // supportsHistory
            true, // xmlExport
            true // xmlImport
        );
    }

    public void None_WithoutHistory(
    ) throws Throwable {
        testDateState(
            false, // supportsView
            false, // supportsHistory
            false, // xmlExport
            false // xmlImport
        );
    }

    public void Jdbc_WithoutHistory(
    ) throws Throwable {
        testDateState(
            true, // supportsView
            false, // supportsHistory
            false, // xmlExport
            false // xmlImport
        );
    }

    public void Jdbc_WithoutHistory_ReadOnly(
    ) throws Throwable {
        testDateState(
            true, // supportsView
            false, // supportsHistory
            true, // xmlExport
            false // xmlImport
        );
    }

    public void Jdbc_WithoutHistory_ExportImport(
    ) throws Throwable {
        testDateState(
            true, // supportsView
            false, // supportsHistory
            true, // xmlExport
            true // xmlImport
        );
    }

    /**
     * Date State View Test
     * 
     * @throws Exception
     */
    public void lenientNone_CR20006291_sameManager(
    ) throws Exception{
        testCR20006291(false);
    }
    
    /**
     * Date State View Test
     * 
     * @throws Exception
     */
    public void lenientNone_CR20006291_newManagers(
    ) throws Exception{
        testCR20006291(true);
    }
    
    /**
     * Date State View Test
     * 
     * @throws Exception
     */
    public void lenientJdbc_CR20006291_sameManager(
    ) throws Exception{
        testCR20006291(false);
    }
    
    /**
     * Date State View Test
     * 
     * @throws Exception
     */
    public void lenientJdbc_CR20006291_newManagers(
    ) throws Exception{
        testCR20006291(true);
    }

}
