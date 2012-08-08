/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestExplorer_1.java,v 1.7 2007/12/14 15:11:22 hburger Exp $
 * Description: class TestExplorer_1
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/14 15:11:22 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.test.compatibility.runtime1;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.application.container.LightweightContainer_1;
import org.openmdx.compatibility.base.application.container.SimpleServiceLocator;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.deployment1.accessor.basic.DeploymentConfiguration_1;

public class TestExplorer_1 extends TestCase {
    
    /**
     * Constructs a test case with the given name.
     */
    public TestExplorer_1(
        String name
    ) {
        super(name);
    }

    /**
     * 
     * @throws ServiceException
     */
    private static void registerLightweightContainer(
    ){
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main(
      String[] args
    ){
        TestRunner.run(suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ) {
        return new TestSuite(TestExplorer_1.class);
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        if(! deployed){
            System.out.println("Deploying...");
            DeploymentConfiguration_1.createInstance(
                new String[]{
                    "xri://+resource/org/openmdx/test/deployment.configuration.xml",
                    "xri://+resource/org/openmdx/test/compatibility/runtime1/deployment.configuration.xml"
                }
            );
            new LightweightContainer_1(
                "testexplorer",
                CONNECTOR_DEPLOYMENT_UNITS,
                PROVIDER_DEPLOYMENT_UNITS
            );
            deployed = true;
        }
        System.out.println(">>>> **** Start Test: " + this.getName());
        AppLog.info("Start Test", this.getName());
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void tearDown(
    ) throws Exception {
        try {
          System.out.println("<<<< **** End Test: " + this.getName());
          AppLog.info("End test",this.getName());
        }
        catch(Exception e) {
          System.out.println("error in tearDown");
        }
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testExplorer(
    ) throws Throwable {
        try {
            Dataprovider_1_0Connection connection = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                SimpleServiceLocator.getInitialContext().lookup("org/openmdx/JUnit/NoOrNewTransaction/access")
            );
            RequestCollection requests = new RequestCollection(
                new ServiceHeader(),
                connection
            );
            requests.beginUnitOfWork(false);
            requests.addCreateRequest(
                createDataproviderObject(
                    DATA_CONTAINER.getDescendant(new String[]{"A","domain","A1"}),
                    DATA_CLASS
                )
            );
            requests.addCreateRequest(
                createDataproviderObject(
                    DATA_CONTAINER.getDescendant(new String[]{"A","domain","A2"}),
                    DATA_CLASS
                )
            );
            requests.addCreateRequest(
                createDataproviderObject(
                    DATA_CONTAINER.getDescendant(new String[]{"B","domain","B1"}),
                    DATA_CLASS
                )
            );
            List bDomains = requests.addFindRequest(
                new Path(DATA_CONTAINER.getDescendant(new String[]{"B","domain"})),
                null
            );
            List aDomains = requests.addFindRequest(
                new Path(DATA_CONTAINER.getDescendant(new String[]{"A","domain"})),
                null
            );
            requests.endUnitOfWork();
            assertEquals("aDomains.size()",2,aDomains.size());
            assertEquals("bDomains.size()",1,bDomains.size());
        } catch (ServiceException exception) {
            throw exception.log();
        }
    }
    
    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    static private boolean deployed = false;
    
    static private Path[] PROVIDER_DEPLOYMENT_UNITS = new Path[]{
        new Path("xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration/junit/domain/apps/deploymentUnit/testexplorer")
    };
    static private Path[] CONNECTOR_DEPLOYMENT_UNITS = new Path[]{
    };     

    private DataproviderObject createDataproviderObject(
        Path path,
        String objectClass
    ){
        DataproviderObject result = new DataproviderObject(path);
        result.values(SystemAttributes.OBJECT_CLASS).add(objectClass);
        return result;
    }
    
    private static Path DATA_CONTAINER = new Path(
        "xri:@openmdx:org.openmdx.deployment1/provider/org:openmdx/segment/org:openmdx:test/configuration"
    );

    private static String DATA_CLASS = "org:openmdx:deployment1:Domain";    
    
    static {
        registerLightweightContainer();
    }
    
}
