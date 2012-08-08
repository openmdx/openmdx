/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestJdbcDriverUrlProperties.java,v 1.4 2006/01/13 19:29:52 hburger Exp $
 * Description: JDBC URL Properties Test
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/13 19:29:52 $
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
package org.openmdx.test.kernel.application.deploy;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.compatibility.kernel.application.cci.Classes;

/**
 * Container Test
 */
public class TestJdbcDriverUrlProperties extends TestCase {

    /**
     * Constructs a test case with the given name.
     */ 
    public TestJdbcDriverUrlProperties(String name) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestJdbcDriverUrlProperties.class);
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        this.mapping = new Properties();
    }

    /**
     * 
     */
    protected void tearDown(
    ) throws Exception {
    }

    /**
     * 
     * @throws Exception
     */
    public void testJdbcUrlProperties(
    ) throws Exception {
        URL url = Classes.getApplicationResource(JDBC_DRIVER_URL_PROPERTIES);
        assertNotNull(JDBC_DRIVER_URL_PROPERTIES, url);
        this.mapping.load(url.openStream());	      	   
        assertEquals(
            "jdbc:microsoft:sqlserver://localhost:1433;databasename=openMDX_test;selectmethod=cursor", 
            "com.microsoft.jdbc.sqlserver.SQLServerDriver",
            findDriver("jdbc:microsoft:sqlserver://localhost:1433;databasename=openMDX_test;selectmethod=cursor")
        );
        assertEquals(
            "jdbc:sqlserver://localhost:1433;databaseName=openMDX_test;forwardReadOnlyMethod=serverCursor", 
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            findDriver("jdbc:sqlserver://localhost:1433;databaseName=openMDX_test;forwardReadOnlyMethod=serverCursor")
        );
        assertEquals(
            "jdbc:oracle:oci8:@OPENMDX_TEST", 
            "oracle.jdbc.OracleDriver",
            findDriver("jdbc:oracle:oci8:@OPENMDX_TEST")
        );
        assertEquals(
            "jdbc:oracle:thin:@OPENMDX_TEST", 
            "oracle.jdbc.OracleDriver",
            findDriver("jdbc:oracle:thin:@OPENMDX_TEST")
        );
        assertEquals(
            "jdbc:sapdb://localhost/OPENMDX_TEST", 
            "com.sap.dbtech.jdbc.DriverSapDB",
            findDriver("jdbc:sapdb://localhost/OPENMDX_TEST")
        );
        assertNull(
            "jdbc:any:other:uri://localhost:12345", 
            findDriver("jdbc:any:other:uri://localhost:12345")
        );
    }

    /**
     * 
     * @param url
     * @return
     */
    protected String findDriver(
        String url
    ){
        for(
            Iterator i = this.mapping.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry e = (Entry) i.next();
            if(url.startsWith((String) e.getValue())) return (String) e.getKey();
        }
        return null;
    }
    
    /**
     * 
     */
    private static final String JDBC_DRIVER_URL_PROPERTIES = "org/openmdx/kernel/application/deploy/jdbc-driver-url.properties";

    /**
     * 
     */
    protected Properties mapping;

}
