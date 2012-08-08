/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestOracle_1.java,v 1.4 2009/01/12 17:50:59 wfro Exp $
 * Description: Test Oracle
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 17:50:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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

import java.io.Closeable;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openmdx.application.dataprovider.deployment.Deployment_1;

/**
 * Test Oracle
 */
public class TestOracle_1 extends AbstractExtensionTest_1 {

    /**
     * The ORACLE connector URI
     */
    private final static String CONNECTOR_URI = "file:../test-core/src/connector/openmdx-2/oracle-10g.rar";

    @BeforeClass
    public static void createPersistenceManagerFactory(
    ){
        managerFactory = new Deployment_1(
            IN_PROCESS,
            CONNECTOR_URI,
            APPLICATION_URI,
            LOG_DEPLOYMENT_DETAIL,
            ENTITY_MANAGER_FACTORY_JNDI_NAME,
            GATEWAY_JNDI_NAME,
            MODEL
        );
    }

    @AfterClass
    public static void closePersistenceManagerFactory(
    ) throws IOException{
        if(managerFactory instanceof Closeable) {
            ((Closeable)managerFactory).close();
        }
    }

}
