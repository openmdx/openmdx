/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestQueryFilter_1.java,v 1.8 2009/05/18 13:06:57 hburger Exp $
 * Description: Context Query Test
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/18 13:06:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2009, OMEX AG, Switzerland
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefPackage;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.application.dataprovider.deployment.Deployment_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.compatibility.datastore1.jmi1.Datastore1Package;
import org.openmdx.compatibility.datastore1.jmi1.QueryFilter;
import org.openmdx.test.clock1.cci2.SegmentQuery;
import org.openmdx.test.clock1.jmi1.Clock1Package;

/**
 * Context Query Test
 */
public class TestQueryFilter_1 {

    static final String[] expectedAttributes = new String[]{
        "dateParam",
        "decimalParam",
        "clause",
        "booleanParam",
        "integerParam",
        "dateTimeParam",
        "stringParam"
    };

    static final List<?>[] expectedValues = {
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST,
        Collections.singletonList("SELECT something FROM somewhere WHERE b0 = ? AND i0 = ? AND i1 = ? AND i2 = ?"),
        Collections.singletonList(Boolean.TRUE),
        Arrays.asList(new Integer[]{new Integer(1), new Integer(2), new Integer(3)}),
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST
    };

    protected static Deployment_1 deployment;
    
    @Before
    public void setUp(
    ) throws Exception {
        PersistenceManager entityManager = deployment.getEntityManager();
        this.provider = entityManager.getObjectById(
            Provider.class,
            PROVIDER_PATH
        );
        RefPackage rootPkg = this.provider.refOutermostPackage();
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

    @Test
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
        Collection<?> filterProperties = ((RefFilter_1_0)query).refGetFilterProperties();
        System.out.println(filterProperties);
        assertEquals("Filter property count", 9, filterProperties.size());
        Iterator<?> i = filterProperties.iterator();
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
    static private final String PROVIDER_PATH = "xri:@openmdx:org.openmdx.test.clock1/provider/Java";

    @BeforeClass
    public static void deploy(){
        deployment = new Deployment_1(
            "xri://@openmdx*(+lightweight)*ENTERPRISE_APPLICATION_CONTAINER",
//          "xri://@openmdx*(+openejb)*ENTERPRISE_APPLICATION_CONTAINER",
            new String[]{}, // connectorURI 
            new String[]{"file:src/ear/test-classloading.ear"}, // applicationURI 
            true, // logDeploymentDetails
            "test/openmdx/clock1/EntityProviderFactory", // entityManagerFactoryURI 
            null, // gatewayURI
            new String[]{"org:openmdx:test:clock1"} // model
        );
    }

    @AfterClass
    public static void close(
    ) throws IOException{
        deployment.close();
    }
}
