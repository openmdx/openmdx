/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestQueryFilter_1.java,v 1.2 2009/12/19 16:26:55 wfro Exp $
 * Description: Context Query Test
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/19 16:26:55 $
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
package test.openmdx.base.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefPackage;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.PiggyBackCondition;
import org.openmdx.base.query.Quantors;
import org.openmdx.compatibility.datastore1.jmi1.Datastore1Package;
import org.openmdx.compatibility.datastore1.jmi1.QueryFilter;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;

import test.openmdx.clock1.cci2.SegmentQuery;
import test.openmdx.clock1.jmi1.Clock1Package;

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

    protected static PersistenceManagerFactory entityManagerFactory;
    
    @Before
    public void setUp(
    ) throws Exception {
        PersistenceManager entityManager = entityManagerFactory.getPersistenceManager();
        this.provider = entityManager.getObjectById(
            Provider.class,
            PROVIDER_PATH
        );
        RefPackage rootPkg = this.provider.refOutermostPackage();
        this.clock1 = (Clock1Package)rootPkg.refPackage(
            "test:openmdx:clock1"
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
        assertTrue("Query instance of RefFilter_1_0", query instanceof RefQuery_1_0);
        Filter filter = ((RefQuery_1_0)query).refGetFilter();
        System.out.println(filter);
        assertEquals("Filter condition count", 9, filter.getCondition().length);
        assertEquals(
            "Instance of",
            new IsInCondition(
                Quantors.THERE_EXISTS,
                SystemAttributes.OBJECT_INSTANCE_OF,
                true,
                "test:openmdx:clock1:Segment"
            ),
            filter.getCondition(0)
        );
        Condition firstCondition = filter.getCondition(1);
        String namespace = firstCondition.getFeature().substring(
            0, 
            firstCondition.getFeature().lastIndexOf(':') + 1
        );
        assertEquals(
            "Context object class",
            new PiggyBackCondition(
                namespace + SystemAttributes.OBJECT_CLASS,
                "org:openmdx:compatibility:datastore1:QueryFilter"
            ),
            filter.getCondition(1)
        );
        for(int i = 2; i < filter.getCondition().length; i++) {
            Condition p = filter.getCondition(i);
            assertEquals("Piggy back quantor", Quantors.PIGGY_BACK, p.getQuantor());
            assertEquals("Piggy back operator", FilterOperators.PIGGY_BACK, p.getQuantor());
            for(
                int j = 0;
                j < expectedAttributes.length;
                j++
            ){
                if(p.getFeature().equals(namespace + expectedAttributes[j])) {
                    assertEquals(expectedAttributes[j], expectedValues[j], Arrays.asList(p.getValue()));
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
    static private final String PROVIDER_PATH = "xri://@openmdx*test.openmdx.clock1/provider/Java";

    @BeforeClass
    public static void deploy() throws NamingException{
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory("test-Clock-EntityManagerFactory");
    }

    @AfterClass
    public static void close(
    ) throws IOException{
        entityManagerFactory.close();
    }
}
