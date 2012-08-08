/*
 * ====================================================================
 * Name:        $Id: TestTransaction.java,v 1.26 2009/04/03 08:06:10 hburger Exp $
 * Description: Lightweight container transaction management test
 * Revision:    $Revision: 1.26 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/03 08:06:10 $
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.resource.ResourceException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.application.dataprovider.deployment.Deployment_1;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.ProductGroup;
import org.openmdx.test.app1.jmi1.Segment;

/**
 * Test the lightweight container's transaction management
 */
public class TestTransaction {

    /**
     * Entity Manager Factory
     */
    protected static Deployment_1 entityManagerFactory;

    /**
     * The resource adapter URIs
     */
    private final static String[] CONNECTOR_URIS = {
        //      "file:../test-core/src/connector/openmdx-2/sql-server-2000.rar",
        //      "file:../test-core/src/connector/openmdx-xa/sql-server-2000.rar",
        //      "file:../test-core/src/connector/openmdx-xa/sql-server-2005.rar",
        //      "file:../test-core/src/connector/openmdx-xa/oracle-8.rar",
        "file:../test-core/src/connector/openmdx-2/sql-server-2005.rar"
    };

    /**
     * The enterprice applucation URIs
     */
    private final static String[] APPLICATION_URIS = {
        "file:../test-core/src/ear/test-transaction.ear", // DTD_BASED_APPLICATION_URL
        "file:../test-core/src/ear/test-ejb_2_1.ear" // SCHEMA_BASED_APPLICATION_URL
    };

    /**
     * The models to be loaded
     */
    private final static String[] MODELS = {
        "org:openmdx:test:app1"
    };

    /**
     * Define whether deployment details should logged to the console
     */
    final private static boolean LOG_DEPLOYMENT_DETAILS = false;

    @Test
    public void ejb20Transaction() throws ResourceException{
        runTest("transactional_ejb-2.0", "test/openmdx/ejb20/EntityManagerFactory");
    }

    @Test
    public void ejb21Transaction() throws ResourceException{
        runTest("transactional_ejb-2.1", "test/openmdx/ejb21/EntityManagerFactory");
    }    

    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    protected void runTest(
        String name,
        String connectionFactoryName
    ) throws ResourceException{
        // TODO handle transactional flag
        PersistenceManager entityManager = entityManagerFactory.getEntityManager(connectionFactoryName);
        Transaction unitOfWork = entityManager.currentTransaction();
        Authority authority = entityManager.getObjectById(Authority.class, App1Package.AUTHORITY_XRI);
        Provider provider = authority.getProvider("ch:omex:test:junit");
        Segment segment = (Segment) provider.getSegment(name);
        if(segment != null) {            
            unitOfWork.begin();
            segment.refDelete();
            unitOfWork.commit();
        }
        unitOfWork.begin();
        App1Package app1 = (App1Package) provider.refOutermostPackage().refPackage("org:openmdx:test:app1");
        segment = app1.getSegment().createSegment();
        provider.addSegment(false, name, segment);
        unitOfWork.commit();
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
        } catch (JDOFatalDataStoreException jdoException) {
            BasicException exception = (BasicException) jdoException.getCause();
            exception.printStackTrace();
            assertEquals(
                "Unit fo work failure", 
                BasicException.Code.ROLLBACK,
                exception.getExceptionCode()
            );
        }
        unitOfWork.begin();
        ProductGroup productGroup = app1.getProductGroup().createProductGroup();
        productGroup.setDescription("Ok");
        segment.addProductGroup(false, "ok", productGroup);
        unitOfWork.commit();
    }

    /**
     * The JUnit principal
     */
    static protected final String[] PRINCIPALS = {"anonymous", "authenticated"};

    @BeforeClass
    public static void deploy(){
        entityManagerFactory = new Deployment_1(
            true, // IN_PROCESS_LIGHTWEIGHT_CONTAINER
            CONNECTOR_URIS,
            APPLICATION_URIS,
            LOG_DEPLOYMENT_DETAILS,
            null, // ENTITY_MANAGER_FACTORY_JNDI_NAME
            null, // GATEWAY_JNDI_NAME
            MODELS
        );
    }

    @AfterClass
    public static void close(
    ) throws IOException{
        entityManagerFactory.close();
    }

}
