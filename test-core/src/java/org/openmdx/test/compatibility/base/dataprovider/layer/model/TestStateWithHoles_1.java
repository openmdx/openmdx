/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestStateWithHoles_1.java,v 1.46 2007/02/02 13:59:22 hburger Exp $
 * Description: TestState_1 junit class
 * Revision:    $Revision: 1.46 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/02/02 13:59:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.test.compatibility.base.dataprovider.layer.model;

import org.openmdx.compatibility.base.naming.Path;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test State With Holes
 */
public class TestStateWithHoles_1
    extends AbstractTestStateWithHoles_1 
{

    /**
     * Constructor 
     *
     * @param name
     */
    public TestStateWithHoles_1(String name) {
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
        suite.addTest(new TestStateWithHoles_1("None_Standard"));
        suite.addTest(new TestStateWithHoles_1("Jdbc_Standard"));
        suite.addTest(new TestStateWithHoles_1("Jdbc_WithoutHistory"));
        suite.addTest(new TestStateWithHoles_1("None_WithoutHistory"));
        return suite;
    }

    /**
     * Connector Deployment Unit Callback
     * 
     * @return the URLs of the connector deployment units
     */
    protected Path[] connectorDeploymentUnits(){
        return new Path[]{
            new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/connectors")
        };
    }
  
    /** 
     * Provider Deployment Unit Callback
     * 
     * @return the URLs of the provider deployment units
     */
    protected Path[] providerDeploymentUnits(){
        return new Path[]{
            new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/teststate")
        };
    }

}
