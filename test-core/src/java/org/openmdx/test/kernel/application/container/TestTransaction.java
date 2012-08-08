/*
 * ====================================================================
 * Name:        $Id: TestTransaction.java,v 1.17 2008/11/16 22:21:38 hburger Exp $
 * Description: Lightweight container transaction management test
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/16 22:21:38 $
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
package org.openmdx.test.kernel.application.container;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.Transaction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.application.cci.Model_1Deployment;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.ProductGroup;
import org.openmdx.test.app1.jmi1.Segment;

/**
 * Test the lightweight container's transaction management
 */
public class TestTransaction extends TestCase {

    /**
     * Constructor
     * 
     * @param name
     */
    public TestTransaction(
        String name
    ){
        super(name);
    }  
  
    /**
     * Main
     * 
     * @param args arguments
     */
    public static void main(
        String[] args
    ){
        TestRunner.run(suite());
    }
    
    /**
     * Define Test Suite
     * 
     * @return the test suite
     */
    public static Test suite(
    ) {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestTransaction("transactional_prolog"));
        suite.addTest(new TestTransaction("transactional_ejb-2.0"));
        suite.addTest(new TestTransaction("transactional_ejb-2.1"));
//      suite.addTest(new TestTransaction("non-transactional_ejb-2.0"));
//      suite.addTest(new TestTransaction("non-transactional_ejb-2.1"));
        return suite;
    }
  
    /**
     * Set-up
     */
    protected synchronized void setUp(
    ) throws Exception {
        System.out.println("Creating connection to " + this.getName() + "...");
        this.transactional = getName().startsWith("transactional_");
        this.schema = !getName().endsWith("_ejb-2.0");
        this.dataprovider = new Dataprovider_1Deployment(
            dataproviderDeployment,
            modelDeployment,
            this.schema ? "org/openmdx/test-ejb-2_1/gateway1/NoOrNew" : "org/openmdx/test/gateway1/NoOrNew"
        );
        this.provider1 = new Provider_1(
            new RequestCollection(
                new ServiceHeader(
                    PRINCIPALS[0],
                    this.getName(),
                    false, // traceRequest,
                    new QualityOfService()
                ),
                dataprovider.createConnection()
            ),
            this.transactional
        );
        Segment segment = getSegment();
        Transaction unitOfWork = JDOHelper.getPersistenceManager(this.provider).currentTransaction();

        if(segment != null) {
            unitOfWork.begin();
            segment.refDelete();
            unitOfWork.commit();
        }
        unitOfWork.begin();
        segment = app1.getSegment().createSegment();
        this.provider.addSegment(false, getName(), segment);
        unitOfWork.commit();
    }

    /**
     * Tear-down
     */
    protected void tearDown(
    ) {
        System.out.println("Tearing down " + this.getName() + "...");
    }

    /**
     * Return the segment
     * 
     * @return the (maybe newly created) segment
     * @throws ServiceException  
     */
    private Segment getSegment(
    ) throws ServiceException {
        ObjectFactory_1_4 manager = new Manager_1(
            new Connection_1(
                this.provider1,
                false // containerManagedUnitOfWork
           )
        ); 
        manager.setModel(new Model_1());
        RefPackage_1_1 rootPkg = new RefRootPackage_1(
            manager,
            null, // packageImpls
            null, // context
            null // binding
        );
        this.provider = (Provider) rootPkg.refPersistenceManager().getObjectById(
            Provider.class,
            new Path(PROVIDER_PATH)
        );
        this.app1 = (App1Package)rootPkg.refPackage(
            "org:openmdx:test:app1"
        );
        try {
            return (Segment) this.provider.getSegment(getName());
        } catch (JmiServiceException exception) {
            if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return null;
            } else {
                throw new ServiceException(exception);
            }
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    protected void runTest() throws Throwable {
        Segment segment = getSegment();
        Transaction unitOfWork = JDOHelper.getPersistenceManager(provider).currentTransaction();
        try {
            unitOfWork.begin();
            ProductGroup productGroup = app1.getProductGroup().createProductGroup();
            productGroup.setDescription("");
            segment.addProductGroup(false, "ko", productGroup);
            productGroup = app1.getProductGroup().createProductGroup();
            productGroup.setDescription("Ok");
            segment.addProductGroup(false, "ok", productGroup);
            unitOfWork.commit();
            fail("Empty description should have been rejected");
        } catch (NullPointerException prologException) {
            if(getName().endsWith("prolog")) {
                return; // TODO find the reason for this exception
            } else {
                throw prologException;
            }
        } catch (JDOFatalDataStoreException jdoException) {
            ServiceException exception = (ServiceException) jdoException.getCause();
            if(this.transactional) {
                assertEquals(
                    "Rollback", 
                    BasicException.Code.ROLLBACK, 
                    exception.getExceptionCode()
                 );
            } else {
                assertEquals(
                    "Abort", 
                    BasicException.Code.ABORT, 
                    exception.getExceptionCode()
                );
                segment = getSegment();
            }
        }
        unitOfWork.begin();
        ProductGroup productGroup = app1.getProductGroup().createProductGroup();
        productGroup.setDescription("Ok");
        segment.addProductGroup(false, "ok", productGroup);
        unitOfWork.commit();
    }
    
    /**
     * 
     */
    protected Provider_1_0 provider1;
    
    /**
     * 
     */
    protected App1Package app1;

    /**
     * 
     */
    protected Provider provider;
    
    /**
     * 
     */
    protected boolean transactional;

    /**
     * <code>false</code> in case of EJB 2.0, <code>true</code> otherweise.
     */
    protected boolean schema;
    
    /**
     * 
     */
    private static final String PROVIDER_PATH = "xri:@openmdx:org.openmdx.test.app1/provider/ch:omex:test:junit";
    
    /**
     * The JUnit principal
     */
    static protected final String[] PRINCIPALS = {"anonymous", "authenticated"};
    
    /**
     * Define whether deployment details should logged to the console
     */
    final private static boolean LOG_DEPLOYMENT_DETAIL = false;

    private final static String CONNECTOR_URL = 
//      "file:../test-core/src/connector/openmdx-2/sql-server-2000.rar";
        "file:../test-core/src/connector/openmdx-2/sql-server-2005.rar";
//      "file:../test-core/src/connector/openmdx-xa/sql-server-2000.rar";
//      "file:../test-core/src/connector/openmdx-xa/sql-server-2005.rar";
//      "file:../test-core/src/connector/openmdx-xa/oracle-8.rar";
    final private static String DTD_BASED_APPLICATION_URL = 
        "file:../test-core/src/ear/test-transaction.ear";
    final private static String SCHEMA_BASED_APPLICATION_URL = 
        "file:../test-core/src/ear/test-ejb_2_1.ear";
    
    /**
     * The model deployment is shared
     */
    final private static Deployment modelDeployment = new Model_1Deployment(
        new String[]{
            "org:openmdx:base",
            "org:openmdx:state2",
            "org:openmdx:generic1",
            "org:openmdx:test:app1",
            "org:w3c"
        }
    );    

    /**
     * The dataprovider deployment is shared
     */
    protected final static Deployment dataproviderDeployment = new InProcessDeployment(
        new String[]{
            CONNECTOR_URL
         },
        new String[]{
            DTD_BASED_APPLICATION_URL,
            SCHEMA_BASED_APPLICATION_URL
        },
        LOG_DEPLOYMENT_DETAIL ? System.out : null,
        System.err
    );
    
    /**
     * Test specific data provider
     */
    protected Dataprovider_1ConnectionFactory dataprovider;

}
