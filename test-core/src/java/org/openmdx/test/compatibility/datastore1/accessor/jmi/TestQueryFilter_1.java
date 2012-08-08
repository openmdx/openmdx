/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestQueryFilter_1.java,v 1.3 2008/09/09 14:20:58 hburger Exp $
 * Description: Context Query Test
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 14:20:58 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.test.compatibility.datastore1.accessor.jmi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jmi.reflect.RefPackage;

import junit.framework.TestCase;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.base.application.deploy.RemoteDeployment;
import org.openmdx.base.cci.Provider;
import org.openmdx.base.jmi.BasePackage;
import org.openmdx.compatibility.base.application.cci.Dataprovider_1Deployment;
import org.openmdx.compatibility.base.application.cci.Model_1Deployment;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.compatibility.datastore1.cci.QueryFilter;
import org.openmdx.compatibility.datastore1.jmi.Datastore1Package;
import org.openmdx.kernel.application.deploy.cci.DeploymentProperties;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.test.clock1.jmi.Clock1Package;
import org.openmdx.test.clock1.query.SegmentQuery;

/**
 * Context Query Test
 */
public class TestQueryFilter_1 extends TestCase {

    /**
     * Constructor
     * 
     * @param name
     */
    public TestQueryFilter_1(
        String name
    ){
        super(name);
    }

    /**
     * 
     */
    protected synchronized void setUp(
    ) throws Exception {
        String message = ">>>> **** Start Test: " + this.getName();
        Path segmentPath = new Path(PROVIDER_PATH).add("segment").add(this.getName());
        System.out.println(message);
        AppLog.info(message, segmentPath);
        this.dataproviderConnection = (
                "Remote".equals(getName()) ? TestQueryFilter_1.remoteConnectionfactory : TestQueryFilter_1.inProcessConnectionfactory
        ).createConnection();
        RefPackage rootPkg = new RefRootPackage_1(
            new Manager_1(
                new Connection_1(
                    new Provider_1(
                        new RequestCollection(
                            new ServiceHeader(),
                            dataproviderConnection
                        ),
                        false
                    ),
                    false
                )
            ),
            null, // impls
            null, // context
            "cci",
            false
        );
        this.clock1 = (Clock1Package)rootPkg.refPackage(
            "org:openmdx:test:clock1"
        );
        this.datastore1 = (Datastore1Package)rootPkg.refPackage(
            "org:openmdx:compatibility:datastore1"
        );
        BasePackage base = (BasePackage) rootPkg.refPackage(
            "org:openmdx:base"
        );
        this.provider = base.getProvider().getProvider(
            new Path(PROVIDER_PATH)
        );
    }

    /**
     * 
     */
    protected void tearDown(
    ) {
        String message = "<<<< **** End Test: " + this.getName();
        System.out.println(message);
        AppLog.info(message);
        this.dataproviderConnection.close();
    }

    /**
     * 
     * @throws Exception
     */
    public void testQueryFilter(
    ) throws Exception {
        SegmentQuery query = this.clock1.createSegmentQuery();
        QueryFilter context = this.datastore1.getQueryFilter().createQueryFilter();
        context.setClause(
            "SELECT something FROM somewhere WHERE b0 = ? AND i0 = ? AND i1 = ? AND i2 = ?"
        );
        context.setBooleanParam(
            new boolean[]{true}
        );
        context.setIntegerParam(
            new int[]{1, 2, 3}
        );
        query.thereExistsContext().equalTo(context);
        assertTrue("Query instance of RefFilter_1_0", query instanceof RefFilter_1_0);
        Collection filterProperties = ((RefFilter_1_0)query).refGetFilterProperties();
        System.out.println(filterProperties);
        assertEquals("Filter property count", 9, filterProperties.size());
        Iterator i = filterProperties.iterator();
        assertEquals(
            "Instance of",
            new FilterProperty(
                Quantors.THERE_EXISTS,
                SystemAttributes.OBJECT_INSTANCE_OF,
                FilterOperators.IS_IN,
                "org:openmdx:test:clock1:Segment"
            ),
            i.next()
        );
        FilterProperty p = (FilterProperty) i.next();
        String namespace = p.name().substring(0, p.name().lastIndexOf(':') + 1);
        assertEquals(
            "Context object class",
            new FilterProperty(
                Quantors.PIGGY_BACK,
                namespace + SystemAttributes.OBJECT_CLASS,
                FilterOperators.PIGGY_BACK,
                "org:openmdx:compatibility:datastore1:QueryFilter"
            ),
            p
        );
        while(i.hasNext()) {
            p = (FilterProperty) i.next();
            assertEquals("Piggy back quantor", Quantors.PIGGY_BACK, p.quantor());
            assertEquals("Piggy back operator", FilterOperators.PIGGY_BACK, p.quantor());
            for(
                int j = 0;
                j < expectedAttributes.length;
                j++
            ){
                if(p.name().equals(namespace + expectedAttributes[j])) {
                    assertEquals(expectedAttributes[j], expectedValues[j], p.values());
                }
            }
        }
    }

    /**
     * 
     */
    private Provider provider;
    
    /**
     * 
     */
    private Clock1Package clock1;

    /**
     * 
     */
    private Datastore1Package datastore1;

    /**
     * 
     */
    private Dataprovider_1_1Connection dataproviderConnection;

    /**
     * 
     */
    static private final String PROVIDER_PATH = "xri:@openmdx:org.openmdx.test.clock1/provider/Java";

    /**
     * 
     */
    static private final String APPLICATION_URL = "file:src/ear/test-classloading.ear";

    /**
     * 
     */
    static private final String JNDI_NAME = "org/openmdx/test/supports/clock";

    /**
     * Define whether deployment details should logged to the console
     */
    final private static boolean LOG_DEPLOYMENT_DETAIL = false;

    private final static Deployment modelDeployment = new Model_1Deployment(
        new String[]{
            "org:openmdx:base",
            "org:w3c",
            "org:oasis-open",
            "org:openmdx:compatibility:view1",
            "org:openmdx:test:clock1"
        }
    );

    /**
     * 
     */
    protected final static Dataprovider_1ConnectionFactory inProcessConnectionfactory = new Dataprovider_1Deployment(
        new InProcessDeployment(
            null,
            APPLICATION_URL,
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
                System.err
        ),
        modelDeployment,
        JNDI_NAME
    );


    /**
     * 
     */
    protected final static Dataprovider_1ConnectionFactory remoteConnectionfactory = new Dataprovider_1Deployment(
        new RemoteDeployment(
            ArraysExtension.asMap(
                new String[]{
                    DeploymentProperties.APPLICATION_URLS,
                    "build.java.platform",
                    "build.target.platform"
                },
                new String[]{
                    APPLICATION_URL,
                    System.getProperty("build.java.platform"),
                    System.getProperty("build.target.platform")
                }
            ),
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
                System.err
        ),
        modelDeployment,
        JNDI_NAME
    );

    static final String[] expectedAttributes = new String[]{
        "dateParam",
        "decimalParam",
        "clause",
        "booleanParam",
        "integerParam",
        "dateTimeParam",
        "stringParam"
    };

    static final List[] expectedValues = new List[]{
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST,
        Collections.singletonList("SELECT something FROM somewhere WHERE b0 = ? AND i0 = ? AND i1 = ? AND i2 = ?"),
        Collections.singletonList(Boolean.TRUE),
        Arrays.asList(new Integer[]{new Integer(1), new Integer(2), new Integer(3)}),
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST
    };

}
