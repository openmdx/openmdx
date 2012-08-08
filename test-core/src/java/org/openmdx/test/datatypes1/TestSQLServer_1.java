/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestSQLServer_1.java,v 1.1 2007/01/16 15:51:18 hburger Exp $
 * Description: TestOracle_1 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/16 15:51:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.test.datatypes1;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;

/**
 * TestOracle_1
 */
public class TestSQLServer_1
    extends AbstractExtensionTest_1
{

    /**
     * Constructor 
     *
     * @param name
     */
    public TestSQLServer_1(String name) {
        super(name);
    }

    /**
     * Launch the test suite from command line
     * 
     * @param args
     */
    public static void main(
        String[] args
    ){
        TestRunner.run(suite());
    }

    /**
     * Retrieve the test suite
     * 
     * @return this class' test suite
     */
    public static Test suite(
    ){
        return new TestSuite(TestSQLServer_1.class);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.test.datatypes1.TestExtension_1#getConnectionFactory()
     */
    protected Dataprovider_1ConnectionFactory getConnectionFactory() {
        return TestSQLServer_1.deployment;
    }

    /**
     * The ORACLE connector URI
     */
    private final static String CONNECTOR_URI = 
        "file:src/connector/openmdx-2/sql-server-2005.rar";
        
    /**
     * Oracle in-Process deployment with lazy initialization
     */
    protected final static Dataprovider_1ConnectionFactory deployment = new Dataprovider_1Deployment(
        new InProcessDeployment(
            CONNECTOR_URI,
            APPLICATION_URI,
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
            System.err
        ),
        models,
        JNDI_NAME
    );    

}
