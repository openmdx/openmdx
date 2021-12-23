/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test Query Extension
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_BOOLEAN_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_CLASS;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_CLAUSE;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DATETIME_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DATE_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DECIMAL_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_INTEGER_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_STRING_PARAM;

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
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;

import test.openmdx.clock1.cci2.SegmentQuery;
import test.openmdx.clock1.jmi1.Clock1Package;

/**
 * Context Query Test
 */
public class TestQueryExtension {

    static final String[] expectedAttributes = new String[]{
        QUERY_EXTENSION_DATE_PARAM,
        QUERY_EXTENSION_DECIMAL_PARAM,
        QUERY_EXTENSION_CLAUSE,
        QUERY_EXTENSION_BOOLEAN_PARAM,
        QUERY_EXTENSION_INTEGER_PARAM,
        QUERY_EXTENSION_DATETIME_PARAM,
        QUERY_EXTENSION_STRING_PARAM
    };

    static final List<?>[] expectedValues = {
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST,
        Collections.singletonList("SELECT something FROM somewhere WHERE b0 = ? AND i0 = ? AND i1 = ? AND i2 = ?"),
        Collections.singletonList(Boolean.TRUE),
        Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}),
        Collections.EMPTY_LIST,
        Collections.EMPTY_LIST
    };

    protected static PersistenceManagerFactory entityManagerFactory;
    
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
    static private final String PROVIDER_PATH = "xri://@openmdx*test.openmdx.clock1/provider/Java";

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
        QueryExtensionRecord extension = PersistenceHelper.newQueryExtension(query);
        extension.setClause(
            "SELECT something FROM somewhere WHERE b0 = ? AND i0 = ? AND i1 = ? AND i2 = ?"
        );
        extension.setBooleanParam(
            new boolean[]{true}
        );
        extension.setIntegerParam(
            new int[]{1, 2, 3}
        );
        assertTrue("Query instance of RefFilter_1_0", query instanceof RefQuery_1_0);
        QueryFilterRecord filter = ((RefQuery_1_0)query).refGetFilter();
        System.out.println(filter);
        assertEquals("Filter condition count", 1, filter.getCondition().size());
        assertEquals(
            "Instance of",
            new IsInstanceOfCondition(
                "test:openmdx:clock1:Segment"
            ),
            filter.getCondition().get(0)
        );
        List<FilterProperty> filterProperties = FilterProperty.getFilterProperties(filter);
        assertEquals(
            "Filter property count", 
            2 + expectedAttributes.length, 
            filterProperties.size()
        );
        FilterProperty firstCondition = filterProperties.get(1);
        String firstFeature = firstCondition.name();
        String namespace = firstFeature.substring(
            0, 
            firstFeature.lastIndexOf(':') + 1
        );
        assertEquals(
            "Context object class",
            new FilterProperty(
                Quantifier.codeOf(null),
                namespace + SystemAttributes.OBJECT_CLASS,
                ConditionType.codeOf(null),
                QUERY_EXTENSION_CLASS
            ),
            filterProperties.get(1)
        );
        int processedAttributes = 0;
        for(int i = 2; i < filterProperties.size(); i++) {
            FilterProperty p = filterProperties.get(i);
            assertNull("Piggy back quantifier", Quantifier.valueOf(p.quantor()));
            Expected: for(
                int j = 0;
                j < expectedAttributes.length;
                j++
            ){
                if(p.name().equals(namespace + expectedAttributes[j])) {
                    assertEquals(expectedAttributes[j], expectedValues[j], p.values());
                    processedAttributes++;
                    continue Expected;
                }
            }
        }
        assertEquals(
            "Expected attributes", 
            expectedAttributes.length, 
            processedAttributes
        );
    }

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
        System.out.println(UUIDs.newUUID());
    }
}
